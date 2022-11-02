package com.app.selfcare.crypto

import android.content.Context
import android.util.Base64
import com.app.selfcare.BuildConfig
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper
import okio.ByteString.Companion.toByteString
import java.io.*
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.*
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import com.app.selfcare.preference.PreferenceHelper.get
import com.app.selfcare.preference.PreferenceHelper.set


object CryptLib {
    /**
     * Encryption mode enumeration
     */
    private enum class EncryptMode {
        ENCRYPT, DECRYPT
    }

    // cipher to be used for encryption and decryption
    private val myCipher: Cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
    val secureRandom = SecureRandom()

    // encryption key and initialization vector
    private val myKey: ByteArray = ByteArray(32)
    private val myIv: ByteArray = ByteArray(16)
    private const val iv = BuildConfig.myIVKEY
    private var key = BuildConfig.mySPKEY

    private const val CIPHER_KEY_LEN = 32//128 bits

    /**
     *
     * @param inputText Text to be encrypted or decrypted
     * @param encryptionKey Encryption key to used for encryption / decryption
     * @param mode specify the mode encryption / decryption
     * @return encrypted or decrypted bytes based on the mode
     * @throws UnsupportedEncodingException
     * @throws InvalidKeyException
     * @throws InvalidAlgorithmParameterException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    @Throws(
        UnsupportedEncodingException::class,
        InvalidKeyException::class,
        InvalidAlgorithmParameterException::class,
        IllegalBlockSizeException::class,
        BadPaddingException::class
    )
    private fun encryptDecrypt(
        inputText: String, encryptionKey: String,
        mode: EncryptMode
    ): ByteArray {
        var len = encryptionKey.toByteArray(Charsets.UTF_8).size
        if (encryptionKey.toByteArray(Charsets.UTF_8).size > myKey.size) len = myKey.size
        var ivlength = iv.toByteArray(Charsets.UTF_8).size
        if (iv.toByteArray(Charsets.UTF_8).size > myIv.size) ivlength = myIv.size
        System.arraycopy(encryptionKey.toByteArray(Charsets.UTF_8), 0, myKey, 0, len)
        System.arraycopy(iv.toByteArray(Charsets.UTF_8), 0, myIv, 0, ivlength)
        val keySpec = SecretKeySpec(myKey, "AES")
        val ivSpec = IvParameterSpec(myIv)

        // encryption
        return if (mode == EncryptMode.ENCRYPT) {
            // Potentially insecure random numbers on Android 4.3 and older. Read for more info.
            // https://android-developers.blogspot.com/2013/08/some-securerandom-thoughts.html
            myCipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec) // Initialize this cipher instance
            myCipher.doFinal(inputText.toByteArray(Charsets.UTF_8))
        } else {
            myCipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
            val decodedValue = Base64.decode(inputText.toByteArray(), Base64.DEFAULT)
            myCipher.doFinal(decodedValue)
        }
    }

    @Throws(Exception::class)
    fun encrypt(
        plainText: String
    ): String {
        val bytes = encryptDecrypt(plainText, sHA256(key), EncryptMode.ENCRYPT)
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    @Throws(Exception::class)
    fun decrypt(cipherText: String): String {
        val bytes = encryptDecrypt(cipherText, sHA256(key), EncryptMode.DECRYPT)
        return String(bytes)
    }


//    @Throws(Exception::class)
//    fun encryptPlainTextWithRandomIV(plainText: String, key: String): String {
//        val bytes = encryptDecrypt(
//            generateRandomIV16() + plainText,
//            SHA256(key, 32),
//            EncryptMode.ENCRYPT,
//            generateRandomIV16()
//        )
//        return Base64.encodeToString(bytes, Base64.DEFAULT)
//    }
//
//    @Throws(Exception::class)
//    fun decryptCipherTextWithRandomIV(
//        cipherText: String,
//        key: String
//    ): String {
//        val bytes = encryptDecrypt(
//            cipherText,
//            SHA256(key, 32),
//            EncryptMode.DECRYPT,
//            generateRandomIV16()
//        )
//        val out = String(bytes)
//        return out.substring(16, out.length)
//    }

    /**
     * Generate IV with 16 bytes
     * @return
     */
    /*fun generateRandomIV16(): String {
        val ranGen = SecureRandom()
        val aesKey = ByteArray(16)
        ranGen.nextBytes(aesKey)
        val result = StringBuilder()
        for (b in aesKey) {
            result.append(String.format("%02x", b)) //convert to hex
        }
        return if (16 > result.toString().length) {
            result.toString()
        } else {
            result.toString().substring(0, 16)
        }
    }*/

    /*private fun SHA256(text: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val encodedhash = digest.digest(
            text.toByteArray(StandardCharsets.UTF_8)
        )
        return bytesToHex(encodedhash).toString()
    }

    private fun bytesToHex(hash: ByteArray): String? {
        val hexString = StringBuilder(2 * hash.size)
        for (i in hash.indices) {
            val hex = Integer.toHexString(0xff and hash[i].toInt())
            if (hex.length == 1) {
                hexString.append('0')
            }
            hexString.append(hex)
        }
        return hexString.toString()
    }*/

    private fun sHA256(text: String): String {
        val resultString: String
        val md = MessageDigest.getInstance("SHA-256")
        md.update(text.toByteArray(charset("UTF-8")))
        val digest = md.digest()
        val result = StringBuilder()
        for (b in digest) {
            result.append(String.format("%02x", b)) //convert to hex
        }
        resultString = if (CIPHER_KEY_LEN > result.toString().length) {
            result.toString()
        } else {
            result.toString().substring(0, CIPHER_KEY_LEN)
        }
        return resultString
    }
}