package io.github.hlaolil.greekparadigms

import java.text.Normalizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ParadigmDataTest {

    @Test
    fun idsAreUnique() {
        assertEquals(paradigms.size, paradigms.map { it.id }.toSet().size)
        val forms = paradigms.flatMap { it.forms }
        assertEquals(forms.size, forms.map { it.id }.toSet().size)
    }

    @Test
    fun everyFormIsWellFormedPolytonicGreek() {
        val allowed = Regex("^[\\p{InGreek}\\p{InGreekExtended}()]+$")
        paradigms.flatMap { it.forms }.forEach { f ->
            f.answers.forEach { a ->
                assertTrue("${f.id}: '$a' is not Greek", allowed.matches(a))
                assertEquals("${f.id}: '$a' is not NFC", Normalizer.normalize(a, Normalizer.Form.NFC), a)
                assertEquals("${f.id}: '$a' final sigma", GreekText.finalSigma(a), a)
            }
        }
    }

    @Test
    fun everyWordCarriesAnAccentOrIsAProclitic() {
        // Proclitics have no accent; the enclitic personal pronouns are listed as unaccented alternatives.
        val unaccented = setOf("ὁ", "ἡ", "οἱ", "αἱ", "μου", "μοι", "με", "σου", "σοι", "σε")
        paradigms.flatMap { it.forms }.flatMap { it.answers }.forEach { a ->
            val d = Normalizer.normalize(a, Normalizer.Form.NFD)
            val accented = d.any { it == '́' || it == '͂' || it == '̀' }
            assertTrue("'$a' has no accent", accented || a in unaccented)
        }
    }

    @Test
    fun cellsAreInsideTheirTables() {
        paradigms.forEach { p ->
            assertTrue(p.forms.isNotEmpty())
            p.forms.forEach { f ->
                assertTrue(f.row in p.rowLabels.indices && f.col in p.colLabels.indices)
                assertEquals(p.id, f.paradigmId)
            }
        }
    }

    @Test
    fun spotCheckKnownForms() {
        assertEquals("τοῦ", paradigmById.getValue("art").cell(1, 0)!!.display)
        assertEquals("masculine genitive singular", paradigmById.getValue("art").cell(1, 0)!!.parse)
        assertEquals("λόγε", paradigmById.getValue("logos").cell(4, 0)!!.display)
        assertEquals("ἐλύθησαν", paradigmById.getValue("aor_pass").cell(2, 1)!!.display)
        assertEquals("3rd person plural", paradigmById.getValue("aor_pass").cell(2, 1)!!.parse)
        assertEquals("λῦσαι", paradigmById.getValue("inf").cell(1, 0)!!.display)
        assertEquals(null, paradigmById.getValue("heis").cell(4, 0))
    }

    @Test
    fun libraryIsSubstantial() {
        assertTrue(paradigms.size >= 50)
        assertTrue(paradigms.sumOf { it.forms.size } >= 600)
        Category.entries.forEach { c -> assertTrue(c.title, paradigms.any { it.category == c }) }
    }
}
