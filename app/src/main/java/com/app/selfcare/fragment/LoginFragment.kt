package com.app.selfcare.fragment

import android.os.Bundle
import android.view.View
import com.app.selfcare.R
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper.get
import kotlinx.android.synthetic.main.fragment_login.*
import com.app.selfcare.preference.PreferenceHelper.set


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [LoginFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LoginFragment : BaseFragment() {
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
        return R.layout.fragment_login
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE
        getSubTitle().text = ""

        if (preference!![PrefKeys.PREF_IS_REMEMBER_ME, false]!!) {
            checkBoxKeepMeSignedIn.isChecked = true
            edit_username.setText(preference!![PrefKeys.PREF_EMAIL, ""]!!)
            edit_password.setText(preference!![PrefKeys.PREF_PASS, ""]!!)
        }

        loginBack.setOnClickListener {
            replaceFragmentNoBackStack(
                WelcomeFragment(),
                R.id.layout_home,
                WelcomeFragment.TAG
            )
        }

        loginForgotPassword.setOnClickListener {
            replaceFragment(
                ResetPasswordFragment(),
                R.id.layout_home,
                ResetPasswordFragment.TAG
            )
        }

        btn_login.setOnClickListener {
            if (getText(edit_username).isNotEmpty()) {
                if (getText(edit_password).isNotEmpty()) {
                    userLogin(getText(edit_username), getText(edit_password)) { result ->
                        preference!![PrefKeys.PREF_IS_REMEMBER_ME] = checkBoxKeepMeSignedIn.isChecked
                        setBottomNavigation(null)
                        setLayoutBottomNavigation(null)
                        replaceFragmentNoBackStack(
                            BottomNavigationFragment(),
                            R.id.layout_home,
                            BottomNavigationFragment.TAG
                        )
                    }
                } else {
                    setEditTextError(edit_password, "Password cannot be blank.")
                }
            } else {
                setEditTextError(edit_username, "Email Id cannot be blank.")
            }
        }

        layoutLoginBottom.setOnClickListener {
            replaceFragment(CarouselFragment(), R.id.layout_home, CarouselFragment.TAG)
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment LoginFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            LoginFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_Login"
    }
}