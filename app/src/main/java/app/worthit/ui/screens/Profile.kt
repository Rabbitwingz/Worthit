package app.worthit.ui.screens

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Language
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.worthit.data.AppData
import app.worthit.data.Fmt
import app.worthit.data.IncomeType
import app.worthit.data.Voice
import app.worthit.data.WorthMath
import app.worthit.ui.AppViewModel
import app.worthit.ui.components.BtnStyle
import app.worthit.ui.components.ChoiceGroup
import app.worthit.ui.components.ExpressiveButton
import app.worthit.ui.components.MoneyField
import app.worthit.ui.components.ScreenTitle
import app.worthit.ui.components.SectionCard
import app.worthit.ui.components.Stat
import app.worthit.ui.components.Stepper
import app.worthit.ui.components.screenPadding
import app.worthit.ui.components.toInput
import app.worthit.ui.shareText
import kotlin.math.roundToInt

@Composable
fun ProfileScreen(vm: AppViewModel, data: AppData) {
    val profile = data.profile
    val income = profile.income()
    val context = LocalContext.current
    var confirmDelete by remember { mutableStateOf(false) }

    var name by rememberSaveable { mutableStateOf(profile.name) }
    var incomeText by rememberSaveable { mutableStateOf(profile.monthlyIncome.toInput()) }
    var fixed by rememberSaveable { mutableStateOf(profile.fixedExpenses.toInput()) }
    var invest by rememberSaveable { mutableStateOf(profile.investments.toInput()) }
    var savings by rememberSaveable { mutableStateOf(profile.savingsTarget.toInput()) }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = screenPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { ScreenTitle("You", "Everything here stays on this phone.") }

        item {
            SectionCard(color = MaterialTheme.colorScheme.primaryContainer) {
                Text("Your time is worth", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Row {
                    WorthMath.hourlyIncome(income)?.let { Stat(Fmt.money(it, profile.currency), "per hour", Modifier.weight(1f)) }
                    WorthMath.dailyIncome(income)?.let { Stat(Fmt.money(it, profile.currency), "per day", Modifier.weight(1f)) }
                        ?: Text("Add your income below to start.", style = MaterialTheme.typography.bodyLarge)
                }
                WorthMath.discretionary(income)?.let {
                    Spacer(Modifier.height(12.dp))
                    Stat(Fmt.money(it, profile.currency), "free to spend each month")
                }
            }
        }

        item {
            SectionCard(title = "Income") {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(24); vm.updateProfile { p -> p.copy(name = name.trim()) } },
                    label = { Text("Your first name (optional)") },
                    singleLine = true,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                MoneyField("Monthly income", incomeText, {
                    incomeText = it
                    vm.updateProfile { p -> p.copy(monthlyIncome = it.toDoubleOrNull() ?: 0.0) }
                }, profile.currency)
                Spacer(Modifier.height(12.dp))
                ChoiceGroup(IncomeType.entries, profile.incomeType, { t -> vm.updateProfile { it.copy(incomeType = t) } }, { it.label })
                Text(profile.incomeType.blurb, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp))
                Spacer(Modifier.height(16.dp))
                Text("Currency", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(bottom = 8.dp))
                ChoiceGroup(Fmt.currencies, Fmt.currencies.firstOrNull { it.code == profile.currency }, { c -> vm.updateProfile { it.copy(currency = c.code) } }, { "${it.symbol.trim()} ${it.code}" })
                Text(
                    "Changing currency relabels amounts. Conversion isn't available.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }

        item {
            SectionCard(title = "Work schedule") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Fixed schedule", style = MaterialTheme.typography.titleMedium)
                        Text(
                            if (profile.fixedSchedule) "Based on working days and hours." else "Using calendar-day income (30 days).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(profile.fixedSchedule, { v -> vm.updateProfile { it.copy(fixedSchedule = v) } })
                }
                if (profile.fixedSchedule) {
                    Spacer(Modifier.height(12.dp))
                    Stepper("Working days", profile.workingDaysPerMonth.roundToInt(), { v -> vm.updateProfile { it.copy(workingDaysPerMonth = v.toDouble()) } }, 1..31, "per month")
                    Spacer(Modifier.height(10.dp))
                    Stepper("Hours", profile.hoursPerDay.roundToInt(), { v -> vm.updateProfile { it.copy(hoursPerDay = v.toDouble()) } }, 1..18, "per day")
                }
            }
        }

        item {
            SectionCard(title = "Monthly baseline", subtitle = "Optional. Unlocks affordability.") {
                MoneyField("Fixed expenses", fixed, { fixed = it; vm.updateProfile { p -> p.copy(fixedExpenses = it.toDoubleOrNull() ?: 0.0) } }, profile.currency, supporting = "Rent, EMIs, bills")
                Spacer(Modifier.height(8.dp))
                MoneyField("Investments", invest, { invest = it; vm.updateProfile { p -> p.copy(investments = it.toDoubleOrNull() ?: 0.0) } }, profile.currency, supporting = "SIPs, retirement")
                Spacer(Modifier.height(8.dp))
                MoneyField("Savings target", savings, { savings = it; vm.updateProfile { p -> p.copy(savingsTarget = it.toDoubleOrNull() ?: 0.0) } }, profile.currency, supporting = "What you aim to set aside")
            }
        }

        item {
            SectionCard(title = "How should Worth It talk to you?") {
                ChoiceGroup(Voice.entries, profile.voice, { v -> vm.updateProfile { it.copy(voice = v) } }, { it.label })
                Text(profile.voice.blurb, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp))
                Spacer(Modifier.height(16.dp))
                ToggleRow("Cooling-off pauses", "Wishlist items get a short pause before you buy.", profile.coolingOff) { v -> vm.updateProfile { it.copy(coolingOff = v) } }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Spacer(Modifier.height(12.dp))
                    ToggleRow("Match my wallpaper", "Use Material You dynamic colours.", profile.dynamicColor) { v -> vm.updateProfile { it.copy(dynamicColor = v) } }
                }
            }
        }

        item {
            SectionCard(title = "Your data", subtitle = "Local-first. No accounts, no tracking, no ads.") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExpressiveButton("Export", { shareText(context, vm.exportJson()) }, Modifier.weight(1f), icon = Icons.Rounded.Download, style = BtnStyle.Tonal)
                    ExpressiveButton("Sample data", { vm.loadSample() }, Modifier.weight(1f), icon = Icons.Rounded.AutoAwesome, style = BtnStyle.Tonal)
                }
                Spacer(Modifier.height(8.dp))
                ExpressiveButton("Delete everything", { confirmDelete = true }, Modifier.fillMaxWidth(), icon = Icons.Rounded.DeleteForever, style = BtnStyle.Outlined)
            }
        }

        item {
            val uri = LocalUriHandler.current
            ExpressiveButton(
                "worthitapp.vercel.app", { uri.openUri("https://worthitapp.vercel.app/") },
                Modifier.fillMaxWidth(), icon = Icons.Rounded.Language, style = BtnStyle.Text,
            )
        }

        item {
            Text(
                "Worth It is a personal awareness tool, not financial advice.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            )
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete everything?") },
            text = { Text("All purchases, goals, reviews and settings will be erased from this phone. This can't be undone.") },
            confirmButton = { TextButton({ confirmDelete = false; vm.deleteEverything() }) { Text("Delete") } },
            dismissButton = { TextButton({ confirmDelete = false }) { Text("Cancel") } },
            shape = RoundedCornerShape(32.dp),
        )
    }
}

@Composable
private fun ToggleRow(title: String, body: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.width(12.dp))
        Switch(checked, onChange)
    }
}
