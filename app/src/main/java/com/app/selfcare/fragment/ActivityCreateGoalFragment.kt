package com.app.selfcare.fragment

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.app.selfcare.R
import com.app.selfcare.data.CreatePersonalGoal
import com.app.selfcare.databinding.FragmentActivityCarePlanBinding
import com.app.selfcare.databinding.FragmentActivityCreateGoalBinding
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper.get
import com.app.selfcare.utils.CalenderUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ActivityCreateGoalFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ActivityCreateGoalFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var selectedDuration: String = ""
    private var durationData: Array<String>? = null
    private var selectedGoalType: String = ""
    private var goalTypeData: Array<String>? = null
    private lateinit var binding: FragmentActivityCreateGoalBinding

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
        binding = FragmentActivityCreateGoalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getLayout(): Int {
        return R.layout.fragment_activity_create_goal
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE

        binding.activityCreateGoalBack.setOnClickListener {
            popBackStack()
        }

        goalDurationSpinner()
        goalTypeSpinner()

        val sdf = SimpleDateFormat("MM/dd/yyyy")
        val cal = Calendar.getInstance()
        //cal.add(Calendar.DAY_OF_YEAR, 1)
        val formattedDate = sdf.format(cal.time)
        binding.txtActivityGoalDate.text = formattedDate
        val dateSetListener =
            DatePickerDialog.OnDateSetListener { views, year, monthOfYear, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                val myFormat = "MM/dd/yyyy" // mention the format you need
                val sdf = SimpleDateFormat(myFormat)
                binding.txtActivityGoalDate.text = sdf.format(cal.time)
            }

        binding.txtActivityGoalDate.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                mActivity!!, dateSetListener,
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.datePicker.minDate =
                System.currentTimeMillis()/* + 24 * 60 * 60 * 1000*/
            datePickerDialog.show()
        }

        binding.btnActivityGoalStart.setOnClickListener {
            if (isValidText(binding.editTextActivityGoalTitle)) {
                if (isValidText(binding.editTextActivityGoalDesc)) {
                    if (selectedGoalType.isNotEmpty()) {
                        if (selectedDuration.isNotEmpty()) {
                            createPersonalGoal()
                        } else {
                            displayMsg("Alert", "Select the duration.")
                        }
                    } else {
                        displayMsg("Alert", "Select the type.")
                    }
                } else {
                    setEditTextError(binding.editTextActivityGoalDesc, "Enter the Goal Description")
                }
            } else {
                setEditTextError(binding.editTextActivityGoalTitle, "Enter the Goal Title")
            }
        }
    }

    private fun goalTypeSpinner() {
        goalTypeData = resources.getStringArray(R.array.goal_type)

        val adapter = ArrayAdapter(
            requireActivity(),
            R.layout.spinner_dropdown_custom_item, goalTypeData!!
        )

        /*val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
            requireActivity(), android.R.layout.simple_spinner_dropdown_item, durationData!!
        )*/
        binding.spinnerActivityGoalType.adapter = adapter

        binding.spinnerActivityGoalType.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View, position: Int, id: Long
            ) {
                selectedGoalType = goalTypeData!![position]
                if (selectedGoalType == goalTypeData!![0]) {
                    binding.layoutActivityGoalDuration.visibility = View.VISIBLE
                    binding.layoutActivityGoalDate.visibility = View.GONE
                } else {
                    binding.layoutActivityGoalDuration.visibility = View.GONE
                    binding.layoutActivityGoalDate.visibility = View.VISIBLE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // write code to perform some action
            }
        }

        /*goalDuration.onItemClickListener =
            AdapterView.OnItemClickListener { parent, arg1, position, id ->
                //TODO: You can your own logic.
                selectedDuration = durationData!![position]
            }*/
    }

    private fun goalDurationSpinner() {
        durationData = resources.getStringArray(R.array.goal_duration)

        val adapter = ArrayAdapter(
            requireActivity(),
            R.layout.spinner_dropdown_custom_item, durationData!!
        )

        /*val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
            requireActivity(), android.R.layout.simple_spinner_dropdown_item, durationData!!
        )*/
        binding.spinnerActivityGoalDuration.adapter = adapter

        binding.spinnerActivityGoalDuration.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View, position: Int, id: Long
            ) {
                selectedDuration = durationData!![position]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // write code to perform some action
            }
        }

        /*goalDuration.onItemClickListener =
            AdapterView.OnItemClickListener { parent, arg1, position, id ->
                //TODO: You can your own logic.
                selectedDuration = durationData!![position]
            }*/
    }

    private fun createPersonalGoal() {
        var durationPos = 0
        when (selectedDuration) {
            "Does not repeat" -> durationPos = 0
            "Every day" -> durationPos = 1
            "Every week" -> durationPos = 2
            "Every month" -> durationPos = 3
            "Every year" -> durationPos = 4
        }
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .createPersonalGoalData(
                        "PI0010",
                        CreatePersonalGoal(
                            getText(binding.editTextActivityGoalTitle),
                            getText(binding.editTextActivityGoalDesc),
                            "Personal",
                            binding.txtActivityGoalDate.text.toString(),
                            durationPos,
                            selectedGoalType,
                            preference!![PrefKeys.PREF_PATIENT_ID, ""]!!
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
                            if (status == "201") {
                                CalenderUtils.addEvent(
                                    requireActivity(),
                                    binding.txtActivityGoalDate.text.toString() + " 00:00:00",
                                    getText(binding.editTextActivityGoalTitle),
                                    getText(binding.editTextActivityGoalDesc),
                                    selectedDuration, "30", "9", 5
                                )
                                popBackStack()
                            } else {
                                displayMsg(
                                    "Alert",
                                    "Something went wrong.. Please try after sometime"
                                )
                            }
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
                                createPersonalGoal()
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
         * @return A new instance of fragment ActivityCreateGoalFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ActivityCreateGoalFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_activity_create_goal"
    }
}