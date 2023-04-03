package com.app.selfcare.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.app.selfcare.R
import com.app.selfcare.data.*
import com.app.selfcare.databinding.FragmentActivityCarePlanBinding
import com.app.selfcare.databinding.FragmentNotificationSettingsBinding
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
    private lateinit var binding: FragmentNotificationSettingsBinding

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
        binding = FragmentNotificationSettingsBinding.inflate(inflater, container, false)
        return binding.root
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
        binding.notificationSettingsBack.setOnClickListener {
            updateNotificationData()
        }

        binding.layoutProviderMatched.setOnClickListener {
            binding.switchProviderMatched.isChecked = !binding.switchProviderMatched.isChecked
        }

        binding.layoutProviderAvailable.setOnClickListener {
            binding.switchProviderAvailable.isChecked = !binding.switchProviderAvailable.isChecked
        }

        binding.layoutAppointmentCancelled.setOnClickListener {
            binding.switchAppointmentCancelled.isChecked = !binding.switchAppointmentCancelled.isChecked
        }

        binding.layoutAppointmentStarting.setOnClickListener {
            binding.switchAppointmentStarting.isChecked = !binding.switchAppointmentStarting.isChecked
        }

        binding.layoutResourceAvailable.setOnClickListener {
            binding.switchResourceAvailable.isChecked = !binding.switchResourceAvailable.isChecked
        }

        binding.layoutNewTaskAvailable.setOnClickListener {
            binding.switchNewTaskAvailable.isChecked = !binding.switchNewTaskAvailable.isChecked
        }

        binding.layoutTaskCompletion.setOnClickListener {
            binding.switchTaskCompletion.isChecked = !binding.switchTaskCompletion.isChecked
        }

        binding.layoutCarePlanAppointmentStarting.setOnClickListener {
            binding.switchCarePlanAppointmentStarting.isChecked =
                !binding.switchCarePlanAppointmentStarting.isChecked
        }
    }

    private fun onSwitchChangeEvents() {
        binding.switchProviderMatched.setOnCheckedChangeListener { _, isChecked ->
            providerMatched = isChecked
        }

        binding.switchProviderAvailable.setOnCheckedChangeListener { buttonView, isChecked ->
            providerAvailable = isChecked
        }

        binding.switchAppointmentCancelled.setOnCheckedChangeListener { buttonView, isChecked ->
            appointmentCancelled = isChecked
        }

        binding.switchAppointmentStarting.setOnCheckedChangeListener { buttonView, isChecked ->
            appointmentStarting = isChecked
        }

        binding.switchResourceAvailable.setOnCheckedChangeListener { buttonView, isChecked ->
            resourceAvailable = isChecked
        }

        binding.switchNewTaskAvailable.setOnCheckedChangeListener { buttonView, isChecked ->
            newTaskAvailable = isChecked
        }

        binding.switchTaskCompletion.setOnCheckedChangeListener { buttonView, isChecked ->
            taskCompletion = isChecked
        }

        binding.switchCarePlanAppointmentStarting.setOnCheckedChangeListener { buttonView, isChecked ->
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
        binding.switchProviderMatched.isChecked = notificationSettings.provider
        binding.switchProviderAvailable.isChecked = notificationSettings.provider
        binding.switchAppointmentCancelled.isChecked = notificationSettings.appointment_cancelled
        binding.switchAppointmentStarting.isChecked = notificationSettings.appointment_started
        binding.switchResourceAvailable.isChecked = notificationSettings.wellness
        binding.switchNewTaskAvailable.isChecked = notificationSettings.task_assigned
        binding.switchTaskCompletion.isChecked = notificationSettings.task_completed
        binding.switchCarePlanAppointmentStarting.isChecked = notificationSettings.appointment_started
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
                            binding.switchAppointmentStarting.isChecked,
                            binding.switchAppointmentCancelled.isChecked,
                            appointment_completed = false,
                            consent = false,
                            provider = binding.switchProviderMatched.isChecked,
                            task_assigned = binding.switchNewTaskAvailable.isChecked,
                            task_completed = binding.switchTaskCompletion.isChecked,
                            task_missed = false,
                            email_notification = false,
                            wellness = binding.switchResourceAvailable.isChecked
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