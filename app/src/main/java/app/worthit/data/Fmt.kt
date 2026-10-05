package app.worthit.data

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToLong

/** Formatting helpers. INR gets Indian digit grouping (1,00,000) and lakh/crore compact forms. */
object Fmt {
    data class Currency(val code: String, val symbol: String, val name: String)

    val currencies = listOf(
        Currency("INR", "₹", "Rupee"),
        Currency("USD", "$", "Dollar"),
        Currency("EUR", "€", "Euro"),
        Currency("GBP", "£", "Pound"),
        Currency("JPY", "¥", "Yen"),
        Currency("AUD", "A$", "Aus dollar"),
        Currency("CAD", "C$", "Can dollar"),
        Currency("SGD", "S$", "Sing dollar"),
        Currency("AED", "AED ", "Dirham"),
    )

    fun symbol(code: String): String = currencies.firstOrNull { it.code == code }?.symbol ?: "$code "

    fun isIndian(code: String) = code == "INR"

    fun groupDigits(digits: String, indian: Boolean): String {
        if (digits.length <= 3) return digits
        val sb = StringBuilder()
        val last3 = digits.takeLast(3)
        val head = digits.dropLast(3)
        val size = if (indian) 2 else 3
        var i = head.length
        val parts = ArrayList<String>()
        while (i > 0) {
            val start = maxOf(0, i - size)
            parts.add(0, head.substring(start, i))
            i = start
        }
        parts.forEach { sb.append(it).append(',') }
        sb.append(last3)
        return sb.toString()
    }

    fun group(n: Long, indian: Boolean): String {
        val s = groupDigits(abs(n).toString(), indian)
        return if (n < 0) "-$s" else s
    }

    private fun trim(s: String): String = if (s.contains('.')) s.trimEnd('0').trimEnd('.') else s

    fun money(amount: Double, code: String): String {
        if (!amount.isFinite()) return "—"
        val sign = if (amount < 0) "-" else ""
        val a = abs(amount)
        val body = if (a < 10 && a != floor(a)) trim(String.format(Locale.US, "%.2f", a))
        else group(a.roundToLong(), isIndian(code))
        return sign + symbol(code) + body
    }

    fun compact(amount: Double, code: String): String {
        val a = abs(amount)
        val s = (if (amount < 0) "-" else "") + symbol(code)
        fun one(x: Double) = trim(String.format(Locale.US, if (x >= 100) "%.0f" else "%.1f", x))
        return if (isIndian(code)) when {
            a >= 1e7 -> s + one(a / 1e7) + "Cr"
            a >= 1e5 -> s + one(a / 1e5) + "L"
            a >= 1e3 -> s + one(a / 1e3) + "k"
            else -> money(amount, code)
        } else when {
            a >= 1e9 -> s + one(a / 1e9) + "B"
            a >= 1e6 -> s + one(a / 1e6) + "M"
            a >= 1e3 -> s + one(a / 1e3) + "k"
            else -> money(amount, code)
        }
    }

    /** Human number: 13.5, 2.8, 1,040, 0.25 */
    fun num(x: Double): String = when {
        !x.isFinite() -> "—"
        abs(x) >= 100 -> group(x.roundToLong(), false)
        abs(x) >= 0.1 -> trim(String.format(Locale.US, "%.1f", x))
        x == 0.0 -> "0"
        else -> trim(String.format(Locale.US, "%.2f", x))
    }

    fun pct(x: Double): String = when {
        x > 0 && x < 0.1 -> "<0.1%"
        else -> num(x) + "%"
    }

    data class TimeText(val value: String, val unit: String, val short: String) {
        override fun toString() = "$value $unit"
        val compact get() = "$value $short"
    }

    fun time(price: Double, i: WorthMath.Income): TimeText? {
        val days = WorthMath.incomeDays(price, i) ?: return null
        if (i.usesCalendarDays) {
            val v = num(days)
            return TimeText(v, if (v == "1") "income day" else "income days", if (v == "1") "day" else "days")
        }
        val hours = WorthMath.incomeHours(price, i)
        if (days >= 1.0 || hours == null) {
            val v = num(days)
            return TimeText(v, if (v == "1") "working day" else "working days", if (v == "1") "day" else "days")
        }
        if (hours >= 1.0) {
            val v = num(hours)
            return TimeText(v, if (v == "1") "working hour" else "working hours", "hrs")
        }
        val minutes = hours * 60
        return if (minutes < 1) TimeText("<1", "minute of work", "min")
        else TimeText(num(minutes), "minutes of work", "min")
    }

    fun monthYear(ms: Long): String = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date(ms))

    fun shortDate(ms: Long): String = SimpleDateFormat("d MMM", Locale.getDefault()).format(Date(ms))

    fun monthName(ms: Long): String = SimpleDateFormat("MMMM", Locale.getDefault()).format(Date(ms))

    fun addMonths(ms: Long, months: Int): Long =
        Calendar.getInstance().apply { timeInMillis = ms; add(Calendar.MONTH, months) }.timeInMillis

    fun durationLeft(ms: Long): String {
        if (ms <= 0) return "now"
        val h = ms / WorthMath.HOUR_MS
        val d = h / 24
        return when {
            d >= 1 -> "${d}d ${h % 24}h"
            h >= 1 -> "${h}h ${(ms / 60_000) % 60}m"
            else -> "${maxOf(1, ms / 60_000)}m"
        }
    }

    fun coolOffLabel(hours: Int): String = when (hours) {
        24 -> "a day"
        48 -> "2 days"
        168 -> "a week"
        336 -> "2 weeks"
        else -> "${hours / 24} days"
    }

    fun monthsBetween(from: Long, to: Long): Int {
        val a = Calendar.getInstance().apply { timeInMillis = from }
        val b = Calendar.getInstance().apply { timeInMillis = to }
        val m = (b.get(Calendar.YEAR) - a.get(Calendar.YEAR)) * 12 + (b.get(Calendar.MONTH) - a.get(Calendar.MONTH))
        return maxOf(0, m)
    }

    fun ago(ms: Long, now: Long): String {
        val days = (now - ms) / WorthMath.DAY_MS
        return when {
            days < 1 -> "today"
            days < 2 -> "yesterday"
            days < 30 -> "$days days ago"
            days < 365 -> "${days / 30} months ago".replace("1 months", "a month")
            else -> "${days / 365} years ago".replace("1 years", "a year")
        }
    }

    /** Rounds to two significant figures: 45,312 → 45,000. */
    fun nice(x: Double): Double {
        if (x <= 0) return 0.0
        var mag = 1.0
        while (x / mag >= 100) mag *= 10
        return Math.round(x / mag) * mag
    }
}
