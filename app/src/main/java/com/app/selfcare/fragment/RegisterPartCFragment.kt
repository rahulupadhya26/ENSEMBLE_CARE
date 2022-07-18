package com.app.selfcare.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import kotlinx.android.synthetic.main.fragment_register_part_b.*
import kotlinx.android.synthetic.main.fragment_register_part_c.*
import kotlinx.android.synthetic.main.fragment_registration.*
import org.json.JSONObject
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
        getBackButton().visibility = View.VISIBLE
        getSubTitle().visibility = View.GONE
        getSubTitle().text = ""

        // Get radio group selected item using on checked change listener
        radio_group.setOnCheckedChangeListener { group, checkedId ->
            val radio: RadioButton = view.findViewById(checkedId)
            if (radio.text == "Yes") {
                layout_employeeId.visibility = View.VISIBLE
            } else {
                layout_employeeId.visibility = View.GONE
            }
        }

        if (preference!![PrefKeys.PREF_REG, ""]!!.isNotEmpty()) {
            val register =
                Gson().fromJson(preference!![PrefKeys.PREF_REG, ""]!!, Register::class.java)
            if (register.is_employee) {
                empYes.isChecked = true
                empNo.isChecked = false
                layout_employeeId.visibility = View.VISIBLE
                etSignUpEmployeeId.setText(register.employee_id)
                etSignUpEmployer.setText(register.employer)
            } else {
                empNo.isChecked = true
                empYes.isChecked = false
                layout_employeeId.visibility = View.GONE
            }
        }

        empYes.setOnClickListener {
            layout_employeeId.visibility = View.VISIBLE
        }

        empNo.setOnClickListener {
            layout_employeeId.visibility = View.GONE
        }

        btnRegisterC.setOnClickListener {
            if (empNo.isChecked) {
                replaceFragment(
                    SignUpFragment(),
                    R.id.layout_home,
                    SignUpFragment.TAG
                )
            } else {
                if (empYes.isChecked && getText(etSignUpEmployeeId).isNotEmpty()) {
                    if (empYes.isChecked && getText(etSignUpEmployer).isNotEmpty()) {
                        callVerifyEmp()
                    } else {
                        setEditTextError(etSignUpEmployer, "Employer cannot be blank")
                    }
                } else {
                    setEditTextError(etSignUpEmployeeId, "Employee Id cannot be blank")
                }
            }
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
                            responseBody = respBody[0]
                            if (responseBody == "Record Found") {
                                Utils.refEmp = empYes.isChecked
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
                        displayToast("Error ${error.localizedMessage}")
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