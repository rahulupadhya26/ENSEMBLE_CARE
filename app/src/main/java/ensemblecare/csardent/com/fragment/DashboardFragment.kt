package ensemblecare.csardent.com.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import ensemblecare.csardent.com.*
import ensemblecare.csardent.com.adapters.*
import ensemblecare.csardent.com.controller.*
import ensemblecare.csardent.com.data.*
import ensemblecare.csardent.com.databinding.DialogAppointmentCancelledAlertBinding
import ensemblecare.csardent.com.databinding.DialogInspirationBinding
import ensemblecare.csardent.com.databinding.DialogPlanSubscriptionAlertBinding
import ensemblecare.csardent.com.databinding.DialogReachOutAttentionBinding
import ensemblecare.csardent.com.databinding.FragmentDashboardBinding
import ensemblecare.csardent.com.preference.PrefKeys
import ensemblecare.csardent.com.preference.PreferenceHelper.get
import ensemblecare.csardent.com.preference.PreferenceHelper.set
import ensemblecare.csardent.com.utils.DateUtils
import ensemblecare.csardent.com.utils.Utils
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
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.HttpException
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*

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
    private var isGetNotification: Boolean = false
    private lateinit var binding: FragmentDashboardBinding
    private var notifyId = 0

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
        binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
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
        updateStatusBarColor(R.color.resource_background)
        checkPermissionsAndRun(FitActionRequestCode.SUBSCRIBE)

        //Show Good morning, afternoon, evening or night message to user.
        showMessageToUser()

        binding.txtUserName.text = preference!![PrefKeys.PREF_FNAME, ""] + " " +
                preference!![PrefKeys.PREF_LNAME, ""]

        onClickEvents()

        displayAppointments()

        displayDashboardNotifications()

        binding.itemsSwipeToRefresh.setOnRefreshListener {
            refreshDashboardData()
        }
    }

    private fun refreshDashboardData() {
        try {
            isGetNotification = false
            displayAppointments()
            displayDashboardNotifications()
            val photo = preference!![PrefKeys.PREF_PHOTO, ""]!!
            if (photo != "null" && photo.isNotEmpty()) {
                binding.imgUserPic.visibility = View.VISIBLE
                binding.txtUserPic.visibility = View.GONE
                Glide.with(requireActivity())
                    .load(BaseActivity.baseURL.dropLast(5) + photo)
                    .placeholder(R.drawable.user_pic)
                    .transform(CenterCrop(), RoundedCorners(5))
                    .into(binding.imgUserPic)
            } else {
                //img_user_pic.setImageResource(R.drawable.user_pic)
                binding.imgUserPic.visibility = View.GONE
                binding.txtUserPic.visibility = View.VISIBLE
                val userTxt = preference!![PrefKeys.PREF_FNAME, ""]!!
                if (userTxt.isNotEmpty()) {
                    binding.txtUserPic.text = userTxt.substring(0, 1).uppercase()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun displayAppointmentCancelledAlert(jsonObj: JSONObject) {
        val cancelAppointmentNotifyType: Type =
            object : TypeToken<CancelledAppointmentNotify?>() {}.type
        val cancelAppointmentNotify: CancelledAppointmentNotify =
            Gson().fromJson(jsonObj.toString(), cancelAppointmentNotifyType)
        if (apptCancelledAlertDialog != null && apptCancelledAlertDialog!!.isShowing) {
            apptCancelledAlertDialog!!.dismiss()
        }
        apptCancelledAlertDialog = Dialog(requireActivity())
        apptCancelledAlertDialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        apptCancelledAlertDialog!!.setCancelable(false)
        apptCancelledAlertDialog!!.setCanceledOnTouchOutside(false)
        val apptCancelled = DialogAppointmentCancelledAlertBinding.inflate(layoutInflater)
        val view = apptCancelled.root
        apptCancelledAlertDialog!!.setContentView(view)

        val appointmentDate =
            DateUtils(cancelAppointmentNotify.extra_data.prev_appt_details.booking_date + " 00:00:00")

        apptCancelled.txtCancelledAppointTherapistName.text =
            cancelAppointmentNotify.extra_data.prev_appt_details.doctor.name

        apptCancelled.txtCancelledAppointTherapistType.text =
            cancelAppointmentNotify.extra_data.prev_appt_details.doctor.designation

        apptCancelled.txtCancelledAppointmentDateTime.text =
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
            apptCancelled.cancelledAppointmentCall.setImageResource(R.drawable.video)
            apptCancelled.cancelledAppointmentCall.imageTintList =
                ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireActivity(),
                        R.color.primaryGreen
                    )
                )
        } else {
            apptCancelled.cancelledAppointmentCall.setImageResource(R.drawable.telephone)
            apptCancelled.cancelledAppointmentCall.imageTintList =
                ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireActivity(),
                        R.color.primaryGreen
                    )
                )
        }

        apptCancelled.cancelledAppointImgUser.visibility = View.VISIBLE
        apptCancelled.cancelledAppointGroupImg.visibility = View.GONE
        Glide.with(requireActivity())
            .load(BaseActivity.baseURL.dropLast(5) + cancelAppointmentNotify.extra_data.prev_appt_details.doctor.photo)
            .placeholder(R.drawable.doctor_img)
            .transform(CenterCrop(), RoundedCorners(5))
            .into(apptCancelled.cancelledAppointImgUser)

        apptCancelled.cardViewApptCancelledReschedule.setOnClickListener {
            apptCancelledAlertDialog!!.dismiss()
            Utils.providerId =
                cancelAppointmentNotify.extra_data.next_appt_detials.doctor.id.toString()
            Utils.providerType =
                cancelAppointmentNotify.extra_data.next_appt_detials.doctor.designation
            Utils.providerName = cancelAppointmentNotify.extra_data.next_appt_detials.doctor.name
            Utils.aptScheduleDate = cancelAppointmentNotify.extra_data.next_appt_detials.date
            Utils.aptScheduleTime =
                cancelAppointmentNotify.extra_data.next_appt_detials.time_slot.starting_time
            Utils.appointmentId =
                cancelAppointmentNotify.extra_data.next_appt_detials.appointment_id.toString()
            updateNotificationStatus(cancelAppointmentNotify.id) {
                clearPreviousFragmentStack()
                replaceFragmentNoBackStack(
                    TherapyBasicDetailsCFragment.newInstance(true),
                    R.id.layout_home,
                    TherapyBasicDetailsCFragment.TAG
                )
            }
        }

        apptCancelled.cardViewTryDiffProvider.setOnClickListener {
            apptCancelledAlertDialog!!.dismiss()
            Utils.isTherapististScreen = false
            clearTempFormData()
            updateNotificationStatus(cancelAppointmentNotify.id) {
                clearPreviousFragmentStack()
                replaceFragmentNoBackStack(
                    ClientAvailabilityFragment.newInstance(false),
                    R.id.layout_home,
                    ClientAvailabilityFragment.TAG
                )
            }
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
            val subscriptionStatus = DialogPlanSubscriptionAlertBinding.inflate(layoutInflater)
            val view = subscriptionStatus.root
            subscriptionStatusDialog!!.setContentView(view)
            subscriptionStatus.cardViewRenewSubscription.setOnClickListener {
                subscriptionStatusDialog!!.dismiss()
                preference!![PrefKeys.PREF_STEP] = Utils.REGISTER
                preference!![PrefKeys.PREF_SELECTED_PLAN] = ""
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

    private fun displayDailyInspiration(jsonObj: JSONObject) {
        val dailyInspirationType: Type = object : TypeToken<DailyInspiration?>() {}.type
        val dailyInspiration: DailyInspiration =
            Gson().fromJson(jsonObj.toString(), dailyInspirationType)
        try {
            val dialog = Dialog(requireActivity())
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val dialogInspiration = DialogInspirationBinding.inflate(layoutInflater)
            val view = dialogInspiration.root
            dialog.setContentView(view)
            dialog.setCanceledOnTouchOutside(false)
            dialogInspiration.txtShareInspiration.text = "OK"
            dialogInspiration.txtShareInspiration.setOnClickListener {
                dialog.dismiss()
            }
            if (dailyInspiration.extra_data.text.isNotEmpty()) {
                dialogInspiration.txtInspiration.visibility = View.VISIBLE
                dialogInspiration.imgQuote.visibility = View.GONE
                dialogInspiration.txtInspiration.text = dailyInspiration.extra_data.text
            } else {
                dialogInspiration.txtInspiration.visibility = View.GONE
                dialogInspiration.imgQuote.visibility = View.VISIBLE
                Glide.with(requireActivity())
                    .load(BaseActivity.baseURL.dropLast(5) + dailyInspiration.extra_data.image)
                    .into(dialogInspiration.imgQuote)
            }
            dialog.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun displayReachOutSentSuccess(jsonObj: JSONObject) {
        notifyId = jsonObj.getInt("id")
        val phoneNo = jsonObj.getString("extra_data")
        val reachOutAttentionDialog = Dialog(requireActivity())
        reachOutAttentionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        reachOutAttentionDialog.setCancelable(false)
        reachOutAttentionDialog.setCanceledOnTouchOutside(false)
        val reachOutAttentionStatus = DialogReachOutAttentionBinding.inflate(layoutInflater)
        val view = reachOutAttentionStatus.root
        reachOutAttentionDialog.setContentView(view)
        reachOutAttentionStatus.txtReachOutAttentionName.text = jsonObj.getString("description")
        reachOutAttentionStatus.txtReachOutPhoneNo.text = phoneNo
        reachOutAttentionStatus.cardViewReachOutPhoneNo.setOnClickListener {
            val sIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${phoneNo}"))
            startActivity(sIntent)
        }
        reachOutAttentionStatus.cardViewReachOutAttention.setOnClickListener {
            reachOutAttentionDialog.dismiss()
            if (notifyId != 0)
                updateNotificationStatus(notifyId) {
                    val sIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${phoneNo}"))
                    startActivity(sIntent)
                }
        }
        reachOutAttentionDialog.show()
    }

    private fun showMessageToUser() {
        val c = Calendar.getInstance()
        when (c[Calendar.HOUR_OF_DAY]) {
            in 0..11 -> {
                binding.txtShowMessageToUser.text = "Good Morning,"
            }

            in 12..15 -> {
                binding.txtShowMessageToUser.text = "Good Afternoon,"
            }

            in 16..19 -> {
                binding.txtShowMessageToUser.text = "Good Evening,"
            }

            in 20..23 -> {
                binding.txtShowMessageToUser.text = "Good Night,"
            }
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            val photo = preference!![PrefKeys.PREF_PHOTO, ""]!!
            if (photo != "null" && photo.isNotEmpty()) {
                binding.imgUserPic.visibility = View.VISIBLE
                binding.txtUserPic.visibility = View.GONE
                Glide.with(requireActivity())
                    .load(BaseActivity.baseURL.dropLast(5) + photo)
                    .placeholder(R.drawable.user_pic)
                    .transform(CenterCrop(), RoundedCorners(5))
                    .into(binding.imgUserPic)
            } else {
                //img_user_pic.setImageResource(R.drawable.user_pic)
                binding.imgUserPic.visibility = View.GONE
                binding.txtUserPic.visibility = View.VISIBLE
                val userTxt = preference!![PrefKeys.PREF_FNAME, ""]!!
                if (userTxt.isNotEmpty()) {
                    binding.txtUserPic.text = userTxt.substring(0, 1).uppercase()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun onClickEvents() {
        binding.cardViewUserPic.setOnClickListener {
            clearPreviousFragmentStack()
            replaceFragmentNoBackStack(
                SettingsFragment(),
                R.id.layout_home,
                SettingsFragment.TAG
            )
        }

        binding.layoutUserName.setOnClickListener {
            clearPreviousFragmentStack()
            replaceFragmentNoBackStack(
                SettingsFragment(),
                R.id.layout_home,
                SettingsFragment.TAG
            )
        }

        binding.layoutAppointments.setOnClickListener {
            clearPreviousFragmentStack()
            replaceFragmentNoBackStack(
                AppointmentsFragment(),
                R.id.layout_home,
                AppointmentsFragment.TAG
            )
        }

        binding.layoutResource.setOnClickListener {
            if (preference!![PrefKeys.PREF_INTEREST_SELECTED, false]!!) {
                clearPreviousFragmentStack()
                replaceFragmentNoBackStack(
                    ResourcesFragment(),
                    R.id.layout_home,
                    ResourcesFragment.TAG
                )
            } else {
                clearPreviousFragmentStack()
                replaceFragmentNoBackStack(
                    InterestFragment.newInstance("resources"),
                    R.id.layout_home,
                    InterestFragment.TAG
                )
            }
        }

        binding.layoutDocuments.setOnClickListener {
            clearPreviousFragmentStack()
            replaceFragmentNoBackStack(
                DocumentFragment(),
                R.id.layout_home,
                DocumentFragment.TAG
            )
        }

        binding.layoutGoals.setOnClickListener {
            clearPreviousFragmentStack()
            replaceFragmentNoBackStack(
                ToDoTabbedFragment(),
                R.id.layout_home,
                ToDoTabbedFragment.TAG
            )
        }

        binding.layoutJournals.setOnClickListener {
            clearPreviousFragmentStack()
            replaceFragmentNoBackStack(
                JournalFragment(),
                R.id.layout_home,
                JournalFragment.TAG
            )
        }

        binding.layoutCommunity.setOnClickListener {
            clearPreviousFragmentStack()
            replaceFragmentNoBackStack(
                CommunityFragment(),
                R.id.layout_home,
                CommunityFragment.TAG
            )
        }

        binding.fabCreateAppointmentBtn.setOnClickListener {
            Utils.isTherapististScreen = false
            clearTempFormData()
            replaceFragmentNoBackStack(
                ClientAvailabilityFragment.newInstance(false),
                R.id.layout_home,
                ClientAvailabilityFragment.TAG
            )
        }
    }

    private fun <T> getItemImpl(list: List<T>, item: T): Int {
        list.forEachIndexed { index, it ->
            if (it == item)
                return index
        }
        return -1
    }

    @SuppressLint("SetTextI18n")
    private fun displayAppointments() {
        binding.itemsSwipeToRefresh.isRefreshing = false
        binding.cardViewShimmerAppointment.startShimmer()
        binding.cardViewShimmerAppointment.visibility = View.VISIBLE
        binding.cardViewAppointment.visibility = View.GONE
        binding.txtNoAppointments.visibility = View.GONE
        binding.txtViewAllAppointments.visibility = View.GONE
        getAppointmentList { response ->
            val appointmentType: Type =
                object : TypeToken<GetAppointmentList?>() {}.type
            val appointmentLists: GetAppointmentList =
                Gson().fromJson(response, appointmentType)
            if (appointmentLists.today.isNotEmpty()) {
                binding.cardViewAppointment.visibility = View.VISIBLE
                binding.txtNoAppointments.visibility = View.GONE
                binding.txtViewAllAppointments.visibility = View.GONE

                if (appointmentLists.today[0].is_group_appointment) {
                    binding.txtAppointTherapistName.text =
                        if (appointmentLists.today[0].group_name == null) "Group Appointment" else appointmentLists.today[0].group_name
                    binding.txtAppointTherapistType.text =
                        appointmentLists.today[0].doctor_first_name + " " +
                                appointmentLists.today[0].doctor_last_name
                } else {
                    binding.txtAppointTherapistName.text =
                        appointmentLists.today[0].doctor_first_name + " " +
                                appointmentLists.today[0].doctor_last_name
                    binding.txtAppointTherapistType.text =
                        appointmentLists.today[0].doctor_designation
                }

                if (appointmentLists.today[0].is_group_appointment) {
                    val appointmentDate =
                        DateUtils(appointmentLists.today[0].group_appointment!!.date + " 00:00:00")

                    binding.txtAppointmentDateTime.text =
                        appointmentDate.getCurrentDay() + ", " +
                                appointmentDate.getDay() + " " +
                                appointmentDate.getMonth() + " at " +
                                appointmentLists.today[0].group_appointment!!.time + " " +
                                appointmentLists.today[0].group_appointment!!.select_am_or_pm
                } else {
                    val appointmentDate =
                        DateUtils(appointmentLists.today[0].appointment!!.booking_date + " 00:00:00")

                    binding.txtAppointmentDateTime.text =
                        appointmentDate.getCurrentDay() + ", " +
                                appointmentDate.getDay() + " " +
                                appointmentDate.getMonth() + " at " +
                                appointmentLists.today[0].appointment!!.time_slot.starting_time.dropLast(
                                    3
                                ) + " - " +
                                appointmentLists.today[0].appointment!!.time_slot.ending_time.dropLast(
                                    3
                                )
                }

                if (!appointmentLists.today[0].is_group_appointment) {
                    when (appointmentLists.today[0].appointment!!.type_of_visit) {
                        "Video" -> {
                            binding.appointmentCall.setImageResource(R.drawable.video)
                            binding.appointmentCall.imageTintList =
                                ColorStateList.valueOf(
                                    ContextCompat.getColor(
                                        requireActivity(),
                                        R.color.primaryGreen
                                    )
                                )
                        }

                        "Audio" -> {
                            binding.appointmentCall.setImageResource(R.drawable.telephone)
                            binding.appointmentCall.imageTintList =
                                ColorStateList.valueOf(
                                    ContextCompat.getColor(
                                        requireActivity(),
                                        R.color.primaryGreen
                                    )
                                )
                        }

                        else -> {
                            binding.appointmentCall.setImageResource(R.drawable.chat)
                            binding.appointmentCall.imageTintList =
                                ColorStateList.valueOf(
                                    ContextCompat.getColor(
                                        requireActivity(),
                                        R.color.primaryGreen
                                    )
                                )
                        }
                    }
                } else {
                    binding.appointmentCall.setImageResource(R.drawable.video)
                    binding.appointmentCall.imageTintList =
                        ColorStateList.valueOf(
                            ContextCompat.getColor(
                                requireActivity(),
                                R.color.primaryGreen
                            )
                        )
                }

                if (appointmentLists.today[0].is_group_appointment) {
                    binding.dashboardAppointImgUser.visibility = View.GONE
                    binding.dashboardAppointGroupImg.visibility = View.VISIBLE
                } else {
                    binding.dashboardAppointImgUser.visibility = View.VISIBLE
                    binding.dashboardAppointGroupImg.visibility = View.GONE
                    Glide.with(requireActivity())
                        .load(BaseActivity.baseURL.dropLast(5) + appointmentLists.today[0].doctor_photo)
                        .placeholder(R.drawable.doctor_new_image)
                        .transform(CenterCrop(), RoundedCorners(5))
                        .into(binding.dashboardAppointImgUser)
                }

                binding.cardViewAppointment.setOnClickListener {
                    if (appointmentLists.today[0].is_group_appointment) {
                        getGroupApptToken(appointmentLists.today[0])
                    } else {
                        getToken(appointmentLists.today[0])
                    }
                }

                var sessionIndex = -1

                for (index in 0 until appointmentLists.today.size) {
                    if (appointmentLists.today[index].appointment_type == "Training_appointment") {
                        sessionIndex = index
                        break
                    }
                }

                if (sessionIndex != -1) {
                    binding.layoutTrainingSession.visibility = View.VISIBLE
                    binding.txtTrainingSessionTherapistName.text =
                        appointmentLists.today[sessionIndex].host!!.first_name + " " +
                                appointmentLists.today[sessionIndex].host!!.last_name
                    binding.txtTrainingSessionTherapistType.text =
                        appointmentLists.today[sessionIndex].title
                    val sessionDate =
                        DateUtils(appointmentLists.today[sessionIndex].date.replace("-", " "))
                    binding.txtTrainingSessionDateTime.text =
                        sessionDate.getCurrentDay() + ", " +
                                sessionDate.getDay() + " " +
                                sessionDate.getMonth() + " at " +
                                appointmentLists.today[sessionIndex].start_time.dropLast(
                                    3
                                ) + " - " +
                                appointmentLists.today[sessionIndex].end_time.dropLast(
                                    3
                                )
                    binding.trainingSessionCall.setImageResource(R.drawable.video)
                    binding.trainingSessionCall.imageTintList =
                        ColorStateList.valueOf(
                            ContextCompat.getColor(
                                requireActivity(),
                                R.color.primaryGreen
                            )
                        )
                    Glide.with(requireActivity())
                        .load(BaseActivity.baseURL.dropLast(5) + appointmentLists.today[sessionIndex].host!!.photo)
                        .placeholder(R.drawable.doctor_new_image)
                        .transform(CenterCrop(), RoundedCorners(5))
                        .into(binding.dashboardTrainingSessionImgUser)

                    binding.cardViewTrainingSession.setOnClickListener {
                        getTrainingSessionToken(appointmentLists.today[sessionIndex])
                    }
                } else {
                    binding.layoutTrainingSession.visibility = View.GONE
                }
            } else if (appointmentLists.upcoming.isNotEmpty()) {
                binding.cardViewAppointment.visibility = View.VISIBLE
                binding.txtNoAppointments.visibility = View.GONE
                binding.txtViewAllAppointments.visibility = View.GONE

                if (appointmentLists.upcoming[0].is_group_appointment) {
                    binding.txtAppointTherapistName.text =
                        if (appointmentLists.upcoming[0].group_name == null) "Group Appointment" else appointmentLists.upcoming[0].group_name
                    binding.txtAppointTherapistType.text =
                        appointmentLists.upcoming[0].doctor_first_name + " " +
                                appointmentLists.upcoming[0].doctor_last_name
                } else {
                    binding.txtAppointTherapistName.text =
                        appointmentLists.upcoming[0].doctor_first_name + " " +
                                appointmentLists.upcoming[0].doctor_last_name
                    binding.txtAppointTherapistType.text =
                        appointmentLists.upcoming[0].doctor_designation
                }

                if (appointmentLists.upcoming[0].is_group_appointment) {
                    val appointmentDate =
                        DateUtils(appointmentLists.upcoming[0].group_appointment!!.date + " 00:00:00")

                    binding.txtAppointmentDateTime.text =
                        appointmentDate.getCurrentDay() + ", " +
                                appointmentDate.getDay() + " " +
                                appointmentDate.getMonth() + " at " +
                                appointmentLists.upcoming[0].group_appointment!!.time + " " +
                                appointmentLists.upcoming[0].group_appointment!!.select_am_or_pm
                } else {
                    val appointmentDate =
                        DateUtils(appointmentLists.upcoming[0].appointment!!.date + " 00:00:00")

                    binding.txtAppointmentDateTime.text =
                        appointmentDate.getCurrentDay() + ", " +
                                appointmentDate.getDay() + " " +
                                appointmentDate.getMonth() + " at " +
                                appointmentLists.upcoming[0].appointment!!.time_slot.starting_time.dropLast(
                                    3
                                ) + " - " +
                                appointmentLists.upcoming[0].appointment!!.time_slot.ending_time.dropLast(
                                    3
                                )
                }

                if (!appointmentLists.upcoming[0].is_group_appointment) {
                    when (appointmentLists.upcoming[0].appointment!!.type_of_visit) {
                        "Video" -> {
                            binding.appointmentCall.setImageResource(R.drawable.video)
                            binding.appointmentCall.imageTintList =
                                ColorStateList.valueOf(
                                    ContextCompat.getColor(
                                        requireActivity(),
                                        R.color.primaryGreen
                                    )
                                )
                        }

                        "Audio" -> {
                            binding.appointmentCall.setImageResource(R.drawable.telephone)
                            binding.appointmentCall.imageTintList =
                                ColorStateList.valueOf(
                                    ContextCompat.getColor(
                                        requireActivity(),
                                        R.color.primaryGreen
                                    )
                                )
                        }

                        else -> {
                            binding.appointmentCall.setImageResource(R.drawable.chat)
                            binding.appointmentCall.imageTintList =
                                ColorStateList.valueOf(
                                    ContextCompat.getColor(
                                        requireActivity(),
                                        R.color.primaryGreen
                                    )
                                )
                        }
                    }
                } else {
                    binding.appointmentCall.setImageResource(R.drawable.video)
                    binding.appointmentCall.imageTintList =
                        ColorStateList.valueOf(
                            ContextCompat.getColor(
                                requireActivity(),
                                R.color.primaryGreen
                            )
                        )
                }

                if (appointmentLists.upcoming[0].is_group_appointment) {
                    binding.dashboardAppointImgUser.visibility = View.GONE
                    binding.dashboardAppointGroupImg.visibility = View.VISIBLE
                } else {
                    binding.dashboardAppointImgUser.visibility = View.VISIBLE
                    binding.dashboardAppointGroupImg.visibility = View.GONE
                    Glide.with(requireActivity())
                        .load(BaseActivity.baseURL.dropLast(5) + appointmentLists.upcoming[0].doctor_photo)
                        .placeholder(R.drawable.doctor_new_image)
                        .transform(CenterCrop(), RoundedCorners(5))
                        .into(binding.dashboardAppointImgUser)
                }
            } else {
                binding.cardViewAppointment.visibility = View.GONE
                binding.txtNoAppointments.visibility = View.VISIBLE
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun displayDashboardNotifications() {
        binding.itemsSwipeToRefresh.isRefreshing = false
        binding.cardViewNotify.visibility = View.GONE
        var consentRoisCount = 0
        if (!isGetNotification) {
            isGetNotification = !isGetNotification
            getDashboardNotifications { response ->
                try {
                    val jsonArr = JSONArray(response)
                    val consentRoisFormsNotifyList: ArrayList<ConsentRoisFormsNotify> =
                        arrayListOf()
                    for (i in 0 until jsonArr.length()) {
                        val jsonObj = jsonArr.getJSONObject(i)
                        if (jsonObj.getString("title")
                                .contains("daily_inspiration", ignoreCase = true)
                        ) {
                            if (Utils.isLoggedInFirstTime) {
                                Utils.isLoggedInFirstTime = false
                                displayDailyInspiration(jsonObj)
                            }
                        }
                        if (jsonObj.getString("title")
                                .contains("Appointment", ignoreCase = true)
                        ) {
                            displayAppointmentCancelledAlert(jsonObj)
                        } else if (jsonObj.getString("type")
                                .contains("Consent", ignoreCase = true)
                        ) {
                            consentRoisCount += 1
                            val consentRoisNotifyType: Type =
                                object : TypeToken<ConsentRoisFormsNotify?>() {}.type
                            val consentRoisNotify: ConsentRoisFormsNotify =
                                Gson().fromJson(jsonObj.toString(), consentRoisNotifyType)
                            consentRoisFormsNotifyList.add(consentRoisNotify)
                        } else if (jsonObj.getString("type")
                                .contains("ROI", ignoreCase = true)
                        ) {
                            consentRoisCount += 1
                            val consentRoisNotifyType: Type =
                                object : TypeToken<ConsentRoisFormsNotify?>() {}.type
                            val consentRoisNotify: ConsentRoisFormsNotify =
                                Gson().fromJson(jsonObj.toString(), consentRoisNotifyType)
                            consentRoisFormsNotifyList.add(consentRoisNotify)
                        } else if (jsonObj.getString("title")
                                .contains("Plan", ignoreCase = true)
                        ) {
                            displayPlanSubscriptionAlert(jsonObj)
                        } else if (jsonObj.getString("type")
                                .contains("Reach Out", ignoreCase = true)
                        ) {
                            displayReachOutSentSuccess(jsonObj)
                        }
                    }
                    if (consentRoisCount > 0) {
                        binding.cardViewConsentsRoisNotify.visibility = View.VISIBLE
                        binding.consentsRoisNotify.text =
                            "Consents and ROI's - $consentRoisCount pending forms."
                        binding.cardViewConsentsRoisNotify.setOnClickListener {
                            clearPreviousFragmentStack()
                            replaceFragmentNoBackStack(
                                ConsentRoisSignFragment.newInstance(consentRoisFormsNotifyList),
                                R.id.layout_home,
                                ConsentRoisSignFragment.TAG
                            )
                            /*replaceFragmentNoBackStack(
                                ConsentsListFragment.newInstance(consentRoisFormsNotifyList),
                                R.id.layout_home,
                                ConsentsListFragment.TAG
                            )*/
                        }
                    } else {
                        binding.cardViewConsentsRoisNotify.visibility = View.GONE
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun getToken(appointment: GetAppointment) {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .getToken(GetToken(appointment.appointment!!.appointment_id), getAccessToken())
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
                            when (appointment.appointment.type_of_visit) {
                                "Text" -> {
                                    clearPreviousFragmentStack()
                                    //Start video call
                                    replaceFragmentNoBackStack(
                                        TextAppointmentFragment.newInstance(
                                            appointment,
                                            rtcToken,
                                            rtmToken,
                                            channelName
                                        ),
                                        R.id.layout_home,
                                        TextAppointmentFragment.TAG
                                    )
                                }

                                else -> {
                                    clearPreviousFragmentStack()
                                    //Start video call
                                    replaceFragmentNoBackStack(
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
                                clearCache()
                            }
                        } else {
                            displayAfterLoginErrorMsg(error)
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    private fun getGroupApptToken(appointment: GetAppointment) {
        showProgress()
        val id = if (appointment.is_group_appointment) {
            appointment.group_appointment!!.id
        } else {
            appointment.appointment!!.appointment_id
        }
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .getGroupApptToken(
                        GetGroupApptToken(
                            id,
                            preference!![PrefKeys.PREF_EMAIL, ""]!!
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
                            val jsonObj = JSONObject(responseBody)
                            val rtcToken = jsonObj.getString("rtc_token")
                            val rtmToken = jsonObj.getString("rtm_token")
                            val channelName = jsonObj.getString("channel_name")
                            if (appointment.is_group_appointment) {
                                /*val i = Intent(requireActivity(), CallActivity::class.java)
                                val bundle = Bundle()
                                bundle.putString(
                                    ConstantApp.ACTION_KEY_ID,
                                    appointment.group_appointment!!.id.toString()
                                )
                                bundle.putString(ConstantApp.ACTION_KEY_TOKEN, rtcToken)
                                bundle.putString(ConstantApp.ACTION_KEY_RTM, rtmToken)
                                bundle.putString(
                                    ConstantApp.ACTION_KEY_CHANNEL_NAME,
                                    channelName
                                )
                                bundle.putString(
                                    ConstantApp.ACTION_KEY_FNAME,
                                    appointment.doctor_first_name
                                )
                                bundle.putString(
                                    ConstantApp.ACTION_KEY_LNAME,
                                    appointment.doctor_last_name
                                )
                                i.putExtras(bundle)
                                startActivity(i)
                                requireActivity().overridePendingTransition(0, 0)*/
                                /*val intent = Intent(requireActivity(), GroupVideoCall::class.java)
                                intent.putExtra("token", rtcToken)
                                intent.putExtra("channelName", channelName)
                                startActivity(intent)
                                requireActivity().overridePendingTransition(0, 0)*/
                                clearPreviousFragmentStack()
                                replaceFragmentNoBackStack(
                                    GroupVideoCallFragment.newInstance(
                                        rtcToken,
                                        channelName,
                                        rtmToken,
                                        appointment
                                    ), R.id.layout_home, GroupVideoCallFragment.TAG
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
                                clearCache()
                            }
                        } else {
                            displayAfterLoginErrorMsg(error)
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    private fun getTrainingSessionToken(appointment: GetAppointment) {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .getTrainingSessionToken(
                        GetTrainingSessionToken(
                            appointment.id,
                            preference!![PrefKeys.PREF_EMAIL, ""]!!
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
                            val jsonObj = JSONObject(responseBody)
                            val rtcToken = jsonObj.getString("rtc_token")
                            val rtmToken = jsonObj.getString("rtm_token")
                            val channelName = jsonObj.getString("channel_name")
                            clearPreviousFragmentStack()
                            //Start video call
                            replaceFragmentNoBackStack(
                                TrainingSessionFragment.newInstance(
                                    appointment,
                                    rtcToken,
                                    rtmToken,
                                    channelName
                                ),
                                R.id.layout_home,
                                TrainingSessionFragment.TAG
                            )
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
                                clearCache()
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
                            binding.cardViewShimmerAppointment.stopShimmer()
                            binding.cardViewShimmerAppointment.visibility = View.GONE
                            binding.cardViewAppointment.visibility = View.GONE
                            var responseBody = result.string()
                            Log.d("Response Body", responseBody)
                            val respBody = responseBody.split("|")
                            val status = respBody[1]
                            responseBody = respBody[0]
                            myCallback.invoke(responseBody)
                        } catch (e: Exception) {
                            hideProgress()
                            try {
                                binding.cardViewShimmerAppointment.stopShimmer()
                                binding.cardViewShimmerAppointment.visibility = View.GONE
                                binding.txtNoAppointments.visibility = View.VISIBLE
                                e.printStackTrace()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            //displayToast("Something went wrong.. Please try after sometime")
                        }
                    }, { error ->
                        hideProgress()
                        if ((error as HttpException).code() == 401) {
                            userLogin(
                                preference!![PrefKeys.PREF_EMAIL]!!,
                                preference!![PrefKeys.PREF_PASS]!!
                            ) { result ->
                                clearCache()
                            }
                        } else {
                            binding.cardViewShimmerAppointment.stopShimmer()
                            binding.cardViewShimmerAppointment.visibility = View.GONE
                            binding.txtNoAppointments.visibility = View.VISIBLE
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
                            appointment.appointment!!.appointment_id
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
                                clearCache()
                            }
                        } else {
                            displayAfterLoginErrorMsg(error)
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    private fun updateNotificationStatus(
        notificationId: Int,
        myCallback: (result: String?) -> Unit
    ) {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .updateNotificationStatus(
                        "PI0061",
                        NotifyStatus(notificationId),
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
                        }
                    }, { error ->
                        hideProgress()
                        //displayToast("Error ${error.localizedMessage}")
                        if ((error as HttpException).code() == 401) {
                            userLogin(
                                preference!![PrefKeys.PREF_EMAIL]!!,
                                preference!![PrefKeys.PREF_PASS]!!
                            ) { result ->
                                clearCache()
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
                refreshDashboardData()
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
        refreshDashboardData()
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
                if (binding.txtStepsValue != null) {
                    binding.txtStepsValue.text = total.toString()
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
                if (binding.txtDistanceValue != null)
                    binding.txtDistanceValue.text =
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
                if (binding.txtCaloriesValue != null)
                    binding.txtCaloriesValue.text = "$total KCAL"
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "There was a problem getting the step count.", e)
            }

        val sdf = SimpleDateFormat("MM/dd/yyyy' 'HH:mm:ss")
        val currentDate = sdf.format(Date())
        val currentDateTime = DateUtils(currentDate)

        binding.txtViewUpdatedAt.text =
            "Updated today at " + currentDateTime.getTimeWithFormat()

        binding.goalTrackerProgress.setProgress(74.0, 100.0)
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
                    binding.dashboardLayout,
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
                    binding.dashboardLayout,
                    "Permission Denied",
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction(R.string.settings) {
                        // Build intent that displays the App settings screen.
                        val intent = Intent()
                        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        val uri = Uri.fromParts(
                            "package",
                            "com.app.selfcare", null
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