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
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.loader.content.CursorLoader
import com.app.selfcare.controller.IController
import com.app.selfcare.controller.IOnBackPressed
import com.app.selfcare.databinding.ActivityMainBinding
import com.app.selfcare.databinding.DialogPictureOptionBinding
import com.app.selfcare.databinding.LayoutHeaderBinding
import com.app.selfcare.databinding.LayoutHeaderDrawerBinding
import com.app.selfcare.fragment.*
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper
import com.app.selfcare.preference.PreferenceHelper.get
import com.app.selfcare.preference.PreferenceHelper.set
import com.app.selfcare.utils.NSFWDetector
import com.app.selfcare.utils.NetworkConnection
import com.app.selfcare.utils.Utils
import com.bumptech.glide.Glide
import com.github.ybq.android.spinkit.SpinKitView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

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
    private var layoutBottomNav: RelativeLayout? = null
    private var createPictureDialog: BottomSheetDialog? = null
    private var networkConnection: NetworkConnection? = null
    private var spinKit: SpinKitView? = null
    private lateinit var binding: ActivityMainBinding
    private lateinit var headerBinding: LayoutHeaderBinding
    private lateinit var headerDrawerBinding: LayoutHeaderDrawerBinding
    private val PICKFILE_REQUEST_CODE = 9

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adjustFontScale(resources.configuration, 1.0f)
        /*window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )*/
        //window.statusBarColor = Color.WHITE
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        Utils.bottomNav = Utils.BOTTOM_NAV_DASHBOARD
        preference = PreferenceHelper.defaultPrefs(this)
        FirebaseApp.initializeApp(this)
        headerBinding = LayoutHeaderBinding.inflate(layoutInflater)
        headerDrawerBinding = LayoutHeaderDrawerBinding.inflate(layoutInflater)
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        headerBinding.icMenu.setOnClickListener { handleClick(it) }
        headerBinding.imgUserPic.setOnClickListener {
            replaceFragment(
                ProfileFragment(),
                R.id.layout_home,
                ProfileFragment.TAG
            )
        }
        binding.navView.getHeaderView(0)
        headerDrawerBinding.drawerImgBack.setOnClickListener { _view -> handleClick(_view) }
        binding.navView.setNavigationItemSelectedListener { item: MenuItem ->
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            handleClick(item.itemId)
            false
        }
        if (!checkPermission()) {
            requestPermission(permissionCode)
        }

        getBackButton().setOnClickListener {
            popBackStack()
        }

        spinKit = findViewById(R.id.spin_kit)

        networkConnection = NetworkConnection(applicationContext)
        replaceFragmentNoBackStack(SplashFragment(), R.id.layout_home, SplashFragment.TAG)
        networkConnection!!.observe(this) { isConnected ->
            if (!isConnected) {
                Toast.makeText(this, "Not Connected", Toast.LENGTH_SHORT).show()
                replaceFragment(
                    InternetConnectionFragment(),
                    R.id.layout_home,
                    InternetConnectionFragment.TAG
                )
            }
        }
    }

    private fun clearPreviousFragments() {
        supportFragmentManager.fragments.let {
            if (it.isNotEmpty()) {
                supportFragmentManager.beginTransaction().apply {
                    for (fragment in it) {
                        remove(fragment)
                    }
                    commit()
                }
            }
        }
    }

    override fun onBackPressed() {
        val fragment =
            this.supportFragmentManager.findFragmentById(R.id.layout_home)
        (fragment as? IOnBackPressed)?.onBackPressed()?.not()?.let {
            super.onBackPressed()
        }
    }

    @SuppressLint("SetTextI18n")
    fun setUserDetails() {
        if (preference!![PrefKeys.PREF_USER_ID, ""]!!.isNotEmpty()) {
            headerDrawerBinding.txtProfileName.text =
                preference!![PrefKeys.PREF_FNAME, ""]!! + " " +
                        preference!![PrefKeys.PREF_MNAME, ""]!! + " " +
                        preference!![PrefKeys.PREF_LNAME, ""]!!

            headerDrawerBinding.txtProfileEmail.text = preference!![PrefKeys.PREF_EMAIL, ""]!!

            headerDrawerBinding.imgProfile.setOnClickListener {
                replaceFragment(
                    ProfileFragment(),
                    R.id.layout_home,
                    ProfileFragment.TAG
                )
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            }
        }
    }

    private fun handleClick(view: View) {
        when (view.id) {
            R.id.drawer_img_back -> {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            }
            R.id.ic_menu -> {
                binding.drawerLayout.openDrawer(GravityCompat.START)
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
            binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNDEFINED)
        else
            binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }

    @SuppressLint("WrongViewCast")
    override fun getSubTitle(): TextView {
        return findViewById(R.id.txt_sub_title)
    }

    override fun showProgress() {
        /*val fadingCircle = FadingCircle()
        fadingCircle.setBounds(0,0,200,200)
        fadingCircle.color = R.color.primaryGreen
        progress!!.indeterminateDrawable = fadingCircle*/
        binding.layoutProgress.visibility = View.VISIBLE
        /*if (progressAlive) {
            pDialog!!.cancel()
            progressAlive = false
        }
        pDialog = ProgressDialog(this)
        pDialog!!.setMessage("Please wait...")
        if ("Please wait".contains("Please wait")) pDialog!!.setCanceledOnTouchOutside(false)
        progressAlive = true
        pDialog!!.show()*/
    }

    override fun hideProgress() {
        binding.layoutHome.setBackgroundColor(Color.TRANSPARENT)
        binding.layoutProgress.visibility = View.GONE
        /*if (progressAlive) {
            pDialog!!.dismiss()
            pDialog!!.cancel()
            progressAlive = false
        }*/
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun captureImage() {
        if (checkPermission()) takePicture() else requestPermission(permissionRequestCode)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun captureImage(imageView: ImageView?, type: String) {
        this.imageView = imageView
        displayPictureDialog(type)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun captureImage(imageView: ImageView) {
        this.imageView = imageView
        showPictureDialog()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun showImageDialog() {
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

    fun setLayoutBottomNavigation(navigationView: RelativeLayout?) {
        this.layoutBottomNav = navigationView
    }

    fun getLayoutBottomNavigation(): RelativeLayout? {
        return layoutBottomNav
    }

    fun getNetworkInit(): NetworkConnection? {
        return networkConnection
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

    private var mUri: Uri? = null

    override fun selectFile() {
        chooseFile()
    }

    override fun getFileUri(): Uri {
        return mUri!!
    }

    private fun chooseFile() {
        val mimeTypes = arrayOf("image/jpeg", "image/png", "image/gif", "application/pdf")
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            intent.type = if (mimeTypes.size == 1) mimeTypes[0] else "*/*"
            if (mimeTypes.isNotEmpty()) {
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            }
        } else {
            var mimeTypesStr = ""
            for (mimeType in mimeTypes) {
                mimeTypesStr += "$mimeType|"
            }
            intent.type = mimeTypesStr.substring(0, mimeTypesStr.length - 1)
        }

        startActivityForResult(
            Intent.createChooser(intent, "Select a file"),
            PICKFILE_REQUEST_CODE
        )
    }

    private fun takePicture() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    ex.printStackTrace()
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.example.android.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, requestImageCapture)
                }
            }
        }

        /*val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val file: File = createFile()

        val uri: Uri = FileProvider.getUriForFile(
            this,
            "com.example.android.fileprovider",
            file
        )
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        startActivityForResult(intent, requestImageCapture)*/
    }

    private fun choosePhotoFromGallery() {
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
        startActivityForResult(galleryIntent, requestGalleryImage)
    }

    fun updateStatusBarColor(color: Int) { // Color must be in hexadecimal fromat
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window: Window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = getColor(color)
        }
    }

    @Throws(IOException::class)
    private fun createFile(): File {
        // Create an image file name
        val timeStamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
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

    private lateinit var pictureDialog: DialogPictureOptionBinding

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun displayPictureDialog(type: String) {
        createPictureDialog = BottomSheetDialog(this, R.style.SheetDialog)
        pictureDialog = DialogPictureOptionBinding.inflate(layoutInflater)
        val view = pictureDialog.root
        //val pictureDialog: View = layoutInflater.inflate(R.layout.dialog_picture_option, null)
        createPictureDialog!!.setContentView(view)
        createPictureDialog!!.setCanceledOnTouchOutside(true)

        pictureDialog.pictureDialogCamera.setOnClickListener {
            createPictureDialog!!.dismiss()
            selectedOption = 1
            captureImage()
        }

        pictureDialog.pictureDialogGallery.setOnClickListener {
            createPictureDialog!!.dismiss()
            selectedOption = 0
            uploadPicture()
        }

        if (type == "Prescription") {
            pictureDialog.pictureDialogRemove.visibility = View.GONE
        } else {
            pictureDialog.pictureDialogRemove.visibility = View.VISIBLE
        }

        pictureDialog.pictureDialogRemove.setOnClickListener {
            createPictureDialog!!.dismiss()
            when (type) {
                "Profile" -> {
                    imageView!!.setImageDrawable(null)
                    imageView!!.setImageResource(R.drawable.user_pic)
                }
                "Insurance" -> {
                    imageView!!.setImageDrawable(null)
                    imageView!!.setImageResource(R.drawable.plusnew)
                }
            }
        }
        createPictureDialog!!.show()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun showPictureDialog() {
        val pictureDialog = AlertDialog.Builder(this)
        pictureDialog.setTitle("Select Action")
        val pictureDialogItems =
            arrayOf("Take image from gallery", "Capture image using camera")
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
            bitmapList.add(fileName)
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
                            Toast.makeText(
                                this,
                                "Error while loading image - $label - $isNSFW - $confidence",
                                Toast.LENGTH_SHORT
                            )
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

    private fun setPic() {
        try {
            // Get the dimensions of the View
            val targetW: Int = imageView!!.width
            val targetH: Int = imageView!!.height

            val bmOptions = BitmapFactory.Options().apply {
                // Get the dimensions of the bitmap
                inJustDecodeBounds = true

                BitmapFactory.decodeFile(mCurrentPhotoPath!!, this)

                val photoW: Int = outWidth
                val photoH: Int = outHeight

                // Determine how much to scale down the image
                val scaleFactor: Int = Math.max(1, Math.min(photoW / targetW, photoH / targetH))

                // Decode the image file into a Bitmap sized to fill the View
                inJustDecodeBounds = false
                inSampleSize = scaleFactor
                inPurgeable = true
            }
            BitmapFactory.decodeFile(mCurrentPhotoPath!!, bmOptions)?.also { bitmap ->
                imageView!!.setImageBitmap(bitmap)
            }
        } catch (e: Exception) {
            e.printStackTrace()
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
        } else if (requestCode == PICKFILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                this.mUri = data.data
            }
        }
    }

    private fun displayConfirmPopup() {
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
        preference!![PrefKeys.PREF_IS_CARE_BUDDY_LOGGEDIN] = false
        getHeader().visibility = View.GONE
        swipeSliderEnable(false)
        replaceFragmentNoBackStack(LoginFragment(), R.id.layout_home, LoginFragment.TAG)
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    v.clearFocus()
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }
}