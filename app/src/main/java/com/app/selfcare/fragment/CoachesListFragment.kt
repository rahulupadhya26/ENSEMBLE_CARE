package com.app.selfcare.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.R
import com.app.selfcare.adapters.CoachListAdapter
import com.app.selfcare.data.CoachReqBody
import com.app.selfcare.data.Coaches
import com.app.selfcare.databinding.FragmentActivityCarePlanBinding
import com.app.selfcare.databinding.FragmentCoachesListBinding
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper.get
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import java.lang.reflect.Type

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CoachesListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CoachesListFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var coachType: String? = null
    private var specialization: ArrayList<String>? = null
    private lateinit var binding: FragmentCoachesListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            coachType = it.getString(ARG_PARAM1)
            val specializationType: Type =
                object : TypeToken<ArrayList<String?>?>() {}.type
            specialization = Gson().fromJson(it.getString(ARG_PARAM2), specializationType)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCoachesListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getLayout(): Int {
        return R.layout.fragment_coaches_list
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.VISIBLE
        getSubTitle().visibility = View.GONE

        if (coachType != null && specialization != null) {
            getCoachesList()
        }
    }

    private fun getCoachesList() {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .getCoaches(CoachReqBody(coachType!!, specialization!!), getAccessToken())
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
                            var coachesList: ArrayList<Coaches> = arrayListOf()
                            val coachList: Type =
                                object : TypeToken<ArrayList<Coaches?>?>() {}.type
                            coachesList = Gson().fromJson(responseBody, coachList)
                            if (coachesList.isNotEmpty()) {
                                binding.recyclerViewCoaches.visibility = View.VISIBLE
                                binding.txtNoCoachFound.visibility = View.GONE
                                binding.recyclerViewCoaches.apply {
                                    layoutManager = LinearLayoutManager(
                                        mActivity!!,
                                        RecyclerView.VERTICAL,
                                        false
                                    )
                                    adapter = CoachListAdapter(
                                        mActivity!!,
                                        coachesList
                                    )
                                }
                            } else {
                                binding.recyclerViewCoaches.visibility = View.GONE
                                binding.txtNoCoachFound.visibility = View.VISIBLE
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
                                getCoachesList()
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
         * @return A new instance of fragment CoachesListFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CoachesListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_CoachesList"
    }
}