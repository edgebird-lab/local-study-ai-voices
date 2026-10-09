// SPDX-FileCopyrightText: 2026 Robin Olbricht – Olbricht Digital (edgebird-lab)
// SPDX-License-Identifier: GPL-3.0-or-later

package de.edgebird.voices

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

/** Einfache Statusseite der Stimmen-App: was sie ist, Hörproben, Weg zur Auswahl als Standard-Sprachausgabe, Lizenz und Quelltext. */
class MainActivity : Activity() {
    private var tts: TextToSpeech? = null

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(20), dp(28), dp(20), dp(28)) }
        fun text(s: String, size: Float = 16f, bold: Boolean = false, color: Int = Color.DKGRAY) = TextView(this).apply {
            text = s; textSize = size; setTextColor(color); setPadding(0, dp(6), 0, dp(6)); if (bold) setTypeface(typeface, android.graphics.Typeface.BOLD)
        }
        fun button(label: String, onClick: () -> Unit) = Button(this).apply { text = label; setOnClickListener { onClick() }; isAllCaps = false }
        root.addView(text(getString(R.string.intro)))
        val status = text(getString(R.string.status_checking), 15f, true)
        root.addView(status)
        for (v in VoiceCatalog.all) root.addView(button(getString(R.string.listen, v.title)) { speak(v) })
        root.addView(button(getString(R.string.choose_default)) {
            val i = Intent("com.android.settings.TTS_SETTINGS")
            if (!runCatching { startActivity(i) }.isSuccess) runCatching { startActivity(Intent(android.provider.Settings.ACTION_SETTINGS)) }
        })
        root.addView(text(getString(R.string.privacy)))
        root.addView(text(getString(R.string.license), 13f))
        root.addView(button(getString(R.string.source)) { runCatching { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/edgebird-lab/local-study-ai-voices"))) } })
        setContentView(ScrollView(this).apply { addView(root); post { scrollTo(0, 0) } })
        val engine = VoiceEngine(applicationContext)
        status.text = if (VoiceCatalog.all.all { engine.isReady(it) }) getString(R.string.status_ready) else getString(R.string.status_missing)
    }

    private fun speak(v: VoiceInfo) {
        tts?.shutdown()
        tts = TextToSpeech(applicationContext, { st ->
            if (st == TextToSpeech.SUCCESS) { tts?.language = v.locale; tts?.speak(v.sample, TextToSpeech.QUEUE_FLUSH, null, "sample") }
        }, packageName)
    }

    override fun onDestroy() { tts?.shutdown(); super.onDestroy() }
}
