package com.app.selfcare.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Html
import android.text.method.ScrollingMovementMethod
import android.view.View
import androidx.fragment.app.Fragment
import com.app.selfcare.R
import com.app.selfcare.data.Journal
import com.app.selfcare.utils.DateUtils
import kotlinx.android.synthetic.main.fragment_detail_journal.*
import java.util.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [DetailJournalFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DetailJournalFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var journal: Journal? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            journal = it.getParcelable(ARG_PARAM1)
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_detail_journal
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.VISIBLE
        getSubTitle().visibility = View.GONE
        txt_journal_desc.movementMethod = ScrollingMovementMethod()
        val journalDate = DateUtils(journal!!.journal_date + " 01:00:00")
        txt_journal_date.text =
            Html.fromHtml("<b>Date</b> : " + journalDate.getDay() + " " + journalDate.getMonth() + " " + journalDate.getYear())
        txt_journal_title.text = journal!!.name
        txt_journal_desc.text = Html.fromHtml("<b>Description</b> : " + journal!!.description)
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
}