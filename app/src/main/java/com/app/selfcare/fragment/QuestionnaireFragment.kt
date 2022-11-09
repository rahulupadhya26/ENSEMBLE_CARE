package com.app.selfcare.fragment

import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.app.selfcare.R
import com.app.selfcare.adapters.OptionListAdapter
import com.app.selfcare.data.*
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper.get
import com.app.selfcare.preference.PreferenceHelper.set
import com.app.selfcare.utils.Utils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_questionnaire.*
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.HttpException
import java.io.*
import java.lang.reflect.Type


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [QuestionnaireFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class QuestionnaireFragment : BaseFragment(), View.OnClickListener {
    // TODO: Rename and change types of parameters
    private var therapy: String? = null
    private var param2: String? = null

    private var mSelectedOptionId: Int = -1
    private var mAnsweredQuesitonId: Int = -1
    private var questions: ArrayList<Question>? = null
    private var options = ArrayList<OptionModel>()
    private var question: Question? = null
    private var noOfAnswers: Int = 0
    private var count: Int = 0
    private var setAnswers: ArrayList<EachAnswer> = ArrayList()
    private var nextOptionId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            therapy = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_questionnaire
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        questions = ArrayList()
        progressBar.max = 0

        therapy = preference!![PrefKeys.PREF_SELECTED_THERAPY, ""]!!
        if (preference!![PrefKeys.PREF_NEXT_QUESTION_ID, ""]!!.isNotEmpty()) {
            val readDataFromFile = readFromFile()
            val type: Type = object : TypeToken<ArrayList<Question?>?>() {}.type
            questions = Gson().fromJson(readDataFromFile, type)
            if (questions!!.isNotEmpty()) {
                setFirstQuestion(questions!![0])
            } else {
                displayToast("Questions are not available")
            }
        } else {
            getAllQuestions()
        }

        cv_option_one.setOnClickListener(this)
        cv_option_two.setOnClickListener(this)
        cv_option_three.setOnClickListener(this)
        cv_option_four.setOnClickListener(this)
        cardViewBtnNext.setOnClickListener(this)

        /*val jsonString = Constants.getJson(requireActivity())
        jsonArr = JSONArray(jsonString)
        val type: Type = object : TypeToken<ArrayList<Question?>?>() {}.type
        questions = Gson().fromJson(jsonString, type)*/
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setQuestion()
        }*/
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun getAllQuestions() {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .getAllQuestionnaire(therapy!!)
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
                            if (status == "208") {
                                preference!![PrefKeys.PREF_STEP] = Utils.QUESTIONNAIRE
                                replaceFragmentNoBackStack(
                                    RegistrationFragment(),
                                    R.id.layout_home,
                                    RegistrationFragment.TAG
                                )
                            } else {
                                storeAllQuestion(responseBody)
                                val readDataFromFile = readFromFile()
                                val type: Type = object : TypeToken<ArrayList<Question?>?>() {}.type
                                questions = Gson().fromJson(readDataFromFile, type)
                                if (questions!!.isNotEmpty()) {
                                    setFirstQuestion(questions!![0])
                                } else {
                                    displayToast("Questions are not available")
                                }
                            }
                        } catch (e: Exception) {
                            hideProgress()
                            displayToast("Something went wrong.. Please try after sometime")
                        }

                    }, { error ->
                        hideProgress()
                        //displayToast("Error ${error.localizedMessage}")
                        if ((error as HttpException).code() == 400) {
                            displayErrorMsg(error)
                        } else {
                            displayToast("Something went wrong.. Please try after sometime")
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun getQuestions() {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .getQuestionnaire(therapy!!, preference!![PrefKeys.PREF_DEVICE_ID, ""]!!)
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
                            if (status == "208") {
                                preference!![PrefKeys.PREF_STEP] = Utils.QUESTIONNAIRE
                                replaceFragmentNoBackStack(
                                    RegistrationFragment(),
                                    R.id.layout_home,
                                    RegistrationFragment.TAG
                                )
                            } else {
                                //setQuestion(responseBody)
                            }
                        } catch (e: Exception) {
                            hideProgress()
                            displayToast("Something went wrong.. Please try after sometime")
                        }

                    }, { error ->
                        hideProgress()
                        //displayToast("Error ${error.localizedMessage}")
                        if ((error as HttpException).code() == 400) {
                            displayErrorMsg(error)
                        } else {
                            displayToast("Something went wrong.. Please try after sometime")
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun sendAnswers() {
        showProgress()
        val type: Type = object : TypeToken<ArrayList<EachAnswer?>?>() {}.type
        setAnswers = Gson().fromJson(preference!![PrefKeys.PREF_SET_ANSWER, ""], type)
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .sendAllAnswers(SendAnswer(preference!![PrefKeys.PREF_ID, 0]!!, setAnswers))
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
                                preference!![PrefKeys.PREF_STEP] = Utils.QUESTIONNAIRE
                                replaceFragmentNoBackStack(
                                    CongratsUserFragment(),
                                    R.id.layout_home,
                                    CongratsUserFragment.TAG
                                )
                            } else {
                                //setQuestion(responseBody)
                            }
                        } catch (error: Exception) {
                            hideProgress()
                            displayToast("Something went wrong.. Please try after sometime")
                        }
                    }, { error ->
                        hideProgress()
                        if ((error as HttpException).code() == 400) {
                            displayErrorMsg(error)
                        } else {
                            displayToast("Something went wrong.. Please try after sometime")
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setFirstQuestion(question: Question) {
        this.question = question
        progressBar.max = 2
        progressBar.progress = count
        defaultOptionsView()
        tv_current_question.text = question.question
        tv_next_question.text = ""
        tv_start_text.text = "Let's get started.."
        noOfAnswers = question.answers.size
        when (noOfAnswers) {
            1 -> {
                mSelectedOptionId = 0
                rv_option_list.visibility = View.VISIBLE
                layout_options.visibility = View.GONE
                val mOptionList = question.answers[0].answer.split(",").toTypedArray()
                rv_option_list.layoutManager = GridLayoutManager(
                    requireActivity(),
                    3
                )
                rv_option_list.adapter = OptionListAdapter(
                    requireActivity(),
                    getListData(mOptionList)
                )
            }
            2 -> {
                rv_option_list.visibility = View.GONE
                layout_options.visibility = View.VISIBLE
                tv_option_one.text = question.answers[0].answer
                tv_option_two.text = question.answers[1].answer
                cv_option_three.visibility = View.GONE
                cv_option_four.visibility = View.GONE
            }
            3 -> {
                rv_option_list.visibility = View.GONE
                layout_options.visibility = View.VISIBLE
                tv_option_one.text = question.answers[0].answer
                tv_option_two.text = question.answers[1].answer
                tv_option_three.text = question.answers[2].answer
                tv_option_three.visibility = View.VISIBLE
                cv_option_four.visibility = View.GONE
            }
            4 -> {
                rv_option_list.visibility = View.GONE
                layout_options.visibility = View.VISIBLE
                tv_option_one.text = question.answers[0].answer
                tv_option_two.text = question.answers[1].answer
                tv_option_three.text = question.answers[2].answer
                tv_option_four.text = question.answers[3].answer
                cv_option_three.visibility = View.VISIBLE
                cv_option_four.visibility = View.VISIBLE
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setQuestion(questions: ArrayList<Question>) {
        for (i in 0 until questions.size) {
            if (questions[i].option_id == nextOptionId) {
                this.question = questions[i]
                break
            }
        }
        progressBar.max = questions.size - 1
        progressBar.progress = count
        defaultOptionsView()
        tv_current_question.text = question!!.question
        tv_next_question.text = ""
        when (count) {
            1 -> {
                tv_start_text.text = "Keep going.."
            }
            2 -> {
                tv_start_text.text = "Bit more.."
            }
            3 -> {
                tv_start_text.text = "Little more.."
            }
            4 -> {
                tv_start_text.text = "You're doing great.."
            }
            5 -> {
                tv_start_text.text = "You're doing great.."
            }
            else -> {
                tv_start_text.text = "Few more.."
            }
        }
        if (question!!.next != null && question!!.next.isEmpty()) {
            tv_start_text.text = "And we are done..."
        }
        Log.i("Total count", (questions.size - 1).toString())
        Log.i("Count", count.toString())
        noOfAnswers = question!!.answers.size
        when (noOfAnswers) {
            1 -> {
                mSelectedOptionId = 0
                rv_option_list.visibility = View.VISIBLE
                layout_options.visibility = View.GONE
                val mOptionList = question!!.answers[0].answer.split(",").toTypedArray()
                rv_option_list.layoutManager = GridLayoutManager(
                    requireActivity(),
                    3
                )
                rv_option_list.adapter = OptionListAdapter(
                    requireActivity(),
                    getListData(mOptionList)
                )
            }
            2 -> {
                rv_option_list.visibility = View.GONE
                layout_options.visibility = View.VISIBLE
                tv_option_one.text = question!!.answers[0].answer
                tv_option_two.text = question!!.answers[1].answer
                cv_option_three.visibility = View.GONE
                cv_option_four.visibility = View.GONE
            }
            3 -> {
                rv_option_list.visibility = View.GONE
                layout_options.visibility = View.VISIBLE
                tv_option_one.text = question!!.answers[0].answer
                tv_option_two.text = question!!.answers[1].answer
                tv_option_three.text = question!!.answers[2].answer
                tv_option_three.visibility = View.VISIBLE
                cv_option_four.visibility = View.GONE
            }
            4 -> {
                rv_option_list.visibility = View.GONE
                layout_options.visibility = View.VISIBLE
                tv_option_one.text = question!!.answers[0].answer
                tv_option_two.text = question!!.answers[1].answer
                tv_option_three.text = question!!.answers[2].answer
                tv_option_four.text = question!!.answers[3].answer
                cv_option_three.visibility = View.VISIBLE
                cv_option_four.visibility = View.VISIBLE
            }
        }
        /*if (count == tempQuestions!!.size) {
            tempQuestions = ArrayList()
            count = 0
            for (question in questions!!) {
                if (question.id.toInt() == mCurrentPosition) {
                    tempQuestions!!.add(question)
                }
            }
            progressBar.max = progressBar.max + tempQuestions!!.size
        }
        question = tempQuestions!![count]*/
        /*if (tempQuestions!!.size > 2) {
            if (count == tempQuestions!!.size - 1) {
                tv_next_question.text = ""
                tv_start_text.text = "And we are done..."
            } else {
                tv_start_text.text = "Few more..."
                tv_next_question.text = tempQuestions!![count + 1].question
            }
        }*/
        /*when (question!!.no_of_options) {
            "1" -> {
                mSelectedOptionId = 0
                rv_option_list.visibility = View.VISIBLE
                layout_options.visibility = View.GONE
                val mOptionList = question!!.option_1.answer!!.split(",").toTypedArray()
                rv_option_list.layoutManager = GridLayoutManager(
                    requireActivity(),
                    3
                )
                rv_option_list.adapter = OptionListAdapter(
                    requireActivity(),
                    getListData(mOptionList)
                )
            }
            "2" -> {
                rv_option_list.visibility = View.GONE
                layout_options.visibility = View.VISIBLE
                tv_option_one.text = question!!.option_1.answer
                tv_option_two.text = question!!.option_2.answer
                tv_option_three.visibility = View.GONE
                tv_option_four.visibility = View.GONE
            }
            "3" -> {
                rv_option_list.visibility = View.GONE
                layout_options.visibility = View.VISIBLE
                tv_option_one.text = question!!.option_1.answer
                tv_option_two.text = question!!.option_2.answer
                tv_option_three.text = question!!.option_3.answer
                tv_option_three.visibility = View.VISIBLE
                tv_option_four.visibility = View.GONE
            }
            "4" -> {
                rv_option_list.visibility = View.GONE
                layout_options.visibility = View.VISIBLE
                tv_option_one.text = question!!.option_1.answer
                tv_option_two.text = question!!.option_2.answer
                tv_option_three.text = question!!.option_3.answer
                tv_option_four.text = question!!.option_4.answer
                tv_option_three.visibility = View.VISIBLE
                tv_option_four.visibility = View.VISIBLE
            }
        }*/
    }

    private fun getListData(optionList: Array<String>): ArrayList<OptionModel> {
        options = ArrayList()
        for (str in optionList) {
            options.add(OptionModel(str, false))
        }
        options[0].isSelected = true
        return options
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun defaultOptionsView() {
        val options = ArrayList<TextView>()
        options.add(0, tv_option_one)
        options.add(1, tv_option_two)
        options.add(2, tv_option_three)
        options.add(3, tv_option_four)

        for (option in options) {
            option.setTextColor(requireActivity().getColor(R.color.secondary_text))
            option.typeface = Typeface.DEFAULT
            /*option.background =
                ContextCompat.getDrawable(requireActivity(), R.drawable.bg_box_border_grey)*/
        }

    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun selectedOptionsView(
        tv: TextView,
        selectedAnswerId: Int,
        answeredQuestionId: Int,
        selectedOptionId: String
    ) {
        defaultOptionsView()
        mSelectedOptionId = selectedAnswerId
        mAnsweredQuesitonId = answeredQuestionId
        tv.setTextColor(requireActivity().getColor(R.color.white))
        tv.setTypeface(tv.typeface, Typeface.BOLD)
        /*tv.background =
            ContextCompat.getDrawable(requireActivity(), R.drawable.bg_box_border_selected)*/
        count += 1
        nextOptionId = selectedOptionId
        preference!![PrefKeys.PREF_NEXT_QUESTION_ID] = nextOptionId
        if (preference!![PrefKeys.PREF_SET_ANSWER, ""]!!.isNotEmpty()) {
            val type: Type = object : TypeToken<ArrayList<EachAnswer?>?>() {}.type
            setAnswers = Gson().fromJson(preference!![PrefKeys.PREF_SET_ANSWER, ""]!!, type)
        }
        setAnswers.add(EachAnswer(answeredQuestionId, selectedAnswerId))
        preference!![PrefKeys.PREF_SET_ANSWER] = Gson().toJson(setAnswers)
        if (nextOptionId.isNotEmpty()) {
            setQuestion(questions!!)
        } else {
            sendAnswers()
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment QuestionnaireFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String = "") =
            QuestionnaireFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_Questionnaire"
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.cv_option_one -> {
                var nextQuestionId = ""
                if (question!!.answers[0].option_id != null) {
                    nextQuestionId = question!!.answers[0].option_id
                } else {
                    nextQuestionId = question!!.next
                }
                selectedOptionsView(
                    tv_option_one,
                    question!!.answers[0].answer_id,
                    question!!.question_id,
                    nextQuestionId
                )
            }
            R.id.cv_option_two -> {
                var nextQuestionId = ""
                if (question!!.answers[1].option_id != null) {
                    nextQuestionId = question!!.answers[1].option_id
                } else {
                    nextQuestionId = question!!.next
                }
                selectedOptionsView(
                    tv_option_two,
                    question!!.answers[1].answer_id,
                    question!!.question_id,
                    nextQuestionId
                )
            }
            R.id.cv_option_three -> {
                var nextQuestionId = ""
                if (question!!.answers[2].option_id != null) {
                    nextQuestionId = question!!.answers[2].option_id
                } else {
                    nextQuestionId = question!!.next
                }
                selectedOptionsView(
                    tv_option_three,
                    question!!.answers[2].answer_id,
                    question!!.question_id,
                    nextQuestionId
                )
            }
            R.id.cv_option_four -> {
                var nextQuestionId = ""
                if (question!!.answers[3].option_id != null) {
                    nextQuestionId = question!!.answers[3].option_id
                } else {
                    nextQuestionId = question!!.next
                }
                selectedOptionsView(
                    tv_option_four,
                    question!!.answers[3].answer_id,
                    question!!.question_id,
                    nextQuestionId
                )
            }
            R.id.cardViewBtnNext -> {
                if (mSelectedOptionId != 0) {
                    //sendAnswers(mAnsweredQuesitonId, mSelectedOptionId)
                } else {
                    displayToast("Please select the option")
                }
                /*if (mSelectedOptionPosition != -1) {
                    if (mSelectedOptionPosition != 0) {
                        mCurrentPosition = mSelectedOptionPosition
                    }
                    when {
                        count < tempQuestions!!.size - 1 || mSelectedOptionPosition != 0 -> {
                            mSelectedOptionPosition = -1
                            count++
                            progressBar.progress = progressBar.progress + 1
                            setQuestion()
                        }
                        else -> {
                            replaceFragmentNoBackStack(
                                CongratsUserFragment(),
                                R.id.layout_home,
                                CongratsUserFragment.TAG
                            )
                            *//*Toast.makeText(
                                this,
                                "You have successfully completed the assessment",
                                Toast.LENGTH_SHORT
                            ).show()*//*
                        }
                    }
                } else {
                    Toast.makeText(
                        requireActivity(),
                        "Please select the option",
                        Toast.LENGTH_SHORT
                    ).show()
                }*/
            }
        }
    }

    private fun storeAllQuestion(response: String) {
        val file = File(requireActivity().filesDir, "Questionnaire")
        if (!file.exists()) {
            file.mkdir()
        }
        try {
            val questionFile = File(file, "questions.json")
            if (questionFile.exists()) {
                questionFile.delete()
                file.createNewFile()
            }
            val writer = FileWriter(questionFile)
            writer.append(response)
            writer.flush()
            writer.close()
        } catch (e: Exception) {
            e.printStackTrace()
            displayToast("Error while create and write to the file.")
        }
    }

    private fun readFromFile(): String {
        val stringBuilder = StringBuilder()
        try {
            val file = File(requireActivity().filesDir, "Questionnaire")
            if (file.exists()) {
                val questionFile = File(file, "questions.json")
                val fileReader = FileReader(questionFile)
                val bufferedReader = BufferedReader(fileReader)
                var line = bufferedReader.readLine()
                while (line != null) {
                    stringBuilder.append(line).append("\n")
                    line = bufferedReader.readLine()
                }
                bufferedReader.close()
            } else {
                displayToast("Cannot read the file.")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            displayToast("Error while reading the file.")
        }
        return stringBuilder.toString()
    }
}