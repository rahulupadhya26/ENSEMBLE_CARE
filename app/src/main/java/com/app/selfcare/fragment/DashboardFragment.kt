package com.app.selfcare.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.Window
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.app.selfcare.BaseActivity
import com.app.selfcare.BuildConfig
import com.app.selfcare.GroupVideoCall
import com.app.selfcare.R
import com.app.selfcare.adapters.*
import com.app.selfcare.controller.*
import com.app.selfcare.data.*
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper.get
import com.app.selfcare.utils.DateUtils
import com.app.selfcare.utils.Utils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.dialog_appointment_cancelled_alert.*
import kotlinx.android.synthetic.main.dialog_plan_subscription_alert.*
import kotlinx.android.synthetic.main.fragment_dashboard.*
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.HttpException
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

enum class FitActionRequestCode {
    SUBSCRIBE,
    READ_DATA
}

/**
 * A simple [Fragment] subclass.
 * Use the [DashboardFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DashboardFragment : BaseFragment(), OnAppointmentItemClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private val fitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE)
        .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
        .addDataType(DataType.TYPE_DISTANCE_DELTA)
        .addDataType(DataType.TYPE_HEART_RATE_BPM)
        .addDataType(DataType.TYPE_CALORIES_EXPENDED)
        .build()

    private var GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 1

    private val runningQOrLater =
        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q

    private var subscriptionStatusDialog: Dialog? = null
    private var apptCancelledAlertDialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_dashboard
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE
        mActivity!!.setUserDetails()
        updateStatusBarColor(R.color.screen_background_color)
        checkPermissionsAndRun(FitActionRequestCode.SUBSCRIBE)

        //Show Good morning, afternoon, evening or night message to user.
        showMessageToUser()

        txtUserName.text = preference!![PrefKeys.PREF_FNAME, ""] + " " +
                preference!![PrefKeys.PREF_LNAME, ""]

        onClickEvents()

        displayAppointments()

        displayDashboardNotifications()

        itemsSwipeToRefresh.setOnRefreshListener {
            try {
                displayAppointments()
                displayDashboardNotifications()
                val photo = preference!![PrefKeys.PREF_PHOTO, ""]!!
                if (photo != "null" && photo.isNotEmpty()) {
                    Glide.with(requireActivity())
                        .load(BaseActivity.baseURL.dropLast(5) + photo)
                        .placeholder(R.drawable.user_pic)
                        .transform(CenterCrop(), RoundedCorners(5))
                        .into(img_user_pic)
                } else {
                    img_user_pic.setImageResource(R.drawable.user_pic)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun displayAppointmentCancelledAlert(jsonObj: JSONObject) {
        val cancelAppointmentNotifyType: Type =
            object : TypeToken<CancelledAppointmentNotify?>() {}.type
        val cancelAppointmentNotify: CancelledAppointmentNotify =
            Gson().fromJson(jsonObj.toString(), cancelAppointmentNotifyType)
        updateNotificationStatus(cancelAppointmentNotify.id)
        apptCancelledAlertDialog = Dialog(requireActivity())
        apptCancelledAlertDialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        apptCancelledAlertDialog!!.setCancelable(false)
        apptCancelledAlertDialog!!.setCanceledOnTouchOutside(false)
        apptCancelledAlertDialog!!.setContentView(R.layout.dialog_appointment_cancelled_alert)

        val appointmentDate =
            DateUtils(cancelAppointmentNotify.extra_data.prev_appt_details.booking_date + " 00:00:00")

        apptCancelledAlertDialog!!.txtCancelledAppointTherapistName.text =
            cancelAppointmentNotify.extra_data.prev_appt_details.doctor.name

        apptCancelledAlertDialog!!.txtCancelledAppointTherapistType.text =
            cancelAppointmentNotify.extra_data.prev_appt_details.doctor.designation

        apptCancelledAlertDialog!!.txtCancelledAppointmentDateTime.text =
            appointmentDate.getCurrentDay() + ", " +
                    appointmentDate.getDay() + " " +
                    appointmentDate.getMonth() + " at " +
                    cancelAppointmentNotify.extra_data.prev_appt_details.time_slot.starting_time.dropLast(
                        3
                    ) + " - " +
                    cancelAppointmentNotify.extra_data.prev_appt_details.time_slot.ending_time.dropLast(
                        3
                    )

        if (cancelAppointmentNotify.extra_data.prev_appt_details.type_of_visit == "Video") {
            apptCancelledAlertDialog!!.cancelledAppointmentCall.setImageResource(R.drawable.video)
            apptCancelledAlertDialog!!.cancelledAppointmentCall.imageTintList =
                ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireActivity(),
                        R.color.primaryGreen
                    )
                )
        } else {
            apptCancelledAlertDialog!!.cancelledAppointmentCall.setImageResource(R.drawable.telephone)
            apptCancelledAlertDialog!!.cancelledAppointmentCall.imageTintList =
                ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireActivity(),
                        R.color.primaryGreen
                    )
                )
        }

        apptCancelledAlertDialog!!.cancelledAppointImgUser.visibility = View.VISIBLE
        apptCancelledAlertDialog!!.cancelledAppointGroupImg.visibility = View.GONE
        Glide.with(requireActivity())
            .load(BaseActivity.baseURL.dropLast(5) + cancelAppointmentNotify.extra_data.prev_appt_details.doctor.photo)
            .placeholder(R.drawable.doctor_img)
            .transform(CenterCrop(), RoundedCorners(5))
            .into(apptCancelledAlertDialog!!.cancelledAppointImgUser)

        apptCancelledAlertDialog!!.cardViewApptCancelledReschedule.setOnClickListener {
            apptCancelledAlertDialog!!.dismiss()
            Utils.providerId = cancelAppointmentNotify.extra_data.next_appt_detials.doctor.id.toString()
            Utils.providerType = cancelAppointmentNotify.extra_data.next_appt_detials.doctor.designation
            Utils.providerName = cancelAppointmentNotify.extra_data.next_appt_detials.doctor.name
            Utils.aptScheduleDate = cancelAppointmentNotify.extra_data.next_appt_detials.date
            Utils.aptScheduleTime = cancelAppointmentNotify.extra_data.next_appt_detials.time_slot.starting_time
            Utils.appointmentId = cancelAppointmentNotify.extra_data.next_appt_detials.appointment_id.toString()
            replaceFragment(
                TherapyBasicDetailsCFragment(),
                R.id.layout_home,
                TherapyBasicDetailsCFragment.TAG
            )
        }

        apptCancelledAlertDialog!!.cardViewTryDiffProvider.setOnClickListener {
            apptCancelledAlertDialog!!.dismiss()
            Utils.isTherapististScreen = false
            clearTempFormData()
            replaceFragment(
                ClientAvailabilityFragment.newInstance(false),
                R.id.layout_home,
                ClientAvailabilityFragment.TAG
            )
        }
        if (apptCancelledAlertDialog!!.isShowing) {
            apptCancelledAlertDialog!!.dismiss()
        }
        apptCancelledAlertDialog!!.show()
    }

    @SuppressLint("SetTextI18n")
    private fun displayPlanSubscriptionAlert(jsonObj: JSONObject) {
        val subscriptionStatusNotifyType: Type =
            object : TypeToken<SubscriptionStatus?>() {}.type
        val subscriptionStatusNotify: SubscriptionStatus =
            Gson().fromJson(jsonObj.toString(), subscriptionStatusNotifyType)
        if (!subscriptionStatusNotify.extra_data.is_active) {
            subscriptionStatusDialog = Dialog(requireActivity())
            subscriptionStatusDialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
            subscriptionStatusDialog!!.setCancelable(false)
            subscriptionStatusDialog!!.setCanceledOnTouchOutside(false)
            subscriptionStatusDialog!!.setContentView(R.layout.dialog_plan_subscription_alert)
            subscriptionStatusDialog!!.cardViewRenewSubscription.setOnClickListener {
                replaceFragmentNoBackStack(
                    RegisterPartCFragment(),
                    R.id.layout_home,
                    RegisterPartCFragment.TAG
                )
            }
            if (subscriptionStatusDialog!!.isShowing) {
                subscriptionStatusDialog!!.dismiss()
            }
            subscriptionStatusDialog!!.show()
        }
    }

    private fun showMessageToUser() {
        val c = Calendar.getInstance()
        val timeOfDay = c[Calendar.HOUR_OF_DAY]
        if (timeOfDay in 0..11) {
            txtShowMessageToUser.text = "Good Morning,"
        } else if (timeOfDay in 12..15) {
            txtShowMessageToUser.text = "Good Afternoon,"
        } else if (timeOfDay in 16..19) {
            txtShowMessageToUser.text = "Good Evening,"
        } else if (timeOfDay in 20..23) {
            txtShowMessageToUser.text = "Good Night,"
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            displayAppointments()
            val photo = preference!![PrefKeys.PREF_PHOTO, ""]!!
            if (photo != "null" && photo.isNotEmpty()) {
                Glide.with(requireActivity())
                    .load(BaseActivity.baseURL.dropLast(5) + photo)
                    .placeholder(R.drawable.user_pic)
                    .transform(CenterCrop(), RoundedCorners(5))
                    .into(img_user_pic)
            } else {
                img_user_pic.setImageResource(R.drawable.user_pic)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun onClickEvents() {
        img_user_pic.setOnClickListener {
            replaceFragment(
                SettingsFragment(),
                R.id.layout_home,
                SettingsFragment.TAG
            )
        }

        layoutUserName.setOnClickListener {
            replaceFragment(
                SettingsFragment(),
                R.id.layout_home,
                SettingsFragment.TAG
            )
        }

        layoutAppointments.setOnClickListener {
            replaceFragment(
                AppointmentsFragment(),
                R.id.layout_home,
                AppointmentsFragment.TAG
            )
        }

        layoutResource.setOnClickListener {
            replaceFragment(
                ResourcesFragment(),
                R.id.layout_home,
                ResourcesFragment.TAG
            )
        }

        layoutDocuments.setOnClickListener {
            replaceFragment(
                DocumentFragment(),
                R.id.layout_home,
                DocumentFragment.TAG
            )
        }

        layoutGoals.setOnClickListener {
            replaceFragment(
                ToDoFragment(),
                R.id.layout_home,
                ToDoFragment.TAG
            )
        }

        layoutJournals.setOnClickListener {
            replaceFragment(
                JournalFragment(),
                R.id.layout_home,
                JournalFragment.TAG
            )
        }

        fabCreateAppointmentBtn.setOnClickListener {
            Utils.isTherapististScreen = false
            clearTempFormData()
            replaceFragment(
                ClientAvailabilityFragment.newInstance(false),
                R.id.layout_home,
                ClientAvailabilityFragment.TAG
            )
        }
    }

    @SuppressLint("SetTextI18n")
    private fun displayAppointments() {
        itemsSwipeToRefresh.isRefreshing = false
        getAppointmentList { response ->
            val appointmentType: Type =
                object : TypeToken<GetAppointmentList?>() {}.type
            val appointmentLists: GetAppointmentList =
                Gson().fromJson(response, appointmentType)
            if (appointmentLists.today.isNotEmpty()) {
                cardViewAppointment.visibility = View.VISIBLE
                txtNoAppointments.visibility = View.GONE
                txtViewAllAppointments.visibility = View.GONE

                if (appointmentLists.today[0].is_group_appointment) {
                    txtAppointTherapistName.text =
                        if (appointmentLists.today[0].meeting_title == null) "Group Appointment" else appointmentLists.today[0].meeting_title
                    txtAppointTherapistType.text =
                        appointmentLists.today[0].doctor_first_name + " " +
                                appointmentLists.today[0].doctor_last_name
                } else {
                    txtAppointTherapistName.text =
                        appointmentLists.today[0].doctor_first_name + " " +
                                appointmentLists.today[0].doctor_last_name
                    txtAppointTherapistType.text = appointmentLists.today[0].doctor_designation
                }

                if (appointmentLists.today[0].is_group_appointment) {
                    val appointmentDate =
                        DateUtils(appointmentLists.today[0].group_appointment.date + " 00:00:00")

                    txtAppointmentDateTime.text =
                        appointmentDate.getCurrentDay() + ", " +
                                appointmentDate.getDay() + " " +
                                appointmentDate.getMonth() + " at " +
                                appointmentLists.today[0].group_appointment.time + " " +
                                appointmentLists.today[0].group_appointment.select_am_or_pm
                } else {
                    val appointmentDate =
                        DateUtils(appointmentLists.today[0].appointment.booking_date + " 00:00:00")

                    txtAppointmentDateTime.text =
                        appointmentDate.getCurrentDay() + ", " +
                                appointmentDate.getDay() + " " +
                                appointmentDate.getMonth() + " at " +
                                appointmentLists.today[0].appointment.time_slot.starting_time.dropLast(
                                    3
                                ) + " - " +
                                appointmentLists.today[0].appointment.time_slot.ending_time.dropLast(
                                    3
                                )
                }

                if (!appointmentLists.today[0].is_group_appointment) {
                    if (appointmentLists.today[0].appointment.type_of_visit == "Video") {
                        appointmentCall.setImageResource(R.drawable.video)
                        appointmentCall.imageTintList =
                            ColorStateList.valueOf(
                                ContextCompat.getColor(
                                    requireActivity(),
                                    R.color.primaryGreen
                                )
                            )
                    } else {
                        appointmentCall.setImageResource(R.drawable.telephone)
                        appointmentCall.imageTintList =
                            ColorStateList.valueOf(
                                ContextCompat.getColor(
                                    requireActivity(),
                                    R.color.primaryGreen
                                )
                            )
                    }
                } else {
                    appointmentCall.setImageResource(R.drawable.telephone)
                    appointmentCall.imageTintList =
                        ColorStateList.valueOf(
                            ContextCompat.getColor(
                                requireActivity(),
                                R.color.primaryGreen
                            )
                        )
                }

                if (appointmentLists.today[0].is_group_appointment) {
                    dashboardAppointImgUser.visibility = View.GONE
                    dashboardAppointGroupImg.visibility = View.VISIBLE
                } else {
                    dashboardAppointImgUser.visibility = View.VISIBLE
                    dashboardAppointGroupImg.visibility = View.GONE
                    Glide.with(requireActivity())
                        .load(BaseActivity.baseURL.dropLast(5) + appointmentLists.today[0].doctor_photo)
                        .placeholder(R.drawable.doctor_img)
                        .transform(CenterCrop(), RoundedCorners(5))
                        .into(dashboardAppointImgUser)
                }

                cardViewAppointment.setOnClickListener {
                    if (appointmentLists.today[0].is_group_appointment) {
                        val intent = Intent(requireActivity(), GroupVideoCall::class.java)
                        intent.putExtra("token", appointmentLists.today[0].rtc_token)
                        intent.putExtra("channelName", appointmentLists.today[0].channel_name)
                        startActivity(intent)
                        requireActivity().overridePendingTransition(0, 0)
                    } else {
                        getToken(appointmentLists.today[0])
                    }
                }
            } else {
                cardViewAppointment.visibility = View.GONE
                txtNoAppointments.visibility = View.VISIBLE
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun displayDashboardNotifications() {
        itemsSwipeToRefresh.isRefreshing = false
        var consentRoisCount = 0
        getDashboardNotifications { response ->
            try {
                val jsonArr = JSONArray(response)
                val consentRoisFormsNotifyList :ArrayList<ConsentRoisFormsNotify> = arrayListOf()
                for (i in 0 until jsonArr.length()) {
                    val jsonObj = jsonArr.getJSONObject(i)
                    if (jsonObj.getString("title").contains("Appointment", ignoreCase = true)) {
                        displayAppointmentCancelledAlert(jsonObj)
                    } else if (jsonObj.getString("title").contains("Consent", ignoreCase = true)) {
                        consentRoisCount += 1
                        val consentRoisNotifyType: Type = object : TypeToken<ConsentRoisFormsNotify?>() {}.type
                        val consentRoisNotify: ConsentRoisFormsNotify =
                            Gson().fromJson(jsonObj.toString(), consentRoisNotifyType)
                        consentRoisFormsNotifyList.add(consentRoisNotify)
                    } else if (jsonObj.getString("title").contains("ROI", ignoreCase = true)) {
                        consentRoisCount += 1
                        val consentRoisNotifyType: Type = object : TypeToken<ConsentRoisFormsNotify?>() {}.type
                        val consentRoisNotify: ConsentRoisFormsNotify =
                            Gson().fromJson(jsonObj.toString(), consentRoisNotifyType)
                        consentRoisFormsNotifyList.add(consentRoisNotify)
                    } else if (jsonObj.getString("title").contains("Plan", ignoreCase = true)) {
                        displayPlanSubscriptionAlert(jsonObj)
                    }
                }
                if (consentRoisCount > 0) {
                    cardViewConsentsRoisNotify.visibility = View.VISIBLE
                    consentsRoisNotify.text =
                        "Consents and Rois - $consentRoisCount pending forms."
                    cardViewConsentsRoisNotify.setOnClickListener {
                        replaceFragment(
                            ConsentsListFragment.newInstance(consentRoisFormsNotifyList),
                            R.id.layout_home,
                            ConsentsListFragment.TAG
                        )
                    }
                } else {
                    cardViewConsentsRoisNotify.visibility = View.GONE
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getToken(appointment: GetAppointment) {
        showProgress()
        val id = if (appointment.is_group_appointment) {
            appointment.group_appointment.id
        } else {
            appointment.appointment.appointment_id
        }
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .getToken(
                        GetToken(id),
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
                            val jsonObj = JSONObject(responseBody)
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

                        } catch (e: Exception) {
                            hideProgress()
                            //displayToast("Something went wrong.. Please try after sometime")
                        }
                    }, { error ->
                        hideProgress()
                        if ((error as HttpException).code() == 401) {
                            userLogin(
                                preference!![PrefKeys.PREF_EMAIL]!!,
                                preference!![PrefKeys.PREF_PASS]!!
                            ) { result ->
                                getToken(appointment)
                            }
                        } else {
                            displayAfterLoginErrorMsg(error)
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
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
                            e.printStackTrace()
                            //displayToast("Something went wrong.. Please try after sometime")
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

    private fun cancelAppointment(appointment: GetAppointment) {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .cancelAppointment(
                        CancelAppointment(
                            preference!![PrefKeys.PREF_PATIENT_ID, ""]!!.toInt(),
                            appointment.appointment.appointment_id
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

    private fun getDashboardNotifications(myCallback: (result: String?) -> Unit) {
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .getDashboardNotification(getAccessToken())
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
                            e.printStackTrace()
                        }
                    }, { error ->
                        hideProgress()
                        if ((error as HttpException).code() == 401) {
                            userLogin(
                                preference!![PrefKeys.PREF_EMAIL]!!,
                                preference!![PrefKeys.PREF_PASS]!!
                            ) { result ->
                                displayDashboardNotifications()
                            }
                        } else {
                            displayAfterLoginErrorMsg(error)
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    private fun checkPermissionsAndRun(fitActionRequestCode: FitActionRequestCode) {
        if (permissionApproved()) {
            fitSignIn(fitActionRequestCode)
        } else {
            requestRuntimePermissions(fitActionRequestCode)
        }
    }

    /**
     * Checks that the user is signed in, and if so, executes the specified function. If the user is
     * not signed in, initiates the sign in flow, specifying the post-sign in function to execute.
     *
     * @param requestCode The request code corresponding to the action to perform after sign in.
     */
    private fun fitSignIn(requestCode: FitActionRequestCode) {
        try {
            if (oAuthPermissionsApproved()) {
                performActionForRequestCode(requestCode)
            } else {
                requestCode.let {
                    GoogleSignIn.requestPermissions(
                        this,
                        GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
                        getGoogleAccount(), fitnessOptions
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Handles the callback from the OAuth sign in flow, executing the post sign in function
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (resultCode) {
            AppCompatActivity.RESULT_OK -> {
                val postSignInAction = FitActionRequestCode.values()[requestCode]
                performActionForRequestCode(postSignInAction)
            }
            else -> oAuthErrorMsg(requestCode, resultCode)
        }
    }

    /**
     * Runs the desired method, based on the specified request code. The request code is typically
     * passed to the Fit sign-in flow, and returned with the success callback. This allows the
     * caller to specify which method, post-sign-in, should be called.
     *
     * @param requestCode The code corresponding to the action to perform.
     */
    private fun performActionForRequestCode(requestCode: FitActionRequestCode) =
        when (requestCode) {
            FitActionRequestCode.READ_DATA -> readData()
            FitActionRequestCode.SUBSCRIBE -> subscribe()
        }

    private fun oAuthErrorMsg(requestCode: Int, resultCode: Int) {
        val message = """
            There was an error signing into Fit. Check the troubleshooting section of the README
            for potential issues.
            Request code was: $requestCode
            Result code was: $resultCode
        """.trimIndent()
        Log.e(TAG, message)
    }

    private fun oAuthPermissionsApproved() =
        GoogleSignIn.hasPermissions(getGoogleAccount(), fitnessOptions)

    /**
     * Gets a Google account for use in creating the Fitness client. This is achieved by either
     * using the last signed-in account, or if necessary, prompting the user to sign in.
     * `getAccountForExtension` is recommended over `getLastSignedInAccount` as the latter can
     * return `null` if there has been no sign in before.
     */
    private fun getGoogleAccount() =
        GoogleSignIn.getAccountForExtension(requireActivity(), fitnessOptions)

    /** Records step data by requesting a subscription to background step data.  */
    private fun subscribe() {
        // To create a subscription, invoke the Recording API. As soon as the subscription is
        // active, fitness data will start recording.
        Fitness.getRecordingClient(requireActivity(), getGoogleAccount())
            .subscribe(DataType.TYPE_STEP_COUNT_CUMULATIVE)
            //.listSubscriptions()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.i(TAG, "Successfully subscribed!")
                    fitSignIn(FitActionRequestCode.READ_DATA)
                } else {
                    Log.w(TAG, "There was a problem subscribing.", task.exception)
                }
            }
    }

    private fun convertMeterToKilometer(meter: Float): Float {
        return (meter * 0.001).toFloat()
    }

    /**
     * Reads the current daily step total, computed from midnight of the current day on the device's
     * current timezone.
     */
    private fun readData() {
        Fitness.getHistoryClient(requireActivity(), getGoogleAccount())
            .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
            .addOnSuccessListener { dataSet ->
                val total = when {
                    dataSet.isEmpty -> 0
                    else -> dataSet.dataPoints.first().getValue(Field.FIELD_STEPS).asInt()
                }
                Log.i(TAG, "Total steps: $total")
                if (txtStepsValue != null) {
                    txtStepsValue.text = total.toString()
                }

            }
            .addOnFailureListener { e ->
                Log.w(TAG, "There was a problem getting the step count.", e)
            }

        Fitness.getHistoryClient(requireActivity(), getGoogleAccount())
            .readDailyTotal(DataType.TYPE_DISTANCE_DELTA)
            .addOnSuccessListener { dataSet ->
                val total = when {
                    dataSet.isEmpty -> 0
                    else -> dataSet.dataPoints.first().getValue(Field.FIELD_DISTANCE).asFloat()
                }
                Log.i(TAG, "Total distance: $total")
                if (txtDistanceValue != null)
                    txtDistanceValue.text =
                        String.format("%.2f", convertMeterToKilometer(total.toFloat())) + " KM"

            }
            .addOnFailureListener { e ->
                Log.w(TAG, "There was a problem getting the step count.", e)
            }

        Fitness.getHistoryClient(requireActivity(), getGoogleAccount())
            .readDailyTotal(DataType.TYPE_HEART_RATE_BPM)
            .addOnSuccessListener { dataSet ->
                val total = when {
                    dataSet.isEmpty -> 0
                    else -> dataSet.dataPoints.first().getValue(Field.FIELD_AVERAGE)
                }
                Log.i(TAG, "Total BPM: $total")
                //txt_heart_rate.text = "$total"
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "There was a problem getting the step count.", e)
            }
        Fitness.getHistoryClient(requireActivity(), getGoogleAccount())
            .readDailyTotal(DataType.TYPE_CALORIES_EXPENDED)
            .addOnSuccessListener { dataSet ->
                var total = when {
                    dataSet.isEmpty -> 0
                    else -> dataSet.dataPoints.first().getValue(Field.FIELD_CALORIES)
                }
                if (total == null) {
                    total = ""
                }
                Log.i(TAG, "Total Calories: $total")
                if (txtCaloriesValue != null)
                    txtCaloriesValue.text = "$total KCAL"
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "There was a problem getting the step count.", e)
            }

        val sdf = SimpleDateFormat("MM/dd/yyyy' 'HH:mm:ss")
        val currentDate = sdf.format(Date())
        val currentDateTime = DateUtils(currentDate)

        txtViewUpdatedAt.text = "Updated today at " + currentDateTime.getTimeWithFormat()

        goalTrackerProgress.setProgress(74.0, 100.0)
    }

    private fun permissionApproved(): Boolean {
        val approved = if (runningQOrLater) {
            PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACTIVITY_RECOGNITION
            )
        } else {
            true
        }
        return approved
    }

    private fun requestRuntimePermissions(requestCode: FitActionRequestCode) {
        val shouldProvideRationale =
            ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.ACTIVITY_RECOGNITION
            )

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        requestCode.let {
            if (shouldProvideRationale) {
                Log.i(TAG, "Displaying permission rationale to provide additional context.")
                Snackbar.make(
                    dashboardLayout,
                    "Permission Rationale",
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction("OK") {
                        // Request permission
                        ActivityCompat.requestPermissions(
                            requireActivity(),
                            arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                            1001
                        )
                    }
                    .show()
            } else {
                Log.i(TAG, "Requesting permission")
                // Request permission. It's possible this can be auto answered if device policy
                // sets the permission in a given state or the user denied the permission
                // previously and checked "Never ask again".
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                    requestCode.ordinal
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when {
            grantResults.isEmpty() -> {
                // If user interaction was interrupted, the permission request
                // is cancelled and you receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.")
            }
            grantResults[0] == PackageManager.PERMISSION_GRANTED -> {
                // Permission was granted.
                val fitActionRequestCode = FitActionRequestCode.values()[requestCode]
                fitActionRequestCode.let {
                    fitSignIn(fitActionRequestCode)
                }
            }
            else -> {
                // Permission denied.

                // In this Activity we've chosen to notify the user that they
                // have rejected a core permission for the app since it makes the Activity useless.
                // We're communicating this message in a Snackbar since this is a sample app, but
                // core permissions would typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.

                Snackbar.make(
                    dashboardLayout,
                    "Permission Denied",
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction(R.string.settings) {
                        // Build intent that displays the App settings screen.
                        val intent = Intent()
                        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        val uri = Uri.fromParts(
                            "package",
                            BuildConfig.APPLICATION_ID, null
                        )
                        intent.data = uri
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                    }
                    .show()
            }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment DashboardFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            DashboardFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_dashboard"
    }

    override fun onAppointmentItemClickListener(
        appointment: GetAppointment,
        isStartAppointment: Boolean
    ) {
        if (isStartAppointment) {
            //Start video call
            /*replaceFragment(
                VideoCallFragment.newInstance(appointment),
                R.id.layout_home,
                VideoCallFragment.TAG
            )*/
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