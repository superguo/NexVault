# NexVault — UI/UX Design Specification

## 1. Design System

### 1.1 Theme

NexVault uses Material 3 dynamic color with a custom seed color and full dark mode
support. The default experience is dark mode (standard in crypto/finance apps).

**Seed Color:** `#6C5CE7` (a vivid indigo-purple)

```kotlin
// core-ui/src/main/java/com/nexvault/wallet/ui/theme/Color.kt

// Light scheme (auto-generated from seed via Material Theme Builder)
val md_theme_light_primary = Color(0xFF5B52A4)
val md_theme_light_onPrimary = Color(0xFFFFFFFF)
val md_theme_light_primaryContainer = Color(0xFFE4DFFF)
// ... full palette generated

// Dark scheme
val md_theme_dark_primary = Color(0xFFC6BFFF)
val md_theme_dark_onPrimary = Color(0xFF2D2174)
val md_theme_dark_primaryContainer = Color(0xFF44398B)
val md_theme_dark_surface = Color(0xFF121015)
val md_theme_dark_surfaceVariant = Color(0xFF1E1B2E)
// ... full palette generated
```

**Typography:** Use Google Fonts `Inter` for body text and `Space Grotto` for headings
and numerical values (monospaced-feel for balances).

**Shape:** Rounded corners — small: 8dp, medium: 16dp, large: 24dp.

### 1.2 Design Tokens (Spacing & Elevation)

```kotlin
object NexVaultSpacing {
    val xxs = 2.dp
    val xs = 4.dp
    val sm = 8.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
    val xxl = 48.dp
}
```

### 1.3 Shared Components (core-ui)

Reusable composables to create in core-ui:

- `NexVaultTopAppBar` — customized top bar with optional back arrow and action icons
- `NexVaultBottomNavBar` — 5-tab bottom bar with icons and labels
- `NexVaultButton` / `NexVaultOutlinedButton` — styled buttons
- `NexVaultCard` — card with consistent elevation and shape
- `NexVaultTextField` — styled text field with error state
- `NexVaultLoadingShimmer` — shimmer placeholder for lists
- `NexVaultEmptyState` — illustration + message for empty screens
- `NexVaultErrorState` — retry button + error message
- `TokenIcon` — circle with token logo (Coil) and fallback initial
- `ChainBadge` — small colored badge indicating network
- `BalanceText` — formatted balance with currency symbol and dynamic font sizing
- `AddressChip` — truncated address `0x1234...abcd` with copy button
- `QRCodeImage` — ZXing-generated QR code composable
- `ConfirmationDialog` — custom modal dialog (replaces system alert)
- `PinInputField` — 6-dot PIN entry composable
- `BiometricPromptWrapper` — launches biometric prompt from Compose

## 2. Screen-by-Screen Design

### 2.1 Onboarding Flow

#### WelcomeScreen
```
┌──────────────────────────────┐
│                              │
│     [Lottie: Vault anim]    │
│                              │
│        NexVault              │
│   Your keys. Your crypto.   │
│                              │
│  ┌────────────────────────┐  │
│  │   Create New Wallet    │  │
│  └────────────────────────┘  │
│  ┌────────────────────────┐  │
│  │   Import Wallet        │  │
│  └────────────────────────┘  │
│                              │
└──────────────────────────────┘
```

- Two primary actions.
- Lottie animation of a vault opening loop.

#### CreateWalletScreen
```
┌──────────────────────────────┐
│ ← Back           Step 1 of 3│
│                              │
│   Your Recovery Phrase       │
│   Write these 12 words down  │
│   in order. Never share them.│
│                              │
│  ┌──────┐ ┌──────┐ ┌──────┐ │
│  │1 apple│ │2 brave│ │3 crane││
│  ├──────┤ ├──────┤ ├──────┤ │
│  │4 delta│ │5 eagle│ │6 frost││
│  ├──────┤ ├──────┤ ├──────┤ │
│  │7 grape│ │8 house│ │9 ivory││
│  ├──────┤ ├──────┤ ├──────┤ │
│  │10jump │ │11king │ │12lamp ││
│  └──────┘ └──────┘ └──────┘ │
│                              │
│  ☐ I have written it down    │
│  ┌────────────────────────┐  │
│  │        Continue         │  │
│  └────────────────────────┘  │
└──────────────────────────────┘
```

- 12-word mnemonic displayed in a 4×3 grid.
- Checkbox acknowledgment required before Continue is enabled.
- Copy button available but with a warning dialog.
- Screenshot detection: show warning if user tries to screenshot.

