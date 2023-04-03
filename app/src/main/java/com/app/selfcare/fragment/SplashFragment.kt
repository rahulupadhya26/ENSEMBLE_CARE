package com.app.selfcare.fragment


import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.app.selfcare.R
import com.app.selfcare.data.ConsentRoisFormsNotify
import com.app.selfcare.data.NotifyStatus
import com.app.selfcare.data.PlanDetails
import com.app.selfcare.databinding.FragmentActivityCarePlanBinding
import com.app.selfcare.databinding.FragmentSplashBinding
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper.get
import com.app.selfcare.utils.Utils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.json.JSONArray
import org.json.JSONObject

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SplashFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SplashFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentSplashBinding

    override fun getLayout(): Int {
        return R.layout.fragment_splash
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        Utils.bottomNav = Utils.BOTTOM_NAV_DASHBOARD
        Handler().postDelayed(Runnable { // This method will be executed once the timer is over
            if (preference!![PrefKeys.PREF_USER_ID, ""]!!.isNotEmpty()) {
                if (preference!![PrefKeys.PREF_IS_LOGGEDIN, false]!!) {
                    if (preference!![PrefKeys.PREF_STEP, 0]!! == Utils.REGISTER) {
                        if (preference!![PrefKeys.PREF_INSURANCE_VERIFICATION, false]!!) {
                            checkVOB()
                        } else {
                            replaceFragmentNoBackStack(
                                RegisterPartCFragment(),
                                R.id.layout_home,
                                RegisterPartCFragment.TAG
                            )
                        }
                    } else {
                        replaceFragmentNoBackStack(
                            BottomNavigationFragment(),
                            R.id.layout_home,
                            BottomNavigationFragment.TAG
                        )
                    }
                } else {
                    replaceFragmentNoBackStack(
                        WelcomeFragment(),
                        R.id.layout_home,
                        WelcomeFragment.TAG
                    )
                }
            } else {
                replaceFragmentNoBackStack(WelcomeFragment(), R.id.layout_home, WelcomeFragment.TAG)
            }
        }, 1000)
    }

    private fun checkVOB() {
        getDashboardNotifications { response ->
            try {
                val jsonArr = JSONArray(response)
                var isVobFound = false
                var jsonObj = JSONObject()
                for (i in 0 until jsonArr.length()) {
                    jsonObj = jsonArr.getJSONObject(i)
                    if (jsonObj.getString("type").contains("vob", ignoreCase = true)) {
                        isVobFound = true
                    }
                }
                if (isVobFound) {
                    updateNotificationStatus(jsonObj.getInt("id"))
                    val planDetailJsonObj =
                        jsonObj.getJSONObject("extra_data").getJSONObject("subscription_detail")
                    when (jsonObj.getString("description")) {
                        Utils.VOB_STATUS_PENDING -> {
                            replaceFragmentNoBackStack(
                                InsuranceVerifyFragment.newInstance(null, Utils.VOB_PENDING, null),
                                R.id.layout_home,
                                InsuranceVerifyFragment.TAG
                            )
                        }
                        Utils.VOB_STATUS_COMPLETED -> {
                            val planDetails = PlanDetails(
                                planDetailJsonObj.getInt("id"),
                                planDetailJsonObj.getString("plan_detail"),
                                planDetailJsonObj.getString("price"),
                                planDetailJsonObj.getString("expired_at"),
                                planDetailJsonObj.getString("created_at"),
                                planDetailJsonObj.getString("updated_on"),
                                planDetailJsonObj.getInt("plan"),
                                planDetailJsonObj.getInt("user"),
                                planDetailJsonObj.getString("transaction"),
                                jsonObj.getString("description")
                            )
                            replaceFragmentNoBackStack(
                                InsuranceVerifyFragment.newInstance(
                                    planDetails,
                                    Utils.VOB_COMPLETED,
                                    null
                                ),
                                R.id.layout_home,
                                InsuranceVerifyFragment.TAG
                            )
                        }
                        Utils.VOB_STATUS_FAILED -> {
                            replaceFragmentNoBackStack(
                                InsuranceVerifyFragment.newInstance(null, Utils.VOB_FAILED, null),
                                R.id.layout_home,
                                InsuranceVerifyFragment.TAG
                            )
                        }
                    }
                } else {
                    replaceFragmentNoBackStack(
                        InsuranceVerifyFragment.newInstance(null, Utils.VOB_PENDING, null),
                        R.id.layout_home,
                        InsuranceVerifyFragment.TAG
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updateNotificationStatus(notificationId: Int) {
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .updateNotificationStatus(
                        "PI0061",
                        NotifyStatus(notificationId),
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
                        } catch (e: Exception) {
                            hideProgress()
                            e.printStackTrace()
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
         * @return A new instance of fragment SplashFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SplashFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_Splash"
    }
}
