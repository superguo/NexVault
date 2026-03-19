Refer to:
- @doc/01-PROJECT-OVERVIEW.md for project structure
- @doc/02-ARCHITECTURE-AND-TECH-STACK.md for dependencies (Compose BOM, Material 3, Lottie, Coil, Vico)
- @doc/03-UI-UX-DESIGN.md for the full design spec — colors, typography, spacing, components, animations
- @doc/04-IMPLEMENTATION-PLAN-PHASE1.md for Task 1.2 details

The project already has:
- Full multi-module Gradle setup (all modules compile)
- Version catalog (`libs.versions.toml`) with all dependencies
- `NexVaultApplication` with `@HiltAndroidApp`
- `MainActivity` with `@AndroidEntryPoint` and placeholder text
- Detekt + Spotless configured

All code for this prompt goes in the `core/core-ui` module under:
`core/core-ui/src/main/java/com/nexvault/wallet/core/ui/`

## What I need you to do:

### 1. Color Tokens — `theme/Color.kt`

Define two complete Material 3 color schemes for NexVault:

**Dark Theme (Primary — the default):**
- Background/Surface: Deep navy (#0A0E1A) to charcoal (#121829)
- Surface variants: #1A1F35, #232842
- Primary: Electric blue (#2D5AF0)
- Secondary: Cyan accent (#00D4AA)
- Tertiary: Purple (#7B61FF)
- Error: #FF4C6E
- OnPrimary: White
- OnBackground / OnSurface: #FFFFFF (high emphasis), #A0A8C8 (medium), #5A6180 (disabled)
- Positive/gain: #00E676
- Negative/loss: #FF5252
- Warning: #FFB74D
- Card surfaces: #141928 with subtle border #1E2440

**Light Theme:**
- Provide a sensible light counterpart: white/gray backgrounds, same primary blue,
  adjust all on-colors for readability
- Surface: #F5F6FA, Background: #FFFFFF
- OnBackground/OnSurface: #0A0E1A (high), #5A6180 (medium)

Create:
- `val NexVaultDarkColorScheme = darkColorScheme(...)`
- `val NexVaultLightColorScheme = lightColorScheme(...)`
- Extension colors for crypto-specific semantics as a custom `NexVaultColors` class
  accessible via `LocalNexVaultColors` CompositionLocal:
  `positive`, `negative`, `warning`, `cardSurface`, `cardBorder`,
  `shimmer`, `textHigh`, `textMedium`, `textDisabled`, `gradientStart`, `gradientEnd`

### 2. Typography — `theme/Type.kt`

Using the **Inter** font family (import from Google Fonts or bundle the TTF):
- Create `val NexVaultTypography = Typography(...)` mapping all M3 text styles
- Scale:
  - displayLarge: 36sp Bold (portfolio total value)
  - headlineLarge: 28sp Bold
  - headlineMedium: 24sp SemiBold
  - headlineSmall: 20sp SemiBold
  - titleLarge: 18sp SemiBold
  - titleMedium: 16sp SemiBold
  - titleSmall: 14sp SemiBold
  - bodyLarge: 16sp Regular
  - bodyMedium: 14sp Regular
  - bodySmall: 12sp Regular
  - labelLarge: 14sp Medium
  - labelMedium: 12sp Medium
  - labelSmall: 10sp Medium (captions, timestamps)

All with appropriate line heights and letter spacing as per Material 3 guidelines.
If bundling Inter TTFs is complex, fall back to `FontFamily.Default` but keep the
size/weight scale exact.

### 3. Shape — `theme/Shape.kt`

```
val NexVaultShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)
```

### 4. Dimensions / Spacing — `theme/Dimens.kt`

Create an object `NexVaultDimens` with:
- `spacingXxs = 2.dp`, `spacingXs = 4.dp`, `spacingSm = 8.dp`,
  `spacingMd = 16.dp`, `spacingLg = 24.dp`, `spacingXl = 32.dp`, `spacingXxl = 48.dp`
- `cardElevation = 0.dp` (flat cards with border)
- `iconSizeSmall = 20.dp`, `iconSizeMedium = 24.dp`, `iconSizeLarge = 40.dp`,
  `tokenIconSize = 40.dp`, `nftThumbnailSize = 120.dp`
- `buttonHeight = 52.dp`, `inputHeight = 56.dp`
- `bottomNavHeight = 64.dp`
- `topBarHeight = 56.dp`

Provide via a `LocalNexVaultDimens` CompositionLocal so features can access it.

### 5. NexVaultTheme Composable — `theme/NexVaultTheme.kt`

Create the main theme composable:

```kotlin
@Composable
fun NexVaultTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Pick color scheme based on darkTheme
    // Provide NexVaultColors via CompositionLocalProvider
    // Provide NexVaultDimens via CompositionLocalProvider
    // Apply MaterialTheme with colorScheme, typography, shapes
    // Set status bar / nav bar colors (edge-to-edge friendly)
    MaterialTheme(
        colorScheme = colorScheme,
        typography = NexVaultTypography,
        shapes = NexVaultShapes,
        content = content
    )
}
```

Create a convenience accessor object:
```kotlin
object NexVaultTheme {
    val colors: NexVaultColors @Composable get() = LocalNexVaultColors.current
    val dimens: NexVaultDimens @Composable get() = LocalNexVaultDimens.current
}
```

### 6. Reusable Components — `components/`

Create these composable components (each in its own file):

**a) `NexVaultButton.kt`**
- Primary filled button (electric blue, rounded `large` shape, 52dp height)
- Secondary outlined button variant
- Loading state with small `CircularProgressIndicator` replacing text
- Disabled state with reduced opacity
- Full width by default

**b) `NexVaultCard.kt`**
- Dark card with `cardSurface` background and `cardBorder` border (1dp)
- Rounded `large` shape
- Optional subtle gradient background variant
- Content padding of `spacingMd`

**c) `NexVaultTextField.kt`**
- Custom-styled `OutlinedTextField` matching the dark theme
- Error state styling (red border + error text below)
- Optional trailing icon slot (for paste, scan, clear)
- Optional prefix text (for amounts)

**d) `NexVaultTopBar.kt`**
- Transparent/surface-colored top bar
- Back arrow navigation (optional)
- Title centered
- Optional trailing action icons

