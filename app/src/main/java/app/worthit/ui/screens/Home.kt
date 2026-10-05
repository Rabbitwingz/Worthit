package app.worthit.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.lerp
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.worthit.data.AppData
import app.worthit.data.Checkins
import app.worthit.data.Fmt
import app.worthit.data.Guess
import app.worthit.data.Insights
import app.worthit.data.Profile
import app.worthit.data.Purchase
import app.worthit.data.Review
import app.worthit.data.Status
import app.worthit.data.Verdict
import app.worthit.data.WorthMath
import app.worthit.ui.AppViewModel
import app.worthit.ui.Draft
import app.worthit.ui.Route
import app.worthit.ui.components.BtnStyle
import app.worthit.ui.components.ExpressiveButton
import app.worthit.ui.components.GroupingTransformation
import app.worthit.ui.components.MorphingBlob
import app.worthit.ui.components.PlainField
import app.worthit.ui.components.RollingText
import app.worthit.ui.components.SectionTitle
import app.worthit.ui.components.ShapeBadge
import app.worthit.ui.components.pressScale
import app.worthit.ui.components.rememberNow
import app.worthit.ui.components.reveal
import app.worthit.ui.components.sanitizeAmount
import app.worthit.ui.components.screenPadding
import app.worthit.ui.components.shape
import app.worthit.ui.components.tick
import app.worthit.ui.components.bleed
import app.worthit.ui.theme.Motion
import java.util.Calendar

@Composable
fun HomeScreen(vm: AppViewModel, data: AppData) {
    val profile = data.profile
    val now by rememberNow()
    val checkins = remember(data.purchases, now) { Checkins.due(data.purchases, now) }
    val cooled = remember(data.purchases, now) {
        data.purchases.filter { it.status == Status.Wishlist && it.coolOffUntil != null && it.coolOffUntil <= now }
    }
    val recent = remember(data.purchases) { data.purchases.sortedByDescending { it.createdAt }.take(4) }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = screenPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { HomeHeader(profile, Modifier.reveal(0)) }
        item { CalculatorCard(vm, profile, Modifier.reveal(1)) }
        items(checkins, key = { "c" + it.purchase.id }) { due ->
            CheckinCard(due, vm, profile, Modifier.animateItem())
        }
        items(cooled, key = { "w" + it.id }) { p ->
            CooledCard(p, vm, profile, Modifier.animateItem())
        }
        item { InsightCard(data, now, Modifier.reveal(2)) }
        if (recent.isNotEmpty()) {
            item { SectionTitle("Recently weighed", "See all") { vm.tab = 1 } }
            items(recent, key = { it.id }) { p ->
                PurchaseRow(p, profile, now, onClick = { vm.push(Route.Detail(p.id)) }, modifier = Modifier.animateItem())
            }
        }
    }
}

