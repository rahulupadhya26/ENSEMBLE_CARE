package com.app.selfcare.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.selfcare.GroupVideoCall
import com.app.selfcare.R
import com.app.selfcare.adapters.AppointmentsAdapter
import com.app.selfcare.controller.OnAppointmentItemClickListener
import com.app.selfcare.data.AppointmentPatientId
import com.app.selfcare.data.GetAppointment
import com.app.selfcare.data.GetAppointmentList
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper.get
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_today_appointment.*
import org.json.JSONObject
import retrofit2.HttpException
import java.lang.reflect.Type

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [TodayAppointmentFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TodayAppointmentFragment : BaseFragment(), OnAppointmentItemClickListener {
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
        return R.layout.fragment_today_appointment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE

        displayTodayAppointments()
    }

    private fun displayTodayAppointments() {
        getAppointmentList { response ->
            val appointmentType: Type = object : TypeToken<GetAppointmentList?>() {}.type
            val appointmentList: GetAppointmentList =
                Gson().fromJson(response, appointmentType)
            if (appointmentList.today.isNotEmpty()) {
                recyclerViewTodayAppointmentList.visibility = View.VISIBLE
                txtNoTodayAppointmentList.visibility = View.GONE
                recyclerViewTodayAppointmentList.apply {
                    layoutManager =
                        LinearLayoutManager(mActivity!!, LinearLayoutManager.VERTICAL, false)
                    adapter = AppointmentsAdapter(
                        mActivity!!,
                        appointmentList.today, "Today", this@TodayAppointmentFragment
                    )
                }
            } else {
                recyclerViewTodayAppointmentList.visibility = View.GONE
                txtNoTodayAppointmentList.visibility = View.VISIBLE
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
                                getAppointmentList(myCallback)
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
         * @return A new instance of fragment TodayAppointmentFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            TodayAppointmentFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_Today_Appointments"
    }

    override fun onAppointmentItemClickListener(
        appointment: GetAppointment,
        isStartAppointment: Boolean
    ) {
        if (isStartAppointment) {
            try {
                if (appointment.is_group_appointment) {
                    val intent = Intent(requireActivity(), GroupVideoCall::class.java)
                    intent.putExtra("token", appointment.rtc_token)
                    intent.putExtra("channelName", appointment.channel_name)
                    startActivity(intent)
                    requireActivity().overridePendingTransition(0, 0)
                } else {
                    getToken(appointment) { response ->
                        val jsonObj = JSONObject(response)
                        val rtcToken = jsonObj.getString("rtc_token")
                        val rtmToken = jsonObj.getString("rtm_token")
                        val channelName = jsonObj.getString("channel_name")
                        if (appointment.is_group_appointment) {
                            val intent = Intent(requireActivity(), GroupVideoCall::class.java)
                            intent.putExtra("token", rtcToken)
                            intent.putExtra("channelName", channelName)
                            startActivity(intent)
                            requireActivity().overridePendingTransition(0, 0)
                        } else {
                            //Start video call
                            replaceFragment(
                                VideoCallFragment.newInstance(
                                    appointment,
                                    rtcToken,
                                    rtmToken,
                                    channelName
                                ),
                                R.id.layout_home,
                                VideoCallFragment.TAG
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            /*val builder = AlertDialog.Builder(requireActivity())
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
            alert.show()*/
        }
    }
}