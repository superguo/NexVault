# NexVault — Test Cases

## 1. Unit Tests (domain, data, core modules)

### 1.1 core-security

**TC-SEC-001: Mnemonic Generation**
- Given: no input
- When: `MnemonicGenerator.generate()` is called
- Then: returns a string of exactly 12 valid BIP-39 English words separated by spaces
- And: checksum is valid (re-validate with `MnemonicGenerator.validate()`)

**TC-SEC-002: Mnemonic Validation — Valid**
- Given: a known valid 12-word mnemonic
- When: `MnemonicGenerator.validate(mnemonic)` is called
- Then: returns true

**TC-SEC-003: Mnemonic Validation — Invalid Checksum**
- Given: a 12-word mnemonic with the last word changed to an incorrect word
- When: `MnemonicGenerator.validate(mnemonic)` is called
- Then: returns false

**TC-SEC-004: Mnemonic Validation — Wrong Word Count**
- Given: a 10-word string
- When: `MnemonicGenerator.validate(mnemonic)` is called
- Then: returns false

**TC-SEC-005: HD Wallet Derivation — Known Test Vector**
- Given: mnemonic = "abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about"
- When: `HdWalletDeriver.derive(mnemonic, path="m/44'/60'/0'/0/0")`
- Then: derived address equals `0x9858EfFD232B4033E47d90003D41EC34EcaEda94` (known test vector — verify exact value from BIP-44 reference)

**TC-SEC-006: Encrypt-Decrypt Round Trip**
- Given: a plaintext byte array of the mnemonic
- When: encrypted with `KeyStoreManager.encrypt()`, then decrypted with `KeyStoreManager.decrypt()`
- Then: result equals original plaintext

**TC-SEC-007: PIN Hash Verification — Correct**
- Given: PIN "123456" stored via `WalletSecureStorage.storePin()`
- When: `WalletSecureStorage.verifyPin("123456")`
- Then: returns true

**TC-SEC-008: PIN Hash Verification — Incorrect**
- Given: PIN "123456" stored
- When: `WalletSecureStorage.verifyPin("654321")`
- Then: returns false

### 1.2 domain — Use Cases

**TC-UC-001: CreateWalletUseCase — Success**
- Given: `WalletRepository.createWallet()` returns `Result.success(Wallet(...))`
- When: `CreateWalletUseCase()` is invoked
- Then: returns `Result.success` with a valid Wallet containing a non-empty address

**TC-UC-002: ImportWalletUseCase — Valid Mnemonic**
- Given: a valid 12-word mnemonic
- When: `ImportWalletUseCase(mnemonic)` is invoked
- Then: returns `Result.success` with correct derived address

**TC-UC-003: ImportWalletUseCase — Invalid Mnemonic**
- Given: an invalid mnemonic string
- When: `ImportWalletUseCase(mnemonic)` is invoked
- Then: returns `Result.failure` with `InvalidMnemonicException`

**TC-UC-004: ImportWalletUseCase — Private Key**
- Given: a valid 64-hex-char private key
- When: `ImportWalletUseCase(privateKey)` is invoked
- Then: returns `Result.success` with correct derived address

### 1.3 data — Repositories

**TC-REPO-001: TokenRepository — Refresh Balances**
- Given: mock Web3j returning 2.5 ETH balance and mock CoinGecko returning price 1650
- When: `tokenRepository.refreshBalances(chainId=1, address="0x...")`
- Then: Room TokenDao contains ETH entry with balance="2.5", fiatPrice=1650.0, fiatValue=4125.0

**TC-REPO-002: TokenRepository — Add Custom Token**
- Given: mock Web3j contract calls returning name="Uniswap", symbol="UNI", decimals=18
- When: `tokenRepository.addCustomToken(chainId=1, contractAddress="0x1f9840...")`
- Then: returns `Result.success(Token(...))` and Room contains the new token

