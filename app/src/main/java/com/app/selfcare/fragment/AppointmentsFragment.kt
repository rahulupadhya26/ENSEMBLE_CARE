package com.app.selfcare.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.R
import com.app.selfcare.adapters.AppointmentsAdapter
import com.app.selfcare.controller.OnAppointmentItemClickListener
import com.app.selfcare.data.Appointment
import com.app.selfcare.data.AppointmentPatientId
import com.app.selfcare.data.CancelAppointment
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper.get
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_appointments.*
import kotlinx.android.synthetic.main.fragment_dashboard.*
import org.json.JSONArray
import retrofit2.HttpException
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AppointmentsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AppointmentsFragment : BaseFragment(), OnAppointmentItemClickListener {
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
        return R.layout.fragment_appointments
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.VISIBLE
        getSubTitle().visibility = View.GONE

        //Appointments
        displayAppointments()

        refresh.setOnClickListener {
            displayAppointments()
        }
    }

    private fun displayAppointments() {
        val appointmentLists: ArrayList<Appointment> = arrayListOf()
        getAppointmentList { response ->
            val jsonArr = JSONArray(response)
            if (jsonArr.length() != 0) {
                for (i in 0 until jsonArr.length()) {
                    val appointmentObj = jsonArr.getJSONObject(i)
                    val appointment = Appointment(
                        appointmentObj.getInt("appointment_id"),
                        appointmentObj.getString("date"),
                        appointmentObj.getBoolean("is_book"),
                        appointmentObj.getString("type_of_visit"),
                        appointmentObj.getString("booking_date"),
                        appointmentObj.getJSONObject("doctor").getString("doctor_id"),
                        appointmentObj.getJSONObject("doctor").getString("ssn"),
                        appointmentObj.getJSONObject("doctor").getString("first_name"),
                        appointmentObj.getJSONObject("doctor").getString("middle_name"),
                        appointmentObj.getJSONObject("doctor").getString("last_name"),
                        appointmentObj.getJSONObject("doctor").getString("doctor_type"),
                        appointmentObj.getJSONObject("doctor").getString("dob"),
                        appointmentObj.getJSONObject("doctor").getString("qualification"),
                        appointmentObj.getJSONObject("doctor").getString("years_of_experiance"),
                        appointmentObj.getJSONObject("doctor").getString("gender"),
                        appointmentObj.getJSONObject("doctor").getString("practice_state"),
                        appointmentObj.getJSONObject("time_slot").getString("time_slot_id"),
                        appointmentObj.getJSONObject("time_slot").getString("starting_time"),
                        appointmentObj.getJSONObject("time_slot").getString("ending_time"),
                        appointmentObj.getJSONObject("meeting").getString("meeting_id"),
                        appointmentObj.getJSONObject("meeting").getString("appointment"),
                        appointmentObj.getJSONObject("meeting").getString("doctor"),
                        appointmentObj.getJSONObject("meeting").getString("patient"),
                        appointmentObj.getJSONObject("meeting").getString("meeting_title"),
                        appointmentObj.getJSONObject("meeting").getString("channel_name"),
                        appointmentObj.getJSONObject("meeting").getString("rtc_token"),
                        appointmentObj.getJSONObject("meeting").getString("rtm_token"),
                        appointmentObj.getJSONObject("meeting").getString("rtm_token_doctor"),
                        appointmentObj.getJSONObject("meeting").getString("meeting_date"),
                        appointmentObj.getJSONObject("meeting").getString("duration")
                    )
                    appointmentLists.add(appointment)
                }
            }
            if (appointmentLists.isNotEmpty()) {
                recyclerViewAppointmentList.visibility = View.VISIBLE
                txtNoAppointmentList.visibility = View.GONE
                recyclerViewAppointmentList.apply {
                    layoutManager = LinearLayoutManager(mActivity!!, RecyclerView.VERTICAL, false)
                    adapter = AppointmentsAdapter(
                        mActivity!!,
                        appointmentLists, this@AppointmentsFragment
                    )
                }
            } else {
                recyclerViewAppointmentList.visibility = View.GONE
                txtNoAppointmentList.visibility = View.VISIBLE
            }
        }
    }

    private fun getAppointmentList(myCallback: (result: String?) -> Unit) {
        showProgress()
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
                            var responseBody = result.string()
                            Log.d("Response Body", responseBody)
                            val respBody = responseBody.split("|")
                            val status = respBody[1]
                            responseBody = respBody[0]
                            myCallback.invoke(responseBody)
                        } catch (e: Exception) {
                            hideProgress()
                            displayToast("Something went wrong.. Please try after sometime")
                        }
                    }, { error ->
                        hideProgress()
                        if ((error as HttpException).code() == 401) {
                            userLogin(
                                preference!![PrefKeys.PREF_EMAIL]!!,
                                preference!![PrefKeys.PREF_PASS]!!
                            ) { result ->
                                displayAppointments()
                            }
                        } else {
                            displayAfterLoginErrorMsg(error)
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    private fun cancelAppointment(appointment: Appointment) {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .cancelAppointment(
                        CancelAppointment(
                            preference!![PrefKeys.PREF_PATIENT_ID, ""]!!.toInt(),
                            appointment.appointment_id
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
                            displayAppointments()
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
                                cancelAppointment(appointment)
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
         * @return A new instance of fragment AppointmentsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AppointmentsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
        const val TAG = "Screen_Appointments"
    }

    override fun onAppointmentItemClickListener(
        appointment: Appointment,
        isStartAppointment: Boolean
    ) {
        if (isStartAppointment) {
            //Start video call
            replaceFragment(
                VideoCallFragment.newInstance(appointment),
                R.id.layout_home,
                VideoCallFragment.TAG
            )
        } else {
            val builder = AlertDialog.Builder(requireActivity())
            builder.setTitle("Alert")
            builder.setMessage("Do you really want to cancel this appointment?")
            builder.setPositiveButton("Yes") { dialogInterface, _ ->
                dialogInterface.dismiss()
                //Cancel the appointment
                cancelAppointment(appointment)
            }
            //performing cancel action
            builder.setNeutralButton("Cancel") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            val alert = builder.create()
            alert.setCancelable(false)
            alert.show()
        }
    }
}