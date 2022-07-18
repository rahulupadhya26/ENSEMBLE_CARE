package com.app.selfcare.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.app.selfcare.R
import com.app.selfcare.data.Appointment
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.RtcEngine
import io.agora.rtc.video.VideoCanvas
import io.agora.rtc.video.VideoEncoderConfiguration
import kotlinx.android.synthetic.main.fragment_video_call.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [VideoCallFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class VideoCallFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var appointment: Appointment? = null
    private var param2: String? = null

    // Fill the App ID of your project generated on Agora Console.
    private val APP_ID = "faf494ec1c25481893b8041961433ab8"

    // Fill the channel name.
    private val CHANNEL = "sampleagora"

    // Fill the temp token generated on Agora Console.
    private val TOKEN =
        "006faf494ec1c25481893b8041961433ab8IADCRyETtNwgim1HMxhaNsXWT9cemzhjGQDsZrQ0rXp02qvypUEAAAAAEABdi2YtG1yYYgEAAQAcXJhi"
    private val PERMISSION_REQUEST_ID = 7

    // Ask for Android device permissions at runtime.
    private val ALL_REQUESTED_PERMISSIONS = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CAMERA,
        Manifest.permission.READ_PHONE_STATE
    )

    private var mEndCall = false
    private var mMuted = false
    private var remoteView: SurfaceView? = null
    private var localView: SurfaceView? = null
    private lateinit var rtcEngine: RtcEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            appointment = it.getParcelable(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_video_call
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE

        // If all the permissions are granted, initialize the RtcEngine object and join a channel.
        if (checkSelfPermission()) {
            initAndJoinChannel()
        } else {
            requestPermission(PERMISSION_REQUEST_ID)
        }

        buttonCall.setOnClickListener {
            if (mEndCall) {
                startCall()
                mEndCall = false
                buttonCall.setImageResource(R.drawable.btn_endcall)
                buttonMute.visibility = View.VISIBLE
                buttonSwitchCamera.visibility = View.VISIBLE
            } else {
                endCall()
                popBackStack()
                /*mEndCall = true
                buttonCall.setImageResource(R.drawable.btn_startcall)
                buttonMute.visibility = View.INVISIBLE
                buttonSwitchCamera.visibility = View.INVISIBLE*/
            }
        }

        buttonSwitchCamera.setOnClickListener {
            rtcEngine.switchCamera()
        }

        buttonMute.setOnClickListener {
            mMuted = !mMuted
            rtcEngine.muteLocalAudioStream(mMuted)
            val res: Int = if (mMuted) {
                R.drawable.btn_mute
            } else {
                R.drawable.btn_unmute
            }
            buttonMute.setImageResource(res)
        }
    }

    private val mRtcEventHandler = object : IRtcEngineEventHandler() {

        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
            requireActivity().runOnUiThread {
                displayToast("Joined Channel Successfully")
            }
        }

        /*
         * Listen for the onFirstRemoteVideoDecoded callback.
         * This callback occurs when the first video frame of a remote user is received and decoded after the remote user successfully joins the channel.
         * You can call the setupRemoteVideoView method in this callback to set up the remote video view.
         */
        override fun onFirstRemoteVideoDecoded(uid: Int, width: Int, height: Int, elapsed: Int) {
            requireActivity().runOnUiThread {
                setupRemoteVideoView(uid)
            }
        }

        /*
        * Listen for the onUserOffline callback.
        * This callback occurs when the remote user leaves the channel or drops offline.
        */
        override fun onUserOffline(uid: Int, reason: Int) {
            requireActivity().runOnUiThread {
                onRemoteUserLeft()
            }
        }
    }

    private fun initAndJoinChannel() {

        // This is our usual steps for joining
        // a channel and starting a call.
        initRtcEngine()
        setupVideoConfig()
        setupLocalVideoView()
        joinChannel()
    }

    // Initialize the RtcEngine object.
    private fun initRtcEngine() {
        try {
            rtcEngine = RtcEngine.create(requireActivity(), APP_ID, mRtcEventHandler)
        } catch (e: Exception) {
            Log.d("TAG", "initRtcEngine: $e")
        }
    }

    private fun setupLocalVideoView() {
        localView = RtcEngine.CreateRendererView(requireActivity())
        localView!!.setZOrderMediaOverlay(true)
        localVideoView.addView(localView)
        // Set the local video view.
        rtcEngine.setupLocalVideo(VideoCanvas(localView, VideoCanvas.RENDER_MODE_HIDDEN, 0))
    }

    private fun setupRemoteVideoView(uid: Int) {
        if (remoteVideoView.childCount > 1) {
            return
        }
        remoteView = RtcEngine.CreateRendererView(requireActivity())
        remoteVideoView.addView(remoteView)
        rtcEngine.setupRemoteVideo(VideoCanvas(remoteView, VideoCanvas.RENDER_MODE_FILL, uid))
    }

    private fun setupVideoConfig() {
        rtcEngine.enableVideo()
        rtcEngine.setVideoEncoderConfiguration(
            VideoEncoderConfiguration(
                VideoEncoderConfiguration.VD_640x360,
                VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                VideoEncoderConfiguration.STANDARD_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT
            )
        )
    }

    private fun joinChannel() {
        //val token = getString(R.string.agora_token)
        // Join a channel with a token.
        rtcEngine.joinChannel(TOKEN, CHANNEL, "Extra Optional Data", 0)
    }

    private fun startCall() {
        setupLocalVideoView()
        joinChannel()
    }

    private fun endCall() {
        removeLocalVideo()
        removeRemoteVideo()
        leaveChannel()
    }

    private fun removeLocalVideo() {
        if (localView != null) {
            localVideoView.removeView(localView)
        }
        localView = null
    }

    private fun removeRemoteVideo() {
        if (remoteView != null) {
            remoteVideoView.removeView(remoteView)
        }
        remoteView = null
    }

    private fun leaveChannel() {
        rtcEngine.leaveChannel()
    }

    private fun onRemoteUserLeft() {
        removeRemoteVideo()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!mEndCall) {
            leaveChannel()
        }
        RtcEngine.destroy()
    }

    private fun checkSelfPermission(): Boolean {
        return (ContextCompat.checkSelfPermission(requireActivity(), ALL_REQUESTED_PERMISSIONS[0])
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(requireActivity(), ALL_REQUESTED_PERMISSIONS[1])
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(requireActivity(), ALL_REQUESTED_PERMISSIONS[2])
                == PackageManager.PERMISSION_GRANTED)
    }

    private fun requestPermission(id: Int) {
        ActivityCompat.requestPermissions(
            requireActivity(), arrayOf(
                ALL_REQUESTED_PERMISSIONS[0],
                ALL_REQUESTED_PERMISSIONS[1],
                ALL_REQUESTED_PERMISSIONS[2]
            ), id
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_ID) {
            if (
                grantResults[0] != PackageManager.PERMISSION_GRANTED ||
                grantResults[1] != PackageManager.PERMISSION_GRANTED ||
                grantResults[2] != PackageManager.PERMISSION_GRANTED
            ) {
                displayToast("Permissions needed")
                requireActivity().finish()
                return
            }
            // Here we continue only if all permissions are granted.
            // The permissions can also be granted in the system settings manually.
            initAndJoinChannel()
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment VideoCallFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: Appointment, param2: String = "") =
            VideoCallFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_Video_Call"
    }
}