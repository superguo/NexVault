# NexVault — Phase 2: Core Features (Weeks 3–5)

## Goal
Implement multichain support, token balances, fiat prices, send/receive functionality,
transaction history, and the portfolio chart. By the end of Phase 2, the app is a
functional crypto wallet for daily use.

---

## Task 2.1: Network Module (core-network)

**Description:** Set up Retrofit, OkHttp, Web3j, and all API service interfaces.

**Subtasks:**

2.1.1. Create `OkHttpClient` with:
- `HttpLoggingInterceptor` (DEBUG only)
- `ChainIdInterceptor` that adds chain-specific base URL
- 30-second timeouts
- Certificate pinning for Infura/Alchemy (optional but recommended)

2.1.2. Create `MoshiInstance` with custom adapters:
- `BigDecimalAdapter`
- `BigIntegerAdapter`
- `AddressAdapter` (validates 0x prefix, 42 chars)

2.1.3. Create Retrofit API service interfaces:

       ```kotlin
       // CoinGecko price API
       interface CoinGeckoApi {
           @GET("simple/price")
           suspend fun getTokenPrices(
               @Query("ids") ids: String,
               @Query("vs_currencies") vsCurrencies: String,
               @Query("include_24hr_change") include24hChange: Boolean = true,
           ): Map<String, TokenPriceResponse>

           @GET("coins/{id}/market_chart")
           suspend fun getPriceHistory(
               @Query("vs_currency") vsCurrency: String,
               @Query("days") days: Int,
           ): PriceHistoryResponse
       }

       // Etherscan-like API for transaction history
       interface BlockExplorerApi {
           @GET("api")
           suspend fun getTransactions(
               @Query("module") module: String = "account",
               @Query("action") action: String = "txlist",
               @Query("address") address: String,
               @Query("startblock") startBlock: Long = 0,
               @Query("sort") sort: String = "desc",
               @Query("apikey") apiKey: String,
           ): EtherscanResponse<List<TransactionDto>>

           @GET("api")
           suspend fun getTokenTransfers(
               @Query("module") module: String = "account",
               @Query("action") action: String = "tokentx",
               @Query("address") address: String,
               @Query("sort") sort: String = "desc",
               @Query("apikey") apiKey: String,
           ): EtherscanResponse<List<TokenTransferDto>>
       }
       ```

2.1.4. Create `Web3jProvider`:
```kotlin
class Web3jProvider @Inject constructor() {
private val instances = ConcurrentHashMap<Int, Web3j>()

   fun getWeb3j(chain: Chain): Web3j {
       return instances.getOrPut(chain.chainId) {
           Web3j.build(HttpService(chain.rpcUrl))
       }
   }
}
```

2.1.5. Provide all services via Hilt `NetworkModule`.

**Acceptance:** All API interfaces compile. Web3j connects to Sepolia and returns
block number. CoinGecko returns BTC/ETH prices.

---

## Task 2.2: Database Module (core-database)

**Description:** Set up Room database with tables for tokens, transactions, and NFTs.

**Subtasks:**

2.2.1. Create Room entities:

       ```kotlin
       @Entity(tableName = "tokens",
               primaryKeys = ["contractAddress", "chainId"])
       data class TokenEntity(
           val contractAddress: String,   // "native" for native coin
           val chainId: Int,
           val symbol: String,
           val name: String,
           val decimals: Int,
           val logoUrl: String?,
           val balance: String,           // BigDecimal as String
           val fiatPrice: Double?,
           val fiatValue: Double?,
           val priceChange24h: Double?,
           val isCustom: Boolean,
           val sortOrder: Int,
           val lastUpdated: Long,
       )

       @Entity(tableName = "transactions",
               primaryKeys = ["txHash", "chainId"])
       data class TransactionEntity(
           val txHash: String,
           val chainId: Int,
           val fromAddress: String,
           val toAddress: String,
           val value: String,
           val gasUsed: String?,
           val gasPrice: String?,
           val tokenSymbol: String?,
           val tokenContractAddress: String?,
           val tokenDecimals: Int?,
           val blockNumber: Long,
           val timestamp: Long,
           val status: Int,               // 0 = pending, 1 = confirmed, 2 = failed
           val type: String,              // "send", "receive", "swap", "contract"
       )

       @Entity(tableName = "nfts",
               primaryKeys = ["contractAddress", "tokenId", "chainId"])
       data class NftEntity(
           val contractAddress: String,
           val tokenId: String,
           val chainId: Int,
           val name: String?,
           val description: String?,
           val imageUrl: String?,
           val animationUrl: String?,
           val collectionName: String?,
           val standard: String,          // "ERC-721" or "ERC-1155"
           val attributes: String?,       // JSON string of attributes
           val lastUpdated: Long,
       )

       @Entity(tableName = "address_book")
       data class AddressBookEntity(
           @PrimaryKey(autoGenerate = true) val id: Int = 0,
           val name: String,
           val address: String,
           val chainId: Int?,
           val createdAt: Long,
       )
       ```

