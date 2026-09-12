package com.example.mywallpaper.ui.navigation

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.mywallpaper.Wallpaper
import com.example.mywallpaper.ui.ai.AICreateScreen
import com.example.mywallpaper.ui.ai.AICreateViewModel
import com.example.mywallpaper.ui.ai.AIGeneratingScreen
import com.example.mywallpaper.ui.ai.AIHistoryScreen
import com.example.mywallpaper.ui.ai.AIResultScreen
import com.example.mywallpaper.ui.auth.AuthViewModel
import com.example.mywallpaper.ui.auth.SignInScreen
import com.example.mywallpaper.ui.auth.SignUpScreen
import com.example.mywallpaper.ui.auth.SplashScreen
import com.example.mywallpaper.ui.auth.WelcomeScreen
import com.example.mywallpaper.ui.category.CategoryScreen
import com.example.mywallpaper.ui.explore.ExploreScreen
import com.example.mywallpaper.ui.explore.ExploreViewModel
import com.example.mywallpaper.ui.favorites.FavoritesScreen
import com.example.mywallpaper.ui.home.HomeScreen
import com.example.mywallpaper.ui.home.HomeViewModel
import com.example.mywallpaper.ui.notifications.NotificationSettingsScreen
import com.example.mywallpaper.ui.notifications.NotificationsScreen
import com.example.mywallpaper.ui.notifications.NotificationsViewModel
import com.example.mywallpaper.ui.profile.AboutScreen
import com.example.mywallpaper.ui.profile.AppearanceScreen
import com.example.mywallpaper.ui.profile.PrivacyPolicyScreen
import com.example.mywallpaper.ui.profile.ProfileScreen
import com.example.mywallpaper.ui.profile.ProfileViewModel
import com.example.mywallpaper.ui.profile.SettingsScreen
import com.example.mywallpaper.ui.theme.Background
import com.example.mywallpaper.ui.wallpaper.WallpaperDetailByIdScreen
import com.example.mywallpaper.ui.wallpaper.WallpaperDetailScreen
import com.example.mywallpaper.ui.wallpaper.WallpaperDetailViewModel
import com.google.firebase.auth.FirebaseAuth
import java.net.URLDecoder

val bottomNavRoutes = setOf(
    Routes.HOME,
    Routes.EXPLORE,
    Routes.AI_CREATE,
    Routes.FAVORITES,
    Routes.PROFILE
)

val fullscreenRoutes = setOf(
    Routes.WALLPAPER_DETAIL,
    Routes.WALLPAPER_BY_ID,
    Routes.CATEGORY,
    Routes.AI_GENERATING,
    Routes.AI_RESULT,
    Routes.NOTIFICATIONS,
    Routes.NOTIFICATION_SETTINGS,
    Routes.SETTINGS,
    Routes.APPEARANCE,
    Routes.PRIVACY_POLICY,
    Routes.ABOUT
)

