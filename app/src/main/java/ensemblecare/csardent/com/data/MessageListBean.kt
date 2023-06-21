package ensemblecare.csardent.com.data

import ensemblecare.csardent.com.realTimeMessaging.ChatManager
import io.agora.rtm.RtmMessage

class MessageListBean {

    private var accountOther: String? = null
    private var messageBeanList: ArrayList<MessageBean>? = null

    constructor(accountOther: String?, messageBeanList: ArrayList<MessageBean>?) {
        this.accountOther = accountOther
        this.messageBeanList = messageBeanList
    }

    /**
     * Create message list bean from offline messages
     *
     * @param account     peer user id to find offline messages from
     * @param chatManager chat manager that managers offline message pool
     */
    constructor(account: String?, chatManager: ChatManager) {
        accountOther = account
        messageBeanList = ArrayList()
        val messageList: ArrayList<RtmMessage>? = chatManager.getAllOfflineMessages(account)
        for (m in messageList!!) {
            // All offline messages are from peer users
            val bean = MessageBean(account, m, false)
            messageBeanList!!.add(bean)
        }
    }

    fun getAccountOther(): String? {
        return accountOther
    }

    fun setAccountOther(accountOther: String?) {
        this.accountOther = accountOther
    }

    fun getMessageBeanList(): ArrayList<MessageBean>? {
        return messageBeanList
    }

    fun setMessageBeanList(messageBeanList: ArrayList<MessageBean>?) {
        this.messageBeanList = messageBeanList
    }
}