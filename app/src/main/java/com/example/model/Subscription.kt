package com.example.model


enum class SubscriptionCategory {
    DEV_TOOLS, AI_TOOLS, DESIGN, PRODUCTIVITY,
    CLOUD_INFRA, SECURITY, COMMUNICATION, ANALYTICS, OTHER;

    val displayName: String
        get() = when (this) {
            DEV_TOOLS -> "Dev Tools"
            AI_TOOLS -> "AI Tools"
            DESIGN -> "Design"
            PRODUCTIVITY -> "Productivity"
            CLOUD_INFRA -> "Cloud Infra"
            SECURITY -> "Security"
            COMMUNICATION -> "Communication"
            ANALYTICS -> "Analytics"
            OTHER -> "Other"
        }
}

enum class SubscriptionStatus { 
    ACTIVE, TRIAL, PAUSED, CANCELLED, EXPIRED;

    val displayName: String
        get() = name.lowercase().replaceFirstChar { it.uppercase() }
}

enum class BillingCycleType { 
    MONTHLY, QUARTERLY, ANNUAL, CUSTOM;

    val displayName: String
        get() = when (this) {
            MONTHLY -> "Monthly"
            QUARTERLY -> "Quarterly"
            ANNUAL -> "Annual"
            CUSTOM -> "Custom"
        }
}

data class LicenseInfo(
    val totalSeats: Int,
    val usedSeats: Int,
    val tier: String
)

data class Subscription(
    val id: String,
    val name: String,
    val vendor: String,
    val logoUrl: String? = null,
    val category: SubscriptionCategory,
    val costAmount: Double,
    val costCurrency: String = "USD",
    val billingCycle: BillingCycleType,
    val billingCycleCustomDays: Int = 30,
    val status: SubscriptionStatus = SubscriptionStatus.ACTIVE,
    val startDate: String, // YYYY-MM-DD
    val nextRenewalDate: String, // YYYY-MM-DD
    val paymentMethod: String? = null,
    val seats: LicenseInfo? = null,
    val tags: List<String> = emptyList(),
    val notes: String? = null,
    val cancellationUrl: String? = null
) {
    fun daysUntilRenewal(): Long {
        try {
            val parts = nextRenewalDate.split("-")
            if (parts.size == 3) {
                val year = parts[0].toIntOrNull() ?: return 0L
                val month = parts[1].toIntOrNull()?.minus(1) ?: return 0L // Calendar months are 0-based
                val day = parts[2].toIntOrNull() ?: return 0L

                val todayCal = java.util.Calendar.getInstance()
                todayCal.set(java.util.Calendar.HOUR_OF_DAY, 0)
                todayCal.set(java.util.Calendar.MINUTE, 0)
                todayCal.set(java.util.Calendar.SECOND, 0)
                todayCal.set(java.util.Calendar.MILLISECOND, 0)

                val renewalCal = java.util.Calendar.getInstance()
                renewalCal.set(year, month, day, 0, 0, 0)
                renewalCal.set(java.util.Calendar.MILLISECOND, 0)

                val diffMs = renewalCal.timeInMillis - todayCal.timeInMillis
                return diffMs / (1000 * 60 * 60 * 24)
            }
        } catch (t: Throwable) {
            // Safe fallback
        }
        return 0L
    }

    fun monthlyCost(): Double = when (billingCycle) {
        BillingCycleType.MONTHLY -> costAmount
        BillingCycleType.QUARTERLY -> costAmount / 3.0
        BillingCycleType.ANNUAL -> costAmount / 12.0
        BillingCycleType.CUSTOM -> {
            val days = if (billingCycleCustomDays > 0) billingCycleCustomDays else 30
            (costAmount / days.toDouble()) * 30.4375
        }
    }

    fun annualCost(): Double = monthlyCost() * 12.0
}
