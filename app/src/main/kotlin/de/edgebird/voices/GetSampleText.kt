// SPDX-FileCopyrightText: 2026 Robin Olbricht – Olbricht Digital (edgebird-lab)
// SPDX-License-Identifier: GPL-3.0-or-later

package de.edgebird.voices

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech

/** Liefert den Beispieltext für die Hörprobe in den Systemeinstellungen. */
class GetSampleText : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val lang = intent.getStringExtra("language")
        val v = VoiceCatalog.forLanguage(lang) ?: VoiceCatalog.all.first()
        setResult(TextToSpeech.LANG_AVAILABLE, Intent().putExtra("sampleText", v.sample))
        finish()
    }
}
