package com.example.data.repository

import android.content.Context
import com.example.data.local.WaygoDao
import com.example.data.local.WaygoDatabase
import com.example.data.model.*
import com.example.data.service.LocationAndPlacesProvider
import com.example.data.service.WeatherInfo
import com.example.data.service.WeatherService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class CheckInResult {
    data class Success(val placeName: String, val xpAwarded: Int) : CheckInResult()
    data class TooFar(val distanceMeters: Int) : CheckInResult()
    object AlreadyVisited : CheckInResult()
}

class WaygoRepository(
    private val dao: WaygoDao,
    private val locationProvider: LocationAndPlacesProvider,
    private val weatherService: WeatherService
) {

    val allPlaces: Flow<List<Place>> = dao.getAllPlaces()
    val savedPlaces: Flow<List<Place>> = dao.getSavedPlaces()
    val visitedPlaces: Flow<List<Place>> = dao.getVisitedPlaces()
    val userProfile: Flow<UserProfile?> = dao.getUserProfile()
    val allBadges: Flow<List<Badge>> = dao.getAllBadges()
    val collections: Flow<List<UserCollection>> = dao.getCollections()
    val recentTransactions: Flow<List<XPTransaction>> = dao.getRecentXpTransactions()
    val notifications: Flow<List<NotificationItem>> = dao.getNotifications()
    val allReports: Flow<List<ReportItem>> = dao.getAllReports()
    val businessAccounts: Flow<List<BusinessAccount>> = dao.getAllBusinessAccounts()

    suspend fun initializeDatabaseIfEmpty() = withContext(Dispatchers.IO) {
        val existingProfile = dao.getUserProfile().firstOrNull()
        if (existingProfile == null) {
            dao.insertOrUpdateProfile(UserProfile())

            // Default system achievement badges (all start locked)
            val initialBadges = listOf(
                Badge("b_first_discovery", "First Discovery", "أول اكتشاف", "Checked in to your very first live place", "قمت بتسجيل زيارة أول مكان حقيقي", "🌟", BadgeCategory.EXPLORATION, 50, false, null, 0, 1),
                Badge("b_coffee_hunter", "Coffee Hunter", "صائد القهوة", "Discovered 5 unique local coffee shops", "استكشفت 5 مقاهي وكافيهات محلية", "☕", BadgeCategory.FOOD_AND_DRINK, 100, false, null, 0, 5),
                Badge("b_food_explorer", "Foodie Connoisseur", "خبير النكهات", "Checked in to 5 dining destinations", "قمت بزيارة 5 مطاعم وتجارب طعام", "🍔", BadgeCategory.FOOD_AND_DRINK, 100, false, null, 0, 5),
                Badge("b_nature_lover", "Nature Lover", "عاشق الطبيعة", "Explored 3 outdoor parks or gardens", "استكشفت 3 حدائق أو معالم طبيعية", "🌲", BadgeCategory.EXPLORATION, 120, false, null, 0, 3),
                Badge("b_culture_seeker", "Heritage Seeker", "مستكشف التراث", "Visited 3 museums or historical sights", "زرت 3 معالم سياحية أو متاحف أثرية", "🏛️", BadgeCategory.SPECIAL, 150, false, null, 0, 3),
                Badge("b_night_explorer", "Night Explorer", "مستكشف الليالي", "Discovered venues active after dark", "استكشفت أماكن في الفترات المسائية", "🌙", BadgeCategory.NIGHT_LIFE, 100, false, null, 0, 3),
                Badge("b_critic", "Community Voice", "صوت المجتمع", "Published 3 honest local reviews", "نشرت 3 تقييمات مجتمعية صادقة", "✍️", BadgeCategory.SOCIAL, 150, false, null, 0, 3)
            )
            dao.insertBadges(initialBadges)

            // Starter collection
            val starterCollection = UserCollection(
                id = "col_favorites",
                title = "My Favorites",
                description = "Places and spots I love or want to visit soon",
                coverEmoji = "❤️",
                isPublic = true,
                itemsCount = 0
            )
            dao.insertCollection(starterCollection)

            // Welcome notification
            dao.insertNotification(
                NotificationItem(
                    id = "notif_welcome",
                    title = "Welcome to WINNROH! 🚀",
                    body = "Ready to discover what is around you? Search any city or turn on GPS to explore live places.",
                    iconEmoji = "🗺️"
                )
            )
        }
    }

    suspend fun fetchAndCacheNearbyPlaces(
        lat: Double,
        lon: Double,
        radiusMeters: Int = 8000,
        category: CategoryType = CategoryType.ALL
    ): List<Place> = withContext(Dispatchers.IO) {
        val livePlaces = locationProvider.fetchNearbyPlaces(lat, lon, radiusMeters, category)
        if (livePlaces.isNotEmpty()) {
            dao.insertPlaces(livePlaces)
        }
        livePlaces
    }

    fun getPlaceById(id: String): Flow<Place?> = dao.getPlaceByIdFlow(id)

    fun getReviewsForPlace(placeId: String): Flow<List<Review>> = dao.getReviewsForPlace(placeId)

    suspend fun fetchWeather(lat: Double, lon: Double): WeatherInfo {
        return weatherService.fetchWeather(lat, lon)
    }

    suspend fun searchLivePlaces(query: String, userLat: Double, userLon: Double): List<Place> = withContext(Dispatchers.IO) {
        val results = locationProvider.searchLivePlaces(query, userLat, userLon)
        if (results.isNotEmpty()) {
            dao.insertPlaces(results)
        }
        results
    }

    suspend fun toggleSavePlace(placeId: String, currentSaved: Boolean) = withContext(Dispatchers.IO) {
        dao.toggleSavedPlace(placeId, !currentSaved)
        if (!currentSaved) {
            awardXpWithDeduplication(10, "Saved place to favorites", placeId)
        }
    }

    suspend fun checkInWithProximity(place: Place, userLat: Double, userLon: Double): CheckInResult = withContext(Dispatchers.IO) {
        val userId = "current_user"
        val alreadyVisited = dao.hasUserVisitedPlace(userId, place.id) > 0
        if (alreadyVisited) {
            return@withContext CheckInResult.AlreadyVisited
        }

        // Distance proximity verification (500 meters threshold)
        val distKm = locationProvider.calculateDistanceKm(userLat, userLon, place.latitude, place.longitude)
        val distMeters = (distKm * 1000).toInt()

        // If GPS coordinates are realistic and user is farther than 500m, reject check-in
        if (userLat != 0.0 && userLon != 0.0 && distMeters > 500) {
            return@withContext CheckInResult.TooFar(distMeters)
        }

        dao.insertVisitedLog(VisitedPlaceLog(userId, place.id, latitude = userLat, longitude = userLon))
        dao.markPlaceVisited(place.id)
        awardXpWithDeduplication(50, "Verified physical check-in at ${place.name}", place.id, placesDelta = 1)
        checkBadgesProgress()

        return@withContext CheckInResult.Success(place.name, 50)
    }

    suspend fun addReview(placeId: String, rating: Double, comment: String, userName: String, photoUrl: String? = null) = withContext(Dispatchers.IO) {
        val review = Review(
            id = "rev_${System.currentTimeMillis()}",
            placeId = placeId,
            userName = userName,
            userAvatarUrl = "",
            rating = rating,
            text = comment,
            timestamp = System.currentTimeMillis(),
            helpfulLikesCount = 0,
            isVerifiedVisit = true,
            photoUrl = photoUrl
        )
        dao.insertReview(review)
        awardXpWithDeduplication(30, "Published a community review", placeId)
        checkBadgesProgress()
    }

    suspend fun upvoteReview(reviewId: String) = withContext(Dispatchers.IO) {
        dao.upvoteReview(reviewId)
    }

    suspend fun reportItem(targetType: String, targetId: String, reason: String) = withContext(Dispatchers.IO) {
        val report = ReportItem(
            id = "rep_${System.currentTimeMillis()}",
            reporterUserId = "current_user",
            targetType = targetType,
            targetId = targetId,
            reason = reason,
            status = "PENDING"
        )
        dao.insertReport(report)
    }

    suspend fun resolveReport(reportId: String, status: String) = withContext(Dispatchers.IO) {
        dao.updateReportStatus(reportId, status)
    }

    suspend fun claimBusiness(placeId: String, businessName: String, email: String, phone: String, promo: String) = withContext(Dispatchers.IO) {
        val account = BusinessAccount(
            id = "biz_${System.currentTimeMillis()}",
            businessName = businessName,
            ownerEmail = email,
            placeId = placeId,
            isVerified = true,
            promotionalOffer = promo.ifBlank { null },
            contactPhone = phone.ifBlank { null }
        )
        dao.insertBusinessAccount(account)
        dao.setPlaceSponsored(placeId, true)
    }

    suspend fun createCollection(title: String, description: String, emoji: String) = withContext(Dispatchers.IO) {
        val col = UserCollection(
            id = "col_${System.currentTimeMillis()}",
            title = title,
            description = description,
            coverEmoji = emoji,
            isPublic = true,
            itemsCount = 0
        )
        dao.insertCollection(col)
        awardXpWithDeduplication(25, "Created a themed collection")
    }

    suspend fun deleteCollection(collectionId: String) = withContext(Dispatchers.IO) {
        dao.deleteCollection(collectionId)
    }

    suspend fun updateProfile(profile: UserProfile) = withContext(Dispatchers.IO) {
        dao.insertOrUpdateProfile(profile)
    }

    suspend fun deleteAccount() = withContext(Dispatchers.IO) {
        dao.deleteUserProfile()
        dao.clearAllNotifications()
        // Reset profile to default
        dao.insertOrUpdateProfile(UserProfile())
    }

    suspend fun awardXpWithDeduplication(amount: Int, reason: String, placeId: String? = null, placesDelta: Int = 0) = withContext(Dispatchers.IO) {
        if (placeId != null) {
            val existingCount = dao.countTransactionsForPlaceAction(reason, placeId)
            if (existingCount > 0) {
                return@withContext
            }
        }

        dao.addXP(amount, placesDelta)
        dao.insertXpTransaction(XPTransaction(actionName = reason, xpAmount = amount, placeId = placeId))

        // Check for level progression
        val currentProfile = dao.getUserProfile().firstOrNull() ?: return@withContext
        val newXp = currentProfile.xp + amount
        val newLevel = calculateLevel(newXp)
        val newTitle = calculateLevelTitle(newLevel)

        if (newLevel > currentProfile.level) {
            dao.insertNotification(
                NotificationItem(
                    id = "notif_${System.currentTimeMillis()}",
                    title = "LEVEL UP! Level $newLevel $newTitle 🚀",
                    body = "Awesome exploration! You reached Level $newLevel ($newTitle). Keep going!",
                    iconEmoji = "🎉"
                )
            )
            dao.insertOrUpdateProfile(currentProfile.copy(xp = newXp, level = newLevel, levelTitle = newTitle))
        }
    }

    fun calculateLevel(xp: Int): Int {
        return (xp / 300).coerceAtLeast(1)
    }

    fun calculateLevelTitle(level: Int): String {
        return when {
            level >= 50 -> "Legend"
            level >= 20 -> "Local Expert"
            level >= 10 -> "Traveler"
            level >= 5 -> "Adventurer"
            else -> "Explorer"
        }
    }

    private suspend fun checkBadgesProgress() = withContext(Dispatchers.IO) {
        val visited = dao.getVisitedPlaces().firstOrNull() ?: emptyList()
        if (visited.isNotEmpty()) {
            dao.unlockBadge("b_first_discovery")
        }
        if (visited.count { it.category == CategoryType.COFFEE } >= 5) {
            dao.unlockBadge("b_coffee_hunter")
        }
        if (visited.count { it.category == CategoryType.FOOD } >= 5) {
            dao.unlockBadge("b_food_explorer")
        }
        if (visited.count { it.category == CategoryType.NATURE } >= 3) {
            dao.unlockBadge("b_nature_lover")
        }
    }

    fun filterPlaces(places: List<Place>, filter: PlaceFilter): List<Place> {
        return places.filter { place ->
            val matchesQuery = filter.query.isBlank() ||
                    place.name.contains(filter.query, ignoreCase = true) ||
                    place.arabicName.contains(filter.query, ignoreCase = true) ||
                    place.description.contains(filter.query, ignoreCase = true) ||
                    place.features.any { it.contains(filter.query, ignoreCase = true) }

            val matchesCategory = filter.category == CategoryType.ALL || place.category == filter.category

            val matchesBudget = filter.maxBudget == null ||
                    place.priceLevel.ordinal <= filter.maxBudget.ordinal

            val matchesDistance = place.distanceKm <= filter.maxDistanceKm

            val matchesTime = place.averageDurationMinutes <= filter.maxTimeMinutes

            val matchesCompanion = filter.companion == null ||
                    place.suitableCompanions.contains(filter.companion.name)

            val matchesOpenNow = !filter.openNowOnly || place.isOpenNow

            val matchesIndoor = !filter.indoorOnly || place.isIndoor
            val matchesOutdoor = !filter.outdoorOnly || !place.isIndoor

            val matchesRating = place.rating >= filter.minRating

            matchesQuery && matchesCategory && matchesBudget && matchesDistance &&
                    matchesTime && matchesCompanion && matchesOpenNow &&
                    matchesIndoor && matchesOutdoor && matchesRating
        }
    }

    fun openDirections(place: Place) {
        locationProvider.openDirections(place)
    }

    fun sharePlace(place: Place) {
        locationProvider.sharePlace(place)
    }

    companion object {
        @Volatile
        private var INSTANCE: WaygoRepository? = null

        fun getInstance(context: Context): WaygoRepository {
            return INSTANCE ?: synchronized(this) {
                val db = WaygoDatabase.getInstance(context)
                val loc = LocationAndPlacesProvider(context.applicationContext)
                val weather = WeatherService()
                val repo = WaygoRepository(db.waygoDao(), loc, weather)
                CoroutineScope(Dispatchers.IO).launch {
                    repo.initializeDatabaseIfEmpty()
                }
                INSTANCE = repo
                repo
            }
        }
    }
}
