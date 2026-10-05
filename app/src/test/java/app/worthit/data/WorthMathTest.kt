package app.worthit.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WorthMathTest {
    private val lakh = WorthMath.Income(monthly = 100_000.0, workingDays = 22.0, hoursPerDay = 8.0)
    private val eps = 0.01

    @Test fun incomeDays_matchesPrdExample() {
        // ₹45,000 on ₹1L over 22 days ≈ 9.9 days. PRD example uses 30 days → 13.5.
        assertEquals(9.9, WorthMath.incomeDays(45_000.0, lakh)!!, eps)
        val calendar = lakh.copy(fixedSchedule = false)
        assertEquals(13.5, WorthMath.incomeDays(45_000.0, calendar)!!, eps)
    }

    @Test fun hourly_and_hours() {
        assertEquals(100_000.0 / 176, WorthMath.hourlyIncome(lakh)!!, eps)
        assertEquals(79.2, WorthMath.incomeHours(45_000.0, lakh)!!, eps)
        assertEquals(79.2 * 60, WorthMath.incomeMinutes(45_000.0, lakh)!!, 0.5)
    }

    @Test fun calendarFallback_hasNoHourly() {
        val i = lakh.copy(workingDays = 0.0)
        assertTrue(i.usesCalendarDays)
        assertNull(WorthMath.hourlyIncome(i))
        assertEquals(3_333.33, WorthMath.dailyIncome(i)!!, eps)
    }

    @Test fun zeroIncome_neverDivides() {
        val zero = WorthMath.Income(monthly = 0.0)
        assertNull(WorthMath.incomeDays(45_000.0, zero))
        assertNull(WorthMath.incomeHours(45_000.0, zero))
        assertNull(WorthMath.salaryPercentage(45_000.0, zero))
        assertNull(WorthMath.affordabilityMonths(45_000.0, zero))
        assertNull(Fmt.time(45_000.0, zero))
    }

    @Test fun salaryPercentage() {
        assertEquals(45.0, WorthMath.salaryPercentage(45_000.0, lakh)!!, eps)
    }

    @Test fun discretionary_and_affordability() {
        assertNull(WorthMath.discretionary(lakh))
        val i = lakh.copy(fixedExpenses = 40_000.0, investments = 15_000.0, savingsTarget = 10_000.0)
        assertEquals(35_000.0, WorthMath.discretionary(i)!!, eps)
        assertEquals(1.29, WorthMath.affordabilityMonths(45_000.0, i)!!, eps)
        val broke = lakh.copy(fixedExpenses = 120_000.0)
        assertEquals(0.0, WorthMath.discretionary(broke)!!, eps)
        assertNull(WorthMath.affordabilityMonths(45_000.0, broke))
    }

    @Test fun costPerUse_matchesPrdExample() {
        assertEquals(1_040.0, WorthMath.totalUses(Frequency.SeveralWeek, Lifespan.Five), eps)
        assertEquals(43.27, WorthMath.costPerUse(45_000.0, Frequency.SeveralWeek, Lifespan.Five), eps)
    }

    @Test fun savingMath() {
        assertEquals(7, WorthMath.monthsToSave(33_000.0, 5_000.0))
        assertEquals(0, WorthMath.monthsToSave(0.0, 5_000.0))
        assertNull(WorthMath.monthsToSave(1_000.0, 0.0))
        assertEquals(5, WorthMath.monthsToSave(100_000.0, 20_000.0))
        assertEquals(7_500.0, WorthMath.monthlyNeeded(45_000.0, 6), eps)
    }

    @Test fun coolOff_followsPrdThresholds() {
        assertEquals(24, WorthMath.coolOffHours(3_000.0, lakh))
        assertEquals(48, WorthMath.coolOffHours(15_000.0, lakh))
        assertEquals(168, WorthMath.coolOffHours(45_000.0, lakh))
        assertEquals(336, WorthMath.coolOffHours(80_000.0, lakh))
        assertEquals(336, WorthMath.coolOffHours(80_000.0, WorthMath.Income(0.0)))
    }

    @Test fun worthScore_pianoIsStrong() {
        val i = lakh.copy(fixedExpenses = 40_000.0, investments = 15_000.0, savingsTarget = 10_000.0)
        val s = WorthMath.worthScore(45_000.0, i, Frequency.SeveralWeek, Lifespan.Five, 3, PurchaseType.Want)
        assertEquals(8.4, s.total, 0.1)
        assertEquals("Strong value candidate", WorthMath.scoreLabel(s.total))
    }

    @Test fun extremes_stillWork() {
        assertEquals(22_000.0, WorthMath.incomeDays(100_000_000.0, lakh)!!, 1.0)
        val tiny = Fmt.time(10.0, lakh)!!
        assertEquals("minutes of work", tiny.unit)
        assertEquals("₹10,00,00,000", Fmt.money(100_000_000.0, "INR"))
        assertEquals("₹10Cr", Fmt.compact(100_000_000.0, "INR"))
    }

    @Test fun formatting() {
        assertEquals("₹45,000", Fmt.money(45_000.0, "INR"))
        assertEquals("₹1,00,000", Fmt.money(100_000.0, "INR"))
        assertEquals("$100,000", Fmt.money(100_000.0, "USD"))
        assertEquals("₹45k", Fmt.compact(45_000.0, "INR"))
        assertEquals("₹1.8L", Fmt.compact(180_000.0, "INR"))
        assertEquals("$1.2M", Fmt.compact(1_200_000.0, "USD"))
        assertEquals("13.5", Fmt.num(13.5))
        assertEquals("1,040", Fmt.num(1_040.0))
        assertEquals("12,34,567", Fmt.groupDigits("1234567", true))
        assertEquals("1,234,567", Fmt.groupDigits("1234567", false))
    }

    @Test fun guessing() {
        assertEquals("🎹", Guess.from("Yamaha digital piano").emoji)
        assertEquals(Category.Travel, Guess.from("Goa trip").category)
        assertEquals(Category.Other, Guess.from("cardigan thing").category.let { if (it == Category.Fashion) Category.Other else it })
    }

    @Test fun checkins_dueAtStages() {
        val now = 1_000L * WorthMath.DAY_MS
        val p = Purchase(id = "1", name = "x", emoji = "x", price = 1.0, status = Status.Bought, createdAt = 0, boughtAt = now - 8 * WorthMath.DAY_MS)
        assertEquals(7, Checkins.due(listOf(p), now).single().stage)
        val reviewed = p.copy(reviews = listOf(Review(stage = 7, at = now)))
        assertTrue(Checkins.due(listOf(reviewed), now).isEmpty())
        val old = p.copy(boughtAt = now - 100 * WorthMath.DAY_MS)
        assertEquals(90, Checkins.due(listOf(old), now).single().stage)
    }
}
