#!/usr/bin/env python3
"""
Builds app/src/main/assets/nt_examples.tsv: for every form in the app, how often
it occurs in the Greek New Testament and up to three short phrases where it does.

Inputs
  forms.tsv     exported by the FormsExport unit test (see FormsExport.kt)
  morphgnt dir  the 27 *-morphgnt.txt files from https://github.com/morphgnt/sblgnt

Usage
  python3 tools/build_nt_examples.py forms.tsv path/to/sblgnt app/src/main/assets/nt_examples.tsv

λύω is the model regular verb but occurs only 42 times in the NT, so for its
paradigms the script also collects "pattern" examples: other regular verbs with
exactly the same parsing and the same ending (πιστεύουσιν for λύουσι(ν)).

Data: SBL Greek New Testament (CC BY 4.0) with MorphGNT tagging (CC BY-SA 3.0).
The generated file is a derived work and is distributed under CC BY-SA 3.0.
"""
import os
import sys
import unicodedata
from collections import defaultdict

BOOKS = {
    "61": "Matt", "62": "Mark", "63": "Luke", "64": "John", "65": "Acts", "66": "Rom",
    "67": "1 Cor", "68": "2 Cor", "69": "Gal", "70": "Eph", "71": "Phil", "72": "Col",
    "73": "1 Thess", "74": "2 Thess", "75": "1 Tim", "76": "2 Tim", "77": "Titus",
    "78": "Phlm", "79": "Heb", "80": "Jas", "81": "1 Pet", "82": "2 Pet", "83": "1 John",
    "84": "2 John", "85": "3 John", "86": "Jude", "87": "Rev",
}
# Readers tend to know these books best, so their verses are preferred on a tie.
FAMILIAR = ["64", "62", "61", "63", "83", "66", "65"]

LUO = "λύω"

# paradigm id -> (NT lemma, tenses, voices, mood). None means "taken from the parse text".
VERBS = {
    "eimi_pres": ("εἰμί", "P", "A", "I"), "eimi_impf": ("εἰμί", "I", "A", "I"),
    "eimi_fut": ("εἰμί", "F", "M", "I"), "eimi_subj": ("εἰμί", "P", "A", "S"),
    "pres_act": (LUO, "P", "A", "I"), "pres_mp": (LUO, "P", "MP", "I"),
    "impf_act": (LUO, "I", "A", "I"), "impf_mp": (LUO, "I", "MP", "I"),
    "fut_act": (LUO, "F", "A", "I"), "fut_mid": (LUO, "F", "M", "I"), "fut_pass": (LUO, "F", "P", "I"),
    "aor_act": (LUO, "A", "A", "I"), "aor_mid": (LUO, "A", "M", "I"), "aor_pass": (LUO, "A", "P", "I"),
    "aor2_act": ("λαμβάνω", "A", "A", "I"),
    "perf_act": (LUO, "X", "A", "I"), "perf_mp": (LUO, "X", "MP", "I"),
    "agapao": ("ἀγαπάω", "P", "A", "I"), "poieo": ("ποιέω", "P", "A", "I"),
    "plerooo": ("πληρόω", "P", "A", "I"), "didomi": ("δίδωμι", "P", "A", "I"),
    "pres_subj": (LUO, "P", "A", "S"), "aor_subj": (LUO, "A", "A", "S"), "aor_pass_subj": (LUO, "A", "P", "S"),
    "pres_impv": (LUO, "P", "A", "D"), "aor_impv": (LUO, "A", "A", "D"),
    "inf": (LUO, None, None, "N"),
    "ptc_pres_act": (LUO, "P", "A", "P"), "ptc_pres_mp": (LUO, "P", "MP", "P"),
    "ptc_aor_act": (LUO, "A", "A", "P"), "ptc_aor_pass": (LUO, "A", "P", "P"),
}
OTHER_LEMMAS = {
    "art": "ὁ", "ego": "ἐγώ", "su": "σύ", "autos": "αὐτός", "houtos": "οὗτος",
    "ekeinos": "ἐκεῖνος", "hos": "ὅς", "tis": "τίς", "agathos": "ἀγαθός", "pas": "πᾶς",
    "heis": "εἷς", "alethes": "ἀληθής",
    "arche": "ἀρχή", "hemera": "ἡμέρα", "doxa": "δόξα", "prophetes": "προφήτης", "logos": "λόγος",
    "ergon": "ἔργον", "sarx": "σάρξ", "pater": "πατήρ", "pneuma": "πνεῦμα", "polis": "πόλις",
    "basileus": "βασιλεύς", "genos": "γένος",
}
# Personal pronouns are not tagged for gender.
NO_GENDER = {"ego", "su"}

