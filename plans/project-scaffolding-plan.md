# Project Scaffolding & Multi-Module Setup Plan

## 1. Version Catalog (`gradle/libs.versions.toml`)

Proposed content:

```toml
[versions]
agp = "9.1.0"
kotlin = "2.0.21"
compose-compiler = "1.5.12"
compose-bom = "2025.01.00"
core-ktx = "1.13.0"
appcompat = "1.6.1"
material = "1.11.0"
junit = "4.13.2"
junitVersion = "1.1.5"
espressoCore = "3.5.1"
hilt = "2.51"
room = "2.6.1"
datastore = "1.0.0"
lifecycle = "2.8.0"
work = "2.9.0"
cameraX = "1.4.0"
biometric = "1.2.0"
retrofit = "2.11.0"
okhttp = "4.12.0"
moshi = "1.15.1"
web3j = "4.10.1"
walletconnect-bom = "1.10.0"
kethereum = "0.86.0"
tink = "1.12.0"
coil = "2.6.0"
lottie = "6.4.0"
vico = "1.13.0"
mlkit-barcode = "17.2.0"
accompanist = "0.34.0"
shimmer = "1.2.0"
junit-jupiter = "5.10.0"
mockk = "1.13.10"
turbine = "1.1.0"
truth = "1.1.5"
compose-ui-test = "1.6.0"
robolectric = "4.12.1"
leakcanary = "2.13.0"
detekt = "1.23.6"
ktlint = "0.50.0"
spotless = "6.25.0"
kotlinx-serialization = "1.6.3"
kotlinx-collections-immutable = "0.3.7"
navigation-compose = "2.8.0"
hilt-navigation-compose = "1.2.0"
lifecycle-viewmodel-compose = "2.8.0"
zxing = "3.5.3"

[libraries]
# AndroidX Core
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "core-ktx" }
androidx-appcompat = { group = "androidx.appcompat", name = "appcompat", version.ref = "appcompat" }

# Material
material = { group = "com.google.android.material", name = "material", version.ref = "material" }

# Compose BOM
compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "compose-bom" }
# Individual compose libraries (use via BOM, but we can still reference for explicit version if needed)
androidx-compose-ui = { group = "androidx.compose.ui", name = "ui" }
androidx-compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
androidx-compose-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
androidx-compose-foundation = { group = "androidx.compose.foundation", name = "foundation" }
androidx-compose-material3 = { group = "androidx.compose.material3", name = "material3" }
androidx-compose-material-icons-extended = { group = "androidx.compose.material", name = "material-icons-extended" }

# Compose Navigation
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigation-compose" }
hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version.ref = "hilt-navigation-compose" }

# Lifecycle
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycle-viewmodel-compose" }
androidx-lifecycle-runtime-compose = { group = "androidx.lifecycle", name = "lifecycle-runtime-compose" }
androidx-lifecycle-viewmodel-ktx = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-ktx", version.ref = "lifecycle" }

# Hilt
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-compiler", version.ref = "hilt" }

# Room
androidx-room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
androidx-room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
androidx-room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }

# DataStore
androidx-datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastore" }

# WorkManager
androidx-work-runtime-ktx = { group = "androidx.work", name = "work-runtime-ktx", version.ref = "work" }

# CameraX
androidx-camera-core = { group = "androidx.camera", name = "camera-core", version.ref = "cameraX" }
androidx-camera-camera2 = { group = "androidx.camera", name = "camera-camera2", version.ref = "cameraX" }
androidx-camera-lifecycle = { group = "androidx.camera", name = "camera-lifecycle", version.ref = "cameraX" }
androidx-camera-view = { group = "androidx.camera", name = "camera-view", version.ref = "cameraX" }

# Biometric
androidx-biometric = { group = "androidx.biometric", name = "biometric", version.ref = "biometric" }

# Networking
retrofit = { group = "com.squareup.retrofit2", name = "retrofit", version.ref = "retrofit" }
retrofit-converter-moshi = { group = "com.squareup.retrofit2", name = "converter-moshi", version.ref = "retrofit" }
okhttp = { group = "com.squareup.okhttp3", name = "okhttp", version.ref = "okhttp" }
okhttp-logging-interceptor = { group = "com.squareup.okhttp3", name = "logging-interceptor", version.ref = "okhttp" }
moshi = { group = "com.squareup.moshi", name = "moshi", version.ref = "moshi" }
moshi-kotlin-codegen = { group = "com.squareup.moshi", name = "moshi-kotlin-codegen", version.ref = "moshi" }

# Blockchain
web3j-android = { group = "org.web3j", name = "core", version.ref = "web3j" }
walletconnect-bom = { group = "com.walletconnect", name = "android-bom", version.ref = "walletconnect-bom" }
walletconnect-android-core = { group = "com.walletconnect", name = "android-core" }
walletconnect-web3wallet = { group = "com.walletconnect", name = "web3wallet" }
kethereum = { group = "com.github.komputing.kethereum", name = "bip39", version.ref = "kethereum" }

# Security
tink-android = { group = "com.google.crypto.tink", name = "tink-android", version.ref = "tink" }

# UI & Media
coil-compose = { group = "io.coil-kt", name = "coil-compose", version.ref = "coil" }
lottie-compose = { group = "com.airbnb.android", name = "lottie-compose", version.ref = "lottie" }
vico-compose = { group = "com.patrykandpatrick.vico", name = "compose", version.ref = "vico" }
mlkit-barcode-scanning = { group = "com.google.mlkit", name = "barcode-scanning", version.ref = "mlkit-barcode" }
accompanist-permissions = { group = "com.google.accompanist", name = "accompanist-permissions", version.ref = "accompanist" }
accompanist-systemuicontroller = { group = "com.google.accompanist", name = "accompanist-systemuicontroller", version.ref = "accompanist" }
shimmer = { group = "com.facebook.shimmer", name = "shimmer", version.ref = "shimmer" }

# Kotlin Extensions
kotlinx-serialization-json = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version.ref = "kotlinx-serialization" }
kotlinx-collections-immutable = { group = "org.jetbrains.kotlinx", name = "kotlinx-collections-immutable", version.ref = "kotlinx-collections-immutable" }

# Testing
junit = { group = "junit", name = "junit", version.ref = "junit" }
androidx-junit = { group = "androidx.test.ext", name = "junit", version.ref = "junitVersion" }
androidx-espresso-core = { group = "androidx.test.espresso", name = "espresso-core", version.ref = "espressoCore" }
junit-jupiter = { group = "org.junit.jupiter", name = "junit-jupiter", version.ref = "junit-jupiter" }
mockk = { group = "io.mockk", name = "mockk", version.ref = "mockk" }
mockk-android = { group = "io.mockk", name = "mockk-android", version.ref = "mockk" }
turbine = { group = "app.cash.turbine", name = "turbine", version.ref = "turbine" }
truth = { group = "com.google.truth", name = "truth", version.ref = "truth" }
androidx-compose-ui-test = { group = "androidx.compose.ui", name = "ui-test", version.ref = "compose-ui-test" }
androidx-compose-ui-test-junit4 = { group = "androidx.compose.ui", name = "ui-test-junit4", version.ref = "compose-ui-test" }
androidx-compose-ui-test-manifest = { group = "androidx.compose.ui", name = "ui-test-manifest", version.ref = "compose-ui-test" }
hilt-android-testing = { group = "com.google.dagger", name = "hilt-android-testing", version.ref = "hilt" }
robolectric = { group = "org.robolectric", name = "robolectric", version.ref = "robolectric" }
leakcanary-android = { group = "com.squareup.leakcanary", name = "leakcanary-android", version.ref = "leakcanary" }

# Code Quality
detekt = { group = "io.gitlab.arturbosch.detekt", name = "detekt", version.ref = "detekt" }
ktlint = { group = "org.jlleitschuh.gradle", name = "ktlint-gradle", version.ref = "ktlint" }
spotless = { group = "com.diffplug.spotless", name = "spotless-plugin-gradle", version.ref = "spotless" }

[bundles]
compose = [
    "androidx-compose-ui",
    "androidx-compose-ui-tooling",
    "androidx-compose-ui-tooling-preview",
    "androidx-compose-foundation",
    "androidx-compose-material3",
    "androidx-compose-material-icons-extended",
    "androidx-lifecycle-runtime-compose"
]
compose-navigation = [
    "androidx-navigation-compose",
    "hilt-navigation-compose"
]
compose-testing = [
    "androidx-compose-ui-test",
    "androidx-compose-ui-test-junit4",
    "androidx-compose-ui-test-manifest"
]
networking = [
    "retrofit",
    "retrofit-converter-moshi",
    "okhttp",
    "okhttp-logging-interceptor",
    "moshi",
    "moshi-kotlin-codegen"
]
blockchain = [
    "web3j-android",
    "kethereum"
]
camerax = [
    "androidx-camera-core",
    "androidx-camera-camera2",
    "androidx-camera-lifecycle",
    "androidx-camera-view"
]
testing = [
    "junit-jupiter",
    "mockk",
    "turbine",
    "truth",
    "robolectric"
]
debug = [
    "leakcanary-android"
]

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
android-library = { id = "com.android.library", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-kapt = { id = "org.jetbrains.kotlin.kapt", version.ref = "kotlin" }
kotlinx-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
hilt-android = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
detekt = { id = "io.gitlab.arturbosch.detekt", version.ref = "detekt" }
ktlint = { id = "org.jlleitschuh.gradle.ktlint", version.ref = "ktlint" }
spotless = { id = "com.diffplug.spotless", version.ref = "spotless" }
```

