# Overview

I wanted to find out by shipping something small and complete how a native Android app is actually put together. I chose a subject I know well, Biblical Greek so that all of my attention could go on the unfamiliar part.

**Greek Paradigm Trainer** drills the inflected forms of Greek nouns and verbs. You open it to a list of nine decks. Picking one opens the practice screen, which shows a lemma and a grammatical slot. You recall the form, tap **Show answer** to check yourself, then mark it **Known** or **Not known**. The app keeps a running score and a position counter, and tells you how you did at the end. A **Reference** button shows the whole paradigm as a table, and **Decks** takes you back to the list.

My purpose was to understand three things I had never worked with: how a declarative UI framework differs from the imperative code I was used to, how state drives what appears on screen, and how to structure an app so that the screens know nothing about where their data comes from.

[Software Demo Video](https://youtu.be/8Wt8cYFHZhY)

# Development Environment

I built the app in **Android Studio**, writing **Kotlin** with **Jetpack Compose** and **Material 3**. The minimum SDK is 24, and I developed and tested throughout on a physical Android phone over USB rather than an emulator. Version control was **Git** with **GitHub**. The paradigm data was generated separately from MorphGNT text files

# Useful Websites

* [Android Basics with Compose](https://developer.android.com/courses/android-basics-compose/course)
* [State in Jetpack Compose](https://developer.android.com/codelabs/jetpack-compose-state)
* [Jetpack Compose documentation](https://developer.android.com/develop/ui/compose/documentation)
* [Run apps on a hardware device](https://developer.android.com/studio/run/device)
* [MorphGNT](https://github.com/morphgnt/sblgnt)
* [SBLGNT](https://www.sblgnt.com/)

# Future Work

* Save progress so scores survive closing the app
* Shuffle the order within a deck, so I learn the forms rather than the sequence
* Add the rest of the verb system: the present middle and passive, then the aorist
* Move the paradigms into a JSON asset, so adding one does not mean recompiling
* Show a verse reference beside each form

# Attribution

Greek text: SBL Greek New Testament, © 2010 Society of Biblical Literature and Logos Bible Software, licensed CC BY 4.0 — https://sblgnt.com

Morphological tagging: MorphGNT, licensed CC BY-SA 3.0 — https://github.com/morphgnt/sblgnt

Lemma frequencies and paradigm forms were computed from the above.
