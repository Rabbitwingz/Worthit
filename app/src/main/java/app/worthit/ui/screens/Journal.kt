package app.worthit.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Compare
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.worthit.data.AppData
import app.worthit.data.Fmt
import app.worthit.data.Guess
import app.worthit.data.Insights
import app.worthit.data.Profile
import app.worthit.data.Status
import app.worthit.data.Subscription
import app.worthit.data.WorthMath
import app.worthit.ui.AppViewModel
import app.worthit.ui.Draft
import app.worthit.ui.Route
import app.worthit.ui.components.BtnStyle
import app.worthit.ui.components.ChoiceChip
import app.worthit.ui.components.ChoiceGroup
import app.worthit.ui.components.EmptyState
import app.worthit.ui.components.ExpressiveButton
import app.worthit.ui.components.MoneyField
import app.worthit.ui.components.RoundIconButton
import app.worthit.ui.components.ScreenTitle
import app.worthit.ui.components.SectionCard
import app.worthit.ui.components.ShapeBadge
import app.worthit.ui.components.Stepper
import app.worthit.ui.components.rememberNow
import app.worthit.ui.components.screenPadding
import app.worthit.ui.components.shape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.MaterialShapes

private enum class Filter(val label: String) {
    All("All"), Thinking("Thinking"), Wishlist("Wishlist"), Saving("Saving"),
    Bought("Bought"), LetGo("Let go"), Subs("Subscriptions"),
}

