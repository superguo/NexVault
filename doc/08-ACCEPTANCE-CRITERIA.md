# NexVault — Acceptance Criteria

## Phase 1: Foundation

### AC-1.1: Project Setup
- The project compiles with zero errors on the latest stable Android Studio.
- All modules resolve dependencies from the version catalog.
- `./gradlew detekt` and `./gradlew ktlintCheck` pass with zero issues.
- The app launches on an emulator (API 26+) and a physical device (API 30+).

### AC-1.2: Design System
- The app uses Material 3 theming with a consistent purple/indigo color scheme.
- Dark mode is the default. Light mode toggle works.
- All text uses the defined typography scale (Inter / Space Grotesk).
- No hardcoded colors, dimensions, or strings anywhere in feature modules.
- All shared composables have Compose Preview functions.

### AC-1.3: Wallet Creation
- User can create a new wallet and see a 12-word mnemonic phrase.
- Words display in a 4×3 grid, clearly numbered.
- User must acknowledge (checkbox) before proceeding.
- Verification screen requires tapping words in correct order.
- Incorrect order triggers visual feedback and reset.
- After successful verification, wallet is created and encrypted mnemonic stored.

### AC-1.4: Wallet Import
- User can import via 12-word mnemonic — app validates the phrase in real time.
- User can import via 24-word mnemonic.
- User can import via raw private key (64 hex characters).
- Invalid input shows a clear error message.
- After import, derived address matches expected (verifiable on Etherscan).

### AC-1.5: PIN & Biometric
- User sets a 6-digit PIN after wallet creation/import.
- PIN confirmation (enter twice) works; mismatch shows error.
- Biometric toggle is available on supported devices.
- Unlock screen appears on every app open.
- Biometric prompt auto-shows if enabled.
- 5 wrong PIN attempts triggers a 30-second lockout with countdown.

### AC-1.6: Auto-Lock
- When the app is backgrounded beyond the auto-lock duration, returning to the
  app shows the unlock screen.
- Default auto-lock is 5 minutes, configurable in settings.

### AC-1.7: Navigation
- App correctly routes to onboarding (no wallet), unlock (wallet exists), or
  home (already authenticated).
- Bottom navigation bar shows 5 tabs with correct icons and labels.
- Tab switching preserves scroll position and state.

---

## Phase 2: Core Features

### AC-2.1: Multi-Chain
- Chain selector in the home screen top bar shows all supported chains.
- Switching chains refreshes balances, token list, and transaction history.
- Selected chain persists across app restarts.

### AC-2.2: Token Balances
- Native coin (ETH/BNB/MATIC) balance displays correctly (verified against Etherscan).
- At least 3 popular ERC-20 tokens show balances.
- Fiat values display in the user's selected currency (USD default).
- 24-hour price change percentage is shown per token.
- Total portfolio value sums all token fiat values.
- Pull-to-refresh updates all balances.
- Data loads within 5 seconds on a reasonable connection.

### AC-2.3: Portfolio Chart
- Line chart shows portfolio value over time.
- Time range buttons (1D, 7D, 1M, 3M, ALL) change the chart data.
- Chart is smooth and responsive (no jank during range switching).

### AC-2.4: Add Custom Token
- User can add a token by pasting its contract address.
- App fetches name, symbol, decimals from chain and shows for confirmation.
- Invalid contract address shows error.
- Added token appears in the list with correct balance.

### AC-2.5: Token Detail
- Tapping a token opens a detail screen with balance, fiat value, price chart.
- Price chart supports range selection.
- Send and Receive buttons navigate to pre-filled flows.
- Recent transactions for this token are shown.

### AC-2.6: Send Transaction
- User can enter recipient address manually or via QR scan.
- User can pick from address book.
- Amount field validates against available balance.
- MAX button fills maximum amount minus estimated gas.
- Gas fee options (Slow/Normal/Fast) display with estimated time and fiat cost.
- Review screen shows all transaction details.
- Confirmation requires PIN or biometric.
- After submission, tx hash is shown with "View on Explorer" link.
- Transaction appears in history as "pending" and eventually updates to "confirmed."
- Sending ERC-20 tokens works (not just native coin).