CASES = {"nominative": "N", "genitive": "G", "dative": "D", "accusative": "A", "vocative": "V"}
TENSES = {"present": "P", "imperfect": "I", "future": "F", "aorist": "A", "perfect": "X"}
VOICES = {"active": "A", "middle": "M", "passive": "P"}


def nfc(s):
    return unicodedata.normalize("NFC", s)


def bare(s):
    d = unicodedata.normalize("NFD", s.lower())
    return "".join(c for c in d if not unicodedata.combining(c)).replace("ς", "σ")


def expand(answer):
    if "(" not in answer:
        return [answer]
    o, c = answer.index("("), answer.index(")")
    return [answer[:o] + answer[c + 1:], answer[:o] + answer[o + 1:c] + answer[c + 1:]]


def accent_key(s):
    """Normalises the way GreekText.strict does: grave counts as acute, final sigma."""
    s = nfc(unicodedata.normalize("NFD", s.lower()).replace("̀", "́"))
    return s[:-1] + "ς" if s.endswith("σ") else s


def constraints(pid, col_label, parse):
    """The MorphGNT code positions a form must match: {index: allowed letters}."""
    words = parse.lower().replace("masc./fem.", "masc/fem").split()
    c = {}
    for w in words:
        if w in ("1st", "2nd", "3rd"):
            c[0] = w[0]
        elif w == "singular":
            c[5] = "S"
        elif w == "plural":
            c[5] = "P"
        elif w in CASES:
            c[4] = CASES[w]
    if pid not in NO_GENDER:
        g = col_label.lower()
        if "masc" in g and "fem" in g:
            c[6] = "MF"
        elif g.startswith("masc"):
            c[6] = "M"
        elif g.startswith("fem"):
            c[6] = "F"
        elif g.startswith("neut"):
            c[6] = "N"
    if pid in VERBS:
        _, tense, voice, mood = VERBS[pid]
        if tense is None:  # infinitives: "aorist passive infinitive"
            tense = TENSES[words[0]]
            voice = "MP" if words[0] in ("present", "perfect") and words[1] != "active" else VOICES[words[1]]
        c[1], c[2], c[3] = tense, voice, mood
    return c


def matches(code, cons):
    return all(code[i] in allowed for i, allowed in cons.items())


def regular_verb(lemma):
    """An ω verb whose endings follow λύω: no contract or μι verbs."""
    b = bare(lemma)
    if b.endswith(("αω", "εω", "οω", "μι", "αομαι", "εομαι", "οομαι")):
        return False
    return b.endswith("ω") or b.endswith("ομαι")


def luo_ending(answer):
    """The part of a λύω form after the stem: ἐλύθησαν -> θησαν."""
    b = bare(answer)
    i = b.find("λυ")
    return b[i + 2:] if i >= 0 else None


# Text-critical signs in the SBLGNT (⸀ ⸁ ⸂ ⸃ ⸄ ⸅) that mark where the apparatus has a variant.
SIGLA = {c: None for c in range(0x2E00, 0x2E06)}


def load_nt(directory):
    words, verses = [], defaultdict(list)
    for name in sorted(os.listdir(directory)):
        if not name.endswith("-morphgnt.txt"):
            continue
        with open(os.path.join(directory, name), encoding="utf-8") as fh:
            for line in fh:
                parts = line.split()
                if len(parts) != 7:
                    continue
                bcv, pos, code, text, word, norm, lemma = parts
                key = bcv
                w = {"bcv": bcv, "pos": pos, "code": code, "text": nfc(text).translate(SIGLA), "norm": nfc(norm),
                     "lemma": nfc(lemma), "verse": key, "index": len(verses[key])}
                verses[key].append(w)
                words.append(w)
    return words, verses


def reference(bcv):
    book = BOOKS["%d" % (60 + int(bcv[0:2]))]
    return "%s %d:%d" % (book, int(bcv[2:4]), int(bcv[4:6]))


def phrase(verses, w, window=5, whole=14):
    tokens = [t["text"] for t in verses[w["verse"]]]
    i = w["index"]
    lo, hi = (0, len(tokens)) if len(tokens) <= whole else (max(0, i - window), min(len(tokens), i + window + 1))
    before = " ".join(tokens[lo:i])
    after = " ".join(tokens[i + 1:hi])
    if lo > 0:
        before = "… " + before
    if hi < len(tokens):
        after = after + " …"
    return before, tokens[i], after


