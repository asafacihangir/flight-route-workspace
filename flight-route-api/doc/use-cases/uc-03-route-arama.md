# Use Case: İki Location Arasındaki Geçerli Route'ları Listeleme

**System:** Flight Route API

**Primary Actor:** Agency (Admin de bu endpoint'i çağırabilir)

**Goal:** Seçilen tarihte, başlangıç location'ından hedef location'a giden tüm geçerli route'ları görmek.

---

## Main Success Scenario

1. Agency geçerli Authorization header ile route arama isteği gönderir.
2. İstek; origin location, destination location ve date parametrelerini içerir.
3. Sistem origin ve destination location'ların veritabanında var olduğunu doğrular.
4. Sistem verilen tarihten haftanın gününü hesaplar (1=Pazartesi ... 7=Pazar).
5. Sistem tüm transportation kayıtları arasından bağlantılı diziler oluşturur:
   - Opsiyonel before flight transfer (FLIGHT olmayan)
   - Zorunlu bir FLIGHT
   - Opsiyonel after flight transfer (FLIGHT olmayan)
6. Sistem sadece operating days listesi seçilen güne uyan transportation'ları kullanır.
7. Sistem geçerli route kurallarını uygulayarak listeyi filtreler.
8. Sistem bulunan tüm geçerli route'ları cevap olarak döner.
9. Cevap, yüksek yük altında performans için cache'lenir.

---

## Extensions

### 1a. Authorization header yok:
- **1a1.** Sistem HTTP 401 döner.

### 2a. Zorunlu parametre eksik (origin, destination veya date):
- **2a1.** Sistem HTTP 400 ile validation hatası döner.

### 2b. Date formatı hatalı:
- **2b1.** Sistem beklenen format bilgisini içeren hata döner.

### 3a. Origin veya destination location bulunamadı:
- **3a1.** Sistem HTTP 404 döner.

### 5a. Hiçbir bağlantılı transportation dizisi bulunamadı:
- **5a1.** Sistem boş bir liste döner (HTTP 200).

### 6a. Aday route içinde herhangi bir transportation o gün operate etmiyor:
- **6a1.** O route geçersiz sayılır ve sonuca eklenmez.

### 7a. Aday dizide birden fazla FLIGHT var:
- **7a1.** Route geçersiz sayılır.

### 7b. Aday dizide hiç FLIGHT yok:
- **7b1.** Route geçersiz sayılır.

### 7c. Aday dizide birden fazla before flight veya after flight transfer var:
- **7c1.** Route geçersiz sayılır.

### 7d. Aday dizi 3'ten fazla transportation içeriyor:
- **7d1.** Route geçersiz sayılır.

### 9a. İstek daha önce cache'lenmiş bir aramayla aynı:
- **9a1.** Sistem veritabanına gitmeden cache'den cevap döner.

---

## Variations

### 5. Geçerli route şekilleri:
- a) FLIGHT (sadece uçuş)
- b) before transfer ➡ FLIGHT
- c) FLIGHT ➡ after transfer
- d) before transfer ➡ FLIGHT ➡ after transfer

### 5. Aynı origin–destination çifti için birden fazla seçenek:
- a) Farklı before transfer seçenekleri (UBER, BUS, SUBWAY)
- b) Farklı flight seçenekleri (örn. IST veya SAW üzerinden)
- c) Farklı after transfer seçenekleri
- d) Hepsinin kombinasyonu ayrı ayrı route olarak döner