# Use Case: Transportation Yönetimi (CRUD)

**System:** Flight Route API

**Primary Actor:** Admin

**Goal:** İki location arasındaki taşıma seçeneklerini (FLIGHT, BUS, SUBWAY, UBER) tanımlamak ve yönetmek.

---

## Main Success Scenario

1. Admin geçerli Authorization header ile sisteme istek gönderir.
2. Admin yeni transportation için origin location, destination location, transportation type ve operating days bilgilerini gönderir.
3. Sistem origin ve destination location'ların var olduğunu doğrular.
4. Sistem transportation type değerinin geçerli olduğunu kontrol eder (FLIGHT, BUS, SUBWAY, UBER).
5. Sistem operating days dizisindeki değerlerin 1 ile 7 arasında olduğunu kontrol eder.
6. Sistem kaydı veritabanına ekler ve sonucu döner.
7. Admin liste, detay, update veya delete işlemlerini çağırabilir.

---

## Extensions

### 1a. Authorization header yok:
- **1a1.** Sistem HTTP 401 döner.

### 1b. Kullanıcı Agency rolünde:
- **1b1.** Sistem HTTP 403 döner.

### 3a. Origin veya destination location bulunamadı:
- **3a1.** Sistem HTTP 400 veya 404 döner ve hatayı bildirir.

### 3b. Origin ve destination aynı location:
- **3b1.** Sistem validation hatası döner.

### 4a. Transportation type geçersiz:
- **4a1.** Sistem geçerli değerleri içeren bir hata mesajı döner.

### 5a. Operating days dizisi boş veya geçersiz değer içeriyor:
- **5a1.** Sistem validation hatası döner.

### 7a. Update edilmek istenen transportation bulunamadı:
- **7a1.** Sistem HTTP 404 döner.

---

## Variations

### 2. Transportation type:
- a) FLIGHT
- b) BUS
- c) SUBWAY
- d) UBER

### 2. Operating days:
- a) Her gün: [1,2,3,4,5,6,7]
- b) Sadece hafta içi: [1,2,3,4,5]
- c) Sadece hafta sonu: [6,7]
- d) Belirli günler: örn. [1,3,5]