package com.wenshu.app

import android.app.Application
import com.wenshu.app.data.SharedPreferencesManager

class WenshuApp : Application() {

    companion object {
        lateinit var instance: WenshuApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
