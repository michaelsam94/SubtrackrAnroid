package com.example.db

import com.example.model.ChatMessage
import com.example.model.SeedData
import com.example.model.Subscription
import com.example.model.SubscriptionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.first

class SubscriptionRepository(
    private val subscriptionDao: SubscriptionDao,
    private val chatMessageDao: ChatMessageDao
) {
    // Flow of all subscriptions
    val subscriptions: Flow<List<Subscription>> = subscriptionDao.getAllSubscriptions()
        .map { entities ->
            entities.map { it.toDomain() }
        }

    val chatMessages: Flow<List<ChatMessage>> = chatMessageDao.getAllMessages()
        .map { entities ->
            entities.map { ChatMessage(it.id, it.sender, it.content, it.timestamp) }
        }

    suspend fun seedIfEmpty() {
        val current = subscriptionDao.getAllSubscriptions().first()
        if (current.isEmpty()) {
            SeedData.generateSeedData().forEach { sub ->
                subscriptionDao.insertSubscription(SubscriptionEntity.fromDomain(sub))
            }
        }
    }

    suspend fun getSubscriptionById(id: String): Subscription? {
        return subscriptionDao.getSubscriptionById(id)?.toDomain()
    }

    suspend fun saveSubscription(subscription: Subscription) {
        subscriptionDao.insertSubscription(SubscriptionEntity.fromDomain(subscription))
    }

    suspend fun deleteSubscription(id: String) {
        subscriptionDao.deleteSubscriptionById(id)
    }

    suspend fun addChatMessage(sender: String, content: String) {
        chatMessageDao.insertMessage(
            ChatMessageEntity(sender = sender, content = content)
        )
    }

    suspend fun clearChatHistory() {
        chatMessageDao.clearHistory()
    }

    suspend fun clearAllSubscriptions() {
        subscriptionDao.clearAllSubscriptions()
    }
}
