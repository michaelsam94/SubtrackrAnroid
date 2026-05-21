package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.Subscription
import com.example.viewmodel.SubscriptionViewModel

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionDetailScreen(
    subId: String,
    viewModel: SubscriptionViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val subscriptions by viewModel.subscriptions.collectAsState()
    val predictions by viewModel.predictions.collectAsState()

    val sub = subscriptions.find { it.id == subId }

    if (sub == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Subscription Not Found", fontWeight = FontWeight.Bold)
                Button(onClick = onNavigateBack, modifier = Modifier.padding(top = 16.dp)) {
                    Text("Go Back")
                }
            }
        }
        return
    }

    val matchingPrediction = predictions.find { it.id == sub.id }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(sub.name, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Go back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.deleteSubscription(sub.id)
                            Toast.makeText(context, "${sub.name} deleted.", Toast.LENGTH_SHORT).show()
                            onNavigateBack()
                        },
                        modifier = Modifier.testTag("delete_sub_button")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete subscription record completely", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // 1. Service Head Header Metadata Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(getCategoryColor(sub.category).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = sub.name.take(2).uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = getCategoryColor(sub.category),
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(sub.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                        Text(sub.vendor, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatusChip(status = sub.status)
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondaryContainer)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = sub.category.displayName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // 2. Billing Breakdown Financial info
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Billing Conditions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Divider()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Cycle Cost Amount", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${viewModel.formatCurrency(sub.costAmount)} / ${sub.billingCycle.displayName}", fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Simulated Monthly Cost", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${viewModel.formatCurrency(sub.monthlyCost())} / mo", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Next Cycle Renewal", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(sub.nextRenewalDate, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Payment Instrument", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(sub.paymentMethod ?: "Corporate Billing", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // 3. AI Renewal Forecast Analytics
            if (matchingPrediction != null) {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            Text("AI Renewal Propensity Forecast", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        }
                        Text(
                            text = "Propensity Model Score: ${matchingPrediction.confidence}% confidence to ${matchingPrediction.suggestedAction}.",
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = matchingPrediction.reasoning,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // 4. Detailed interactive billing logs notes / description
            sub.notes?.let { note ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Notes, contentDescription = null)
                            Text("Administrative Billing Notes", fontWeight = FontWeight.Bold)
                        }
                        Text(note, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Toggle active status button CTA
            Button(
                onClick = {
                    viewModel.toggleSubscriptionStatus(sub.id)
                    Toast.makeText(context, "Billing configuration updated.", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("toggle_status_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (sub.status == com.example.model.SubscriptionStatus.ACTIVE) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.primary
                )
            ) {
                if (sub.status == com.example.model.SubscriptionStatus.ACTIVE) {
                    Icon(Icons.Default.Pause, contentDescription = "Pause active subscription")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pause Recurring Invoicing", fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Activate cycle invoicing")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reactivate Invoicing Tracker", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
