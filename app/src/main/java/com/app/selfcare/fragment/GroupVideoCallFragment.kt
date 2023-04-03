package com.app.selfcare.fragment

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.app.selfcare.BuildConfig
import com.app.selfcare.R
import com.app.selfcare.controller.IOnBackPressed
import io.agora.agorauikit_android.AgoraConnectionData
import io.agora.agorauikit_android.AgoraSettings
import io.agora.agorauikit_android.AgoraVideoViewer
import io.agora.agorauikit_android.AgoraVideoViewerDelegate
import io.agora.rtc2.Constants
import io.agora.rtc2.video.VideoEncoderConfiguration


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [GroupVideoCallFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GroupVideoCallFragment : BaseFragment(), IOnBackPressed {
    // TODO: Rename and change types of parameters
    private var token: String? = null
    private var channelName: String? = null
    var agView: AgoraVideoViewer? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            token = it.getString(ARG_PARAM1)
            channelName = it.getString(ARG_PARAM2)
        }
    }

    override fun getLayout(): Int {
        return R.layout.activity_group_video_call
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE

        try {

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

            agView!!.delegate = delegate

            agView!!.join(channelName!!, token, role = Constants.CHANNEL_PROFILE_COMMUNICATION)

        } catch (e: Exception) {
            e.printStackTrace()
            displayToast("Try again...")
        }
    }

    private val delegate = object : AgoraVideoViewerDelegate {
        override fun joinedChannel(channel: String) {
            super.joinedChannel(channel)

        }

        override fun leftChannel(channel: String) {
            super.leftChannel(channel)
            setLayoutBottomNavigation(null)
            replaceFragmentNoBackStack(
                BottomNavigationFragment(),
                R.id.layout_home,
                BottomNavigationFragment.TAG
            )
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

    override fun onBackPressed(): Boolean {
        if (agView != null) {
            agView!!.leaveChannel()
            agView = null
        }
        setLayoutBottomNavigation(null)
        replaceFragmentNoBackStack(
            BottomNavigationFragment(),
            R.id.layout_home,
            BottomNavigationFragment.TAG
        )
        return false
    }
}