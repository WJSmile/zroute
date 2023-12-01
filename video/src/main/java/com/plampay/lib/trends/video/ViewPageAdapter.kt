package com.plampay.lib.trends.video

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPageAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    private val list: MutableList<String> = mutableListOf(
        "https://www.w3schools.com/html/movie.mp4",
        "https://media.w3.org/2010/05/sintel/trailer.mp4",
        "http://vjs.zencdn.net/v/oceans.mp4",
        "http://vjs.zencdn.net/v/oceans.mp4",
        "https://media.w3.org/2010/05/sintel/trailer.mp4",
        "https://media.w3.org/2010/05/sintel/trailer.mp4",
        "https://media.w3.org/2010/05/sintel/trailer.mp4",
    )

    override fun getItemCount(): Int {
        return list.size
    }

    override fun createFragment(position: Int): Fragment {
        return VideoFragment.getInstance(list[position])
    }
}