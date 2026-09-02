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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlace(place: Place)

    @Update
    suspend fun updatePlace(place: Place)

    @Query("UPDATE places SET isSaved = :isSaved WHERE id = :placeId")
    suspend fun toggleSavedPlace(placeId: String, isSaved: Boolean)

    @Query("UPDATE places SET isVisited = 1 WHERE id = :placeId")
    suspend fun markPlaceVisited(placeId: String)

    @Query("UPDATE places SET isSponsored = :isSponsored WHERE id = :placeId")
    suspend fun setPlaceSponsored(placeId: String, isSponsored: Boolean)

    // User Profile
    @Query("SELECT * FROM user_profile WHERE id = 'current_user' LIMIT 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfile)

    @Query("UPDATE user_profile SET xp = xp + :xpDelta, placesDiscoveredCount = placesDiscoveredCount + :placesDelta WHERE id = 'current_user'")
    suspend fun addXP(xpDelta: Int, placesDelta: Int = 0)

    @Query("DELETE FROM user_profile WHERE id = 'current_user'")
    suspend fun deleteUserProfile()

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

    @Query("SELECT * FROM reviews ORDER BY timestamp DESC")
    fun getAllReviews(): Flow<List<Review>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: Review)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReviews(reviews: List<Review>)

    @Query("UPDATE reviews SET helpfulLikesCount = helpfulLikesCount + 1 WHERE id = :reviewId")
    suspend fun upvoteReview(reviewId: String)

    @Query("DELETE FROM reviews WHERE id = :reviewId")
    suspend fun deleteReview(reviewId: String)

    // Collections
    @Query("SELECT * FROM collections ORDER BY createdAt DESC")
    fun getCollections(): Flow<List<UserCollection>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollection(collection: UserCollection)

    @Query("DELETE FROM collections WHERE id = :collectionId")
    suspend fun deleteCollection(collectionId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollectionItem(item: CollectionItem)

    @Query("DELETE FROM collection_items WHERE collectionId = :collectionId AND placeId = :placeId")
    suspend fun removeCollectionItem(collectionId: String, placeId: String)

    @Query("SELECT p.* FROM places p INNER JOIN collection_items ci ON p.id = ci.placeId WHERE ci.collectionId = :collectionId")
    fun getPlacesForCollection(collectionId: String): Flow<List<Place>>

    // XP Transactions & Deduplication
    @Query("SELECT * FROM xp_transactions ORDER BY timestamp DESC LIMIT 30")
    fun getRecentXpTransactions(): Flow<List<XPTransaction>>

    @Query("SELECT COUNT(*) FROM xp_transactions WHERE actionName = :actionName AND placeId = :placeId")
    suspend fun countTransactionsForPlaceAction(actionName: String, placeId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertXpTransaction(tx: XPTransaction)

    // Notifications
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getNotifications(): Flow<List<NotificationItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationItem)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationRead(id: String)

    @Query("DELETE FROM notifications")
    suspend fun clearAllNotifications()

    // Reports & Moderation
    @Query("SELECT * FROM reports ORDER BY timestamp DESC")
    fun getAllReports(): Flow<List<ReportItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ReportItem)

    @Query("UPDATE reports SET status = :status WHERE id = :reportId")
    suspend fun updateReportStatus(reportId: String, status: String)

    // Business Accounts
    @Query("SELECT * FROM business_accounts WHERE placeId = :placeId LIMIT 1")
    fun getBusinessAccountForPlace(placeId: String): Flow<BusinessAccount?>

    @Query("SELECT * FROM business_accounts ORDER BY claimedAt DESC")
    fun getAllBusinessAccounts(): Flow<List<BusinessAccount>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBusinessAccount(account: BusinessAccount)

    @Query("UPDATE business_accounts SET isVerified = :isVerified WHERE id = :id")
    suspend fun updateBusinessVerification(id: String, isVerified: Boolean)

    // Visited Logs (Anti-Farming)
    @Query("SELECT COUNT(*) FROM visited_logs WHERE userId = :userId AND placeId = :placeId")
    suspend fun hasUserVisitedPlace(userId: String, placeId: String): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertVisitedLog(log: VisitedPlaceLog)

    // Social Follows
    @Query("SELECT COUNT(*) FROM social_follows WHERE followerId = :followerId AND followingId = :followingId")
    suspend fun isFollowingUser(followerId: String, followingId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun followUser(follow: SocialFollow)

    @Query("DELETE FROM social_follows WHERE followerId = :followerId AND followingId = :followingId")
    suspend fun unfollowUser(followerId: String, followingId: String)
}
