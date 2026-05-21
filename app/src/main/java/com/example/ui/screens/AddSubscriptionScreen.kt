package com.example.ui.screens

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.BillingCycleType
import com.example.model.LicenseInfo
import com.example.model.Subscription
import com.example.model.SubscriptionCategory
import com.example.model.SubscriptionStatus
import com.example.viewmodel.SubscriptionViewModel
import java.util.Calendar
import java.util.UUID

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddSubscriptionScreen(
    viewModel: SubscriptionViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    val todayStr = remember {
        val cal = Calendar.getInstance()
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        sdf.format(cal.time)
    }
    val nextMonthStr = remember {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, 1)
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        sdf.format(cal.time)
    }

    // Form states
    var name by remember { mutableStateOf("") }
    var vendor by remember { mutableStateOf("") }
    var costAmountStr by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(SubscriptionCategory.AI_TOOLS) }
    var billingCycle by remember { mutableStateOf(BillingCycleType.MONTHLY) }
    var startDate by remember { mutableStateOf(todayStr) }
    var nextRenewalDate by remember { mutableStateOf(nextMonthStr) }
    var paymentMethod by remember { mutableStateOf("Visa ending in 4242") }
    
    // License states
    var hasLicenses by remember { mutableStateOf(false) }
    var totalSeats by remember { mutableStateOf("1") }
    var usedSeats by remember { mutableStateOf("1") }
    var licenseTier by remember { mutableStateOf("Pro") }
    
    var tagsStr by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    // DatePicker triggers
    fun showDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val monthStr = if (month + 1 < 10) "0${month + 1}" else "${month + 1}"
                val dayStr = if (dayOfMonth < 10) "0$dayOfMonth" else "$dayOfMonth"
                onDateSelected("$year-$monthStr-$dayStr")
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    fun validateAndSave() {
        if (name.isBlank()) {
            Toast.makeText(context, "SaaS Name is required.", Toast.LENGTH_SHORT).show()
            return
        }
        val cost = costAmountStr.toDoubleOrNull()
        if (cost == null || cost < 0.0) {
            Toast.makeText(context, "Enter a valid positive SaaS Cost.", Toast.LENGTH_SHORT).show()
            return
        }

        val seats = if (hasLicenses) {
            val total = totalSeats.toIntOrNull() ?: 1
            val used = usedSeats.toIntOrNull() ?: 1
            LicenseInfo(
                totalSeats = total,
                usedSeats = java.lang.Math.min(used, total),
                tier = licenseTier
            )
        } else null

        val parsedTags = tagsStr.split(",").map { it.trim() }.filter { it.isNotEmpty() }

        val newSub = Subscription(
            id = UUID.randomUUID().toString(),
            name = name,
            vendor = vendor.ifBlank { name },
            logoUrl = null,
            category = category,
            costAmount = cost,
            billingCycle = billingCycle,
            startDate = startDate,
            nextRenewalDate = nextRenewalDate,
            paymentMethod = paymentMethod.ifBlank { "Card" },
            seats = seats,
            tags = parsedTags,
            notes = notes.ifBlank { null }
        )

        viewModel.addSubscription(newSub)
        Toast.makeText(context, "SaaS Subscription Active Tracker Added!", Toast.LENGTH_SHORT).show()
        onNavigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Recurring SaaS", fontWeight = FontWeight.Bold) },
                actions = {
                    TextButton(
                        onClick = { validateAndSave() },
                        modifier = Modifier.testTag("save_subscription_button")
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold)
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
            
            // 1. Name & Vendor Box Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("SaaS Service Name (e.g. Figma)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("sub_name_field"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = vendor,
                            onValueChange = { vendor = it },
                            label = { Text("Vendor / Company Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            }

            // 2. Cost & Frequency Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = costAmountStr,
                            onValueChange = { costAmountStr = it },
                            label = { Text("Billing Cost Amount ($)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("sub_cost_field"),
                            singleLine = true,
                            prefix = { Text("$ ") }
                        )

                        Text("Billing Cycle Period:")
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            BillingCycleType.values().forEach { cycle ->
                                FilterChip(
                                    selected = billingCycle == cycle,
                                    onClick = { billingCycle = cycle },
                                    label = { Text(cycle.displayName) }
                                )
                            }
                        }
                    }
                }
            }

            // 3. Category selector
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Service Category:")
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SubscriptionCategory.values().forEach { cat ->
                                FilterChip(
                                    selected = category == cat,
                                    onClick = { category = cat },
                                    label = { Text(cat.displayName) }
                                )
                            }
                        }
                    }
                }
            }

            // 4. Renewal & Dates Cards
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                            Text("Start Date: $startDate", fontWeight = FontWeight.Bold)
                            IconButton(onClick = { showDatePicker { startDate = it } }) {
                                Icon(Icons.Default.CalendarToday, contentDescription = "Pick SaaS Start Date")
                            }
                        }
                        Divider()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Renewal Date: $nextRenewalDate", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            IconButton(onClick = { showDatePicker { nextRenewalDate = it } }) {
                                Icon(Icons.Default.CalendarToday, contentDescription = "Pick Next Renewal Cycle Date")
                            }
                        }
                    }
                }
            }

            // 5. Team Licenses Expandable Toggle Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                            Text("Has Team Seats / Multiple Licenses", fontWeight = FontWeight.SemiBold)
                            Switch(
                                checked = hasLicenses,
                                onCheckedChange = { hasLicenses = it }
                            )
                        }

                        if (hasLicenses) {
                            OutlinedTextField(
                                value = totalSeats,
                                onValueChange = { totalSeats = it },
                                label = { Text("Paid Seats count") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = usedSeats,
                                onValueChange = { usedSeats = it },
                                label = { Text("Active/Used Seats count") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = licenseTier,
                                onValueChange = { licenseTier = it },
                                label = { Text("Licensing Tier/Plan") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                    }
                }
            }

            // 6. Payment, Tags, Notes section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = paymentMethod,
                            onValueChange = { paymentMethod = it },
                            label = { Text("Payment Method (Visa ending 4242)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = tagsStr,
                            onValueChange = { tagsStr = it },
                            label = { Text("Tags (comma separated: dev, team, ai)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Billing notes or cancellation conditions") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )
                    }
                }
            }
        }
    }
}
