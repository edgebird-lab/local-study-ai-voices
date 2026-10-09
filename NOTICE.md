# Lizenzen von Local Study AI Voices

Diese App steht unter der **GNU General Public License, Version 3 oder später** (siehe [LICENSE](LICENSE)). © 2026 Robin Olbricht – Olbricht Digital (edgebird-lab).
Der vollständige Quelltext liegt öffentlich unter https://github.com/edgebird-lab/local-study-ai-voices (jede Version hat ein Tag `v<Version>`).

| Komponente | Zweck | Lizenz |
|---|---|---|
| sherpa-onnx (k2-fsa), `Tts.kt` und `libsherpa-onnx-jni.so` (v1.13.8) | Ausführung der Stimmen, JNI-Anbindung | Apache-2.0 |
| ONNX Runtime (`libonnxruntime.so`) | Rechenkern | MIT |
| **eSpeak NG** (in `libsherpa-onnx-jni.so`, Fassung `csukuangfj/espeak-ng` Commit `ed530aa113046142eb5115cf2fc9157854d0ffe1`) und `espeak-ng-data` | Aussprache (Phonemisierung) | **GPL-3.0 oder später**, Quelltext: https://github.com/csukuangfj/espeak-ng/tree/ed530aa113046142eb5115cf2fc9157854d0ffe1 und https://github.com/espeak-ng/espeak-ng |
| Piper-VITS-Modelle im sherpa-onnx-Format (rhasspy/piper) | Stimmenmodelle | MIT |
| Stimme „Thorsten“ (Thorsten-Voice, Thorsten Müller) | Daten der deutschen Stimme | CC0 1.0 |
| Stimme „LJSpeech“ (LJ Speech Dataset) | Daten der englischen Stimme | gemeinfrei |

Die Stimmenpakete werden beim Bauen aus https://github.com/edgebird-lab/lernsystem-modelle (Releases v2 und v3) geladen und per SHA-256 geprüft (`tools/fetch_voices.sh`). Auf Wunsch schicken wir den Quelltext auch auf einem Datenträger oder per E-Mail (kontakt@olbricht-digital.de).
