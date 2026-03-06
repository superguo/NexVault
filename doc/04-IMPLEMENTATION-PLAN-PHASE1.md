# NexVault — Phase 1: Foundation (Weeks 1–2)

## Goal
Set up the project skeleton, implement wallet creation/import, secure key storage,
and PIN/biometric authentication. By the end of Phase 1, the user can create or
import a wallet and reach a placeholder home screen.

---

## Task 1.1: Project Initialization (Manual Setup)

**Description:** Create the Android project with multi-module structure and configure
the build system.

**Subtasks:**

1.1.1. Create new Android project in Android Studio with package `com.nexvault.wallet`,
min SDK 26, target SDK 34, Compose Activity template.

1.1.2. Set up `gradle/libs.versions.toml` version catalog with ALL dependencies listed
in `02-ARCHITECTURE-AND-TECH-STACK.md`.

1.1.3. Create the multi-module structure:
- `core/core-ui`, `core/core-common`, `core/core-network`, `core/core-database`,
`core/core-datastore`, `core/core-security`
- `domain`, `data`
- `feature/feature-onboarding`
- Register all modules in `settings.gradle.kts`.

1.1.4. Create `build-logic` convention plugins:
- `nexvault.android.library` — common Android library config
- `nexvault.android.feature` — feature module config (Compose + Hilt)
- `nexvault.android.application` — app module config

1.1.5. Configure Hilt in the `app` module. Create `NexVaultApplication` annotated
with `@HiltAndroidApp`. Set up `MainActivity` with `@AndroidEntryPoint`.

1.1.6. Set up Detekt and ktlint in the root build file.

1.1.7. Initialize Git repo, create `.gitignore`, add `local.properties` to ignore list,
make initial commit.

**Acceptance:** Project compiles, empty Compose `Text("Hello NexVault")` displays,
all modules resolve dependencies correctly, `./gradlew detekt` passes.

---

## Task 1.2: Design System & Theme (core-ui)

**Description:** Implement the Material 3 theme, typography, color scheme, spacing
tokens, and all shared composable components defined in the UI/UX spec.

**Subtasks:**

1.2.1. Create `Color.kt` with light and dark color palettes from seed `#6C5CE7`.
Use Material Theme Builder to generate the full palette.

1.2.2. Create `Type.kt` with Typography using Google Fonts `Inter` and `Space Grotesk`.
Define `displayLarge` through `labelSmall` overrides.

1.2.3. Create `Shape.kt` with `RoundedCornerShape` values (8, 16, 24 dp).

1.2.4. Create `Theme.kt` with `NexVaultTheme` composable supporting light/dark.

1.2.5. Create `NexVaultSpacing` object with spacing tokens.

1.2.6. Implement shared composables:
- `NexVaultButton`, `NexVaultOutlinedButton`
- `NexVaultCard`
- `NexVaultTextField` (with error state)
- `NexVaultTopAppBar`
- `NexVaultBottomNavBar`
- `PinInputField` (6-dot display + numeric keypad)
- `NexVaultLoadingShimmer`
- `NexVaultEmptyState`
- `NexVaultErrorState`
- `AddressChip`
- `ConfirmationDialog` (custom modal dialog)

1.2.7. Create a preview file showing all components in dark mode for visual verification.

**Acceptance:** All composables render correctly in Compose Preview in both light and
dark themes. No hardcoded colors or dimensions outside the design system.

---

## Task 1.3: Security Module (core-security)

**Description:** Implement the cryptographic infrastructure for mnemonic generation,
HD wallet derivation, and encrypted storage using AndroidKeyStore.

**Subtasks:**

1.3.1. Create `KeyStoreManager` class:
- Generate an AES-256-GCM key in AndroidKeyStore (alias: `nexvault_master_key`)
- `encrypt(plaintext: ByteArray): EncryptedData` (returns IV + ciphertext)
- `decrypt(encryptedData: EncryptedData): ByteArray`
- Use `KeyGenParameterSpec` with `setUserAuthenticationRequired(false)` for
now (biometric guard is at app-level).

1.3.2. Create `MnemonicGenerator`:
- Generate 12-word BIP-39 mnemonic (use `org.web3j:crypto` or `bitcoinj`).
- Validate mnemonic.
- Derive seed from mnemonic + optional passphrase.

1.3.3. Create `HdWalletDeriver`:
- From seed, derive ETH private key using BIP-44 path `m/44'/60'/0'/0/0`.
- Return `WalletKeyPair(privateKey, publicKey, address)`.

