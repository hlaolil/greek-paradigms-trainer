package com.example.greekparadigms

/*
 * The paradigm library. Forms follow the standard Koine paradigms taught in
 * first-year grammars (λύω as the model regular verb), checked against usage
 * in the SBL Greek New Testament / MorphGNT.
 */

private const val M = "Masculine"
private const val F = "Feminine"
private const val N = "Neuter"

private val CASE_ABBR4 = listOf("Nom", "Gen", "Dat", "Acc")
private val CASES4 = listOf("nominative", "genitive", "dative", "accusative")
private fun num(col: Int) = if (col == 0) "singular" else "plural"

// --- Article & pronouns ----------------------------------------------------

private val pronouns = listOf(
    gendered(
        "art", "ὁ, ἡ, τό", "the", "Definite article", Category.PRONOUNS,
        "No vocative. ὁ, ἡ, οἱ, αἱ have a rough breathing instead of τ and no accent. " +
            "The genitive plural τῶν is the same in all genders.",
        M to "ὁ τοῦ τῷ τόν οἱ τῶν τοῖς τούς",
        F to "ἡ τῆς τῇ τήν αἱ τῶν ταῖς τάς",
        N to "τό τοῦ τῷ τό τά τῶν τοῖς τά",
    ),
    grid(
        "ego", "ἐγώ", "I, me", "1st person personal pronoun", Category.PRONOUNS,
        "The unaccented forms μου, μοι, με are enclitic and unemphatic. " +
            "The plural ἡμ- (we) has a rough breathing; compare ὑμ- (you).",
        rows = CASE_ABBR4, cols = listOf("Singular", "Plural"),
        grid = listOf("ἐγώ ἡμεῖς", "ἐμοῦ/μου ἡμῶν", "ἐμοί/μοι ἡμῖν", "ἐμέ/με ἡμᾶς"),
    ) { r, c -> "${CASES4[r]} ${num(c)}" },
    grid(
        "su", "σύ", "you", "2nd person personal pronoun", Category.PRONOUNS,
        "σου, σοι, σε are the enclitic forms. ἡμεῖς (we) and ὑμεῖς (you) differ only in their first vowel.",
        rows = CASE_ABBR4, cols = listOf("Singular", "Plural"),
        grid = listOf("σύ ὑμεῖς", "σοῦ/σου ὑμῶν", "σοί/σοι ὑμῖν", "σέ/σε ὑμᾶς"),
    ) { r, c -> "${CASES4[r]} ${num(c)}" },
    gendered(
        "autos", "αὐτός, αὐτή, αὐτό", "he, she, it; self; same", "3rd person pronoun", Category.PRONOUNS,
        "Endings of ἀγαθός, but the neuter nom/acc singular is αὐτό with no ν. " +
            "Smooth breathing: αὐτ-, not the rough-breathing αὑτ- of the reflexive.",
        M to "αὐτός αὐτοῦ αὐτῷ αὐτόν αὐτοί αὐτῶν αὐτοῖς αὐτούς",
        F to "αὐτή αὐτῆς αὐτῇ αὐτήν αὐταί αὐτῶν αὐταῖς αὐτάς",
        N to "αὐτό αὐτοῦ αὐτῷ αὐτό αὐτά αὐτῶν αὐτοῖς αὐτά",
    ),
    gendered(
        "houtos", "οὗτος, αὕτη, τοῦτο", "this, these", "Near demonstrative", Category.PRONOUNS,
        "Like the article: rough breathing instead of τ in οὗτος, αὕτη, οὗτοι, αὗται. " +
            "The stem vowel follows the ending: ου with an ο/ω ending, αυ with an α/η ending.",
        M to "οὗτος τούτου τούτῳ τοῦτον οὗτοι τούτων τούτοις τούτους",
        F to "αὕτη ταύτης ταύτῃ ταύτην αὗται τούτων ταύταις ταύτας",
        N to "τοῦτο τούτου τούτῳ τοῦτο ταῦτα τούτων τούτοις ταῦτα",
    ),
    gendered(
        "ekeinos", "ἐκεῖνος, -η, -ο", "that, those", "Far demonstrative", Category.PRONOUNS,
        "Regular 2-1-2 endings, but neuter nom/acc singular ἐκεῖνο has no ν (like αὐτό).",
        M to "ἐκεῖνος ἐκείνου ἐκείνῳ ἐκεῖνον ἐκεῖνοι ἐκείνων ἐκείνοις ἐκείνους",
        F to "ἐκείνη ἐκείνης ἐκείνῃ ἐκείνην ἐκεῖναι ἐκείνων ἐκείναις ἐκείνας",
        N to "ἐκεῖνο ἐκείνου ἐκείνῳ ἐκεῖνο ἐκεῖνα ἐκείνων ἐκείνοις ἐκεῖνα",
    ),
    gendered(
        "hos", "ὅς, ἥ, ὅ", "who, which", "Relative pronoun", Category.PRONOUNS,
        "The endings of the article with a rough breathing and an accent. " +
            "Don't confuse ἥ (who) with the article ἡ, or ὅ (which) with ὁ.",
        M to "ὅς οὗ ᾧ ὅν οἵ ὧν οἷς οὕς",
        F to "ἥ ἧς ᾗ ἥν αἵ ὧν αἷς ἅς",
        N to "ὅ οὗ ᾧ ὅ ἅ ὧν οἷς ἅ",
    ),
    gendered(
        "tis", "τίς, τί", "who? what?", "Interrogative pronoun", Category.PRONOUNS,
        "3rd declension, stem τιν-. The interrogative always keeps its acute on the first syllable " +
            "(τίς, τίνος); the indefinite τις, τι (someone) is enclitic.",
        "Masc./Fem." to "τίς τίνος τίνι τίνα τίνες τίνων τίσι(ν) τίνας",
        N to "τί τίνος τίνι τί τίνα τίνων τίσι(ν) τίνα",
    ),
)

