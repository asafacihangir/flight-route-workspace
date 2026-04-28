# Flight Route — Mülakat Hazırlığı

> Take-home projenin teknik review'ı + olası mülakat soruları + canlı kodlama egzersizleri.
> Dosya yollarına `flight-route-api/...` veya `flight-route-web/...` ön ekiyle bakın.

---

## 1. Proje Özeti (30 saniyede anlatım)

> "Spring Boot 3 (Java 25) backend ve React 19 + Vite frontend'den oluşan bir monorepo. Acentelerin lokasyonlar ve ulaşım bağlantıları üzerinden geçerli rotaları sorgulamasına izin veren bir uçuş rota sorgulama sistemi. Backend feature-sliced (location/transportation/security/config) yapıda; rota arama algoritması RouteFinder içinde izole. Geçerli rota = en fazla 1 FLIGHT + öncesinde ve/veya sonrasında en fazla birer non-flight transfer (toplam ≤ 3 leg). Kimlik doğrulama JWT (HS256) + Spring OAuth2 Resource Server. MySQL + Flyway migration, Caffeine cache. Frontend shadcn/ui + TanStack Query + Zustand + i18next (TR/EN). Docker Compose + nginx reverse proxy."

---

## 2. Teknik Review Özeti

### Güçlü Yanlar
- **Domain-driven invariants**: `LocationEntity`/`TransportationEntity` constructor'da normalizasyon (kod uppercase, day range 1..7, distinct endpoints).
- **DTO disiplini**: `Request → Cmd → Entity → VM` ayrımı her feature'da uygulanmış.
- **N+1 koruması**: `@EntityGraph(attributePaths={"origin","destination"})` + Hibernate `Statistics` ile assert eden `RouteServiceN1IT`.
- **Cache eviction integration test**: Trivial olmayan `RouteServiceCacheIT` cache hit + eviction'ı doğruluyor.
- **Modern stack**: Java 25, Spring Boot Resource Server JWT (jjwt değil), `ProblemDetail` (RFC 7807), `open-in-view=false`, `@EnableMethodSecurity`.
- **Güvenlik detayları**: Login hatası için generic mesaj (user enumeration koruması), CSP/Referrer/Permissions header'ları set edilmiş, stateless session.
- **Deployment**: Multi-stage Dockerfile, nginx `/api` → `flight-route-api:8080` reverse proxy → tarayıcıda CORS sorunu yok.

### Zayıf Yanlar / Riskler
| Risk | Yer | Etki |
|------|-----|------|
| MSW worker production bundle'da çalışıyor | `flight-route-web/src/main.tsx` | Mock istekler canlıda da çalışabilir |
| Vite dev proxy yanlış porta gidiyor (`:3000` vs API `:8080`) | `vite.config.ts:38` | Lokal dev'de istekler düşer |
| `@PreAuthorize` görünmüyor; AGENCY de Location/Transportation yazabiliyor olabilir | controller'lar | Yetki ayrımı çalışmıyor olabilir |
| JWT secret + MySQL root password + BCrypt hash repo'da | `application.yml`, `infra.yml`, `V5/V7` migration | Secret leak |
| `@CacheEvict(allEntries=true)` Caffeine üstünde, multi-instance'ta divergence | `TransportationService` | Yatay ölçek bozulur (Redis hazır ama kullanılmıyor) |
| `findAllWithLocations()` her uncached sorguda **tüm** transportation'ları çekiyor | `RouteService` | Veri büyüyünce O(N) memory load |
| `operating_days` VARCHAR(20) → SQL'de filtrelenemez, JVM'de filtreleniyor | `OperatingDaysConverter` | "Çarşamba günü uçuşları" diye query yazılamaz |
| Time zone yok, `LocalDate.getDayOfWeek()` server-local | `RouteFinder` | Global uçuşlar için yanlış gün |
| Zaman/duration modeli yok, sadece reachability | domain | Gerçekçi connection (örneğin "varış öncesi 2 saatlik transfer") modellenemiyor |
| Refresh token / logout / revocation yok | `JwtService` | Stateless JWT'nin klasik kısıtı |
| `/api/auth/login` rate limit yok | `SecurityConfig` | Brute force riski |
| `getReferenceById` null check (ulaşılamaz dal) | `LocationService.getLocationRef` | Dead code |
| Frontend ~175 dosyalık template fork; çoğu kullanılmıyor | `flight-route-web/src` | Review yüzeyi şişkin |
| Testler gerçek MySQL'e bağlı (Testcontainers değil) | `BaseIT` | CI'da kırılganlık |

