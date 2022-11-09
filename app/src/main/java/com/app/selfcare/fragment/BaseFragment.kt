package com.app.selfcare.fragment


import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.StrictMode
import android.provider.MediaStore
import android.provider.Settings
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.app.selfcare.MainActivity
import com.app.selfcare.R
import com.app.selfcare.controller.IController
import com.app.selfcare.controller.IFragment
import com.app.selfcare.crypto.DecryptionImpl
import com.app.selfcare.data.AppointmentReq
import com.app.selfcare.data.DataId
import com.app.selfcare.data.DeviceId
import com.app.selfcare.data.Login
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper
import com.app.selfcare.preference.PreferenceHelper.get
import com.app.selfcare.preference.PreferenceHelper.set
import com.app.selfcare.services.RequestInterface
import com.app.selfcare.utils.Utils
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.display_image.*
import kotlinx.android.synthetic.main.display_image.view.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.HttpException
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.regex.Pattern


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
        val passwordREGEX = Pattern.compile(
            "^" +
                    "(?=.*[a-z])" +         //at least 1 lower case letter
                    "(?=.*[A-Z])" +         //at least 1 upper case letter
                    "(?=.*[a-zA-Z])" +      //any letter
                    "(?=\\S+$)" +           //no white spaces
                    ".{9,}" +               //at least 9 characters
                    "$"
        );
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

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun captureImage() {
        mActivity!!.captureImage()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    protected fun captureImage(imageView: ImageView) {
        mActivity!!.captureImage(imageView)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun uploadPicture() {
        mActivity!!.uploadPicture()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
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

    fun setBottomNavigation(navigationView: BottomNavigationView?) {
        mActivity!!.setBottomNavigation(navigationView)
    }

    fun getBottomNavigation(): BottomNavigationView? {
        return mActivity!!.getBottomNavigation()
    }

    override fun setBottomMenu(id: Int) {
        mActivity!!.setBottomMenu(id)
    }

    fun redirectToHome() {
        setBottomMenu(R.id.navigation_home)
        popBackStack()
    }

    @Synchronized
    fun getEncryptedRequestInterface(): RequestInterface {
        return mActivity!!.getEncryptedRequestInterface()
    }

    @Synchronized
    fun getSyncHealthRequestInterface(syncHealthUrl: String): RequestInterface {
        return mActivity!!.getSyncHealthRequestInterface(syncHealthUrl)
    }

    fun getAccessToken(): String {
        return "Bearer " + preference!![PrefKeys.PREF_ACCESS_TOKEN, ""]!!
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
                        if ((error as HttpException).code() == 400) {
                            displayErrorMsg(error)
                        } else {
                            displayToast("Something went wrong.. Please try after sometime")
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    private fun saveUserDetails(jsonObj: JSONObject, password: String) {
        preference!![PrefKeys.PREF_USER_ID] = jsonObj.getJSONObject("patient").getString("user")
        preference!![PrefKeys.PREF_EMAIL] = jsonObj.getString("email")
        preference!![PrefKeys.PREF_PHONE_NO] = jsonObj.getString("phone")
        preference!![PrefKeys.PREF_FNAME] = jsonObj.getJSONObject("patient").getString("first_name")
        preference!![PrefKeys.PREF_MNAME] =
            jsonObj.getJSONObject("patient").getString("middle_name")
        preference!![PrefKeys.PREF_LNAME] = jsonObj.getJSONObject("patient").getString("last_name")
        preference!![PrefKeys.PREF_DOB] = jsonObj.getJSONObject("patient").getString("dob")
        preference!![PrefKeys.PREF_SSN] = Utils.ssn
        preference!![PrefKeys.PREF_PATIENT_ID] =
            jsonObj.getJSONObject("patient").getString("patient_id")
        preference!![PrefKeys.PREF_SELECTED_PLAN] =
            jsonObj.getJSONObject("patient").getString("selected_plan")
        preference!![PrefKeys.PREF_SEVERITY_SCORE] =
            jsonObj.getJSONObject("patient").getString("patient_severity_score")
        preference!![PrefKeys.PREF_SELECTED_SERVICE] =
            jsonObj.getJSONObject("patient").getString("selected_service")
        preference!![PrefKeys.PREF_SELECTED_DISORDER] =
            jsonObj.getJSONObject("patient").getString("selected_dissorder")
        preference!![PrefKeys.PREF_PASS] = password
        preference!![PrefKeys.PREF_IS_LOGGEDIN] = true
        saveTokens(jsonObj.getString("refresh"), jsonObj.getString("access"))
    }

    private fun saveTokens(refreshToken: String, accessToken: String) {
        preference!![PrefKeys.PREF_REFRESH_TOKEN] = refreshToken
        preference!![PrefKeys.PREF_ACCESS_TOKEN] = accessToken
    }

    fun displayErrorMsg(error: Throwable) {
        val errorMsg = (error as HttpException).response()!!.errorBody()!!.string()
        if (errorMsg.isNotEmpty()) {
            try {
                val errorJsonObj = JSONObject(errorMsg)
                val encryptedErrMsg = errorJsonObj.getString("msg")
                val decryptedErrMsg = DecryptionImpl().decrypt(encryptedErrMsg)!!
                try {
                    val decryptedErrorJsonObj = JSONObject(decryptedErrMsg)
                    val errList = ArrayList<String>()
                    val iterator: Iterator<*> = decryptedErrorJsonObj.keys()
                    while (iterator.hasNext()) {
                        val key = iterator.next() as String
                        val array: JSONArray = decryptedErrorJsonObj.optJSONArray(key)!!
                        for (i in 0 until array.length()) {
                            errList.add(array[i].toString())
                        }
                    }
                    if (errList.isNotEmpty()) {

                        val errorStr = errList.joinToString("\n")
                        displayToast(errorStr)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    displayToast(decryptedErrMsg)
                }
            } catch (e: Exception) {
                displayToast(errorMsg)
            }
        }
    }

    fun displayAfterLoginErrorMsg(error: Throwable) {
        try {
            val errorMsg = (error as HttpException).response()!!.errorBody()!!.string()
            if (errorMsg.isNotEmpty()) {
                val errorJsonObj = JSONObject(errorMsg)
                val encryptedErrMsg = errorJsonObj.getString("msg")
                val decryptedErrMsg = DecryptionImpl().decrypt(encryptedErrMsg)!!
                displayToast(decryptedErrMsg)
            } else {
                displayToast("Something went wrong.. Please try after sometime")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            displayToast("Something went wrong.. Please try after sometime")
        }
    }

    @JvmName("hideKeyboard1")
    fun View.hideKeyboard() {
        val inputManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(windowToken, 0)
    }

    fun deleteData(tableId: String, dataId: Int, myCallback: (result: String?) -> Unit) {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .deleteData(tableId, DataId(dataId), getAccessToken())
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
                            if (status == "200") {
                                myCallback.invoke("Success")
                            } else {
                                displayToast("Something went wrong.. Please try after sometime")
                            }
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

    fun callAppointmentApi(
        apptId: String,
        bookingDate: String,
        visitType: String,
        status: Int,
        myCallback: (result: String?) -> Unit
    ) {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .changeAppointmentStatus(
                        "PI0040",
                        AppointmentReq(
                            apptId,
                            preference!![PrefKeys.PREF_PATIENT_ID, ""]!!,
                            true,
                            bookingDate,
                            visitType,
                            status,
                            ""
                        ), getAccessToken()
                    )
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
                            if (status == "202" || status == "200") {
                                myCallback.invoke("Success")
                            }
                        } catch (e: Exception) {
                            hideProgress()
                            displayToast("Something went wrong.. Please try after sometime")
                        }
                    }, { error ->
                        hideProgress()
                        //displayToast("Error ${error.localizedMessage}")
                        if ((error as HttpException).code() == 401) {
                            userLogin(
                                preference!![PrefKeys.PREF_EMAIL]!!,
                                preference!![PrefKeys.PREF_PASS]!!
                            ) { result ->
                                callAppointmentApi(
                                    apptId,
                                    bookingDate,
                                    visitType,
                                    status
                                ) { response ->
                                    if (response == "Success") {
                                        replaceFragmentNoBackStack(
                                            TherapistFeedbackFragment(),
                                            R.id.layout_home,
                                            TherapistFeedbackFragment.TAG
                                        )
                                    }
                                }
                            }
                        } else {
                            displayAfterLoginErrorMsg(error)
                            popBackStack()
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    fun showImage(imageView: ImageView) {
        val dialog = Dialog(requireActivity())
        dialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(layoutInflater.inflate(R.layout.display_image, null))
        dialog.setCanceledOnTouchOutside(true)
        dialog.btnImageDialog.setOnClickListener {
            dialog.dismiss()
        }
        dialog.imgView.setImageBitmap((imageView.drawable as BitmapDrawable).bitmap)
        dialog.show()
    }

    fun displayConfirmPopup() {
        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle("Alert")
        builder.setMessage("Would you like to exit the App?")
        builder.setPositiveButton(android.R.string.yes) { dialog, _ ->
            dialog.dismiss()
            clearCache()
        }
        builder.setNegativeButton(android.R.string.no) { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun clearCache() {
        preference!![PrefKeys.PREF_IS_LOGGEDIN] = false
        getHeader().visibility = View.GONE
        swipeSliderEnable(false)
        replaceFragmentNoBackStack(LoginFragment(), R.id.layout_home, LoginFragment.TAG)
    }

    fun Fragment?.runOnUiThread(action: () -> Unit) {
        this ?: return
        if (!isAdded) return // Fragment not attached to an Activity
        activity?.runOnUiThread(action)
    }

    fun convert(bitmap: Bitmap): String? {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
    }

    fun createAnonymousUser(
        selectedTherapy: String,
        consentImg: String,
        parentName: String,
        relation: String,
        contactNo: String
    ) {
        var selectedTherapyId = 1
        when (selectedTherapy) {
            "Individual" -> {
                selectedTherapyId = 1
            }
            "Teen" -> {
                selectedTherapyId = 2
            }
            "Couple" -> {
                selectedTherapyId = 3
            }
            "LGBTQ" -> {
                selectedTherapyId = 4
            }
        }
        showProgress()
        val deviceId: String =
            Settings.Secure.getString(requireActivity().contentResolver, Settings.Secure.ANDROID_ID)
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .sendDeviceId(
                        DeviceId(
                            deviceId,
                            selectedTherapyId,
                            consentImg,
                            parentName,
                            relation,
                            contactNo
                        )
                    )
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
                            val jsonObject = JSONObject(responseBody)
                            preference!![PrefKeys.PREF_DEVICE_ID] =
                                jsonObject.getString("device_id")
                            preference!![PrefKeys.PREF_SELECTED_THERAPY] = selectedTherapy
                            preference!![PrefKeys.PREF_ID] =
                                jsonObject.getInt("id")
                            when (status) {
                                "500" -> {
                                    displayToast("Something went wrong.. Please try after sometime")
                                }
                                "400" -> {
                                    displayToast("Something went wrong.. Please try after sometime")
                                }
                                "201" -> {
                                    replaceFragmentNoBackStack(
                                        QuestionnaireFragment.newInstance(selectedTherapy),
                                        R.id.layout_home,
                                        QuestionnaireFragment.TAG
                                    )
                                }
                                "208" -> {
                                    replaceFragmentNoBackStack(
                                        QuestionnaireFragment.newInstance(selectedTherapy),
                                        R.id.layout_home,
                                        QuestionnaireFragment.TAG
                                    )
                                }
                            }
                            /*if (status == "208") {
                                replaceFragmentNoBackStack(
                                    RegistrationFragment(),
                                    R.id.layout_home,
                                    RegistrationFragment.TAG
                                )
                            } else {*/

                            //}
                            preference!![PrefKeys.PREF_STEP] = Utils.INTRO_SCREEN
                            //getQuestionnaire(selectedTherapy)
                        } catch (e: Exception) {
                            hideProgress()
                            displayToast("Something went wrong.. Please try after sometime")
                        }
                    }, { error ->
                        hideProgress()
                        //displayToast("Error ${error.localizedMessage}")
                        if ((error as HttpException).code() == 404) {
                            displayErrorMsg(error)
                        } else {
                            displayToast("Something went wrong.. Please try after sometime")
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }
}
