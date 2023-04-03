package com.app.selfcare.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.app.selfcare.R
import com.app.selfcare.databinding.FragmentActivityCarePlanBinding
import com.app.selfcare.databinding.FragmentCheckEmailBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CheckEmailFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CheckEmailFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var emailID: String? = null
    private var token: String? = null
    private lateinit var binding: FragmentCheckEmailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            emailID = it.getString(ARG_PARAM1)
            token = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCheckEmailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getLayout(): Int {
        return R.layout.fragment_check_email
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE
        updateStatusBarColor(R.color.white)

        binding.resetPasswordVerifyBack.setOnClickListener {
            popBackStack()
        }

        binding.txtVerifyEmailId.text = emailID!!

        binding.resetPassCode1.addTextChangedListener(GenericTextWatcher(binding.resetPassCode2, binding.resetPassCode1))
        binding.resetPassCode2.addTextChangedListener(GenericTextWatcher(binding.resetPassCode3, binding.resetPassCode1))
        binding.resetPassCode3.addTextChangedListener(GenericTextWatcher(binding.resetPassCode4, binding.resetPassCode2))
        binding.resetPassCode4.addTextChangedListener(GenericTextWatcher(binding.resetPassCode5, binding.resetPassCode3))
        binding.resetPassCode5.addTextChangedListener(GenericTextWatcher(binding.resetPassCode5, binding.resetPassCode4))

        binding.btnResetPasswordVerify.setOnClickListener {
            val enteredToken =
                getText(binding.resetPassCode1) +
                        getText(binding.resetPassCode2) +
                        getText(binding.resetPassCode3) +
                        getText(binding.resetPassCode4) +
                        getText(binding.resetPassCode5)
            if (enteredToken == token!!) {
                replaceFragment(
                    PasswordFragment.newInstance(emailID!!, token!!),
                    R.id.layout_home,
                    PasswordFragment.TAG
                )
            } else {
                displayMsg("Alert", "Wrong Code Entered.")
            }
        }

        binding.txtCheckEmailLogin.setOnClickListener {
            replaceFragmentNoBackStack(
                LoginFragment(),
                R.id.layout_home,
                LoginFragment.TAG
            )
        }

        binding.layoutTryAnotherEmailAddress.setOnClickListener {
            popBackStack()
        }

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

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CheckEmailFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CheckEmailFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_check_email"
    }
}