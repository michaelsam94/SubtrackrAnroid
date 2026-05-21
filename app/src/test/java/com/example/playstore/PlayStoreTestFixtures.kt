package com.example.playstore

import android.app.Application
import com.example.db.AppDatabase
import com.example.db.SubscriptionRepository
import com.example.model.SeedData
import kotlinx.coroutines.runBlocking

object PlayStoreTestFixtures {
    fun resetAndSeed(application: Application) = runBlocking {
        val db = AppDatabase.getDatabase(application)
        val repo = SubscriptionRepository(db.subscriptionDao(), db.chatMessageDao())

        repo.clearAllSubscriptions()
        repo.clearChatHistory()

        SeedData.generateSeedData().forEach { repo.saveSubscription(it) }

        repo.addChatMessage("user", "What renews next week?")
        repo.addChatMessage(
            "assistant",
            "You have **Slack Business (\$87.50)** renewing tomorrow, and **ChatGPT Plus (\$20.00)** " +
                "renewing in 2 days. The total renewal volume due in the next 7 days across all vendors is **\$126.50**.",
        )
        repo.addChatMessage("user", "How can I save money on SaaS?")
        repo.addChatMessage(
            "assistant",
            "Based on your portfolio, I found **3 major optimization opportunities** that could save you up to " +
                "**\$660.00 annually**:\n\n" +
                "1. **Grammarly Business**: Downgrade seats from 10 to 2.\n" +
                "2. **Duplicative AI Tools**: Review ChatGPT and Midjourney overlap.\n" +
                "3. **Vercel Pro**: Revoke the unused seat.\n\n" +
                "Would you like me to draft a seat reduction email?",
        )
    }
}
