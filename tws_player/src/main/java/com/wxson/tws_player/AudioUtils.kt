package com.wxson.tws_player

import android.content.Context
import android.media.MediaPlayer
import java.util.*

enum class AudioUtils {
    INSTANCE;
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var timer: Timer
    private lateinit var timerTask: TimerTask
    fun playMedia(activity: Context?) {
        mediaPlayer = MediaPlayer.create(activity, R.raw.red_river_valley)
        timer = Timer()
        timerTask = object : TimerTask() {
            override fun run() {}
        }
        timer.schedule(timerTask, 0, 10)
        mediaPlayer.start()
    }

    fun stopPlay() {
        mediaPlayer.stop()
    }
}