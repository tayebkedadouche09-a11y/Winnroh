package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AppCurrency
import com.example.data.model.Badge
import com.example.ui.components.BadgeCard
import com.example.ui.components.XPProgressBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.WaygoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: WaygoViewModel,
    onNavigateToAdmin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val authUser by viewModel.authUser.collectAsState()
    val badges by viewModel.allBadges.collectAsState()
    val language by viewModel.currentLanguage.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val currency by viewModel.selectedCurrency.collectAsState()

    var selectedBadgeDetail by remember { mutableStateOf<Badge?>(null) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showAuthDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("profile_screen"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Top Profile Card Header
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Avatar with Glowing Border
                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .background(
                            Brush.linearGradient(listOf(CoralPrimary, AmberAccent)),
                            CircleShape
                        )
                        .padding(3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surface, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🧑‍🚀", fontSize = 42.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = authUser?.displayName ?: userProfile?.displayName ?: "Ahmed",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black
                )

                Text(
                    text = "@${authUser?.username ?: userProfile?.username ?: "ahmed_explorer"} • ${userProfile?.country ?: "Global"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (authUser?.role == "admin" || authUser?.role == "business") {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = CoralPrimary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "👑 ${authUser?.role?.uppercase()}",
                            color = CoralPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = userProfile?.bio ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Stats Row (Discovered, Reviews, XP)
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatItem(
                            count = "${userProfile?.placesDiscoveredCount ?: 0}",
                            label = Localization.get("places_discovered", language)
                        )
                        Divider(modifier = Modifier.height(28.dp).width(1.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        StatItem(
                            count = "${userProfile?.reviewsCount ?: 0}",
                            label = Localization.get("reviews", language)
                        )
                        Divider(modifier = Modifier.height(28.dp).width(1.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        StatItem(
                            count = "${userProfile?.xp ?: 0}",
                            label = Localization.get("xp_points", language)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                XPProgressBar(userProfile = userProfile)
            }
        }

        // Badges Section
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🏆 ${Localization.get("badges", language)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "${badges.count { it.isUnlocked }}/${badges.size} Unlocked",
                    fontSize = 12.sp,
                    color = CoralPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Badges Grid
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                badges.chunked(3).forEach { rowBadges ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowBadges.forEach { badge ->
                            BadgeCard(
                                badge = badge,
                                language = language,
                                onClick = { selectedBadgeDetail = badge },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Preferences & System Settings Section
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Preferences & System ⚙️",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Language item
                    ListItem(
                        headlineContent = { Text("Language / اللغة / Langue") },
                        supportingContent = { Text(language.displayName) },
                        leadingContent = { Icon(Icons.Outlined.Language, contentDescription = null, tint = CoralPrimary) },
                        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                        modifier = Modifier
                            .testTag("setting_language_item")
                            .clickable { showLanguageDialog = true }
                    )

                    Divider(color = MaterialTheme.colorScheme.surfaceVariant)

                    // Currency item
                    ListItem(
                        headlineContent = { Text(Localization.get("currency_selector", language)) },
                        supportingContent = { Text(currency.displayName) },
                        leadingContent = { Icon(Icons.Outlined.AttachMoney, contentDescription = null, tint = AmberAccent) },
                        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                        modifier = Modifier
                            .testTag("setting_currency_item")
                            .clickable { showCurrencyDialog = true }
                    )

                    Divider(color = MaterialTheme.colorScheme.surfaceVariant)

                    // Dark Mode item
                    ListItem(
                        headlineContent = { Text("Dark Theme") },
                        leadingContent = { Icon(Icons.Outlined.DarkMode, contentDescription = null, tint = VioletSecondary) },
                        trailingContent = {
                            Switch(
                                checked = isDarkMode,
                                onCheckedChange = { viewModel.toggleDarkMode() },
                                colors = SwitchDefaults.colors(checkedThumbColor = CoralPrimary)
                            )
                        }
                    )

                    Divider(color = MaterialTheme.colorScheme.surfaceVariant)

                    // Business & Moderation Portal
                    ListItem(
                        headlineContent = { Text(Localization.get("admin_portal", language)) },
                        supportingContent = { Text("Manage listings & user reports") },
                        leadingContent = { Icon(Icons.Outlined.AdminPanelSettings, contentDescription = null, tint = AmberAccent) },
                        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                        modifier = Modifier
                            .testTag("admin_portal_item")
                            .clickable(onClick = onNavigateToAdmin)
                    )
                }
            }
        }

        // Account & Legal (Google Play Store Readiness)
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Account & Legal 🔒",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    ListItem(
                        headlineContent = { Text(Localization.get("privacy_policy", language)) },
                        leadingContent = { Icon(Icons.Outlined.Policy, contentDescription = null, tint = Color.Gray) },
                        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                        modifier = Modifier.clickable { showPrivacyPolicyDialog = true }
                    )

                    Divider(color = MaterialTheme.colorScheme.surfaceVariant)

                    ListItem(
                        headlineContent = { Text(Localization.get("terms_of_service", language)) },
                        leadingContent = { Icon(Icons.Outlined.Description, contentDescription = null, tint = Color.Gray) },
                        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                        modifier = Modifier.clickable { showTermsDialog = true }
                    )

                    Divider(color = MaterialTheme.colorScheme.surfaceVariant)

                    if (authUser != null) {
                        ListItem(
                            headlineContent = { Text(Localization.get("auth_logout", language), color = CoralPrimary) },
                            leadingContent = { Icon(Icons.Outlined.Logout, contentDescription = null, tint = CoralPrimary) },
                            modifier = Modifier.clickable { viewModel.logout() }
                        )

                        Divider(color = MaterialTheme.colorScheme.surfaceVariant)

                        ListItem(
                            headlineContent = { Text(Localization.get("auth_delete_account", language), color = Color(0xFFE53935)) },
                            leadingContent = { Icon(Icons.Outlined.DeleteForever, contentDescription = null, tint = Color(0xFFE53935)) },
                            modifier = Modifier.clickable { showDeleteAccountDialog = true }
                        )
                    } else {
                        ListItem(
                            headlineContent = { Text(Localization.get("auth_login", language), color = CoralPrimary, fontWeight = FontWeight.Bold) },
                            leadingContent = { Icon(Icons.Outlined.Login, contentDescription = null, tint = CoralPrimary) },
                            modifier = Modifier.clickable { showAuthDialog = true }
                        )
                    }
                }
            }
        }
    }

    // Currency Selector Dialog
    if (showCurrencyDialog) {
        Dialog(onDismissRequest = { showCurrencyDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                    Text(text = Localization.get("currency_selector", language), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(14.dp))
                    AppCurrency.values().forEach { c ->
                        val isSel = c == currency
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSel) CoralPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(12.dp)).clickable {
                                viewModel.setCurrency(c)
                                showCurrencyDialog = false
                            }
                        ) {
                            Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "${c.displayName} (${c.symbol})", fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium, color = if (isSel) CoralPrimary else MaterialTheme.colorScheme.onSurface)
                                if (isSel) { Icon(Icons.Default.Check, contentDescription = null, tint = CoralPrimary) }
                            }
                        }
                    }
                }
            }
        }
    }

    // Language Selector Dialog
    if (showLanguageDialog) {
        Dialog(onDismissRequest = { showLanguageDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                    Text(text = "Choose Language / اختر اللغة", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    AppLanguage.values().forEach { lang ->
                        val isSel = lang == language
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSel) CoralPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(12.dp)).clickable {
                                viewModel.setLanguage(lang)
                                showLanguageDialog = false
                            }
                        ) {
                            Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(text = lang.displayName, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium, color = if (isSel) CoralPrimary else MaterialTheme.colorScheme.onSurface)
                                if (isSel) { Icon(Icons.Default.Check, contentDescription = null, tint = CoralPrimary) }
                            }
                        }
                    }
                }
            }
        }
    }

    // Badge Details Dialog
    if (selectedBadgeDetail != null) {
        val b = selectedBadgeDetail!!
        val name = if (language == AppLanguage.ARABIC) b.nameAr else b.nameEn
        val desc = if (language == AppLanguage.ARABIC) b.descAr else b.descEn

        Dialog(onDismissRequest = { selectedBadgeDetail = null }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth().padding(24.dp)
                ) {
                    Box(
                        modifier = Modifier.size(72.dp).background(AmberAccent.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = b.iconEmoji, fontSize = 38.sp)
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(text = name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = desc, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(shape = RoundedCornerShape(10.dp), color = CoralPrimary.copy(alpha = 0.12f)) {
                        Text(text = "+${b.xpReward} XP Reward", color = CoralPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Button(onClick = { selectedBadgeDetail = null }, colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary), shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                        Text("Close")
                    }
                }
            }
        }
    }

    // Google Play Account Deletion Confirmation
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = { Text(Localization.get("auth_delete_account", language)) },
            text = { Text(Localization.get("auth_delete_confirm", language)) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAccount()
                        showDeleteAccountDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                ) {
                    Text(Localization.get("delete", language))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text(Localization.get("cancel", language))
                }
            }
        )
    }

    // Privacy Policy Dialog
    if (showPrivacyPolicyDialog) {
        Dialog(onDismissRequest = { showPrivacyPolicyDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(text = Localization.get("privacy_policy", language), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "WAYGO is committed to respecting and protecting your privacy. We only use location permissions to calculate distances to nearby experiences and provide local weather recommendations. Your data is never sold to third parties. You have full right to delete your account and associated local data at any time from this screen.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { showPrivacyPolicyDialog = false }, modifier = Modifier.fillMaxWidth()) {
                        Text("I Understand")
                    }
                }
            }
        }
    }

    // Terms of Service Dialog
    if (showTermsDialog) {
        Dialog(onDismissRequest = { showTermsDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(text = Localization.get("terms_of_service", language), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "By using WAYGO, you agree to discover, review, and share public places respectfully. Fraudulent reviews, hate speech, or abuse of the gamification XP system are strictly prohibited and subject to moderation removal.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { showTermsDialog = false }, modifier = Modifier.fillMaxWidth()) {
                        Text("Agree & Close")
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(
    count: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = count,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = CoralPrimary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

