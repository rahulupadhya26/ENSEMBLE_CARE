package com.app.selfcare.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.app.selfcare.R
import com.app.selfcare.data.Register
import com.app.selfcare.data.SendOtp
import com.app.selfcare.data.VerifyOtp
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper.get
import com.app.selfcare.preference.PreferenceHelper.set
import com.app.selfcare.utils.Utils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_sign_up.*
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.HttpException
import java.lang.Error
import java.text.DecimalFormat
import java.text.NumberFormat


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SignUpFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SignUpFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var sid: String? = null
    var counter: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_sign_up
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE
        getSubTitle().text = ""
        updateStatusBarColor(R.color.initial_screen_background)

        etOtp1.addTextChangedListener(GenericTextWatcher(etOtp2, etOtp1))
        etOtp2.addTextChangedListener(GenericTextWatcher(etOtp3, etOtp1))
        etOtp3.addTextChangedListener(GenericTextWatcher(etOtp4, etOtp2))
        etOtp4.addTextChangedListener(GenericTextWatcher(etOtp4, etOtp3))

        txtName.text = "Name : " + Utils.firstName + " " + Utils.middleName + " " + Utils.lastName
        txtEmail.text = "Email : " + Utils.email
        txtPhoneNo.text = "Phone No. : " + formatNumbersAsCode(Utils.phoneNo)

        resendBtnTimer.text = "00:45"

        getOtp(Utils.email, Utils.phoneNo)

        resend.setOnClickListener {
            getOtp(
                Utils.email,
                Utils.phoneNo
            )
        }

        btn_verify_continue.setOnClickListener {
            if (getText(etOtp1).isNotEmpty() && getText(etOtp2).isNotEmpty() &&
                getText(etOtp3).isNotEmpty()
            ) {
                it.hideKeyboard()
                verifyOtp()
            } else {
                displayToast("Enter the OTP.")
            }
        }

        txt_already_account.setOnClickListener {
            for (i in 0 until mActivity!!.supportFragmentManager.backStackEntryCount) {
                if (mActivity!!.getCurrentFragment() !is WelcomeFragment) {
                    popBackStack()
                } else {
                    replaceFragment(LoginFragment(), R.id.layout_home, LoginFragment.TAG)
                    break
                }
            }
        }
    }

    private fun formatNumbersAsCode(s: CharSequence): String? {
        return java.lang.String.format(
            "%s-%s-%s",
            s.substring(0, 3),
            s.substring(3, 6),
            s.substring(6)
        )
    }

    @SuppressLint("SetTextI18n")
    private fun resendBtnTimer() {
        resend.isEnabled = false
        resend.setTextColor(ContextCompat.getColor(requireActivity(), R.color.gray))
        counter = object : CountDownTimer(45000, 1000) {
            // Callback function, fired on regular interval
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                val f: NumberFormat = DecimalFormat("00")
                val min = millisUntilFinished / 60000 % 60
                val sec = millisUntilFinished / 1000 % 60

                resendBtnTimer.text = f.format(min) + ":" + f.format(sec)
            }

            // Callback function, fired
            // when the time is up
            override fun onFinish() {
                resend.isEnabled = true
                resend.setTextColor(
                    ContextCompat.getColor(
                        requireActivity(),
                        R.color.primaryGreen
                    )
                );
                resendBtnTimer.text = ""
            }
        }.start()
    }

    class GenericTextWatcher(private val etNext: EditText, private val etPrev: EditText) :
        TextWatcher {
        override fun afterTextChanged(editable: Editable) {
            val text = editable.toString()
            if (text.length == 1) etNext.requestFocus() else if (text.isEmpty()) etPrev.requestFocus()
        }

        override fun beforeTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int) {}
        override fun onTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int) {}
    }

    private fun getOtp(email: String, phoneNo: String) {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .sendOtp(SendOtp(email, "+91917982579475"))
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
                            val jsonObj = JSONObject(responseBody)
                            sid = jsonObj.getString("sid")
                            displayToast(jsonObj.getString("otp"))
                            when (jsonObj.getString("otp").length) {
                                1 -> {
                                    etOtp1.setText(jsonObj.getString("otp")[0].toString())
                                }
                                2 -> {
                                    etOtp1.setText(jsonObj.getString("otp")[0].toString())
                                    etOtp2.setText(jsonObj.getString("otp")[1].toString())
                                }
                                3 -> {
                                    etOtp1.setText(jsonObj.getString("otp")[0].toString())
                                    etOtp2.setText(jsonObj.getString("otp")[1].toString())
                                    etOtp3.setText(jsonObj.getString("otp")[2].toString())
                                }
                                4 -> {
                                    etOtp1.setText(jsonObj.getString("otp")[0].toString())
                                    etOtp2.setText(jsonObj.getString("otp")[1].toString())
                                    etOtp3.setText(jsonObj.getString("otp")[2].toString())
                                    etOtp4.setText(jsonObj.getString("otp")[3].toString())
                                }
                            }
                            //resendBtnTimer()
                        } catch (e: Exception) {
                            hideProgress()
                            displayToast("Something went wrong.. Please try after sometime")
                        }
                    }, { error ->
                        hideProgress()
                        if ((error as HttpException).code() == 400) {
                            displayErrorMsg(error)
                        } else {
                            displayToast("Something went wrong.. Please try after sometime")
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    private fun verifyOtp() {
        val otp = getText(etOtp1) + getText(etOtp2) + getText(etOtp3) + getText(etOtp4)
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .verifyOtp(VerifyOtp(sid!!, otp))
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
                            if (counter != null) {
                                counter!!.cancel()
                            }
                            callSignUpApi()
                        } catch (e: Exception) {
                            hideProgress()
                            displayToast("Something went wrong.. Please try after sometime")
                        }
                    }, { error ->
                        hideProgress()
                        if ((error as HttpException).code() == 400) {
                            displayErrorMsg(error)
                        } else {
                            displayToast("Something went wrong.. Please try after sometime")
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    private fun callSignUpApi() {
        val registerData = Register(
            Utils.email,
            Utils.phoneNo,
            Utils.pass,
            Utils.confirmPass,
            Utils.firstName,
            Utils.lastName,
            Utils.middleName,
            Utils.dob,
            preference!![PrefKeys.PREF_DEVICE_ID, ""]!!,
            Utils.refEmp,
            Utils.employer,
            Utils.employeeId,
            Utils.gender
        )
        preference!![PrefKeys.PREF_REG] = registerData
        preference!![PrefKeys.PREF_GENDER] = Utils.gender
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .register(registerData)
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
                            //val jsonObj = JSONObject(responseBody)
                            /*preference!![PrefKeys.PREF_EMAIL] = jsonObj.getString("email")
                            preference!![PrefKeys.PREF_PHONE_NO] = jsonObj.getString("phone_0")
                            preference!![PrefKeys.PREF_FNAME] = jsonObj.getString("first_name")
                            preference!![PrefKeys.PREF_MNAME] = jsonObj.getString("middle_name")
                            preference!![PrefKeys.PREF_LNAME] = jsonObj.getString("last_name")
                            preference!![PrefKeys.PREF_DOB] = jsonObj.getString("dob")
                            preference!![PrefKeys.PREF_SSN] = jsonObj.getString("ssn")
                            preference!![PrefKeys.PREF_USER_TYPE] = "Patient"
                            preference!![PrefKeys.PREF_PASS] = Utils.pass*/
                            preference!![PrefKeys.PREF_REG] = ""
                            userLogin(
                                Utils.email,
                                Utils.pass
                            ) { result ->
                                preference!![PrefKeys.PREF_STEP] = Utils.REGISTER
                                replaceFragment(
                                    PlanFragment(),
                                    R.id.layout_home,
                                    PlanFragment.TAG
                                )
                            }
                        } catch (e: Exception) {
                            hideProgress()
                            //displayToast("Something went wrong.. Please try after sometime")
                            displayToast("We found invalid data")
                            replaceFragmentNoBackStack(
                                RegistrationFragment(),
                                R.id.layout_home,
                                RegistrationFragment.TAG
                            )
                        }

                    }, { error ->
                        hideProgress()
                        if ((error as HttpException).code() == 400) {
                            displayErrorMsg(error)
                            replaceFragmentNoBackStack(
                                RegistrationFragment(),
                                R.id.layout_home,
                                RegistrationFragment.TAG
                            )
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
         * @return A new instance of fragment SignUpFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SignUpFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_SignUp"
    }
}