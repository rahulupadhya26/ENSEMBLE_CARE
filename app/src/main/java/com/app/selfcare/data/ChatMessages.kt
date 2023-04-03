package com.app.selfcare.data

import io.agora.chat.ChatMessage

class ChatMessages(mType: ChatMessage.Type, mMessage: String, mFrom: String, mBeSelf: Boolean) {
    private var type = mType
    private var message = mMessage
    private var from = mFrom
    private var background = 0
    private var beSelf = mBeSelf

    fun getType(): ChatMessage.Type {
        return type
    }

    fun setType(mType: ChatMessage.Type) {
        this.type = mType
    }

    fun getMessage(): String {
        return message
    }

    fun setMessage(mMessage: String) {
        this.message = mMessage
    }

    fun getFrom(): String {
        return from
    }

    fun setFrom(mFrom: String) {
        this.from = mFrom
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
}