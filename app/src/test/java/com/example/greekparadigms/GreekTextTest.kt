package com.example.greekparadigms

import com.example.greekparadigms.GreekText.Verdict
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GreekTextTest {

    @Test
    fun exactAnswerIsCorrect() {
        assertEquals(Verdict.CORRECT, GreekText.check("λόγου", listOf("λόγου"), requireAccents = true))
    }

    @Test
    fun missingAccentIsLenientOnlyWhenAllowed() {
        assertEquals(Verdict.CORRECT_IGNORING_ACCENTS, GreekText.check("λογου", listOf("λόγου"), requireAccents = false))
        assertEquals(Verdict.WRONG, GreekText.check("λογου", listOf("λόγου"), requireAccents = true))
    }

    @Test
    fun wrongLettersAreWrong() {
        assertEquals(Verdict.WRONG, GreekText.check("λόγῳ", listOf("λόγου"), requireAccents = false))
        assertEquals(Verdict.WRONG, GreekText.check("  ", listOf("λόγου"), requireAccents = false))
    }

    @Test
    fun movableNuIsOptional() {
        assertEquals(Verdict.CORRECT, GreekText.check("λύουσι", listOf("λύουσι(ν)"), true))
        assertEquals(Verdict.CORRECT, GreekText.check("λύουσιν", listOf("λύουσι(ν)"), true))
        assertEquals(listOf("ἐστί", "ἐστίν"), GreekText.expand("ἐστί(ν)"))
    }

    @Test
    fun alternativesAreAccepted() {
        assertEquals(Verdict.CORRECT, GreekText.check("μου", listOf("ἐμοῦ", "μου"), true))
        assertEquals(Verdict.CORRECT, GreekText.check("ἐμοῦ", listOf("ἐμοῦ", "μου"), true))
    }

    @Test
    fun finalSigmaAndGraveAreNormalised() {
        assertEquals(Verdict.CORRECT, GreekText.check("λογοσ", listOf("λόγος"), false).let {
            if (it == Verdict.CORRECT_IGNORING_ACCENTS) Verdict.CORRECT else it
        })
        assertEquals(Verdict.CORRECT, GreekText.check("λόγοσ", listOf("λόγος"), true))
        assertEquals(Verdict.CORRECT, GreekText.check("τὸν", listOf("τόν"), true))
        assertEquals("σάρξ λόγος", GreekText.finalSigma("σάρξ λόγοσ"))
    }

    @Test
    fun oxiaAndTonosCompareEqual() {
        // U+1F79 (omicron with oxia) vs U+03CC (omicron with tonos)
        assertEquals(Verdict.CORRECT, GreekText.check("λόγος", listOf("λόγος"), true))
    }

    @Test
    fun keyboardComposesDiacriticsInAnyOrder() {
        val a = GreekText.applyMark(GreekText.applyMark("α", GreekText.SMOOTH), GreekText.ACUTE)
        val b = GreekText.applyMark(GreekText.applyMark("α", GreekText.ACUTE), GreekText.SMOOTH)
        assertEquals("ἄ", a)
        assertEquals("ἄ", b)
        assertEquals(1, a.length) // precomposed
    }

    @Test
    fun keyboardReplacesAndTogglesMarks() {
        val rough = GreekText.applyMark("ἀ", GreekText.ROUGH)
        assertEquals("ἁ", rough)
        val circ = GreekText.applyMark("ά", GreekText.CIRCUMFLEX)
        assertEquals("ᾶ", circ)
        assertEquals("α", GreekText.applyMark("ά", GreekText.ACUTE))
        assertEquals("ᾧ", GreekText.applyMark(GreekText.applyMark(GreekText.applyMark("ω", GreekText.IOTA_SUB), GreekText.CIRCUMFLEX), GreekText.ROUGH))
    }

    @Test
    fun backspaceRemovesWholeLetter() {
        assertEquals("λόγ", GreekText.backspace("λόγῳ"))
        assertEquals("", GreekText.backspace("ᾧ"))
        assertEquals("", GreekText.backspace(""))
    }

    @Test
    fun betaCodeConverts() {
        assertEquals("λόγος", GreekText.fromBetaCode("lo/gos"))
        assertEquals("ἀρχή", GreekText.fromBetaCode("a)rxh/"))
        assertEquals("ἡμέρᾳ", GreekText.fromBetaCode("h(me/ra|"))
        assertEquals("πνεῦμα", GreekText.fromBetaCode("pneu=ma"))
        // Already-Greek text passes through, so converting on every keystroke is safe.
        assertEquals("λόγος", GreekText.fromBetaCode("λόγος"))
    }

    @Test
    fun sameFormIgnoresMovableNu() {
        assertTrue(GreekText.sameForm(listOf("πᾶσι(ν)"), listOf("πᾶσιν")))
        assertFalse(GreekText.sameForm(listOf("τοῦ"), listOf("τῷ")))
    }

    @Test
    fun transliterationSupportsSearch() {
        assertEquals("logos", GreekText.transliterate("λόγος"))
        assertEquals("pneuma", GreekText.transliterate("πνεῦμα"))
    }
}