### AC-2.7: Receive
- Receive screen shows a QR code encoding the wallet address.
- QR code scans correctly with any QR reader.
- Address is displayed in full, copyable, and shareable.
- Warning about network compatibility is displayed.

### AC-2.8: Transaction History
- History shows all past transactions for the wallet on the selected chain.
- Transactions are grouped by date with sticky headers.
- Filter chips (All, Sent, Received, Swap, Failed) work.
- Scrolling loads more transactions (pagination).
- Tapping a transaction opens a detail view with full info.
- "View on Explorer" opens the correct block explorer page.

---

## Phase 3: Advanced Features

### AC-3.1: WalletConnect
- Scanning a WalletConnect QR code initiates a pairing.
- Session proposal bottom sheet shows DApp info (name, URL, icon).
- User can approve or reject the session.
- Approved sessions appear in the active sessions list.
- DApp sign requests (personal_sign, eth_signTypedData, eth_sendTransaction)
  display correctly in a bottom sheet.
- Signing requires PIN/biometric authentication.
- Disconnecting a session works and the DApp reflects it.
- WalletConnect deep links (`wc:...`) work from other apps.

### AC-3.2: NFT Gallery
- Gallery displays all NFTs owned on the selected chain.
- Images load (including IPFS-hosted images).
- Tapping an NFT opens a detail screen with metadata and attributes.
- Empty state is shown when the user owns no NFTs.
- Pull-to-refresh updates the NFT list.

### AC-3.3: Token Swap
- User can select "from" and "to" tokens.
- Entering an amount fetches a swap quote with exchange rate and price impact.
- Quote updates automatically on amount change (debounced).
- Slippage is configurable (default 0.5%).
- "Swap" triggers approval (if needed) then swap transaction.
- Transaction confirmation flow is the same as send.
- Swap works on testnet with test tokens.

### AC-3.4: Settings
- All settings (biometric, auto-lock, currency, theme, default network) persist.
- Change PIN flow works (verify old → set new → confirm new).
- Export recovery phrase requires authentication and shows FLAG_SECURE screen.
- Network management allows toggling chain visibility.
- Address book supports create, edit, delete of saved addresses.

### AC-3.5: Background Sync
- Balances update even without opening the app (within WorkManager constraints).
- Pending transactions update to confirmed/failed in the background.

### AC-3.6: Polish
- App icon is visible and correct on the launcher.
- Splash screen displays on cold start (Android 12+).
- Edge-to-edge draws behind navigation and status bars.
- Release build (R8 minified) runs without crashes.

---

## Cross-Cutting Acceptance Criteria

### Performance
- Home screen renders (first meaningful paint) within 2 seconds on mid-range device.
- Token list scrolls at 60fps with no dropped frames.
- App cold start to interactive in under 3 seconds.
- APK size under 25MB (release, minified).

### Security
- Mnemonic is never logged, displayed in plaintext outside designated screens,
  or stored unencrypted.
- Private key is held in memory only during signing, then byte array is zeroed.
- Sensitive screens have FLAG_SECURE.
- PIN is stored as a salted hash, never in plaintext.
- Network requests to RPCs and APIs use HTTPS only.

### Reliability
- App does not crash on any normal user flow.
- Network errors show user-friendly messages with retry options.
- App handles process death gracefully (state restored via SavedStateHandle / Room).
- No memory leaks (verified with LeakCanary in debug builds).

### Accessibility
- All interactive elements have minimum 48dp touch targets.
- All images/icons have contentDescription.
- App is navigable via TalkBack.
- Text respects system font scale.

### Code Quality
- Detekt and ktlint pass with zero issues.
- All public functions and classes have KDoc comments.
- Test coverage ≥ 70% for domain and data layers.
- No TODO/FIXME left unresolved in main branch.
