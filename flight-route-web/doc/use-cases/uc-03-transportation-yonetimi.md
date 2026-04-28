# Use Case: Transportation Yönetimi (Web)

**System:** Flight Route Web

**Primary Actor:** Admin

**Goal:** Web arayüzü üzerinden iki location arasındaki taşıma seçeneklerini (FLIGHT, BUS, SUBWAY, UBER) yönetmek.

---

## Main Success Scenario

1. Admin login olduktan sonra sidebar'daki "Transportations" linkine tıklar.
2. Web uygulaması API'den transportation listesini çağırır ve tabloda gösterir (origin, destination, type, operating days).
3. Admin "Yeni Transportation" butonuna basar ve form açılır.
4. Admin form üzerinde:
   - Origin location'ı dropdown'dan seçer.
   - Destination location'ı dropdown'dan seçer.
   - Transportation type'ı seçer (FLIGHT, BUS, SUBWAY, UBER).
   - Operating days için 1–7 arası bir veya birden fazla gün işaretler.
5. Web client-side validation yapar (origin ≠ destination, en az bir operating day seçili).
6. Web API'ye POST isteği gönderir.
7. API başarılı cevap döner; web listeyi yeniler ve başarı mesajı gösterir.
8. Admin satırlardaki aksiyonlarla (düzenle / sil) işlem yapar.

---

## Extensions

### 1a. Kullanıcı Agency rolünde:
- **1a1.** Sidebar'da "Transportations" linki görünmez.
- **1a2.** URL doğrudan girilirse router yetki kontrolü yapar ve Routes sayfasına yönlendirir.

### 2a. API HTTP 401 döner:
- **2a1.** Token temizlenir, kullanıcı login sayfasına yönlendirilir.

### 2b. API HTTP 403 döner:
- **2b1.** Web "yetkiniz yok" mesajı gösterir.

### 4a. Location dropdown'ları boş (henüz hiç location yok):
- **4a1.** Web kullanıcıya önce Locations sayfasından kayıt eklemesi gerektiğini söyler.

### 5a. Origin ve destination aynı location:
- **5a1.** Form validation hatası gösterir, istek gönderilmez.

### 5b. Hiç operating day seçilmedi:
- **5b1.** Form validation hatası gösterir, istek gönderilmez.

### 6a. API validation hatası döner (örn. transportation type geçersiz):
- **6a1.** Web ilgili alanın altında hata mesajını gösterir.

### 8a. Düzenlenmek istenen transportation bulunamadı (HTTP 404):
- **8a1.** Web "kayıt bulunamadı" mesajı gösterir ve listeyi yeniler.

---

## Variations

### 4. Transportation type:
- a) FLIGHT
- b) BUS
- c) SUBWAY
- d) UBER

### 4. Operating days seçimi (UI olarak):
- a) Hafta günleri için checkbox grubu (Pzt..Paz).
- b) "Her gün" hızlı seçim butonu.
- c) "Sadece hafta içi" hızlı seçim.
- d) "Sadece hafta sonu" hızlı seçim.

### 8. İşlem tipi:
- a) Listeleme.
- b) Yeni kayıt.
- c) Güncelleme.
- d) Silme (onay diyaloğu).
