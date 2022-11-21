package com.app.selfcare.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.R
import com.app.selfcare.adapters.FacilityGoalAdapter
import com.app.selfcare.adapters.PersonalGoalAdapter
import com.app.selfcare.controller.OnGoalItemClickListener
import com.app.selfcare.data.Goal
import com.app.selfcare.data.Recommended
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper.get
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_facility_goal.*
import kotlinx.android.synthetic.main.fragment_personal_goal.*
import retrofit2.HttpException
import java.lang.Exception
import java.lang.reflect.Type
import kotlin.collections.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FacilityGoalFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FacilityGoalFragment : BaseFragment(), OnGoalItemClickListener {
    // TODO: Rename and change types of parameters
    private var providerGoals: ArrayList<Goal>? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            providerGoals = it.getParcelableArrayList(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_facility_goal
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getSubTitle().visibility = View.GONE
        if (providerGoals != null && providerGoals!!.isNotEmpty()) {
            getBackButton().visibility = View.VISIBLE
            txtProviderGoals.visibility = View.VISIBLE
            displayProviderGoals(providerGoals!!)
        } else {
            getRecommendedData()
        }
    }

    private fun getRecommendedData() {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .getRecommendedData(getAccessToken())
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
                            val recommend: Type = object : TypeToken<Recommended?>() {}.type
                            val recommended:Recommended = Gson().fromJson(responseBody, recommend)
                            displayProviderGoals(recommended.provider_goals)
                        } catch (e: Exception) {
                            hideProgress()
                            displayToast("Something went wrong.. Please try after sometime")
                        }
                    }, { error ->
                        hideProgress()
                        if ((error as HttpException).code() == 401) {
                            userLogin(
                                preference!![PrefKeys.PREF_EMAIL]!!,
                                preference!![PrefKeys.PREF_PASS]!!
                            ) { result ->
                                getRecommendedData()
                            }
                        } else {
                            //displayAfterLoginErrorMsg(error)
                            recycler_view_providerGoalsList.visibility = View.GONE
                            txtNoProviderGoals.visibility = View.VISIBLE
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    private fun displayProviderGoals(providerGoals: ArrayList<Goal>) {
        if (providerGoals.isNotEmpty()) {
            recycler_view_providerGoalsList.visibility = View.VISIBLE
            txtNoProviderGoals.visibility = View.GONE
            recycler_view_providerGoalsList.apply {
                layoutManager = GridLayoutManager(mActivity!!, 2)
                adapter = FacilityGoalAdapter(
                    mActivity!!,
                    providerGoals, this@FacilityGoalFragment
                )
            }
        } else {
            recycler_view_providerGoalsList.visibility = View.GONE
            txtNoProviderGoals.visibility = View.VISIBLE
        }
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FacilityGoalFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: ArrayList<Goal>, param2: String) =
            FacilityGoalFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
        const val TAG = "Screen_provider_goal"
    }

    override fun onGoalItemClickListener(goal: Goal, isDelete: Boolean) {
        if (isDelete) {
            //Delete Goal
        } else {
            replaceFragment(
                DetailGoalFragment.newInstance(goal),
                R.id.layout_home,
                DetailGoalFragment.TAG
            )
        }
    }
}