def pick(occurrences, verses, n=3):
    """Short verses first, familiar books on a tie, at most one per book."""
    def rank(w):
        book = "%d" % (60 + int(w["bcv"][0:2]))
        familiar = FAMILIAR.index(book) if book in FAMILIAR else len(FAMILIAR)
        return (len(verses[w["verse"]]) > 14, familiar, len(verses[w["verse"]]), w["bcv"])
    chosen, books = [], set()
    for w in sorted(occurrences, key=rank):
        if w["bcv"][0:2] in books:
            continue
        chosen.append(w)
        books.add(w["bcv"][0:2])
        if len(chosen) == n:
            break
    return chosen


def main(forms_path, nt_dir, out_path):
    words, verses = load_nt(nt_dir)
    by_lemma = defaultdict(list)
    for w in words:
        by_lemma[w["lemma"]].append(w)
    verbs = [w for w in words if w["pos"] == "V-" and regular_verb(w["lemma"])]

    out = ["# NT occurrences and example phrases for each form. Generated by tools/build_nt_examples.py.",
           "# Source: SBL Greek New Testament (CC BY 4.0), MorphGNT tagging (CC BY-SA 3.0). This file: CC BY-SA 3.0.",
           "# F <formId> <exact count> <pattern count>",
           "# E <formId> <exact|pattern> <lemma> <reference> <before> <word> <after>"]
    mismatches, stats = [], defaultdict(int)

    with open(forms_path, encoding="utf-8") as fh:
        forms = [line.rstrip("\n").split("\t") for line in fh if line.strip()]

    for form_id, pid, col_label, parse, answers in forms:
        answers = [nfc(a) for a in answers.split("/")]
        spellings = {accent_key(x) for a in answers for x in expand(a)}
        lemma = VERBS[pid][0] if pid in VERBS else OTHER_LEMMAS[pid]
        cons = constraints(pid, col_label, parse)

        tagged = [w for w in by_lemma[lemma] if matches(w["code"], cons)]
        def attested(w):
            return any(accent_key(x) in spellings for x in expand(w["norm"]))
        exact = [w for w in tagged if attested(w)]
        for w in tagged:
            if not attested(w):
                mismatches.append((form_id, w["norm"], reference(w["bcv"])))

        pattern = []
        if lemma == LUO:
            endings = {e for a in answers for x in expand(a) for e in [luo_ending(x)] if e is not None}
            pattern = [w for w in verbs if w["lemma"] != LUO and matches(w["code"], cons)
                       and any(bare(w["norm"]).endswith(e) for e in endings)]

        out.append("F\t%s\t%d\t%d" % (form_id, len(exact), len(pattern)))
        chosen = [("exact", w) for w in pick(exact, verses)]
        if len(chosen) < 3 and pattern:
            # One example per lemma, most frequent lemmas first, so the patterns are recognisable.
            freq = defaultdict(int)
            for w in pattern:
                freq[w["lemma"]] += 1
            seen = set()
            for w in sorted(pattern, key=lambda w: (-freq[w["lemma"]], len(verses[w["verse"]]), w["bcv"])):
                if w["lemma"] in seen or len(verses[w["verse"]]) > 30:
                    continue
                seen.add(w["lemma"])
                chosen.append(("pattern", w))
                if len(chosen) == 3:
                    break
        stats["exact" if exact else ("pattern" if pattern else "none")] += 1
        for kind, w in chosen:
            before, word, after = phrase(verses, w)
            out.append("\t".join(["E", form_id, kind, w["lemma"], reference(w["bcv"]), before, word, after]))

    with open(out_path, "w", encoding="utf-8") as fh:
        fh.write("\n".join(out) + "\n")

    print("forms: %d  with exact NT examples: %d  pattern only: %d  none: %d"
          % (len(forms), stats["exact"], stats["pattern"], stats["none"]))
    if mismatches:
        agg = defaultdict(list)
        for form_id, norm, ref in mismatches:
            agg[(form_id, norm)].append(ref)
        print("NT spellings that differ from the paradigm tables (%d kinds):" % len(agg))
        for (form_id, norm), refs in sorted(agg.items(), key=lambda kv: -len(kv[1])):
            print("  %-45s %-14s %4d×  e.g. %s" % (form_id, norm, len(refs), refs[0]))


if __name__ == "__main__":
    if len(sys.argv) != 4:
        sys.exit(__doc__)
    main(*sys.argv[1:])
