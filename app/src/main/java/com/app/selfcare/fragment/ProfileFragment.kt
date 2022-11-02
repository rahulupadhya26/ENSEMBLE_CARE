package com.app.selfcare.fragment

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.telephony.PhoneNumberFormattingTextWatcher
import android.telephony.PhoneNumberUtils
import android.text.Editable
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.app.selfcare.R
import com.app.selfcare.data.PartProfileData
import com.app.selfcare.data.PatientId
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper.get
import com.app.selfcare.preference.PreferenceHelper.set
import com.app.selfcare.utils.Utils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_profile.*
import org.json.JSONArray
import retrofit2.HttpException
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
    var selectedGender: String? = null
    var genderData: Array<String>? = null
    var selectedMartialStatus: String? = null
    var martialStatusData: Array<String>? = null

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
        getBackButton().visibility = View.VISIBLE
        getSubTitle().visibility = View.GONE

        getProfileDetails { result ->
            bindProfileData(result!!)
        }

        genderSpinner()
        setDobCalender()
        martialStatus()

        logout.setOnClickListener {
            displayConfirmPopup()
        }

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
            captureImage(imgProfilePhoto)
        }

        imgProfilePhoto.setOnClickListener {
            showImage(imgProfilePhoto)
        }

        imgProfilePhotoClear.setOnClickListener {
            imgProfilePhoto.setImageDrawable(null)
            imgProfilePhoto.setImageResource(R.drawable.profile_img)
        }

        verifyProfileDetails()
    }

    private fun bindProfileData(result: String) {
        val jsonArray = JSONArray(result)
        val jsonObject = jsonArray.getJSONObject(0)
        etProfileFname.setText(
            if (jsonObject.getString("first_name").equals("null")) "" else jsonObject.getString(
                "first_name"
            )
        )
        etProfileMname.setText(
            if (jsonObject.getString("middle_name")
                    .equals("null")
            ) "" else jsonObject.getString(
                "middle_name"
            )
        )
        etProfileLname.setText(
            if (jsonObject.getString("last_name").equals("null")) "" else jsonObject.getString(
                "last_name"
            )
        )
        etProfileSSN.setText(
            "XXX-XX-" + if (jsonObject.getString("ssn")
                    .equals("null")
            ) "" else jsonObject.getString(
                "ssn"
            ).takeLast(4)
        )
        //selectedGender = jsonObject.getString("gender")
        txtProfileDob.setText(
            if (jsonObject.getString("dob").equals("null")) "" else jsonObject.getString(
                "dob"
            )
        )
        etProfileMailId.setText(preference!![PrefKeys.PREF_EMAIL, ""]!!)
        val phoneNo =
            preference!![PrefKeys.PREF_PHONE_NO, ""]!!.replace("(", "").replace(")", "")
                .replace(" ", "").replace("-", "")
        etProfilePhoneNo.setText(formatNumbersAsCode(phoneNo))
        etProfileEmerConName.setText(preference!![PrefKeys.PREF_EMERGENCY_CONTACT_NAME, ""]!!)
        etProfileEmerConPhno.setText(
            if (jsonObject.getString("emergency_phone")
                    .equals("null")
            ) "" else jsonObject.getString(
                "emergency_phone"
            )
        )
        etProfileStreet.setText(
            if (jsonObject.getString("street").equals("null")) "" else jsonObject.getString(
                "street"
            )
        )

        etProfileCity.setText(
            if (jsonObject.getString("city").equals("null")) "" else jsonObject.getString(
                "city"
            )
        )
        etProfileState.setText(
            if (jsonObject.getString("state").equals("null")) "" else jsonObject.getString(
                "state"
            )
        )
        etProfileCountry.setText(
            if (jsonObject.getString("county").equals("null")) "" else jsonObject.getString(
                "county"
            )
        )
        etProfileZipcode.setText(
            if (jsonObject.getString("zipcode").equals("null")) "" else jsonObject.getString(
                "zipcode"
            )
        )
        preference!![PrefKeys.PREF_MARTIAL_STATUS] = if (jsonObject.getString("marital_status")
                .equals("null")
        ) "" else jsonObject.getString(
            "marital_status"
        )

        val photo = preference!![PrefKeys.PREF_PHOTO, ""]!!
        if (photo.isNotEmpty()) {
            val base64Image = photo.split(",")[1]
            val decodedString: ByteArray = Base64.decode(base64Image, Base64.DEFAULT)
            val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
            imgProfilePhoto.setImageBitmap(decodedByte)
        }

        preference!![PrefKeys.PREF_SEVERITY_SCORE] =
            jsonObject.getString("patient_severity_score")
        preference!![PrefKeys.PREF_SELECTED_SERVICE] = jsonObject.getString("selected_service")
        preference!![PrefKeys.PREF_SELECTED_DISORDER] =
            jsonObject.getString("selected_dissorder")
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
                                    bindProfileData(result!!)
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
                    //if (getText(etProfileEmerConName).isNotEmpty()) {
                    //if (getText(etProfileEmerConPhno).isNotEmpty()) {
                    //if (getText(etProfileStreet).isNotEmpty()) {
                    //if (getText(etProfileCity).isNotEmpty()) {
                    //if (getText(etProfileState).isNotEmpty()) {
                    //if (getText(etProfileCountry).isNotEmpty()) {
                    //if (getText(etProfileZipcode).isNotEmpty()) {
                    updateProfileData()
                    /*} else {
                        setEditTextError(
                            etProfileZipcode,
                            "ZipCode cannot be empty!"
                        )
                    }*/
                    /*} else {
                        setEditTextError(
                            etProfileCountry,
                            "Country cannot be empty!"
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
                    /*} else {
                        setEditTextError(etProfileStreet, "Street cannot be empty!")
                    }*/
                    /*} else {
                        setEditTextError(
                            etProfileEmerConPhno,
                            "Emergency contact phone number cannot be empty!"
                        )
                    }*/
                    /*} else {
                        setEditTextError(
                            etProfileEmerConName,
                            "Emergency contact person name cannot be empty!"
                        )
                    }*/
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
        preference!![PrefKeys.PREF_PHOTO] = photo
        val profileData = PartProfileData(
            preference!![PrefKeys.PREF_PATIENT_ID, ""]!!.toInt(),
            getText(etProfileFname),
            getText(etProfileLname),
            getText(txtProfileDob),
            photo
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
                            val responseBody = result.string()
                            Log.d("Response Body", responseBody)
                            popBackStack()
                        } catch (e: Exception) {
                            hideProgress()
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
            requireActivity(), android.R.layout.simple_spinner_dropdown_item, genderData!!
        )
        spinnerProfileGender.setAdapter(adapter)
        spinnerProfileGender.onItemClickListener =
            AdapterView.OnItemClickListener { parent, arg1, position, id ->
                //TODO: You can your own logic.
                selectedGender = genderData!![position]
            }
    }

    private fun martialStatus() {
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
            requireActivity(), android.R.layout.simple_spinner_dropdown_item, martialStatusData!!
        )
        spinnerMartialStatus.setAdapter(adapter)
        spinnerMartialStatus.onItemClickListener =
            AdapterView.OnItemClickListener { parent, arg1, position, id ->
                //TODO: You can your own logic.
                selectedMartialStatus = martialStatusData!![position]
            }
    }

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
        martialStatus()
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