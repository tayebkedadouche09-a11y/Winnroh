package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: String = "current_user",
    val username: String = "explorer",
    val displayName: String = "Explorer",
    val bio: String = "Discovering real places and hidden gems around the world 🗺️✨",
    val avatarUrl: String = "",
    val country: String = "Global",
    val city: String = "Worldwide",
    val selectedInterests: List<String> = listOf("COFFEE", "FOOD", "NATURE", "CINEMA", "TOURISM"),
    val typicalBudget: BudgetLevel = BudgetLevel.MODERATE,
    val xp: Int = 0,
    val level: Int = 1,
    val levelTitle: String = "Novice",
    val placesDiscoveredCount: Int = 0,
    val reviewsCount: Int = 0,
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val isPremium: Boolean = false,
    val hasCompletedOnboarding: Boolean = true
)

enum class BadgeCategory {
    EXPLORATION,
    FOOD_AND_DRINK,
    GAMING,
    NIGHT_LIFE,
    SOCIAL,
    SPECIAL
}

@Entity(tableName = "badges")
data class Badge(
    @PrimaryKey val id: String,
    val nameEn: String,
    val nameAr: String,
    val descEn: String,
    val descAr: String,
    val iconEmoji: String,
    val category: BadgeCategory,
    val xpReward: Int,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null,
    val progressCurrent: Int = 0,
    val progressMax: Int = 1
)

@Entity(tableName = "xp_transactions")
data class XPTransaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val actionName: String,
    val xpAmount: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val placeId: String? = null
)

@Entity(tableName = "reviews")
data class Review(
    @PrimaryKey val id: String,
    val placeId: String,
    val userName: String,
    val userAvatarUrl: String,
    val rating: Double,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val helpfulLikesCount: Int = 0,
    val isVerifiedVisit: Boolean = true,
    val photoUrl: String? = null
)

@Entity(tableName = "collections")
data class UserCollection(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val coverEmoji: String = "📍",
    val isPublic: Boolean = true,
    val itemsCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "collection_items", primaryKeys = ["collectionId", "placeId"])
data class CollectionItem(
    val collectionId: String,
    val placeId: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationItem(
    @PrimaryKey val id: String,
    val title: String,
    val body: String,
    val iconEmoji: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val actionRoute: String? = null
)
