package com.roganskyerik.cookly

import android.app.Application
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CooklyApp : Application() {
    @Override
    override fun onCreate() {
        super.onCreate()

        FacebookSdk.sdkInitialize(this)
        FacebookSdk.setAutoLogAppEventsEnabled(true)

        AppEventsLogger.activateApp(this)
    }
}
