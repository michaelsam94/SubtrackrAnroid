package com.example.playstore

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ScreenRoute
import com.example.ui.theme.DarkBg
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.RoyalIndigo
import com.example.viewmodel.SubscriptionViewModel

/**
 * Play Store feature graphic (1024×500): app name + hero dashboard on indigo/emerald gradient.
 */
@Composable
fun FeatureGraphicContent(viewModel: SubscriptionViewModel) {
    val gradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF312E81),
            RoyalIndigo,
            Color(0xFF065F46),
        ),
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 48.dp, vertical = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(0.42f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = EmeraldGreen,
                    )
                    Text(
                        text = "SubTrackr",
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
                Text(
                    text = "AI-Powered Subscription Tracker",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFE0E7FF),
                )
                Text(
                    text = "Forecast spend • Spot waste • Chat with your portfolio",
                    fontSize = 15.sp,
                    color = Color(0xFFCBD5E1),
                )
            }

            Surface(
                modifier = Modifier
                    .weight(0.52f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(20.dp)),
                color = DarkBg,
                shadowElevation = 16.dp,
            ) {
                Box(Modifier.fillMaxSize()) {
                    PlayStoreScreenshotFrame(
                        selectedRoute = ScreenRoute.Dashboard,
                        viewModel = viewModel,
                    )
                }
            }
        }
    }
}
