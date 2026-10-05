package app.worthit.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.DirectionsBike
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.worthit.data.Fmt
import app.worthit.data.IncomeType
import app.worthit.data.Profile
import app.worthit.data.Voice
import app.worthit.data.WorthMath
import app.worthit.ui.AppViewModel
import app.worthit.ui.components.BtnStyle
import app.worthit.ui.components.ChoiceChip
import app.worthit.ui.components.ChoiceGroup
import app.worthit.ui.components.ExpressiveButton
import app.worthit.ui.components.GroupingTransformation
import app.worthit.ui.components.MoneyField
import app.worthit.ui.components.MorphingBlob
import app.worthit.ui.components.PlainField
import app.worthit.ui.components.RollingText
import app.worthit.ui.components.Stepper
import app.worthit.ui.components.pressScale
import app.worthit.ui.components.reveal
import app.worthit.ui.components.sanitizeAmount
import app.worthit.ui.components.tick
import app.worthit.ui.components.bleed
import app.worthit.ui.theme.Motion
import kotlinx.coroutines.delay

private const val STEPS = 5

@Composable
fun OnboardingScreen(vm: AppViewModel, initial: Profile) {
    var step by rememberSaveable { mutableIntStateOf(0) }
    var currency by rememberSaveable { mutableStateOf(initial.currency) }
    var incomeText by rememberSaveable { mutableStateOf("") }
    var incomeType by rememberSaveable { mutableStateOf(IncomeType.TakeHome) }
    var days by rememberSaveable { mutableIntStateOf(22) }
    var hours by rememberSaveable { mutableIntStateOf(8) }
    var fixedSchedule by rememberSaveable { mutableStateOf(true) }
    var fixed by rememberSaveable { mutableStateOf("") }
    var invest by rememberSaveable { mutableStateOf("") }
    var save by rememberSaveable { mutableStateOf("") }
    var voice by rememberSaveable { mutableStateOf(Voice.Balanced) }
    var finishing by remember { mutableStateOf(false) }
    val focus = LocalFocusManager.current

    fun profile() = initial.copy(
        currency = currency,
        monthlyIncome = incomeText.toDoubleOrNull() ?: 0.0,
        incomeType = incomeType,
        workingDaysPerMonth = days.toDouble(),
        hoursPerDay = hours.toDouble(),
        fixedSchedule = fixedSchedule,
        fixedExpenses = fixed.toDoubleOrNull() ?: 0.0,
        investments = invest.toDoubleOrNull() ?: 0.0,
        savingsTarget = save.toDoubleOrNull() ?: 0.0,
        voice = voice,
    )

    BackHandler(enabled = step > 0 && !finishing) { step-- }

    if (finishing) {
        LaunchedEffect(Unit) {
            delay(1300)
            vm.finishOnboarding(profile())
        }
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            LoadingIndicator(Modifier.size(96.dp))
            Spacer(Modifier.height(20.dp))
            Text("Turning money into time…", style = MaterialTheme.typography.titleLarge)
        }
        return
    }

    Column(Modifier.fillMaxSize().systemBarsPadding().imePadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(STEPS) { i ->
                val w by animateFloatAsState(if (i == step) 3f else 1f, Motion.bouncy(), label = "dotW")
                val c by animateColorAsState(
                    if (i <= step) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest,
                    label = "dotC",
                )
                Box(Modifier.weight(w).height(6.dp).clip(CircleShape).background(c))
            }
        }

        AnimatedContent(
            targetState = step,
            modifier = Modifier.weight(1f),
            transitionSpec = {
                val forward = targetState > initialState
                (slideInHorizontally(Motion.slide()) { if (forward) it else -it } + fadeIn()) togetherWith
                    (slideOutHorizontally(Motion.slide()) { if (forward) -it / 2 else it / 2 } + fadeOut())
            },
            label = "onboarding",
        ) { s ->
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)) {
                when (s) {
                    0 -> Welcome()
                    1 -> IncomeStep(currency, { currency = it }, incomeText, { incomeText = it }, incomeType, { incomeType = it }, days, fixedSchedule)
                    2 -> ScheduleStep(days, { days = it }, hours, { hours = it }, fixedSchedule, { fixedSchedule = it }, profile())
                    3 -> BaselineStep(currency, fixed, { fixed = it }, invest, { invest = it }, save, { save = it }, profile())
                    else -> VoiceStep(voice, { voice = it })
                }
            }
        }

        Row(Modifier.fillMaxWidth().padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
            AnimatedVisibility(step > 0, enter = fadeIn() + scaleIn(), exit = fadeOut() + scaleOut()) {
                ExpressiveButton("Back", { step-- }, style = BtnStyle.Text)
            }
            Spacer(Modifier.weight(1f))
            val label = when {
                step == 0 -> "Get started"
                step == 1 && incomeText.isBlank() -> "Skip for now"
                step == 3 && fixed.isBlank() && invest.isBlank() && save.isBlank() -> "Skip"
                step == STEPS - 1 -> "Let's go"
                else -> "Continue"
            }
            AnimatedContent(label, transitionSpec = {
                (slideInVertically { it } + fadeIn()) togetherWith (slideOutVertically { -it } + fadeOut())
            }, label = "cta") { l ->
                ExpressiveButton(
                    l, {
                        focus.clearFocus()
                        if (step < STEPS - 1) step++ else finishing = true
                    },
                    icon = Icons.AutoMirrored.Rounded.ArrowForward,
                    large = true,
                )
            }
        }
    }
}

