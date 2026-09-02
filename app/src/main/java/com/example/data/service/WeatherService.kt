package com.example.data.service

import android.util.Log
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class CurrentWeatherDto(
    val temperature_2m: Double? = 22.0,
    val relative_humidity_2m: Int? = 55,
    val precipitation: Double? = 0.0,
    val weather_code: Int? = 0,
    val wind_speed_10m: Double? = 10.0
)

@JsonClass(generateAdapter = true)
data class OpenMeteoResponse(
    val current: CurrentWeatherDto? = null
)

data class WeatherInfo(
    val temperatureC: Double,
    val conditionTitle: String,
    val conditionTitleAr: String,
    val conditionTitleFr: String,
    val iconEmoji: String,
    val isRainy: Boolean,
    val recommendationHint: String,
    val recommendationHintAr: String,
    val recommendationHintFr: String
)

class WeatherService {

    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    suspend fun fetchWeather(latitude: Double, longitude: Double): WeatherInfo = withContext(Dispatchers.IO) {
        try {
            val url = "https://api.open-meteo.com/v1/forecast?latitude=$latitude&longitude=$longitude&current=temperature_2m,relative_humidity_2m,precipitation,weather_code,wind_speed_10m"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "WINNROH-App/1.0 (contact: info@winnroh.app)")
                .header("Accept", "application/json")
                .build()
            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val json = response.body?.string()?.trim() ?: ""
                if (json.isNotEmpty() && json.startsWith("{")) {
                    var temp = 22.0
                    var code = 0
                    var precip = 0.0

                    try {
                        val dto = moshi.adapter(OpenMeteoResponse::class.java).lenient().fromJson(json)
                        val current = dto?.current
                        if (current != null) {
                            code = current.weather_code ?: 0
                            temp = current.temperature_2m ?: 22.0
                            precip = current.precipitation ?: 0.0
                        }
                    } catch (moshiErr: Exception) {
                        // Fallback with org.json.JSONObject for maximum resilience
                        val jsonObj = org.json.JSONObject(json)
                        if (jsonObj.has("current")) {
                            val currentObj = jsonObj.getJSONObject("current")
                            temp = currentObj.optDouble("temperature_2m", 22.0)
                            code = currentObj.optInt("weather_code", 0)
                            precip = currentObj.optDouble("precipitation", 0.0)
                        }
                    }

                    val isRain = precip > 0.1 || code in listOf(51, 53, 55, 61, 63, 65, 80, 81, 82, 95, 96, 99)

                    return@withContext if (isRain) {
                        WeatherInfo(
                            temperatureC = temp,
                            conditionTitle = "Rainy & Breezy",
                            conditionTitleAr = "ممطر وعاصف نسبياً",
                            conditionTitleFr = "Pluvieux et frais",
                            iconEmoji = "🌧️",
                            isRainy = true,
                            recommendationHint = "Rain outside • WAYGO is prioritizing cozy indoor cafes, arcade arenas & escape rooms",
                            recommendationHintAr = "الجو ماطر بالخارج • نقترح كافيهات دافئة وصالات ألعاب ومتاحف مغلقة",
                            recommendationHintFr = "Temps pluvieux • Priorité aux cafés chaleureux, arcades et cinémas"
                        )
                    } else if (temp > 28.0) {
                        WeatherInfo(
                            temperatureC = temp,
                            conditionTitle = "Sunny & Warm",
                            conditionTitleAr = "مشمس ودافئ",
                            conditionTitleFr = "Ensoleillé et chaud",
                            iconEmoji = "☀️",
                            isRainy = false,
                            recommendationHint = "Warm vibes • Perfect for sunset rooftops, refreshing iced lattes & evening walks",
                            recommendationHintAr = "أجواء دافئة • مثالية للمطلات البانورامية وجلسات الغروب الخارجية والمشروبات المنعشة",
                            recommendationHintFr = "Belle météo • Idéal pour les terrasses en rooftop et promenades au coucher du soleil"
                        )
                    } else {
                        WeatherInfo(
                            temperatureC = temp,
                            conditionTitle = "Pleasant & Clear",
                            conditionTitleAr = "معتدل وصافٍ",
                            conditionTitleFr = "Agréable et dégagé",
                            iconEmoji = "✨",
                            isRainy = false,
                            recommendationHint = "Great weather • Ideal balance of outdoor views, padel matches & culinary spots",
                            recommendationHintAr = "طقس مثالي • توازن رائع بين المسارات الطبيعية وملاعب البادل والمطاعم المفتوحة",
                            recommendationHintFr = "Temps splendide • Équilibre parfait entre découvertes de plein air et gastronomie"
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("WeatherService", "Failed to fetch live weather, using graceful fallback", e)
        }

        // Graceful standard default
        WeatherInfo(
            temperatureC = 23.5,
            conditionTitle = "Clear Evening",
            conditionTitleAr = "أمسية لطيفة وصافية",
            conditionTitleFr = "Soirée douce et claire",
            iconEmoji = "🌇",
            isRainy = false,
            recommendationHint = "Sunset hour • Best time for rooftops, specialty coffees, and social gaming",
            recommendationHintAr = "وقت الغروب • التوقيت الأمثل للمقاهي المختصة، ألعاب الآركيد والمطلات",
            recommendationHintFr = "Heure dorée • Parfait pour les rooftops et cafés spécialisés"
        )
    }
}
