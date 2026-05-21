package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.OfflineBolt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.SubscriptionViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: SubscriptionViewModel = viewModel()
            val themeMode by viewModel.themeMode.collectAsState()
            
            val isDarkTheme = when (themeMode) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }

            MyApplicationTheme(darkTheme = isDarkTheme) {
                MainAppNavContainer(viewModel)
            }
        }
    }
}

sealed class ScreenRoute(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : ScreenRoute("dashboard", "Dashboard", Icons.Default.TrendingUp)
    object Subscriptions : ScreenRoute("subscriptions", "Plans", Icons.Default.List)
    object Renewals : ScreenRoute("renewals", "Renewals", Icons.Default.CalendarToday)
    object Licenses : ScreenRoute("licenses", "Licenses", Icons.Default.Group)
    object AIChat : ScreenRoute("chat", "AI Chat", Icons.Default.AutoAwesome)
    object Settings : ScreenRoute("settings", "Settings", Icons.Default.Settings)
}

@Composable
fun MainAppNavContainer(viewModel: SubscriptionViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Tab routes to display on the Bottom Navigation Bar
    val bottomTabItems = listOf(
        ScreenRoute.Dashboard,
        ScreenRoute.Subscriptions,
        ScreenRoute.Renewals,
        ScreenRoute.Licenses,
        ScreenRoute.AIChat,
        ScreenRoute.Settings
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            // Only toggle bottom bar on primary screen modes (avoiding add/details overlays)
            val shouldShowBottomBar = bottomTabItems.any { it.route == currentRoute }
            if (shouldShowBottomBar) {
                NavigationBar {
                    bottomTabItems.forEach { screen ->
                        NavigationBarItem(
                            modifier = Modifier.testTag("nav_item_${screen.route}"),
                            icon = { Icon(screen.icon, contentDescription = "${screen.title} navigation tab icon") },
                            label = { 
                                Text(
                                    text = screen.title, 
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                ) 
                            },
                            selected = currentRoute == screen.route,
                            alwaysShowLabel = false,
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
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = ScreenRoute.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            
            // Dashboard Route
            composable(ScreenRoute.Dashboard.route) {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToAdd = { navController.navigate("add_subscription") },
                    onNavigateToChat = { navController.navigate(ScreenRoute.AIChat.route) },
                    onNavigateToDetails = { id -> navController.navigate("detail/$id") }
                )
            }

            // Subscription lists route
            composable(ScreenRoute.Subscriptions.route) {
                SubscriptionsScreen(
                    viewModel = viewModel,
                    onNavigateToAdd = { navController.navigate("add_subscription") },
                    onNavigateToDetails = { id -> navController.navigate("detail/$id") }
                )
            }

            // Renewals calendar heatmap route
            composable(ScreenRoute.Renewals.route) {
                RenewalsScreen(
                    viewModel = viewModel,
                    onNavigateToDetails = { id -> navController.navigate("detail/$id") }
                )
            }

            // License Seat monitoring route
            composable(ScreenRoute.Licenses.route) {
                LicensesScreen(
                    viewModel = viewModel
                )
            }

            // AI Interactive conversation assistant route
            composable(ScreenRoute.AIChat.route) {
                AIChatScreen(
                    viewModel = viewModel
                )
            }

            // Application options/preferences Settings route
            composable(ScreenRoute.Settings.route) {
                SettingsScreen(
                    viewModel = viewModel
                )
            }

            // Add new subscription input screen route
            composable("add_subscription") {
                AddSubscriptionScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Subscription Detailed info overlay route
            composable(
                route = "detail/{subId}",
                arguments = listOf(navArgument("subId") { type = NavType.StringType })
            ) { backStackEntry ->
                val subId = backStackEntry.arguments?.getString("subId") ?: ""
                SubscriptionDetailScreen(
                    subId = subId,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
