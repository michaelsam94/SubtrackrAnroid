package com.example.model

import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale

object SeedData {
    private fun getDateOffset(daysOffset: Int = 0, monthsOffset: Int = 0, yearsOffset: Int = 0): String {
        val cal = Calendar.getInstance()
        if (daysOffset != 0) cal.add(Calendar.DAY_OF_YEAR, daysOffset)
        if (monthsOffset != 0) cal.add(Calendar.MONTH, monthsOffset)
        if (yearsOffset != 0) cal.add(Calendar.YEAR, yearsOffset)
        
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(cal.time)
    }

    fun generateSeedData(): List<Subscription> {
        return listOf(
            Subscription(
                id = "1",
                name = "GitHub Copilot",
                vendor = "GitHub Inc.",
                logoUrl = "https://github.com/fluidicon.png",
                category = SubscriptionCategory.AI_TOOLS,
                costAmount = 19.0,
                costCurrency = "USD",
                billingCycle = BillingCycleType.MONTHLY,
                status = SubscriptionStatus.ACTIVE,
                startDate = getDateOffset(monthsOffset = -6),
                nextRenewalDate = getDateOffset(daysOffset = 4),
                paymentMethod = "Visa ending in 4242",
                seats = LicenseInfo(1, 1, "Pro"),
                tags = listOf("dev", "ai", "coding"),
                notes = "Essential tool for coding assistance and velocity."
            ),
            Subscription(
                id = "2",
                name = "Figma Design",
                vendor = "Figma",
                logoUrl = "https://figma.com/favicon.ico",
                category = SubscriptionCategory.DESIGN,
                costAmount = 45.0,
                costCurrency = "USD",
                billingCycle = BillingCycleType.MONTHLY,
                status = SubscriptionStatus.ACTIVE,
                startDate = getDateOffset(yearsOffset = -1),
                nextRenewalDate = getDateOffset(daysOffset = 12),
                paymentMethod = "Apple Pay Mastercard",
                seats = LicenseInfo(5, 3, "Professional"),
                tags = listOf("design", "shared", "uiux"),
                notes = "Shared design workspace. Includes 3 active editor seats."
            ),
            Subscription(
                id = "3",
                name = "AWS Cloud Hosting",
                vendor = "Amazon Web Services",
                category = SubscriptionCategory.CLOUD_INFRA,
                costAmount = 340.0,
                costCurrency = "USD",
                billingCycle = BillingCycleType.MONTHLY,
                status = SubscriptionStatus.ACTIVE,
                startDate = getDateOffset(monthsOffset = -18),
                nextRenewalDate = getDateOffset(daysOffset = 10),
                paymentMethod = "Amex ending in 1002",
                seats = null,
                tags = listOf("infra", "servers", "production"),
                notes = "EC2 and RDS databases instance costs. Monitor spikes."
            ),
            Subscription(
                id = "4",
                name = "Notion Personal",
                vendor = "Notion Labs",
                category = SubscriptionCategory.PRODUCTIVITY,
                costAmount = 96.0,
                costCurrency = "USD",
                billingCycle = BillingCycleType.ANNUAL,
                status = SubscriptionStatus.ACTIVE,
                startDate = getDateOffset(monthsOffset = -3),
                nextRenewalDate = getDateOffset(daysOffset = 45),
                paymentMethod = "Visa ending in 4242",
                seats = LicenseInfo(1, 1, "Plus"),
                tags = listOf("notes", "wiki", "docs"),
                notes = "Yearly note-taking and documentation hub subscription."
            ),
            Subscription(
                id = "5",
                name = "Slack Business",
                vendor = "Slack",
                category = SubscriptionCategory.COMMUNICATION,
                costAmount = 87.50,
                costCurrency = "USD",
                billingCycle = BillingCycleType.MONTHLY,
                status = SubscriptionStatus.ACTIVE,
                startDate = getDateOffset(monthsOffset = -11),
                nextRenewalDate = getDateOffset(daysOffset = 1),
                paymentMethod = "Amex ending in 1002",
                seats = LicenseInfo(10, 10, "Business+"),
                tags = listOf("chat", "team", "internal"),
                notes = "Team communication workspace. Fully provisioned."
            ),
            Subscription(
                id = "6",
                name = "Midjourney AI",
                vendor = "Midjourney",
                category = SubscriptionCategory.AI_TOOLS,
                costAmount = 30.0,
                costCurrency = "USD",
                billingCycle = BillingCycleType.MONTHLY,
                status = SubscriptionStatus.ACTIVE,
                startDate = getDateOffset(monthsOffset = -2),
                nextRenewalDate = getDateOffset(daysOffset = 7),
                paymentMethod = "Visa ending in 4242",
                seats = LicenseInfo(1, 1, "Standard"),
                tags = listOf("images", "inspiration", "ai"),
                notes = "AI image generation for product concepts and design mockup."
            ),
            Subscription(
                id = "7",
                name = "ChatGPT Plus",
                vendor = "OpenAI",
                category = SubscriptionCategory.AI_TOOLS,
                costAmount = 20.0,
                costCurrency = "USD",
                billingCycle = BillingCycleType.MONTHLY,
                status = SubscriptionStatus.ACTIVE,
                startDate = getDateOffset(monthsOffset = -5),
                nextRenewalDate = getDateOffset(daysOffset = 2),
                paymentMethod = "Visa ending in 4242",
                seats = LicenseInfo(1, 1, "Plus"),
                tags = listOf("gpt", "helper", "ai"),
                notes = "Prompt engineering and brainstorming support helper."
            ),
            Subscription(
                id = "8",
                name = "Google Workspace",
                vendor = "Google Inc.",
                category = SubscriptionCategory.COMMUNICATION,
                costAmount = 72.0,
                costCurrency = "USD",
                billingCycle = BillingCycleType.MONTHLY,
                status = SubscriptionStatus.ACTIVE,
                startDate = getDateOffset(yearsOffset = -2),
                nextRenewalDate = getDateOffset(daysOffset = 6),
                paymentMethod = "Google Pay Visa",
                seats = LicenseInfo(6, 6, "Business Starter"),
                tags = listOf("email", "drive", "docs"),
                notes = "Corporate emails, Google Drive storage, and shared calendars."
            ),
            Subscription(
                id = "9",
                name = "1Password Team",
                vendor = "AgileBits",
                category = SubscriptionCategory.SECURITY,
                costAmount = 19.95,
                costCurrency = "USD",
                billingCycle = BillingCycleType.MONTHLY,
                status = SubscriptionStatus.ACTIVE,
                startDate = getDateOffset(monthsOffset = -10),
                nextRenewalDate = getDateOffset(daysOffset = 15),
                paymentMethod = "Apple Pay Mastercard",
                seats = LicenseInfo(5, 5, "Teams"),
                tags = listOf("security", "passwords", "vault"),
                notes = "Corporate password vault and team credential sharing."
            ),
            Subscription(
                id = "10",
                name = "Vercel Pro",
                vendor = "Vercel",
                category = SubscriptionCategory.DEV_TOOLS,
                costAmount = 20.0,
                costCurrency = "USD",
                billingCycle = BillingCycleType.MONTHLY,
                status = SubscriptionStatus.ACTIVE,
                startDate = getDateOffset(monthsOffset = -4),
                nextRenewalDate = getDateOffset(daysOffset = 18),
                paymentMethod = "Visa ending in 4242",
                seats = LicenseInfo(2, 1, "Pro Team"),
                tags = listOf("cloud", "frontend", "hosting"),
                notes = "Wasting a seat since we only have 1 active developer. Plan review recommended."
            ),
            Subscription(
                id = "11",
                name = "Sentry Error Tracker",
                vendor = "Functional Software",
                category = SubscriptionCategory.DEV_TOOLS,
                costAmount = 29.0,
                costCurrency = "USD",
                billingCycle = BillingCycleType.MONTHLY,
                status = SubscriptionStatus.ACTIVE,
                startDate = getDateOffset(monthsOffset = -7),
                nextRenewalDate = getDateOffset(daysOffset = 8),
                paymentMethod = "Amex ending in 1002",
                seats = null,
                tags = listOf("errors", "analytics", "debug"),
                notes = "Frontend crash reporting and backend performance profiling."
            ),
            Subscription(
                id = "12",
                name = "Adobe Creative Cloud",
                vendor = "Adobe Inc.",
                category = SubscriptionCategory.DESIGN,
                costAmount = 54.99,
                costCurrency = "USD",
                billingCycle = BillingCycleType.MONTHLY,
                status = SubscriptionStatus.ACTIVE,
                startDate = getDateOffset(yearsOffset = -1),
                nextRenewalDate = getDateOffset(daysOffset = 3),
                paymentMethod = "Apple Pay Mastercard",
                seats = LicenseInfo(1, 1, "All Apps"),
                tags = listOf("design", "photos", "vectors"),
                notes = "Photoshop, Illustrator, and Premiere editing toolset."
            ),
            Subscription(
                id = "13",
                name = "Grammarly Business",
                vendor = "Grammarly",
                category = SubscriptionCategory.PRODUCTIVITY,
                costAmount = 150.0,
                costCurrency = "USD",
                billingCycle = BillingCycleType.ANNUAL,
                status = SubscriptionStatus.PAUSED,
                startDate = getDateOffset(monthsOffset = -8),
                nextRenewalDate = getDateOffset(daysOffset = 120),
                paymentMethod = "Apple Pay Mastercard",
                seats = LicenseInfo(10, 2, "Enterprise"),
                tags = listOf("writing", "office", "team"),
                notes = "Currently paused until next quarter. Major seat over-provisioning."
            ),
            Subscription(
                id = "14",
                name = "Claude Pro",
                vendor = "Anthropic",
                category = SubscriptionCategory.AI_TOOLS,
                costAmount = 20.0,
                costCurrency = "USD",
                billingCycle = BillingCycleType.MONTHLY,
                status = SubscriptionStatus.CANCELLED,
                startDate = getDateOffset(monthsOffset = -4),
                nextRenewalDate = getDateOffset(daysOffset = 5),
                paymentMethod = "Visa ending in 4242",
                seats = LicenseInfo(1, 1, "Plus"),
                tags = listOf("ai", "chat", "writing"),
                notes = "Cancelled. Evaluating OpenAI / ChatGPT alternative features instead."
            ),
            Subscription(
                id = "15",
                name = "Tableau Analyst Suite",
                vendor = "Salesforce",
                category = SubscriptionCategory.ANALYTICS,
                costAmount = 70.0,
                costCurrency = "USD",
                billingCycle = BillingCycleType.MONTHLY,
                status = SubscriptionStatus.ACTIVE,
                startDate = getDateOffset(monthsOffset = -5),
                nextRenewalDate = getDateOffset(daysOffset = 20),
                paymentMethod = "Amex ending in 1002",
                seats = LicenseInfo(4, 1, "Creator License"),
                tags = listOf("analytics", "bi", "data"),
                notes = "High-cost BI tools. Over-provisioned seats (1 used of 4). Optimize needed!"
            )
        )
    }
}
