package com.app.selfcare.fragment

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.app.selfcare.R
import com.app.selfcare.data.Register
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper.get
import com.app.selfcare.utils.Utils
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_registration.*
import java.text.SimpleDateFormat
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [RegistrationFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RegistrationFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var selectedGender: String? = null
    private var genderData: Array<String>? = null
    private var register: Register? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_registration
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE
        getSubTitle().text = ""

        if (preference!![PrefKeys.PREF_REG, ""]!!.isNotEmpty()) {
            register = Gson().fromJson(preference!![PrefKeys.PREF_REG, ""]!!, Register::class.java)
            etSignUpFname.setText(register!!.first_name)
            etSignUpMname.setText(register!!.middle_name)
            etSignUpLname.setText(register!!.last_name)
            etSignUpSSN.setText(register!!.ssn)
            txt_signup_dob.setText(register!!.dob)
        }

        genderSpinner()
        setDobCalender()

        onClickEvents(view)
    }

    private fun genderSpinner() {
        genderData = resources.getStringArray(R.array.gender)
        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
            requireActivity(), android.R.layout.simple_spinner_dropdown_item, genderData!!
        )
        spinner_signup_gender.setAdapter(adapter)
        if (preference!![PrefKeys.PREF_GENDER, ""]!!.isNotEmpty()) {
            val spinnerPosition = adapter.getPosition(preference!![PrefKeys.PREF_GENDER, ""]!!)
            spinner_signup_gender.setSelection(spinnerPosition)
        }
        spinner_signup_gender.onItemClickListener =
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
                txt_signup_dob.setText(sdf.format(cal.time))
            }

        txt_signup_dob.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                mActivity!!, dateSetListener,
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
            datePickerDialog.show()
        }
    }

    private fun onClickEvents(view: View) {
        btnRegister.setOnClickListener {
            if (getText(etSignUpFname).isNotEmpty()) {
                if (getText(etSignUpMname).isNotEmpty()) {
                    if (getText(etSignUpLname).isNotEmpty()) {
                        if (getText(etSignUpSSN).isNotEmpty()) {
                            if (txt_signup_dob.text.toString().isNotEmpty()) {
                                Utils.firstName = getText(etSignUpFname)
                                Utils.middleName = getText(etSignUpMname)
                                Utils.lastName = getText(etSignUpLname)
                                Utils.ssn = getText(etSignUpSSN)
                                Utils.gender = selectedGender!!
                                Utils.dob = txt_signup_dob.text.toString()
                                replaceFragment(
                                    RegisterPartBFragment(),
                                    R.id.layout_home,
                                    RegisterPartBFragment.TAG
                                )
                            } else {
                                displayToast("DOB cannot be blank")
                            }
                        } else {
                            setEditTextError(
                                etSignUpSSN, "SSN cannot be blank"
                            )
                        }
                    } else {
                        setEditTextError(
                            etSignUpLname,
                            "Last name cannot be blank"
                        )
                    }
                } else {
                    setEditTextError(
                        etSignUpMname,
                        "Middle name cannot be blank"
                    )
                }
            } else {
                setEditTextError(
                    etSignUpFname,
                    "First name cannot be blank"
                )
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
         * @return A new instance of fragment RegistrationFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            RegistrationFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_Profile_SignUp"
    }
}