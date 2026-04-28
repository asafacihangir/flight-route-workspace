## Route Nedir? Basitçe Anlatımı

Bir **route**, seni başlangıç noktandan varış noktana ulaştıran bir **yolculuk planıdır**. Ama bu planın çok net kuralları var.

### Temel Kural: Her Route'da Mutlaka Bir Flight Olmalı

Bir route'un kalbi **flight**'tır. Yani uçak olmadan route olmaz.

### Bir Route En Fazla 3 Parçadan Oluşur

Bir route'u 3 aşamalı bir yolculuk gibi düşün:

```
[1. Aşama]  →  [2. Aşama]  →  [3. Aşama]
   ↓              ↓              ↓
Before flight   FLIGHT      After flight
  transfer    (zorunlu)      transfer
(opsiyonel)                 (opsiyonel)
```

- **1. Aşama (Before flight transfer):** Evden havalimanına gitmen. Opsiyonel. FLIGHT olamaz (yani BUS, SUBWAY veya UBER olmalı).
- **2. Aşama (FLIGHT):** Asıl uçuş. **Zorunlu.** Mutlaka olmalı.
- **3. Aşama (After flight transfer):** Vardığın havalimanından son durağına gitmen. Opsiyonel. FLIGHT olamaz.

### Gerçek Hayat Örneği

Diyelim ki **Taksim Meydanı**'ndan **Wembley Stadyumu**'na gitmek istiyorsun:

```
Taksim → (BUS) → İstanbul Havalimanı → (FLIGHT) → Heathrow → (UBER) → Wembley
         1.Aşama                       2.Aşama                3.Aşama
```

Bu geçerli bir route'tur. ✔

### "Connected" Ne Demek?

İki transportation'ın birbirine bağlı sayılması için, **birinin bittiği yerde diğeri başlamalı**.

- Bus seni **İstanbul Havalimanı**'na bırakıyor → Flight **İstanbul Havalimanı**'ndan kalkıyor ✔ (Bağlı)
- Füniküler seni **Taksim**'e bırakıyor → Flight **İstanbul Havalimanı**'ndan kalkıyor ✖ (Bağlı değil, çünkü aralarında boşluk var)

### "Available" Ne Demek?

Bir transportation'ın seçtiğin günde çalışıp çalışmadığıdır.

- Flight'ın operating days'i [1, 3, 7] (Pazartesi, Çarşamba, Pazar)
- Sen 12 Mart 2025 (Çarşamba) seçersen → Available ✔
- Sen 11 Mart 2025 (Salı) seçersen → Available değil ✖

> **Önemli:** Route'taki **her parça** o gün çalışıyor olmalı. Sadece flight değil, bus ve uber da.

### Neler Geçersizdir?

- 3'ten fazla transportation olmaz
- Hiç flight olmazsa olmaz
- Birden fazla flight olamaz
- Birden fazla before transfer veya after transfer olamaz

### Geçerli vs Geçersiz Hızlı Karşılaştırma

| Route               | Durum | Sebep                           |
| ------------------- | ----- | ------------------------------- |
| FLIGHT              | ✔     | Tek başına flight yeter         |
| UBER → FLIGHT       | ✔     | Önce transfer, sonra uçak       |
| FLIGHT → BUS        | ✔     | Uçak, sonra transfer            |
| UBER → FLIGHT → BUS | ✔     | Tam 3 aşama                     |
| UBER → BUS → FLIGHT | ✖     | Before flight'ta 2 transfer var |
| UBER → BUS          | ✖     | Flight yok                      |
| FLIGHT → FLIGHT     | ✖     | İki flight olmaz                |

### Aynı Yol İçin Birden Fazla Route Olabilir

Taksim'den Wembley'e gitmenin birçok kombinasyonu olabilir (bus yerine subway, uber yerine bus gibi). Sistem bunların **hepsini ayrı ayrı** sana göstermeli, böylece sen seçim yapabilirsin.

Özetle: **Route = (opsiyonel transfer) + FLIGHT + (opsiyonel transfer)**, hepsi birbirine bağlı ve hepsi o gün çalışıyor olmalı.