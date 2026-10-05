package app.worthit.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.worthit.data.AppData
import app.worthit.data.Fmt
import app.worthit.data.Goal
import app.worthit.data.Guess
import app.worthit.data.Profile
import app.worthit.data.WorthMath
import app.worthit.ui.AppViewModel
import app.worthit.ui.components.BtnStyle
import app.worthit.ui.components.ChoiceChip
import app.worthit.ui.components.ChoiceGroup
import app.worthit.ui.components.EmptyState
import app.worthit.ui.components.ExpressiveButton
import app.worthit.ui.components.MoneyField
import app.worthit.ui.components.RollingText
import app.worthit.ui.components.RoundIconButton
import app.worthit.ui.components.ScreenTitle
import app.worthit.ui.components.SectionTitle
import app.worthit.ui.components.ShapeBadge
import app.worthit.ui.components.reveal
import app.worthit.ui.components.screenPadding
import app.worthit.ui.components.shape
import app.worthit.ui.newId
import app.worthit.ui.theme.Motion
import kotlin.math.roundToInt

@Composable
fun GoalsScreen(vm: AppViewModel, data: AppData) {
    val profile = data.profile
    val active = data.goals.filter { it.completedAt == null }
    val done = data.goals.filter { it.completedAt != null }.sortedByDescending { it.completedAt }
    var showNew by remember { mutableStateOf(false) }
    var contributeTo by remember { mutableStateOf<String?>(null) }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = screenPadding(80.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item { ScreenTitle("Goals", "Save toward things you genuinely want.") }
            if (active.isNotEmpty()) {
                item {
                    val saved = active.sumOf { it.saved }
                    val target = active.sumOf { it.target }
                    Surface(Modifier.fillMaxWidth().reveal(0), shape = RoundedCornerShape(32.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                        Column(Modifier.padding(22.dp)) {
                            Text("Saved so far", style = MaterialTheme.typography.titleMedium)
                            Text(Fmt.money(saved, profile.currency), style = MaterialTheme.typography.displayMedium)
                            Text(
                                "of ${Fmt.money(target, profile.currency)} across ${active.size} goal${if (active.size > 1) "s" else ""}",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }
                }
            }
            items(active, key = { it.id }) { g ->
                GoalCard(g, profile, vm, onAdd = { contributeTo = g.id }, modifier = Modifier.animateItem())
            }
            if (active.isEmpty()) {
                item {
                    EmptyState(
                        "🎯",
                        "No goals yet",
                        "Tap “Save up” on anything you're weighing, or start a goal here.",
                    )
                }
            }
            if (done.isNotEmpty()) {
                item { SectionTitle("Completed 🎉") }
                items(done, key = { it.id }) { g ->
                    Surface(Modifier.fillMaxWidth().animateItem(), shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surfaceContainerLow) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            ShapeBadge(Guess.from(g.name).category.shape(), MaterialTheme.colorScheme.tertiaryContainer, 48.dp) { Text(g.emoji, fontSize = 22.sp) }
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text(g.name, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "${Fmt.money(g.target, profile.currency)} · done ${Fmt.shortDate(g.completedAt ?: 0)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            RoundIconButton(Icons.Rounded.Delete, "Remove", { vm.deleteGoal(g.id) }, size = 40.dp)
                        }
                    }
                }
            }
        }
        ExpressiveButton(
            "New goal", { showNew = true },
            Modifier.align(Alignment.BottomEnd).navigationBarsPadding().padding(end = 20.dp, bottom = 96.dp),
            icon = Icons.Rounded.Add, large = true,
        )
    }

    if (showNew) NewGoalSheet(profile, onDismiss = { showNew = false }) { g ->
        showNew = false
        vm.addGoal(g)
        vm.toast("${g.emoji} Goal started. You've got this.")
    }
    contributeTo?.let { id ->
        val g = data.goals.firstOrNull { it.id == id }
        if (g != null) ContributeSheet(g, profile, onDismiss = { contributeTo = null }) { amount ->
            contributeTo = null
            vm.contribute(id, amount)
        }
    }
}

@Composable
private fun GoalCard(g: Goal, profile: Profile, vm: AppViewModel, onAdd: () -> Unit, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val anim by animateFloatAsState(g.progress, Motion.gentle(), label = "goalProgress")
    Surface(modifier.fillMaxWidth(), shape = RoundedCornerShape(32.dp), color = if (g.reached) scheme.tertiaryContainer else scheme.surfaceContainerLow) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ShapeBadge(Guess.from(g.name).category.shape(), scheme.surfaceContainerHighest, 52.dp) { Text(g.emoji, fontSize = 24.sp) }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(g.name, style = MaterialTheme.typography.titleLarge)
                    Text(
                        "${Fmt.money(g.saved, profile.currency)} of ${Fmt.money(g.target, profile.currency)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onSurfaceVariant,
                    )
                }
                RollingText("${(anim * 100).roundToInt()}%", MaterialTheme.typography.headlineMedium)
            }
            Spacer(Modifier.height(16.dp))
            LinearWavyProgressIndicator(progress = { anim }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            AnimatedContent(g.reached, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "reached") { reached ->
                if (reached) {
                    Column {
                        Text("You did it! 🎉 Time to enjoy it, guilt-free.", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(12.dp))
                        ExpressiveButton(
                            if (g.purchaseId != null) "I bought it" else "Mark complete",
                            { vm.completeGoal(g.id); vm.toast("${g.emoji} Goal complete!") },
                            Modifier.fillMaxWidth(), icon = Icons.Rounded.Celebration,
                        )
                    }
                } else {
                    Column {
                        val remaining = g.target - g.saved
                        val months = WorthMath.monthsToSave(remaining, g.monthly)
                        Text(
                            buildString {
                                if (g.monthly > 0) append("${Fmt.money(g.monthly, profile.currency)}/month")
                                if (months != null && months > 0) append(" · ready by ${Fmt.monthYear(Fmt.addMonths(System.currentTimeMillis(), months))}")
                                if (isEmpty()) append("${Fmt.money(remaining, profile.currency)} to go")
                            },
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            ExpressiveButton("Add money", onAdd, Modifier.weight(1f), icon = Icons.Rounded.Add, style = BtnStyle.Tonal)
                            RoundIconButton(Icons.Rounded.Delete, "Delete goal", { vm.deleteGoal(g.id) })
                        }
                    }
                }
            }
        }
    }
}

