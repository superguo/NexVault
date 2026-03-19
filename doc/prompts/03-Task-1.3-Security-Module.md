# Prompt: Security Module — Wallet Key Management & Encryption (Task 1.3)

Refer to:
- @doc/01-PROJECT-OVERVIEW.md for project structure
- @doc/02-ARCHITECTURE-AND-TECH-STACK.md for security dependencies (Web3j, Tink, AndroidX Biometric, Android Keystore)
- @doc/05-IMPLEMENTATION-PLAN-PHASE2.md for the full security design — encryption layers, key hierarchy, biometric flow, threat model
- @doc/04-IMPLEMENTATION-PLAN-PHASE1.md for Task 1.3 details

The project already has:
- Full multi-module Gradle setup (all modules compile)
- Version catalog with all dependencies including Web3j, Tink, AndroidX Biometric
- `core-security` module with build.gradle.kts configured
- Design system & theme in `core-ui` (complete)
- `NexVaultApplication` with `@HiltAndroidApp`

All code for this prompt goes in the `core/core-security` module under:
`core/core-security/src/main/java/com/nexvault/wallet/core/security/`

## What I need you to do:

### 1. Android Keystore Manager — `keystore/KeyStoreManager.kt`

Create a class that wraps Android Keystore operations:

```kotlin
@Singleton
class KeyStoreManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val MASTER_KEY_ALIAS = "nexvault_master_key"
        private const val BIOMETRIC_KEY_ALIAS = "nexvault_biometric_key"
    }

    // Generate or retrieve AES-256-GCM key in Android Keystore
    // This key is hardware-backed (StrongBox if available, TEE otherwise)
    fun getOrCreateMasterKey(): SecretKey

    // Generate or retrieve biometric-bound key
    // Requires BiometricPrompt authentication before every use
    // Use setUserAuthenticationRequired(true),
    //       setUserAuthenticationParameters(0, AUTH_BIOMETRIC_STRONG)
    //       setInvalidatedByBiometricEnrollment(true)
    fun getOrCreateBiometricKey(): SecretKey

    // Check if StrongBox is available
    fun isStrongBoxAvailable(): Boolean

    // Delete all keys (for wallet reset/wipe)
    fun deleteAllKeys()

    // Check if master key exists (to determine if wallet has been set up)
    fun hasMasterKey(): Boolean
}
```

Use `KeyGenParameterSpec.Builder` with AES/GCM/NoPadding, 256-bit key size.
Prefer StrongBox when available, fall back to TEE.

### 2. Encryption Manager — `encryption/EncryptionManager.kt`

Create the two-layer encryption system described in the security doc:

```kotlin
@Singleton
class EncryptionManager @Inject constructor(
    private val keyStoreManager: KeyStoreManager
) {
    // --- Layer 1: Keystore-backed encryption ---

    // Encrypt data using the Android Keystore master key (AES-256-GCM)
    // Returns: IV (12 bytes) + ciphertext + auth tag concatenated
    fun encryptWithKeystore(plaintext: ByteArray): ByteArray

    // Decrypt data using the Android Keystore master key
    fun decryptWithKeystore(ciphertext: ByteArray): ByteArray

    // --- Layer 2: Password-derived encryption ---

    // Derive a key from user's PIN/password using Argon2 (or PBKDF2 as fallback)
    // Parameters: at least 100,000 iterations for PBKDF2, 256-bit output
    // Returns the derived key + salt
    fun deriveKeyFromPassword(password: String, salt: ByteArray? = null): DerivedKeyResult

    // Encrypt with password-derived key (AES-256-GCM)
    fun encryptWithPassword(plaintext: ByteArray, password: String): PasswordEncryptedData

    // Decrypt with password-derived key
    fun decryptWithPassword(encryptedData: PasswordEncryptedData, password: String): ByteArray

    // --- Double encryption (Keystore + Password) ---

    // First encrypt with password-derived key, then encrypt result with Keystore key
    fun doubleEncrypt(plaintext: ByteArray, password: String): DoubleEncryptedData

    // Decrypt: first Keystore layer, then password layer
    fun doubleDecrypt(data: DoubleEncryptedData, password: String): ByteArray

    // --- Biometric encryption ---

    // Encrypt using biometric-bound key (requires Cipher from BiometricPrompt result)
    fun encryptWithBiometric(plaintext: ByteArray, cipher: Cipher): ByteArray

    // Decrypt using biometric-bound key
    fun decryptWithBiometric(ciphertext: ByteArray, cipher: Cipher): ByteArray

    // Get initialized Cipher for biometric prompt (encrypt mode or decrypt mode)
    fun getBiometricCipher(forEncryption: Boolean, iv: ByteArray? = null): Cipher
}
```

