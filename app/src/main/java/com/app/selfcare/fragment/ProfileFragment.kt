package com.app.selfcare.fragment

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.app.selfcare.R
import com.app.selfcare.preference.PrefKeys
import kotlinx.android.synthetic.main.fragment_profile.*
import com.app.selfcare.preference.PreferenceHelper.get
import kotlinx.android.synthetic.main.fragment_registration.*
import java.text.SimpleDateFormat
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    var selectedGender: String? = null
    var genderData: Array<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_profile
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.VISIBLE
        getSubTitle().visibility = View.GONE
        genderSpinner()
        setDobCalender()

        etProfileFname.setText(preference!![PrefKeys.PREF_FNAME, ""]!!)
        etProfileMname.setText(preference!![PrefKeys.PREF_MNAME, ""]!!)
        etProfileLname.setText(preference!![PrefKeys.PREF_LNAME, ""]!!)
        etProfileSSN.setText(preference!![PrefKeys.PREF_SSN, ""]!!)
        //Set gender
        txtProfileDob.setText(preference!![PrefKeys.PREF_DOB, ""]!!)
        etProfileMailId.setText(preference!![PrefKeys.PREF_EMAIL, ""]!!)
        etProfilePhoneNo.setText(preference!![PrefKeys.PREF_PHONE_NO, ""]!!)
    }

    private fun genderSpinner() {
        genderData = resources.getStringArray(R.array.gender)
        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
            requireActivity(), android.R.layout.simple_spinner_dropdown_item, genderData!!
        )
        spinnerProfileGender.setAdapter(adapter)
        spinnerProfileGender.onItemClickListener =
            AdapterView.OnItemClickListener { parent, arg1, position, id ->
                //TODO: You can your own logic.
                selectedGender = genderData!![position]
            }
    }

    private fun setDobCalender() {
        val cal = Calendar.getInstance()
        val dateSetListener =
            DatePickerDialog.OnDateSetListener { views, year, monthOfYear, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                val myFormat = "yyyy-MM-dd" // mention the format you need
                val sdf = SimpleDateFormat(myFormat)
                txtProfileDob.setText(sdf.format(cal.time))
            }

        txtProfileDob.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                mActivity!!, dateSetListener,
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ProfileFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_Profile"
    }
}