package app.worthit.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.worthit.data.AppData
import app.worthit.data.Fmt
import app.worthit.data.Insights
import app.worthit.ui.AppViewModel
import app.worthit.ui.components.Bar
import app.worthit.ui.components.BtnStyle
import app.worthit.ui.components.ExpressiveButton
import app.worthit.ui.components.ScreenTitle
import app.worthit.ui.components.SectionCard
import app.worthit.ui.components.ShapeBadge
import app.worthit.ui.components.Stat
import app.worthit.ui.components.rememberNow
import app.worthit.ui.components.reveal
import app.worthit.ui.components.screenPadding
import app.worthit.ui.theme.Motion

@Composable
fun InsightsScreen(vm: AppViewModel, data: AppData) {
    val profile = data.profile
    val income = profile.income()
    val ps = data.purchases
    val now by rememberNow()
    val scheme = MaterialTheme.colorScheme
    val month = remember(ps, now) { Insights.period(ps, Insights.monthStart(now), Long.MAX_VALUE) }
    val year = remember(ps, now) { Insights.period(ps, Insights.yearStart(now), Long.MAX_VALUE) }
    val worth = remember(ps) { Insights.worthRate(ps) }
    val regret = remember(ps) { Insights.regretRate(ps) }
    val cats = remember(ps) { Insights.categoryStats(ps) }
    val sweet = remember(ps, income) { Insights.sweetSpot(ps, income) }
    val cpu = remember(ps) { Insights.avgCostPerUse(ps) }
    val exp = remember(ps) { Insights.experienceShare(ps) }
    val (letGoCount, letGoSum) = remember(ps) { Insights.letGo(ps) }
    val reviewedCount = remember(ps) { Insights.reviewed(ps).size }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = screenPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { ScreenTitle("Insights", "What your purchases are telling you.") }

        item {
            SectionCard(Modifier.reveal(0), title = Fmt.monthName(now), color = scheme.primaryContainer) {
                Text("Logged spending", style = MaterialTheme.typography.labelLarge)
                Text(Fmt.money(month.total, profile.currency), style = MaterialTheme.typography.displayMedium)
                Fmt.time(month.total, income)?.takeIf { month.total > 0 }?.let {
                    Text("= ${it.value} ${it.unit}", style = MaterialTheme.typography.titleLarge)
                }
                if (month.byCategory.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    month.byCategory.take(5).forEachIndexed { i, (c, amt) ->
                        Row(Modifier.fillMaxWidth().padding(bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("${c.emoji} ${c.label}", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                            Text(Fmt.compact(amt, profile.currency), style = MaterialTheme.typography.labelLarge)
                        }
                        Bar((amt / month.total).toFloat(), scheme.primary, Modifier.padding(bottom = 10.dp), delayMillis = i * 90)
                    }
                } else {
                    Spacer(Modifier.height(6.dp))
                    Text("Nothing logged as bought this month.", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        if (year.total > 0) {
            item {
                SectionCard(Modifier.reveal(1), title = "This year") {
                    Row {
                        Stat(Fmt.compact(year.total, profile.currency), "discretionary spending", Modifier.weight(1f))
                        Fmt.time(year.total, income)?.let { Stat(it.value, it.unit, Modifier.weight(1f)) }
                    }
                    Fmt.time(year.total, income)?.let {
                        Spacer(Modifier.height(10.dp))
                        Text(
                            "You've spent approximately ${it.value} ${it.unit.replace("working ", "")} of income on things you chose this year.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = scheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        item {
            if (worth != null) {
                SectionCard(Modifier.reveal(2), title = "Worth-it rate") {
                    val anim by animateFloatAsState((worth / 100).toFloat(), Motion.gentle(), label = "worth")
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(110.dp), contentAlignment = Alignment.Center) {
                            CircularWavyProgressIndicator(progress = { anim }, modifier = Modifier.fillMaxSize())
                            Text("${Fmt.num(anim * 100.0)}%", style = MaterialTheme.typography.headlineSmall)
                        }
                        Spacer(Modifier.width(18.dp))
                        Column {
                            Text("of $reviewedCount reviewed purchases were “definitely” worth it.", style = MaterialTheme.typography.bodyLarge)
                            regret?.let {
                                Spacer(Modifier.height(6.dp))
                                Text("${Fmt.num(it)}% were later rated “not really”.", style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            } else {
                Locked(
                    "Worth-it rate",
                    "Review ${Insights.moreNeeded(ps)} more bought item${if (Insights.moreNeeded(ps) == 1) "" else "s"} to see how often your purchases pay off.",
                    Modifier.reveal(2),
                )
            }
        }

        item {
            if (cats.isNotEmpty()) {
                SectionCard(Modifier.reveal(3), title = "Your purchase patterns") {
                    cats.forEachIndexed { i, s ->
                        Row(Modifier.fillMaxWidth().padding(bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("${s.category.emoji} ${s.category.label}", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                            Text("${Fmt.num(s.rate)}% worth it", style = MaterialTheme.typography.labelLarge)
                        }
                        Bar((s.rate / 100).toFloat(), if (s.rate >= 50) scheme.tertiary else scheme.secondary, Modifier.padding(bottom = 10.dp), delayMillis = i * 90)
                    }
                    val best = cats.first()
                    val worst = cats.last()
                    if (best != worst) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "${best.category.label} are your highest-value category. ${worst.category.label} are where regret tends to hide.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = scheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                Locked("Purchase patterns", "Needs 5+ reviewed purchases, with at least two in a category.", Modifier.reveal(3))
            }
        }

        item {
            if (sweet != null) {
                Highlight("🎯", "Your sweet spot", "Purchases costing ${sweet.first.label} have your highest satisfaction: ${Fmt.num(sweet.second)}% worth it.", Modifier.reveal(4))
            } else {
                Locked("Your sweet spot", "Needs 6+ reviewed purchases to find the price range that makes you happiest.", Modifier.reveal(4))
            }
        }

        if (exp != null) {
            item {
                val text = if (exp >= 50) "You tend to spend more on experiences than on things (${Fmt.num(exp)}% of spending)."
                else "Most of your spending goes to things rather than experiences (${Fmt.num(100 - exp)}%)."
                Highlight("🧭", "Your spending personality", text, Modifier.reveal(5))
            }
        }

        if (cpu.isNotEmpty()) {
            item {
                SectionCard(Modifier.reveal(6), title = "Average cost per use") {
                    cpu.take(4).forEach { (c, v) ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Text("${c.emoji} ${c.label}", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                            Text(Fmt.money(v, profile.currency), style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }

        if (letGoCount > 0) {
            item {
                val t = Fmt.time(letGoSum, income)
                Highlight(
                    "🌱", "Things you let go",
                    "$letGoCount thing${if (letGoCount > 1) "s" else ""}, ${Fmt.money(letGoSum, profile.currency)}${t?.let { ", about ${it.value} ${it.unit}" } ?: ""} you kept.",
                    Modifier.reveal(7),
                )
            }
        }

        if (ps.isEmpty()) {
            item {
                SectionCard(Modifier.reveal(8), title = "Want a preview?") {
                    Text(
                        "Insights grow as you log and review purchases. Load a year of sample data to see what this screen becomes. You can delete it anytime in You.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(Modifier.height(12.dp))
                    ExpressiveButton("Load sample data", { vm.loadSample() }, style = BtnStyle.Tonal)
                }
            }
        }
    }
}

@Composable
private fun Highlight(emoji: String, title: String, body: String, modifier: Modifier = Modifier) {
    SectionCard(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ShapeBadge(MaterialShapes.Sunny, MaterialTheme.colorScheme.tertiaryContainer, 56.dp) { Text(emoji, fontSize = 24.sp) }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun Locked(title: String, body: String, modifier: Modifier = Modifier) {
    SectionCard(modifier, color = MaterialTheme.colorScheme.surfaceContainer) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ShapeBadge(MaterialShapes.Cookie12Sided, MaterialTheme.colorScheme.surfaceContainerHighest, 56.dp) {
                Icon(Icons.Rounded.Lock, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
