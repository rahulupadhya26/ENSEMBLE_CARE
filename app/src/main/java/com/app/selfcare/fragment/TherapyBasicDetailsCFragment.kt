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
import com.app.selfcare.databinding.FragmentActivityCarePlanBinding
import com.app.selfcare.databinding.FragmentTherapyBasicDetailsCBinding
import com.app.selfcare.utils.Utils
import com.bumptech.glide.Glide
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
    private var isFromDashboard: Boolean = false
    private var param2: String? = null
    var communicationType: String? = null
    private lateinit var binding: FragmentTherapyBasicDetailsCBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            isFromDashboard = it.getBoolean(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTherapyBasicDetailsCBinding.inflate(inflater, container, false)
        return binding.root
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

        binding.callModeBack.setOnClickListener {
            if (isFromDashboard) {
                setBottomNavigation(null)
                setLayoutBottomNavigation(null)
                replaceFragmentNoBackStack(
                    BottomNavigationFragment(),
                    R.id.layout_home,
                    BottomNavigationFragment.TAG
                )
            } else {
                popBackStack()
            }
        }

        if (Utils.selectedCommunicationMode.isNotEmpty()) {
            when (Utils.selectedCommunicationMode) {
                "Audio" -> {
                    onAudioModeClick()
                }
                "Video" -> {
                    onVideoModeClick()
                }
                "Text" -> {
                    onTextModeClick()
                }
            }
        } else {
            binding.layoutPhoneCall.setCardBackgroundColor(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.white
                )
            )
            binding.imgPhoneCall.imageTintList =
                ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireActivity(),
                        R.color.primaryGreen
                    )
                )
            binding.txtPhoneCall.setTextColor(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.primaryGreen
                )
            )

            binding.layoutVideoCall.setCardBackgroundColor(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.white
                )
            )
            binding.imgVideoCall.imageTintList =
                ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireActivity(),
                        R.color.primaryGreen
                    )
                )
            binding.txtVideoCall.setTextColor(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.primaryGreen
                )
            )

            binding.layoutText.setCardBackgroundColor(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.white
                )
            )
            binding.imgText.imageTintList =
                ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireActivity(),
                        R.color.primaryGreen
                    )
                )
            binding.txtText.setTextColor(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.primaryGreen
                )
            )
        }

        binding.layoutPhoneCall.setOnClickListener {
            communicationType = "Audio"
            onAudioModeClick()
        }

        binding.layoutVideoCall.setOnClickListener {
            communicationType = "Video"
            onVideoModeClick()
        }

        binding.layoutText.setOnClickListener {
            communicationType = "Text"
            onTextModeClick()
        }

        binding.cardViewPrescription1.setOnClickListener {
            if (getBitmapList().size > 0) {
                showImage(getBitmapList()[0])
            } else {
                captureImage(null, "Prescription")
                //showImageDialog()
            }
        }

        binding.cardViewPrescription2.setOnClickListener {
            if (getBitmapList().size > 1) {
                showImage(getBitmapList()[1])
            } else {
                //showImageDialog()
                captureImage(null, "Prescription")
            }
        }

        binding.cardViewPrescription3.setOnClickListener {
            if (getBitmapList().size > 2) {
                showImage(getBitmapList()[2])
            } else {
                //showImageDialog()
                captureImage(null, "Prescription")
            }
        }

        binding.imgPrescriptionPic1Clear.setOnClickListener {
            if (getBitmapList().size > 0) {
                getBitmapList().removeAt(0)
                binding.imgPrescriptionPic1.setImageDrawable(null)
                binding.imgPrescriptionPic1.setImageResource(R.drawable.plusnew)
                onResume()
            }
        }

        binding.imgPrescriptionPic2Clear.setOnClickListener {
            if (getBitmapList().size > 1) {
                getBitmapList().removeAt(1)
                binding.imgPrescriptionPic2.setImageDrawable(null)
                binding.imgPrescriptionPic2.setImageResource(R.drawable.plusnew)
                onResume()
            }
        }

        binding.imgPrescriptionPic3Clear.setOnClickListener {
            if (getBitmapList().size > 2) {
                getBitmapList().removeAt(2)
                binding.imgPrescriptionPic3.setImageDrawable(null)
                binding.imgPrescriptionPic3.setImageResource(R.drawable.plusnew)
                onResume()
            }
        }

        binding.btnBasicDetailC.setOnClickListener {
            if (communicationType != null) {
                Utils.selectedCommunicationMode = communicationType!!
                replaceFragment(FinalReviewFragment(), R.id.layout_home, FinalReviewFragment.TAG)
            } else {
                displayMsg("Alert", "Select the communication mode")
            }
        }
    }

    private fun onAudioModeClick() {
        binding.layoutPhoneCall.setCardBackgroundColor(
            ContextCompat.getColor(
                requireActivity(),
                R.color.primaryGreen
            )
        )
        binding.imgPhoneCall.imageTintList =
            ColorStateList.valueOf(ContextCompat.getColor(requireActivity(), R.color.white))
        binding.txtPhoneCall.setTextColor(
            ContextCompat.getColor(
                requireActivity(),
                R.color.white
            )
        )

        binding.layoutVideoCall.setCardBackgroundColor(
            ContextCompat.getColor(
                requireActivity(),
                R.color.white
            )
        )
        binding.imgVideoCall.imageTintList =
            ColorStateList.valueOf(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.primaryGreen
                )
            )
        binding.txtVideoCall.setTextColor(
            ContextCompat.getColor(
                requireActivity(),
                R.color.primaryGreen
            )
        )

        binding.layoutText.setCardBackgroundColor(
            ContextCompat.getColor(
                requireActivity(),
                R.color.white
            )
        )
        binding.imgText.imageTintList =
            ColorStateList.valueOf(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.primaryGreen
                )
            )
        binding.txtText.setTextColor(
            ContextCompat.getColor(
                requireActivity(),
                R.color.primaryGreen
            )
        )
    }

    private fun onVideoModeClick() {
        binding.layoutVideoCall.setCardBackgroundColor(
            ContextCompat.getColor(
                requireActivity(),
                R.color.primaryGreen
            )
        )
        binding.imgVideoCall.imageTintList =
            ColorStateList.valueOf(ContextCompat.getColor(requireActivity(), R.color.white))
        binding.txtVideoCall.setTextColor(
            ContextCompat.getColor(
                requireActivity(),
                R.color.white
            )
        )

        binding.layoutPhoneCall.setCardBackgroundColor(
            ContextCompat.getColor(
                requireActivity(),
                R.color.white
            )
        )
        binding.imgPhoneCall.imageTintList =
            ColorStateList.valueOf(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.primaryGreen
                )
            )
        binding.txtPhoneCall.setTextColor(
            ContextCompat.getColor(
                requireActivity(),
                R.color.primaryGreen
            )
        )

        binding.layoutText.setCardBackgroundColor(
            ContextCompat.getColor(
                requireActivity(),
                R.color.white
            )
        )
        binding.imgText.imageTintList =
            ColorStateList.valueOf(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.primaryGreen
                )
            )
        binding.txtText.setTextColor(
            ContextCompat.getColor(
                requireActivity(),
                R.color.primaryGreen
            )
        )
    }

    private fun onTextModeClick() {
        binding.layoutText.setCardBackgroundColor(
            ContextCompat.getColor(
                requireActivity(),
                R.color.primaryGreen
            )
        )
        binding.imgText.imageTintList =
            ColorStateList.valueOf(ContextCompat.getColor(requireActivity(), R.color.white))
        binding.txtText.setTextColor(ContextCompat.getColor(requireActivity(), R.color.white))

        binding.layoutPhoneCall.setCardBackgroundColor(
            ContextCompat.getColor(
                requireActivity(),
                R.color.white
            )
        )
        binding.imgPhoneCall.imageTintList =
            ColorStateList.valueOf(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.primaryGreen
                )
            )
        binding.txtPhoneCall.setTextColor(
            ContextCompat.getColor(
                requireActivity(),
                R.color.primaryGreen
            )
        )

        binding.layoutVideoCall.setCardBackgroundColor(
            ContextCompat.getColor(
                requireActivity(),
                R.color.white
            )
        )
        binding.imgVideoCall.imageTintList =
            ColorStateList.valueOf(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.primaryGreen
                )
            )
        binding.txtVideoCall.setTextColor(
            ContextCompat.getColor(
                requireActivity(),
                R.color.primaryGreen
            )
        )
    }

    override fun onResume() {
        super.onResume()
        when (getBitmapList().size) {
            1 -> {
                Glide.with(this)
                    .load(File(getBitmapList()[0]))
                    .into(binding.imgPrescriptionPic1)
                binding.imgPrescriptionPic2.setImageDrawable(null)
                binding.imgPrescriptionPic2.setImageResource(R.drawable.plusnew)
                binding.imgPrescriptionPic3.setImageDrawable(null)
                binding.imgPrescriptionPic3.setImageResource(R.drawable.plusnew)
                binding.imgPrescriptionPic1Clear.visibility = View.VISIBLE
                binding.imgPrescriptionPic2Clear.visibility = View.GONE
                binding.imgPrescriptionPic3Clear.visibility = View.GONE
            }
            2 -> {
                Glide.with(this)
                    .load(File(getBitmapList()[0]))
                    .into(binding.imgPrescriptionPic1)
                Glide.with(this)
                    .load(File(getBitmapList()[1]))
                    .into(binding.imgPrescriptionPic2)
                binding.imgPrescriptionPic3.setImageDrawable(null)
                binding.imgPrescriptionPic3.setImageResource(R.drawable.plusnew)
                binding.imgPrescriptionPic1Clear.visibility = View.VISIBLE
                binding.imgPrescriptionPic2Clear.visibility = View.VISIBLE
                binding.imgPrescriptionPic3Clear.visibility = View.GONE
            }
            3 -> {
                Glide.with(this)
                    .load(File(getBitmapList()[0]))
                    .into(binding.imgPrescriptionPic1)
                Glide.with(this)
                    .load(File(getBitmapList()[1]))
                    .into(binding.imgPrescriptionPic2)
                Glide.with(this)
                    .load(File(getBitmapList()[2]))
                    .into(binding.imgPrescriptionPic3)
                binding.imgPrescriptionPic1Clear.visibility = View.VISIBLE
                binding.imgPrescriptionPic2Clear.visibility = View.VISIBLE
                binding.imgPrescriptionPic3Clear.visibility = View.VISIBLE
            }
            else -> {
                binding.imgPrescriptionPic1Clear.visibility = View.GONE
                binding.imgPrescriptionPic2Clear.visibility = View.GONE
                binding.imgPrescriptionPic3Clear.visibility = View.GONE
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
        fun newInstance(param1: Boolean, param2: String = "") =
            TherapyBasicDetailsCFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_Therapy_Basic_Details_C"
    }
}