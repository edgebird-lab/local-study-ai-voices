// SPDX-FileCopyrightText: 2026 Robin Olbricht – Olbricht Digital (edgebird-lab)
// SPDX-License-Identifier: GPL-3.0-or-later

package de.edgebird.voices

import android.media.AudioFormat
import android.speech.tts.SynthesisCallback
import android.speech.tts.SynthesisRequest
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeechService
import android.speech.tts.Voice
import java.util.Locale

/**
 * Android-Sprachausgabe („Text-to-Speech-Engine“) mit den Offline-Stimmen von Local Study AI. Andere Apps (auch Local Study AI) sprechen sie über
 * die normale `TextToSpeech`-Schnittstelle an; sie ist keine App-eigene Funktion, sondern lässt sich in den Geräteeinstellungen als Standard wählen.
 */
class VoicesService : TextToSpeechService() {
    private val engine by lazy { VoiceEngine(applicationContext) }
    @Volatile private var stopped = false
    private var current: VoiceInfo? = VoiceCatalog.all.first()

    override fun onDestroy() { engine.release(); super.onDestroy() }

    private fun voiceFor(lang: String?, country: String?): VoiceInfo? = VoiceCatalog.forLanguage(lang)

    override fun onIsLanguageAvailable(lang: String?, country: String?, variant: String?): Int {
        val v = voiceFor(lang, country) ?: return TextToSpeech.LANG_NOT_SUPPORTED
        return if (!country.isNullOrEmpty() && !country.equals(v.locale.isO3Country, ignoreCase = true) && !country.equals(v.locale.country, ignoreCase = true)) TextToSpeech.LANG_AVAILABLE else TextToSpeech.LANG_COUNTRY_AVAILABLE
    }

    override fun onGetLanguage(): Array<String> { val l = (current ?: VoiceCatalog.all.first()).locale; return arrayOf(l.isO3Language, l.isO3Country, "") }

    override fun onLoadLanguage(lang: String?, country: String?, variant: String?): Int {
        val v = voiceFor(lang, country) ?: return TextToSpeech.LANG_NOT_SUPPORTED
        current = v
        return onIsLanguageAvailable(lang, country, variant)
    }

    override fun onGetVoices(): List<Voice> = VoiceCatalog.all.map { Voice(it.name, it.locale, Voice.QUALITY_HIGH, Voice.LATENCY_NORMAL, false, emptySet()) }

    override fun onIsValidVoiceName(name: String?) = if (VoiceCatalog.byName(name) != null) TextToSpeech.SUCCESS else TextToSpeech.ERROR

    override fun onLoadVoice(name: String?): Int { current = VoiceCatalog.byName(name) ?: return TextToSpeech.ERROR; return TextToSpeech.SUCCESS }

    override fun onGetDefaultVoiceNameFor(lang: String?, country: String?, variant: String?): String? = voiceFor(lang, country)?.name

    override fun onStop() { stopped = true }

    override fun onSynthesizeText(request: SynthesisRequest, callback: SynthesisCallback) {
        stopped = false
        val voice = VoiceCatalog.byName(request.voiceName) ?: voiceFor(request.language, request.country) ?: run { callback.error(TextToSpeech.ERROR_INVALID_REQUEST); return }
        val text = request.charSequenceText?.toString().orEmpty().trim()
        if (text.isEmpty()) { callback.done(); return }
        try {
            val tts = engine.tts(voice)
            val rate = tts.sampleRate()
            if (callback.start(rate, AudioFormat.ENCODING_PCM_16BIT, 1) != TextToSpeech.SUCCESS) { callback.error(); return }
            val speed = (request.speechRate / 100f).coerceIn(0.5f, 2.5f)
            val maxBytes = callback.maxBufferSize
            // Ein echtes Objekt statt eines Lambdas: die native Bibliothek sucht die Methode invoke(Object) und findet sie bei desugarten Lambdas nicht
            val sink = object : Function1<FloatArray, Int> {
                override fun invoke(samples: FloatArray): Int {
                    if (stopped) return 0
                    val pcm = ByteArray(samples.size * 2)
                    for (i in samples.indices) {
                        val v = (samples[i].coerceIn(-1f, 1f) * 32767f).toInt()
                        pcm[2 * i] = (v and 0xFF).toByte(); pcm[2 * i + 1] = ((v shr 8) and 0xFF).toByte()
                    }
                    var off = 0
                    while (off < pcm.size && !stopped) {
                        val n = minOf(maxBytes, pcm.size - off)
                        if (callback.audioAvailable(pcm, off, n) != TextToSpeech.SUCCESS) return 0
                        off += n
                    }
                    return if (stopped) 0 else 1
                }
            }
            tts.generateWithCallback(text, 0, speed, sink)
            if (!stopped) callback.done()
        } catch (e: Throwable) {
            callback.error()
        }
    }
}
