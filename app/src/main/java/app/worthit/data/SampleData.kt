package app.worthit.data

import java.util.UUID

/** A realistic year of purchases, scaled to the user's own income, so Insights can be explored right away. */
object SampleData {
    fun build(profile: Profile, now: Long): AppData {
        val base = if (profile.monthlyIncome > 0) profile.monthlyIncome else 100_000.0
        val day = WorthMath.DAY_MS
        fun price(f: Double) = Fmt.nice(base * f)
        fun id() = UUID.randomUUID().toString()

        fun bought(
            name: String, emoji: String, f: Double, cat: Category, type: PurchaseType,
            freq: Frequency, life: Lifespan, desire: Int, daysAgo: Int, verdict: Verdict?, rating: Int?,
        ): Purchase {
            val at = now - daysAgo * day
            val stage = Checkins.stageFor(at, now) ?: 0
            val reviews = if (verdict != null) listOf(Review(stage = stage, at = now - day, rating = rating, worth = verdict)) else emptyList()
            return Purchase(
                id = id(), name = name, emoji = emoji, price = price(f), category = cat, type = type,
                frequency = freq, lifespan = life, desire = desire, status = Status.Bought,
                createdAt = at - 3 * day, boughtAt = at, reviews = reviews,
            )
        }

        val purchases = listOf(
            bought("Digital piano", "🎹", 0.45, Category.Hobbies, PurchaseType.Want, Frequency.SeveralWeek, Lifespan.Five, 3, 200, Verdict.Yes, 5),
            bought("Noise-cancelling headphones", "🎧", 0.32, Category.Electronics, PurchaseType.Want, Frequency.Daily, Lifespan.Three, 2, 400, Verdict.Yes, 5),
            bought("Smartwatch", "⌚", 0.18, Category.Electronics, PurchaseType.Want, Frequency.Daily, Lifespan.Three, 1, 300, Verdict.No, 2),
            bought("Goa weekend", "✈️", 0.25, Category.Travel, PurchaseType.Experience, Frequency.Occasionally, Lifespan.UnderYear, 3, 120, Verdict.Yes, 5),
            bought("Concert tickets", "🎤", 0.06, Category.Experiences, PurchaseType.Experience, Frequency.Occasionally, Lifespan.UnderYear, 2, 60, Verdict.Yes, 4),
            bought("Designer jacket", "🧥", 0.15, Category.Fashion, PurchaseType.Want, Frequency.Monthly, Lifespan.Three, 2, 150, Verdict.Maybe, 3),
            bought("Air fryer", "🍟", 0.08, Category.Home, PurchaseType.Need, Frequency.Weekly, Lifespan.Five, 1, 250, Verdict.Yes, 4),
            bought("Mini drone", "🚁", 0.09, Category.Electronics, PurchaseType.Want, Frequency.Occasionally, Lifespan.One, 2, 100, Verdict.No, 1),
            bought("Running shoes", "👟", 0.07, Category.Fitness, PurchaseType.Want, Frequency.SeveralWeek, Lifespan.One, 2, 40, Verdict.Maybe, 4),
            bought("Pottery class", "🏺", 0.04, Category.Experiences, PurchaseType.Experience, Frequency.Weekly, Lifespan.UnderYear, 2, 95, Verdict.Yes, 5),
            bought("LEGO set", "🧱", 0.1, Category.Hobbies, PurchaseType.Want, Frequency.Occasionally, Lifespan.One, 2, 9, null, null),
            Purchase(
                id = id(), name = "Espresso machine", emoji = "☕", price = price(0.2), category = Category.Home,
                frequency = Frequency.Daily, lifespan = Lifespan.Five, desire = 2, status = Status.Wishlist,
                createdAt = now - 8 * day, coolOffUntil = now - day,
            ),
            Purchase(
                id = id(), name = "Mechanical keyboard", emoji = "⌨️", price = price(0.06), category = Category.Electronics,
                frequency = Frequency.Daily, lifespan = Lifespan.Three, desire = 1, status = Status.Wishlist,
                createdAt = now - day, coolOffUntil = now + day,
            ),
            Purchase(
                id = id(), name = "Gaming console", emoji = "🎮", price = price(0.5), category = Category.Entertainment,
                desire = 1, status = Status.Skipped, createdAt = now - 45 * day,
            ),
        )
        val goals = listOf(
            Goal(id = id(), name = "Japan trip", emoji = "🗾", target = price(1.5), saved = price(0.4), monthly = price(0.15), createdAt = now - 90 * day),
        )
        val subs = listOf(
            Subscription(id(), "Netflix", "🎬", price(0.0065), false, now - 540 * day),
            Subscription(id(), "Spotify", "🎵", price(0.0012), false, now - 900 * day),
            Subscription(id(), "Gym membership", "🏋️", price(0.02), false, now - 240 * day),
            Subscription(id(), "Cloud storage", "☁️", price(0.03), true, now - 400 * day),
        )
        return AppData(profile, purchases, goals, subs)
    }
}
