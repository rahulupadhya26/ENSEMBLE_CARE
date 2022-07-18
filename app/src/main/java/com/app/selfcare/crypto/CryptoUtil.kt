package com.app.selfcare.crypto

import okhttp3.RequestBody
import okio.Buffer
import java.io.IOException

object CryptoUtil {
    @Throws(Exception::class)
    fun encrypt(data: String): String {
        return MCrypt.encrypt(data)!!
    }

    @Throws(Exception::class)
    fun decrypt(data: String?): String {
        return MCrypt.decrypt(data!!)!!
    }

    @Throws(IOException::class)
    fun requestBodyToString(requestBody: RequestBody): String {
        val buffer = Buffer()
        requestBody.writeTo(buffer)
        return buffer.readUtf8()
    }
}