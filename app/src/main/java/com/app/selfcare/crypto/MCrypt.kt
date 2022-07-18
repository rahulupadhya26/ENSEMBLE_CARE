package com.app.selfcare.crypto

import android.util.Base64
import com.app.selfcare.BuildConfig
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


object MCrypt {
    private const val iv = BuildConfig.myIVKEY
    private const val CIPHER_NAME = "AES/CBC/PKCS5PADDING"
    private const val CIPHER_KEY_LEN = 32//128 bits
    private var key = BuildConfig.mySPKEY

    fun encrypt(data: String): String? {
        try {
            if (key.length < CIPHER_KEY_LEN) {
                val numPad: Int = CIPHER_KEY_LEN - key.length
                for (i in 0 until numPad) {
                    key += "0" //0 pad to len 16 bytes
                }
            } else if (key.length > CIPHER_KEY_LEN) {
                key = key.substring(0, CIPHER_KEY_LEN) //truncate to 16 bytes
            }
            val initVector =
                IvParameterSpec(iv.toByteArray(charset("ISO-8859-1")))
            val keySpec =
                SecretKeySpec(key.toByteArray(charset("ISO-8859-1")), "AES")
            val cipher =
                Cipher.getInstance(CIPHER_NAME)
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, initVector)
            val encryptedData = cipher.doFinal(data.toByteArray())
            val base64EncryptedData = Base64.encodeToString(encryptedData, Base64.DEFAULT)

            val base64IV = Base64.encodeToString(iv.toByteArray(charset("ISO-8859-1")), Base64.DEFAULT)
            return "$base64EncryptedData:$base64IV"

        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
        }
        return null
    }

    fun decrypt(data: String): String? {
        try {
            if (key.length < CIPHER_KEY_LEN) {
                val numPad: Int = CIPHER_KEY_LEN - key.length
                for (i in 0 until numPad) {
                    key = key.plus("0") //0 pad to len 16 bytes
                }
            } else if (key.length >CIPHER_KEY_LEN) {
                key = key.substring(0, CIPHER_KEY_LEN) //truncate to 16 bytes
            }
            val parts = data.split(":").toTypedArray()
            val iv = IvParameterSpec(
                Base64.decode(
                    parts[1],
                    Base64.DEFAULT
                )
            )
            val skeySpec =
                SecretKeySpec(key.toByteArray(charset("ISO-8859-1")), "AES")
            val cipher =
                Cipher.getInstance(CIPHER_NAME)
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv)
            val decodedEncryptedData =
                Base64.decode(parts[0], Base64.DEFAULT)
            val original = cipher.doFinal(decodedEncryptedData)
            return String(original)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return null
    }

//    fun generateRandomIV16(): String? {
//        val ranGen = SecureRandom()
//        val aesKey = ByteArray(16)
//        ranGen.nextBytes(aesKey)
//        val result = StringBuilder()
//        for (b in aesKey) {
//            result.append(String.format("%02x", b)) //convert to hex
//        }
//        return if (16 > result.toString().length) {
//            result.toString()
//        } else {
//            result.toString().substring(0, 16)
//        }
//    }

}