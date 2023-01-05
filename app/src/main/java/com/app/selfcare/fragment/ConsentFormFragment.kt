package com.app.selfcare.fragment

import android.graphics.*
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import com.app.selfcare.R
import com.app.selfcare.data.AppointmentReq
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper.get
import com.app.selfcare.utils.CalenderUtils
import com.app.selfcare.utils.SignatureView
import com.app.selfcare.utils.Utils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_consent_form.*
import retrofit2.HttpException
import java.io.File
import java.io.IOException

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ConsentFormFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ConsentFormFragment : BaseFragment(), SignatureView.OnSignedListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    var bSigned: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_consent_form
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.VISIBLE
        getSubTitle().visibility = View.GONE
        layout_consent_letter.visibility = View.VISIBLE
        layout_screenshot.visibility = View.GONE

        signatureView.setOnSignedListener(this)
        signatureView.clear()
        btnConsentLetter.setOnClickListener {
            if (bSigned) {
                val screenshot: Bitmap =
                    takeScreenshotOfRootView(requireActivity().window.decorView.rootView)
                img_screenshot.setImageBitmap(screenshot)
                layout_consent_letter.visibility = View.GONE
                layout_screenshot.visibility = View.VISIBLE
                //Call book appointment api
                createAppointmentApi(
                    Utils.appointmentId,
                    Utils.aptScheduleDate,
                    Utils.selectedCommunicationMode,
                    Utils.APPOINTMENT_BOOKED,
                    screenshot
                )
            } else {
                displayToast("Please sign the consent letter")
            }
        }

        btn_clear.setOnClickListener {
            signatureView.clear()
        }

        txt_screenshot_close.setOnClickListener {
            replaceFragment(
                AppointCongratFragment(),
                R.id.layout_home,
                AppointCongratFragment.TAG
            )
        }
    }

    private fun createAppointmentApi(
        apptId: String,
        bookingDate: String,
        visitType: String,
        status: Int,
        form: Bitmap
    ) {
        var prescription1: Bitmap? = null
        var prescription2: Bitmap? = null
        var prescription3: Bitmap? = null
        try {
            when (getBitmapList().size) {
                1 -> {
                    prescription1 = compressImage(File(getBitmapList()[0]).absolutePath)
                }
                2 -> {
                    prescription1 = compressImage(File(getBitmapList()[0]).absolutePath)
                    prescription2 = compressImage(File(getBitmapList()[1]).absolutePath)
                }
                3 -> {
                    prescription1 = compressImage(File(getBitmapList()[0]).absolutePath)
                    prescription2 = compressImage(File(getBitmapList()[1]).absolutePath)
                    prescription3 = compressImage(File(getBitmapList()[2]).absolutePath)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        txt_form_upload.text = "Uploading details to server"
        getBackButton().visibility = View.GONE
        txt_screenshot_close.visibility = View.VISIBLE
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .bookAppointment(
                        "PI0040",
                        AppointmentReq(
                            apptId,
                            preference!![PrefKeys.PREF_PATIENT_ID, ""]!!,
                            true,
                            bookingDate,
                            visitType,
                            status,
                            "data:image/jpg;base64," + convert(form),
                            if (prescription1 != null) "data:image/jpg;base64," + convert(
                                prescription1
                            ) else "",
                            if (prescription2 != null) "data:image/jpg;base64," + convert(
                                prescription2
                            ) else "",
                            if (prescription3 != null) "data:image/jpg;base64," + convert(
                                prescription3
                            ) else "",
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
                                clearTempFormData()
                                val description =
                                    "Type of Visit : " + Utils.selectedCommunicationMode + "\n" +
                                            "Time slot : " + Utils.aptScheduleTime
                                txt_form_upload.text = "Form uploaded successfully"
                                //val appointmentDate = DateUtils("$bookingDate 01:00:00")
                                val durationData = resources.getStringArray(R.array.goal_duration)
                                CalenderUtils.addEvent(
                                    requireActivity(),
                                    "$bookingDate 00:00:00",
                                    "Ensemble Care appointment",
                                    description,
                                    durationData[0], "30", Utils.aptScheduleTime.take(2),
                                    30
                                )
                                replaceFragment(
                                    AppointCongratFragment(),
                                    R.id.layout_home,
                                    AppointCongratFragment.TAG
                                )
                            } else {
                                getBackButton().visibility = View.VISIBLE
                                txt_screenshot_close.visibility = View.GONE
                                txt_form_upload.text = "Failed to upload form"
                            }
                        } catch (e: Exception) {
                            hideProgress()
                            getBackButton().visibility = View.VISIBLE
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
                                createAppointmentApi(
                                    apptId,
                                    bookingDate,
                                    visitType,
                                    status,
                                    form
                                )
                            }
                        } else {
                            getBackButton().visibility = View.VISIBLE
                            displayAfterLoginErrorMsg(error)
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    private fun compressImage(filePath: String): Bitmap {
        var scaledBitmap: Bitmap? = null
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true

        val file = File(filePath)
        val fileSize = (file.length() / 1024).toString()
        Log.i("File size", fileSize)
        var bitmapImage = BitmapFactory.decodeFile(filePath, options)
        var actualHeight = options.outHeight
        var actualWidth = options.outWidth
        //      max Height and width values of the compressed image is taken as 816x612
        val maxHeight = 816.0f
        val maxWidth = 612.0f
        var imgRatio = (actualWidth / actualHeight).toFloat()
        val maxRatio = maxWidth / maxHeight
        //      width and height values are set maintaining the aspect ratio of the image
        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight
                actualWidth = (imgRatio * actualWidth).toInt()
                actualHeight = maxHeight.toInt()
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth
                actualHeight = (imgRatio * actualHeight).toInt()
                actualWidth = maxWidth.toInt()
            } else {
                actualHeight = maxHeight.toInt()
                actualWidth = maxWidth.toInt()
            }
        }
        //      setting inSampleSize value allows to load a scaled down version of the original image
        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight)
        //      inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false
        //      this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true
        options.inInputShareable = true
        options.inTempStorage = ByteArray(16 * 1024)

        try {
            //          load the bitmap from its path
            bitmapImage = BitmapFactory.decodeFile(filePath, options)
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888)
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
        val ratioX = actualWidth / options.outWidth.toFloat()
        val ratioY = actualHeight / options.outHeight.toFloat()
        val middleX = actualWidth / 2.0f
        val middleY = actualHeight / 2.0f

        val scaleMatrix = Matrix()
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY)

        val canvas = Canvas(scaledBitmap!!)
        canvas.setMatrix(scaleMatrix)
        canvas.drawBitmap(
            bitmapImage,
            middleX - bitmapImage.width / 2,
            middleY - bitmapImage.height / 2,
            Paint(Paint.FILTER_BITMAP_FLAG)
        )

        //      check the rotation of the image and display it properly
        val exif: ExifInterface
        try {
            exif = ExifInterface(filePath)
            val orientation: Int = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION, 0
            )
            Log.d("EXIF", "Exif: $orientation")
            val matrix = Matrix()
            when (orientation) {
                6 -> {
                    matrix.postRotate(90F)
                    Log.d("EXIF", "Exif: $orientation")
                }
                3 -> {
                    matrix.postRotate(180F)
                    Log.d("EXIF", "Exif: $orientation")
                }
                8 -> {
                    matrix.postRotate(270F)
                    Log.d("EXIF", "Exif: $orientation")
                }
            }
            scaledBitmap = Bitmap.createBitmap(
                scaledBitmap, 0, 0,
                scaledBitmap.width, scaledBitmap.height, matrix,
                true
            )
        } catch (e: IOException) {
            e.printStackTrace()
        }

        /*var out: FileOutputStream? = null
        try {
            out = FileOutputStream(filePath)
            //write the compressed bitmap at the destination specified by filename.
            scaledBitmap!!.compress(Bitmap.CompressFormat.JPEG, 70, out)

            scaledBitmap = BitmapFactory.decodeFile(filePath, options)
        } catch (e: Exception) {
            e.printStackTrace()
        }*/
        //val nh = (bitmapImage.height * (512.0 / bitmapImage.width)).toInt()
        return scaledBitmap!!
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val heightRatio = Math.round(height.toFloat() / reqHeight.toFloat())
            val widthRatio = Math.round(width.toFloat() / reqWidth.toFloat())
            inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio
        }
        val totalPixels = (width * height).toFloat()
        val totalReqPixelsCap = (reqWidth * reqHeight * 2).toFloat()
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++
        }
        return inSampleSize
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ConsentFormFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ConsentFormFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        private fun takeScreenshot(view: View): Bitmap {
            view.isDrawingCacheEnabled = true
            view.buildDrawingCache(true)
            val b = Bitmap.createBitmap(view.drawingCache)
            view.isDrawingCacheEnabled = false
            return b
        }

        fun takeScreenshotOfRootView(v: View): Bitmap {
            return takeScreenshot(v)
        }

        const val TAG = "Screen_consent_letter"
    }

    override fun onStartSigning() {
        bSigned = true
    }

    override fun onSigned() {
        bSigned = true
    }

    override fun onClear() {
        bSigned = false
    }
}