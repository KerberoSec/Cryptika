// app/build.gradle.kts
// Cryptika v1.0.0
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
    kotlin("kapt")
}
// NOTE: kapt is used exclusively for Hilt. KSP is used exclusively for Room.
// Do NOT mix ksp() and kapt() for the same annotation processor.

android {
    namespace = "com.cryptika.messenger"
    compileSdk = 34

    // APK output name
    setProperty("archivesBaseName", "Cryptika")

    defaultConfig {
        applicationId = "com.cryptika.messenger"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }

        // ⚠️  REPLACE BEFORE RUNNING:
        // 1. Start the relay server: cd server && node index.js
        // 2. Copy the printed server public key hex below
        // 3. Replace 192.168.1.X with your machine's LAN IP
        // 4. Use ws:// and http:// for local dev (Node.js runs plain HTTP by default)
        //    Use wss:// and https:// only if you add TLS to the server
        buildConfigField(
            "String",
            "SERVER_PUBLIC_KEY_HEX",
            "\"c402f7a9ec0f9e45f5cc0a1abc0342e36d57841e28bca0bffb60d327a529297d\""
        )
        // LAN IP for release / physical device builds
        buildConfigField("String", "RELAY_BASE_URL", "\"ws://13.235.150.23:8443\"")
        buildConfigField("String", "API_BASE_URL", "\"http://13.235.150.23:8443\"")
        buildConfigField("String", "APP_VERSION", "\"1.0.0\"")
        // Safe fallback: debug buildType overrides this to "true"; release to "false".
        // Network security config (domain-config) is the real authority for cleartext.
        manifestPlaceholders["usesCleartextTraffic"] = "false"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            // Release: TLS-only. Upgrade plaintext build-config URLs to secure schemes.
            manifestPlaceholders["usesCleartextTraffic"] = "false"
            buildConfigField("String", "RELAY_BASE_URL", "\"wss://13.235.150.23:8443\"")
            buildConfigField("String", "API_BASE_URL",   "\"https://13.235.150.23:8443\"")
        }
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            // Cleartext allowed: EC2 server runs plain ws:// and http:// in dev.
            manifestPlaceholders["usesCleartextTraffic"] = "true"
            // Uses RELAY_BASE_URL and API_BASE_URL from defaultConfig (plain ws/http)
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.8" }
    packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
}

dependencies {
    // Cryptography
    implementation("org.bouncycastle:bcprov-jdk15on:1.70")

    // Secure Storage
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Hilt DI
    implementation("com.google.dagger:hilt-android:2.51")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation("androidx.hilt:hilt-work:1.2.0")
    kapt("com.google.dagger:hilt-android-compiler:2.51")
    kapt("androidx.hilt:hilt-compiler:1.2.0")

    // Room + SQLCipher
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    implementation("net.zetetic:android-database-sqlcipher:4.5.4")
    implementation("androidx.sqlite:sqlite-ktx:2.4.0")
    // Networking
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // WebRTC
    // Note: org.webrtc:google-webrtc is included via direct AAR if needed
    // implementation("org.webrtc:google-webrtc:1.0.32006")

    // QR Code
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Camera (QR scanning)
    implementation("androidx.camera:camera-camera2:1.3.1")
    implementation("androidx.camera:camera-lifecycle:1.3.1")
    implementation("androidx.camera:camera-view:1.3.1")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("io.mockk:mockk:1.13.9")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}

kapt { correctErrorTypes = true }

// Room schema export location (required when exportSchema = true in @Database)
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
}

// Custom task for running call tests
tasks.register("testCallFlow") {
    description = "Run Cryptika call flow tests"
    group = "verification"
    
    doLast {
        // Run Kotlin test with Espresso
        println("Running Cryptika call flow tests...")
        println("Tests: CallFlowTest (UI automation)")
        
        val testCmd = if (System.getProperty("os.name").lowercase().contains("windows")) {
            "cmd /c gradlew connectedAndroidTest --tests com.cryptika.messenger.ui.CallFlowTest"
        } else {
            "./gradlew connectedAndroidTest --tests com.cryptika.messenger.ui.CallFlowTest"
        }
        
        exec {
            commandLine = if (System.getProperty("os.name").lowercase().contains("windows")) {
                listOf("cmd", "/c", testCmd)
            } else {
                listOf("/bin/sh", "-c", testCmd)
            }
        }
    }
}

tasks.register("testCallFlowPython") {
    description = "Run Python-based call flow tests (requires emulators)"
    group = "verification"
    
    doLast {
        println("Running Python call flow tests...")
        val pythonCmd = "python ../test_cryptika_call.py emulator-5554 emulator-5556 com.cryptika.messenger.debug"
        
        exec {
            commandLine = if (System.getProperty("os.name").lowercase().contains("windows")) {
                listOf("cmd", "/c", pythonCmd)
            } else {
                listOf("/bin/sh", "-c", pythonCmd)
            }
            isIgnoreExitValue = true
        }
    }
}

tasks.register("collectTestLogs") {
    description = "Collect ADB logs from test devices"
    group = "verification"
    
    doLast {
        println("Collecting logs from test devices...")
        
        val logsDir = File("${project.buildDir}/test-logs")
        logsDir.mkdirs()
        
        // Device 1 logs
        exec {
            commandLine = if (System.getProperty("os.name").lowercase().contains("windows")) {
                listOf("cmd", "/c", "adb -s emulator-5554 logcat -d > ${logsDir}/device1.txt")
            } else {
                listOf("/bin/sh", "-c", "adb -s emulator-5554 logcat -d > ${logsDir}/device1.txt")
            }
            isIgnoreExitValue = true
        }
        
        // Device 2 logs
        exec {
            commandLine = if (System.getProperty("os.name").lowercase().contains("windows")) {
                listOf("cmd", "/c", "adb -s emulator-5556 logcat -d > ${logsDir}/device2.txt")
            } else {
                listOf("/bin/sh", "-c", "adb -s emulator-5556 logcat -d > ${logsDir}/device2.txt")
            }
            isIgnoreExitValue = true
        }
        
        println("✓ Logs collected to ${logsDir}/")
    }
}
