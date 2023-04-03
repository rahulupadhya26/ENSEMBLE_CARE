package com.app.selfcare.fragment

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.app.selfcare.R
import com.app.selfcare.data.*
import com.app.selfcare.databinding.FragmentActivityCarePlanBinding
import com.app.selfcare.databinding.FragmentInsuranceBinding
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper.get
import com.app.selfcare.preference.PreferenceHelper.set
import com.app.selfcare.utils.Utils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
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
    private var selectedSecondaryInsuranceName: String = "Select Insurance Company"
    private var secondaryInsuranceNameData: Array<String>? = null
    private var selectedPharmacyInsuranceName: String = "Select Insurance Company"
    private var pharmacyInsuranceNameData: Array<String>? = null
    private var isPrimaryInsuranceAvailable: Boolean = false
    private var isSecondaryInsuranceAvailable: Boolean = false
    private var isPharmacyInsuranceAvailable: Boolean = false
    private var isPrimaryInsuranceVisible: Boolean = false
    private var isSecondaryInsuranceVisible: Boolean = false
    private var isPharmacyInsuranceVisible: Boolean = false
    private var isSecondaryInsuranceUserChange: Boolean = false
    private var isPharmacyInsuranceUserChange: Boolean = false
    private lateinit var binding: FragmentInsuranceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            plan = it.getParcelable(ARG_PARAM1)
            addOn = it.getParcelable(ARG_PARAM2)
            type = it.getString(ARG_PARAM3)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentInsuranceBinding.inflate(inflater, container, false)
        return binding.root
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

        if (plan != null) {
            binding.txtInsurancePlanName.visibility = View.VISIBLE
            if (addOn == null) {
                when (type) {
                    "Monthly" -> {
                        binding.txtInsurancePlanName.text =
                            "Plan Name: " + plan!!.name + "($" + plan!!.monthly_price + "/" + type!! + ")"
                    }
                    "Quarterly" -> {
                        binding.txtInsurancePlanName.text =
                            "Plan Name: " + plan!!.name + "($" + plan!!.quarterly_price + "/" + type!! + ")"
                    }
                    "Annually" -> {
                        binding.txtInsurancePlanName.text =
                            "Plan Name: " + plan!!.name + "($" + plan!!.annually_price + "/" + type!! + ")"
                    }
                }
            } else {
                when (type) {
                    "Monthly" -> {
                        binding.txtInsurancePlanName.text =
                            "Plan Name: " + plan!!.name + "+Addon ($" + (plan!!.monthly_price.toInt() + addOn!!.monthly_price.toInt()) + "/" + type!! + ")"
                    }
                    "Quarterly" -> {
                        binding.txtInsurancePlanName.text =
                            "Plan Name: " + plan!!.name + "+Addon ($" + (plan!!.quarterly_price.toInt() + addOn!!.monthly_price.toInt()) + "/" + type!! + ")"
                    }
                    "Annually" -> {
                        binding.txtInsurancePlanName.text =
                            "Plan Name: " + plan!!.name + "+Addon ($" + (plan!!.annually_price.toInt() + addOn!!.monthly_price.toInt()) + "/" + type!! + ")"
                    }
                }
            }
        } else {
            binding.txtInsurancePlanName.visibility = View.INVISIBLE
        }

        primaryInsuranceNameSpinner()
        secondaryInsuranceNameSpinner()
        pharmacyInsuranceNameSpinner()

        binding.imgInsuranceBack.setOnClickListener {
            popBackStack()
        }

        binding.btnInsuranceDetails.setOnClickListener {
            if (checkPrimaryInsuranceData()) {
                if (isSecondaryInsuranceUserChange || binding.spinnerSecondaryInsuranceCompany.text.toString()
                        .trim().isNotEmpty()
                ) {
                    if (checkSecondaryInsuranceData()) {
                        if (isPharmacyInsuranceUserChange || binding.spinnerPharmacyInsuranceCompany.text.toString()
                                .trim().isNotEmpty()
                        ) {
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
                } else if (isPharmacyInsuranceUserChange || binding.spinnerPharmacyInsuranceCompany.text.toString()
                        .trim().isNotEmpty()
                ) {
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

        binding.cardViewPrimaryInsurance1.setOnClickListener {
            captureImage(binding.imgInsurancePic1, "Insurance")
        }

        binding.cardViewPrimaryInsurance2.setOnClickListener {
            captureImage(binding.imgInsurancePic2, "Insurance")
        }

        binding.cardViewSecondaryInsurance1.setOnClickListener {
            captureImage(binding.imgSecondaryInsurancePic1, "Insurance")
        }

        binding.cardViewSecondaryInsurance2.setOnClickListener {
            captureImage(binding.imgSecondaryInsurancePic2, "Insurance")
        }

        binding.cardViewPharmacyInsurance1.setOnClickListener {
            captureImage(binding.imgPharmacyInsurancePic1, "Insurance")
        }

        binding.cardViewPharmacyInsurance2.setOnClickListener {
            captureImage(binding.imgPharmacyInsurancePic2, "Insurance")
        }


        /*imgPrimaryInsurancePic.setOnClickListener {
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

        *//*imgInsurancePicClear.setOnClickListener {
            imgInsurancePic.setImageDrawable(null)
            imgInsurancePic.setImageResource(R.drawable.health_insurance)
        }*/

        binding.layoutPrimaryInsurance.setOnClickListener {
            if (isPrimaryInsuranceVisible) {
                binding.layoutPrimaryInsuranceDetails.visibility = View.GONE
                binding.imgPrimaryDownIcon.rotation = -90f
            } else {
                binding.layoutPrimaryInsuranceDetails.visibility = View.VISIBLE
                binding.imgPrimaryDownIcon.rotation = 0.3f
            }
            isPrimaryInsuranceVisible = !isPrimaryInsuranceVisible
        }

        binding.layoutSecondaryInsurance.setOnClickListener {
            if (isSecondaryInsuranceVisible) {
                binding.layoutSecondaryInsuranceDetails.visibility = View.GONE
                binding.imgSecondaryDownIcon.rotation = -90f
            } else {
                binding.layoutSecondaryInsuranceDetails.visibility = View.VISIBLE
                binding.imgSecondaryDownIcon.rotation = 0.3f
            }
            isSecondaryInsuranceVisible = !isSecondaryInsuranceVisible
        }

        binding.layoutPharmacyInsurance.setOnClickListener {
            if (isPharmacyInsuranceVisible) {
                binding.layoutPharmacyInsuranceDetails.visibility = View.GONE
                binding.imgPharmacyDownIcon.rotation = -90f
            } else {
                binding.layoutPharmacyInsuranceDetails.visibility = View.VISIBLE
                binding.imgPharmacyDownIcon.rotation = 0.3f
            }
            isPharmacyInsuranceVisible = !isPharmacyInsuranceVisible
        }

        binding.etSecondaryPlanId.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val userChange = abs(count - before) == 1
                isSecondaryInsuranceUserChange = userChange
                if (s.toString().isEmpty()) {
                    isSecondaryInsuranceUserChange = false
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.etSecondaryMemberId.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val userChange = abs(count - before) == 1
                isSecondaryInsuranceUserChange = userChange
                if (s.toString().isEmpty()) {
                    isSecondaryInsuranceUserChange = false
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.etSecondaryGroupId.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val userChange = abs(count - before) == 1
                isSecondaryInsuranceUserChange = userChange
                if (s.toString().isEmpty()) {
                    isSecondaryInsuranceUserChange = false
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.etSecondaryMemberName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val userChange = abs(count - before) == 1
                isSecondaryInsuranceUserChange = userChange
                if (s.toString().isEmpty()) {
                    isSecondaryInsuranceUserChange = false
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.etPharmacyPlanId.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val userChange = abs(count - before) == 1
                isPharmacyInsuranceUserChange = userChange
                if (s.toString().isEmpty()) {
                    isPharmacyInsuranceUserChange = false
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.etPharmacyMemberId.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val userChange = abs(count - before) == 1
                isPharmacyInsuranceUserChange = userChange
                if (s.toString().isEmpty()) {
                    isPharmacyInsuranceUserChange = false
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.etPharmacyGroupId.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val userChange = abs(count - before) == 1
                isPharmacyInsuranceUserChange = userChange
                if (s.toString().isEmpty()) {
                    isPharmacyInsuranceUserChange = false
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.etPharmacyMemberName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val userChange = abs(count - before) == 1
                isPharmacyInsuranceUserChange = userChange
                if (s.toString().isEmpty()) {
                    isPharmacyInsuranceUserChange = false
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.layoutSecondaryInsuranceDetails.visibility = View.GONE
        binding.layoutPharmacyInsuranceDetails.visibility = View.GONE
        binding.imgSecondaryDownIcon.rotation = -90f
        binding.imgPharmacyDownIcon.rotation = -90f
    }

    private fun primaryInsuranceNameSpinner() {
        primaryInsuranceNameData = resources.getStringArray(R.array.insurance_name)
        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
            requireActivity(), R.layout.spinner_dropdown_custom_item, primaryInsuranceNameData!!
        )
        binding.spinnerPrimaryInsuranceCompany.setAdapter(adapter)
        binding.spinnerPrimaryInsuranceCompany.onItemClickListener =
            AdapterView.OnItemClickListener { parent, arg1, position, id ->
                //TODO: You can your own logic.
                selectedPrimaryInsuranceName = primaryInsuranceNameData!![position]
            }
        /*val adapter = ArrayAdapter(
            requireActivity(),
            R.layout.spinner_dropdown_custom_item, primaryInsuranceNameData!!
        )*/

        /*spinnerPrimaryInsuranceCompany.onItemSelectedListener = object :
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
        }*/
    }

    private fun secondaryInsuranceNameSpinner() {
        secondaryInsuranceNameData = resources.getStringArray(R.array.insurance_name)
        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
            requireActivity(), R.layout.spinner_dropdown_custom_item, secondaryInsuranceNameData!!
        )
        binding.spinnerSecondaryInsuranceCompany.setAdapter(adapter)
        binding.spinnerSecondaryInsuranceCompany.onItemClickListener =
            AdapterView.OnItemClickListener { parent, arg1, position, id ->
                //TODO: You can your own logic.
                selectedSecondaryInsuranceName = secondaryInsuranceNameData!![position]
            }
        /*val adapter = ArrayAdapter(
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
        }*/
    }

    private fun pharmacyInsuranceNameSpinner() {
        pharmacyInsuranceNameData = resources.getStringArray(R.array.insurance_name)
        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
            requireActivity(), R.layout.spinner_dropdown_custom_item, pharmacyInsuranceNameData!!
        )
        binding.spinnerPharmacyInsuranceCompany.setAdapter(adapter)
        binding.spinnerPharmacyInsuranceCompany.onItemClickListener =
            AdapterView.OnItemClickListener { parent, arg1, position, id ->
                //TODO: You can your own logic.
                selectedPharmacyInsuranceName = pharmacyInsuranceNameData!![position]
            }
        /*val adapter = ArrayAdapter(
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
        }*/
    }

    private fun checkPrimaryInsuranceData(): Boolean {
        isPrimaryInsuranceAvailable = false
        if (binding.spinnerPrimaryInsuranceCompany.text.toString().trim()
                .isNotEmpty() && binding.spinnerPrimaryInsuranceCompany.text.toString()
                .trim() != "Select Insurance Company"
        ) {
            if (getText(binding.etPrimaryPlanId).isNotEmpty()) {
                if (getText(binding.etPrimaryMemberId).isNotEmpty()) {
                    if (getText(binding.etPrimaryGroupId).isNotEmpty()) {
                        if (getText(binding.etPrimaryMemberName).isNotEmpty()) {
                            isPrimaryInsuranceAvailable = true
                        } else {
                            setEditTextError(
                                binding.etPrimaryMemberName,
                                "Member name cannot be empty."
                            )
                        }
                    } else {
                        setEditTextError(binding.etPrimaryGroupId, "Group Id cannot be empty.")
                    }
                } else {
                    setEditTextError(binding.etPrimaryMemberId, "Member Id cannot be empty.")
                }
            } else {
                setEditTextError(binding.etPrimaryPlanId, "Plan Id cannot be empty.")
            }
        } else {
            displayMsg("Alert", "Select primary insurance name")
        }
        return isPrimaryInsuranceAvailable
    }

    private fun checkSecondaryInsuranceData(): Boolean {
        isSecondaryInsuranceAvailable = false
        if (binding.spinnerSecondaryInsuranceCompany.text.toString().trim()
                .isNotEmpty() && binding.spinnerSecondaryInsuranceCompany.text.toString()
                .trim() != "Select Insurance Company"
        ) {
            if (getText(binding.etSecondaryPlanId).isNotEmpty()) {
                if (getText(binding.etSecondaryMemberId).isNotEmpty()) {
                    if (getText(binding.etSecondaryGroupId).isNotEmpty()) {
                        if (getText(binding.etSecondaryMemberName).isNotEmpty()) {
                            isSecondaryInsuranceAvailable = true
                        } else {
                            setEditTextError(
                                binding.etSecondaryMemberName,
                                "Member name cannot be empty."
                            )
                        }
                    } else {
                        setEditTextError(binding.etSecondaryGroupId, "Group Id cannot be empty.")
                    }
                } else {
                    setEditTextError(binding.etSecondaryMemberId, "Member Id cannot be empty.")
                }
            } else {
                setEditTextError(binding.etSecondaryPlanId, "Plan Id cannot be empty.")
            }
        } else {
            displayMsg("Alert", "Select secondary insurance name")
        }
        return isSecondaryInsuranceAvailable
    }

    private fun checkPharmacyInsuranceData(): Boolean {
        isPharmacyInsuranceAvailable = false
        if (binding.spinnerPharmacyInsuranceCompany.text.toString().trim()
                .isNotEmpty() && binding.spinnerPharmacyInsuranceCompany.text.toString()
                .trim() != "Select Insurance Company"
        ) {
            if (getText(binding.etPharmacyPlanId).isNotEmpty()) {
                if (getText(binding.etPharmacyMemberId).isNotEmpty()) {
                    if (getText(binding.etPharmacyGroupId).isNotEmpty()) {
                        if (getText(binding.etPharmacyMemberName).isNotEmpty()) {
                            isPharmacyInsuranceAvailable = true
                        } else {
                            setEditTextError(
                                binding.etPharmacyMemberName,
                                "Member name cannot be empty."
                            )
                        }
                    } else {
                        setEditTextError(binding.etPharmacyGroupId, "Group Id cannot be empty.")
                    }
                } else {
                    setEditTextError(binding.etPharmacyMemberId, "Member Id cannot be empty.")
                }
            } else {
                setEditTextError(binding.etPharmacyPlanId, "Plan Id cannot be empty.")
            }
        } else {
            displayMsg("Alert", "Select pharmacy insurance name")
        }
        return isPharmacyInsuranceAvailable
    }

    private fun verifyPrimaryInsuranceApi() {
        showProgress()
        val drawable: BitmapDrawable = binding.imgInsurancePic1.drawable as BitmapDrawable
        val bitmap: Bitmap = drawable.bitmap
        val drawable1: BitmapDrawable = binding.imgInsurancePic2.drawable as BitmapDrawable
        val bitmap1: Bitmap = drawable1.bitmap
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .insuranceVerifyApi(
                        InsuranceVerifyReqBody(
                            preference!![PrefKeys.PREF_PATIENT_ID, ""]!!.toInt(),
                            selectedPrimaryInsuranceName,
                            getText(binding.etPrimaryPlanId),
                            getText(binding.etPrimaryMemberId),
                            getText(binding.etPrimaryGroupId),
                            getText(binding.etPrimaryMemberName),
                            "Primary",
                            if (bitmap != null) "data:image/jpg;base64," + convert(bitmap) else "",
                            if (bitmap1 != null) "data:image/jpg;base64," + convert(bitmap1) else ""
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
        val drawable: BitmapDrawable = binding.imgSecondaryInsurancePic1.drawable as BitmapDrawable
        val bitmap: Bitmap = drawable.bitmap
        val drawable1: BitmapDrawable = binding.imgSecondaryInsurancePic2.drawable as BitmapDrawable
        val bitmap1: Bitmap = drawable1.bitmap
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .insuranceVerifyApi(
                        InsuranceVerifyReqBody(
                            preference!![PrefKeys.PREF_PATIENT_ID, ""]!!.toInt(),
                            selectedSecondaryInsuranceName,
                            getText(binding.etSecondaryPlanId),
                            getText(binding.etSecondaryMemberId),
                            getText(binding.etSecondaryGroupId),
                            getText(binding.etSecondaryMemberName),
                            "Secondary",
                            if (bitmap != null) "data:image/jpg;base64," + convert(bitmap) else "",
                            if (bitmap1 != null) "data:image/jpg;base64," + convert(bitmap1) else ""
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
        val drawable: BitmapDrawable = binding.imgPharmacyInsurancePic1.drawable as BitmapDrawable
        val bitmap: Bitmap = drawable.bitmap
        val drawable1: BitmapDrawable = binding.imgPharmacyInsurancePic2.drawable as BitmapDrawable
        val bitmap1: Bitmap = drawable1.bitmap
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .insuranceVerifyApi(
                        InsuranceVerifyReqBody(
                            preference!![PrefKeys.PREF_PATIENT_ID, ""]!!.toInt(),
                            selectedPharmacyInsuranceName,
                            getText(binding.etPharmacyPlanId),
                            getText(binding.etPharmacyMemberId),
                            getText(binding.etPharmacyGroupId),
                            getText(binding.etPharmacyMemberName),
                            "Pharmacy",
                            if (bitmap != null) "data:image/jpg;base64," + convert(bitmap) else "",
                            if (bitmap1 != null) "data:image/jpg;base64," + convert(bitmap1) else ""
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