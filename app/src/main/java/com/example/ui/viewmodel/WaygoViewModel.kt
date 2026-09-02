package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.GeminiConciergeService
import com.example.data.model.*
import com.example.data.repository.WaygoRepository
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

    // App Preferences
    val currentLanguage = MutableStateFlow(AppLanguage.ENGLISH)
    val isDarkMode = MutableStateFlow(false)

    // Data Streams from Room
    val allPlaces = repository.allPlaces.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val savedPlaces = repository.savedPlaces.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val visitedPlaces = repository.visitedPlaces.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val userProfile = repository.userProfile.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val allBadges = repository.allBadges.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val collections = repository.collections.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val notifications = repository.notifications.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Explore / Filtering
    val filterState = MutableStateFlow(PlaceFilter())

    val filteredPlaces: StateFlow<List<Place>> = combine(allPlaces, filterState) { places, filter ->
        repository.filterPlaces(places, filter)
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
                text = "👋 Hello! I'm your WAYGO Concierge. Where are you heading, who are you with, and what's your vibe today?"
            )
        )
    )
    val isAiThinking = MutableStateFlow(false)
    val activeItinerary = MutableStateFlow<ItineraryPlan?>(null)

    // Gamification Celebrations
    val levelUpCelebration = MutableStateFlow<Int?>(null)
    val badgeUnlockCelebration = MutableStateFlow<Badge?>(null)

    fun setLanguage(lang: AppLanguage) {
        currentLanguage.value = lang
    }

    fun toggleDarkMode() {
        isDarkMode.value = !isDarkMode.value
    }

    fun updateSearchQuery(query: String) {
        filterState.value = filterState.value.copy(query = query)
    }

    fun selectCategory(category: CategoryType) {
        filterState.value = filterState.value.copy(category = category)
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
            repository.markPlaceVisited(place.id)
        }
    }

    fun submitReview(placeId: String, rating: Double, comment: String) {
        viewModelScope.launch {
            val user = userProfile.value?.displayName ?: "Ahmed"
            repository.addReview(placeId, rating, comment, user)
        }
    }

    fun createCollection(title: String, description: String, emoji: String) {
        viewModelScope.launch {
            repository.createCollection(title, description, emoji)
        }
    }

    fun rollSurpriseMe() {
        viewModelScope.launch {
            isSurpriseSpinning.value = true
            surpriseResult.value = null
            delay(1200) // Engaging suspense animation

            val eligiblePlaces = allPlaces.value.filter { p ->
                p.priceLevel.ordinal <= surpriseBudget.value.ordinal &&
                        p.suitableCompanions.contains(surpriseCompanion.value.name)
            }

            val chosen = if (eligiblePlaces.isNotEmpty()) {
                eligiblePlaces.random()
            } else {
                allPlaces.value.randomOrNull()
            }

            surpriseResult.value = chosen
            isSurpriseSpinning.value = false
            repository.awardXP(15, "Rolled Surprise Me Discovery 🎲")
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
            val contextInfo = "User: ${profile?.displayName}, City: ${profile?.city}, Interests: ${profile?.selectedInterests?.joinToString()}"

            // If prompt requests itinerary
            if (prompt.lowercase().contains("itinerary") || prompt.lowercase().contains("plan") || prompt.lowercase().contains("جدول") || prompt.lowercase().contains("برنامج")) {
                val itinerary = aiService.generateItinerary(places, "Friends Squad", 50.0, 3.5)
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
            repository.awardXP(20, "Used AI Concierge Discovery")
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
            repository.awardXP(50, "Completed Explorer Onboarding ✨")
        }
    }
}
