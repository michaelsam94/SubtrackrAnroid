package com.example.playstore

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.example.ScreenRoute
import com.example.ui.screens.AIChatScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.RenewalsScreen
import com.example.ui.screens.SubscriptionsScreen
import com.example.viewmodel.SubscriptionViewModel

/**
 * Full-screen Play Store frame: primary tab content + bottom navigation (matches production).
 */
@Composable
fun PlayStoreScreenshotFrame(
    selectedRoute: ScreenRoute,
    viewModel: SubscriptionViewModel,
) {
    val bottomTabItems = listOf(
        ScreenRoute.Dashboard,
        ScreenRoute.Subscriptions,
        ScreenRoute.Renewals,
        ScreenRoute.Licenses,
        ScreenRoute.AIChat,
        ScreenRoute.Settings,
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                bottomTabItems.forEach { screen ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                screen.icon,
                                contentDescription = "${screen.title} navigation tab icon",
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                        selected = selectedRoute.route == screen.route,
                        alwaysShowLabel = false,
                        onClick = {},
                    )
                }
            }
        },
    ) { innerPadding ->
        Box(Modifier.padding(innerPadding).fillMaxSize()) {
            when (selectedRoute) {
                ScreenRoute.Dashboard ->
                    DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToAdd = {},
                        onNavigateToChat = {},
                        onNavigateToDetails = {},
                    )
                ScreenRoute.Subscriptions ->
                    SubscriptionsScreen(
                        viewModel = viewModel,
                        onNavigateToAdd = {},
                        onNavigateToDetails = {},
                    )
                ScreenRoute.Renewals ->
                    RenewalsScreen(
                        viewModel = viewModel,
                        onNavigateToDetails = {},
                    )
                ScreenRoute.AIChat -> AIChatScreen(viewModel = viewModel)
                else -> Unit
            }
        }
    }
}
