package com.app.selfcare.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import com.app.selfcare.R
import com.app.selfcare.data.AppointmentReq
import com.app.selfcare.data.Feedback
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper.get
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_service_feedback.*
import kotlinx.android.synthetic.main.fragment_therapist_feedback.*
import retrofit2.HttpException

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val ARG_PARAM3 = "param3"

/**
 * A simple [Fragment] subclass.
 * Use the [ServiceFeedbackFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ServiceFeedbackFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var therapistRating: String? = null
    private var feedbackOnTherapist: String? = null
    private var serviceFeedbackRating: String? = null
    private var apptId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            therapistRating = it.getString(ARG_PARAM1)
            feedbackOnTherapist = it.getString(ARG_PARAM2)
            apptId = it.getString(ARG_PARAM3)
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_service_feedback
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE

        /*ratingBarServiceRating.onRatingBarChangeListener =
            RatingBar.OnRatingBarChangeListener { p0, p1, p2 ->
                serviceFeedbackRating = p1.toString()
            }*/

        btnServiceFeedback.setOnClickListener {
            if (serviceFeedbackRating != null) {
                if (getText(editTxtServiceFeedback).isNotEmpty()) {
                    sendFeedback()
                } else {
                    displayMsg("Alert", "Please provide feedback")
                }
            } else {
                displayMsg("Alert", "Please give rating")
            }
        }
    }

    private fun sendFeedback() {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .sendFeedback(
                        "PI0045",
                        Feedback(
                            preference!![PrefKeys.PREF_PATIENT_ID, ""]!!.toInt(),
                            apptId!!.toInt(),
                            therapistRating!!.toDouble(),
                            feedbackOnTherapist!!,
                            serviceFeedbackRating!!.toDouble(),
                            getText(editTxtServiceFeedback)
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
                            if (status == "201") {
                                for (i in 0 until mActivity!!.supportFragmentManager.backStackEntryCount) {
                                    if (mActivity!!.getCurrentFragment() !is DashboardFragment) {
                                        popBackStack()
                                    } else {
                                        break
                                    }
                                }
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
                                sendFeedback()
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

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ServiceFeedbackFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String, param3: String) =
            ServiceFeedbackFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                    putString(ARG_PARAM3, param3)
                }
            }

        const val TAG = "Screen_service_feedback"
    }
}