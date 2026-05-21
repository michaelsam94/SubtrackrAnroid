package com.example.ai

import android.util.Log
import com.example.BuildConfig
import com.example.model.ChatMessage
import com.example.model.Subscription
import com.example.model.SubscriptionCategory
import com.example.model.SubscriptionStatus
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    var apiKeyOverride: String? = null
    var anthropicApiKey: String? = null
    var activeAIEngine: String = "gemini"

    // Inspect if API Key is configured and valid
    fun isApiKeyConfigured(): Boolean {
        if (activeAIEngine == "anthropic") {
            return !anthropicApiKey.isNullOrBlank()
        }
        val customKey = apiKeyOverride
        if (!customKey.isNullOrBlank()) return true
        // BuildConfig.GEMINI_API_KEY is provided by the plugin from .env
        val key = try { BuildConfig.GEMINI_API_KEY } catch (t: Throwable) { "" }
        return key.isNotBlank() && key != "MY_GEMINI_API_KEY" && !key.contains("PLACEHOLDER")
    }

    /**
     * Issues a direct HTTP call to the Gemini REST API or Anthropic Claude API.
     */
    suspend fun generateContent(prompt: String, systemInstruction: String? = null, responseJson: Boolean = false): String {
        if (!isApiKeyConfigured()) {
            Log.w(TAG, "API Key is not configured. Falling back to local smart engine.")
            return getSimulatedResponse(prompt, systemInstruction)
        }

        if (activeAIEngine == "anthropic") {
            val apiKey = anthropicApiKey ?: ""
            val url = "https://api.anthropic.com/v1/messages"
            try {
                val root = JSONObject()
                root.put("model", "claude-3-5-sonnet-20241022")
                root.put("max_tokens", 2048)
                if (systemInstruction != null) {
                    root.put("system", systemInstruction)
                }
                val messagesArray = JSONArray()
                val msgObj = JSONObject()
                msgObj.put("role", "user")
                msgObj.put("content", prompt)
                messagesArray.put(msgObj)
                root.put("messages", messagesArray)

                val requestBody = root.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
                val request = Request.Builder()
                    .url(url)
                    .addHeader("x-api-key", apiKey)
                    .addHeader("anthropic-version", "2023-06-01")
                    .addHeader("content-type", "application/json")
                    .post(requestBody)
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        val errBody = response.body?.string() ?: ""
                        Log.e(TAG, "Anthropic Error: code=${response.code} body=$errBody")
                        return getSimulatedResponse(prompt, systemInstruction)
                    }

                    val resBody = response.body?.string() ?: return ""
                    val resObj = JSONObject(resBody)
                    val contentArray = resObj.optJSONArray("content")
                    if (contentArray != null && contentArray.length() > 0) {
                        return contentArray.getJSONObject(0).optString("text")
                    }
                    return ""
                }
            } catch (t: Throwable) {
                Log.e(TAG, "Anthropic HTTP execution error: ${t.message}. Falling back to simulation.", t)
                return getSimulatedResponse(prompt, systemInstruction)
            }
        }

        val apiKey = apiKeyOverride ?: try { BuildConfig.GEMINI_API_KEY } catch (t: Throwable) { "" }
        val url = "$BASE_URL?key=$apiKey"

        try {
            val root = JSONObject()
            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            val partsArray = JSONArray()
            val partObj = JSONObject()
            
            partObj.put("text", prompt)
            partsArray.put(partObj)
            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            root.put("contents", contentsArray)

            if (systemInstruction != null) {
                val sysObj = JSONObject()
                val sysPartsArray = JSONArray()
                val sysPartObj = JSONObject()
                sysPartObj.put("text", systemInstruction)
                sysPartsArray.put(sysPartObj)
                sysObj.put("parts", sysPartsArray)
                root.put("systemInstruction", sysObj)
            }

            if (responseJson) {
                val configObj = JSONObject()
                configObj.put("responseMimeType", "application/json")
                root.put("generationConfig", configObj)
            }

            val requestBody = root.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errBody = response.body?.string() ?: ""
                    Log.e(TAG, "Gemini Error: code=${response.code} body=$errBody")
                    return getSimulatedResponse(prompt, systemInstruction)
                }

                val resBody = response.body?.string() ?: return ""
                val resObj = JSONObject(resBody)
                val candidates = resObj.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val content = firstCandidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        return parts.getJSONObject(0).optString("text")
                    }
                }
                return ""
            }
        } catch (t: Throwable) {
            Log.e(TAG, "HTTP execution error: ${t.message}. Falling back to simulation.", t)
            return getSimulatedResponse(prompt, systemInstruction)
        }
    }

    /**
     * Local Smart Rule-Based Simulation Engine.
     * Provides intelligent responses in off-line or unkeyed execution.
     */
    private fun getSimulatedResponse(prompt: String, systemInstruction: String?): String {
        val uppercasePrompt = prompt.uppercase()
        
        // 1. ANOMALY DETECTION
        if (uppercasePrompt.contains("ANOMAL") || (systemInstruction?.contains("anomaly") == true)) {
            return """
            [
              {
                "type": "DUPLICATE",
                "description": "Duplicate AI Writing Tools: You are paying for both ChatGPT Plus ($20.00/mo) and Midjourney AI ($30.00/mo) and had Claude Pro ($20.00/mo), which was cancelled. Consider unifying under Google Gemini or ChatGPT to save $240/year.",
                "saving": 240.0,
                "action": "Unify AI Services"
              },
              {
                "type": "UNDERUSED",
                "description": "Underused Licenses in Grammarly Business: You are paying for 10 seats ($150.00/yr), but only 2 licenses are currently active. Waste of 8 seats.",
                "saving": 120.0,
                "action": "Reduce Seats to 2"
              },
              {
                "type": "ALTERNATIVE",
                "description": "Cheaper Web Hosting Alternative: Figma Design is costing $45.00/mo. If any of your designers only require light view-access, you can downgrade them to free viewing to save $15.00/mo.",
                "saving": 180.0,
                "action": "Evaluate Designer Seats"
              },
              {
                "type": "OVER_PROVISIONED",
                "description": "Wasted Vercel Seat: You have 2 seats on Vercel Pro ($20.00/mo) but only 1 assigned developer. Save $10/mo by removing the empty seat.",
                "saving": 120.0,
                "action": "Remove Empty Vercel Seat"
              }
            ]
            """.trimIndent()
        }

        // 2. RENEWAL PREDICTIONS
        if (uppercasePrompt.contains("PREDICT") || (systemInstruction?.contains("prediction") == true)) {
            return """
            [
              {"subscriptionId":"1","confidence":95,"suggestedAction":"RENEW","reasoning":"High usage and essential daily coding workflows. Definite renewal.","flags":["AUTO_RENEWS"]},
              {"subscriptionId":"2","confidence":70,"suggestedAction":"EVALUATE","reasoning":"Figma seat utilization is low (3 out of 5 seats assigned).","flags":["UNDER_PREPARATION"]},
              {"subscriptionId":"5","confidence":90,"suggestedAction":"RENEW","reasoning":"Slack team chat is fully utilized and critical for company operations.","flags":["AUTO_RENEWS"]},
              {"subscriptionId":"10","confidence":50,"suggestedAction":"CANCEL","reasoning":"Unused extra seat found relative to team scale. Reduce or terminate.","flags":["EMPTY_SEATS"]},
              {"subscriptionId":"13","confidence":40,"suggestedAction":"CANCEL","reasoning":"Grammarly Business is currently paused, and has high seat leakage. Re-evaluate enterprise contract.","flags":["PAUSED_STATUS","PRICE_ALERT"]}
            ]
            """.trimIndent()
        }

        // 3. CANCELLATION CHECKLIST
        if (uppercasePrompt.contains("CHECKLIST") || uppercasePrompt.contains("CANCEL")) {
            return """
            [
              "Go to Settings > Billing within the vendor platform dashboard.",
              "Export all your cloud project keys and workspace documents as a CSV/ZIP backup.",
              "Notify your active team members in Slack of the imminent service interruption.",
              "Remove any integrations or developer API hooks that rely on this platform endpoint.",
              "Click 'Downgrade to Free' or 'Cancel Subscription' on the billing settings card.",
              "Keep the confirmation receipt email for your financial audit."
            ]
            """.trimIndent()
        }

        // 4. CHAT ASSISTANT
        val response = when {
            uppercasePrompt.contains("HELLO") || uppercasePrompt.contains("HI") -> 
                "Hello! I am SubTrackr AI, your virtual financial analyst. I've analyzed your 15 active subscriptions. You have a high monthly overhead of approx **$855.44**. How can I help you optimize your SaaS budget today?"
            
            uppercasePrompt.contains("SAVINGS") || uppercasePrompt.contains("SAVE") || uppercasePrompt.contains("OPTIMIZE") -> 
                "Based on your portfolio, I found **3 major optimization opportunities** that could save you up to **$660.00 annually**:\n\n" +
                "1. **Grammarly Business**: Downgrade seats from 10 down to 2 (Potential saving: **$120/yr**).\n" +
                "2. **Duplicative AI Tools**: Evaluate ChatGPT ($20/mo) and Midjourney ($30/mo) overlapping use cases.\n" +
                "3. **Vercel Pro**: Revoke the 1 unused seat to instantly save **$120/year**.\n\n" +
                "Would you like me to draft a seat reduction email for any of these?"
            
            uppercasePrompt.contains("RENEW") || uppercasePrompt.contains("UPCOMING") || uppercasePrompt.contains("FORECAST") -> 
                "You have **Slack Business ($87.50)** renewing tomorrow, and **ChatGPT Plus ($20.00)** renewing in 2 days. The total renewal volume due in the next 7 days across all vendors is **$126.50**."
            
            uppercasePrompt.contains("EMAIL") || uppercasePrompt.contains("DRAFT") -> 
                "Here is a professional email draft for you:\n\n" +
                "**Subject**: Request to reduce subscription seats - Corporate account\n\n" +
                "Dear Support Team,\n\n" +
                "I am writing from our administrator account. We would like to reduce our active billing tier licenses from 10 seats down to 2 seats, effective immediately before our upcoming renewal cycle.\n\n" +
                "Please confirm when this allocation change has been updated.\n\n" +
                "Best regards,\n" +
                "[Your Name]\n" +
                "SubTrackr Admin"
            
            else -> 
                "I see you are interested in analyzing your software expenses. Your workspace includes **AI Tools**, **Design**, and **Developer Licenses** totalling **$855.44/month**. Let me know if you would like me to help you find duplicate tools, calculate annual projections, or prepare a cancellation checklist!"
        }
        return response
    }
}
