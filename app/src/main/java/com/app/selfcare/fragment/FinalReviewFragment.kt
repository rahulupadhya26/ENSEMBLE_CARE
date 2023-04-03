package com.app.selfcare.fragment

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.BaseActivity
import com.app.selfcare.R
import com.app.selfcare.adapters.FinalReviewAdapter
import com.app.selfcare.databinding.FragmentActivityCarePlanBinding
import com.app.selfcare.databinding.FragmentFinalReviewBinding
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper.get
import com.app.selfcare.utils.DateUtils
import com.app.selfcare.utils.Utils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import java.io.File

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
    private lateinit var binding: FragmentFinalReviewBinding

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
        binding = FragmentFinalReviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getLayout(): Int {
        return R.layout.fragment_final_review
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE
        updateStatusBarColor(R.color.screen_background_color)

        binding.finalReviewBack.setOnClickListener {
            popBackStack()
        }

        binding.txtFinalReviewTherapistName.text = Utils.providerName
        binding.txtFinalReviewTherapistType.text = Utils.providerType

        val appointmentDate = DateUtils(Utils.aptScheduleDate + " 00:00:00")

        binding.txtFinalReviewSelectedDate.text =
            appointmentDate.getMonth() + " " +
                    appointmentDate.getDay() + appointmentDate.getDayNumberSuffix(
                appointmentDate.getDay().toInt()
            ) + " " +
                    appointmentDate.getYear()

        binding.txtFinalReviewSelectedTime.text = Utils.aptScheduleTime.dropLast(3)

        Glide.with(requireActivity())
            .load(BaseActivity.baseURL.dropLast(5) + Utils.providerPhoto)
            .placeholder(R.drawable.doctor_img)
            .transform(CenterCrop(), RoundedCorners(5))
            .into(binding.imgFinalReviewTherapist)

        when (Utils.selectedCommunicationMode) {
            "Video" -> {
                binding.imgFinalReviewSelectedMode.setBackgroundResource(R.drawable.video)
                binding.imgFinalReviewSelectedMode.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireActivity(),
                        R.color.primaryGreen
                    )
                )
                binding.txtFinalReviewSelectedMode.text = "Video Call"
            }
            "Audio" -> {
                binding.imgFinalReviewSelectedMode.setBackgroundResource(R.drawable.telephone)
                binding.imgFinalReviewSelectedMode.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireActivity(),
                        R.color.primaryGreen
                    )
                )
                binding.txtFinalReviewSelectedMode.text = "Audio Call"
            }
            else -> {
                binding.imgFinalReviewSelectedMode.setBackgroundResource(R.drawable.chat)
                binding.imgFinalReviewSelectedMode.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireActivity(),
                        R.color.primaryGreen
                    )
                )
                binding.txtFinalReviewSelectedMode.text = "Text"
            }
        }

        if (getBitmapList().size > 0) {
            binding.layoutSelectedImageForReview.visibility = View.VISIBLE
            when (getBitmapList().size) {
                1 -> {
                    Glide.with(this)
                        .load(File(getBitmapList()[0]))
                        .into(binding.imgFinalReviewPrescriptionPic1)
                }
                2 -> {
                    Glide.with(this)
                        .load(File(getBitmapList()[0]))
                        .into(binding.imgFinalReviewPrescriptionPic1)
                    Glide.with(this)
                        .load(File(getBitmapList()[1]))
                        .into(binding.imgFinalReviewPrescriptionPic2)
                }
                3 -> {
                    Glide.with(this)
                        .load(File(getBitmapList()[0]))
                        .into(binding.imgFinalReviewPrescriptionPic1)
                    Glide.with(this)
                        .load(File(getBitmapList()[1]))
                        .into(binding.imgFinalReviewPrescriptionPic2)
                    Glide.with(this)
                        .load(File(getBitmapList()[2]))
                        .into(binding.imgFinalReviewPrescriptionPic3)
                }
            }
        } else {
            binding.layoutSelectedImageForReview.visibility = View.GONE
        }

        val finalReviewList = ArrayList<String>()
        val patientName =
            preference!![PrefKeys.PREF_FNAME, ""]!! + " " +
                    preference!![PrefKeys.PREF_MNAME, ""]!! + " " +
                    preference!![PrefKeys.PREF_LNAME, ""]!!
        finalReviewList.add("Need consultation for - $patientName")
        finalReviewList.add("Phone to reach me - " + preference!![PrefKeys.PREF_PHONE_NO, ""]!!)
        /*finalReviewList.add(
            "My location is - " + Utils.selectedStreet + "," +
                    Utils.selectedCity + "," +
                    Utils.selectedState + "," +
                    Utils.selectedPostalCode + "," +
                    Utils.selectedCountry
        )*/
        finalReviewList.add("Therapist - " + Utils.providerName)
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

        binding.recyclerViewFinalReview.apply {
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

        binding.btnFinalReview.setOnClickListener {
            if (binding.checkboxTermsConditions.isChecked) {
                replaceFragment(
                    ConsentFormFragment(),
                    R.id.layout_home,
                    ConsentFormFragment.TAG
                )
            } else {
                displayMsg("Message", "Please select terms and conditions for further procedure")
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