## 2. Module Structure

Create the following module directories:

```
core/
├── core-ui/
├── core-common/
├── core-network/
├── core-database/
├── core-datastore/
└── core-security/
domain/
data/
feature/
├── feature-onboarding/
├── feature-home/
├── feature-tokens/
├── feature-send/
├── feature-receive/
├── feature-history/
├── feature-dapp/
├── feature-nft/
├── feature-swap/
└── feature-settings/
```

Each module will have:
- `build.gradle.kts` (Android library or application plugin)
- `src/main/java/com/nexvault/wallet/<module>/` (source directory)
- `src/main/AndroidManifest.xml` (minimal `<manifest>` tag)
- `src/main/res/` (optional, empty)

## 3. Dependency Rules

Follow Clean Architecture:

- `core/*` modules have no dependency on `domain`, `data`, or `feature/*`
- `domain` depends only on `core-common`
- `data` depends on `domain`, `core-network`, `core-database`, `core-datastore`, `core-security`, `core-common`
- `feature/*` modules depend on `domain`, `core-ui`, `core-common` (never on `data` directly)
- `app` depends on ALL modules (wires everything with Hilt)

### Module-specific dependencies:

- All modules: Hilt (kapt/ksp)
- `core-ui`: Compose BOM, Material 3, Coil Compose, Lottie Compose, Vico, kotlinx-collections-immutable
- `core-network`: Retrofit, OkHttp, Moshi, Web3j
- `core-database`: Room (with kapt/ksp for annotation processing)
- `core-datastore`: DataStore Preferences
- `core-security`: Tink, AndroidX Biometric, Web3j (for BIP39/BIP44)
- `feature/*`: Compose BOM, Material 3, Navigation Compose, Hilt Navigation Compose, Lifecycle ViewModel Compose
- `app`: all feature modules, data module, all core modules, Hilt Android

