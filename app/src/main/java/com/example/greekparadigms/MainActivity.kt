package com.example.greekparadigms

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
import androidx.compose.ui.Modifier
import com.example.greekparadigms.ui.theme.GreekParadigmsTheme

class MainActivity : ComponentActivity() {
    // Held by the activity, which handles configuration changes itself (see the manifest),
    // so a rotation doesn't lose a session in progress.
    private lateinit var state: AppState

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val store = ProgressStore(applicationContext)
        state = AppState(store, store.load())
        setContent {
            GreekParadigmsTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(Modifier.padding(innerPadding)) {
                        App(state)
                    }
                }
            }
        }
    }
}

/** Routes the current screen and wires each one to [AppState]. */
@Composable
fun App(state: AppState) {
    BackHandler(enabled = state.backStack.size > 1) { state.back() }
    val progress = state.progress
    val today = today()

    when (val screen = state.screen) {
        Screen.Home -> HomeScreen(
            progress = progress,
            today = today,
            onOpen = { state.navigate(Screen.ParadigmDetail(it.id)) },
            onReview = { state.startSession("Daily review", StudyMode.FLASHCARD, it) },
            onStats = { state.navigate(Screen.Stats) },
            onSettings = { state.navigate(Screen.Settings) },
        )

        is Screen.ParadigmDetail -> ParadigmScreen(
            paradigm = paradigmById.getValue(screen.paradigmId),
            progress = progress,
            onBack = { state.back() },
            onStudy = { mode, forms -> state.startSession(paradigmById.getValue(screen.paradigmId).lemma, mode, forms) },
        )

        is Screen.Study -> StudyScreen(
            session = screen.session,
            settings = progress.settings,
            onUpdate = { state.replaceTop(Screen.Study(it)) },
            onRecord = { form, correct -> state.record(form, correct) },
            onRestart = { forms, mode ->
                state.replaceTop(Screen.Study(Session.start(screen.session.title, mode, forms, progress.settings.shuffle)))
            },
            onExit = { state.back() },
        )

        Screen.Stats -> StatsScreen(progress, today, onBack = { state.back() })

        Screen.Settings -> SettingsScreen(
            settings = progress.settings,
            onChange = { state.updateSettings(it) },
            onReset = { state.resetProgress() },
            onBack = { state.back() },
        )
    }
}
