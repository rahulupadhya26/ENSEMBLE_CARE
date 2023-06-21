package ensemblecare.csardent.com.utils

import ensemblecare.csardent.com.R
import ensemblecare.csardent.com.data.ChatRoomMsgListBean
import java.util.Random

object ChatRoomMsgUtil {

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

    private val messageListBeanList: ArrayList<ChatRoomMsgListBean> = ArrayList()

    fun addMessageListBeanList(messageListBean: ChatRoomMsgListBean) {
        messageListBeanList.add(messageListBean)
    }

    // clean up list on logout
    fun cleanMessageListBeanList() {
        messageListBeanList.clear()
    }

    fun getExistMessageListBean(accountOther: String): ChatRoomMsgListBean? {
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
}