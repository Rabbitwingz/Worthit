package app.worthit.data

import kotlinx.serialization.Serializable

@Serializable
enum class IncomeType(val label: String, val blurb: String) {
    TakeHome("Take-home", "What actually lands in your account. Recommended."),
    Gross("Gross", "Your headline salary, before tax."),
    Irregular("Irregular", "A rough monthly average, for freelancers."),
}

@Serializable
enum class Voice(val label: String, val sample: String, val blurb: String) {
    Minimal("Minimal", "45k = 13.5 days.", "Just the number. Fast."),
    Balanced("Balanced", "45k = 13.5 days. Here's what that means…", "The number, plus a little context."),
    Detailed("Detailed", "13.5 days · 43 per use · 8.4/10 value", "Everything: value, affordability, a score."),
}

@Serializable
enum class Category(val label: String, val emoji: String) {
    Electronics("Tech", "📱"),
    Fashion("Fashion", "👟"),
    Food("Food", "🍜"),
    Travel("Travel", "✈️"),
    Hobbies("Hobbies", "🎨"),
    Entertainment("Entertainment", "🎬"),
    Experiences("Experiences", "🎟️"),
    Home("Home", "🛋️"),
    Fitness("Fitness", "🏋️"),
    Education("Learning", "📚"),
    Other("Other", "✨"),
}

@Serializable
enum class Frequency(val label: String, val short: String, val usesPerYear: Double) {
    Daily("Daily", "every day", 365.0),
    SeveralWeek("Few times a week", "4× a week", 208.0),
    Weekly("Weekly", "once a week", 52.0),
    Monthly("Monthly", "once a month", 12.0),
    Occasionally("Occasionally", "a few times a year", 6.0),
}

@Serializable
enum class Lifespan(val label: String, val long: String, val years: Double) {
    UnderYear("< 1 yr", "a few months", 0.5),
    One("1 yr", "1 year", 1.0),
    Three("3 yrs", "3 years", 3.0),
    Five("5 yrs", "5 years", 5.0),
    TenPlus("10+ yrs", "10 years", 10.0),
}

@Serializable
enum class PurchaseType(val label: String, val emoji: String, val necessity: Double) {
    Need("Need", "🧰", 1.0),
    Want("Want", "💫", 0.45),
    Experience("Experience", "🌅", 0.7),
    Investment("Investment", "🌱", 0.85),
    Gift("Gift", "🎁", 0.6),
}

@Serializable
enum class Status(val label: String) {
    Considering("Thinking"),
    Wishlist("Wishlist"),
    Saving("Saving"),
    Bought("Bought"),
    Skipped("Let go"),
}

@Serializable
enum class Verdict(val label: String, val emoji: String) {
    Yes("Definitely", "💚"),
    Maybe("Maybe", "🤔"),
    No("Not really", "🫠"),
}

object Desire {
    val emojis = listOf("😐", "🙂", "😍", "🔥")
    val labels = listOf("Meh", "Nice to have", "Love it", "Must have")
}

@Serializable
data class Profile(
    val name: String = "",
    val currency: String = "INR",
    val monthlyIncome: Double = 0.0,
    val incomeType: IncomeType = IncomeType.TakeHome,
    val workingDaysPerMonth: Double = 22.0,
    val hoursPerDay: Double = 8.0,
    val fixedSchedule: Boolean = true,
    val fixedExpenses: Double = 0.0,
    val investments: Double = 0.0,
    val savingsTarget: Double = 0.0,
    val voice: Voice = Voice.Balanced,
    val coolingOff: Boolean = true,
    val dynamicColor: Boolean = false,
    val onboarded: Boolean = false,
) {
    fun income() = WorthMath.Income(
        monthly = monthlyIncome,
        workingDays = workingDaysPerMonth,
        hoursPerDay = hoursPerDay,
        fixedSchedule = fixedSchedule,
        fixedExpenses = fixedExpenses,
        investments = investments,
        savingsTarget = savingsTarget,
    )
}

@Serializable
data class Review(
    /** Days after purchase this check-in belongs to (7, 30, 90, 180), or 0 for a manual review. */
    val stage: Int,
    val at: Long,
    val rating: Int? = null,
    val usage: Int? = null,
    val worth: Verdict? = null,
    val buyAgain: Verdict? = null,
)

@Serializable
data class Purchase(
    val id: String,
    val name: String,
    val emoji: String,
    val price: Double,
    val category: Category = Category.Other,
    val type: PurchaseType = PurchaseType.Want,
    val frequency: Frequency? = null,
    val lifespan: Lifespan? = null,
    val desire: Int = -1,
    val status: Status = Status.Considering,
    val createdAt: Long,
    val coolOffUntil: Long? = null,
    val boughtAt: Long? = null,
    val reviews: List<Review> = emptyList(),
    val notes: String = "",
) {
    val verdict: Verdict?
        get() = reviews.lastOrNull { it.worth != null }?.worth
            ?: reviews.lastOrNull { it.buyAgain != null }?.buyAgain

    val rating: Int?
        get() = reviews.lastOrNull { it.rating != null }?.rating

    val costPerUse: Double?
        get() = if (frequency != null && lifespan != null) WorthMath.costPerUse(price, frequency, lifespan) else null
}

@Serializable
data class Goal(
    val id: String,
    val name: String,
    val emoji: String,
    val target: Double,
    val saved: Double = 0.0,
    val monthly: Double = 0.0,
    val targetDate: Long? = null,
    val createdAt: Long,
    val purchaseId: String? = null,
    val completedAt: Long? = null,
) {
    val progress: Float get() = if (target <= 0) 1f else (saved / target).toFloat().coerceIn(0f, 1f)
    val reached: Boolean get() = saved >= target
}

@Serializable
data class Subscription(
    val id: String,
    val name: String,
    val emoji: String,
    val amount: Double,
    val yearly: Boolean = false,
    val startedAt: Long,
) {
    val annual: Double get() = if (yearly) amount else amount * 12
}

@Serializable
data class AppData(
    val profile: Profile = Profile(),
    val purchases: List<Purchase> = emptyList(),
    val goals: List<Goal> = emptyList(),
    val subscriptions: List<Subscription> = emptyList(),
)
