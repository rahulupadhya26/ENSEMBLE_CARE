package com.app.selfcare.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.R
import com.app.selfcare.adapters.AllGoalsAdapter
import com.app.selfcare.adapters.DashboardJournalAdapter
import com.app.selfcare.controller.OnGoalItemClickListener
import com.app.selfcare.controller.OnJournalItemClickListener
import com.app.selfcare.data.*
import com.app.selfcare.databinding.FragmentActivityCarePlanBinding
import com.app.selfcare.databinding.FragmentExploreBinding
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper.get
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import java.lang.reflect.Type
import java.util.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ExploreFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ExploreFragment : BaseFragment(), OnGoalItemClickListener, OnJournalItemClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private val isDebug: Boolean = true
    private lateinit var binding: FragmentExploreBinding

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
        binding = FragmentExploreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getLayout(): Int {
        return R.layout.fragment_explore
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE

        displayGoalsData()

        displayJournalsData()

        displayDiscussionData()
    }

    private fun displayGoalsData() {
        //Goals
        /*if (isDebug) {
            goalLists.add(Goal("Goal 1", "", "2022-07-25", "Everyday"))
            goalLists.add(Goal("Goal 2", "", "2022-07-22", "Everyday"))
            goalLists.add(Goal("Goal 3", "", "2022-07-20", "Everyday"))
        }*/
        getData("PI0010") { response ->
            var goalLists: ArrayList<Goal> = arrayListOf()
            val goalList: Type = object : TypeToken<ArrayList<Goal?>?>() {}.type
            goalLists = Gson().fromJson(response, goalList)

            if (goalLists.isNotEmpty()) {
                binding.recyclerviewGoals.visibility = View.VISIBLE
                binding.txtNoGoalsData.visibility = View.GONE
                binding.recyclerviewGoals.apply {
                    layoutManager = LinearLayoutManager(mActivity!!, RecyclerView.HORIZONTAL, false)
                    adapter = AllGoalsAdapter(mActivity!!, goalLists, this@ExploreFragment, false)
                }
            } else {
                binding.recyclerviewGoals.visibility = View.GONE
                binding.txtNoGoalsData.visibility = View.VISIBLE
            }
        }

        binding.txtViewAllGoals.setOnClickListener {
            replaceFragment(
                GoalsFragment(),
                R.id.layout_home,
                GoalsFragment.TAG
            )
        }
    }

    private fun displayJournalsData() {
        //Journals
        getData("PI0017") { response ->
            var journalLists: ArrayList<Journal> = arrayListOf()
            val journalList: Type = object : TypeToken<ArrayList<Journal?>?>() {}.type
            journalLists = Gson().fromJson(response, journalList)

            if (journalLists.isNotEmpty()) {
                binding.recyclerviewJournals.visibility = View.VISIBLE
                binding.txtNoJournalsData.visibility = View.GONE
                binding.recyclerviewJournals.apply {
                    layoutManager = LinearLayoutManager(mActivity!!, RecyclerView.HORIZONTAL, false)
                    adapter =
                        DashboardJournalAdapter(mActivity!!, journalLists, this@ExploreFragment)
                }
            } else {
                binding.recyclerviewJournals.visibility = View.GONE
                binding.txtNoJournalsData.visibility = View.VISIBLE
            }
        }
        /*if (isDebug) {
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
        }*/

        binding.txtViewAllJournals.setOnClickListener {
            replaceFragment(JournalFragment(), R.id.layout_home, JournalFragment.TAG)
        }
    }

    private fun displayDiscussionData() {

    }

    /*private fun getData(tableId: String, myCallback: (result: String?) -> Unit) {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .getRequiredData(tableId, PatientId(preference!![PrefKeys.PREF_PATIENT_ID, 0]!!), getAccessToken())
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
                            myCallback.invoke(responseBody)
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
                                when (tableId) {
                                    "PI0010" -> displayGoalsData()
                                    "PI0017" -> displayJournalsData()
                                }
                            }
                        } else {
                            displayAfterLoginErrorMsg(error)
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }*/

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ExploreFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ExploreFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_explore"
    }

    override fun onGoalItemClickListener(goal: Goal, isDelete: Boolean) {
        if (isDelete) {
            //Delete Goal
            deleteData("PI0010", goal.id) { response ->
                if (response == "Success") {
                    displayGoalsData()
                }
            }
        } else {
            replaceFragment(
                DetailGoalFragment.newInstance(goal),
                R.id.layout_home,
                DetailGoalFragment.TAG
            )
        }
    }

    override fun onJournalItemClicked(journal: Journal, isDelete: Boolean) {
        if (isDelete) {
            //Delete Journal
            deleteData("PI0017", journal.id) { response ->
                if (response == "Success") {
                    displayJournalsData()
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