1.3.4. Create `WalletSecureStorage`:
- `storeMnemonic(mnemonic: String)` — encrypt with `KeyStoreManager`, save
encrypted blob to a private file (or EncryptedSharedPreferences).
- `retrieveMnemonic(): String` — decrypt and return.
- `storePin(pin: String)` — hash PIN with Argon2 (or bcrypt), store hash.
- `verifyPin(pin: String): Boolean` — compare hashes.
- `clearAll()` — wipe wallet data.

1.3.5. Create `BiometricHelper`:
- Check device biometric capability.
- `authenticate(activity, onSuccess, onError)` — show biometric prompt.

1.3.6. Write unit tests for:
- Mnemonic generation (valid 12 words, valid checksum).
- Mnemonic to address derivation (use known test vectors).
- Encrypt → decrypt round-trip.
- PIN hash verification.

**Acceptance:** `MnemonicGenerator` produces valid BIP-39 12-word phrases.
`HdWalletDeriver` derives correct ETH addresses from known test mnemonics.
Encrypted storage round-trips without data loss. All unit tests pass.

---

## Task 1.4: DataStore & Preferences (core-datastore)

**Description:** Set up Preferences DataStore for app-wide settings.

**Subtasks:**

1.4.1. Create `NexVaultPreferences` interface and implementation:
```kotlin
interface NexVaultPreferences {
    val isWalletCreated: Flow<Boolean>
    val selectedChainId: Flow<Int>
    val isBiometricEnabled: Flow<Boolean>
    val currencyCode: Flow<String>   // "USD", "EUR", etc.
    val themeMode: Flow<ThemeMode>    // DARK, LIGHT, SYSTEM
    val autoLockSeconds: Flow<Int>
    suspend fun setWalletCreated(created: Boolean)
    suspend fun setSelectedChainId(chainId: Int)
    suspend fun setBiometricEnabled(enabled: Boolean)
    suspend fun setCurrencyCode(code: String)
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setAutoLockSeconds(seconds: Int)
}
```

1.4.2. Implement with Jetpack DataStore in `core-datastore`.

1.4.3. Provide via Hilt `@Module` binding.

**Acceptance:** Preferences persist across process death. Flow emissions are correct
when values change.

---

## Task 1.5: Domain Models & Repository Interfaces (domain)

**Description:** Define core domain models and repository interfaces for Phase 1.

**Subtasks:**

1.5.1. Create domain models:
```kotlin
data class Wallet(
val address: String,
val chainId: Int,
)

       data class Chain(
           val chainId: Int,
           val name: String,
           val symbol: String,
           val rpcUrl: String,
           val explorerUrl: String,
           val iconRes: Int,
       )
       ```

1.5.2. Create repository interfaces:
```kotlin
interface WalletRepository {
suspend fun createWallet(): Result<Wallet>
suspend fun importWalletFromMnemonic(mnemonic: String): Result<Wallet>
suspend fun importWalletFromPrivateKey(key: String): Result<Wallet>
fun getCurrentWallet(): Flow<Wallet?>
suspend fun deleteWallet()
}

       interface AuthRepository {
           suspend fun setPin(pin: String)
           suspend fun verifyPin(pin: String): Boolean
           suspend fun isBiometricAvailable(): Boolean
       }
       ```

1.5.3. Create use cases:
- `CreateWalletUseCase`
- `ImportWalletUseCase`
- `SetPinUseCase`
- `VerifyPinUseCase`

**Acceptance:** All domain classes compile. Interfaces are well-documented with KDoc.

---

## Task 1.6: Data Layer — Repository Implementations (data)

**Description:** Implement Phase 1 repositories using core modules.

**Subtasks:**

1.6.1. Implement `WalletRepositoryImpl`:
- `createWallet()`: use `MnemonicGenerator` → `HdWalletDeriver` →
`WalletSecureStorage.storeMnemonic()` → return `Wallet`.
- `importWalletFromMnemonic()`: validate mnemonic → derive → store → return.
- `importWalletFromPrivateKey()`: validate key → derive address → store → return.
- `getCurrentWallet()`: check `NexVaultPreferences.isWalletCreated`, if true
derive address from stored mnemonic, emit as Flow.

1.6.2. Implement `AuthRepositoryImpl`:
- Delegates to `WalletSecureStorage` and `BiometricHelper`.

1.6.3. Bind implementations in Hilt `@Module`.

**Acceptance:** Integration test: create wallet → store → retrieve → derive same address.

---

## Task 1.7: Onboarding Feature (feature-onboarding)

**Description:** Build all onboarding screens as Compose UIs with ViewModels.

