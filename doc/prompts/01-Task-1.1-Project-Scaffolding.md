# Prompt: Project Scaffolding & Multi-Module Setup

Refer to the following documentation files for full context:
- `/doc/01-PROJECT-OVERVIEW.md` — project structure, module layout, environment setup
- `/doc/02-ARCHITECTURE-AND-TECH-STACK.md` — all dependencies and versions

## What I need you to do:

### 1. Create the Version Catalog
Create `gradle/libs.versions.toml` with ALL dependencies listed in
`02-ARCHITECTURE-AND-TECH-STACK.md`, including:

- Kotlin 2.0+, Compose BOM (latest stable), Material 3
- Hilt + Hilt Navigation Compose
- Room, DataStore Preferences
- Retrofit 2, OkHttp 4, Moshi (kotlin-codegen)
- Web3j Android
- WalletConnect v2 BOM (android-core + web3wallet)
- Coil Compose
- Vico (chart library)
- ZXing (QR generation)
- CameraX + ML Kit Barcode
- AndroidX Biometric
- Google Tink (encryption)
- kotlinx-collections-immutable
- Lottie Compose
- WorkManager
- Navigation Compose (type-safe)
- Lifecycle ViewModel Compose
- JUnit 5, Mockk, Turbine, Truth, Compose UI Test, Robolectric, Hilt Testing
- LeakCanary (debug only)
- Detekt, ktlint/Spotless

Group versions at the top, then libraries, then bundles for common groups
(e.g., compose, testing, networking).

### 2. Create All Modules

Create the following module directories with their own `build.gradle.kts`:

```
core/core-ui/
core/core-common/
core/core-network/
core/core-database/
core/core-datastore/
core/core-security/
domain/
data/
feature/feature-onboarding/
feature/feature-home/
feature/feature-tokens/
feature/feature-send/
feature/feature-receive/
feature/feature-history/
feature/feature-dapp/
feature/feature-nft/
feature/feature-swap/
feature/feature-settings/
```

Each module should have:
- A `build.gradle.kts` with the correct plugin (Android library or application)
- A `src/main/java/com/nexvault/wallet/<module>/` source directory
- A `src/main/AndroidManifest.xml` (minimal, just the `<manifest>` tag)

### 3. Module Dependency Rules

Follow Clean Architecture dependency rules:
- `core/*` modules have no dependency on `domain`, `data`, or `feature/*`
- `domain` depends only on `core-common`
- `data` depends on `domain`, `core-network`, `core-database`, `core-datastore`, `core-security`, `core-common`
- `feature/*` modules depend on `domain`, `core-ui`, `core-common` (never on `data` directly)
- `app` depends on ALL modules (wires everything together with Hilt)

Apply these dependencies:
- All modules: Hilt (kapt or ksp)
- `core-ui`: Compose BOM, Material 3, Coil Compose, Lottie Compose, Vico, kotlinx-collections-immutable
- `core-network`: Retrofit, OkHttp, Moshi, Web3j
- `core-database`: Room (with kapt/ksp for annotation processing)
- `core-datastore`: DataStore Preferences
- `core-security`: Tink, AndroidX Biometric, Web3j (for BIP39/BIP44)
- `feature/*`: Compose BOM, Material 3, Navigation Compose, Hilt Navigation Compose, Lifecycle ViewModel Compose
- `app`: all feature modules, data module, all core modules, Hilt Android

### 4. Register Modules in settings.gradle.kts

Add all modules to `settings.gradle.kts` using `include()`.

### 5. Configure the app module

In `app/build.gradle.kts`:
- Set applicationId to `com.nexvault.wallet`
- Set minSdk 26, targetSdk 34
- Enable Compose with the Kotlin compiler plugin
- Enable buildConfig
- Read API keys from `local.properties` and expose as BuildConfig fields:
  INFURA_API_KEY, ALCHEMY_API_KEY, COINGECKO_API_KEY, WALLETCONNECT_PROJECT_ID
- Enable R8/ProGuard for release builds

### 6. Create NexVaultApplication

In `app/src/main/java/com/nexvault/wallet/NexVaultApplication.kt`:
- Annotate with `@HiltAndroidApp`
- Empty body for now

### 7. Create MainActivity

In `app/src/main/java/com/nexvault/wallet/MainActivity.kt`:
- Annotate with `@AndroidEntryPoint`
- `setContent` with a placeholder `Text("NexVault")` wrapped in a basic MaterialTheme

### 8. Configure Detekt and Spotless

In the root `build.gradle.kts`, add Detekt and Spotless plugins configured
for the entire project.

## Acceptance:
- `./gradlew assembleDebug` compiles with zero errors
- All modules are recognized in Android Studio project view
- `./gradlew detekt` runs (may have zero source files to scan, that's OK)
- The app launches and shows "NexVault" text on an emulator
```

---

## After Prompt 1 succeeds, here's your full prompt sequence:

```
Prompt 1:  Project Scaffolding (above — you are here)
Prompt 2:  Design System & Theme (Task 1.2)
Prompt 3:  Security Module (Task 1.3)
Prompt 4:  DataStore & Preferences (Task 1.4)
Prompt 5:  Domain Models & Repository Interfaces (Task 1.5)
Prompt 6:  Data Layer — Repository Implementations (Task 1.6)
Prompt 7:  Onboarding Feature — Screens & ViewModels (Task 1.7)
Prompt 8:  Auth / Unlock Feature (Task 1.8)
Prompt 9:  Main Scaffold & Navigation Shell (Task 1.9)
Prompt 10: Network Module — Retrofit, Web3j, APIs (Task 2.1)
Prompt 11: Database Module — Room Entities & DAOs (Task 2.2)
Prompt 12: Chain Management (Task 2.3)
Prompt 13: Home Dashboard — Token Balances & Chart (Task 2.4)
Prompt 14: Token Detail Screen (Task 2.5)
Prompt 15: Send Transaction Flow (Task 2.6)
Prompt 16: Receive Screen with QR (Task 2.7)
Prompt 17: Transaction History (Task 2.8)
Prompt 18: Default Token List Seeding (Task 2.9)
Prompt 19: WalletConnect v2 Integration (Task 3.1)
Prompt 20: NFT Gallery & Detail (Task 3.2)
Prompt 21: Token Swap UI (Task 3.3)
Prompt 22: Settings & Network Management (Task 3.4)
Prompt 23: Background Sync Workers (Task 3.5)
Prompt 24: Deep Linking & Intent Handling (Task 3.6)
Prompt 25: Final Polish — Icon, Splash, Edge-to-Edge, i18n (Task 3.7)
Prompt 26: Unit Tests (referencing 07-TEST-CASES.md)
Prompt 27: UI / Integration Tests (referencing 07-TEST-CASES.md)
Prompt 28: Performance Optimization Pass (referencing 09-PERFORMANCE-OPTIMIZATION.md)
```

A few practical tips for the process:

**Always start each subsequent prompt with this preamble pattern** so the AI has context:

```markdown
# Prompt: [Task Name]

Refer to:
- `/doc/01-PROJECT-OVERVIEW.md` for project structure
- `/doc/02-ARCHITECTURE-AND-TECH-STACK.md` for dependencies and architecture
- `/doc/03-UI-UX-DESIGN.md` for screen designs (when building UI)
- `/doc/04-IMPLEMENTATION-PLAN-PHASE1.md` for Task [X.X] details

The project already has: [list what's been built so far]

## What I need you to do:
[Paste the relevant task's subtasks from the implementation plan]

## Acceptance:
[Paste the relevant acceptance criteria]
