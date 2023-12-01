package com.plampay.lib.trends.video

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VideoFragment: Fragment() {


    private lateinit var   video: PlayerView

    private lateinit var exoPlayer:ExoPlayer
    companion object{
        fun getInstance(url:String) = VideoFragment().apply {
            arguments = Bundle().apply {
                putString("url",url)
            }
        }
    }
    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       val rootView = inflater.inflate(R.layout.video_frament,container,false)
        video = rootView.findViewById(R.id.video)
        return rootView
    }


    @UnstableApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        context?.let {
            exoPlayer = ExoPlayerHelp.getExoPlayer(it)
        }

        arguments?.getString("url")?.let {
            lifecycleScope.launch(Dispatchers.Default) {
                //CacheController.cacheMedia(it)
            }
            exoPlayer.addMediaItem(MediaItem.fromUri(it))
        }
    }

    override fun onStart() {
        super.onStart()
        exoPlayer.prepare()
    }

    override fun onResume() {
        super.onResume()
        video.player = exoPlayer
        exoPlayer.play()
    }

    override fun onPause() {
        super.onPause()
        exoPlayer.pause()
        video.player = null

    }
}