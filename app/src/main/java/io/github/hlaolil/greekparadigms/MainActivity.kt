package io.github.hlaolil.greekparadigms

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import androidx.compose.ui.Modifier
import io.github.hlaolil.greekparadigms.ui.theme.GreekParadigmsTheme

class MainActivity : ComponentActivity() {
    // Held by the activity, which handles configuration changes itself (see the manifest),
    // so a rotation doesn't lose a session in progress. If Android kills the process in the
    // background, the back stack (including any session) is restored from the saved state.
    private lateinit var state: AppState

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val store = ProgressStore(applicationContext)
        val corpus = runCatching {
            NtCorpus.parse(assets.open("nt_examples.tsv").bufferedReader().use { it.readText() })
        }.getOrDefault(NtCorpus.EMPTY)
        state = AppState(store, store.load(), corpus = corpus)
        savedInstanceState?.getString(NAV_KEY)?.let { state.restoreNavigation(it) }
        setContent {
            // Roll the day over at midnight even if the app stays open.
            LaunchedEffect(Unit) {
                while (true) {
                    delay(60_000)
                    state.refreshDay()
                }
            }
            GreekParadigmsTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(Modifier.padding(innerPadding)) {
                        App(state)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        state.refreshDay()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(NAV_KEY, state.saveNavigation())
    }

    private companion object {
        const val NAV_KEY = "navigation"
    }
}

/** Routes the current screen and wires each one to [AppState]. */
@Composable
fun App(state: AppState) {
    BackHandler(enabled = state.backStack.size > 1) { state.back() }
    val progress = state.progress
    val today = state.today

    CompositionLocalProvider(LocalNt provides state.corpus) {
        when (val screen = state.screen) {
            Screen.Home -> HomeScreen(
                progress = progress,
                today = today,
                onOpen = { state.navigate(Screen.ParadigmDetail(it.id)) },
                onReview = { state.startSession("Daily review", StudyMode.SMART, it) },
                onLearn = { p -> state.startSession(p.lemma, StudyMode.SMART, p.forms.filter { progress.state(it.id)?.seen != true }) },
                onStrengthen = { state.startSession("Strengthen", StudyMode.SMART, it) },
                onStats = { state.navigate(Screen.Stats) },
                onSettings = { state.navigate(Screen.Settings) },
            )

            is Screen.ParadigmDetail -> ParadigmScreen(
                paradigm = paradigmById.getValue(screen.paradigmId),
                progress = progress,
                today = today,
                onBack = { state.back() },
                onStudy = { mode, forms -> state.startSession(paradigmById.getValue(screen.paradigmId).lemma, mode, forms) },
            )

            is Screen.Study -> {
                val now = Achievements.unlocked(progress)
                StudyScreen(
                    session = screen.session,
                    progress = progress,
                    today = today,
                    newAchievements = Achievements.all.filter { it.id in now && it.id !in state.achievementsAtStart },
                    onAnswer = { correct, asked -> state.answer(screen.session, correct, asked) },
                    onFinish = { state.finishSession(screen.session) },
                    onRestart = { forms, mode -> state.restartSession(screen.session.title, mode, forms) },
                    onExit = { state.back() },
                )
            }

            Screen.Stats -> StatsScreen(
                progress, today,
                onBack = { state.back() },
                onStrengthen = { state.startSession("Strengthen", StudyMode.SMART, it) },
            )

            Screen.Settings -> SettingsScreen(
                settings = progress.settings,
                onChange = { state.updateSettings(it) },
                onReset = { state.resetProgress() },
                onBack = { state.back() },
            )
        }
    }
}
