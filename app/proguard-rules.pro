# SPDX-FileCopyrightText: 2026 Robin Olbricht – Olbricht Digital (edgebird-lab)
# SPDX-License-Identifier: GPL-3.0-or-later
# sherpa-onnx wird aus nativem Code über Klassen- und Feldnamen angesprochen: nicht umbenennen
-keep class com.k2fsa.sherpa.onnx.** { *; }
# Der Rückruf, den sherpa-onnx aus nativem Code aufruft (Function1.invoke), darf weder umbenannt noch wegoptimiert werden
-keep class de.edgebird.voices.** implements kotlin.jvm.functions.Function1 { *; }
-keep class kotlin.jvm.functions.Function1 { *; }