**TC-REPO-003: TransactionRepository — Send Native Coin**
- Given: mock Web3j returning nonce=5, gas estimate, and successful send result with txHash
- When: `transactionRepository.sendNativeTransaction(to, value, gasPrice, chainId=1)`
- Then: returns `Result.success("0xtxhash...")`

**TC-REPO-004: TransactionRepository — Insufficient Balance**
- Given: wallet balance is 0.1 ETH, attempting to send 1.0 ETH
- When: `sendNativeTransaction()` validates balance
- Then: returns `Result.failure(InsufficientBalanceException)`

**TC-REPO-005: TransactionHistory — Pagination**
- Given: mock BlockExplorerApi returns 20 transactions for page 1, 5 for page 2
- When: `getTransactionHistory(chainId=1, address, page=1)` then `page=2`
- Then: page 1 returns 20 items, page 2 returns 5 items

### 1.4 core-network

**TC-NET-001: MoshiAdapter — BigDecimal Parsing**
- Given: JSON string `"123456789.123456789"`
- When: parsed by `BigDecimalAdapter`
- Then: produces `BigDecimal("123456789.123456789")` without precision loss

**TC-NET-002: Address Validation**
- Given: address `"0x1234567890abcdef1234567890abcdef12345678"`
- When: validated by `AddressAdapter`
- Then: accepted (42 chars, 0x prefix)

**TC-NET-003: Address Validation — Invalid**
- Given: address `"1234567890abcdef"` (no 0x, too short)
- When: validated
- Then: throws `JsonDataException`

### 1.5 core-database

**TC-DB-001: TokenDao — Upsert**
- Given: insert token with contractAddress="native", chainId=1, balance="1.0"
- When: upsert same token with balance="2.0"
- Then: query returns single entry with balance="2.0"

**TC-DB-002: TokenDao — Get by Chain**
- Given: 3 tokens on chainId=1 and 2 tokens on chainId=56
- When: `getTokensByChain(1)`
- Then: returns 3 tokens ordered by fiatValue DESC

**TC-DB-003: TransactionDao — Paginated Query**
- Given: 25 transactions for address "0xABC" on chain 1
- When: `getTransactions(chainId=1, address="0xABC", limit=10, offset=0)`
- Then: returns 10 newest transactions

---

## 2. ViewModel Tests

**TC-VM-001: HomeViewModel — Initial Load**
- Given: `GetPortfolioUseCase` returns portfolio with 3 tokens
- When: ViewModel initializes
- Then: `uiState` transitions from Loading to Success with 3 tokens and total value

**TC-VM-002: HomeViewModel — Refresh Error**
- Given: `GetPortfolioUseCase` throws `IOException`
- When: pull-to-refresh triggers refresh
- Then: `uiState` has `error = "Network error. Please check your connection."`
- And: previous cached data is still shown

**TC-VM-003: SendViewModel — Address Validation**
- Given: user enters "not_an_address" in the To field
- When: `uiState` updates
- Then: `addressError = "Invalid Ethereum address"`

**TC-VM-004: SendViewModel — Amount Exceeds Balance**
- Given: balance is 1.0 ETH, user enters 2.0 in amount
- When: `uiState` updates
- Then: `amountError = "Insufficient balance"`

**TC-VM-005: SendViewModel — Max Button**
- Given: balance is 1.0 ETH, estimated gas is 0.002 ETH
- When: user taps MAX
- Then: amount field is filled with "0.998"

**TC-VM-006: CreateWalletViewModel — Mnemonic Loaded**
- Given: `CreateWalletUseCase` returns success
- When: ViewModel initializes
- Then: `uiState.mnemonicWords` has exactly 12 non-empty strings

**TC-VM-007: VerifyMnemonicViewModel — Correct Order**
- Given: original words [A, B, C, D, ...]
- When: user taps words in correct order
- Then: `uiState.isVerified = true` and Confirm button is enabled

**TC-VM-008: VerifyMnemonicViewModel — Incorrect Order**
- Given: original words [A, B, C, D, ...]
- When: user taps B first (wrong)
- Then: error state triggered, selected words reset

