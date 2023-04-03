package com.app.selfcare.fragment

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import com.app.selfcare.R
import com.app.selfcare.data.CreateToDo
import com.app.selfcare.databinding.FragmentActivityCarePlanBinding
import com.app.selfcare.databinding.FragmentAddToDoBinding
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper.get
import com.app.selfcare.utils.DateUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import java.text.SimpleDateFormat
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AddToDoFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddToDoFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var selectedDateTime: String = ""
    private lateinit var binding: FragmentAddToDoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_add_to_do
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddToDoBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE
        updateStatusBarColor(R.color.white)

        binding.addToDoBack.setOnClickListener {
            if (getText(binding.editTxtToDoName).isNotEmpty()) {
                if (getText(binding.editTextToDoDescription).isNotEmpty()) {
                    if (selectedDateTime.isNotEmpty()) {
                        addToDo()
                    } else {
                        popBackStack()
                    }
                } else {
                    popBackStack()
                }
            } else {
                popBackStack()
            }
        }

        binding.btnAddToDo.setOnClickListener {
            if (getText(binding.editTxtToDoName).isNotEmpty()) {
                if (getText(binding.editTextToDoDescription).isNotEmpty()) {
                    if (selectedDateTime.isNotEmpty()) {
                        addToDo()
                    } else {
                        displayMsg("Alert", "Select due date")
                    }
                } else {
                    setEditTextError(binding.editTextToDoDescription, "Description cannot be blank")
                }
            } else {
                setEditTextError(binding.editTxtToDoName, "Title cannot be blank")
            }
        }

        val cal = Calendar.getInstance()
        //cal.add(Calendar.YEAR, -13)
        val dateSetListener =
            DatePickerDialog.OnDateSetListener { views, year, monthOfYear, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                val myFormat = "MM/dd/yyyy" // mention the format you need
                val sdf = SimpleDateFormat(myFormat)
                selectedDateTime = sdf.format(cal.time)
                val dateTime = DateUtils(sdf.format(cal.time) + " 00:00:00")
                binding.txtToDoSelectedDate.text =
                    dateTime.getDay() + " " + dateTime.getMonth() + " " + dateTime.getYear()
            }

        binding.layoutToDoSelectDate.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                mActivity!!, dateSetListener,
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.datePicker.minDate = cal.timeInMillis
            datePickerDialog.show()
        }
    }

    private fun addToDo() {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .createToDoData(
                        "PI0009",
                        CreateToDo(
                            preference!![PrefKeys.PREF_PATIENT_ID, ""]!!.toInt(),
                            getText(binding.editTxtToDoName),
                            getText(binding.editTextToDoDescription),
                            selectedDateTime!!,
                            false
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
                                addToDo()
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
         * @return A new instance of fragment AddToDoFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AddToDoFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_add_todo"
    }
}