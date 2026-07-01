# Mobase

**Mobase** is a robust infrastructure foundation for Android applications. It acts as a unified middleware layer that orchestrates app startup, abstracts third-party SDKs, and provides a clean API for essential services like Analytics, Billing, Attribution, and Feature Flags.

## Key Features

- **Startup Orchestration:** Manages complex initialization sequences and fetches essential startup data (feature flags, user identity, attribution, entitlements) before the app becomes interactive — with per-task timeouts and graceful fallbacks.
- **Unified Analytics:** A single interface to track events across multiple providers. Integrates **Amplitude**, **Firebase**, and **AppsFlyer** without leaking their implementation details into business logic.
- **Identity Management:** Synchronizes user identifiers across all integrated services automatically to ensure data consistency.
- **Smart Attribution:** Out-of-the-box support for **AppsFlyer** conversion data, **Google Play Install Referrer**, and deep link handling.
- **Monetization & Billing:** Simplified wrapper around **Google Play Billing Library** (Billing 8+) for entitlement management with persistent storage and automatic refresh.
- **Feature Flags:** Built-in support via **Amplitude Experiment** with sticky bucketing, local caching, and timeout-safe fetching.
- **Session Replay:** Optional **Amplitude Session Replay** integration with configurable sample rate.
- **Device Intelligence:** Collects device info (RAM, CPU, display, storage, theme) and tracks first-launch cohort data automatically.
- **In-App Updates:** Lifecycle-aware minimum version enforcement via **Google Play In-App Updates**.

## Tech Stack

- **Language:** 100% Kotlin
- **Asynchrony:** Kotlin Coroutines & Flow
- **Storage:** Jetpack DataStore
- **Min SDK:** 24 (Android 7.0+)
- **Architecture:** Clean Architecture with explicit dependency injection and adapter/wrapper patterns.

## Modular Integrations

| Domain          | Providers                                      |
|-----------------|------------------------------------------------|
| Analytics       | Amplitude (v1.27), Firebase Analytics          |
| Attribution     | AppsFlyer (v6.18), Google Play Install Referrer |
| Purchases       | Google Play Billing (v8.3)                     |
| Feature Flags   | Amplitude Experiment (v1.15)                   |
| Session Replay  | Amplitude Session Replay (v0.24)               |
| Identity        | Advertising ID, App Set ID, AppsFlyer UID      |
| App Updates     | Google Play In-App Updates (v2.1)              |

## Getting Started

### 1. Build the Mobase instance

Initialize the core in your `Application` class using the fluent **Builder API**:

```kotlin
class MyApplication : Application() {
    lateinit var mobase: Mobase

    override fun onCreate() {
        super.onCreate()

        // Analytics
        val amplitude = AmplitudeAnalytics(apiKey = "...", context = this)
        val firebase = FirebaseAnalytics()
        val analytics = amplitude + firebase  // CompositeAnalytics

        // Attribution & deep links
        val appsFlyer = AppsFlyer(apiKey = "...", context = this)

        // Purchases
        val purchases = GooglePlayPurchases(context = this)
            .withAnalytics(appsFlyer, firebase)

        // Feature flags
        val featureFlags = AmplitudeFeatureFlags(application = this, deploymentKey = "...")

        mobase = Mobase.builder(applicationContext = this)
            .setAppsFlyer(appsFlyer)
            .setAnalytics(analytics)
            .setPurchases(purchases)
            .setFeatureFlags(featureFlags)
            .build()

        mobase.initialize()
    }
}
```

### 2. Await startup data

Call `start()` from your launch screen to await all startup tasks and receive resolved data:

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mobase = (application as MyApplication).mobase

        lifecycleScope.launch {
            val data: StartupData = mobase.start()
            // data.userId          — resolved user identifier
            // data.entitlements    — active subscriptions / purchases
            // data.storeCountry    — user's store country code
            // data.attribution     — media source, campaign, deep link value, etc.
        }
    }
}
```

## Startup Data

`start()` returns a `StartupData` instance populated by parallel async tasks (each with individual timeouts):

```kotlin
data class StartupData(
    val userId: String?,
    val entitlements: Entitlements?,
    val storeCountry: String?,
    val attribution: AttributionData,
)
```

**Startup sequence (max 6 seconds total):**
1. Set initial user properties and SDK metadata
2. Detect first launch, collect device info, record cohort
3. In parallel: resolve attribution, entitlements, store country, Advertising ID, App Set ID
4. Track attribution event
5. Fetch feature flags with user context
6. Check minimum app version and request update if needed
7. Return `StartupData` with graceful fallbacks for any timed-out tasks

## Key Interfaces

### Analytics

```kotlin
interface Analytics {
    fun setUserId(userId: String?)
    fun track(event: AnalyticsEvent)
    fun track(event: String, properties: Map<String, Any?>? = null)
    fun setUserProperties(properties: Map<String, Any?>)
}

// Combine multiple providers:
val analytics: Analytics = amplitudeAnalytics + firebaseAnalytics
```

### Purchases

```kotlin
interface Purchases {
    val entitlements: Flow<Entitlements>

    suspend fun getStorefront(): Result<Storefront>
    suspend fun getProducts(productIds: List<String>): Result<List<Product>>
    suspend fun getEntitlements(): Result<Entitlements>
    suspend fun purchase(activity: Activity, productId: String): Result<PurchaseTransaction>
    suspend fun restore(): Result<Entitlements>
}
```

### Feature Flags

```kotlin
interface FeatureFlags {
    suspend fun fetch(context: EvaluationContext): Result<Unit>
    operator fun get(key: String): Variant
}
```

### App Update

```kotlin
interface AppUpdateManager {
    fun requestUpdate(minVersion: Long)
}
```

## Project Structure

```
mobase-android/
├── app/                        # Sample application
│   └── src/main/
│       ├── MyApplication.kt    # Mobase initialization example
│       └── MainActivity.kt     # Startup usage example
└── mobase/                     # Core SDK library
    └── src/main/java/dev/mobase/
        ├── Mobase.kt           # Main interface & Builder
        ├── core/               # Startup orchestration, identity, storage
        ├── analytics/          # Analytics abstraction & CompositeAnalytics
        ├── amplitude/          # Amplitude Analytics & Session Replay
        ├── firebase/           # Firebase Analytics
        ├── appsflyer/          # AppsFlyer attribution & deep links
        ├── purchases/          # Purchases interface & Google Play impl
        ├── featureflags/       # Feature Flags interface & Amplitude impl
        ├── attribution/        # Attribution manager & Install Referrer
        ├── identity/           # Advertising ID, App Set ID, User ID
        ├── deviceinfo/         # Device hardware & theme info
        ├── appupdate/          # In-App Update manager
        ├── deeplink/           # Deep link listener interface
        └── common/             # Coroutine utilities
```