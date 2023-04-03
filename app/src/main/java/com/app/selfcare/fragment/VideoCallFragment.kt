package com.app.selfcare.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
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
import com.app.selfcare.controller.OnMessageClickListener
import com.app.selfcare.data.GetAppointment
import com.app.selfcare.data.MessageBean
import com.app.selfcare.data.MessageListBean
import com.app.selfcare.databinding.DialogOnlineChatBinding
import com.app.selfcare.databinding.FragmentActivityCarePlanBinding
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
class VideoCallFragment : BaseFragment(), OnMessageClickListener {
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

    private var mChatManager: ChatManager? = null
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

        if (appointment!!.appointment.type_of_visit == "Video") {
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
                rtcEngine.disableVideo()
                R.mipmap.ic_video_disabled
            } else {
                rtcEngine.enableVideo()
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
                OnlineChatFragment.newInstance(appointment!!, targetName),
                R.id.layout_home, OnlineChatFragment.TAG
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
            val date: Calendar = Calendar.getInstance()
            val nextMonthDate = SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(date.time)
            val endTime = DateUtils(nextMonthDate)
            actualEndTime = endTime.getTime()
            duration = binding.txtUserAudioTime.text.toString()
            endCall()
            Utils.rtmLoggedIn = false
            sendApptStatus(
                appointment!!.appointment.appointment_id.toString(),
                actualStartTime!!, actualEndTime!!, duration!!
            ) {
                clearPreviousFragmentStack()
                replaceFragmentNoBackStack(
                    TherapistFeedbackFragment.newInstance(appointment!!.appointment.appointment_id.toString()),
                    R.id.layout_home,
                    TherapistFeedbackFragment.TAG
                )
            }
            /*callAppointmentApi(
                appointment!!.appointment.appointment_id.toString(),
                appointment!!.appointment.booking_date,
                appointment!!.appointment.type_of_visit,
                Utils.APPOINTMENT_COMPLETED
            ) { response ->
                if (response == "Success") {

                }
            }*/
        }
        builder.setNegativeButton("No") { dialog, which ->
            dialog.dismiss()
        }
        builder.setCancelable(false)
        builder.show()
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

