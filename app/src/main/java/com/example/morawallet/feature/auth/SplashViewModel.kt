package com.example.morawallet.feature.auth

import androidx.lifecycle.ViewModel
import com.example.morawallet.data.repository.AuthRepository

class SplashViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {
    val isLoggedIn: Boolean get() = authRepository.isLoggedIn
}
