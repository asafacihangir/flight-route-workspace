# flight-route-workspace

Monorepo: `flight-route-api` (Spring Boot, Java 25) + `flight-route-web` (Vite + React, pnpm).
Deployment ve orkestrasyon katmanı bu workspace kökünde yaşar.

## Repo Yapısı

```
flight-route-workspace/
├── Taskfile.yml                  ← komut orkestrasyonu
├── deployment/
│   └── docker-compose/
│       ├── infra.yml             (mysql, redis)
│       ├── apps.yml              (api + web)
│       ├── docker-conf/
│       └── docker-mounted/
├── flight-route-api/
│   ├── Dockerfile
│   └── ...
└── flight-route-web/
    ├── Dockerfile
    ├── nginx.conf
    ├── .dockerignore
    └── ...
```

## Önkoşullar

Stack'i çalıştırmak için **sadece iki araç yeterli** — build ve runtime tamamen container içinde:

| Araç | Neden | Kurulum (macOS) |
|---|---|---|
| **Docker** + **Docker Compose v2** | Tüm servislerin build & runtime'ı | Docker Desktop |
| **Task** (go-task) | `Taskfile.yml` orkestrasyonu | `brew install go-task` |

> Diğer platformlar için: [Task kurulum](https://taskfile.dev/installation/).

`task start` her şeyi (mysql, redis, api, web) ayağa kaldırır — host'ta Java, Node, pnpm kurulu olmasına gerek yok. `flight-route-project-network` Docker network'ü ilk `up` çağrısında Compose tarafından otomatik oluşturulur.


## Servis Adresleri

| Servis | Host URL | Container içi |
|---|---|---|
| Web (nginx) | http://localhost:8081 | flight-route-web:80 |
| API (Spring) | http://localhost:8080 | flight-route-api:8080 |
| MySQL | localhost:33055 | flight-route-db:3306 |
| Redis | localhost:6379 | flight-route-cahce:6379 |

Tarayıcıdan `http://localhost:8081` → nginx → `/api/*` istekleri `flight-route-api:8080`'a proxy'lenir. **CORS yoktur** çünkü tarayıcı tek origin görür.

## Test Kullanıcıları

Geliştirme/test ortamında hazır seed edilmiş kullanıcılar:

| Rol | Kullanıcı adı | Parola |
|---|---|---|
| Admin | `systemadmin` | `Anadolu1071*` |
| Agency | `systemagency` | `Anadolu1071*` |

## Adım Adım Çalıştırma (geliştirme modu)

Geliştirme sırasında **infra'yı container'da, API ve web'i host'ta** çalıştırmak en pratiği — kod değişikliği anında reflect olur (Spring DevTools / Vite HMR), debugger bağlanır.

> **Mantık:** Infra (mysql + redis) sabit servis, Task ile container'da. API ve web aktif geliştirilen kod, host'ta kendi araçlarıyla.

### 1) Infra'yı başlat (zorunlu ön koşul — Task ile)

```bash
task start_infra
```

Bu mysql + redis container'larını ayağa kaldırır. Host'a açık portlar:

| Servis | Host portu |
|---|---|
| MySQL | `localhost:33055` |
| Redis | `localhost:6379` |

Sağlık kontrolü:

```bash
docker ps --filter "name=flight-route-db" --filter "name=flight-route-cahce"
```

### 2) Backend (host'ta — `flight-route-api/`)

**Önkoşullar:**

| Araç | Sürüm | Kurulum |
|---|---|---|
| **JDK** | Java **25** (Temurin) | `.sdkmanrc` ile yönetilir → `sdk env install` |
| **SDKMAN** | — | [sdkman.io](https://sdkman.io/install) |
| **Maven** | — | Gerekmez; repo'da `./mvnw` (wrapper) var |

**Çalıştırma:**

```bash
cd flight-route-api

# Java 25'i .sdkmanrc'den aktive et (her yeni shell'de)
source "$HOME/.sdkman/bin/sdkman-init.sh" && sdk env

# (İlk seferde) doğrula
java -version    # → Temurin 25.x

# DB/Redis host'tan erişilecek — container hostname'leri override et
export SPRING_DATASOURCE_URL="jdbc:mysql://localhost:33055/flight_route"
export SPRING_DATASOURCE_USERNAME=phoenixsqluser
export SPRING_DATASOURCE_PASSWORD=phoenixsqluser
export SPRING_DATA_REDIS_HOST=localhost
export SPRING_DATA_REDIS_PORT=6379
export SPRING_DATA_REDIS_PASSWORD='A;7=Uf/6<bp'
export CORS_ALLOWED_ORIGINS="http://localhost:3001"

# Çalıştır
./mvnw spring-boot:run
```

Doğrulama:

```bash
curl http://localhost:8080/actuator/health
```

> **Not:** Bu env var'lar `apps.yml`'deki container env'inin host versiyonu — container içi hostname (`flight-route-db`, `flight-route-cahce`) host'tan çözülmediği için `localhost:33055` ve `localhost:6379`'a override etmek gerekir.

### 3) Frontend (host'ta — `flight-route-web/`)

**Önkoşullar:**

| Araç | Sürüm | Kurulum (macOS) |
|---|---|---|
| **Node.js** | **20.x** (engines'te zorunlu) | `brew install node@20` veya `nvm install 20` |
| **pnpm** | **10.8.0** | `corepack enable && corepack prepare pnpm@10.8.0 --activate` |

**Çalıştırma:**

```bash
cd flight-route-web

# Bağımlılıklar (ilk seferde / lockfile değişince)
pnpm install

# Dev server
pnpm dev
```

Vite dev server `http://localhost:3001` adresinde açılır (otomatik tarayıcı).

> ⚠️ **Dikkat — proxy ayarı:** `vite.config.ts:38`'de `/api` proxy target'i `http://localhost:3000` olarak yazılı, ancak host'ta API `:8080`'de çalışır. Eğer frontend `/api/...` çağrıları yapıyorsa proxy'yi `http://localhost:8080`'e çevir veya `VITE_*` env ile API URL'ini geçersiz kıl.

### 4) Durdurma

- **Web:** `pnpm dev` çalıştığı terminalde `Ctrl+C`
- **API:** `./mvnw spring-boot:run` çalıştığı terminalde `Ctrl+C`
- **Infra:** `task stop_infra`

---

## Sık Kullanılan Komutlar

```bash
# Tüm imajları build et (api + web)
task build

# Sadece bir tanesi
task build_flight_route_api
task build_flight_route_web

# Tüm stack'i ayağa kaldır (infra + apps)
task start

# Durdur
task stop

# Yeniden başlat
task restart

# Sadece infra (mysql, redis)
task start_infra
task stop_infra

# Sadece app'ler (api + web)
task start_apps
task stop_apps

# Servis bazında (infra'yı otomatik başlatır)
task start:api    # infra + api
task start:web    # infra + api + web

# API testleri
task test
```


