package io.github.hlaolil.greekparadigms

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressTest {
    private val logos = paradigmById.getValue("logos").forms

    @Test
    fun correctAnswersClimbBoxesAndDoubleIntervals() {
        var s: CardState? = null
        val dues = mutableListOf<Long>()
        repeat(5) { s = Scheduler.grade(s, true, 100); dues += s!!.due - 100 }
        assertEquals(listOf(1L, 2L, 4L, 8L, 16L), dues)
        assertTrue(s!!.mastered)
    }

    @Test
    fun missResetsToToday() {
        var s = Scheduler.grade(null, true, 10)
        s = Scheduler.grade(s, true, 11)
        s = Scheduler.grade(s, false, 13)
        assertEquals(0, s.box)
        assertEquals(13, s.due)
        assertEquals(3, s.reps)
        assertEquals(2, s.correct)
    }

    @Test
    fun streakCountsConsecutiveDays() {
        val p = Progress(studyDays = setOf(5, 6, 7, 9, 10))
        assertEquals(2, p.streak(10))
        assertEquals(2, p.streak(11)) // not studied yet today: yesterday's streak still stands
        assertEquals(0, p.streak(12))
    }

    @Test
    fun dailyCounterResetsOnANewDay() {
        var p = Progress()
        p = p.record(logos[0].id, true, 1).record(logos[1].id, true, 1)
        assertEquals(2, p.answeredOn(1))
        assertEquals(0, p.answeredOn(2))
        p = p.record(logos[2].id, false, 2)
        assertEquals(1, p.answeredOn(2))
    }

    @Test
    fun reviewQueueHasDueThenNewFromStartedParadigms() {
        val p = Progress().record(logos[0].id, false, 1).record(logos[1].id, true, 1)
        val q = p.reviewQueue(1)
        assertEquals(logos[0].id, q.first().id) // missed today, so due today
        assertTrue(q.drop(1).all { it.paradigmId == "logos" && p.state(it.id) == null })
        assertFalse(q.any { it.id == logos[1].id }) // not due until tomorrow
    }

    @Test
    fun serialisationRoundTrips() {
        val p = Progress(settings = Settings(requireAccents = true, shuffle = false, dailyGoal = 80, newPerReview = 5))
            .record(logos[0].id, true, 20).record(logos[3].id, false, 21)
        val back = Progress.deserialize(p.serialize())
        assertEquals(p, back)
    }

    @Test
    fun corruptOrUnknownLinesAreIgnored() {
        val p = Progress.deserialize("v1\ncard nope 1 2 3 4 5\ncard ${logos[0].id} x\ngarbage\n")
        assertTrue(p.cards.isEmpty())
        assertEquals(Progress(), Progress.deserialize(null))
    }

    @Test
    fun formIdsNameTheirSlot() {
        assertEquals("logos:genitive-singular", logos.first { it.row == 1 && it.col == 0 }.id)
        assertEquals("tis:masc-fem-nominative-singular", paradigmById.getValue("tis").forms[0].id)
    }

    @Test
    fun positionalIdsFromVersion1AreMigrated() {
        val p = Progress.deserialize("v1\ncard logos.1.0 3 50 4 3 46\n")
        assertEquals(CardState(3, 50, 4, 3, 46), p.state("logos:genitive-singular"))
        assertEquals(1, p.cards.size)
    }

    @Test
    fun sessionsSurviveSerialisation() {
        val s = Session.start("ὁ, ἡ, τό", StudyMode.TYPE, logos.take(5), shuffle = true, seed = 7)
            .answer(false).answer(true)
        assertEquals(s, Session.deserialize(s.serialize()))
        assertEquals(null, Session.deserialize("garbage"))
    }

    @Test
    fun missedCardsComeBackLaterInTheSession() {
        var s = Session.start("t", StudyMode.FLASHCARD, logos.take(5), shuffle = false)
        val first = s.current!!.id
        s = s.answer(false)
        assertEquals(6, s.queue.size)
        assertEquals(first, s.queue[1 + Session.REINSERT_GAP])
        // Work through the rest, getting everything right.
        while (!s.finished) s = s.answer(true)
        assertEquals(5, s.total)
        assertEquals(4, s.correct) // the first miss still counts against the score
        assertEquals(listOf(first), s.missed.map { it.id })
    }

    @Test
    fun retriesAreCapped() {
        var s = Session.start("t", StudyMode.FLASHCARD, logos.take(1), shuffle = false)
        repeat(10) { if (!s.finished) s = s.answer(false) }
        assertTrue(s.finished)
        assertEquals(1 + Session.MAX_RETRIES, s.queue.size)
    }

    @Test
    fun choicesContainExactlyOneCorrectOption() {
        val random = Random(42)
        paradigms.flatMap { it.forms }.forEach { f ->
            val forms = Choices.forms(f, random)
            assertEquals(f.id, 1, forms.count { it == f.display })
            assertEquals(forms.size, forms.distinct().size)
            assertTrue(f.id, forms.size >= 2)

            val parses = Choices.parses(f, random)
            assertEquals(1, parses.count { GreekText.sameForm(it.answers, f.answers) })
            assertTrue(parses.any { it.id == f.id })
        }
    }

    @Test
    fun searchFindsByGreekLatinAndGrammar() {
        assertTrue(search("logos").any { it.id == "logos" })
        assertTrue(search("λογος").any { it.id == "logos" })
        assertTrue(search("aorist").any { it.id == "aor_act" })
        assertTrue(search("ἐλύθην").any { it.id == "aor_pass" })
        assertEquals(paradigms, search(""))
    }
}
