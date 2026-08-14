package com.dennis.bookora

import android.app.Application
import com.dennis.bookora.repository.auth.AuthSession
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BookoraApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AuthSession.init(this)
    }
}
