# Local Study AI Voices (Stimmen-App)

Kleine Zusatz-App zu [Local Study AI](https://play.google.com/store/apps/details?id=de.edgebird.lernsystem): eine **Android-Sprachausgabe (Text-to-Speech-Engine)** mit natürlichen Offline-Stimmen. Local Study AI nutzt sie automatisch, sobald sie installiert ist; sonst liest die Haupt-App mit den Offline-Stimmen des Geräts vor. Die Stimmen-App lässt sich auch in den Geräteeinstellungen (Sprachausgabe) als Standard wählen und funktioniert dann in jeder App.

- Stimmen: **Thorsten** (Deutsch, CC0, [Thorsten-Voice](https://github.com/thorstenMueller/Thorsten-Voice)) und **LJSpeech** (Englisch, gemeinfrei), Modelle im Piper-VITS-Format (MIT).
- Technik: [sherpa-onnx](https://github.com/k2-fsa/sherpa-onnx) (Apache-2.0) mit ONNX Runtime (MIT); die Aussprache erledigt **eSpeak NG (GPL-3.0+)**. Darum steht diese App unter der **GPL-3.0 oder später** und ihr Quelltext ist öffentlich. Local Study AI selbst ist eine eigene, getrennte App und spricht diese Engine nur über die normale Android-Schnittstelle (`TextToSpeech`) an.
- Keine Berechtigungen, kein Netzwerk, keine Daten verlassen das Gerät.

## Installieren (ohne Play Store)

1. Auf der [Release-Seite](https://github.com/edgebird-lab/local-study-ai-voices/releases/latest) die Datei `local-study-ai-voices-<Version>.apk` herunterladen (ca. 150 MB, am besten im WLAN).
2. Die heruntergeladene Datei antippen. Android fragt einmalig, ob der Browser Apps installieren darf: „Erlauben“ wählen und die Installation bestätigen.
3. Local Study AI öffnen: Unter „KI-Modelle“ steht dann „Installiert“, und die App liest mit diesen Stimmen vor. Beim ersten Vorlesen werden die Aussprachedaten einmalig entpackt.

Die Prüfsumme (SHA-256) steht in den Release-Notizen. Aktualisieren: einfach die neue APK desselben Schlüssels darüber installieren.

## Bauen

```bash
export ANDROID_HOME=$HOME/android-sdk
./gradlew :app:assembleDebug     # lädt beim ersten Mal sherpa-onnx und die Stimmenpakete (tools/fetch_*.sh, mit Prüfsummen)
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Release-Bundle: Upload-Schlüssel in `keystore.properties` (nicht im Repo) oder `-PkeystoreProps=/pfad`, dann `./gradlew :app:bundleRelease`.

## Aufbau

- `VoicesService` – die Engine (`TextToSpeechService`), Stimmenliste, Sprache wählen, Audio streamen.
- `VoiceEngine` – lädt sherpa-onnx mit den Modellen aus den Assets; entpackt die eSpeak-Daten einmalig.
- `MainActivity` – Statusseite mit Hörproben, Weg zur Auswahl als Standard-Sprachausgabe, Lizenzhinweise.
- `CheckVoiceData`, `GetSampleText` – von Android erwartete Hilfs-Activities.

Lizenz: GPL-3.0-or-later, siehe [LICENSE](LICENSE) und [NOTICE.md](NOTICE.md). Kontakt: kontakt@olbricht-digital.de
