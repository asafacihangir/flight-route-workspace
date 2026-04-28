# Giriş

Projenin amacı, havacılık sektörü için Flight Route platformunun front-end (web) deneyimini hayata geçirmektir. Web uygulaması; acentelerin seçilen bir tarihte iki konum arasındaki tüm geçerli seyahat rotalarını keşfetmesine ve adminlerin rota keşfini besleyen konum ve ulaşım kataloğunu yönetmesine imkân tanıyan responsive bir Single Page Application (SPA)'dır.

Front-end, `flight-route-api` tarafından sunulan Spring Boot REST API'yi tüketir ve yeteneklerini role duyarlı bir UI üzerinden sunar.

Gereksinimler şunlardır:

- Responsive bir SPA, **React** (TypeScript ile) kullanılarak hayata geçirilmelidir.
- Uygulamanın üstte bir **header** ve solda bir **side bar menu** içermesi gereklidir.
- Uygulama; authentication, location, transportation ve route endpoint'leri için mevcut REST API ile entegre olmalıdır.
- Uygulama, sunucu tarafı kontrollerine ek olarak istemci tarafında da role-based access (Admin / Agency) zorunluluğunu uygulamalıdır.
- Tüm endpoint çağrıları, login akışından elde edilen JWT/Authorization header'ını içermelidir.
- UI; desktop, tablet ve mobile breakpoint'lerinde kullanılabilir kalmalıdır (responsive layout).

# Navigation Gereksinimleri

Side bar 3 navigation girdisi içermelidir:

## Locations

- **Yalnızca Admin'lere** görünür.
- Location kataloğu üzerinde CRUD işlemleri sağlar (name, country, city, location code).
- Agency kullanıcıları için gizlenir / render edilmez.

## Transportations

- **Yalnızca Admin'lere** görünür.
- Transportation kayıtları üzerinde CRUD işlemleri sağlar (origin, destination, type, operating days).
- Agency kullanıcıları için gizlenir / render edilmez.

## Routes

- **Tüm authenticated kullanıcı tipleri** için görünür (Admin ve Agency).
- Agency kullanıcıları için login sonrası varsayılan landing page'tir.
- Aşağıda açıklanan rota arama deneyimini sağlar.

# Sayfa Gereksinimleri

## Login Page

- Kullanıcının username ve password girdiği public sayfa (header yok, sidebar yok).
- Başarılı girişte access token istemci tarafında saklanır ve kullanıcı role'üne göre uygun landing page'e yönlendirilir.
- Hata durumunda; username veya password'ün yanlış olduğunu açığa vurmadan generic bir hata mesajı gösterilir.

## Locations Page (Yalnızca Admin)

- Mevcut location'ları paginated/searchable bir tabloda listeler.
- Admin'in yapabilecekleri:
  - Bir form üzerinden yeni location oluşturma (Name, Country, City, Location Code).
  - Tek bir location'ın detayını görüntüleme.
  - Mevcut bir location'ı güncelleme.
  - Bir location'ı silme.
- API'den gelen validation hatalarını yüzeye çıkarır (ör. mükerrer location code, eksik zorunlu alanlar).
- Location, aktif bir transportation tarafından referans edildiği için API tarafından reddedildiğinde silmeye izin vermez.

## Transportations Page (Yalnızca Admin)

- Mevcut transportation'ları; origin, destination, transportation type ve operating days bilgilerini insan-okur biçimde gösteren bir tabloda listeler.
- Admin'in yapabilecekleri:
  - Mevcut location'lardan origin ve destination seçerek, transportation type (FLIGHT, BUS, SUBWAY, UBER) belirleyerek ve bir veya daha fazla operating day (1–7) seçerek yeni bir transportation oluşturma.
  - Mevcut bir transportation'ı güncelleme.
  - Bir transportation'ı silme.
- İstek gönderilmeden önce origin ile destination'ın aynı olmadığını ve en az bir operating day'in seçildiğini istemci tarafında doğrular.