@Composable
private fun Welcome() {
    // Vector icons stay razor sharp at hero size (colour emoji are small bitmaps and blur when enlarged).
    val icons = listOf(
        Icons.Rounded.Headphones, Icons.Rounded.Flight, Icons.Rounded.LocalCafe,
        Icons.Rounded.SportsEsports, Icons.Rounded.DirectionsBike, Icons.Rounded.CameraAlt, Icons.Rounded.ShoppingBag,
    )
    var i by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1500)
            i = (i + 1) % icons.size
        }
    }
    val scheme = MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth().padding(top = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(260.dp), contentAlignment = Alignment.Center) {
            MorphingBlob(scheme.tertiaryContainer, Modifier.size(260.dp), holdMillis = 1700, spinMillis = 40_000)
            MorphingBlob(scheme.primary, Modifier.size(196.dp), holdMillis = 1100) {
                AnimatedContent(icons[i], transitionSpec = {
                    (scaleIn(Motion.bouncy(), initialScale = 0.2f) + fadeIn()) togetherWith (scaleOut(targetScale = 0.2f) + fadeOut())
                }, label = "welcomeIcon") { icon ->
                    Icon(icon, null, Modifier.size(88.dp), tint = scheme.onPrimary)
                }
            }
        }
        Spacer(Modifier.height(32.dp))
        Text("What does money mean to you?", style = MaterialTheme.typography.displaySmall, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        Text(
            "Worth It turns prices into something more tangible: your time.",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(28.dp))
        listOf(
            "⏳" to "See any price as hours of your life",
            "🎯" to "Learn which purchases are actually worth it",
            "🌱" to "No judgement, no bank logins. It all stays on your phone",
        ).forEachIndexed { idx, (e, t) ->
            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp).reveal(idx + 2), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh) {
                    Text(e, Modifier.padding(10.dp), fontSize = 22.sp)
                }
                Spacer(Modifier.width(14.dp))
                Text(t, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
private fun StepHeader(title: String, body: String) {
    Spacer(Modifier.height(16.dp))
    Text(title, style = MaterialTheme.typography.displaySmall)
    Spacer(Modifier.height(8.dp))
    Text(body, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(Modifier.height(24.dp))
}

@Composable
private fun IncomeStep(
    currency: String, onCurrency: (String) -> Unit,
    income: String, onIncome: (String) -> Unit,
    type: IncomeType, onType: (IncomeType) -> Unit,
    days: Int, fixedSchedule: Boolean,
) {
    val focus = LocalFocusManager.current
    StepHeader("What's your monthly income?", "Just a rough number is fine. It never leaves your phone.")
    Row(Modifier.bleed(24.dp).horizontalScroll(rememberScrollState()).padding(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Fmt.currencies.forEach { c -> ChoiceChip("${c.symbol.trim()} ${c.code}", c.code == currency, { onCurrency(c.code) }) }
    }
    Spacer(Modifier.height(20.dp))
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(32.dp), color = MaterialTheme.colorScheme.primaryContainer) {
        Column(Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(Fmt.symbol(currency), style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.55f))
                Spacer(Modifier.width(4.dp))
                PlainField(
                    value = income,
                    onValueChange = { onIncome(sanitizeAmount(it)) },
                    placeholder = "0",
                    style = MaterialTheme.typography.displayMedium,
                    modifier = Modifier.weight(1f),
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done,
                    onIme = { focus.clearFocus() },
                    visualTransformation = GroupingTransformation(Fmt.isIndian(currency)),
                )
            }
            val m = income.toDoubleOrNull() ?: 0.0
            AnimatedVisibility(m > 0) {
                val daily = WorthMath.dailyIncome(WorthMath.Income(m, days.toDouble(), fixedSchedule = fixedSchedule)) ?: 0.0
                Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.Bottom) {
                    Text("That's about ", style = MaterialTheme.typography.titleMedium)
                    RollingText(Fmt.money(daily, currency), MaterialTheme.typography.titleLarge)
                    Text(" a day", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
    Spacer(Modifier.height(20.dp))
    ChoiceGroup(IncomeType.entries, type, onType, { it.label })
    AnimatedContent(type.blurb, label = "blurb") {
        Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 10.dp))
    }
}

@Composable
private fun ScheduleStep(
    days: Int, onDays: (Int) -> Unit,
    hours: Int, onHours: (Int) -> Unit,
    fixedSchedule: Boolean, onFixed: (Boolean) -> Unit,
    profile: Profile,
) {
    StepHeader("How much do you work?", "This is how we turn prices into working days and hours.")
    AnimatedVisibility(fixedSchedule) {
        Column {
            Stepper("Working days", days, onDays, 1..31, "per month")
            Spacer(Modifier.height(12.dp))
            Stepper("Hours", hours, onHours, 1..18, "per day")
            Spacer(Modifier.height(12.dp))
        }
    }
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surfaceContainerLow) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("My schedule is irregular", style = MaterialTheme.typography.titleMedium)
                Text("We'll use calendar-day income instead.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(!fixedSchedule, { onFixed(!it) })
        }
    }
    val income = profile.income()
    val hourly = WorthMath.hourlyIncome(income)
    val daily = WorthMath.dailyIncome(income)
    if (daily != null) {
        Spacer(Modifier.height(16.dp))
        Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.tertiaryContainer) {
            Column(Modifier.padding(20.dp)) {
                Text(if (hourly != null) "An hour of your work is worth" else "A day of your income is worth", style = MaterialTheme.typography.titleMedium)
                RollingText(Fmt.money(hourly ?: daily, profile.currency), MaterialTheme.typography.displaySmall)
            }
        }
    }
}

