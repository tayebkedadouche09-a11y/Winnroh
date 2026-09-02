package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategoryType
import com.example.ui.theme.*

@Composable
fun CategoryPill(
    category: CategoryType,
    isSelected: Boolean,
    language: AppLanguage,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val label = when (language) {
        AppLanguage.ARABIC -> category.labelAr
        AppLanguage.FRENCH -> category.labelFr
        AppLanguage.ENGLISH -> category.labelEn
    }

    val unselectedTint = when (category) {
        CategoryType.FOOD -> MoodFoodBg
        CategoryType.COFFEE -> MoodCoffeeBg
        CategoryType.GAMING -> MoodGamingBg
        CategoryType.ENTERTAINMENT -> MoodCinemaBg
        CategoryType.NATURE -> Color(0xFFE0F2F1)
        CategoryType.SPORTS -> Color(0xFFF3E5F5)
        CategoryType.SHOPPING -> Color(0xFFFCE4EC)
        CategoryType.DATE -> Color(0xFFFBE9E7)
        CategoryType.MUSIC -> Color(0xFFEDE7F6)
        CategoryType.FAMILY -> Color(0xFFE8EAF6)
        CategoryType.TOURISM -> Color(0xFFE0F7FA)
        CategoryType.NIGHTLIFE -> Color(0xFFEDE7F6)
        CategoryType.RELAXATION -> Color(0xFFE8F5E9)
        CategoryType.ALL -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) CoralPrimary else unselectedTint,
        label = "pill_bg"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
        label = "pill_text"
    )

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor,
        shadowElevation = if (isSelected) 3.dp else 0.dp,
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, CoralPrimary.copy(alpha = 0.08f)),
        modifier = modifier
            .testTag("cat_pill_${category.id}")
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Text(
                text = category.emoji,
                fontSize = 15.sp,
                modifier = Modifier.padding(end = 6.dp)
            )
            Text(
                text = label,
                color = contentColor,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                fontSize = 13.sp
            )
        }
    }
}