Data classes:
```kotlin
data class DerivedKeyResult(
    val key: SecretKey,
    val salt: ByteArray
)

data class PasswordEncryptedData(
    val ciphertext: ByteArray,
    val iv: ByteArray,
    val salt: ByteArray
)

data class DoubleEncryptedData(
    val outerCiphertext: ByteArray,   // Keystore-encrypted layer
    val innerIv: ByteArray,           // IV for password layer
    val outerIv: ByteArray,           // IV for keystore layer
    val salt: ByteArray               // Salt for key derivation
)
```

Important: All ByteArrays in memory should be zeroed after use. Create a utility
`fun ByteArray.secureWipe()` that fills with zeros.

### 3. Mnemonic Manager — `mnemonic/MnemonicManager.kt`

Create the BIP39 mnemonic generation and validation:

```kotlin
@Singleton
class MnemonicManager @Inject constructor() {

    // Generate a new BIP39 mnemonic (12 or 24 words)
    // Use Web3j's MnemonicUtils or a secure implementation
    // 12 words = 128 bits entropy, 24 words = 256 bits entropy
    fun generateMnemonic(wordCount: Int = 12): String

    // Validate a mnemonic phrase (checksum + word list)
    fun validateMnemonic(mnemonic: String): Boolean

    // Convert mnemonic to seed (BIP39 seed derivation with optional passphrase)
    fun mnemonicToSeed(mnemonic: String, passphrase: String = ""): ByteArray

    // Get the BIP39 word list (English) for autocomplete during import
    fun getWordList(): List<String>

    // Suggest words based on prefix (for autocomplete in import flow)
    fun suggestWords(prefix: String): List<String>
}
```

Use `SecureRandom` for entropy generation. Use Web3j's `MnemonicUtils` for
BIP39 operations.

### 4. HD Key Derivation — `wallet/HDKeyManager.kt`

Create BIP32/BIP44 hierarchical deterministic key derivation:

```kotlin
@Singleton
class HDKeyManager @Inject constructor() {

    // Derive an Ethereum keypair from seed at BIP44 path
    // Default path: m/44'/60'/0'/0/index
    fun deriveEthereumKeyPair(
        seed: ByteArray,
        accountIndex: Int = 0,
        addressIndex: Int = 0
    ): ECKeyPair

    // Derive Ethereum address from keypair
    fun deriveAddress(keyPair: ECKeyPair): String

    // Derive multiple addresses at once (for account discovery)
    fun deriveAddresses(
        seed: ByteArray,
        accountIndex: Int = 0,
        count: Int = 5
    ): List<DerivedAddress>

    // Get the private key bytes from keypair (for signing)
    fun getPrivateKeyBytes(keyPair: ECKeyPair): ByteArray

    // Get the public key bytes from keypair
    fun getPublicKeyBytes(keyPair: ECKeyPair): ByteArray
}

data class DerivedAddress(
    val address: String,
    val path: String,        // e.g., "m/44'/60'/0'/0/0"
    val index: Int
)
```

Use Web3j's BIP32/BIP44 derivation utilities.

### 5. Secure Wallet Storage — `wallet/WalletStore.kt`

Create the secure storage layer for wallet data:

```kotlin
@Singleton
class WalletStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val encryptionManager: EncryptionManager
) {

    // Store encrypted mnemonic (double-encrypted: password + keystore)
    suspend fun storeMnemonic(mnemonic: String, password: String)

    // Retrieve and decrypt mnemonic
    suspend fun retrieveMnemonic(password: String): String

    // Store encrypted private key for a specific address
    suspend fun storePrivateKey(address: String, privateKey: ByteArray, password: String)

    // Retrieve and decrypt private key
    suspend fun retrievePrivateKey(address: String, password: String): ByteArray

    // Store the biometric-encrypted password (so biometric can unlock the wallet)
    // The actual wallet password is encrypted with the biometric-bound key
    suspend fun storeBiometricEncryptedPassword(password: String, cipher: Cipher)

    // Retrieve the password using biometric cipher
    suspend fun retrieveBiometricEncryptedPassword(cipher: Cipher): String

    // Check if wallet data exists
    fun hasWalletData(): Boolean

    // Check if biometric unlock is configured
    fun hasBiometricData(): Boolean

    // Delete all wallet data (nuclear option — wallet reset)
    suspend fun wipeAll()
}
```

