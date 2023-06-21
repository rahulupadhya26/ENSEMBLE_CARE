package ensemblecare.csardent.com.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextWatcher
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import ensemblecare.csardent.com.R
import ensemblecare.csardent.com.controller.IOnBackPressed
import ensemblecare.csardent.com.data.Journal
import ensemblecare.csardent.com.data.UpdateJournal
import ensemblecare.csardent.com.databinding.FragmentDetailJournalBinding
import ensemblecare.csardent.com.databinding.JournalMenuBinding
import ensemblecare.csardent.com.preference.PrefKeys
import ensemblecare.csardent.com.preference.PreferenceHelper.get
import ensemblecare.csardent.com.utils.DateUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import java.util.*
import kotlin.math.abs


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [DetailJournalFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DetailJournalFragment : BaseFragment(), IOnBackPressed {
    // TODO: Rename and change types of parameters
    private var journal: Journal? = null
    private var userHasChanged = false
    private var popup: PopupWindow? = null
    private lateinit var binding: FragmentDetailJournalBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            journal = it.getParcelable(ARG_PARAM1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDetailJournalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getLayout(): Int {
        return R.layout.fragment_detail_journal
    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE
        updateStatusBarColor(R.color.journal_background)

        userHasChanged = false

        showPopUp()

        binding.detailJournalBack.setOnClickListener {
            if (userHasChanged) {
                if (getText(binding.txtJournalTitle).isNotEmpty()) {
                    if (getText(binding.txtJournalDesc).isNotEmpty()) {
                        //Call create journal api
                        updateJournal()
                    } else {
                        //setEditTextError(edit_txt_journal, "Description cannot be empty!")
                        popBackStack()
                    }
                } else {
                    //setEditTextError(edit_txt_journal_title, "Title cannot be empty!")
                    popBackStack()
                }
            } else {
                popBackStack()
            }
        }

        binding.txtJournalDesc.movementMethod = ScrollingMovementMethod()
        val journalDate = DateUtils(journal!!.journal_date + " " + journal!!.journal_time)
        binding.detailJournalDateTime.text = journalDate.getDay() + " " +
                journalDate.getFullMonthName() + " " +
                journalDate.getYear() + " at " +
                journalDate.getTimeWithFormat().uppercase()
        /*txt_journal_date.text =
            Html.fromHtml("<b>Date</b> : " + journalDate.getDay() + " " + journalDate.getMonth() + " " + journalDate.getYear())*/
        binding.txtJournalTitle.setText(journal!!.name)
        binding.txtJournalDesc.setText(journal!!.description)

        /*if (DateMethods().isToday(journalDate.mDate)) {
            txt_journal_title.isEnabled = true
            txt_journal_desc.isEnabled = true
        } else {
            txt_journal_title.isEnabled = false
            txt_journal_desc.isEnabled = false
        }*/

        binding.detailJournalMenu.setOnClickListener {
            popup!!.showAsDropDown(binding.detailJournalMenu, -400, 0)
        }

        binding.txtJournalTitle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val userChange = abs(count - before) == 1
                if (userChange) {
                    userHasChanged = true
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })

        binding.txtJournalDesc.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val userChange = abs(count - before) == 1
                if (userChange) {
                    userHasChanged = true
                }
            }

            override fun afterTextChanged(s: Editable?) {
                if (s.toString().isNotEmpty())
                    userHasChanged = true
            }

        })

        binding.speechToText.setOnClickListener {
            binding.txtJournalDesc.requestFocus()
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
    }

    private fun updateJournal() {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .updateJournal(
                        "PI0017",
                        UpdateJournal(
                            journal!!.id,
                            getText(binding.txtJournalTitle),
                            getText(binding.txtJournalDesc)
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
                                updateJournal()
                            }
                        } else {
                            displayAfterLoginErrorMsg(error)
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    @SuppressLint("RestrictedApi")
    private fun showPopUp() {
        val journalMenu = JournalMenuBinding.inflate(layoutInflater)
        /*val inflater: LayoutInflater =
            requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater*/
        val v: View = journalMenu.root
        val layoutJournalShare: LinearLayout = journalMenu.layoutJournalShare
        val layoutJournalDelete: LinearLayout = journalMenu.layoutJournalDelete
        popup = PopupWindow(v, 500, RelativeLayout.LayoutParams.WRAP_CONTENT, true)
        popup!!.contentView.setOnClickListener {
            popup!!.dismiss()
        }
        layoutJournalShare.setOnClickListener {
            popup!!.dismiss()
            shareDetails(
                "",
                journal!!.name + "\n\n" + journal!!.description,
                "",
                "",
                "Journal"
            )
        }
        layoutJournalDelete.setOnClickListener {
            popup!!.dismiss()
            //Delete Journal
            deleteData("PI0017", journal!!.id) { response ->
                if (response == "Success") {
                    popBackStack()
                }
            }
        }
        /*val popup = PopupWindow(view, 300, RelativeLayout.LayoutParams.WRAP_CONTENT, true)
        popup.menuInflater.inflate(R.menu.journal_menu, popup.menu)
        popup.setOnMenuItemClickListener { item: MenuItem? ->
            when (item!!.itemId) {
                R.id.journalShare -> {
                    shareDetails(
                        "",
                        journal!!.name + "\n" + journal!!.description,
                        "",
                        "",
                        "Journal"
                    )
                }
                R.id.journalDelete -> {
                    //Delete Journal
                    deleteData("PI0017", journal!!.id) { response ->
                        if (response == "Success") {
                            popBackStack()
                        }
                    }
                }
            }
            true
        }
        popup.show()*/
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
                        val text = getText(binding.txtJournalDesc)
                        if (text.isEmpty()) {
                            binding.txtJournalDesc.setText("$text$recognizedText")
                        } else {
                            binding.txtJournalDesc.setText("$text $recognizedText")
                        }
                    }
                }
            }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment DetailJournalFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(journal: Journal) =
            DetailJournalFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM1, journal)
                }
            }

        const val TAG = "Screen_detailed_journal"
        private const val REQUEST_CODE_STT = 1
    }

    override fun onBackPressed(): Boolean {
        if (popup!!.isShowing) {
            popup!!.dismiss()
        }
        return false
    }
}