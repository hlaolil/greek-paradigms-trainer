package io.github.hlaolil.greekparadigms

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
class AppState(
    private val store: ProgressStore?,
    initial: Progress,
    private val clock: () -> Long = ::today,
) {
    var progress by mutableStateOf(initial)
        private set

    /** The current local day. Call [refreshDay] when it may have changed (on resume, periodically). */
    var today by mutableStateOf(clock())
        private set

    fun refreshDay() {
        val now = clock()
        if (now != today) today = now
    }

    val backStack = mutableStateListOf<Screen>(Screen.Home)
    val screen: Screen get() = backStack.last()

    /** The back stack as text, for saving in the activity's instance state. */
    fun saveNavigation(): String = backStack.joinToString("\n") {
        when (it) {
            Screen.Home -> "home"
            is Screen.ParadigmDetail -> "paradigm\t${it.paradigmId}"
            is Screen.Study -> "study\t${it.session.serialize()}"
            Screen.Stats -> "stats"
            Screen.Settings -> "settings"
        }
    }

    /** Restores a back stack saved by [saveNavigation], skipping anything that no longer makes sense. */
    fun restoreNavigation(saved: String) {
        val screens = saved.lines().mapNotNull { line ->
            val rest = line.substringAfter('\t', "")
            when (line.substringBefore('\t')) {
                "home" -> Screen.Home
                "paradigm" -> rest.takeIf { it in paradigmById }?.let { Screen.ParadigmDetail(it) }
                "study" -> Session.deserialize(rest)?.let { Screen.Study(it) }
                "stats" -> Screen.Stats
                "settings" -> Screen.Settings
                else -> null
            }
        }
        if (screens.firstOrNull() == Screen.Home) {
            backStack.clear()
            backStack.addAll(screens)
        }
    }

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

    fun record(form: Form, correct: Boolean) {
        refreshDay()
        update(progress.record(form.id, correct, today))
    }

    fun updateSettings(settings: Settings) = update(progress.copy(settings = settings))

    fun resetProgress() = update(Progress(settings = progress.settings))

    private fun update(p: Progress) {
        progress = p
        store?.save(p)
    }
}
