package ensemblecare.csardent.com.crypto

import android.util.Base64
import ensemblecare.csardent.com.BuildConfig
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


object MCrypt {
    private const val iv = BuildConfig.myIVKEY
    private const val CIPHER_NAME = "AES/CBC/PKCS5PADDING"
    private const val CIPHER_KEY_LEN = 16//128 bits
    private var key = BuildConfig.mySPKEY
    private const val SALT = "ssshhhhhhhhhhh!!!!"

    fun encrypt(data: String): String? {
        try {
            key = sHA256(key)
            /*if (key.length < CIPHER_KEY_LEN) {
                val numPad: Int = CIPHER_KEY_LEN - key.length
                for (i in 0 until numPad) {
                    key += "0" //0 pad to len 16 blytes
                }
            } else if (key.length > CIPHER_KEY_LEN) {
                key = key.substring(0, CIPHER_KEY_LEN) //truncate to 16 bytes
            }*/
            val initVector = IvParameterSpec(iv.toByteArray(Charsets.UTF_8))
            val keySpec = SecretKeySpec(hexToByteArray(key), "AES")
            val cipher = Cipher.getInstance(CIPHER_NAME)
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, initVector)
            val encryptedData = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
            //val base64IV = Base64.encodeToString(iv.toByteArray(), Base64.DEFAULT)
            return Base64.encodeToString((iv).toByteArray(Charsets.UTF_8) + encryptedData, Base64.DEFAULT)

        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return null
    }

    fun decrypt(data: String): String? {
        try {
            key = sHA256(key)
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
                    parts[0],
                    Base64.DEFAULT
                )
            )
            val skeySpec =
                SecretKeySpec(key.toByteArray(Charsets.UTF_8), "AES")
            val cipher =
                Cipher.getInstance(CIPHER_NAME)
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv)
            val decodedEncryptedData =
                Base64.decode(parts[1], Base64.DEFAULT)
            val original = cipher.doFinal(decodedEncryptedData)
            return String(original)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return null
    }

    private fun sHA256(text: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        //md.update(text.toByteArray())
        val digest = md.digest(text.toByteArray(Charsets.UTF_8))
        val result = StringBuilder()
        for (b in digest) {
            result.append(String.format("%02x", b)) //convert to hex
        }
        return result.toString()
    }

    private fun hexToByteArray(hex: String): ByteArray? {
        val hexString = if (hex.length % 2 != 0) "0$hex" else hex
        val b = ByteArray(hexString.length / 2)
        for (i in b.indices) {
            val index = i * 2
            val v = hexString.substring(index, index + 2).toInt(16)
            b[i] = v.toByte()
        }
        return b
    }

    private fun byteArrayToHexString(bytes: ByteArray): String {
        val result = StringBuilder()
        for (b in bytes) {
            result.append(String.format("%02x", b)) //convert to hex
        }
        return String(result)
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