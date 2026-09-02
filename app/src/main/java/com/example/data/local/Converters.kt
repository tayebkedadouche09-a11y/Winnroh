package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.BadgeCategory
import com.example.data.model.BudgetLevel
import com.example.data.model.CategoryType

class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return value?.joinToString(";;;") ?: ""
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        return value.split(";;;").filter { it.isNotBlank() }
    }

    @TypeConverter
    fun fromCategoryType(value: CategoryType?): String {
        return value?.name ?: CategoryType.ALL.name
    }

    @TypeConverter
    fun toCategoryType(value: String?): CategoryType {
        return try {
            if (value != null) CategoryType.valueOf(value) else CategoryType.ALL
        } catch (e: Exception) {
            CategoryType.ALL
        }
    }

    @TypeConverter
    fun fromBudgetLevel(value: BudgetLevel?): String {
        return value?.name ?: BudgetLevel.MODERATE.name
    }

    @TypeConverter
    fun toBudgetLevel(value: String?): BudgetLevel {
        return try {
            if (value != null) BudgetLevel.valueOf(value) else BudgetLevel.MODERATE
        } catch (e: Exception) {
            BudgetLevel.MODERATE
        }
    }

    @TypeConverter
    fun fromBadgeCategory(value: BadgeCategory?): String {
        return value?.name ?: BadgeCategory.EXPLORATION.name
    }

    @TypeConverter
    fun toBadgeCategory(value: String?): BadgeCategory {
        return try {
            if (value != null) BadgeCategory.valueOf(value) else BadgeCategory.EXPLORATION
        } catch (e: Exception) {
            BadgeCategory.EXPLORATION
        }
    }
}
