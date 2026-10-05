package app.worthit.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import java.io.File

/** Local-first storage: one JSON file in the app's private directory. Nothing leaves the phone. */
class Store(private val file: File, private val scope: CoroutineScope) {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        coerceInputValues = true
    }
    private val pretty = Json(json) { prettyPrint = true }
    private val mutex = Mutex()
    private val _data = MutableStateFlow(load())
    val data: StateFlow<AppData> = _data.asStateFlow()

    private fun load(): AppData = runCatching {
        if (file.exists()) json.decodeFromString(AppData.serializer(), file.readText()) else AppData()
    }.getOrDefault(AppData())

    fun update(transform: (AppData) -> AppData) {
        _data.update(transform)
        scope.launch(Dispatchers.IO) {
            mutex.withLock {
                val snapshot = _data.value
                val tmp = File(file.parentFile, file.name + ".tmp")
                tmp.writeText(json.encodeToString(AppData.serializer(), snapshot))
                if (!tmp.renameTo(file)) {
                    file.writeText(tmp.readText())
                    tmp.delete()
                }
            }
        }
    }

    fun export(): String = pretty.encodeToString(AppData.serializer(), _data.value)
}
