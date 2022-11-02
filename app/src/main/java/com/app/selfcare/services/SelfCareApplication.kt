package com.app.selfcare.services

import android.app.Application
import com.app.selfcare.BuildConfig
import com.app.selfcare.realTimeMessaging.ChatManager

class SelfCareApplication: Application() {
    private var mChatManager: ChatManager? = null

    override fun onCreate() {
        super.onCreate()
        instance = this

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
}