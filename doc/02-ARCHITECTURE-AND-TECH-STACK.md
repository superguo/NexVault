```markdown
# NexVault — Architecture & Technology Stack

## 1. Architecture Pattern

The project follows **Clean Architecture** with **MVVM** at the presentation layer and
**Unidirectional Data Flow (UDF)** in every Compose screen.

```
┌──────────────────────────────────────────────────────────┐
│                     Presentation                         │
│  Compose Screen  ──▶  ViewModel  ──▶  UiState (Flow)    │
│       ▲                   │                              │
│       │              User Intent                         │
│       └───────────────────┘                              │
├──────────────────────────────────────────────────────────┤
│                       Domain                             │
│         Use Cases  ◀──  Repository Interfaces            │
├──────────────────────────────────────────────────────────┤
│                        Data                              │
│  Repository Impls  ──▶  Remote (Retrofit / Web3j)       │
│                    ──▶  Local  (Room / DataStore)        │
│                    ──▶  Security (KeyStore wrapper)      │
└──────────────────────────────────────────────────────────┘
```

Every feature module only depends on `domain` and `core-*` modules. The `data` module
provides concrete implementations injected via Hilt.

## 2. Key Technology Choices

### 2.1 Language & Build

| Item              | Choice                              | Reason                                     |
|-------------------|-------------------------------------|--------------------------------------------|
| Language          | Kotlin 2.0+                         | JD requirement; 100% Kotlin                |
| Build             | Gradle KTS + Version Catalog        | Modern, type-safe build scripts            |
| Compose Compiler  | Kotlin 2.0 compose compiler plugin  | Latest compiler integration                |

### 2.2 Android Jetpack

| Library               | Purpose                                                |
|-----------------------|--------------------------------------------------------|
| Jetpack Compose (BOM) | Declarative UI — Material 3                            |
| Navigation Compose    | Type-safe screen navigation with arguments             |
| Hilt                  | Dependency injection across all modules                |
| Room                  | Local database for tokens, transactions, NFTs cache    |
| DataStore Preferences | Lightweight key-value for settings, selected network   |
| Lifecycle ViewModel   | ViewModel + Compose integration                        |
| Work Manager          | Background sync of balances and pending txs            |
| CameraX               | QR code scanning for receive / WalletConnect URI       |
| Biometric             | Fingerprint / face unlock for sensitive operations     |

### 2.3 Networking & Blockchain

| Library            | Version (approx.) | Purpose                                          |
|--------------------|--------------------|--------------------------------------------------|
| Retrofit 2         | 2.9+               | REST API client (CoinGecko, 1inch, NFT metadata) |
| OkHttp 4           | 4.12+              | HTTP client, logging interceptor                  |
| Moshi               | 1.15+              | JSON serialization (kotlin-codegen)              |
| Web3j (Android)    | 4.10+              | Ethereum JSON-RPC, transaction signing, ABI       |
| WalletConnect v2   | latest BOM         | DApp connection protocol                          |
| kotlinx-serialization | 1.6+           | Backup serialization for data classes             |

### 2.4 Security

| Library / API             | Purpose                                              |
|---------------------------|------------------------------------------------------|
| AndroidKeyStore           | Hardware-backed key wrapping for mnemonic encryption  |
| Tink (Google)             | AEAD encryption primitives wrapping KeyStore          |
| BIP39 (bitcoinj or custom)| Mnemonic generation / validation                     |
| BIP32/BIP44               | HD wallet derivation (via Web3j or kethereum)         |
| SQLCipher (optional)       | Encrypted Room database on-disk                      |

### 2.5 UI & Media

| Library       | Purpose                                     |
|---------------|---------------------------------------------|
| Coil Compose  | Async image loading (NFT thumbnails, icons) |
| Lottie Compose| Animated illustrations (onboarding, success)|
| Vico          | Chart library for portfolio value over time |
| ZXing / ML Kit| QR code generation and scanning             |
| Accompanist   | System UI controller, permissions            |
| Shimmer       | Loading placeholder effects                  |

### 2.6 Testing

| Library                | Purpose                          |
|------------------------|----------------------------------|
| JUnit 5                | Unit tests                       |
| Mockk                  | Kotlin-first mocking             |
| Turbine                | Flow testing                     |
| Compose UI Test        | Instrumented Compose UI tests    |
| Truth                  | Fluent assertions                |
| Hilt Testing           | DI-aware instrumented tests      |
| Robolectric            | JVM-based Android unit tests     |

### 2.7 Code Quality

| Tool          | Purpose                       |
|---------------|-------------------------------|
| Detekt        | Static analysis for Kotlin    |
| ktlint        | Code formatting               |
| Spotless      | Formatting enforcement        |
| LeakCanary    | Memory leak detection (debug) |

## 3. Dependency Injection Graph (Hilt)

```
@HiltAndroidApp NexVaultApplication
│
├── @Module NetworkModule          → Retrofit, OkHttp, Moshi, Web3j
├── @Module DatabaseModule         → Room DB, DAOs
├── @Module DataStoreModule        → Preferences DataStore
├── @Module SecurityModule         → KeyStore wrapper, Tink AEAD
├── @Module RepositoryModule       → Bind repository interfaces → impls
├── @Module UseCaseModule          → (optional, can @Inject construct)
└── @Module WalletConnectModule    → WalletConnect Core, Web3Wallet
```

## 4. Navigation Graph

The app uses a single-Activity architecture with a bottom navigation bar after
authentication.

```
NavHost (startDestination depends on wallet existence)
│
├── onboarding_graph (nested)
│   ├── WelcomeScreen
│   ├── CreateWalletScreen (show mnemonic)
│   ├── VerifyMnemonicScreen
│   ├── ImportWalletScreen
│   └── SetPinScreen
│
├── auth_graph (nested)
│   └── UnlockScreen (PIN / Biometric)
│
└── main_graph (nested, with BottomNavBar)
├── HomeTab
│   ├── HomeScreen (dashboard)
│   └── TokenDetailScreen
├── HistoryTab
│   ├── HistoryListScreen
│   └── TransactionDetailScreen
├── DAppTab
│   ├── DAppBrowserScreen
│   └── SessionDetailScreen
├── NFTTab
│   ├── NFTGalleryScreen
│   └── NFTDetailScreen
└── SettingsTab
├── SettingsScreen
├── NetworkManagementScreen
├── AddressBookScreen
└── ExportWalletScreen
```

## 5. Data Flow Example — "View Token Balance"

```
1. HomeScreen composable observes HomeViewModel.uiState (StateFlow)
2. HomeViewModel on init calls GetPortfolioUseCase()
3. GetPortfolioUseCase calls TokenRepository.getTokensWithBalances(chainId)
4. TokenRepository:
   a. Room DAO returns cached tokens (Flow)
   b. In parallel, fetches on-chain balances via Web3j multicall
   c. Fetches fiat prices from CoinGecko API
   d. Merges results, updates Room cache, emits combined Flow
5. ViewModel maps domain models → UiState
6. Compose recomposes with new data
```

## 6. Error Handling Strategy

All repository methods return `kotlin.Result<T>` or a custom sealed class:

```kotlin
sealed interface DataResult<out T> {
    data class Success<T>(val data: T) : DataResult<T>
    data class Error(val exception: Throwable, val message: String? = null) : DataResult<Nothing>
    data object Loading : DataResult<Nothing>
}
```

ViewModels catch errors and map them to user-friendly UiState with error messages.
A global `SnackbarHostState` in the main scaffold is used for transient error toasts.