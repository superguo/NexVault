# NexVault — Phase 3: Advanced Features (Weeks 6–8)

## Goal
Add WalletConnect v2 DApp connectivity, NFT gallery, token swap UI, and polish
settings. By the end of Phase 3, the app is a portfolio-worthy, full-featured
Web3 wallet.

---

## Task 3.1: WalletConnect v2 Integration (feature-dapp)

**Description:** Integrate WalletConnect v2 SDK to allow connecting to any DApp
that supports the protocol.

**Subtasks:**

3.1.1. Add WalletConnect dependencies:
```kotlin
// libs.versions.toml
walletconnect-bom = { module = "com.walletconnect:android-bom"; version = "..." }
walletconnect-core = { module = "com.walletconnect:android-core" }
walletconnect-web3wallet = { module = "com.walletconnect:web3wallet" }
```

3.1.2. Create `WalletConnectModule` (Hilt):
- Initialize `CoreClient` with `WALLETCONNECT_PROJECT_ID`.
- Initialize `Web3Wallet` with metadata (app name, description, URL, icon).

3.1.3. Create `DAppRepository`:
```kotlin
interface DAppRepository {
fun getActiveSessions(): Flow<List<DAppSession>>
suspend fun pair(uri: String): Result<Unit>
suspend fun approveSession(proposalId: String, approvedChains: List<Int>, approvedMethods: List<String>): Result<DAppSession>
suspend fun rejectSession(proposalId: String): Result<Unit>
suspend fun disconnectSession(sessionTopic: String): Result<Unit>
fun getSessionRequests(): Flow<SignRequest>
suspend fun approveRequest(requestId: Long, result: String): Result<Unit>
suspend fun rejectRequest(requestId: Long, message: String): Result<Unit>
}

data class DAppSession(
   val topic: String,
   val peerName: String,
   val peerUrl: String,
   val peerIconUrl: String?,
   val chains: List<Int>,
   val connectedAt: Long,
)

sealed interface SignRequest {
   data class PersonalSign(val requestId: Long, val message: String, val peerName: String) : SignRequest
   data class SignTypedData(val requestId: Long, val data: String, val peerName: String) : SignRequest
   data class SendTransaction(val requestId: Long, val tx: TransactionParams, val peerName: String) : SignRequest
}
```

3.1.4. Implement `DAppRepositoryImpl`:
- Register `Web3Wallet.WalletDelegate` listeners.
- Convert SDK events to domain Flows using `MutableSharedFlow`.
- Sign messages using private key from secure storage.
- Sign transactions using `TransactionRepository`.

3.1.5. Create `DAppBrowserViewModel`:
- State: active sessions, pairing URI input, current session proposal (bottom sheet).
- Handle QR scan result as WalletConnect URI.

3.1.6. Create `DAppBrowserScreen`:
- Search/paste URI field.
- QR scan button (reuse `QRScannerScreen`).
- Active sessions list with disconnect button.
- Recent DApps history.

3.1.7. Create `SessionProposalBottomSheet`:
- Shows DApp name, icon, URL.
- Lists requested chains and methods.
- Approve / Reject buttons.

3.1.8. Create `SignRequestBottomSheet`:
- For personal_sign: show message.
- For eth_signTypedData: show structured data.
- For eth_sendTransaction: show transaction details (to, value, data).
- Sign / Reject buttons.
- PIN/biometric required before signing.

3.1.9. Handle deep links: `nexvault://wc?uri=...` for WalletConnect pairing.

**Acceptance:** Scan QR from Uniswap → session proposal appears → approve →
session active → swap on Uniswap triggers SendTransaction request → approve
with auth → transaction submitted. Disconnect session works.

---

## Task 3.2: NFT Gallery (feature-nft)

**Description:** Fetch and display the user's NFTs with metadata and images.

**Subtasks:**

3.2.1. Create `NftRepository`:
```kotlin
interface NftRepository {
fun getNfts(chainId: Int, address: String): Flow<List<Nft>>
suspend fun refreshNfts(chainId: Int, address: String)
suspend fun getNftDetail(contractAddress: String, tokenId: String, chainId: Int): Result<NftDetail>
}

data class Nft(
   val contractAddress: String,
   val tokenId: String,
   val chainId: Int,
   val name: String?,
   val imageUrl: String?,
   val collectionName: String?,
)

data class NftDetail(
   val contractAddress: String,
   val tokenId: String,
   val chainId: Int,
   val name: String?,
   val description: String?,
   val imageUrl: String?,
   val animationUrl: String?,
   val collectionName: String?,
   val standard: String,
   val attributes: List<NftAttribute>,
)

data class NftAttribute(val traitType: String, val value: String)
```

