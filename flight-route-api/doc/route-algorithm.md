# `findRoutes` Algoritması — Sade Anlatım

Bu method, kullanıcıya **origin** noktasından **destination** noktasına gitmek için olası rota listelerini bulur. Her rotanın ortasında mutlaka bir **flight** vardır; öncesinde ve sonrasında en fazla birer transfer (örneğin otobüs, taksi) olabilir.

## Adım Adım Mantık

**1. Tarihe göre filtreleme**
- Verilen `date` değerinin haftanın hangi günü olduğu bulunur (`dayOfWeek`).
- Tüm transportation listesinden sadece o gün çalışanlar (`operatingDays` içinde o günü olanlar) seçilir. Buna `available` denir.

**2. Her flight'ı merkez kabul et**
- `available` listesindeki her transportation tek tek gezilir.
- Eğer transportation bir flight değilse atlanır. Çünkü her rotanın merkezinde bir flight olmak zorundadır.

**3. Flight öncesi ve sonrası için transfer seçenekleri bul**
- **Before:** Kullanıcının `originId`'sinden flight'ın kalkış noktasına gidebilecek transferler (`transferOptions` ile).
- **After:** Flight'ın iniş noktasından kullanıcının `destinationId`'sine gidebilecek transferler.

**4. Tüm kombinasyonları birleştir**
- Her `before × after` ikilisi için yeni bir rota oluşturulur:
  `before + flight + after`
- Bu rota `result` listesine eklenir.

**5. Sonuç**
- Tüm flight'lar için bulunmuş geçerli rotaların listesi geri döner.

## `transferOptions` Yardımcı Method'u

İki nokta arasında transfer seçenekleri üretir:

- Eğer `fromId` ile `toId` aynı ise → boş liste döner (`List.of(List.of())`). Yani transfer gerekmez ama "bir seçenek var" anlamına gelir; böylece kombinasyon döngüsü kırılmaz.
- Aynı değilse → flight olmayan, `fromId`'den başlayıp `toId`'de biten transportation'lar bulunur. Her biri tek elemanlı bir liste olarak döner.

## Özet

Algoritma şunu der: *"Her flight için, ona ulaşmanın yollarını ve ondan sonra hedefe gitmenin yollarını bul, hepsini çaprazlayarak rota üret."* Sonuçta rotalar **en fazla 3 segment** içerir: `[transfer?] → flight → [transfer?]`.