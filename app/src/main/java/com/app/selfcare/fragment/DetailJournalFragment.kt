package com.app.selfcare.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import com.app.selfcare.R
import com.app.selfcare.controller.IOnBackPressed
import com.app.selfcare.data.Journal
import com.app.selfcare.data.UpdateJournal
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper.get
import com.app.selfcare.utils.DateUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_create_journal.*
import kotlinx.android.synthetic.main.fragment_detail_journal.*
import kotlinx.android.synthetic.main.journal_menu.view.*
import kotlinx.android.synthetic.main.layout_item_appointments.view.*
import retrofit2.HttpException
import java.lang.reflect.Field
import java.lang.reflect.Method
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            journal = it.getParcelable(ARG_PARAM1)
        }
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

        detailJournalBack.setOnClickListener {
            if (userHasChanged) {
                if (getText(txt_journal_title).isNotEmpty()) {
                    if (getText(txt_journal_desc).isNotEmpty()) {
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

        txt_journal_desc.movementMethod = ScrollingMovementMethod()
        val journalDate = DateUtils(journal!!.journal_date + " " + journal!!.journal_time)
        detailJournalDateTime.text = journalDate.getDay() + " " +
                journalDate.getFullMonthName() + " " +
                journalDate.getYear() + " at " +
                journalDate.getTimeWithFormat().uppercase()
        /*txt_journal_date.text =
            Html.fromHtml("<b>Date</b> : " + journalDate.getDay() + " " + journalDate.getMonth() + " " + journalDate.getYear())*/
        txt_journal_title.setText(journal!!.name)
        txt_journal_desc.setText(journal!!.description)

        /*if (DateMethods().isToday(journalDate.mDate)) {
            txt_journal_title.isEnabled = true
            txt_journal_desc.isEnabled = true
        } else {
            txt_journal_title.isEnabled = false
            txt_journal_desc.isEnabled = false
        }*/

        detailJournalMenu.setOnClickListener {
            popup!!.showAsDropDown(detailJournalMenu, -400, 0)
        }

        txt_journal_title.addTextChangedListener(object : TextWatcher {
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

        txt_journal_desc.addTextChangedListener(object : TextWatcher {
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
                            getText(txt_journal_title),
                            getText(txt_journal_desc)
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
        val inflater: LayoutInflater =
            requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val v: View = inflater.inflate(R.layout.journal_menu, null)
        val layoutJournalShare: LinearLayout = v.layoutJournalShare
        val layoutJournalDelete: LinearLayout = v.layoutJournalDelete
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
    }

    override fun onBackPressed(): Boolean {
        if (popup!!.isShowing) {
            popup!!.dismiss()
        }
        return false
    }
}