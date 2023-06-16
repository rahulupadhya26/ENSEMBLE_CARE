package com.app.selfcare.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.OpenableColumns
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.BaseActivity
import com.app.selfcare.BuildConfig
import com.app.selfcare.R
import com.app.selfcare.adapters.MessageAdapter
import com.app.selfcare.controller.OnBottomReachedListener
import com.app.selfcare.controller.OnMessageClickListener
import com.app.selfcare.data.FileDetails
import com.app.selfcare.data.GetAppointment
import com.app.selfcare.data.MessageBean
import com.app.selfcare.data.MessageListBean
import com.app.selfcare.databinding.DialogOnlineChatBinding
import com.app.selfcare.databinding.FragmentVideoCallBinding
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper.get
import com.app.selfcare.realTimeMessaging.ChatManager
import com.app.selfcare.services.SelfCareApplication
import com.app.selfcare.utils.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.video.VideoCanvas
import io.agora.rtc2.video.VideoEncoderConfiguration
import io.agora.rtm.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject
import retrofit2.HttpException
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val ARG_PARAM3 = "param3"
private const val ARG_PARAM4 = "param4"

/**
 * A simple [Fragment] subclass.
 * Use the [VideoCallFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class VideoCallFragment : BaseFragment(), OnMessageClickListener, OnBottomReachedListener {
    // TODO: Rename and change types of parameters
    private var appointment: GetAppointment? = null
    private var RTC_TOKEN: String? = null
    private var RTM_TOKEN: String? = null
    private var CHANNEL_NAME: String? = null
    private var createPostDialog: BottomSheetDialog? = null
    private var actualStartTime: String? = null
    private var actualEndTime: String? = null
    private var duration: String? = null

    // Fill the App ID of your project generated on Agora Console.
    //Gourav
    //private val APP_ID = "0583a41d1d7a40fbbed542383e62f45d"
    //Rahul
    //private val APP_ID = "faf494ec1c25481893b8041961433ab8"

    // Fill the channel name.
    private var CHANNEL = ""

    // Fill the temp token generated on Agora Console.
    private val PERMISSION_REQUEST_ID = 7

    // Ask for Android device permissions at runtime.
    private val ALL_REQUESTED_PERMISSIONS = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CAMERA,
        Manifest.permission.READ_PHONE_STATE
    )

    private var mEndCall = false
    private var mMuted = false
    private var mVideoDisabled = false
    private var remoteView: SurfaceView? = null
    private var localView: SurfaceView? = null
    private lateinit var rtcEngine: RtcEngine

    private var mRtmClient: RtmClient? = null
    private var mClientListener: RtmClientListener? = null
    private var mRtmChannel: RtmChannel? = null

    private var mIsPeerToPeerMode = false
    private var mPeerId = ""
    private var mChannelName = ""
    private var mChannelMemberCount = 1

    private val mMessageBeanList: ArrayList<MessageBean> = ArrayList()
    private var mMessageAdapter: MessageAdapter? = null

    // Number of seconds displayed
    // on the stopwatch.
    private var seconds = 0

    // Is the stopwatch running?
    private var running = false

    private var wasRunning = false
    private lateinit var binding: FragmentVideoCallBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            appointment = it.getParcelable(ARG_PARAM1)
            RTC_TOKEN = it.getString(ARG_PARAM2)
            RTM_TOKEN = it.getString(ARG_PARAM3)
            CHANNEL_NAME = it.getString(ARG_PARAM4)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentVideoCallBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getLayout(): Int {
        return R.layout.fragment_video_call
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE

        CHANNEL = appointment!!.channel_name
        //TOKEN = appointment!!.rtc_token

        if (appointment!!.appointment!!.type_of_visit == "Video") {
            binding.videoCallLayout.visibility = View.VISIBLE
            binding.audioCallLayout.visibility = View.GONE
        } else {
            binding.audioCallLayout.visibility = View.VISIBLE
            binding.videoCallLayout.visibility = View.GONE
            try {
                val photo = preference!![PrefKeys.PREF_PHOTO, ""]!!
                if (photo.isNotEmpty()) {
                    Glide.with(requireActivity())
                        .load(BaseActivity.baseURL.dropLast(5) + photo)
                        .placeholder(R.drawable.user_pic)
                        .transform(CenterCrop(), RoundedCorners(5))
                        .into(binding.imgUserLargeImage)
                    Glide.with(requireActivity())
                        .load(BaseActivity.baseURL.dropLast(5) + photo)
                        .placeholder(R.drawable.user_pic)
                        .transform(CenterCrop(), RoundedCorners(5))
                        .into(binding.imgUserSmallImage)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // If all the permissions are granted, initialize the RtcEngine object and join a channel.
        if (checkSelfPermission()) {
            initAndJoinChannel()
        } else {
            requestPermission(PERMISSION_REQUEST_ID)
        }

        binding.txtUserNameVideo.text =
            appointment!!.doctor_first_name + " " + appointment!!.doctor_last_name
        /*preference!![PrefKeys.PREF_FNAME, ""] + " " + preference!![PrefKeys.PREF_LNAME, ""]*/

        binding.txtUserNameAudio.text =
            appointment!!.doctor_first_name + " " + appointment!!.doctor_last_name
        /*preference!![PrefKeys.PREF_FNAME, ""] + " " + preference!![PrefKeys.PREF_LNAME, ""]*/

        binding.buttonAudioCall.setOnClickListener {
            if (mEndCall) {
                startCall()
                mEndCall = false
                binding.buttonAudioCall.setImageResource(R.drawable.btn_endcall)
                binding.buttonAudioMute.visibility = View.VISIBLE
            } else {
                endVideoCall()
            }
        }

        binding.buttonAudioMute.setOnClickListener {
            mMuted = !mMuted
            rtcEngine.muteLocalAudioStream(mMuted)
            val res: Int = if (mMuted) {
                R.drawable.btn_mute
            } else {
                R.drawable.btn_unmute
            }
            binding.buttonAudioMute.setImageResource(res)
        }

        binding.buttonAudioChat.setOnClickListener {
            if (createPostDialog != null) {
                createPostDialog!!.show()
            } else {
                displayMsg("Alert", "Cannot open chat screen.")
            }
        }

        binding.buttonCall.setOnClickListener {
            if (mEndCall) {
                startCall()
                mEndCall = false
                binding.buttonCall.setImageResource(R.drawable.btn_endcall)
                binding.buttonMute.visibility = View.VISIBLE
                binding.buttonSwitchCamera.visibility = View.VISIBLE
            } else {
                endVideoCall()
                /*mEndCall = true
                buttonCall.setImageResource(R.drawable.btn_startcall)
                buttonMute.visibility = View.INVISIBLE
                buttonSwitchCamera.visibility = View.INVISIBLE*/
            }
        }

        binding.buttonSwitchCamera.setOnClickListener {
            rtcEngine.switchCamera()
        }

        binding.buttonSwitchVideo.setOnClickListener {
            mVideoDisabled = !mVideoDisabled
            val res: Int = if (mVideoDisabled) {
                rtcEngine.enableLocalVideo(false)
                //rtcEngine.disableVideo()
                R.mipmap.ic_video_disabled
            } else {
                rtcEngine.enableLocalVideo(true)
                //rtcEngine.enableVideo()
                R.mipmap.ic_video_enabled
            }
            binding.buttonSwitchVideo.setImageResource(res)
        }

        binding.buttonChat.setOnClickListener {
            //Navigate to chat screen
            //displayToast("Screen under construction...")
            if (createPostDialog != null) {
                createPostDialog!!.show()
            } else {
                displayMsg("Alert", "Cannot open chat screen.")
            }
            /*replaceFragment(
                newInstance(appointment!!, targetName),
                R.id.layout_home, TAG
            )*/
        }

        binding.buttonMute.setOnClickListener {
            mMuted = !mMuted
            rtcEngine.muteLocalAudioStream(mMuted)
            val res: Int = if (mMuted) {
                R.drawable.btn_mute
            } else {
                R.drawable.btn_unmute
            }
            binding.buttonMute.setImageResource(res)
        }
    }

    private fun endVideoCall() {
        val builder = AlertDialog.Builder(mActivity!!)
        builder.setTitle("Confirmation")
        builder.setMessage("Do you wish to end the call?")
        builder.setPositiveButton("Yes") { dialog, which ->
            dialog.dismiss()
            processEndCall()
        }
        builder.setNegativeButton("No") { dialog, which ->
            dialog.dismiss()
        }
        builder.setCancelable(false)
        builder.show()
    }

    private fun processEndCall(){
        val date: Calendar = Calendar.getInstance()
        val nextMonthDate = SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(date.time)
        val endTime = DateUtils(nextMonthDate)
        actualEndTime = endTime.getTime()
        duration = binding.txtUserAudioTime.text.toString()
        endCall()
        Utils.rtmLoggedIn = false
        sendApptStatus(
            appointment!!.appointment!!.appointment_id.toString(),
            actualStartTime!!, actualEndTime!!, duration!!
        ) {
            clearPreviousFragmentStack()
            replaceFragmentNoBackStack(
                TherapistFeedbackFragment.newInstance(appointment!!),
                R.id.layout_home,
                TherapistFeedbackFragment.TAG
            )
        }
    }

    private val mRtcEventHandler = object : IRtcEngineEventHandler() {

        override fun onUserJoined(uid: Int, elapsed: Int) {
            super.onUserJoined(uid, elapsed)
            requireActivity().runOnUiThread(Runnable { // Call setupRemoteVideo to set the remote video view after getting uid from the onUserJoined callback.
                setupRemoteVideoView(uid)
            })
        }

        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
            requireActivity().runOnUiThread {
                //displayToast("Joined Channel Successfully")
            }
        }

        /*
         * Listen for the onFirstRemoteVideoDecoded callback.
         * This callback occurs when the first video frame of a remote user is received and decoded after the remote user successfully joins the channel.
         * You can call the setupRemoteVideoView method in this callback to set up the remote video view.
         */
        override fun onFirstRemoteVideoDecoded(uid: Int, width: Int, height: Int, elapsed: Int) {
            /*requireActivity().runOnUiThread {
                setupRemoteVideoView(uid)
            }*/
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
            rtcEngine = RtcEngine.create(requireActivity(), BuildConfig.appId, mRtcEventHandler)

            //mRtmClient = mChatManager!!.getRtmClient()
            mRtmClient = RtmClient.createInstance(
                requireActivity(),
                BuildConfig.appId,
                object : RtmClientListener {
                    override fun onConnectionStateChanged(state: Int, reason: Int) {
                        runOnUiThread {
                            when (state) {
                                RtmStatusCode.ConnectionState.CONNECTION_STATE_RECONNECTING -> showToast(
                                    getString(R.string.reconnecting)
                                )

                                RtmStatusCode.ConnectionState.CONNECTION_STATE_ABORTED -> {
                                    showToast(getString(R.string.account_offline))
                                    requireActivity().setResult(MessageUtil.ACTIVITY_RESULT_CONN_ABORTED)
                                    popBackStack()
                                }
                            }
                        }
                    }

                    override fun onMessageReceived(message: RtmMessage?, peerId: String?) {
                        runOnUiThread {
                            //if (peerId == mPeerId) {
                            if (message!!.messageType == 1) {
                                if (JSONObject(message.text).has("audio")) {
                                    val isAudio = JSONObject(message.text).getBoolean("audio")
                                    mMuted = !isAudio
                                    rtcEngine.muteLocalAudioStream(mMuted)
                                    val res: Int = if (mMuted) {
                                        R.drawable.btn_mute
                                    } else {
                                        R.drawable.btn_unmute
                                    }
                                    binding.buttonMute.setImageResource(res)
                                    binding.buttonAudioMute.setImageResource(res)
                                    binding.buttonMute.isEnabled = isAudio
                                    binding.buttonAudioMute.isEnabled = isAudio
                                } else if (JSONObject(message.text).has("video")) {
                                    val isVideo = JSONObject(message.text).getBoolean("video")
                                    mVideoDisabled = !isVideo
                                    val res: Int = if (mVideoDisabled) {
                                        rtcEngine.enableLocalVideo(false)
                                        //rtcEngine.disableVideo()
                                        R.mipmap.ic_video_disabled
                                    } else {
                                        rtcEngine.enableLocalVideo(true)
                                        //rtcEngine.enableVideo()
                                        R.mipmap.ic_video_enabled
                                    }
                                    binding.buttonSwitchVideo.setImageResource(res)
                                    binding.buttonSwitchVideo.isEnabled = isVideo
                                } else if (JSONObject(message.text).has("block")) {
                                    val isBlocked = JSONObject(message.text).getBoolean("block")
                                    if (isBlocked) {
                                        displayToast("You have been blocked by the provider.")
                                        processEndCall()
                                    }
                                }
                            } else {
                                val messageBean = MessageBean(mPeerId, message, false)
                                messageBean.setBackground(getMessageColor(mPeerId))
                                mMessageBeanList.add(messageBean)
                                mMessageAdapter!!.notifyItemRangeChanged(mMessageBeanList.size, 1)
                                onlineChatView.chatMessageList.scrollToPosition(mMessageBeanList.size - 1)
                            }
                            /*} else {
                                MessageUtil.addMessageBean(peerId!!, message)
                            }*/
                        }
                    }

                    override fun onImageMessageReceivedFromPeer(p0: RtmImageMessage?, p1: String?) {
                    }

                    override fun onFileMessageReceivedFromPeer(p0: RtmFileMessage?, p1: String?) {
                        Log.i("File received", "Hurrray ")
                    }

                    override fun onMediaUploadingProgress(
                        p0: RtmMediaOperationProgress?,
                        p1: Long
                    ) {
                    }

                    override fun onMediaDownloadingProgress(
                        p0: RtmMediaOperationProgress?,
                        p1: Long
                    ) {
                    }

                    override fun onTokenExpired() {
                    }

                    override fun onPeersOnlineStatusChanged(p0: MutableMap<String, Int>?) {
                    }

                })
            if (!Utils.rtmLoggedIn) {
                doLogin()
            }
        } catch (e: Exception) {
            Log.d("TAG", "initRtcEngine: $e")
        }
    }

    private fun setupLocalVideoView() {
        try {
            localView = RtcEngine.CreateRendererView(requireActivity())
            localView!!.setZOrderMediaOverlay(true)
            binding.localVideoView.addView(localView)
            // Set the local video view.
            rtcEngine.setupLocalVideo(VideoCanvas(localView, VideoCanvas.RENDER_MODE_HIDDEN, 0))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupRemoteVideoView(uid: Int) {
        try {
            /*if (binding.remoteVideoView.childCount > 1) {
                return
            }*/
            remoteView = RtcEngine.CreateRendererView(requireActivity())
            binding.remoteVideoView.addView(remoteView)
            rtcEngine.setupRemoteVideo(VideoCanvas(remoteView, VideoCanvas.RENDER_MODE_FIT, uid))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupVideoConfig() {
        try {
            if (appointment!!.appointment!!.type_of_visit == "Video") {
                rtcEngine.enableVideo()
            } else {
                rtcEngine.disableVideo()
                rtcEngine.enableAudio()
                rtcEngine.setDefaultAudioRoutetoSpeakerphone(true)
            }
            //rtcEngine.enableVideo()
            rtcEngine.setVideoEncoderConfiguration(
                VideoEncoderConfiguration(
                    VideoEncoderConfiguration.VD_640x360,
                    VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                    VideoEncoderConfiguration.STANDARD_BITRATE,
                    VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun joinChannel() {
        try {
            //val token = getString(R.string.agora_token)
            // Join a channel with a token.
            //TOKEN = "0060583a41d1d7a40fbbed542383e62f45dIABbaILOTT/XIkPDxZxq4BIpXJUx3HLRv6Shgvu7MetK4cBbkbR857iyIgBmT/kB9+XxYgQAAQCHovBiAgCHovBiAwCHovBiBACHovBi"
            //CHANNEL ="45bae49f-f580-4730-bf85-409a5d166d25"
            /*val options = ChannelMediaOptions()
            options.channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION
            options.clientRoleType = Constants.CLIENT_ROLE_AUDIENCE*/
            rtcEngine.joinChannel(
                RTC_TOKEN,
                CHANNEL_NAME,
                "",
                preference!![PrefKeys.PREF_USER_ID, "0"]!!.toInt()
            )
            running = true
            val date: Calendar = Calendar.getInstance()
            val nextMonthDate = SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(date.time)
            val startTime = DateUtils(nextMonthDate)
            actualStartTime = startTime.getTime()
            runTimer()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startCall() {
        setupLocalVideoView()
        joinChannel()
    }

    private fun endCall() {
        removeLocalVideo()
        removeRemoteVideo()
        leaveChannel()
        doLogout()
    }

    private fun removeLocalVideo() {
        try {
            if (localView != null) {
                binding.localVideoView.removeView(localView)
            }
            localView = null
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            localView = null
        }
    }

    private fun removeRemoteVideo() {
        try {
            if (remoteView != null) {
                binding.remoteVideoView.removeView(remoteView)
            }
            remoteView = null
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            remoteView = null
        }
    }

    private fun leaveChannel() {
        try {
            rtcEngine.leaveChannel()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun onRemoteUserLeft() {
        removeRemoteVideo()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            if (!mEndCall) {
                leaveChannel()
            }
            RtcEngine.destroy()

            if (mIsPeerToPeerMode) {
                MessageUtil.addMessageListBeanList(MessageListBean(mPeerId, mMessageBeanList))
            } else {
                leaveAndReleaseChannel()
            }
            //mChatManager!!.unregisterListener(mClientListener)
            doLogout()
        } catch (e: Exception) {
            e.printStackTrace()
        }
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

    private lateinit var onlineChatView: DialogOnlineChatBinding

    fun openOnlineChatWindow() {
        createPostDialog = BottomSheetDialog(mContext!!)
        /*onlineChatView = requireActivity().layoutInflater.inflate(
            R.layout.dialog_online_chat, null
        )*/
        //onlineChatView!!.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        onlineChatView = DialogOnlineChatBinding.inflate(layoutInflater)
        val view = onlineChatView.root
        createPostDialog!!.setContentView(view)
        createPostDialog!!.behavior.peekHeight =
            Resources.getSystem().displayMetrics.heightPixels
        createPostDialog!!.setCanceledOnTouchOutside(false)
        onlineChatView.relativeLayoutSend.setOnClickListener {
            val msg: String = getText(onlineChatView.editTextMessage)
            if (msg != "") {
                val message = mRtmClient!!.createMessage()
                message.text = msg
                val patientName =
                    preference!![PrefKeys.PREF_FNAME, ""]!!.take(1) + "" + preference!![PrefKeys.PREF_LNAME, ""]!!.take(
                        1
                    )
                val messageBean = MessageBean(patientName, message, true)
                mMessageBeanList.add(messageBean)
                mMessageAdapter!!.notifyItemRangeChanged(mMessageBeanList.size, 1)
                onlineChatView.chatMessageList.scrollToPosition(mMessageBeanList.size - 1)
                if (mIsPeerToPeerMode) {
                    //sendPeerMessage(message)
                } else {
                    sendChannelMessage(message)
                }
            }
            onlineChatView.editTextMessage.setText("")
        }

        onlineChatView.imgGroupVideoCloseFileWebView.setOnClickListener {
            onlineChatView.layoutGroupVideoWebViewFile.visibility = View.GONE
            onlineChatView.layoutGroupVideoChatMessage.visibility = View.VISIBLE
        }

        onlineChatView.imgGroupChatAddFile.setOnClickListener {
            val mimeTypes = arrayOf("image/jpeg", "image/png", "image/gif", "application/pdf")
            var intent: Intent
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            } else {
                intent = Intent(Intent.ACTION_GET_CONTENT)
            }
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                intent.type = if (mimeTypes.size == 1) mimeTypes[0] else "*/*"
                if (mimeTypes.isNotEmpty()) {
                    intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
                }
            } else {
                var mimeTypesStr = ""
                for (mimeType in mimeTypes) {
                    mimeTypesStr += "$mimeType|"
                }
                intent.type = mimeTypesStr.substring(0, mimeTypesStr.length - 1)
            }
            try {
                intent = Intent.createChooser(intent, "Select a file")
                startActivityForResult(intent, PICKFILE_REQUEST_CODE)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        onlineChatView.imgCloseChatScreen.setOnClickListener {
            createPostDialog!!.dismiss()
        }
    }

    /**
     * API CALL: login RTM server
     */
    private fun doLogin() {
        try {
            if (RTM_TOKEN!!.isNotEmpty()) {
                mRtmClient!!.login(
                    RTM_TOKEN!!,
                    preference!![PrefKeys.PREF_USER_ID, "0"]!!,
                    object : ResultCallback<Void?> {
                        override fun onSuccess(responseInfo: Void?) {
                            Utils.rtmLoggedIn = true
                            Log.i(TAG, "login success")
                            runOnUiThread {
                                try {
                                    openOnlineChatWindow()
                                    //displayToast("Successfully loggedIn")
                                    init()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }

                        override fun onFailure(errorInfo: ErrorInfo) {
                            Log.i(TAG, "login failed: " + errorInfo.errorDescription)
                            runOnUiThread {
                                displayToast(getString(R.string.login_failed))
                            }
                        }
                    })
            } else {
                displayToast("RTM token is empty...")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun init() {
        mClientListener = rtmClientListener
        //mChatManager!!.registerListener(mClientListener)
        mIsPeerToPeerMode = false
        val targetName =
            appointment!!.doctor_first_name + " " + appointment!!.doctor_last_name
        mPeerId = targetName
        if (mIsPeerToPeerMode) {

            onlineChatView.txtTherapistChatName.text = mPeerId
            // load history chat records
            val messageListBean = MessageUtil.getExistMessageListBean(mPeerId)
            if (messageListBean != null) {
                mMessageBeanList.addAll(messageListBean.getMessageBeanList()!!)
            }

            // load offline messages since last chat with this peer.
            // Then clear cached offline messages from message pool
            // since they are already consumed.
        } else {
            mChannelName = targetName
            mChannelMemberCount = 1
            onlineChatView.txtTherapistChatName.text = mChannelName
            /*onlineChatView!!.txtTherapistChatName.text = MessageFormat.format(
                "{0}({1})",
                mChannelName,
                mChannelMemberCount
            )*/
            createAndJoinChannel()
        }
        runOnUiThread {
            val layoutManager = LinearLayoutManager(requireActivity())
            layoutManager.orientation = RecyclerView.VERTICAL
            mMessageAdapter = MessageAdapter(
                requireActivity(),
                mMessageBeanList,
                this,
                preference!![PrefKeys.PREF_PHOTO, ""]!!,
                appointment!!.doctor_photo,
                this
            )
            onlineChatView.chatMessageList.layoutManager = layoutManager
            onlineChatView.chatMessageList.adapter = mMessageAdapter
        }
    }

    /**
     * API CALL: create and join channel
     */
    private fun createAndJoinChannel() {
        // step 1: create a channel instance
        mRtmChannel = mRtmClient!!.createChannel(CHANNEL_NAME, myChannelListener)
        if (mRtmChannel == null) {
            showToast(getString(R.string.join_channel_failed))
            popBackStack()
            return
        }
        Log.e("channel", mRtmChannel.toString() + "")

        // step 2: join the channel
        mRtmChannel!!.join(object : ResultCallback<Void?> {
            override fun onSuccess(responseInfo: Void?) {
                Log.i(TAG, "join channel success")
                runOnUiThread {
                    //showToast("join channel success")
                }
                getChannelMemberList()
            }

            override fun onFailure(errorInfo: ErrorInfo) {
                Log.e(TAG, "join channel failed")
                runOnUiThread {
                    showToast(getString(R.string.join_channel_failed))
                    showToast(errorInfo.errorDescription + " - " + errorInfo.errorCode)
                    popBackStack()
                }
            }
        })
    }

    /**
     * API CALL: get channel member list
     */
    private fun getChannelMemberList() {
        mRtmChannel!!.getMembers(object : ResultCallback<List<RtmChannelMember?>> {
            override fun onSuccess(responseInfo: List<RtmChannelMember?>) {
                runOnUiThread {
                    mChannelMemberCount = responseInfo.size
                    refreshChannelTitle()
                }
            }

            override fun onFailure(errorInfo: ErrorInfo) {
                Log.e(
                    TAG,
                    "failed to get channel members, err: " + errorInfo.errorCode
                )
            }
        })
    }

    /**
     * API CALL: send message to a channel
     */
    private fun sendChannelMessage(message: RtmMessage) {
        mRtmChannel!!.sendMessage(message, object : ResultCallback<Void?> {
            override fun onSuccess(aVoid: Void?) {
                runOnUiThread {
                    //showToast("Message sent successfully...")
                }
            }

            override fun onFailure(errorInfo: ErrorInfo) {
                // refer to RtmStatusCode.ChannelMessageState for the message state
                val errorCode = errorInfo.errorCode
                runOnUiThread {
                    when (errorCode) {
                        RtmStatusCode.ChannelMessageError.CHANNEL_MESSAGE_ERR_TIMEOUT,
                        RtmStatusCode.ChannelMessageError.CHANNEL_MESSAGE_ERR_FAILURE ->
                            showToast(getString(R.string.send_msg_failed))

                        else -> showToast(errorInfo.errorDescription + " - " + errorInfo.errorCode)
                    }
                }
            }
        })
    }

    /**
     * API CALL: leave and release channel
     */
    private fun leaveAndReleaseChannel() {
        if (mRtmChannel != null) {
            mRtmChannel!!.leave(null)
            mRtmChannel!!.release()
            mRtmChannel = null
        }
    }

    /**
     * API CALLBACK: rtm event listener
     */
    private val rtmClientListener = object : RtmClientListener {
        override fun onConnectionStateChanged(state: Int, reason: Int) {
            runOnUiThread {
                when (state) {
                    RtmStatusCode.ConnectionState.CONNECTION_STATE_RECONNECTING -> showToast(
                        getString(R.string.reconnecting)
                    )

                    RtmStatusCode.ConnectionState.CONNECTION_STATE_ABORTED -> {
                        showToast(getString(R.string.account_offline))
                        requireActivity().setResult(MessageUtil.ACTIVITY_RESULT_CONN_ABORTED)
                        popBackStack()
                    }
                }
            }
        }

        override fun onMessageReceived(message: RtmMessage, peerId: String) {
            runOnUiThread {
                //if (peerId == account) {
                val messageBean = MessageBean(peerId, message, false)
                messageBean.setBackground(getMessageColor(mPeerId))
                mMessageBeanList.add(messageBean)
                mMessageAdapter!!.notifyItemRangeChanged(mMessageBeanList.size, 1)
                onlineChatView.chatMessageList.scrollToPosition(mMessageBeanList.size - 1)
                /*} else {
                    MessageUtil.addMessageBean(peerId, message)
                }*/
            }
        }

        override fun onImageMessageReceivedFromPeer(p0: RtmImageMessage?, p1: String?) {

        }

        override fun onFileMessageReceivedFromPeer(p0: RtmFileMessage?, p1: String?) {
        }

        override fun onMediaUploadingProgress(p0: RtmMediaOperationProgress?, p1: Long) {
        }

        override fun onMediaDownloadingProgress(p0: RtmMediaOperationProgress?, p1: Long) {
        }

        override fun onTokenExpired() {}
        override fun onPeersOnlineStatusChanged(map: Map<String, Int>) {}
    }

    /**
     * API CALLBACK: rtm channel event listener
     */
    private val myChannelListener = object : RtmChannelListener {
        override fun onMemberCountUpdated(i: Int) {}
        override fun onAttributesUpdated(list: List<RtmChannelAttribute>) {}
        override fun onMessageReceived(message: RtmMessage, fromMember: RtmChannelMember) {
            runOnUiThread {
                val account = fromMember.userId
                Log.i(TAG, "onMessageReceived account = $account msg = $message")
                val messageBean = MessageBean(mPeerId, message, false)
                messageBean.setBackground(getMessageColor(mPeerId))
                mMessageBeanList.add(messageBean)
                mMessageAdapter!!.notifyItemRangeChanged(mMessageBeanList.size, 1)
                onlineChatView.chatMessageList.scrollToPosition(mMessageBeanList.size - 1)
            }
        }

        override fun onImageMessageReceived(p0: RtmImageMessage?, p1: RtmChannelMember?) {

        }

        override fun onFileMessageReceived(p0: RtmFileMessage?, p1: RtmChannelMember?) {
        }

        override fun onMemberJoined(member: RtmChannelMember) {
            runOnUiThread {
                mChannelMemberCount++
                refreshChannelTitle()
            }
        }

        override fun onMemberLeft(member: RtmChannelMember) {
            runOnUiThread {
                mChannelMemberCount--
                refreshChannelTitle()
            }
        }
    }

    private fun getMessageColor(account: String): Int {
        for (i in mMessageBeanList.indices) {
            if (account == mMessageBeanList[i].getAccount()) {
                return mMessageBeanList[i].getBackground()
            }
        }
        return MessageUtil.COLOR_ARRAY[MessageUtil.RANDOM.nextInt(MessageUtil.COLOR_ARRAY.size)]
    }

    private fun refreshChannelTitle() {
        val titleFormat = getString(R.string.channel_title)
        val title = String.format(titleFormat, mChannelName, mChannelMemberCount)
        onlineChatView!!.txtTherapistChatName.text = mChannelName
    }

    private fun showToast(text: String) {
        runOnUiThread {
            Toast.makeText(
                requireActivity(),
                text,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /**
     * API CALL: logout from RTM server
     */
    private fun doLogout() {
        try {
            if (mRtmClient != null) {
                mRtmClient!!.logout(null)
                MessageUtil.cleanMessageListBeanList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onPause() {
        super.onPause()
        wasRunning = running;
        running = false;
    }

    override fun onResume() {
        super.onResume()
        if (wasRunning) {
            running = true;
        }
    }

    // Sets the NUmber of seconds on the timer.
    // The runTimer() method uses a Handler
    // to increment the seconds and
    // update the text view.
    private fun runTimer() {

        // Creates a new Handler
        val handler = Handler()

        // Call the post() method,
        // passing in a new Runnable.
        // The post() method processes
        // code without a delay,
        // so the code in the Runnable
        // will run almost immediately.
        handler.post(object : Runnable {
            override fun run() {
                val hours: Int = seconds / 3600
                val minutes: Int = seconds % 3600 / 60
                val secs: Int = seconds % 60

                // Format the seconds into hours, minutes,
                // and seconds.
                val time: String = String
                    .format(
                        Locale.getDefault(),
                        "%02d:%02d:%02d", hours,
                        minutes, secs
                    )

                // Set the text view text.
                if (binding.txtUserVideoTime != null) {
                    binding.txtUserVideoTime.text = time
                }
                if (binding.txtUserAudioTime != null) {
                    binding.txtUserAudioTime.text = time
                }

                // If running is true, increment the
                // seconds variable.
                if (running) {
                    seconds++
                }

                // Post the code again
                // with a delay of 1 second.
                handler.postDelayed(this, 1000)
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICKFILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                data.data.let { returnUri ->
                    requireActivity().contentResolver.query(returnUri!!, null, null, null, null)
                }?.use { cursor ->
                    /*
                 * Get the column indexes of the data in the Cursor,
                 * move to the first row in the Cursor, get the data,
                 * and display it.
                 */
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    cursor.moveToFirst()
                    val selectedFile = data.data
                    convertToString(
                        selectedFile!!,
                        cursor.getString(nameIndex),
                        cursor.getLong(sizeIndex)
                    )
                }
            }
        }
    }

    private fun convertToString(uri: Uri, fileName: String, fileSize: Long) {
        if (fileSize <= 10000000) {
            val uriString = uri.toString()
            val fileType = getMimeType(uri)
            Log.d("data", "onActivityResult: uri$uriString")
            //            myFile = new File(uriString);
            //            ret = myFile.getAbsolutePath();
            try {
                val inputStream: InputStream =
                    requireActivity().contentResolver.openInputStream(uri)!!
                val bytes = getBytes(inputStream)
                val document = Base64.encodeToString(bytes, Base64.NO_WRAP)
                /*val document =
                    "JVBERi0xLjMKMyAwIG9iago8PC9UeXBlIC9QYWdlCi9QYXJlbnQgMSAwIFIKL1Jlc291cmNlcyAyIDAgUgovQ29udGVudHMgNCAwIFI+PgplbmRvYmoKNCAwIG9iago8PC9GaWx0ZXIgL0ZsYXRlRGVjb2RlIC9MZW5ndGggNjQ3Pj4Kc3RyZWFtCnicnZbBctowFEX3fMVbtjNBlmRZsndtSNLSaVMK7qJLBz8cN8YmspxM/r6ygdrgpDMVCyHEWOceJD3B4cuEkkDB8+QyBu+GAQsIpRBv4DreD3Fgqh9itkuBDlqdTR5tl1FuWxGJQ6szsI/7jLAIpJREMYhTePerajQs4zks8bHB2sAmLzCFulmvsa43TVG8kPcQ/7Ys+DGhkO1D+MD4WS5xMvRqLh4SPwBplRgHbkMICVPOiQxBI6ygzc0DdtKe5KbWK+xyLzHLa6MTk1cl3DbbO9R9TB4JEjInEI8UkWJIWnxfxStv6V173Pcok1INQJ1QEHEihItQoBQJaIe5TbY4MnCZ+WAwmPqTXePkCVZ5md2PssuIKOmUPbAR/Q5wlRiEagM3eWEZYwsHxtGih1AxpXzKKfdHDsIeiMjJweeEBfsNZc/ADaI9B2vMnzAdWzhQjhY9htFRei6IYE7paUSE2m/S5GWLpYFvVfrKLnIgHJP3iHlpUJdo4DIpH+wyX8BMY5obqDRc4Z3tzBKdggfLxqbpPlzAz8V85EsZUb6Lrwht2YpOfJe4QY3lGt+qAS60g/sAN1t8nH39HMeXIfx9nXsJFRHqdJKEFG2dakGxTso6WXdVbWUS09QjIxfO0agHzartrkBjS/1qUOpHToEkfujkJFhbufalenizPOfmfuzkwDk69aAr3CXadPvCFqNFVZvBr/d/d5Rg9kunO8qPZFsmusXEAnf3Vfnm5nShHLQHGMrYlPtUiJCPriY/7N5cPJTfFo0WcL1N8gLm45LoMvsxfz+9NnmK9cOHvEzzZGdXjWTVE8nLs7V79a9Q2I/8A6lkVwMCEgWH6pEhMI/1hD/eriz+CmVuZHN0cmVhbQplbmRvYmoKMSAwIG9iago8PC9UeXBlIC9QYWdlcwovS2lkcyBbMyAwIFIgXQovQ291bnQgMQovTWVkaWFCb3ggWzAgMCA1OTUuMjggODQxLjg5XQo+PgplbmRvYmoKNSAwIG9iago8PC9UeXBlIC9Gb250Ci9CYXNlRm9udCAvSGVsdmV0aWNhLUJvbGQKL1N1YnR5cGUgL1R5cGUxCi9FbmNvZGluZyAvV2luQW5zaUVuY29kaW5nCj4+CmVuZG9iago2IDAgb2JqCjw8L1R5cGUgL0ZvbnQKL0Jhc2VGb250IC9IZWx2ZXRpY2EtT2JsaXF1ZQovU3VidHlwZSAvVHlwZTEKL0VuY29kaW5nIC9XaW5BbnNpRW5jb2RpbmcKPj4KZW5kb2JqCjcgMCBvYmoKPDwvVHlwZSAvRm9udAovQmFzZUZvbnQgL1RpbWVzLVJvbWFuCi9TdWJ0eXBlIC9UeXBlMQovRW5jb2RpbmcgL1dpbkFuc2lFbmNvZGluZwo+PgplbmRvYmoKOCAwIG9iago8PC9UeXBlIC9Gb250Ci9CYXNlRm9udCAvSGVsdmV0aWNhCi9TdWJ0eXBlIC9UeXBlMQovRW5jb2RpbmcgL1dpbkFuc2lFbmNvZGluZwo+PgplbmRvYmoKMiAwIG9iago8PAovUHJvY1NldCBbL1BERiAvVGV4dCAvSW1hZ2VCIC9JbWFnZUMgL0ltYWdlSV0KL0ZvbnQgPDwKL0YxIDUgMCBSCi9GMiA2IDAgUgovRjMgNyAwIFIKL0Y0IDggMCBSCj4+Ci9YT2JqZWN0IDw8Cj4+Cj4+CmVuZG9iago5IDAgb2JqCjw8Ci9Qcm9kdWNlciAoRlBERiAxLjcpCi9DcmVhdGlvbkRhdGUgKEQ6MjAyMzAyMDQwMDE0MzkpCj4+CmVuZG9iagoxMCAwIG9iago8PAovVHlwZSAvQ2F0YWxvZwovUGFnZXMgMSAwIFIKPj4KZW5kb2JqCnhyZWYKMCAxMQowMDAwMDAwMDAwIDY1NTM1IGYgCjAwMDAwMDA4MDQgMDAwMDAgbiAKMDAwMDAwMTI5MCAwMDAwMCBuIAowMDAwMDAwMDA5IDAwMDAwIG4gCjAwMDAwMDAwODcgMDAwMDAgbiAKMDAwMDAwMDg5MSAwMDAwMCBuIAowMDAwMDAwOTkyIDAwMDAwIG4gCjAwMDAwMDEwOTYgMDAwMDAgbiAKMDAwMDAwMTE5NCAwMDAwMCBuIAowMDAwMDAxNDI0IDAwMDAwIG4gCjAwMDAwMDE0OTkgMDAwMDAgbiAKdHJhaWxlcgo8PAovU2l6ZSAxMQovUm9vdCAxMCAwIFIKL0luZm8gOSAwIFIKPj4Kc3RhcnR4cmVmCjE1NDkKJSVFT0YK"*/
                sendClientFile(
                    appointment!!.appointment!!.appointment_id,
                    fileName.split(".")[0],
                    fileType.split("/")[1],
                    "data:$fileType;base64,$document"
                ) { response ->
                    onlineChatView.layoutProgress.visibility = View.GONE
                    Log.i("Response", response.toString())
                    val jsonObj = JSONObject(response.toString())
                    val message = mRtmClient!!.createMessage()
                    val msg = "$fileName," + jsonObj.getString("file")
                    val list: MutableList<String> = ArrayList()
                    list.add(fileName)
                    list.add(jsonObj.getString("file"))

                    message.rawMessage = msg.toByteArray()
                    sendChannelMessage(message)
                    val patientName =
                        preference!![PrefKeys.PREF_FNAME, ""]!!.take(1) + "" + preference!![PrefKeys.PREF_LNAME, ""]!!.take(
                            1
                        )
                    message.rawMessage = msg.toByteArray()
                    val messageBean = MessageBean(patientName, message, true)
                    mMessageBeanList.add(messageBean)
                    mMessageAdapter!!.notifyItemRangeChanged(mMessageBeanList.size, 1)
                    onlineChatView.chatMessageList.scrollToPosition(mMessageBeanList.size - 1)
                }
            } catch (e: Exception) {
                // TODO: handle exception
                e.printStackTrace()
                Log.d("error", "onActivityResult: $e")
            }
        } else {
            displayToast("File size should not exceed more than 10 MB")
        }
    }

    private fun getMimeType(uri: Uri): String {
        var mimeType = ""
        try {
            mimeType = if (uri.scheme.equals(ContentResolver.SCHEME_CONTENT)) {
                val cr = requireActivity().contentResolver
                cr.getType(uri)!!
            } else {
                val fileExt: String = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExt.lowercase())!!
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return mimeType
    }

    @Throws(IOException::class)
    private fun getBytes(inputStream: InputStream): ByteArray? {
        val byteBuffer = ByteArrayOutputStream()
        val bufferSize = 1024
        val buffer = ByteArray(bufferSize)
        var len = 0
        while (inputStream.read(buffer).also { len = it } != -1) {
            byteBuffer.write(buffer, 0, len)
        }
        return byteBuffer.toByteArray()
    }

    private fun sendClientFile(
        apptId: Int,
        fileName: String,
        fileExt: String,
        file: String,
        myCallback: (result: String?) -> Unit
    ) {
        onlineChatView.layoutProgress.visibility = View.VISIBLE
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .sendFile(
                        "PI0071",
                        FileDetails(
                            preference!![PrefKeys.PREF_PATIENT_ID, ""]!!.toInt(),
                            apptId.toString(),
                            fileName,
                            fileExt,
                            file
                        ),
                        getAccessToken()
                    )
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ result ->
                        try {
                            onlineChatView.layoutProgress.visibility = View.GONE
                            var responseBody = result.string()
                            Log.d("Response Body", responseBody)
                            val respBody = responseBody.split("|")
                            val status = respBody[1]
                            responseBody = respBody[0]
                            if (status == "401") {
                                clearCache()
                            } else {
                                myCallback.invoke(responseBody)
                            }
                        } catch (e: Exception) {
                            onlineChatView.layoutProgress.visibility = View.GONE
                            e.printStackTrace()
                            displayToast("Something went wrong.. Please try after sometime")
                        }
                    }, { error ->
                        onlineChatView.layoutProgress.visibility = View.GONE
                        //displayToast("Error ${error.localizedMessage}")
                        if ((error as HttpException).code() == 401) {
                            clearCache()
                        } else {
                            displayAfterLoginErrorMsg(error)
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
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
        fun newInstance(param1: GetAppointment, param2: String, param3: String, param4: String) =
            VideoCallFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                    putString(ARG_PARAM3, param3)
                    putString(ARG_PARAM4, param4)
                }
            }

        const val TAG = "Screen_Video_Call"
        private const val PICKFILE_REQUEST_CODE = 9
    }

    override fun onItemClick(message: MessageBean?) {
        onlineChatView.layoutGroupVideoChatMessage.visibility = View.GONE
        onlineChatView.layoutGroupVideoWebViewFile.visibility = View.VISIBLE
        val browser = onlineChatView.webViewGroupVideoFile.settings
        browser.javaScriptEnabled = true
        browser.builtInZoomControls = true
        browser.pluginState = WebSettings.PluginState.ON
        browser.loadWithOverviewMode = true
        browser.useWideViewPort = true
        onlineChatView.webViewGroupVideoFile.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                view?.loadUrl(url!!)
                return true
            }
        }
        onlineChatView.webViewGroupVideoFile.webViewClient = object : WebViewClient() {
            private var mProgressDialog: ProgressDialog? = null
            override fun onPageFinished(view: WebView, url: String?) {
                view.settings.loadsImagesAutomatically = true
                onlineChatView.webViewGroupVideoFile.visibility = View.VISIBLE
                //progressView.setVisibility(View.VISIBLE);
                dismissProgressDialog()
                view.loadUrl("javascript:clickFunction(){  })()")
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                showProgressDialog()
            }

            override fun onReceivedError(
                view: WebView?,
                errorCode: Int,
                description: String?,
                failingUrl: String?
            ) {
                dismissProgressDialog()
                Log.e("error", description!!)
            }

            private fun showProgressDialog() {
                dismissProgressDialog()
                mProgressDialog = ProgressDialog.show(requireActivity(), "", "Loading...")
            }

            private fun dismissProgressDialog() {
                if (mProgressDialog != null && mProgressDialog!!.isShowing) {
                    mProgressDialog!!.dismiss()
                    mProgressDialog = null
                }
            }
        }

        val mMessage = String(message!!.getMessage()!!.rawMessage)
        val mMsgArr = mMessage.split(",")
        if (mMsgArr[1].contains("pdf")) {
            onlineChatView.webViewGroupVideoFile.loadUrl(
                "http://docs.google.com/gview?embedded=true&url=" + BaseActivity.baseURL.dropLast(
                    5
                ) + mMsgArr[1]
            )
        } else {
            onlineChatView.webViewGroupVideoFile.loadUrl(BaseActivity.baseURL.dropLast(5) + mMsgArr[1])
        }
    }

    override fun onBottomReached(position: Int) {

    }
}