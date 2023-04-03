package com.app.selfcare.fragment

import android.os.Bundle
import android.view.SurfaceView
import android.view.View
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.app.selfcare.R
import io.agora.rtc2.*
import io.agora.rtc2.video.VideoCanvas

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SampleGroupVideoCallFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SampleGroupVideoCallFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private val appId = "<Your app Id>"
    private val channelName = "Your channel name"
    private val token = "<your access token>"
    private val uid = 0
    private var isJoined = false
    private var agoraEngine: RtcEngine? = null
    private var localSurfaceView: SurfaceView? = null
    private var remoteSurfaceView: SurfaceView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_sample_group_video_call
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE
        updateStatusBarColor(R.color.white)

        setupVideoSDKEngine()
    }

    private fun setupVideoSDKEngine() {
        try {
            val config = RtcEngineConfig()
            config.mContext = requireActivity().baseContext
            config.mAppId = appId
            config.mEventHandler = mRtcEventHandler
            agoraEngine = RtcEngine.create(config)
            // By default, the video module is disabled, call enableVideo to enable it.
            agoraEngine!!.enableVideo()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val mRtcEventHandler: IRtcEngineEventHandler = object : IRtcEngineEventHandler() {
        // Listen for the remote host joining the channel to get the uid of the host.
        override fun onUserJoined(uid: Int, elapsed: Int) {
            displayToast("Remote user joined $uid")
            // Set the remote video view
            runOnUiThread { setupRemoteVideo(uid) }
        }

        override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
            isJoined = true
            displayToast("Joined Channel $channel")
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            displayToast("Remote user offline $uid $reason")
            runOnUiThread { remoteSurfaceView!!.visibility = View.GONE }
        }
    }

    private fun setupRemoteVideo(uid: Int) {
        val container: FrameLayout = requireView().findViewById(R.id.remote_video_view_container)
        remoteSurfaceView = SurfaceView(requireActivity().baseContext)
        remoteSurfaceView!!.setZOrderMediaOverlay(true)
        container.addView(remoteSurfaceView)
        agoraEngine!!.setupRemoteVideo(
            VideoCanvas(
                remoteSurfaceView,
                VideoCanvas.RENDER_MODE_FIT,
                uid
            )
        )
        // Display RemoteSurfaceView.
        remoteSurfaceView!!.visibility = View.VISIBLE
    }

    private fun setupLocalVideo() {
        val container: FrameLayout = requireView().findViewById(R.id.local_video_view_container)
        // Create a SurfaceView object and add it as a child to the FrameLayout.
        localSurfaceView = SurfaceView(requireActivity().baseContext)
        container.addView(localSurfaceView)
        // Pass the SurfaceView object to Agora so that it renders the local video.
        agoraEngine!!.setupLocalVideo(
            VideoCanvas(
                localSurfaceView,
                VideoCanvas.RENDER_MODE_HIDDEN,
                0
            )
        )
    }

    fun joinChannel() {
        val options = ChannelMediaOptions()
        // For a Video call, set the channel profile as COMMUNICATION.
        options.channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION
        // Set the client role as BROADCASTER or AUDIENCE according to the scenario.
        options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
        // Display LocalSurfaceView.
        setupLocalVideo()
        localSurfaceView!!.visibility = View.VISIBLE
        // Start local preview.
        agoraEngine!!.startPreview()
        // Join the channel with a temp token.
        // You need to specify the user ID yourself, and ensure that it is unique in the channel.
        agoraEngine!!.joinChannel(token, channelName, uid, options)
    }

    fun leaveChannel() {
        if (!isJoined) {
            displayToast("Join a channel first")
        } else {
            agoraEngine!!.leaveChannel()
            displayToast("You left the channel")
            // Stop remote video rendering.
            if (remoteSurfaceView != null) remoteSurfaceView!!.visibility = View.GONE
            // Stop local video rendering.
            if (localSurfaceView != null) localSurfaceView!!.visibility = View.GONE
            isJoined = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        agoraEngine!!.stopPreview()
        agoraEngine!!.leaveChannel()
        Thread {
            RtcEngine.destroy()
            agoraEngine = null
        }.start()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SampleGroupVideoCallFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SampleGroupVideoCallFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "SampleGroupVideoCallFragment"
    }
}