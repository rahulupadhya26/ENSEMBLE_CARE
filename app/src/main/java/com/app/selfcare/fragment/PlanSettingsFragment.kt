package com.app.selfcare.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.app.selfcare.R
import com.app.selfcare.data.PatientId
import com.app.selfcare.data.PlanSettingsData
import com.app.selfcare.data.Therapist
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper.get
import com.app.selfcare.utils.DateUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_plan.*
import kotlinx.android.synthetic.main.fragment_plan_settings.*
import retrofit2.HttpException
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PlanSettingsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PlanSettingsFragment : BaseFragment() {
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
        return R.layout.fragment_plan_settings
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE
        updateStatusBarColor(R.color.white)

        onClickEvents()

        getPlanSettingsData()
    }

    private fun onClickEvents() {
        planSettingsBack.setOnClickListener {
            popBackStack()
        }

        layoutPlanUpgrade.setOnClickListener {

        }

        layoutChangePaymentMethod.setOnClickListener {

        }

        layoutInsurancePay.setOnClickListener {

        }

        layoutEmployeeAssistedProgram.setOnClickListener {

        }
    }

    @SuppressLint("SetTextI18n")
    private fun getPlanSettingsData() {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .getPlanSettingsData(getAccessToken())
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
                            val planSettingsType: Type =
                                object : TypeToken<PlanSettingsData?>() {}.type
                            val planSettingsObj: PlanSettingsData =
                                Gson().fromJson(responseBody, planSettingsType)

                            when (planSettingsObj.current_subscription.plan_detail) {
                                "Plus+Addon" -> {
                                    planSettingsAddon.visibility = View.VISIBLE
                                    txtPlanSettingPlanPrice.text =
                                        "$" + (planSettingsObj.current_subscription.price - 20) + ".00"
                                    txtPlanSettingAddonPrice.text = "$20.00"
                                }
                                "Premium+Addon" -> {
                                    planSettingsAddon.visibility = View.VISIBLE
                                    txtPlanSettingPlanPrice.text =
                                        "$" + (planSettingsObj.current_subscription.price - 20) + ".00"
                                    txtPlanSettingAddonPrice.text = "$20.00"
                                }
                                "Plus" -> {
                                    planSettingsAddon.visibility = View.GONE
                                    txtPlanSettingPlanPrice.text =
                                        "$" + (planSettingsObj.current_subscription.price - 20) + ".00"
                                }
                                "Premium" -> {
                                    planSettingsAddon.visibility = View.GONE
                                    txtPlanSettingPlanPrice.text =
                                        "$" + (planSettingsObj.current_subscription.price - 20) + ".00"
                                }
                            }

                            val date: Calendar = Calendar.getInstance()
                            date.add(Calendar.MONTH, 1)
                            val nextMonthDate = SimpleDateFormat("MM/dd/yyyy").format(date.time)
                            val nextMonthDateFormat = DateUtils("$nextMonthDate 00:00:00")

                            /*txtAutoRenewPlanSettings.text =
                                "Your current pack will auto renew on " + nextMonthDateFormat.getDay() +
                                        nextMonthDateFormat.getDayNumberSuffix(
                                            nextMonthDateFormat.getDay().toInt()
                                        ) + " " + nextMonthDateFormat.getFullMonthName() + ", " +
                                        nextMonthDateFormat.getYear()*/
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
                                getPlanSettingsData()
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
         * @return A new instance of fragment PlanSettingsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PlanSettingsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_Plan_Settings"
    }
}