// --- Nouns -----------------------------------------------------------------

private val nouns = listOf(
    noun(
        "arche", "ἀρχή", "beginning", "1st decl. feminine (η)",
        "η throughout the singular. The 1st declension genitive plural is always -ῶν.",
        sg = "ἀρχή ἀρχῆς ἀρχῇ ἀρχήν ἀρχή", pl = "ἀρχαί ἀρχῶν ἀρχαῖς ἀρχάς ἀρχαί",
    ),
    noun(
        "hemera", "ἡμέρα", "day", "1st decl. feminine (α pure)",
        "The stem ends in ε, ι or ρ, so α is kept throughout the singular. " +
            "Genitive singular ἡμέρας looks like the accusative plural; the article tells them apart.",
        sg = "ἡμέρα ἡμέρας ἡμέρᾳ ἡμέραν ἡμέρα", pl = "ἡμέραι ἡμερῶν ἡμέραις ἡμέρας ἡμέραι",
    ),
    noun(
        "doxa", "δόξα", "glory", "1st decl. feminine (mixed α/η)",
        "After a stem not ending in ε, ι or ρ, a short α in the nominative becomes η in the genitive and dative singular.",
        sg = "δόξα δόξης δόξῃ δόξαν δόξα", pl = "δόξαι δοξῶν δόξαις δόξας δόξαι",
    ),
    noun(
        "prophetes", "προφήτης", "prophet", "1st decl. masculine",
        "A masculine noun of the 1st declension: nominative -ης and a genitive -ου borrowed from the 2nd declension.",
        sg = "προφήτης προφήτου προφήτῃ προφήτην προφῆτα",
        pl = "προφῆται προφητῶν προφήταις προφήτας προφῆται",
    ),
    noun(
        "logos", "λόγος", "word", "2nd decl. masculine",
        "The model 2nd declension noun. The vocative singular ends in -ε.",
        sg = "λόγος λόγου λόγῳ λόγον λόγε", pl = "λόγοι λόγων λόγοις λόγους λόγοι",
    ),
    noun(
        "ergon", "ἔργον", "work", "2nd decl. neuter",
        "The neuter rule: nominative, accusative and vocative are always the same, and the neuter plural ends in -α.",
        sg = "ἔργον ἔργου ἔργῳ ἔργον ἔργον", pl = "ἔργα ἔργων ἔργοις ἔργα ἔργα",
    ),
    noun(
        "sarx", "σάρξ", "flesh", "3rd decl. (stop stem)",
        "Stem σαρκ-. Before σ, κ combines to ξ: σάρξ < σαρκ-ς, σαρξί < σαρκ-σι.",
        sg = "σάρξ σαρκός σαρκί σάρκα σάρξ", pl = "σάρκες σαρκῶν σαρξί(ν) σάρκας σάρκες",
    ),
    noun(
        "pater", "πατήρ", "father", "3rd decl. (syncopated)",
        "The stem shifts between πατερ- and πατρ-; the dative plural is πατράσι(ν).",
        sg = "πατήρ πατρός πατρί πατέρα πάτερ", pl = "πατέρες πατέρων πατράσι(ν) πατέρας πατέρες",
    ),
    noun(
        "pneuma", "πνεῦμα", "spirit, wind", "3rd decl. neuter (-ματ-)",
        "Stem πνευματ-. The τ drops at the end of the word, and before σ in the dative plural.",
        sg = "πνεῦμα πνεύματος πνεύματι πνεῦμα πνεῦμα",
        pl = "πνεύματα πνευμάτων πνεύμασι(ν) πνεύματα πνεύματα",
    ),
    noun(
        "polis", "πόλις", "city", "3rd decl. (ι/ε stem)",
        "The ι of the nominative becomes ε in most forms; genitive singular -εως.",
        sg = "πόλις πόλεως πόλει πόλιν πόλι", pl = "πόλεις πόλεων πόλεσι(ν) πόλεις πόλεις",
    ),
    noun(
        "basileus", "βασιλεύς", "king", "3rd decl. (ευ stem)",
        "Stem βασιλευ-/βασιλε-. Genitive -εως, accusative -εα; the nominative and accusative plural are both βασιλεῖς.",
        sg = "βασιλεύς βασιλέως βασιλεῖ βασιλέα βασιλεῦ",
        pl = "βασιλεῖς βασιλέων βασιλεῦσι(ν) βασιλεῖς βασιλεῖς",
    ),
    noun(
        "genos", "γένος", "race, kind", "3rd decl. neuter (-ες stem)",
        "Stem γενεσ-. The σ between vowels drops and the vowels contract: γένεσ-ος > γένεος > γένους.",
        sg = "γένος γένους γένει γένος γένος", pl = "γένη γενῶν γένεσι(ν) γένη γένη",
    ),
)

