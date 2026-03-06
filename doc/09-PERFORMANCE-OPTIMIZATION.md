# NexVault — Performance Optimization Guide

## 1. App Startup

### 1.1 Lazy Initialization
Avoid initializing heavy objects (Web3j instances, Room database, WalletConnect SDK)
in `Application.onCreate()`. Use Hilt's `@Inject` with lazy access patterns or
the `Initializer` pattern with `App Startup` library so that initialization is
deferred until first use.

### 1.2 Baseline Profiles
Generate a Baseline Profile using Jetpack Macrobenchmark. This pre-compiles critical
user paths (app startup → unlock → home screen) into AOT-compiled code, reducing
first-frame render time by 20–40%.

```kotlin
// benchmark/src/main/java/com/nexvault/wallet/benchmark/BaselineProfileGenerator.kt
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {
    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() {
        rule.collect("com.nexvault.wallet") {
            startActivityAndWait()
            // Navigate through unlock → home
        }
    }
}
```

### 1.3 Splash Screen Optimization
Use the Android 12 `SplashScreen` API with a static vector drawable. Avoid
any network call or heavy computation before the main content renders.

---

## 2. Compose UI Performance

### 2.1 Stability and Skippability
Ensure all UI state classes are **stable** for the Compose compiler. Use
`@Immutable` or `@Stable` annotations where needed. Avoid passing unstable
types (mutable lists, maps) directly to composables.

```kotlin
@Immutable
data class HomeUiState(
    val portfolio: Portfolio?,
    val isLoading: Boolean,
    val error: String?,
)

@Immutable
data class Portfolio(
    val totalFiatValue: Double,
    val change24hPercent: Double,
    val tokens: ImmutableList<TokenUi>,  // use kotlinx.collections.immutable
    val chartData: ImmutableList<PricePoint>,
)
```

Use `kotlinx-collections-immutable` (`ImmutableList`, `ImmutableMap`) for all
list/map fields in UI state to guarantee Compose stability.

### 2.2 Derived State and Remember
Use `derivedStateOf` for computed values that shouldn't trigger recomposition
on every frame (e.g., "is scroll position past threshold for FAB visibility").

```kotlin
val showScrollToTop by remember {
    derivedStateOf { lazyListState.firstVisibleItemIndex > 5 }
}
```

### 2.3 Lazy List Optimization
For token lists and transaction history, always provide a stable `key` parameter:

```kotlin
LazyColumn {
    items(
        items = tokens,
        key = { "${it.contractAddress}-${it.chainId}" }
    ) { token ->
        TokenRow(token)
    }
}
```

Use `contentType` parameter when mixing different item types (headers vs. items)
so the LazyColumn can reuse the correct composable pools.

### 2.4 Image Loading
Configure Coil with aggressive memory and disk caching for token icons and NFT images:

```kotlin
ImageLoader.Builder(context)
    .memoryCache {
        MemoryCache.Builder(context)
            .maxSizePercent(0.25)  // 25% of app memory
            .build()
    }
    .diskCache {
        DiskCache.Builder()
            .directory(context.cacheDir.resolve("coil_cache"))
            .maxSizeBytes(50 * 1024 * 1024)  // 50MB
            .build()
    }
    .crossfade(true)
    .build()
```

For NFT images, use `SubcomposeAsyncImage` with a shimmer placeholder.

### 2.5 Chart Rendering
Portfolio charts can be expensive. Ensure the chart composable uses `remember`
to cache the data model and only recomposes when the data list reference changes
(ensured by `ImmutableList`).

---

## 3. Network Performance

### 3.1 Batch RPC Calls
Instead of calling `ethGetBalance` + N separate `balanceOf` calls, use a
**Multicall3 contract** to batch all balance reads into a single RPC call:

```kotlin
// Pseudocode for multicall
val multicallContract = Multicall3.load(MULTICALL3_ADDRESS, web3j, credentials, gasProvider)
val calls = tokens.map { token ->
    Multicall3.Call3(
        token.contractAddress,
        false,
        encodeBalanceOfCall(walletAddress)
    )
}
val results = multicallContract.aggregate3(calls).send()
```

This reduces N+1 RPC calls to 1, significantly speeding up balance refresh.

