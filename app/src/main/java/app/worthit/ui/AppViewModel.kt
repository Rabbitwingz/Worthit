package app.worthit.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import app.worthit.data.AppData
import app.worthit.data.Category
import app.worthit.data.Frequency
import app.worthit.data.Goal
import app.worthit.data.Lifespan
import app.worthit.data.Profile
import app.worthit.data.Purchase
import app.worthit.data.PurchaseType
import app.worthit.data.Review
import app.worthit.data.SampleData
import app.worthit.data.Status
import app.worthit.data.Store
import app.worthit.data.Subscription
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.util.UUID

/** Everything the Decide screen needs to start from. */
data class Draft(
    val name: String,
    val price: Double,
    val emoji: String,
    val category: Category,
    val type: PurchaseType = PurchaseType.Want,
    val frequency: Frequency? = null,
    val lifespan: Lifespan? = null,
    val desire: Int = -1,
    val existingId: String? = null,
)

sealed interface Route {
    data class Decide(val draft: Draft) : Route
    data class Detail(val id: String) : Route
    data class Compare(val ids: List<String>) : Route
}

fun newId(): String = UUID.randomUUID().toString()

class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val store = Store(File(app.filesDir, "worthit.json"), ioScope)
    val data: StateFlow<AppData> = store.data

    val routes = mutableStateListOf<Route>()
    var tab by mutableIntStateOf(0)
    var confetti by mutableIntStateOf(0)
        private set

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val messages: SharedFlow<String> = _messages

    fun push(route: Route) { routes.add(route) }
    fun pop() { if (routes.isNotEmpty()) routes.removeAt(routes.lastIndex) }
    fun popAll() { routes.clear() }

    fun toast(message: String) { _messages.tryEmit(message) }
    fun celebrate() { confetti++ }

    fun exportJson(): String = store.export()

    // Profile
    fun updateProfile(transform: (Profile) -> Profile) = store.update { it.copy(profile = transform(it.profile)) }

    fun finishOnboarding(profile: Profile) {
        store.update { it.copy(profile = profile.copy(onboarded = true)) }
        celebrate()
    }

    // Purchases
    fun upsert(p: Purchase) = store.update { d ->
        val exists = d.purchases.any { it.id == p.id }
        d.copy(purchases = if (exists) d.purchases.map { if (it.id == p.id) p else it } else d.purchases + p)
    }

    fun updatePurchase(id: String, transform: (Purchase) -> Purchase) = store.update { d ->
        d.copy(purchases = d.purchases.map { if (it.id == id) transform(it) else it })
    }

    fun deletePurchase(id: String) = store.update { d ->
        d.copy(
            purchases = d.purchases.filterNot { it.id == id },
            goals = d.goals.map { if (it.purchaseId == id) it.copy(purchaseId = null) else it },
        )
    }

    fun addReview(id: String, review: Review) = updatePurchase(id) { it.copy(reviews = it.reviews + review) }

    fun setStatus(id: String, status: Status, boughtAt: Long? = null) = updatePurchase(id) {
        it.copy(
            status = status,
            boughtAt = if (status == Status.Bought) (boughtAt ?: System.currentTimeMillis()) else it.boughtAt,
            coolOffUntil = if (status == Status.Wishlist) it.coolOffUntil else null,
        )
    }

    // Goals
    fun addGoal(goal: Goal) = store.update { it.copy(goals = it.goals + goal) }

    fun deleteGoal(id: String) = store.update { d -> d.copy(goals = d.goals.filterNot { it.id == id }) }

    fun contribute(id: String, amount: Double) {
        val before = data.value.goals.firstOrNull { it.id == id } ?: return
        store.update { d -> d.copy(goals = d.goals.map { if (it.id == id) it.copy(saved = (it.saved + amount).coerceAtLeast(0.0)) else it }) }
        if (!before.reached && before.saved + amount >= before.target) {
            celebrate()
            toast("Goal reached! ${before.emoji} You did it.")
        }
    }

    fun completeGoal(id: String) {
        val goal = data.value.goals.firstOrNull { it.id == id } ?: return
        val now = System.currentTimeMillis()
        store.update { d ->
            d.copy(
                goals = d.goals.map { if (it.id == id) it.copy(completedAt = now) else it },
                purchases = d.purchases.map {
                    if (it.id == goal.purchaseId) it.copy(status = Status.Bought, boughtAt = now) else it
                },
            )
        }
        celebrate()
    }

    // Subscriptions
    fun addSubscription(s: Subscription) = store.update { it.copy(subscriptions = it.subscriptions + s) }

    fun deleteSubscription(id: String) = store.update { d -> d.copy(subscriptions = d.subscriptions.filterNot { it.id == id }) }

    // Data
    fun loadSample() {
        val sample = SampleData.build(data.value.profile, System.currentTimeMillis())
        store.update { d ->
            d.copy(
                purchases = d.purchases + sample.purchases,
                goals = d.goals + sample.goals,
                subscriptions = d.subscriptions + sample.subscriptions,
            )
        }
        toast("Added a year of sample purchases. Explore away!")
    }

    fun deleteEverything() {
        routes.clear()
        tab = 0
        store.update { AppData() }
    }
}
