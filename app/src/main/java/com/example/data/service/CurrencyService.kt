package com.example.data.service

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.data.model.AppCurrency
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class ExchangeRateResponse(
    val result: String? = null,
    val time_last_update_utc: String? = null,
    val rates: Map<String, Double>? = null
)

interface CurrencyProvider {
    suspend fun getExchangeRates(): Map<String, Double>
    fun getLastUpdatedTimestamp(): String
}

class CurrencyService(context: Context) : CurrencyProvider {

    private val prefs: SharedPreferences = context.getSharedPreferences("winnroh_currency_cache", Context.MODE_PRIVATE)
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val _rates = MutableStateFlow<Map<String, Double>>(emptyMap())
    val rates: StateFlow<Map<String, Double>> = _rates

    private var lastUpdateUtc: String = "Live / Baseline"

    init {
        // Load cached rates from disk
        val cachedMap = mutableMapOf<String, Double>()
        AppCurrency.values().forEach { curr ->
            val cachedRate = prefs.getFloat("rate_${curr.code}", curr.defaultRateFromUsd.toFloat())
            cachedMap[curr.code] = cachedRate.toDouble()
        }
        _rates.value = cachedMap
        lastUpdateUtc = prefs.getString("last_update_utc", "Live / Baseline") ?: "Live / Baseline"
    }

    override suspend fun getExchangeRates(): Map<String, Double> = withContext(Dispatchers.IO) {
        try {
            val url = "https://open.er-api.com/v6/latest/USD"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "WINNROH-App/1.0 (contact: info@winnroh.app)")
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val json = response.body?.string() ?: ""
                val adapter = moshi.adapter(ExchangeRateResponse::class.java).lenient()
                val data = adapter.fromJson(json)
                val newRates = data?.rates
                if (!newRates.isNullOrEmpty()) {
                    val updated = mutableMapOf<String, Double>()
                    val editor = prefs.edit()
                    AppCurrency.values().forEach { curr ->
                        val rate = newRates[curr.code] ?: curr.defaultRateFromUsd
                        updated[curr.code] = rate
                        editor.putFloat("rate_${curr.code}", rate.toFloat())
                    }
                    val updateTime = data.time_last_update_utc ?: "Recent"
                    lastUpdateUtc = updateTime
                    editor.putString("last_update_utc", updateTime)
                    editor.apply()
                    _rates.value = updated
                    return@withContext updated
                }
            }
        } catch (e: Exception) {
            Log.w("CurrencyService", "Failed to fetch live exchange rates, falling back to cached/central baseline", e)
        }
        return@withContext _rates.value
    }

    override fun getLastUpdatedTimestamp(): String = lastUpdateUtc
}
