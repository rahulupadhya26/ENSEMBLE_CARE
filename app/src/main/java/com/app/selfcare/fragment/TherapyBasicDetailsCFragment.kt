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
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_final_review.*
import kotlinx.android.synthetic.main.fragment_insurance.*
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_therapy_basic_details_c.*
import kotlinx.android.synthetic.main.fragment_therapy_basic_details_c.imgPrescriptionPic1
import kotlinx.android.synthetic.main.fragment_therapy_basic_details_c.layoutVideoCall
import java.io.File

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
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE
        updateStatusBarColor(R.color.screen_background_color)

        callModeBack.setOnClickListener {
            popBackStack()
        }

        layoutPhoneCall.setOnClickListener {
            communicationType = "Audio"
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
                ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireActivity(),
                        R.color.primaryGreen
                    )
                )
            txtVideoCall.setTextColor(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.primaryGreen
                )
            )
        }

        layoutVideoCall.setOnClickListener {
            communicationType = "Video"
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
                ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireActivity(),
                        R.color.primaryGreen
                    )
                )
            txtPhoneCall.setTextColor(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.primaryGreen
                )
            )
        }

        cardViewPrescription1.setOnClickListener {
            if (getBitmapList().size > 0) {
                showImage(getBitmapList()[0])
            } else {
                captureImage(null, "Prescription")
                //showImageDialog()
            }
        }

        cardViewPrescription2.setOnClickListener {
            if (getBitmapList().size > 1) {
                showImage(getBitmapList()[1])
            } else {
                //showImageDialog()
                captureImage(null, "Prescription")
            }
        }

        cardViewPrescription3.setOnClickListener {
            if (getBitmapList().size > 2) {
                showImage(getBitmapList()[2])
            } else {
                //showImageDialog()
                captureImage(null, "Prescription")
            }
        }

        imgPrescriptionPic1Clear.setOnClickListener {
            if (getBitmapList().size > 0) {
                getBitmapList().removeAt(0)
                imgPrescriptionPic1.setImageDrawable(null)
                imgPrescriptionPic1.setImageResource(R.drawable.plusnew)
                onResume()
            }
        }

        imgPrescriptionPic2Clear.setOnClickListener {
            if (getBitmapList().size > 1) {
                getBitmapList().removeAt(1)
                imgPrescriptionPic2.setImageDrawable(null)
                imgPrescriptionPic2.setImageResource(R.drawable.plusnew)
                onResume()
            }
        }

        imgPrescriptionPic3Clear.setOnClickListener {
            if (getBitmapList().size > 2) {
                getBitmapList().removeAt(2)
                imgPrescriptionPic3.setImageDrawable(null)
                imgPrescriptionPic3.setImageResource(R.drawable.plusnew)
                onResume()
            }
        }

        btnBasicDetailC.setOnClickListener {
            if (communicationType != null) {
                Utils.selectedCommunicationMode = communicationType!!
                replaceFragment(FinalReviewFragment(), R.id.layout_home, FinalReviewFragment.TAG)
            } else {
                displayMsg("Alert", "Select the communication mode")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        when (getBitmapList().size) {
            1 -> {
                Glide.with(this)
                    .load(File(getBitmapList()[0]))
                    .into(imgPrescriptionPic1)
                imgPrescriptionPic2.setImageDrawable(null)
                imgPrescriptionPic2.setImageResource(R.drawable.plusnew)
                imgPrescriptionPic3.setImageDrawable(null)
                imgPrescriptionPic3.setImageResource(R.drawable.plusnew)
                imgPrescriptionPic1Clear.visibility = View.VISIBLE
                imgPrescriptionPic2Clear.visibility = View.GONE
                imgPrescriptionPic3Clear.visibility = View.GONE
            }
            2 -> {
                Glide.with(this)
                    .load(File(getBitmapList()[0]))
                    .into(imgPrescriptionPic1)
                Glide.with(this)
                    .load(File(getBitmapList()[1]))
                    .into(imgPrescriptionPic2)
                imgPrescriptionPic3.setImageDrawable(null)
                imgPrescriptionPic3.setImageResource(R.drawable.plusnew)
                imgPrescriptionPic1Clear.visibility = View.VISIBLE
                imgPrescriptionPic2Clear.visibility = View.VISIBLE
                imgPrescriptionPic3Clear.visibility = View.GONE
            }
            3 -> {
                Glide.with(this)
                    .load(File(getBitmapList()[0]))
                    .into(imgPrescriptionPic1)
                Glide.with(this)
                    .load(File(getBitmapList()[1]))
                    .into(imgPrescriptionPic2)
                Glide.with(this)
                    .load(File(getBitmapList()[2]))
                    .into(imgPrescriptionPic3)
                imgPrescriptionPic1Clear.visibility = View.VISIBLE
                imgPrescriptionPic2Clear.visibility = View.VISIBLE
                imgPrescriptionPic3Clear.visibility = View.VISIBLE
            }
            else -> {
                imgPrescriptionPic1Clear.visibility = View.GONE
                imgPrescriptionPic2Clear.visibility = View.GONE
                imgPrescriptionPic3Clear.visibility = View.GONE
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