2.2.2. Create DAOs:
```kotlin
@Dao
interface TokenDao {
@Query("SELECT * FROM tokens WHERE chainId = :chainId ORDER BY fiatValue DESC")
fun getTokensByChain(chainId: Int): Flow<List<TokenEntity>>

   @Upsert
   suspend fun upsertTokens(tokens: List<TokenEntity>)

   @Query("DELETE FROM tokens WHERE contractAddress = :address AND chainId = :chainId")
   suspend fun deleteToken(address: String, chainId: Int)

   @Query("SELECT SUM(fiatValue) FROM tokens WHERE chainId = :chainId")
   fun getTotalFiatValue(chainId: Int): Flow<Double?>
}

@Dao
interface TransactionDao {
   @Query("SELECT * FROM transactions WHERE chainId = :chainId AND (fromAddress = :address OR toAddress = :address) ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
   suspend fun getTransactions(chainId: Int, address: String, limit: Int, offset: Int): List<TransactionEntity>

   @Upsert
   suspend fun upsertTransactions(txs: List<TransactionEntity>)
}
// ... similarly for NftDao, AddressBookDao
```

2.2.3. Create `NexVaultDatabase` abstract class with all DAOs.

2.2.4. Provide via Hilt `DatabaseModule`.

**Acceptance:** All DAO queries execute on an in-memory test database.
Upsert correctly updates existing rows.

---

## Task 2.3: Chain Management

**Description:** Implement multichain support and chain switching.

**Subtasks:**

2.3.1. Create `ChainRepository`:
```kotlin
interface ChainRepository {
    fun getSupportedChains(): Flow<List<Chain>>
    fun getSelectedChain(): Flow<Chain>
    suspend fun setSelectedChain(chainId: Int)
    fun getChainById(chainId: Int): Chain?
}
```

2.3.2. Implement with hardcoded chain list (Ethereum, Sepolia, BSC, Polygon) and
DataStore for selected chain.

2.3.3. Create `ChainSelectorDropdown` composable — shown in HomeScreen top bar.
On selection change, refresh all data.

**Acceptance:** Switching chains updates the entire app's data context.
Selected chain persists across app restarts.

---

## Task 2.4: Token Balance & Portfolio (feature-home)

**Description:** Implement the home dashboard showing total balance, portfolio chart,
and token list.

**Subtasks:**

2.4.1. Create domain models:
```kotlin
data class Token(
val contractAddress: String,
val chainId: Int,
val symbol: String,
val name: String,
val decimals: Int,
val logoUrl: String?,
val balance: BigDecimal,
val fiatPrice: Double?,
val fiatValue: Double?,
val priceChange24h: Double?,
)

data class Portfolio(
   val totalFiatValue: Double,
   val change24hPercent: Double,
   val tokens: List<Token>,
   val chartData: List<PricePoint>,
)

data class PricePoint(val timestamp: Long, val value: Double)
```

2.4.2. Create `TokenRepository`:
```kotlin
interface TokenRepository {
    fun getTokensWithBalances(chainId: Int, address: String): Flow<List<Token>>
    suspend fun refreshBalances(chainId: Int, address: String)
    suspend fun addCustomToken(chainId: Int, contractAddress: String): Result<Token>
    suspend fun removeToken(chainId: Int, contractAddress: String)
}
```

2.4.3. Implement `TokenRepositoryImpl`:
- `refreshBalances()`:
a. Fetch native coin balance via `web3j.ethGetBalance()`.
b. For each tracked ERC-20, call `balanceOf` via Web3j contract call.
c. Fetch fiat prices from CoinGecko for all token IDs.
d. Update Room database.
- `getTokensWithBalances()`: return Room DAO Flow mapped to domain model.
- `addCustomToken()`: call ERC-20 `name()`, `symbol()`, `decimals()` on-chain
to validate, then insert into Room.

