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
import com.app.selfcare.adapters.ViewAssessmentListAdapter
import com.app.selfcare.data.Assessments
import com.app.selfcare.data.ViewAssessment
import com.app.selfcare.databinding.FragmentActivityCarePlanBinding
import com.app.selfcare.databinding.FragmentViewAssessmentBinding
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper.get
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ViewAssessmentFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ViewAssessmentFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var assessment: Assessments? = null
    private var param2: String? = null
    private lateinit var binding: FragmentViewAssessmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            assessment = it.getParcelable(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentViewAssessmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getLayout(): Int {
        return R.layout.fragment_view_assessment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.VISIBLE
        getSubTitle().visibility = View.GONE

        binding.txtViewAssessmentType.text = assessment!!.type_of_assessment
        viewAssessment()
    }

    private fun viewAssessment() {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .viewAssessment(assessment!!.id, getAccessToken())
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
                            val viewAssessment =
                                Gson().fromJson(responseBody, ViewAssessment::class.java)
                            binding.txtScore.text =
                                viewAssessment.patient_score + "/" + viewAssessment.Total_score
                            binding.recyclerViewCompletedAssessment.apply {
                                layoutManager = LinearLayoutManager(
                                    mActivity!!,
                                    RecyclerView.VERTICAL,
                                    false
                                )
                                adapter =
                                    ViewAssessmentListAdapter(viewAssessment.questions_answers)
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
                                viewAssessment()
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
         * @return A new instance of fragment ViewAssessmentFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(assessment: Assessments, param2: String) =
            ViewAssessmentFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM1, assessment)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_view_assessments"
    }
}