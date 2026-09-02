package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.BudgetLevel
import com.example.data.model.CategoryType
import com.example.ui.theme.*
import com.example.ui.viewmodel.WaygoViewModel

@Composable
fun OnboardingScreen(
    viewModel: WaygoViewModel,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val language by viewModel.currentLanguage.collectAsState()
    var currentStep by remember { mutableStateOf(0) }

    val selectedInterests = remember { mutableStateListOf("COFFEE", "GAMING", "FOOD") }
    var selectedBudget by remember { mutableStateOf(BudgetLevel.MODERATE) }

    val totalSteps = 4

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .testTag("onboarding_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Step Indicators & Skip Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                (0 until totalSteps).forEach { stepIndex ->
                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .width(if (stepIndex == currentStep) 28.dp else 10.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                if (stepIndex == currentStep) CoralPrimary else MaterialTheme.colorScheme.surfaceVariant
                            )
                    )
                }
            }

            if (currentStep < totalSteps - 1) {
                Text(
                    text = Localization.get("skip", language),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onComplete() }
                )
            }
        }

        // Body Content Based On Current Step
        when (currentStep) {
            0 -> {
                // Step 1: Welcome Hero
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Card(
                        shape = RoundedCornerShape(28.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_hero_explore),
                            contentDescription = "Welcome",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Text(
                        text = "WAYGO • وين نروح؟",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = CoralPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = Localization.get("onboarding_sub_1", language),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
            1 -> {
                // Step 2: Interests Selection
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = Localization.get("onboarding_title_2", language),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = Localization.get("onboarding_sub_2", language),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Grid of Interests
                    val interestCategories = listOf(
                        CategoryType.COFFEE, CategoryType.FOOD, CategoryType.GAMING,
                        CategoryType.NATURE, CategoryType.ENTERTAINMENT, CategoryType.SPORTS,
                        CategoryType.SHOPPING, CategoryType.DATE
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        interestCategories.chunked(2).forEach { pair ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                pair.forEach { cat ->
                                    val isSelected = selectedInterests.contains(cat.name)
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = if (isSelected) CoralPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                                        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, CoralPrimary) else null,
                                        shadowElevation = 1.dp,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(52.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .clickable {
                                                if (isSelected) selectedInterests.remove(cat.name) else selectedInterests.add(cat.name)
                                            }
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 12.dp)
                                        ) {
                                            Text(text = cat.emoji, fontSize = 20.sp)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = cat.labelEn.split(" ").first(),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            2 -> {
                // Step 3: Budget Preference
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = Localization.get("onboarding_title_3", language),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = Localization.get("onboarding_sub_3", language),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    BudgetLevel.values().forEach { b ->
                        val isSel = selectedBudget == b
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = if (isSel) CoralPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                            border = if (isSel) androidx.compose.foundation.BorderStroke(2.dp, CoralPrimary) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp)
                                .height(56.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .clickable { selectedBudget = b }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 18.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = b.symbol, fontWeight = FontWeight.Black, fontSize = 16.sp, color = CoralPrimary)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(text = b.labelEn, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }

                                if (isSel) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = CoralPrimary)
                                }
                            }
                        }
                    }
                }
            }
            3 -> {
                // Step 4: Ready to explore
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .background(
                                Brush.linearGradient(listOf(CoralPrimary, VioletSecondary)),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "✨", fontSize = 54.sp)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = Localization.get("onboarding_title_4", language),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = Localization.get("onboarding_sub_4", language),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            }
        }

        // Bottom Action Button
        Button(
            onClick = {
                if (currentStep < totalSteps - 1) {
                    currentStep++
                } else {
                    viewModel.completeOnboarding(selectedInterests.toList(), selectedBudget)
                    onComplete()
                }
            },
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("onboarding_next_btn")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (currentStep == totalSteps - 1) Localization.get("get_started", language) else Localization.get("next", language),
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White)
            }
        }
    }
}
