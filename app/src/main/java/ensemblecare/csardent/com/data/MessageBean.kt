package ensemblecare.csardent.com.data

import io.agora.rtm.RtmMessage

class MessageBean(mAccount: String?, mMessage: RtmMessage?, mBeSelf: Boolean) {
    private var account =  mAccount
    private var message = mMessage
    private var cacheFile: String? = null
    private var background = 0
    private var beSelf = mBeSelf

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

    fun getCacheFile(): String? {
        return cacheFile
    }

    fun setCacheFile(cacheFile: String?) {
        this.cacheFile = cacheFile
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