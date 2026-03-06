# NexVault — Web3 Crypto Wallet for Android

## 1. Project Summary

NexVault is a noncustodial, multichain crypto wallet Android application designed for
personal use and portfolio demonstration. It showcases Android development best practices
aligned with Web3 mobile wallet development: asset management, token transfers,
transaction history, DApp connectivity via WalletConnect, NFT display, and
security-first key management.

## 2. App Identity

- **App Name:** NexVault
- **Package Name:** `com.nexvault.wallet`
- **Application ID:** `com.nexvault.wallet`
- **Min SDK:** 26 (Android 8.0)
- **Target SDK:** 34 (Android 14)
- **Language:** Kotlin (100%)
- **UI Framework:** Jetpack Compose with Material 3
- **Build System:** Gradle with Kotlin DSL (libs.versions.toml catalog)

## 3. Supported Chains (Initial Scope)

| Chain            | Chain ID | RPC Source        | Token Standard |
|------------------|----------|-------------------|----------------|
| Ethereum Mainnet | 1        | Infura / Alchemy  | ERC-20 / 721   |
| Ethereum Sepolia | 11155111 | Infura / Alchemy  | ERC-20 / 721   |
| BNB Smart Chain  | 56       | Public RPC        | BEP-20 / 721   |
| Polygon          | 137      | Public RPC        | ERC-20 / 721   |

Sepolia testnet is included so you can demo the app without real funds.

## 4. High-Level Feature Map

### Phase 1 — Foundation (Weeks 1–2)
- Wallet creation (mnemonic generation, import)
- Secure key storage (encrypted with AndroidKeyStore)
- PIN / Biometric authentication
- Single-chain ETH balance display

### Phase 2 — Core Features (Weeks 3–5)
- Multi-chain switching
- ERC-20 token list and balances
- Send / Receive tokens (QR code)
- Transaction history
- Fiat price conversion (CoinGecko API)
- Portfolio value chart

### Phase 3 — Advanced Features (Weeks 6–8)
- WalletConnect v2 DApp browser
- NFT gallery (ERC-721 / ERC-1155 display)
- In-app token swap UI (1inch or 0x API)
- Push notification for confirmed transactions (optional)
- Settings, network management, address book

## 5. Project Structure (Multi-Module)

```
NexVault/
├── app/                          # Application module, DI wiring, navigation
├── core/
│   ├── core-ui/                  # Shared Compose components, theme, design tokens
│   ├── core-common/              # Utility extensions, constants, result wrappers
│   ├── core-network/             # Retrofit setup, interceptors, API models
│   ├── core-database/            # Room database, DAOs, entities
│   ├── core-datastore/           # Preferences DataStore (settings, selected chain)
│   └── core-security/            # Encryption, keystore wrapper, biometric helpers
├── feature/
│   ├── feature-onboarding/       # Create / import wallet screens
│   ├── feature-home/             # Dashboard, balance, portfolio chart
│   ├── feature-tokens/           # Token list, token detail
│   ├── feature-send/             # Send transaction flow
│   ├── feature-receive/          # Receive address + QR
│   ├── feature-history/          # Transaction history list + detail
│   ├── feature-dapp/             # DApp browser, WalletConnect session
│   ├── feature-nft/              # NFT gallery + detail
│   ├── feature-swap/             # Token swap UI
│   └── feature-settings/         # Settings, network mgmt, address book
├── domain/                       # Use cases, repository interfaces, domain models
├── data/                         # Repository implementations, mappers
├── build-logic/                  # Convention plugins for consistent module config
├── gradle/
│   └── libs.versions.toml
├── build.gradle.kts
└── settings.gradle.kts
```

## 6. Git Branching Strategy

- `main` — stable, passing all tests
- `develop` — integration branch
- `feature/<phase>-<name>` — per-task branches (e.g., `feature/p1-wallet-creation`)
- Each phase ends with a merge to `develop`, then a tagged release to `main`.

## 7. Environment & Secrets

Store API keys in `local.properties` (git-ignored) and expose via BuildConfig:

```properties
# local.properties
INFURA_API_KEY=your_key_here
ALCHEMY_API_KEY=your_key_here
COINGECKO_API_KEY=your_key_here        # free tier is fine
WALLETCONNECT_PROJECT_ID=your_id_here
```

Gradle reads them:

```kotlin
// app/build.gradle.kts
android {
    defaultConfig {
        buildConfigField("String", "INFURA_API_KEY",
            "\"${project.findProperty("INFURA_API_KEY") ?: ""}\"")
        // ... repeat for others
    }
}
```