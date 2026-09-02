package com.example.data.model

enum class AppCurrency(
    val code: String,
    val symbol: String,
    val symbolAr: String,
    val displayName: String,
    val defaultRateFromUsd: Double
) {
    DZD("DZD", "DA", "د.ج", "Algerian Dinar (DZD)", 134.5),
    USD("USD", "$", "$", "US Dollar (USD)", 1.0),
    EUR("EUR", "€", "€", "Euro (EUR)", 0.92),
    GBP("GBP", "£", "£", "British Pound (GBP)", 0.79),
    CAD("CAD", "CA$", "د.ك", "Canadian Dollar (CAD)", 1.36),
    SAR("SAR", "SAR", "ر.س", "Saudi Riyal (SAR)", 3.75),
    AED("AED", "AED", "د.إ", "UAE Dirham (AED)", 3.67),
    EGP("EGP", "EGP", "ج.م", "Egyptian Pound (EGP)", 48.5),
    JPY("JPY", "¥", "¥", "Japanese Yen (JPY)", 155.0),
    KWD("KWD", "KWD", "د.ك", "Kuwaiti Dinar (KWD)", 0.31),
    QAR("QAR", "QAR", "ر.ق", "Qatari Riyal (QAR)", 3.64);

    fun formatPrice(amountUsd: Double?, liveRate: Double? = null): String {
        if (amountUsd == null || amountUsd < 0) {
            return "Price unavailable"
        }
        if (amountUsd == 0.0) {
            return "Free"
        }
        val rate = liveRate ?: defaultRateFromUsd
        val converted = amountUsd * rate
        return when (this) {
            DZD, JPY -> "${converted.toInt()} $symbol"
            KWD -> "${String.format("%.3f", converted)} $symbol"
            EUR -> "$symbol${String.format("%.1f", converted)}"
            USD, GBP, CAD -> "$symbol${String.format("%.1f", converted)}"
            SAR, AED, EGP, QAR -> "${String.format("%.1f", converted)} $symbol"
        }
    }

    fun formatPriceAr(amountUsd: Double?, liveRate: Double? = null): String {
        if (amountUsd == null || amountUsd < 0) {
            return "السعر غير متوفر"
        }
        if (amountUsd == 0.0) {
            return "مجاني"
        }
        val rate = liveRate ?: defaultRateFromUsd
        val converted = amountUsd * rate
        return when (this) {
            DZD, JPY -> "${converted.toInt()} $symbolAr"
            KWD -> "${String.format("%.3f", converted)} $symbolAr"
            else -> "${String.format("%.1f", converted)} $symbolAr"
        }
    }

    fun formatPriceFr(amountUsd: Double?, liveRate: Double? = null): String {
        if (amountUsd == null || amountUsd < 0) {
            return "Prix non disponible"
        }
        if (amountUsd == 0.0) {
            return "Gratuit"
        }
        val rate = liveRate ?: defaultRateFromUsd
        val converted = amountUsd * rate
        return when (this) {
            DZD, JPY -> "${converted.toInt()} $symbol"
            KWD -> "${String.format("%.3f", converted)} $symbol"
            EUR -> "${String.format("%.1f", converted)} $symbol"
            else -> "$symbol${String.format("%.1f", converted)}"
        }
    }
}
