package app.worthit.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.worthit.data.AppData
import app.worthit.data.Checkins
import app.worthit.data.Desire
import app.worthit.data.Fmt
import app.worthit.data.Purchase
import app.worthit.data.Review
import app.worthit.data.Status
import app.worthit.data.Verdict
import app.worthit.data.WorthMath
import app.worthit.ui.AppViewModel
import app.worthit.ui.Draft
import app.worthit.ui.Route
import app.worthit.ui.components.BtnStyle
import app.worthit.ui.components.CountdownRing
import app.worthit.ui.components.ExpressiveButton
import app.worthit.ui.components.MorphingBlob
import app.worthit.ui.components.RoundIconButton
import app.worthit.ui.components.SectionCard
import app.worthit.ui.components.Stat
import app.worthit.ui.components.pressScale
import app.worthit.ui.components.rememberNow
import app.worthit.ui.components.reveal
import app.worthit.ui.components.shape
import app.worthit.ui.components.tick
import app.worthit.ui.shareText
import app.worthit.ui.theme.Motion
import androidx.compose.material3.LinearWavyProgressIndicator

@Composable
fun DetailScreen(vm: AppViewModel, data: AppData, id: String) {
    val p = data.purchases.firstOrNull { it.id == id }
    if (p == null) {
        LaunchedEffect(Unit) { vm.pop() }
        return
    }
    val profile = data.profile
    val income = profile.income()
    val now by rememberNow()
    val context = LocalContext.current
    var confirmDelete by remember { mutableStateOf(false) }
    var askWhen by remember { mutableStateOf(false) }
    val top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = top + 8.dp, bottom = bottom + 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RoundIconButton(Icons.AutoMirrored.Rounded.ArrowBack, "Back", { vm.pop() })
                Spacer(Modifier.weight(1f))
                RoundIconButton(Icons.Rounded.Share, "Share", {
                    val t = Fmt.time(p.price, income)
                    shareText(
                        context,
                        buildString {
                            append("${p.emoji} ${p.name}: ${Fmt.money(p.price, profile.currency)}")
                            if (t != null) append(" = ${t.value} ${t.unit} for me")
                            p.costPerUse?.let { append(", about ${Fmt.money(it, profile.currency)} per use") }
                            p.verdict?.let { append(". Verdict: ${it.label} ${it.emoji}") }
                            append("\n— via Worth It")
                        },
                    )
                })
                Spacer(Modifier.width(8.dp))
                RoundIconButton(Icons.Rounded.Delete, "Delete", { confirmDelete = true })
            }
        }
        item {
            Column(Modifier.fillMaxWidth().reveal(0), horizontalAlignment = Alignment.CenterHorizontally) {
                MorphingBlob(MaterialTheme.colorScheme.primaryContainer, Modifier.size(120.dp)) { Text(p.emoji, fontSize = 52.sp) }
                Spacer(Modifier.height(12.dp))
                Text(p.name, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
                Text(Fmt.money(p.price, profile.currency), style = MaterialTheme.typography.displayMedium)
                Fmt.time(p.price, income)?.let {
                    Text("= ${it.value} ${it.unit}", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.height(8.dp))
                StatusTag(p, now)
            }
        }
        item { StatsCard(p, vm, data, Modifier.reveal(1)) }
        item { StatusCard(p, vm, data, now, onBought = { askWhen = true }, modifier = Modifier.reveal(2)) }
        if (p.status == Status.Bought) {
            item { ReviewCard(p, vm, now, Modifier.reveal(3)) }
        }
        if (p.reviews.isNotEmpty()) {
            item { ReviewTimeline(p, Modifier.reveal(4)) }
        }
    }

    if (askWhen) {
        BoughtWhenSheet(onDismiss = { askWhen = false }) { at ->
            askWhen = false
            vm.setStatus(p.id, Status.Bought, at)
            data.goals.firstOrNull { it.purchaseId == p.id && it.completedAt == null }?.let { vm.completeGoal(it.id) }
            vm.celebrate()
            vm.toast("Enjoy it! ${p.emoji}")
        }
    }
    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Remove ${p.name}?") },
            text = { Text("This removes it and its reviews from your journal.") },
            confirmButton = {
                TextButton({ confirmDelete = false; vm.pop(); vm.deletePurchase(p.id) }) { Text("Remove") }
            },
            dismissButton = { TextButton({ confirmDelete = false }) { Text("Keep") } },
            shape = RoundedCornerShape(32.dp),
        )
    }
}

