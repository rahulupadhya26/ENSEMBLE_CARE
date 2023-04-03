package com.app.selfcare.fragment

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.app.selfcare.R
import com.app.selfcare.controller.IOnBackPressed
import com.app.selfcare.databinding.DialogCheckTeenDobBinding
import com.app.selfcare.databinding.FragmentActivityCarePlanBinding
import com.app.selfcare.databinding.FragmentCarouselBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.text.SimpleDateFormat
import java.util.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CarouselFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CarouselFragment : BaseFragment(), IOnBackPressed {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var therapySel: String = ""
    private lateinit var binding: FragmentCarouselBinding

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
        binding = FragmentCarouselBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getLayout(): Int {
        return R.layout.fragment_carousel
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE
        getSubTitle().text = ""
        updateStatusBarColor(R.color.initial_screen_background)

        binding.cardViewSelf.setOnClickListener {
            therapySel = "Individual"
            getConfirmation(therapySel, binding.txtTherapySelf.text.toString())
        }

        binding.cardViewTeen.setOnClickListener {
            therapySel = "Teen"
            getConfirmation(therapySel, binding.txtTherapyTeen.text.toString())
        }

        binding.cardViewCouple.setOnClickListener {
            therapySel = "Couple"
            getConfirmation(therapySel, binding.txtTherapyCouple.text.toString())
        }

        binding.cardViewLgbtq.setOnClickListener {
            therapySel = "LGBTQ"
            getConfirmation(therapySel, binding.txtTherapyLgbtqia.text.toString())
        }
    }

    @SuppressLint("HardwareIds")
    private fun sendDeviceId(selectedTherapy: String, actualTherapyTxt:String) {
        /*if (selectedTherapy == "Teen") {
            checkTeenDob(selectedTherapy)
        } else {*/
        //createAnonymousUser(selectedTherapy, "", "", "", "")
        //}
    }

    private fun checkTeenDob(selectedTherapy: String) {
        val createPostDialog = BottomSheetDialog(mContext!!)
        val checkTeenDob = DialogCheckTeenDobBinding.inflate(layoutInflater)
        val view = checkTeenDob.root
        /*val checkTeenDob = requireActivity().layoutInflater.inflate(
            R.layout.dialog_check_teen_dob, null
        )*/
        //onlineChatView!!.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        createPostDialog.setContentView(view)
        createPostDialog.setCanceledOnTouchOutside(false)

        checkTeenDob.txtVerify.setOnClickListener {
            if (getAge(checkTeenDob.txtTeenDob.text.toString()) in 13..17) {
                createPostDialog.dismiss()
                //checkApplicationUser(selectedTherapy)
            } else {
                displayMsg(
                    "Alert",
                    "Age must be greater than 12 years and less than 18 years."
                )
            }
        }

        checkTeenDob.txtCancel.setOnClickListener {
            createPostDialog.dismiss()
        }
        val cal = Calendar.getInstance()
        //cal.add(Calendar.YEAR, -13)
        val dateSetListener =
            DatePickerDialog.OnDateSetListener { views, year, monthOfYear, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                val myFormat = "MM/dd/yyyy" // mention the format you need
                val sdf = SimpleDateFormat(myFormat)
                checkTeenDob.txtTeenDob.setText(sdf.format(cal.time))
            }

        checkTeenDob.txtTeenDob.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                mActivity!!, dateSetListener,
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            )
            //datePickerDialog.datePicker.maxDate = cal.timeInMillis
            datePickerDialog.show()
        }
        createPostDialog.show()
    }

    private fun getConfirmation(selectedTherapy: String, actualTherapyTxt:String) {
        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle("Confirmation")
        builder.setMessage("Selected therapy is $actualTherapyTxt")
        builder.setPositiveButton("Confirm") { dialog, _ ->
            dialog.dismiss()
            createAnonymousUser(selectedTherapy, "", "", "", "")
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CarouselFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CarouselFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_Therapy_Select"
    }

    override fun onBackPressed(): Boolean {
        popBackStack()
        return false
    }
}