package com.app.selfcare.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.app.selfcare.R
import com.app.selfcare.data.ConsentRoisFormsNotify
import com.app.selfcare.data.ConsentRoisPk
import com.app.selfcare.data.CreateJournal
import com.app.selfcare.databinding.FragmentCreateJournalBinding
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper.get
import com.app.selfcare.preference.PreferenceHelper.set
import com.app.selfcare.utils.DateUtils
import com.google.gson.Gson
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
 * Use the [CreateJournalFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CreateJournalFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    var createdJournalDate: String? = null
    var createdJournalTime: String? = null
    private lateinit var binding: FragmentCreateJournalBinding

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
        binding = FragmentCreateJournalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getLayout(): Int {
        return R.layout.fragment_create_journal
    }

    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE
        updateStatusBarColor(R.color.journal_background)

        val myFormat = "MM/dd/yyyy" // mention the format you need
        val sdf = SimpleDateFormat(myFormat)
        val myTimeFormat = "HH:mm:ss" // mention the format you need
        val timeSdf = SimpleDateFormat(myTimeFormat)
        val cal = Calendar.getInstance()
        val formattedDate = sdf.format(cal.time)
        binding.editTextJournalDate.setText(formattedDate)
        createdJournalDate = formattedDate
        val formattedTIme = timeSdf.format(cal.time)
        createdJournalTime = formattedTIme
        val journalCurrentDateTime = DateUtils("$formattedDate $formattedTIme")
        binding.currentJournalDateTime.text =
            journalCurrentDateTime.getDay() + " " +
                    journalCurrentDateTime.getFullMonthName() + " " +
                    journalCurrentDateTime.getYear() + " at " +
                    journalCurrentDateTime.getTimeWithFormat().uppercase()
        /*val dateSetListener =
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
        }*/

        /*createJournalBack.setOnClickListener {
            popBackStack()
        }*/

        binding.fabSpeechToText.setOnClickListener {
            // Get the Intent action
            val sttIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            // Language model defines the purpose, there are special models for other use cases, like search.
            sttIntent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            // Adding an extra language, you can use any language from the Locale class.
            sttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            // Text that shows up on the Speech input prompt.
            sttIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now!")
            try {
                // Start the intent for a result, and pass in our request code.
                startActivityForResult(sttIntent, REQUEST_CODE_STT)
            } catch (e: ActivityNotFoundException) {
                // Handling error when the service is not available.
                e.printStackTrace()
                displayToast("Your device does not support STT.")
            }
        }

        binding.createJournalBack.setOnClickListener {
            if (getText(binding.editTxtJournalTitle).isNotEmpty()) {
                if (getText(binding.editTxtJournal).isNotEmpty()) {
                    //Call create journal api
                    createJournal()
                } else {
                    //setEditTextError(edit_txt_journal, "Description cannot be empty!")
                    popBackStack()
                }
            } else {
                //setEditTextError(edit_txt_journal_title, "Title cannot be empty!")
                popBackStack()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            // Handle the result for our request code.
            REQUEST_CODE_STT -> {
                // Safety checks to ensure data is available.
                if (resultCode == Activity.RESULT_OK && data != null) {
                    // Retrieve the result array.
                    val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    // Ensure result array is not null or empty to avoid errors.
                    if (!result.isNullOrEmpty()) {
                        // Recognized text is in the first position.
                        val recognizedText = result[0]
                        // Do what you want with the recognized text.
                        val text = getText(binding.editTxtJournal)
                        if (text.isEmpty()) {
                            binding.editTxtJournal.setText("$text$recognizedText")
                        } else {
                            binding.editTxtJournal.setText("$text $recognizedText")
                        }
                    }
                }
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
                            getText(binding.editTxtJournalTitle),
                            getText(binding.editTxtJournal),
                            preference!![PrefKeys.PREF_PATIENT_ID, ""]!!,
                            getText(binding.editTextJournalDate),
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
                            if (detectSeverity(getText(binding.editTxtJournal))) {
                                preference!![PrefKeys.PREF_IS_COLUMBIA_SEVERITY] = ""
                                val consentRoisFormsNotifyList: ArrayList<ConsentRoisFormsNotify> =
                                    arrayListOf()
                                val consentRoisPk = ConsentRoisPk(
                                    pk = 0,
                                    category = "form_mobile"
                                )
                                val consentRoisFormsNotify = ConsentRoisFormsNotify(
                                    id = 0,
                                    title = "Columbia Suicide Severity Form",
                                    type = "consent_form",
                                    description = "columbia_suicide_severity_screen",
                                    extra_data = consentRoisPk
                                )
                                consentRoisFormsNotifyList.add(consentRoisFormsNotify)
                                preference!![PrefKeys.PREF_IS_COLUMBIA_SEVERITY] =
                                    Gson().toJson(consentRoisFormsNotifyList)
                                popBackStack()
                                replaceFragment(
                                    ConsentRoisSignFragment.newInstance(
                                        consentRoisFormsNotifyList,
                                        true
                                    ),
                                    R.id.layout_home,
                                    ConsentRoisSignFragment.TAG
                                )
                            } else {
                                popBackStack()
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
        private const val REQUEST_CODE_STT = 1
    }
}