@Composable
fun AppNavGraph(
    pendingWallpaperId: String? = null,
    onClearPendingWallpaperId: () -> Unit = {}
) {
    val navController = rememberNavController()
    val navBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStack?.destination?.route
    val context = LocalContext.current

    // Reactive Firebase Auth state
    var isLoggedIn by remember { mutableStateOf(FirebaseAuth.getInstance().currentUser != null) }
    DisposableEffect(Unit) {
        val auth = FirebaseAuth.getInstance()
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            isLoggedIn = firebaseAuth.currentUser != null
        }
        auth.addAuthStateListener(listener)
        onDispose {
            auth.removeAuthStateListener(listener)
        }
    }

    // Top-level destinations must NOT accumulate in the back stack.
    // Back from any top-level tab (Home, Explore, Create, Favorites, Profile) minimizes/exits the app.
    val isTopLevelDestination = currentRoute in bottomNavRoutes
    val activity = context as? Activity
    BackHandler(enabled = isTopLevelDestination) {
        activity?.moveTaskToBack(true)
    }

    // Shared ViewModels
    val authViewModel = remember { AuthViewModel(context) }
    val homeViewModel = remember { HomeViewModel() }
    val exploreViewModel = remember { ExploreViewModel() }
    val aiViewModel = remember { AICreateViewModel() }
    val wallpaperDetailViewModel = remember { WallpaperDetailViewModel(context) }
    val profileViewModel = remember { ProfileViewModel(context) }
    val notificationsViewModel = remember { NotificationsViewModel() }
    val unreadCount by notificationsViewModel.unreadCount.collectAsState()

    // Deep link handling for foreground / background taps while authenticated
    LaunchedEffect(pendingWallpaperId, isLoggedIn, currentRoute) {
        if (!pendingWallpaperId.isNullOrBlank() && isLoggedIn &&
            currentRoute != null &&
            currentRoute != Routes.SPLASH &&
            currentRoute != Routes.WELCOME &&
            currentRoute != Routes.SIGN_IN &&
            currentRoute != Routes.SIGN_UP
        ) {
            val target = pendingWallpaperId
            onClearPendingWallpaperId()
            navController.navigate(Routes.wallpaperById(target)) {
                launchSingleTop = true
            }
        }
    }

    // Debounced safe navigation helper to prevent rapid double-clicks
    var lastNavTime by remember { mutableLongStateOf(0L) }
    val navigateSafe: (String) -> Unit = { route ->
        val now = System.currentTimeMillis()
        if (now - lastNavTime > 350L) {
            lastNavTime = now
            navController.navigate(route)
        }
    }

    val showBottomBar = currentRoute in bottomNavRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(navController = navController)
            }
        },
        containerColor = Background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(bottom = if (showBottomBar) paddingValues.calculateBottomPadding() else 0.dp)
        ) {
            NavHost(
                navController = navController,
                startDestination = Routes.SPLASH,
                enterTransition = { fadeIn(animationSpec = tween(180)) },
                exitTransition = { fadeOut(animationSpec = tween(180)) },
                popEnterTransition = { fadeIn(animationSpec = tween(180)) },
                popExitTransition = { fadeOut(animationSpec = tween(180)) },
                modifier = Modifier
                    .fillMaxSize()
                    .background(Background)
            ) {
                // --- Splash -----------------------------------------------
                composable(Routes.SPLASH) {
                    SplashScreen(
                        isLoggedIn = isLoggedIn,
                        onNavigateToHome = {
                            if (!pendingWallpaperId.isNullOrBlank()) {
                                val target = pendingWallpaperId
                                onClearPendingWallpaperId()
                                navController.navigate(Routes.wallpaperById(target)) {
                                    popUpTo(Routes.SPLASH) { inclusive = true }
                                }
                            } else {
                                navController.navigate(Routes.HOME) {
                                    popUpTo(Routes.SPLASH) { inclusive = true }
                                }
                            }
                        },
                        onNavigateToWelcome = {
                            navController.navigate(Routes.WELCOME) {
                                popUpTo(Routes.SPLASH) { inclusive = true }
                            }
                        }
                    )
                }

                // Helper lambda for navigating after auth, preserving pending notification target
                val onAuthSuccess = {
                    profileViewModel.loadProfile()
                    notificationsViewModel.registerToken()
                    if (!pendingWallpaperId.isNullOrBlank()) {
                        val target = pendingWallpaperId
                        onClearPendingWallpaperId()
                        navController.navigate(Routes.wallpaperById(target)) {
                            popUpTo(Routes.WELCOME) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.WELCOME) { inclusive = true }
                        }
                    }
                }

                // --- Auth -------------------------------------------------
                composable(Routes.WELCOME) {
                    WelcomeScreen(
                        viewModel = authViewModel,
                        onNavigateToSignIn = { navigateSafe(Routes.SIGN_IN) },
                        onNavigateToSignUp = { navigateSafe(Routes.SIGN_UP) },
                        onAuthenticated = onAuthSuccess
                    )
                }
                composable(Routes.SIGN_IN) {
                    SignInScreen(
                        viewModel = authViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onAuthenticated = onAuthSuccess
                    )
                }
                composable(Routes.SIGN_UP) {
                    SignUpScreen(
                        viewModel = authViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onAuthenticated = onAuthSuccess
                    )
                }

                // --- Top-Level Destinations -------------------------------
                composable(Routes.HOME) {
                    HomeScreen(
                        viewModel = homeViewModel,
                        unreadCount = unreadCount,
                        onNotificationsClick = {
                            navigateSafe(Routes.NOTIFICATIONS)
                        },
                        onWallpaperClick = { wallpaper ->
                            if (wallpaper.id.isNotBlank()) {
                                navigateSafe(Routes.wallpaperById(wallpaper.id))
                            } else {
                                navigateSafe(Routes.wallpaperDetail(wallpaper.imageUrl, wallpaper.category))
                            }
                        },
                        onCategoryClick = { category ->
                            navigateSafe(Routes.category(category))
                        },
                        onSearchSubmit = { query ->
                            exploreViewModel.updateSearch(query)
                            navController.navigate(Routes.EXPLORE) {
                                popUpTo(Routes.HOME) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onAICreateClick = {
                            navController.navigate(Routes.AI_CREATE) {
                                popUpTo(Routes.HOME) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
                composable(Routes.EXPLORE) {
                    ExploreScreen(
                        viewModel = exploreViewModel,
                        onWallpaperClick = { wallpaper ->
                            if (wallpaper.id.isNotBlank()) {
                                navigateSafe(Routes.wallpaperById(wallpaper.id))
                            } else {
                                navigateSafe(Routes.wallpaperDetail(wallpaper.imageUrl, wallpaper.category))
                            }
                        }
                    )
                }
                composable(Routes.AI_CREATE) {
                    AICreateScreen(
                        viewModel = aiViewModel,
                        onGenerationStarted = {
                            navController.navigate(Routes.AI_GENERATING) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToHistory = { navigateSafe(Routes.AI_HISTORY) }
                    )
                }
                composable(Routes.FAVORITES) {
                    FavoritesScreen(
                        onWallpaperClick = { wallpaper ->
                            if (wallpaper.id.isNotBlank()) {
                                navigateSafe(Routes.wallpaperById(wallpaper.id))
                            } else {
                                navigateSafe(Routes.wallpaperDetail(wallpaper.imageUrl, wallpaper.category))
                            }
                        }
                    )
                }
                composable(Routes.PROFILE) {
                    ProfileScreen(
                        viewModel = profileViewModel,
                        onSignOut = {
                            authViewModel.resetState()
                            navController.navigate(Routes.WELCOME) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onNavigateToFavorites = { navigateSafe(Routes.FAVORITES) },
                        onNavigateToHistory = { navigateSafe(Routes.AI_HISTORY) },
                        onNavigateToSettings = { navigateSafe(Routes.SETTINGS) },
                        onNavigateToAppearance = { navigateSafe(Routes.APPEARANCE) },
                        onNavigateToNotificationSettings = { navigateSafe(Routes.NOTIFICATION_SETTINGS) },
                        onNavigateToPrivacyPolicy = { navigateSafe(Routes.PRIVACY_POLICY) },
                        onNavigateToAbout = { navigateSafe(Routes.ABOUT) }
                    )
                }

                // --- Child Destinations: Category -------------------------
                composable(
                    route = Routes.CATEGORY,
                    arguments = listOf(navArgument("categoryName") { type = NavType.StringType })
                ) { backStack ->
                    val rawCat = backStack.arguments?.getString("categoryName") ?: ""
                    val categoryName = try {
                        URLDecoder.decode(rawCat, "UTF-8")
                    } catch (_: Exception) {
                        rawCat
                    }
                    CategoryScreen(
                        categoryName = categoryName,
                        onWallpaperClick = { wallpaper ->
                            if (wallpaper.id.isNotBlank()) {
                                navigateSafe(Routes.wallpaperById(wallpaper.id))
                            } else {
                                navigateSafe(Routes.wallpaperDetail(wallpaper.imageUrl, wallpaper.category))
                            }
                        },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                // --- Child Destinations: Settings & Legal -----------------
                composable(Routes.SETTINGS) {
                    SettingsScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToAppearance = { navigateSafe(Routes.APPEARANCE) },
                        onNavigateToNotifications = { navigateSafe(Routes.NOTIFICATION_SETTINGS) },
                        onNavigateToPrivacyPolicy = { navigateSafe(Routes.PRIVACY_POLICY) },
                        onNavigateToAbout = { navigateSafe(Routes.ABOUT) }
                    )
                }
                composable(Routes.APPEARANCE) {
                    AppearanceScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                composable(Routes.PRIVACY_POLICY) {
                    PrivacyPolicyScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                composable(Routes.ABOUT) {
                    AboutScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                // --- Child Destinations: Notifications --------------------
                composable(Routes.NOTIFICATIONS) {
                    NotificationsScreen(
                        viewModel = notificationsViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onWallpaperClick = { wallpaperId ->
                            navigateSafe(Routes.wallpaperById(wallpaperId))
                        }
                    )
                }
                composable(Routes.NOTIFICATION_SETTINGS) {
                    NotificationSettingsScreen(
                        viewModel = notificationsViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                // --- Child Destinations: Wallpapers -----------------------
                composable(
                    route = Routes.WALLPAPER_BY_ID,
                    arguments = listOf(
                        navArgument("wallpaperId") { type = NavType.StringType }
                    )
                ) { backStack ->
                    val wallpaperId = backStack.arguments?.getString("wallpaperId") ?: ""
                    WallpaperDetailByIdScreen(
                        wallpaperId = wallpaperId,
                        viewModel = wallpaperDetailViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToExplore = {
                            navController.navigate(Routes.EXPLORE) {
                                popUpTo(Routes.HOME) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onNavigateToHome = {
                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.HOME) { inclusive = false; saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
                composable(
                    route = Routes.WALLPAPER_DETAIL,
                    arguments = listOf(
                        navArgument("imageUrl") { type = NavType.StringType },
                        navArgument("category") { type = NavType.StringType }
                    )
                ) { backStack ->
                    val rawUrl = backStack.arguments?.getString("imageUrl") ?: ""
                    val rawCat = backStack.arguments?.getString("category") ?: ""
                    val imageUrl = try { URLDecoder.decode(rawUrl, "UTF-8") } catch (_: Exception) { rawUrl }
                    val category = try { URLDecoder.decode(rawCat, "UTF-8") } catch (_: Exception) { rawCat }
                    val wallpaper = Wallpaper(imageUrl = imageUrl, category = category)
                    WallpaperDetailScreen(
                        wallpaper = wallpaper,
                        viewModel = wallpaperDetailViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                // --- Child Destinations: AI Generation --------------------
                composable(Routes.AI_GENERATING) {
                    AIGeneratingScreen(
                        viewModel = aiViewModel,
                        onResult = {
                            navController.navigate(Routes.AI_RESULT) {
                                popUpTo(Routes.AI_GENERATING) { inclusive = true }
                            }
                        }
                    )
                }
                composable(Routes.AI_RESULT) {
                    AIResultScreen(
                        viewModel = aiViewModel,
                        onGenerateAgain = {
                            navController.navigate(Routes.AI_CREATE) {
                                popUpTo(Routes.AI_CREATE) { inclusive = true }
                            }
                        },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                composable(Routes.AI_HISTORY) {
                    AIHistoryScreen(
                        viewModel = aiViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
