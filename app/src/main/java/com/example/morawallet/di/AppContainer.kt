package com.example.morawallet.di

import android.content.Context
import com.example.morawallet.BuildConfig
import com.example.morawallet.data.firebase.FirestoreRefs
import com.example.morawallet.data.remote.ExchangeRateApi
import com.example.morawallet.data.remote.NewsApi
import com.example.morawallet.data.repository.AuthRepository
import com.example.morawallet.data.repository.ExchangeRateRepository
import com.example.morawallet.data.repository.FirebaseAuthRepository
import com.example.morawallet.data.repository.FirestoreTransactionRepository
import com.example.morawallet.data.repository.FirestoreUserRepository
import com.example.morawallet.data.repository.FirestoreWalletRepository
import com.example.morawallet.data.repository.FrankfurterExchangeRateRepository
import com.example.morawallet.data.repository.NewsApiRepository
import com.example.morawallet.data.repository.NewsRepository
import com.example.morawallet.data.repository.TransactionRepository
import com.example.morawallet.data.repository.UserRepository
import com.example.morawallet.data.repository.WalletRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/**
 * Manual dependency container (lightweight alternative to Hilt for this toolchain).
 * Singletons are lazily created and shared across the app.
 */
@OptIn(ExperimentalSerializationApi::class)
class AppContainer(private val appContext: Context) {

    // --- Firebase ---
    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val refs: FirestoreRefs by lazy { FirestoreRefs(firestore) }

    // --- Networking ---
    private val json = Json { ignoreUnknownKeys = true }
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC },
            )
            .build()
    }
    private val converterFactory = json.asConverterFactory("application/json".toMediaType())

    private val exchangeRateApi: ExchangeRateApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.frankfurter.app/")
            .client(okHttpClient)
            .addConverterFactory(converterFactory)
            .build()
            .create(ExchangeRateApi::class.java)
    }
    private val newsApi: NewsApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://newsapi.org/")
            .client(okHttpClient)
            .addConverterFactory(converterFactory)
            .build()
            .create(NewsApi::class.java)
    }

    // --- Repositories ---
    val authRepository: AuthRepository by lazy { FirebaseAuthRepository(firebaseAuth) }
    val userRepository: UserRepository by lazy { FirestoreUserRepository(refs) }
    val walletRepository: WalletRepository by lazy { FirestoreWalletRepository(refs) }
    val transactionRepository: TransactionRepository by lazy { FirestoreTransactionRepository(refs) }
    val exchangeRateRepository: ExchangeRateRepository by lazy {
        FrankfurterExchangeRateRepository(exchangeRateApi)
    }
    val newsRepository: NewsRepository by lazy {
        NewsApiRepository(newsApi, BuildConfig.NEWS_API_KEY)
    }
}
