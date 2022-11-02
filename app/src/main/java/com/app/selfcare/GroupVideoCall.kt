package com.app.selfcare

import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.agora.agorauikit_android.AgoraConnectionData
import io.agora.agorauikit_android.AgoraSettings
import io.agora.agorauikit_android.AgoraVideoViewer
import io.agora.agorauikit_android.AgoraVideoViewerDelegate
import io.agora.rtc2.Constants
import io.agora.rtc2.video.VideoEncoderConfiguration


class GroupVideoCall : AppCompatActivity() {

    var agView: AgoraVideoViewer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_video_call)

        try {
            val intent = intent
            val token = intent.getStringExtra("token")
            val channelName = intent.getStringExtra("channelName")

            // Set the video profile
            val videoConfig = VideoEncoderConfiguration()
            // Set mirror mode
            videoConfig.mirrorMode = VideoEncoderConfiguration.MIRROR_MODE_TYPE.MIRROR_MODE_AUTO
            // Set framerate
            videoConfig.frameRate = VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_10.value
            // Set bitrate
            videoConfig.bitrate = VideoEncoderConfiguration.STANDARD_BITRATE
            // Set dimensions
            videoConfig.dimensions = VideoEncoderConfiguration.VD_640x360
            // Set orientation mode
            videoConfig.orientationMode =
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_ADAPTIVE
            // Set degradation preference
            videoConfig.degradationPrefer =
                VideoEncoderConfiguration.DEGRADATION_PREFERENCE.MAINTAIN_BALANCED
            val agSettings = AgoraSettings()
            agSettings.videoConfiguration = videoConfig
            agView = AgoraVideoViewer(
                this, AgoraConnectionData(BuildConfig.appId, token),
                agoraSettings = agSettings
            )
            this.addContentView(
                agView,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            )

            agView!!.delegate = delegate

            agView!!.join(channelName!!, token, role = Constants.CLIENT_ROLE_BROADCASTER)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Try again...", Toast.LENGTH_LONG).show()
        }
    }

    private val delegate = object : AgoraVideoViewerDelegate {
        override fun joinedChannel(channel: String) {
            super.joinedChannel(channel)

        }

        override fun leftChannel(channel: String) {
            super.leftChannel(channel)
            finish()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (agView != null) {
            agView!!.leaveChannel()
            agView = null
        }
        finish()
    }
}