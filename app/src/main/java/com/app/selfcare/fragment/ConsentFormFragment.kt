package com.app.selfcare.fragment

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import okhttp3.MultipartBody
import retrofit2.HttpException
import java.io.ByteArrayOutputStream

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
        getBackButton().visibility = View.GONE
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
            replaceFragmentNoBackStack(
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
        txt_form_upload.text = "Uploading details to server"
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
                            "data:image/jpg;base64," + convert(form)
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
                                val description =
                                    "Type of Visit : " + Utils.selectedCommunicationMode + "\n" +
                                            "Doctor Name : Dr. " + Utils.providerName + "\n" +
                                            "Time slot : " + Utils.aptScheduleTime
                                txt_form_upload.text = "Form uploaded successfully"
                                //val appointmentDate = DateUtils("$bookingDate 01:00:00")
                                val durationData = resources.getStringArray(R.array.goal_duration)
                                CalenderUtils.addEvent(
                                    requireActivity(),
                                    "$bookingDate 00:00:00",
                                    "Appointment",
                                    description,
                                    durationData[0], "30", Utils.aptScheduleTime.take(2),
                                    30
                                )
                            } else {
                                txt_form_upload.text = "Failed to upload form"
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
                                createAppointmentApi(
                                    apptId,
                                    bookingDate,
                                    visitType,
                                    status,
                                    form
                                )
                            }
                        } else {
                            displayAfterLoginErrorMsg(error)
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
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