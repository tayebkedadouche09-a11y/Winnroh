package com.example.data.ai

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.ItineraryPlan
import com.example.data.model.ItineraryStop
import com.example.data.model.Place
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiPart(val text: String? = null)

@JsonClass(generateAdapter = true)
data class GeminiContent(val parts: List<GeminiPart>, val role: String? = null)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val systemInstruction: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(val content: GeminiContent?)

@JsonClass(generateAdapter = true)
data class GeminiResponse(val candidates: List<GeminiCandidate>?)

class GeminiConciergeService {

    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun askConcierge(
        userQuery: String,
        availablePlaces: List<Place>,
        userContext: String
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext generateSmartFallbackResponse(userQuery, availablePlaces)
        }

        try {
            val placesContext = availablePlaces.joinToString("\n") { p ->
                "- ID:${p.id} | Name: ${p.name} (${p.arabicName}) | Cat: ${p.category.name} | Price: ${p.priceLevel.symbol} (~$${p.estimatedCostUsd}) | Rating: ${p.rating}★ | Time: ${p.averageDurationMinutes}m | Indoor: ${p.isIndoor} | For: ${p.suitableCompanions.joinToString()} | Highlights: ${p.features.joinToString()}"
            }

            val systemPrompt = """
                You are the WAYGO AI Concierge ("وين نروح؟"), an energetic, friendly, and expert discovery guide.
                Rules:
                1. Only recommend places from the provided list below. NEVER invent places.
                2. Match the user's budget, companions, mood, and time constraints.
                3. Format your recommendation clearly with emoji bullet points, rationale, estimated timing, and approximate total cost.
                4. Always respond warmly and support English, Arabic, or French depending on the user's message language.
                
                Available Places Database:
                $placesContext
                
                User Context: $userContext
            """.trimIndent()

            val requestObj = GeminiRequest(
                contents = listOf(
                    GeminiContent(parts = listOf(GeminiPart(text = userQuery)))
                ),
                systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemPrompt)))
            )

            val jsonBody = moshi.adapter(GeminiRequest::class.java).toJson(requestObj)
            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
                .post(jsonBody.toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val respBody = response.body?.string() ?: ""
                val parsed = moshi.adapter(GeminiResponse::class.java).fromJson(respBody)
                val answer = parsed?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!answer.isNullOrBlank()) {
                    return@withContext answer
                }
            }
            generateSmartFallbackResponse(userQuery, availablePlaces)
        } catch (e: Exception) {
            Log.e("GeminiConcierge", "API Call failed, using intelligent fallback", e)
            generateSmartFallbackResponse(userQuery, availablePlaces)
        }
    }

    fun generateItinerary(
        places: List<Place>,
        companion: String,
        budgetUsd: Double,
        availableHours: Double
    ): ItineraryPlan {
        val selectedStops = mutableListOf<ItineraryStop>()
        var remainingBudget = budgetUsd
        var accumulatedMinutes = 0
        val targetMinutes = (availableHours * 60).toInt()

        val startHours = 17 // 5:00 PM
        var currentMinutes = startHours * 60

        // Pick 1 activity/gaming/sight, 1 food/dining, 1 coffee/chill
        val activityPlace = places.firstOrNull { it.category.name in listOf("GAMING", "SPORTS", "ENTERTAINMENT", "TOURISM", "NATURE") }
        val diningPlace = places.firstOrNull { it.category.name in listOf("FOOD") }
        val coffeePlace = places.firstOrNull { it.category.name in listOf("COFFEE", "RELAXATION") }

        val candidateList = listOfNotNull(activityPlace, diningPlace, coffeePlace)

        candidateList.forEach { place ->
            if (accumulatedMinutes + place.averageDurationMinutes <= targetMinutes + 30) {
                val hour = currentMinutes / 60
                val min = currentMinutes % 60
                val timeLabel = String.format("%02d:%02d", hour, min)

                selectedStops.add(
                    ItineraryStop(
                        timeLabel = timeLabel,
                        place = place,
                        activityTitle = when (place.category.name) {
                            "GAMING" -> "🎮 Multiplayer VR & Arcade Challenge"
                            "FOOD" -> "🍔 Sunset Dining & Gourmet Bites"
                            "COFFEE" -> "☕ Relaxing Artisanal Brews & Dessert"
                            "NATURE" -> "🌳 Scenic Golden Hour Walk"
                            else -> "✨ Explore ${place.name}"
                        },
                        estimatedDurationMin = place.averageDurationMinutes,
                        estimatedCost = "~$${place.estimatedCostUsd.toInt()}",
                        tip = "Great spot for $companion. " + place.whyMatchReason
                    )
                )

                currentMinutes += place.averageDurationMinutes + 20 // 20m transit
                accumulatedMinutes += place.averageDurationMinutes + 20
                remainingBudget -= place.estimatedCostUsd
            }
        }

        val totalCost = selectedStops.sumOf { it.place.estimatedCostUsd }

        return ItineraryPlan(
            title = "Personalized Adventure Flow 🚀",
            subtitle = "Crafted for $companion • $availableHours hrs total",
            totalTime = "${(accumulatedMinutes / 60)}h ${accumulatedMinutes % 60}m",
            totalCost = "~$${totalCost.toInt()}",
            stops = selectedStops,
            weatherNote = "☀️ Clear skies expected — great balance of indoor thrills and sunset dining."
        )
    }

    private fun generateSmartFallbackResponse(userQuery: String, places: List<Place>): String {
        val q = userQuery.lowercase()
        val matchedPlaces = places.filter { p ->
            when {
                q.contains("coffee") || q.contains("cafe") || q.contains("قهوة") || q.contains("مقهى") -> p.category.name == "COFFEE"
                q.contains("game") || q.contains("gaming") || q.contains("لعب") || q.contains("vr") -> p.category.name == "GAMING"
                q.contains("food") || q.contains("eat") || q.contains("مطعم") || q.contains("اكل") -> p.category.name == "FOOD"
                q.contains("outdoor") || q.contains("nature") || q.contains("طبيعة") || q.contains("حديقة") -> p.category.name == "NATURE"
                q.contains("friend") || q.contains("friends") || q.contains("صحاب") || q.contains("شلة") -> p.suitableCompanions.contains("FRIENDS")
                else -> true
            }
        }.take(3)

        val sb = StringBuilder()
        sb.append("🎯 **Here is what WAYGO recommends for you:**\n\n")

        if (matchedPlaces.isEmpty()) {
            val topPicks = places.take(2)
            topPicks.forEach { p ->
                sb.append("📍 **${p.name}** (${p.rating}★)\n")
                sb.append("• ${p.description}\n")
                sb.append("• Est. Cost: ~\$${p.estimatedCostUsd.toInt()} | Time: ${p.averageDurationMinutes} mins\n\n")
            }
        } else {
            matchedPlaces.forEachIndexed { i, p ->
                sb.append("${i + 1}. 📍 **${p.name}** (${p.arabicName})\n")
                sb.append("   • **Rating:** ${p.rating}★ (${p.reviewCount} reviews)\n")
                sb.append("   • **Why it matches:** ${p.whyMatchReason}\n")
                sb.append("   • **Budget:** ${p.priceLevel.symbol} (~$${p.estimatedCostUsd.toInt()}) • **Duration:** ~${p.averageDurationMinutes}m\n\n")
            }
        }

        sb.append("💡 *Tip: Tap on any place to see photos, directions, or add to your weekend collection!*")
        return sb.toString()
    }
}
