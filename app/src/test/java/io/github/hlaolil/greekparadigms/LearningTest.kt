package io.github.hlaolil.greekparadigms

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/** Memory strength, XP, streak freezes, levels, the learning path, smart lessons and achievements. */
class LearningTest {
    private val logos = paradigmById.getValue("logos").forms
    private val article = paradigmById.getValue("art")

    @Test
    fun strengthIsNinetyPercentWhenDueAndFadesAfter() {
        val s = CardState(box = 3, due = 104, reps = 3, correct = 3, lastSeen = 100) // interval 4
        assertEquals(1.0, s.strength(100), 1e-9)
        assertEquals(0.9, s.strength(104), 1e-9)
        assertTrue(s.strength(120) < CardState.FADING)
        assertEquals(0.0, CardState().strength(100), 1e-9)
        assertEquals(0.5, CardState(box = 0, due = 10, reps = 1, lastSeen = 10).strength(10), 1e-9)
    }

    @Test
    fun fadingFormsAreWeakestFirst() {
        val p = Progress(cards = mapOf(
            logos[0].id to CardState(1, 11, 1, 1, 10),
            logos[1].id to CardState(2, 12, 2, 2, 10),
            logos[2].id to CardState(5, 26, 5, 5, 10),
        ))
        val fading = p.fadingForms(30)
        assertEquals(listOf(logos[0].id, logos[1].id), fading.map { it.id })
    }

    @Test
    fun xpAccumulatesAndTheDailyCounterResets() {
        var p = Progress().record(logos[0].id, true, 1, 10).record(logos[1].id, true, 1, 15)
        assertEquals(25L, p.xp)
        assertEquals(25, p.xpOn(1))
        p = p.record(logos[2].id, true, 2, 5)
        assertEquals(30L, p.xp)
        assertEquals(5, p.xpOn(2))
        assertEquals(0, p.xpOn(3))
    }

    @Test
    fun aWeekOfStudyEarnsAFreezeThatSavesAMissedDay() {
        var p = Progress()
        (1L..7L).forEach { p = p.record(logos[0].id, true, it) }
        assertEquals(7, p.streak(7))
        assertEquals(1, p.freezes)
        // Day 8 missed. On day 9 the streak still stands, because the freeze will cover day 8.
        assertEquals(7, p.streak(9))
        p = p.record(logos[0].id, true, 9)
        assertEquals(0, p.freezes)
        assertEquals(setOf(8L), p.frozenDays)
        assertEquals(8, p.streak(9)) // the frozen day keeps the streak but doesn't add to it
        // Two missed days with no freezes left break it.
        assertEquals(0, p.streak(12))
    }

    @Test
    fun freezesAreCapped() {
        var p = Progress()
        (1L..28L).forEach { p = p.record(logos[0].id, true, it) }
        assertEquals(Progress.MAX_FREEZES, p.freezes)
        assertEquals(28, p.bestStreak())
    }

    @Test
    fun levelsClimbWithTheWeakestForm() {
        fun allAt(box: Int) = Progress(cards = article.forms.associate { it.id to CardState(box, 0, box + 1, box, 0) })
        assertEquals(0, Progress().level(article))
        assertEquals(1, allAt(0).level(article))
        assertEquals(2, allAt(2).level(article))
        assertEquals(3, allAt(3).level(article))
        assertEquals(4, allAt(4).level(article))
        assertEquals(5, allAt(6).level(article))
        val oneUnseen = Progress(cards = article.forms.drop(1).associate { it.id to CardState(6, 0, 6, 6, 0) })
        assertEquals(0, oneUnseen.level(article))
    }

    @Test
    fun thePathStartsWithTheArticleAndMovesOn() {
        assertEquals("art", Progress().nextOnPath()!!.id)
        val done = Progress(cards = article.forms.associate { it.id to CardState(1, 0, 1, 1, 0) })
        assertEquals(paradigms[1].id, done.nextOnPath()!!.id)
    }

