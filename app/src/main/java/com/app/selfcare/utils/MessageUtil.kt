package com.app.selfcare.utils

import com.app.selfcare.R
import com.app.selfcare.data.MessageBean
import com.app.selfcare.data.MessageListBean
import io.agora.rtm.RtmMessage
import java.util.*
import kotlin.collections.ArrayList

object MessageUtil {

    val MAX_INPUT_NAME_LENGTH = 64

    val ACTIVITY_RESULT_CONN_ABORTED = 1

    val INTENT_EXTRA_IS_PEER_MODE = "chatMode"
    val INTENT_EXTRA_USER_ID = "userId"
    val INTENT_EXTRA_TARGET_NAME = "targetName"

    var RANDOM = Random()

    val COLOR_ARRAY = intArrayOf(
        R.drawable.shape_circle_black,
        R.drawable.shape_circle_blue,
        R.drawable.shape_circle_pink,
        R.drawable.shape_circle_pink_dark,
        R.drawable.shape_circle_yellow,
        R.drawable.shape_circle_red
    )

    private val messageListBeanList: ArrayList<MessageListBean> = ArrayList()

    fun addMessageListBeanList(messageListBean: MessageListBean) {
        messageListBeanList.add(messageListBean)
    }

    // clean up list on logout
    fun cleanMessageListBeanList() {
        messageListBeanList.clear()
    }

    fun getExistMessageListBean(accountOther: String): MessageListBean? {
        val ret = existMessageListBean(accountOther)
        return if (ret > -1) {
            messageListBeanList.removeAt(ret)
        } else null
    }

    // return existing list position
    private fun existMessageListBean(userId: String): Int {
        val size = messageListBeanList.size
        for (i in 0 until size) {
            if (messageListBeanList[i].getAccountOther().equals(userId)) {
                return i
            }
        }
        return -1
    }

    fun addMessageBean(account: String, msg: RtmMessage?) {
        val messageBean = MessageBean(account, msg, false)
        val ret = existMessageListBean(account)
        if (ret == -1) {
            // account not exist new messagelistbean
            messageBean.setBackground(COLOR_ARRAY[RANDOM.nextInt(COLOR_ARRAY.size)])
            val messageBeanList: ArrayList<MessageBean> = ArrayList()
            messageBeanList.add(messageBean)
            messageListBeanList.add(MessageListBean(account, messageBeanList))
        } else {
            // account exist get messagelistbean
            val bean: MessageListBean = messageListBeanList.removeAt(ret)
            val messageBeanList: ArrayList<MessageBean>? = bean.getMessageBeanList()
            if (messageBeanList!!.isNotEmpty()) {
                messageBean.setBackground(messageBeanList[0].getBackground())
            } else {
                messageBean.setBackground(COLOR_ARRAY[RANDOM.nextInt(COLOR_ARRAY.size)])
            }
            messageBeanList.add(messageBean)
            bean.setMessageBeanList(messageBeanList)
            messageListBeanList.add(bean)
        }
    }
}