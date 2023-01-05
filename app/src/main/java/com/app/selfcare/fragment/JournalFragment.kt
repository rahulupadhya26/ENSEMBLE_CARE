package com.app.selfcare.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.app.selfcare.R
import com.app.selfcare.adapters.JournalListAdapter
import com.app.selfcare.controller.OnJournalItemClickListener
import com.app.selfcare.data.Journal
import com.app.selfcare.data.PatientId
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper.get
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_journal.*
import retrofit2.HttpException
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs

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
    var journalLists: ArrayList<Journal> = arrayListOf()
    private var journalPrevious7DaysLists: ArrayList<Journal> = arrayListOf()
    private var journalWeeksLists: ArrayList<Journal> = arrayListOf()
    private var adapter: JournalListAdapter? = null
    private var adapter2: JournalListAdapter? = null

    @SuppressLint("SimpleDateFormat")
    private var sdf = SimpleDateFormat(
        "MM/dd/yyyy"
    )
    private var currentDate: String? = null
    private var isPlay = true

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
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE
        updateStatusBarColor(R.color.resource_background)

        currentDate = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(Date())

        journalListBack.setOnClickListener {
            popBackStack()
        }

        et_search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            }

            override fun afterTextChanged(editable: Editable) {

                filterOne(editable.toString())
            }
        })

        rl_expanded_list_last_week.visibility = View.GONE
        iv_compress_one.rotation = -90f

        getJournalsData()

        ll_previous_7_days.setOnClickListener {
            if (isPlay) {
                rl_expanded_list.visibility = View.GONE
                iv_compress.rotation = -90f
            } else {
                rl_expanded_list.visibility = View.VISIBLE
                iv_compress.rotation = 0.3f
                rl_expanded_list_last_week.visibility = View.GONE
                iv_compress_one.rotation = -90f
            }
            isPlay = !isPlay
        }

        ll_previous_months.setOnClickListener {
            if (isPlay) {
                rl_expanded_list_last_week.visibility = View.GONE
                iv_compress_one.rotation = -90f
            } else {
                rl_expanded_list_last_week.visibility = View.VISIBLE
                iv_compress_one.rotation = 0.3f
                rl_expanded_list.visibility = View.GONE
                iv_compress.rotation = -90f
            }
            isPlay = !isPlay
        }

        fabCreateJournalBtn.setOnClickListener {
            replaceFragment(
                CreateJournalFragment(),
                R.id.layout_home,
                CreateJournalFragment.TAG
            )
        }
    }

    private fun getJournalsData() {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .getRequiredData(
                        "PI0017",
                        PatientId(preference!![PrefKeys.PREF_PATIENT_ID, 0]!!),
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

                            journalPrevious7DaysLists = arrayListOf()
                            journalWeeksLists = arrayListOf()
                            val journalList: Type =
                                object : TypeToken<ArrayList<Journal?>?>() {}.type
                            journalLists = Gson().fromJson(responseBody, journalList)

                            if (journalLists.isNotEmpty()) {
                                layoutJournalData.visibility = View.VISIBLE
                                txt_no_journal.visibility = View.GONE

                                for (i in 0 until journalLists.size) {
                                    Log.d(
                                        TAG,
                                        "getJournalsDataList: ${journalLists[i].journal_date}"
                                    )
                                    val d1 = sdf.parse(journalLists[i].journal_date)
                                    val d2 = sdf.parse(currentDate!!)

                                    // Finding the absolute difference between
                                    // the two dates.time (in milli seconds)
                                    val mDifference = abs(d1!!.time - d2!!.time)

                                    // Converting milli seconds to dates
                                    val mDifferenceDates = mDifference / (24 * 60 * 60 * 1000)

                                    val mDayDifference = mDifferenceDates.toString()
                                    Log.d(TAG, "getJournalsDataDays:$mDayDifference")

                                    if (mDayDifference.toInt() <= 7) {
                                        recyclerViewJournalList.visibility = View.VISIBLE
                                        journalPrevious7DaysLists.add(journalLists[i])
                                    }

                                    if (mDayDifference.toInt() in 8..15) {
                                        journalWeeksLists.add(journalLists[i])
                                        Log.d(
                                            TAG,
                                            "getJournalsDataLastWeeksData: $journalWeeksLists"
                                        )
                                        ll_previous_months.visibility = View.VISIBLE
                                    }
                                }

                                recyclerViewJournalList.apply {
                                    layoutManager = StaggeredGridLayoutManager(
                                        2,
                                        RecyclerView.VERTICAL,
                                    )
                                    adapter = JournalListAdapter(
                                        mActivity!!,
                                        journalPrevious7DaysLists, this@JournalFragment
                                    )
                                }
                                recyclerViewJournalList_last_week.apply {
                                    layoutManager = StaggeredGridLayoutManager(
                                        2,
                                        RecyclerView.VERTICAL,
                                    )
                                    adapter = JournalListAdapter(
                                        mActivity!!,
                                        journalWeeksLists, this@JournalFragment
                                    )
                                }

                                /*recyclerViewJournalList.apply {
                                    layoutManager = LinearLayoutManager(
                                        mActivity!!,
                                        RecyclerView.VERTICAL,
                                        false
                                    )
                                    adapter = JournalListAdapter(
                                        mActivity!!,
                                        journalLists, this@JournalFragment
                                    )
                                }*/
                            } else {
                                layoutJournalData.visibility = View.GONE
                                txt_no_journal.visibility = View.VISIBLE
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
                                getJournalsData()
                            }
                        } else {
                            displayAfterLoginErrorMsg(error)
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    private fun filterOne(text: String) {
        val filteredNames = ArrayList<Journal>()
        journalLists.filterTo(filteredNames) {
            it.name.toLowerCase().contains(text.toLowerCase())
        }

        if (text.isEmpty()) {
            ll_previous_7_days.visibility = View.VISIBLE
            ll_previous_months.visibility = View.VISIBLE
            recyclerViewJournalList_last_week.visibility = View.VISIBLE
            journalPrevious7DaysLists = arrayListOf()
            journalWeeksLists = arrayListOf()
            for (i in 0 until journalLists.size) {
                Log.d(TAG, "getJournalsDataList: ${journalLists[i].journal_date}")
                val d1 = sdf.parse(journalLists[i].journal_date)
                val d2 = sdf.parse(currentDate!!)

                // Finding the absolute difference between
                // the two dates.time (in milli seconds)
                val mDifference = abs(d1!!.time - d2!!.time)

                // Converting milli seconds to dates
                val mDifferenceDates = mDifference / (24 * 60 * 60 * 1000)

                val mDayDifference = mDifferenceDates.toString()
                Log.d(TAG, "getJournalsDataDays:$mDayDifference")

                if (mDayDifference.toInt() <= 7) {
                    recyclerViewJournalList.visibility = View.VISIBLE
                    journalPrevious7DaysLists.add(journalLists[i])
                }

                if (mDayDifference.toInt() in 8..15) {
                    journalWeeksLists.add(journalLists[i])
                    Log.d(TAG, "getJournalsDataLastWeeksData: $journalWeeksLists")
                    ll_previous_months.visibility = View.VISIBLE
                }
            }
            recyclerViewJournalList.apply {
                layoutManager = StaggeredGridLayoutManager(
                    2,
                    RecyclerView.VERTICAL,
                )
                adapter = JournalListAdapter(
                    mActivity!!,
                    journalPrevious7DaysLists, this@JournalFragment
                )
            }
            recyclerViewJournalList_last_week.apply {
                layoutManager = StaggeredGridLayoutManager(
                    2,
                    RecyclerView.VERTICAL,
                )
                adapter = JournalListAdapter(
                    mActivity!!,
                    journalWeeksLists, this@JournalFragment
                )
            }
        } else {
            ll_previous_7_days.visibility = View.GONE
            ll_previous_months.visibility = View.GONE
            recyclerViewJournalList_last_week.visibility = View.GONE
            adapter = JournalListAdapter(
                mActivity!!,
                journalLists, this@JournalFragment
            )
            adapter!!.filterList(filteredNames)
            recyclerViewJournalList.adapter = adapter
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
            deleteData("PI0017", journal.id) { response ->
                if (response == "Success") {
                    getJournalsData()
                }
            }
        } else {
            //Navigate to Journal detail screen
            replaceFragment(
                DetailJournalFragment.newInstance(journal),
                R.id.layout_home,
                DetailJournalFragment.TAG
            )
        }
    }
}