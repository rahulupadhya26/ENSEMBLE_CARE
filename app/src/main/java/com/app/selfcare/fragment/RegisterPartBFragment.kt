package com.app.selfcare.fragment

import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import android.telephony.PhoneNumberUtils
import android.text.Editable
import android.text.SpannableString
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.app.selfcare.R
import com.app.selfcare.data.Register
import com.app.selfcare.data.UserDetails
import com.app.selfcare.databinding.FragmentRegisterPartBBinding
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper.get
import com.app.selfcare.utils.Utils
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import java.lang.Exception
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [RegisterPartBFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RegisterPartBFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentRegisterPartBBinding

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
        binding = FragmentRegisterPartBBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getLayout(): Int {
        return R.layout.fragment_register_part_b
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE
        getSubTitle().text = ""
        updateStatusBarColor(R.color.initial_screen_background)

        if (preference!![PrefKeys.PREF_REG, ""]!!.isNotEmpty()) {
            val register =
                Gson().fromJson(preference!![PrefKeys.PREF_REG, ""]!!, Register::class.java)
            binding.etSignUpMailId.setText(register.email)
            binding.etSignUpPhoneNo.setText(register.phone_0)
            binding.etSignUpPass.setText(register.password1)
            binding.etSignUpConfirmPass.setText(register.password2)
        }

        binding.etSignUpMailId.requestFocus()

        binding.etSignUpPhoneNo.addTextChangedListener(object : PhoneNumberFormattingTextWatcher("US") {
            private var mFormatting = false
            private var mAfter = 0

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                super.beforeTextChanged(s, start, count, after)
                mAfter = after
            }

            override fun afterTextChanged(s: Editable?) {
                super.afterTextChanged(s)
                if (!mFormatting) {
                    mFormatting = true
                    // using US formatting.
                    if (mAfter != 0) // in case back space ain't clicked.
                        PhoneNumberUtils.formatNumber(
                            s, PhoneNumberUtils.getFormatTypeForLocale(
                                Locale.US
                            )
                        )
                    mFormatting = false
                }
            }
        })

        binding.txtRegisterTermsConditions.addClickableLink(
            "I hereby acknowledge and agree to abide by the terms and conditions set forth by EnsembleCare.",
            SpannableString("terms and conditions")
        ) {
            val createRegisterTermsConditions = BottomSheetDialog(requireActivity(), R.style.SheetDialog)
            val registerTermsConditionsDialog: View = layoutInflater.inflate(
                R.layout.dialog_register_part_terms_conditions, null
            )
            createRegisterTermsConditions.setContentView(registerTermsConditionsDialog)
            createRegisterTermsConditions.behavior.isFitToContents = false
            createRegisterTermsConditions.behavior.halfExpandedRatio = 0.6f
            createRegisterTermsConditions.setCanceledOnTouchOutside(true)
            createRegisterTermsConditions.show()
        }

        binding.imgRegister2Back.setOnClickListener {
            popBackStack()
        }

        binding.btnRegisterB.setOnClickListener {
            if (getText(binding.etSignUpMailId).isNotEmpty()) {
                if (isValidEmail(binding.etSignUpMailId)) {
                    if (getText(binding.etSignUpPhoneNo).isNotEmpty()) {
                        if (getText(binding.etSignUpPhoneNo).replace("-", "").length == 10) {
                            if (getText(binding.etSignUpPass).isNotEmpty()) {
                                if (getText(binding.etSignUpConfirmPass).isNotEmpty()) {
                                    if (getText(binding.etSignUpPass) == getText(binding.etSignUpConfirmPass)) {
                                        if (isValidPasswordFormat(getText(binding.etSignUpPass))) {
                                            if (!(getText(binding.etSignUpPass).contains(Utils.firstName) ||
                                                        getText(binding.etSignUpPass).contains(Utils.lastName) ||
                                                        getText(binding.etSignUpPass).contains(
                                                            getText(
                                                                binding.etSignUpMailId
                                                            )
                                                        ))
                                            ) {
                                                if (binding.checkboxRegisterTermsConditions.isChecked) {
                                                    validateUserDetails()
                                                } else {
                                                    displayMsg(
                                                        "Message",
                                                        "Please select terms and conditions for further procedure"
                                                    )
                                                }
                                            } else {
                                                setEditTextError(
                                                    binding.etSignUpPass,
                                                    "Password should not contain name or email"
                                                )
                                            }
                                        } else {
                                            setEditTextError(
                                                binding.etSignUpPass,
                                                "Password must be contain at least 9 characters, " +
                                                        "1 uppercase, 1 lowercase, alphanumeric, special characters " +
                                                        "and should not contain whitespaces."
                                            )
                                        }
                                    } else {
                                        displayToast("Password Mismatch")
                                    }
                                } else {
                                    setEditTextError(
                                        binding.etSignUpConfirmPass,
                                        "Confirm password cannot be blank"
                                    )
                                }
                            } else {
                                setEditTextError(
                                    binding.etSignUpPass,
                                    "Password cannot be blank"
                                )
                            }
                        } else {
                            setEditTextError(
                                binding.etSignUpPhoneNo,
                                "Enter valid phone number"
                            )
                        }
                    } else {
                        setEditTextError(
                            binding.etSignUpPhoneNo,
                            "Phone number cannot be blank"
                        )
                    }
                } else {
                    setEditTextError(
                        binding.etSignUpMailId,
                        "Enter valid Mail Id"
                    )
                }
            } else {
                setEditTextError(
                    binding.etSignUpMailId,
                    "Email ID cannot be blank"
                )
            }
        }
    }

    private fun validateUserDetails() {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .verifyUserDetail(
                        UserDetails(
                            getText(binding.etSignUpMailId),
                            getText(binding.etSignUpPhoneNo)
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
                            if (status == "200") {
                                Utils.email = getText(binding.etSignUpMailId)
                                Utils.phoneNo = getText(binding.etSignUpPhoneNo).replace("-", "")
                                Utils.pass = getText(binding.etSignUpPass)
                                Utils.confirmPass = getText(binding.etSignUpConfirmPass)
                                replaceFragment(
                                    SignUpFragment(),
                                    R.id.layout_home,
                                    SignUpFragment.TAG
                                )
                            } else {
                                displayMsg("Alert", responseBody.trim())
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
         * @return A new instance of fragment RegisterPartBFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            RegisterPartBFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_Register_PartB"
    }
}