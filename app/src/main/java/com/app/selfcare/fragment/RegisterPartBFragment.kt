package com.app.selfcare.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.app.selfcare.R
import com.app.selfcare.data.Register
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper.get
import com.app.selfcare.utils.Utils
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_register_part_a.*
import kotlinx.android.synthetic.main.fragment_register_part_b.*

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_register_part_b
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.VISIBLE
        getSubTitle().visibility = View.GONE
        getSubTitle().text = ""

        if (preference!![PrefKeys.PREF_REG, ""]!!.isNotEmpty()) {
            val register =
                Gson().fromJson(preference!![PrefKeys.PREF_REG, ""]!!, Register::class.java)
            etSignUpMailId.setText(register.email)
            etSignUpPhoneNo.setText(register.phone_0)
            etSignUpPass.setText(register.password1)
            etSignUpConfirmPass.setText(register.password2)
        }

        btnRegisterB.setOnClickListener {
            if (getText(etSignUpMailId).isNotEmpty()) {
                if (getText(etSignUpPhoneNo).isNotEmpty()) {
                    if (getText(etSignUpPass).isNotEmpty()) {
                        if (getText(etSignUpConfirmPass).isNotEmpty()) {
                            if (getText(etSignUpPass) == getText(etSignUpConfirmPass)) {
                                if (isValidPasswordFormat(getText(etSignUpPass))) {
                                    if (!(getText(etSignUpPass).contains(Utils.firstName) ||
                                                getText(etSignUpPass).contains(Utils.middleName) ||
                                                getText(etSignUpPass).contains(Utils.lastName) ||
                                                getText(etSignUpPass).contains(getText(etSignUpMailId)))
                                    ) {
                                        Utils.email = getText(etSignUpMailId)
                                        Utils.phoneNo = getText(etSignUpPhoneNo)
                                        Utils.pass = getText(etSignUpPass)
                                        Utils.confirmPass = getText(etSignUpConfirmPass)
                                        replaceFragment(
                                            RegisterPartCFragment(),
                                            R.id.layout_home,
                                            RegisterPartCFragment.TAG
                                        )
                                    } else {
                                        setEditTextError(
                                            etSignUpPass,
                                            "Password should not contain name or email"
                                        )
                                    }
                                } else {
                                    setEditTextError(
                                        etSignUpPass,
                                        "Password must be contain at least 9 characters, 1 uppercase, alphanumeric and should not contain whitespaces."
                                    )
                                }
                            } else {
                                displayToast("Password Mismatch")
                            }
                        } else {
                            setEditTextError(
                                etSignUpConfirmPass,
                                "Confirm password cannot be blank"
                            )
                        }
                    } else {
                        setEditTextError(
                            etSignUpPass,
                            "Password cannot be blank"
                        )
                    }
                } else {
                    setEditTextError(
                        etSignUpPhoneNo,
                        "Phone number cannot be blank"
                    )
                }
            } else {
                setEditTextError(
                    etSignUpMailId,
                    "Mail Id cannot be blank"
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