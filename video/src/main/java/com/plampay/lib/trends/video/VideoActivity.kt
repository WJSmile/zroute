package com.plampay.lib.trends.video

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.viewpager2.widget.ViewPager2

class VideoActivity : AppCompatActivity() {


    private lateinit var viewPage: ViewPager2

    private lateinit var exoPlayer: ExoPlayer

    private var lastPosition = 0

    @UnstableApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.video_layout)
        exoPlayer = ExoPlayerHelp.getExoPlayer(this)
        viewPage = findViewById(R.id.view_page)
        viewPage.offscreenPageLimit = 2
        viewPage.orientation = ViewPager2.ORIENTATION_VERTICAL
        viewPage.adapter = ViewPageAdapter(this)
        viewPage.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (lastPosition != position) {
                    exoPlayer.seekToDefaultPosition(position)
                }
                lastPosition = position
            }
        })
    }
}