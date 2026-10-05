package app.worthit.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import app.worthit.data.Category
import app.worthit.data.Desire
import app.worthit.data.Fmt
import app.worthit.data.Frequency
import app.worthit.data.Goal
import app.worthit.data.Insights
import app.worthit.data.Lifespan
import app.worthit.data.Profile
import app.worthit.data.Purchase
import app.worthit.data.PurchaseType
import app.worthit.data.Status
import app.worthit.data.Voice
import app.worthit.data.WorthMath
import app.worthit.ui.AppViewModel
import app.worthit.ui.Draft
import app.worthit.ui.components.Bar
import app.worthit.ui.components.BtnStyle
import app.worthit.ui.components.ChoiceChip
import app.worthit.ui.components.ChoiceGroup
import app.worthit.ui.components.CountUpText
import app.worthit.ui.components.DayGrid
import app.worthit.ui.components.ExpressiveButton
import app.worthit.ui.components.MorphingBlob
import app.worthit.ui.components.RollingText
import app.worthit.ui.components.RoundIconButton
import app.worthit.ui.components.SectionCard
import app.worthit.ui.components.ShapeBadge
import app.worthit.ui.components.Stat
import app.worthit.ui.components.pressScale
import app.worthit.ui.components.reveal
import app.worthit.ui.components.shape
import app.worthit.ui.components.tick
import app.worthit.ui.newId
import app.worthit.ui.shareText
import app.worthit.ui.theme.Motion
import androidx.compose.material3.MaterialShapes
import kotlin.math.roundToInt

@Composable
fun DecideScreen(vm: AppViewModel, data: AppData, draft: Draft) {
    val profile = data.profile
    val income = profile.income()
    val price = draft.price
    val existing = data.purchases.firstOrNull { it.id == draft.existingId }
    val context = LocalContext.current

    var category by rememberSaveable { mutableStateOf(draft.category) }
    var type by rememberSaveable { mutableStateOf(draft.type) }
    var frequency by rememberSaveable { mutableStateOf(draft.frequency) }
    var lifespan by rememberSaveable { mutableStateOf(draft.lifespan) }
    var desire by rememberSaveable { mutableIntStateOf(draft.desire) }
    var askWhen by remember { mutableStateOf(false) }

    val voice = profile.voice
    val time = Fmt.time(price, income)
    val score = WorthMath.worthScore(price, income, frequency, lifespan, desire, type)

    fun build(status: Status, boughtAt: Long? = null, coolOff: Long? = null) = Purchase(
        id = existing?.id ?: newId(),
        name = draft.name,
        emoji = draft.emoji,
        price = price,
        category = category,
        type = type,
        frequency = frequency,
        lifespan = lifespan,
        desire = desire,
        status = status,
        createdAt = existing?.createdAt ?: System.currentTimeMillis(),
        coolOffUntil = coolOff,
        boughtAt = boughtAt,
        reviews = existing?.reviews ?: emptyList(),
        notes = existing?.notes ?: "",
    )

    val top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = top + 8.dp, bottom = bottom + 180.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    RoundIconButton(Icons.AutoMirrored.Rounded.ArrowBack, "Back", { vm.pop() })
                    Spacer(Modifier.weight(1f))
                    RoundIconButton(Icons.Rounded.Share, "Share", {
                        shareText(context, shareCard(draft, profile, frequency, lifespan))
                    })
                }
            }
            item { Hero(draft, profile, Modifier.reveal(0)) }
            if (time != null) {
                item { MonthCard(price, profile, Modifier.reveal(1)) }
                item { AffordCard(price, profile, voice, Modifier.reveal(2)) }
            } else {
                item {
                    SectionCard(Modifier.reveal(1), title = "Make it personal") {
                        Text(
                            "Add your income to calculate working time. It takes ten seconds and never leaves your phone.",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Spacer(Modifier.height(12.dp))
                        ExpressiveButton("Add my income", { vm.popAll(); vm.tab = 4 }, style = BtnStyle.Tonal)
                    }
                }
            }
            item {
                MeaningCard(
                    price, profile,
                    category, { category = it },
                    type, { type = it },
                    frequency, { frequency = it },
                    lifespan, { lifespan = it },
                    Modifier.reveal(3),
                )
            }
            if (voice != Voice.Minimal) {
                item { OpportunityCard(price, data, Modifier.reveal(4)) }
            }
            item { DesireCard(desire, { desire = it }, Modifier.reveal(5)) }
            if (voice != Voice.Minimal) {
                item { ScoreCard(score, price, profile, frequency, lifespan, voice, Modifier.reveal(6)) }
            }
        }

        DecisionBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            onWishlist = {
                val hours = WorthMath.coolOffHours(price, income)
                val until = if (profile.coolingOff) System.currentTimeMillis() + hours * WorthMath.HOUR_MS else null
                vm.upsert(build(Status.Wishlist, coolOff = until))
                vm.pop()
                vm.toast(
                    if (until != null) "On your wishlist. We'll ask again in ${Fmt.coolOffLabel(hours)} ⏳"
                    else "Added to your wishlist.",
                )
            },
            onSave = {
                val p = build(Status.Saving)
                vm.upsert(p)
                val monthly = suggestedMonthly(price, profile)
                vm.addGoal(
                    Goal(
                        id = newId(), name = draft.name, emoji = draft.emoji, target = price,
                        monthly = monthly, createdAt = System.currentTimeMillis(), purchaseId = p.id,
                    ),
                )
                vm.popAll()
                vm.tab = 2
                val months = WorthMath.monthsToSave(price, monthly)
                vm.toast(
                    if (months != null) "Goal started. At ${Fmt.compact(monthly, profile.currency)}/month you'll have it by ${Fmt.monthYear(Fmt.addMonths(System.currentTimeMillis(), months))}."
                    else "Goal started!",
                )
            },
            onBought = { askWhen = true },
        )
    }

    if (askWhen) {
        BoughtWhenSheet(onDismiss = { askWhen = false }) { at ->
            askWhen = false
            vm.upsert(build(Status.Bought, boughtAt = at))
            vm.pop()
            vm.celebrate()
            vm.toast("Logged ${draft.emoji} We'll check in to see if it was worth it.")
        }
    }
}

