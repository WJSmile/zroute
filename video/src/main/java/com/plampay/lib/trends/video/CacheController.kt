package com.plampay.lib.trends.video

import android.annotation.SuppressLint
import android.content.Context
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.CacheSpan
import androidx.media3.datasource.cache.CacheWriter
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

@UnstableApi
class CacheController(context: Context) {
    private val cache: Cache
    private val cacheDataSourceFactory: CacheDataSource.Factory
    private val cacheDataSource: CacheDataSource
    private val cacheTask: ConcurrentHashMap<String, CacheWriter> = ConcurrentHashMap()

    init {


        // 设置缓存目录和缓存机制，假如不需求清除缓存能够运用NoOpCacheEvictor
        cache = SimpleCache(
            File(context.cacheDir, "example_media_cache"),
            LeastRecentlyUsedCacheEvictor(1024 * 1024 * 300),
            ExampleDatabaseProvider(context)
        )
        // 根据缓存目录创立缓存数据源
        cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(cache)
            // 设置上游数据源，缓存未射中时经过此获取数据
            .setUpstreamDataSourceFactory(
                DefaultHttpDataSource.Factory().setAllowCrossProtocolRedirects(true)
            )
        cacheDataSource = cacheDataSourceFactory.createDataSource()
    }

    companion object {
        @Volatile
        private var cacheController: CacheController? = null
        fun init(context: Context) {
            if (cacheController == null) {
                synchronized(CacheController::class.java) {
                    if (cacheController == null) {
                        cacheController = CacheController(context)
                    }
                }
            }
        }

        fun cacheMedia(mediaSources: ArrayList<String>) {
            cacheController?.run {
                mediaSources.forEach { mediaUrl ->
                    // 创立CacheWriter缓存数据
                    CacheWriter(
                        cacheDataSource,
                        DataSpec.Builder()
                            // 设置资源链接
                            .setUri(mediaUrl)
                            // 设置需求缓存的巨细（能够只缓存一部分）
                            .setLength((getMediaResourceSize(mediaUrl) * 0.2).toLong())
                            .build(),
                        null
                    ) { _, _, _ ->
                        // 缓冲进度改变时回调
                        // requestLength 恳求总巨细
                        // bytesCached 已缓冲的字节数
                        // newBytesCached 新缓冲的字节数
                    }.let { cacheWriter ->
                        cacheWriter.cache()
                        cacheTask[mediaUrl] = cacheWriter
                    }
                }
            }
        }

        @SuppressLint("SuspiciousIndentation")
        fun cacheMedia(mediaUrl: String) {
            cacheController?.run {
                Log.e(">>>>>", (cache.getCachedBytes(mediaUrl, 0, Long.MAX_VALUE)/1024F/1024F).toString()+mediaUrl)

                if (cacheTask.containsKey(mediaUrl)){
                    return
                }
                if (cache.getCachedBytes(mediaUrl, 0, Long.MAX_VALUE)>1024*1024*1){
                    return
                }
                // 创立CacheWriter缓存数据
                CacheWriter(
                    cacheDataSource,
                    DataSpec.Builder()
                        // 设置资源链接
                        .setUri(mediaUrl)
                        // 设置需求缓存的巨细（能够只缓存一部分）
                        .setLength(1024 * 1024 * 2)
                        .build(),
                    null
                ) { requestLength, bytesCached, newBytesCached ->
                    // 缓冲进度改变时回调
                    // requestLength 恳求总巨细
                    // bytesCached 已缓冲的字节数
                    // newBytesCached 新缓冲的字节数
                    Log.e(">>>>>>",bytesCached.toString())
                }.let { cacheWriter ->
                    try {
                        cacheWriter.cache()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    cacheTask[mediaUrl] = cacheWriter
                }
            }
        }

        fun cancelCache(mediaUrl: String) {
            // 撤销缓存
            cacheController?.cacheTask?.get(mediaUrl)?.cancel()
            cacheController?.cacheTask?.remove(mediaUrl)
        }

        fun getMediaSourceFactory(): MediaSource.Factory? {
            var mediaSourceFactory: MediaSource.Factory? = null
            cacheController?.run {
                // 创立逐渐加载数据的数据源
                mediaSourceFactory = ProgressiveMediaSource.Factory(cacheDataSourceFactory)
            }
            return mediaSourceFactory
        }

        fun release() {
            cacheController?.cacheTask?.values?.forEach { it.cancel() }
            cacheController?.cache?.release()
        }
    }

    // 获取媒体资源的巨细
    private fun getMediaResourceSize(mediaUrl: String): Long {
        try {
            val connection = URL(mediaUrl).openConnection() as HttpURLConnection
            // 恳求办法设置为HEAD，只获取恳求头
            connection.requestMethod = "HEAD"
            connection.connect()
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                return connection.getHeaderField("Content-Length").toLong()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 0L
    }
}