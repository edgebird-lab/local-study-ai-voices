// SPDX-FileCopyrightText: 2026 Robin Olbricht – Olbricht Digital (edgebird-lab)
// SPDX-License-Identifier: GPL-3.0-or-later

package de.edgebird.voices

import java.util.Locale

/** Die mitgelieferten Stimmen. [dir] liegt unter assets/voices/. ISO-3-Sprachcodes, wie sie die Android-Sprachausgabe erwartet. */
data class VoiceInfo(val name: String, val title: String, val dir: String, val locale: Locale, val iso3Lang: String, val sample: String)

object VoiceCatalog {
    val all = listOf(
        VoiceInfo("de-thorsten", "Thorsten (Deutsch)", "de", Locale("de", "DE"), "deu", "Hallo! So klinge ich, wenn ich dir deine Lernunterlagen vorlese."),
        VoiceInfo("en-ljspeech", "LJSpeech (English)", "en", Locale("en", "US"), "eng", "Hello! This is how I sound when I read your study notes aloud."),
    )

    fun byName(name: String?) = all.firstOrNull { it.name == name }

    /** Passende Stimme zu ISO-3-Sprache (und optional Land); `null`, wenn die Sprache nicht angeboten wird. */
    fun forLanguage(iso3: String?): VoiceInfo? = all.firstOrNull { it.iso3Lang.equals(iso3, ignoreCase = true) || it.locale.isO3Language.equals(iso3, ignoreCase = true) }
}
