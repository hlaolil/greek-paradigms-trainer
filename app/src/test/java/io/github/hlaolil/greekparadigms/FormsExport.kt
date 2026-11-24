package io.github.hlaolil.greekparadigms

import java.io.File
import org.junit.Assume.assumeTrue
import org.junit.Test

/**
 * Writes every form to a TSV for `tools/build_nt_examples.py`. Skipped unless the
 * EXPORT_FORMS environment variable names the output file:
 *
 *     EXPORT_FORMS=build/forms.tsv ./gradlew :app:testDebugUnitTest --tests '*FormsExport*'
 */
class FormsExport {
    @Test
    fun export() {
        val out = System.getenv("EXPORT_FORMS")
        assumeTrue("EXPORT_FORMS not set", !out.isNullOrBlank())
        File(out!!).apply { parentFile?.mkdirs() }.printWriter().use { w ->
            paradigms.forEach { p ->
                p.forms.forEach { f ->
                    w.println(listOf(f.id, p.id, p.colLabels[f.col], f.parse, f.answers.joinToString("/")).joinToString("\t"))
                }
            }
        }
    }
}
