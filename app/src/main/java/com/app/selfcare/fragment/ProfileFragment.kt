package com.app.selfcare.fragment

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.telephony.PhoneNumberFormattingTextWatcher
import android.telephony.PhoneNumberUtils
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.app.selfcare.BaseActivity
import com.app.selfcare.R
import com.app.selfcare.data.PartProfileData
import com.app.selfcare.data.PatientId
import com.app.selfcare.data.ProfileData
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper.get
import com.app.selfcare.preference.PreferenceHelper.set
import com.app.selfcare.utils.Utils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.dialog_picture_option.view.*
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_therapy_basic_details_c.*
import org.json.JSONObject
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
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var selectedGender: String = "Prefer not to say"
    private var genderData: Array<String>? = null
    private var selectedMartialStatus: String = "Single"
    private var martialStatusData: Array<String>? = null
    private var selectedRelationship: String = ""
    private var relationshipsData: Array<String>? = null
    private var selectedPreferredLang: String = "English"
    private var preferredLanguageData: Array<String>? = null
    private var createPictureDialog: BottomSheetDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_profile
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE

        profileDetailBack.setOnClickListener {
            popBackStack()
        }

        getProfileDetails { result ->
            val profileDataType: Type = object : TypeToken<ArrayList<ProfileData?>?>() {}.type
            val profileDataList: ArrayList<ProfileData> = Gson().fromJson(result, profileDataType)
            val profileData: ProfileData = profileDataList[0]
            bindProfileData(profileData)
        }

        setDobCalender()

        etProfilePhoneNo.addTextChangedListener(object :
            PhoneNumberFormattingTextWatcher("US") {
            private var mFormatting = false
            private var mAfter = 0

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                super.beforeTextChanged(s, start, count, after)
                mAfter = after
            }

            override fun afterTextChanged(s: Editable?) {
                super.afterTextChanged(s)
                if (!mFormatting) {
                    mFormatting = true
                    // using US formatting.
                    if (mAfter != 0) // in case back space ain't clicked.
                        PhoneNumberUtils.formatNumber(
                            s, PhoneNumberUtils.getFormatTypeForLocale(
                                Locale.US
                            )
                        )
                    mFormatting = false
                }
            }
        })

        etProfileEmerConPhno.addTextChangedListener(object :
            PhoneNumberFormattingTextWatcher("US") {
            private var mFormatting = false
            private var mAfter = 0

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                super.beforeTextChanged(s, start, count, after)
                mAfter = after
            }

            override fun afterTextChanged(s: Editable?) {
                super.afterTextChanged(s)
                if (!mFormatting) {
                    mFormatting = true
                    // using US formatting.
                    if (mAfter != 0) // in case back space ain't clicked.
                        PhoneNumberUtils.formatNumber(
                            s, PhoneNumberUtils.getFormatTypeForLocale(
                                Locale.US
                            )
                        )
                    mFormatting = false
                }
            }
        })

        tvAddProfilePic.setOnClickListener {
            captureImage(imgProfilePhoto, "Profile")
        }

        imgProfilePhoto.setOnClickListener {
            showImage(imgProfilePhoto)
        }

        verifyProfileDetails()
    }

    private fun bindProfileData(profileData: ProfileData) {
        etProfileFname.setText(if (profileData.first_name == "null") "" else profileData.first_name)
        etProfileMname.setText(if (profileData.middle_name == "null") "" else profileData.middle_name)
        etProfileLname.setText(if (profileData.last_name == "null") "" else profileData.last_name)
        //etProfileSSN.setText(if (profileData.ssn == null) "" else profileData.ssn)
        txtProfileDob.setText(if (profileData.dob == "null") "" else profileData.dob)
        etProfileMailId.setText(preference!![PrefKeys.PREF_EMAIL, ""]!!)
        val phoneNo =
            preference!![PrefKeys.PREF_PHONE_NO, ""]!!.replace("(", "").replace(")", "")
                .replace(" ", "").replace("-", "")
        etProfilePhoneNo.setText(formatNumbersAsCode(phoneNo))
        etProfileEmerConPhno.setText(if (profileData.emergency_phone == "null") "" else profileData.emergency_phone)
        etProfileAddress.setText(if (profileData.address == "null") "" else profileData.address)
        etProfileAddress2.setText(if (profileData.address == "null") "" else profileData.address1)
        etProfileStreet.setText(if (profileData.street == "null") "" else profileData.street)
        etProfileCity.setText(if (profileData.city == "null") "" else profileData.city)
        etProfileState.setText(if (profileData.state == "null") "" else profileData.state)
        etProfileCountry.setText(if (profileData.country == "null") "" else profileData.country)
        etProfileZipcode.setText(if (profileData.zipcode == "null") "" else profileData.zipcode)
        preference!![PrefKeys.PREF_MARTIAL_STATUS] = profileData.marital_status
        preference!![PrefKeys.PREF_GENDER] = profileData.gender
        preference!![PrefKeys.PREF_PREFERRED_LANG] = profileData.preffered_language
        preference!![PrefKeys.PREF_RELATIONSHIP] = profileData.relation_to
        preference!![PrefKeys.PREF_PHOTO] = profileData.photo
        selectedGender = profileData.gender
        selectedRelationship = profileData.relation_to
        selectedPreferredLang = profileData.preffered_language
        selectedMartialStatus = profileData.marital_status
        val photo = preference!![PrefKeys.PREF_PHOTO, ""]!!
        if (photo.isNotEmpty()) {
            Glide.with(requireActivity())
                .load(BaseActivity.baseURL.dropLast(5) + photo)
                .placeholder(R.drawable.user_pic)
                .transform(CenterCrop(), RoundedCorners(5))
                .into(imgProfilePhoto)
        }
        preference!![PrefKeys.PREF_SEVERITY_SCORE] = profileData.patient_severity_score
        preference!![PrefKeys.PREF_SELECTED_SERVICE] = profileData.selected_service
        preference!![PrefKeys.PREF_SELECTED_DISORDER] = profileData.selected_dissorder
        //martialStatus()
        genderSpinner()
        preferredLanguageSpinner()
        relationshipsSpinner()
    }

    private fun getProfileDetails(myCallback: (result: String?) -> Unit) {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .getRequiredData(
                        "PI0002",
                        PatientId(preference!![PrefKeys.PREF_PATIENT_ID]!!),
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
                            myCallback.invoke(responseBody)
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
                                getProfileDetails { result ->
                                    val profileDataType: Type =
                                        object : TypeToken<ArrayList<ProfileData?>?>() {}.type
                                    val profileDataList: ArrayList<ProfileData> =
                                        Gson().fromJson(result, profileDataType)
                                    val profileData: ProfileData = profileDataList[0]
                                    bindProfileData(profileData)
                                }
                            }
                        } else {
                            displayAfterLoginErrorMsg(error)
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    private fun verifyProfileDetails() {
        btnProfileUpdate.setOnClickListener {
            if (getText(etProfileFname).isNotEmpty()) {
                //if (getText(etProfileMname).isNotEmpty()) {
                if (getText(etProfileLname).isNotEmpty()) {
                    if (getText(spinnerProfileGender).isNotEmpty()) {
                        //if (getText(etProfileCity).isNotEmpty()) {
                        //if (getText(etProfileState).isNotEmpty()) {
                        //if (getText(etProfileAddress).isNotEmpty()) {
                        //if (getText(etProfileZipcode).isNotEmpty()) {
                        if (txtProfileDob.text.toString().isNotEmpty()) {
                            when (preference!![PrefKeys.PREF_SELECTED_THERAPY, ""]!!) {
                                "Teen" -> {
                                    if (getAge(txtProfileDob.text.toString()) in 13..17) {
                                        updateProfileData()
                                    } else {
                                        displayMsg(
                                            "Alert",
                                            "Age must be greater than 12 years and less than 18 years."
                                        )
                                    }
                                }
                                else -> {
                                    if (getAge(txtProfileDob.text.toString()) > 18) {
                                        updateProfileData()
                                    } else {
                                        displayMsg(
                                            "Alert",
                                            "Age must be more than 18 years."
                                        )
                                    }
                                }
                            }
                        } else {
                            displayMsg("Alert", "Date of birth cannot be blank")
                        }
                        /*} else {
                            setEditTextError(
                                etProfileZipcode,
                                "ZipCode cannot be empty!"
                            )
                        }*/
                        /*} else {
                            setEditTextError(
                                etProfileAddress,
                                "Address cannot be empty!"
                            )
                        }*/
                        /*} else {
                            setEditTextError(
                                etProfileState,
                                "State cannot be empty!"
                            )
                        }*/
                        /*} else {
                            setEditTextError(etProfileCity, "City cannot be empty!")
                        }*/
                    } else {
                        displayMsg("Alert", "Select the gender")
                    }
                } else {
                    setEditTextError(etProfileLname, "Last name cannot be empty!")
                }
                /*} else {
                    setEditTextError(etProfileMname, "Middle name cannot be empty!")
                }*/
            } else {
                setEditTextError(etProfileFname, "First name cannot be empty!")
            }
        }
    }

    private fun updateProfileData() {
        preference!![PrefKeys.PREF_EMERGENCY_CONTACT_NAME] = getText(etProfileEmerConName)
        val photo =
            "data:image/jpg;base64," + Utils.convert(Utils.convertImageToBitmap(imgProfilePhoto))!!
        val profileData = PartProfileData(
            preference!![PrefKeys.PREF_PATIENT_ID, ""]!!.toInt(),
            getText(etProfileFname),
            getText(etProfileMname),
            getText(etProfileLname),
            getText(txtProfileDob),
            photo,
            getText(etProfileEmerConPhno),
            getText(etProfileCity),
            getText(etProfileState),
            getText(etProfileAddress),
            selectedMartialStatus,
            getText(etProfileZipcode),
            selectedRelationship,
            getText(etProfileAddress2),
            getText(etProfileCountry),
            selectedGender,
            selectedPreferredLang
        )
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .updateProfileData("PI0044", profileData, getAccessToken())
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
                            val jsonObject = JSONObject(responseBody)
                            preference!![PrefKeys.PREF_PHOTO] = jsonObject.getString("photo")
                            popBackStack()
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
                                updateProfileData()
                            }
                        } else {
                            displayAfterLoginErrorMsg(error)
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    private fun formatNumbersAsCode(s: CharSequence): String? {
        return String.format("%s-%s-%s", s.substring(0, 3), s.substring(3, 6), s.substring(6))
    }

    private fun maskSSNNumber(ssnNumber: String, mask: String): String {
        // format the number
        var index = 0
        val maskedNumber = StringBuilder()
        for (element in mask) {
            when (element) {
                '#' -> {
                    maskedNumber.append(ssnNumber[index])
                    index++
                }
                'X' -> {
                    maskedNumber.append(element)
                    index++
                }
                else -> {
                    maskedNumber.append(element)
                }
            }
        }
        // return the masked number
        return maskedNumber.toString()
    }

    private fun genderSpinner() {
        if (preference!![PrefKeys.PREF_GENDER, ""]!!.isNotEmpty()) {
            Handler().postDelayed({
                if (spinnerProfileGender != null)
                    spinnerProfileGender.setText(preference!![PrefKeys.PREF_GENDER, ""]!!, false)
            }, 300)
        }
        genderData = resources.getStringArray(R.array.gender)
        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
            requireActivity(), R.layout.spinner_dropdown_custom_item, genderData!!
        )
        spinnerProfileGender.setAdapter(adapter)
        spinnerProfileGender.onItemClickListener =
            AdapterView.OnItemClickListener { parent, arg1, position, id ->
                //TODO: You can your own logic.
                selectedGender = genderData!![position]
            }

        spinnerProfileGender.setOnClickListener {
            spinnerProfileGender.showDropDown()
        }
    }

    private fun preferredLanguageSpinner() {
        if (preference!![PrefKeys.PREF_PREFERRED_LANG, ""]!!.isNotEmpty()) {
            Handler().postDelayed({
                if (spinnerProfilePreferredLanguage != null)
                    spinnerProfilePreferredLanguage.setText(
                        preference!![PrefKeys.PREF_PREFERRED_LANG, ""]!!,
                        false
                    )
            }, 300)
        }
        preferredLanguageData = resources.getStringArray(R.array.preferred_language)
        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
            requireActivity(), R.layout.spinner_dropdown_custom_item, preferredLanguageData!!
        )
        spinnerProfilePreferredLanguage.setAdapter(adapter)
        spinnerProfilePreferredLanguage.onItemClickListener =
            AdapterView.OnItemClickListener { parent, arg1, position, id ->
                //TODO: You can your own logic.
                selectedPreferredLang = preferredLanguageData!![position]
            }
        spinnerProfilePreferredLanguage.setOnClickListener {
            spinnerProfilePreferredLanguage.showDropDown()
        }
    }

    private fun relationshipsSpinner() {
        try {
            if (preference!![PrefKeys.PREF_RELATIONSHIP, ""]!!.isNotEmpty()) {
                Handler().postDelayed({
                    if (spinnerProfileRelationship != null)
                        spinnerProfileRelationship.setText(
                            preference!![PrefKeys.PREF_RELATIONSHIP, ""]!!,
                            false
                        )
                }, 300)
            }
            relationshipsData = resources.getStringArray(R.array.relationship)
            val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
                requireActivity(),
                R.layout.spinner_dropdown_custom_item,
                relationshipsData!!
            )
            spinnerProfileRelationship.setAdapter(adapter)
            spinnerProfileRelationship.onItemClickListener =
                AdapterView.OnItemClickListener { parent, arg1, position, id ->
                    //TODO: You can your own logic.
                    selectedRelationship = relationshipsData!![position]
                }
            spinnerProfileRelationship.setOnClickListener {
                spinnerProfileRelationship.showDropDown()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /*private fun martialStatus() {
        if (preference!![PrefKeys.PREF_MARTIAL_STATUS, ""]!!.isNotEmpty()) {
            Handler().postDelayed({
                if (spinnerMartialStatus != null)
                    spinnerMartialStatus.setText(
                        preference!![PrefKeys.PREF_MARTIAL_STATUS, ""]!!,
                        false
                    )
            }, 300)
        }
        martialStatusData = resources.getStringArray(R.array.martialStatus)
        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
            requireActivity(), R.layout.spinner_dropdown_custom_item, martialStatusData!!
        )
        spinnerMartialStatus.setAdapter(adapter)
        spinnerMartialStatus.onItemClickListener =
            AdapterView.OnItemClickListener { parent, arg1, position, id ->
                //TODO: You can your own logic.
                selectedMartialStatus = martialStatusData!![position]
            }
    }*/

    private fun setDobCalender() {
        val cal = Calendar.getInstance()
        val dateSetListener =
            DatePickerDialog.OnDateSetListener { views, year, monthOfYear, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                val myFormat = "MM/dd/yyyy" // mention the format you need
                val sdf = SimpleDateFormat(myFormat)
                txtProfileDob.setText(sdf.format(cal.time))
            }

        txtProfileDob.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                mActivity!!, dateSetListener,
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }
    }

    override fun onResume() {
        super.onResume()
        genderSpinner()
        preferredLanguageSpinner()
        relationshipsSpinner()
        //martialStatus()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ProfileFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_Profile"
    }
}