# MoraWallet

MoraWallet is a multi-currency personal finance tracker for Android. It helps users manage wallets, record income and expenses, track transfers, review category reports, monitor exchange rates, and read finance news.

The app is built with Jetpack Compose and uses a blue/cyan theme with distinct semantic colors for income, expenses, transfers, categories, charts, and page actions.

## Features

| Area | What it does |
| --- | --- |
| Dashboard | Total balance, quick add actions, wallet summary, reports, recent records, and exchange-rate preview |
| Wallets | Create wallets, view balances, inspect wallet detail, and review wallet-specific records |
| Records / Report | Filter by income, expense, or transfer; choose start and end dates; filter by wallet; review category cards and matching record list |
| Add Record | Add income, expense, and transfer records with amount, wallet, category, note, date, and time |
| Markets | View exchange rates, inspect rate history charts, and convert currencies |
| News | Browse and search financial news with card-based layouts and article details |
| Settings | Manage base currency, theme preference, and account actions |

## Tech Stack

- Kotlin
- Jetpack Compose and Material 3
- MVVM with repository layer
- Firebase Auth and Cloud Firestore
- Retrofit, OkHttp, and kotlinx.serialization
- DataStore Preferences
- Coil for news thumbnails
- Manual dependency injection through `di/AppContainer.kt`
- Custom Compose Canvas charts

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

- Android Studio with Android SDK 36 installed
- JDK 11 or newer
- A Firebase project
- Optional: a NewsAPI.org key for the News tab

### 2. Firebase

1. Open the Firebase Console.
2. Create or open a Firebase project.
3. Add an Android app with package name `com.example.morawallet`.
4. Download `google-services.json`.
5. Place it at:

```text
app/google-services.json
```

6. Enable Email/Password sign-in in Firebase Authentication.
7. Create a Cloud Firestore database.

`app/google-services.json` is ignored by Git because it is environment-specific.

### 3. News API Key

The News tab uses NewsAPI.org. Add your key as a local Gradle property:

```properties
NEWS_API_KEY=your_key_here
```

You can place that property in the project `gradle.properties` file or in your user-level Gradle properties file. Do not commit a real key to a public repository.

### 4. Build

On Windows:

```powershell
.\gradlew.bat assembleDebug
```

On macOS or Linux:

```bash
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
    ui/components/      Shared Compose UI components
    util/               Currency, date, validator, category, and report helpers
  data/
    firebase/           Firestore references
    model/              App data models
    remote/             Retrofit API interfaces
    repository/         Auth, wallet, transaction, market, news, and user repositories
  di/                   AppContainer and ViewModel helpers
  feature/
    auth/               Login, register, and splash screens
    dashboard/          Main dashboard
    markets/            Exchange rates, converter, and rate charts
    news/               News list and detail screens
    settings/           Settings screen
    transaction/        Record list, report filters, detail, and form screens
    wallet/             Wallet list, detail, and form screens
  navigation/           App routes, nav host, top bar, bottom bar, and root scaffold
  ui/theme/             Color, typography, shape, spacing, and theme tokens
```

## Firestore Rules Starter

Use rules like this as a starting point while developing:

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

Review and harden rules before shipping.

## Notes

- Exchange rates use the Frankfurter API and do not require an API key.
- News requires `NEWS_API_KEY`; without it, the News tab shows a setup message instead of crashing.
- Release minification is currently disabled. Enable and test minification before publishing.
