package com.smart.zwj.zroute

import android.app.Application
import androidx.media3.common.util.UnstableApi
import com.plampay.lib.trends.video.CacheController

class App:Application() {

    @UnstableApi
    override fun onCreate() {
        super.onCreate()
        CacheController.init(this)
    }
}