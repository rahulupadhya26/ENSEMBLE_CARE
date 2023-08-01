package ensemblecare.csardent.com.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import ensemblecare.csardent.com.R
import ensemblecare.csardent.com.adapters.AppointmentsAdapter
import ensemblecare.csardent.com.controller.OnAppointmentItemClickListener
import ensemblecare.csardent.com.data.AppointmentPatientId
import ensemblecare.csardent.com.data.GetAppointment
import ensemblecare.csardent.com.data.GetAppointmentList
import ensemblecare.csardent.com.databinding.FragmentUpcomingAppointmentBinding
import ensemblecare.csardent.com.preference.PrefKeys
import ensemblecare.csardent.com.preference.PreferenceHelper.get
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ensemblecare.csardent.com.controller.OnRescheduleAppointment
import ensemblecare.csardent.com.databinding.DialogAppointmentOptionsBinding
import ensemblecare.csardent.com.databinding.DialogPictureOptionBinding
import ensemblecare.csardent.com.utils.DateMethods
import ensemblecare.csardent.com.utils.DateUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.Calendar

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [UpcomingAppointmentFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UpcomingAppointmentFragment : BaseFragment(), OnAppointmentItemClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentUpcomingAppointmentBinding
    private var dispOptionsDialog: BottomSheetDialog? = null
    private lateinit var optionsDialog: DialogAppointmentOptionsBinding

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
        binding = FragmentUpcomingAppointmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getLayout(): Int {
        return R.layout.fragment_upcoming_appointment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE

        displayUpcomingAppointments()
    }

    private fun displayUpcomingAppointments() {
        binding.shimmerUpcomingAppointmentList.startShimmer()
        binding.shimmerUpcomingAppointmentList.visibility = View.VISIBLE
        binding.recyclerViewUpcomingAppointmentList.visibility = View.GONE
        binding.txtNoUpcomingAppointmentList.visibility = View.GONE
        getAppointmentList { response ->
            val appointmentType: Type = object : TypeToken<GetAppointmentList?>() {}.type
            val appointmentList: GetAppointmentList =
                Gson().fromJson(response, appointmentType)
            if (appointmentList.upcoming.isNotEmpty()) {
                binding.recyclerViewUpcomingAppointmentList.visibility = View.VISIBLE
                binding.txtNoUpcomingAppointmentList.visibility = View.GONE
                binding.recyclerViewUpcomingAppointmentList.apply {
                    layoutManager =
                        LinearLayoutManager(mActivity!!, LinearLayoutManager.VERTICAL, false)
                    adapter = AppointmentsAdapter(
                        mActivity!!,
                        appointmentList.upcoming,
                        "Upcoming",
                        this@UpcomingAppointmentFragment
                    )
                    adapter!!.notifyDataSetChanged()
                }
            } else {
                binding.recyclerViewUpcomingAppointmentList.visibility = View.GONE
                binding.txtNoUpcomingAppointmentList.visibility = View.VISIBLE
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
                            binding.shimmerUpcomingAppointmentList.stopShimmer()
                            binding.shimmerUpcomingAppointmentList.visibility = View.GONE
                            var responseBody = result.string()
                            Log.d("Response Body", responseBody)
                            val respBody = responseBody.split("|")
                            val status = respBody[1]
                            responseBody = respBody[0]
                            myCallback.invoke(responseBody)
                        } catch (e: Exception) {
                            hideProgress()
                            binding.shimmerUpcomingAppointmentList.stopShimmer()
                            binding.shimmerUpcomingAppointmentList.visibility = View.GONE
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
                            binding.shimmerUpcomingAppointmentList.stopShimmer()
                            binding.shimmerUpcomingAppointmentList.visibility = View.GONE
                            displayAfterLoginErrorMsg(error)
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    private fun displayOptions(appointment: GetAppointment) {
        dispOptionsDialog = BottomSheetDialog(requireActivity(), R.style.SheetDialog)
        optionsDialog = DialogAppointmentOptionsBinding.inflate(layoutInflater)
        val view = optionsDialog.root
        //val pictureDialog: View = layoutInflater.inflate(R.layout.dialog_appointment_options, null)
        dispOptionsDialog!!.setContentView(view)
        dispOptionsDialog!!.setCanceledOnTouchOutside(true)

        val appointmentDateTime = if (appointment.is_group_appointment) {
            appointment.group_appointment!!.date + " " + appointment.group_appointment.starttime!!.dropLast(3)
        } else {
            appointment.appointment!!.date + " " + appointment.appointment.time_slot.starting_time
        }
        val sdf = SimpleDateFormat("MM/dd/yyyy' 'HH:mm:ss")
        val currentDate = sdf.format(Calendar.getInstance().time)
        val currentDateTime = DateUtils(currentDate)
        val appointDateTime = DateUtils(appointmentDateTime)
        if (!DateMethods().areDatesWithinNHours(
                appointDateTime.mDate!!,
                currentDateTime.mDate!!,
                48
            )
        ) {
            optionsDialog.optionDialogReschedule.visibility = View.VISIBLE
            optionsDialog.optionDialogCancel.visibility = View.VISIBLE
        } else {
            optionsDialog.optionDialogReschedule.visibility = View.GONE
            optionsDialog.optionDialogCancel.visibility = View.GONE
        }

        optionsDialog.optionDialogReschedule.setOnClickListener {
            dispOptionsDialog!!.dismiss()
            //Reschedule the appointment
            val appointmentId = if (appointment.is_group_appointment) {
                appointment.group_appointment!!.id.toString()
            } else {
                appointment.appointment!!.appointment_id.toString()
            }
            rescheduleAppointment(appointmentId) { response ->
                displayUpcomingAppointments()
            }
        }

        optionsDialog.optionDialogCancel.setOnClickListener {
            dispOptionsDialog!!.dismiss()
            val builder = AlertDialog.Builder(requireActivity())
            builder.setTitle("Alert")
            builder.setMessage("Do you really want to cancel this appointment?")
            builder.setPositiveButton("Yes") { dialogInterface, _ ->
                dialogInterface.dismiss()
                //Cancel the appointment
                cancelAppointment(appointment) {
                    displayUpcomingAppointments()
                }
            }
            //performing cancel action
            builder.setNeutralButton("Cancel") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            val alert = builder.create()
            alert.setCancelable(false)
            alert.show()
        }

        optionsDialog.optionDialogClose.setOnClickListener {
            dispOptionsDialog!!.dismiss()
        }

        dispOptionsDialog!!.show()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment UpcomingAppointmentFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String = "") =
            UpcomingAppointmentFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_Upcoming_Appointments"
    }

    override fun onAppointmentItemClickListener(
        appointment: GetAppointment,
        isStartAppointment: Boolean
    ) {
        if (!isStartAppointment) {
            displayOptions(appointment)
        }
    }
}