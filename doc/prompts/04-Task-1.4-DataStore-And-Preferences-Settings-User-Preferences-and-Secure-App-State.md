# Prompt: DataStore & Preferences — Settings, User Preferences, and Secure App State (Task 1.4)

Refer to:
- @doc/01-PROJECT-OVERVIEW.md for project structure
- @doc/02-ARCHITECTURE-AND-TECH-STACK.md for security dependencies (Web3j, Tink, AndroidX Biometric, Android Keystore)
- @doc/05-IMPLEMENTATION-PLAN-PHASE2.md for the full security design — encryption layers, key hierarchy, biometric flow, threat model
- @doc/04-IMPLEMENTATION-PLAN-PHASE1.md
The project already has:
- Full multi-module Gradle setup (all modules compile)
- Version catalog with all dependencies including AndroidX DataStore (Preferences + Proto)
- `core-security` module fully implemented (KeyStoreManager, EncryptionManager, WalletStore, BiometricHelper, MnemonicManager, HDKeyManager, PasswordValidator, SecurityUtils)
- `core-ui` with design system and theme
- `NexVaultApplication` with `@HiltAndroidApp`

All code for this prompt goes in the `core/core-datastore` module under:
`core/core-datastore/src/main/java/com/nexvault/wallet/core/datastore/`

## What I need you to do:

### 1. User Preferences DataStore — `preferences/UserPreferencesDataStore.kt`

This handles all non-sensitive app settings using AndroidX DataStore Preferences:

```kotlin
@Singleton
class UserPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val Context.dataStore by preferencesDataStore(name = "user_preferences")

    // --- Theme ---
    val themeMode: Flow<ThemeMode>
    suspend fun setThemeMode(mode: ThemeMode)

    // --- Currency Display ---
    val fiatCurrency: Flow<String>             // e.g., "USD", "EUR", "GBP"
    suspend fun setFiatCurrency(currency: String)

    // --- Network ---
    val selectedNetwork: Flow<NetworkType>      // MAINNET, GOERLI, SEPOLIA, CUSTOM
    suspend fun setSelectedNetwork(network: NetworkType)

    val customRpcUrl: Flow<String>
    suspend fun setCustomRpcUrl(url: String)

    // --- Notifications ---
    val notificationsEnabled: Flow<Boolean>
    suspend fun setNotificationsEnabled(enabled: Boolean)

    val transactionAlerts: Flow<Boolean>
    suspend fun setTransactionAlerts(enabled: Boolean)

    val priceAlerts: Flow<Boolean>
    suspend fun setPriceAlerts(enabled: Boolean)

    // --- Display ---
    val hideBalances: Flow<Boolean>              // Privacy mode — hide balances on home screen
    suspend fun setHideBalances(hide: Boolean)

    val showTestNetworks: Flow<Boolean>
    suspend fun setShowTestNetworks(show: Boolean)

    // --- App Behavior ---
    val autoLockTimeout: Flow<AutoLockTimeout>   // IMMEDIATE, 1_MINUTE, 5_MINUTES, 15_MINUTES, 30_MINUTES, NEVER
    suspend fun setAutoLockTimeout(timeout: AutoLockTimeout)

    val requireAuthForTransactions: Flow<Boolean> // Always require PIN/biometric before sending
    suspend fun setRequireAuthForTransactions(required: Boolean)

    // --- Onboarding ---
    val hasCompletedOnboarding: Flow<Boolean>
    suspend fun setHasCompletedOnboarding(completed: Boolean)

    val hasBackedUpMnemonic: Flow<Boolean>
    suspend fun setHasBackedUpMnemonic(backedUp: Boolean)

    // --- Clear all preferences ---
    suspend fun clearAll()
}
```

### 2. Enums / Models — `model/PreferenceModels.kt`

```kotlin
enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}

enum class NetworkType {
    MAINNET, GOERLI, SEPOLIA, CUSTOM
}

enum class AutoLockTimeout(val seconds: Long) {
    IMMEDIATE(0),
    ONE_MINUTE(60),
    FIVE_MINUTES(300),
    FIFTEEN_MINUTES(900),
    THIRTY_MINUTES(1800),
    NEVER(-1)
}
```

### 3. Security Preferences DataStore — `security/SecurityPreferencesDataStore.kt`

