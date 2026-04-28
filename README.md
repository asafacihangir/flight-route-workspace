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
├── openspec/                     ← cross-cutting değişiklikler
├── flight-route-api/
│   ├── Dockerfile
│   ├── openspec/                 (sadece API değişiklikleri)
│   └── ...
└── flight-route-web/
    ├── Dockerfile
    ├── nginx.conf
    ├── .dockerignore
    ├── openspec/                 (sadece Web değişiklikleri)
    └── ...
```

## İki Geliştirici Workflow'u

| Workflow | Komut | Ne zaman |
|---|---|---|
| **Frontend dev (host)** | `cd flight-route-web && pnpm dev` | Aktif frontend geliştirme — HMR, hot-reload |
| **Integration / smoke (container)** | `task start` | Full-stack davranış testi, API geliştirici hızlı erişim |

Web container'da hot-reload **yoktur** — bilinçli karar. Build edilmiş halini nginx ile serve eder.

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

# API testleri
task test
```

## Servis Adresleri

| Servis | Host URL | Container içi |
|---|---|---|
| Web (nginx) | http://localhost:8081 | flight-route-web:80 |
| API (Spring) | http://localhost:8080 | flight-route-api:8080 |
| MySQL | localhost:33055 | flight-route-db:3306 |
| Redis | localhost:6379 | flight-route-cahce:6379 |

Tarayıcıdan `http://localhost:8081` → nginx → `/api/*` istekleri `flight-route-api:8080`'a proxy'lenir. **CORS yoktur** çünkü tarayıcı tek origin görür.

## Network

Tüm servisler `flight-route-project-network` adlı external Docker network'ünde konuşur. İlk kullanımda `docker network create flight-route-project-network` gerekebilir.

## Java Setup (API)

API tarafında çalışmadan önce:

```bash
source "$HOME/.sdkman/bin/sdkman-init.sh" && sdk env
```

Bu komut `flight-route-api/.sdkmanrc` dosyasındaki Java sürümünü aktif eder.

## OpenSpec

| Değişiklik tipi | Konum |
|---|---|
| Sadece API | `flight-route-api/openspec/` |
| Sadece Web | `flight-route-web/openspec/` |
| Cross-cutting / deployment | `openspec/` (workspace kökü) |