// --- Adjectives ------------------------------------------------------------

private val adjectives = listOf(
    gendered(
        "agathos", "ἀγαθός, -ή, -όν", "good", "2-1-2 adjective", Category.ADJECTIVES,
        "Masculine and neuter follow λόγος and ἔργον; the feminine follows ἀρχή.",
        M to "ἀγαθός ἀγαθοῦ ἀγαθῷ ἀγαθόν ἀγαθοί ἀγαθῶν ἀγαθοῖς ἀγαθούς",
        F to "ἀγαθή ἀγαθῆς ἀγαθῇ ἀγαθήν ἀγαθαί ἀγαθῶν ἀγαθαῖς ἀγαθάς",
        N to "ἀγαθόν ἀγαθοῦ ἀγαθῷ ἀγαθόν ἀγαθά ἀγαθῶν ἀγαθοῖς ἀγαθά",
    ),
    gendered(
        "pas", "πᾶς, πᾶσα, πᾶν", "all, every", "3-1-3 adjective", Category.ADJECTIVES,
        "Stem παντ-. Masculine and neuter are 3rd declension; the feminine πᾶσα is 1st declension.",
        M to "πᾶς παντός παντί πάντα πάντες πάντων πᾶσι(ν) πάντας",
        F to "πᾶσα πάσης πάσῃ πᾶσαν πᾶσαι πασῶν πάσαις πάσας",
        N to "πᾶν παντός παντί πᾶν πάντα πάντων πᾶσι(ν) πάντα",
    ),
    gendered(
        "heis", "εἷς, μία, ἕν", "one", "Numeral", Category.ADJECTIVES,
        "Singular only. Masculine and neuter share the stem ἑν-; the feminine μία is 1st declension.",
        M to "εἷς ἑνός ἑνί ἕνα - - - -",
        F to "μία μιᾶς μιᾷ μίαν - - - -",
        N to "ἕν ἑνός ἑνί ἕν - - - -",
    ),
    gendered(
        "alethes", "ἀληθής, -ές", "true", "3rd decl. adjective (2-2)", Category.ADJECTIVES,
        "Masculine and feminine share one set of forms. Stem ἀληθεσ-: the σ drops and the vowels contract (ἀληθέσ-ος > ἀληθοῦς).",
        "Masc./Fem." to "ἀληθής ἀληθοῦς ἀληθεῖ ἀληθῆ ἀληθεῖς ἀληθῶν ἀληθέσι(ν) ἀληθεῖς",
        N to "ἀληθές ἀληθοῦς ἀληθεῖ ἀληθές ἀληθῆ ἀληθῶν ἀληθέσι(ν) ἀληθῆ",
    ),
)

