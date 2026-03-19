# Prompt: Domain Models & Repository Interfaces (Task 1.5)

Refer to:
- @doc/01-PROJECT-OVERVIEW.md for project structure and feature scope
- @doc/02-ARCHITECTURE-AND-TECH-STACK.md for Clean Architecture layers and data flow
- @doc/04-IMPLEMENTATION-PLAN-PHASE1.md for Task 1.5 details
- @doc/05-IMPLEMENTATION-PLAN-PHASE2.md for Phase 2 domain models (build interfaces now)
- @doc/07-TEST-CASES.md for use case test expectations

The project already has these completed modules:

**core-security** (fully implemented):
- `KeyStoreManager` — AES-256-GCM encrypt/decrypt via AndroidKeyStore
- `EncryptionManager` — high-level encrypt/decrypt API using KeyStoreManager
- `WalletStore` — encrypted mnemonic and private key storage
- `BiometricHelper` — biometric capability check and prompt
- `MnemonicManager` — BIP-39 mnemonic generation, validation, seed derivation
- `HDKeyManager` — BIP-44 HD wallet key derivation (m/44'/60'/0'/0/x)
- `PasswordValidator` — PIN/password strength validation
- `SecurityUtils` — hashing, secure random, byte array zeroing

**core-datastore** (fully implemented):
- `UserPreferencesDataStore` — theme, currency, network, notifications, auto-lock, onboarding flags
- `SecurityPreferencesDataStore` — wallet setup state, biometric toggle, auth method, failed attempts, password hash, active wallet/account, last auth timestamp
- `WalletMetadataDataStore` — wallet list (JSON-serialized), account list, CRUD operations
- `AppStateManager` — composite state (isFirstRun, isWalletLocked, isLockedOut, progressive lockout logic, resetApp)
- Models: `ThemeMode`, `NetworkType`, `AutoLockTimeout`, `AuthMethod`, `WalletMetadata`, `AccountMetadata`, `WalletType`, `WalletDisplayInfo`, `AuthFailureResult`

**core-ui** (fully implemented):
- Full Material 3 theme, typography, spacing, shared composables

All code for this prompt goes in the `domain` module under:
`domain/src/main/java/com/nexvault/wallet/domain/`

## Important Architecture Rule:
The `domain` module is **pure Kotlin** — NO Android framework dependencies.
No `Context`, no `Activity`, no Android imports. Only Kotlin stdlib, coroutines,
and `javax.inject` for `@Inject`.

## What I need you to do:

### 1. Domain Models — `model/`

#### a) `model/wallet/Wallet.kt`
```kotlin
/**
 * Represents a wallet in the domain layer.
 * A wallet contains one or more accounts derived from a single mnemonic (HD)
 * or a single imported private key.
 */
data class Wallet(
    val id: String,                    // UUID matching WalletMetadata.id
    val name: String,
    val type: WalletType,
    val accounts: List<Account>,
    val createdAt: Long,
    val isActive: Boolean,
)

enum class WalletType {
    HD,
    IMPORTED
}

data class Account(
    val walletId: String,
    val index: Int,                    // Derivation index (0, 1, 2, ...)
    val address: String,               // Ethereum address (0x...)
    val name: String,                  // e.g., "Account 1"
    val derivationPath: String,        // e.g., "m/44'/60'/0'/0/0"
    val isActive: Boolean,
)
```

#### b) `model/chain/Chain.kt`
```kotlin
/**
 * Represents a blockchain network.
 */
data class Chain(
    val chainId: Int,
    val name: String,
    val symbol: String,                // Native coin symbol, e.g., "ETH"
    val rpcUrl: String,
    val explorerUrl: String,
    val isTestnet: Boolean,
    val iconResName: String?,          // Resource name (not ID, since domain is pure Kotlin)
)

/**
 * Predefined supported chains.
 */
object SupportedChains {
    val ETHEREUM_MAINNET = Chain(
        chainId = 1,
        name = "Ethereum",
        symbol = "ETH",
        rpcUrl = "",                   // Populated from BuildConfig at data layer
        explorerUrl = "https://etherscan.io",
        isTestnet = false,
        iconResName = "ic_ethereum",
    )

    val ETHEREUM_SEPOLIA = Chain(
        chainId = 11155111,
        name = "Sepolia Testnet",
        symbol = "ETH",
        rpcUrl = "",
        explorerUrl = "https://sepolia.etherscan.io",
        isTestnet = true,
        iconResName = "ic_ethereum",
    )

    val BSC_MAINNET = Chain(
        chainId = 56,
        name = "BNB Smart Chain",
        symbol = "BNB",
        rpcUrl = "https://bsc-dataseed.binance.org",
        explorerUrl = "https://bscscan.com",
        isTestnet = false,
        iconResName = "ic_bnb",
    )

    val POLYGON_MAINNET = Chain(
        chainId = 137,
        name = "Polygon",
        symbol = "MATIC",
        rpcUrl = "https://polygon-rpc.com",
        explorerUrl = "https://polygonscan.com",
        isTestnet = false,
        iconResName = "ic_polygon",
    )

    fun all(): List<Chain> = listOf(
        ETHEREUM_MAINNET,
        ETHEREUM_SEPOLIA,
        BSC_MAINNET,
        POLYGON_MAINNET,
    )

    fun getByChainId(chainId: Int): Chain? = all().find { it.chainId == chainId }
}
```

#### c) `model/token/Token.kt`
```kotlin
import java.math.BigDecimal

/**
 * Domain model for a token (native coin or ERC-20).
 */
data class Token(
    val contractAddress: String,       // "native" for native coin (ETH, BNB, MATIC)
    val chainId: Int,
    val symbol: String,
    val name: String,
    val decimals: Int,
    val logoUrl: String?,
    val balance: BigDecimal,
    val fiatPrice: Double?,
    val fiatValue: Double?,
    val priceChange24h: Double?,
) {
    val isNative: Boolean get() = contractAddress == NATIVE_TOKEN_ADDRESS

    companion object {
        const val NATIVE_TOKEN_ADDRESS = "native"
    }
}

/**
 * Portfolio aggregation of all tokens.
 */
data class Portfolio(
    val totalFiatValue: Double,
    val change24hPercent: Double,
    val tokens: List<Token>,
    val chartData: List<PricePoint>,
)

data class PricePoint(
    val timestamp: Long,               // Epoch millis
    val value: Double,
)
```

#### d) `model/transaction/Transaction.kt`
```kotlin
import java.math.BigDecimal
import java.math.BigInteger

/**
 * Domain model for a blockchain transaction.
 */
data class Transaction(
    val txHash: String,
    val chainId: Int,
    val fromAddress: String,
    val toAddress: String,
    val value: BigDecimal,
    val gasUsed: BigInteger?,
    val gasPrice: BigInteger?,
    val tokenSymbol: String?,
    val tokenContractAddress: String?,
    val tokenDecimals: Int?,
    val blockNumber: Long,
    val timestamp: Long,
    val status: TransactionStatus,
    val type: TransactionType,
)

enum class TransactionStatus {
    PENDING,
    CONFIRMED,
    FAILED,
}

enum class TransactionType {
    SEND,
    RECEIVE,
    SWAP,
    CONTRACT_INTERACTION,
    APPROVAL,
}

/**
 * Gas estimation options for sending transactions.
 */
data class GasEstimate(
    val slow: GasOption,
    val normal: GasOption,
    val fast: GasOption,
)

data class GasOption(
    val gasPrice: BigInteger,
    val maxFeePerGas: BigInteger?,     // EIP-1559
    val maxPriorityFeePerGas: BigInteger?, // EIP-1559
    val estimatedTimeSeconds: Int,
    val estimatedTimeName: String,     // "~5 min", "~2 min", "~30 sec"
    val fiatCost: Double?,
)

/**
 * Parameters for sending a transaction.
 */
data class SendTransactionParams(
    val toAddress: String,
    val amount: BigDecimal,
    val tokenAddress: String?,         // null for native coin
    val gasOption: GasOption,
    val chainId: Int,
    val data: String?,                 // Hex-encoded contract call data
)
```

#### e) `model/auth/AuthModels.kt`
```kotlin
/**
 * Authentication-related domain models.
 */
data class AuthState(
    val isWalletSetUp: Boolean,
    val isLocked: Boolean,
    val isLockedOut: Boolean,
    val lockoutRemainingSeconds: Long,
    val authMethod: AuthMethod,
    val isBiometricEnabled: Boolean,
    val isBiometricAvailable: Boolean,
)

enum class AuthMethod {
    PIN,
    PASSWORD,
}

sealed class AuthResult {
    data object Success : AuthResult()
    data class Failed(val remainingAttempts: Int?, val message: String) : AuthResult()
    data class LockedOut(val durationSeconds: Long) : AuthResult()
    data object WalletWiped : AuthResult()
}

/**
 * Result of wallet creation or import.
 */
data class WalletCreationResult(
    val walletId: String,
    val address: String,
    val mnemonicWords: List<String>,    // Empty for private key import
)
```

#### f) `model/common/DataResult.kt`
```kotlin
/**
 * Generic result wrapper for data operations.
 * Used by repositories and use cases for consistent error handling.
 */
sealed interface DataResult<out T> {
    data class Success<T>(val data: T) : DataResult<T>
    data class Error(
        val exception: Throwable,
        val message: String? = null,
    ) : DataResult<Nothing>
}

/**
 * Extension to convert kotlin.Result to DataResult.
 */
fun <T> Result<T>.toDataResult(): DataResult<T> = fold(
    onSuccess = { DataResult.Success(it) },
    onFailure = { DataResult.Error(it, it.message) },
)

/**
 * Extension to map Success data.
 */
inline fun <T, R> DataResult<T>.map(transform: (T) -> R): DataResult<R> = when (this) {
    is DataResult.Success -> DataResult.Success(transform(data))
    is DataResult.Error -> this
}

/**
 * Extension to flatMap Success data.
 */
inline fun <T, R> DataResult<T>.flatMap(transform: (T) -> DataResult<R>): DataResult<R> = when (this) {
    is DataResult.Success -> transform(data)
    is DataResult.Error -> this
}

/**
 * Extension to get data or null.
 */
fun <T> DataResult<T>.getOrNull(): T? = when (this) {
    is DataResult.Success -> data
    is DataResult.Error -> null
}

/**
 * Extension to get data or throw.
 */
fun <T> DataResult<T>.getOrThrow(): T = when (this) {
    is DataResult.Success -> data
    is DataResult.Error -> throw exception
}
```

#### g) `model/common/Exceptions.kt`
```kotlin
/**
 * Domain-specific exceptions.
 */
open class NexVaultException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)

class InvalidMnemonicException(
    message: String = "Invalid mnemonic phrase",
) : NexVaultException(message)

class InvalidPrivateKeyException(
    message: String = "Invalid private key",
) : NexVaultException(message)

class InvalidAddressException(
    message: String = "Invalid Ethereum address",
) : NexVaultException(message)

class WalletNotFoundException(
    message: String = "Wallet not found",
) : NexVaultException(message)

class WalletAlreadyExistsException(
    message: String = "Wallet already exists",
) : NexVaultException(message)

class InsufficientBalanceException(
    val available: String,
    val required: String,
) : NexVaultException("Insufficient balance: available=$available, required=$required")

class TransactionFailedException(
    message: String,
    cause: Throwable? = null,
) : NexVaultException(message, cause)

class AuthenticationException(
    message: String = "Authentication failed",
) : NexVaultException(message)

class NetworkException(
    message: String = "Network error",
    cause: Throwable? = null,
) : NexVaultException(message, cause)

class EncryptionException(
    message: String = "Encryption error",
    cause: Throwable? = null,
) : NexVaultException(message, cause)
```

### 2. Repository Interfaces — `repository/`

#### a) `repository/WalletRepository.kt`
```kotlin
import kotlinx.coroutines.flow.Flow

/**
 * Repository for wallet lifecycle operations.
 * Implementation lives in the `data` module.
 */
interface WalletRepository {
    /**
     * Create a new HD wallet with a fresh mnemonic.
     * Returns the mnemonic words (for user backup) and derived address.
     * The mnemonic is encrypted and stored securely.
     */
    suspend fun createWallet(walletName: String): DataResult<WalletCreationResult>

    /**
     * Import a wallet from a BIP-39 mnemonic phrase (12 or 24 words).
     */
    suspend fun importFromMnemonic(
        mnemonic: String,
        walletName: String,
    ): DataResult<WalletCreationResult>

    /**
     * Import a wallet from a raw private key (64 hex chars, with or without 0x prefix).
     */
    suspend fun importFromPrivateKey(
        privateKey: String,
        walletName: String,
    ): DataResult<WalletCreationResult>

    /**
     * Get all wallets with their accounts.
     */
    fun getWallets(): Flow<List<Wallet>>

    /**
     * Get the currently active wallet with its accounts.
     */
    fun getActiveWallet(): Flow<Wallet?>

    /**
     * Set a wallet as the active wallet.
     */
    suspend fun setActiveWallet(walletId: String): DataResult<Unit>

    /**
     * Add a new derived account to an existing HD wallet.
     * Returns the new account with its address.
     */
    suspend fun addAccount(walletId: String, accountName: String): DataResult<Account>

    /**
     * Get the active account's Ethereum address.
     */
    fun getActiveAddress(): Flow<String?>

    /**
     * Retrieve the mnemonic for backup display (requires prior authentication).
     * Returns the decrypted mnemonic words.
     */
    suspend fun getMnemonicForBackup(walletId: String): DataResult<List<String>>

    /**
     * Delete a wallet and all associated data.
     */
    suspend fun deleteWallet(walletId: String): DataResult<Unit>

    /**
     * Delete all wallets and reset to clean state.
     */
    suspend fun deleteAllWallets(): DataResult<Unit>

    /**
     * Check if any wallet exists.
     */
    fun hasWallet(): Flow<Boolean>
}
```

#### b) `repository/AuthRepository.kt`
```kotlin
import kotlinx.coroutines.flow.Flow

/**
 * Repository for authentication operations.
 */
interface AuthRepository {
    /**
     * Set the user's PIN (6 digits).
     * Stores a salted hash, never the plaintext PIN.
     */
    suspend fun setPin(pin: String): DataResult<Unit>

    /**
     * Verify the entered PIN against the stored hash.
     * Handles progressive lockout via AppStateManager.
     */
    suspend fun verifyPin(pin: String): AuthResult

    /**
     * Change the PIN (requires verification of old PIN first).
     */
    suspend fun changePin(currentPin: String, newPin: String): DataResult<Unit>

    /**
     * Check if biometric authentication is available on this device.
     */
    suspend fun isBiometricAvailable(): Boolean

    /**
     * Enable or disable biometric authentication.
     */
    suspend fun setBiometricEnabled(enabled: Boolean): DataResult<Unit>

    /**
     * Get the current biometric enabled state.
     */
    fun isBiometricEnabled(): Flow<Boolean>

    /**
     * Get the current authentication state.
     */
    fun getAuthState(): Flow<AuthState>

    /**
     * Record a successful authentication (resets lockout, updates timestamp).
     */
    suspend fun onAuthSuccess()

    /**
     * Check if authentication is required (based on auto-lock timeout).
     */
    suspend fun isAuthRequired(): Boolean

    /**
     * Check if a PIN has been set.
     */
    fun hasPinSet(): Flow<Boolean>

    /**
     * Get the configured auto-lock timeout in seconds.
     */
    fun getAutoLockTimeout(): Flow<Long>

    /**
     * Set the auto-lock timeout.
     */
    suspend fun setAutoLockTimeout(timeoutSeconds: Long): DataResult<Unit>
}
```

#### c) `repository/ChainRepository.kt`
```kotlin
import kotlinx.coroutines.flow.Flow

/**
 * Repository for blockchain network management.
 */
interface ChainRepository {
    /**
     * Get all supported chains.
     */
    fun getSupportedChains(): Flow<List<Chain>>

    /**
     * Get only visible chains (user may hide some).
     */
    fun getVisibleChains(): Flow<List<Chain>>

    /**
     * Get the currently selected chain.
     */
    fun getSelectedChain(): Flow<Chain>

    /**
     * Set the selected chain by chain ID.
     */
    suspend fun setSelectedChain(chainId: Int): DataResult<Unit>

    /**
     * Get a chain by its ID.
     */
    fun getChainById(chainId: Int): Chain?

    /**
     * Toggle visibility of a chain in the selector.
     */
    suspend fun setChainVisible(chainId: Int, visible: Boolean): DataResult<Unit>
}
```

#### d) `repository/TokenRepository.kt`
```kotlin
import kotlinx.coroutines.flow.Flow

/**
 * Repository for token balance and price operations.
 */
interface TokenRepository {
    /**
     * Get all tokens with balances for a chain and address.
     * Returns cached data immediately via Flow, refreshes in background.
     */
    fun getTokensWithBalances(chainId: Int, address: String): Flow<List<Token>>

    /**
     * Force refresh all token balances from the blockchain and prices from API.
     */
    suspend fun refreshBalances(chainId: Int, address: String): DataResult<Unit>

    /**
     * Add a custom ERC-20 token by contract address.
     * Fetches name, symbol, decimals from chain for confirmation.
     */
    suspend fun addCustomToken(chainId: Int, contractAddress: String): DataResult<Token>

    /**
     * Remove a token from the tracked list.
     */
    suspend fun removeToken(chainId: Int, contractAddress: String): DataResult<Unit>

    /**
     * Get total portfolio value in fiat for a chain.
     */
    fun getTotalFiatValue(chainId: Int, address: String): Flow<Double>

    /**
     * Get portfolio chart data.
     */
    suspend fun getPortfolioChartData(
        chainId: Int,
        days: Int,
    ): DataResult<List<PricePoint>>

    /**
     * Get a single token's detail with extended price data.
     */
    suspend fun getTokenDetail(
        chainId: Int,
        contractAddress: String,
        address: String,
    ): DataResult<Token>

    /**
     * Get price chart for a specific token.
     */
    suspend fun getTokenPriceChart(
        chainId: Int,
        contractAddress: String,
        days: Int,
    ): DataResult<List<PricePoint>>
}
```

#### e) `repository/TransactionRepository.kt`
```kotlin
import kotlinx.coroutines.flow.Flow
import java.math.BigInteger

/**
 * Repository for transaction operations (send, history, status).
 */
interface TransactionRepository {
    /**
     * Estimate gas for a transaction.
     * Returns slow, normal, and fast options.
     */
    suspend fun estimateGas(
        fromAddress: String,
        toAddress: String,
        value: BigInteger,
        data: String?,
        chainId: Int,
    ): DataResult<GasEstimate>

    /**
     * Send a native coin transaction (ETH, BNB, MATIC).
     * Returns the transaction hash.
     */
    suspend fun sendNativeTransaction(
        params: SendTransactionParams,
        walletId: String,
        accountIndex: Int,
    ): DataResult<String>

    /**
     * Send an ERC-20 token transfer.
     * Returns the transaction hash.
     */
    suspend fun sendTokenTransaction(
        params: SendTransactionParams,
        walletId: String,
        accountIndex: Int,
    ): DataResult<String>

    /**
     * Get transaction history for an address on a chain.
     * Paginated — returns a page of transactions.
     */
    fun getTransactionHistory(
        chainId: Int,
        address: String,
        page: Int,
        pageSize: Int,
    ): Flow<List<Transaction>>

    /**
     * Force refresh transaction history from the block explorer API.
     */
    suspend fun refreshTransactionHistory(
        chainId: Int,
        address: String,
    ): DataResult<Unit>

    /**
     * Get a single transaction's full details.
     */
    suspend fun getTransactionDetail(
        txHash: String,
        chainId: Int,
    ): DataResult<Transaction>

    /**
     * Get pending transactions that need status polling.
     */
    fun getPendingTransactions(chainId: Int, address: String): Flow<List<Transaction>>

    /**
     * Check and update the status of a pending transaction.
     * Returns updated transaction.
     */
    suspend fun updateTransactionStatus(
        txHash: String,
        chainId: Int,
    ): DataResult<Transaction>
}
```

### 3. Use Cases — `usecase/`

Each use case is a single-responsibility class with `@Inject constructor`.
Use cases orchestrate one or more repositories.

#### a) `usecase/wallet/CreateWalletUseCase.kt`
```kotlin
import javax.inject.Inject

/**
 * Creates a new HD wallet with a fresh BIP-39 mnemonic.
 *
 * Flow: generate mnemonic → derive first account → encrypt & store →
 *       update metadata → return mnemonic words for user backup.
 */
class CreateWalletUseCase @Inject constructor(
    private val walletRepository: WalletRepository,
) {
    suspend operator fun invoke(
        walletName: String = "Main Wallet",
    ): DataResult<WalletCreationResult> {
        return walletRepository.createWallet(walletName)
    }
}
```

#### b) `usecase/wallet/ImportWalletUseCase.kt`
```kotlin
import javax.inject.Inject

/**
 * Imports a wallet from a mnemonic phrase or private key.
 */
class ImportWalletUseCase @Inject constructor(
    private val walletRepository: WalletRepository,
) {
    /**
     * Import from BIP-39 mnemonic (12 or 24 words).
     */
    suspend fun fromMnemonic(
        mnemonic: String,
        walletName: String = "Imported Wallet",
    ): DataResult<WalletCreationResult> {
        // Basic validation before delegating to repository
        val trimmed = mnemonic.trim().lowercase()
        val words = trimmed.split("\\s+".toRegex())
        if (words.size != 12 && words.size != 24) {
            return DataResult.Error(
                InvalidMnemonicException("Mnemonic must be 12 or 24 words, got ${words.size}"),
            )
        }
        return walletRepository.importFromMnemonic(trimmed, walletName)
    }

    /**
     * Import from raw private key (64 hex chars).
     */
    suspend fun fromPrivateKey(
        privateKey: String,
        walletName: String = "Imported Wallet",
    ): DataResult<WalletCreationResult> {
        val cleaned = privateKey.trim().removePrefix("0x").removePrefix("0X")
        if (cleaned.length != 64 || !cleaned.all { it in "0123456789abcdefABCDEF" }) {
            return DataResult.Error(
                InvalidPrivateKeyException("Private key must be 64 hex characters"),
            )
        }
        return walletRepository.importFromPrivateKey(cleaned, walletName)
    }
}
```

#### c) `usecase/wallet/GetActiveWalletUseCase.kt`
```kotlin
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Observes the currently active wallet.
 */
class GetActiveWalletUseCase @Inject constructor(
    private val walletRepository: WalletRepository,
) {
    operator fun invoke(): Flow<Wallet?> {
        return walletRepository.getActiveWallet()
    }
}
```

#### d) `usecase/wallet/GetMnemonicForBackupUseCase.kt`
```kotlin
import javax.inject.Inject

/**
 * Retrieves the mnemonic for backup display.
 * Caller must ensure authentication has been performed before calling.
 */
class GetMnemonicForBackupUseCase @Inject constructor(
    private val walletRepository: WalletRepository,
) {
    suspend operator fun invoke(walletId: String): DataResult<List<String>> {
        return walletRepository.getMnemonicForBackup(walletId)
    }
}
```

#### e) `usecase/auth/SetPinUseCase.kt`
```kotlin
import javax.inject.Inject

/**
 * Sets the user's PIN after wallet creation/import.
 * Validates PIN format before storing.
 */
class SetPinUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(pin: String): DataResult<Unit> {
        if (pin.length != 6 || !pin.all { it.isDigit() }) {
            return DataResult.Error(
                AuthenticationException("PIN must be exactly 6 digits"),
            )
        }
        return authRepository.setPin(pin)
    }
}
```

#### f) `usecase/auth/VerifyPinUseCase.kt`
```kotlin
import javax.inject.Inject

/**
 * Verifies the user's PIN for unlock or transaction confirmation.
 * Handles progressive lockout tracking.
 */
class VerifyPinUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(pin: String): AuthResult {
        if (pin.length != 6 || !pin.all { it.isDigit() }) {
            return AuthResult.Failed(
                remainingAttempts = null,
                message = "Invalid PIN format",
            )
        }
        return authRepository.verifyPin(pin)
    }
}
```

#### g) `usecase/auth/ChangePinUseCase.kt`
```kotlin
import javax.inject.Inject

/**
 * Changes the user's PIN.
 * Verifies the current PIN, validates the new PIN, then stores.
 */
class ChangePinUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(
        currentPin: String,
        newPin: String,
    ): DataResult<Unit> {
        if (newPin.length != 6 || !newPin.all { it.isDigit() }) {
            return DataResult.Error(
                AuthenticationException("New PIN must be exactly 6 digits"),
            )
        }
        if (currentPin == newPin) {
            return DataResult.Error(
                AuthenticationException("New PIN must be different from current PIN"),
            )
        }
        return authRepository.changePin(currentPin, newPin)
    }
}
```

#### h) `usecase/auth/GetAuthStateUseCase.kt`
```kotlin
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Observes the current authentication state.
 * Used by navigation logic to determine which screen to show.
 */
class GetAuthStateUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    operator fun invoke(): Flow<AuthState> {
        return authRepository.getAuthState()
    }
}
```

#### i) `usecase/auth/CheckBiometricAvailabilityUseCase.kt`
```kotlin
import javax.inject.Inject

/**
 * Checks if biometric authentication is available on the device.
 */
class CheckBiometricAvailabilityUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(): Boolean {
        return authRepository.isBiometricAvailable()
    }
}
```

#### j) `usecase/chain/GetSelectedChainUseCase.kt`
```kotlin
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Observes the currently selected blockchain network.
 */
class GetSelectedChainUseCase @Inject constructor(
    private val chainRepository: ChainRepository,
) {
    operator fun invoke(): Flow<Chain> {
        return chainRepository.getSelectedChain()
    }
}
```

#### k) `usecase/chain/SetSelectedChainUseCase.kt`
```kotlin
import javax.inject.Inject

/**
 * Changes the selected blockchain network.
 */
class SetSelectedChainUseCase @Inject constructor(
    private val chainRepository: ChainRepository,
) {
    suspend operator fun invoke(chainId: Int): DataResult<Unit> {
        if (SupportedChains.getByChainId(chainId) == null) {
            return DataResult.Error(
                NexVaultException("Unsupported chain ID: $chainId"),
            )
        }
        return chainRepository.setSelectedChain(chainId)
    }
}
```

#### l) `usecase/token/GetPortfolioUseCase.kt`
```kotlin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * Combines token balances, prices, and chart data into a Portfolio.
 * This is the main use case for the Home screen.
 */
class GetPortfolioUseCase @Inject constructor(
    private val tokenRepository: TokenRepository,
    private val walletRepository: WalletRepository,
    private val chainRepository: ChainRepository,
) {
    operator fun invoke(): Flow<DataResult<Portfolio>> {
        return combine(
            walletRepository.getActiveAddress(),
            chainRepository.getSelectedChain(),
        ) { address, chain ->
            if (address == null) {
                return@combine DataResult.Error(WalletNotFoundException())
            }
            try {
                DataResult.Success(
                    Portfolio(
                        totalFiatValue = 0.0,    // Will be populated by collect
                        change24hPercent = 0.0,
                        tokens = emptyList(),
                        chartData = emptyList(),
                    )
                )
            } catch (e: Exception) {
                DataResult.Error(e, e.message)
            }
        }
    }

    /**
     * Trigger a full refresh of balances and prices.
     */
    suspend fun refresh(chainId: Int, address: String): DataResult<Unit> {
        return tokenRepository.refreshBalances(chainId, address)
    }
}
```

#### m) `usecase/token/AddCustomTokenUseCase.kt`
```kotlin
import javax.inject.Inject

/**
 * Adds a custom ERC-20 token by contract address.
 * Validates the address format, then fetches token info from chain.
 */
class AddCustomTokenUseCase @Inject constructor(
    private val tokenRepository: TokenRepository,
) {
    suspend operator fun invoke(
        chainId: Int,
        contractAddress: String,
    ): DataResult<Token> {
        val cleaned = contractAddress.trim()
        if (!isValidAddress(cleaned)) {
            return DataResult.Error(InvalidAddressException())
        }
        return tokenRepository.addCustomToken(chainId, cleaned)
    }

    private fun isValidAddress(address: String): Boolean {
        return address.startsWith("0x") &&
            address.length == 42 &&
            address.substring(2).all { it in "0123456789abcdefABCDEF" }
    }
}
```

### 4. Unit Tests — `domain/src/test/`

#### a) `model/ChainTest.kt`
```
- Test SupportedChains.all() returns exactly 4 chains
- Test SupportedChains.getByChainId(1) returns Ethereum Mainnet
- Test SupportedChains.getByChainId(999) returns null
- Test each chain has non-empty name and symbol
- Test testnet flags are correct (only Sepolia is testnet)
```

#### b) `model/TokenTest.kt`
```
- Test Token with contractAddress="native" → isNative is true
- Test Token with contractAddress="0x..." → isNative is false
- Test Token.NATIVE_TOKEN_ADDRESS constant equals "native"
```

#### c) `model/DataResultTest.kt`
```
- Test DataResult.Success contains data
- Test DataResult.Error contains exception and message
- Test map on Success transforms data
- Test map on Error passes through unchanged
- Test flatMap on Success chains correctly
- Test flatMap on Error passes through unchanged
- Test getOrNull returns data for Success, null for Error
- Test getOrThrow returns data for Success, throws for Error
- Test toDataResult converts kotlin.Result.success to DataResult.Success
- Test toDataResult converts kotlin.Result.failure to DataResult.Error
```

#### d) `model/ExceptionsTest.kt`
```
- Test each exception type has correct default message
- Test InsufficientBalanceException contains available and required amounts
- Test NexVaultException preserves cause chain
```

#### e) `usecase/ImportWalletUseCaseTest.kt`
```
- Test fromMnemonic with valid 12-word phrase calls repository
- Test fromMnemonic with 10-word phrase returns Error(InvalidMnemonicException)
- Test fromMnemonic with 24-word phrase calls repository
- Test fromPrivateKey with valid 64 hex chars calls repository
- Test fromPrivateKey with "0x" prefix strips prefix and calls repository
- Test fromPrivateKey with invalid length returns Error(InvalidPrivateKeyException)
- Test fromPrivateKey with non-hex chars returns Error(InvalidPrivateKeyException)
- Use MockK to mock WalletRepository
```

#### f) `usecase/SetPinUseCaseTest.kt`
```
- Test with valid 6-digit PIN calls repository.setPin
- Test with 5-digit PIN returns Error
- Test with 7-digit PIN returns Error
- Test with alphabetic PIN returns Error
- Use MockK to mock AuthRepository
```

#### g) `usecase/VerifyPinUseCaseTest.kt`
```
- Test with valid format delegates to authRepository.verifyPin
- Test with invalid format returns Failed without calling repository
- Use MockK to mock AuthRepository
```

#### h) `usecase/ChangePinUseCaseTest.kt`
```
- Test with valid current and new PINs calls repository.changePin
- Test with same current and new PIN returns Error
- Test with invalid new PIN format returns Error
- Use MockK to mock AuthRepository
```

#### i) `usecase/SetSelectedChainUseCaseTest.kt`
```
- Test with valid chainId (1) calls repository
- Test with unsupported chainId (999) returns Error
- Use MockK to mock ChainRepository
```

#### j) `usecase/AddCustomTokenUseCaseTest.kt`
```
- Test with valid 0x address calls repository
- Test with address missing 0x prefix returns Error
- Test with address wrong length returns Error
- Test with non-hex characters returns Error
- Use MockK to mock TokenRepository
```

## File Structure Summary:

```
domain/src/main/java/com/nexvault/wallet/domain/
├── model/
│   ├── wallet/
│   │   └── Wallet.kt           (Wallet, WalletType, Account)
│   ├── chain/
│   │   └── Chain.kt            (Chain, SupportedChains)
│   ├── token/
│   │   └── Token.kt            (Token, Portfolio, PricePoint)
│   ├── transaction/
│   │   └── Transaction.kt      (Transaction, GasEstimate, GasOption, SendTransactionParams, enums)
│   ├── auth/
│   │   └── AuthModels.kt       (AuthState, AuthMethod, AuthResult, WalletCreationResult)
│   └── common/
│       ├── DataResult.kt        (DataResult sealed interface + extensions)
│       └── Exceptions.kt        (Domain exception hierarchy)
├── repository/
│   ├── WalletRepository.kt
│   ├── AuthRepository.kt
│   ├── ChainRepository.kt
│   ├── TokenRepository.kt
│   └── TransactionRepository.kt
└── usecase/
    ├── wallet/
    │   ├── CreateWalletUseCase.kt
    │   ├── ImportWalletUseCase.kt
    │   ├── GetActiveWalletUseCase.kt
    │   └── GetMnemonicForBackupUseCase.kt
    ├── auth/
    │   ├── SetPinUseCase.kt
    │   ├── VerifyPinUseCase.kt
    │   ├── ChangePinUseCase.kt
    │   ├── GetAuthStateUseCase.kt
    │   └── CheckBiometricAvailabilityUseCase.kt
    ├── chain/
    │   ├── GetSelectedChainUseCase.kt
    │   └── SetSelectedChainUseCase.kt
    └── token/
        ├── GetPortfolioUseCase.kt
        └── AddCustomTokenUseCase.kt

domain/src/test/java/com/nexvault/wallet/domain/
├── model/
│   ├── ChainTest.kt
│   ├── TokenTest.kt
│   ├── DataResultTest.kt
│   └── ExceptionsTest.kt
└── usecase/
    ├── ImportWalletUseCaseTest.kt
    ├── SetPinUseCaseTest.kt
    ├── VerifyPinUseCaseTest.kt
    ├── ChangePinUseCaseTest.kt
    ├── SetSelectedChainUseCaseTest.kt
    └── AddCustomTokenUseCaseTest.kt
```

## Dependencies This Module Needs:

The `domain` module's `build.gradle.kts` should be a **pure Kotlin/JVM** module
(NOT an Android library). It should depend on:
- Kotlin stdlib (automatic)
- `kotlinx-coroutines-core` (for Flow)
- `javax.inject:javax.inject:1` (for @Inject annotation — allows Hilt injection without Android dependency)
- JUnit + MockK for tests
- Turbine for Flow testing (optional, for use case Flow tests)

**Do NOT add any Android dependencies** to this module. No `core-security`,
no `core-datastore` — those are implementation details that belong in the `data` layer.

If the module is currently configured as `com.android.library`, convert it to
a pure Kotlin JVM module:
```kotlin
// domain/build.gradle.kts
plugins {
    id("org.jetbrains.kotlin.jvm")
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.javax.inject)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.truth)
}
```

Verify `javax.inject` (or `jakarta.inject`) is in the version catalog. If not, add:
```toml
[libraries]
javax-inject = { module = "javax.inject:javax.inject", version = "1" }
```

## Key Rules:

1. **Pure Kotlin** — zero Android imports in the entire domain module
2. **Interfaces only for repositories** — no implementations here
3. **Use cases are simple** — single `invoke` or named function, thin orchestration
4. **Domain models are independent** — they don't mirror database entities or API DTOs
5. **DataResult for all fallible operations** — consistent error handling pattern
6. **Every public class and function has KDoc** — documentation is mandatory
7. **No Hilt @Module in domain** — use cases use `@Inject constructor` and are auto-provided

## Acceptance Criteria:

1. `./gradlew :domain:compileKotlin` compiles with zero errors (JVM module, not Android)
2. `./gradlew :domain:test` — all unit tests pass
3. `./gradlew assembleDebug` (full project) compiles with zero errors
4. Domain module has ZERO Android framework imports
5. All repository interfaces compile and have complete KDoc
6. All use cases have `@Inject constructor`
7. DataResult extensions (map, flatMap, getOrNull, getOrThrow) work correctly
8. All domain exceptions extend NexVaultException
9. SupportedChains contains all 4 chains with correct data
10. Import validation catches invalid mnemonics and private keys before calling repository