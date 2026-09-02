package com.example

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.ui.components.LevelUpCelebrationDialog
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.WaygoViewModel

sealed class Screen(val route: String, val titleKey: String, val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector, val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Home : Screen("home", "nav_home", Icons.Filled.Home, Icons.Outlined.Home)
    object Explore : Screen("explore", "nav_explore", Icons.Filled.Explore, Icons.Outlined.Explore)
    object Surprise : Screen("surprise", "nav_surprise", Icons.Filled.Casino, Icons.Outlined.Casino)
    object Map : Screen("map", "nav_map", Icons.Filled.Map, Icons.Outlined.Map)
    object Saved : Screen("saved", "nav_saved", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder)
    object Profile : Screen("profile", "nav_profile", Icons.Filled.Person, Icons.Outlined.Person)
    object PlaceDetail : Screen("place_detail/{placeId}", "", Icons.Filled.Place, Icons.Outlined.Place) {
        fun createRoute(placeId: String) = "place_detail/$placeId"
    }
    object AiConcierge : Screen("ai_concierge", "ai_concierge_title", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome)
    object Onboarding : Screen("onboarding", "", Icons.Filled.Star, Icons.Outlined.Star)
    object Notifications : Screen("notifications", "", Icons.Filled.Notifications, Icons.Outlined.Notifications)
    object Admin : Screen("admin", "", Icons.Filled.AdminPanelSettings, Icons.Outlined.AdminPanelSettings)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: WaygoViewModel = viewModel()
            val isDarkMode by viewModel.isDarkMode.collectAsState()
            val language by viewModel.currentLanguage.collectAsState()
            val layoutDirection = if (language.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                MyApplicationTheme(darkTheme = isDarkMode) {
                    WaygoAppRoot(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun WaygoAppRoot(viewModel: WaygoViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val language by viewModel.currentLanguage.collectAsState()

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            viewModel.useDeviceGpsLocation()
        }
    }

    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    val levelUpLevel by viewModel.levelUpCelebration.collectAsState()

    val bottomNavItems = listOf(
        Screen.Home,
        Screen.Explore,
        Screen.Surprise,
        Screen.Map,
        Screen.Saved,
        Screen.Profile
    )

    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp,
                        modifier = Modifier.testTag("main_bottom_nav")
                    ) {
                        bottomNavItems.forEach { screen ->
                            val selected = currentRoute == screen.route
                            val isSurprise = screen == Screen.Surprise

                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    if (currentRoute != screen.route) {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = {
                                    if (isSurprise) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .background(
                                                    Brush.linearGradient(listOf(CoralPrimary, AmberAccent)),
                                                    CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (selected) screen.selectedIcon else screen.unselectedIcon,
                                                contentDescription = Localization.get(screen.titleKey, language),
                                                tint = Color.White,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    } else {
                                        Icon(
                                            imageVector = if (selected) screen.selectedIcon else screen.unselectedIcon,
                                            contentDescription = Localization.get(screen.titleKey, language),
                                            tint = if (selected) CoralPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                label = {
                                    Text(
                                        text = Localization.get(screen.titleKey, language),
                                        fontSize = 10.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selected) CoralPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = if (isSurprise) Color.Transparent else CoralPrimary.copy(alpha = 0.12f)
                                )
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(
                        viewModel = viewModel,
                        onNavigateToExplore = { navController.navigate(Screen.Explore.route) },
                        onNavigateToSurprise = { navController.navigate(Screen.Surprise.route) },
                        onNavigateToPlaceDetail = { id -> navController.navigate(Screen.PlaceDetail.createRoute(id)) },
                        onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) }
                    )
                }

                composable(Screen.Explore.route) {
                    ExploreScreen(
                        viewModel = viewModel,
                        onNavigateToPlaceDetail = { id -> navController.navigate(Screen.PlaceDetail.createRoute(id)) }
                    )
                }

                composable(Screen.Surprise.route) {
                    SurpriseMeScreen(
                        viewModel = viewModel,
                        onNavigateToPlaceDetail = { id -> navController.navigate(Screen.PlaceDetail.createRoute(id)) }
                    )
                }

                composable(Screen.Map.route) {
                    MapViewScreen(
                        viewModel = viewModel,
                        onNavigateToPlaceDetail = { id -> navController.navigate(Screen.PlaceDetail.createRoute(id)) }
                    )
                }

                composable(Screen.Saved.route) {
                    SavedCollectionsScreen(
                        viewModel = viewModel,
                        onNavigateToPlaceDetail = { id -> navController.navigate(Screen.PlaceDetail.createRoute(id)) },
                        onNavigateToExplore = { navController.navigate(Screen.Explore.route) }
                    )
                }

                composable(Screen.Profile.route) {
                    ProfileScreen(
                        viewModel = viewModel,
                        onNavigateToAdmin = { navController.navigate(Screen.Admin.route) }
                    )
                }

                composable(
                    route = Screen.PlaceDetail.route,
                    arguments = listOf(navArgument("placeId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val placeId = backStackEntry.arguments?.getString("placeId") ?: ""
                    PlaceDetailScreen(
                        placeId = placeId,
                        viewModel = viewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.AiConcierge.route) {
                    AiConciergeScreen(
                        viewModel = viewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToPlaceDetail = { id -> navController.navigate(Screen.PlaceDetail.createRoute(id)) }
                    )
                }

                composable(Screen.Onboarding.route) {
                    OnboardingScreen(
                        viewModel = viewModel,
                        onComplete = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Onboarding.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.Notifications.route) {
                    NotificationsScreen(
                        viewModel = viewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.Admin.route) {
                    AdminModerationScreen(
                        viewModel = viewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }

        // Floating AI Concierge "WAYGO Genie" Button on main pages
        if (showBottomBar && currentRoute != Screen.Surprise.route) {
            FloatingConciergePill(
                onClick = { navController.navigate(Screen.AiConcierge.route) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 76.dp, end = 16.dp)
            )
        }

        // Level Up Celebration Modal
        if (levelUpLevel != null) {
            LevelUpCelebrationDialog(
                newLevel = levelUpLevel!!,
                levelTitle = "Traveler",
                onDismiss = { viewModel.levelUpCelebration.value = null }
            )
        }
    }
}

@Composable
fun FloatingConciergePill(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Surface(
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 8.dp,
        color = Color.Transparent,
        modifier = modifier
            .scale(scale)
            .testTag("floating_concierge_btn")
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(listOf(CoralPrimary, VioletSecondary))
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "AI Concierge",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "AI Genie 🤖",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp
                )
            }
        }
    }
}
