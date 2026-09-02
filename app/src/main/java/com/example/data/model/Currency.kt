package com.example.data.model

enum class AppCurrency(
    val code: String,
    val symbol: String,
    val symbolAr: String,
    val displayName: String,
    val rateFromUsd: Double // Standard benchmark conversion rate
) {
    DZD("DZD", "DA", "د.ج", "Algerian Dinar (DZD)", 134.5),
    EUR("EUR", "€", "€", "Euro (EUR)", 0.92),
    USD("USD", "$", "$", "US Dollar (USD)", 1.0),
    CAD("CAD", "CA$", "د.ك", "Canadian Dollar (CAD)", 1.36),
    GBP("GBP", "£", "£", "British Pound (GBP)", 0.79);

    fun formatPrice(amountUsd: Double?): String {
        if (amountUsd == null || amountUsd < 0) {
            return "Price unavailable"
        }
        if (amountUsd == 0.0) {
            return "Free"
        }
        val converted = amountUsd * rateFromUsd
        return when (this) {
            DZD -> "${converted.toInt()} $symbol"
            EUR -> "$symbol${String.format("%.1f", converted)}"
            USD -> "$symbol${String.format("%.1f", converted)}"
            CAD -> "$symbol${String.format("%.1f", converted)}"
            GBP -> "$symbol${String.format("%.1f", converted)}"
        }
    }

    fun formatPriceAr(amountUsd: Double?): String {
        if (amountUsd == null || amountUsd < 0) {
            return "السعر غير متوفر"
        }
        if (amountUsd == 0.0) {
            return "مجاني"
        }
        val converted = amountUsd * rateFromUsd
        return when (this) {
            DZD -> "${converted.toInt()} $symbolAr"
            else -> "${String.format("%.1f", converted)} $symbolAr"
        }
    }

    fun formatPriceFr(amountUsd: Double?): String {
        if (amountUsd == null || amountUsd < 0) {
            return "Prix non disponible"
        }
        if (amountUsd == 0.0) {
            return "Gratuit"
        }
        val converted = amountUsd * rateFromUsd
        return when (this) {
            DZD -> "${converted.toInt()} $symbol"
            EUR -> "${String.format("%.1f", converted)} $symbol"
            else -> "$symbol${String.format("%.1f", converted)}"
        }
    }
}
