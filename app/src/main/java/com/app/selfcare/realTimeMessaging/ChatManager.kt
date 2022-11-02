package com.app.selfcare.realTimeMessaging

import android.content.Context
import android.util.Log
import io.agora.rtm.RtmClient
import io.agora.rtm.RtmClientListener
import io.agora.rtm.RtmMessage
import io.agora.rtm.SendMessageOptions

class ChatManager(private var mContext: Context?, appId: String) {

    private val TAG = ChatManager::class.java.simpleName

    private var appID: String? = appId
    private var mRtmClient: RtmClient? = null
    private var mSendMsgOptions: SendMessageOptions? = null
    private val mListenerList: ArrayList<RtmClientListener> = ArrayList()
    private val mMessagePool: RtmMessagePool = RtmMessagePool()

    fun init() {
        try {
            mRtmClient = RtmClient.createInstance(mContext, appID, object : RtmClientListener {
                override fun onConnectionStateChanged(state: Int, reason: Int) {
                    for (listener in mListenerList) {
                        listener.onConnectionStateChanged(state, reason)
                    }
                }

                override fun onMessageReceived(rtmMessage: RtmMessage, peerId: String) {
                    if (mListenerList.isEmpty()) {
                        // If currently there is no callback to handle this
                        // message, this message is unread yet. Here we also
                        // take it as an offline message.
                        mMessagePool.insertOfflineMessage(rtmMessage, peerId)
                    } else {
                        for (listener in mListenerList) {
                            listener.onMessageReceived(rtmMessage, peerId)
                        }
                    }
                }

                override fun onTokenExpired() {}
                override fun onTokenPrivilegeWillExpire() {}
                override fun onPeersOnlineStatusChanged(status: Map<String, Int>) {}
            })
        } catch (e: Exception) {
            Log.e(TAG, Log.getStackTraceString(e))
            throw RuntimeException(
                """
                NEED TO check rtm sdk init fatal error
                ${Log.getStackTraceString(e)}
                """.trimIndent()
            )
        }

        // Global option, mainly used to determine whether
        // to support offline messages now.
        mSendMsgOptions = SendMessageOptions()
    }

    fun getRtmClient(): RtmClient? {
        return mRtmClient
    }

    fun registerListener(listener: RtmClientListener?) {
        mListenerList.add(listener!!)
    }

    fun unregisterListener(listener: RtmClientListener?) {
        mListenerList.remove(listener)
    }

    fun getSendMessageOptions(): SendMessageOptions? {
        return mSendMsgOptions
    }

    fun getAllOfflineMessages(peerId: String?): ArrayList<RtmMessage>? {
        return mMessagePool.getAllOfflineMessages(peerId!!)
    }

    fun removeAllOfflineMessages(peerId: String?) {
        mMessagePool.removeAllOfflineMessages(peerId!!)
    }

}