package com.example.greekparadigms

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.TimeZone

/** Persists [Progress] in SharedPreferences. */
class ProgressStore(context: Context) {
    private val prefs = context.getSharedPreferences("progress", Context.MODE_PRIVATE)

    fun load(): Progress = Progress.deserialize(prefs.getString(KEY, null))

    fun save(progress: Progress) {
        prefs.edit().putString(KEY, progress.serialize()).apply()
    }

    private companion object {
        const val KEY = "progress.v1"
    }
}

/** Today's date as a local epoch day (java.time needs API 26; minSdk is 24). */
fun today(): Long {
    val now = System.currentTimeMillis()
    return (now + TimeZone.getDefault().getOffset(now)) / 86_400_000L
}

sealed interface Screen {
    data object Home : Screen
    data class ParadigmDetail(val paradigmId: String) : Screen
    data class Study(val session: Session) : Screen
    data object Stats : Screen
    data object Settings : Screen
}

/**
 * The app's single source of truth: saved progress and a back stack of screens.
 * Screens receive data and callbacks only; they never touch storage directly.
 */
class AppState(private val store: ProgressStore?, initial: Progress) {
    var progress by mutableStateOf(initial)
        private set

    val backStack = mutableStateListOf<Screen>(Screen.Home)
    val screen: Screen get() = backStack.last()

    fun navigate(to: Screen) { backStack.add(to) }

    fun back(): Boolean {
        if (backStack.size <= 1) return false
        backStack.removeAt(backStack.lastIndex)
        return true
    }

    fun replaceTop(screen: Screen) { backStack[backStack.lastIndex] = screen }

    fun startSession(title: String, mode: StudyMode, forms: List<Form>) {
        if (forms.isEmpty()) return
        navigate(Screen.Study(Session.start(title, mode, forms, progress.settings.shuffle)))
    }

    fun record(form: Form, correct: Boolean) = update(progress.record(form.id, correct, today()))

    fun updateSettings(settings: Settings) = update(progress.copy(settings = settings))

    fun resetProgress() = update(Progress(settings = progress.settings))

    private fun update(p: Progress) {
        progress = p
        store?.save(p)
    }
}
