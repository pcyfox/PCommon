package com.pcommon.lib_common.application

import android.app.Application
import androidx.annotation.Keep

@Keep
class CommApplication : AbstractApplication() {
    override fun onCreate() {
        super.onCreate()
        appContext = getApplication()
    }

    companion object {
        var appContext: Application? = null
    }
}


