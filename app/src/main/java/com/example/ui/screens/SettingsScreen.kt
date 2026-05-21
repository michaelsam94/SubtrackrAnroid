package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.ai.GeminiClient
import com.example.viewmodel.SubscriptionViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SubscriptionViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val themeMode by viewModel.themeMode.collectAsState()
    val baseCurrency by viewModel.baseCurrency.collectAsState()
    val geminiApiKey by viewModel.geminiApiKey.collectAsState()
    val anthropicApiKey by viewModel.anthropicApiKey.collectAsState()
    val activeAiEngine by viewModel.activeAiEngine.collectAsState()

    var showClearConfirm by remember { mutableStateOf(false) }
    var apiStatusText by remember { mutableStateOf("Not Tested") }
    var isTestingApi by remember { mutableStateOf(false) }

    var showThemeDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }

    var geminiVisible by remember { mutableStateOf(false) }
    var anthropicVisible by remember { mutableStateOf(false) }

    fun testApiConnection() {
        scope.launch {
            isTestingApi = true
            apiStatusText = "Connecting..."
            try {
                if (GeminiClient.isApiKeyConfigured()) {
                    val res = GeminiClient.generateContent(
                        prompt = "Ping",
                        systemInstruction = "Reply ONLY with OK"
                    )
                    apiStatusText = if (res.trim().contains("OK", ignoreCase = true)) "Valid & Live" else "Key config error"
                } else {
                    apiStatusText = "Offline (Local Engine Active)"
                }
            } catch (e: Exception) {
                apiStatusText = "Connection Failed"
            } finally {
                isTestingApi = false
            }
        }
    }

    val currencyList = listOf(
        "USD" to "USD ($)",
        "EUR" to "EUR (€)",
        "GBP" to "GBP (£)",
        "JPY" to "JPY (¥)",
        "CAD" to "CAD (C$)",
        "AUD" to "AUD (A$)",
        "EGP" to "EGP (E£)"
    )

    val themeList = listOf(
        "system" to "System Default",
        "dark" to "Dark Mode",
        "light" to "Light Mode"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Preferences", fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // 1. Core Engine status card
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Key,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                val displayEngine = if (activeAiEngine == "gemini") "Gemini" else "Claude (Anthropic)"
                                Text("$displayEngine Engine Status", fontWeight = FontWeight.Bold)
                                Text("Runtime connection state", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Text(
                            text = apiStatusText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (apiStatusText.contains("Live") || apiStatusText.contains("Local")) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                        )
                    }

                    Button(
                        onClick = { testApiConnection() },
                        enabled = !isTestingApi,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isTestingApi) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Test Connector")
                        }
                    }
                }
            }

            // 2. Active AI Intelligence Engine config card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Select Intelligence Provider", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = activeAiEngine == "gemini",
                            onClick = { viewModel.setActiveAiEngine("gemini") },
                            label = { Text("Google Gemini") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = activeAiEngine == "anthropic",
                            onClick = { viewModel.setActiveAiEngine("anthropic") },
                            label = { Text("Anthropic Claude") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    HorizontalDivider()

                    if (activeAiEngine == "gemini") {
                        OutlinedTextField(
                            value = geminiApiKey,
                            onValueChange = { viewModel.setGeminiApiKey(it) },
                            label = { Text("Gemini API Key Override") },
                            placeholder = { Text("Enter your gemini-3.5-flash key...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = if (geminiVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { geminiVisible = !geminiVisible }) {
                                    Icon(
                                        imageVector = if (geminiVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle Gemini visibility"
                                    )
                                }
                            }
                        )
                    } else {
                        OutlinedTextField(
                            value = anthropicApiKey,
                            onValueChange = { viewModel.setAnthropicApiKey(it) },
                            label = { Text("Anthropic API Key") },
                            placeholder = { Text("Enter your claude-3-5 key (sk-...)...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = if (anthropicVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { anthropicVisible = !anthropicVisible }) {
                                    Icon(
                                        imageVector = if (anthropicVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle Anthropic visibility"
                                    )
                                }
                            }
                        )
                    }
                }
            }

            // 3. Localization and Theme Settings Cards
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column {
                    ListItem(
                        headlineContent = { Text("Base Currency") },
                        supportingContent = { Text("Current format: $baseCurrency") },
                        leadingContent = { Icon(Icons.Default.Language, contentDescription = null) },
                        trailingContent = { 
                            Text(
                                text = currencyList.firstOrNull { it.first == baseCurrency }?.second ?: "USD ($)", 
                                fontWeight = FontWeight.Bold, 
                                color = MaterialTheme.colorScheme.primary
                            ) 
                        },
                        modifier = Modifier.clickable { showCurrencyDialog = true }
                    )
                    HorizontalDivider()
                    ListItem(
                        headlineContent = { Text("App Theme Mode") },
                        supportingContent = { Text("Adjust the application paint style") },
                        leadingContent = { Icon(Icons.Default.Settings, contentDescription = null) },
                        trailingContent = { 
                            Text(
                                text = themeList.firstOrNull { it.first == themeMode }?.second ?: "Auto", 
                                fontWeight = FontWeight.Bold, 
                                color = MaterialTheme.colorScheme.primary
                            ) 
                        },
                        modifier = Modifier.clickable { showThemeDialog = true }
                    )
                }
            }

            // 4. Destructive database Actions
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column {
                    ListItem(
                        headlineContent = { Text("Reset Application Cache", color = MaterialTheme.colorScheme.error) },
                        supportingContent = { Text("Instantly wipe database entries and rebuild defaults") },
                        leadingContent = { Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                        modifier = Modifier.clickable { showClearConfirm = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Footer branding metadata
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "SubTrackr Pro v1.1.0",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Crafted in Native Kotlin and Jetpack Compose",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }

        // Show theme select dialog
        if (showThemeDialog) {
            AlertDialog(
                onDismissRequest = { showThemeDialog = false },
                title = { Text("Select App Theme") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        themeList.forEach { (mode, label) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.setThemeMode(mode)
                                        showThemeDialog = false
                                        Toast.makeText(context, "$label Theme Saved", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = themeMode == mode,
                                    onClick = {
                                        viewModel.setThemeMode(mode)
                                        showThemeDialog = false
                                        Toast.makeText(context, "$label Theme Saved", Toast.LENGTH_SHORT).show()
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(label, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showThemeDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }

        // Show currency select dialog
        if (showCurrencyDialog) {
            AlertDialog(
                onDismissRequest = { showCurrencyDialog = false },
                title = { Text("Select Base Currency") },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        currencyList.forEach { (code, label) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.setBaseCurrency(code)
                                        showCurrencyDialog = false
                                        Toast.makeText(context, "Currency set to $code", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = baseCurrency == code,
                                    onClick = {
                                        viewModel.setBaseCurrency(code)
                                        showCurrencyDialog = false
                                        Toast.makeText(context, "Currency set to $code", Toast.LENGTH_SHORT).show()
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(label, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showCurrencyDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }

        if (showClearConfirm) {
            AlertDialog(
                onDismissRequest = { showClearConfirm = false },
                title = { Text("Confirm Full Wiping") },
                text = { Text("This will permanently clear all your custom added SaaS inputs and load the 15 seed vendor items.") },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        onClick = {
                            viewModel.clearAllSaaSSubscriptions()
                            showClearConfirm = false
                            Toast.makeText(context, "Database flushed and Seeded successfully.", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text("Reset data")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearConfirm = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