// --- Verbs: indicative -----------------------------------------------------

private val I = Category.INDICATIVE

private val indicative = listOf(
    finite(
        "eimi_pres", "εἰμί", "I am", "Present indicative", I,
        "Irregular, so it has to be memorised. Every form except εἶ is enclitic in running text.",
        "εἰμί εἶ ἐστί(ν) ἐσμέν ἐστέ εἰσί(ν)",
    ),
    finite(
        "eimi_impf", "εἰμί", "I was", "Imperfect indicative", I,
        "1st singular ἤμην has a middle ending; 3rd singular ἦν.",
        "ἤμην ἦς ἦν ἦμεν ἦτε ἦσαν",
    ),
    finite(
        "eimi_fut", "εἰμί", "I will be", "Future indicative", I,
        "Middle endings on the stem ἐσ-. 3rd singular ἔσται has no connecting vowel.",
        "ἔσομαι ἔσῃ ἔσται ἐσόμεθα ἔσεσθε ἔσονται",
    ),
    finite(
        "pres_act", "λύω", "I loose", "Present active indicative", I,
        "Primary active endings: -ω, -εις, -ει, -ομεν, -ετε, -ουσι(ν).",
        "λύω λύεις λύει λύομεν λύετε λύουσι(ν)",
    ),
    finite(
        "pres_mp", "λύω", "I am being loosed", "Present middle/passive indicative", I,
        "Primary middle/passive endings: -μαι, -σαι, -ται, -μεθα, -σθε, -νται. " +
            "In the 2nd singular the σ drops and ε + αι contracts to ῃ.",
        "λύομαι λύῃ λύεται λυόμεθα λύεσθε λύονται",
    ),
    finite(
        "impf_act", "λύω", "I was loosing", "Imperfect active indicative", I,
        "Augment ἐ- + present stem + secondary active endings. The 1st singular and 3rd plural are both ἔλυον.",
        "ἔλυον ἔλυες ἔλυε(ν) ἐλύομεν ἐλύετε ἔλυον",
    ),
    finite(
        "impf_mp", "λύω", "I was being loosed", "Imperfect middle/passive indicative", I,
        "Augment + secondary middle/passive endings: -μην, -σο (> ου), -το, -μεθα, -σθε, -ντο.",
        "ἐλυόμην ἐλύου ἐλύετο ἐλυόμεθα ἐλύεσθε ἐλύοντο",
    ),
    finite(
        "fut_act", "λύω", "I will loose", "Future active indicative", I,
        "Tense formative σ + the primary active endings of the present.",
        "λύσω λύσεις λύσει λύσομεν λύσετε λύσουσι(ν)",
    ),
    finite(
        "fut_mid", "λύω", "I will loose (for myself)", "Future middle indicative", I,
        "σ + primary middle/passive endings.",
        "λύσομαι λύσῃ λύσεται λυσόμεθα λύσεσθε λύσονται",
    ),
    finite(
        "fut_pass", "λύω", "I will be loosed", "Future passive indicative", I,
        "The aorist passive stem without augment (λυθη-) + σ + primary middle/passive endings.",
        "λυθήσομαι λυθήσῃ λυθήσεται λυθησόμεθα λυθήσεσθε λυθήσονται",
    ),
    finite(
        "aor_act", "λύω", "I loosed", "1st aorist active indicative", I,
        "Augment + stem + σα + secondary endings. The 3rd singular ends in -σε(ν).",
        "ἔλυσα ἔλυσας ἔλυσε(ν) ἐλύσαμεν ἐλύσατε ἔλυσαν",
    ),
    finite(
        "aor_mid", "λύω", "I loosed (for myself)", "1st aorist middle indicative", I,
        "Augment + σα + secondary middle endings. The 2nd singular ἐλύσω comes from ἐλύσασο.",
        "ἐλυσάμην ἐλύσω ἐλύσατο ἐλυσάμεθα ἐλύσασθε ἐλύσαντο",
    ),
    finite(
        "aor_pass", "λύω", "I was loosed", "Aorist passive indicative", I,
        "Augment + stem + θη + secondary active endings (3rd plural -σαν).",
        "ἐλύθην ἐλύθης ἐλύθη ἐλύθημεν ἐλύθητε ἐλύθησαν",
    ),
    finite(
        "aor2_act", "λαμβάνω", "I took, received", "2nd aorist active indicative", I,
        "Second aorist: augment + a changed stem (λαβ-) + the endings of the imperfect.",
        "ἔλαβον ἔλαβες ἔλαβε(ν) ἐλάβομεν ἐλάβετε ἔλαβον",
    ),
    finite(
        "perf_act", "λύω", "I have loosed", "Perfect active indicative", I,
        "Reduplication (λε-) + stem + κα. The 3rd plural is also found as λέλυκαν.",
        "λέλυκα λέλυκας λέλυκε(ν) λελύκαμεν λελύκατε λελύκασι(ν)/λέλυκαν",
    ),
    finite(
        "perf_mp", "λύω", "I have been loosed", "Perfect middle/passive indicative", I,
        "Reduplication, no tense formative and no connecting vowel: the endings sit directly on the stem.",
        "λέλυμαι λέλυσαι λέλυται λελύμεθα λέλυσθε λέλυνται",
    ),
    finite(
        "agapao", "ἀγαπάω", "I love", "Contract verb (α): present active", I,
        "α + ε > α, α + ο/ω > ω, and ι becomes subscript: ἀγαπάεις > ἀγαπᾷς.",
        "ἀγαπῶ ἀγαπᾷς ἀγαπᾷ ἀγαπῶμεν ἀγαπᾶτε ἀγαπῶσι(ν)",
    ),
    finite(
        "poieo", "ποιέω", "I do, make", "Contract verb (ε): present active", I,
        "ε + ε > ει, ε + ο > ου, ε + ω > ω, ε + ει > ει, ε + ου > ου.",
        "ποιῶ ποιεῖς ποιεῖ ποιοῦμεν ποιεῖτε ποιοῦσι(ν)",
    ),
    finite(
        "plerooo", "πληρόω", "I fill, fulfil", "Contract verb (ο): present active", I,
        "ο + ε/ο/ου > ου, ο + ω > ω, and any ο + ι combination > οι.",
        "πληρῶ πληροῖς πληροῖ πληροῦμεν πληροῦτε πληροῦσι(ν)",
    ),
    finite(
        "didomi", "δίδωμι", "I give", "μι verb: present active", I,
        "Reduplicated stem διδω-/διδο- with the μι endings -μι, -ς, -σι(ν), -μεν, -τε, -ασι(ν).",
        "δίδωμι δίδως δίδωσι(ν) δίδομεν δίδοτε διδόασι(ν)",
    ),
)

