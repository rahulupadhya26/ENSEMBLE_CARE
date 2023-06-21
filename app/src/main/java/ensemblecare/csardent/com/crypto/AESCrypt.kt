package ensemblecare.csardent.com.crypto

import android.util.Base64
import android.util.Log
import ensemblecare.csardent.com.BuildConfig
import java.io.UnsupportedEncodingException
import java.security.GeneralSecurityException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


/*
* https://github.com/scottyab/AESCrypt-Android
*/
object AESCrypt {

    private val TAG = "AESCrypt"

    //AESCrypt-ObjC uses CBC and PKCS7Padding
    private val AES_MODE = "AES/CBC/PKCS7Padding"
    private val CHARSET = "UTF-8"
    private var password = BuildConfig.mySPKEY

    //AESCrypt-ObjC uses SHA-256 (and so a 256-bit key)
    private val HASH_ALGORITHM = "SHA-256"

    //AESCrypt-ObjC uses blank IV (not the best security, but the aim here is compatibility)
    private var ivBytes = byteArrayOf(
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00
    )

    //togglable log option (please turn off in live!)
    private var DEBUG_LOG_ENABLED = true

    /**
     * Generates SHA256 hash of the password which is used as key
     *
     * @param password used to generated key
     * @return SHA256 of the password
     */
    @Throws(NoSuchAlgorithmException::class, UnsupportedEncodingException::class)
    private fun generateKey(password: String): SecretKeySpec? {
        val digest: MessageDigest = MessageDigest.getInstance(HASH_ALGORITHM)
        val bytes = password.toByteArray(charset("UTF-8"))
        val key: ByteArray = digest.digest(bytes)
        log("SHA-256 key ", key)
        return SecretKeySpec(key, "AES")
    }

    /**
     * Encrypt and encode message using 256-bit AES with key generated from password.
     *
     *
     * @param password used to generated key
     * @param message the thing you want to encrypt assumed String UTF-8
     * @return Base64 encoded CipherText
     * @throws GeneralSecurityException if problems occur during encryption
     */
    @Throws(GeneralSecurityException::class)
    fun encrypt(message: String): String {
        return try {
            val key = generateKey(password)
            log("message", message)
            val cipherText: ByteArray =
                encrypt(key, ivBytes, message.toByteArray(charset(CHARSET)))
            //NO_WRAP is important as was getting \n at the end
            val encoded: String = Base64.encodeToString(ivBytes + cipherText, Base64.NO_WRAP)
            log("Base64.NO_WRAP", encoded)
            encoded
        } catch (e: UnsupportedEncodingException) {
            if (DEBUG_LOG_ENABLED) Log.e(TAG, "UnsupportedEncodingException ", e)
            throw GeneralSecurityException(e)
        }
    }

    /**
     * More flexible AES encrypt that doesn't encode
     * @param key AES key typically 128, 192 or 256 bit
     * @param iv Initiation Vector
     * @param message in bytes (assumed it's already been decoded)
     * @return Encrypted cipher text (not encoded)
     * @throws GeneralSecurityException if something goes wrong during encryption
     */
    @Throws(GeneralSecurityException::class)
    private fun encrypt(key: SecretKeySpec?, iv: ByteArray?, message: ByteArray?): ByteArray {
        val cipher: Cipher = Cipher.getInstance(AES_MODE)
        val ivSpec = IvParameterSpec(iv)
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec)
        val cipherText: ByteArray = cipher.doFinal(message)
        log("cipherText", cipherText)
        return cipherText
    }

    /**
     * Decrypt and decode ciphertext using 256-bit AES with key generated from password
     *
     * @param password used to generated key
     * @param base64EncodedCipherText the encrpyted message encoded with base64
     * @return message in Plain text (String UTF-8)
     * @throws GeneralSecurityException if there's an issue decrypting
     */
    @Throws(GeneralSecurityException::class)
    fun decrypt(base64EncodedCipherText: String?): String? {
        return try {
            val key = generateKey(password)
            log("base64EncodedCipherText", base64EncodedCipherText!!)
            val decodedCipherText = Base64.decode(base64EncodedCipherText, Base64.NO_WRAP)
            log("decodedCipherText", decodedCipherText)
            ivBytes = ByteArray(16)
            val decryptMsg = ByteArray(decodedCipherText.size - 16)
            System.arraycopy(decodedCipherText, 0, ivBytes, 0, 16)
            System.arraycopy(decodedCipherText, 16, decryptMsg, 0, decodedCipherText.size - 16)
            //decodedCipherText.copyOfRange(15, decodedCipherText.size - 1)
            val decryptedBytes = decrypt(key, ivBytes, decryptMsg)
            log("decryptedBytes", decryptedBytes)
            val message = String(decryptedBytes, Charsets.UTF_8)
            log("message", message)
            message
        } catch (e: UnsupportedEncodingException) {
            if (DEBUG_LOG_ENABLED) Log.e(TAG, "UnsupportedEncodingException ", e)
            throw GeneralSecurityException(e)
        }
    }


    /**
     * More flexible AES decrypt that doesn't encode
     *
     * @param key AES key typically 128, 192 or 256 bit
     * @param iv Initiation Vector
     * @param decodedCipherText in bytes (assumed it's already been decoded)
     * @return Decrypted message cipher text (not encoded)
     * @throws GeneralSecurityException if something goes wrong during encryption
     */
    @Throws(GeneralSecurityException::class)
    fun decrypt(key: SecretKeySpec?, iv: ByteArray?, decodedCipherText: ByteArray?): ByteArray {
        val cipher = Cipher.getInstance(AES_MODE)
        val ivSpec = IvParameterSpec(iv)
        cipher.init(Cipher.DECRYPT_MODE, key, ivSpec)
        val decryptedBytes = cipher.doFinal(decodedCipherText)
        log("decryptedBytes", decryptedBytes)
        return decryptedBytes
    }

    private fun log(what: String, bytes: ByteArray) {
        if (DEBUG_LOG_ENABLED) Log.d(TAG, what + "[" + bytes.size + "] [" + bytesToHex(bytes) + "]")
    }

    private fun log(what: String, value: String) {
        if (DEBUG_LOG_ENABLED) Log.d(TAG, what + "[" + value.length + "] [" + value + "]")
    }

    /**
     * Converts byte array to hexidecimal useful for logging and fault finding
     * @param bytes
     * @return
     */
    private fun bytesToHex(bytes: ByteArray): String {
        val result = StringBuilder()
        for (b in bytes) {
            result.append(String.format("%02x", b)) //convert to hex
        }
        return String(result)
    }
}