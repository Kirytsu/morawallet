package com.example.morawallet

import android.app.Application
import com.example.morawallet.di.AppContainer

/** Application entry point. Builds the manual DI container used across the app. */
class MoraApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
