package com.plampay.lib.trends.video

import android.content.Context
import android.util.AttributeSet
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

class VideoView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : PlayerView(context, attrs) {

    private val exoPlayer = ExoPlayer.Builder(context).build()

    fun setUrl(url:String){
        exoPlayer.addMediaItem(MediaItem.fromUri(url))
        exoPlayer.prepare()
    }

    fun play(){
        exoPlayer.play()
    }

    fun pause(){
        exoPlayer.pause()
    }



}