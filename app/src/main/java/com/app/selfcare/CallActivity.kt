package com.app.selfcare

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.adapters.GridVideoViewContainer
import com.app.selfcare.adapters.MessageAdapter
import com.app.selfcare.adapters.SmallVideoViewAdapter
import com.app.selfcare.controller.AGEventHandler
import com.app.selfcare.controller.DuringCallEventHandler
import com.app.selfcare.controller.OnMessageClickListener
import com.app.selfcare.crypto.DecryptionImpl
import com.app.selfcare.crypto.EncryptionImpl
import com.app.selfcare.data.*
import com.app.selfcare.databinding.DialogOnlineChatBinding
import com.app.selfcare.databinding.FragmentGroupVideoCallBinding
import com.app.selfcare.fragment.OnlineChatFragment
import com.app.selfcare.fragment.VideoCallFragment
import com.app.selfcare.interceptor.DecryptionInterceptor
import com.app.selfcare.interceptor.EncryptionInterceptor
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper
import com.app.selfcare.preference.PreferenceHelper.get
import com.app.selfcare.realTimeMessaging.ChatManager
import com.app.selfcare.services.RequestInterface
import com.app.selfcare.services.SelfCareApplication
import com.app.selfcare.utils.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.video.VideoCanvas
import io.agora.rtm.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.slf4j.LoggerFactory
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class CallActivity : BaseClass(), DuringCallEventHandler, OnMessageClickListener {
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
    private var channelName = ""
    private var token = ""
    private var rtm = ""
    private var id = ""
    private var fname = ""
    private var lname = ""
    private var actualStartTime: String? = null
    private var actualEndTime: String? = null
    private var duration: String? = null
    private var time: String? = null
    protected val handler: Handler = Handler()
    protected var runnable: Runnable? = null
    private var mCompositeDisposable: CompositeDisposable = CompositeDisposable()
    var preference: SharedPreferences? = null

    private var createPostDialog: BottomSheetDialog? = null
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
    private var mEndCall = false
    private var mMuted = false

    private var wasRunning = false
    private lateinit var binding: FragmentGroupVideoCallBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //makeActivityContentShownUnderStatusBar()
        binding = FragmentGroupVideoCallBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        preference = PreferenceHelper.defaultPrefs(this)
        val ab = supportActionBar
        if (ab != null) {
            ab.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        }
    }

    override fun initUIandEvent() {
        addEventHandler(this)
        val bundle = intent.extras
        channelName = bundle!!.getString(ConstantApp.ACTION_KEY_CHANNEL_NAME)!!
        token = bundle.getString(ConstantApp.ACTION_KEY_TOKEN)!!
        rtm = bundle.getString(ConstantApp.ACTION_KEY_RTM)!!
        id = bundle.getString(ConstantApp.ACTION_KEY_ID)!!
        fname = bundle.getString(ConstantApp.ACTION_KEY_FNAME)!!
        lname = bundle.getString(ConstantApp.ACTION_KEY_LNAME)!!
        mGridVideoViewContainer =
            findViewById<View>(R.id.grid_video_view_container) as GridVideoViewContainer
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
        val surfaceV = RtcEngine.CreateRendererView(applicationContext)
        preview(true, surfaceV, 0)
        surfaceV.setZOrderOnTop(false)
        surfaceV.setZOrderMediaOverlay(false)
        mUidsList[0] = surfaceV // get first surface view
        mGridVideoViewContainer!!.initViewContainer(
            this,
            0,
            mUidsList,
            mIsLandscape
        ) // first is now full view
        mChatManager = SelfCareApplication.instance.getChatManager()
        mRtmClient = mChatManager!!.getRtmClient()
        if (!Utils.rtmLoggedIn) {
            doLogin()
        }
        joinChannel(channelName, token, 0)
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

    private fun makeActivityContentShownUnderStatusBar() {
        // https://developer.android.com/training/system-ui/status
        // May fail on some kinds of devices
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            val decorView = window.decorView
            val uiOptions = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
            decorView.systemUiVisibility = uiOptions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.statusBarColor = resources.getColor(R.color.white)
            }
        }
    }

    private fun showOrHideStatusBar(hide: Boolean) {
        // May fail on some kinds of devices
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            val decorView = window.decorView
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
        val ab = supportActionBar
        if (ab != null) {
            if (hide) {
                ab.hide()
            } else {
                ab.show()
            }
        }
        /*findViewById<View>(R.id.extra_ops_container).visibility =
            if (hide) View.INVISIBLE else View.VISIBLE*/
        findViewById<View>(R.id.bottom_action_container).visibility =
            if (hide) View.INVISIBLE else View.VISIBLE
    }

    private fun relayoutForVirtualKeyPad(orientation: Int) {
        val virtualKeyHeight = virtualKeyHeight()
        val eopsContainer = findViewById<LinearLayout>(R.id.extra_ops_container)
        val eofmp = eopsContainer.layoutParams as ViewGroup.MarginLayoutParams
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            eofmp.rightMargin = virtualKeyHeight
            eofmp.leftMargin = 0
        } else {
            eofmp.leftMargin = 0
            eofmp.rightMargin = 0
        }
        val bottomContainer = findViewById<LinearLayout>(R.id.bottom_container)
        val fmp = bottomContainer.layoutParams as ViewGroup.MarginLayoutParams
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            fmp.bottomMargin = 0
            fmp.rightMargin = virtualKeyHeight
            fmp.leftMargin = 0
        } else {
            fmp.bottomMargin = virtualKeyHeight
            fmp.leftMargin = 0
            fmp.rightMargin = 0
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CALL_OPTIONS_REQUEST) {
            /*RecyclerView msgListView = (RecyclerView) findViewById(R.id.msg_list);
            msgListView.setVisibility(Constant.DEBUG_INFO_ENABLED ? View.VISIBLE : View.INVISIBLE);*/
        }
    }

    fun onClickHideIME(view: View) {
        log.debug("onClickHideIME $view")

        /*closeIME(findViewById(R.id.msg_content));
        findViewById(R.id.msg_input_container).setVisibility(View.GONE);*/findViewById<View>(R.id.bottom_action_container).visibility =
            View.VISIBLE
    }

    private fun optional() {
        volumeControlStream = AudioManager.STREAM_VOICE_CALL
    }

    private fun optionalDestroy() {}

    // save the new value
    private val videoEncResolutionIndex: Int
        private get() {
            val pref = PreferenceManager.getDefaultSharedPreferences(
                applicationContext
            )
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

    // save the new value
    private val videoEncFpsIndex: Int
        private get() {
            val pref = PreferenceManager.getDefaultSharedPreferences(
                applicationContext
            )
            var videoEncFpsIndex = pref.getInt(
                ConstantApp.PrefManager.PREF_PROPERTY_VIDEO_ENC_FPS,
                ConstantApp.DEFAULT_VIDEO_ENC_FPS_IDX
            )
            if (videoEncFpsIndex > ConstantApp.VIDEO_FPS.size - 1) {
                videoEncFpsIndex = ConstantApp.DEFAULT_VIDEO_ENC_FPS_IDX

                // save the new value
                val editor = pref.edit()
                editor.putInt(ConstantApp.PrefManager.PREF_PROPERTY_VIDEO_ENC_FPS, videoEncFpsIndex)
                editor.apply()
            }
            return videoEncFpsIndex
        }

    fun onSwitchCameraClicked(view: View?) {
        val rtcEngine = rtcEngine()
        // Switches between front and rear cameras.
        rtcEngine!!.switchCamera()
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

    override fun deInitUIandEvent() {
        optionalDestroy()
        doLeaveChannel()
        removeEventHandler(this)
        mUidsList.clear()
    }

    private fun doLeaveChannel() {
        leaveChannel(config()!!.mChannel)
        preview(false, null, 0)
    }

    fun onHangupClicked(view: View) {
        log.info("onHangupClicked $view")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirmation")
        builder.setMessage("Do you wish to end the call?")
        builder.setPositiveButton("Yes") { dialog, which ->
            val date: Calendar = Calendar.getInstance()
            val nextMonthDate = SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(date.time)
            val endTime = DateUtils(nextMonthDate)
            actualEndTime = endTime.getTime()
            duration = time
            sendApptStatus(id, actualStartTime!!, actualEndTime!!, duration!!) {
                finish()
            }
        }
        builder.setNegativeButton("No") { dialog, which ->
            dialog.dismiss()
        }
        builder.setCancelable(false)
        builder.show()
    }

    override fun onPause() {
        super.onPause()
        if (runnable != null) {
            handler.removeCallbacks(runnable!!)
        }
    }

    private fun sendApptStatus(
        apptId: String,
        actualStartTime: String,
        actualEndTime: String,
        duration: String,
        myCallback: (result: String?) -> Unit
    ) {
        binding.layoutProgress.visibility = View.VISIBLE
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .sendApptStatus(
                        "PI0060",
                        AppointmentStatus(
                            apptId,
                            actualStartTime,
                            actualEndTime,
                            duration,
                            status = 4
                        ), "Bearer " + preference!![PrefKeys.PREF_ACCESS_TOKEN, ""]!!
                    )
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ result ->
                        try {
                            binding.layoutProgress.visibility = View.GONE
                            var responseBody = result.string()
                            Log.d("Response Body", responseBody)
                            val respBody = responseBody.split("|")
                            val status = respBody[1]
                            responseBody = respBody[0]
                            if (status == "202" || status == "200") {
                                myCallback.invoke("Success")
                            }
                        } catch (e: Exception) {
                            binding.layoutProgress.visibility = View.GONE
                            Toast.makeText(
                                this,
                                "Something went wrong.. Please try after sometime",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }, { error ->
                        binding.layoutProgress.visibility = View.GONE
                        //displayToast("Error ${error.localizedMessage}")
                        if ((error as HttpException).code() == 401) {

                        } else {
                            finish()
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    fun getEncryptedRequestInterface(): RequestInterface {
        val httpClient = getHttpClient()
        //Encryption Interceptor
        val encryptionInterceptor = EncryptionInterceptor(EncryptionImpl())
        //Decryption Interceptor
        val decryptionInterceptor = DecryptionInterceptor(DecryptionImpl())
        httpClient.addInterceptor(encryptionInterceptor)
        httpClient.addInterceptor(decryptionInterceptor)
        return Retrofit.Builder()
            .baseUrl(BaseActivity.baseURL).client(httpClient.build())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(RequestInterface::class.java)
    }

    private fun getHttpClient(): OkHttpClient.Builder {
        val httpClient = OkHttpClient.Builder()
        httpClient.connectTimeout(180, TimeUnit.SECONDS)
        httpClient.callTimeout(4, TimeUnit.MINUTES)
        httpClient.readTimeout(180, TimeUnit.SECONDS)
        httpClient.writeTimeout(1000, TimeUnit.SECONDS)
        val logging = HttpLoggingInterceptor()
        // set your desired log level
        logging.level = HttpLoggingInterceptor.Level.BODY
        // add logging as last interceptor
        httpClient.addInterceptor(logging)  // <-- this is the important line!
        return httpClient
    }

    fun onVideoMuteClicked(view: View) {
        log.info("onVoiceChatClicked " + view + " " + mUidsList.size + " video_status: " + mVideoMuted + " audio_status: " + mAudioMuted)
        if (mUidsList.size == 0) {
            return
        }
        val surfaceV = localView
        var parent: ViewParent?
        if (surfaceV == null || surfaceV.parent.also { parent = it } == null) {
            log.warn("onVoiceChatClicked $view $surfaceV")
            return
        }
        val rtcEngine = rtcEngine()
        mVideoMuted = !mVideoMuted
        if (mVideoMuted) {
            rtcEngine!!.disableVideo()
        } else {
            rtcEngine!!.enableVideo()
        }
        val iv = view as ImageView
        iv.setImageResource(if (mVideoMuted) R.mipmap.ic_video_disabled else R.mipmap.ic_video_enabled)
        hideLocalView(mVideoMuted)
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

    fun onVoiceMuteClicked(view: View) {
        log.info("onVoiceMuteClicked " + view + " " + mUidsList.size + " video_status: " + mVideoMuted + " audio_status: " + mAudioMuted)
        if (mUidsList.size == 0) {
            return
        }
        mAudioMuted = !mAudioMuted
        val rtcEngine = rtcEngine()
        rtcEngine!!.muteLocalAudioStream(mAudioMuted)
        val iv = view as ImageView
        iv.setImageResource(if (mAudioMuted) R.drawable.btn_mute else R.drawable.btn_unmute)
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
            if (isFinishing) {
                return@Runnable
            }
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
            val surfaceV = RtcEngine.CreateRendererView(applicationContext)
            mUidsList[uid] = surfaceV
            val useDefaultLayout = mLayoutType == LAYOUT_TYPE_DEFAULT
            surfaceV.setZOrderOnTop(true)
            surfaceV.setZOrderMediaOverlay(true)

            /*
                      Initializes the video view of a remote user.
                      This method initializes the video view of a remote stream on the local device. It affects only the video view that the local user sees.
                      Call this method to bind the remote video stream to a video view and to set the rendering and mirror modes of the video view.
                     */rtcEngine()!!.setupRemoteVideo(
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
            if (isFinishing) {
                return@Runnable
            }
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
            if (isFinishing) {
                return@Runnable
            }
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
        mGridVideoViewContainer!!.initViewContainer(this, config()!!.mUid, mUidsList, mIsLandscape)
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
        mGridVideoViewContainer!!.initViewContainer(this, bigBgUid, slice, mIsLandscape)
        bindToSmallVideoView(bigBgUid)
        mLayoutType = LAYOUT_TYPE_SMALL
        requestRemoteStreamType(mUidsList.size)
    }

    private fun bindToSmallVideoView(exceptUid: Int) {
        if (mSmallVideoViewDock == null) {
            val stub = findViewById<View>(R.id.small_video_view_dock) as ViewStub
            mSmallVideoViewDock = stub.inflate() as RelativeLayout
        }
        val twoWayVideoCall = mUidsList.size == 2
        val recycler = findViewById<View>(R.id.small_video_view_container) as RecyclerView
        var create = false
        if (mSmallVideoViewAdapter == null) {
            create = true
            mSmallVideoViewAdapter =
                SmallVideoViewAdapter(this, config()!!.mUid, exceptUid, mUidsList)
            mSmallVideoViewAdapter!!.setHasStableIds(true)
        }
        recycler.setHasFixedSize(true)
        log.debug("bindToSmallVideoView " + twoWayVideoCall + " " + (exceptUid and 0xFFFFFFFFL.toInt()))
        if (twoWayVideoCall) {
            recycler.layoutManager =
                RtlLinearLayoutManager(applicationContext, RtlLinearLayoutManager.HORIZONTAL, false)
        } else {
            recycler.layoutManager = LinearLayoutManager(
                applicationContext, LinearLayoutManager.HORIZONTAL, false
            )
        }
        recycler.addItemDecoration(SmallVideoViewDecoration())
        recycler.adapter = mSmallVideoViewAdapter
        recycler.addOnItemTouchListener(
            RecyclerItemClickListener(
                baseContext,
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

    fun notifyHeadsetPlugged(routing: Int) {
        log.info("notifyHeadsetPlugged $routing $mVideoMuted")
        mAudioRouting = routing
        val iv = findViewById<View>(R.id.switch_speaker_id) as ImageView
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

    fun onClickOfChat(view: View) {
        if (createPostDialog != null) {
            createPostDialog!!.show()
        } else {
            displayToast("Cannot open chat screen.")
        }
    }

    private lateinit var dialogBinding:DialogOnlineChatBinding
    fun openOnlineChatWindow() {
        createPostDialog = BottomSheetDialog(this)
        dialogBinding = DialogOnlineChatBinding.inflate(layoutInflater)
        val onlineChatView = dialogBinding.root
        /*onlineChatView = layoutInflater.inflate(
            R.layout.dialog_online_chat, null
        )*/
        //onlineChatView!!.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
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

        dialogBinding.imgCloseChatScreen.setOnClickListener {
            createPostDialog!!.dismiss()
        }
    }

    /**
     * API CALL: login RTM server
     */
    private fun doLogin() {
        try {
            if (rtm.isNotEmpty()) {
                mRtmClient!!.login(rtm, preference!![PrefKeys.PREF_EMAIL, ""],
                    object : ResultCallback<Void?> {
                        override fun onSuccess(responseInfo: Void?) {
                            Utils.rtmLoggedIn = true
                            Log.i(VideoCallFragment.TAG, "login success")
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
                            Log.i(
                                VideoCallFragment.TAG,
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
        mClientListener = rtmClientListener
        mChatManager!!.registerListener(mClientListener)
        mIsPeerToPeerMode = false
        val targetName = "$fname $lname"
        if (mIsPeerToPeerMode) {
            mPeerId = targetName
            dialogBinding.txtTherapistChatName.text = mPeerId

            // load history chat records
            val messageListBean = MessageUtil.getExistMessageListBean(mPeerId)
            if (messageListBean != null) {
                mMessageBeanList.addAll(messageListBean.getMessageBeanList()!!)
            }

            // load offline messages since last chat with this peer.
            // Then clear cached offline messages from message pool
            // since they are already consumed.
            val peerName =
                fname.take(1) + "" + lname.take(1)
            val offlineMessageBean = MessageListBean(peerName, mChatManager!!)
            mMessageBeanList.addAll(offlineMessageBean.getMessageBeanList()!!)
            mChatManager!!.removeAllOfflineMessages(mPeerId)
        } else {
            mChannelName = targetName
            mChannelMemberCount = 1
            dialogBinding.txtTherapistChatName.text = mChannelName
            /*onlineChatView!!.txtTherapistChatName.text = MessageFormat.format(
                "{0}({1})",
                mChannelName,
                mChannelMemberCount
            )*/
            createAndJoinChannel()
        }
        runOnUiThread {
            val layoutManager = LinearLayoutManager(this)
            layoutManager.orientation = RecyclerView.VERTICAL
            mMessageAdapter = MessageAdapter(this, mMessageBeanList, this)
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
            finish()
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
                    finish()
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
                        setResult(MessageUtil.ACTIVITY_RESULT_CONN_ABORTED)
                        finish()
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
                    dialogBinding.chatMessageList.scrollToPosition(mMessageBeanList.size - 1)
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
                val targetName = fname.take(1) + "" + lname.take(1)
                val account = fromMember.userId
                Log.i(OnlineChatFragment.TAG, "onMessageReceived account = $account msg = $message")
                val messageBean = MessageBean(targetName, message, false)
                messageBean.setBackground(getMessageColor(account))
                mMessageBeanList.add(messageBean)
                mMessageAdapter!!.notifyItemRangeChanged(mMessageBeanList.size, 1)
                dialogBinding.chatMessageList.scrollToPosition(mMessageBeanList.size - 1)
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
        dialogBinding.txtTherapistChatName.text = mChannelName
    }

    private fun showToast(text: String) {
        runOnUiThread {
            Toast.makeText(
                this,
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

    fun displayToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val LAYOUT_TYPE_DEFAULT = 0
        const val LAYOUT_TYPE_SMALL = 1
        private val log = LoggerFactory.getLogger(CallActivity::class.java)
        private const val CALL_OPTIONS_REQUEST = 3222
    }

    override fun onItemClick(message: MessageBean?) {
        TODO("Not yet implemented")
    }
}