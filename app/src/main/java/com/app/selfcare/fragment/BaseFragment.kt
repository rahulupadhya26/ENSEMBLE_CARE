package com.app.selfcare.fragment


import android.annotation.SuppressLint
import android.app.Dialog
import android.content.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.provider.Settings
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.TextUtils
import android.text.format.DateFormat
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Base64
import android.util.DisplayMetrics
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.BaseActivity
import com.app.selfcare.GroupVideoCall
import com.app.selfcare.MainActivity
import com.app.selfcare.R
import com.app.selfcare.adapters.CarePlanDayListAdapter
import com.app.selfcare.controller.IController
import com.app.selfcare.controller.IFragment
import com.app.selfcare.crypto.DecryptionImpl
import com.app.selfcare.data.*
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper
import com.app.selfcare.preference.PreferenceHelper.get
import com.app.selfcare.preference.PreferenceHelper.set
import com.app.selfcare.services.RequestInterface
import com.app.selfcare.utils.Utils
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.display_image.*
import kotlinx.android.synthetic.main.fragment_care_plan.*
import kotlinx.android.synthetic.main.fragment_therapy_basic_details_c.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.HttpException
import java.io.*
import java.lang.reflect.Type
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
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

    fun isValidPasswordFormat(password: String): Boolean {
        val passwordREGEX = Pattern.compile(
            "^" +
                    "(?=.*[0-9])" +         //at least 1 digit
                    "(?=.*[a-z])" +         //at least 1 lower case letter
                    "(?=.*[A-Z])" +         //at least 1 upper case letter
                    "(?=.*[a-zA-Z])" +      //any letter
                    "(?=.*[@#$%^&+=])" +    //at least 1 special character
                    "(?=\\S+$)" +           //no white spaces
                    ".{8,}" +               //at least 8 characters
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
    protected fun captureImage(imageView: ImageView?, type: String) {
        mActivity!!.captureImage(imageView, type)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    protected fun captureImage(imageView: ImageView) {
        mActivity!!.captureImage(imageView)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    protected fun showImageDialog() {
        mActivity!!.showImageDialog()
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

    fun setLayoutBottomNavigation(navigationView: RelativeLayout?) {
        mActivity!!.setLayoutBottomNavigation(navigationView)
    }

    fun getLayoutBottomNavigation(): RelativeLayout? {
        return mActivity!!.getLayoutBottomNavigation()
    }

    fun updateStatusBarColor(colorId: Int) {
        return mActivity!!.updateStatusBarColor(colorId)
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
                            e.printStackTrace()
                            displayToast("Something went wrong.. Please try after sometime")
                        }

                    }, { error ->
                        hideProgress()
                        if ((error as HttpException).code() == 401) {
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
        preference!![PrefKeys.PREF_PHOTO] = jsonObj.getJSONObject("patient").getString("photo")
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
                    displayMsg("Alert", decryptedErrMsg)
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
                            "", "", "", ""
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

    fun sendApptStatus(
        apptId: String,
        actualStartTime: String,
        actualEndTime: String,
        duration: String,
        myCallback: (result: String?) -> Unit
    ) {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .sendApptStatus(
                        "PI0060",
                        AppointmentStatus(
                            apptId,
                            actualStartTime,
                            actualEndTime,
                            duration,
                            status = 4
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
                                sendApptStatus(
                                    apptId,
                                    actualStartTime,
                                    actualEndTime,
                                    duration
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

    fun showImage(bitmapStr: String) {
        val dialog = Dialog(requireActivity())
        dialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(layoutInflater.inflate(R.layout.display_image, null))
        dialog.setCanceledOnTouchOutside(true)
        dialog.btnImageDialog.setOnClickListener {
            dialog.dismiss()
        }
        Glide.with(this)
            .load(File(bitmapStr))
            .into(dialog.imgView)
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
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
        Log.i("File size after compress", bitmap.byteCount.toString())
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
                selectedTherapyId = 3
            }
            "Teen" -> {
                selectedTherapyId = 1
            }
            "Couple" -> {
                selectedTherapyId = 2
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
                            selectedTherapyId
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

    fun getAge(dobString: String): Int {
        var date: Date? = null
        val sdf = SimpleDateFormat("MM/dd/yyyy")
        try {
            date = sdf.parse(dobString)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        if (date == null) return 0
        val dob: Calendar = Calendar.getInstance()
        val today: Calendar = Calendar.getInstance()
        dob.time = date
        val year: Int = dob.get(Calendar.YEAR)
        val month: Int = dob.get(Calendar.MONTH)
        val day: Int = dob.get(Calendar.DAY_OF_MONTH)
        dob.set(year, month + 1, day)
        var age: Int = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR)
        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--
        }
        return age
    }

    fun takeScreenshot() {
        try {
            showProgress()
            val now = Date()
            DateFormat.format("yyyy-MM-dd_hh:mm:ss", now)
            // image naming and path  to include sd card  appending name you choose for file
            val mPath: String =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    .toString() + "/" + System.currentTimeMillis().toString()
                    .replace(":", ".") + ".jpg"

            // create bitmap screen capture
            val v1: View = requireActivity().window.decorView.rootView
            v1.isDrawingCacheEnabled = true
            v1.buildDrawingCache(true)
            val bitmap = Bitmap.createBitmap(v1.drawingCache)
            v1.isDrawingCacheEnabled = false
            val imageFile = File(mPath)
            val outputStream = FileOutputStream(imageFile)
            val quality = 100
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            outputStream.flush()
            outputStream.close()
            displayToast("Saved screenshot")
            hideProgress()
        } catch (e: Exception) {
            hideProgress()
            // Several error may come out with file handling or DOM
            e.printStackTrace()
        }
    }

    fun getData(tableId: String, myCallback: (result: String?) -> Unit) {
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .getData(tableId, getAccessToken())
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
                            if (status == "401") {
                                userLogin(
                                    preference!![PrefKeys.PREF_EMAIL]!!,
                                    preference!![PrefKeys.PREF_PASS]!!
                                ) { result ->

                                }
                            } else {
                                myCallback.invoke(responseBody)
                            }
                        } catch (e: Exception) {
                            hideProgress()
                            e.printStackTrace()
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

                            }
                        } else {
                            displayAfterLoginErrorMsg(error)
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    fun getDetailData(type: String, category: String, myCallback: (result: String?) -> Unit) {
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .getDetailData(type, category, getAccessToken())
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
                            if (status == "401") {
                                userLogin(
                                    preference!![PrefKeys.PREF_EMAIL]!!,
                                    preference!![PrefKeys.PREF_PASS]!!
                                ) { result ->

                                }
                            } else {
                                myCallback.invoke(responseBody)
                            }
                        } catch (e: Exception) {
                            hideProgress()
                            e.printStackTrace()
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

                            }
                        } else {
                            displayAfterLoginErrorMsg(error)
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    fun calculateNoOfColumns(context: Context, columnWidthDp: Float): Int {
        val displayMetrics: DisplayMetrics = context.resources.displayMetrics
        val screenWidthDp: Float = displayMetrics.widthPixels / displayMetrics.density
        return (screenWidthDp / columnWidthDp + 0.5).toInt()
    }

    fun sendFavoriteData(
        id: Int,
        type: String,
        isFavorite: Boolean,
        wellnessType: String,
        myCallback: (result: String?) -> Unit
    ) {
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .sendFavoriteData(
                        wellnessType + "_favourites",
                        FavoriteData(id, type, isFavorite),
                        getAccessToken()
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
                            if (status == "401") {
                                userLogin(
                                    preference!![PrefKeys.PREF_EMAIL]!!,
                                    preference!![PrefKeys.PREF_PASS]!!
                                ) { result ->

                                }
                            } else {
                                myCallback.invoke(responseBody)
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

                            }
                        } else {
                            displayAfterLoginErrorMsg(error)
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    fun getFavoriteData(wellnessType: String, myCallback: (result: String?) -> Unit) {
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .getFavoriteData(wellnessType + "_favourites", getAccessToken())
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
                            if (status == "401") {
                                userLogin(
                                    preference!![PrefKeys.PREF_EMAIL]!!,
                                    preference!![PrefKeys.PREF_PASS]!!
                                ) { result ->

                                }
                            } else {
                                myCallback.invoke(responseBody)
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

                            }
                        } else {
                            displayAfterLoginErrorMsg(error)
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    fun sendResourceFavoriteData(
        id: Int,
        type: String,
        isFavorite: Boolean,
        myCallback: (result: String?) -> Unit
    ) {
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .sendResourceFavoriteData(
                        FavoriteData(id, type, isFavorite),
                        getAccessToken()
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
                            if (status == "401") {
                                userLogin(
                                    preference!![PrefKeys.PREF_EMAIL]!!,
                                    preference!![PrefKeys.PREF_PASS]!!
                                ) { result ->

                                }
                            } else {
                                myCallback.invoke(responseBody)
                            }
                        } catch (e: Exception) {
                            hideProgress()
                            e.printStackTrace()
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

                            }
                        } else {
                            displayAfterLoginErrorMsg(error)
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    fun getResourceFavoriteData(myCallback: (result: String?) -> Unit) {
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .getResourceFavoriteData(getAccessToken())
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
                            if (status == "401") {
                                userLogin(
                                    preference!![PrefKeys.PREF_EMAIL]!!,
                                    preference!![PrefKeys.PREF_PASS]!!
                                ) { result ->

                                }
                            } else {
                                myCallback.invoke(responseBody)
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

                            }
                        } else {
                            displayAfterLoginErrorMsg(error)
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    fun shareDetails(title: String, text: String, link: String, imgLink: String, type: String) {
        when (type) {
            "Video" -> {

            }
            "Article" -> {

            }
            "Podcast" -> {
                if (text.isNotEmpty() && link.isNotEmpty() && imgLink.isNotEmpty()) {
                    val imageUrl = BaseActivity.baseURL.dropLast(5) + imgLink
                    Glide.with(requireActivity()).asBitmap().load(imageUrl)
                        .into(object : CustomTarget<Bitmap>() {
                            override fun onResourceReady(
                                resource: Bitmap,
                                transition: Transition<in Bitmap>?
                            ) {
                                val imageUri = getImageUri(resource)
                                val shareIntent = Intent()
                                shareIntent.action = Intent.ACTION_SEND
                                shareIntent.putExtra(Intent.EXTRA_TITLE, text + "\n\n" + link)
                                if (imageUri != null) {
                                    shareIntent.type = "image/*"
                                    shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri)
                                }
                                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                startActivity(
                                    Intent.createChooser(
                                        shareIntent,
                                        "Shared via Ensemble care user"
                                    )
                                )
                            }

                            override fun onLoadCleared(placeholder: Drawable?) {

                            }
                        })
                }
            }
            "Journal" -> {
                if (text.isNotEmpty()) {
                    val shareIntent = Intent()
                    shareIntent.action = Intent.ACTION_SEND
                    shareIntent.putExtra(Intent.EXTRA_TITLE, "Shared via Ensemble care user")
                    shareIntent.putExtra(Intent.EXTRA_TEXT, text)
                    shareIntent.type = "text/plain"
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    startActivity(
                        Intent.createChooser(
                            shareIntent,
                            "Shared via Ensemble care user"
                        )
                    )
                }
            }
            "Quote" -> {
                if (text.isNotEmpty() && imgLink.isNotEmpty()) {
                    val imageUrl = BaseActivity.baseURL.dropLast(5) + imgLink
                    Glide.with(requireActivity()).asBitmap().load(imageUrl)
                        .into(object : CustomTarget<Bitmap>() {
                            override fun onResourceReady(
                                resource: Bitmap,
                                transition: Transition<in Bitmap>?
                            ) {
                                /*val imgBitmapPath: String = MediaStore.Images.Media.insertImage(
                                    requireActivity().contentResolver,
                                    resource,
                                    text,
                                    null
                                )*/
                                val imageUri = getImageUri(resource)
                                val shareIntent = Intent()
                                shareIntent.action = Intent.ACTION_SEND
                                shareIntent.putExtra(Intent.EXTRA_TITLE, text)
                                if (imageUri != null) {
                                    shareIntent.type = "image/*"
                                    shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri)
                                }
                                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                startActivity(
                                    Intent.createChooser(
                                        shareIntent,
                                        "Shared via Ensemble care user"
                                    )
                                )
                            }

                            override fun onLoadCleared(placeholder: Drawable?) {

                            }
                        })
                }
            }
        }
    }

    private fun getLocalBitmapUri(bmp: Bitmap): Uri? {
        var bmpUri: Uri? = null
        try {
            val file = File(
                requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "share_image.jpeg"
            )
            val out = FileOutputStream(file)
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out)
            out.close()
            bmpUri = Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return bmpUri
    }

    /**
     * Return the URI for a file. This URI is used for
     * sharing of video.
     * NOTE: You cannot share a file by file path.
     *
     * @param context Context
     * @param videoFile File
     * @return Uri?
     */
    @SuppressLint("Range")
    fun getVideoContentUri(context: Context, videoFile: File): Uri? {
        var uri: Uri? = null
        try {
            val filePath = videoFile.absolutePath
            val cursor = context.contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.Video.Media._ID),
                MediaStore.Video.Media.DATA + "=? ",
                arrayOf(filePath), null
            )

            if (cursor != null && cursor.moveToFirst()) {
                val id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID))
                val baseUri = Uri.parse("content://media/external/video/media")
                uri = Uri.withAppendedPath(baseUri, "" + id)
            } else if (videoFile.exists()) {
                val values = ContentValues()
                values.put(MediaStore.Video.Media.DATA, filePath)
                uri = context.contentResolver.insert(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values
                )
            }
            cursor!!.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return uri
    }

    fun getDayWiseCarePlanData(dayNo: Int, myCallback: (result: String?) -> Unit) {
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .getDayWiseCarePlanData(dayNo, getAccessToken())
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
                            myCallback.invoke(responseBody)
                        } catch (e: Exception) {
                            hideProgress()
                            e.printStackTrace()
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

                            }
                        } else {
                            displayAfterLoginErrorMsg(error)
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    fun getToken(appointment: GetAppointment, myCallback: (result: String?) -> Unit) {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .getToken(
                        GetToken(appointment.appointment.appointment_id),
                        getAccessToken()
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
                            myCallback.invoke(responseBody)
                        } catch (e: Exception) {
                            hideProgress()
                            //displayToast("Something went wrong.. Please try after sometime")
                        }
                    }, { error ->
                        hideProgress()
                        if ((error as HttpException).code() == 401) {
                            userLogin(
                                preference!![PrefKeys.PREF_EMAIL]!!,
                                preference!![PrefKeys.PREF_PASS]!!
                            ) { result ->

                            }
                        } else {
                            displayAfterLoginErrorMsg(error)
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    fun CheckBox.addClickableLink(
        fullText: String,
        linkText: SpannableString,
        callback: () -> Unit
    ) {
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                widget.cancelPendingInputEvents() // Prevent CheckBox state from being toggled when link is clicked
                callback.invoke()
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = true // Show links with underlines
                ds.color = resources.getColor(R.color.primaryGreen);
            }
        }
        linkText.setSpan(
            clickableSpan,
            0,
            linkText.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        val fullTextWithTemplate = fullText.replace(linkText.toString(), "^1", false)
        val cs = TextUtils.expandTemplate(fullTextWithTemplate, linkText)

        text = cs
        movementMethod = LinkMovementMethod.getInstance() // Make link clickable
    }

    fun updateNotificationStatus(notificationId: Int) {
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .updateNotificationStatus(
                        "PI0061",
                        NotifyStatus(notificationId),
                        getAccessToken()
                    )
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ result ->
                        try {
                            var responseBody = result.string()
                            Log.d("Response Body", responseBody)
                            val respBody = responseBody.split("|")
                            val status = respBody[1]
                            responseBody = respBody[0]
                        } catch (e: Exception) {
                            hideProgress()
                            e.printStackTrace()
                        }
                    }, { error ->
                        displayToast("Error ${error.localizedMessage}")
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }
}
