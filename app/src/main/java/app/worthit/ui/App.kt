package app.worthit.ui

import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.worthit.data.AppData
import app.worthit.ui.components.Confetti
import app.worthit.ui.components.ExpressiveNavBar
import app.worthit.ui.components.NavItem
import app.worthit.ui.screens.CompareScreen
import app.worthit.ui.screens.DecideScreen
import app.worthit.ui.screens.DetailScreen
import app.worthit.ui.screens.GoalsScreen
import app.worthit.ui.screens.HomeScreen
import app.worthit.ui.screens.InsightsScreen
import app.worthit.ui.screens.JournalScreen
import app.worthit.ui.screens.OnboardingScreen
import app.worthit.ui.screens.ProfileScreen
import app.worthit.ui.theme.Motion
import kotlinx.coroutines.launch

private val navItems = listOf(
    NavItem("Home", Icons.Rounded.Home),
    NavItem("Journal", Icons.Rounded.ShoppingBag),
    NavItem("Goals", Icons.Rounded.Savings),
    NavItem("Insights", Icons.Rounded.AutoAwesome),
    NavItem("You", Icons.Rounded.Person),
)

@Composable
fun WorthItApp(vm: AppViewModel) {
    val data by vm.data.collectAsStateWithLifecycle()
    // A Surface (not a Box) so every Text inherits onSurface instead of defaulting to black.
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.onSurface) {
      Box(Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = data.profile.onboarded,
            transitionSpec = {
                (fadeIn(tween(450, 150)) + scaleIn(tween(450, 150), initialScale = 0.92f)) togetherWith
                    (fadeOut(tween(200)) + scaleOut(tween(250), targetScale = 1.05f))
            },
            label = "root",
        ) { onboarded ->
            if (onboarded) MainShell(vm, data) else OnboardingScreen(vm, data.profile)
        }
        Confetti(vm.confetti)
      }
    }
}

@Composable
private fun MainShell(vm: AppViewModel, data: AppData) {
    val routes = vm.routes
    BackHandler(enabled = routes.isEmpty() && vm.tab != 0) { vm.tab = 0 }
    BackHandler(enabled = routes.isNotEmpty()) { vm.pop() }

    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        vm.messages.collect { msg ->
            snackbar.currentSnackbarData?.dismiss()
            launch { snackbar.showSnackbar(msg) }
        }
    }

    Box(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = vm.tab,
                transitionSpec = {
                    (fadeIn(tween(240, 70)) + scaleIn(tween(320, 70), initialScale = 0.96f)) togetherWith fadeOut(tween(90))
                },
                label = "tabs",
            ) { tab ->
                when (tab) {
                    0 -> HomeScreen(vm, data)
                    1 -> JournalScreen(vm, data)
                    2 -> GoalsScreen(vm, data)
                    3 -> InsightsScreen(vm, data)
                    else -> ProfileScreen(vm, data)
                }
            }
            ExpressiveNavBar(navItems, vm.tab, { vm.tab = it }, Modifier.align(Alignment.BottomCenter))
        }

        // Pushed screens slide over the tabs, which keep their state underneath.
        AnimatedContent(
            targetState = routes.lastOrNull()?.let { it to routes.size },
            transitionSpec = {
                val forward = (targetState?.second ?: 0) > (initialState?.second ?: 0)
                val transform = if (forward) {
                    (slideInHorizontally(Motion.slide()) { it / 3 } + fadeIn(tween(200))) togetherWith fadeOut(tween(250, 100))
                } else {
                    fadeIn(tween(1)) togetherWith (slideOutHorizontally(Motion.slide()) { it / 3 } + fadeOut(tween(200)))
                }
                transform.targetContentZIndex = if (forward) 1f else -1f
                transform
            },
            label = "routes",
        ) { entry ->
            if (entry != null) {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
                    when (val route = entry.first) {
                        is Route.Decide -> DecideScreen(vm, data, route.draft)
                        is Route.Detail -> DetailScreen(vm, data, route.id)
                        is Route.Compare -> CompareScreen(vm, data, route.ids)
                    }
                }
            } else {
                Box(Modifier.fillMaxSize())
            }
        }

        SnackbarHost(
            snackbar,
            Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 88.dp, start = 16.dp, end = 16.dp),
        ) { d ->
            Snackbar(
                shape = RoundedCornerShape(20.dp),
                containerColor = MaterialTheme.colorScheme.inverseSurface,
                contentColor = MaterialTheme.colorScheme.inverseOnSurface,
            ) { Text(d.visuals.message) }
        }
    }
}

fun shareText(context: Context, text: String) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(send, "Share"))
}
