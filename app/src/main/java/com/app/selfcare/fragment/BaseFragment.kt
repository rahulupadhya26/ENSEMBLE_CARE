package com.app.selfcare.fragment


import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.StrictMode
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.app.selfcare.MainActivity
import com.app.selfcare.R
import com.app.selfcare.controller.IController
import com.app.selfcare.controller.IFragment
import com.app.selfcare.data.Login
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper
import com.app.selfcare.preference.PreferenceHelper.set
import com.app.selfcare.services.RequestInterface
import com.app.selfcare.utils.Utils
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_login.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.*
import java.lang.Exception
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList


abstract class BaseFragment : Fragment(), IFragment, IController {
    // TODO: Rename and change types of parameters
    private var root: View? = null
    var mActivity: MainActivity? = null
    protected var mContext: Context? = null
    private var fragment: BaseFragment? = null
    var mCompositeDisposable: CompositeDisposable = CompositeDisposable()
    protected abstract fun getLayout(): Int
    private var createPostDialog: BottomSheetDialog? = null
    protected val handler: Handler = Handler()
    protected var runnable: Runnable? = null
    var preference: SharedPreferences? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        this.mActivity = mContext as MainActivity
        fragment = this
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        if (root == null)
            root = inflater.inflate(getLayout(), container, false)
        val builder: StrictMode.VmPolicy.Builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        preference = PreferenceHelper.defaultPrefs(requireActivity())
        return root
    }


    override fun addFragment(fragment: Fragment, frameId: Int, fragmentName: String) {
        mActivity!!.addFragment(fragment, frameId, fragmentName)
    }

    override fun replaceFragment(fragment: Fragment, frameId: Int, fragmentName: String) {
        mActivity!!.replaceFragment(fragment, frameId, fragmentName)
    }

    override fun replaceFragmentNoBackStack(
        fragment: Fragment,
        frameId: Int,
        fragmentName: String
    ) {
        mActivity!!.replaceFragmentNoBackStack(fragment, frameId, fragmentName)
    }

    override fun popBackStack() {
        mActivity!!.popBackStack()
    }

    override fun hideKeyboard(view: View) {
        mActivity!!.hideKeyboard(view)
    }

    override fun getHeader(): View {
        return mActivity!!.getHeader()
    }

    override fun getBackButton(): View {
        return mActivity!!.getBackButton()
    }

    override fun getSubTitle(): TextView {
        return mActivity!!.getSubTitle()
    }

    fun displayToast(msg: String) {
        Toast.makeText(mContext!!, msg, Toast.LENGTH_SHORT).show()
    }

    fun displayMsg(title: String, msg: String) {
        val builder = AlertDialog.Builder(mActivity!!)
        //builder.setIcon(R.drawable.work_in_progress)
        builder.setTitle(title)
        builder.setMessage(msg)
        builder.setPositiveButton("OK") { dialog, which ->
            dialog.dismiss()
        }
        builder.setCancelable(false)
        builder.show()
    }

    //Get editText string
    protected fun getText(editText: EditText): String {
        return editText.text.toString().trim()
    }

    //Set the error to editText
    protected fun setEditTextError(editText: EditText, errMsg: String) {
        editText.error = errMsg
        editText.requestFocus()
    }

    //Check the text is valid
    protected fun isValidText(editText: EditText): Boolean {
        val words = editText.text.toString().lowercase().split("\\s+".toRegex())
        val containsBadWords = words.firstOrNull { it in Utils.NSFW_WORDS } != null
        return editText.text.toString().trim().isNotEmpty() && !containsBadWords
    }

    //Check the Mail Id is valid
    protected fun isValidEmail(editText: EditText): Boolean {
        return !TextUtils.isEmpty(getText(editText)) && Patterns.EMAIL_ADDRESS.matcher(
            getText(
                editText
            )
        ).matches()
    }

    protected fun isValidPasswordFormat(password: String): Boolean {
        val passwordREGEX = Pattern.compile("^" +
                "(?=.*[a-z])" +         //at least 1 lower case letter
                "(?=.*[A-Z])" +         //at least 1 upper case letter
                "(?=.*[a-zA-Z])" +      //any letter
                "(?=\\S+$)" +           //no white spaces
                ".{9,}" +               //at least 9 characters
                "$");
        return passwordREGEX.matcher(password).matches()
    }

    override fun showProgress() {
        mActivity!!.showProgress()
    }

    override fun hideProgress() {
        mActivity!!.hideProgress()
    }

    override fun getPhotoPath(): String {
        return mActivity!!.getPhotoPath()
    }

    fun getMultiParts(): List<MultipartBody.Part> {
        val parts = ArrayList<MultipartBody.Part>()
        if (getPhotoPath().isNotEmpty())
            parts.add(prepareImagePart("bitmapImage0", getPhotoPath()))
        return parts
    }

    fun getBitmapMultiParts(bitmap: Bitmap): List<MultipartBody.Part> {
        val parts = ArrayList<MultipartBody.Part>()
        parts.add(prepareBitmapImagePart("bitmapImage0", bitmap))
        return parts
    }

    private fun prepareImagePart(name: String, filePath: String): MultipartBody.Part {
        val file = File(filePath)
        val requestBody = RequestBody.create(
            "image/*".toMediaTypeOrNull(),
            imageToBitmap(BitmapFactory.decodeFile(file.path))
        )
        return MultipartBody.Part.createFormData(name, file.name, requestBody)
    }

    private fun prepareBitmapImagePart(name: String, bitmap: Bitmap): MultipartBody.Part {
        val requestBody = RequestBody.create("image/*".toMediaTypeOrNull(), imageToBitmap(bitmap))
        return MultipartBody.Part.createFormData(
            name,
            System.currentTimeMillis().toString() + ".jpg",
            requestBody
        )
    }

    fun getVideoMultiPart(filePath: String): MultipartBody.Part {
        val file = File(filePath)
        val requestBody = file.asRequestBody("*/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData("video", file.name, requestBody)
    }

    private fun imageToBitmap(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream)
        return stream.toByteArray()
    }

    override fun captureImage() {
        mActivity!!.captureImage()
    }

    protected fun captureImage(imageView: ImageView) {
        mActivity!!.captureImage(imageView)
    }

    override fun uploadPicture() {
        mActivity!!.uploadPicture()
    }

    override fun uploadVideo() {
        mActivity!!.uploadVideo()
    }

    override fun clearTempFormData() {
        mActivity!!.clearTempFormData()
    }

    override fun getBitmapList(): ArrayList<String> {
        return mActivity!!.getBitmapList()
    }

    override fun swipeSliderEnable(flag: Boolean) {
        mActivity!!.swipeSliderEnable(flag)
    }

    override fun onPause() {
        super.onPause()
        if (runnable != null) {
            handler.removeCallbacks(runnable!!)
        }
        if (mCompositeDisposable.size() >= 1) {
            mCompositeDisposable.clear()
        }
        hideProgress()
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap? {
        var bitmap: Bitmap? = null
        if (drawable is BitmapDrawable) {
            val bitmapDrawable = drawable
            if (bitmapDrawable.bitmap != null) {
                return bitmapDrawable.bitmap
            }
        }
        bitmap = if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
            Bitmap.createBitmap(
                1,
                1,
                Bitmap.Config.ARGB_8888
            ) // Single color bitmap will be created of 1x1 pixel
        } else {
            Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
        }
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight())
        drawable.draw(canvas)
        return bitmap
    }

    fun getImageUri(inImage: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path =
            MediaStore.Images.Media.insertImage(mActivity!!.contentResolver, inImage, "Title", null)
        return Uri.parse(path)
    }

    @Synchronized
    fun getEncryptedRequestInterface(): RequestInterface {
        return mActivity!!.getEncryptedRequestInterface()
    }

    @Synchronized
    fun getSyncHealthRequestInterface(syncHealthUrl: String): RequestInterface {
        return mActivity!!.getSyncHealthRequestInterface(syncHealthUrl)
    }

    fun userLogin(
        email: String,
        pass: String,
        myCallback: (result: String?) -> Unit
    ) {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .login(Login(email, pass))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ result ->
                        try {
                            hideProgress()
                            var responseBody = result.string()
                            Log.d("Response Body", responseBody)
                            val respBody = responseBody.split("|")
                            val status = respBody[1]
                            responseBody = respBody[0]
                            saveUserDetails(JSONObject(responseBody), pass)
                            myCallback.invoke("")
                        } catch (e: Exception) {
                            hideProgress()
                            displayToast("Something went wrong.. Please try after sometime")
                        }

                    }, { error ->
                        hideProgress()
                        displayToast("Error ${error.localizedMessage}")
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    private fun saveUserDetails(jsonObj: JSONObject, password: String) {
        preference!![PrefKeys.PREF_USER_ID] = jsonObj.getString("user_id")
        preference!![PrefKeys.PREF_EMAIL] = jsonObj.getString("email")
        preference!![PrefKeys.PREF_PHONE_NO] = jsonObj.getString("phone")
        preference!![PrefKeys.PREF_FNAME] = Utils.firstName
        preference!![PrefKeys.PREF_MNAME] = Utils.middleName
        preference!![PrefKeys.PREF_LNAME] = Utils.lastName
        preference!![PrefKeys.PREF_DOB] = Utils.dob
        preference!![PrefKeys.PREF_SSN] = Utils.ssn
        preference!![PrefKeys.PREF_PATIENT_ID] = jsonObj.getString("patient_id")
        preference!![PrefKeys.PREF_PASS] = password
        preference!![PrefKeys.PREF_IS_LOGGEDIN] = true
        saveTokens(jsonObj.getString("refresh"), jsonObj.getString("access"))
    }

    private fun saveTokens(refreshToken: String, accessToken: String) {
        preference!![PrefKeys.PREF_REFRESH_TOKEN] = refreshToken
        preference!![PrefKeys.PREF_ACCESS_TOKEN] = accessToken
    }
}