// --- Verbs: subjunctive, imperative, infinitive ---------------------------

private val NI = Category.NON_INDICATIVE

private val nonIndicative = listOf(
    finite(
        "pres_subj", "λύω", "(that) I may loose", "Present active subjunctive", NI,
        "The connecting vowel lengthens (ο > ω, ε > η), and ι becomes subscript: λύῃς, λύῃ.",
        "λύω λύῃς λύῃ λύωμεν λύητε λύωσι(ν)",
    ),
    finite(
        "aor_subj", "λύω", "(that) I may loose", "Aorist active subjunctive", NI,
        "No augment (that belongs to the indicative only). Aorist stem λυσ- + the endings of the present subjunctive.",
        "λύσω λύσῃς λύσῃ λύσωμεν λύσητε λύσωσι(ν)",
    ),
    finite(
        "aor_pass_subj", "λύω", "(that) I may be loosed", "Aorist passive subjunctive", NI,
        "Unaugmented λυθ- + the lengthened vowel endings, all with a circumflex on the ending.",
        "λυθῶ λυθῇς λυθῇ λυθῶμεν λυθῆτε λυθῶσι(ν)",
    ),
    finite(
        "eimi_subj", "εἰμί", "(that) I may be", "Present subjunctive", NI,
        "The bare subjunctive endings with a rough breathing and a circumflex. These are also the endings of λυθῶ.",
        "ὦ ᾖς ᾖ ὦμεν ἦτε ὦσι(ν)",
    ),
    imperative(
        "pres_impv", "λύω", "loose!", "Present active imperative",
        "2nd singular -ε, 3rd -έτω / -έτωσαν. The 2nd plural λύετε is the same as the indicative.",
        "λῦε λυέτω λύετε λυέτωσαν",
    ),
    imperative(
        "aor_impv", "λύω", "loose!", "Aorist active imperative",
        "No augment. 2nd singular -σον, and the rest keep the σα of the aorist.",
        "λῦσον λυσάτω λύσατε λυσάτωσαν",
    ),
    grid(
        "inf", "λύω", "to loose", "Infinitives", NI,
        "Infinitives have tense and voice but no person or number. The present middle and passive share one form, λύεσθαι.",
        rows = listOf("Present", "Aorist", "Perfect"),
        cols = listOf("Active", "Middle", "Passive"),
        grid = listOf(
            "λύειν λύεσθαι λύεσθαι",
            "λῦσαι λύσασθαι λυθῆναι",
            "λελυκέναι λελύσθαι λελύσθαι",
        ),
    ) { r, c ->
        "${listOf("present", "aorist", "perfect")[r]} ${listOf("active", "middle", "passive")[c]} infinitive"
    },
)