@Composable
private fun HomeHeader(profile: Profile, modifier: Modifier = Modifier) {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when (hour) {
        in 5..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        in 17..21 -> "Good evening"
        else -> "Hey, night owl"
    }
    val income = profile.income()
    var flip by rememberSaveable { mutableIntStateOf(0) }
    val haptic = LocalHapticFeedback.current
    Row(modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        MorphingBlob(MaterialTheme.colorScheme.primary, Modifier.size(56.dp), holdMillis = 2600) {
            Text("⏳", fontSize = 24.sp)
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(greeting + if (profile.name.isNotBlank()) ", ${profile.name}" else "", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Worth It", style = MaterialTheme.typography.headlineLarge)
        }
        val hourly = WorthMath.hourlyIncome(income)
        val daily = WorthMath.dailyIncome(income)
        if (daily != null) {
            val options = listOfNotNull(
                hourly?.let { "Your hour" to Fmt.compact(it, profile.currency) },
                "Your day" to Fmt.compact(daily, profile.currency),
            )
            val (label, value) = options[flip % options.size]
            val interaction = remember { MutableInteractionSource() }
            Surface(
                onClick = { haptic.tick(); flip++ },
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                interactionSource = interaction,
                modifier = Modifier.pressScale(interaction, 0.9f),
            ) {
                AnimatedContent(label to value, transitionSpec = {
                    (slideInVertically { it } + fadeIn()) togetherWith (slideOutVertically { -it } + fadeOut())
                }, label = "rate") { (l, v) ->
                    Column(Modifier.padding(horizontal = 14.dp, vertical = 8.dp), horizontalAlignment = Alignment.End) {
                        Text(l, style = MaterialTheme.typography.labelSmall)
                        Text(v, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun CalculatorCard(vm: AppViewModel, profile: Profile, modifier: Modifier = Modifier) {
    val income = profile.income()
    var name by rememberSaveable { mutableStateOf("") }
    var priceText by rememberSaveable { mutableStateOf("") }
    val price = priceText.toDoubleOrNull() ?: 0.0
    val guess = remember(name) { Guess.from(name) }
    val focus = LocalFocusManager.current
    val scheme = MaterialTheme.colorScheme

    val gradient = Brush.linearGradient(
        listOf(scheme.primaryContainer, lerp(scheme.primaryContainer, scheme.tertiaryContainer, 0.55f)),
    )
    Surface(modifier.fillMaxWidth(), shape = RoundedCornerShape(40.dp), color = scheme.primaryContainer, contentColor = scheme.onPrimaryContainer) {
      Box(Modifier.background(gradient)) {
        MorphingBlob(
            scheme.onPrimaryContainer.copy(alpha = 0.06f),
            Modifier.align(Alignment.TopEnd).offset(x = 70.dp, y = (-60).dp).size(240.dp),
            holdMillis = 3000,
            spinMillis = 60_000,
        )
        Column(Modifier.padding(24.dp)) {
            Text("What are you eyeing?", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                AnimatedContent(
                    targetState = guess,
                    transitionSpec = {
                        (scaleIn(Motion.bouncy(), initialScale = 0.3f) + fadeIn()) togetherWith (scaleOut(targetScale = 0.3f) + fadeOut())
                    },
                    label = "emoji",
                ) { g ->
                    ShapeBadge(g.category.shape(), scheme.surface, 56.dp) { Text(g.emoji, fontSize = 26.sp) }
                }
                Spacer(Modifier.width(14.dp))
                PlainField(
                    value = name,
                    onValueChange = { name = it.take(40) },
                    placeholder = "A digital piano…",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(18.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    Fmt.symbol(profile.currency),
                    style = MaterialTheme.typography.displayMedium,
                    color = scheme.onPrimaryContainer.copy(alpha = 0.55f),
                )
                Spacer(Modifier.width(4.dp))
                PlainField(
                    value = priceText,
                    onValueChange = { priceText = sanitizeAmount(it) },
                    placeholder = "0",
                    style = MaterialTheme.typography.displayMedium,
                    modifier = Modifier.weight(1f),
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done,
                    onIme = { focus.clearFocus() },
                    visualTransformation = GroupingTransformation(Fmt.isIndian(profile.currency)),
                )
            }
            AnimatedVisibility(
                visible = price > 0,
                enter = expandVertically(Motion.bouncy()) + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                Column(Modifier.padding(top = 10.dp)) {
                    val t = Fmt.time(price, income)
                    if (t != null) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("≈ ", style = MaterialTheme.typography.headlineSmall)
                            RollingText(t.value, MaterialTheme.typography.headlineMedium)
                            Text(" ${t.unit}", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 3.dp))
                        }
                        WorthMath.salaryPercentage(price, income)?.let {
                            Text("${Fmt.pct(it)} of your monthly income", style = MaterialTheme.typography.bodyMedium, color = scheme.onPrimaryContainer.copy(alpha = 0.75f))
                        }
                    } else {
                        Text(
                            "Add your income in You to see this as working time.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
            ExpressiveButton(
                text = "Is it worth it?",
                icon = Icons.AutoMirrored.Rounded.ArrowForward,
                enabled = price > 0,
                large = true,
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    focus.clearFocus()
                    vm.push(
                        Route.Decide(
                            Draft(
                                name = name.trim().ifBlank { "Something nice" },
                                price = price,
                                emoji = guess.emoji,
                                category = guess.category,
                            ),
                        ),
                    )
                    name = ""
                    priceText = ""
                },
            )
            AnimatedVisibility(visible = name.isEmpty() && priceText.isEmpty()) {
                ExampleChips(profile) { n, p ->
                    name = n
                    priceText = p.toLong().toString()
                }
            }
        }
      }
    }
}

@Composable
private fun ExampleChips(profile: Profile, onPick: (String, Double) -> Unit) {
    val base = if (profile.monthlyIncome > 0) profile.monthlyIncome else 100_000.0
    val examples = listOf(
        "🎹" to ("Digital piano" to 0.45),
        "🎧" to ("Headphones" to 0.25),
        "✈️" to ("Weekend trip" to 0.2),
        "👟" to ("Sneakers" to 0.08),
        "☕" to ("Fancy coffee" to 0.003),
    )
    Column(Modifier.padding(top = 16.dp)) {
        Text("Or try one", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
        Spacer(Modifier.height(8.dp))
        Row(Modifier.bleed(24.dp).horizontalScroll(rememberScrollState()).padding(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            examples.forEach { (emoji, pair) ->
                val (label, f) = pair
                val price = Fmt.nice(base * f).coerceAtLeast(1.0)
                val interaction = remember { MutableInteractionSource() }
                Surface(
                    onClick = { onPick(label, price) },
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                    interactionSource = interaction,
                    modifier = Modifier.pressScale(interaction, 0.92f),
                ) {
                    Text(
                        "$emoji  $label",
                        Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}

private val firstImpressions = listOf("😖", "😕", "😐", "🙂", "🤩")

@Composable
private fun CheckinCard(due: Checkins.Due, vm: AppViewModel, profile: Profile, modifier: Modifier = Modifier) {
    val p = due.purchase
    val scheme = MaterialTheme.colorScheme
    val haptic = LocalHapticFeedback.current
    fun answer(review: Review, happy: Boolean) {
        vm.addReview(p.id, review)
        if (happy) vm.celebrate()
        vm.toast(if (happy) "Love that. Noted! ${p.emoji}" else "Thanks for being honest. That's how the patterns get smart.")
    }
    Surface(modifier.fillMaxWidth(), shape = RoundedCornerShape(32.dp), color = scheme.tertiaryContainer, contentColor = scheme.onTertiaryContainer) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ShapeBadge(p.category.shape(), scheme.surface, 44.dp) { Text(p.emoji, fontSize = 20.sp) }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("${p.name} · ${due.stage} days in", style = MaterialTheme.typography.labelLarge)
                    Text(Checkins.question(due.stage), style = MaterialTheme.typography.titleLarge)
                }
            }
            Spacer(Modifier.height(14.dp))
            val now = System.currentTimeMillis()
            when (due.stage) {
                7 -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    firstImpressions.forEachIndexed { i, e ->
                        val interaction = remember { MutableInteractionSource() }
                        Surface(
                            onClick = { haptic.tick(); answer(Review(stage = 7, at = now, rating = i + 1), i >= 3) },
                            shape = RoundedCornerShape(18.dp),
                            color = scheme.surface.copy(alpha = 0.6f),
                            interactionSource = interaction,
                            modifier = Modifier.pressScale(interaction, 0.8f),
                        ) { Text(e, Modifier.padding(10.dp), fontSize = 26.sp) }
                    }
                }
                30 -> {
                    val labels = mapOf(Verdict.Yes to "All the time", Verdict.Maybe to "Sometimes", Verdict.No to "Barely")
                    VerdictPicker(null, { v ->
                        val usage = when (v) { Verdict.Yes -> 3; Verdict.Maybe -> 2; Verdict.No -> 1 }
                        answer(Review(stage = 30, at = now, usage = usage), v == Verdict.Yes)
                    }, labels)
                }
                90 -> VerdictPicker(null, { v -> answer(Review(stage = 90, at = now, worth = v), v == Verdict.Yes) })
                else -> {
                    val labels = mapOf(Verdict.Yes to "In a heartbeat", Verdict.Maybe to "Maybe", Verdict.No to "Nope")
                    VerdictPicker(null, { v -> answer(Review(stage = due.stage, at = now, buyAgain = v), v == Verdict.Yes) }, labels)
                }
            }
        }
    }
}

@Composable
private fun CooledCard(p: Purchase, vm: AppViewModel, profile: Profile, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    var showWhen by remember { mutableStateOf(false) }
    Surface(modifier.fillMaxWidth(), shape = RoundedCornerShape(32.dp), color = scheme.secondaryContainer, contentColor = scheme.onSecondaryContainer) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ShapeBadge(p.category.shape(), scheme.surface, 44.dp) { Text(p.emoji, fontSize = 20.sp) }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Your pause is over", style = MaterialTheme.typography.labelLarge)
                    Text("Still want the ${p.name}?", style = MaterialTheme.typography.titleLarge)
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ExpressiveButton("Let it go", {
                    vm.setStatus(p.id, Status.Skipped)
                    val t = timeShort(p.price, profile)
                    vm.toast("Nice. ${t ?: Fmt.money(p.price, profile.currency)} of work stays yours 🌱")
                }, style = BtnStyle.Outlined, modifier = Modifier.weight(1f))
                ExpressiveButton("Yes, bought it", { showWhen = true }, modifier = Modifier.weight(1f))
            }
        }
    }
    if (showWhen) {
        BoughtWhenSheet(onDismiss = { showWhen = false }) { at ->
            showWhen = false
            vm.setStatus(p.id, Status.Bought, at)
            vm.celebrate()
            vm.toast("Enjoy it! We'll check in to see if it was worth it.")
        }
    }
}

@Composable
private fun InsightCard(data: AppData, now: Long, modifier: Modifier = Modifier) {
    val profile = data.profile
    val income = profile.income()
    val insights = remember(data, now) {
        buildList {
            val month = Insights.period(data.purchases, Insights.monthStart(now), Long.MAX_VALUE)
            if (month.total > 0) {
                val t = Fmt.time(month.total, income)
                add("📅" to "You've logged ${Fmt.money(month.total, profile.currency)} this month${t?.let { " — about ${it.compact} of work" } ?: ""}.")
            }
            val (n, kept) = Insights.letGo(data.purchases)
            if (n > 0) {
                val t = Fmt.time(kept, income)
                add("🌱" to "Letting go of $n thing${if (n > 1) "s" else ""} kept ${t?.compact ?: Fmt.money(kept, profile.currency)} of your time in your pocket.")
            }
            Insights.averagePrice(data.purchases)?.let { avg ->
                Fmt.time(avg, income)?.let { add("⚖️" to "Your average purchase costs ${it.compact} of work.") }
            }
            Insights.worthRate(data.purchases)?.let { add("💚" to "You've rated ${Fmt.num(it)}% of your purchases as worth it.") }
            val subs = Insights.subsAnnual(data.subscriptions)
            if (subs > 0) Fmt.time(subs, income)?.let { add("🔁" to "Your subscriptions cost ${it.compact} of work every year.") }
            add("💡" to "A ${Fmt.compact(3_000.0, profile.currency)} gadget used twice can be worse value than a ${Fmt.compact(45_000.0, profile.currency)} piano played every week.")
            add("🧭" to "Spend less on things you don't care about. Spend confidently on things you do.")
        }
    }
    var index by rememberSaveable { mutableIntStateOf(0) }
    val haptic = LocalHapticFeedback.current
    val interaction = remember { MutableInteractionSource() }
    Surface(
        onClick = { haptic.tick(); index++ },
        modifier = modifier.fillMaxWidth().pressScale(interaction, 0.97f),
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        interactionSource = interaction,
    ) {
        AnimatedContent(
            targetState = insights[index % insights.size],
            transitionSpec = { (slideInVertically { it / 2 } + fadeIn()) togetherWith (slideOutVertically { -it / 2 } + fadeOut()) },
            label = "insight",
        ) { (emoji, text) ->
            Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(emoji, fontSize = 28.sp)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("Quick insight", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    Text(text, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}