#### VerifyMnemonicScreen
```
┌──────────────────────────────┐
│ ← Back           Step 2 of 3│
│                              │
│   Verify Your Phrase         │
│   Tap words in correct order │
│                              │
│  Selected:                   │
│  [1 apple] [2 brave] [?]    │
│                              │
│  Available (shuffled):       │
│  [crane] [frost] [delta]    │
│  [grape] [eagle] [house]    │
│  [ivory] [jump]  [king]     │
│  [lamp]                      │
│                              │
│  ┌────────────────────────┐  │
│  │       Confirm          │  │  (disabled until all correct)
│  └────────────────────────┘  │
└──────────────────────────────┘
```

#### ImportWalletScreen
```
┌──────────────────────────────┐
│ ← Back                       │
│                              │
│   Import Wallet              │
│                              │
│  ┌────────────────────────┐  │
│  │ Enter your 12 or 24    │  │
│  │ word recovery phrase   │  │
│  │ separated by spaces... │  │
│  └────────────────────────┘  │
│                              │
│  --- OR ---                  │
│                              │
│  ┌────────────────────────┐  │
│  │ Enter private key      │  │
│  └────────────────────────┘  │
│                              │
│  ┌────────────────────────┐  │
│  │       Import           │  │
│  └────────────────────────┘  │
└──────────────────────────────┘
```

#### SetPinScreen
```
┌──────────────────────────────┐
│ ← Back           Step 3 of 3│
│                              │
│   Set Your PIN               │
│                              │
│      ● ● ● ○ ○ ○            │
│                              │
│   ┌───┬───┬───┐             │
│   │ 1 │ 2 │ 3 │             │
│   ├───┼───┼───┤             │
│   │ 4 │ 5 │ 6 │             │
│   ├───┼───┼───┤             │
│   │ 7 │ 8 │ 9 │             │
│   ├───┼───┼───┤             │
│   │   │ 0 │ ⌫ │             │
│   └───┴───┴───┘             │
│                              │
│   Enable Biometric? [Toggle] │
└──────────────────────────────┘
```

- 6-digit PIN.
- After first entry, ask to confirm PIN.
- Optional biometric toggle.

### 2.2 Unlock Screen

```
┌──────────────────────────────┐
│                              │
│       [App Icon]             │
│       NexVault               │
│                              │
│      ○ ○ ○ ○ ○ ○            │
│                              │
│   ┌───┬───┬───┐             │
│   │ 1 │ 2 │ 3 │             │
│   ├───┼───┼───┤             │
│   │ 4 │ 5 │ 6 │             │
│   ├───┼───┼───┤             │
│   │ 7 │ 8 │ 9 │             │
│   ├───┼───┼───┤             │
│   │   │ 0 │ ⌫ │             │
│   └───┴───┴───┘             │
│                              │
│   [Use Biometric]            │
└──────────────────────────────┘
```

- Auto-launches biometric prompt if enabled.
- 5 wrong attempts → 30s lockout with timer.

### 2.3 Home Screen (Dashboard)

```
┌──────────────────────────────┐
│ [ChainSelector ▼]   [⚙] [🔔]│
│                              │
│   Total Balance              │
│   $12,345.67                 │
│   +2.34% ↑ (24h)            │
│                              │
│   ┌─────────────────────┐    │
│   │  Portfolio chart     │    │
│   │  (7d line chart)     │    │
│   │  ~~~~~~~~~/~~        │    │
│   └─────────────────────┘    │
│   [1D] [7D] [1M] [3M] [ALL] │
│                              │
│   ┌──────┐┌──────┐┌──────┐  │
│   │ Send ││Receive││ Swap │  │
│   └──────┘└──────┘└──────┘  │
│                              │
│   Tokens                     │
│   ┌──────────────────────┐   │
│   │ Ξ ETH       2.5 ETH │   │
│   │              $4,125  │   │
│   ├──────────────────────┤   │
│   │ 🔵 USDC   1,000 USDC│   │
│   │              $1,000  │   │
│   ├──────────────────────┤   │
│   │ 🟡 UNI      50 UNI  │   │
│   │               $320   │   │
│   └──────────────────────┘   │
│   + Add Token                │
│                              │
├──────────────────────────────┤
│ 🏠Home │📜History│🌐DApp│🖼NFT│⚙Set│
└──────────────────────────────┘
```

- Chain selector dropdown at top-left (Ethereum, BSC, Polygon).
- Animated balance counter on load.
- Pull-to-refresh to reload balances.
- Token list sorted by fiat value descending.
- "Add Token" opens a search dialog for custom ERC-20 contract addresses.