**e) `TokenIcon.kt`**
- Circular image loaded via Coil (`AsyncImage`)
- Fallback: colored circle with first letter of token symbol
- Size configurable, default `tokenIconSize` (40dp)

**f) `PriceChangeChip.kt`**
- Shows percentage with up/down arrow icon
- Green background tint for positive, red for negative
- Rounded pill shape

**g) `ShimmerPlaceholder.kt`**
- Shimmer/skeleton loading animation
- Configurable width, height, and corner radius
- Uses `InfiniteTransition` with shimmer gradient

**h) `GradientBackground.kt`**
- Full-screen gradient background (top-to-bottom, using `gradientStart` to `gradientEnd`)
- Used as the base layer for onboarding and auth screens

**i) `EmptyStateView.kt`**
- Centered column with icon/Lottie, title text, subtitle text
- Optional action button

**j) `NexVaultBottomSheet.kt`**
- Styled `ModalBottomSheet` with drag handle
- Surface color matching theme
- Rounded top corners (`extraLarge`)

### 7. Animation Utilities — `animation/`

**a) `NexVaultAnimations.kt`**
- Define standard duration constants: `DURATION_SHORT = 200`, `DURATION_MEDIUM = 350`, `DURATION_LONG = 500`
- Standard easing: `FastOutSlowIn`, `LinearOutSlowIn`
- Reusable `fadeIn + slideInVertically` / `fadeOut + slideOutVertically` `EnterTransition` / `ExitTransition` for navigation

**b) `PulseAnimation.kt`**
- A `Modifier.pulseAnimation()` extension that gently scales 1.0→1.05→1.0
  in an infinite loop (for "awaiting confirmation" states)

**c) `CountUpAnimation.kt`**
- A composable/state helper that animates a number from 0 to target value
  (for portfolio total display on load)

### 8. Preview Helpers — `preview/`

**`ThemePreviewWrapper.kt`**
- A helper composable that wraps content in `NexVaultTheme(darkTheme = true)`
  for use in `@Preview` annotations across feature modules

### 9. Update MainActivity

Update `app/.../MainActivity.kt` to:
- Use `NexVaultTheme` instead of plain `MaterialTheme`
- Enable edge-to-edge (`enableEdgeToEdge()`)
- Show a simple preview screen with:
  - `GradientBackground` behind content
  - A `NexVaultCard` containing a `Text("NexVault Wallet")` with `displayLarge` style
  - A `NexVaultButton` that says "Get Started"
  - A `PriceChangeChip` showing "+5.24%"
  - A `ShimmerPlaceholder` rectangle
  This acts as a visual verification that the theme is working correctly.

## File Structure Summary:

```
core/core-ui/src/main/java/com/nexvault/wallet/core/ui/
├── theme/
│   ├── Color.kt
│   ├── Type.kt
│   ├── Shape.kt
│   ├── Dimens.kt
│   ├── NexVaultTheme.kt
│   └── NexVaultColors.kt       (custom color class + CompositionLocal)
├── components/
│   ├── NexVaultButton.kt
│   ├── NexVaultCard.kt
│   ├── NexVaultTextField.kt
│   ├── NexVaultTopBar.kt
│   ├── TokenIcon.kt
│   ├── PriceChangeChip.kt
│   ├── ShimmerPlaceholder.kt
│   ├── GradientBackground.kt
│   ├── EmptyStateView.kt
│   └── NexVaultBottomSheet.kt
├── animation/
│   ├── NexVaultAnimations.kt
│   ├── PulseAnimation.kt
│   └── CountUpAnimation.kt
└── preview/
    └── ThemePreviewWrapper.kt
```

## Acceptance Criteria:

1. `./gradlew :core:core-ui:assembleDebug` compiles with zero errors
2. `./gradlew assembleDebug` (full project) compiles with zero errors
3. App launches on emulator and shows the theme preview screen with:
   - Dark navy gradient background visible
   - Card with proper dark surface + border styling
   - "NexVault Wallet" text in large bold white font
   - Electric blue "Get Started" button with rounded corners
   - Green "+5.24%" chip
   - Animated shimmer placeholder rectangle
4. All custom colors, typography, and shapes are applied through `NexVaultTheme`
5. `NexVaultTheme.colors.positive` and other custom accessors compile and work
6. Components use theme tokens exclusively — no hardcoded colors or text styles
7. Switching `NexVaultTheme(darkTheme = false)` shows a coherent light theme