Store encrypted data in the app's private files directory using a dedicated
subfolder (`files/wallet/`). Use a JSON structure to serialize the encrypted
payloads with their IVs and salts. Never log or expose any key material.

### 6. Biometric Helper — `biometric/BiometricHelper.kt`

Create a helper that wraps AndroidX Biometric APIs:

```kotlin
@Singleton
class BiometricHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    // Check if device supports biometric authentication
    fun isBiometricAvailable(): BiometricStatus

    // Check if biometric is enrolled
    fun isBiometricEnrolled(): Boolean

    // Create BiometricPrompt.PromptInfo for authentication
    fun createPromptInfo(
        title: String = "Authenticate",
        subtitle: String = "Use your fingerprint or face to continue",
        negativeButtonText: String = "Use PIN"
    ): BiometricPrompt.PromptInfo

    // Create CryptoObject from cipher for biometric-bound operations
    fun createCryptoObject(cipher: Cipher): BiometricPrompt.CryptoObject
}

enum class BiometricStatus {
    AVAILABLE,
    NO_HARDWARE,
    HARDWARE_UNAVAILABLE,
    NOT_ENROLLED
}
```

Note: The actual `BiometricPrompt.authenticate()` call must happen in the
Activity/Fragment layer (feature modules), not here. This helper only prepares
the prompt info and crypto objects.

### 7. Password Validator — `validation/PasswordValidator.kt`

```kotlin
object PasswordValidator {

    // Minimum 8 characters, at least 1 uppercase, 1 lowercase, 1 digit, 1 special char
    fun validate(password: String): PasswordValidationResult

    // Calculate password strength (0-100 score)
    fun calculateStrength(password: String): Int

    // Validate PIN (exactly 6 digits)
    fun validatePin(pin: String): Boolean
}

data class PasswordValidationResult(
    val isValid: Boolean,
    val hasMinLength: Boolean,
    val hasUppercase: Boolean,
    val hasLowercase: Boolean,
    val hasDigit: Boolean,
    val hasSpecialChar: Boolean
)
```

### 8. Security Utilities — `util/SecurityUtils.kt`

```kotlin
object SecurityUtils {

    // Secure wipe a ByteArray (fill with zeros)
    fun ByteArray.secureWipe()

    // Secure wipe a CharArray
    fun CharArray.secureWipe()

    // Secure wipe a String (best effort — clear backing char array via reflection)
    // Document that this is best-effort due to String immutability in JVM
    fun secureWipeString(value: String)

    // Generate cryptographically secure random bytes
    fun generateSecureRandom(length: Int): ByteArray

    // Constant-time byte array comparison (timing-attack resistant)
    fun constantTimeEquals(a: ByteArray, b: ByteArray): Boolean

    // Convert ByteArray to hex string
    fun ByteArray.toHex(): String

    // Convert hex string to ByteArray
    fun String.hexToByteArray(): ByteArray

    // Checksum an address (EIP-55 mixed-case checksum)
    fun checksumAddress(address: String): String

    // Validate Ethereum address format
    fun isValidEthereumAddress(address: String): Boolean
}
```

### 9. Hilt DI Module — `di/SecurityModule.kt`

