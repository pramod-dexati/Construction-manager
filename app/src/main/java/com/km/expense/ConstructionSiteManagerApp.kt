package com.km.expense

import android.app.Application

class ConstructionSiteManagerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }
    
    companion object {
        lateinit var instance: ConstructionSiteManagerApp
            private set
    }
}
