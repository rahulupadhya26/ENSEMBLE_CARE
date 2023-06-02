package com.app.selfcare.fragment

import android.app.Dialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.R
import com.app.selfcare.adapters.SpecialistAdapter
import com.app.selfcare.controller.OnItemTherapistImageClickListener
import com.app.selfcare.controller.OnTherapistItemClickListener
import com.app.selfcare.data.PatientId
import com.app.selfcare.data.Therapist
import com.app.selfcare.databinding.DialogSameDayAppointmentBinding
import com.app.selfcare.databinding.FragmentActivityCarePlanBinding
import com.app.selfcare.databinding.FragmentTherapistListBinding
import com.app.selfcare.preference.PrefKeys
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import com.app.selfcare.preference.PreferenceHelper.get
import com.app.selfcare.utils.Utils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.HttpException
import java.lang.reflect.Type
import kotlin.collections.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [TherapistListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TherapistListFragment : BaseFragment(), OnItemTherapistImageClickListener,
    OnTherapistItemClickListener {
    // TODO: Rename and change types of parameters
    private var isRegister: Boolean? = null
    private var specialist: ArrayList<Therapist>? = null
    //var specialist: ArrayList<Therapist> = ArrayList()
    private lateinit var binding: FragmentTherapistListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            isRegister = it.getBoolean(ARG_PARAM1)
            specialist = it.getParcelableArrayList(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTherapistListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getLayout(): Int {
        return R.layout.fragment_therapist_list
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE
        updateStatusBarColor(R.color.therapist_list_background)

        binding.therapistListBack.setOnClickListener {
            popBackStack()
        }

        /*btnConfirmDoctor.backgroundTintList = ColorStateList.valueOf(
            ContextCompat.getColor(
                requireActivity(),
                R.color.buttonBackground
            )
        )*/

        //getTherapistList()
        if (specialist != null) {
            if (specialist!!.isNotEmpty()) {
                binding.layoutTherapistFound.visibility = View.VISIBLE
                binding.layoutTherapistNotFound.visibility = View.GONE
                updatePhysiciansList(specialist!!)
            } else {
                binding.layoutTherapistFound.visibility = View.GONE
                binding.layoutTherapistNotFound.visibility = View.VISIBLE
            }
        } else {
            binding.layoutTherapistFound.visibility = View.GONE
            binding.layoutTherapistNotFound.visibility = View.VISIBLE
        }

        binding.btnTherapistGoToDashboard.setOnClickListener {
            setBottomNavigation(null)
            setLayoutBottomNavigation(null)
            replaceFragmentNoBackStack(
                BottomNavigationFragment(),
                R.id.layout_home,
                BottomNavigationFragment.TAG
            )
        }

        /*btnConfirmDoctor.setOnClickListener {
            if (Utils.providerId.isNotEmpty() &&
                Utils.providerPublicId.isNotEmpty() &&
                Utils.providerType.isNotEmpty() &&
                Utils.providerName.isNotEmpty()
            ) {
                replaceFragment(
                    TherapySelectionFragment(),
                    R.id.layout_home,
                    TherapySelectionFragment.TAG
                )
            }
        }*/
    }

    private fun getTherapistList() {
        Utils.providerId = ""
        Utils.providerPublicId = ""
        Utils.providerType = ""
        Utils.providerName = ""
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .getTherapistList(
                        PatientId(preference!![PrefKeys.PREF_PATIENT_ID, 0]!!),
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
                            specialist = ArrayList()
                            val therapistList: Type =
                                object : TypeToken<ArrayList<Therapist?>?>() {}.type
                            specialist = Gson().fromJson(responseBody, therapistList)
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
                                getTherapistList()
                            }
                        } else {
                            displayAfterLoginErrorMsg(error)
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    private fun updatePhysiciansList(specialists: ArrayList<Therapist>) {
        binding.recyclerViewTherapist.apply {
            layoutManager = LinearLayoutManager(
                mActivity!!,
                RecyclerView.VERTICAL,
                false
            )
            adapter = SpecialistAdapter(
                mActivity!!,
                specialists, this@TherapistListFragment,
                this@TherapistListFragment
            )
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment TherapistListFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: Boolean, param2: ArrayList<Therapist>) =
            TherapistListFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(ARG_PARAM1, param1)
                    putParcelableArrayList(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_Therapy_List"
    }

    override fun onItemTherapistImageClickListener(therapist: Therapist) {
        Utils.providerId = therapist.doctor_id
        Utils.providerPublicId = therapist.doctor_public_id
        Utils.providerType = therapist.doctor_type
        Utils.providerName =
            therapist.first_name + " " + therapist.middle_name + " " + therapist.last_name
        Utils.aptScheduleDate = therapist.appointment.date
        Utils.aptScheduleTime = therapist.appointment.time_slot.starting_time
        Utils.appointmentId = therapist.appointment.appointment_id.toString()
        Utils.providerPhoto = therapist.photo
        Utils.selectedCommunicationMode = ""
        clearTempFormData()
        if (!therapist.appointment.on_sameday) {
            replaceFragment(
                TherapistDetailFragment.newInstance(therapist),
                R.id.layout_home,
                TherapistDetailFragment.TAG
            )
        } else {
            //display alert message
            val dialog = Dialog(requireActivity())
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(false)
            dialog.setCanceledOnTouchOutside(false)
            val sameDayAppoint = DialogSameDayAppointmentBinding.inflate(layoutInflater)
            val view = sameDayAppoint.root
            dialog.setContentView(view)
            sameDayAppoint.cardViewSameDayApptYes.setOnClickListener {
                dialog.dismiss()
                replaceFragment(
                    TherapistDetailFragment.newInstance(therapist),
                    R.id.layout_home,
                    TherapistDetailFragment.TAG
                )
            }
            sameDayAppoint.cardViewSameDayApptNo.setOnClickListener {
                dialog.dismiss()
            }
            dialog.show()
        }
    }

    override fun onTherapistItemClickListener(therapist: Therapist) {
        Utils.providerId = therapist.doctor_id
        Utils.providerPublicId = therapist.doctor_public_id
        Utils.providerType = therapist.doctor_type
        Utils.providerName =
            therapist.first_name + " " + therapist.middle_name + " " + therapist.last_name
        /*btnConfirmDoctor.backgroundTintList =
            ColorStateList.valueOf(ContextCompat.getColor(requireActivity(), R.color.primaryGreen))*/
    }
}