### 2.4 Token Detail Screen

```
┌──────────────────────────────┐
│ ← Back              ETH     │
│                              │
│   [Token Icon]               │
│   Ethereum                   │
│   2.5 ETH ≈ $4,125.00       │
│                              │
│   ┌─────────────────────┐    │
│   │  Price chart (30d)  │    │
│   │  ~~~/\~~~~          │    │
│   └─────────────────────┘    │
│   [1D] [7D] [1M] [1Y]       │
│                              │
│   Price: $1,650.00           │
│   24h Change: +2.3%         │
│   Market Cap: $198B          │
│                              │
│   ┌──────┐  ┌──────┐        │
│   │ Send │  │Receive│        │
│   └──────┘  └──────┘        │
│                              │
│   Recent Transactions        │
│   ├─ Sent 0.5 ETH   Mar 01  │
│   ├─ Received 1 ETH Feb 28  │
│   └─ See All →               │
└──────────────────────────────┘
```

### 2.5 Send Transaction Flow

```
Step 1: SendFormScreen
┌──────────────────────────────┐
│ ← Back          Send ETH    │
│                              │
│  To Address:                 │
│  ┌────────────────────[📷]┐  │
│  │ 0x...                  │  │
│  └────────────────────────┘  │
│  [Address Book ▼]            │
│                              │
│  Amount:                     │
│  ┌────────────────[MAX]───┐  │
│  │ 0.5                    │  │
│  └────────────────────────┘  │
│  ≈ $825.00                   │
│  Available: 2.5 ETH         │
│                              │
│  Network Fee:                │
│  ○ Slow   ~$0.50  ~5 min    │
│  ● Normal ~$1.20  ~2 min    │
│  ○ Fast   ~$2.50  ~30 sec   │
│                              │
│  ┌────────────────────────┐  │
│  │       Review           │  │
│  └────────────────────────┘  │
└──────────────────────────────┘

Step 2: ConfirmTransactionScreen
┌──────────────────────────────┐
│ ← Back     Confirm Transfer │
│                              │
│  Sending        0.5 ETH     │
│  To             0x12...cd   │
│  Network Fee    0.00072 ETH │
│  Total          0.50072 ETH │
│                              │
│  ┌────────────────────────┐  │
│  │   Confirm & Send       │  │
│  └────────────────────────┘  │
│                              │
│  (Triggers PIN / Biometric)  │
└──────────────────────────────┘

Step 3: TransactionSubmittedScreen
┌──────────────────────────────┐
│                              │
│     [Lottie: Checkmark]     │
│     Transaction Submitted    │
│                              │
│  Tx Hash: 0xabc...789       │
│  [View on Explorer]          │
│                              │
│  ┌────────────────────────┐  │
│  │      Done              │  │
│  └────────────────────────┘  │
└──────────────────────────────┘
```

### 2.6 Receive Screen

```
┌──────────────────────────────┐
│ ← Back        Receive ETH   │
│                              │
│   ┌────────────────────┐     │
│   │                    │     │
│   │   [QR Code of      │     │
│   │    wallet address]  │     │
│   │                    │     │
│   └────────────────────┘     │
│                              │
│   Your Ethereum Address      │
│   0x1234567890abcdef...cdef  │
│                              │
│   [Copy Address]  [Share]    │
│                              │
│   ⚠ Only send ETH and       │
│   ERC-20 tokens on Ethereum  │
│   network to this address.   │
└──────────────────────────────┘
```

### 2.7 Transaction History Screen

```
┌──────────────────────────────┐
│   History          [Filter▼] │
│                              │
│   Today                      │
│   ┌──────────────────────┐   │
│   │ ↑ Sent 0.5 ETH      │   │
│   │   To: 0xab...cd      │   │
│   │   Mar 06 · Confirmed │   │
│   ├──────────────────────┤   │
│   │ ↓ Received 1,000 USDC│   │
│   │   From: 0xef...12    │   │
│   │   Mar 06 · Confirmed │   │
│   └──────────────────────┘   │
│                              │
│   Yesterday                  │
│   ┌──────────────────────┐   │
│   │ 🔄 Swap              │   │
│   │   0.3 ETH → 495 USDC│   │
│   │   Mar 05 · Confirmed │   │
│   └──────────────────────┘   │
│                              │
│   (Paginated infinite scroll)│
├──────────────────────────────┤
│ 🏠Home │📜History│🌐DApp│🖼NFT│⚙Set│
└──────────────────────────────┘
```

- Filter by: All, Sent, Received, Swap, Failed.
- Grouped by date.
- Color coded: green = received, red = sent, blue = swap.

