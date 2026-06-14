package com.example.morawallet.di

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.morawallet.MoraApp

/**
 * Obtains a ViewModel built from the manual [AppContainer].
 *
 * Usage: `val vm = moraViewModel { LoginViewModel(it.authRepository) }`
 */
@Composable
inline fun <reified VM : ViewModel> moraViewModel(
    crossinline create: (AppContainer) -> VM,
): VM {
    val container = (LocalContext.current.applicationContext as MoraApp).container
    return viewModel(factory = viewModelFactory { initializer { create(container) } })
}
