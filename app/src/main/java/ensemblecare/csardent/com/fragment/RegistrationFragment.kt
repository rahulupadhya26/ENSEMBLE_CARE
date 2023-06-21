package ensemblecare.csardent.com.fragment

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import ensemblecare.csardent.com.R
import ensemblecare.csardent.com.data.Register
import ensemblecare.csardent.com.databinding.FragmentRegistrationBinding
import ensemblecare.csardent.com.preference.PrefKeys
import ensemblecare.csardent.com.preference.PreferenceHelper.get
import ensemblecare.csardent.com.utils.Utils
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [RegistrationFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RegistrationFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var selectedGender: String? = null
    private var genderData: Array<String>? = null
    private var selectedPrefLang: String? = null
    private var prefLangData: Array<String>? = null
    private var selectedEthnicity: String? = null
    private var ethnicityData: Array<String>? = null
    private var selectedStatus: String? = null
    private var statusData: Array<String>? = null
    private var register: Register? = null
    private var selectedTherapy: String? = null
    private lateinit var binding: FragmentRegistrationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegistrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getLayout(): Int {
        return R.layout.fragment_registration
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE
        getSubTitle().text = ""
        updateStatusBarColor(R.color.initial_screen_background)

        selectedTherapy = preference!![PrefKeys.PREF_SELECTED_THERAPY, ""]!!

        if (preference!![PrefKeys.PREF_REG, ""]!!.isNotEmpty()) {
            register = Gson().fromJson(preference!![PrefKeys.PREF_REG, ""]!!, Register::class.java)
            binding.etSignUpFname.setText(register!!.first_name)
            binding.etSignUpMname.setText(register!!.middle_name)
            binding.etSignUpLname.setText(register!!.last_name)
            //etSignUpSSN.setText(register!!.ssn)
            binding.txtSignupDob.text = register!!.dob
        } else {
            binding.txtSignupDob.text = Utils.dob
        }

        genderSpinner()
        preferredLangSpinner()
        statusSpinner()
        ethnicitySpinner()
        setDobCalender()

        onClickEvents()
        //binding.txtSignupDob.hint = "mm/dd/yyyy"
        binding.etSignUpFname.requestFocus()

        binding.etSignUpSSN.addTextChangedListener(object : TextWatcher {
            private var spaceDeleted = false
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                val charDeleted = s.subSequence(start, start + count)
                spaceDeleted = "-" == charDeleted.toString()
            }

            override fun afterTextChanged(editable: Editable) {
                try {
                    binding.etSignUpSSN.removeTextChangedListener(this)
                    val cursorPosition: Int = binding.etSignUpSSN.selectionStart
                    val withSpaces = formatText(editable)
                    binding.etSignUpSSN.setText(withSpaces)
                    binding.etSignUpSSN.setSelection(cursorPosition + (withSpaces.length - editable.length))
                    if (spaceDeleted) {
                        //  userNameET.setSelection(userNameET.getSelectionStart() - 1);
                        spaceDeleted = false
                    }
                    binding.etSignUpSSN.addTextChangedListener(this)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            private fun formatText(text: CharSequence): String {
                val formatted = StringBuilder()
                if (text.length == 3 || text.length == 6) {
                    if (!spaceDeleted) formatted.append("$text-") else formatted.append(text)
                } else formatted.append(text)
                return formatted.toString()
            }
        })
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
            binding.spinnerSignupGender.adapter = adapter
            if (Utils.gender.isNotEmpty()) {
                Handler().postDelayed({
                    if (binding.spinnerSignupGender != null)
                        binding.spinnerSignupGender.setSelection(genderData!!.indexOf(Utils.gender))
                }, 300)
            }
            binding.spinnerSignupGender.onItemSelectedListener = object :
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

    private fun preferredLangSpinner() {
        try {
            prefLangData = resources.getStringArray(R.array.preferred_language)
            val adapter = object : ArrayAdapter<String>(
                requireActivity(),
                R.layout.spinner_dropdown_custom_item, prefLangData!!
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
            binding.txtSignupPreferredLang.adapter = adapter
            if (Utils.prefLang.isNotEmpty()) {
                Handler().postDelayed({
                    if (binding.txtSignupPreferredLang != null)
                        binding.txtSignupPreferredLang.setSelection(prefLangData!!.indexOf(Utils.prefLang))
                }, 300)
            }
            binding.txtSignupPreferredLang.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View, position: Int, id: Long
                ) {
                    val value = parent.getItemAtPosition(position).toString()
                    if (value == prefLangData!![0]) {
                        (view as TextView).setTextColor(Color.GRAY)
                    }
                    selectedPrefLang = prefLangData!![position]
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // write code to perform some action
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun statusSpinner() {
        try {
            statusData = resources.getStringArray(R.array.status)

            val adapter = object : ArrayAdapter<String>(
                requireActivity(),
                R.layout.spinner_dropdown_custom_item, statusData!!
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
            binding.spinnerSignUpStatus.adapter = adapter
            if (Utils.gender.isNotEmpty()) {
                Handler().postDelayed({
                    if (binding.spinnerSignUpStatus != null)
                        binding.spinnerSignUpStatus.setSelection(statusData!!.indexOf(Utils.role))
                }, 300)
            }
            binding.spinnerSignUpStatus.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?, position: Int, id: Long
                ) {
                    val value = parent.getItemAtPosition(position).toString()
                    if (value == statusData!![0]) {
                        (view as TextView).setTextColor(Color.GRAY)
                    }
                    selectedStatus = statusData!![position]
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // write code to perform some action
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun ethnicitySpinner() {
        try {
            ethnicityData = resources.getStringArray(R.array.ethnicity)

            val adapter = object : ArrayAdapter<String>(
                requireActivity(),
                R.layout.spinner_dropdown_custom_item, ethnicityData!!
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
            binding.spinnerSignUpEthnicity.adapter = adapter
            if (Utils.gender.isNotEmpty()) {
                Handler().postDelayed({
                    if (binding.spinnerSignUpEthnicity != null)
                        binding.spinnerSignUpEthnicity.setSelection(ethnicityData!!.indexOf(Utils.ethnicity))
                }, 300)
            }
            binding.spinnerSignUpEthnicity.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?, position: Int, id: Long
                ) {
                    val value = parent.getItemAtPosition(position).toString()
                    if (value == ethnicityData!![0]) {
                        (view as TextView).setTextColor(Color.GRAY)
                    }
                    selectedEthnicity = ethnicityData!![position]
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // write code to perform some action
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        genderSpinner()
        preferredLangSpinner()
    }

    private fun setDobCalender() {
        val cal = Calendar.getInstance()
        //cal.add(Calendar.YEAR, -13)
        val dateSetListener =
            DatePickerDialog.OnDateSetListener { views, year, monthOfYear, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                val myFormat = "MM/dd/yyyy" // mention the format you need
                val sdf = SimpleDateFormat(myFormat)
                binding.txtSignupDob.setText(sdf.format(cal.time))
            }

        binding.txtSignupDob.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                mActivity!!, dateSetListener,
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            )
            //datePickerDialog.datePicker.maxDate = cal.timeInMillis
            datePickerDialog.show()
        }
    }

    private fun onClickEvents() {
        binding.btnRegister.setOnClickListener {
            if (getText(binding.etSignUpFname).isNotEmpty()) {
                if (getText(binding.etSignUpLname).isNotEmpty()) {
                    //if (getText(etSignUpSSN).isNotEmpty()) {
                    //if (getText(etSignUpSSN).replace("-", "").length == 9) {
                    if (binding.txtSignupDob.text.toString().isNotEmpty()) {
                        if (selectedGender!! != "Gender") {
                            if (selectedPrefLang!! != "Language") {
                                if (selectedEthnicity!! != "Ethnicity") {
                                    if (selectedStatus!! != "Military Status") {
                                        when (selectedTherapy) {
                                            "Teen" -> {
                                                if (getAge(binding.txtSignupDob.text.toString()) in 13..17) {
                                                    storeAndNavigateToNextScreen()
                                                } else {
                                                    displayMsg(
                                                        "Alert",
                                                        "Age must be greater than 12 years and less than 18 years."
                                                    )
                                                }
                                            }

                                            else -> {
                                                if (getAge(binding.txtSignupDob.text.toString()) > 18) {
                                                    storeAndNavigateToNextScreen()
                                                } else {
                                                    displayMsg(
                                                        "Alert",
                                                        "Age must be more than 18 years."
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        displayMsg("Alert", "Select the Military Status")
                                    }
                                } else {
                                    displayMsg("Alert", "Select the Ethnicity")
                                }
                            } else {
                                displayMsg("Alert", "Select the preferred language")
                            }
                        } else {
                            displayMsg("Alert", "Select the gender")
                        }
                    } else {
                        displayMsg("Alert", "DOB cannot be blank")
                    }
                    /*} else {
                        setEditTextError(
                            etSignUpSSN, "Enter valid SSN."
                        )
                    }*/
                    /*} else {
                        setEditTextError(
                            etSignUpSSN, "SSN cannot be blank"
                        )
                    }*/
                } else {
                    setEditTextError(
                        binding.etSignUpLname,
                        "Last name cannot be blank"
                    )
                }
            } else {
                setEditTextError(
                    binding.etSignUpFname,
                    "First name cannot be blank"
                )
            }
        }
    }

    private fun storeAndNavigateToNextScreen() {
        Utils.firstName = getText(binding.etSignUpFname)
        Utils.middleName = getText(binding.etSignUpMname)
        Utils.lastName = getText(binding.etSignUpLname)
        Utils.ssn = getText(binding.etSignUpSSN).replace("-", "")
        Utils.gender = selectedGender!!
        Utils.prefLang = selectedPrefLang!!
        Utils.ethnicity = selectedEthnicity!!
        Utils.role = selectedStatus!!
        Utils.dob = binding.txtSignupDob.text.toString()
        replaceFragment(
            RegisterPartBFragment(),
            R.id.layout_home,
            RegisterPartBFragment.TAG
        )
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment RegistrationFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            RegistrationFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_Profile_SignUp"
    }
}