private val goalMonths = listOf(3, 6, 9, 12, 18, 24)

@Composable
private fun NewGoalSheet(profile: Profile, onDismiss: () -> Unit, onCreate: (Goal) -> Unit) {
    var name by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("") }
    var saved by remember { mutableStateOf("") }
    var byDate by remember { mutableStateOf(false) }
    var monthly by remember { mutableStateOf(suggestedMonthly(0.0, profile).takeIf { it > 1 }?.toLong()?.toString() ?: "") }
    var months by remember { mutableIntStateOf(6) }
    val guess = remember(name) { Guess.from(name) }
    val t = target.toDoubleOrNull() ?: 0.0
    val s = saved.toDoubleOrNull() ?: 0.0
    val remaining = (t - s).coerceAtLeast(0.0)
    val monthlyValue = if (byDate) WorthMath.monthlyNeeded(remaining, months) else monthly.toDoubleOrNull() ?: 0.0
    val eta = WorthMath.monthsToSave(remaining, monthlyValue)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.padding(horizontal = 24.dp).padding(bottom = 24.dp).navigationBarsPadding()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ShapeBadge(guess.category.shape(), MaterialTheme.colorScheme.primaryContainer, 52.dp) { Text(guess.emoji, fontSize = 24.sp) }
                Spacer(Modifier.width(12.dp))
                Text("New goal", style = MaterialTheme.typography.headlineSmall)
            }
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = name, onValueChange = { name = it.take(40) }, label = { Text("What are you saving for?") },
                singleLine = true, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(10.dp))
            MoneyField("Target", target, { target = it }, profile.currency)
            Spacer(Modifier.height(10.dp))
            MoneyField("Already saved", saved, { saved = it }, profile.currency)
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ChoiceChip("Monthly amount", !byDate, { byDate = false })
                ChoiceChip("By a date", byDate, { byDate = true })
            }
            Spacer(Modifier.height(12.dp))
            AnimatedContent(byDate, label = "mode") { date ->
                if (date) {
                    ChoiceGroup(goalMonths, months, { months = it }, { "$it months" })
                } else {
                    MoneyField("Each month", monthly, { monthly = it }, profile.currency)
                }
            }
            AnimatedVisibility(t > 0) {
                Surface(
                    Modifier.fillMaxWidth().padding(top = 14.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                ) {
                    Column(Modifier.padding(16.dp)) {
                        if (byDate) {
                            Text("Save ${Fmt.money(monthlyValue, profile.currency)}/month", style = MaterialTheme.typography.titleLarge)
                            Text("to have it by ${Fmt.monthYear(Fmt.addMonths(System.currentTimeMillis(), months))}.", style = MaterialTheme.typography.bodyMedium)
                        } else if (eta != null) {
                            Text(if (eta == 0) "You're already there!" else "Ready in ~$eta month${if (eta > 1) "s" else ""}", style = MaterialTheme.typography.titleLarge)
                            if (eta > 0) Text("That's ${Fmt.monthYear(Fmt.addMonths(System.currentTimeMillis(), eta))}.", style = MaterialTheme.typography.bodyMedium)
                        } else {
                            Text("Add a monthly amount to see when you'll get there.", style = MaterialTheme.typography.bodyMedium)
                        }
                        if (profile.savingsTarget > 0 && monthlyValue > 0) {
                            Text(
                                "That's ${Fmt.pct(monthlyValue / profile.savingsTarget * 100)} of your monthly savings capacity.",
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(top = 6.dp),
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            ExpressiveButton(
                "Start saving", {
                    onCreate(
                        Goal(
                            id = newId(), name = name.trim().ifBlank { "My goal" }, emoji = guess.emoji,
                            target = t, saved = s, monthly = monthlyValue,
                            targetDate = if (byDate) Fmt.addMonths(System.currentTimeMillis(), months) else null,
                            createdAt = System.currentTimeMillis(),
                        ),
                    )
                },
                Modifier.fillMaxWidth(), enabled = t > 0, large = true,
            )
        }
    }
}

@Composable
private fun ContributeSheet(g: Goal, profile: Profile, onDismiss: () -> Unit, onAdd: (Double) -> Unit) {
    var custom by remember { mutableStateOf("") }
    val remaining = (g.target - g.saved).coerceAtLeast(0.0)
    val quick = listOfNotNull(
        g.monthly.takeIf { it > 0 },
        Fmt.nice(g.target * 0.05).takeIf { it > 0 },
        Fmt.nice(g.target * 0.1).takeIf { it > 0 },
        remaining.takeIf { it > 0 },
    ).distinct()
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.padding(horizontal = 24.dp).padding(bottom = 24.dp).navigationBarsPadding()) {
            Text("Add to ${g.emoji} ${g.name}", style = MaterialTheme.typography.headlineSmall)
            Text("${Fmt.money(remaining, profile.currency)} to go", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(16.dp))
            ChoiceGroup(quick, null, { onAdd(it) }, { amt -> (if (amt == remaining) "All of it · " else "+ ") + Fmt.money(amt, profile.currency) })
            Spacer(Modifier.height(14.dp))
            MoneyField("Custom amount", custom, { custom = it }, profile.currency)
            Spacer(Modifier.height(14.dp))
            val c = custom.toDoubleOrNull() ?: 0.0
            ExpressiveButton("Add", { onAdd(c) }, Modifier.fillMaxWidth(), enabled = c > 0, large = true)
        }
    }
}