2.4.4. Create `GetPortfolioUseCase` combining token balances and price chart data.

2.4.5. Create `HomeViewModel`:
- State: `data class HomeUiState(val portfolio: Portfolio?, val isLoading: Boolean,
         val error: String?, val selectedChain: Chain)`
- On init and chain change, call refresh.
- Pull-to-refresh calls refresh.

2.4.6. Create `HomeScreen` composable:
- Total balance with animated counter.
- Portfolio chart using Vico library (line chart, selectable time range).
- Quick action buttons: Send, Receive, Swap.
- Token list with `TokenIcon`, symbol, balance, fiat value, 24h change.
- Pull-to-refresh via `pullRefresh` modifier.
- Shimmer loading state.

2.4.7. Create `TokenIcon` composable:
- Loads token logo via Coil from `logoUrl`.
- Fallback: colored circle with first letter of symbol.

2.4.8. Create `AddTokenDialog`:
- Text field for contract address.
- On paste, auto-fetch token info from chain.
- Show name, symbol, decimals for confirmation.

**Acceptance:** Home screen displays ETH balance and at least 2 ERC-20 tokens.
Portfolio chart renders 7-day price data. Pull-to-refresh updates balances.
Adding a custom token by contract address works.

---

## Task 2.5: Token Detail Screen (feature-tokens)

**Subtasks:**

2.5.1. Create `TokenDetailViewModel`:
- Receives `contractAddress` and `chainId` as navigation arguments.
- Fetches token detail + extended price chart data.
- Fetches recent transactions for this specific token.

2.5.2. Create `TokenDetailScreen`:
- Large token icon and name.
- Balance and fiat value.
- Price chart with selectable range (1D, 7D, 1M, 1Y).
- Price, 24h change, market cap (from CoinGecko).
- Send / Receive buttons.
- Recent transactions list (max 5, "See All" navigates to History with filter).

**Acceptance:** Token detail shows correct balance. Price chart loads and updates
with range selection. "Send" navigates to Send screen pre-filled with token.

---

## Task 2.6: Send Transaction Flow (feature-send)

**Description:** Implement the complete send flow: form → review → submit → result.

**Subtasks:**

2.6.1. Create domain layer:
```kotlin
data class GasEstimate(
val slow: GasOption,
val normal: GasOption,
val fast: GasOption,
)
data class GasOption(
val gasPrice: BigInteger,
val estimatedTime: String,
val fiatCost: Double,
)

       interface TransactionRepository {
           suspend fun estimateGas(to: String, value: BigInteger, data: ByteArray?, chainId: Int): Result<GasEstimate>
           suspend fun sendNativeTransaction(to: String, value: BigInteger, gasPrice: BigInteger, chainId: Int): Result<String>
           suspend fun sendTokenTransaction(contractAddress: String, to: String, amount: BigInteger, gasPrice: BigInteger, chainId: Int): Result<String>
           fun getTransactionHistory(chainId: Int, address: String, page: Int): Flow<List<Transaction>>
       }
       ```

2.6.2. Implement `TransactionRepositoryImpl`:
- `estimateGas()`: use `web3j.ethEstimateGas()` + `web3j.ethGasPrice()`.
Calculate slow (0.8x), normal (1x), fast (1.5x) multipliers.
- `sendNativeTransaction()`:
a. Load private key from secure storage.
b. Create `RawTransaction` with nonce from `ethGetTransactionCount`.
c. Sign with `TransactionEncoder.signMessage()`.
d. Send via `ethSendRawTransaction`.
e. Return tx hash.
f. **IMPORTANT:** Zero out private key bytes after signing.
- `sendTokenTransaction()`: same but encode ERC-20 `transfer()` call data.

2.6.3. Create `SendViewModel`:
- State: recipient address, amount, selected gas option, validation errors,
flow step (form / review / submitting / submitted / error).
- Validate address format (checksum validation).
- Validate amount (not exceeding balance, accounting for gas).
- QR scan result populates address.

2.6.4. Create `SendFormScreen`:
- Recipient address field with QR scan button and address book picker.
- Amount field with MAX button (fills max minus gas estimate).
- Fiat equivalent shown below amount.
- Gas option selector (Slow / Normal / Fast) with cost and time.
- "Review" button enabled only when all validations pass.