This handles security-related state that needs Keystore-level encryption.
Use the `EncryptionManager` from `core-security` to encrypt sensitive values
before storing them in DataStore:

```kotlin
@Singleton
class SecurityPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val encryptionManager: EncryptionManager
) {
    private val Context.dataStore by preferencesDataStore(name = "security_preferences")

    // --- Wallet Setup State ---
    val isWalletSetUp: Flow<Boolean>
    suspend fun setWalletSetUp(isSetUp: Boolean)

    // --- Biometric ---
    val isBiometricEnabled: Flow<Boolean>
    suspend fun setBiometricEnabled(enabled: Boolean)

    // --- Authentication ---
    val authMethod: Flow<AuthMethod>            // PIN, PASSWORD
    suspend fun setAuthMethod(method: AuthMethod)

    // --- Failed Attempts Tracking ---
    val failedAttemptCount: Flow<Int>
    suspend fun incrementFailedAttempts(): Int   // Returns new count
    suspend fun resetFailedAttempts()

    val lockoutEndTime: Flow<Long>               // Epoch millis; 0 = no lockout
    suspend fun setLockoutEndTime(time: Long)

    // --- Password Hash (for quick PIN/password verification) ---
    // Store a salted hash of the PIN/password for fast verification
    // before attempting expensive decryption operations.
    // The hash itself is encrypted with the Keystore master key.
    suspend fun storePasswordHash(hash: ByteArray, salt: ByteArray)
    suspend fun getPasswordHash(): Pair<ByteArray, ByteArray>?   // hash + salt, or null
    fun hasPasswordHash(): Flow<Boolean>

    // --- Active Wallet Info (non-sensitive metadata) ---
    val activeWalletId: Flow<String>
    suspend fun setActiveWalletId(walletId: String)

    val activeAccountIndex: Flow<Int>
    suspend fun setActiveAccountIndex(index: Int)

    // --- Last Authentication Timestamp ---
    // Used to determine if auto-lock timeout has expired
    val lastAuthTimestamp: Flow<Long>
    suspend fun setLastAuthTimestamp(timestamp: Long)

    // --- Clear all security preferences ---
    suspend fun clearAll()
}

enum class AuthMethod {
    PIN, PASSWORD
}
```

### 4. Wallet Metadata DataStore — `wallet/WalletMetadataDataStore.kt`

This stores non-sensitive wallet metadata (what wallets exist, their names,
which accounts are active). No private keys or mnemonics here — those live
in `WalletStore` from `core-security`.

```kotlin
@Singleton
class WalletMetadataDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val Context.dataStore by preferencesDataStore(name = "wallet_metadata")

    // Store/retrieve wallet list as JSON string
    // Each wallet: id, name, creation date, number of accounts, type (HD or imported)
    val wallets: Flow<List<WalletMetadata>>
    suspend fun addWallet(wallet: WalletMetadata)
    suspend fun updateWallet(wallet: WalletMetadata)
    suspend fun removeWallet(walletId: String)

    // Store/retrieve account list for a specific wallet
    val accounts: Flow<List<AccountMetadata>>
    suspend fun addAccount(account: AccountMetadata)
    suspend fun updateAccount(account: AccountMetadata)
    suspend fun removeAccount(walletId: String, accountIndex: Int)

    // Quick access to active wallet + account
    suspend fun getActiveWalletWithAccounts(): Pair<WalletMetadata, List<AccountMetadata>>?

    // Clear all metadata
    suspend fun clearAll()
}
```

### 5. Wallet/Account Metadata Models — `model/WalletModels.kt`

```kotlin
data class WalletMetadata(
    val id: String,                              // UUID
    val name: String,                            // User-defined name, e.g., "Main Wallet"
    val createdAt: Long,                         // Epoch millis
    val type: WalletType,
    val accountCount: Int,
    val isActive: Boolean
)

enum class WalletType {
    HD,          // Hierarchical Deterministic (from mnemonic)
    IMPORTED     // Single private key import
}

data class AccountMetadata(
    val walletId: String,
    val accountIndex: Int,
    val address: String,                         // Ethereum address (public, non-sensitive)
    val name: String,                            // User-defined name, e.g., "Account 1"
    val derivationPath: String,                  // e.g., "m/44'/60'/0'/0/0"
    val isActive: Boolean,
    val addedAt: Long                            // Epoch millis
)
```

### 6. App State Manager — `state/AppStateManager.kt`