3.2.2. Implement `NftRepositoryImpl`:
- Use Alchemy NFT API (`getNFTsForOwner`) or fallback to on-chain enumeration
via ERC-721 `tokenOfOwnerByIndex`.
- Fetch metadata from `tokenURI`, parse JSON (handle IPFS URLs by converting
`ipfs://` to `https://ipfs.io/ipfs/`).
- Cache in Room `nfts` table.

3.2.3. Create `NFTGalleryViewModel`:
- State: list of NFTs, loading, error.
- Pull-to-refresh.

3.2.4. Create `NFTGalleryScreen`:
- `LazyVerticalGrid` with 2 columns.
- Each item: NFT image (Coil, with placeholder), name, collection name.
- Shimmer loading for images.
- Empty state if no NFTs owned.

3.2.5. Create `NFTDetailViewModel`:
- Receives contract address + token ID as nav args.
- Fetches full detail with attributes.

3.2.6. Create `NFTDetailScreen`:
- Large image (zoomable with double-tap).
- Name, collection, description.
- Attributes as chips/tags.
- Contract address, Token ID, Standard as info rows.
- "View on OpenSea" button (deep link).
- "View on Explorer" button.

**Acceptance:** NFT gallery shows owned NFTs with images. Detail screen shows
metadata and attributes. IPFS images load correctly. Empty state when no NFTs.

---

## Task 3.3: Token Swap UI (feature-swap)

**Description:** Build a token swap interface using a DEX aggregator API
(1inch or 0x). This is a UI + quote feature; the actual swap is executed as
a standard transaction.

**Subtasks:**

3.3.1. Create `SwapRepository`:
```kotlin
interface SwapRepository {
suspend fun getSwapQuote(
fromToken: String,
toToken: String,
amount: BigInteger,
chainId: Int,
slippage: Double,
): Result<SwapQuote>

   suspend fun getSwapTransaction(
       fromToken: String,
       toToken: String,
       amount: BigInteger,
       chainId: Int,
       slippage: Double,
       fromAddress: String,
   ): Result<SwapTransaction>
}

data class SwapQuote(
   val fromToken: TokenInfo,
   val toToken: TokenInfo,
   val fromAmount: BigDecimal,
   val toAmount: BigDecimal,
   val rate: BigDecimal,
   val estimatedGas: BigInteger,
   val priceImpact: Double,
)

data class SwapTransaction(
   val to: String,
   val data: String,
   val value: String,
   val gasLimit: BigInteger,
)
```

3.3.2. Implement using 1inch Swap API v6:
- GET `/quote` for quote.
- GET `/swap` for transaction data.
- Handle token approval (ERC-20 approve to 1inch router).

