# Use Case: Kullanıcı Login ve Authentication

**System:** Flight Route API

**Primary Actor:** Admin veya Agency kullanıcısı

**Goal:** API endpoint'lerini çağırabilmek için kimlik doğrulaması yapmak ve geçerli bir token elde etmek.

---

## Main Success Scenario

1. Kullanıcı login endpoint'ine username ve password bilgilerini gönderir.
2. Sistem gönderilen bilgileri veritabanındaki kullanıcı kaydı ile karşılaştırır.
3. Sistem kullanıcıya ait rolü (Admin veya Agency) belirler.
4. Sistem bir authentication token üretir ve cevap olarak döner.
5. Kullanıcı sonraki isteklerde bu token'ı HTTP Authorization header'ında gönderir.
6. Sistem her istekte token'ı doğrular ve role göre yetki kontrolü yapar.

---

## Extensions

### 1a. Username veya password eksik:
- **1a1.** Sistem HTTP 400 validation hatası döner.

### 2a. Kullanıcı bulunamadı veya password hatalı:
- **2a1.** Sistem HTTP 401 döner ve hangi alanın hatalı olduğunu belirtmez (güvenlik için).

### 5a. İstek Authorization header'ı içermiyor:
- **5a1.** Sistem HTTP 401 döner.

### 6a. Token geçersiz veya süresi dolmuş:
- **6a1.** Sistem HTTP 401 döner ve kullanıcıdan tekrar login olmasını ister.

### 6b. Agency kullanıcısı location veya transportation endpoint'ine istek atıyor:
- **6b1.** Sistem HTTP 403 döner.

### 6c. Agency kullanıcısı route arama endpoint'ine istek atıyor:
- **6c1.** Sistem isteği kabul eder ve normal akış devam eder.

---

## Variations

### 3. Kullanıcı rolü:
- a) Admin – tüm endpoint'leri çağırabilir
- b) Agency – sadece route listeleme endpoint'ini çağırabilir

### 1. Kullanıcı oluşturma yöntemi:
- a) Manuel olarak veritabanına eklenir (registration flow yoktur)