---

## 3. Mülakat Soruları & Cevapları

### A. Domain & Algoritma

**S1: "Geçerli rota" tanımını açıklar mısın?**
> En fazla 3 leg'den oluşur, **tam olarak 1 FLIGHT** içerir. FLIGHT'tan önce en fazla bir non-flight transfer (BUS/SUBWAY/UBER), FLIGHT'tan sonra en fazla bir non-flight transfer olabilir. Yani şekiller: `[F]`, `[X,F]`, `[F,Y]`, `[X,F,Y]`. İki uçuşlu connection veya peş peşe iki bus desteklenmiyor.

**S2: `RouteFinder.findRoutes` algoritmasının zaman karmaşıklığı?**
> Generic graph search değil, **flight-pivot enumeration**. Tüm flights'ları o güne filtreliyoruz: `O(F)`. Her flight için origin'e gelen ve destination'dan giden non-flight transfer adaylarını `byOrigin` map'inden çekiyoruz: `O(B)` ortalama. Toplam `O(F × B²)` worst case ama her flight için before/after listeleri tipik olarak küçük. Memory: tüm transportation'lar yüklendiği için `O(N)`.

**S3: Algoritmayı genel n-leg routing'e nasıl genişletirsin?**
> Graph kurup BFS yapardım: node = `(locationId, flightUsed: boolean)`. Edge traversal'da flight kullanıldıysa flag true olur ve bir daha flight edge'i kullanılamaz. Day-of-week filter edge predicate'inde uygulanır. Maksimum derinlik kısıtıyla cycle önlenir. Ya da Dijkstra: weight = duration/maliyet/transfer sayısı.

**S4: `operating_days` neden `Set<Integer>` ve VARCHAR(20)? Trade-off?**
> Sade tutmak için `@ElementCollection` + ayrı `transportation_operating_days` join table'ından kaçınılmış. Ama maliyeti: **SQL filter imkânsız**, "Çarşamba uçuşları" sorgusu için `LIKE '%3%'` saçmalığı yerine JVM'de filtreliyoruz. Doğru çözüm: bitmask `INT` (Pzt=1, Sal=2, ...=64) tutmak — `(operating_days & ?) <> 0` ile sorgulanabilir. Ya da join table.

### B. Performance & Caching

**S5: Cache key neden `originId + "_" + destinationId + "_" + date`?**
> Spring SpEL ile composite key. `_` separator olmazsa `1+23=123` ile `12+3=123` çakışırdı. Daha güvenli: `T(java.util.List).of(#originId, #destinationId, #date)` ile gerçek composite key.

**S6: 3 instance behind-LB'de `@CacheEvict(allEntries=true)` ne kırılır?**
> Caffeine in-process. Instance A bir transportation update'inde kendi cache'ini boşaltır, B ve C'nin cache'i stale kalır. Çözümler:
> 1. **Redis-backed cache**: `RedisCacheManager` swap → eviction tüm instance'ları etkiler.
> 2. **Mesaj bus**: write event'i Kafka/Redis pub-sub'a publish et, instance'lar dinleyip lokal cache'lerini boşaltsın.
> 3. **TTL'i kısalt** — eventual consistency kabul ediyorsan.

**S7: `findAllWithLocations()` her sorguda tüm transportation'ları yüklüyor — daha iyisi?**
> İlk filtreyi DB'de yap: `WHERE origin_id = :originId OR origin_id IN (...transfer endpoints...) OR destination_id IN (...)` veya date filter (operating_days bitmask olsa). Cache hit oranı yüksekse mevcut yaklaşım pragmatik ama N büyürken patlar.

**S8: N+1 problemini nasıl tespit ettiniz?**
> `RouteServiceN1IT` Hibernate `Statistics`'i enable edip `prepareStatementCount`'u test sonunda assert ediyor (`<= 3`). `@EntityGraph(attributePaths={"origin","destination"})` ile tek join sorgusunda location'lar çekiliyor.

### C. Güvenlik

