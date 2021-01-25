package com.pcommon.lib_common.application

import android.app.Application


class CommApplication : AbstractApplication() {
    override fun onCreate() {
        super.onCreate()
        appContext = getApplication()
    }

    companion object {
        var appContext: Application? = null
    }
}


