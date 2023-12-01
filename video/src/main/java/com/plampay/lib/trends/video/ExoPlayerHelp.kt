package com.plampay.lib.trends.video

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.ExoPlayer
import java.io.File

object ExoPlayerHelp {

    private var exoPlayer: ExoPlayer? = null

    @UnstableApi
    private fun getCache(context: Context): DataSource.Factory {
        val cache = SimpleCache(
            File(context.cacheDir, "example_media_cache"),
            LeastRecentlyUsedCacheEvictor(100 * 1024 * 1024),
            ExampleDatabaseProvider(context)
        )
        // 根据缓存目录创立缓存数据源
        return CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(
                DefaultHttpDataSource.Factory().setAllowCrossProtocolRedirects(true)
            )
    }

    @UnstableApi
    fun getExoPlayer(context: Context): ExoPlayer {

        if (exoPlayer == null) {
            exoPlayer = CacheController.getMediaSourceFactory()?.let {
                ExoPlayer.Builder(context)
                    .setMediaSourceFactory(it)
                    .build()
            }
        }
        return exoPlayer!!
    }
}