    @Test
    fun sessionsPayXpByDifficultyWithCombos() {
        var s = Session.start("t", StudyMode.TYPE, logos.take(6), shuffle = false)
        repeat(5) { s = s.answer(true, StudyMode.TYPE) }
        assertEquals(5 * 15 + Session.COMBO_BONUS, s.xp)
        assertEquals(5, s.combo)
        s = s.answer(false, StudyMode.TYPE)
        assertEquals(0, s.combo)
        assertEquals(5, s.bestCombo)
        // The missed form comes back; getting it right then pays only the retry XP.
        val before = s.xp
        while (!s.finished) s = s.answer(true, StudyMode.TYPE)
        assertEquals(before + Session.RETRY_XP, s.xp)
        assertFalse(s.perfect)
        assertEquals(10, s.completionBonus)
    }

    @Test
    fun aFlawlessSessionEarnsTheBiggerBonus() {
        var s = Session.start("t", StudyMode.CHOOSE, logos.take(3), shuffle = false)
        while (!s.finished) s = s.answer(true)
        assertTrue(s.perfect)
        assertEquals(20, s.completionBonus)
        assertEquals(s, Session.deserialize(s.serialize()))
    }

    @Test
    fun lessonsAreCountedOnce() {
        val p = Progress().completeLesson(perfect = true, bonusXp = 20, today = 3)
        assertEquals(1, p.lessons)
        assertEquals(1, p.perfectLessons)
        assertEquals(20L, p.xp)
    }

    @Test
    fun smartLessonsAskByHowWellAFormIsKnown() {
        val s = Session.start("t", StudyMode.SMART, logos.take(3), shuffle = false)
        fun asked(box: Int?, hasExample: Boolean = true) =
            s.cardMode(box?.let { CardState(it, 0, it + 1, it, 0) }, hasExample)
        assertEquals(StudyMode.FLASHCARD, asked(null))
        assertEquals(StudyMode.CHOOSE, asked(0))
        assertEquals(StudyMode.CHOOSE, asked(1))
        assertEquals(StudyMode.CONTEXT, asked(2))
        assertEquals(StudyMode.PARSE, asked(2, hasExample = false))
        assertEquals(StudyMode.TYPE, asked(3))
        // A form missed earlier in the session comes back as multiple choice.
        val retry = s.answer(false).let { var x = it; while (x.current?.id != logos[0].id) x = x.answer(true); x }
        assertEquals(StudyMode.CHOOSE, retry.cardMode(CardState(3, 0, 4, 3, 0), true))
        // Fixed modes stay fixed; "in context" falls back to parsing without an example.
        val ctx = Session.start("t", StudyMode.CONTEXT, logos.take(1), shuffle = false)
        assertEquals(StudyMode.PARSE, ctx.cardMode(null, hasExample = false))
    }

    @Test
    fun contextParsesMixTensesButKeepOneRightAnswer() {
        val random = Random(1)
        allForms.forEach { f ->
            val options = Choices.contextParses(f, random)
            assertEquals(f.id, 1, options.count { it.id == f.id })
            assertTrue(f.id, options.none { it.id != f.id && GreekText.sameForm(it.answers, f.answers) })
            assertEquals(f.id, options.size, options.map { Choices.label(it, true) }.distinct().size)
        }
        val aorPass = paradigmById.getValue("aor_pass").forms.first { it.parse == "3rd person plural" }
        val labels = Choices.contextParses(aorPass, Random(2)).map { Choices.label(it, true) }
        assertTrue(labels.toString(), labels.any { !it.startsWith("Aorist passive indicative") })
    }

    @Test
    fun achievementsUnlockFromProgress() {
        assertTrue(Achievements.unlocked(Progress()).isEmpty())
        val mastered = Progress(cards = article.forms.associate { it.id to CardState(4, 0, 4, 4, 0) })
        assertTrue("article" in Achievements.unlocked(mastered))
        assertTrue("first" in Achievements.unlocked(mastered))
        assertFalse("m100" in Achievements.unlocked(mastered))
        assertEquals(Achievements.all.size, Achievements.all.map { it.id }.distinct().size)
    }

    @Test
    fun newFieldsSurviveSaving() {
        var p = Progress()
        (1L..8L).forEach { p = p.record(logos[0].id, true, it, 10) }
        p = p.record(logos[1].id, false, 10, 0).completeLesson(true, 20, 10)
        val back = Progress.deserialize(p.serialize())
        assertEquals(p, back)
        assertEquals(0L, Progress.deserialize("v2\nxp nonsense\n").xp)
    }
}
