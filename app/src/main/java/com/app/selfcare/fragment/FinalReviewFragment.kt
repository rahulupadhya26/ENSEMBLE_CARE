package com.app.selfcare.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.R
import com.app.selfcare.adapters.FinalReviewAdapter
import com.app.selfcare.data.AppointmentReq
import com.app.selfcare.data.TimeSlots
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper.get
import com.app.selfcare.utils.Utils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_final_review.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FinalReviewFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FinalReviewFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_final_review
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.VISIBLE
        getSubTitle().visibility = View.GONE

        val finalReviewList = ArrayList<String>()
        finalReviewList.add("Need consultation for - Myself")
        /*finalReviewList.add(
            "My location is - " + Utils.selectedStreet + "," +
                    Utils.selectedCity + "," +
                    Utils.selectedState + "," +
                    Utils.selectedPostalCode + "," +
                    Utils.selectedCountry
        )*/
        finalReviewList.add("Therapist - Dr. " + Utils.providerName)
        finalReviewList.add("Phone# - " + preference!![PrefKeys.PREF_PHONE_NO, ""]!!)
        finalReviewList.add("Would like to talk to - " + Utils.providerType)
        finalReviewList.add("Mode of communications - " + Utils.selectedCommunicationMode)
        finalReviewList.add("Tentative date of appointment - " + Utils.aptScheduleDate)
        //finalReviewList.add("Appointment duration would be for - 30 mins")
        finalReviewList.add("Tentative time of appointment - " + Utils.aptScheduleTime)
        //finalReviewList.add("Chosen pharmacy - " + Utils.pharmacyVal)
        //finalReviewList.add("Details - " + Utils.details)
        //finalReviewList.add("Symptoms - " + Utils.selectedSymptoms)
        //finalReviewList.add("Medication - " + Utils.medication)
        //finalReviewList.add("Allergies - " + Utils.allergies)

        recyclerViewFinalReview.apply {
            layoutManager = LinearLayoutManager(
                mActivity!!,
                RecyclerView.VERTICAL,
                false
            )
            adapter = FinalReviewAdapter(
                mActivity!!,
                finalReviewList
            )
        }

        btnFinalReview.setOnClickListener {
            if (checkbox_terms_conditions.isChecked) {
                //Call book appointment api
                callBookAppointmentApi()
                /*replaceFragmentNoBackStack(
                    ConsentFormFragment(),
                    R.id.layout_home,
                    ConsentFormFragment.TAG
                )*/
            } else {
                val builder = AlertDialog.Builder(mActivity!!)
                builder.setTitle("Message")
                builder.setMessage("Please select terms and conditions for further procedure")
                builder.setPositiveButton("OK") { dialog, which ->
                    dialog.dismiss()
                }
                builder.setCancelable(false)
                builder.show()
            }
        }
    }

    private fun callBookAppointmentApi() {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .bookAppointment(
                        "PI0040",
                        AppointmentReq(
                            Utils.appointmentId,
                            preference!![PrefKeys.PREF_PATIENT_ID, ""]!!,
                            true,
                            Utils.aptScheduleDate,
                            Utils.selectedCommunicationMode
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
                            replaceFragmentNoBackStack(
                                ConsentFormFragment(),
                                R.id.layout_home,
                                ConsentFormFragment.TAG
                            )
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

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FinalReviewFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FinalReviewFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_Therapy_Final_Review"
    }
}