@Composable
private fun BaselineStep(
    currency: String,
    fixed: String, onFixed: (String) -> Unit,
    invest: String, onInvest: (String) -> Unit,
    save: String, onSave: (String) -> Unit,
    profile: Profile,
) {
    StepHeader("Help us calculate affordability", "Totally optional. Skip it and add it later.")
    MoneyField("Fixed expenses", fixed, onFixed, currency, supporting = "Rent, EMIs, bills")
    Spacer(Modifier.height(8.dp))
    MoneyField("Investments", invest, onInvest, currency, supporting = "SIPs, retirement")
    Spacer(Modifier.height(8.dp))
    MoneyField("Savings target", save, onSave, currency, supporting = "What you aim to set aside")
    val free = WorthMath.discretionary(profile.income())
    AnimatedVisibility(free != null) {
        Surface(Modifier.fillMaxWidth().padding(top = 16.dp), shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.tertiaryContainer) {
            Column(Modifier.padding(20.dp)) {
                Text("Free to spend each month", style = MaterialTheme.typography.titleMedium)
                RollingText(Fmt.money(free ?: 0.0, currency), MaterialTheme.typography.displaySmall)
            }
        }
    }
}

@Composable
private fun VoiceStep(voice: Voice, onVoice: (Voice) -> Unit) {
    val haptic = LocalHapticFeedback.current
    StepHeader("How should we talk to you?", "You can change this anytime.")
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Voice.entries.forEach { v ->
            val sel = v == voice
            val corner by animateDpAsState(if (sel) 20.dp else 32.dp, Motion.bouncy(), label = "voiceCorner")
            val bg by animateColorAsState(if (sel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow, label = "voiceBg")
            val interaction = remember { MutableInteractionSource() }
            Surface(
                onClick = { haptic.tick(); onVoice(v) },
                modifier = Modifier.fillMaxWidth().pressScale(interaction, 0.97f),
                shape = RoundedCornerShape(corner),
                color = bg,
                border = if (sel) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                interactionSource = interaction,
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text(v.label, style = MaterialTheme.typography.titleLarge)
                    Text(v.blurb, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(10.dp))
                    Surface(shape = RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomEnd = 18.dp, bottomStart = 18.dp), color = MaterialTheme.colorScheme.surface) {
                        Text("“${v.sample}”", Modifier.padding(horizontal = 14.dp, vertical = 10.dp), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}