### 3.2 HTTP Caching and Conditional Requests
Configure OkHttp cache for CoinGecko responses (prices don't change every second):

```kotlin
OkHttpClient.Builder()
    .cache(Cache(cacheDir.resolve("http_cache"), 10 * 1024 * 1024))
    .addInterceptor { chain ->
        val request = chain.request().newBuilder()
            .header("Cache-Control", "public, max-age=30") // 30s cache for prices
            .build()
        chain.proceed(request)
    }
```

### 3.3 Request Deduplication
Use a `Mutex` or coroutine-based deduplication to prevent multiple simultaneous
refreshes (e.g., user rapid-taps refresh):

```kotlin
private val refreshMutex = Mutex()

suspend fun refreshBalances(chainId: Int, address: String) {
    if (!refreshMutex.tryLock()) return  // already refreshing
    try {
        // ... do refresh
    } finally {
        refreshMutex.unlock()
    }
}
```

### 3.4 Pagination for History
Never load all transaction history at once. Use page-based pagination with
Etherscan API parameters and display using Paging 3 library or manual
offset-based loading.

---

## 4. Database Performance

### 4.1 Indices
Add database indices for frequently queried columns:

```kotlin
@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["chainId", "fromAddress"]),
        Index(value = ["chainId", "toAddress"]),
        Index(value = ["timestamp"]),
    ]
)
data class TransactionEntity(...)
```

### 4.2 Avoid Main Thread Access
Room is already configured to prevent main thread queries by default. Ensure
all DAO interactions happen on `Dispatchers.IO` (handled automatically with
`suspend` DAO functions and `Flow` return types).

### 4.3 Transaction Batching
When inserting/updating many tokens or transactions after a refresh, use Room's
`@Transaction` annotation to wrap upserts in a single database transaction:

```kotlin
@Transaction
suspend fun refreshTokensAndPrices(tokens: List<TokenEntity>) {
    upsertTokens(tokens)
}
```

---

## 5. Memory Management

### 5.1 LeakCanary
Include LeakCanary in debug builds to automatically detect memory leaks:

```kotlin
// app/build.gradle.kts
debugImplementation("com.squareup.leakcanary:leakcanary-android:2.13")
```

### 5.2 Web3j Instance Caching
Reuse `Web3j` instances per chain rather than creating new ones on every call.
The `Web3jProvider` with `ConcurrentHashMap` handles this. Ensure instances
are shut down when no longer needed (e.g., when a chain is deselected for a
long time).

### 5.3 Mnemonic Memory Handling
When the mnemonic is decrypted for display or signing, hold it in a `CharArray`
or `ByteArray` (not `String`, which is immutable and pooled) and zero it out
immediately after use:

```kotlin
fun signTransaction(...) {
    val mnemonicBytes = secureStorage.retrieveMnemonicBytes()
    try {
        // derive key and sign
    } finally {
        mnemonicBytes.fill(0)  // zero out
    }
}
```

---

## 6. APK Size Optimization

### 6.1 R8 Full Mode
Enable R8 full mode in release builds for aggressive optimization:

```kotlin
android {
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
```

### 6.2 ABI Splits
Web3j includes native libraries. Use ABI splits to reduce APK size per architecture:

```kotlin
android {
    splits {
        abi {
            isEnable = true
            reset()
            include("armeabi-v7a", "arm64-v8a", "x86_64")
            isUniversalApk = true
        }
    }
}
```

Or use Android App Bundle (AAB) which handles this automatically.

### 6.3 Remove Unused Resources
With `isShrinkResources = true`, unused resources are stripped. Also audit
third-party library resources (some include large drawable sets).

### 6.4 Vector Drawables
Use vector drawables (SVG) instead of PNGs for all icons. Token icons loaded
from network are the exception — use WebP format via Coil's built-in decoder.

---

## 7. Background Work Optimization

### 7.1 WorkManager Constraints
Set network constraints and battery considerations:

```kotlin
val syncRequest = PeriodicWorkRequestBuilder<BalanceSyncWorker>(
    repeatInterval = 15,
    repeatIntervalTimeUnit = TimeUnit.MINUTES,
)
    .setConstraints(
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()
    )
    .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
    .build()
```

### 7.2 Efficient Sync
Only sync tokens that the user has added (not all possible tokens). For pending
transactions, poll with exponential backoff until confirmed, then stop.

---

## 8. Monitoring & Profiling Checklist

Before each release, run through this checklist:

- [ ] Profile startup with Android Studio Profiler — first frame under 2s on
  mid-range device
- [ ] Record a Compose recomposition trace — verify no unnecessary recompositions
  on the Home screen
- [ ] Scroll token list and history with GPU rendering profiler — all frames
  under 16ms (60fps)
- [ ] Check memory with Profiler — no continuous growth pattern (leak)
- [ ] LeakCanary shows no retained objects after navigating back from all screens
- [ ] Run `./gradlew app:dependencies` — no duplicate or conflicting versions
- [ ] Release APK size under 25MB (universal), under 15MB (single ABI)
- [ ] Baseline Profile is included in release build