3.3.3. Create `SwapViewModel`:
- State: fromToken, toToken, amount, quote, isLoading, error.
- Debounced amount input → auto-fetch quote.
- Token selector (pick from user's token list).
- Slippage setting (0.5%, 1%, custom).

3.3.4. Create `SwapScreen`:
```
┌──────────────────────────────┐
│ ← Back            Swap       │
│                              │
│  From:                       │
│  ┌────────────────────────┐  │
│  │ [ETH ▼]      0.5      │  │
│  │              ≈ $825   │  │
│  └────────────────────────┘  │
│              [⇅]             │
│  To:                         │
│  ┌────────────────────────┐  │
│  │ [USDC ▼]    824.50    │  │
│  │              ≈ $824   │  │
│  └────────────────────────┘  │
│                              │
│  Rate: 1 ETH = 1,649 USDC   │
│  Price Impact: 0.05%        │
│  Slippage: 0.5%  [⚙]       │
│  Network Fee: ~$1.50        │
│                              │
│  ┌────────────────────────┐  │
│  │      Swap             │  │
│  └────────────────────────┘  │
└──────────────────────────────┘
```

3.3.5. On "Swap" tap:
- Check if ERC-20 approval needed, if so prompt approval transaction first.
- Then execute swap transaction via `TransactionRepository`.
- Show same ConfirmTransactionScreen → PIN/bio → submitted.

**Acceptance:** Swap quote loads for ETH → USDC. Rate and price impact display
correctly. Slippage is configurable. Swap execution submits a real transaction
(test on Sepolia with test tokens).

---

## Task 3.4: Settings & Network Management (feature-settings)

**Subtasks:**

3.4.1. Create `SettingsViewModel`:
- State: all settings values from DataStore.
- Actions: toggle biometric, change currency, change theme, change auto-lock.

3.4.2. Create `SettingsScreen`:
- Security section: Change PIN, Biometric toggle, Auto-lock timer selector,
Export Recovery Phrase (requires auth).
- Network section: Manage Networks, Default Network selector.
- General section: Currency selector, Theme selector (Dark/Light/System),
Language selector.
- Address Book section.
- About section: version, licenses.

3.4.3. Create `ExportWalletScreen`:
- Requires PIN/biometric to view.
- Shows mnemonic phrase (same 4×3 grid as creation).
- Warning about security.
- FLAG_SECURE on this screen (prevent screenshots).

3.4.4. Create `NetworkManagementScreen`:
- List supported networks with toggle (show/hide in chain selector).
- Future: "Add Custom Network" form (RPC URL, chain ID, symbol, explorer).

3.4.5. Create `AddressBookScreen`:
- List saved addresses with name, address, chain.
- Add / Edit / Delete entries.
- Used in Send screen's address picker.

3.4.6. Implement `ChangePinFlow`:
- Verify current PIN → Enter new PIN → Confirm new PIN.

**Acceptance:** All settings persist across restarts. Theme changes apply immediately.
Recovery phrase export requires auth. Address book CRUD works.

---

## Task 3.5: Background Sync (WorkManager)

**Description:** Periodically sync balances and pending transaction statuses.

**Subtasks:**

3.5.1. Create `BalanceSyncWorker`:
- Runs every 15 minutes (minimum periodic interval).
- Refreshes native and ERC-20 balances.
- Updates Room cache.

3.5.2. Create `PendingTxWorker`:
- Checks status of pending transactions.
- On confirmation, update Room status.
- Optional: show notification when tx confirms.

3.5.3. Schedule workers in `NexVaultApplication.onCreate()`.

3.5.4. Create `NotificationHelper` for transaction confirmation notifications.

**Acceptance:** Balances update even without opening the app. Pending transactions
eventually show as confirmed.

---

## Task 3.6: Deep Linking & Intent Handling

**Subtasks:**

3.6.1. Register intent filter for `ethereum:` URI scheme:
- Parse EIP-681 payment requests.
- Pre-fill Send screen with address, amount, token.

3.6.2. Register intent filter for `wc:` URI scheme:
- Trigger WalletConnect pairing.

3.6.3. Register intent filter for custom scheme `nexvault://`.

**Acceptance:** Tapping an `ethereum:0x1234...?value=1e18` link opens the app's
Send screen pre-filled. WalletConnect URI triggers pairing.

---

## Task 3.7: Final Polish

**Subtasks:**

3.7.1. Add app icon (adaptive icon with foreground/background layers).
Generate programmatically or use a simple vault/shield design.

3.7.2. Add splash screen using `SplashScreen` API (Android 12+).

3.7.3. Add edge-to-edge support (draw behind system bars).

3.7.4. Add haptic feedback on important actions (send confirm, PIN press).

3.7.5. Add string resources for all user-facing text (prepare for i18n).
Add `strings.xml` for English, `strings-zh.xml` for Chinese.

3.7.6. ProGuard/R8 rules for Web3j, WalletConnect, and Moshi.

3.7.7. Create a README.md for the GitHub repository.

**Acceptance:** App icon displays correctly. Splash screen shows.
Edge-to-edge works on Android 15. No ProGuard crashes in release build.

---

## Phase 3 Deliverable Checklist

- [ ] WalletConnect v2 pairing via QR works
- [ ] Session approval/rejection works
- [ ] DApp sign requests display and work (personal_sign, sendTransaction)
- [ ] Active sessions list with disconnect works
- [ ] NFT gallery displays owned NFTs
- [ ] NFT detail shows metadata and attributes
- [ ] IPFS images resolve and load
- [ ] Token swap quotes load from 1inch
- [ ] Token swap execution works on testnet
- [ ] ERC-20 approval flow for swap works
- [ ] All settings persist and apply
- [ ] Recovery phrase export with auth works
- [ ] Address book CRUD works
- [ ] Background balance sync works
- [ ] Pending transaction status updates
- [ ] Deep links for ethereum: and wc: work
- [ ] App icon, splash screen, edge-to-edge work
- [ ] Release build with R8 runs without crashes
- [ ] English and Chinese string resources present