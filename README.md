# MoraWallet

MoraWallet is a multi-currency personal finance tracker for Android. Manage wallets in any currency, record income, expenses, and transfers, review category breakdowns and period reports, monitor live exchange rates with interactive charts, and browse financial news — all in one app.

Built with Jetpack Compose and Material 3. Uses a blue/cyan palette with distinct semantic colors for income, expense, transfer, categories, and chart series.

## Features

| Area | What it does |
| --- | --- |
| Dashboard | Portfolio total (K/M/B/T compact), quick-add tiles, wallet row, income/expense reports, recent records, exchange-rate preview |
| Wallets | Summary card with wallet count and add button; per-wallet detail with balance, category breakdown, and transaction history |
| Records | Filter by type (income / expense / transfer), date range, and wallet; category cards; full record list |
| Add Record | Amount, wallet, category, and note; date/time auto-recorded at submission |
| Markets | Live exchange rates with smart base/quote inversion for weak currencies; interactive rate-history chart with high/low lines and drag crosshair; currency converter |
| News | Browse and search financial news; card list and article detail |
| Settings | Base currency, theme preference, account actions |

## Tech Stack

- Kotlin
- Jetpack Compose + Material 3
- MVVM with repository layer
- Firebase Auth (Email/Password + Google via Credential Manager) + Cloud Firestore
- Retrofit, OkHttp, kotlinx.serialization
- DataStore Preferences
- Coil for news thumbnails
- Manual dependency injection via `di/AppContainer.kt`
- Custom Canvas charts (line chart, bar chart, donut chart)

## Toolchain

| Tool | Version |
| --- | --- |
| Android Gradle Plugin | 9.1.1 |
| Gradle | 9.3.1 |
| Kotlin | 2.2.10 |
| Compose BOM | 2024.09.00 |
| minSdk | 24 |
| targetSdk | 36 |
| compileSdk | 36 |

## Setup

### 1. Prerequisites

- Android Studio with Android SDK 36
- JDK 11 or newer
- A Firebase project
- For Google Sign-In: a Play-enabled emulator/device with a Google account
- Optional: a NewsAPI.org key for the News tab

### 2. Firebase

1. Open the Firebase Console and create or open a project.
2. Add an Android app with package name `com.example.morawallet`.
3. Download `google-services.json` and place it at:

```text
app/google-services.json
```

4. Enable **Email/Password** sign-in under Firebase Authentication.
5. Create a **Cloud Firestore** database.

`app/google-services.json` is git-ignored — never commit it.

#### Google Sign-In

The login and register screens offer **Continue with Google**, implemented with the
AndroidX **Credential Manager** API (not the deprecated `GoogleSignInClient`). To enable it:

1. **Firebase Console → Authentication → Sign-in method → enable Google.**
   This auto-creates the OAuth **Web client** for the project.
2. **Register your signing certificate's SHA-1** so Google can verify the app.
   Get the debug fingerprint with:

   ```powershell
   .\gradlew.bat :app:signingReport
   ```

   Copy the `SHA1` (and `SHA-256`) from the `debug` variant, then add it under
   **Firebase Console → Project Settings → Your apps → Add fingerprint**.
   Each developer machine has its own debug keystore, and release builds need the
   release keystore's fingerprint added too.
3. **Re-download `google-services.json`** into `app/` after the above — the web client
   ID and certificate hashes are baked into it.

No manual ID copy-paste is needed: the app reads `R.string.default_web_client_id`, which
the `google-services` Gradle plugin generates from the web client (`client_type: 3`) entry
in `google-services.json`.

> **Runtime requirements:** the device/emulator must have **Google Play Services** (use a
> Play-enabled emulator image, not plain AOSP) and at least one Google account signed in.
> A `[28444] Developer console is not set up correctly` error means the SHA-1 or web client
> ID does not match — re-check steps 2 and 3 and reinstall the app.

### 3. News API Key

The News tab uses [NewsAPI.org](https://newsapi.org). Add your key to `gradle.properties` (project-level or user-level):

```properties
NEWS_API_KEY=your_key_here
```

Without a key the News tab shows a setup message instead of crashing. Do not commit a real key to a public repository.

### 4. Build

```powershell
# Windows
.\gradlew.bat assembleDebug

# macOS / Linux
./gradlew assembleDebug
```

Run unit tests:

```powershell
.\gradlew.bat testDebugUnitTest
```

## Project Structure

```text
app/src/main/java/com/example/morawallet/
  core/
    ui/components/      Shared Compose components (charts, cards, fields, pickers)
    util/               Currency formatting (compact K/M/B/T), date, category, report helpers
  data/
    firebase/           Firestore collection references
    model/              App data models (Wallet, Transaction, …)
    remote/             Retrofit API interfaces (exchange rates, news)
    repository/         Auth, wallet, transaction, market, news, user repositories
  di/                   AppContainer and ViewModel factory helpers
  feature/
    auth/               Login, register, splash
    dashboard/          Portfolio card, quick-add, reports, recent transactions
    markets/            Rate list, rate-history chart, currency converter
    news/               News list and article detail
    settings/           Settings screen
    transaction/        Transaction list, filters, form, detail
    wallet/             Wallet list, detail, form
  navigation/           Routes, NavHost, top bar, bottom bar, root scaffold
  ui/theme/             Color tokens, typography, spacing, shape, palette helpers
```

## Firestore Security Rules

Use this as a starting point during development:

```js
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{uid}/{document=**} {
      allow read, write: if request.auth != null && request.auth.uid == uid;
    }
  }
}
```

Harden rules before shipping to production.

## Notes

- Exchange rates use the [Frankfurter API](https://www.frankfurter.app) — no API key required.
- When a weak base currency produces rates below `0.01` (e.g. IDR as base), the chart automatically inverts the series and swaps base/quote labels for a readable graph.
- Amounts compact-format above 1,000 (K / M / B / T) in cards and pills; full precision is shown in transaction and wallet detail screens.
- Release minification is currently disabled. Enable and test ProGuard rules before publishing.
