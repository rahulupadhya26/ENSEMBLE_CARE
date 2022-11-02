package com.app.selfcare.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import android.view.View
import android.widget.FrameLayout
import android.widget.Switch
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.app.selfcare.BuildConfig
import com.app.selfcare.R
import io.agora.agorauikit_android.AgoraConnectionData
import io.agora.agorauikit_android.AgoraSettings
import io.agora.agorauikit_android.AgoraVideoViewer
import io.agora.agorauikit_android.AgoraVideoViewerDelegate
import io.agora.rtc2.*
import io.agora.rtc2.video.VideoCanvas
import io.agora.rtc2.video.VideoEncoderConfiguration
import kotlinx.android.synthetic.main.fragment_group_video_call.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [GroupVideoCallFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GroupVideoCallFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var token: String? = null
    private var channelName: String? = null

    // An integer that identifies the local user.
    private val uid = 0
    private var isJoined = false

    private var agoraEngine: RtcEngine? = null

    //SurfaceView to render local video in a Container.
    private var localSurfaceView: SurfaceView? = null

    //SurfaceView to render Remote video in a Container.
    private var remoteSurfaceView: SurfaceView? = null

    private val PERMISSION_REQ_ID = 22
    private val REQUESTED_PERMISSIONS = arrayOf<String>(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CAMERA
    )

    var agView: AgoraVideoViewer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            token = it.getString(ARG_PARAM1)
            channelName = it.getString(ARG_PARAM2)
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_group_video_call
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE

        /*setupVideoSDKEngine()

        joinButton.setOnClickListener {
           joinChannel(view)
        }

        leaveButton.setOnClickListener {
          leaveChannel(view)
        }*/

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
        var agSettings = AgoraSettings()
        agSettings.videoConfiguration = VideoEncoderConfiguration()
        agView = AgoraVideoViewer(
            requireActivity(), AgoraConnectionData(BuildConfig.appId, token),
            agoraSettings = agSettings
        )
        requireActivity().addContentView(
            agView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        agView!!.join(channelName!!,token, role = Constants.CLIENT_ROLE_BROADCASTER)

        agView!!.delegate = object : AgoraVideoViewerDelegate {
            override fun joinedChannel(channel: String) {
                super.joinedChannel(channel)
                Log.i("" +
                        "","joined")
            }

            override fun leftChannel(channel: String) {
                super.leftChannel(channel)
                Log.i("GroupVideoCall","left")
                popBackStack()
            }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment GroupVideoCallFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            GroupVideoCallFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
        const val TAG = "Screen_group_video_call"
    }
}