**TC-VM-009: UnlockViewModel — Lockout After 5 Failures**
- Given: 4 failed attempts already
- When: 5th incorrect PIN entered
- Then: `uiState.isLockedOut = true` and `uiState.lockoutRemainingSeconds = 30`

**TC-VM-010: SwapViewModel — Quote Debounce**
- Given: user types "0", "0.", "0.5" rapidly (within 500ms)
- When: debounce period elapses after last input
- Then: only ONE API call is made (for "0.5")

---

## 3. UI / Compose Tests (Instrumented)

**TC-UI-001: WelcomeScreen — Buttons Exist**
- Given: WelcomeScreen is rendered
- Then: "Create New Wallet" and "Import Wallet" buttons are displayed

**TC-UI-002: CreateWalletScreen — Checkbox Enables Button**
- Given: CreateWalletScreen with mnemonic loaded
- When: checkbox is unchecked
- Then: Continue button is disabled
- When: checkbox is checked
- Then: Continue button is enabled

**TC-UI-003: PinInputField — Dots Fill**
- Given: PinInputField with empty PIN
- When: user taps 1, 2, 3
- Then: 3 dots are filled, 3 are empty

**TC-UI-004: HomeScreen — Token List**
- Given: HomeScreen with 3 tokens in state
- Then: 3 token rows are displayed with correct symbols

**TC-UI-005: HomeScreen — Pull to Refresh**
- Given: HomeScreen displayed
- When: user performs swipe-down gesture
- Then: refresh indicator appears and data reloads

**TC-UI-006: SendFormScreen — QR Button Navigation**
- Given: SendFormScreen displayed
- When: user taps QR scan icon
- Then: navigation to QR scanner screen occurs

**TC-UI-007: NFTGalleryScreen — Empty State**
- Given: no NFTs in state
- Then: empty state illustration and "No NFTs found" message displayed

**TC-UI-008: HistoryListScreen — Filter Chips**
- Given: HistoryListScreen with "All" filter active
- When: user taps "Sent" chip
- Then: only sent transactions are shown

**TC-UI-009: SettingsScreen — Theme Toggle**
- Given: current theme is Dark
- When: user selects Light
- Then: theme immediately changes to light colors

**TC-UI-010: ConfirmationDialog — Displays and Dismisses**
- Given: ConfirmationDialog shown with title "Confirm"
- When: user taps "Cancel"
- Then: dialog dismisses

---

## 4. Integration Tests

**TC-INT-001: Full Wallet Creation Flow**
- Create wallet → verify mnemonic → set PIN → reach home screen → balance loads

**TC-INT-002: Full Send Flow**
- From home → tap Send → enter address → enter amount → review → confirm with PIN → submitted

**TC-INT-003: WalletConnect Pair and Sign**
- Pair with test DApp URI → approve session → receive sign request → approve → signed

**TC-INT-004: Import and Export Round Trip**
- Create wallet → export mnemonic → delete wallet → import same mnemonic → same address

---

## 5. Security Tests

**TC-SECTEST-001: Mnemonic Not in Logs**
- Given: wallet creation flow
- Then: logcat output does NOT contain any mnemonic word sequences

**TC-SECTEST-002: Private Key Zeroed After Use**
- Given: transaction signing
- Then: byte array holding private key is zeroed after `sign()` call

**TC-SECTEST-003: FLAG_SECURE on Sensitive Screens**
- Given: CreateWalletScreen and ExportWalletScreen
- Then: window has FLAG_SECURE set (cannot screenshot)

**TC-SECTEST-004: Encrypted Storage**
- Given: mnemonic stored on disk
- Then: file contents are not readable as plaintext (binary encrypted blob)

**TC-SECTEST-005: Auto-Lock**
- Given: auto-lock set to 1 minute
- When: app is backgrounded for >1 minute and resumed
- Then: unlock screen is shown