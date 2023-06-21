package ensemblecare.csardent.com.interceptor

import android.util.Log
import ensemblecare.csardent.com.crypto.CryptoStrategy
import ensemblecare.csardent.com.crypto.CryptoUtil.requestBodyToString
import ensemblecare.csardent.com.utils.Utils
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException


class EncryptionInterceptor(private val mEncryptionStrategy: CryptoStrategy?) :
    Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        var reqUrl = request.url.toString()
        reqUrl = reqUrl.replace("%2F", "/").replace("%3F", "?").replace("%40", "@")
        val rawBody = request.body
        var encryptedBody: String? = ""
        val mediaType = "application/json".toMediaTypeOrNull()
        if (mEncryptionStrategy != null) {
            try {
                if (rawBody != null) {
                    val rawBodyStr: String = requestBodyToString(rawBody)
                    encryptedBody = if (Utils.CONST_ENCRYPT_DECRYPT) {
                        val json = JSONObject()
                        json.put("msg", mEncryptionStrategy.encrypt(rawBodyStr)).toString()
                        //mEncryptionStrategy.encrypt(json.toString())
                    } else {
                        rawBodyStr
                    }
                    Log.i("Raw body=> %s", rawBodyStr)
                    Log.i("Encrypted BODY=> %s", encryptedBody)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            throw IllegalArgumentException("No encryption strategy!")
        }
        var body: RequestBody? = null
        if (rawBody != null) {
            body = encryptedBody!!.toRequestBody(mediaType)
        }

        //        request = request.newBuilder().header("User-Agent", "Your-App-Name");
        request = if (body != null) {
            request.newBuilder()
                .url(reqUrl)
                .header("Content-Type", "application/json")
                .header("Content-Length", body!!.contentLength().toString())
                .method(request.method, body).build()
        } else {
            request.newBuilder()
                .url(reqUrl)
                .header("Content-Type", "application/json")
                .build()
        }

        return chain.proceed(request)
    }
}