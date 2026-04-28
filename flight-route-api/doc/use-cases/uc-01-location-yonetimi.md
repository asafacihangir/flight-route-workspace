# Use Case: Location Yönetimi (CRUD)

**System:** Flight Route API

**Primary Actor:** Admin

**Goal:** Sistemdeki location kayıtlarını oluşturmak, görüntülemek, güncellemek ve silmek.

---

## Main Success Scenario

1. Admin sisteme login olur ve geçerli bir Authorization header ile istek gönderir.
2. Admin yeni bir location için name, country, city ve location code bilgilerini gönderir.
3. Sistem gönderilen bilgileri doğrular (zorunlu alanlar dolu, location code benzersiz).
4. Sistem location kaydını veritabanına ekler ve oluşturulan kaydı geri döner.
5. Admin daha sonra location listesini, tek bir location'ı, güncellemeyi veya silme işlemini çağırır.
6. Sistem ilgili işlemi yapar ve sonucu döner.

---

## Extensions

### 1a. Authorization header gönderilmedi:
- **1a1.** Sistem HTTP 401 döner ve işlem sonlanır.

### 1b. Kullanıcı Admin değil (Agency):
- **1b1.** Sistem HTTP 403 döner ve işlem sonlanır.

### 2a. Zorunlu alan eksik (örn. name veya location code yok):
- **2a1.** Sistem validation hatası ile HTTP 400 döner.
- **2a2.** Admin eksik bilgiyi tamamlayıp tekrar gönderir.

### 3a. Aynı location code zaten mevcut:
- **3a1.** Sistem benzersizlik hatası döner.
- **3a2.** Admin farklı bir location code ile tekrar dener.

### 5a. Güncellenmek veya silinmek istenen location bulunamadı:
- **5a1.** Sistem HTTP 404 döner.

### 5b. Silinmek istenen location aktif bir transportation tarafından kullanılıyor:
- **5b1.** Sistem hata döner ve silme işlemini reddeder.

---

## Variations

### 1. Location tipi:
- a) Havalimanı (3 karakter IATA code, örn. SAW, IST)
- b) Şehir merkezi veya başka bir nokta (serbest format code, örn. CCIST)

### 2. İşlem tipi:
- a) Create (POST)
- b) Read list (GET)
- c) Read single (GET by id)
- d) Update (PUT/PATCH)
- e) Delete (DELETE)