**S9: JWT secret rotasyonu nasıl olur?**
> HS256 simetrik — secret değişirse mevcut tokenlar geçersiz olur. Production'da:
> - **Key ID (`kid`) header** ile çoklu key support, eski key validate-only modda tutulur.
> - Veya RS256/ES256'ya geç, public key rotation kolaylaşır.
> - Refresh token + kısa-ömürlü access token kombinasyonu zorunlu.

**S10: Login endpoint'inde `BadCredentialsException` ile `UsernameNotFoundException` neden aynı response döner?**
> **User enumeration** saldırısı önlemek. Saldırgan farklı kullanıcı adları deneyip "user yok" vs "şifre yanlış" cevabını ayırt edemesin. `AuthExceptionHandler.handleAuthFailure` ikisini de "Invalid username or password" çevirir.

**S11: AGENCY rolü Location oluşturabiliyor mu?**
> `SecurityConfig`'de path-based kısıt sadece `permitAll`/`hasAuthority(ADMIN)` actuator için. `/api/locations` mutation'ları sadece `authenticated()` — yani AGENCY de POST/PUT/DELETE atabilir. **Bu bir gap.** Çözüm: `@PreAuthorize("hasAuthority('ROLE_ADMIN')")` controller method'larında veya path matcher'da `.requestMatchers(POST, "/api/locations").hasAuthority(ADMIN)`.

**S12: CSRF neden disable?**
> Stateless JWT API + cookie tabanlı auth değil → CSRF vector yok. Token Authorization header'da gönderildiği için third-party site automatically attach edemez. SPA'lar için tipik konfigürasyon.

**S13: MSW production bundle'da çalışıyor — neden risk?**
> `main.tsx` her ortamda `worker.start()` çağırıyor. Her ne kadar `onUnhandledRequest: "bypass"` mock'lanmamış istekleri geçirse de, mock handler'lar yüklü olduğu için yanlışlıkla canlı API'yi mock'layabilir. Build-time `import.meta.env.DEV` guard zorunlu.

### D. Persistence

**S14: Flyway baseline-on-migrate ne işe yarar?**
> Existing DB'ye Flyway sonradan eklendiyse, ilk çalıştırmada `flyway_schema_history` tablosunu oluştururken mevcut state'i baseline kabul eder. Greenfield için zorunlu değil ama defensive setting.

**S15: `ddl-auto=validate` neden tercih edildi?**
> Hibernate sadece schema'yı validate eder, DDL üretmez. Tüm schema değişiklikleri Flyway migration'dan geçer → audit'lenebilir, code-review'a girer, prod'da kontrollü uygulanır. `update`/`create-drop` prod'da asla.

**S16: `open-in-view=false` neden önemli?**
> Default `true`: HTTP request boyunca Hibernate session açık → controller layer'dan lazy collection erişimi gizlice DB sorgusu tetikler → görünmez N+1. `false` yaparak lazy access'i service layer'a hapsediyoruz, performance bug'ları erken yakalanıyor.

### E. Frontend

**S17: TanStack Query'den ne kazanıyorsun?**
> Server state cache, automatic refetch on window focus, optimistic updates, mutation invalidation. `useTransportationsQuery` ile component re-render'da yeniden fetch yok; `useCreateTransportation` mutation success'inde `queryClient.invalidateQueries(["transportations"])` ile liste otomatik refresh.

**S18: Zustand vs Redux?**
> Zustand boilerplate-free, provider gerektirmez, hook-first. Redux Toolkit daha opinionated (slice, RTK Query). Burada server state TanStack Query'de, client state (sidebar, theme, locale) Zustand'da — clean separation.

**S19: i18next setup'ı nasıl?**
> `src/locales/en_US/`, `tr_TR/` altında namespace'lere bölünmüş JSON. `t("sys.transportation.type.FLIGHT")` ile dot-path lookup. Route component'ları `useTranslation()` hook'u ile locale değişimini reactive izler.

### F. Mimari & Tasarım

**S20: Feature-sliced vs layered package — neden?**
> Layered (`controller/service/repository`) küçük projelerde işe yarar; büyürken cross-cutting. Feature-sliced (`location/`, `transportation/`) bounded context'i bir araya tutar, refactor lokalize. Cross-feature bağımlılık `web` (DTO-only) üzerinden gider, internal entity sızmaz.

