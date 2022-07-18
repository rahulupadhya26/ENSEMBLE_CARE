package com.app.selfcare.crypto

import com.app.selfcare.crypto.CryptoStrategy

class EncryptionImpl : CryptoStrategy {
    @Throws(Exception::class)
    override fun encrypt(body: String?): String? {
        return CryptoUtil.encrypt(body!!)
    }

    override fun decrypt(data: String?): String? {
        return null
    }
}