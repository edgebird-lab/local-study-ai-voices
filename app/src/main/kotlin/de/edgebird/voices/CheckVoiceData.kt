// SPDX-FileCopyrightText: 2026 Robin Olbricht – Olbricht Digital (edgebird-lab)
// SPDX-License-Identifier: GPL-3.0-or-later

package de.edgebird.voices

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech

/** Antwort auf `ACTION_CHECK_TTS_DATA`: nennt die verfügbaren Stimmen (alle sind eingebaut, es gibt nichts nachzuladen). */
class CheckVoiceData : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val available = ArrayList(VoiceCatalog.all.map { "${it.locale.isO3Language}-${it.locale.isO3Country}" })
        setResult(TextToSpeech.Engine.CHECK_VOICE_DATA_PASS, Intent().putStringArrayListExtra(TextToSpeech.Engine.EXTRA_AVAILABLE_VOICES, available).putStringArrayListExtra(TextToSpeech.Engine.EXTRA_UNAVAILABLE_VOICES, ArrayList()))
        finish()
    }
}
