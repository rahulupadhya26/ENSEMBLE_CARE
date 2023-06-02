package com.app.selfcare.fragment

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentResolver
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.provider.OpenableColumns
import android.text.TextUtils
import android.util.Base64
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.webkit.MimeTypeMap
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.BaseActivity
import com.app.selfcare.BuildConfig
import com.app.selfcare.R
import com.app.selfcare.adapters.GridVideoViewContainer
import com.app.selfcare.adapters.MessageAdapter
import com.app.selfcare.adapters.SmallVideoViewAdapter
import com.app.selfcare.controller.AGEventHandler
import com.app.selfcare.controller.DuringCallEventHandler
import com.app.selfcare.controller.OnMessageClickListener
import com.app.selfcare.data.*
import com.app.selfcare.databinding.DialogOnlineChatBinding
import com.app.selfcare.databinding.FragmentGroupVideoCallBinding
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper.get
import com.app.selfcare.realTimeMessaging.ChatManager
import com.app.selfcare.services.SelfCareApplication
import com.app.selfcare.utils.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.video.VideoCanvas
import io.agora.rtm.*
import org.json.JSONObject
import org.slf4j.LoggerFactory
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
 * Use the [GroupVideoCallFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GroupVideoCallFragment : BaseFragment(), DuringCallEventHandler, OnMessageClickListener {
    // TODO: Rename and change types of parameters
    private var token: String? = null
    private var channelName: String? = null
    private var rtm: String? = null
    private var appointment: GetAppointment? = null
    private lateinit var binding: FragmentGroupVideoCallBinding

    // should only be modified under UI thread
    private val mUidsList = HashMap<Int, SurfaceView>() // uid = 0 || uid == EngineConfig.mUid
    var mLayoutType = LAYOUT_TYPE_DEFAULT
    private var mGridVideoViewContainer: GridVideoViewContainer? = null
    private var mSmallVideoViewDock: RelativeLayout? = null

    @Volatile
    private var mVideoMuted = false

    @Volatile
    private var mAudioMuted = false

    @Volatile
    private var mAudioRouting = Constants.AUDIO_ROUTE_DEFAULT

    @Volatile
    private var mFullScreen = false
    private var mIsLandscape = false
    private var mSmallVideoViewAdapter: SmallVideoViewAdapter? = null
    private val mUIHandler = Handler()
    private var actualStartTime: String? = null
    private var actualEndTime: String? = null
    private var duration: String? = null
    private var time: String? = null
    private var createPostDialog: BottomSheetDialog? = null
    private var mChatManager: ChatManager? = null
    private var mRtmClient: RtmClient? = null
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
    private var mEndCall = false
    private var mMuted = false

    private var wasRunning = false
    private lateinit var dialogBinding: DialogOnlineChatBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            token = it.getString(ARG_PARAM1)
            channelName = it.getString(ARG_PARAM2)
            rtm = it.getString(ARG_PARAM3)
            appointment = it.getParcelable(ARG_PARAM4)
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_group_video_call
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGroupVideoCallBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE

        binding.buttonCall.setOnClickListener {
            log.info("onHangupClicked $view")
            val builder = AlertDialog.Builder(requireActivity())
            builder.setTitle("Confirmation")
            builder.setMessage("Do you wish to end the call?")
            builder.setPositiveButton("Yes") { dialog, which ->
                val date: Calendar = Calendar.getInstance()
                val nextMonthDate = SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(date.time)
                val endTime = DateUtils(nextMonthDate)
                actualEndTime = endTime.getTime()
                duration = time
                clearPreviousFragmentStack()
                replaceFragmentNoBackStack(
                    TherapistFeedbackFragment.newInstance(appointment!!),
                    R.id.layout_home,
                    TherapistFeedbackFragment.TAG
                )
            }
            builder.setNegativeButton("No") { dialog, which ->
                dialog.dismiss()
            }
            builder.setCancelable(false)
            builder.show()
        }

        binding.buttonMute.setOnClickListener {
            log.info("onVoiceMuteClicked " + view + " " + mUidsList.size + " video_status: " + mVideoMuted + " audio_status: " + mAudioMuted)
            mAudioMuted = !mAudioMuted
            val rtcEngine = rtcEngine()
            rtcEngine!!.muteLocalAudioStream(mAudioMuted)
            val iv = binding.buttonMute
            iv.setImageResource(if (mAudioMuted) R.drawable.btn_mute else R.drawable.btn_unmute)
        }

        binding.buttonChat.setOnClickListener {
            if (createPostDialog != null) {
                createPostDialog!!.show()
            } else {
                displayToast("Cannot open chat screen.")
            }
        }

        binding.buttonSwitchCamera.setOnClickListener {
            val rtcEngine = rtcEngine()
            // Switches between front and rear cameras.
            rtcEngine!!.switchCamera()
        }

        binding.buttonSwitchVideo.setOnClickListener {
            log.info("onVoiceChatClicked " + view + " " + mUidsList.size + " video_status: " + mVideoMuted + " audio_status: " + mAudioMuted)
            val rtcEngine = rtcEngine()
            mVideoMuted = !mVideoMuted
            if (mVideoMuted) {
                rtcEngine!!.enableLocalVideo(false)
            } else {
                rtcEngine!!.enableLocalVideo(true)
            }
            val iv = binding.buttonSwitchVideo
            iv.setImageResource(if (mVideoMuted) R.mipmap.ic_video_disabled else R.mipmap.ic_video_enabled)
            hideLocalView(mVideoMuted)
        }

        initUIandEvent()
    }

    private fun virtualKeyHeight(): Int {
        val hasPermanentMenuKey = ViewConfiguration.get(requireActivity()).hasPermanentMenuKey()
        if (hasPermanentMenuKey) {
            return 0
        }

        // Also can use getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        val metrics = DisplayMetrics()
        val display: Display = requireActivity().windowManager.defaultDisplay
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            display.getRealMetrics(metrics)
        } else {
            display.getMetrics(metrics)
        }
        var fullHeight = metrics.heightPixels
        var fullWidth = metrics.widthPixels
        if (fullHeight < fullWidth) {
            fullHeight = fullHeight xor fullWidth
            fullWidth = fullWidth xor fullHeight
            fullHeight = fullHeight xor fullWidth
        }
        display.getMetrics(metrics)
        var newFullHeight = metrics.heightPixels
        var newFullWidth = metrics.widthPixels
        if (newFullHeight < newFullWidth) {
            newFullHeight = newFullHeight xor newFullWidth
            newFullWidth = newFullWidth xor newFullHeight
            newFullHeight = newFullHeight xor newFullWidth
        }
        var virtualKeyHeight = fullHeight - newFullHeight
        if (virtualKeyHeight > 0) {
            return virtualKeyHeight
        }
        virtualKeyHeight = fullWidth - newFullWidth
        return virtualKeyHeight
    }

    private fun application(): SelfCareApplication {
        return requireActivity().application as SelfCareApplication
    }

    private fun rtcEngine(): RtcEngine? {
        return application().rtcEngine()
    }

    private fun config(): EngineConfig? {
        return application().config()
    }

    private fun initUIandEvent() {
        addEventHandler(this)
        mGridVideoViewContainer = binding.gridVideoViewContainer
        mGridVideoViewContainer!!.setItemEventHandler(object :
            RecyclerItemClickListener.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                onBigVideoViewClicked(view, position)
            }

            override fun onItemLongClick(view: View, position: Int) {}
            override fun onItemDoubleClick(view: View, position: Int) {
                onBigVideoViewDoubleClicked(view, position)
            }
        })
        val surfaceV = RtcEngine.CreateRendererView(requireActivity())
        preview(true, surfaceV, 0)
        surfaceV.setZOrderOnTop(false)
        surfaceV.setZOrderMediaOverlay(false)
        mUidsList[0] = surfaceV // get first surface view
        mGridVideoViewContainer!!.initViewContainer(
            requireActivity(),
            0,
            mUidsList,
            mIsLandscape
        ) // first is now full view
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
                            dialogBinding.chatMessageList.scrollToPosition(
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
        joinChannel(channelName!!, token, preference!![PrefKeys.PREF_USER_ID, "0"]!!.toInt())
        running = true
        val date: Calendar = Calendar.getInstance()
        val nextMonthDate = SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(date.time)
        val startTime = DateUtils(nextMonthDate)
        actualStartTime = startTime.getTime()
        runTimer()
        optional()
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
                time = String
                    .format(
                        Locale.getDefault(),
                        "%02d:%02d:%02d", hours,
                        minutes, secs
                    )

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

    private fun onBigVideoViewClicked(view: View, position: Int) {
        log.debug("onItemClick $view $position $mLayoutType")
        toggleFullscreen()
    }

    private fun onBigVideoViewDoubleClicked(view: View, position: Int) {
        log.debug("onItemDoubleClick $view $position $mLayoutType")
        if (mUidsList.size < 2) {
            return
        }
        val user = mGridVideoViewContainer!!.getItem(position)
        val uid = if (user.mUid == 0) config()!!.mUid else user.mUid
        if (mLayoutType == LAYOUT_TYPE_DEFAULT && mUidsList.size != 1) {
            switchToSmallVideoView(uid)
        } else {
            switchToDefaultVideoView()
        }
    }

    private fun onSmallVideoViewDoubleClicked(view: View, position: Int) {
        log.debug("onItemDoubleClick small $view $position $mLayoutType")
        switchToDefaultVideoView()
    }

    private fun showOrHideStatusBar(hide: Boolean) {
        // May fail on some kinds of devices
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            val decorView = requireActivity().window.decorView
            var uiOptions = decorView.systemUiVisibility
            uiOptions = if (hide) {
                uiOptions or View.SYSTEM_UI_FLAG_FULLSCREEN
            } else {
                uiOptions xor View.SYSTEM_UI_FLAG_FULLSCREEN
            }
            decorView.systemUiVisibility = uiOptions
        }
    }

    private fun toggleFullscreen() {
        mFullScreen = !mFullScreen
        showOrHideCtrlViews(mFullScreen)
        mUIHandler.postDelayed(
            { showOrHideStatusBar(mFullScreen) },
            200
        ) // action bar fade duration
    }

    private fun showOrHideCtrlViews(hide: Boolean) {
        val ab = (requireActivity() as AppCompatActivity).supportActionBar
        if (ab != null) {
            if (hide) {
                ab.hide()
            } else {
                ab.show()
            }
        }
        /*findViewById<View>(R.id.extra_ops_container).visibility =
            if (hide) View.INVISIBLE else View.VISIBLE*/
        binding.bottomActionContainer.visibility =
            if (hide) View.INVISIBLE else View.VISIBLE
    }

    private fun optional() {
        requireActivity().volumeControlStream = AudioManager.STREAM_VOICE_CALL
    }

    private fun optionalDestroy() {}

    // save the new value
    private val videoEncResolutionIndex: Int
        private get() {
            val pref = PreferenceManager.getDefaultSharedPreferences(requireActivity())
            var videoEncResolutionIndex = pref.getInt(
                ConstantApp.PrefManager.PREF_PROPERTY_VIDEO_ENC_RESOLUTION,
                ConstantApp.DEFAULT_VIDEO_ENC_RESOLUTION_IDX
            )
            if (videoEncResolutionIndex > ConstantApp.VIDEO_DIMENSIONS.size - 1) {
                videoEncResolutionIndex = ConstantApp.DEFAULT_VIDEO_ENC_RESOLUTION_IDX

                // save the new value
                val editor = pref.edit()
                editor.putInt(
                    ConstantApp.PrefManager.PREF_PROPERTY_VIDEO_ENC_RESOLUTION,
                    videoEncResolutionIndex
                )
                editor.apply()
            }
            return videoEncResolutionIndex
        }

    fun onSwitchSpeakerClicked(view: View?) {
        val rtcEngine = rtcEngine()
        /*
          Enables/Disables the audio playback route to the speakerphone.
          This method sets whether the audio is routed to the speakerphone or earpiece.
          After calling this method, the SDK returns the onAudioRouteChanged callback
          to indicate the changes.
         */rtcEngine!!.setEnableSpeakerphone(mAudioRouting != Constants.AUDIO_ROUTE_SPEAKERPHONE)
    }

    private fun deInitUIandEvent() {
        optionalDestroy()
        doLeaveChannel()
        removeEventHandler(this)
        mUidsList.clear()
    }

    private fun doLeaveChannel() {
        leaveChannel(config()!!.mChannel)
        preview(false, null, 0)
    }

    override fun onPause() {
        super.onPause()
        if (runnable != null) {
            handler.removeCallbacks(runnable!!)
        }
        wasRunning = running;
        running = false;
    }

    override fun onResume() {
        super.onResume()
        if (wasRunning) {
            running = true;
        }
    }

    private val localView: SurfaceView?
        private get() {
            for ((key, value) in mUidsList) {
                if (key == 0 || key == config()!!.mUid) {
                    return value
                }
            }
            return null
        }

    private fun hideLocalView(hide: Boolean) {
        val uid = config()!!.mUid
        doHideTargetView(uid, hide)
    }

    private fun doHideTargetView(targetUid: Int, hide: Boolean) {
        val status = HashMap<Int, Int>()
        status[targetUid] = if (hide) UserStatusData.VIDEO_MUTED else UserStatusData.DEFAULT_STATUS
        if (mLayoutType == LAYOUT_TYPE_DEFAULT) {
            mGridVideoViewContainer!!.notifyUiChanged(mUidsList, targetUid, status, null)
        } else if (mLayoutType == LAYOUT_TYPE_SMALL) {
            val bigBgUser = mGridVideoViewContainer!!.getItem(0)
            if (bigBgUser.mUid == targetUid) { // big background is target view
                mGridVideoViewContainer!!.notifyUiChanged(mUidsList, targetUid, status, null)
            } else { // find target view in small video view list
                log.warn("SmallVideoViewAdapter call notifyUiChanged " + mUidsList + " " + (bigBgUser.mUid and 0xFFFFFFFFL.toInt()) + " target: " + (targetUid and 0xFFFFFFFFL.toInt()) + "==" + targetUid + " " + status)
                mSmallVideoViewAdapter!!.notifyUiChanged(mUidsList, bigBgUser.mUid, status, null)
            }
        }
    }

    override fun onUserJoined(uid: Int) {
        log.debug("onUserJoined " + (uid and 0xFFFFFFFFL.toInt()))
        doRenderRemoteUi(uid)
    }

    override fun onFirstRemoteVideoDecoded(uid: Int, width: Int, height: Int, elapsed: Int) {
        log.debug("onFirstRemoteVideoDecoded " + (uid and 0xFFFFFFFFL.toInt()) + " " + width + " " + height + " " + elapsed)
    }

    private fun doRenderRemoteUi(uid: Int) {
        runOnUiThread(Runnable {
            if (mUidsList.containsKey(uid)) {
                return@Runnable
            }

            /*
                      Creates the video renderer view.
                      CreateRendererView returns the SurfaceView type. The operation and layout of the
                      view are managed by the app, and the Agora SDK renders the view provided by the
                      app. The video display view must be created using this method instead of
                      directly calling SurfaceView.
                     */
            val surfaceV = RtcEngine.CreateRendererView(requireActivity())
            mUidsList[uid] = surfaceV
            val useDefaultLayout = mLayoutType == LAYOUT_TYPE_DEFAULT
            surfaceV.setZOrderOnTop(true)
            surfaceV.setZOrderMediaOverlay(true)

            /*
                      Initializes the video view of a remote user.
                      This method initializes the video view of a remote stream on the local device. It affects only the video view that the local user sees.
                      Call this method to bind the remote video stream to a video view and to set the rendering and mirror modes of the video view.
                     */
            rtcEngine()!!.setupRemoteVideo(
                VideoCanvas(
                    surfaceV,
                    VideoCanvas.RENDER_MODE_HIDDEN,
                    uid
                )
            )
            if (useDefaultLayout) {
                log.debug("doRenderRemoteUi LAYOUT_TYPE_DEFAULT " + (uid and 0xFFFFFFFFL.toInt()))
                switchToDefaultVideoView()
            } else {
                val bigBgUid =
                    if (mSmallVideoViewAdapter == null) uid else mSmallVideoViewAdapter!!.exceptedUid
                log.debug("doRenderRemoteUi LAYOUT_TYPE_SMALL " + (uid and 0xFFFFFFFFL.toInt()) + " " + (bigBgUid and 0xFFFFFFFFL.toInt()))
                switchToSmallVideoView(bigBgUid)
            }
        })
    }

    override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
        log.debug("onJoinChannelSuccess " + channel + " " + (uid and 0xFFFFFFFFL.toInt()) + " " + elapsed)
    }

    override fun onUserOffline(uid: Int, reason: Int) {
        log.debug("onUserOffline " + (uid and 0xFFFFFFFFL.toInt()) + " " + reason)
        doRemoveRemoteUi(uid)
    }

    override fun onExtraCallback(type: Int, vararg data: Any) {
        runOnUiThread(Runnable {
            doHandleExtraCallback(type, *data)
        })
    }

    private fun doHandleExtraCallback(type: Int, vararg data: Any) {
        var peerUid: Int
        val muted: Boolean
        when (type) {
            AGEventHandler.EVENT_TYPE_ON_USER_AUDIO_MUTED -> {
                peerUid = data[0] as Int
                muted = data[1] as Boolean
                if (mLayoutType == LAYOUT_TYPE_DEFAULT) {
                    val status = HashMap<Int, Int>()
                    status[peerUid] =
                        if (muted) UserStatusData.AUDIO_MUTED else UserStatusData.DEFAULT_STATUS
                    mGridVideoViewContainer!!.notifyUiChanged(
                        mUidsList,
                        config()!!.mUid,
                        status,
                        null
                    )
                }
            }
            AGEventHandler.EVENT_TYPE_ON_USER_VIDEO_MUTED -> {
                peerUid = data[0] as Int
                muted = data[1] as Boolean
                doHideTargetView(peerUid, muted)
            }
            AGEventHandler.EVENT_TYPE_ON_USER_VIDEO_STATS -> {
                val stats = data[0] as IRtcEngineEventHandler.RemoteVideoStats
                if (Constant.SHOW_VIDEO_INFO) {
                    if (mLayoutType == LAYOUT_TYPE_DEFAULT) {
                        mGridVideoViewContainer!!.addVideoInfo(
                            stats.uid,
                            VideoInfoData(
                                stats.width,
                                stats.height,
                                stats.delay,
                                stats.rendererOutputFrameRate,
                                stats.receivedBitrate
                            )
                        )
                        val uid = config()!!.mUid
                        val profileIndex = videoEncResolutionIndex
                        val resolution =
                            resources.getStringArray(R.array.string_array_resolutions)[profileIndex]
                        val fps =
                            resources.getStringArray(R.array.string_array_frame_rate)[profileIndex]
                        val rwh = resolution.split("x").toTypedArray()
                        val width = Integer.valueOf(rwh[0])
                        val height = Integer.valueOf(rwh[1])
                        mGridVideoViewContainer!!.addVideoInfo(
                            uid, VideoInfoData(
                                if (width > height) width else height,
                                if (width > height) height else width,
                                0, Integer.valueOf(fps), Integer.valueOf(0)
                            )
                        )
                    }
                } else {
                    mGridVideoViewContainer!!.cleanVideoInfo()
                }
            }
            AGEventHandler.EVENT_TYPE_ON_SPEAKER_STATS -> {
                val infos = data[0] as Array<IRtcEngineEventHandler.AudioVolumeInfo>
                // local guy, ignore it
                if (infos.size == 1 && infos[0].uid == 0) {

                }
                if (mLayoutType == LAYOUT_TYPE_DEFAULT) {
                    val volume = HashMap<Int, Int>()
                    for (each in infos) {
                        peerUid = each.uid
                        val peerVolume = each.volume
                        if (peerUid == 0) {
                            continue
                        }
                        volume[peerUid] = peerVolume
                    }
                    mGridVideoViewContainer!!.notifyUiChanged(
                        mUidsList,
                        config()!!.mUid,
                        null,
                        volume
                    )
                }
            }
            AGEventHandler.EVENT_TYPE_ON_APP_ERROR -> {}
            AGEventHandler.EVENT_TYPE_ON_DATA_CHANNEL_MSG -> {}
            AGEventHandler.EVENT_TYPE_ON_AGORA_MEDIA_ERROR -> {}
            AGEventHandler.EVENT_TYPE_ON_AUDIO_ROUTE_CHANGED -> notifyHeadsetPlugged(data[0] as Int)
        }
    }

    private fun requestRemoteStreamType(currentHostCount: Int) {
        log.debug("requestRemoteStreamType $currentHostCount")
    }

    private fun doRemoveRemoteUi(uid: Int) {
        runOnUiThread(Runnable {
            val target = mUidsList.remove(uid) ?: return@Runnable
            var bigBgUid = -1
            if (mSmallVideoViewAdapter != null) {
                bigBgUid = mSmallVideoViewAdapter!!.exceptedUid
            }
            log.debug("doRemoveRemoteUi " + (uid and 0xFFFFFFFFL.toInt()) + " " + (bigBgUid and 0xFFFFFFFFL.toInt()) + " " + mLayoutType)
            if (mLayoutType == LAYOUT_TYPE_DEFAULT || uid == bigBgUid) {
                switchToDefaultVideoView()
            } else {
                switchToSmallVideoView(bigBgUid)
            }
        })
    }

    private fun switchToDefaultVideoView() {
        if (mSmallVideoViewDock != null) {
            mSmallVideoViewDock!!.visibility = View.GONE
        }
        mGridVideoViewContainer!!.initViewContainer(
            requireActivity(),
            config()!!.mUid,
            mUidsList,
            mIsLandscape
        )
        mLayoutType = LAYOUT_TYPE_DEFAULT
        var setRemoteUserPriorityFlag = false
        var sizeLimit = mUidsList.size
        if (sizeLimit > ConstantApp.MAX_PEER_COUNT + 1) {
            sizeLimit = ConstantApp.MAX_PEER_COUNT + 1
        }
        for (i in 0 until sizeLimit) {
            val uid = mGridVideoViewContainer!!.getItem(i).mUid
            if (config()!!.mUid != uid) {
                if (!setRemoteUserPriorityFlag) {
                    setRemoteUserPriorityFlag = true
                    rtcEngine()!!.setRemoteUserPriority(uid, Constants.USER_PRIORITY_HIGH)
                    log.debug("setRemoteUserPriority USER_PRIORITY_HIGH " + mUidsList.size + " " + (uid and 0xFFFFFFFFL.toInt()))
                } else {
                    rtcEngine()!!.setRemoteUserPriority(uid, Constants.USER_PRIORITY_NORANL)
                    log.debug("setRemoteUserPriority USER_PRIORITY_NORANL " + mUidsList.size + " " + (uid and 0xFFFFFFFFL.toInt()))
                }
            }
        }
    }

    private fun switchToSmallVideoView(bigBgUid: Int) {
        val slice = HashMap<Int, SurfaceView?>(1)
        slice[bigBgUid] = mUidsList[bigBgUid]
        val iterator: Iterator<SurfaceView> = mUidsList.values.iterator()
        while (iterator.hasNext()) {
            val s = iterator.next()
            s.setZOrderOnTop(true)
            s.setZOrderMediaOverlay(true)
        }
        mUidsList[bigBgUid]!!.setZOrderOnTop(false)
        mUidsList[bigBgUid]!!.setZOrderMediaOverlay(false)
        mGridVideoViewContainer!!.initViewContainer(
            requireActivity(),
            bigBgUid,
            slice,
            mIsLandscape
        )
        bindToSmallVideoView(bigBgUid)
        mLayoutType = LAYOUT_TYPE_SMALL
        requestRemoteStreamType(mUidsList.size)
    }

    private fun bindToSmallVideoView(exceptUid: Int) {
        if (mSmallVideoViewDock == null) {
            val stub = binding.smallVideoViewDock
            mSmallVideoViewDock = stub.inflate() as RelativeLayout
        }
        val twoWayVideoCall = mUidsList.size == 2
        val recycler =
            mSmallVideoViewDock!!.findViewById<View>(R.id.small_video_view_container) as RecyclerView
        var create = false
        if (mSmallVideoViewAdapter == null) {
            create = true
            mSmallVideoViewAdapter =
                SmallVideoViewAdapter(requireActivity(), config()!!.mUid, exceptUid, mUidsList)
            mSmallVideoViewAdapter!!.setHasStableIds(true)
        }
        recycler.setHasFixedSize(true)
        log.debug("bindToSmallVideoView " + twoWayVideoCall + " " + (exceptUid and 0xFFFFFFFFL.toInt()))
        if (twoWayVideoCall) {
            recycler.layoutManager =
                RtlLinearLayoutManager(requireActivity(), RtlLinearLayoutManager.HORIZONTAL, false)
        } else {
            recycler.layoutManager = LinearLayoutManager(
                requireActivity(), LinearLayoutManager.HORIZONTAL, false
            )
        }
        recycler.addItemDecoration(SmallVideoViewDecoration())
        recycler.adapter = mSmallVideoViewAdapter
        recycler.addOnItemTouchListener(
            RecyclerItemClickListener(
                requireActivity(),
                object : RecyclerItemClickListener.OnItemClickListener {
                    override fun onItemClick(view: View, position: Int) {}
                    override fun onItemLongClick(view: View, position: Int) {}
                    override fun onItemDoubleClick(view: View, position: Int) {
                        onSmallVideoViewDoubleClicked(view, position)
                    }
                })
        )
        recycler.isDrawingCacheEnabled = true
        recycler.drawingCacheQuality = View.DRAWING_CACHE_QUALITY_AUTO
        if (!create) {
            mSmallVideoViewAdapter!!.setLocalUid(config()!!.mUid)
            mSmallVideoViewAdapter!!.notifyUiChanged(mUidsList, exceptUid, null, null)
        }
        for (tempUid in mUidsList.keys) {
            if (config()!!.mUid != tempUid) {
                if (tempUid == exceptUid) {
                    rtcEngine()!!.setRemoteUserPriority(tempUid, Constants.USER_PRIORITY_HIGH)
                    log.debug("setRemoteUserPriority USER_PRIORITY_HIGH " + mUidsList.size + " " + (tempUid and 0xFFFFFFFFL.toInt()))
                } else {
                    rtcEngine()!!.setRemoteUserPriority(tempUid, Constants.USER_PRIORITY_NORANL)
                    log.debug("setRemoteUserPriority USER_PRIORITY_NORANL " + mUidsList.size + " " + (tempUid and 0xFFFFFFFFL.toInt()))
                }
            }
        }
        recycler.visibility = View.VISIBLE
        mSmallVideoViewDock!!.visibility = View.VISIBLE
    }

    private fun notifyHeadsetPlugged(routing: Int) {
        log.info("notifyHeadsetPlugged $routing $mVideoMuted")
        mAudioRouting = routing
        val iv = binding.switchSpeakerId
        if (mAudioRouting == Constants.AUDIO_ROUTE_SPEAKERPHONE) {
            iv.setImageResource(R.drawable.btn_speaker)
        } else {
            iv.setImageResource(R.drawable.btn_speaker_off)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        mIsLandscape = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE
        if (mLayoutType == LAYOUT_TYPE_DEFAULT) {
            switchToDefaultVideoView()
        } else if (mSmallVideoViewAdapter != null) {
            switchToSmallVideoView(mSmallVideoViewAdapter!!.exceptedUid)
        }
    }

    fun openOnlineChatWindow() {
        createPostDialog = BottomSheetDialog(requireActivity())
        dialogBinding = DialogOnlineChatBinding.inflate(layoutInflater)
        val onlineChatView = dialogBinding.root
        createPostDialog!!.setContentView(onlineChatView)
        createPostDialog!!.behavior.peekHeight =
            Resources.getSystem().displayMetrics.heightPixels
        createPostDialog!!.setCanceledOnTouchOutside(false)
        dialogBinding.relativeLayoutSend.setOnClickListener {
            val msg: String = dialogBinding.editTextMessage.text.toString().trim()
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
                dialogBinding.chatMessageList.scrollToPosition(mMessageBeanList.size - 1)
                if (mIsPeerToPeerMode) {
                    sendPeerMessage(message)
                } else {
                    sendChannelMessage(message)
                }
            }
            dialogBinding.editTextMessage.setText("")
        }

        dialogBinding.imgGroupVideoCloseFileWebView.setOnClickListener {
            dialogBinding.layoutGroupVideoWebViewFile.visibility = View.GONE
            dialogBinding.layoutGroupVideoChatMessage.visibility = View.VISIBLE
        }

        dialogBinding.imgGroupChatAddFile.setOnClickListener {
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

        dialogBinding.imgCloseChatScreen.setOnClickListener {
            createPostDialog!!.dismiss()
        }
    }

    /**
     * API CALL: login RTM server
     */
    private fun doLogin() {
        try {
            if (rtm!!.isNotEmpty()) {
                mRtmClient!!.login(rtm, preference!![PrefKeys.PREF_USER_ID, "0"]!!,
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
                            displayToast("chat login failed: " + errorInfo.errorDescription)
                            Log.i(
                                TAG,
                                "login failed: " + errorInfo.errorDescription
                            )

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
        mIsPeerToPeerMode = false
        val targetName = appointment!!.doctor_first_name + " " + appointment!!.doctor_last_name
        mChannelName = targetName
        mChannelMemberCount = 1
        dialogBinding.txtTherapistChatName.text = mChannelName
        createAndJoinChannel()
        runOnUiThread {
            val layoutManager = LinearLayoutManager(requireActivity())
            layoutManager.orientation = RecyclerView.VERTICAL
            mMessageAdapter = MessageAdapter(requireActivity(), mMessageBeanList, this, preference!![PrefKeys.PREF_PHOTO, ""]!!, appointment!!.doctor_photo)
            dialogBinding.chatMessageList.layoutManager = layoutManager
            dialogBinding.chatMessageList.adapter = mMessageAdapter
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
        mRtmChannel = mRtmClient!!.createChannel(channelName, myChannelListener)
        if (mRtmChannel == null) {
            showToast(getString(R.string.join_channel_failed))
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
            //mRtmChannel!!.leave(null)
            mRtmChannel!!.release()
            mRtmChannel = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            RtcEngine.destroy()
            if (mIsPeerToPeerMode) {
                MessageUtil.addMessageListBeanList(MessageListBean(mPeerId, mMessageBeanList))
            } else {
                leaveAndReleaseChannel()
            }
            deInitUIandEvent()
            doLogout()
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
                Log.i(TAG, "onMessageReceived account = $account msg = $message")
                val messageBean = MessageBean(targetName, message, false)
                messageBean.setBackground(getMessageColor(account))
                mMessageBeanList.add(messageBean)
                mMessageAdapter!!.notifyItemRangeChanged(mMessageBeanList.size, 1)
                dialogBinding.chatMessageList.scrollToPosition(mMessageBeanList.size - 1)
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
        dialogBinding.txtTherapistChatName.text = mChannelName
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
                sendFileGroupVideoCall(
                    appointment!!.group_appointment!!.id,
                    fileName.split(".")[0],
                    fileType.split("/")[1],
                    "data:$fileType;base64,$document"
                ) { response ->
                    hideProgress()
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
                    dialogBinding.chatMessageList.scrollToPosition(mMessageBeanList.size - 1)
                }
            } catch (e: java.lang.Exception) {
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

    /**
     *
     * Starts/Stops the local video preview
     *
     * Before calling this method, you must:
     * Call the enableVideo method to enable the video.
     *
     * @param start Whether to start/stop the local preview
     * @param view The SurfaceView in which to render the preview
     * @param uid User ID.
     */
    private fun preview(start: Boolean, view: SurfaceView?, uid: Int) {
        if (start) {
            rtcEngine()!!.setupLocalVideo(VideoCanvas(view, VideoCanvas.RENDER_MODE_HIDDEN, uid))
            rtcEngine()!!.startPreview()
        } else {
            rtcEngine()!!.stopPreview()
        }
    }

    /**
     * Allows a user to join a channel.
     *
     * Users in the same channel can talk to each other, and multiple users in the same channel can start a group chat. Users with different App IDs cannot call each other.
     *
     * You must call the leaveChannel method to exit the current call before joining another channel.
     *
     * A successful joinChannel method call triggers the following callbacks:
     *
     * The local client: onJoinChannelSuccess.
     * The remote client: onUserJoined, if the user joining the channel is in the Communication profile, or is a BROADCASTER in the Live Broadcast profile.
     *
     * When the connection between the client and Agora's server is interrupted due to poor
     * network conditions, the SDK tries reconnecting to the server. When the local client
     * successfully rejoins the channel, the SDK triggers the onRejoinChannelSuccess callback
     * on the local client.
     *
     * @param channel The unique channel name for the AgoraRTC session in the string format.
     * @param uid User ID.
     */
    fun joinChannel(channel: String, token: String?, uid: Int) {
        var accessToken = token
        if (TextUtils.equals(accessToken, "") || TextUtils.equals(
                accessToken,
                "<#YOUR ACCESS TOKEN#>"
            )
        ) {
            accessToken = null // default, no token
        }
        rtcEngine()!!.joinChannel(accessToken, channel, "", uid)
        config()!!.mChannel = channel
    }

    /**
     * Allows a user to leave a channel.
     *
     * After joining a channel, the user must call the leaveChannel method to end the call before
     * joining another channel. This method returns 0 if the user leaves the channel and releases
     * all resources related to the call. This method call is asynchronous, and the user has not
     * exited the channel when the method call returns. Once the user leaves the channel,
     * the SDK triggers the onLeaveChannel callback.
     *
     * A successful leaveChannel method call triggers the following callbacks:
     *
     * The local client: onLeaveChannel.
     * The remote client: onUserOffline, if the user leaving the channel is in the
     * Communication channel, or is a BROADCASTER in the Live Broadcast profile.
     *
     * @param channel Channel Name
     */
    fun leaveChannel(channel: String) {
        config()!!.mChannel = null
        rtcEngine()!!.leaveChannel()
        config()!!.reset()
    }

    private fun addEventHandler(handler: AGEventHandler?) {
        application().addEventHandler(handler)
    }

    private fun removeEventHandler(handler: AGEventHandler?) {
        application().remoteEventHandler(handler)
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
        fun newInstance(param1: String, param2: String, param3: String, param4: GetAppointment) =
            GroupVideoCallFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                    putString(ARG_PARAM3, param3)
                    putParcelable(ARG_PARAM4, param4)
                }
            }

        const val TAG = "Screen_group_video_call"
        const val LAYOUT_TYPE_DEFAULT = 0
        const val LAYOUT_TYPE_SMALL = 1
        private val log = LoggerFactory.getLogger(GroupVideoCallFragment::class.java)
        private const val CALL_OPTIONS_REQUEST = 3222
        private const val PICKFILE_REQUEST_CODE = 9
    }

    override fun onItemClick(message: MessageBean?) {
        dialogBinding.layoutGroupVideoChatMessage.visibility = View.GONE
        dialogBinding.layoutGroupVideoWebViewFile.visibility = View.VISIBLE
        val browser = dialogBinding.webViewGroupVideoFile.settings
        browser.javaScriptEnabled = true
        browser.builtInZoomControls = true
        browser.pluginState = WebSettings.PluginState.ON
        browser.loadWithOverviewMode = true
        browser.useWideViewPort = true
        dialogBinding.webViewGroupVideoFile.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                view?.loadUrl(url!!)
                return true
            }
        }
        dialogBinding.webViewGroupVideoFile.webViewClient = object : WebViewClient() {
            private var mProgressDialog: ProgressDialog? = null
            override fun onPageFinished(view: WebView, url: String?) {
                view.settings.loadsImagesAutomatically = true
                dialogBinding.webViewGroupVideoFile.visibility = View.VISIBLE
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
            dialogBinding.webViewGroupVideoFile.loadUrl(
                "http://docs.google.com/gview?embedded=true&url=" + BaseActivity.baseURL.dropLast(
                    5
                ) + mMsgArr[1]
            )
        } else {
            dialogBinding.webViewGroupVideoFile.loadUrl(BaseActivity.baseURL.dropLast(5) + mMsgArr[1])
        }
    }
}