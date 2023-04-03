package com.app.selfcare.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.selfcare.R
import com.app.selfcare.adapters.AppointmentsAdapter
import com.app.selfcare.controller.OnAppointmentItemClickListener
import com.app.selfcare.data.AppointmentPatientId
import com.app.selfcare.data.GetAppointment
import com.app.selfcare.data.GetAppointmentList
import com.app.selfcare.databinding.FragmentActivityCarePlanBinding
import com.app.selfcare.databinding.FragmentPastAppointmentBinding
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper.get
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import java.lang.reflect.Type

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PastAppointmentFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PastAppointmentFragment : BaseFragment(), OnAppointmentItemClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentPastAppointmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPastAppointmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getLayout(): Int {
        return R.layout.fragment_past_appointment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE

        displayUpcomingAppointments()
    }

    private fun displayUpcomingAppointments() {
        getAppointmentList { response ->
            val appointmentType: Type = object : TypeToken<GetAppointmentList?>() {}.type
            val appointmentList: GetAppointmentList =
                Gson().fromJson(response, appointmentType)
            if (appointmentList.past.isNotEmpty()) {
                binding.recyclerViewPastAppointmentList.visibility = View.VISIBLE
                binding.txtNoPastAppointmentList.visibility = View.GONE
                binding.recyclerViewPastAppointmentList.apply {
                    layoutManager =
                        LinearLayoutManager(mActivity!!, LinearLayoutManager.VERTICAL, false)
                    adapter = AppointmentsAdapter(
                        mActivity!!,
                        appointmentList.past, "", this@PastAppointmentFragment
                    )
                }
            } else {
                binding.recyclerViewPastAppointmentList.visibility = View.GONE
                binding.txtNoPastAppointmentList.visibility = View.VISIBLE
            }
        }
    }

    private fun getAppointmentList(myCallback: (result: String?) -> Unit) {
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .getAppointmentList(
                        AppointmentPatientId(preference!![PrefKeys.PREF_PATIENT_ID, ""]!!.toInt()),
                        getAccessToken()
                    )
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ result ->
                        try {
                            hideProgress()
                            binding.shimmerPastAppointmentList.stopShimmer()
                            binding.shimmerPastAppointmentList.visibility = View.GONE
                            var responseBody = result.string()
                            Log.d("Response Body", responseBody)
                            val respBody = responseBody.split("|")
                            val status = respBody[1]
                            responseBody = respBody[0]
                            myCallback.invoke(responseBody)
                        } catch (e: Exception) {
                            hideProgress()
                            binding.shimmerPastAppointmentList.stopShimmer()
                            binding.shimmerPastAppointmentList.visibility = View.GONE
                            e.printStackTrace()
                            displayToast("Something went wrong.. Please try after sometime")
                        }
                    }, { error ->
                        hideProgress()
                        if ((error as HttpException).code() == 401) {
                            userLogin(
                                preference!![PrefKeys.PREF_EMAIL]!!,
                                preference!![PrefKeys.PREF_PASS]!!
                            ) { result ->
                                getAppointmentList(myCallback)
                            }
                        } else {
                            binding.shimmerPastAppointmentList.stopShimmer()
                            binding.shimmerPastAppointmentList.visibility = View.GONE
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
         * @return A new instance of fragment PastAppointmentFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String = "") =
            PastAppointmentFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_Past_Appointments"
    }

    override fun onAppointmentItemClickListener(
        appointment: GetAppointment,
        isStartAppointment: Boolean
    ) {

    }
}