## 4. Settings.gradle.kts Updates

Add the following includes:

```kotlin
include(":app")
include(":core:core-ui")
include(":core:core-common")
include(":core:core-network")
include(":core:core-database")
include(":core:core-datastore")
include(":core:core-security")
include(":domain")
include(":data")
include(":feature:feature-onboarding")
include(":feature:feature-home")
include(":feature:feature-tokens")
include(":feature:feature-send")
include(":feature:feature-receive")
include(":feature:feature-history")
include(":feature:feature-dapp")
include(":feature:feature-nft")
include(":feature:feature-swap")
include(":feature:feature-settings")
```

## 5. Root build.gradle.kts Configuration

Add Detekt and Spotless plugins:

```kotlin
plugins {
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.spotless) apply false
}

subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "com.diffplug.spotless")

    // Configure detekt, ktlint, spotless as needed
}
```

## 6. App Module Configuration

Update `app/build.gradle.kts`:

- Set applicationId `com.nexvault.wallet`
- minSdk 26, targetSdk 34
- Enable Compose with Kotlin compiler plugin
- Enable buildConfig
- Read API keys from `local.properties` and expose as BuildConfig fields:
  - INFURA_API_KEY
  - ALCHEMY_API_KEY
  - COINGECKO_API_KEY
  - WALLETCONNECT_PROJECT_ID
- Enable R8/ProGuard for release builds
- Dependencies: include all feature modules, data, core modules, Hilt

## 7. NexVaultApplication.kt

Create `app/src/main/java/com/nexvault/wallet/NexVaultApplication.kt`:

```kotlin
package com.nexvault.wallet

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NexVaultApplication : Application()
```

## 8. MainActivity.kt

Create `app/src/main/java/com/nexvault/wallet/MainActivity.kt`:

```kotlin
package com.nexvault.wallet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Text("NexVault")
                }
            }
        }
    }
}
```

## 9. Validation Steps

- Run `./gradlew assembleDebug` to ensure compilation succeeds
- Verify all modules appear in Android Studio project view
- Run `./gradlew detekt` (may have zero source files to scan)
- Build and run on an emulator to see "NexVault" text

## 10. Next Steps

Once the plan is approved, switch to **Code** mode to implement the changes.