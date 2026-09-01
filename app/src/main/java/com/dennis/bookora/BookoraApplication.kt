package com.dennis.bookora

import android.app.Application
import com.dennis.bookora.repository.auth.AuthSession
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BookoraApplication : Application() {
    companion object {
        private lateinit var _instance: BookoraApplication
        val instance: BookoraApplication get() = _instance
    }

    override fun onCreate() {
        super.onCreate()
        _instance = this
        AuthSession.init(this)
    }
}
