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

| Library            | Version (actual)   | Purpose                                          |
|--------------------|--------------------|--------------------------------------------------|
| Retrofit 2         | 3.0.0              | REST API client (CoinGecko, 1inch, NFT metadata) |
| OkHttp 5           | 5.3.2              | HTTP client, logging interceptor                 |
| Moshi              | 1.15.2             | JSON serialization (kotlin-codegen)              |
| Web3j (Android)    | 5.0.2              | Ethereum JSON-RPC, transaction signing, ABI      |
| WalletConnect v2 BOM | 1.35.2           | DApp connection protocol                         |
| kotlinx-serialization | 1.10.0         | Backup serialization for data classes            |
| kethereum          | 0.86.0             | BIP39/BIP44 implementation                       |

### 2.4 Security

| Library / API             | Version           | Purpose                                              |
|---------------------------|-------------------|------------------------------------------------------|
| AndroidKeyStore           | –                 | Hardware-backed key wrapping for mnemonic encryption  |
| Tink (Google)             | 1.20.0            | AEAD encryption primitives wrapping KeyStore          |
| BIP39 (via kethereum)     | 0.86.0            | Mnemonic generation / validation                     |
| BIP32/BIP44 (via kethereum)| 0.86.0           | HD wallet derivation                                 |
| SQLCipher (optional)      | –                 | Encrypted Room database on-disk                      |

### 2.5 UI & Media

| Library       | Version  | Purpose                                     |
|---------------|----------|---------------------------------------------|
| Coil Compose  | 2.7.0    | Async image loading (NFT thumbnails, icons) |
| Lottie Compose| 6.7.1    | Animated illustrations (onboarding, success)|
| Vico          | 3.0.3    | Chart library for portfolio value over time |
| ZXing / ML Kit| 3.5.3 / 17.3.0 | QR code generation and scanning |
| Accompanist   | 0.37.3   | System UI controller, permissions            |
| Shimmer       | 1.2.0    | Loading placeholder effects                  |

### 2.6 Testing

| Library                | Version  | Purpose                          |
|------------------------|----------|----------------------------------|
| JUnit 5                | 6.0.3    | Unit tests                       |
| Mockk                  | 1.14.9   | Kotlin-first mocking             |
| Turbine                | 1.2.1    | Flow testing                     |
| Compose UI Test        | 1.10.4   | Instrumented Compose UI tests    |
| Truth                  | 1.4.5    | Fluent assertions                |
| Hilt Testing           | 2.59.2   | DI-aware instrumented tests      |
| Robolectric            | 4.16.1   | JVM-based Android unit tests     |

### 2.7 Code Quality

| Tool          | Version  | Purpose                       |
|---------------|----------|-------------------------------|
| Detekt        | 1.23.8   | Static analysis for Kotlin    |
| ktlint        | 14.1.0   | Code formatting               |
| Spotless      | 8.3.0    | Formatting enforcement        |
| LeakCanary    | 2.14     | Memory leak detection (debug) |

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