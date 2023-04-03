package com.app.selfcare.fragment

import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import com.app.selfcare.R
import com.app.selfcare.databinding.FragmentActivityCarePlanBinding
import com.app.selfcare.databinding.FragmentRegisterPartABinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [RegisterPartAFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RegisterPartAFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    var selectedGender: String? = null
    var genderData: Array<String>? = null
    private lateinit var binding: FragmentRegisterPartABinding

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
        binding = FragmentRegisterPartABinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getLayout(): Int {
        return R.layout.fragment_register_part_a
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.VISIBLE
        getSubTitle().visibility = View.GONE
        getSubTitle().text = ""

        //genderSpinner()
        //setDobCalender()

        /*btnRegisterA.setOnClickListener {
            if (getText(etSignUpSSN).isNotEmpty()) {
                if (txt_signup_dob.text.toString().isNotEmpty()) {
                    Utils.ssn = getText(etSignUpSSN)
                    Utils.sex = selectedGender!!
                    Utils.dob = txt_signup_dob.text.toString()
                    replaceFragment(
                        RegisterPartBFragment(),
                        R.id.layout_home,
                        RegisterPartBFragment.TAG
                    )
                } else {
                    displayToast("DOB cannot be blank")
                }
            } else {
                setEditTextError(
                    etSignUpSSN,
                    "SSN cannot be blank"
                )
            }
        }*/
    }

    /*private fun genderSpinner() {
        genderData = resources.getStringArray(R.array.gender)
        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
            requireActivity(), android.R.layout.simple_spinner_item, genderData!!
        )
        spinner_signup_gender.setAdapter(adapter)
        spinner_signup_gender.onItemClickListener =
            AdapterView.OnItemClickListener { parent, arg1, position, id ->
                //TODO: You can your own logic.
                selectedGender = genderData!![position]
            }
    }*/

    /*private fun setDobCalender() {
        val cal = Calendar.getInstance()
        val dateSetListener =
            DatePickerDialog.OnDateSetListener { views, year, monthOfYear, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                val myFormat = "yyyy-MM-dd" // mention the format you need
                val sdf = SimpleDateFormat(myFormat)
                txt_signup_dob.setText(sdf.format(cal.time))
            }

        txt_signup_dob.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                mActivity!!, dateSetListener,
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }
    }*/

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment RegisterPartAFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            RegisterPartAFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_Register_PartA"
    }
}