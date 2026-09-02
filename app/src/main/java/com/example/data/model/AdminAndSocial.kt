package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reports")
data class ReportItem(
    @PrimaryKey val id: String,
    val reporterUserId: String,
    val targetType: String, // "PLACE", "REVIEW", "USER"
    val targetId: String,
    val reason: String,
    val status: String = "PENDING", // "PENDING", "RESOLVED", "DISMISSED"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "business_accounts")
data class BusinessAccount(
    @PrimaryKey val id: String,
    val businessName: String,
    val ownerEmail: String,
    val placeId: String,
    val isVerified: Boolean = false,
    val promotionalOffer: String? = null,
    val contactPhone: String? = null,
    val websiteUrl: String? = null,
    val claimedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "visited_logs", primaryKeys = ["userId", "placeId"])
data class VisitedPlaceLog(
    val userId: String,
    val placeId: String,
    val visitedAt: Long = System.currentTimeMillis(),
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

@Entity(tableName = "social_follows", primaryKeys = ["followerId", "followingId"])
data class SocialFollow(
    val followerId: String,
    val followingId: String,
    val createdAt: Long = System.currentTimeMillis()
)
