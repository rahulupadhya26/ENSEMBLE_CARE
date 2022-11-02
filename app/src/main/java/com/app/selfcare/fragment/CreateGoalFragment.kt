package com.app.selfcare.fragment

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.app.selfcare.R
import com.app.selfcare.data.CreateJournal
import com.app.selfcare.data.CreatePersonalGoal
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper.get
import com.app.selfcare.utils.CalenderUtils
import com.app.selfcare.utils.Utils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_create_goal.*
import kotlinx.android.synthetic.main.fragment_create_journal.*
import kotlinx.android.synthetic.main.fragment_registration.*
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
 * Use the [CreateGoalFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CreateGoalFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    var selectedDuration: String = ""
    var durationData: Array<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_create_goal
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.VISIBLE
        getSubTitle().visibility = View.GONE

        goalDurationSpinner()

        val sdf = SimpleDateFormat("MM/dd/yyyy")
        val cal = Calendar.getInstance()
        //cal.add(Calendar.DAY_OF_YEAR, 1)
        val formattedDate = sdf.format(cal.time)
        txtGoalStartDate.setText(formattedDate)
        val dateSetListener =
            DatePickerDialog.OnDateSetListener { views, year, monthOfYear, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                val myFormat = "MM/dd/yyyy" // mention the format you need
                val sdf = SimpleDateFormat(myFormat)
                txtGoalStartDate.setText(sdf.format(cal.time))
            }

        txtGoalStartDate.setOnClickListener {
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

        btnGoalSave.setOnClickListener {
            if (isValidText(editTextGoalTitle)) {
                if (isValidText(editTextGoalDescription)) {
                    if (getText(goalDuration).isNotEmpty()) {
                        createPersonalGoal()
                    } else {
                        displayMsg("Alert", "Select the duration.")
                    }
                } else {
                    setEditTextError(editTextGoalDescription, "Enter the Goal Description")
                }
            } else {
                setEditTextError(editTextGoalTitle, "Enter the Goal Title")
            }
        }

    }

    private fun goalDurationSpinner() {
        durationData = resources.getStringArray(R.array.goal_duration)
        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
            requireActivity(), android.R.layout.simple_spinner_dropdown_item, durationData!!
        )
        goalDuration.setAdapter(adapter)

        goalDuration.onItemClickListener =
            AdapterView.OnItemClickListener { parent, arg1, position, id ->
                //TODO: You can your own logic.
                selectedDuration = durationData!![position]
            }
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
                            getText(editTextGoalTitle),
                            getText(editTextGoalDescription),
                            "Personal",
                            getText(txtGoalStartDate),
                            durationPos,
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
                                    getText(txtGoalStartDate) + " 00:00:00",
                                    getText(editTextGoalTitle),
                                    getText(editTextGoalDescription),
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
         * @return A new instance of fragment CreateGoalFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CreateGoalFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_create_goal"
    }
}