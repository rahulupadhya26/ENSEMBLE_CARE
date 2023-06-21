package ensemblecare.csardent.com.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import ensemblecare.csardent.com.R
import ensemblecare.csardent.com.data.ConfirmPassword
import ensemblecare.csardent.com.databinding.FragmentPasswordBinding
import ensemblecare.csardent.com.preference.PrefKeys
import ensemblecare.csardent.com.preference.PreferenceHelper.get
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import java.lang.Exception

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PasswordFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PasswordFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var emailID: String? = null
    private var token: String? = null
    private lateinit var binding: FragmentPasswordBinding

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
        binding = FragmentPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getLayout(): Int {
        return R.layout.fragment_password
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE
        getSubTitle().text = ""

        binding.newPasswordBack.setOnClickListener {
            popBackStack()
        }

        binding.btnResetPassword.setOnClickListener {
            if (isValidPasswordFormat(getText(binding.createNewPassword))) {
                if (getText(binding.createNewPassword) == getText(binding.createNewConfirmPassword)) {
                    //Call Api
                    confirmPassword()
                } else {
                    displayMsg("Alert", "Password mismatch")
                }
            } else {
                setEditTextError(binding.createNewPassword, "Enter valid password")
            }
        }

        /*for (i in 0 until mActivity!!.supportFragmentManager.backStackEntryCount) {
                replaceFragmentNoBackStack(WelcomeFragment(), R.id.layout_home, WelcomeFragment.TAG)
                if (mActivity!!.getCurrentFragment() !is WelcomeFragment) {
                    popBackStack()
                } else {
                    replaceFragment(LoginFragment(), R.id.layout_home, LoginFragment.TAG)
                    break
                }
            }*/
    }

    private fun confirmPassword() {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .confirmPassword(
                        ConfirmPassword(getText(binding.createNewPassword), token!!),
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
                            responseBody = respBody[0]
                            replaceFragmentNoBackStack(
                                LoginFragment(),
                                R.id.layout_home,
                                LoginFragment.TAG
                            )
                        } catch (e: Exception) {
                            hideProgress()
                            displayToast("Something went wrong.. Please try after sometime")
                        }

                    }, { error ->
                        hideProgress()
                        if ((error as HttpException).code() == 401) {
                            userLogin(
                                preference!![PrefKeys.PREF_EMAIL]!!,
                                preference!![PrefKeys.PREF_PASS]!!
                            ) { result ->
                                confirmPassword()
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
         * @return A new instance of fragment PasswordFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PasswordFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_change_password"
    }
}