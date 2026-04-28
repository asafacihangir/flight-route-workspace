# Use Case: Kullanıcı Login (Web)

**System:** Flight Route Web

**Primary Actor:** Admin veya Agency kullanıcısı

**Goal:** Web arayüzü üzerinden kimlik doğrulamasını tamamlayıp role uygun açılış sayfasına yönlendirilmek.

---

## Main Success Scenario

1. Kullanıcı tarayıcıdan uygulamayı açar ve oturumu yoksa otomatik olarak login sayfasına yönlendirilir.
2. Kullanıcı username ve password alanlarını doldurup "Login" butonuna basar.
3. Web uygulaması API'nin login endpoint'ine istek gönderir.
4. API başarılı cevap ile token ve kullanıcı rolünü (Admin / Agency) döner.
5. Web uygulaması token'ı client-side store'a kaydeder ve sonraki tüm isteklere `Authorization` header'ı olarak ekler.
6. Web uygulaması kullanıcıyı role göre uygun açılış sayfasına yönlendirir:
   - Admin → Locations veya Routes (uygulama varsayılanı)
   - Agency → Routes
7. Header ve sidebar role uygun şekilde render edilir.

---

## Extensions

### 2a. Username veya password boş:
- **2a1.** Form client-side validation hatası gösterir, istek gönderilmez.

### 3a. API HTTP 400 döner (validation hatası):
- **3a1.** Web genel bir "geçersiz istek" mesajı gösterir.

### 3b. API HTTP 401 döner (yanlış kullanıcı/şifre):
- **3b1.** Web genel bir "kullanıcı adı veya şifre hatalı" mesajı gösterir, hangi alanın hatalı olduğunu söylemez.

### 3c. Network hatası:
- **3c1.** Web "bağlantı hatası" mesajı gösterir ve tekrar denemesini ister.

### 5a. Kullanıcı login olduktan sonra token süresi dolarsa (sonraki bir API çağrısında 401 alınırsa):
- **5a1.** Web token'ı temizler ve kullanıcıyı tekrar login sayfasına yönlendirir.

### 6a. Agency kullanıcısı doğrudan `/locations` veya `/transportations` URL'ine erişmeye çalışır:
- **6a1.** Router yetki kontrolü yapar ve kullanıcıyı Routes sayfasına yönlendirir veya "yetkiniz yok" ekranı gösterir.

---

## Variations

### 4. Kullanıcı rolü:
- a) Admin – Locations, Transportations ve Routes menüleri görünür.
- b) Agency – Sadece Routes menüsü görünür.

### 5. Token saklama:
- a) Memory store (uygulama yenilenince login tekrar gerekir).
- b) Persistent store (browser refresh'i sonrası oturum korunur).