A centralized manager that combines preferences into useful app-level state.
Other modules observe this instead of accessing individual DataStores:

```kotlin
@Singleton
class AppStateManager @Inject constructor(
    private val userPreferences: UserPreferencesDataStore,
    private val securityPreferences: SecurityPreferencesDataStore,
    private val walletMetadata: WalletMetadataDataStore
) {
    // --- Composite State Flows ---

    // Is the app in a "first run" state? (no wallet, no onboarding completed)
    val isFirstRun: Flow<Boolean>

    // Is the wallet currently locked? (requires authentication)
    // Combines: isWalletSetUp + lastAuthTimestamp + autoLockTimeout
    val isWalletLocked: Flow<Boolean>

    // Is the user currently locked out due to too many failed attempts?
    val isLockedOut: Flow<Boolean>

    // Remaining lockout time in seconds (0 if not locked out)
    val lockoutRemainingSeconds: Flow<Long>

    // Current display state: active wallet name + active account address + network
    val currentWalletDisplayInfo: Flow<WalletDisplayInfo?>

    // --- Actions ---

    // Record successful authentication
    suspend fun onAuthenticationSuccess()

    // Record failed authentication attempt
    // Returns: the new failed attempt count
    // Implements progressive lockout:
    //   5 failures  → 30-second lockout
    //   8 failures  → 5-minute lockout
    //   10 failures → 15-minute lockout
    //   15 failures → 1-hour lockout (and warn about wallet wipe)
    //   20 failures → wipe wallet data
    suspend fun onAuthenticationFailure(): AuthFailureResult

    // Check if auth is required right now
    // (based on timeout, last auth time, and settings)
    suspend fun isAuthenticationRequired(): Boolean

    // Full app reset (wipe everything)
    suspend fun resetApp()
}

data class WalletDisplayInfo(
    val walletName: String,
    val accountName: String,
    val address: String,
    val network: NetworkType
)

sealed class AuthFailureResult {
    data class TemporaryLockout(val seconds: Long, val attemptCount: Int) : AuthFailureResult()
    data class Warning(val message: String, val attemptCount: Int) : AuthFailureResult()
    object WalletWiped : AuthFailureResult()
    data class NoLockout(val attemptCount: Int) : AuthFailureResult()
}
```

### 7. JSON Serialization Utilities — `util/DataStoreSerializer.kt`

Since DataStore Preferences stores primitives and strings, we need to serialize
complex objects to/from JSON. Use Kotlinx Serialization (already in version catalog)
or Gson — whichever is in the project dependencies:

```kotlin
object DataStoreSerializer {
    // Serialize WalletMetadata list to JSON string
    fun serializeWallets(wallets: List<WalletMetadata>): String

    // Deserialize JSON string to WalletMetadata list
    fun deserializeWallets(json: String): List<WalletMetadata>

    // Serialize AccountMetadata list to JSON string
    fun serializeAccounts(accounts: List<AccountMetadata>): String

    // Deserialize JSON string to AccountMetadata list
    fun deserializeAccounts(json: String): List<AccountMetadata>
}
```

Add `@Serializable` annotations to `WalletMetadata` and `AccountMetadata` if
using Kotlinx Serialization. Or use Gson `TypeToken` if using Gson. Either is fine —
use whatever serialization library is already in the version catalog.

