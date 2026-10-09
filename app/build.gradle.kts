// SPDX-FileCopyrightText: 2026 Robin Olbricht – Olbricht Digital (edgebird-lab)
// SPDX-License-Identifier: GPL-3.0-or-later

import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "de.edgebird.voices"
    compileSdk = 37

    defaultConfig {
        applicationId = "de.edgebird.voices"
        minSdk = 31
        targetSdk = 37
        versionCode = 1
        versionName = "0.1.0"
        // sherpa-onnx liegt nur für 64-Bit-ARM vor
        ndk { abiFilters += "arm64-v8a" }
    }

    // Upload-Schlüssel (nicht im Repo): -PkeystoreProps=/pfad/zur/keystore.properties oder keystore.properties im Projektordner
    val keystoreProps = Properties().apply {
        val f = (project.findProperty("keystoreProps") as String?)?.let { File(it) } ?: rootProject.file("keystore.properties")
        f.takeIf { it.exists() }?.inputStream()?.use { load(it) }
    }
    signingConfigs {
        if (keystoreProps.isNotEmpty()) create("upload") {
            storeFile = file(keystoreProps.getProperty("storeFile")); storePassword = keystoreProps.getProperty("storePassword")
            keyAlias = keystoreProps.getProperty("keyAlias"); keyPassword = keystoreProps.getProperty("keyPassword")
        }
    }

    // Die Stimmenmodelle werden direkt aus dem Asset gelesen: nicht erneut komprimieren
    androidResources { noCompress += listOf("onnx", "txt") }
    bundle { language { enableSplit = false } }
    packaging { jniLibs { useLegacyPackaging = false } }

    buildTypes {
        release {
            signingConfigs.findByName("upload")?.let { signingConfig = it }
            (project.findProperty("appIdSuffix") as String?)?.let { applicationIdSuffix = it }
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug { (project.findProperty("debugIdSuffix") as String?)?.let { applicationIdSuffix = it } }
    }
}

// Native Bibliotheken (sherpa-onnx, ONNX Runtime) und Stimmenmodelle werden bei Bedarf geladen (tools/fetch_*.sh); sie liegen nicht im Repo
val fetchSherpa by tasks.registering(Exec::class) {
    val marker = layout.projectDirectory.file("src/main/jniLibs/arm64-v8a/libsherpa-onnx-jni.so").asFile
    onlyIf { !marker.exists() }
    commandLine("bash", rootProject.file("tools/fetch_sherpa.sh").absolutePath)
}
val fetchVoices by tasks.registering(Exec::class) {
    val marker = layout.projectDirectory.file("src/main/assets/voices/de/model.onnx").asFile
    onlyIf { !marker.exists() }
    commandLine("bash", rootProject.file("tools/fetch_voices.sh").absolutePath)
}
tasks.named("preBuild") { dependsOn(fetchSherpa, fetchVoices) }
