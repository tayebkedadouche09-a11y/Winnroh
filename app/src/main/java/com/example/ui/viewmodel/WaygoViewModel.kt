package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.GeminiConciergeService
import com.example.data.model.*
import com.example.data.repository.CheckInResult
import com.example.data.repository.WaygoRepository
import com.example.data.service.*
import com.example.ui.theme.AppLanguage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

data class ChatMessage(
    val id: String = "msg_${System.currentTimeMillis()}_${Random.nextInt(1000)}",
    val sender: String, // "user" or "concierge"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val itinerary: ItineraryPlan? = null
)

class WaygoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = WaygoRepository.getInstance(application)
    private val aiService = GeminiConciergeService()
    private val authService = AuthService(application)
    private val locationProvider = LocationAndPlacesProvider(application)
    private val currencyService = CurrencyService(application)

    // App Preferences & Globalization
    val currentLanguage = MutableStateFlow(AppLanguage.ENGLISH)
    val isDarkMode = MutableStateFlow(false)
    val selectedCurrency = MutableStateFlow(AppCurrency.USD)
    val liveExchangeRates = currencyService.rates
    val exchangeRateLastUpdated: String get() = currencyService.getLastUpdatedTimestamp()

    val defaultGlobalCities = locationProvider.defaultGlobalCities
    val activeCity = MutableStateFlow(locationProvider.defaultGlobalCities.first())

    // Authentication State
    val authUser = authService.currentUser
    val authError = MutableStateFlow<String?>(null)

    // Weather State
    val weatherState = MutableStateFlow<WeatherInfo?>(null)

    // Live Places Search & Loading State
    val liveSearchResults = MutableStateFlow<List<Place>>(emptyList())
    val isSearchingLive = MutableStateFlow(false)
    val isDiscoveringNearby = MutableStateFlow(false)

    // Check-in Feedback
    val checkInMessage = MutableStateFlow<String?>(null)

    // Data Streams from Room
    val allPlaces = repository.allPlaces.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val savedPlaces = repository.savedPlaces.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val visitedPlaces = repository.visitedPlaces.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val userProfile = repository.userProfile.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val allBadges = repository.allBadges.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val collections = repository.collections.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val notifications = repository.notifications.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allReports = repository.allReports.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val businessAccounts = repository.businessAccounts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Explore / Filtering
    val filterState = MutableStateFlow(PlaceFilter())

    val filteredPlaces: StateFlow<List<Place>> = combine(allPlaces, filterState, activeCity) { places, filter, city ->
        val mappedPlaces = places.map { p ->
            val dist = locationProvider.calculateDistanceKm(city.latitude, city.longitude, p.latitude, p.longitude)
            p.copy(distanceKm = dist)
        }
        repository.filterPlaces(mappedPlaces, filter)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected Place Details
    val selectedPlaceId = MutableStateFlow<String?>(null)
    val selectedPlace: StateFlow<Place?> = combine(allPlaces, selectedPlaceId) { places, id ->
        if (id == null) null else places.firstOrNull { it.id == id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val selectedPlaceReviews = selectedPlaceId.flatMapLatest { id ->
        if (id != null) repository.getReviewsForPlace(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Surprise Me State
    val isSurpriseSpinning = MutableStateFlow(false)
    val surpriseResult = MutableStateFlow<Place?>(null)
    val surpriseCompanion = MutableStateFlow(CompanionType.FRIENDS)
    val surpriseBudget = MutableStateFlow(BudgetLevel.MODERATE)
    val surpriseMaxTimeMin = MutableStateFlow(120)

    // AI Concierge Chat
    val chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = "concierge",
                text = "👋 مرحباً بك في وين نروح؟ (WINNROH). أنا مرشدك الذكي. أخبرني بأجواء خروجتك، اهتماماتك أو المدينة التي ترغب باستكشافها!"
            )
        )
    )
    val isAiThinking = MutableStateFlow(false)
    val activeItinerary = MutableStateFlow<ItineraryPlan?>(null)
    val levelUpCelebration = MutableStateFlow<Int?>(null)

    init {
        // Try getting device GPS location initially
        val deviceLoc = locationProvider.getDeviceLocation()
        if (deviceLoc != null) {
            activeCity.value = deviceLoc
        }
        loadWeather()
        refreshPlacesAroundCurrentLocation()

        viewModelScope.launch {
            currencyService.getExchangeRates()
        }
    }

    fun loadWeather() {
        viewModelScope.launch {
            val city = activeCity.value
            val info = repository.fetchWeather(city.latitude, city.longitude)
            weatherState.value = info
        }
    }

    fun setCity(city: CityLocation) {
        activeCity.value = city
        loadWeather()
        refreshPlacesAroundCurrentLocation()
    }

    fun useDeviceGpsLocation() {
        val loc = locationProvider.getDeviceLocation()
        if (loc != null) {
            activeCity.value = loc
            loadWeather()
            refreshPlacesAroundCurrentLocation()
        }
    }

    fun searchAndSelectCity(query: String) {
        viewModelScope.launch {
            val geocoded = locationProvider.geocodeAddress(query)
            if (geocoded != null) {
                setCity(geocoded)
            }
        }
    }

    fun refreshPlacesAroundCurrentLocation() {
        viewModelScope.launch {
            isDiscoveringNearby.value = true
            val city = activeCity.value
            repository.fetchAndCacheNearbyPlaces(city.latitude, city.longitude, 10000, filterState.value.category)
            isDiscoveringNearby.value = false
        }
    }

    fun setCurrency(currency: AppCurrency) {
        selectedCurrency.value = currency
    }

    fun setLanguage(lang: AppLanguage) {
        currentLanguage.value = lang
    }

    fun toggleDarkMode() {
        isDarkMode.value = !isDarkMode.value
    }

    // Authentication Actions
    fun login(email: String, pass: String): Boolean {
        return when (val res = authService.login(email, pass)) {
            is AuthResult.Success -> {
                authError.value = null
                true
            }
            is AuthResult.Error -> {
                authError.value = res.messageEn
                false
            }
        }
    }

    fun register(email: String, pass: String, name: String, username: String): Boolean {
        return when (val res = authService.register(email, pass, name, username)) {
            is AuthResult.Success -> {
                authError.value = null
                true
            }
            is AuthResult.Error -> {
                authError.value = res.messageEn
                false
            }
        }
    }

    fun logout() {
        authService.logout()
    }

    fun deleteAccount() {
        viewModelScope.launch {
            repository.deleteAccount()
            authService.deleteAccount()
        }
    }

    // Search & Live Places
    fun updateSearchQuery(query: String) {
        filterState.value = filterState.value.copy(query = query)
        if (query.length >= 3) {
            searchLive(query)
        } else {
            liveSearchResults.value = emptyList()
        }
    }

    private fun searchLive(query: String) {
        viewModelScope.launch {
            isSearchingLive.value = true
            val city = activeCity.value
            val live = repository.searchLivePlaces(query, city.latitude, city.longitude)
            liveSearchResults.value = live
            isSearchingLive.value = false
        }
    }

    fun selectCategory(category: CategoryType) {
        filterState.value = filterState.value.copy(category = category)
        refreshPlacesAroundCurrentLocation()
    }

    fun updateFilter(filter: PlaceFilter) {
        filterState.value = filter
    }

    fun resetFilters() {
        filterState.value = PlaceFilter()
    }

    fun selectPlace(placeId: String?) {
        selectedPlaceId.value = placeId
    }

    fun toggleSavePlace(place: Place) {
        viewModelScope.launch {
            repository.toggleSavePlace(place.id, place.isSaved)
        }
    }

    fun checkInPlace(place: Place) {
        viewModelScope.launch {
            val city = activeCity.value
            val result = repository.checkInWithProximity(place, city.latitude, city.longitude)
            when (result) {
                is CheckInResult.Success -> {
                    checkInMessage.value = "✅ Checked in to ${result.placeName}! +${result.xpAwarded} XP"
                }
                is CheckInResult.TooFar -> {
                    checkInMessage.value = "⚠️ Too far to check in (${result.distanceMeters}m away). Must be within 500m."
                }
                is CheckInResult.AlreadyVisited -> {
                    checkInMessage.value = "📍 You have already checked in to this place."
                }
            }
        }
    }

    fun clearCheckInMessage() {
        checkInMessage.value = null
    }

    fun submitReview(placeId: String, rating: Double, comment: String, photoUrl: String? = null) {
        viewModelScope.launch {
            val user = authUser.value?.displayName ?: userProfile.value?.displayName ?: "Explorer"
            repository.addReview(placeId, rating, comment, user, photoUrl)
        }
    }

    fun upvoteReview(reviewId: String) {
        viewModelScope.launch {
            repository.upvoteReview(reviewId)
        }
    }

    fun reportItem(targetType: String, targetId: String, reason: String) {
        viewModelScope.launch {
            repository.reportItem(targetType, targetId, reason)
        }
    }

    fun resolveReport(reportId: String, status: String) {
        viewModelScope.launch {
            repository.resolveReport(reportId, status)
        }
    }

    fun claimBusiness(placeId: String, name: String, email: String, phone: String, promo: String) {
        viewModelScope.launch {
            repository.claimBusiness(placeId, name, email, phone, promo)
        }
    }

    fun createCollection(title: String, description: String, emoji: String) {
        viewModelScope.launch {
            repository.createCollection(title, description, emoji)
        }
    }

    fun deleteCollection(collectionId: String) {
        viewModelScope.launch {
            repository.deleteCollection(collectionId)
        }
    }

    fun openDirections(place: Place) {
        repository.openDirections(place)
    }

    fun sharePlace(place: Place) {
        repository.sharePlace(place)
    }

    fun rollSurpriseMe() {
        viewModelScope.launch {
            isSurpriseSpinning.value = true
            surpriseResult.value = null
            delay(1000)

            val places = allPlaces.value
            val eligiblePlaces = places.filter { p ->
                p.priceLevel.ordinal <= surpriseBudget.value.ordinal
            }

            val chosen = if (eligiblePlaces.isNotEmpty()) {
                eligiblePlaces.random()
            } else {
                places.randomOrNull()
            }

            surpriseResult.value = chosen
            isSurpriseSpinning.value = false
            if (chosen != null) {
                repository.awardXpWithDeduplication(15, "Rolled Surprise Me Discovery 🎲")
            }
        }
    }

    fun sendAiMessage(prompt: String) {
        if (prompt.isBlank()) return
        val userMsg = ChatMessage(sender = "user", text = prompt)
        chatMessages.value = chatMessages.value + userMsg
        isAiThinking.value = true

        viewModelScope.launch {
            val places = allPlaces.value
            val profile = userProfile.value
            val city = activeCity.value
            val contextInfo = "User: ${profile?.displayName}, Location: ${city.nameEn} (${city.country}), Interests: ${profile?.selectedInterests?.joinToString()}"

            if (prompt.lowercase().contains("itinerary") || prompt.lowercase().contains("plan") || prompt.lowercase().contains("جدول") || prompt.lowercase().contains("برنامج")) {
                val itinerary = aiService.generateItinerary(places, "Group Outing", 50.0, 3.5)
                activeItinerary.value = itinerary
                val aiMsg = ChatMessage(
                    sender = "concierge",
                    text = "Here is an optimized multi-stop adventure itinerary for your outing! 🌟",
                    itinerary = itinerary
                )
                chatMessages.value = chatMessages.value + aiMsg
            } else {
                val response = aiService.askConcierge(prompt, places, contextInfo)
                val aiMsg = ChatMessage(sender = "concierge", text = response)
                chatMessages.value = chatMessages.value + aiMsg
            }

            isAiThinking.value = false
            repository.awardXpWithDeduplication(20, "Used AI Concierge Discovery")
        }
    }

    fun completeOnboarding(interests: List<String>, budget: BudgetLevel) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfile()
            repository.updateProfile(
                current.copy(
                    selectedInterests = interests,
                    typicalBudget = budget,
                    hasCompletedOnboarding = true
                )
            )
            repository.awardXpWithDeduplication(50, "Completed Explorer Onboarding ✨")
        }
    }
}
