# Giriş

Projenin amacı, havacılık endüstrisi için bir sistem geliştirmektir. Bu sistem, kullanıcıların uçuş rezervasyonu yaparken daha iyi bir deneyim yaşamalarını sağlamak amacıyla A noktasından B noktasına olası tüm rotaları hesaplayabilmelidir.

Gereksinimler aşağıdaki gibidir:

- Veri saklamak için bir database başlatılmalıdır. Herhangi bir database (PostgreSQL, MySQL, MSSQL veya H2) kullanılabilir.
- Bir Spring Boot Java REST API geliştirilmelidir.
- Java sınıflarını ve database tablolarını eşlemek için ORM framework olarak Hibernate framework kullanılmalıdır.
- Swagger desteği sağlanmalı ve Swagger UI erişilebilir olmalıdır.

# Database Gereksinimleri

Proje kapsamında, aşağıdaki entity'ler database'de tutulmalıdır:

## Locations

Uçuş arama sırasında "from" ve "to" dropdown kutularında görüntülenecek lokasyonları temsil eder. Locations entity'leri aşağıdaki alanları içermelidir:

- Name
- Country
- City
- Location code
  - Havalimanları için location code, 3 karakter uzunluğundaki IATA kodlarıdır. Örnek: Sabiha Gökçen Havalimanı için SAW, İstanbul Havalimanı için IST. (Belirli bir havalimanının IATA kodunu aramak için: https://www.iata.org/en/publications/directories/code-search/)
  - Diğer lokasyonlar için herhangi bir kodlama deseni kullanılabilir (örnek: İstanbul Şehir Merkezi için CCIST)

## Transportations

Bir lokasyondan diğerine ulaşımları temsil eder. Transportation entity'leri aşağıdaki alanlara sahip olmalıdır:

- Origin Location
- Destination Location
- Transportation Type: FLIGHT, BUS, SUBWAY, UBER
- Operating Days: İlgili ulaşımın haftanın hangi günlerinde çalıştığını temsil eden integer dizisi. Dizinin değeri [1, 3, 5, 6] ise, ilgili ulaşımın yalnızca Pazartesi, Çarşamba, Cuma ve Cumartesi günlerinde aktif olduğu anlamına gelir.

# API Gereksinimleri

Bir Spring Boot Java REST API geliştirilmeli ve database entegrasyonu için Hibernate ile birleştirilmelidir. Rest controller'lar aracılığıyla, API aşağıdakiler için endpoint sağlamalıdır:

- Locations için CRUD operasyonları
- Transportations için CRUD operasyonları
- Seçilen tarihte bir lokasyondan diğerine geçerli tüm rotaların döndürülmesi.

## Route Tanımı

- Bir route, kullanılabilir ve birbirine bağlı transportation'ların bir dizisi olarak tanımlanabilir. Her route 3 bağlı transportation içerebilir:
  - **Before flight transfer**: Opsiyonel. Transportation type FLIGHT dışında olmalıdır.
  - **Flight**: Zorunlu. Transportation type "FLIGHT" olmalıdır.
  - **After flight transfer**: Opsiyonel. Transportation type FLIGHT dışında olmalıdır.
- İki transportation'ın "connected" sayılabilmesi için, önceki transportation'ın varış lokasyonu, sonraki transportation'ın kalkış lokasyonuyla eşleşmelidir.

Örnekler:

- Taksim Meydanı'ndan İstanbul Havalimanı'na bir bus yolculuğu ve İstanbul Havalimanı'ndan Londra Heathrow Havalimanı'na bir flight, connected transportation'lardır. ✔
- Kabataş İskelesi'nden Taksim Meydanı'na füniküler ve İstanbul Havalimanı'ndan Londra Heathrow Havalimanı'na bir flight, connected transportation değildir. ✖

- Bir transportation'ın "available" sayılabilmesi için, seçilen tarih, transportation'ın operating days'i içinde olmalıdır.

Örnek:

İstanbul Havalimanı'ndan Londra Heathrow Havalimanı'na bir flight'ın operating days değeri [1, 3, 7]'dir. "date" parametresi şu şekilde seçilirse:

- "12 Mart 2025" ise, yukarıda bahsedilen flight available'dır. ✔
- "11 Mart 2025" ise, yukarıda bahsedilen transportation available değildir. Çünkü 11 Mart Salı günüdür ve flight'ın operating days'i içinde yer almaz. ✖

## Kısıtlamalar

Bir connected transportation dizisi aşağıdaki durumlarda geçerli bir route olarak kabul edilemez:

- Origin'den destination'a 3'ten fazla transportation varsa.
- Aralarında flight yoksa.
- Aralarında birden fazla flight varsa.
- Aralarında birden fazla before flight transfer varsa.
- Aralarında birden fazla after flight transfer varsa.
- İlgili route'da available olmayan HERHANGİ bir transportation varsa. Lütfen unutmayın ki bu kriter yalnızca flight'lara değil, before flight ve after flight transfer'lara da uygulanır.

Geçerli route örnekleri (tüm transportation'ların haftanın her günü çalıştığı varsayılarak):

- UBER ➡ FLIGHT ➡ BUS ✔
- FLIGHT ➡ BUS ✔
- UBER ➡ FLIGHT ✔
- FLIGHT ✔

Geçersiz route örnekleri:

- UBER ➡ BUS ➡ FLIGHT ✖ (birden fazla before flight transfer)
- UBER ➡ BUS ✖ (flight yok)
- UBER ➡ FLIGHT ➡ FLIGHT ✖ (birden fazla flight)
- FLIGHT ➡ FLIGHT ✖ (birden fazla flight)
- FLIGHT ➡ SUBWAY ➡ UBER ✖ (birden fazla after flight transfer)

"Taksim Meydanı -> Wembley Stadyumu" için örnek bir geçerli route şu şekilde olabilir:

- Taksim Meydanı'ndan İstanbul Havalimanı'na bir bus yolculuğu
- İstanbul Havalimanı'ndan Londra Heathrow Havalimanı'na bir flight
- Londra Heathrow Havalimanı'ndan Wembley Stadyumu'na bir Uber yolculuğu

> **ÖNEMLİ**: Aynı route için birden fazla transfer seçeneği varsa, hepsi ayrı route'lar gibi döndürülmelidir.

Örnek:

- Taksim Meydanı (UBER) ➡ İstanbul Havalimanı ➡ Londra Heathrow Havalimanı (BUS) ➡ Wembley Stadyumu
- Taksim Meydanı (UBER) ➡ İstanbul Havalimanı ➡ Londra Heathrow Havalimanı (UBER) ➡ Wembley Stadyumu
- Taksim Meydanı (SUBWAY) ➡ İstanbul Havalimanı ➡ Londra Heathrow Havalimanı (BUS) ➡ Wembley Stadyumu
- Taksim Meydanı (SUBWAY) ➡ İstanbul Havalimanı ➡ Londra Heathrow Havalimanı (UBER) ➡ Wembley Stadyumu
- Taksim Meydanı (BUS) ➡ İstanbul Sabiha Gökçen Havalimanı ➡ Londra Heathrow Havalimanı (BUS) ➡ Wembley Stadyumu
- Taksim Meydanı (BUS) ➡ İstanbul Sabiha Gökçen Havalimanı ➡ Londra Heathrow Havalimanı (UBER) ➡ Wembley Stadyumu

# Non-Functional Gereksinimler

- Yüksek yük alabilecek endpoint'ler için cache desteği eklenmelidir. Herhangi bir caching tool kullanılabilir (Redis, Couchbase vb.).
- Service sınıfları için Spring Boot veya JUnit testleri eklenmelidir.
- Proje Dockerize edilmelidir. Hem docker-compose hem de Dockerfile sağlanmalıdır.
- Login ve authentication akışı (Registration akışının uygulanması atlanabilir. Kullanıcılar bunun yerine database'e manuel olarak eklenebilir).

2 farklı kullanıcı tipi olmalıdır:

- **Admins**: Tüm endpoint'leri çağırabilirler.
- **Agencies**: Yalnızca route listeleme endpoint'ini çağırabilirler. Agencies, location ve transportation endpoint'lerini çağırmaya çalışırsa, HTTP status code 403 almalıdırlar.

Eğer istekte authentication sağlanmamışsa (HTTP Authorization header), HTTP status code 401 döndürülmelidir.
