package com.example.data.repository

import android.content.Context
import com.example.data.local.SampleData
import com.example.data.local.WaygoDao
import com.example.data.local.WaygoDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WaygoRepository(private val dao: WaygoDao) {

    val allPlaces: Flow<List<Place>> = dao.getAllPlaces()
    val savedPlaces: Flow<List<Place>> = dao.getSavedPlaces()
    val visitedPlaces: Flow<List<Place>> = dao.getVisitedPlaces()
    val userProfile: Flow<UserProfile?> = dao.getUserProfile()
    val allBadges: Flow<List<Badge>> = dao.getAllBadges()
    val collections: Flow<List<UserCollection>> = dao.getCollections()
    val recentTransactions: Flow<List<XPTransaction>> = dao.getRecentXpTransactions()
    val notifications: Flow<List<NotificationItem>> = dao.getNotifications()

    suspend fun initializeDatabaseIfEmpty() = withContext(Dispatchers.IO) {
        val existingProfile = dao.getUserProfile().firstOrNull()
        if (existingProfile == null) {
            dao.insertOrUpdateProfile(UserProfile())
            dao.insertPlaces(SampleData.initialPlaces)
            dao.insertBadges(SampleData.initialBadges)
            dao.insertReviews(SampleData.initialReviews)
            SampleData.initialCollections.forEach { dao.insertCollection(it) }
            SampleData.initialNotifications.forEach { dao.insertNotification(it) }

            // Add sample collection items
            dao.insertCollectionItem(CollectionItem("col_1", "p_1"))
            dao.insertCollectionItem(CollectionItem("col_1", "p_3"))
            dao.insertCollectionItem(CollectionItem("col_2", "p_2"))
            dao.insertCollectionItem(CollectionItem("col_3", "p_3"))
        }
    }

    fun getPlaceById(id: String): Flow<Place?> = dao.getPlaceByIdFlow(id)

    fun getReviewsForPlace(placeId: String): Flow<List<Review>> = dao.getReviewsForPlace(placeId)

    suspend fun toggleSavePlace(placeId: String, currentSaved: Boolean) = withContext(Dispatchers.IO) {
        dao.toggleSavedPlace(placeId, !currentSaved)
        if (!currentSaved) {
            awardXP(10, "Saved place to favorites")
        }
    }

    suspend fun markPlaceVisited(placeId: String) = withContext(Dispatchers.IO) {
        dao.markPlaceVisited(placeId)
        awardXP(50, "Discovered and checked in to a new place", placesDelta = 1)
        checkBadgesProgress()
    }

    suspend fun addReview(placeId: String, rating: Double, comment: String, userName: String) = withContext(Dispatchers.IO) {
        val review = Review(
            id = "rev_${System.currentTimeMillis()}",
            placeId = placeId,
            userName = userName,
            userAvatarUrl = "",
            rating = rating,
            text = comment,
            timestamp = System.currentTimeMillis(),
            helpfulLikesCount = 1,
            isVerifiedVisit = true
        )
        dao.insertReview(review)
        awardXP(30, "Published a community review")
        checkBadgesProgress()
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
        awardXP(25, "Created a themed collection")
    }

    suspend fun updateProfile(profile: UserProfile) = withContext(Dispatchers.IO) {
        dao.insertOrUpdateProfile(profile)
    }

    suspend fun awardXP(amount: Int, reason: String, placeId: String? = null, placesDelta: Int = 0) = withContext(Dispatchers.IO) {
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

    private fun calculateLevel(xp: Int): Int {
        return (xp / 300).coerceAtLeast(1)
    }

    private fun calculateLevelTitle(level: Int): String {
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
        if (visited.size >= 1) {
            dao.unlockBadge("b_first_discovery")
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

    companion object {
        @Volatile
        private var INSTANCE: WaygoRepository? = null

        fun getInstance(context: Context): WaygoRepository {
            return INSTANCE ?: synchronized(this) {
                val db = WaygoDatabase.getInstance(context)
                val repo = WaygoRepository(db.waygoDao())
                CoroutineScope(Dispatchers.IO).launch {
                    repo.initializeDatabaseIfEmpty()
                }
                INSTANCE = repo
                repo
            }
        }
    }
}
