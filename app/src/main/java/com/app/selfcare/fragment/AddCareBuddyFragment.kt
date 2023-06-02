package com.app.selfcare.fragment

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.telephony.PhoneNumberFormattingTextWatcher
import android.telephony.PhoneNumberUtils
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import com.app.selfcare.R
import com.app.selfcare.data.AddCareBuddy
import com.app.selfcare.data.PartProfileData
import com.app.selfcare.databinding.FragmentActivityCarePlanBinding
import com.app.selfcare.databinding.FragmentAddCareBuddyBinding
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper.get
import com.app.selfcare.preference.PreferenceHelper.set
import com.app.selfcare.utils.Utils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject
import retrofit2.HttpException
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AddCareBuddyFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddCareBuddyFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var selectedGender: String? = null
    private var genderData: Array<String>? = null
    private var selectedRelationship: String = ""
    private var relationshipsData: Array<String>? = null
    private lateinit var binding: FragmentAddCareBuddyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_add_care_buddy
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddCareBuddyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE

        genderSpinner()
        relationshipsSpinner()

        binding.addCareBuddyBack.setOnClickListener {
            popBackStack()
        }

        binding.etCareBuddyPhoneNo.addTextChangedListener(object : PhoneNumberFormattingTextWatcher("US") {
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

        binding.btnSendInvitation.setOnClickListener {
            if (getText(binding.etCareBuddyFname).isNotEmpty()) {
                if (getText(binding.etCareBuddyLname).isNotEmpty()) {
                    if (getText(binding.etCareBuddyPhoneNo).isNotEmpty()) {
                        if (getText(binding.etCareBuddyPhoneNo).replace("-", "").length == 10) {
                            if (getText(binding.etCareBuddyEmail).isNotEmpty()) {
                                sendCareBuddyDetails()
                            } else {
                                setEditTextError(binding.etCareBuddyEmail, "Email cannot be blank")
                            }
                        } else {
                            setEditTextError(binding.etCareBuddyPhoneNo, "Enter valid phone number")
                        }
                    } else {
                        setEditTextError(binding.etCareBuddyPhoneNo, "Phone number cannot be blank")
                    }
                } else {
                    setEditTextError(binding.etCareBuddyLname, "Last name cannot be blank")
                }
            } else {
                setEditTextError(binding.etCareBuddyFname, "First name cannot be blank")
            }
        }
    }

    private fun genderSpinner() {
        try {
            genderData = resources.getStringArray(R.array.gender)
            val adapter = object : ArrayAdapter<String>(
                requireActivity(),
                R.layout.spinner_dropdown_custom_item, genderData!!
            ) {
                override fun isEnabled(position: Int): Boolean {
                    return position != 0
                }

                override fun getDropDownView(
                    position: Int,
                    convertView: View?,
                    parent: ViewGroup
                ): View {
                    val view: TextView =
                        super.getDropDownView(position, convertView, parent) as TextView
                    //set the color of first item in the drop down list to gray
                    if (position == 0) {
                        view.setTextColor(Color.GRAY)
                    } else {
                        //here it is possible to define color for other items by
                        //view.setTextColor(Color.BLACK)
                    }
                    return view
                }
            }
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_custom_item)
            binding.spinnerCareBuddyGender.adapter = adapter
            binding.spinnerCareBuddyGender.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View, position: Int, id: Long
                ) {
                    val value = parent.getItemAtPosition(position).toString()
                    if (value == genderData!![0]) {
                        (view as TextView).setTextColor(Color.GRAY)
                    }
                    selectedGender = genderData!![position]
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // write code to perform some action
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun relationshipsSpinner() {
        try {
            relationshipsData = resources.getStringArray(R.array.relationship)
            val adapter = object : ArrayAdapter<String>(
                requireActivity(),
                R.layout.spinner_dropdown_custom_item, relationshipsData!!
            ) {
                override fun isEnabled(position: Int): Boolean {
                    return position != 0
                }

                override fun getDropDownView(
                    position: Int,
                    convertView: View?,
                    parent: ViewGroup
                ): View {
                    val view: TextView =
                        super.getDropDownView(position, convertView, parent) as TextView
                    //set the color of first item in the drop down list to gray
                    if (position == 0) {
                        view.setTextColor(Color.GRAY)
                    } else {
                        //here it is possible to define color for other items by
                        view.setTextColor(Color.BLACK)
                    }
                    return view
                }
            }
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_custom_item)
            binding.spinnerCareBuddyRelation.adapter = adapter
            binding.spinnerCareBuddyRelation.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View, position: Int, id: Long
                ) {
                    val value = parent.getItemAtPosition(position).toString()
                    if (value == relationshipsData!![0]) {
                        (view as TextView).setTextColor(Color.GRAY)
                    }
                    selectedRelationship = relationshipsData!![position]
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // write code to perform some action
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun sendCareBuddyDetails() {
        val careBuddyData = AddCareBuddy(
            preference!![PrefKeys.PREF_PATIENT_ID, ""]!!.toInt(),
            getText(binding.etCareBuddyFname),
            getText(binding.etCareBuddyLname),
            selectedGender!!,
            getText(binding.etCareBuddyEmail),
            getText(binding.etCareBuddyPhoneNo),
            selectedRelationship,
            getText(binding.etCareBuddyState),
            getText(binding.etCareBuddyCity),
            getText(binding.etCareBuddyZipcode),
            getText(binding.etCareBuddyCountry),
            getText(binding.etCareBuddyAddress1),
            getText(binding.etCareBuddyAddress2)
        )
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .createCareBuddyData("PI0069", careBuddyData, getAccessToken())
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
                            clearCache()
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
         * @return A new instance of fragment AddCareBuddyFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AddCareBuddyFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_Add_CareBuddy"
    }
}