### 2.8 DApp Browser Screen

```
┌──────────────────────────────┐
│   DApps                      │
│                              │
│  ┌────────────────────────┐  │
│  │ 🔍 Search or paste URL │  │
│  └────────────────────────┘  │
│                              │
│  ┌────────────────────┐      │
│  │ [📷 Scan QR]       │      │
│  │  Scan WalletConnect │      │
│  │  QR code            │      │
│  └────────────────────┘      │
│                              │
│  Active Sessions             │
│  ┌──────────────────────┐    │
│  │ 🟢 Uniswap           │    │
│  │   Connected · Ethereum│    │
│  │              [Disconnect]│ │
│  └──────────────────────┘    │
│                              │
│  Recent DApps                │
│  [Uniswap] [OpenSea] [Aave] │
│                              │
├──────────────────────────────┤
│ 🏠Home │📜History│🌐DApp│🖼NFT│⚙Set│
└──────────────────────────────┘
```

- WalletConnect v2 pairing via QR scan or pasted URI.
- Session approval dialog appears as a bottom sheet.
- Transaction signing requests show ConfirmTransactionScreen.

### 2.9 NFT Gallery Screen

```
┌──────────────────────────────┐
│   My NFTs              [Grid]│
│                              │
│  ┌─────────┐ ┌─────────┐    │
│  │         │ │         │    │
│  │ [NFT 1] │ │ [NFT 2] │    │
│  │  img    │ │  img    │    │
│  │         │ │         │    │
│  │CryptoPunk│ │ BAYC   │    │
│  │  #1234  │ │ #5678  │    │
│  └─────────┘ └─────────┘    │
│  ┌─────────┐ ┌─────────┐    │
│  │         │ │         │    │
│  │ [NFT 3] │ │ [NFT 4] │    │
│  │  img    │ │  img    │    │
│  └─────────┘ └─────────┘    │
│                              │
│  (LazyVerticalGrid, 2 cols)  │
├──────────────────────────────┤
│ 🏠Home │📜History│🌐DApp│🖼NFT│⚙Set│
└──────────────────────────────┘
```

### 2.10 NFT Detail Screen

```
┌──────────────────────────────┐
│ ← Back                       │
│                              │
│  ┌────────────────────────┐  │
│  │                        │  │
│  │    [Large NFT Image]   │  │
│  │                        │  │
│  └────────────────────────┘  │
│                              │
│  CryptoPunk #1234            │
│  CryptoPunks Collection      │
│                              │
│  Properties                  │
│  [Type: Male] [Hair: Mohawk] │
│  [Eyes: Blue]                │
│                              │
│  Contract: 0xb47e...3bbb    │
│  Token ID: 1234              │
│  Standard: ERC-721           │
│                              │
│  [View on OpenSea]           │
│  [View on Explorer]          │
└──────────────────────────────┘
```

### 2.11 Settings Screen

```
┌──────────────────────────────┐
│   Settings                   │
│                              │
│  Security                    │
│  ├─ Change PIN               │
│  ├─ Biometric Lock    [ON]   │
│  ├─ Auto-Lock Timer   [5min] │
│  └─ Export Recovery Phrase    │
│                              │
│  Network                     │
│  ├─ Manage Networks          │
│  └─ Default Network  [ETH ▼] │
│                              │
│  General                     │
│  ├─ Currency          [USD ▼]│
│  ├─ Theme        [Dark ▼]   │
│  └─ Language     [English ▼] │
│                              │
│  Address Book                │
│  └─ Manage Saved Addresses   │
│                              │
│  About                       │
│  └─ Version 1.0.0            │
│                              │
├──────────────────────────────┤
│ 🏠Home │📜History│🌐DApp│🖼NFT│⚙Set│
└──────────────────────────────┘
```

## 3. Animation & Micro-Interactions

- **Screen transitions:** Shared element transitions for token icon → token detail.
- **Balance loading:** AnimatedContent with number counter effect.
- **Pull-to-refresh:** Material 3 pull refresh indicator.
- **Button press:** Scale animation on tap (0.95x).
- **Mnemonic reveal:** Words fade in sequentially (staggered animation).
- **Transaction submitted:** Lottie checkmark animation.
- **Error shake:** Horizontal shake on invalid PIN entry.
- **Skeleton loading:** Shimmer effect for all list items while data loads.

## 4. Accessibility

- All icons have contentDescription.
- Minimum touch target: 48dp.
- Sufficient color contrast ratios (WCAG AA).
- Support for TalkBack screen reader.
- Dynamic font sizing (sp-based, respects system font scale).
