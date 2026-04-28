# Use Case: Location Yönetimi (Web)

**System:** Flight Route Web

**Primary Actor:** Admin

**Goal:** Web arayüzü üzerinden location kayıtlarını listelemek, oluşturmak, güncellemek ve silmek.

---

## Main Success Scenario

1. Admin login olduktan sonra sidebar'daki "Locations" linkine tıklar.
2. Web uygulaması API'den location listesini çağırır ve sonucu tablo halinde gösterir.
3. Admin "Yeni Location" butonuna basarak form'u açar.
4. Admin name, country, city ve location code alanlarını doldurur.
5. Web uygulaması client-side validation yapar (zorunlu alanlar, location code formatı).
6. Web uygulaması API'nin POST endpoint'ine isteği gönderir.
7. API başarılı cevap döner; web tabloyu yeniler ve başarı mesajı gösterir.
8. Admin satırlardaki aksiyonlardan biriyle (görüntüle / düzenle / sil) işlem yapar.
9. Web ilgili API çağrısını yapar ve sonucu UI'a yansıtır.

---

## Extensions

### 1a. Kullanıcı Agency rolünde:
- **1a1.** Sidebar'da "Locations" linki görünmez.
- **1a2.** URL doğrudan girilirse router kullanıcıyı Routes sayfasına yönlendirir.

### 2a. API HTTP 401 döner:
- **2a1.** Token temizlenir ve kullanıcı login sayfasına yönlendirilir.

### 2b. API HTTP 403 döner:
- **2b1.** Web "bu işlem için yetkiniz yok" mesajı gösterir.

### 5a. Zorunlu alan eksik:
- **5a1.** Form ilgili alanın altında hata mesajı gösterir, istek gönderilmez.

### 6a. API duplicate location code hatası döner:
- **6a1.** Form, location code alanı için "bu kod zaten kullanılıyor" hatasını gösterir.

### 8a. Silinmek istenen location aktif transportation tarafından kullanılıyor (API hata döner):
- **8a1.** Web kullanıcıya silme işleminin neden reddedildiğini açıklayan bir mesaj gösterir.

### 8b. Düzenlenmek istenen location bulunamadı (HTTP 404):
- **8b1.** Web "kayıt bulunamadı" mesajı gösterir ve listeyi yeniler.

---

## Variations

### 4. Location tipi:
- a) Havalimanı – 3 karakter IATA code (örn. SAW, IST).
- b) Diğer (şehir merkezi vb.) – serbest format code (örn. CCIST).

### 8. İşlem tipi:
- a) Listeleme (tablo görünümü).
- b) Detay görüntüleme (modal / yan panel).
- c) Yeni kayıt (form modal).
- d) Güncelleme (form modal).
- e) Silme (onay diyaloğu).
