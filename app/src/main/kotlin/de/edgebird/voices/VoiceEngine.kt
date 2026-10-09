// SPDX-FileCopyrightText: 2026 Robin Olbricht – Olbricht Digital (edgebird-lab)
// SPDX-License-Identifier: GPL-3.0-or-later

package de.edgebird.voices

import android.content.Context
import com.k2fsa.sherpa.onnx.OfflineTts
import com.k2fsa.sherpa.onnx.OfflineTtsConfig
import com.k2fsa.sherpa.onnx.OfflineTtsModelConfig
import com.k2fsa.sherpa.onnx.OfflineTtsVitsModelConfig
import java.io.File

/**
 * Lädt die Piper-Stimmen (VITS über sherpa-onnx) und erzeugt Sprache. Alles läuft auf dem Gerät; die App hat keine Netzwerkberechtigung.
 * Die Modelle werden direkt aus den Assets gelesen; die eSpeak-NG-Daten (Aussprachewörterbücher) werden beim ersten Start einmalig entpackt.
 */
class VoiceEngine(private val context: Context) {
    private val loaded = HashMap<String, OfflineTts>()

    private val dataDir: File get() = File(context.filesDir, "espeak-ng-data")
    private val stamp: File get() = File(context.filesDir, "espeak-ng-data.stamp")

    private fun versionStamp(): String = runCatching { context.packageManager.getPackageInfo(context.packageName, 0).lastUpdateTime.toString() }.getOrDefault("0")

    /** Entpackt die Aussprachedaten, wenn sie fehlen oder von einer älteren App-Version stammen. */
    @Synchronized fun ensureData() {
        if (stamp.exists() && stamp.readText() == versionStamp() && File(dataDir, "phondata").exists()) return
        dataDir.deleteRecursively()
        copyAssetDir("espeak-ng-data", dataDir)
        stamp.writeText(versionStamp())
    }

    private fun copyAssetDir(assetPath: String, target: File) {
        val children = context.assets.list(assetPath).orEmpty()
        if (children.isEmpty()) {
            target.parentFile?.mkdirs()
            context.assets.open(assetPath).use { input -> target.outputStream().use { input.copyTo(it, 64 * 1024) } }
        } else {
            target.mkdirs()
            for (c in children) copyAssetDir("$assetPath/$c", File(target, c))
        }
    }

    /** Bereit zum Sprechen? (Modell vorhanden und Daten entpackt.) */
    fun isReady(voice: VoiceInfo) = runCatching { context.assets.list("voices/${voice.dir}").orEmpty().contains("model.onnx") }.getOrDefault(false)

    @Synchronized fun tts(voice: VoiceInfo): OfflineTts {
        loaded[voice.name]?.let { return it }
        ensureData()
        // nur eine Stimme gleichzeitig im Speicher halten (je ca. 100 MB)
        loaded.values.forEach { it.release() }; loaded.clear()
        val base = "voices/${voice.dir}"
        val t = OfflineTts(
            context.assets,
            OfflineTtsConfig(
                model = OfflineTtsModelConfig(
                    vits = OfflineTtsVitsModelConfig(
                        model = "$base/model.onnx", tokens = "$base/tokens.txt", dataDir = dataDir.path,
                        // etwas ruhiger und gleichmäßiger als die Voreinstellung; Lernstoff soll klar zu verstehen sein
                        noiseScale = 0.6f, noiseScaleW = 0.7f, lengthScale = 1.0f,
                    ),
                    numThreads = 3, provider = "cpu",
                ),
                silenceScale = 0.2f,
            ),
        )
        loaded[voice.name] = t
        return t
    }

    @Synchronized fun release() { loaded.values.forEach { it.release() }; loaded.clear() }
}
