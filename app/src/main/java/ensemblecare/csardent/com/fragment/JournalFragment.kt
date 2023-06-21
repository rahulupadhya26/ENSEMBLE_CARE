package ensemblecare.csardent.com.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import ensemblecare.csardent.com.R
import ensemblecare.csardent.com.adapters.JournalListAdapter
import ensemblecare.csardent.com.controller.OnJournalItemClickListener
import ensemblecare.csardent.com.data.Journal
import ensemblecare.csardent.com.data.JournalDashboard
import ensemblecare.csardent.com.data.Patient
import ensemblecare.csardent.com.databinding.FragmentJournalBinding
import ensemblecare.csardent.com.preference.PrefKeys
import ensemblecare.csardent.com.preference.PreferenceHelper.get
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
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
    private lateinit var binding: FragmentJournalBinding

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentJournalBinding.inflate(inflater, container, false)
        return binding.root
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

        binding.journalListBack.setOnClickListener {
            setBottomNavigation(null)
            setLayoutBottomNavigation(null)
            replaceFragmentNoBackStack(
                BottomNavigationFragment(),
                R.id.layout_home,
                BottomNavigationFragment.TAG
            )
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            }

            override fun afterTextChanged(editable: Editable) {
                filterOne(editable.toString())
            }
        })

        binding.rlExpandedListLastWeek.visibility = View.GONE
        binding.ivCompressOne.rotation = -90f

        getJournalsData()

        binding.llPrevious7Days.setOnClickListener {
            if (isPlay) {
                binding.rlExpandedList.visibility = View.GONE
                binding.ivCompress.rotation = -90f
            } else {
                binding.rlExpandedList.visibility = View.VISIBLE
                binding.ivCompress.rotation = 0.3f
                binding.rlExpandedListLastWeek.visibility = View.GONE
                binding.ivCompressOne.rotation = -90f
            }
            isPlay = !isPlay
        }

        binding.llPreviousMonths.setOnClickListener {
            if (isPlay) {
                binding.rlExpandedListLastWeek.visibility = View.GONE
                binding.ivCompressOne.rotation = -90f
            } else {
                binding.rlExpandedListLastWeek.visibility = View.VISIBLE
                binding.ivCompressOne.rotation = 0.3f
                binding.rlExpandedList.visibility = View.GONE
                binding.ivCompress.rotation = -90f
            }
            isPlay = !isPlay
        }

        binding.fabCreateJournalBtn.setOnClickListener {
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
                        Patient(preference!![PrefKeys.PREF_PATIENT_ID, 0]!!),
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
                                object : TypeToken<JournalDashboard?>() {}.type
                            val journalDashboard: JournalDashboard =
                                Gson().fromJson(responseBody, journalList)
                            journalLists = journalDashboard.results
                            if (journalLists.isNotEmpty()) {
                                binding.layoutJournalData.visibility = View.VISIBLE
                                binding.txtNoJournal.visibility = View.GONE

                                for (i in 0 until journalLists.size) {
                                    val d1 = sdf.parse(journalLists[i].journal_date)
                                    val d2 = sdf.parse(currentDate!!)

                                    // Finding the absolute difference between
                                    // the two dates.time (in milli seconds)
                                    val mDifference = abs(d1!!.time - d2!!.time)
                                    // Converting milli seconds to dates
                                    val mDifferenceDates = mDifference / (24 * 60 * 60 * 1000)
                                    val mDayDifference = mDifferenceDates.toString()
                                    if (mDayDifference.toInt() <= 7) {
                                        binding.recyclerViewJournalList.visibility = View.VISIBLE
                                        journalPrevious7DaysLists.add(journalLists[i])
                                    }
                                    if (mDayDifference.toInt() > 7) {
                                        journalWeeksLists.add(journalLists[i])
                                        binding.llPreviousMonths.visibility = View.VISIBLE
                                    }
                                }

                                binding.recyclerViewJournalList.apply {
                                    layoutManager = StaggeredGridLayoutManager(
                                        2,
                                        RecyclerView.VERTICAL,
                                    )
                                    adapter = JournalListAdapter(
                                        mActivity!!,
                                        journalPrevious7DaysLists, this@JournalFragment
                                    )
                                }
                                binding.recyclerViewJournalListLastWeek.apply {
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
                                binding.layoutJournalData.visibility = View.GONE
                                binding.txtNoJournal.visibility = View.VISIBLE
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
            binding.scrollViewJournal.visibility = View.VISIBLE
            binding.llPrevious7Days.visibility = View.VISIBLE
            binding.llPreviousMonths.visibility = View.VISIBLE
            binding.recyclerViewJournalListLastWeek.visibility = View.VISIBLE
            binding.recyclerViewFilteredJournalList.visibility = View.GONE
            journalPrevious7DaysLists = arrayListOf()
            journalWeeksLists = arrayListOf()
            for (i in 0 until journalLists.size) {
                val d1 = sdf.parse(journalLists[i].journal_date)
                val d2 = sdf.parse(currentDate!!)
                // Finding the absolute difference between
                // the two dates.time (in milli seconds)
                val mDifference = abs(d1!!.time - d2!!.time)

                // Converting milli seconds to dates
                val mDifferenceDates = mDifference / (24 * 60 * 60 * 1000)
                val mDayDifference = mDifferenceDates.toString()
                if (mDayDifference.toInt() <= 7) {
                    binding.recyclerViewJournalList.visibility = View.VISIBLE
                    journalPrevious7DaysLists.add(journalLists[i])
                }
                if (mDayDifference.toInt() > 7) {
                    journalWeeksLists.add(journalLists[i])
                    binding.llPreviousMonths.visibility = View.VISIBLE
                }
            }
            binding.recyclerViewJournalList.apply {
                layoutManager = StaggeredGridLayoutManager(
                    2,
                    RecyclerView.VERTICAL,
                )
                adapter = JournalListAdapter(
                    mActivity!!,
                    journalPrevious7DaysLists, this@JournalFragment
                )
            }
            binding.recyclerViewJournalListLastWeek.apply {
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
            binding.scrollViewJournal.visibility = View.GONE
            binding.llPrevious7Days.visibility = View.GONE
            binding.llPreviousMonths.visibility = View.GONE
            binding.recyclerViewJournalListLastWeek.visibility = View.GONE
            binding.recyclerViewFilteredJournalList.visibility = View.VISIBLE
            val layoutManager = StaggeredGridLayoutManager(
                2,
                RecyclerView.VERTICAL,
            )
            adapter = JournalListAdapter(
                mActivity!!,
                journalLists, this@JournalFragment
            )
            adapter!!.filterList(filteredNames)
            binding.recyclerViewFilteredJournalList.layoutManager = layoutManager
            binding.recyclerViewFilteredJournalList.adapter = adapter
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