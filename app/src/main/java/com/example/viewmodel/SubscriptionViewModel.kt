package com.example.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiClient
import com.example.db.AppDatabase
import com.example.db.SubscriptionRepository
import com.example.model.BillingCycleType
import com.example.model.ChatMessage
import com.example.model.LicenseInfo
import com.example.model.Subscription
import com.example.model.SubscriptionCategory
import com.example.model.SubscriptionStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONArray

// AI Output Data wrappers
data class AnomalyAlert(
    val type: String,
    val description: String,
    val saving: Double,
    val action: String
)

data class RenewalPrediction(
    val id: String,
    val confidence: Int,
    val suggestedAction: String, // RENEW, EVALUATE, CANCEL
    val reasoning: String,
    val flags: List<String>
)

class SubscriptionViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "SubscriptionViewModel"
    private val repository: SubscriptionRepository
    private val sharedPrefs = application.getSharedPreferences("sub_trackr_prefs", android.content.Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(sharedPrefs.getString("theme_mode", "system") ?: "system")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _baseCurrency = MutableStateFlow(sharedPrefs.getString("base_currency", "USD") ?: "USD")
    val baseCurrency: StateFlow<String> = _baseCurrency.asStateFlow()

    private val _geminiApiKey = MutableStateFlow(sharedPrefs.getString("gemini_api_key", "") ?: "")
    val geminiApiKey: StateFlow<String> = _geminiApiKey.asStateFlow()

    private val _anthropicApiKey = MutableStateFlow(sharedPrefs.getString("anthropic_api_key", "") ?: "")
    val anthropicApiKey: StateFlow<String> = _anthropicApiKey.asStateFlow()

    private val _activeAiEngine = MutableStateFlow(sharedPrefs.getString("active_ai_engine", "gemini") ?: "gemini")
    val activeAiEngine: StateFlow<String> = _activeAiEngine.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = SubscriptionRepository(db.subscriptionDao(), db.chatMessageDao())

        // Sync local client state to GeminiClient
        GeminiClient.apiKeyOverride = _geminiApiKey.value.ifBlank { null }
        GeminiClient.anthropicApiKey = _anthropicApiKey.value.ifBlank { null }
        GeminiClient.activeAIEngine = _activeAiEngine.value
    }

    fun setThemeMode(mode: String) {
        _themeMode.value = mode
        sharedPrefs.edit().putString("theme_mode", mode).apply()
    }

    fun setBaseCurrency(curr: String) {
        _baseCurrency.value = curr
        sharedPrefs.edit().putString("base_currency", curr).apply()
        runAIAnalysis()
    }

    fun setGeminiApiKey(key: String) {
        _geminiApiKey.value = key
        sharedPrefs.edit().putString("gemini_api_key", key).apply()
        GeminiClient.apiKeyOverride = key.ifBlank { null }
        runAIAnalysis()
    }

    fun setAnthropicApiKey(key: String) {
        _anthropicApiKey.value = key
        sharedPrefs.edit().putString("anthropic_api_key", key).apply()
        GeminiClient.anthropicApiKey = key.ifBlank { null }
        runAIAnalysis()
    }

    fun setActiveAiEngine(engine: String) {
        _activeAiEngine.value = engine
        sharedPrefs.edit().putString("active_ai_engine", engine).apply()
        GeminiClient.activeAIEngine = engine
        runAIAnalysis()
    }

    fun getCurrencySymbol(): String {
        return when (_baseCurrency.value) {
            "USD" -> "$"
            "EUR" -> "€"
            "GBP" -> "£"
            "JPY" -> "¥"
            "CAD" -> "C$"
            "AUD" -> "A$"
            "EGP" -> "E£"
            else -> "$"
        }
    }

    fun formatCurrency(amount: Double): String {
        return "${getCurrencySymbol()}${String.format("%.2f", amount)}"
    }

    // Flow of raw subscriptions
    val subscriptions: StateFlow<List<Subscription>> = repository.subscriptions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Chat History
    val chatMessages: StateFlow<List<ChatMessage>> = repository.chatMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filters for listing
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow<SubscriptionCategory?>(null)
    val selectedStatus = MutableStateFlow<SubscriptionStatus?>(null)

    // Filtered subscriptions combine query
    val filteredSubscriptions: StateFlow<List<Subscription>> = combine(
        subscriptions,
        searchQuery,
        selectedCategory,
        selectedStatus
    ) { subs, query, category, status ->
        subs.filter { sub ->
            (query.isEmpty() || sub.name.contains(query, ignoreCase = true) || sub.vendor.contains(query, ignoreCase = true)) &&
            (category == null || sub.category == category) &&
            (status == null || sub.status == status)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI state for AI analysis
    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _anomalies = MutableStateFlow<List<AnomalyAlert>>(emptyList())
    val anomalies: StateFlow<List<AnomalyAlert>> = _anomalies.asStateFlow()

    private val _predictions = MutableStateFlow<List<RenewalPrediction>>(emptyList())
    val predictions: StateFlow<List<RenewalPrediction>> = _predictions.asStateFlow()

    // Chat view sending state
    private val _isSendingChat = MutableStateFlow(false)
    val isSendingChat: StateFlow<Boolean> = _isSendingChat.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            // First, run database seed if it is empty, completely in background
            repository.seedIfEmpty()
            // Next, run initial AI optimization check on the freshly seeded/loaded data
            runAIAnalysis()
        }
    }

    fun runAIAnalysis() {
        viewModelScope.launch(Dispatchers.IO) {
            _isAnalyzing.value = true
            try {
                // Fetch latest subscription array directly from DB flow to ensure fresh access
                val currentList = repository.subscriptions.first()
                val dataJson = currentList.joinToString("\n") { 
                    "${it.name} (${it.category.displayName}): $${it.costAmount}/${it.billingCycle.displayName}, Seats=${it.seats?.usedSeats}/${it.seats?.totalSeats}, Status=${it.status.name}" 
                }

                // 1. Detect Anomalies
                val anomalyResponse = GeminiClient.generateContent(
                    prompt = "Analyze these subscriptions for anomalies, duplicates, underutilized seats, and potential savings:\n$dataJson",
                    systemInstruction = "You are a cost optimization AI. Analyze subscriptions and return a raw JSON array matching this exact schema: [{\"type\":\"DUPLICATE|UNDERUSED|ALTERNATIVE|OVER_PROVISIONED\",\"description\":\"string details\",\"saving\":0.00,\"action\":\"string recommendation\"}]. Return absolutely nothing except the direct raw JSON, no markdown blocks, no formatting wrapper.",
                    responseJson = true
                )
                parseAnomalies(anomalyResponse)

                // 2. Generate Renewal Predictions
                val predictionResponse = GeminiClient.generateContent(
                    prompt = "Evaluate upcoming renewals for these subscriptions: $dataJson",
                    systemInstruction = "You are a renewal prediction AI. Generate confidence scores (0-100), logical suggestions (RENEW|EVALUATE|CANCEL), and brief reasoning. Return a raw JSON array matching this exact schema: [{\"subscriptionId\":\"string\",\"confidence\":80,\"suggestedAction\":\"RENEW|EVALUATE|CANCEL\",\"reasoning\":\"reasoning details\",\"flags\":[\"flag1\"]}]. Return absolutely nothing except the direct raw JSON, no markdown wrapper.",
                    responseJson = true
                )
                parsePredictions(predictionResponse)

            } catch (t: Throwable) {
                Log.e(TAG, "AI automated analysis failed: ${t.message}", t)
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    private fun parseAnomalies(rawJson: String) {
        try {
            val cleanJson = cleanJsonString(rawJson)
            val jsonArray = JSONArray(cleanJson)
            val list = mutableListOf<AnomalyAlert>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    AnomalyAlert(
                        type = obj.optString("type", "OPTIMIZATION"),
                        description = obj.optString("description", ""),
                        saving = obj.optDouble("saving", 0.0),
                        action = obj.optString("action", "Evaluate")
                    )
                )
            }
            _anomalies.value = list
        } catch (t: Throwable) {
            Log.e(TAG, "Error parsing anomalies JSON: $rawJson", t)
        }
    }

    private fun parsePredictions(rawJson: String) {
        try {
            val cleanJson = cleanJsonString(rawJson)
            val jsonArray = JSONArray(cleanJson)
            val list = mutableListOf<RenewalPrediction>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val flagsArray = obj.optJSONArray("flags")
                val flags = mutableListOf<String>()
                if (flagsArray != null) {
                    for (j in 0 until flagsArray.length()) {
                        flags.add(flagsArray.getString(j))
                    }
                }
                list.add(
                    RenewalPrediction(
                        id = obj.optString("subscriptionId", ""),
                        confidence = obj.optInt("confidence", 80),
                        suggestedAction = obj.optString("suggestedAction", "RENEW"),
                        reasoning = obj.optString("reasoning", ""),
                        flags = flags
                    )
                )
            }
            _predictions.value = list
        } catch (t: Throwable) {
            Log.e(TAG, "Error parsing predictions JSON: $rawJson", t)
        }
    }

    private fun cleanJsonString(raw: String): String {
        return raw.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
    }

    // Core Business CRUD
    fun addSubscription(sub: Subscription) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveSubscription(sub)
            runAIAnalysis() // run optimization check on change
        }
    }

    fun deleteSubscription(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteSubscription(id)
            runAIAnalysis()
        }
    }

    fun toggleSubscriptionStatus(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val sub = repository.getSubscriptionById(id) ?: return@launch
            val nextStatus = when (sub.status) {
                SubscriptionStatus.ACTIVE -> SubscriptionStatus.PAUSED
                SubscriptionStatus.PAUSED -> SubscriptionStatus.ACTIVE
                else -> SubscriptionStatus.ACTIVE
            }
            repository.saveSubscription(sub.copy(status = nextStatus))
            runAIAnalysis()
        }
    }

    fun updateSeats(id: String, used: Int, total: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val sub = repository.getSubscriptionById(id) ?: return@launch
            val currentSeats = sub.seats ?: LicenseInfo(total, used, "Standard")
            repository.saveSubscription(sub.copy(
                seats = currentSeats.copy(usedSeats = used, totalSeats = total)
            ))
            runAIAnalysis()
        }
    }

    // AI Chat Conversation Log Handler
    fun sendChatMessage(inputText: String) {
        if (inputText.isBlank()) return
        
        viewModelScope.launch(Dispatchers.IO) {
            _isSendingChat.value = true
            
            // 1. Persist User Message
            repository.addChatMessage("user", inputText)

            try {
                // 2. Prep Context Prompt
                val currentSubs = subscriptions.value
                val dataJson = currentSubs.joinToString("\n") { 
                    "- ${it.name} (${it.category.displayName}) - Cost: $${it.costAmount}/${it.billingCycle.displayName}, Status: ${it.status.name}, Seats utilized: ${it.seats?.usedSeats ?: 0}/${it.seats?.totalSeats ?: 0}. Payment Card: ${it.paymentMethod ?: "None"}." 
                }

                val sysInstruction = """
                    You are SubTrackr AI, a friendly financial analyst integrated into SubTrackr — an Android application that tracks billing renewals, cloud software licenses, and optimization leakage.
                    Here is the current portfolio data from the user's database:
                    $dataJson
                    
                    Respond helpful, directly addressing their queries. Use markdown typography, make predictions, list steps, compute math accurately, and suggest concrete budget adjustments. Keep responses concise so they fit beautifully on a mobile phone screen. Always sound professional and empathetic.
                """.trimIndent()

                // Generate response
                val aiReply = GeminiClient.generateContent(
                    prompt = inputText,
                    systemInstruction = sysInstruction
                )

                // 3. Persist AI Assistant Response
                repository.addChatMessage("assistant", aiReply)
            } catch (t: Throwable) {
                repository.addChatMessage("assistant", "I had trouble contacting the intelligence service. Locally, I see you have ${currentSubs().size} active SaaS accounts with approx $${String.format("%.2f", totalMonthlyCost())}/mo of billing. Let me know what I can compute or generate for you!")
            } finally {
                _isSendingChat.value = false
            }
        }
    }

    fun clearChatMessages() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearChatHistory()
        }
    }

    fun clearAllSaaSSubscriptions() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAllSubscriptions()
            runAIAnalysis()
        }
    }

    // High Level Aggregated Stats helpers
    fun currentSubs(): List<Subscription> = subscriptions.value

    fun totalMonthlyCost(): Double = subscriptions.value
        .filter { it.status == SubscriptionStatus.ACTIVE || it.status == SubscriptionStatus.TRIAL }
        .sumOf { it.monthlyCost() }

    fun annualProjectionCost(): Double = totalMonthlyCost() * 12.0

    fun activeSubscriptionsCount(): Int = subscriptions.value
        .filter { it.status == SubscriptionStatus.ACTIVE || it.status == SubscriptionStatus.TRIAL }
        .size

    fun totalAnnualSaaSWaterSavings(): Double = anomalies.value.sumOf { it.saving }
}
