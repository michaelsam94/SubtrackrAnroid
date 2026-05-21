package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.BillingCycleType
import com.example.model.Subscription
import com.example.model.SubscriptionCategory
import com.example.model.SubscriptionStatus
import com.example.viewmodel.SubscriptionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionsScreen(
    viewModel: SubscriptionViewModel,
    onNavigateToAdd: () -> Unit,
    onNavigateToDetails: (String) -> Unit
) {
    val filteredSubs by viewModel.filteredSubscriptions.collectAsState()
    val rawSearchQuery by viewModel.searchQuery.collectAsState()
    val activeCategory by viewModel.selectedCategory.collectAsState()
    val activeStatus by viewModel.selectedStatus.collectAsState()

    var isGridView by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Subscriptions", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { isGridView = !isGridView }) {
                        Icon(
                            imageVector = if (isGridView) Icons.Default.List else Icons.Default.GridView,
                            contentDescription = "Toggle Grid/List layout options"
                        )
                    }
                    IconButton(onClick = { showFilterSheet = true }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Open Filter Dialog Options"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAdd,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_sub_fab_subs")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add New Subscription")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            
            // 1. Sleek Search Input Form Field
            OutlinedTextField(
                value = rawSearchQuery,
                onValueChange = { viewModel.searchQuery.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sub_search_input"),
                placeholder = { Text("Search by name or vendor...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search icon") },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                )
            )

            // Active Filters Row (Indicator scrollable bubble tags)
            if (activeCategory != null || activeStatus != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Active Filters:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    activeCategory?.let { cat ->
                        InputChip(
                            selected = true,
                            onClick = { viewModel.selectedCategory.value = null },
                            label = { Text(cat.displayName) },
                            trailingIcon = { Text("✕") }
                        )
                    }
                    activeStatus?.let { stat ->
                        InputChip(
                            selected = true,
                            onClick = { viewModel.selectedStatus.value = null },
                            label = { Text(stat.displayName) },
                            trailingIcon = { Text("✕") }
                        )
                    }
                }
            }

            // 2. Animated Grid or List Toggle content
            AnimatedContent(
                targetState = isGridView,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "GridListTransition"
            ) { gridTarget ->
                if (filteredSubs.isEmpty()) {
                    EmptyStateView(
                        title = "No Subscriptions Found",
                        description = "Add a recurring software expense or click the filter icon to adjust search tags."
                    )
                } else if (gridTarget) {
                    // grid layout
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(filteredSubs, key = { it.id }) { sub ->
                            SubscriptionGridCard(
                                subscription = sub,
                                viewModel = viewModel,
                                onClick = { onNavigateToDetails(sub.id) }
                            )
                        }
                    }
                } else {
                    // list layout
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(filteredSubs, key = { it.id }) { sub ->
                            SubscriptionListCard(
                                subscription = sub,
                                viewModel = viewModel,
                                onClick = { onNavigateToDetails(sub.id) }
                            )
                        }
                    }
                }
            }
        }

        // Filters SheetDialog Setup
        if (showFilterSheet) {
            ModalBottomSheet(
                onDismissRequest = { showFilterSheet = false }
            ) {
                FilterSheetContent(
                    selectedCategory = activeCategory,
                    selectedStatus = activeStatus,
                    onSelectCategory = { viewModel.selectedCategory.value = it },
                    onSelectStatus = { viewModel.selectedStatus.value = it },
                    onClearAll = {
                        viewModel.selectedCategory.value = null
                        viewModel.selectedStatus.value = null
                    },
                    onClose = { showFilterSheet = false }
                )
            }
        }
    }
}

@Composable
fun SubscriptionGridCard(
    subscription: Subscription,
    viewModel: SubscriptionViewModel,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular Placeholder Logo matching brand design colors
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(getCategoryColor(subscription.category).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = subscription.name.take(2).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = getCategoryColor(subscription.category),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                StatusChip(status = subscription.status)
            }

            Column {
                Text(
                    text = subscription.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subscription.vendor,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = viewModel.formatCurrency(subscription.costAmount),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = subscription.billingCycle.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SubscriptionListCard(
    subscription: Subscription,
    viewModel: SubscriptionViewModel,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Circle Avatar branding
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(getCategoryColor(subscription.category).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = subscription.name.take(2).uppercase(),
                    fontWeight = FontWeight.Bold,
                    color = getCategoryColor(subscription.category),
                    style = MaterialTheme.typography.titleSmall
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = subscription.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = subscription.vendor,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = viewModel.formatCurrency(subscription.costAmount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Every ${subscription.billingCycle.displayName.lowercase()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun StatusChip(status: SubscriptionStatus) {
    val container = when (status) {
        SubscriptionStatus.ACTIVE -> MaterialTheme.colorScheme.primaryContainer
        SubscriptionStatus.TRIAL -> MaterialTheme.colorScheme.secondaryContainer
        SubscriptionStatus.PAUSED -> MaterialTheme.colorScheme.surfaceVariant
        SubscriptionStatus.CANCELLED, SubscriptionStatus.EXPIRED -> MaterialTheme.colorScheme.errorContainer
    }
    val content = when (status) {
        SubscriptionStatus.ACTIVE -> MaterialTheme.colorScheme.onPrimaryContainer
        SubscriptionStatus.TRIAL -> MaterialTheme.colorScheme.onSecondaryContainer
        SubscriptionStatus.PAUSED -> MaterialTheme.colorScheme.onSurfaceVariant
        SubscriptionStatus.CANCELLED, SubscriptionStatus.EXPIRED -> MaterialTheme.colorScheme.onErrorContainer
    }

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(container)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = status.displayName,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = content
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterSheetContent(
    selectedCategory: SubscriptionCategory?,
    selectedStatus: SubscriptionStatus?,
    onSelectCategory: (SubscriptionCategory?) -> Unit,
    onSelectStatus: (SubscriptionStatus?) -> Unit,
    onClearAll: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Filter Settings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            TextButton(onClick = onClearAll) { Text("Clear All") }
        }

        Divider()

        Text("SaaS Category", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SubscriptionCategory.values().forEach { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { onSelectCategory(if (selectedCategory == cat) null else cat) },
                    label = { Text(cat.displayName) }
                )
            }
        }

        Text("Billing Status", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SubscriptionStatus.values().forEach { stat ->
                FilterChip(
                    selected = selectedStatus == stat,
                    onClick = { onSelectStatus(if (selectedStatus == stat) null else stat) },
                    label = { Text(stat.displayName) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onClose,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Apply Filters")
        }
    }
}

// Helpers for Category Color mapping
fun getCategoryColor(category: SubscriptionCategory): Color = when (category) {
    SubscriptionCategory.AI_TOOLS -> Color(0xFF818CF8)
    SubscriptionCategory.DEV_TOOLS -> Color(0xFF60A5FA)
    SubscriptionCategory.DESIGN -> Color(0xFFF43F5E)
    SubscriptionCategory.PRODUCTIVITY -> Color(0xFF34D399)
    SubscriptionCategory.CLOUD_INFRA -> Color(0xFFFBBF24)
    SubscriptionCategory.SECURITY -> Color(0xFF38BDF8)
    SubscriptionCategory.COMMUNICATION -> Color(0xFFA78BFA)
    SubscriptionCategory.ANALYTICS -> Color(0xFFEC4899)
    SubscriptionCategory.OTHER -> Color(0xFF94A3B8)
}
