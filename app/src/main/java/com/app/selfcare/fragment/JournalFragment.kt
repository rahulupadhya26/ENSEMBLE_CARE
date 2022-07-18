package com.app.selfcare.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.R
import com.app.selfcare.adapters.JournalListAdapter
import com.app.selfcare.controller.OnJournalItemClickListener
import com.app.selfcare.data.Journal
import kotlinx.android.synthetic.main.fragment_journal.*
import java.util.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [JournalFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class JournalFragment : BaseFragment(), OnJournalItemClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_journal
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.VISIBLE
        getSubTitle().visibility = View.GONE

        val journalLists: ArrayList<Journal> = arrayListOf()
        journalLists.add(
            Journal(
                "Walking",
                "Walk for 10 kms for good health",
                "2022-07-06 11:12:06",
                "July",
                "2022",
                "11:12"
            )
        )
        journalLists.add(
            Journal(
                "Sleeping",
                "Sleep for 8 hours for good health",
                "2022-07-06 12:14:06",
                "July",
                "2022",
                "12:14"
            )
        )
        journalLists.add(
            Journal(
                "Eating",
                "Eat 3 times a day good health",
                "2022-07-06 12:15:16",
                "July",
                "2022",
                "12:14"
            )
        )
        recyclerViewJournalList.apply {
            layoutManager = LinearLayoutManager(
                mActivity!!,
                RecyclerView.VERTICAL,
                false
            )
            adapter = JournalListAdapter(
                mActivity!!,
                journalLists, this@JournalFragment
            )
        }

        fabCreateJournalBtn.setOnClickListener {
            replaceFragment(
                CreateJournalFragment(),
                R.id.layout_home,
                CreateJournalFragment.TAG
            )
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment JournalFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            JournalFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
        const val TAG = "Screen_journals"
    }

    override fun onJournalItemClicked(journal: Journal, isDelete: Boolean) {
        if (isDelete) {
            //Delete Journal
        } else {
            //Navigate to Journal detail screen
        }
    }
}