2.6.5. Create `ConfirmTransactionScreen`:
- Summary: To, Amount, Network Fee, Total.
- "Confirm & Send" button triggers PIN/biometric, then submits.

2.6.6. Create `TransactionSubmittedScreen`:
- Lottie checkmark animation.
- Tx hash with copy button.
- "View on Explorer" link (opens block explorer in browser).
- "Done" button returns to Home.

2.6.7. Integrate QR scanning:
- Use CameraX + ML Kit Barcode Scanning.
- `QRScannerScreen` with camera preview and scan overlay.
- On scan, validate address format, return result via navigation.

**Acceptance:** Full send flow: enter address → enter amount → select gas → review →
authenticate → submit → see tx hash. Transaction appears on block explorer.
Send ERC-20 token works. QR scan fills address. Insufficient balance shows error.

---

## Task 2.7: Receive Screen (feature-receive)

**Subtasks:**

2.7.1. Create `ReceiveScreen`:
- QR code generated from wallet address using ZXing.
- Address displayed in full with copy button.
- Share button using Android share sheet.
- Warning text about network compatibility.
- Chain badge showing current network.

2.7.2. Create `QRCodeImage` composable:
- Takes a string, generates QR bitmap via ZXing `MultiFormatWriter`.
- Displays in a themed card with rounded corners.

**Acceptance:** QR code scans correctly to the wallet address. Copy and Share work.

---

## Task 2.8: Transaction History (feature-history)

**Subtasks:**

2.8.1. Implement `TransactionRepository.getTransactionHistory()`:
- Fetch from Etherscan-compatible API (module=account, action=txlist + tokentx).
- Cache in Room. Return paginated Flow.
- Map to domain `Transaction` model with type detection (send/receive/swap).

2.8.2. Create `HistoryViewModel`:
- State: transactions grouped by date, filter, loading, error.
- Pagination: load more when scrolling near bottom.
- Filter: All, Sent, Received, Swap, Failed.

2.8.3. Create `HistoryListScreen`:
- Sticky date headers.
- Transaction row: direction icon (arrow up/down), token symbol, amount,
address preview, timestamp, status chip.
- Color coding: green received, red sent, blue swap, gray failed.
- Filter chips at top.
- Infinite scroll pagination.

2.8.4. Create `TransactionDetailScreen`:
- Full details: hash, from, to, value, gas used, gas price, block number,
timestamp, status.
- "View on Explorer" button.

**Acceptance:** History shows correct transactions from the blockchain. Pagination
loads more. Filters work. Transaction detail shows all fields.

---

## Task 2.9: Default Token List

**Description:** Seed the database with popular tokens per chain so the user doesn't
start with an empty token list.

**Subtasks:**

2.9.1. Create `default_tokens.json` in assets with top 20 tokens per chain:
- Ethereum: ETH, USDC, USDT, DAI, WETH, UNI, LINK, AAVE, etc.
- BSC: BNB, BUSD, CAKE, etc.
- Polygon: MATIC, USDC, WETH, etc.
Include contract addresses, decimals, symbol, name, and CoinGecko ID.

2.9.2. On first wallet creation, import default tokens into Room.

2.9.3. Balances are zero until first refresh.

**Acceptance:** After wallet creation, home screen shows default token list.
Balances update to correct values on refresh.

---

## Phase 2 Deliverable Checklist

- [ ] Multi-chain switching works (Ethereum, Sepolia, BSC, Polygon)
- [ ] Home dashboard shows total portfolio value
- [ ] Portfolio chart renders with time range selection
- [ ] Token list shows balances and fiat values
- [ ] Add custom token by contract address works
- [ ] Token detail screen with price chart works
- [ ] Send native coin (ETH/BNB/MATIC) works end-to-end
- [ ] Send ERC-20 token works end-to-end
- [ ] Gas estimation with Slow/Normal/Fast options works
- [ ] QR code scanning fills recipient address
- [ ] Receive screen shows QR code and copyable address
- [ ] Transaction history loads and paginates
- [ ] Transaction filters work
- [ ] Transaction detail screen shows all fields
- [ ] Default token list seeded on wallet creation
- [ ] All Phase 2 unit and integration tests pass