**S21: Controller'lar neden package-private?**
> Spring component scan public olmasını gerektirmez. Package-private bırakmak controller'ın yanlışlıkla başka bir feature'dan import edilmesini engeller — dependency direction korunur.

**S22: `Cmd` record ile `Request` DTO neden ayrı?**
> `Request` web concern'i (validation, JSON binding). `Cmd` service concern'i (sanitized, normalized input). Controller `Request → Cmd` map'ler, service entity'si DB'ye yazar. Service'i web framework'ten bağımsız test edebiliyorsun.

### G. DevOps

**S23: Dockerfile multi-stage'in faydası?**
> Builder stage Maven + JDK içerir (~600MB). Runtime stage sadece JRE + JAR (~250MB). Build-time bağımlılıklar runtime image'a sızmaz → attack surface ↓, image size ↓, pull süresi ↓.

**S24: Nginx neden API'nin önünde değil de SPA önünde?**
> SPA static asset serving + `/api` reverse proxy. Tarayıcı her şeyi aynı origin'den görüyor → CORS gerekmiyor. Ek olarak gzip, cache header'ları, `try_files $uri /index.html` ile SPA fallback nginx'te halloluyor.

---

## 4. Canlı Mülakatta Sorulabilecek Ekstra Feature'lar

Aşağıdakiler bu projenin "spec'inde olabilirdi ama yok" feature'lar — büyük ihtimalle **canlı kodlama** sorusu olarak karşına gelir.

### 4.1 Pagination / Sorting (kolay)
`/api/locations?page=0&size=20&sort=code,asc`
- Spring `Pageable` injection, `Page<LocationEntity>` repository, `PageVM<LocationVM>` wrapper.
- Frontend: TanStack Query `useInfiniteQuery` veya cursor pagination.

### 4.2 Free-text Search (orta)
`/api/locations/search?q=istanbul`
- JPA `Specification` veya `@Query` ile `LIKE '%?%' OR city LIKE ... OR country LIKE ...`.
- Daha gelişmişi: MySQL `FULLTEXT` index, ya da Postgres `tsvector`.

### 4.3 Bulk Import CSV (orta)
`POST /api/transportations/import` multipart CSV.
- `MultipartFile` parse, satır-bazlı validation, hatalı satırları report eden response.
- Transactional batch insert, `EntityManager.flush()/clear()` her 50 row'da.

### 4.4 Çoklu Uçuş ve Time-Window Connection (zor — en muhtemel)
- Şu an domain'de departure/arrival time yok. Eklemen istenebilir: `LocalTime departureTime, durationMinutes`.
- Geçerli connection: önceki bacak varış + 30 dk ≤ sonraki kalkış.
- Algoritma: Dijkstra; weight = total duration veya transfer count.

### 4.5 Generic Graph BFS (zor)
"Algoritmayı n-leg routing'e genelle, max 5 leg, herhangi bir karışım."
- BFS state = `(currentLocationId, depthSoFar, visitedSet, flightCountSoFar)`.
- Cycle önlemek için `visited`, max depth ile early prune.

