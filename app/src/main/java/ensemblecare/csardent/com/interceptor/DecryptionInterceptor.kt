package ensemblecare.csardent.com.interceptor

import android.text.TextUtils
import android.util.Log
import ensemblecare.csardent.com.crypto.CryptoStrategy
import ensemblecare.csardent.com.utils.Utils
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.json.JSONObject
import java.io.IOException


class DecryptionInterceptor(private val mDecryptionStrategy: CryptoStrategy?) :
    Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (response.isSuccessful) {
            val newResponse = response.newBuilder()
            var contentType = response.header("Content-Type")
            if (TextUtils.isEmpty(contentType)) contentType = "application/json"
            //            InputStream cryptedStream = response.body().byteStream();
            val responseStr = response.body!!.string()
            var decryptedString: String? = null
            if (mDecryptionStrategy != null) {
                try {
                    if (Utils.CONST_ENCRYPT_DECRYPT) {
                        val jsonObj = JSONObject(responseStr)
                        decryptedString = mDecryptionStrategy.decrypt(jsonObj.getString("msg")) + "|" + response.code.toString()
                    } else {
                        decryptedString = responseStr
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                Log.i("Response string => %s", responseStr)
                Log.i("Decrypted BODY=> %s", decryptedString!!)
            } else {
                throw IllegalArgumentException("No decryption strategy!")
            }
            newResponse.body(
                decryptedString
                    .toResponseBody(contentType!!.toMediaTypeOrNull())
            )
            return newResponse.build()
        }
        return response
    }

}