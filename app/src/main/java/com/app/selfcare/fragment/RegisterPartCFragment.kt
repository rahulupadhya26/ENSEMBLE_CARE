package com.app.selfcare.fragment

import android.os.Bundle
import android.text.SpannableString
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.app.selfcare.R
import com.app.selfcare.data.Employee
import com.app.selfcare.data.Register
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper.get
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_register_part_c.*
import retrofit2.HttpException
import kotlin.Exception

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

        checkboxCoveredSelfPay.isChecked = false

        if (preference!![PrefKeys.PREF_REG, ""]!!.isNotEmpty()) {
            val register =
                Gson().fromJson(preference!![PrefKeys.PREF_REG, ""]!!, Register::class.java)
            if (register.is_employee) {
                layout_employeeId.visibility = View.VISIBLE
                etSignUpEmployeeId.setText(register.employee_id)
                etSignUpEmployer.setText(register.employer)
                etSignUpEmployerCode.setText(register.access_code)
            } else {
                layout_employeeId.visibility = View.GONE
            }
        }

        imgRegister3Back.setOnClickListener {
            popBackStack()
        }

        checkboxCoveredSelfPay.addClickableLink(
            "I hereby agree to abide by the terms and conditions provider by EnsembleCare for SelfPay",
            SpannableString("terms and conditions")
        ) {
            val createRegisterTermsConditions = BottomSheetDialog(requireActivity(), R.style.SheetDialog)
            val coveredSelfPayTermsConditionsDialog: View = layoutInflater.inflate(
                R.layout.dialog_register_part_terms_conditions, null
            )
            createRegisterTermsConditions.setContentView(coveredSelfPayTermsConditionsDialog)
            createRegisterTermsConditions.behavior.isFitToContents = false
            createRegisterTermsConditions.behavior.halfExpandedRatio = 0.6f
            createRegisterTermsConditions.setCanceledOnTouchOutside(true)
            createRegisterTermsConditions.show()
        }

        btnRegisterC.setOnClickListener {
            employerTypeData = resources.getStringArray(R.array.employer_type)
            when (selectedType) {
                employerTypeData!![0] -> {
                    if (checkboxCoveredSelfPay.isChecked) {
                        callVerifyEmp("SelfPay")
                    } else {
                        displayMsg(
                            "Message",
                            "Please select terms and conditions for further procedure"
                        )
                    }
                }
                employerTypeData!![1] -> {
                    callVerifyEmp("Insurance")
                }
                employerTypeData!![2] -> {
                    if (getText(etSignUpEmployeeId).isNotEmpty()) {
                        if (getText(etSignUpEmployer).isNotEmpty()) {
                            if (getText(etSignUpEmployerCode).isNotEmpty()) {
                                callVerifyEmp("EAP")
                            } else {
                                setEditTextError(etSignUpEmployer, "Employer Code cannot be blank")
                            }
                        } else {
                            setEditTextError(etSignUpEmployer, "Company name cannot be blank")
                        }
                    } else {
                        setEditTextError(etSignUpEmployeeId, "Employee ID cannot be blank")
                    }
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
                    parent: AdapterView<*>?,
                    view: View?, position: Int, id: Long
                ) {
                    try {
                        selectedType = employerTypeData!![position]
                        when (selectedType) {
                            employerTypeData!![0] -> {
                                layoutCoveredSelfPay.visibility = View.VISIBLE
                                layout_employeeId.visibility = View.GONE
                                etSignUpEmployeeId.setText("")
                                etSignUpEmployer.setText("")
                                etSignUpEmployerCode.setText("")
                            }
                            employerTypeData!![2] -> {
                                layout_employeeId.visibility = View.VISIBLE
                                layoutCoveredSelfPay.visibility = View.GONE
                            }
                            else -> {
                                layout_employeeId.visibility = View.GONE
                                layoutCoveredSelfPay.visibility = View.GONE
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // write code to perform some action
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun callVerifyEmp(coveredType: String) {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .coveredType(
                        Employee(
                            coveredType,
                            getText(etSignUpEmployeeId),
                            getText(etSignUpEmployer),
                            getText(etSignUpEmployerCode)
                        ),
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
                            responseBody = respBody[0].replace("\"", "")
                            if (responseBody == "covered type updated") {
                                /*Utils.refEmp = true
                                Utils.employeeId = getText(etSignUpEmployeeId)
                                Utils.employer = getText(etSignUpEmployer)*/
                                replaceFragment(
                                    PlanFragment(),
                                    R.id.layout_home,
                                    PlanFragment.TAG
                                )
                            } else {
                                displayMsg(
                                    "Alert",
                                    "Employer or Employee Id or Access Code is not correct"
                                )
                            }
                        } catch (e: Exception) {
                            hideProgress()
                            e.printStackTrace()
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
                                callVerifyEmp(coveredType)
                            }
                        } else {
                            displayAfterLoginErrorMsg(error)
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