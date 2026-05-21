package com.example.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.example.model.BillingCycleType
import com.example.model.LicenseInfo
import com.example.model.Subscription
import com.example.model.SubscriptionCategory
import com.example.model.SubscriptionStatus

@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey val id: String,
    val name: String,
    val vendor: String,
    val logoUrl: String?,
    val category: String,
    val costAmount: Double,
    val costCurrency: String,
    val billingCycle: String,
    val billingCycleCustomDays: Int,
    val status: String,
    val startDate: String,
    val nextRenewalDate: String,
    val paymentMethod: String?,
    val seatTotal: Int?,
    val seatUsed: Int?,
    val seatTier: String?,
    val tags: String, // comma separated
    val notes: String?,
    val cancellationUrl: String?
) {
    fun toDomain(): Subscription {
        val licenseInfo = if (seatTotal != null && seatUsed != null && seatTier != null) {
            LicenseInfo(seatTotal, seatUsed, seatTier)
        } else null

        val tagsList = if (tags.isBlank()) emptyList() else tags.split(",").map { it.trim() }

        return Subscription(
            id = id,
            name = name,
            vendor = vendor,
            logoUrl = logoUrl,
            category = try { SubscriptionCategory.valueOf(category) } catch (e: Exception) { SubscriptionCategory.OTHER },
            costAmount = costAmount,
            costCurrency = costCurrency,
            billingCycle = try { BillingCycleType.valueOf(billingCycle) } catch (e: Exception) { BillingCycleType.MONTHLY },
            billingCycleCustomDays = billingCycleCustomDays,
            status = try { SubscriptionStatus.valueOf(status) } catch (e: Exception) { SubscriptionStatus.ACTIVE },
            startDate = startDate,
            nextRenewalDate = nextRenewalDate,
            paymentMethod = paymentMethod,
            seats = licenseInfo,
            tags = tagsList,
            notes = notes,
            cancellationUrl = cancellationUrl
        )
    }

    companion object {
        fun fromDomain(sub: Subscription): SubscriptionEntity = SubscriptionEntity(
            id = sub.id,
            name = sub.name,
            vendor = sub.vendor,
            logoUrl = sub.logoUrl,
            category = sub.category.name,
            costAmount = sub.costAmount,
            costCurrency = sub.costCurrency,
            billingCycle = sub.billingCycle.name,
            billingCycleCustomDays = sub.billingCycleCustomDays,
            status = sub.status.name,
            startDate = sub.startDate,
            nextRenewalDate = sub.nextRenewalDate,
            paymentMethod = sub.paymentMethod,
            seatTotal = sub.seats?.totalSeats,
            seatUsed = sub.seats?.usedSeats,
            seatTier = sub.seats?.tier,
            tags = sub.tags.joinToString(","),
            notes = sub.notes,
            cancellationUrl = sub.cancellationUrl
        )
    }
}
