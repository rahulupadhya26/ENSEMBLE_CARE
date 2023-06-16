package com.app.selfcare.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.net.Uri
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import at.huber.youtubeExtractor.VideoMeta
import at.huber.youtubeExtractor.YouTubeExtractor
import at.huber.youtubeExtractor.YtFile
import com.app.selfcare.MainActivity
import com.app.selfcare.R
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.MergingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource


class ExpoPlayerUtils {

    private var player: SimpleExoPlayer? = null
    private var currentWindow = 0
    private var playbackPosition: Long = 0
    private var fullscreen = false
    private var playWhenReady: Boolean = true

    fun initializePlayer(context: Context, playerView: PlayerView, videoUrl: String?) {
        player = SimpleExoPlayer.Builder(context).build()
        playerView.player = player

        if (videoUrl!!.contains("youtube")) {
            playYoutubeVideo(context, videoUrl)
        } else {
            val uri = Uri.parse(videoUrl)
            val mediaSource = buildMediaSource(uri, context)
            //player!!.playWhenReady = playWhenReady
            player!!.seekTo(currentWindow, playbackPosition)
            player!!.prepare(mediaSource!!, false, false)
        }
        val fullScreen = playerView.findViewById<ImageView>(R.id.imgFullScreen)

        fullScreen.setImageDrawable(
            ContextCompat.getDrawable(
                context,
                com.google.android.exoplayer2.ui.R.drawable.exo_controls_fullscreen_exit
            )
        )
        fullscreen = true

        fullScreen.setOnClickListener {
            if (fullscreen) {
                fullScreen.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        com.google.android.exoplayer2.ui.R.drawable.exo_controls_fullscreen_enter
                    )
                )
                (context as MainActivity).window.decorView.systemUiVisibility =
                    View.SYSTEM_UI_FLAG_VISIBLE
                if (context.supportActionBar != null) {
                    context.supportActionBar!!.show()
                }
                context.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                val params: ConstraintLayout.LayoutParams =
                    playerView.layoutParams as ConstraintLayout.LayoutParams
                params.width = ViewGroup.LayoutParams.MATCH_PARENT
                params.height = (200 * context.resources
                    .displayMetrics.density).toInt()
                playerView.layoutParams = params
                fullscreen = false
            } else {
                fullScreen.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        com.google.android.exoplayer2.ui.R.drawable.exo_controls_fullscreen_exit
                    )
                )
                (context as MainActivity).window.decorView.systemUiVisibility =
                    (View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
                if (context.supportActionBar != null) {
                    context.supportActionBar!!.hide()
                }
                context.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                val params =
                    playerView.layoutParams as ConstraintLayout.LayoutParams
                params.width = ViewGroup.LayoutParams.MATCH_PARENT
                params.height = ViewGroup.LayoutParams.MATCH_PARENT
                playerView.layoutParams = params
                fullscreen = true
            }
        }
    }

    private fun buildMediaSource(uri: Uri, context: Context): MediaSource? {
        val dataSourceFactory: DataSource.Factory =
            DefaultDataSourceFactory(context, "exoplayer-codelab")
        return ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(uri))
    }

    fun releasePlayer() {
        if (player != null) {
            playbackPosition = player!!.currentPosition
            currentWindow = player!!.currentWindowIndex
            playWhenReady = player!!.playWhenReady
            player!!.release()
            player = null
        }
    }

    private fun playYoutubeVideo(context: Context, youtubeLink: String) {
        try {
            object : YouTubeExtractor(context) {
                @SuppressLint("StaticFieldLeak")
                override fun onExtractionComplete(
                    ytFiles: SparseArray<YtFile>?,
                    vMeta: VideoMeta?
                ) {
                    try {
                        if (ytFiles != null) {
                            val iTag = 137//tag of video 1080
                            val audioTag = 140 //tag m4a audio
                            // 720, 1080, 480
                            var videoUrl = ""
                            val iTags: List<Int> = listOf(22, 137, 18)
                            for (i in iTags) {
                                val ytFile = ytFiles.get(i)
                                if (ytFile != null) {
                                    val downloadUrl = ytFile.url
                                    if (downloadUrl != null && downloadUrl.isNotEmpty()) {
                                        videoUrl = downloadUrl
                                    }
                                }
                            }
                            if (videoUrl == "")
                                videoUrl = ytFiles[iTag].url
                            val audioUrl = ytFiles[audioTag].url
                            val audioSource: MediaSource = ProgressiveMediaSource
                                .Factory(DefaultHttpDataSource.Factory())
                                .createMediaSource(MediaItem.fromUri(audioUrl))
                            val videoSource: MediaSource = ProgressiveMediaSource
                                .Factory(DefaultHttpDataSource.Factory())
                                .createMediaSource(MediaItem.fromUri(videoUrl))

                            player!!.setMediaSource(
                                MergingMediaSource(true, videoSource, audioSource),
                                true
                            )
                            player!!.prepare()
                            player!!.playWhenReady = playWhenReady
                            player!!.seekTo(currentWindow, playbackPosition)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }.extract(youtubeLink, true, true)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}