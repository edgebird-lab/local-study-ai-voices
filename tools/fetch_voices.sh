#!/usr/bin/env bash
# SPDX-FileCopyrightText: 2026 Robin Olbricht – Olbricht Digital (edgebird-lab)
# SPDX-License-Identifier: GPL-3.0-or-later
# Holt die Stimmenpakete (Piper-VITS im sherpa-onnx-Format, Stimmen „Thorsten“ CC0 und „LJSpeech“ gemeinfrei, plus die Sprachdaten von eSpeak NG, GPL-3.0+)
# aus dem öffentlichen Modell-Repo und legt sie nach app/src/main/assets. Prüft die SHA-256-Summen.
set -euo pipefail
cd "$(dirname "$0")/.."
BASE="https://github.com/edgebird-lab/lernsystem-modelle/releases/download"
DE_URL="$BASE/v2/voice-de-thorsten-medium.zip"; DE_SHA="e879f39e539a3d6bc1a083826e1e25c463711e19b59b8795a1b450545c34fd86"
EN_URL="$BASE/v3/voice-en-ljspeech-medium.zip"; EN_SHA="e79dac3d7eaa74cb45d43d56032db6a60635ac5a6b90543dead59ded42a12650"
A="app/src/main/assets"
TMP="$(mktemp -d)"; trap 'rm -rf "$TMP"' EXIT
fetch() { # url sha kürzel (de|en)
  local f="$TMP/$(basename "$1")"
  echo "Lade $(basename "$1") …"; curl -fL --retry 3 -o "$f" "$1"
  [ "$(sha256sum "$f" | cut -d' ' -f1)" = "$2" ] || { echo "Prüfsumme stimmt nicht: $1" >&2; exit 1; }
  mkdir -p "$TMP/x-$3" "$A/voices/$3"; unzip -q -o "$f" -d "$TMP/x-$3"; cp "$TMP/x-$3/model.onnx" "$TMP/x-$3/tokens.txt" "$A/voices/$3/"
}
fetch "$DE_URL" "$DE_SHA" de
fetch "$EN_URL" "$EN_SHA" en
# eSpeak-NG-Daten: die Deutsch-Daten enthalten auch Englisch und genügen für beide Stimmen
rm -rf "$A/espeak-ng-data"; cp -r "$TMP/x-de/espeak-ng-data" "$A/espeak-ng-data"
echo "Stimmen bereit."
