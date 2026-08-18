# Ulashish (Share) Funksiyasini Tuzatish Rejasi

Foydalanuvchi ta'kidlaganidek, siqilgan rasmni ulashish (Share) tugmasi ishlamayapti. Buning asosiy sababi Android xavfsizlik tizimi (`FileUriExposedException`) bo'lib, u `file://` ko'rinishidagi manzillarni boshqa ilovalarga berishni taqiqlaydi.

## Asosiy Muammolar
1.  **Xavfsiz Uri emas:** Ilova rasmning telefon xotirasidagi ichki yo'lini (`file:///...`) ulashishga harakat qilyapti. Android 7.0 dan boshlab bu taqiqlangan. Buning o'rniga `content://` (FileProvider) formatidan foydalanish kerak.
2.  **MimeType xatosi:** Ulashishda rasm turi har doim `image/jpeg` deb ko'rsatilgan. Agar rasm PNG yoki WebP bo'lsa, ba'zi ilovalar uni ocha olmaydi.

## Taklif Qilinayotgan O'zgarishlar

### Funksional Qatlam (Feature)

#### [MODIFY] [CompareFragment.kt](file:///C:/Users/User/AndroidStudioProjects/LumoraCompress/app/src/main/java/uz/kmax/compress/feature/compare/fragment/CompareFragment.kt)
- `shareImage` funksiyasini yangilaymiz.
- Agar Uri `file://` formatida bo'lsa, uni `FileProvider` orqali xavfsiz `content://` manziliga o'tkazamiz.
- Rasmning haqiqiy turini (MimeType) aniqlab, ulashish intentiga uzatamiz.

### Asosiy Qatlam (Core)

#### [MODIFY] [OutputWriterImpl.kt](file:///C:/Users/User/AndroidStudioProjects/LumoraCompress/app/src/main/java/uz/kmax/compress/core/compressor/writer/OutputWriterImpl.kt)
- `OutputResult` yaratishda ichki xotira uchun imkon qadar xavfsizroq manzil qaytarishni ko'rib chiqamiz.

## Tekshirish Rejasi

### Qo'lda Tekshirish
1.  Rasmni siqish.
2.  Solishtirish ekranida "Share" tugmasini bosish.
3.  Telegram, WhatsApp yoki boshqa ilovaga yuborib ko'rish.
4.  JPEG, PNG va WebP formatlarini alohida tekshirish.
