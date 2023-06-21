package ensemblecare.csardent.com.fragment

import android.os.Bundle
import android.text.SpannableString
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import ensemblecare.csardent.com.R
import ensemblecare.csardent.com.data.Employee
import ensemblecare.csardent.com.data.Register
import ensemblecare.csardent.com.databinding.FragmentRegisterPartCBinding
import ensemblecare.csardent.com.preference.PrefKeys
import ensemblecare.csardent.com.preference.PreferenceHelper.get
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
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
    private var selectedType: String? = null
    private var employerTypeData: Array<String>? = null
    private lateinit var binding: FragmentRegisterPartCBinding

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
        binding = FragmentRegisterPartCBinding.inflate(inflater, container, false)
        return binding.root
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

        binding.checkboxCoveredSelfPay.isChecked = false
        binding.checkboxCoveredEAP.isChecked = false
        binding.checkboxCoveredInsurance.isChecked = false

        if (preference!![PrefKeys.PREF_REG, ""]!!.isNotEmpty()) {
            val register =
                Gson().fromJson(preference!![PrefKeys.PREF_REG, ""]!!, Register::class.java)
            if (register.is_employee) {
                binding.layoutEmployeeId.visibility = View.VISIBLE
                binding.etSignUpEmployeeId.setText(register.employee_id)
                binding.etSignUpEmployer.setText(register.employer)
                binding.etSignUpEmployerCode.setText(register.access_code)
            } else {
                binding.layoutEmployeeId.visibility = View.GONE
            }
        }

        /*imgRegister3Back.setOnClickListener {
            popBackStack()
        }*/

        binding.txtCoveredSelfPay.addClickableLink(
            "I hereby acknowledge and agree to abide by the terms and conditions set forth by EnsembleCare.",
            SpannableString("terms and conditions")
        ) {
            val createRegisterTermsConditions =
                BottomSheetDialog(requireActivity(), R.style.SheetDialog)
            val coveredSelfPayTermsConditionsDialog: View = layoutInflater.inflate(
                R.layout.dialog_register_part_terms_conditions, null
            )
            createRegisterTermsConditions.setContentView(coveredSelfPayTermsConditionsDialog)
            createRegisterTermsConditions.behavior.isFitToContents = false
            createRegisterTermsConditions.behavior.halfExpandedRatio = 0.6f
            createRegisterTermsConditions.setCanceledOnTouchOutside(true)
            createRegisterTermsConditions.show()
        }

        binding.txtCoveredEAP.addClickableLink(
            "I hereby acknowledge and agree to abide by the terms and conditions set forth by EnsembleCare.",
            SpannableString("terms and conditions")
        ) {
            val createRegisterTermsConditions =
                BottomSheetDialog(requireActivity(), R.style.SheetDialog)
            val coveredSelfPayTermsConditionsDialog: View = layoutInflater.inflate(
                R.layout.dialog_register_part_terms_conditions, null
            )
            createRegisterTermsConditions.setContentView(coveredSelfPayTermsConditionsDialog)
            createRegisterTermsConditions.behavior.isFitToContents = false
            createRegisterTermsConditions.behavior.halfExpandedRatio = 0.6f
            createRegisterTermsConditions.setCanceledOnTouchOutside(true)
            createRegisterTermsConditions.show()
        }

        binding.txtCoveredInsurance.addClickableLink(
            "I hereby acknowledge and agree to abide by the terms and conditions set forth by EnsembleCare.",
            SpannableString("terms and conditions")
        ) {
            val createRegisterTermsConditions =
                BottomSheetDialog(requireActivity(), R.style.SheetDialog)
            val coveredSelfPayTermsConditionsDialog: View = layoutInflater.inflate(
                R.layout.dialog_register_part_terms_conditions, null
            )
            createRegisterTermsConditions.setContentView(coveredSelfPayTermsConditionsDialog)
            createRegisterTermsConditions.behavior.isFitToContents = false
            createRegisterTermsConditions.behavior.halfExpandedRatio = 0.6f
            createRegisterTermsConditions.setCanceledOnTouchOutside(true)
            createRegisterTermsConditions.show()
        }

        binding.btnRegisterC.setOnClickListener {
            employerTypeData = resources.getStringArray(R.array.employer_type)
            when (selectedType) {
                employerTypeData!![0] -> {
                    if (binding.checkboxCoveredSelfPay.isChecked) {
                        callVerifyEmp("SelfPay")
                    } else {
                        displayMsg(
                            "Message",
                            "Please select terms and conditions for further procedure"
                        )
                    }
                }
                employerTypeData!![1] -> {
                    if (binding.checkboxCoveredInsurance.isChecked) {
                        callVerifyEmp("Insurance")
                    } else {
                        displayMsg(
                            "Message",
                            "Please select terms and conditions for further procedure"
                        )
                    }
                }
                employerTypeData!![2] -> {
                    if (getText(binding.etSignUpEmployeeId).isNotEmpty()) {
                        if (getText(binding.etSignUpEmployer).isNotEmpty()) {
                            if (getText(binding.etSignUpEmployerCode).isNotEmpty()) {
                                if (binding.checkboxCoveredEAP.isChecked) {
                                    callVerifyEmp("EAP")
                                } else {
                                    displayMsg(
                                        "Message",
                                        "Please select terms and conditions for further procedure"
                                    )
                                }
                            } else {
                                setEditTextError(binding.etSignUpEmployer, "Employer Code cannot be blank")
                            }
                        } else {
                            setEditTextError(binding.etSignUpEmployer, "Company name cannot be blank")
                        }
                    } else {
                        setEditTextError(binding.etSignUpEmployeeId, "Employee ID cannot be blank")
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
            binding.spinnerEmployerTypeList.adapter = adapter

            binding.spinnerEmployerTypeList.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?, position: Int, id: Long
                ) {
                    try {
                        selectedType = employerTypeData!![position]
                        when (selectedType) {
                            employerTypeData!![0] -> {
                                binding.layoutCoveredSelfPay.visibility = View.VISIBLE
                                binding.layoutEmployeeId.visibility = View.GONE
                                binding.layoutCoveredEAP.visibility = View.GONE
                                binding.layoutCoveredInsurance.visibility = View.GONE
                                binding.etSignUpEmployeeId.setText("")
                                binding.etSignUpEmployer.setText("")
                                binding.etSignUpEmployerCode.setText("")
                            }
                            employerTypeData!![2] -> {
                                binding.layoutEmployeeId.visibility = View.VISIBLE
                                binding.layoutCoveredSelfPay.visibility = View.GONE
                                binding.layoutCoveredInsurance.visibility = View.GONE
                                binding.layoutCoveredEAP.visibility = View.VISIBLE
                            }
                            else -> {
                                binding.layoutEmployeeId.visibility = View.GONE
                                binding.layoutCoveredSelfPay.visibility = View.GONE
                                binding.layoutCoveredInsurance.visibility = View.VISIBLE
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
                            getText(binding.etSignUpEmployeeId),
                            getText(binding.etSignUpEmployer),
                            getText(binding.etSignUpEmployerCode)
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
                                when (coveredType) {
                                    "SelfPay" -> {
                                        replaceFragment(
                                            PlanFragment.newInstance("Yes"),
                                            R.id.layout_home,
                                            PlanFragment.TAG
                                        )
                                    }
                                    "Insurance" -> {
                                        replaceFragment(
                                            InsuranceFragment(),
                                            R.id.layout_home,
                                            InsuranceFragment.TAG
                                        )
                                    }
                                    "EAP" -> {
                                        replaceFragment(
                                            InsuranceFragment.newInstance(null, null, "EAP"),
                                            R.id.layout_home,
                                            InsuranceFragment.TAG
                                        )
                                    }
                                }

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