package io.github.hlaolil.greekparadigms

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.hlaolil.greekparadigms.ui.theme.GreekParadigmsTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Drives the real screens end to end, without touching saved progress (no store). */
@RunWith(AndroidJUnit4::class)
class AppFlowTest {
    @get:Rule
    val rule = createComposeRule()

    private fun launch(): AppState {
        val state = AppState(store = null, initial = Progress(settings = Settings(shuffle = false)))
        rule.setContent { GreekParadigmsTheme { App(state) } }
        return state
    }

    private fun openArticle(mode: String) {
        rule.onNodeWithText("Begin with ὁ, ἡ, τό").performClick()
        rule.onNodeWithText(mode).performScrollTo().performClick()
    }

    @Test
    fun flashcardSessionRunsToResultsAndRecordsProgress() {
        val state = launch()
        openArticle("Flashcards")
        val article = paradigmById.getValue("art").forms
        repeat(article.size) {
            rule.onNodeWithText("Show answer").performClick()
            rule.onNodeWithText("Got it").performClick()
        }
        rule.onNodeWithText("100%").assertExists()
        assertEquals(article.size, state.progress.cards.size)
        assertTrue(state.progress.cards.values.all { it.box == 1 })

        rule.onNodeWithText("Done").performClick()
        rule.onNodeWithText("Definite article").assertExists()
    }

    @Test
    fun giveUpOffersNoOverride() {
        launch()
        openArticle("Type it")
        rule.onNodeWithText("I don't know").performClick()
        rule.onNodeWithText("The answer is").assertExists()
        assertEquals(0, rule.onAllNodesWithText("I was right").fetchSemanticsNodes().size)
    }

    @Test
    fun wrongTypedAnswerCanBeOverruled() {
        launch()
        openArticle("Type it")
        rule.onNodeWithText("Type in Latin (Beta Code)").performClick()
        rule.onNodeWithText("lo/gos → λόγος").performTextInput("xyz")
        rule.onNodeWithText("Check").performClick()
        rule.onNodeWithText("I was right").assertExists()
    }

    @Test
    fun navigationSurvivesProcessDeath() {
        val before = AppState(null, Progress())
        val forms = paradigmById.getValue("logos").forms
        before.navigate(Screen.ParadigmDetail("logos"))
        before.startSession("λόγος", StudyMode.PARSE, forms)
        val saved = before.saveNavigation()

        val after = AppState(null, Progress())
        after.restoreNavigation(saved)
        assertEquals(before.backStack.toList(), after.backStack.toList())
    }
}
