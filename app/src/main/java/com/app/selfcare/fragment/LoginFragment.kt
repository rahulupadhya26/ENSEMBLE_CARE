package com.app.selfcare.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.app.selfcare.R
import com.app.selfcare.databinding.FragmentActivityCarePlanBinding
import com.app.selfcare.databinding.FragmentLoginBinding
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper.get
import com.app.selfcare.preference.PreferenceHelper.set
import com.app.selfcare.utils.Utils


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
    private lateinit var binding: FragmentLoginBinding

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
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
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
            binding.checkBoxKeepMeSignedIn.isChecked = true
            binding.editUsername.setText(preference!![PrefKeys.PREF_EMAIL, ""]!!)
            binding.editPassword.setText(preference!![PrefKeys.PREF_PASS, ""]!!)
        }

        binding.loginBack.setOnClickListener {
            replaceFragmentNoBackStack(
                WelcomeFragment(),
                R.id.layout_home,
                WelcomeFragment.TAG
            )
        }

        binding.loginForgotPassword.setOnClickListener {
            replaceFragment(
                ResetPasswordFragment(),
                R.id.layout_home,
                ResetPasswordFragment.TAG
            )
        }

        binding.btnLogin.setOnClickListener {
            if (getText(binding.editUsername).isNotEmpty()) {
                if (getText(binding.editPassword).isNotEmpty()) {
                    userLogin(getText(binding.editUsername), getText(binding.editPassword)) { result ->
                        Utils.isLoggedInFirstTime = true
                        preference!![PrefKeys.PREF_IS_REMEMBER_ME] = binding.checkBoxKeepMeSignedIn.isChecked
                        setBottomNavigation(null)
                        setLayoutBottomNavigation(null)
                        replaceFragmentNoBackStack(
                            BottomNavigationFragment(),
                            R.id.layout_home,
                            BottomNavigationFragment.TAG
                        )
                    }
                } else {
                    setEditTextError(binding.editPassword, "Password cannot be blank.")
                }
            } else {
                setEditTextError(binding.editUsername, "Email Id cannot be blank.")
            }
        }

        binding.layoutLoginBottom.setOnClickListener {
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