fun suggestedMonthly(price: Double, profile: Profile): Double {
    val i = profile.income()
    val m = when {
        profile.savingsTarget > 0 -> profile.savingsTarget
        WorthMath.discretionary(i) != null -> WorthMath.discretionary(i)!! * 0.3
        i.hasIncome -> i.monthly * 0.1
        else -> price / 6
    }
    return Fmt.nice(m.coerceAtLeast(1.0))
}

private fun shareCard(draft: Draft, profile: Profile, f: Frequency?, l: Lifespan?): String {
    val money = Fmt.money(draft.price, profile.currency)
    val t = Fmt.time(draft.price, profile.income())
    return buildString {
        append("I was thinking about buying a $money ${draft.name} ${draft.emoji}.\n")
        if (t != null) append("That's ${t.value} ${t.unit} for me.\n")
        if (f != null && l != null) {
            append("But if I use it ${f.short} for ${l.long}…\n")
            append("${Fmt.money(WorthMath.costPerUse(draft.price, f, l), profile.currency)} per use.\n")
            append("Maybe it's worth it.\n")
        }
        append("\n— via Worth It")
    }
}

@Composable
private fun Hero(draft: Draft, profile: Profile, modifier: Modifier = Modifier) {
    val income = profile.income()
    val price = draft.price
    val scheme = MaterialTheme.colorScheme
    val haptic = LocalHapticFeedback.current
    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        val pop = remember { Animatable(0.4f) }
        LaunchedEffect(Unit) { pop.animateTo(1f, Motion.bouncy()) }
        MorphingBlob(
            scheme.primaryContainer,
            Modifier.size(140.dp).graphicsLayer { scaleX = pop.value; scaleY = pop.value },
        ) { Text(draft.emoji, fontSize = 60.sp) }
        Spacer(Modifier.height(14.dp))
        Text(draft.name, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        CountUpText(price, { Fmt.money(it, profile.currency) }, MaterialTheme.typography.displayLarge)

        val t = Fmt.time(price, income) ?: return@Column
        val hours = WorthMath.incomeHours(price, income)
        val modes = buildList {
            add(t.value to t.unit)
            if (hours != null && hours >= 1 && t.short == "days") add(Fmt.num(hours) to "working hours")
            WorthMath.salaryPercentage(price, income)?.let { add(Fmt.pct(it) to "of a month's income") }
            WorthMath.incomeMinutes(price, income)?.let { if (it >= 1) add(Fmt.num(it) to "minutes of work") }
        }
        var mode by rememberSaveable { mutableIntStateOf(0) }
        val (value, unit) = modes[mode % modes.size]
        val interaction = remember { MutableInteractionSource() }
        Spacer(Modifier.height(8.dp))
        Surface(
            onClick = { haptic.tick(); mode++ },
            shape = RoundedCornerShape(28.dp),
            color = scheme.tertiaryContainer,
            contentColor = scheme.onTertiaryContainer,
            interactionSource = interaction,
            modifier = Modifier.pressScale(interaction, 0.94f),
        ) {
            Row(Modifier.padding(horizontal = 22.dp, vertical = 14.dp), verticalAlignment = Alignment.Bottom) {
                Text("= ", style = MaterialTheme.typography.headlineMedium)
                RollingText(value, MaterialTheme.typography.headlineLarge)
                AnimatedContent(unit, transitionSpec = {
                    (slideInVertically { it } + fadeIn()) togetherWith (slideOutVertically { -it } + fadeOut())
                }, label = "unit") { u ->
                    Text(" $u", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 5.dp))
                }
            }
        }
        Text("tap to flip", style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant, modifier = Modifier.padding(top = 6.dp))
        if (profile.voice != Voice.Minimal && hours != null && t.short == "days") {
            Spacer(Modifier.height(8.dp))
            Text(
                "That's approximately ${Fmt.num(hours)} hours of work.",
                style = MaterialTheme.typography.bodyLarge,
                color = scheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MonthCard(price: Double, profile: Profile, modifier: Modifier = Modifier) {
    val income = profile.income()
    val days = WorthMath.incomeDays(price, income) ?: return
    val month = income.daysPerMonth.roundToInt()
    val caption = if (days <= month) {
        "${Fmt.num(days)} of your $month ${if (income.usesCalendarDays) "days" else "working days"} this month"
    } else {
        "${Fmt.num(days / month)} months of ${if (income.usesCalendarDays) "income" else "working days"}"
    }
    SectionCard(modifier, title = "Your working month", subtitle = caption) {
        DayGrid(days, month)
        Spacer(Modifier.height(12.dp))
        Text(
            "Based on ${profile.incomeType.label.lowercase()} income" + if (income.usesCalendarDays) " · calendar-day income" else "",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AffordCard(price: Double, profile: Profile, voice: Voice, modifier: Modifier = Modifier) {
    val income = profile.income()
    val pct = WorthMath.salaryPercentage(price, income) ?: return
    val months = WorthMath.affordabilityMonths(price, income)
    SectionCard(modifier, title = "Can I afford it?") {
        Row {
            Stat(Fmt.pct(pct), "of one month's income", Modifier.weight(1f))
            if (months != null) Stat(Fmt.num(months), "months of your free-to-spend money", Modifier.weight(1f))
        }
        if (voice != Voice.Minimal) {
            Spacer(Modifier.height(14.dp))
            val text = when {
                months == null -> "Add your fixed costs and savings in You to see how this fits your discretionary budget."
                months < 0.25 -> "This fits comfortably inside one month's free-to-spend money."
                months <= 1.0 -> "This would use about ${Fmt.pct(months * 100)} of this month's discretionary budget."
                else -> "This would use about ${Fmt.num(months)} months of your current discretionary budget."
            }
            Text(text, style = MaterialTheme.typography.bodyLarge)
        }
        if (profile.savingsTarget > 0) {
            val m = WorthMath.monthsToSave(price, profile.savingsTarget)
            if (m != null && m > 0) {
                Spacer(Modifier.height(10.dp))
                Text(
                    "Setting aside ${Fmt.money(profile.savingsTarget, profile.currency)}/month, you'd have it by ${Fmt.monthYear(Fmt.addMonths(System.currentTimeMillis(), m))} (~$m month${if (m > 1) "s" else ""}).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun MeaningCard(
    price: Double,
    profile: Profile,
    category: Category, onCategory: (Category) -> Unit,
    type: PurchaseType, onType: (PurchaseType) -> Unit,
    frequency: Frequency?, onFrequency: (Frequency) -> Unit,
    lifespan: Lifespan?, onLifespan: (Lifespan) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    SectionCard(modifier, title = "Make it meaningful", subtitle = "Value depends on how much you'll use it.") {
        Label("How often will you use it?")
        ChoiceGroup(Frequency.entries, frequency, onFrequency, { it.label })
        Spacer(Modifier.height(18.dp))
        Label("How long will you keep it?")
        ChoiceGroup(Lifespan.entries, lifespan, onLifespan, { it.label })

        AnimatedVisibility(
            visible = frequency != null && lifespan != null,
            enter = expandVertically(Motion.bouncy()) + fadeIn() + scaleIn(initialScale = 0.9f),
            exit = shrinkVertically() + fadeOut(),
        ) {
            val f = frequency ?: Frequency.Weekly
            val l = lifespan ?: Lifespan.One
            val uses = WorthMath.totalUses(f, l)
            val cpu = WorthMath.costPerUse(price, f, l)
            Surface(
                Modifier.fillMaxWidth().padding(top = 20.dp),
                shape = RoundedCornerShape(28.dp),
                color = scheme.tertiaryContainer,
                contentColor = scheme.onTertiaryContainer,
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text("That changes things.", style = MaterialTheme.typography.labelLarge)
                    Row(verticalAlignment = Alignment.Bottom) {
                        CountUpText(cpu, { Fmt.money(it, profile.currency) }, MaterialTheme.typography.displayMedium, durationMillis = 700)
                        Text(" per use", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
                    }
                    Text(
                        "Based on ${f.short} over ${l.long} ≈ ${Fmt.num(uses)} uses.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Fmt.time(cpu, profile.income())?.let {
                        Text("Each use costs about ${it.value} ${it.unit}.", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        Spacer(Modifier.height(18.dp))
        Label("What kind of purchase?")
        ChoiceGroup(PurchaseType.entries, type, onType, { it.label }, leading = { it.emoji })
        Spacer(Modifier.height(18.dp))
        Label("Category")
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Category.entries.forEach { c ->
                ChoiceChip(c.label, c == category, { onCategory(c) }, leading = c.emoji)
            }
        }
    }
}

@Composable
private fun Label(text: String) {
    Text(text, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(bottom = 10.dp))
}

@Composable
private fun OpportunityCard(price: Double, data: AppData, modifier: Modifier = Modifier) {
    val profile = data.profile
    val income = profile.income()
    val items = remember(price, data) {
        buildList {
            if (profile.investments > 0) add("📈" to "${Fmt.num(price / profile.investments)} months of your investment contributions")
            WorthMath.affordabilityMonths(price, income)?.let { add("🧾" to "${Fmt.num(it)} months of discretionary spending") }
            data.goals.filter { it.completedAt == null && it.target > it.saved }.take(2).forEach { g ->
                val pct = price / (g.target - g.saved) * 100
                add(g.emoji to if (pct >= 100) "Your whole ${g.name} goal, fully funded" else "${Fmt.pct(pct)} of what's left for ${g.name}")
            }
            data.subscriptions.maxByOrNull { it.annual }?.let { s ->
                add(s.emoji to "${Fmt.num(price / s.annual)} years of ${s.name}")
            }
            Insights.averagePrice(data.purchases)?.let { avg ->
                if (avg > 0) add("⚖️" to "${Fmt.num(price / avg)}× your average purchase")
            }
            WorthMath.incomeDays(price, income)?.let { add("🏖️" to "${Fmt.num(it)} days of income kept for later") }
            if (isEmpty()) add("🪴" to "Money left free for whatever comes next")
        }
    }
    SectionCard(modifier, title = "What else could this money do?") {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items.forEachIndexed { i, (emoji, text) ->
                Row(Modifier.reveal(i), verticalAlignment = Alignment.CenterVertically) {
                    ShapeBadge(MaterialShapes.Cookie6Sided, MaterialTheme.colorScheme.surfaceContainerHighest, 40.dp) {
                        Text(emoji, fontSize = 18.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(text, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
        if (profile.investments > 0) {
            Spacer(Modifier.height(10.dp))
            Text(
                "Not a prediction of returns, just another way to see it.",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DesireCard(desire: Int, onDesire: (Int) -> Unit, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val haptic = LocalHapticFeedback.current
    SectionCard(modifier, title = "How much do you want it?") {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Desire.emojis.forEachIndexed { i, e ->
                val sel = i == desire
                val scale by animateFloatAsState(if (sel) 1.25f else if (desire >= 0) 0.9f else 1f, Motion.bouncy(), label = "desire")
                val bg by animateColorAsState(if (sel) scheme.primaryContainer else scheme.surfaceContainerHighest, label = "desireBg")
                val interaction = remember { MutableInteractionSource() }
                Surface(
                    onClick = { haptic.tick(); onDesire(i) },
                    shape = RoundedCornerShape(if (sel) 22.dp else 30.dp),
                    color = bg,
                    interactionSource = interaction,
                    modifier = Modifier.size(64.dp).graphicsLayer { scaleX = scale; scaleY = scale },
                ) {
                    Box(contentAlignment = Alignment.Center) { Text(e, fontSize = 30.sp) }
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        AnimatedContent(desire, label = "desireLabel", transitionSpec = {
            (slideInVertically { it } + fadeIn()) togetherWith (slideOutVertically { -it } + fadeOut())
        }) { d ->
            Text(
                if (d in Desire.labels.indices) Desire.labels[d] else "Be honest, nobody's watching",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun ScoreCard(
    score: WorthMath.Score,
    price: Double,
    profile: Profile,
    frequency: Frequency?,
    lifespan: Lifespan?,
    voice: Voice,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val anim by animateFloatAsState((score.total / 10).toFloat(), Motion.gentle(), label = "score")
    SectionCard(modifier, title = "Worth it?", color = scheme.surfaceContainer) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(120.dp), contentAlignment = Alignment.Center) {
                CircularWavyProgressIndicator(progress = { anim }, modifier = Modifier.fillMaxSize())
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(Fmt.num(anim * 10.0), style = MaterialTheme.typography.headlineLarge)
                    Text("/ 10", style = MaterialTheme.typography.labelMedium, color = scheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.width(18.dp))
            Column(Modifier.weight(1f)) {
                AnimatedContent(WorthMath.scoreLabel(score.total), label = "scoreLabel") {
                    Text(it, style = MaterialTheme.typography.titleLarge)
                }
                Text(story(price, profile, frequency, lifespan, score.total), style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
            }
        }
        if (voice == Voice.Detailed) {
            Spacer(Modifier.height(18.dp))
            val rows = listOf(
                "Affordability" to score.affordability,
                "How often you'll use it" to score.usage,
                "How long it lasts" to score.lifespan,
                "How much you want it" to score.joy,
                "Need vs want" to score.need,
            )
            rows.forEachIndexed { i, (label, v) ->
                Text(label, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
                Bar(v.toFloat(), if (i % 2 == 0) scheme.primary else scheme.tertiary, delayMillis = i * 80)
            }
        }
        Spacer(Modifier.height(14.dp))
        Text(
            "A personal decision aid, not a financial recommendation.",
            style = MaterialTheme.typography.labelSmall,
            color = scheme.onSurfaceVariant,
        )
    }
}

private fun story(price: Double, profile: Profile, f: Frequency?, l: Lifespan?, score: Double): String {
    val t = Fmt.time(price, profile.income())
    val head = when {
        f != null && l != null && t != null ->
            "You're trading ${t.value} ${t.unit} for something you expect to use ~${Fmt.num(WorthMath.totalUses(f, l))} times over ${l.long}. "
        f == null || l == null -> "Tell us how often you'll use it to see the value side. "
        else -> ""
    }
    val tail = when {
        score >= 8.0 -> "That's potentially strong value, if you'll actually use it."
        score >= 6.5 -> "Solid value, as long as it doesn't end up in a drawer."
        score >= 5.0 -> "Could go either way. Usage is what tips it."
        else -> "Maybe give it a few days and see if the want sticks."
    }
    return head + tail
}

@Composable
private fun DecisionBar(modifier: Modifier, onWishlist: () -> Unit, onSave: () -> Unit, onBought: () -> Unit) {
    Surface(
        modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shadowElevation = 12.dp,
    ) {
        Column(Modifier.navigationBarsPadding().padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 14.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ExpressiveButton("Wishlist", onWishlist, Modifier.weight(1f), icon = Icons.Rounded.Bookmark, style = BtnStyle.Tonal, vertical = true)
                ExpressiveButton("Save up", onSave, Modifier.weight(1f), icon = Icons.Rounded.Savings, style = BtnStyle.Tonal, vertical = true)
                ExpressiveButton("Bought it", onBought, Modifier.weight(1f), icon = Icons.Rounded.Check, vertical = true)
            }
        }
    }
}
