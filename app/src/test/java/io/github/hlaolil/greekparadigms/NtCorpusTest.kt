package io.github.hlaolil.greekparadigms

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Checks the generated NT data against the paradigm tables it was built for. */
class NtCorpusTest {
    private val file: File = listOfNotNull(
        System.getenv("NT_EXAMPLES"),
        "src/main/assets/nt_examples.tsv",
        "app/src/main/assets/nt_examples.tsv",
    ).map(::File).first { it.exists() }

    private val text = file.readText()
    private val corpus = NtCorpus.parse(text)

    /** The letters of a word, without punctuation, accents or breathings. */
    private fun letters(s: String) = GreekText.bare(s).filter { it.isLetter() }

    @Test
    fun everyEntryIsAKnownForm() {
        text.lines().filter { it.startsWith("F\t") || it.startsWith("E\t") }.forEach { line ->
            val id = line.split("\t")[1]
            assertTrue("unknown form $id", id in formById)
        }
        assertEquals(allForms.size, text.lines().count { it.startsWith("F\t") })
    }

    @Test
    fun exactExamplesShowTheFormItself() {
        allForms.forEach { f ->
            corpus.examples(f.id).filter { it.exact }.forEach { ex ->
                val spellings = f.answers.flatMap { GreekText.expand(it) }.map(::letters)
                assertTrue("${f.id}: ${ex.word} (${ex.reference}) is not ${f.display}", letters(ex.word) in spellings)
            }
        }
    }

    @Test
    fun patternExamplesShareTheEnding() {
        allForms.forEach { f ->
            corpus.examples(f.id).filter { !it.exact }.forEach { ex ->
                val endings = f.answers.flatMap { GreekText.expand(it) }.map { letters(it).substringAfter("λυ") }
                assertTrue("${f.id}: ${ex.word} doesn't end like ${f.display}", endings.any { letters(ex.word).endsWith(it) })
                // Pattern examples are only used for the model verb λύω.
                assertTrue(f.id, letters(paradigmById.getValue(f.paradigmId).lemma).startsWith("λυ"))
            }
        }
    }

    @Test
    fun knownPassagesAndCounts() {
        val ho = corpus.frequency("art:masculine-nominative-singular")
        assertTrue("ὁ occurs ${ho.exact}×", ho.exact > 2000)
        assertTrue(corpus.examples("art:masculine-nominative-singular").any { it.reference == "John 11:35" })
        assertTrue(corpus.examples("inf:aorist-passive-infinitive").any { it.reference == "John 10:35" && it.word == "λυθῆναι" })
        // No vocative of ἀρχή in the NT: no examples, and the app says so rather than inventing one.
        assertEquals(NtFrequency(0, 0), corpus.frequency("arche:vocative-singular"))
        assertFalse(corpus.hasExamples("arche:vocative-singular"))
    }

    @Test
    fun noTextCriticalSignsLeak() {
        assertFalse(text.any { it.code in 0x2E00..0x2E05 })
    }

    @Test
    fun quizExamplePreferExactAndAreStable() {
        val id = "aor_pass:3rd-person-plural"
        val ex = corpus.exampleFor(id, 5)
        assertNotNull(ex)
        assertTrue(ex!!.exact) // Rev 9:15 ἐλύθησαν
        assertEquals(ex, corpus.exampleFor(id, 5))
        assertEquals(null, corpus.exampleFor("arche:vocative-singular", 1))
    }

    @Test
    fun malformedLinesAreSkipped() {
        val c = NtCorpus.parse("# header\nF\tx\tnot-a-number\t0\nE\tlogos:genitive-singular\texact\tλόγος\tJohn 1:1\ta\tλόγου\tb\n")
        assertEquals(NtFrequency(0, 0), c.frequency("x"))
        assertEquals("a λόγου b", c.examples("logos:genitive-singular").single().text)
    }
}
