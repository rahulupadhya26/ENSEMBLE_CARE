package com.app.selfcare.fragment

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.app.selfcare.R
import com.app.selfcare.data.*
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper.get
import com.app.selfcare.preference.PreferenceHelper.set
import com.app.selfcare.utils.Utils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_insurance.*
import retrofit2.HttpException
import java.lang.reflect.Type
import kotlin.math.abs

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val ARG_PARAM3 = "param3"

/**
 * A simple [Fragment] subclass.
 * Use the [InsuranceFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class InsuranceFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var plan: Plan? = null
    private var addOn: AddOn? = null
    private var type: String? = ""
    private var selectedPrimaryInsuranceName: String = ""
    private var primaryInsuranceNameData: Array<String>? = null
    private var selectedSecondaryInsuranceName: String = ""
    private var secondaryInsuranceNameData: Array<String>? = null
    private var selectedPharmacyInsuranceName: String = ""
    private var pharmacyInsuranceNameData: Array<String>? = null
    private var isPrimaryInsuranceAvailable: Boolean = false
    private var isSecondaryInsuranceAvailable: Boolean = false
    private var isPharmacyInsuranceAvailable: Boolean = false
    private var isPrimaryInsuranceVisible: Boolean = false
    private var isSecondaryInsuranceVisible: Boolean = false
    private var isPharmacyInsuranceVisible: Boolean = false
    private var isSecondaryInsuranceUserChange: Boolean = false
    private var isPharmacyInsuranceUserChange: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            plan = it.getParcelable(ARG_PARAM1)
            addOn = it.getParcelable(ARG_PARAM2)
            type = it.getString(ARG_PARAM3)
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

        isPrimaryInsuranceAvailable = false
        isSecondaryInsuranceAvailable = false
        isPharmacyInsuranceAvailable = false
        isPrimaryInsuranceVisible = true
        isSecondaryInsuranceVisible = false
        isPharmacyInsuranceVisible = false
        isSecondaryInsuranceUserChange = false
        isPharmacyInsuranceUserChange = false

        layoutSecondaryInsuranceDetails.visibility = View.GONE
        layoutPharmacyInsuranceDetails.visibility = View.GONE

        if (plan != null) {
            txtInsurancePlanName.visibility = View.VISIBLE
            if (addOn == null) {
                when (type) {
                    "Monthly" -> {
                        txtInsurancePlanName.text =
                            "Plan Name: " + plan!!.name + "($" + plan!!.monthly_price + "/" + type!! + ")"
                    }
                    "Quarterly" -> {
                        txtInsurancePlanName.text =
                            "Plan Name: " + plan!!.name + "($" + plan!!.quarterly_price + "/" + type!! + ")"
                    }
                    "Annually" -> {
                        txtInsurancePlanName.text =
                            "Plan Name: " + plan!!.name + "($" + plan!!.annually_price + "/" + type!! + ")"
                    }
                }
            } else {
                when (type) {
                    "Monthly" -> {
                        txtInsurancePlanName.text =
                            "Plan Name: " + plan!!.name + "+Addon ($" + (plan!!.monthly_price.toInt() + addOn!!.monthly_price.toInt()) + "/" + type!! + ")"
                    }
                    "Quarterly" -> {
                        txtInsurancePlanName.text =
                            "Plan Name: " + plan!!.name + "+Addon ($" + (plan!!.quarterly_price.toInt() + addOn!!.monthly_price.toInt()) + "/" + type!! + ")"
                    }
                    "Annually" -> {
                        txtInsurancePlanName.text =
                            "Plan Name: " + plan!!.name + "+Addon ($" + (plan!!.annually_price.toInt() + addOn!!.monthly_price.toInt()) + "/" + type!! + ")"
                    }
                }
            }
        } else {
            txtInsurancePlanName.visibility = View.INVISIBLE
        }

        primaryInsuranceNameSpinner()
        secondaryInsuranceNameSpinner()
        pharmacyInsuranceNameSpinner()

        imgInsuranceBack.setOnClickListener {
            popBackStack()
        }

        btnInsuranceDetails.setOnClickListener {
            if (checkPrimaryInsuranceData()) {
                if (isSecondaryInsuranceUserChange || selectedSecondaryInsuranceName != "Select Insurance Company") {
                    if (checkSecondaryInsuranceData()) {
                        if (isPharmacyInsuranceUserChange || selectedPharmacyInsuranceName != "Select Insurance Company") {
                            if (checkPharmacyInsuranceData()) {
                                verifyPrimaryInsuranceApi()
                                verifySecondaryInsuranceApi()
                                verifyPharmacyInsuranceApi()
                            } else {
                                displayMsg(
                                    "Alert",
                                    "Please fill the necessary details"
                                )
                            }
                        } else {
                            verifyPrimaryInsuranceApi()
                            verifySecondaryInsuranceApi()
                        }
                    } else {
                        displayMsg(
                            "Alert",
                            "Please fill the necessary details"
                        )
                    }
                } else if (isPharmacyInsuranceUserChange || selectedPharmacyInsuranceName != "Select Insurance Company") {
                    if (checkPharmacyInsuranceData()) {
                        verifyPrimaryInsuranceApi()
                        verifyPharmacyInsuranceApi()
                    } else {
                        displayMsg(
                            "Alert",
                            "Please fill the necessary details"
                        )
                    }
                } else {
                    verifyPrimaryInsuranceApi()
                }
            }
        }

        imgPrimaryInsurancePic.setOnClickListener {
            showImage(imgPrimaryInsurancePic)
        }

        tvAddPrimaryInsuranceCardPic.setOnClickListener {
            //captureImage(imgInsurancePic)
            captureImage(imgPrimaryInsurancePic, "Insurance")
        }

        imgSecondaryInsurancePic.setOnClickListener {
            showImage(imgSecondaryInsurancePic)
        }

        tvAddSecondaryInsuranceCardPic.setOnClickListener {
            //captureImage(imgInsurancePic)
            captureImage(imgSecondaryInsurancePic, "Insurance")
        }

        imgPharmacyInsurancePic.setOnClickListener {
            showImage(imgPharmacyInsurancePic)
        }

        tvAddPharmacyInsuranceCardPic.setOnClickListener {
            //captureImage(imgInsurancePic)
            captureImage(imgPharmacyInsurancePic, "Insurance")
        }

        /*imgInsurancePicClear.setOnClickListener {
            imgInsurancePic.setImageDrawable(null)
            imgInsurancePic.setImageResource(R.drawable.health_insurance)
        }*/

        layoutPrimaryInsurance.setOnClickListener {
            if (isPrimaryInsuranceVisible) {
                layoutPrimaryInsuranceDetails.visibility = View.GONE
                imgPrimaryDownIcon.rotation = -90f
            } else {
                layoutPrimaryInsuranceDetails.visibility = View.VISIBLE
                imgPrimaryDownIcon.rotation = 0.3f
            }
            isPrimaryInsuranceVisible = !isPrimaryInsuranceVisible
        }

        layoutSecondaryInsurance.setOnClickListener {
            if (isSecondaryInsuranceVisible) {
                layoutSecondaryInsuranceDetails.visibility = View.GONE
                imgSecondaryDownIcon.rotation = -90f
            } else {
                layoutSecondaryInsuranceDetails.visibility = View.VISIBLE
                imgSecondaryDownIcon.rotation = 0.3f
            }
            isSecondaryInsuranceVisible = !isSecondaryInsuranceVisible
        }

        layoutPharmacyInsurance.setOnClickListener {
            if (isPharmacyInsuranceVisible) {
                layoutPharmacyInsuranceDetails.visibility = View.GONE
                imgPharmacyDownIcon.rotation = -90f
            } else {
                layoutPharmacyInsuranceDetails.visibility = View.VISIBLE
                imgPharmacyDownIcon.rotation = 0.3f
            }
            isPharmacyInsuranceVisible = !isPharmacyInsuranceVisible
        }

        etSecondaryPlanId.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val userChange = abs(count - before) == 1
                isSecondaryInsuranceUserChange = userChange
                if(s.toString().isEmpty()){
                    isSecondaryInsuranceUserChange = false
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        etSecondaryMemberId.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val userChange = abs(count - before) == 1
                isSecondaryInsuranceUserChange = userChange
                if(s.toString().isEmpty()){
                    isSecondaryInsuranceUserChange = false
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        etSecondaryGroupId.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val userChange = abs(count - before) == 1
                isSecondaryInsuranceUserChange = userChange
                if(s.toString().isEmpty()){
                    isSecondaryInsuranceUserChange = false
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        etSecondaryMemberName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val userChange = abs(count - before) == 1
                isSecondaryInsuranceUserChange = userChange
                if(s.toString().isEmpty()){
                    isSecondaryInsuranceUserChange = false
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        etPharmacyPlanId.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val userChange = abs(count - before) == 1
                isPharmacyInsuranceUserChange = userChange
                if(s.toString().isEmpty()){
                    isPharmacyInsuranceUserChange = false
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        etPharmacyMemberId.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val userChange = abs(count - before) == 1
                isPharmacyInsuranceUserChange = userChange
                if(s.toString().isEmpty()){
                    isPharmacyInsuranceUserChange = false
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        etPharmacyGroupId.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val userChange = abs(count - before) == 1
                isPharmacyInsuranceUserChange = userChange
                if(s.toString().isEmpty()){
                    isPharmacyInsuranceUserChange = false
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        etPharmacyMemberName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val userChange = abs(count - before) == 1
                isPharmacyInsuranceUserChange = userChange
                if(s.toString().isEmpty()){
                    isPharmacyInsuranceUserChange = false
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun primaryInsuranceNameSpinner() {
        primaryInsuranceNameData = resources.getStringArray(R.array.insurance_name)
        val adapter = ArrayAdapter(
            requireActivity(),
            R.layout.spinner_dropdown_custom_item, primaryInsuranceNameData!!
        )
        spinnerPrimaryInsuranceCompany.adapter = adapter
        spinnerPrimaryInsuranceCompany.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View, position: Int, id: Long
            ) {
                selectedPrimaryInsuranceName = primaryInsuranceNameData!![position]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // write code to perform some action
            }
        }
    }

    private fun secondaryInsuranceNameSpinner() {
        secondaryInsuranceNameData = resources.getStringArray(R.array.insurance_name)
        val adapter = ArrayAdapter(
            requireActivity(),
            R.layout.spinner_dropdown_custom_item, secondaryInsuranceNameData!!
        )
        spinnerSecondaryInsuranceCompany.adapter = adapter
        spinnerSecondaryInsuranceCompany.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View, position: Int, id: Long
            ) {
                selectedSecondaryInsuranceName = secondaryInsuranceNameData!![position]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // write code to perform some action
            }
        }
    }

    private fun pharmacyInsuranceNameSpinner() {
        pharmacyInsuranceNameData = resources.getStringArray(R.array.insurance_name)
        val adapter = ArrayAdapter(
            requireActivity(),
            R.layout.spinner_dropdown_custom_item, pharmacyInsuranceNameData!!
        )
        spinnerPharmacyInsuranceCompany.adapter = adapter
        spinnerPharmacyInsuranceCompany.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View, position: Int, id: Long
            ) {
                selectedPharmacyInsuranceName = pharmacyInsuranceNameData!![position]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // write code to perform some action
            }
        }
    }

    private fun checkPrimaryInsuranceData(): Boolean {
        isPrimaryInsuranceAvailable = false
        if (selectedPrimaryInsuranceName != "Select Insurance Company") {
            if (getText(etPrimaryPlanId).isNotEmpty()) {
                if (getText(etPrimaryMemberId).isNotEmpty()) {
                    if (getText(etPrimaryGroupId).isNotEmpty()) {
                        if (getText(etPrimaryMemberName).isNotEmpty()) {
                            isPrimaryInsuranceAvailable = true
                        } else {
                            setEditTextError(
                                etPrimaryMemberName,
                                "Member name cannot be empty."
                            )
                        }
                    } else {
                        setEditTextError(etPrimaryGroupId, "Group Id cannot be empty.")
                    }
                } else {
                    setEditTextError(etPrimaryMemberId, "Member Id cannot be empty.")
                }
            } else {
                setEditTextError(etPrimaryPlanId, "Plan Id cannot be empty.")
            }
        } else {
            displayMsg("Alert", "Select primary insurance name")
        }
        return isPrimaryInsuranceAvailable
    }

    private fun checkSecondaryInsuranceData(): Boolean {
        isSecondaryInsuranceAvailable = false
        if (selectedSecondaryInsuranceName != "Select Insurance Company") {
            if (getText(etSecondaryPlanId).isNotEmpty()) {
                if (getText(etSecondaryMemberId).isNotEmpty()) {
                    if (getText(etSecondaryGroupId).isNotEmpty()) {
                        if (getText(etSecondaryMemberName).isNotEmpty()) {
                            isSecondaryInsuranceAvailable = true
                        } else {
                            setEditTextError(
                                etSecondaryMemberName,
                                "Member name cannot be empty."
                            )
                        }
                    } else {
                        setEditTextError(etSecondaryGroupId, "Group Id cannot be empty.")
                    }
                } else {
                    setEditTextError(etSecondaryMemberId, "Member Id cannot be empty.")
                }
            } else {
                setEditTextError(etSecondaryPlanId, "Plan Id cannot be empty.")
            }
        } else {
            displayMsg("Alert", "Select secondary insurance name")
        }
        return isSecondaryInsuranceAvailable
    }

    private fun checkPharmacyInsuranceData(): Boolean {
        isPharmacyInsuranceAvailable = false
        if (selectedPharmacyInsuranceName != "Select Insurance Company") {
            if (getText(etPharmacyPlanId).isNotEmpty()) {
                if (getText(etPharmacyMemberId).isNotEmpty()) {
                    if (getText(etPharmacyGroupId).isNotEmpty()) {
                        if (getText(etPharmacyMemberName).isNotEmpty()) {
                            isPharmacyInsuranceAvailable = true
                        } else {
                            setEditTextError(
                                etPharmacyMemberName,
                                "Member name cannot be empty."
                            )
                        }
                    } else {
                        setEditTextError(etPharmacyGroupId, "Group Id cannot be empty.")
                    }
                } else {
                    setEditTextError(etPharmacyMemberId, "Member Id cannot be empty.")
                }
            } else {
                setEditTextError(etPharmacyPlanId, "Plan Id cannot be empty.")
            }
        } else {
            displayMsg("Alert", "Select pharmacy insurance name")
        }
        return isSecondaryInsuranceAvailable
    }

    private fun verifyPrimaryInsuranceApi() {
        showProgress()
        val drawable: BitmapDrawable = imgPrimaryInsurancePic.drawable as BitmapDrawable
        val bitmap: Bitmap = drawable.bitmap
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .insuranceVerifyApi(
                        InsuranceVerifyReqBody(
                            preference!![PrefKeys.PREF_PATIENT_ID, ""]!!.toInt(),
                            selectedPrimaryInsuranceName,
                            getText(etPrimaryPlanId),
                            getText(etPrimaryMemberId),
                            getText(etPrimaryGroupId),
                            getText(etPrimaryMemberName),
                            "Primary",
                            if (bitmap != null) "data:image/jpg;base64," + convert(bitmap) else ""
                        ),
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
                            if (status == "200") {
                                val insuranceVerifiedType: Type =
                                    object : TypeToken<InsuranceVerifiedDetails?>() {}.type
                                val insuranceVerified: InsuranceVerifiedDetails =
                                    Gson().fromJson(responseBody, insuranceVerifiedType)
                                when (insuranceVerified.vob_verified) {
                                    Utils.VOB_STATUS_PENDING -> {
                                        preference!![PrefKeys.PREF_INSURANCE_VERIFICATION] = true
                                        replaceFragmentNoBackStack(
                                            InsuranceVerifyFragment.newInstance(
                                                null,
                                                Utils.VOB_PENDING, type
                                            ),
                                            R.id.layout_home,
                                            TransactionStatusFragment.TAG
                                        )
                                    }
                                    Utils.VOB_STATUS_COMPLETED -> {
                                        preference!![PrefKeys.PREF_INSURANCE_VERIFICATION] = false
                                        replaceFragmentNoBackStack(
                                            InsuranceVerifyFragment.newInstance(
                                                null,
                                                Utils.VOB_COMPLETED, type
                                            ),
                                            R.id.layout_home,
                                            TransactionStatusFragment.TAG
                                        )
                                    }
                                    Utils.VOB_STATUS_FAILED -> {
                                        preference!![PrefKeys.PREF_INSURANCE_VERIFICATION] = false
                                        replaceFragmentNoBackStack(
                                            InsuranceVerifyFragment.newInstance(
                                                null,
                                                Utils.VOB_FAILED, type
                                            ),
                                            R.id.layout_home,
                                            TransactionStatusFragment.TAG
                                        )
                                    }
                                }
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
                                verifyPrimaryInsuranceApi()
                            }
                        } else {
                            displayAfterLoginErrorMsg(error)
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    private fun verifySecondaryInsuranceApi() {
        showProgress()
        val drawable: BitmapDrawable = imgSecondaryInsurancePic.drawable as BitmapDrawable
        val bitmap: Bitmap = drawable.bitmap
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .insuranceVerifyApi(
                        InsuranceVerifyReqBody(
                            preference!![PrefKeys.PREF_PATIENT_ID, ""]!!.toInt(),
                            selectedSecondaryInsuranceName,
                            getText(etSecondaryPlanId),
                            getText(etSecondaryMemberId),
                            getText(etSecondaryGroupId),
                            getText(etSecondaryMemberName),
                            "Secondary",
                            if (bitmap != null) "data:image/jpg;base64," + convert(bitmap) else ""
                        ),
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
                                verifySecondaryInsuranceApi()
                            }
                        } else {
                            displayAfterLoginErrorMsg(error)
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    private fun verifyPharmacyInsuranceApi() {
        showProgress()
        val drawable: BitmapDrawable = imgPharmacyInsurancePic.drawable as BitmapDrawable
        val bitmap: Bitmap = drawable.bitmap
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .insuranceVerifyApi(
                        InsuranceVerifyReqBody(
                            preference!![PrefKeys.PREF_PATIENT_ID, ""]!!.toInt(),
                            selectedPharmacyInsuranceName,
                            getText(etPharmacyPlanId),
                            getText(etPharmacyMemberId),
                            getText(etPharmacyGroupId),
                            getText(etPharmacyMemberName),
                            "Pharmacy",
                            if (bitmap != null) "data:image/jpg;base64," + convert(bitmap) else ""
                        ),
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
                                verifyPharmacyInsuranceApi()
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
        fun newInstance(param1: Plan?, param2: AddOn?, param3: String) =
            InsuranceFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM1, param1)
                    putParcelable(ARG_PARAM2, param2)
                    putString(ARG_PARAM3, param3)
                }
            }

        const val TAG = "Screen_Payment_Insurance"
    }
}