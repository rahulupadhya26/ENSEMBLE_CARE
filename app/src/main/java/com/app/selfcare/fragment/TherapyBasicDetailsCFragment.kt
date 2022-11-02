package com.app.selfcare.fragment

import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.app.selfcare.R
import com.app.selfcare.utils.Utils
import kotlinx.android.synthetic.main.fragment_insurance.*
import kotlinx.android.synthetic.main.fragment_therapy_basic_details_c.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [TherapyBasicDetailsCFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TherapyBasicDetailsCFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    var communicationType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_therapy_basic_details_c
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.VISIBLE
        getSubTitle().visibility = View.GONE

        layoutPhoneCall.setOnClickListener {
            communicationType = "Audio"
            displayToast("Phone call")
            layoutPhoneCall.setCardBackgroundColor(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.primaryGreen
                )
            )
            imgPhoneCall.imageTintList =
                ColorStateList.valueOf(ContextCompat.getColor(requireActivity(), R.color.white))
            txtPhoneCall.setTextColor(ContextCompat.getColor(requireActivity(), R.color.white))

            layoutVideoCall.setCardBackgroundColor(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.white
                )
            )
            imgVideoCall.imageTintList =
                ColorStateList.valueOf(ContextCompat.getColor(requireActivity(), R.color.primaryGreen))
            txtVideoCall.setTextColor(ContextCompat.getColor(requireActivity(), R.color.primaryGreen))
        }

        layoutVideoCall.setOnClickListener {
            communicationType = "Video"
            displayToast("Video call")
            layoutVideoCall.setCardBackgroundColor(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.primaryGreen
                )
            )
            imgVideoCall.imageTintList =
                ColorStateList.valueOf(ContextCompat.getColor(requireActivity(), R.color.white))
            txtVideoCall.setTextColor(ContextCompat.getColor(requireActivity(), R.color.white))

            layoutPhoneCall.setCardBackgroundColor(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.white
                )
            )
            imgPhoneCall.imageTintList =
                ColorStateList.valueOf(ContextCompat.getColor(requireActivity(), R.color.primaryGreen))
            txtPhoneCall.setTextColor(ContextCompat.getColor(requireActivity(), R.color.primaryGreen))
        }

        layoutOfficeVisit.setOnClickListener {
            communicationType = "Office visit"
            displayToast("Office visit")
        }

        tvAddTherapyReviewPic.setOnClickListener {
            captureImage(imgPrescriptionPic)
        }

        imgPrescriptionPic.setOnClickListener {
            showImage(imgPrescriptionPic)
        }

        imgTherapistReviewPicClear.setOnClickListener {
            imgPrescriptionPic.setImageDrawable(null)
            imgPrescriptionPic.setImageResource(R.drawable.prescription)
        }

        btnBasicDetailC.setOnClickListener {
            if (communicationType != null) {
                Utils.selectedCommunicationMode = communicationType!!
                replaceFragment(FinalReviewFragment(), R.id.layout_home, FinalReviewFragment.TAG)
            } else {
                displayToast("Select the communication mode")
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
         * @return A new instance of fragment TherapyBasicDetailsCFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            TherapyBasicDetailsCFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_Therapy_Basic_Details_C"
    }
}