@Composable
fun JournalScreen(vm: AppViewModel, data: AppData) {
    val profile = data.profile
    val now by rememberNow()
    var filter by rememberSaveable { mutableStateOf(Filter.All) }
    val selected = remember { mutableStateListOf<String>() }
    var showAdd by remember { mutableStateOf(false) }
    var showSub by remember { mutableStateOf(false) }

    val list = remember(data.purchases, filter) {
        data.purchases.filter {
            when (filter) {
                Filter.All, Filter.Subs -> true
                Filter.Thinking -> it.status == Status.Considering
                Filter.Wishlist -> it.status == Status.Wishlist
                Filter.Saving -> it.status == Status.Saving
                Filter.Bought -> it.status == Status.Bought
                Filter.LetGo -> it.status == Status.Skipped
            }
        }.sortedByDescending { it.boughtAt ?: it.createdAt }
    }

    fun toggle(id: String) {
        if (id in selected) selected.remove(id) else if (selected.size < 5) selected.add(id)
        else vm.toast("You can compare up to 5 at a time.")
    }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = screenPadding(80.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                ScreenTitle(
                    "Journal",
                    if (data.purchases.isEmpty()) "Everything you've weighed lives here." else "${data.purchases.size} things weighed so far",
                )
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 6.dp)) {
                    items(Filter.entries) { f -> ChoiceChip(f.label, f == filter, { filter = f; selected.clear() }) }
                }
            }
            if (filter == Filter.Subs) {
                subscriptionItems(this, data, vm)
            } else {
                if (list.isEmpty()) {
                    item {
                        EmptyState(
                            "🛍️",
                            if (filter == Filter.All) "Nothing weighed yet" else "Nothing here yet",
                            "Type something you're eyeing on Home and see what it really costs you.",
                        )
                    }
                } else {
                    if (list.size >= 2 && selected.isEmpty()) {
                        item {
                            Text(
                                "Tip: long-press to pick items to compare.",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    items(list, key = { it.id }) { p ->
                        PurchaseRow(
                            p, profile, now,
                            onClick = { if (selected.isNotEmpty()) toggle(p.id) else vm.push(Route.Detail(p.id)) },
                            selected = p.id in selected,
                            onLongClick = { toggle(p.id) },
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = selected.isNotEmpty(),
            modifier = Modifier.align(Alignment.TopCenter),
            enter = slideInVertically { -it } + fadeIn(),
            exit = slideOutVertically { -it } + fadeOut(),
        ) {
            Surface(
                Modifier.fillMaxWidth().statusBarsPadding().padding(12.dp),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.inverseSurface,
                contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                shadowElevation = 8.dp,
            ) {
                Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    RoundIconButton(
                        Icons.Rounded.Close, "Clear", { selected.clear() },
                        container = MaterialTheme.colorScheme.inverseSurface, content = MaterialTheme.colorScheme.inverseOnSurface,
                    )
                    Text("${selected.size} selected", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    ExpressiveButton(
                        "Compare", {
                            vm.push(Route.Compare(selected.toList()))
                            selected.clear()
                        },
                        icon = Icons.Rounded.Compare,
                        enabled = selected.size >= 2,
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = selected.isEmpty(),
            modifier = Modifier.align(Alignment.BottomEnd).navigationBarsPadding().padding(end = 20.dp, bottom = 96.dp),
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut(),
        ) {
            ExpressiveButton(
                if (filter == Filter.Subs) "Subscription" else "Weigh something",
                { if (filter == Filter.Subs) showSub = true else showAdd = true },
                icon = Icons.Rounded.Add,
                large = true,
            )
        }
    }

    if (showAdd) QuickAddSheet(profile, onDismiss = { showAdd = false }) { draft ->
        showAdd = false
        vm.push(Route.Decide(draft))
    }
    if (showSub) AddSubscriptionSheet(profile, onDismiss = { showSub = false }) { sub ->
        showSub = false
        vm.addSubscription(sub)
        vm.toast("${sub.emoji} ${sub.name} added")
    }
}

private fun subscriptionItems(scope: androidx.compose.foundation.lazy.LazyListScope, data: AppData, vm: AppViewModel) {
    val profile = data.profile
    val income = profile.income()
    val subs = data.subscriptions
    with(scope) {
        if (subs.isEmpty()) {
            item {
                EmptyState(
                    "🔁",
                    "No subscriptions yet",
                    "Small monthly amounts add up. Add one and see what it costs you in a year.",
                )
            }
            return
        }
        item {
            val annual = Insights.subsAnnual(subs)
            SectionCard(color = MaterialTheme.colorScheme.primaryContainer) {
                Text("Every year, your subscriptions cost", style = MaterialTheme.typography.titleMedium)
                Text(Fmt.money(annual, profile.currency), style = MaterialTheme.typography.displayMedium)
                Fmt.time(annual, income)?.let { Text("= ${it.value} ${it.unit} a year", style = MaterialTheme.typography.titleLarge) }
            }
        }
        items(subs, key = { it.id }) { s ->
            val now = System.currentTimeMillis()
            val months = Fmt.monthsBetween(s.startedAt, now)
            val paid = if (s.yearly) s.amount * (months / 12 + 1) else s.amount * maxOf(1, months)
            Surface(
                Modifier.fillMaxWidth().animateItem(),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    ShapeBadge(MaterialShapes.Cookie9Sided, MaterialTheme.colorScheme.surfaceContainerHighest, 52.dp) {
                        Text(s.emoji, fontSize = 24.sp)
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(s.name, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "${Fmt.money(s.amount, profile.currency)}/${if (s.yearly) "yr" else "mo"} → ${Fmt.money(s.annual, profile.currency)}/yr" +
                                (Fmt.time(s.annual, income)?.let { " · ${it.compact}/yr" } ?: ""),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (months >= 2) {
                            Text(
                                "Paying for $months months · ${Fmt.money(paid, profile.currency)} so far",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.secondary,
                            )
                        }
                    }
                    RoundIconButton(Icons.Rounded.Delete, "Remove", { vm.deleteSubscription(s.id) }, size = 40.dp)
                }
            }
        }
    }
}

@Composable
fun QuickAddSheet(profile: Profile, onDismiss: () -> Unit, onContinue: (Draft) -> Unit) {
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    val guess = remember(name) { Guess.from(name) }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.padding(horizontal = 24.dp).padding(bottom = 24.dp).navigationBarsPadding()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ShapeBadge(guess.category.shape(), MaterialTheme.colorScheme.primaryContainer, 52.dp) { Text(guess.emoji, fontSize = 24.sp) }
                Spacer(Modifier.width(12.dp))
                Text("What are you weighing?", style = MaterialTheme.typography.headlineSmall)
            }
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = name, onValueChange = { name = it.take(40) },
                label = { Text("What is it?") }, singleLine = true,
                shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(10.dp))
            MoneyField("Price", price, { price = it }, profile.currency)
            Spacer(Modifier.height(18.dp))
            val p = price.toDoubleOrNull() ?: 0.0
            ExpressiveButton(
                "See what it costs me",
                { onContinue(Draft(name.trim().ifBlank { "Something nice" }, p, guess.emoji, guess.category)) },
                Modifier.fillMaxWidth(),
                enabled = p > 0,
                large = true,
            )
        }
    }
}

private val subEmojis = listOf("🎬", "🎵", "☁️", "🏋️", "📰", "🎮", "🍿", "📦")

@Composable
private fun AddSubscriptionSheet(profile: Profile, onDismiss: () -> Unit, onAdd: (Subscription) -> Unit) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var yearly by remember { mutableStateOf(false) }
    var months by remember { mutableIntStateOf(6) }
    var emoji by remember { mutableStateOf(subEmojis.first()) }
    val income = profile.income()
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.padding(horizontal = 24.dp).padding(bottom = 24.dp).navigationBarsPadding()) {
            Text("Add a subscription", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(12.dp))
            ChoiceGroup(subEmojis, emoji, { emoji = it }, { it })
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = name, onValueChange = { name = it.take(30) },
                label = { Text("Name") }, singleLine = true,
                shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(10.dp))
            MoneyField("Amount", amount, { amount = it }, profile.currency)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ChoiceChip("Monthly", !yearly, { yearly = false })
                ChoiceChip("Yearly", yearly, { yearly = true })
            }
            Spacer(Modifier.height(12.dp))
            Stepper("Paying for", months, { months = it }, 0..120, if (months == 1) "month" else "months")
            val a = amount.toDoubleOrNull() ?: 0.0
            if (a > 0) {
                val annual = if (yearly) a else a * 12
                Spacer(Modifier.height(12.dp))
                Text(
                    "${Fmt.money(annual, profile.currency)} a year" + (Fmt.time(annual, income)?.let { " = ${it.value} ${it.unit}" } ?: ""),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(Modifier.height(16.dp))
            ExpressiveButton(
                "Add", {
                    onAdd(
                        Subscription(
                            id = app.worthit.ui.newId(), name = name.trim().ifBlank { "Subscription" }, emoji = emoji,
                            amount = a, yearly = yearly,
                            startedAt = Fmt.addMonths(System.currentTimeMillis(), -months),
                        ),
                    )
                },
                Modifier.fillMaxWidth(), enabled = a > 0, large = true,
            )
        }
    }
}