            mChatManager = SelfCareApplication.instance.getChatManager()
            mRtmClient = mChatManager!!.getRtmClient()
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
            if (binding.remoteVideoView.childCount > 1) {
                return
            }
            remoteView = RtcEngine.CreateRendererView(requireActivity())
            binding.remoteVideoView.addView(remoteView)
            rtcEngine.setupRemoteVideo(VideoCanvas(remoteView, VideoCanvas.RENDER_MODE_FIT, uid))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupVideoConfig() {
        try {
            if (appointment!!.appointment.type_of_visit == "Video") {
                rtcEngine.enableVideo()
            } else {
                rtcEngine.disableVideo()
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
            rtcEngine.joinChannel(RTC_TOKEN, CHANNEL_NAME, "", 0)
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
            mChatManager!!.unregisterListener(mClientListener)
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
        onlineChatView!!.relativeLayoutSend.setOnClickListener {
            val msg: String = getText(onlineChatView!!.editTextMessage)
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
                onlineChatView!!.chatMessageList.scrollToPosition(mMessageBeanList.size - 1)
                if (mIsPeerToPeerMode) {
                    sendPeerMessage(message)
                } else {
                    sendChannelMessage(message)
                }
            }
            onlineChatView!!.editTextMessage.setText("")
        }

        onlineChatView!!.imgCloseChatScreen.setOnClickListener {
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
                    preference!![PrefKeys.PREF_EMAIL, ""],
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
        mChatManager!!.registerListener(mClientListener)
        mIsPeerToPeerMode = false
        val targetName =
            appointment!!.doctor_first_name + " " + appointment!!.doctor_last_name
        if (mIsPeerToPeerMode) {
            mPeerId = targetName
            onlineChatView!!.txtTherapistChatName.text = mPeerId

            // load history chat records
            val messageListBean = MessageUtil.getExistMessageListBean(mPeerId)
            if (messageListBean != null) {
                mMessageBeanList.addAll(messageListBean.getMessageBeanList()!!)
            }

            // load offline messages since last chat with this peer.
            // Then clear cached offline messages from message pool
            // since they are already consumed.
            val peerName =
                appointment!!.doctor_first_name.take(1) + "" + appointment!!.doctor_last_name.take(1)
            val offlineMessageBean = MessageListBean(peerName, mChatManager!!)
            mMessageBeanList.addAll(offlineMessageBean.getMessageBeanList()!!)
            mChatManager!!.removeAllOfflineMessages(mPeerId)
        } else {
            mChannelName = targetName
            mChannelMemberCount = 1
            onlineChatView!!.txtTherapistChatName.text = mChannelName
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
            mMessageAdapter = MessageAdapter(requireActivity(), mMessageBeanList, this)
            onlineChatView!!.chatMessageList.layoutManager = layoutManager
            onlineChatView!!.chatMessageList.adapter = mMessageAdapter
        }
    }

    /**
     * API CALL: send message to peer
     */
    private fun sendPeerMessage(message: RtmMessage) {
        mRtmClient!!.sendMessageToPeer(
            mPeerId,
            message,
            mChatManager!!.getSendMessageOptions(),
            object : ResultCallback<Void?> {
                override fun onSuccess(aVoid: Void?) {
                    // do nothing
                    runOnUiThread {
                        //showToast("Message sent successfully...")
                    }
                }

                override fun onFailure(errorInfo: ErrorInfo) {
                    // refer to RtmStatusCode.PeerMessageState for the message state
                    val errorCode = errorInfo.errorCode
                    runOnUiThread {
                        when (errorCode) {
                            RtmStatusCode.PeerMessageError.PEER_MESSAGE_ERR_TIMEOUT, RtmStatusCode.PeerMessageError.PEER_MESSAGE_ERR_FAILURE ->
                                showToast(getString(R.string.send_msg_failed))
                            RtmStatusCode.PeerMessageError.PEER_MESSAGE_ERR_PEER_UNREACHABLE ->
                                showToast(getString(R.string.peer_offline))
                            RtmStatusCode.PeerMessageError.PEER_MESSAGE_ERR_CACHED_BY_SERVER ->
                                showToast(getString(R.string.message_cached))
                            else -> {
                                showToast(errorInfo.errorDescription + " - " + errorInfo.errorCode)
                            }
                        }
                    }
                }
            })
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
                Log.i(OnlineChatFragment.TAG, "join channel success")
                runOnUiThread {
                    //showToast("join channel success")
                }
                getChannelMemberList()
            }

            override fun onFailure(errorInfo: ErrorInfo) {
                Log.e(OnlineChatFragment.TAG, "join channel failed")
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
                    OnlineChatFragment.TAG,
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
                if (peerId == mPeerId) {
                    val messageBean = MessageBean(peerId, message, false)
                    messageBean.setBackground(getMessageColor(peerId))
                    mMessageBeanList.add(messageBean)
                    mMessageAdapter!!.notifyItemRangeChanged(mMessageBeanList.size, 1)
                    onlineChatView!!.chatMessageList.scrollToPosition(mMessageBeanList.size - 1)
                } else {
                    MessageUtil.addMessageBean(peerId, message)
                }
            }
        }

        override fun onTokenExpired() {}
        override fun onTokenPrivilegeWillExpire() {}
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
                val targetName =
                    appointment!!.doctor_first_name.take(1) + "" + appointment!!.doctor_last_name.take(
                        1
                    )
                val account = fromMember.userId
                Log.i(OnlineChatFragment.TAG, "onMessageReceived account = $account msg = $message")
                val messageBean = MessageBean(targetName, message, false)
                messageBean.setBackground(getMessageColor(account))
                mMessageBeanList.add(messageBean)
                mMessageAdapter!!.notifyItemRangeChanged(mMessageBeanList.size, 1)
                onlineChatView!!.chatMessageList.scrollToPosition(mMessageBeanList.size - 1)
            }
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
    }

    override fun onItemClick(message: MessageBean?) {

    }
}