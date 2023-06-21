package ensemblecare.csardent.com.fragment

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
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ensemblecare.csardent.com.BaseActivity
import ensemblecare.csardent.com.adapters.MessageAdapter
import ensemblecare.csardent.com.controller.OnBottomReachedListener
import ensemblecare.csardent.com.controller.OnMessageClickListener
import ensemblecare.csardent.com.data.GetAppointment
import ensemblecare.csardent.com.data.MessageBean
import ensemblecare.csardent.com.data.MessageListBean
import ensemblecare.csardent.com.databinding.DialogTrainingSessionOnlineChatBinding
import ensemblecare.csardent.com.databinding.FragmentTrainingSessionBinding
import ensemblecare.csardent.com.preference.PrefKeys
import ensemblecare.csardent.com.preference.PreferenceHelper.get
import ensemblecare.csardent.com.utils.DateUtils
import ensemblecare.csardent.com.utils.MessageUtil
import ensemblecare.csardent.com.utils.Utils
import com.google.android.material.bottomsheet.BottomSheetDialog
import ensemblecare.csardent.com.BuildConfig
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.video.VideoCanvas
import io.agora.rtc2.video.VideoEncoderConfiguration
import ensemblecare.csardent.com.R
import io.agora.rtm.*
import org.json.JSONObject
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
 * Use the [TrainingSessionFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TrainingSessionFragment : BaseFragment(), OnMessageClickListener, OnBottomReachedListener {
    // TODO: Rename and change types of parameters
    private var appointment: GetAppointment? = null
    private var RTC_TOKEN: String? = null
    private var RTM_TOKEN: String? = null
    private var CHANNEL_NAME: String? = null
    private lateinit var binding: FragmentTrainingSessionBinding
    private var createPostDialog: BottomSheetDialog? = null
    private var actualStartTime: String? = null
    private var actualEndTime: String? = null
    private var duration: String? = null
    private val PERMISSION_REQUEST_ID = 7
    private val ALL_REQUESTED_PERMISSIONS = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CAMERA,
        Manifest.permission.READ_PHONE_STATE
    )
    private var mEndCall = false
    private var mMuted = false
    private var remoteView: SurfaceView? = null
    private lateinit var rtcEngine: RtcEngine

    private var mRtmClient: RtmClient? = null
    private var mRtmChannel: RtmChannel? = null
    private var mIsPeerToPeerMode = false
    private var mPeerId = ""
    private var mChannelName = ""
    private var mChannelMemberCount = 1
    private val mMessageBeanList: ArrayList<MessageBean> = ArrayList()
    private var mMessageAdapter: MessageAdapter? = null
    private var seconds = 0
    private var running = false
    private var wasRunning = false
    private lateinit var onlineChatView: DialogTrainingSessionOnlineChatBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            appointment = it.getParcelable(ARG_PARAM1)
            RTC_TOKEN = it.getString(ARG_PARAM2)
            RTM_TOKEN = it.getString(ARG_PARAM3)
            CHANNEL_NAME = it.getString(ARG_PARAM4)
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_training_session
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTrainingSessionBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
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

        binding.txtTrainingSessionUserName.text =
            appointment!!.host!!.first_name + " " + appointment!!.host!!.last_name

        binding.btnCall.setOnClickListener {
            if (mEndCall) {
                startCall()
                mEndCall = false
                binding.btnCall.setImageResource(R.drawable.btn_endcall)
                binding.btnMute.visibility = View.VISIBLE
            } else {
                endVideoCall()
            }
        }

        binding.btnChat.setOnClickListener {
            if (createPostDialog != null) {
                createPostDialog!!.show()
            } else {
                displayMsg("Alert", "Cannot open chat screen.")
            }
        }

        binding.btnMute.setOnClickListener {
            audioFunc()
        }
    }

    private fun audioFunc(){
        mMuted = !mMuted
        rtcEngine.muteLocalAudioStream(mMuted)
        val res: Int = if (mMuted) {
            R.drawable.btn_mute
        } else {
            R.drawable.btn_unmute
        }
        binding.btnMute.setImageResource(res)
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

    private fun initAndJoinChannel() {
        // This is our usual steps for joining
        // a channel and starting a call.
        initRtcEngine()
        setupVideoConfig()
        joinChannel()
    }

    // Initialize the RtcEngine object.
    private fun initRtcEngine() {
        try {
            rtcEngine = RtcEngine.create(requireActivity(), BuildConfig.appId, mRtcEventHandler)
            audioFunc()
            mRtmClient = RtmClient.createInstance(
                requireActivity(),
                BuildConfig.appId,
                object : RtmClientListener {
                    override fun onConnectionStateChanged(state: Int, reason: Int) {
                        runOnUiThread {
                            when (state) {
                                RtmStatusCode.ConnectionState.CONNECTION_STATE_RECONNECTING -> displayToast(
                                    getString(R.string.reconnecting)
                                )
                                RtmStatusCode.ConnectionState.CONNECTION_STATE_ABORTED -> {
                                    displayToast(getString(R.string.account_offline))
                                    requireActivity().setResult(MessageUtil.ACTIVITY_RESULT_CONN_ABORTED)
                                    popBackStack()
                                }
                            }
                        }
                    }

                    override fun onMessageReceived(message: RtmMessage?, peerId: String?) {
                        runOnUiThread {
                            if (peerId == mPeerId) {
                                val messageBean = MessageBean(peerId, message, false)
                                messageBean.setBackground(getMessageColor(peerId))
                                mMessageBeanList.add(messageBean)
                                mMessageAdapter!!.notifyItemRangeChanged(mMessageBeanList.size, 1)
                                onlineChatView.textTrainingSessionMessageList.scrollToPosition(
                                    mMessageBeanList.size - 1
                                )
                            } else {
                                MessageUtil.addMessageBean(peerId!!, message)
                            }
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
                removeRemoteVideo()
            }
        }
    }

    private fun startCall() {
        joinChannel()
    }

    private fun endVideoCall() {
        val builder = AlertDialog.Builder(mActivity!!)
        builder.setTitle("Confirmation")
        builder.setMessage("Do you wish to end the session?")
        builder.setPositiveButton("Yes") { dialog, which ->
            val date: Calendar = Calendar.getInstance()
            val nextMonthDate = SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(date.time)
            val endTime = DateUtils(nextMonthDate)
            actualEndTime = endTime.getTime()
            duration = binding.txtTrainingSessionTime.text.toString()
            endCall()
            Utils.rtmLoggedIn = false
            setBottomNavigation(null)
            setLayoutBottomNavigation(null)
            replaceFragmentNoBackStack(
                BottomNavigationFragment(),
                R.id.layout_home,
                BottomNavigationFragment.TAG
            )
            /*sendApptStatus(
                appointment!!.id.toString(),
                actualStartTime!!, actualEndTime!!, duration!!
            ) {
                clearPreviousFragmentStack()
                replaceFragmentNoBackStack(
                    TherapistFeedbackFragment.newInstance(appointment!!.id.toString()),
                    R.id.layout_home,
                    TherapistFeedbackFragment.TAG
                )
            }*/
        }
        builder.setNegativeButton("No") { dialog, which ->
            dialog.dismiss()
        }
        builder.setCancelable(false)
        builder.show()
    }

    private fun setupRemoteVideoView(uid: Int) {
        try {
            if (binding.remoteTrainingSessionVideoView.childCount > 1) {
                return
            }
            remoteView = RtcEngine.CreateRendererView(requireActivity())
            binding.remoteTrainingSessionVideoView.addView(remoteView)
            rtcEngine.setupRemoteVideo(VideoCanvas(remoteView, VideoCanvas.RENDER_MODE_FIT, uid))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupVideoConfig() {
        try {
            rtcEngine.enableVideo()
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

    private fun removeRemoteVideo() {
        try {
            if (remoteView != null) {
                binding.remoteTrainingSessionVideoView.removeView(remoteView)
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

    private fun endCall() {
        removeRemoteVideo()
        leaveChannel()
        doLogout()
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
            doLogout()
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

    private fun runTimer() {
        val handler = Handler()
        handler.post(object : Runnable {
            override fun run() {
                val hours: Int = seconds / 3600
                val minutes: Int = seconds % 3600 / 60
                val secs: Int = seconds % 60
                val time: String = String
                    .format(
                        Locale.getDefault(),
                        "%02d:%02d:%02d", hours,
                        minutes, secs
                    )

                if (binding.txtTrainingSessionTime != null) {
                    binding.txtTrainingSessionTime.text = time
                }
                if (running) {
                    seconds++
                }
                handler.postDelayed(this, 1000)
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun openOnlineChatWindow() {
        createPostDialog = BottomSheetDialog(mContext!!)
        onlineChatView = DialogTrainingSessionOnlineChatBinding.inflate(layoutInflater)
        val view = onlineChatView.root
        createPostDialog!!.setContentView(view)
        createPostDialog!!.behavior.peekHeight =
            Resources.getSystem().displayMetrics.heightPixels
        createPostDialog!!.setCanceledOnTouchOutside(false)
        onlineChatView.txtTrainerChatName.text =
            appointment!!.host!!.first_name + " " + appointment!!.host!!.last_name
        onlineChatView.layoutTrainingSessionSend.setOnClickListener {
            val msg: String = getText(onlineChatView.editTextTrainingSessionMessage)
            if (msg.isNotEmpty()) {
                val message = mRtmClient!!.createMessage()
                message.text = msg
                val patientName =
                    preference!![PrefKeys.PREF_FNAME, ""]!!.take(1) + "" + preference!![PrefKeys.PREF_LNAME, ""]!!.take(
                        1
                    )
                val messageBean = MessageBean(patientName, message, true)
                mMessageBeanList.add(messageBean)
                mMessageAdapter!!.notifyItemRangeChanged(mMessageBeanList.size, 1)
                onlineChatView.textTrainingSessionMessageList.scrollToPosition(mMessageBeanList.size - 1)
                sendChannelMessage(message)
            }
            onlineChatView.editTextTrainingSessionMessage.setText("")
        }

        onlineChatView.imgTrainingSessionCloseFileWebView.setOnClickListener {
            onlineChatView.layoutTrainingSessionWebViewFile.visibility = View.GONE
            onlineChatView.layoutTrainingSessionChatMessage.visibility = View.VISIBLE
        }

        onlineChatView.imgTrainingSessionAddFile.setOnClickListener {
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

        onlineChatView.imgTrainingSessionCloseChatScreen.setOnClickListener {
            createPostDialog!!.dismiss()
        }
    }

    private fun doLogin() {
        try {
            if (RTM_TOKEN!!.isNotEmpty()) {
                mRtmClient!!.login(
                    RTM_TOKEN!!,
                    preference!![PrefKeys.PREF_USER_ID, "0"]!!,
                    object : ResultCallback<Void?> {
                        override fun onSuccess(responseInfo: Void?) {
                            Utils.rtmLoggedIn = true
                            Log.i(VideoCallFragment.TAG, "login success")
                            runOnUiThread {
                                try {
                                    openOnlineChatWindow()
                                    init()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }

                        override fun onFailure(errorInfo: ErrorInfo) {
                            val text: CharSequence =
                                "User: failed to log in to Signaling!$errorInfo"
                            Log.i(
                                VideoCallFragment.TAG,
                                "login failed: " + errorInfo.errorDescription
                            )
                            runOnUiThread {
                                displayToast(text.toString())
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
        val date: Calendar = Calendar.getInstance()
        val nextMonthDate = SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(date.time)
        val startTime = DateUtils(nextMonthDate)
        actualStartTime = startTime.getTime()
        mIsPeerToPeerMode = false
        val targetName =
            appointment!!.host!!.first_name + " " + appointment!!.host!!.last_name
        mChannelName = targetName
        mChannelMemberCount = 1
        onlineChatView.txtTrainerChatName.text = mChannelName
        createAndJoinChannel()
        runOnUiThread {
            val layoutManager = LinearLayoutManager(requireActivity())
            layoutManager.orientation = RecyclerView.VERTICAL
            mMessageAdapter = MessageAdapter(requireActivity(), mMessageBeanList, this, preference!![PrefKeys.PREF_PHOTO, ""]!!, appointment!!.doctor_photo, this)
            onlineChatView.textTrainingSessionMessageList.layoutManager = layoutManager
            onlineChatView.textTrainingSessionMessageList.adapter = mMessageAdapter
        }
    }

    private fun createAndJoinChannel() {
        // step 1: create a channel instance
        mRtmChannel = mRtmClient!!.createChannel(CHANNEL_NAME, myChannelListener)
        if (mRtmChannel == null) {
            displayToast(getString(R.string.join_channel_failed))
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
                    displayToast(getString(R.string.join_channel_failed))
                    displayToast(errorInfo.errorDescription + " - " + errorInfo.errorCode)
                    popBackStack()
                }
            }
        })
    }

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
                            displayToast(getString(R.string.send_msg_failed))
                        else -> displayToast(errorInfo.errorDescription + " - " + errorInfo.errorCode)
                    }
                }
            }
        })
    }

    private fun leaveAndReleaseChannel() {
        if (mRtmChannel != null) {
            mRtmChannel!!.leave(null)
            mRtmChannel!!.release()
            mRtmChannel = null
        }
    }

    private val myChannelListener = object : RtmChannelListener {
        override fun onMemberCountUpdated(i: Int) {}
        override fun onAttributesUpdated(list: List<RtmChannelAttribute>) {}
        override fun onMessageReceived(message: RtmMessage, fromMember: RtmChannelMember) {
            runOnUiThread {
                val targetName =
                    appointment!!.host!!.first_name.take(1) + "" + appointment!!.host!!.last_name.take(
                        1
                    )
                val account = fromMember.userId
                Log.i(TAG, "onMessageReceived account = $account msg = $message")
                val messageBean = MessageBean(targetName, message, false)
                messageBean.setBackground(getMessageColor(account))
                mMessageBeanList.add(messageBean)
                mMessageAdapter!!.notifyItemRangeChanged(mMessageBeanList.size, 1)
                onlineChatView.textTrainingSessionMessageList.scrollToPosition(mMessageBeanList.size - 1)
            }
        }

        override fun onImageMessageReceived(p0: RtmImageMessage?, p1: RtmChannelMember?) {

        }

        override fun onFileMessageReceived(p0: RtmFileMessage?, p1: RtmChannelMember?) {
            Log.i("File received", "Hurrray ")
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
        onlineChatView.txtTrainerChatName.text = mChannelName
    }

    private fun doLogout() {
        try {
            leaveAndReleaseChannel()
            if (mRtmClient != null) {
                mRtmClient!!.logout(null)
                MessageUtil.cleanMessageListBeanList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
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
                val document = Base64.encodeToString(bytes, Base64.DEFAULT)
                /*val document =
                    "JVBERi0xLjMKMyAwIG9iago8PC9UeXBlIC9QYWdlCi9QYXJlbnQgMSAwIFIKL1Jlc291cmNlcyAyIDAgUgovQ29udGVudHMgNCAwIFI+PgplbmRvYmoKNCAwIG9iago8PC9GaWx0ZXIgL0ZsYXRlRGVjb2RlIC9MZW5ndGggNjQ3Pj4Kc3RyZWFtCnicnZbBctowFEX3fMVbtjNBlmRZsndtSNLSaVMK7qJLBz8cN8YmspxM/r6ygdrgpDMVCyHEWOceJD3B4cuEkkDB8+QyBu+GAQsIpRBv4DreD3Fgqh9itkuBDlqdTR5tl1FuWxGJQ6szsI/7jLAIpJREMYhTePerajQs4zks8bHB2sAmLzCFulmvsa43TVG8kPcQ/7Ys+DGhkO1D+MD4WS5xMvRqLh4SPwBplRgHbkMICVPOiQxBI6ygzc0DdtKe5KbWK+xyLzHLa6MTk1cl3DbbO9R9TB4JEjInEI8UkWJIWnxfxStv6V173Pcok1INQJ1QEHEihItQoBQJaIe5TbY4MnCZ+WAwmPqTXePkCVZ5md2PssuIKOmUPbAR/Q5wlRiEagM3eWEZYwsHxtGih1AxpXzKKfdHDsIeiMjJweeEBfsNZc/ADaI9B2vMnzAdWzhQjhY9htFRei6IYE7paUSE2m/S5GWLpYFvVfrKLnIgHJP3iHlpUJdo4DIpH+wyX8BMY5obqDRc4Z3tzBKdggfLxqbpPlzAz8V85EsZUb6Lrwht2YpOfJe4QY3lGt+qAS60g/sAN1t8nH39HMeXIfx9nXsJFRHqdJKEFG2dakGxTso6WXdVbWUS09QjIxfO0agHzartrkBjS/1qUOpHToEkfujkJFhbufalenizPOfmfuzkwDk69aAr3CXadPvCFqNFVZvBr/d/d5Rg9kunO8qPZFsmusXEAnf3Vfnm5nShHLQHGMrYlPtUiJCPriY/7N5cPJTfFo0WcL1N8gLm45LoMvsxfz+9NnmK9cOHvEzzZGdXjWTVE8nLs7V79a9Q2I/8A6lkVwMCEgWH6pEhMI/1hD/eriz+CmVuZHN0cmVhbQplbmRvYmoKMSAwIG9iago8PC9UeXBlIC9QYWdlcwovS2lkcyBbMyAwIFIgXQovQ291bnQgMQovTWVkaWFCb3ggWzAgMCA1OTUuMjggODQxLjg5XQo+PgplbmRvYmoKNSAwIG9iago8PC9UeXBlIC9Gb250Ci9CYXNlRm9udCAvSGVsdmV0aWNhLUJvbGQKL1N1YnR5cGUgL1R5cGUxCi9FbmNvZGluZyAvV2luQW5zaUVuY29kaW5nCj4+CmVuZG9iago2IDAgb2JqCjw8L1R5cGUgL0ZvbnQKL0Jhc2VGb250IC9IZWx2ZXRpY2EtT2JsaXF1ZQovU3VidHlwZSAvVHlwZTEKL0VuY29kaW5nIC9XaW5BbnNpRW5jb2RpbmcKPj4KZW5kb2JqCjcgMCBvYmoKPDwvVHlwZSAvRm9udAovQmFzZUZvbnQgL1RpbWVzLVJvbWFuCi9TdWJ0eXBlIC9UeXBlMQovRW5jb2RpbmcgL1dpbkFuc2lFbmNvZGluZwo+PgplbmRvYmoKOCAwIG9iago8PC9UeXBlIC9Gb250Ci9CYXNlRm9udCAvSGVsdmV0aWNhCi9TdWJ0eXBlIC9UeXBlMQovRW5jb2RpbmcgL1dpbkFuc2lFbmNvZGluZwo+PgplbmRvYmoKMiAwIG9iago8PAovUHJvY1NldCBbL1BERiAvVGV4dCAvSW1hZ2VCIC9JbWFnZUMgL0ltYWdlSV0KL0ZvbnQgPDwKL0YxIDUgMCBSCi9GMiA2IDAgUgovRjMgNyAwIFIKL0Y0IDggMCBSCj4+Ci9YT2JqZWN0IDw8Cj4+Cj4+CmVuZG9iago5IDAgb2JqCjw8Ci9Qcm9kdWNlciAoRlBERiAxLjcpCi9DcmVhdGlvbkRhdGUgKEQ6MjAyMzAyMDQwMDE0MzkpCj4+CmVuZG9iagoxMCAwIG9iago8PAovVHlwZSAvQ2F0YWxvZwovUGFnZXMgMSAwIFIKPj4KZW5kb2JqCnhyZWYKMCAxMQowMDAwMDAwMDAwIDY1NTM1IGYgCjAwMDAwMDA4MDQgMDAwMDAgbiAKMDAwMDAwMTI5MCAwMDAwMCBuIAowMDAwMDAwMDA5IDAwMDAwIG4gCjAwMDAwMDAwODcgMDAwMDAgbiAKMDAwMDAwMDg5MSAwMDAwMCBuIAowMDAwMDAwOTkyIDAwMDAwIG4gCjAwMDAwMDEwOTYgMDAwMDAgbiAKMDAwMDAwMTE5NCAwMDAwMCBuIAowMDAwMDAxNDI0IDAwMDAwIG4gCjAwMDAwMDE0OTkgMDAwMDAgbiAKdHJhaWxlcgo8PAovU2l6ZSAxMQovUm9vdCAxMCAwIFIKL0luZm8gOSAwIFIKPj4Kc3RhcnR4cmVmCjE1NDkKJSVFT0YK"*/
                sendFile(
                    appointment!!.appointment!!.appointment_id,
                    fileName.split(".")[0],
                    fileType.split("/")[1],
                    document
                ) { response ->
                    hideProgress()
                    Log.i("Response", response.toString())
                    val jsonObj = JSONObject(response.toString())
                    val message = mRtmClient!!.createMessage()
                    message.rawMessage = ("$fileName," + jsonObj.getString("file")).toByteArray()
                    //message.text = "$fileName,"+jsonObj.getString("file")
                    val patientName =
                        preference!![PrefKeys.PREF_FNAME, ""]!!.take(1) + "" + preference!![PrefKeys.PREF_LNAME, ""]!!.take(
                            1
                        )
                    val messageBean = MessageBean(patientName, message, true)
                    mMessageBeanList.add(messageBean)
                    mMessageAdapter!!.notifyItemRangeChanged(mMessageBeanList.size, 1)
                    onlineChatView.textTrainingSessionMessageList.scrollToPosition(mMessageBeanList.size - 1)
                }
            } catch (e: java.lang.Exception) {
                // TODO: handle exception
                e.printStackTrace()
                Log.d("error", "onActivityResult: $e")
            }
        } else {
            displayMsg("Alert", "File size should not exceed more than 10 MB")
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

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment TrainingSessionFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: GetAppointment, param2: String, param3: String, param4: String) =
            TrainingSessionFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                    putString(ARG_PARAM3, param3)
                    putString(ARG_PARAM4, param4)
                }
            }

        const val TAG = "Screen_Training_Session"
        private const val PICKFILE_REQUEST_CODE = 9
    }

    override fun onItemClick(message: MessageBean?) {
        onlineChatView.layoutTrainingSessionChatMessage.visibility = View.GONE
        onlineChatView.layoutTrainingSessionWebViewFile.visibility = View.VISIBLE
        val browser = onlineChatView.webViewTrainingSessionFile.settings
        browser.javaScriptEnabled = true
        browser.builtInZoomControls = true
        browser.pluginState = WebSettings.PluginState.ON
        browser.loadWithOverviewMode = true
        browser.useWideViewPort = true
        onlineChatView.webViewTrainingSessionFile.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                view?.loadUrl(url!!)
                return true
            }
        }
        onlineChatView.webViewTrainingSessionFile.webViewClient = object : WebViewClient() {
            private var mProgressDialog: ProgressDialog? = null
            override fun onPageFinished(view: WebView, url: String?) {
                view.settings.loadsImagesAutomatically = true
                onlineChatView.webViewTrainingSessionFile.visibility = View.VISIBLE
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
                mProgressDialog = ProgressDialog.show(mContext, "", "Loading...")
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
            onlineChatView.webViewTrainingSessionFile.loadUrl(
                "http://docs.google.com/gview?embedded=true&url=" + BaseActivity.baseURL.dropLast(
                    5
                ) + mMsgArr[1]
            )
        } else {
            onlineChatView.webViewTrainingSessionFile.loadUrl(BaseActivity.baseURL.dropLast(5) + mMsgArr[1])
        }
    }

    override fun onBottomReached(position: Int) {

    }
}