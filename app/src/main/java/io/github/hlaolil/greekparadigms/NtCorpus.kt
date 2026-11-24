package io.github.hlaolil.greekparadigms

/**
 * A phrase from the Greek New Testament containing a form.
 *
 * [exact] examples are the paradigm's own word (λόγου in Luke 1:2). Pattern
 * examples are used for the model verb λύω, which is rare in the NT: another
 * regular verb with the same parsing and ending (πιστεύουσιν for λύουσι(ν)).
 */
data class NtExample(
    val formId: String,
    val exact: Boolean,
    val lemma: String,
    val reference: String,
    val before: String,
    val word: String,
    val after: String,
) {
    val text: String get() = listOf(before, word, after).filter { it.isNotEmpty() }.joinToString(" ")
}

/** How often a form occurs in the NT: as the paradigm's own word, and as the same pattern in other verbs. */
data class NtFrequency(val exact: Int, val pattern: Int)

/**
 * NT occurrences and example phrases for every form, read from the
 * `nt_examples.tsv` asset that `tools/build_nt_examples.py` generates from
 * the SBLGNT with MorphGNT tagging.
 */
class NtCorpus(
    private val frequencies: Map<String, NtFrequency>,
    private val examples: Map<String, List<NtExample>>,
) {
    fun frequency(formId: String): NtFrequency = frequencies[formId] ?: NtFrequency(0, 0)
    fun examples(formId: String): List<NtExample> = examples[formId].orEmpty()
    fun hasExamples(formId: String) = examples(formId).isNotEmpty()

    /** How often the paradigm's own forms occur in the NT. */
    fun occurrences(paradigm: Paradigm): Int = paradigm.forms.sumOf { frequency(it.id).exact }

    /**
     * An example for a quiz, chosen by [seed] so it stays put while the card is on
     * screen. Exact examples are preferred to pattern ones.
     */
    fun exampleFor(formId: String, seed: Int): NtExample? {
        val all = examples(formId)
        if (all.isEmpty()) return null
        val pool = all.filter { it.exact }.ifEmpty { all }
        return pool[Math.floorMod(seed, pool.size)]
    }

    val isEmpty: Boolean get() = examples.isEmpty()

    companion object {
        val EMPTY = NtCorpus(emptyMap(), emptyMap())

        fun parse(text: String): NtCorpus {
            val freq = mutableMapOf<String, NtFrequency>()
            val ex = mutableMapOf<String, MutableList<NtExample>>()
            text.lineSequence().forEach { line ->
                if (line.isBlank() || line.startsWith("#")) return@forEach
                val f = line.split("\t")
                runCatching {
                    when (f[0]) {
                        "F" -> freq[f[1]] = NtFrequency(f[2].toInt(), f[3].toInt())
                        "E" -> ex.getOrPut(f[1]) { mutableListOf() } +=
                            NtExample(f[1], f[2] == "exact", f[3], f[4], f[5], f[6], f.getOrElse(7) { "" })
                    }
                }
            }
            return NtCorpus(freq, ex)
        }
    }
}

/** "1,234×" */
fun timesInNt(n: Int): String = "%,d×".format(n)
