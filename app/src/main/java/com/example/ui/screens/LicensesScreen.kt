package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.Subscription
import com.example.viewmodel.SubscriptionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicensesScreen(
    viewModel: SubscriptionViewModel
) {
    val subscriptions by viewModel.subscriptions.collectAsState()
    
    // Filter only subscriptions with License/Seat tracking
    val licenseSubs = subscriptions.filter { it.seats != null }

    var selectedSubForSeats by remember { mutableStateOf<Subscription?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("License Seats Fleet", fontWeight = FontWeight.Bold) }
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
            
            // 1. Advisory seat overhead banner
            item {
                val overProvisionCounts = licenseSubs.filter { 
                    val seats = it.seats ?: return@filter false
                    (seats.usedSeats.toDouble() / seats.totalSeats.toDouble()) <= 0.5 
                }.size

                if (overProvisionCounts > 0) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Seat Warning Alert",
                                tint = MaterialTheme.colorScheme.error
                            )
                            Column {
                                Text(
                                    text = "$overProvisionCounts Leaky License Seats Identified",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    text = "You are paying for seats where utilization is under 50%. Free up occupied allocations to optimize expenses.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }

            // 2. Head tag
            item {
                Text(
                    text = "Tracked Licenses (${licenseSubs.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // 3. Render license cards
            if (licenseSubs.isEmpty()) {
                item {
                    EmptyStateView(
                        title = "No License Seats Tracked",
                        description = "Add or edit a subscription and fill in the 'Seats' properties section to monitor team allocations."
                    )
                }
            } else {
                items(licenseSubs, key = { it.id }) { sub ->
                    LicenseSeatCard(
                        sub = sub,
                        viewModel = viewModel,
                        onModifySeats = { selectedSubForSeats = sub }
                    )
                }
            }
        }

        // Bottom dialog to adjust seat counts
        selectedSubForSeats?.let { sub ->
            val seats = sub.seats ?: return@let
            var usedSeatsTmp by remember { mutableStateOf(seats.usedSeats) }
            var totalSeatsTmp by remember { mutableStateOf(seats.totalSeats) }

            AlertDialog(
                onDismissRequest = { selectedSubForSeats = null },
                title = { Text("Update Seats: ${sub.name}") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = "Licensing Tier: ${seats.tier}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )

                        // Used Seats Row
                        Column {
                            Text("Used Seats: $usedSeatsTmp", style = MaterialTheme.typography.bodySmall)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { if (usedSeatsTmp > 0) usedSeatsTmp-- },
                                    enabled = usedSeatsTmp > 0
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Decrement used seats count")
                                }
                                LinearProgressIndicator(
                                    progress = if (totalSeatsTmp > 0) usedSeatsTmp.toFloat() / totalSeatsTmp.toFloat() else 0f,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { if (usedSeatsTmp < totalSeatsTmp) usedSeatsTmp++ },
                                    enabled = usedSeatsTmp < totalSeatsTmp
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Increment used seats count")
                                }
                            }
                        }

                        // Total Seats Row
                        Column {
                            Text("Total Paid Seats: $totalSeatsTmp", style = MaterialTheme.typography.bodySmall)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { if (totalSeatsTmp > usedSeatsTmp) totalSeatsTmp-- },
                                    enabled = totalSeatsTmp > usedSeatsTmp
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Decrement paid seats count")
                                }
                                Spacer(modifier = Modifier.weight(1f))
                                IconButton(
                                    onClick = { totalSeatsTmp++ }
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Increment paid seats count")
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.updateSeats(sub.id, usedSeatsTmp, totalSeatsTmp)
                            selectedSubForSeats = null
                        }
                    ) {
                        Text("Save Allocation")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedSubForSeats = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun LicenseSeatCard(
    sub: Subscription,
    viewModel: SubscriptionViewModel,
    onModifySeats: () -> Unit
) {
    val seats = sub.seats ?: return

    val fraction = seats.usedSeats.toFloat() / seats.totalSeats.toFloat()
    val isWasting = fraction <= 0.5

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(sub.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                        Text("Tier: ${seats.tier}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                if (isWasting) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.errorContainer)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Over-provisioned",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            // progress indicator
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Seat allocation: ${seats.usedSeats} of ${seats.totalSeats} assigned",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${(fraction * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isWasting) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }

                LinearProgressIndicator(
                    progress = fraction,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = if (isWasting) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                )
            }

            Divider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cost overhead: ${viewModel.formatCurrency(sub.costAmount)}/${sub.billingCycle.displayName.lowercase()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = onModifySeats,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("Modify allocated", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
