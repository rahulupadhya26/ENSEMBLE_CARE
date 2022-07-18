package com.app.selfcare.crypto

class DecryptionImpl : CryptoStrategy {
    override fun encrypt(body: String?): String? {
        return null
    }

    @Throws(Exception::class)
    override fun decrypt(data: String?): String? {
        return CryptoUtil.decrypt(data)
    }
}