@Composable
private fun StatsCard(p: Purchase, vm: AppViewModel, data: AppData, modifier: Modifier) {
    val profile = data.profile
    val income = profile.income()
    SectionCard(modifier, title = "The numbers") {
        val stats = buildList {
            WorthMath.incomeDays(p.price, income)?.let { add(Fmt.num(it) to if (income.usesCalendarDays) "income days" else "working days") }
            WorthMath.incomeHours(p.price, income)?.let { add(Fmt.num(it) to "working hours") }
            WorthMath.salaryPercentage(p.price, income)?.let { add(Fmt.pct(it) to "of monthly income") }
            p.costPerUse?.let { add(Fmt.money(it, profile.currency) to "per use") }
            if (p.frequency != null && p.lifespan != null) add(Fmt.num(WorthMath.totalUses(p.frequency, p.lifespan)) to "expected uses")
            p.lifespan?.let { add(it.long to "expected life") }
            if (p.desire in Desire.emojis.indices) add(Desire.emojis[p.desire] to Desire.labels[p.desire])
        }
        stats.chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth().padding(bottom = 14.dp)) {
                row.forEach { (v, l) -> Stat(v, l, Modifier.weight(1f)) }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
        Text(
            "${p.category.emoji} ${p.category.label} · ${p.type.emoji} ${p.type.label} · added ${Fmt.shortDate(p.createdAt)}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(12.dp))
        ExpressiveButton(
            "Re-think it", {
                vm.push(
                    Route.Decide(
                        Draft(p.name, p.price, p.emoji, p.category, p.type, p.frequency, p.lifespan, p.desire, existingId = p.id),
                    ),
                )
            },
            icon = Icons.Rounded.Tune, style = BtnStyle.Tonal,
        )
    }
}

@Composable
private fun StatusCard(p: Purchase, vm: AppViewModel, data: AppData, now: Long, onBought: () -> Unit, modifier: Modifier) {
    val profile = data.profile
    val scheme = MaterialTheme.colorScheme
    when (p.status) {
        Status.Considering -> SectionCard(modifier, title = "Still thinking?") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ExpressiveButton("Wishlist it", {
                    val hours = WorthMath.coolOffHours(p.price, profile.income())
                    vm.updatePurchase(p.id) {
                        it.copy(status = Status.Wishlist, coolOffUntil = if (profile.coolingOff) System.currentTimeMillis() + hours * WorthMath.HOUR_MS else null)
                    }
                }, Modifier.weight(1f), style = BtnStyle.Tonal)
                ExpressiveButton("Bought it", onBought, Modifier.weight(1f))
            }
        }
        Status.Wishlist -> SectionCard(modifier, title = "Cooling off", color = scheme.secondaryContainer) {
            val until = p.coolOffUntil
            if (until != null && until > now) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(contentAlignment = Alignment.Center) {
                        CountdownRing(1f - (until - now).toFloat() / (until - p.createdAt).coerceAtLeast(1), Modifier.size(72.dp))
                        Text("⏳", fontSize = 26.sp)
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(Fmt.durationLeft(until - now) + " to go", style = MaterialTheme.typography.headlineSmall)
                        Text(
                            "A short pause helps separate a passing impulse from a genuine want.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            } else {
                Text("Your pause is over. Still want it?", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ExpressiveButton("Let it go", {
                    vm.setStatus(p.id, Status.Skipped)
                    vm.toast("Let go. That's ${timeShort(p.price, profile) ?: "money"} kept 🌱")
                }, Modifier.weight(1f), style = BtnStyle.Outlined)
                ExpressiveButton("Bought it", onBought, Modifier.weight(1f))
            }
        }
        Status.Saving -> {
            val goal = data.goals.firstOrNull { it.purchaseId == p.id }
            SectionCard(modifier, title = "Saving up", color = scheme.tertiaryContainer) {
                if (goal != null) {
                    val anim by animateFloatAsState(goal.progress, Motion.gentle(), label = "goal")
                    Text("${Fmt.money(goal.saved, profile.currency)} of ${Fmt.money(goal.target, profile.currency)}", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(10.dp))
                    LinearWavyProgressIndicator(progress = { anim }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(14.dp))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExpressiveButton("See goal", { vm.popAll(); vm.tab = 2 }, Modifier.weight(1f), style = BtnStyle.Tonal)
                    ExpressiveButton("Bought it", onBought, Modifier.weight(1f))
                }
            }
        }
        Status.Skipped -> SectionCard(modifier, title = "You let this one go 🌱") {
            Text(
                "That's ${timeShort(p.price, profile) ?: Fmt.money(p.price, profile.currency)} of work still yours.",
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(Modifier.height(12.dp))
            ExpressiveButton("Changed my mind", { vm.setStatus(p.id, Status.Considering) }, style = BtnStyle.Tonal)
        }
        Status.Bought -> {
            p.boughtAt?.let { at ->
                SectionCard(modifier, color = scheme.surfaceContainer) {
                    Text("Bought ${Fmt.ago(at, now)}", style = MaterialTheme.typography.titleMedium)
                    p.costPerUse?.let { cpu ->
                        val days = ((now - at) / WorthMath.DAY_MS).coerceAtLeast(1)
                        val usesSoFar = (p.frequency!!.usesPerYear * days / 365.0).coerceAtLeast(1.0)
                        Text(
                            "If you've kept up ${p.frequency.short}, that's ~${Fmt.num(usesSoFar)} uses so far, " +
                                "${Fmt.money(p.price / usesSoFar, profile.currency)} each, heading towards ${Fmt.money(cpu, profile.currency)}.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = scheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewCard(p: Purchase, vm: AppViewModel, now: Long, modifier: Modifier) {
    var rating by rememberSaveable { mutableIntStateOf(p.rating ?: 0) }
    var worth by rememberSaveable { mutableStateOf<Verdict?>(null) }
    var again by rememberSaveable { mutableStateOf<Verdict?>(null) }
    val haptic = LocalHapticFeedback.current
    SectionCard(modifier, title = "How's it going?", subtitle = "Your honest take trains your personal insights.") {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            (1..5).forEach { i ->
                val on = i <= rating
                val scale by animateFloatAsState(if (on) 1.15f else 1f, Motion.bouncy(), label = "star")
                val interaction = remember { MutableInteractionSource() }
                Surface(
                    onClick = { haptic.tick(); rating = i },
                    shape = RoundedCornerShape(16.dp),
                    color = androidx.compose.ui.graphics.Color.Transparent,
                    interactionSource = interaction,
                    modifier = Modifier.pressScale(interaction, 0.8f),
                ) {
                    Icon(
                        if (on) Icons.Rounded.Star else Icons.Rounded.StarOutline, "$i stars",
                        Modifier.padding(6.dp).size(36.dp).graphicsLayer { scaleX = scale; scaleY = scale },
                        tint = if (on) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    )
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        Text("Was it worth it?", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(bottom = 8.dp))
        VerdictPicker(worth, { worth = it })
        Spacer(Modifier.height(14.dp))
        Text("Would you buy it again?", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(bottom = 8.dp))
        VerdictPicker(again, { again = it }, mapOf(Verdict.Yes to "Yes", Verdict.Maybe to "Maybe", Verdict.No to "No"))
        Spacer(Modifier.height(16.dp))
        ExpressiveButton(
            "Save review", {
                val stage = p.boughtAt?.let { Checkins.stageFor(it, now) } ?: 0
                vm.addReview(p.id, Review(stage = stage, at = now, rating = rating.takeIf { it > 0 }, worth = worth, buyAgain = again))
                if (worth == Verdict.Yes) vm.celebrate()
                vm.toast("Review saved ${worth?.emoji ?: "✍️"}")
                worth = null; again = null
            },
            Modifier.fillMaxWidth(),
            enabled = rating > 0 || worth != null || again != null,
        )
    }
}

@Composable
private fun ReviewTimeline(p: Purchase, modifier: Modifier) {
    SectionCard(modifier, title = "Your check-ins") {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            p.reviews.sortedByDescending { it.at }.forEach { r ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surfaceContainerHighest) {
                        Text(
                            if (r.stage > 0) "Day ${r.stage}" else Fmt.shortDate(r.at),
                            Modifier.padding(horizontal = 10.dp, vertical = 6.dp).widthIn(min = 48.dp),
                            style = MaterialTheme.typography.labelMedium,
                            textAlign = TextAlign.Center,
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    val parts = listOfNotNull(
                        r.rating?.let { "★".repeat(it) },
                        r.usage?.let { listOf("", "Barely used", "Used sometimes", "Used all the time")[it] },
                        r.worth?.let { "Worth it: ${it.label} ${it.emoji}" },
                        r.buyAgain?.let { "Buy again: ${it.label}" },
                    )
                    Text(parts.joinToString(" · "), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
fun CompareScreen(vm: AppViewModel, data: AppData, ids: List<String>) {
    val profile = data.profile
    val income = profile.income()
    val items = ids.mapNotNull { id -> data.purchases.firstOrNull { it.id == id } }
    val top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val scheme = MaterialTheme.colorScheme

    data class CmpRow(val label: String, val values: List<Double?>, val format: (Double) -> String, val lowIsBest: Boolean?)

    val rows = listOf(
        CmpRow("Price", items.map { it.price }, { Fmt.compact(it, profile.currency) }, null),
        CmpRow("Work days", items.map { WorthMath.incomeDays(it.price, income) }, { Fmt.num(it) }, null),
        CmpRow("Uses", items.map { p -> if (p.frequency != null && p.lifespan != null) WorthMath.totalUses(p.frequency, p.lifespan) else null }, { Fmt.num(it) }, false),
        CmpRow("Cost / use", items.map { it.costPerUse }, { Fmt.money(it, profile.currency) }, true),
        CmpRow("Lifespan", items.map { it.lifespan?.years }, { if (it < 1) "<1 yr" else "${Fmt.num(it)} yr" }, false),
        CmpRow("Desire", items.map { if (it.desire >= 0) it.desire.toDouble() else null }, { Desire.emojis[it.toInt()] }, false),
    )

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = top + 8.dp, bottom = bottom + 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RoundIconButton(Icons.AutoMirrored.Rounded.ArrowBack, "Back", { vm.pop() })
                Spacer(Modifier.width(12.dp))
                Text("Which one?", style = MaterialTheme.typography.headlineMedium)
            }
        }
        item {
            Surface(Modifier.fillMaxWidth().reveal(0), shape = RoundedCornerShape(32.dp), color = scheme.surfaceContainerLow) {
                Row(Modifier.horizontalScroll(rememberScrollState()).padding(16.dp)) {
                    Column {
                        Spacer(Modifier.height(64.dp))
                        rows.forEach { r ->
                            Box(Modifier.height(48.dp), contentAlignment = Alignment.CenterStart) {
                                Text(r.label, style = MaterialTheme.typography.labelLarge, color = scheme.onSurfaceVariant)
                            }
                        }
                    }
                    items.forEachIndexed { col, p ->
                        Column(Modifier.width(112.dp).padding(start = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(Modifier.height(64.dp), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(p.emoji, fontSize = 26.sp)
                                    Text(p.name, style = MaterialTheme.typography.labelLarge, maxLines = 1, textAlign = TextAlign.Center)
                                }
                            }
                            rows.forEach { r ->
                                val v = r.values[col]
                                val present = r.values.filterNotNull()
                                val best = r.lowIsBest != null && v != null && present.size >= 2 &&
                                    v == (if (r.lowIsBest) present.min() else present.max())
                                Box(Modifier.height(48.dp), contentAlignment = Alignment.Center) {
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = if (best) scheme.tertiaryContainer else androidx.compose.ui.graphics.Color.Transparent,
                                        contentColor = if (best) scheme.onTertiaryContainer else scheme.onSurface,
                                    ) {
                                        Text(
                                            v?.let(r.format) ?: "—",
                                            Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            style = MaterialTheme.typography.titleSmall,
                                            maxLines = 1,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        item {
            SectionCard(Modifier.reveal(1), title = "Value perspective", subtitle = "We don't pick a winner. You do.") {
                val notes = buildList {
                    items.filter { it.costPerUse != null }.minByOrNull { it.costPerUse!! }?.let { add(it to "Lowest cost per use") }
                    items.maxByOrNull { it.price }?.let { add(it to "Highest upfront cost") }
                    items.filter { it.costPerUse != null }.maxByOrNull { it.costPerUse!! }?.let {
                        add(it to "Highest cost per use, though it might bring the most joy")
                    }
                    items.filter { it.lifespan != null }.maxByOrNull { it.lifespan!!.years }?.let { add(it to "Lasts the longest") }
                    items.filter { it.desire >= 0 }.maxByOrNull { it.desire }?.let { add(it to "The one you want most") }
                }.distinctBy { it.second }
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    notes.forEach { (p, note) ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            app.worthit.ui.components.ShapeBadge(p.category.shape(), scheme.surfaceContainerHighest, 44.dp) { Text(p.emoji, fontSize = 20.sp) }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(p.name, style = MaterialTheme.typography.titleMedium)
                                Text(note, style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
                            }
                        }
                    }
                }
                if (items.any { it.costPerUse == null }) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Tip: set how often you'll use each item to compare cost per use.",
                        style = MaterialTheme.typography.labelMedium,
                        color = scheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
