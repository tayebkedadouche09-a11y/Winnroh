package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WaygoDao {
    // Places
    @Query("SELECT * FROM places ORDER BY rating DESC")
    fun getAllPlaces(): Flow<List<Place>>

    @Query("SELECT * FROM places WHERE id = :id LIMIT 1")
    fun getPlaceByIdFlow(id: String): Flow<Place?>

    @Query("SELECT * FROM places WHERE id = :id LIMIT 1")
    suspend fun getPlaceById(id: String): Place?

    @Query("SELECT * FROM places WHERE isSaved = 1 ORDER BY rating DESC")
    fun getSavedPlaces(): Flow<List<Place>>

    @Query("SELECT * FROM places WHERE isVisited = 1")
    fun getVisitedPlaces(): Flow<List<Place>>

    @Query("SELECT * FROM places WHERE category = :category ORDER BY rating DESC")
    fun getPlacesByCategory(category: String): Flow<List<Place>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaces(places: List<Place>)

    @Update
    suspend fun updatePlace(place: Place)

    @Query("UPDATE places SET isSaved = :isSaved WHERE id = :placeId")
    suspend fun toggleSavedPlace(placeId: String, isSaved: Boolean)

    @Query("UPDATE places SET isVisited = 1 WHERE id = :placeId")
    suspend fun markPlaceVisited(placeId: String)

    // User Profile
    @Query("SELECT * FROM user_profile WHERE id = 'current_user' LIMIT 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfile)

    @Query("UPDATE user_profile SET xp = xp + :xpDelta, placesDiscoveredCount = placesDiscoveredCount + :placesDelta WHERE id = 'current_user'")
    suspend fun addXP(xpDelta: Int, placesDelta: Int = 0)

    // Badges
    @Query("SELECT * FROM badges")
    fun getAllBadges(): Flow<List<Badge>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBadges(badges: List<Badge>)

    @Query("UPDATE badges SET isUnlocked = 1, unlockedAt = :timestamp, progressCurrent = progressMax WHERE id = :badgeId")
    suspend fun unlockBadge(badgeId: String, timestamp: Long = System.currentTimeMillis())

    // Reviews
    @Query("SELECT * FROM reviews WHERE placeId = :placeId ORDER BY timestamp DESC")
    fun getReviewsForPlace(placeId: String): Flow<List<Review>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: Review)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReviews(reviews: List<Review>)

    // Collections
    @Query("SELECT * FROM collections ORDER BY createdAt DESC")
    fun getCollections(): Flow<List<UserCollection>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollection(collection: UserCollection)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollectionItem(item: CollectionItem)

    @Query("SELECT p.* FROM places p INNER JOIN collection_items ci ON p.id = ci.placeId WHERE ci.collectionId = :collectionId")
    fun getPlacesForCollection(collectionId: String): Flow<List<Place>>

    // XP Transactions
    @Query("SELECT * FROM xp_transactions ORDER BY timestamp DESC LIMIT 20")
    fun getRecentXpTransactions(): Flow<List<XPTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertXpTransaction(tx: XPTransaction)

    // Notifications
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getNotifications(): Flow<List<NotificationItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationItem)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationRead(id: String)
}
