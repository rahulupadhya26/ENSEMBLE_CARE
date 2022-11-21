package com.app.selfcare.fragment

import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.RadioButton
import com.app.selfcare.R
import com.app.selfcare.data.Employee
import com.app.selfcare.data.Register
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper.get
import com.app.selfcare.preference.PreferenceHelper.set
import com.app.selfcare.utils.Utils
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_register_part_c.*
import retrofit2.HttpException
import java.lang.Exception

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [RegisterPartCFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RegisterPartCFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    var selectedUserType: String = "Patient"
    private var referEmp: String = "false"
    private var selectedType: String? = null
    private var employerTypeData: Array<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_register_part_c
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE
        getSubTitle().text = ""
        updateStatusBarColor(R.color.initial_screen_background)

        employeeTypeListSpinner()

        // Get radio group selected item using on checked change listener
        radio_group.setOnCheckedChangeListener { group, checkedId ->
            val radio: RadioButton = view.findViewById(checkedId)
            if (radio.text == "Yes") {
                layout_employeeId.visibility = View.VISIBLE
                etSignUpEmployeeId.requestFocus()
            } else {
                layout_employeeId.visibility = View.GONE
                hideKeyboard(layoutReferEmp)
            }
        }

        if (preference!![PrefKeys.PREF_REG, ""]!!.isNotEmpty()) {
            val register =
                Gson().fromJson(preference!![PrefKeys.PREF_REG, ""]!!, Register::class.java)
            if (register.is_employee) {
                layout_employeeId.visibility = View.VISIBLE
                etSignUpEmployeeId.setText(register.employee_id)
                etSignUpEmployer.setText(register.employer)
            } else {
                layout_employeeId.visibility = View.GONE
            }
        }

        imgRegister3Back.setOnClickListener {
            popBackStack()
        }

        btnRegisterC.setOnClickListener {
            employerTypeData = resources.getStringArray(R.array.employer_type)
            when (selectedType) {
                employerTypeData!![0] -> {
                    replaceFragment(
                        SignUpFragment(),
                        R.id.layout_home,
                        SignUpFragment.TAG
                    )
                }
                employerTypeData!![1] -> {
                    if (getText(etSignUpEmployeeId).isNotEmpty()) {
                        if (getText(etSignUpEmployer).isNotEmpty()) {
                            callVerifyEmp()
                        } else {
                            setEditTextError(etSignUpEmployer, "Company name cannot be blank")
                        }
                    } else {
                        setEditTextError(etSignUpEmployeeId, "Employee ID cannot be blank")
                    }
                }
                employerTypeData!![2] -> {
                    replaceFragment(
                        SignUpFragment(),
                        R.id.layout_home,
                        SignUpFragment.TAG
                    )
                }
            }
        }
    }

    private fun employeeTypeListSpinner() {
        try {
            employerTypeData = resources.getStringArray(R.array.employer_type)
            val adapter = ArrayAdapter(
                requireActivity(),
                R.layout.spinner_dropdown_custom_item, employerTypeData!!
            )
            spinnerEmployerTypeList.adapter = adapter

            spinnerEmployerTypeList.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View, position: Int, id: Long
                ) {
                    selectedType = employerTypeData!![position]
                    if (selectedType == employerTypeData!![1]) {
                        layout_employeeId.visibility = View.VISIBLE
                    } else {
                        layout_employeeId.visibility = View.GONE
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // write code to perform some action
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun callVerifyEmp() {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .verifyEmp(
                        Employee(
                            getText(etSignUpEmployer),
                            getText(etSignUpEmployeeId)
                        )
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
                            responseBody = respBody[0].replace("\"", "")
                            if (responseBody == "Record Found") {
                                Utils.refEmp = true
                                Utils.employeeId = getText(etSignUpEmployeeId)
                                Utils.employer = getText(etSignUpEmployer)
                                replaceFragment(
                                    SignUpFragment(),
                                    R.id.layout_home,
                                    SignUpFragment.TAG
                                )
                            } else {
                                displayToast("Record not found!")
                            }
                        } catch (e: Exception) {
                            hideProgress()
                            displayToast("Something went wrong.. Please try after sometime")
                        }

                    }, { error ->
                        hideProgress()
                        //displayToast("Error ${error.localizedMessage}")
                        if ((error as HttpException).code() == 404 || (error as HttpException).code() == 400) {
                            displayErrorMsg(error)
                        } else {
                            displayToast("Something went wrong.. Please try after sometime")
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment RegisterPartCFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            RegisterPartCFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_Register_PartC"
    }
}