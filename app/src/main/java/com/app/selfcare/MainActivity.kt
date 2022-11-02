package com.app.selfcare

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.loader.content.CursorLoader
import com.app.selfcare.controller.IController
import com.app.selfcare.fragment.*
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper
import com.app.selfcare.preference.PreferenceHelper.get
import com.app.selfcare.preference.PreferenceHelper.set
import com.app.selfcare.utils.NSFWDetector
import com.app.selfcare.utils.Utils
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_header.*
import kotlinx.android.synthetic.main.layout_header_drawer.*
import kotlinx.android.synthetic.main.layout_header_drawer.view.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : BaseActivity(), IController {

    private val permissionRequestCode: Int = 1001
    private val requestImageCapture: Int = 1002
    private val requestGalleryImage = 1003
    private val requestFileUpload = 1004
    private val requestVideoUpload = 1005
    private val permissionCode: Int = 1000
    private var preference: SharedPreferences? = null
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    var progressAlive = false
    var pDialog: ProgressDialog? = null
    private var selectedOption: Int? = -1
    private var mCurrentPhotoPath: String? = ""
    private var bitmapList: ArrayList<String> = ArrayList()
    private var imageView: ImageView? = null
    private var navigationView: BottomNavigationView? = null

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adjustFontScale(resources.configuration, 1.0f)
        //window.statusBarColor = Color.WHITE
        setContentView(R.layout.activity_main)
        preference = PreferenceHelper.defaultPrefs(this)
        drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        ic_menu.setOnClickListener { handleClick(it) }
        imgUserPic.setOnClickListener {
            replaceFragment(
                ProfileFragment(),
                R.id.layout_home,
                ProfileFragment.TAG
            )
        }
        nav_view.getHeaderView(0).drawer_img_back.setOnClickListener { _view -> handleClick(_view) }
        nav_view.setNavigationItemSelectedListener { item: MenuItem ->
            drawer_layout.closeDrawer(GravityCompat.START)
            handleClick(item.itemId)
            false
        }
        if (!checkPermission()) {
            requestPermission(permissionCode)
        }

        if (preference!![PrefKeys.PREF_USER_ID, ""]!!.isNotEmpty()) {
            if (preference!![PrefKeys.PREF_IS_LOGGEDIN, false]!!) {
                replaceFragmentNoBackStack(
                    BottomNavigationFragment(),
                    R.id.layout_home,
                    BottomNavigationFragment.TAG
                )
            } else {
                replaceFragmentNoBackStack(SplashFragment(), R.id.layout_home, SplashFragment.TAG)
            }
        } else {
            replaceFragmentNoBackStack(SplashFragment(), R.id.layout_home, SplashFragment.TAG)
        }

        getBackButton().setOnClickListener {
            popBackStack()
        }
    }

    @SuppressLint("SetTextI18n")
    fun setUserDetails() {
        if (preference!![PrefKeys.PREF_USER_ID, ""]!!.isNotEmpty()) {
            nav_view.getHeaderView(0).txtProfileName.text =
                preference!![PrefKeys.PREF_FNAME, ""]!! + " " +
                        preference!![PrefKeys.PREF_MNAME, ""]!! + " " +
                        preference!![PrefKeys.PREF_LNAME, ""]!!

            nav_view.getHeaderView(0).txtProfileEmail.text = preference!![PrefKeys.PREF_EMAIL, ""]!!

            nav_view.getHeaderView(0).img_profile.setOnClickListener {
                replaceFragment(
                    ProfileFragment(),
                    R.id.layout_home,
                    ProfileFragment.TAG
                )
                drawer_layout.closeDrawer(GravityCompat.START)
            }
        }
    }

    private fun handleClick(view: View) {
        when (view.id) {
            R.id.drawer_img_back -> {
                drawer_layout.closeDrawer(GravityCompat.START)
            }
            R.id.ic_menu -> {
                drawer_layout.openDrawer(GravityCompat.START)
            }
        }
    }

    private fun handleClick(itemId: Int) {
        when (itemId) {
            R.id.nav_logout -> {
                //sendEventLog("", Utils.NAV_LOGOUT)
                displayConfirmPopup()
            }
            R.id.nav_consent -> {
                replaceFragment(
                    ConsentsListFragment(),
                    R.id.layout_home,
                    ConsentsListFragment.TAG
                )
            }

            /*R.id.nav_sync -> {
                sendEventLog("", Utils.NAV_SYNC_HEALTH)
                replaceFragment(
                    SyncHealthDashboard(),
                    com.google.android.gms.location.R.id.layout_home,
                    SyncHealthDashboard.TAG
                )
            }*/
        }
    }

    private fun adjustFontScale(configuration: Configuration, scale: Float) {
        configuration.fontScale = scale
        val metrics: DisplayMetrics = resources.displayMetrics
        val wm: WindowManager = this.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.defaultDisplay.getMetrics(metrics)
        metrics.scaledDensity = configuration.fontScale * metrics.density
        configuration.fontScale = 1.0f
        baseContext.resources.updateConfiguration(configuration, metrics)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun checkPermission(): Boolean {
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR)
                == PackageManager.PERMISSION_GRANTED)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestPermission(id: Int) {
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACTIVITY_RECOGNITION,
                Manifest.permission.READ_CALENDAR,
                Manifest.permission.WRITE_CALENDAR
            ),
            id
        )
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == permissionRequestCode) {

            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                && grantResults[1] == PackageManager.PERMISSION_GRANTED
            ) {
                takePicture()
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
            return
        } else if (requestCode == requestFileUpload) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                && grantResults[1] == PackageManager.PERMISSION_GRANTED
            ) {
                choosePhotoFromGallery()
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == requestVideoUpload) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                && grantResults[1] == PackageManager.PERMISSION_GRANTED
            ) {
                uploadVideo()
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        } else {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                && grantResults[1] == PackageManager.PERMISSION_GRANTED
            ) {
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getHeader(): View {
        return findViewById(R.id.layout_header)
    }

    @SuppressLint("WrongViewCast")
    override fun getBackButton(): View {
        return findViewById(R.id.ico_action_back)
    }

    override fun swipeSliderEnable(flag: Boolean) {
        if (flag)
            drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNDEFINED)
        else
            drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }

    @SuppressLint("WrongViewCast")
    override fun getSubTitle(): TextView {
        return findViewById(R.id.txt_sub_title)
    }

    override fun showProgress() {
        if (progressAlive) {
            pDialog!!.cancel()
            progressAlive = false
        }
        pDialog = ProgressDialog(this)
        pDialog!!.setMessage("Please wait...")
        if ("Please wait".contains("Please wait")) pDialog!!.setCanceledOnTouchOutside(false)
        progressAlive = true
        pDialog!!.show()
    }

    override fun hideProgress() {
        layout_home.setBackgroundColor(Color.TRANSPARENT)
        layout_progress.visibility = View.GONE
        if (progressAlive) {
            pDialog!!.dismiss()
            pDialog!!.cancel()
            progressAlive = false
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun captureImage() {
        if (checkPermission()) takePicture() else requestPermission(permissionRequestCode)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun captureImage(imageView: ImageView) {
        this.imageView = imageView
        showPictureDialog()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun uploadPicture() {
        if (checkPermission()) choosePhotoFromGallery() else requestPermission(requestFileUpload)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun uploadVideo() {
        if (checkPermission()) {
            val videoPickIntent = Intent(Intent.ACTION_PICK)
            videoPickIntent.type = "video/*"
            startActivityForResult(
                Intent.createChooser(videoPickIntent, "Please pick a video"),
                requestVideoUpload
            )
        } else requestPermission(requestVideoUpload)
    }


    fun setBottomNavigation(navigationView: BottomNavigationView?) {
        this.navigationView = navigationView
    }

    fun getBottomNavigation(): BottomNavigationView? {
        return navigationView
    }

    override fun clearTempFormData() {
        this.bitmapList.clear()
    }

    override fun getPhotoPath(): String {
        return mCurrentPhotoPath!!
    }

    override fun getBitmapList(): ArrayList<String> {
        return bitmapList
    }

    override fun hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun setBottomMenu(id: Int) {
        getBottomNavigation()!!.selectedItemId = id
    }

    private fun takePicture() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val file: File = createFile()

        val uri: Uri = FileProvider.getUriForFile(
            this,
            "com.app.selfcare",
            file
        )
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        startActivityForResult(intent, requestImageCapture)
    }

    private fun choosePhotoFromGallery() {
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(galleryIntent, requestGalleryImage)
    }

    @Throws(IOException::class)
    private fun createFile(): File {
        // Create an image file name
        val timeStamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            mCurrentPhotoPath = absolutePath
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun showPictureDialog() {
        val pictureDialog = AlertDialog.Builder(this)
        pictureDialog.setTitle("Select Action")
        val pictureDialogItems =
            arrayOf("Select photo from gallery", "Capture photo from camera")
        pictureDialog.setItems(
            pictureDialogItems
        ) { dialog, which ->
            dialog.dismiss()
            when (which) {
                0 -> {
                    selectedOption = 0
                    uploadPicture()
                }
                1 -> {
                    selectedOption = 1
                    captureImage()
                }
            }
        }
        pictureDialog.show()
    }

    private fun getRealPathFromURI(contentUri: Uri): String {
        try {
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            val loader =
                CursorLoader(this, contentUri, proj, null, null, null)
            val cursor = loader.loadInBackground()
            val columnIndex = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            val result = cursor.getString(columnIndex)
            cursor.close()
            return result
        } catch (e: Exception) {
        }
        return ""
    }

    private fun loadBitmap(fileName: String) {
        if (imageView != null) {
            if (BitmapFactory.decodeFile(fileName) != null) {
                NSFWDetector.isNSFW(
                    BitmapFactory.decodeFile(fileName),
                    Utils.NSFW_CONFIDENCE_THRESHOLD.toFloat()
                ) { isNSFW, label, confidence, image ->
                    when (label) {
                        Utils.LABEL_SFW -> {
                            Toast.makeText(this, "This is an obscene image", Toast.LENGTH_SHORT)
                                .show()
                        }
                        Utils.LABEL_NSFW -> {
                            Glide.with(this)
                                .load(File(fileName))
                                .into(imageView!!)
                            //Toast.makeText(this, "SFW with confidence: $confidence", Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            Toast.makeText(this, "Error while loading image", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            } else {
                if (imageView != null) {
                    Glide.with(this)
                        .load(File(fileName))
                        .into(imageView!!)
                } else {
                    bitmapList.add(fileName)
                }
            }
        } else {
            bitmapList.add(fileName)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == requestGalleryImage && resultCode == Activity.RESULT_OK) {

            if (data!!.clipData != null) {
                val count = data.clipData!!.itemCount
                for (i in 0 until count) {
                    mCurrentPhotoPath = getRealPathFromURI(data.clipData!!.getItemAt(i).uri)
                    loadBitmap(mCurrentPhotoPath!!)
                }
            } else if (data.data != null) {
                try {
                    mCurrentPhotoPath = getRealPathFromURI(data.data!!)
                    loadBitmap(mCurrentPhotoPath!!)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        } else if (requestCode == requestVideoUpload && resultCode == Activity.RESULT_OK) {

            if (data!!.clipData != null) {
                val count = data.clipData!!.itemCount
                for (i in 0 until count) {
                    mCurrentPhotoPath = getRealPathFromURI(data.clipData!!.getItemAt(i).uri)
                }
            } else if (data.data != null) {
                try {
                    mCurrentPhotoPath = getRealPathFromURI(data.data!!)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            loadBitmap(mCurrentPhotoPath!!)
            if (mCurrentPhotoPath!!.isNotEmpty()) {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Alert")
                builder.setMessage("Do you really want to upload this video?")
                builder.setPositiveButton("Yes") { dialogInterface, _ ->
                    dialogInterface.dismiss()
                    //(getCurrentFragment() as ShareMomentFragment).postVideoFeed(this)
                }
                //performing cancel action
                builder.setNeutralButton("Cancel") { dialogInterface, _ ->
                    dialogInterface.dismiss()
                    bitmapList.clear()
                    //(getCurrentFragment() as ShareMomentFragment).hidePhotosList()
                }
                val alert = builder.create()
                alert.setCancelable(false)
                alert.show()
            }

        } else if (requestCode == requestImageCapture && resultCode == Activity.RESULT_OK) {
            try {
                loadBitmap(mCurrentPhotoPath!!)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun displayConfirmPopup() {
        val builder = AlertDialog.Builder(this)
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
}