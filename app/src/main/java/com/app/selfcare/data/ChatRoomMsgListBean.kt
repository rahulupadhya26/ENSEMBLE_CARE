package com.app.selfcare.data

class ChatRoomMsgListBean{

    private var accountOther: String? = null
    private var messageBeanList: ArrayList<ChatRoomMsgBean>? = null

    constructor(accountOther: String?, messageBeanList: ArrayList<ChatRoomMsgBean>?) {
        this.accountOther = accountOther
        this.messageBeanList = messageBeanList
    }

    fun getAccountOther(): String? {
        return accountOther
    }

    fun setAccountOther(accountOther: String?) {
        this.accountOther = accountOther
    }

    fun getMessageBeanList(): ArrayList<ChatRoomMsgBean>? {
        return messageBeanList
    }

    fun setMessageBeanList(messageBeanList: ArrayList<ChatRoomMsgBean>?) {
        this.messageBeanList = messageBeanList
    }
}