**Subtasks:**

1.7.1. **WelcomeScreen + WelcomeViewModel:**
- Two buttons: "Create New Wallet", "Import Wallet".
- Lottie animation (use a simple vault/shield animation — embed JSON in assets,
or use a programmatic animated composable as fallback).
- Navigation events via Channel/SharedFlow.

1.7.2. **CreateWalletScreen + CreateWalletViewModel:**
- ViewModel calls `CreateWalletUseCase` on init, stores mnemonic in state.
- Display 12 words in a 4×3 `LazyVerticalGrid`.
- Checkbox "I have written it down" enables Continue button.
- Screenshot detection: register `WindowCallbackWrapper` for FLAG_SECURE,
show warning dialog if screenshot detected.
- State: `data class CreateWalletUiState(val mnemonicWords: List<String>,
         val isAcknowledged: Boolean, val isLoading: Boolean)`.

1.7.3. **VerifyMnemonicScreen + VerifyMnemonicViewModel:**
- ViewModel receives original mnemonic words, shuffles a copy.
- User taps words in correct order.
- State tracks `selectedWords` and `availableWords`.
- If order is correct, enable Confirm.
- On error, shake animation and reset.

1.7.4. **ImportWalletScreen + ImportWalletViewModel:**
- Text field for mnemonic phrase (12 or 24 words).
- Text field for private key (alternative).
- Toggle between mnemonic and private key import modes.
- Validate input in real time; show error for invalid mnemonic.
- On submit, call `ImportWalletUseCase`.

1.7.5. **SetPinScreen + SetPinViewModel:**
- Reusable `PinInputField` composable.
- Two-phase: "Set PIN" → "Confirm PIN".
- If mismatch, show error and reset.
- Biometric toggle at the bottom.
- On success, call `SetPinUseCase`, navigate to main graph.

1.7.6. Create nested navigation graph `onboarding_graph` with routes for each screen.

**Acceptance:** Full flow: Welcome → Create → Verify → Set PIN → navigates to main.
Full flow: Welcome → Import (mnemonic) → Set PIN → navigates to main.
Invalid inputs show appropriate error states.

---

## Task 1.8: Auth / Unlock Feature

**Description:** Build the unlock screen shown when the app is opened and a wallet
already exists.

**Subtasks:**

1.8.1. **UnlockScreen + UnlockViewModel:**
- PIN entry via `PinInputField`.
- Auto-launch biometric prompt on screen load if enabled.
- Track failed attempts; after 5 failures, show 30-second countdown timer.
- On success, navigate to `main_graph`.

1.8.2. **Auto-lock logic in MainActivity:**
- Track last interaction timestamp.
- On `onStop`, record time. On `onStart`, check if elapsed > autoLockSeconds.
- If so, navigate to UnlockScreen.

**Acceptance:** App locks after configured timeout. PIN unlock works.
Biometric unlock works on supported devices. Lockout after 5 failures works.

---

## Task 1.9: Main Scaffold & Navigation Shell

**Description:** Build the main screen scaffold with bottom navigation bar and
placeholder screens for all tabs.

**Subtasks:**

1.9.1. Create `MainScreen` composable with `Scaffold`, `NexVaultBottomNavBar`,
and `NavHost` for the 5 tabs (Home, History, DApp, NFT, Settings).

1.9.2. Create placeholder composables for each tab showing the tab name centered.

1.9.3. Implement top-level navigation: `NavController` with `saveState` and
`restoreState` for tab switching.

1.9.4. Implement root-level navigation in `MainActivity` deciding between
`onboarding_graph`, `auth_graph`, and `main_graph` based on:
- `isWalletCreated` from DataStore
- App lock state.

**Acceptance:** After onboarding, user sees bottom nav with 5 tabs. Tapping tabs
switches content. Re-opening the app shows unlock screen.

---

## Phase 1 Deliverable Checklist

- [ ] Multi-module project compiles and runs
- [ ] Material 3 dark theme renders correctly
- [ ] All shared UI components implemented with previews
- [ ] Wallet creation generates valid 12-word mnemonic
- [ ] Mnemonic verification flow works
- [ ] Wallet import from mnemonic works
- [ ] Wallet import from private key works
- [ ] Mnemonic encrypted and stored securely
- [ ] PIN set and verified with hash comparison
- [ ] Biometric authentication works
- [ ] Auto-lock on app background works
- [ ] Navigation between onboarding, auth, and main works
- [ ] All Phase 1 unit tests pass
- [ ] Detekt and ktlint pass with zero issues
