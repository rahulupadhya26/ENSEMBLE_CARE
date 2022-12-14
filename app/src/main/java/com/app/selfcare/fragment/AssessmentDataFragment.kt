package com.app.selfcare.fragment

import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.app.selfcare.R
import com.app.selfcare.data.*
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper.get
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_assessment_data.*
import kotlinx.android.synthetic.main.fragment_my_assessments.*
import org.json.JSONObject
import org.json.JSONStringer
import retrofit2.HttpException
import java.lang.reflect.Type
import kotlin.collections.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AssessmentDataFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AssessmentDataFragment : BaseFragment(), View.OnClickListener {
    // TODO: Rename and change types of parameters
    private var assessment: Assessments? = null
    private var type: String? = null
    private var assessmentQuestions: ArrayList<AssessmentQuestion> = arrayListOf()
    private var assessmentAnswers: ArrayList<AssessmentAnswer> = arrayListOf()
    private var count: Int = 0
    private var submitAssessment = JSONObject()
    private var selectedAnswerValueSum: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            assessment = it.getParcelable(ARG_PARAM1)
            type = it.getString(ARG_PARAM2)
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_assessment_data
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE

        tvAssessmentOptionOne.setOnClickListener(this)
        tvAssessmentOptionTwo.setOnClickListener(this)
        tvAssessmentOptionThree.setOnClickListener(this)
        tvAssessmentOptionFour.setOnClickListener(this)
        tvAssessmentOptionFive.setOnClickListener(this)

        submitAssessment.put("patient", preference!![PrefKeys.PREF_PATIENT_ID, ""]!!.toInt())
        submitAssessment.put("assign_assessment", assessment!!.id)
        getAssessmentData()
    }

    private fun getAssessmentData() {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .getAssessmentData(type!!, getAccessToken())
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
                            val jsonObj = JSONObject(responseBody)
                            val assessmentQuestionList: Type =
                                object : TypeToken<ArrayList<AssessmentQuestion?>?>() {}.type
                            val assessmentAnswerList: Type =
                                object : TypeToken<ArrayList<AssessmentAnswer?>?>() {}.type
                            assessmentQuestions = Gson().fromJson(
                                jsonObj.getString("questions"),
                                assessmentQuestionList
                            )
                            assessmentAnswers =
                                Gson().fromJson(jsonObj.getString("answers"), assessmentAnswerList)
                            setQuestionAnswer()
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
                                getAssessmentData()
                            }
                        } else {
                            displayAfterLoginErrorMsg(error)
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    private fun setQuestionAnswer() {
        val questionCount = assessmentQuestions.size
        val answerCount = assessmentAnswers.size
        assessmentProgressBar.max = questionCount
        assessmentProgressBar.progress = count
        defaultOptionsView()
        tvCurrentAssessmentQuestion.text = assessmentQuestions[count].question
        when (answerCount) {
            2 -> {
                tvAssessmentOptionOne.text = assessmentAnswers[0].answer
                tvAssessmentOptionTwo.text = assessmentAnswers[1].answer
                tvAssessmentOptionThree.visibility = View.GONE
                tvAssessmentOptionFour.visibility = View.GONE
                tvAssessmentOptionFive.visibility = View.GONE
            }
            3 -> {
                tvAssessmentOptionOne.text = assessmentAnswers[0].answer
                tvAssessmentOptionTwo.text = assessmentAnswers[1].answer
                tvAssessmentOptionThree.text = assessmentAnswers[2].answer
                tvAssessmentOptionThree.visibility = View.VISIBLE
                tvAssessmentOptionFour.visibility = View.GONE
                tvAssessmentOptionFive.visibility = View.GONE
            }
            4 -> {
                tvAssessmentOptionOne.text = assessmentAnswers[0].answer
                tvAssessmentOptionTwo.text = assessmentAnswers[1].answer
                tvAssessmentOptionThree.text = assessmentAnswers[2].answer
                tvAssessmentOptionFour.text = assessmentAnswers[3].answer
                tvAssessmentOptionThree.visibility = View.VISIBLE
                tvAssessmentOptionFour.visibility = View.VISIBLE
                tvAssessmentOptionFive.visibility = View.GONE
            }
            5 -> {
                tvAssessmentOptionOne.text = assessmentAnswers[0].answer
                tvAssessmentOptionTwo.text = assessmentAnswers[1].answer
                tvAssessmentOptionThree.text = assessmentAnswers[2].answer
                tvAssessmentOptionFour.text = assessmentAnswers[3].answer
                tvAssessmentOptionFive.text = assessmentAnswers[4].answer
                tvAssessmentOptionThree.visibility = View.VISIBLE
                tvAssessmentOptionFour.visibility = View.VISIBLE
                tvAssessmentOptionFive.visibility = View.VISIBLE
            }
        }

        when (count) {
            0 -> {
                tvStartText.text = "Let's get started.."
            }
            1 -> {
                tvStartText.text = "Keep going.."
            }
            2 -> {
                tvStartText.text = "Bit more.."
            }
            3 -> {
                tvStartText.text = "Little more.."
            }
            4 -> {
                tvStartText.text = "You're doing great.."
            }
            else -> {
                tvStartText.text = "Few more.."
            }
        }
        if (count == assessmentQuestions.size - 1) {
            tvStartText.text = "And we are done..."
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun defaultOptionsView() {
        val options = ArrayList<TextView>()
        options.add(0, tvAssessmentOptionOne)
        options.add(1, tvAssessmentOptionTwo)
        options.add(2, tvAssessmentOptionThree)
        options.add(3, tvAssessmentOptionFour)
        options.add(4, tvAssessmentOptionFive)

        for (option in options) {
            option.setTextColor(requireActivity().getColor(R.color.secondary_text))
            option.typeface = Typeface.DEFAULT
            option.background =
                ContextCompat.getDrawable(requireActivity(), R.drawable.bg_box_border_grey)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun selectedOptionsView(
        tv: TextView,
        selectedAnswerId: Int,
        answeredQuestionId: Int,
        selectedAnswerValue: Int
    ) {
        defaultOptionsView()
        tv.setTextColor(requireActivity().getColor(R.color.white))
        tv.setTypeface(tv.typeface, Typeface.BOLD)
        tv.background =
            ContextCompat.getDrawable(requireActivity(), R.drawable.bg_box_border_selected)
        count += 1
        submitAssessment.put("question$count", answeredQuestionId)
        submitAssessment.put("answer$count", selectedAnswerId)
        selectedAnswerValueSum += selectedAnswerValue
        if (count < assessmentQuestions.size) {
            setQuestionAnswer()
        } else {
            submitAssessment.put("patient_score", selectedAnswerValueSum)
            sendAnswers()
        }
    }

    private fun sendAnswers() {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .sendAssessmentData(type!!, submitAssessment.toString(), getAccessToken())
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
                            if (status == "200") {
                                popBackStack()
                            } else {
                                //setQuestion(responseBody)
                            }
                        } catch (e: Exception) {
                            hideProgress()
                            displayToast("Something went wrong.. Please try after sometime")
                        }
                    }, { error ->
                        hideProgress()
                        displayToast("Error ${error.localizedMessage}")
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
         * @return A new instance of fragment AssessmentDataFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(assessment: Assessments, param2: String) =
            AssessmentDataFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM1, assessment)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_assessments_data"
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.tvAssessmentOptionOne -> {
                selectedOptionsView(
                    tvAssessmentOptionOne,
                    assessmentAnswers[0].id,
                    assessmentQuestions[count].id,
                    assessmentAnswers[0].value
                )
            }
            R.id.tvAssessmentOptionTwo -> {
                selectedOptionsView(
                    tvAssessmentOptionTwo,
                    assessmentAnswers[1].id,
                    assessmentQuestions[count].id,
                    assessmentAnswers[1].value
                )
            }
            R.id.tvAssessmentOptionThree -> {
                selectedOptionsView(
                    tvAssessmentOptionThree,
                    assessmentAnswers[2].id,
                    assessmentQuestions[count].id,
                    assessmentAnswers[2].value
                )
            }
            R.id.tvAssessmentOptionFour -> {
                selectedOptionsView(
                    tvAssessmentOptionFour,
                    assessmentAnswers[3].id,
                    assessmentQuestions[count].id,
                    assessmentAnswers[3].value
                )
            }
            R.id.tvAssessmentOptionFive -> {
                selectedOptionsView(
                    tvAssessmentOptionFive,
                    assessmentAnswers[4].id,
                    assessmentQuestions[count].id,
                    assessmentAnswers[4].value
                )
            }
        }
    }
}