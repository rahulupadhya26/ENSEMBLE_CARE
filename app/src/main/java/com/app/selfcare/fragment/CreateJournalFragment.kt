package com.app.selfcare.fragment

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import java.text.SimpleDateFormat
import java.util.*
import android.util.Log
import com.app.selfcare.R
import com.app.selfcare.data.CreateJournal
import com.app.selfcare.preference.PrefKeys
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_create_journal.*
import com.app.selfcare.preference.PreferenceHelper.get
import retrofit2.HttpException
import java.lang.Exception


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CreateJournalFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CreateJournalFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    var createdJournalDate: String? = null
    var createdJournalTime: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_create_journal
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.VISIBLE
        getSubTitle().visibility = View.GONE

        val myFormat = "MM/dd/yyyy" // mention the format you need
        val sdf = SimpleDateFormat(myFormat)
        val myTimeFormat = "HH:mm:ss" // mention the format you need
        val timeSdf = SimpleDateFormat(myTimeFormat)
        val cal = Calendar.getInstance()
        val formattedDate = sdf.format(cal.time)
        createdJournalDate = formattedDate
        val formattedTIme = timeSdf.format(cal.time)
        createdJournalTime = formattedTIme
        val dateSetListener =
            DatePickerDialog.OnDateSetListener { views, year, monthOfYear, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                txtJournalDate.setText(sdf.format(cal.time))
                createdJournalDate = sdf.format(cal.time)
                createdJournalTime = timeSdf.format(cal.time)
            }

        txtJournalDate.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                mActivity!!, dateSetListener,
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }

        btnSaveJournal.setOnClickListener {
            if (getText(txtJournalDate).isNotEmpty()) {
                if (getText(edit_txt_journal_title).isNotEmpty()) {
                    if (getText(edit_txt_journal).isNotEmpty()) {
                        //Call create journal api
                        createJournal()
                    } else {
                        setEditTextError(edit_txt_journal, "Description cannot be empty!")
                    }
                } else {
                    setEditTextError(edit_txt_journal_title, "Title cannot be empty!")
                }
            } else {
                setEditTextError(txtJournalDate, "Date cannot be empty!")
            }
        }
    }

    private fun createJournal() {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .createJournalData(
                        "PI0017",
                        CreateJournal(
                            getText(edit_txt_journal_title),
                            getText(edit_txt_journal),
                            preference!![PrefKeys.PREF_PATIENT_ID,""]!!,
                            getText(txtJournalDate),
                            createdJournalTime!!
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
                            //if (status == "200") {
                            popBackStack()
                            /*} else {
                                displayMsg(
                                    "Alert",
                                    "Something went wrong.. Please try after sometime"
                                )
                            }*/
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
                                createJournal()
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
         * @return A new instance of fragment CreateJournalFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CreateJournalFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_create_journal"
    }
}