// --- Participles -----------------------------------------------------------

private val P = Category.PARTICIPLES

private val participles = listOf(
    gendered(
        "ptc_pres_act", "λύων, λύουσα, λῦον", "loosing", "Present active participle", P,
        "Masc./neut. 3rd declension on the stem λυοντ-; feminine 1st declension on λυουσ-. " +
            "The dative plural λύουσι(ν) looks like the 3rd plural indicative.",
        M to "λύων λύοντος λύοντι λύοντα λύοντες λυόντων λύουσι(ν) λύοντας",
        F to "λύουσα λυούσης λυούσῃ λύουσαν λύουσαι λυουσῶν λυούσαις λυούσας",
        N to "λῦον λύοντος λύοντι λῦον λύοντα λυόντων λύουσι(ν) λύοντα",
    ),
    gendered(
        "ptc_pres_mp", "λυόμενος, -η, -ον", "being loosed", "Present middle/passive participle", P,
        "The participle morpheme μεν + the endings of ἀγαθός. The accent stays on the ο of λυό-.",
        M to "λυόμενος λυομένου λυομένῳ λυόμενον λυόμενοι λυομένων λυομένοις λυομένους",
        F to "λυομένη λυομένης λυομένῃ λυομένην λυόμεναι λυομένων λυομέναις λυομένας",
        N to "λυόμενον λυομένου λυομένῳ λυόμενον λυόμενα λυομένων λυομένοις λυόμενα",
    ),
    gendered(
        "ptc_aor_act", "λύσας, λύσασα, λῦσαν", "having loosed", "Aorist active participle", P,
        "No augment. Stem λυσαντ- for masc./neut. and λυσασ- for the feminine.",
        M to "λύσας λύσαντος λύσαντι λύσαντα λύσαντες λυσάντων λύσασι(ν) λύσαντας",
        F to "λύσασα λυσάσης λυσάσῃ λύσασαν λύσασαι λυσασῶν λυσάσαις λυσάσας",
        N to "λῦσαν λύσαντος λύσαντι λῦσαν λύσαντα λυσάντων λύσασι(ν) λύσαντα",
    ),
    gendered(
        "ptc_aor_pass", "λυθείς, λυθεῖσα, λυθέν", "having been loosed", "Aorist passive participle", P,
        "Stem λυθεντ- for masc./neut. and λυθεισ- for the feminine. The accent stays on the θε syllable.",
        M to "λυθείς λυθέντος λυθέντι λυθέντα λυθέντες λυθέντων λυθεῖσι(ν) λυθέντας",
        F to "λυθεῖσα λυθείσης λυθείσῃ λυθεῖσαν λυθεῖσαι λυθεισῶν λυθείσαις λυθείσας",
        N to "λυθέν λυθέντος λυθέντι λυθέν λυθέντα λυθέντων λυθεῖσι(ν) λυθέντα",
    ),
)

/** Every paradigm in the app, in display order. */
val paradigms: List<Paradigm> by lazy { pronouns + nouns + adjectives + indicative + nonIndicative + participles }

/** Fast lookup tables. */
val paradigmById: Map<String, Paradigm> by lazy { paradigms.associateBy { it.id } }
val formById: Map<String, Form> by lazy { paradigms.flatMap { it.forms }.associateBy { it.id } }

/** Matches lemma, title or gloss, with or without accents, in Greek or Latin letters. */
fun search(query: String): List<Paradigm> {
    val q = query.trim().lowercase()
    if (q.isEmpty()) return paradigms
    val bareQ = GreekText.bare(q)
    return paradigms.filter { p ->
        val hay = listOf(p.lemma, p.title, p.gloss, p.category.title)
        hay.any { it.lowercase().contains(q) || GreekText.bare(it).contains(bareQ) } ||
            GreekText.transliterate(p.lemma).contains(q) ||
            p.forms.any { f -> f.answers.any { GreekText.bare(it).startsWith(bareQ) } }
    }
}
