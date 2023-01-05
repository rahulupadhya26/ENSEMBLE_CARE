package com.app.selfcare.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.app.selfcare.R
import com.app.selfcare.data.*
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper.get
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_detail_journal.*
import kotlinx.android.synthetic.main.fragment_notification_settings.*
import retrofit2.HttpException
import java.lang.reflect.Type

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [NotificationSettingsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class NotificationSettingsFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var providerMatched: Boolean = false
    private var providerAvailable: Boolean = false
    private var appointmentCancelled: Boolean = false
    private var appointmentStarting: Boolean = false
    private var resourceAvailable: Boolean = false
    private var newTaskAvailable: Boolean = false
    private var taskCompletion: Boolean = false
    private var carePlanAppointmentStarting: Boolean = false
    private var notificationId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_notification_settings
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE
        updateStatusBarColor(R.color.white)

        onClickEvents()

        onSwitchChangeEvents()

        getNotificationSettingsData()
    }

    private fun onClickEvents() {
        notificationSettingsBack.setOnClickListener {
            updateNotificationData()
        }

        layoutProviderMatched.setOnClickListener {
            switchProviderMatched.isChecked = !switchProviderMatched.isChecked
        }

        layoutProviderAvailable.setOnClickListener {
            switchProviderAvailable.isChecked = !switchProviderAvailable.isChecked
        }

        layoutAppointmentCancelled.setOnClickListener {
            switchAppointmentCancelled.isChecked = !switchAppointmentCancelled.isChecked
        }

        layoutAppointmentStarting.setOnClickListener {
            switchAppointmentStarting.isChecked = !switchAppointmentStarting.isChecked
        }

        layoutResourceAvailable.setOnClickListener {
            switchResourceAvailable.isChecked = !switchResourceAvailable.isChecked
        }

        layoutNewTaskAvailable.setOnClickListener {
            switchNewTaskAvailable.isChecked = !switchNewTaskAvailable.isChecked
        }

        layoutTaskCompletion.setOnClickListener {
            switchTaskCompletion.isChecked = !switchTaskCompletion.isChecked
        }

        layoutCarePlanAppointmentStarting.setOnClickListener {
            switchCarePlanAppointmentStarting.isChecked =
                !switchCarePlanAppointmentStarting.isChecked
        }
    }

    private fun onSwitchChangeEvents() {
        switchProviderMatched.setOnCheckedChangeListener { _, isChecked ->
            providerMatched = isChecked
        }

        switchProviderAvailable.setOnCheckedChangeListener { buttonView, isChecked ->
            providerAvailable = isChecked
        }

        switchAppointmentCancelled.setOnCheckedChangeListener { buttonView, isChecked ->
            appointmentCancelled = isChecked
        }

        switchAppointmentStarting.setOnCheckedChangeListener { buttonView, isChecked ->
            appointmentStarting = isChecked
        }

        switchResourceAvailable.setOnCheckedChangeListener { buttonView, isChecked ->
            resourceAvailable = isChecked
        }

        switchNewTaskAvailable.setOnCheckedChangeListener { buttonView, isChecked ->
            newTaskAvailable = isChecked
        }

        switchTaskCompletion.setOnCheckedChangeListener { buttonView, isChecked ->
            taskCompletion = isChecked
        }

        switchCarePlanAppointmentStarting.setOnCheckedChangeListener { buttonView, isChecked ->
            carePlanAppointmentStarting = isChecked
        }
    }

    private fun getNotificationSettingsData() {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .getNotificationSettingsData(
                        "PI0059",
                        ClientId(preference!![PrefKeys.PREF_PATIENT_ID, ""]!!.toInt()),
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
                            val notificationSettingsType: Type =
                                object : TypeToken<ArrayList<NotificationSettings?>?>() {}.type
                            val notificationSettings: ArrayList<NotificationSettings> =
                                Gson().fromJson(responseBody, notificationSettingsType)
                            displayNotificationSettingsData(notificationSettings[0])
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
                                getNotificationSettingsData()
                            }
                        } else {
                            displayAfterLoginErrorMsg(error)
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    private fun displayNotificationSettingsData(notificationSettings: NotificationSettings) {
        notificationId = notificationSettings.id
        switchProviderMatched.isChecked = notificationSettings.provider
        switchProviderAvailable.isChecked = notificationSettings.provider
        switchAppointmentCancelled.isChecked = notificationSettings.appointment_cancelled
        switchAppointmentStarting.isChecked = notificationSettings.appointment_started
        switchResourceAvailable.isChecked = notificationSettings.wellness
        switchNewTaskAvailable.isChecked = notificationSettings.task_assigned
        switchTaskCompletion.isChecked = notificationSettings.task_completed
        switchCarePlanAppointmentStarting.isChecked = notificationSettings.appointment_started
    }

    private fun updateNotificationData() {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .updateNotificationSettingsData(
                        "PI0059",
                        UpdateNotificationSettings(
                            notificationId,
                            preference!![PrefKeys.PREF_PATIENT_ID, ""]!!.toInt(),
                            appointment_created = false,
                            switchAppointmentStarting.isChecked,
                            switchAppointmentCancelled.isChecked,
                            appointment_completed = false,
                            consent = false,
                            provider = switchProviderMatched.isChecked,
                            task_assigned = switchNewTaskAvailable.isChecked,
                            task_completed = switchTaskCompletion.isChecked,
                            task_missed = false,
                            email_notification = false,
                            wellness = switchResourceAvailable.isChecked
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
                            popBackStack()
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
                                updateNotificationData()
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
         * @return A new instance of fragment NotificationSettingsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            NotificationSettingsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_notification_settings"
    }
}