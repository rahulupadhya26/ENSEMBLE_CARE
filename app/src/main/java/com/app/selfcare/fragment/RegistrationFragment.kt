package com.app.selfcare.fragment

import android.app.DatePickerDialog
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextUtils.isEmpty
import android.text.TextWatcher
import android.view.MotionEvent
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
import java.text.ParseException
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
    private var selectedPrefLang: String? = null
    private var prefLangData: Array<String>? = null
    private var register: Register? = null
    private var selectedTherapy: String? = null

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
        updateStatusBarColor(R.color.initial_screen_background)

        selectedTherapy = preference!![PrefKeys.PREF_SELECTED_THERAPY, ""]!!

        if (preference!![PrefKeys.PREF_REG, ""]!!.isNotEmpty()) {
            register = Gson().fromJson(preference!![PrefKeys.PREF_REG, ""]!!, Register::class.java)
            etSignUpFname.setText(register!!.first_name)
            etSignUpMname.setText(register!!.middle_name)
            etSignUpLname.setText(register!!.last_name)
            //etSignUpSSN.setText(register!!.ssn)
            txt_signup_dob.text = register!!.dob
        }

        genderSpinner()
        preferredLangSpinner()
        setDobCalender()

        onClickEvents()

        etSignUpFname.requestFocus()

        etSignUpSSN.addTextChangedListener(object : TextWatcher {
            private var spaceDeleted = false
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                val charDeleted = s.subSequence(start, start + count)
                spaceDeleted = "-" == charDeleted.toString()
            }

            override fun afterTextChanged(editable: Editable) {
                try {
                    etSignUpSSN.removeTextChangedListener(this)
                    val cursorPosition: Int = etSignUpSSN.selectionStart
                    val withSpaces = formatText(editable)
                    etSignUpSSN.setText(withSpaces)
                    etSignUpSSN.setSelection(cursorPosition + (withSpaces.length - editable.length))
                    if (spaceDeleted) {
                        //  userNameET.setSelection(userNameET.getSelectionStart() - 1);
                        spaceDeleted = false
                    }
                    etSignUpSSN.addTextChangedListener(this)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            private fun formatText(text: CharSequence): String {
                val formatted = StringBuilder()
                if (text.length == 3 || text.length == 6) {
                    if (!spaceDeleted) formatted.append("$text-") else formatted.append(text)
                } else formatted.append(text)
                return formatted.toString()
            }
        })
    }

    private fun genderSpinner() {
        try {
            genderData = resources.getStringArray(R.array.gender)
            val adapter = ArrayAdapter(
                requireActivity(),
                R.layout.spinner_dropdown_custom_item, genderData!!
            )
            spinner_signup_gender.adapter = adapter
            if (Utils.gender.isNotEmpty()) {
                Handler().postDelayed({
                    if (spinner_signup_gender != null)
                        spinner_signup_gender.setSelection(genderData!!.indexOf(Utils.gender))
                }, 300)
            }
            spinner_signup_gender.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View, position: Int, id: Long
                ) {
                    selectedGender = genderData!![position]
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // write code to perform some action
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun preferredLangSpinner() {
        try {
            prefLangData = resources.getStringArray(R.array.preferred_language)
            val adapter = ArrayAdapter(
                requireActivity(),
                R.layout.spinner_dropdown_custom_item, prefLangData!!
            )
            txtSignupPreferredLang.adapter = adapter
            if (Utils.prefLang.isNotEmpty()) {
                Handler().postDelayed({
                    if (txtSignupPreferredLang != null)
                        txtSignupPreferredLang.setSelection(prefLangData!!.indexOf(Utils.prefLang))
                }, 300)
            }
            txtSignupPreferredLang.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View, position: Int, id: Long
                ) {
                    selectedPrefLang = prefLangData!![position]
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // write code to perform some action
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        genderSpinner()
        preferredLangSpinner()
    }

    private fun setDobCalender() {
        val cal = Calendar.getInstance()
        //cal.add(Calendar.YEAR, -13)
        val dateSetListener =
            DatePickerDialog.OnDateSetListener { views, year, monthOfYear, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                val myFormat = "MM/dd/yyyy" // mention the format you need
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
            //datePickerDialog.datePicker.maxDate = cal.timeInMillis
            datePickerDialog.show()
        }
    }

    private fun onClickEvents() {
        btnRegister.setOnClickListener {
            if (getText(etSignUpFname).isNotEmpty()) {
                if (getText(etSignUpLname).isNotEmpty()) {
                    //if (getText(etSignUpSSN).isNotEmpty()) {
                    //if (getText(etSignUpSSN).replace("-", "").length == 9) {
                    if (txt_signup_dob.text.toString().isNotEmpty()) {
                        if (selectedGender!! != "Select...") {
                            when (selectedTherapy) {
                                "Teen" -> {
                                    if (getAge(txt_signup_dob.text.toString()) in 13..17) {
                                        storeAndNavigateToNextScreen()
                                    } else {
                                        displayMsg(
                                            "Alert",
                                            "Age must be greater than 12 years and less than 18 years."
                                        )
                                    }
                                }
                                else -> {
                                    if (getAge(txt_signup_dob.text.toString()) > 18) {
                                        storeAndNavigateToNextScreen()
                                    } else {
                                        displayMsg(
                                            "Alert",
                                            "Age must be more than 18 years."
                                        )
                                    }
                                }
                            }
                        } else {
                            displayMsg("Alert", "Select the gender")
                        }
                    } else {
                        displayMsg("Alert", "DOB cannot be blank")
                    }
                    /*} else {
                        setEditTextError(
                            etSignUpSSN, "Enter valid SSN."
                        )
                    }*/
                    /*} else {
                        setEditTextError(
                            etSignUpSSN, "SSN cannot be blank"
                        )
                    }*/
                } else {
                    setEditTextError(
                        etSignUpLname,
                        "Last name cannot be blank"
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

    private fun storeAndNavigateToNextScreen() {
        Utils.firstName = getText(etSignUpFname)
        Utils.middleName = getText(etSignUpMname)
        Utils.lastName = getText(etSignUpLname)
        Utils.ssn = getText(etSignUpSSN).replace("-", "")
        Utils.gender = selectedGender!!
        Utils.prefLang = selectedPrefLang!!
        Utils.dob = txt_signup_dob.text.toString()
        replaceFragment(
            RegisterPartBFragment(),
            R.id.layout_home,
            RegisterPartBFragment.TAG
        )
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