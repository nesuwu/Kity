package com.nesu.kity

import android.app.Application
import com.google.android.material.color.DynamicColors

class KityApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}