## Routes Page (Tüm authenticated kullanıcılar)

Bu, uygulamanın kullanıcıya yönelik merkezi sayfasıdır.

- Kullanıcı şunları seçer:
  - Bir dropdown / searchable select'ten **Origin** location.
  - Bir dropdown / searchable select'ten **Destination** location.
  - Bir date picker'dan **Trip date**.
- Bir **Search** butonu route listeleme çağrısını tetikler.
- Sonuç paneli, API tarafından döndürülen tüm geçerli rotaların listesini gösterir.
  - Her rota özeti flight leg'i öne çıkarır (ör. "Via İstanbul Airport (IST)").
  - UI'da desteklenen rota şekilleri API tanımına uyar: `FLIGHT`, `before ➡ FLIGHT`, `FLIGHT ➡ after`, `before ➡ FLIGHT ➡ after`.
- Bir rotaya tıklandığında, rotanın leg-by-leg tüm detayını gösteren bir **side panel** (veya eşdeğer overlay) açılır:
  - Her leg, from/to konumlarını ve transportation type'ı gösterir.
  - Flight leg, before/after transfer leg'lerinden görsel olarak ayrıştırılır.
- API verilen seçim için geçerli rota döndürmediğinde bir empty state gösterilir.

## Harita Üzerinde Route Detayları (Nice-to-Have)

- Route detail panel açıkken, seçili rotanın leg'leri ek olarak bir harita üzerinde görselleştirilebilir (konumlar arası çizgiler/oklar).
- Bu opsiyoneldir ve case study'de belirtilen aynı tasarım serbestliğini izler.

# Authentication & Authorization Gereksinimleri

- Front-end; API'nin authentication endpoint'ini kullanan bir login akışı uygulamalı ve elde edilen token'ı istemci tarafında güvenli biçimde saklamalıdır.
- Korunan her endpoint isteği `Authorization` header'ını içermelidir.
- UI, API'nin authorization yanıtlarına tepki vermelidir:
  - **HTTP 401** → token geçersiz / süresi dolmuş → kullanıcı login page'e yönlendirilir.
  - **HTTP 403** → kullanıcıya "not authorized" mesajı gösterilir ve kısıtlı sayfaya gitmesine izin verilmez.
- Sidebar ve routing katmanı; Agency kullanıcıları için Locations ve Transportations linklerini gizlemelidir; Agency kullanıcısının bu route'lara deep-linking ile erişmesi durumunda Routes page'e yönlendirilmeli (veya not-authorized state render edilmeli).
- Admin'ler tüm sayfalara erişebilir.

# Non-Functional Gereksinimler

- **Framework:** Vite ile build edilen React + TypeScript SPA.
- **Responsiveness:** Layout, yaygın breakpoint'lere (mobile, tablet, desktop) uyum sağlamalıdır. Sidebar küçük ekranlarda drawer'a dönüşür.
- **Internationalization:** Codebase mevcut bir i18n katmanı içerir (`src/locales`); kullanıcıya yönelik string'ler hard-code edilmek yerine bu katmandan geçmelidir.
- **State management:** API state'i mevcut client store'da tutulur; route arama sonuçları gereksiz re-fetch'leri önlemek için (origin, destination, date) tuple'ı bazında istemci tarafında cache'lenebilir.
- **Error handling:** Tüm API error response'ları kullanıcı-dostu bir mesaj olarak yüzeye çıkar; ham stack trace veya backend error payload'ları kullanıcıya gösterilmez.
- **Form validation:** Form'lar; API'nin kısıtlarını yansıtan istemci tarafı validation uygular (zorunlu alanlar, location code formatı, geçerli operating days vb.).
- **Accessibility:** Form'lar ve interactive control'ler keyboard-navigable'dır ve semantic markup kullanır.
- **Build & Deploy:** Proje static bundle olarak build edilebilir ve static site olarak deploy edilebilir (Vercel konfigürasyonu mevcuttur).