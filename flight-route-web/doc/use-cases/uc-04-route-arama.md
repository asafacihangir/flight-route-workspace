# Use Case: Route Arama ve Listeleme (Web)

**System:** Flight Route Web

**Primary Actor:** Agency (Admin de bu sayfayı kullanabilir)

**Goal:** Web arayüzü üzerinden seçilen tarihte iki location arasındaki tüm geçerli route'ları görmek.

---

## Main Success Scenario

1. Kullanıcı sidebar'daki "Routes" linkine tıklar (Agency için varsayılan açılış sayfası).
2. Web uygulaması arka planda location listesini çağırır ve dropdown'ları doldurur.
3. Kullanıcı:
   - Origin location'ı dropdown'dan seçer.
   - Destination location'ı dropdown'dan seçer.
   - Trip date'i date picker'dan seçer.
4. Kullanıcı "Search" butonuna basar.
5. Web client-side validation yapar (üç alan da dolu, origin ≠ destination).
6. Web API'nin route arama endpoint'ine origin, destination ve date parametreleriyle istek atar.
7. API geçerli route listesini döner.
8. Web her route için özet bir satır oluşturur (örn. "Via İstanbul Airport (IST)") ve "Available Routes" listesinde gösterir.
9. Kullanıcı bir route'a tıkladığında web yan panelde route detayını gösterir:
   - Her leg için from, to ve transportation type yazılı şekilde.
   - Flight leg'i diğer leg'lerden görsel olarak ayrılmış şekilde.
10. Kullanıcı paneli kapatabilir veya başka bir route'a tıklayarak panel içeriğini değiştirebilir.

---

## Extensions

### 2a. Location listesi çağrısı 401 döner:
- **2a1.** Token temizlenir ve kullanıcı login sayfasına yönlendirilir.

### 5a. Zorunlu alan eksik:
- **5a1.** Web validation mesajı gösterir, istek gönderilmez.

### 5b. Origin ve destination aynı location:
- **5b1.** Web "origin ve destination aynı olamaz" uyarısı gösterir.

### 6a. API HTTP 400 döner (örn. tarih formatı hatalı):
- **6a1.** Web kullanıcıya geçersiz parametre uyarısı gösterir.

### 6b. API HTTP 404 döner (origin veya destination bulunamadı):
- **6b1.** Web "seçilen lokasyon bulunamadı, listeyi yenileyin" mesajı gösterir.

### 7a. API boş liste döner:
- **7a1.** Web "bu tarih için uygun route bulunamadı" boş-durum mesajını gösterir.

### 8a. Aynı (origin, destination, date) için arama daha önce yapıldı:
- **8a1.** Web client-side cache'lenmiş sonucu kullanabilir, ek API çağrısı yapmaz.

### 9a. Kullanıcı bir route'a tıklamadan başka bir arama yapar:
- **9a1.** Mevcut detay paneli kapatılır ve liste yeni sonuçlarla güncellenir.

---

## Variations

### 8. Route şekli (UI gösterimi):
- a) Tek leg: FLIGHT.
- b) İki leg: before transfer ➡ FLIGHT.
- c) İki leg: FLIGHT ➡ after transfer.
- d) Üç leg: before transfer ➡ FLIGHT ➡ after transfer.

### 9. Detay görünümü:
- a) Yan panel (varsayılan).
- b) Modal / overlay (alternatif).

### 9. Nice-to-Have – harita görünümü:
- a) Detay panelinde leg'lerin haritada çizgi/ok ile gösterimi.
- b) Sadece metin tabanlı leg listesi (varsayılan).

### 1. Kullanıcı tipi:
- a) Agency – yalnızca bu sayfayı kullanır.
- b) Admin – bu sayfayı diğer admin sayfalarıyla birlikte kullanır.