Create a Hilt module that provides all security dependencies:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object SecurityModule {

    // All managers are @Singleton and constructor-injected,
    // so they should be automatically provided.
    // Add any @Provides methods if needed for interfaces or
    // third-party dependencies.

    // If we later introduce interfaces for testing, add @Binds here.
}
```

If all classes use `@Singleton` + `@Inject constructor`, you may not need
explicit `@Provides`, but create the module file as a placeholder and add
any necessary bindings.

### 10. Unit Tests — `core/core-security/src/test/`

Create unit tests for the most critical components:

**a) `MnemonicManagerTest.kt`**
- Test 12-word mnemonic generation produces 12 words
- Test 24-word mnemonic generation produces 24 words
- Test all generated words are in BIP39 English word list
- Test valid mnemonic passes validation
- Test invalid mnemonic fails validation (wrong checksum, wrong words, wrong count)
- Test mnemonic-to-seed produces 64-byte seed
- Test same mnemonic always produces same seed
- Test word suggestion returns correct matches

**b) `HDKeyManagerTest.kt`**
- Test deriving address from known test mnemonic matches expected address
  (Use a well-known test vector, e.g., the BIP39 test vectors)
- Test deriving multiple addresses produces unique addresses
- Test different account indices produce different addresses
- Test derived address is valid Ethereum address format (0x + 40 hex chars)

**c) `PasswordValidatorTest.kt`**
- Test valid password passes all checks
- Test short password fails
- Test password without uppercase fails
- Test password without digit fails
- Test password without special char fails
- Test PIN validation: valid 6-digit PIN passes
- Test PIN validation: 5-digit, 7-digit, alphabetic PIN fails
- Test strength calculation: weak password < 40, strong password > 70

**d) `SecurityUtilsTest.kt`**
- Test secureWipe zeros out ByteArray
- Test constantTimeEquals returns true for equal arrays
- Test constantTimeEquals returns false for different arrays
- Test hex round-trip (ByteArray → hex → ByteArray)
- Test valid Ethereum address passes validation
- Test invalid addresses fail (too short, missing 0x, non-hex chars)
- Test checksumAddress produces correct EIP-55 output

**e) `EncryptionManagerTest.kt`**
- Note: Keystore tests require instrumented tests (androidTest).
  For unit tests, test the password-derived encryption:
- Test encrypt then decrypt with correct password returns original plaintext
- Test decrypt with wrong password throws or returns garbage
- Test different passwords produce different ciphertexts
- Test salt is different each time (non-deterministic)

## File Structure Summary:

```
core/core-security/src/main/java/com/nexvault/wallet/core/security/
├── keystore/
│   └── KeyStoreManager.kt
├── encryption/
│   ├── EncryptionManager.kt
│   └── EncryptionModels.kt       (DerivedKeyResult, PasswordEncryptedData, DoubleEncryptedData)
├── mnemonic/
│   └── MnemonicManager.kt
├── wallet/
│   ├── HDKeyManager.kt
│   └── WalletStore.kt
├── biometric/
│   └── BiometricHelper.kt
├── validation/
│   └── PasswordValidator.kt
├── util/
│   └── SecurityUtils.kt
└── di/
    └── SecurityModule.kt

core/core-security/src/test/java/com/nexvault/wallet/core/security/
├── mnemonic/
│   └── MnemonicManagerTest.kt
├── wallet/
│   └── HDKeyManagerTest.kt
├── validation/
│   └── PasswordValidatorTest.kt
├── util/
│   └── SecurityUtilsTest.kt
└── encryption/
    └── EncryptionManagerTest.kt
```

## Critical Security Rules (enforce these throughout):

1. **Never log** mnemonics, private keys, passwords, seeds, or any key material
2. **Always wipe** sensitive ByteArrays in a `finally` block after use
3. **Never store** plaintext keys — everything goes through double encryption
4. **Use SecureRandom** exclusively for all random generation (never `java.util.Random`)
5. **No hardcoded** keys, IVs, salts, or secrets anywhere
6. **All encryption** uses AES-256-GCM with unique random IVs per operation
7. **Private files only** — all stored data goes in app-private directory
8. **Timing-safe comparison** for any security-sensitive equality checks

## Acceptance Criteria:

1. `./gradlew :core:core-security:assembleDebug` compiles with zero errors
2. `./gradlew :core:core-security:test` — all unit tests pass
3. `./gradlew assembleDebug` (full project) compiles with zero errors
4. `MnemonicManager` generates valid 12-word and 24-word mnemonics
5. `HDKeyManager` derives correct Ethereum addresses from known test vectors
6. `PasswordValidator` correctly validates passwords and PINs
7. `EncryptionManager` password-based encrypt/decrypt round-trips successfully
8. `SecurityUtils` hex conversion and address validation work correctly
9. No compiler warnings related to security deprecations
10. No sensitive data appears in any log statements