package com.app.selfcare.services

import android.app.Application
import android.text.TextUtils
import android.util.Log
import com.app.selfcare.BuildConfig
import com.app.selfcare.controller.AGEventHandler
import com.app.selfcare.data.CurrentUserSettings
import com.app.selfcare.data.EngineConfig
import com.app.selfcare.data.MyEngineEventHandler
import com.app.selfcare.realTimeMessaging.ChatManager
import io.agora.rtc2.Constants
import io.agora.rtc2.RtcEngine
import org.slf4j.LoggerFactory

class SelfCareApplication : Application() {
    private var mChatManager: ChatManager? = null
    private val mVideoSettings: CurrentUserSettings = CurrentUserSettings()

    private val log = LoggerFactory.getLogger(this.javaClass)
    private var mRtcEngine: RtcEngine? = null
    private var mConfig: EngineConfig? = null
    private var mEventHandler: MyEngineEventHandler? = null

    fun rtcEngine(): RtcEngine? {
        return mRtcEngine
    }

    fun config(): EngineConfig? {
        return mConfig
    }

    fun userSettings(): CurrentUserSettings? {
        return mVideoSettings
    }

    fun addEventHandler(handler: AGEventHandler?) {
        mEventHandler!!.addEventHandler(handler)
    }

    fun remoteEventHandler(handler: AGEventHandler?) {
        mEventHandler!!.removeEventHandler(handler)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        createRtcEngine()
        mChatManager = ChatManager(this, BuildConfig.appId)
        mChatManager!!.init()
    }

    fun getChatManager(): ChatManager? {
        return mChatManager
    }

    companion object {
        lateinit var instance: SelfCareApplication
            private set
    }

    private fun createRtcEngine() {
        val context = applicationContext
        val appId = BuildConfig.appId
        if (TextUtils.isEmpty(appId)) {
            throw RuntimeException("NEED TO use your App ID, get your own ID at https://dashboard.agora.io/")
        }
        mEventHandler = MyEngineEventHandler()
        mRtcEngine = try {
            // Creates an RtcEngine instance
            RtcEngine.create(context, appId, mEventHandler)
        } catch (e: Exception) {
            log.error(Log.getStackTraceString(e))
            throw RuntimeException(
                """
                    NEED TO check rtc sdk init fatal error
                    ${Log.getStackTraceString(e)}
                    """.trimIndent()
            )
        }

        /*
          Sets the channel profile of the Agora RtcEngine.
          The Agora RtcEngine differentiates channel profiles and applies different optimization
          algorithms accordingly. For example, it prioritizes smoothness and low latency for a
          video call, and prioritizes video quality for a video broadcast.
         */mRtcEngine!!.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION)
        // Enables the video module.
        mRtcEngine!!.enableVideo()
        /*
          Enables the onAudioVolumeIndication callback at a set time interval to report on which
          users are speaking and the speakers' volume.
          Once this method is enabled, the SDK returns the volume indication in the
          onAudioVolumeIndication callback at the set time interval, regardless of whether any user
          is speaking in the channel.
         */
        mRtcEngine!!.enableAudioVolumeIndication(200, 3, false)
        mConfig = EngineConfig()
    }

    override fun onTerminate() {
        super.onTerminate()
    }
}