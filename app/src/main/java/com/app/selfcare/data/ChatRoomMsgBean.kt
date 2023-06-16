package com.app.selfcare.data

import io.agora.rtm.RtmMessage

class ChatRoomMsgBean(
    mAccount: String?,
    mMessage: RtmMessage?,
    mBeSelf: Boolean,
    chatRoomMsgs: ChatRoomMsgs
) {
    private var account = mAccount
    private var message = mMessage
    private var background = 0
    private var beSelf = mBeSelf
    private var roomMsgs = chatRoomMsgs

    fun getAccount(): String? {
        return account
    }

    fun setAccount(account: String?) {
        this.account = account
    }

    fun getMessage(): RtmMessage? {
        return message
    }

    fun setMessage(message: RtmMessage?) {
        this.message = message
    }

    fun getBackground(): Int {
        return background
    }

    fun setBackground(background: Int) {
        this.background = background
    }

    fun isBeSelf(): Boolean {
        return beSelf
    }

    fun setBeSelf(beSelf: Boolean) {
        this.beSelf = beSelf
    }

    fun setChatRoomMsgs(data: ChatRoomMsgs) {
        this.roomMsgs = data
    }

    fun getChatRoomMsgs(): ChatRoomMsgs {
        return roomMsgs
    }
}