### 8. Hilt DI Module — `di/DataStoreModule.kt`

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    // All DataStore classes use @Singleton + @Inject constructor,
    // so they should be auto-provided.
    // Add @Provides methods here if needed for DataStore instances
    // or third-party dependencies.
}
```

Note: Multiple `preferencesDataStore` delegates can cause issues if defined
in different classes with the same context extension. Make sure each DataStore
has a unique name and that the delegate is defined at the top level of the file
or as a companion/extension, following the AndroidX DataStore best practices
(single instance per file, top-level extension property).

**Important DataStore best practice:** Define each `preferencesDataStore` delegate
as a **top-level** property in its respective file to ensure single-instance
behavior:

```kotlin
// Top of file, outside the class
private val Context.userPreferencesDataStore by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.userPreferencesDataStore
    // ...
}
```

### 9. Unit Tests — `core/core-datastore/src/test/`

**a) `PreferenceModelsTest.kt`**
- Test ThemeMode enum has all expected values (LIGHT, DARK, SYSTEM)
- Test NetworkType enum has all expected values
- Test AutoLockTimeout seconds values are correct
- Test AutoLockTimeout.NEVER has -1 seconds

**b) `DataStoreSerializerTest.kt`**
- Test serialize then deserialize WalletMetadata list round-trips correctly
- Test empty list serializes and deserializes correctly
- Test single wallet round-trips with all fields preserved
- Test multiple wallets round-trip correctly
- Test AccountMetadata serialize/deserialize round-trip
- Test invalid JSON returns empty list (graceful error handling)

**c) `PasswordValidatorIntegrationTest.kt`** (if not already tested in core-security)
- Skip if already covered. If you add this, just verify password hashing
  used by SecurityPreferencesDataStore is consistent.

**d) `AppStateManagerTest.kt`**
- Test progressive lockout thresholds:
  - After 4 failures → no lockout (NoLockout)
  - After 5 failures → 30-second lockout (TemporaryLockout)
  - After 8 failures → 5-minute lockout
  - After 10 failures → 15-minute lockout
  - After 15 failures → 1-hour lockout + warning
  - After 20 failures → WalletWiped
- Test `onAuthenticationSuccess` resets failed attempts
- Since AppStateManager depends on DataStore classes that need Android context,
  these tests should either:
  - Use fakes/mocks for the DataStore dependencies, OR
  - Be placed in `androidTest` for instrumented testing
  - Prefer mocking approach for unit tests

**e) `WalletMetadataTest.kt`**
- Test WalletMetadata creation with all fields
- Test AccountMetadata creation with derivation path
- Test WalletType enum values

## File Structure Summary:

```
core/core-datastore/src/main/java/com/nexvault/wallet/core/datastore/
├── preferences/
│   └── UserPreferencesDataStore.kt
├── security/
│   └── SecurityPreferencesDataStore.kt
├── wallet/
│   └── WalletMetadataDataStore.kt
├── state/
│   └── AppStateManager.kt
├── model/
│   ├── PreferenceModels.kt        (ThemeMode, NetworkType, AutoLockTimeout)
│   └── WalletModels.kt            (WalletMetadata, AccountMetadata, WalletType, etc.)
├── util/
│   └── DataStoreSerializer.kt
└── di/
    └── DataStoreModule.kt

core/core-datastore/src/test/java/com/nexvault/wallet/core/datastore/
├── model/
│   ├── PreferenceModelsTest.kt
│   └── WalletMetadataTest.kt
├── util/
│   └── DataStoreSerializerTest.kt
└── state/
    └── AppStateManagerTest.kt
```

## Dependencies This Module Needs:

The `core-datastore` module's `build.gradle.kts` should depend on:
- `core-security` (for EncryptionManager in SecurityPreferencesDataStore)
- AndroidX DataStore Preferences
- Kotlinx Serialization (or Gson) for JSON serialization
- Hilt
- Coroutines
- JUnit + Mockk/Mockito for tests

Verify these are already in the module's `build.gradle.kts`. If not, add them
following the existing pattern from other modules.

## Key Rules:

1. **DataStore is async** — all reads return `Flow<T>`, all writes are `suspend` functions
2. **Never block the main thread** — DataStore operations are inherently non-blocking
3. **Single instance per DataStore** — use top-level `preferencesDataStore` delegates
4. **Encrypt sensitive values** — password hashes, lockout state, and auth-related data go through EncryptionManager before storage
5. **Non-sensitive metadata is fine in plaintext** — wallet names, account names, addresses (public), theme preferences, etc.
6. **Graceful defaults** — every preference should have a sensible default (e.g., ThemeMode.SYSTEM, NetworkType.MAINNET, autoLock = 5 minutes)
7. **No sensitive data in logs** — same rule as core-security

## Acceptance Criteria:

1. `./gradlew :core:core-datastore:assembleDebug` compiles with zero errors
2. `./gradlew :core:core-datastore:test` — all unit tests pass
3. `./gradlew assembleDebug` (full project) compiles with zero errors
4. All preference Flows emit correct default values
5. Serialization round-trips preserve all fields
6. Enum values and their properties are correct
7. Progressive lockout thresholds are correctly implemented
8. `core-datastore` correctly depends on `core-security` and can access EncryptionManager
9. Each DataStore file has a unique name (no conflicts)
10. No compiler warnings related to DataStore or coroutine usage