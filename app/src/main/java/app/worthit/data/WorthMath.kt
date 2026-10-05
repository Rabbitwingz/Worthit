package app.worthit.data

import kotlin.math.ceil
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min

/**
 * The one place every number in the app is calculated. UI code never divides by income itself.
 */
object WorthMath {
    const val CALENDAR_DAYS = 30.0
    const val DAY_MS = 86_400_000L
    const val HOUR_MS = 3_600_000L

    data class Income(
        val monthly: Double,
        val workingDays: Double = 22.0,
        val hoursPerDay: Double = 8.0,
        val fixedSchedule: Boolean = true,
        val fixedExpenses: Double = 0.0,
        val investments: Double = 0.0,
        val savingsTarget: Double = 0.0,
    ) {
        val hasIncome: Boolean get() = monthly > 0.0 && monthly.isFinite()

        /** Without a usable working schedule we fall back to calendar-day income (and say so in the UI). */
        val usesCalendarDays: Boolean get() = !(fixedSchedule && workingDays > 0.0)

        val daysPerMonth: Double get() = if (usesCalendarDays) CALENDAR_DAYS else workingDays
    }

    fun dailyIncome(i: Income): Double? = if (i.hasIncome) i.monthly / i.daysPerMonth else null

    fun hourlyIncome(i: Income): Double? =
        if (!i.hasIncome || i.usesCalendarDays || i.hoursPerDay <= 0.0) null
        else i.monthly / (i.workingDays * i.hoursPerDay)

    fun incomeDays(price: Double, i: Income): Double? = dailyIncome(i)?.let { price / it }

    fun incomeHours(price: Double, i: Income): Double? = hourlyIncome(i)?.let { price / it }

    fun incomeMinutes(price: Double, i: Income): Double? = incomeHours(price, i)?.times(60.0)

    fun salaryPercentage(price: Double, i: Income): Double? =
        if (i.hasIncome) price / i.monthly * 100.0 else null

    /** Money left after fixed costs, investing and saving. Null when the user hasn't shared a baseline. */
    fun discretionary(i: Income): Double? {
        if (!i.hasIncome) return null
        val committed = i.fixedExpenses + i.investments + i.savingsTarget
        if (committed <= 0.0) return null
        return max(0.0, i.monthly - committed)
    }

    fun discretionaryPercentage(price: Double, i: Income): Double? =
        discretionary(i)?.takeIf { it > 0.0 }?.let { price / it * 100.0 }

    fun affordabilityMonths(price: Double, i: Income): Double? =
        discretionary(i)?.takeIf { it > 0.0 }?.let { price / it }

    fun totalUses(f: Frequency, l: Lifespan): Double = f.usesPerYear * l.years

    fun costPerUse(price: Double, f: Frequency, l: Lifespan): Double = price / max(1.0, totalUses(f, l))

    /** Whole months to cover [remaining] at [monthly] per month. Null when it would never happen. */
    fun monthsToSave(remaining: Double, monthly: Double): Int? = when {
        remaining <= 0.0 -> 0
        monthly <= 0.0 -> null
        else -> ceil(remaining / monthly - 1e-9).toInt()
    }

    fun monthlyNeeded(remaining: Double, months: Int): Double =
        if (remaining <= 0.0) 0.0 else remaining / max(1, months)

    /**
     * Suggested cooling-off period. Thresholds mirror ₹5k / ₹20k / ₹50k on a ₹1L, 22-day salary,
     * but are expressed in income-days so they work for every currency and income.
     */
    fun coolOffHours(price: Double, i: Income): Int {
        val days = incomeDays(price, i)
        return if (days != null) when {
            days < 1.1 -> 24
            days < 4.4 -> 48
            days < 11.0 -> 24 * 7
            else -> 24 * 14
        } else when {
            price < 5_000 -> 24
            price < 20_000 -> 48
            price < 50_000 -> 24 * 7
            else -> 24 * 14
        }
    }

    data class Score(
        val total: Double,
        val affordability: Double,
        val usage: Double,
        val lifespan: Double,
        val joy: Double,
        val need: Double,
    )

    /**
     * A personal decision aid (0–10), never financial truth.
     * Affordability 25%, frequency of use 25%, lifespan 20%, enjoyment 20%, necessity 10%.
     */
    fun worthScore(
        price: Double,
        i: Income,
        frequency: Frequency?,
        lifespan: Lifespan?,
        desire: Int,
        type: PurchaseType,
    ): Score {
        val affordability = affordabilityMonths(price, i)?.let { (1.0 - it / 4.0).coerceIn(0.0, 1.0) }
            ?: salaryPercentage(price, i)?.let { (1.0 - it / 200.0).coerceIn(0.0, 1.0) }
            ?: 0.5
        val usage = frequency?.let { ln(1.0 + it.usesPerYear) / ln(366.0) } ?: 0.4
        val life = lifespan?.let { min(1.0, it.years / 5.0) } ?: 0.4
        val joy = if (desire in 0..3) desire / 3.0 else 0.5
        val need = type.necessity
        val total = 10.0 * (0.25 * affordability + 0.25 * usage + 0.20 * life + 0.20 * joy + 0.10 * need)
        return Score(total.coerceIn(0.0, 10.0), affordability, usage, life, joy, need)
    }

    fun scoreLabel(score: Double): String = when {
        score >= 8.0 -> "Strong value candidate"
        score >= 6.5 -> "Solid, if you'll use it"
        score >= 5.0 -> "Worth a think"
        else -> "Maybe sleep on it"
    }
}
