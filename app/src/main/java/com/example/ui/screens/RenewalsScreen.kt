package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.Subscription
import com.example.model.SubscriptionStatus
import com.example.viewmodel.SubscriptionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RenewalsScreen(
    viewModel: SubscriptionViewModel,
    onNavigateToDetails: (String) -> Unit
) {
    val subscriptions by viewModel.subscriptions.collectAsState()
    val context = LocalContext.current

    val activeSubs = subscriptions.filter { it.status == SubscriptionStatus.ACTIVE || it.status == SubscriptionStatus.TRIAL }
    val sortedRenewals = activeSubs.sortedBy { it.daysUntilRenewal() }

    // Grouping by timelines
    val next7Days = sortedRenewals.filter { it.daysUntilRenewal() in 0..7 }
    val next30Days = sortedRenewals.filter { it.daysUntilRenewal() in 8..30 }
    val next90Days = sortedRenewals.filter { it.daysUntilRenewal() in 31..90 }

    // Aggregate cost data for the calendar heatmap
    fun getRenewalSumFactorForDay(offsetDays: Int): Double {
        val cal = java.util.Calendar.getInstance()
        if (offsetDays > 0) cal.add(java.util.Calendar.DAY_OF_YEAR, offsetDays)
        val targetDateStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(cal.time)
        return activeSubs.filter { it.nextRenewalDate == targetDateStr }.sumOf { it.costAmount }
    }

    // Export calendar ICS helper
    fun shareIcsCalendar() {
        val icsHeader = "BEGIN:VCALENDAR\nVERSION:2.0\nPRODID:-//SubTrackr//SaaS Tracker//EN\n"
        val icsEvents = activeSubs.joinToString("\n") { sub ->
            val cleanDate = sub.nextRenewalDate.replace("-", "")
            """
            BEGIN:VEVENT
            SUMMARY:SaaS Renewal: ${sub.name}
            DESCRIPTION:Cost: $${sub.costAmount} via ${sub.paymentMethod ?: "SaaS Billing"}.
            DTSTART;VALUE=DATE:$cleanDate
            DTEND;VALUE=DATE:$cleanDate
            END:VEVENT
            """.trimIndent()
        }
        val icsFooter = "\nEND:VCALENDAR"
        val fullIcs = icsHeader + icsEvents + icsFooter

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/calendar"
            putExtra(Intent.EXTRA_SUBJECT, "SubTrackr SaaS Renewals Calendar")
            putExtra(Intent.EXTRA_TEXT, fullIcs)
        }
        context.startActivity(Intent.createChooser(intent, "Share SaaS Calendar"))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Renewal Strategy", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { shareIcsCalendar() }) {
                        Icon(Icons.Default.Share, contentDescription = "Share subscription calendar as ICS")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            
            // 1. Calendar Grid Visual (Canvas Heated Grid map: 3 rows of 7 days representing next 21 days forecast)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "21-Day Renewal Intensity",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                                Text("Premium", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        // Drawing Heat boxes custom in canvas
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                        ) {
                            val boxWidth = (size.width - (6 * 10.dp.toPx())) / 7f
                            val boxHeight = (size.height - (2 * 10.dp.toPx())) / 3f
                            val gap = 10.dp.toPx()

                            var dayOffset = 0
                            for (row in 0 until 3) {
                                for (col in 0 until 7) {
                                    val costOnDay = getRenewalSumFactorForDay(dayOffset)
                                    val color = when {
                                        costOnDay == 0.0 -> Color.LightGray.copy(alpha = 0.2f)
                                        costOnDay < 30.0 -> Color(0xFF818CF8).copy(alpha = 0.4f)
                                        costOnDay < 100.0 -> Color(0xFF4F46E5).copy(alpha = 0.7f)
                                        else -> Color(0xFFEF4444) // Urgent alarm crimson
                                    }

                                    drawRoundRect(
                                        color = color,
                                        topLeft = Offset(col * (boxWidth + gap), row * (boxHeight + gap)),
                                        size = Size(boxWidth, boxHeight),
                                        cornerRadius = CornerRadius(4.dp.toPx())
                                    )
                                    dayOffset++
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Today", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("+21 Days", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // 2. Timeline Sections
            item {
                Text(
                    text = "Next 7 Days Critical",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }

            if (next7Days.isEmpty()) {
                item {
                    Text(
                        "No renewals due in the next 7 days.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            } else {
                items(next7Days, key = { "7_${it.id}" }) { sub ->
                    TimelineItemRow(sub = sub, viewModel = viewModel, onClick = { onNavigateToDetails(sub.id) })
                }
            }

            item {
                Text(
                    text = "Refined Forecast (8-30 Days)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (next30Days.isEmpty()) {
                item {
                    Text(
                        "No renewals due in this interval.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            } else {
                items(next30Days, key = { "30_${it.id}" }) { sub ->
                    TimelineItemRow(sub = sub, viewModel = viewModel, onClick = { onNavigateToDetails(sub.id) })
                }
            }

            item {
                Text(
                    text = "Extended Lookahead (31-90 Days)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (next90Days.isEmpty()) {
                item {
                    Text(
                        "No long-term renewals scheduled inside 90 days.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            } else {
                items(next90Days, key = { "90_${it.id}" }) { sub ->
                    TimelineItemRow(sub = sub, viewModel = viewModel, onClick = { onNavigateToDetails(sub.id) })
                }
            }
        }
    }
}

@Composable
fun TimelineItemRow(sub: Subscription, viewModel: SubscriptionViewModel, onClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon Calendar placeholder
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(sub.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                Text(
                    "Renews on ${sub.nextRenewalDate} (${sub.daysUntilRenewal()} days remaining)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = viewModel.formatCurrency(sub.costAmount),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.bodyLarge
                )
                IconButton(onClick = onClick) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Show item description details")
                }
            }
        }
    }
}
