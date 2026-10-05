package app.worthit.data

import java.util.Calendar
import kotlin.math.max

/** Post-purchase check-ins: gentle questions at 7, 30, 90 and 180 days. */
object Checkins {
    val stages = listOf(7, 30, 90, 180)

    data class Due(val purchase: Purchase, val stage: Int)

    fun question(stage: Int) = when (stage) {
        7 -> "First impressions?"
        30 -> "Still using it?"
        90 -> "Was it worth the money?"
        else -> "Would you buy it again?"
    }

    fun stageFor(boughtAt: Long, now: Long): Int? {
        val age = (now - boughtAt) / WorthMath.DAY_MS
        return stages.lastOrNull { age >= it }
    }

    fun due(purchases: List<Purchase>, now: Long): List<Due> = purchases
        .filter { it.status == Status.Bought && it.boughtAt != null }
        .mapNotNull { p ->
            val stage = stageFor(p.boughtAt!!, now) ?: return@mapNotNull null
            if (p.reviews.any { it.stage >= stage }) null else Due(p, stage)
        }
        .sortedBy { it.purchase.boughtAt }
}

/** Patterns from the user's own history. Every insight needs enough data before it speaks. */
object Insights {
    const val MIN_REVIEWED = 3

    fun bought(ps: List<Purchase>) = ps.filter { it.status == Status.Bought }

    fun reviewed(ps: List<Purchase>) = bought(ps).filter { it.verdict != null }

    fun moreNeeded(ps: List<Purchase>, n: Int = MIN_REVIEWED) = max(0, n - reviewed(ps).size)

    fun worthRate(ps: List<Purchase>): Double? {
        val r = reviewed(ps)
        if (r.size < MIN_REVIEWED) return null
        return r.count { it.verdict == Verdict.Yes } * 100.0 / r.size
    }

    fun regretRate(ps: List<Purchase>): Double? {
        val r = reviewed(ps)
        if (r.size < MIN_REVIEWED) return null
        return r.count { it.verdict == Verdict.No } * 100.0 / r.size
    }

    data class CatStat(val category: Category, val count: Int, val rate: Double)

    fun categoryStats(ps: List<Purchase>): List<CatStat> {
        val r = reviewed(ps)
        if (r.size < 5) return emptyList()
        return r.groupBy { it.category }
            .filter { it.value.size >= 2 }
            .map { (c, l) -> CatStat(c, l.size, l.count { it.verdict == Verdict.Yes } * 100.0 / l.size) }
            .sortedByDescending { it.rate }
    }

    data class Band(val label: String, val lo: Double, val hi: Double)

    val bands = listOf(
        Band("under a working day", 0.0, 1.0),
        Band("1–3 working days", 1.0, 3.0),
        Band("3–7 working days", 3.0, 7.0),
        Band("1–3 weeks of work", 7.0, 15.0),
        Band("3+ weeks of work", 15.0, Double.MAX_VALUE),
    )

    fun sweetSpot(ps: List<Purchase>, income: WorthMath.Income): Pair<Band, Double>? {
        val r = reviewed(ps)
        if (r.size < 6 || !income.hasIncome) return null
        return bands.mapNotNull { b ->
            val inBand = r.filter { p ->
                val d = WorthMath.incomeDays(p.price, income) ?: return@filter false
                d >= b.lo && d < b.hi
            }
            if (inBand.size < 2) null else b to inBand.count { it.verdict == Verdict.Yes } * 100.0 / inBand.size
        }.maxByOrNull { it.second }
    }

    data class Period(val total: Double, val count: Int, val byCategory: List<Pair<Category, Double>>)

    fun period(ps: List<Purchase>, from: Long, to: Long): Period {
        val inPeriod = bought(ps).filter { (it.boughtAt ?: 0) in from until to }
        val byCat = inPeriod.groupBy { it.category }
            .map { (c, l) -> c to l.sumOf { it.price } }
            .sortedByDescending { it.second }
        return Period(inPeriod.sumOf { it.price }, inPeriod.size, byCat)
    }

    fun monthStart(now: Long): Long = Calendar.getInstance().apply {
        timeInMillis = now
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    fun yearStart(now: Long): Long = Calendar.getInstance().apply {
        timeInMillis = monthStart(now)
        set(Calendar.MONTH, Calendar.JANUARY)
    }.timeInMillis

    fun letGo(ps: List<Purchase>): Pair<Int, Double> {
        val s = ps.filter { it.status == Status.Skipped }
        return s.size to s.sumOf { it.price }
    }

    fun avgCostPerUse(ps: List<Purchase>): List<Pair<Category, Double>> =
        bought(ps).filter { it.costPerUse != null }
            .groupBy { it.category }
            .map { (c, l) -> c to l.mapNotNull { it.costPerUse }.average() }
            .sortedBy { it.second }

    /** Share of spending that went to experiences rather than things. Needs 5+ purchases. */
    fun experienceShare(ps: List<Purchase>): Double? {
        val b = bought(ps)
        if (b.size < 5) return null
        val total = b.sumOf { it.price }
        if (total <= 0) return null
        val exp = b.filter {
            it.type == PurchaseType.Experience || it.category == Category.Travel ||
                it.category == Category.Experiences || it.category == Category.Food
        }.sumOf { it.price }
        return exp * 100.0 / total
    }

    fun averagePrice(ps: List<Purchase>): Double? = bought(ps).takeIf { it.isNotEmpty() }?.map { it.price }?.average()

    fun subsAnnual(subs: List<Subscription>) = subs.sumOf { it.annual }
}
