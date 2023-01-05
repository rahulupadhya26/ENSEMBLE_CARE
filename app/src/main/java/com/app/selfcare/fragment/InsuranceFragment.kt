package com.app.selfcare.fragment

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.GridLayoutManager
import com.app.selfcare.R
import com.app.selfcare.adapters.PodcastListAdapter
import com.app.selfcare.data.InsuranceVerifyReqBody
import com.app.selfcare.data.Plan
import com.app.selfcare.data.Podcast
import com.app.selfcare.data.TransactionStatus
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper.get
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_insurance.*
import kotlinx.android.synthetic.main.fragment_podcast.*
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_registration.*
import retrofit2.HttpException
import java.lang.reflect.Type

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [InsuranceFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class InsuranceFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var plan: Plan? = null
    private var addOn: Int = 0
    private var selectedInsuranceName: String = ""
    private var insuranceNameData: Array<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            plan = it.getParcelable(ARG_PARAM1)
            addOn = it.getInt(ARG_PARAM2)
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_insurance
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE
        getSubTitle().text = ""
        updateStatusBarColor(R.color.initial_screen_background)

        if (addOn == 0) {
            txtInsurancePlanName.text =
                "Plan Name: " + plan!!.therapy.name + "($" + plan!!.price + "/month)"
        } else {
            txtInsurancePlanName.text =
                "Plan Name: " + plan!!.therapy.name + "($" + addOn + "/month)"
        }
        insuranceNameSpinner()

        imgInsuranceBack.setOnClickListener {
            popBackStack()
        }

        btnInsuranceDetails.setOnClickListener {
            if (selectedInsuranceName.isNotEmpty()) {
                if (getText(etPlanId).isNotEmpty()) {
                    if (getText(etMemberId).isNotEmpty()) {
                        if (getText(etGroupId).isNotEmpty()) {
                            if (getText(etMemberName).isNotEmpty()) {
                                //Call VerifyTx Api
                                verifyInsuranceApi()
                            } else {
                                setEditTextError(etMemberName, "Member name cannot be empty.")
                            }
                        } else {
                            setEditTextError(etGroupId, "Group Id cannot be empty.")
                        }
                    } else {
                        setEditTextError(etMemberId, "Member Id cannot be empty.")
                    }
                } else {
                    setEditTextError(etPlanId, "Plan Id cannot be empty.")
                }
            } else {
                displayMsg("Alert", "Select insurance name")
            }
        }

        imgInsurancePic.setOnClickListener {
            showImage(imgInsurancePic)
        }

        tvAddInsuranceCardPic.setOnClickListener {
            //captureImage(imgInsurancePic)
            captureImage(imgInsurancePic, "Insurance")
        }

        /*imgInsurancePicClear.setOnClickListener {
            imgInsurancePic.setImageDrawable(null)
            imgInsurancePic.setImageResource(R.drawable.health_insurance)
        }*/
    }

    private fun insuranceNameSpinner() {
        insuranceNameData = resources.getStringArray(R.array.insurance_name)
        val adapter = ArrayAdapter(
            requireActivity(),
            R.layout.spinner_dropdown_custom_item, insuranceNameData!!
        )
        /*val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
            requireActivity(), android.R.layout.simple_spinner_dropdown_item, insuranceNameData!!
        )*/
        spinnerInsuranceCompany.adapter = adapter
        spinnerInsuranceCompany.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View, position: Int, id: Long
            ) {
                selectedInsuranceName = insuranceNameData!![position]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // write code to perform some action
            }
        }
    }

    private fun verifyInsuranceApi() {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .insuranceVerifyApi(
                        InsuranceVerifyReqBody(
                            preference!![PrefKeys.PREF_PATIENT_ID, ""]!!.toInt(),
                            selectedInsuranceName,
                            getText(etPlanId),
                            getText(etMemberId),
                            getText(etGroupId),
                            getText(etMemberName),
                            "Primary"
                        ), getAccessToken()
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
                            if (status == "200") {
                                val transSts = TransactionStatus(
                                    "1",
                                    "56uyw-12edr-edry5-edtgy",
                                    preference!![PrefKeys.PREF_EMAIL, ""]!!,
                                    10,
                                    "dfghj",
                                    true,
                                    12
                                )
                                replaceFragment(
                                    TransactionStatusFragment.newInstance(transSts, true),
                                    R.id.layout_home,
                                    TransactionStatusFragment.TAG
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
                                verifyInsuranceApi()
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
         * @return A new instance of fragment InsuranceFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: Plan, param2: Int) =
            InsuranceFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM1, param1)
                    putInt(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_Payment_Insurance"
    }
}