### 4.6 Refresh Token + Logout (orta)
- Access token (15 dk) + refresh token (7 gün, DB'de stored).
- `/api/auth/refresh` endpoint, refresh token rotate.
- Logout = refresh token DB'den sil + access blacklist (Redis TTL = exp - now).

### 4.7 Audit Log (orta)
- Spring Data Auditing: `@CreatedBy`, `@CreatedDate`, `@LastModifiedBy`, `@LastModifiedDate` BaseEntity'de.
- `AuditorAware<String>` SecurityContext'ten username çekecek bean.
- (V4 migration'da audit kolonları zaten var — sadece annotation eklenmesi yeterli olabilir.)

### 4.8 Rate Limiting (orta)
- Login endpoint için. **Bucket4j** + Redis backend.
- IP bazlı 5 deneme/dakika; aşılırsa 429.

### 4.9 OpenAPI / Swagger (kolay)
- `springdoc-openapi-starter-webmvc-ui` dependency, `/swagger-ui.html`.
- `@Operation`, `@ApiResponse` annotation'ları.

### 4.10 Soft Delete (kolay)
- `@SQLDelete(sql = "UPDATE locations SET deleted = true WHERE id = ?")`.
- `@Where(clause = "deleted = false")`.
- Hibernate 6'da `@SoftDelete` annotation'ı var.

### 4.11 Optimistic Locking (kolay)
- `@Version` field LocationEntity/TransportationEntity'ye.
- Concurrent update'te `OptimisticLockException` → 409 Conflict response.

### 4.12 Internationalization on Backend (orta)
- Validation mesajları için `messages_tr.properties`, `messages_en.properties`.
- `Accept-Language` header'a göre `LocaleResolver`.
- ProblemDetail'de localized error.

### 4.13 Event-Driven Cache Invalidation (zor)
- Transportation update → Spring `ApplicationEventPublisher.publishEvent(new TransportationChangedEvent(...))`.
- Listener'lar: cache evict, search index update, audit log.
- Multi-instance için event'i Redis pub-sub'a yay.

### 4.14 Frontend Map Integration (orta)
- React Leaflet veya MapLibre + lat/lng kolonlarını LocationEntity'ye ekle.
- Rotaları harita üzerinde çiz, leg'leri renk-kodla.

### 4.15 Test Coverage (kolay ama beklenen)
- JaCoCo plugin, %80 hedef.
- Eksik: AuthService unit, TransportationService mutation IT, Controller'da `@PreAuthorize` IT.

---

## 5. Cevaplaman Beklenen Tipik Sorular

1. **"Daha çok zaman olsa neyi farklı yapardın?"**
   > Operating_days bitmask, Redis-backed cache, controller'larda `@PreAuthorize`, MSW'i prod build'den çıkarma, refresh token, OpenAPI doc, Testcontainers, time-window connection modellemesi.

2. **"Bu projede en çok neye gurur duyuyorsun?"**
   > N+1 koruması ve onu enforce eden integration test (`RouteServiceN1IT`). Cache eviction'ı doğrulayan IT. DTO disiplini (Request → Cmd → VM).

3. **"Bug hunt yap"**
   > MSW production'da; Vite proxy yanlış port; AGENCY yetkisi ayrımı kodda yok; cache key separator bilinçli mi; `getReferenceById` null check unreachable.

4. **"Production'a ne eksik?"**
   > Secret management (Vault/SSM), structured logging + Loki/ELK, distributed tracing (Micrometer + Tempo), Testcontainers, CI pipeline, blue/green deploy, DB connection pool tuning, JVM flags (G1, container-aware sizing), graceful shutdown.

---

## 6. "Whiteboard" Hazırlığı — Mutlaka Bilmen Gerekenler

- **DayOfWeek mapping**: `LocalDate.of(2026,4,28).getDayOfWeek().getValue()` → 2 (Salı). `MONDAY=1, ..., SUNDAY=7`.
- **JWT yapısı**: header.payload.signature, base64url encode, HMAC-SHA256 imza.
- **Spring Security filter chain**: `UsernamePasswordAuthenticationFilter` (form), `BearerTokenAuthenticationFilter` (resource server), `ExceptionTranslationFilter`, `FilterSecurityInterceptor`.
- **Hibernate fetch types**: `LAZY` proxy + session-bound, `EAGER` join. `@EntityGraph` query-time override.
- **`@Transactional` propagation**: `REQUIRED` (default), `REQUIRES_NEW`, `SUPPORTS`. `readOnly=true` Hibernate flush mode'u manual yapar.
- **Cache abstraction**: `@Cacheable` (read), `@CachePut` (write-through), `@CacheEvict` (invalidate). SpEL key.

---

## 7. Hızlı Cheat-Sheet (Mülakat Anında)

```
Algorithm  : flight-pivot enumeration, O(F × B²)
Cache      : Caffeine, key=origin_dest_date, evict-all on write
Auth       : JWT HS256, OAuth2 Resource Server, stateless
Roles      : ADMIN (yönetim), AGENCY (sorgu)
DB         : MySQL + Flyway (validate), no open-in-view
Frontend   : React 19 + Vite + shadcn/ui + TanStack Query + Zustand + i18next
Deploy     : Multi-stage Docker, nginx reverse proxy /api → :8080
Tests      : Mockito + WebTestClient + Hibernate Statistics N+1 IT
Risks      : MSW in prod, AGENCY write access, secret in repo, Caffeine multi-instance
```

Başarılar.
