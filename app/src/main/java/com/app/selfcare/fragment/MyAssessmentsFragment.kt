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
import com.app.selfcare.adapters.AssessmentListAdapter
import com.app.selfcare.adapters.JournalListAdapter
import com.app.selfcare.controller.OnAssessmentItemClickListener
import com.app.selfcare.data.Assessments
import com.app.selfcare.data.Journal
import com.app.selfcare.data.PatientId
import com.app.selfcare.databinding.FragmentActivityCarePlanBinding
import com.app.selfcare.databinding.FragmentMyAssessmentsBinding
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
 * Use the [MyAssessmentsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MyAssessmentsFragment : BaseFragment(), OnAssessmentItemClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentMyAssessmentsBinding

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
        binding = FragmentMyAssessmentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getLayout(): Int {
        return R.layout.fragment_my_assessments
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.VISIBLE
        getSubTitle().visibility = View.GONE

        getMyAssessments()
    }

    private fun getMyAssessments() {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .getAssessments(getAccessToken())
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
                            var assessmentLists: ArrayList<Assessments> = arrayListOf()
                            val assessmentList: Type =
                                object : TypeToken<ArrayList<Assessments?>?>() {}.type
                            assessmentLists = Gson().fromJson(responseBody, assessmentList)
                            if (assessmentLists.isNotEmpty()) {
                                binding.recyclerViewAssessments.visibility = View.VISIBLE
                                binding.txtNoAssessments.visibility = View.GONE
                                binding.recyclerViewAssessments.apply {
                                    layoutManager = LinearLayoutManager(
                                        mActivity!!,
                                        RecyclerView.VERTICAL,
                                        false
                                    )
                                    adapter = AssessmentListAdapter(
                                        mActivity!!,
                                        assessmentLists, this@MyAssessmentsFragment
                                    )
                                }
                            } else {
                                binding.recyclerViewAssessments.visibility = View.GONE
                                binding.txtNoAssessments.visibility = View.VISIBLE
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
                                getMyAssessments()
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
         * @return A new instance of fragment MyAssessmentsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MyAssessmentsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_assessments"
    }

    override fun onAssessmentItemClickListener(
        assessment: Assessments,
        type: String,
        isView: Boolean
    ) {
        if (isView) {
            replaceFragment(
                ViewAssessmentFragment.newInstance(assessment, type),
                R.id.layout_home,
                ViewAssessmentFragment.TAG
            )
        } else {
            replaceFragment(
                AssessmentDataFragment.newInstance(assessment, type),
                R.id.layout_home,
                AssessmentDataFragment.TAG
            )
        }
    }
}