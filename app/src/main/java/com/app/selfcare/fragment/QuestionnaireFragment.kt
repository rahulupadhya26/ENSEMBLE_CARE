package com.app.selfcare.fragment

import android.app.Dialog
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.app.selfcare.R
import com.app.selfcare.adapters.OptionListAdapter
import com.app.selfcare.data.*
import com.app.selfcare.databinding.FragmentQuestionnaireBinding
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper.get
import com.app.selfcare.preference.PreferenceHelper.set
import com.app.selfcare.utils.DateUtils
import com.app.selfcare.utils.Utils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import java.io.*
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


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

    private var mSelectedOptionId: ArrayList<Int> = arrayListOf()
    private var mAnsweredQuesitonId: Int = -1
    private var questions: ArrayList<Question>? = null
    private var options = ArrayList<OptionModel>()
    private var question: Question? = null
    private var noOfAnswers: Int = 0
    private var count: Int = 1
    private var setAnswers: ArrayList<EachAnswer> = ArrayList()
    private var nextOptionId: String = ""
    private var multipleSelectionCount = 0
    private lateinit var binding: FragmentQuestionnaireBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            therapy = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentQuestionnaireBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getLayout(): Int {
        return R.layout.fragment_questionnaire
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateStatusBarColor(R.color.initial_screen_background)
        questions = ArrayList()
        binding.progressBar.max = 0
        setAnswers = arrayListOf()
        binding.layoutOptions.visibility = View.GONE
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

        binding.cvOptionOne.setOnClickListener(this)
        binding.cvOptionTwo.setOnClickListener(this)
        binding.cvOptionThree.setOnClickListener(this)
        binding.cvOptionFour.setOnClickListener(this)
        binding.cvOptionFive.setOnClickListener(this)
        binding.cvOptionSix.setOnClickListener(this)
        binding.cvOptionSeven.setOnClickListener(this)
        binding.cvOptionEight.setOnClickListener(this)
        binding.cvOptionNine.setOnClickListener(this)
        binding.cvOptionTen.setOnClickListener(this)
        binding.cvOptionEleven.setOnClickListener(this)
        binding.cvOptionTwelve.setOnClickListener(this)
        binding.cvOptionThirteen.setOnClickListener(this)
        binding.cvOptionFourteen.setOnClickListener(this)
        binding.cvOptionFifteen.setOnClickListener(this)
        binding.btnQuestionnaireContinue.setOnClickListener(this)
        binding.btnResourceContinue.setOnClickListener(this)

        /*val jsonString = Constants.getJson(requireActivity())
        jsonArr = JSONArray(jsonString)
        val type: Type = object : TypeToken<ArrayList<Question?>?>() {}.type
        questions = Gson().fromJson(jsonString, type)*/
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setQuestion()
        }*/
        
        binding.txtCall911.movementMethod = LinkMovementMethod.getInstance()
        binding.txtSuicideCrisisLifeLine.movementMethod = LinkMovementMethod.getInstance()
        binding.txtResourceCrisisTextLine.movementMethod = LinkMovementMethod.getInstance()
        binding.txtDisasterDistressHelpLine.movementMethod = LinkMovementMethod.getInstance()
        binding.txtTransLifeLine.movementMethod = LinkMovementMethod.getInstance()
        binding.txtResourceTrevorProject.movementMethod = LinkMovementMethod.getInstance()
        binding.txtResourceNationalViolence.movementMethod = LinkMovementMethod.getInstance()
        binding.txtResourceNationalChildAbuse.movementMethod = LinkMovementMethod.getInstance()
        binding.txtResourceNationalSexualAssault.movementMethod = LinkMovementMethod.getInstance()
        binding.txtResourceElderDisability.movementMethod = LinkMovementMethod.getInstance()
        binding.txtResourceVeteranCrisis.movementMethod = LinkMovementMethod.getInstance()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun getAllQuestions() {
        showProgress()
        binding.layoutOptions.visibility = View.GONE
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .getAllQuestionnaire("True")
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
                                preference!![PrefKeys.PREF_SET_ANSWER] = ""
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
                            e.printStackTrace()
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
                    .sendAllAnswers(
                        "True",
                        SendAnswer(preference!![PrefKeys.PREF_ID, 0]!!, setAnswers)
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
        setMaxProgress(count)
        //setCurrentProgress(count)
        binding.progressBar.max = 2
        binding.progressBar.progress = count
        defaultOptionsView()
        binding.tvCurrentQuestion.text = question.question
        //tv_start_text.text = "Let's get started.."
        noOfAnswers = question.options.size
        if (question.is_multiple) {
            binding.tvCheckApply.visibility = View.VISIBLE
            binding.btnQuestionnaireContinue.visibility = View.VISIBLE
        } else {
            binding.tvCheckApply.visibility = View.GONE
            binding.btnQuestionnaireContinue.visibility = View.GONE
        }
        when (noOfAnswers) {
            1 -> {
                mSelectedOptionId = arrayListOf()
                binding.rvOptionList.visibility = View.VISIBLE
                binding.layoutOptions.visibility = View.GONE
                val mOptionList = question.options[0].data.split(",").toTypedArray()
                binding.rvOptionList.layoutManager = GridLayoutManager(
                    requireActivity(),
                    3
                )
                binding.rvOptionList.adapter = OptionListAdapter(
                    requireActivity(),
                    getListData(mOptionList)
                )
            }
            2 -> {
                binding.rvOptionList.visibility = View.GONE
                binding.layoutOptions.visibility= View.VISIBLE
                binding.tvOptionOne.text = question.options[0].data
                binding.tvOptionTwo.text = question.options[1].data
                binding.cvOptionThree.visibility = View.GONE
                binding.cvOptionFour.visibility = View.GONE
                binding.cvOptionFive.visibility = View.GONE
                binding.cvOptionSix.visibility = View.GONE
                binding.cvOptionSeven.visibility = View.GONE
                binding.cvOptionEight.visibility = View.GONE
                binding.cvOptionNine.visibility = View.GONE
                binding.cvOptionTen.visibility = View.GONE
                binding.cvOptionEleven.visibility = View.GONE
                binding.cvOptionTwelve.visibility = View.GONE
                binding.cvOptionThirteen.visibility = View.GONE
                binding.cvOptionFourteen.visibility = View.GONE
                binding.cvOptionFifteen.visibility = View.GONE
            }
            3 -> {
                binding.rvOptionList.visibility = View.GONE
                binding.layoutOptions.visibility = View.VISIBLE
                binding.tvOptionOne.text = question.options[0].data
                binding.tvOptionTwo.text = question.options[1].data
                binding.tvOptionThree.text = question.options[2].data
                binding.tvOptionThree.visibility = View.VISIBLE
                binding.cvOptionFour.visibility = View.GONE
                binding.cvOptionFive.visibility = View.GONE
                binding.cvOptionSix.visibility = View.GONE
                binding.cvOptionSeven.visibility = View.GONE
                binding.cvOptionEight.visibility = View.GONE
                binding.cvOptionNine.visibility = View.GONE
                binding.cvOptionTen.visibility = View.GONE
                binding.cvOptionEleven.visibility = View.GONE
                binding.cvOptionTwelve.visibility = View.GONE
                binding.cvOptionThirteen.visibility = View.GONE
                binding.cvOptionFourteen.visibility = View.GONE
                binding.cvOptionFifteen.visibility = View.GONE
            }
            4 -> {
                binding.rvOptionList.visibility = View.GONE
                binding.layoutOptions.visibility = View.VISIBLE
                binding.tvOptionOne.text = question.options[0].data
                binding.tvOptionTwo.text = question.options[1].data
                binding.tvOptionThree.text = question.options[2].data
                binding.tvOptionFour.text = question.options[3].data
                binding.cvOptionThree.visibility = View.VISIBLE
                binding.cvOptionFour.visibility = View.VISIBLE
                binding.cvOptionFive.visibility = View.GONE
                binding.cvOptionSix.visibility = View.GONE
                binding.cvOptionSeven.visibility = View.GONE
                binding.cvOptionEight.visibility = View.GONE
                binding.cvOptionNine.visibility = View.GONE
                binding.cvOptionTen.visibility = View.GONE
                binding.cvOptionEleven.visibility = View.GONE
                binding.cvOptionTwelve.visibility = View.GONE
                binding.cvOptionThirteen.visibility = View.GONE
                binding.cvOptionFourteen.visibility = View.GONE
                binding.cvOptionFifteen.visibility = View.GONE
            }
            5 -> {
                binding.rvOptionList.visibility = View.GONE
                binding.layoutOptions.visibility = View.VISIBLE
                binding.tvOptionOne.text = question.options[0].data
                binding.tvOptionTwo.text = question.options[1].data
                binding.tvOptionThree.text = question.options[2].data
                binding.tvOptionFour.text = question.options[3].data
                binding.tvOptionFive.text = question.options[4].data
                binding.cvOptionThree.visibility = View.VISIBLE
                binding.cvOptionFour.visibility = View.VISIBLE
                binding.cvOptionFive.visibility = View.VISIBLE
                binding.cvOptionSix.visibility = View.GONE
                binding.cvOptionSeven.visibility = View.GONE
                binding.cvOptionEight.visibility = View.GONE
                binding.cvOptionNine.visibility = View.GONE
                binding.cvOptionTen.visibility = View.GONE
                binding.cvOptionEleven.visibility = View.GONE
                binding.cvOptionTwelve.visibility = View.GONE
                binding.cvOptionThirteen.visibility = View.GONE
                binding.cvOptionFourteen.visibility = View.GONE
                binding.cvOptionFifteen.visibility = View.GONE
            }
            6 -> {
                binding.rvOptionList.visibility = View.GONE
                binding.layoutOptions.visibility = View.VISIBLE
                binding.tvOptionOne.text = question.options[0].data
                binding.tvOptionTwo.text = question.options[1].data
                binding.tvOptionThree.text = question.options[2].data
                binding.tvOptionFour.text = question.options[3].data
                binding.tvOptionFive.text = question.options[4].data
                binding.tvOptionSix.text = question.options[5].data
                binding.cvOptionThree.visibility = View.VISIBLE
                binding.cvOptionFour.visibility = View.VISIBLE
                binding.cvOptionFive.visibility = View.VISIBLE
                binding.cvOptionSix.visibility = View.VISIBLE
                binding.cvOptionSeven.visibility = View.GONE
                binding.cvOptionEight.visibility = View.GONE
                binding.cvOptionNine.visibility = View.GONE
                binding.cvOptionTen.visibility = View.GONE
                binding.cvOptionEleven.visibility = View.GONE
                binding.cvOptionTwelve.visibility = View.GONE
                binding.cvOptionThirteen.visibility = View.GONE
                binding.cvOptionFourteen.visibility = View.GONE
                binding.cvOptionFifteen.visibility = View.GONE
            }
            7 -> {
                binding.rvOptionList.visibility = View.GONE
                binding.layoutOptions.visibility = View.VISIBLE
                binding.tvOptionOne.text = question.options[0].data
                binding.tvOptionTwo.text = question.options[1].data
                binding.tvOptionThree.text = question.options[2].data
                binding.tvOptionFour.text = question.options[3].data
                binding.tvOptionFive.text = question.options[4].data
                binding.tvOptionSix.text = question.options[5].data
                binding.tvOptionSeven.text = question.options[6].data
                binding.cvOptionThree.visibility = View.VISIBLE
                binding.cvOptionFour.visibility = View.VISIBLE
                binding.cvOptionFive.visibility = View.VISIBLE
                binding.cvOptionSix.visibility = View.VISIBLE
                binding.cvOptionSeven.visibility = View.VISIBLE
                binding.cvOptionEight.visibility = View.GONE
                binding.cvOptionNine.visibility = View.GONE
                binding.cvOptionTen.visibility = View.GONE
                binding.cvOptionEleven.visibility = View.GONE
                binding.cvOptionTwelve.visibility = View.GONE
                binding.cvOptionThirteen.visibility = View.GONE
                binding.cvOptionFourteen.visibility = View.GONE
                binding.cvOptionFifteen.visibility = View.GONE
            }
            8 -> {
                binding.rvOptionList.visibility = View.GONE
                binding.layoutOptions.visibility = View.VISIBLE
                binding.tvOptionOne.text = question.options[0].data
                binding.tvOptionTwo.text = question.options[1].data
                binding.tvOptionThree.text = question.options[2].data
                binding.tvOptionFour.text = question.options[3].data
                binding.tvOptionFive.text = question.options[4].data
                binding.tvOptionSix.text = question.options[5].data
                binding.tvOptionSeven.text = question.options[6].data
                binding.tvOptionEight.text = question.options[7].data
                binding.cvOptionThree.visibility = View.VISIBLE
                binding.cvOptionFour.visibility = View.VISIBLE
                binding.cvOptionFive.visibility = View.VISIBLE
                binding.cvOptionSix.visibility = View.VISIBLE
                binding.cvOptionSeven.visibility = View.VISIBLE
                binding.cvOptionEight.visibility = View.VISIBLE
                binding.cvOptionNine.visibility = View.GONE
                binding.cvOptionTen.visibility = View.GONE
                binding.cvOptionEleven.visibility = View.GONE
                binding.cvOptionTwelve.visibility = View.GONE
                binding.cvOptionThirteen.visibility = View.GONE
                binding.cvOptionFourteen.visibility = View.GONE
                binding.cvOptionFifteen.visibility = View.GONE
            }
            9 -> {
                binding.rvOptionList.visibility = View.GONE
                binding.layoutOptions.visibility = View.VISIBLE
                binding.tvOptionOne.text = question.options[0].data
                binding.tvOptionTwo.text = question.options[1].data
                binding.tvOptionThree.text = question.options[2].data
                binding.tvOptionFour.text = question.options[3].data
                binding.tvOptionFive.text = question.options[4].data
                binding.tvOptionSix.text = question.options[5].data
                binding.tvOptionSeven.text = question.options[6].data
                binding.tvOptionEight.text = question.options[7].data
                binding.tvOptionNine.text = question.options[8].data
                binding.cvOptionThree.visibility = View.VISIBLE
                binding.cvOptionFour.visibility = View.VISIBLE
                binding.cvOptionFive.visibility = View.VISIBLE
                binding.cvOptionSix.visibility = View.VISIBLE
                binding.cvOptionSeven.visibility = View.VISIBLE
                binding.cvOptionEight.visibility = View.VISIBLE
                binding.cvOptionNine.visibility = View.VISIBLE
                binding.cvOptionTen.visibility = View.GONE
                binding.cvOptionEleven.visibility = View.GONE
                binding.cvOptionTwelve.visibility = View.GONE
                binding.cvOptionThirteen.visibility = View.GONE
                binding.cvOptionFourteen.visibility = View.GONE
                binding.cvOptionFifteen.visibility = View.GONE
            }
            10 -> {
                binding.rvOptionList.visibility = View.GONE
                binding.layoutOptions.visibility = View.VISIBLE
                binding.tvOptionOne.text = question.options[0].data
                binding.tvOptionTwo.text = question.options[1].data
                binding.tvOptionThree.text = question.options[2].data
                binding.tvOptionFour.text = question.options[3].data
                binding.tvOptionFive.text = question.options[4].data
                binding.tvOptionSix.text = question.options[5].data
                binding.tvOptionSeven.text = question.options[6].data
                binding.tvOptionEight.text = question.options[7].data
                binding.tvOptionNine.text = question.options[8].data
                binding.tvOptionTen.text = question.options[9].data
                binding.cvOptionThree.visibility = View.VISIBLE
                binding.cvOptionFour.visibility = View.VISIBLE
                binding.cvOptionFive.visibility = View.VISIBLE
                binding.cvOptionSix.visibility = View.VISIBLE
                binding.cvOptionSeven.visibility = View.VISIBLE
                binding.cvOptionEight.visibility = View.VISIBLE
                binding.cvOptionNine.visibility = View.VISIBLE
                binding.cvOptionTen.visibility = View.VISIBLE
                binding.cvOptionEleven.visibility = View.GONE
                binding.cvOptionTwelve.visibility = View.GONE
                binding.cvOptionThirteen.visibility = View.GONE
                binding.cvOptionFourteen.visibility = View.GONE
                binding.cvOptionFifteen.visibility = View.GONE
            }
            11 -> {
                binding.rvOptionList.visibility = View.GONE
                binding.layoutOptions.visibility = View.VISIBLE
                binding.tvOptionOne.text = question.options[0].data
                binding.tvOptionTwo.text = question.options[1].data
                binding.tvOptionThree.text = question.options[2].data
                binding.tvOptionFour.text = question.options[3].data
                binding.tvOptionFive.text = question.options[4].data
                binding.tvOptionSix.text = question.options[5].data
                binding.tvOptionSeven.text = question.options[6].data
                binding.tvOptionEight.text = question.options[7].data
                binding.tvOptionNine.text = question.options[8].data
                binding.tvOptionTen.text = question.options[9].data
                binding.tvOptionEleven.text = question.options[10].data
                binding.cvOptionThree.visibility = View.VISIBLE
                binding.cvOptionFour.visibility = View.VISIBLE
                binding.cvOptionFive.visibility = View.VISIBLE
                binding.cvOptionSix.visibility = View.VISIBLE
                binding.cvOptionSeven.visibility = View.VISIBLE
                binding.cvOptionEight.visibility = View.VISIBLE
                binding.cvOptionNine.visibility = View.VISIBLE
                binding.cvOptionTen.visibility = View.VISIBLE
                binding.cvOptionEleven.visibility = View.VISIBLE
                binding.cvOptionTwelve.visibility = View.GONE
                binding.cvOptionThirteen.visibility = View.GONE
                binding.cvOptionFourteen.visibility = View.GONE
                binding.cvOptionFifteen.visibility = View.GONE
            }
            12 -> {
                binding.rvOptionList.visibility = View.GONE
                binding.layoutOptions.visibility = View.VISIBLE
                binding.tvOptionOne.text = question.options[0].data
                binding.tvOptionTwo.text = question.options[1].data
                binding.tvOptionThree.text = question.options[2].data
                binding.tvOptionFour.text = question.options[3].data
                binding.tvOptionFive.text = question.options[4].data
                binding.tvOptionSix.text = question.options[5].data
                binding.tvOptionSeven.text = question.options[6].data
                binding.tvOptionEight.text = question.options[7].data
                binding.tvOptionNine.text = question.options[8].data
                binding.tvOptionTen.text = question.options[9].data
                binding.tvOptionEleven.text = question.options[10].data
                binding.tvOptionTwelve.text = question.options[11].data
                binding.cvOptionThree.visibility = View.VISIBLE
                binding.cvOptionFour.visibility = View.VISIBLE
                binding.cvOptionFive.visibility = View.VISIBLE
                binding.cvOptionSix.visibility = View.VISIBLE
                binding.cvOptionSeven.visibility = View.VISIBLE
                binding.cvOptionEight.visibility = View.VISIBLE
                binding.cvOptionNine.visibility = View.VISIBLE
                binding.cvOptionTen.visibility = View.VISIBLE
                binding.cvOptionEleven.visibility = View.VISIBLE
                binding.cvOptionTwelve.visibility = View.VISIBLE
                binding.cvOptionThirteen.visibility = View.GONE
                binding.cvOptionFourteen.visibility = View.GONE
                binding.cvOptionFifteen.visibility = View.GONE
            }
            13 -> {
                binding.rvOptionList.visibility = View.GONE
                binding.layoutOptions.visibility = View.VISIBLE
                binding.tvOptionOne.text = question.options[0].data
                binding.tvOptionTwo.text = question.options[1].data
                binding.tvOptionThree.text = question.options[2].data
                binding.tvOptionFour.text = question.options[3].data
                binding.tvOptionFive.text = question.options[4].data
                binding.tvOptionSix.text = question.options[5].data
                binding.tvOptionSeven.text = question.options[6].data
                binding.tvOptionEight.text = question.options[7].data
                binding.tvOptionNine.text = question.options[8].data
                binding.tvOptionTen.text = question.options[9].data
                binding.tvOptionEleven.text = question.options[10].data
                binding.tvOptionTwelve.text = question.options[11].data
                binding.tvOptionThirteen.text = question.options[12].data
                binding.cvOptionThree.visibility = View.VISIBLE
                binding.cvOptionFour.visibility = View.VISIBLE
                binding.cvOptionFive.visibility = View.VISIBLE
                binding.cvOptionSix.visibility = View.VISIBLE
                binding.cvOptionSeven.visibility = View.VISIBLE
                binding.cvOptionEight.visibility = View.VISIBLE
                binding.cvOptionNine.visibility = View.VISIBLE
                binding.cvOptionTen.visibility = View.VISIBLE
                binding.cvOptionEleven.visibility = View.VISIBLE
                binding.cvOptionTwelve.visibility = View.VISIBLE
                binding.cvOptionThirteen.visibility = View.VISIBLE
                binding.cvOptionFourteen.visibility = View.GONE
                binding.cvOptionFifteen.visibility = View.GONE
            }
            14 -> {
                binding.rvOptionList.visibility = View.GONE
                binding.layoutOptions.visibility = View.VISIBLE
                binding.tvOptionOne.text = question.options[0].data
                binding.tvOptionTwo.text = question.options[1].data
                binding.tvOptionThree.text = question.options[2].data
                binding.tvOptionFour.text = question.options[3].data
                binding.tvOptionFive.text = question.options[4].data
                binding.tvOptionSix.text = question.options[5].data
                binding.tvOptionSeven.text = question.options[6].data
                binding.tvOptionEight.text = question.options[7].data
                binding.tvOptionNine.text = question.options[8].data
                binding.tvOptionTen.text = question.options[9].data
                binding.tvOptionEleven.text = question.options[10].data
                binding.tvOptionTwelve.text = question.options[11].data
                binding.tvOptionThirteen.text = question.options[12].data
                binding.tvOptionFourteen.text = question.options[13].data
                binding.cvOptionThree.visibility = View.VISIBLE
                binding.cvOptionFour.visibility = View.VISIBLE
                binding.cvOptionFive.visibility = View.VISIBLE
                binding.cvOptionSix.visibility = View.VISIBLE
                binding.cvOptionSeven.visibility = View.VISIBLE
                binding.cvOptionEight.visibility = View.VISIBLE
                binding.cvOptionNine.visibility = View.VISIBLE
                binding.cvOptionTen.visibility = View.VISIBLE
                binding.cvOptionEleven.visibility = View.VISIBLE
                binding.cvOptionTwelve.visibility = View.VISIBLE
                binding.cvOptionThirteen.visibility = View.VISIBLE
                binding.cvOptionFourteen.visibility = View.VISIBLE
                binding.cvOptionFifteen.visibility = View.GONE
            }
            15 -> {
                binding.rvOptionList.visibility = View.GONE
                binding.layoutOptions.visibility = View.VISIBLE
                binding.tvOptionOne.text = question.options[0].data
                binding.tvOptionTwo.text = question.options[1].data
                binding.tvOptionThree.text = question.options[2].data
                binding.tvOptionFour.text = question.options[3].data
                binding.tvOptionFive.text = question.options[4].data
                binding.tvOptionSix.text = question.options[5].data
                binding.tvOptionSeven.text = question.options[6].data
                binding.tvOptionEight.text = question.options[7].data
                binding.tvOptionNine.text = question.options[8].data
                binding.tvOptionTen.text = question.options[9].data
                binding.tvOptionEleven.text = question.options[10].data
                binding.tvOptionTwelve.text = question.options[11].data
                binding.tvOptionThirteen.text = question.options[12].data
                binding.tvOptionFourteen.text = question.options[13].data
                binding.tvOptionFifteen.text = question.options[14].data
                binding.cvOptionThree.visibility = View.VISIBLE
                binding.cvOptionFour.visibility = View.VISIBLE
                binding.cvOptionFive.visibility = View.VISIBLE
                binding.cvOptionSix.visibility = View.VISIBLE
                binding.cvOptionSeven.visibility = View.VISIBLE
                binding.cvOptionEight.visibility = View.VISIBLE
                binding.cvOptionNine.visibility = View.VISIBLE
                binding.cvOptionTen.visibility = View.VISIBLE
                binding.cvOptionEleven.visibility = View.VISIBLE
                binding.cvOptionTwelve.visibility = View.VISIBLE
                binding.cvOptionThirteen.visibility = View.VISIBLE
                binding.cvOptionFourteen.visibility = View.VISIBLE
                binding.cvOptionFifteen.visibility = View.VISIBLE
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setQuestion(questions: ArrayList<Question>) {
        var getIndex = 0
        for (i in 0 until questions.size) {
            if (questions[i].option_id == nextOptionId) {
                getIndex = i
                this.question = questions[i]
                break
            }
        }
        binding.progressBar.max = questions.size - 1
        binding.progressBar.progress = count
        /*if (getIndex == 1) {
            count = 0
        }*/
        setMaxProgress(count)
        //setCurrentProgress(count)
        defaultOptionsView()
        binding.tvCurrentQuestion.text = question!!.question
        if (question!!.is_multiple) {
            binding.tvCheckApply.visibility = View.VISIBLE
            binding.btnQuestionnaireContinue.visibility = View.VISIBLE
        } else {
            binding.tvCheckApply.visibility = View.GONE
            binding.btnQuestionnaireContinue.visibility = View.GONE
        }
        /*when (count) {
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
        }*/
        /*if (question!!.next != null && question!!.next.isEmpty()) {
            tv_start_text.text = "And we are done..."
        }*/
        Log.i("Total count", (questions.size - 1).toString())
        Log.i("Count", count.toString())
        noOfAnswers = question!!.options.size
        when (noOfAnswers) {
            1 -> {
                mSelectedOptionId = arrayListOf()
                binding.rvOptionList.visibility = View.VISIBLE
                binding.layoutOptions.visibility = View.GONE
                val mOptionList = question!!.options[0].data.split(",").toTypedArray()
                binding.rvOptionList.layoutManager = GridLayoutManager(
                    requireActivity(),
                    3
                )
                binding.rvOptionList.adapter = OptionListAdapter(
                    requireActivity(),
                    getListData(mOptionList)
                )
            }
            2 -> {
                binding.rvOptionList.visibility = View.GONE
                binding.layoutOptions.visibility = View.VISIBLE
                binding.tvOptionOne.text = question!!.options[0].data
                binding.tvOptionTwo.text = question!!.options[1].data
                binding.cvOptionThree.visibility = View.GONE
                binding.cvOptionFour.visibility = View.GONE
                binding.cvOptionFive.visibility = View.GONE
                binding.cvOptionSix.visibility = View.GONE
                binding.cvOptionSeven.visibility = View.GONE
                binding.cvOptionEight.visibility = View.GONE
                binding.cvOptionNine.visibility = View.GONE
                binding.cvOptionTen.visibility = View.GONE
                binding.cvOptionEleven.visibility = View.GONE
                binding.cvOptionTwelve.visibility = View.GONE
                binding.cvOptionThirteen.visibility = View.GONE
                binding.cvOptionFourteen.visibility = View.GONE
                binding.cvOptionFifteen.visibility = View.GONE
            }
            3 -> {
                binding.rvOptionList.visibility = View.GONE
                binding.layoutOptions.visibility = View.VISIBLE
                binding.tvOptionOne.text = question!!.options[0].data
                binding.tvOptionTwo.text = question!!.options[1].data
                binding.tvOptionThree.text = question!!.options[2].data
                binding.tvOptionThree.visibility = View.VISIBLE
                binding.cvOptionFour.visibility = View.GONE
                binding.cvOptionFive.visibility = View.GONE
                binding.cvOptionSix.visibility = View.GONE
                binding.cvOptionSeven.visibility = View.GONE
                binding.cvOptionEight.visibility = View.GONE
                binding.cvOptionNine.visibility = View.GONE
                binding.cvOptionTen.visibility = View.GONE
                binding.cvOptionEleven.visibility = View.GONE
                binding.cvOptionTwelve.visibility = View.GONE
                binding.cvOptionThirteen.visibility = View.GONE
                binding.cvOptionFourteen.visibility = View.GONE
                binding.cvOptionFifteen.visibility = View.GONE
            }
            4 -> {
                binding.rvOptionList.visibility = View.GONE
                binding.layoutOptions.visibility = View.VISIBLE
                binding.tvOptionOne.text = question!!.options[0].data
                binding.tvOptionTwo.text = question!!.options[1].data
                binding.tvOptionThree.text = question!!.options[2].data
                binding.tvOptionFour.text = question!!.options[3].data
                binding.cvOptionThree.visibility = View.VISIBLE
                binding.cvOptionFour.visibility = View.VISIBLE
                binding.cvOptionFive.visibility = View.GONE
                binding.cvOptionSix.visibility = View.GONE
                binding.cvOptionSeven.visibility = View.GONE
                binding.cvOptionEight.visibility = View.GONE
                binding.cvOptionNine.visibility = View.GONE
                binding.cvOptionTen.visibility = View.GONE
                binding.cvOptionEleven.visibility = View.GONE
                binding.cvOptionTwelve.visibility = View.GONE
                binding.cvOptionThirteen.visibility = View.GONE
                binding.cvOptionFourteen.visibility = View.GONE
                binding.cvOptionFifteen.visibility = View.GONE
            }
            5 -> {
                binding.rvOptionList.visibility = View.GONE
                binding.layoutOptions.visibility = View.VISIBLE
                binding.tvOptionOne.text = question!!.options[0].data
                binding.tvOptionTwo.text = question!!.options[1].data
                binding.tvOptionThree.text = question!!.options[2].data
                binding.tvOptionFour.text = question!!.options[3].data
                binding.tvOptionFive.text = question!!.options[4].data
                binding.cvOptionThree.visibility = View.VISIBLE
                binding.cvOptionFour.visibility = View.VISIBLE
                binding.cvOptionFive.visibility = View.VISIBLE
                binding.cvOptionSix.visibility = View.GONE
                binding.cvOptionSeven.visibility = View.GONE
                binding.cvOptionEight.visibility = View.GONE
                binding.cvOptionNine.visibility = View.GONE
                binding.cvOptionTen.visibility = View.GONE
                binding.cvOptionEleven.visibility = View.GONE
                binding.cvOptionTwelve.visibility = View.GONE
                binding.cvOptionThirteen.visibility = View.GONE
                binding.cvOptionFourteen.visibility = View.GONE
                binding.cvOptionFifteen.visibility = View.GONE
            }
            6 -> {
                binding.rvOptionList.visibility = View.GONE
                binding.layoutOptions.visibility = View.VISIBLE
                binding.tvOptionOne.text = question!!.options[0].data
                binding.tvOptionTwo.text = question!!.options[1].data
                binding.tvOptionThree.text = question!!.options[2].data
                binding.tvOptionFour.text = question!!.options[3].data
                binding.tvOptionFive.text = question!!.options[4].data
                binding.tvOptionSix.text = question!!.options[5].data
                binding.cvOptionThree.visibility = View.VISIBLE
                binding.cvOptionFour.visibility = View.VISIBLE
                binding.cvOptionFive.visibility = View.VISIBLE
                binding.cvOptionSix.visibility = View.VISIBLE
                binding.cvOptionSeven.visibility = View.GONE
                binding.cvOptionEight.visibility = View.GONE
                binding.cvOptionNine.visibility = View.GONE
                binding.cvOptionTen.visibility = View.GONE
                binding.cvOptionEleven.visibility = View.GONE
                binding.cvOptionTwelve.visibility = View.GONE
                binding.cvOptionThirteen.visibility = View.GONE
                binding.cvOptionFourteen.visibility = View.GONE
                binding.cvOptionFifteen.visibility = View.GONE
            }
            7 -> {
                binding.rvOptionList.visibility = View.GONE
                binding.layoutOptions.visibility = View.VISIBLE
                binding.tvOptionOne.text = question!!.options[0].data
                binding.tvOptionTwo.text = question!!.options[1].data
                binding.tvOptionThree.text = question!!.options[2].data
                binding.tvOptionFour.text = question!!.options[3].data
                binding.tvOptionFive.text = question!!.options[4].data
                binding.tvOptionSix.text = question!!.options[5].data
                binding.tvOptionSeven.text = question!!.options[6].data
                binding.cvOptionThree.visibility = View.VISIBLE
                binding.cvOptionFour.visibility = View.VISIBLE
                binding.cvOptionFive.visibility = View.VISIBLE
                binding.cvOptionSix.visibility = View.VISIBLE
                binding.cvOptionSeven.visibility = View.VISIBLE
                binding.cvOptionEight.visibility = View.GONE
                binding.cvOptionNine.visibility = View.GONE
                binding.cvOptionTen.visibility = View.GONE
                binding.cvOptionEleven.visibility = View.GONE
                binding.cvOptionTwelve.visibility = View.GONE
                binding.cvOptionThirteen.visibility = View.GONE
                binding.cvOptionFourteen.visibility = View.GONE
                binding.cvOptionFifteen.visibility = View.GONE
            }
            8 -> {
                binding.rvOptionList.visibility = View.GONE
                binding.layoutOptions.visibility = View.VISIBLE
                binding.tvOptionOne.text = question!!.options[0].data
                binding.tvOptionTwo.text = question!!.options[1].data
                binding.tvOptionThree.text = question!!.options[2].data
                binding.tvOptionFour.text = question!!.options[3].data
                binding.tvOptionFive.text = question!!.options[4].data
                binding.tvOptionSix.text = question!!.options[5].data
                binding.tvOptionSeven.text = question!!.options[6].data
                binding.tvOptionEight.text = question!!.options[7].data
                binding.cvOptionThree.visibility = View.VISIBLE
                binding.cvOptionFour.visibility = View.VISIBLE
                binding.cvOptionFive.visibility = View.VISIBLE
                binding.cvOptionSix.visibility = View.VISIBLE
                binding.cvOptionSeven.visibility = View.VISIBLE
                binding.cvOptionEight.visibility = View.VISIBLE
                binding.cvOptionNine.visibility = View.GONE
                binding.cvOptionTen.visibility = View.GONE
                binding.cvOptionEleven.visibility = View.GONE
                binding.cvOptionTwelve.visibility = View.GONE
                binding.cvOptionThirteen.visibility = View.GONE
                binding.cvOptionFourteen.visibility = View.GONE
                binding.cvOptionFifteen.visibility = View.GONE
            }
            9 -> {
                binding.rvOptionList.visibility = View.GONE
                binding.layoutOptions.visibility = View.VISIBLE
                binding.tvOptionOne.text = question!!.options[0].data
                binding.tvOptionTwo.text = question!!.options[1].data
                binding.tvOptionThree.text = question!!.options[2].data
                binding.tvOptionFour.text = question!!.options[3].data
                binding.tvOptionFive.text = question!!.options[4].data
                binding.tvOptionSix.text = question!!.options[5].data
                binding.tvOptionSeven.text = question!!.options[6].data
                binding.tvOptionEight.text = question!!.options[7].data
                binding.tvOptionNine.text = question!!.options[8].data
                binding.cvOptionThree.visibility = View.VISIBLE
                binding.cvOptionFour.visibility = View.VISIBLE
                binding.cvOptionFive.visibility = View.VISIBLE
                binding.cvOptionSix.visibility = View.VISIBLE
                binding.cvOptionSeven.visibility = View.VISIBLE
                binding.cvOptionEight.visibility = View.VISIBLE
                binding.cvOptionNine.visibility = View.VISIBLE
                binding.cvOptionTen.visibility = View.GONE
                binding.cvOptionEleven.visibility = View.GONE
                binding.cvOptionTwelve.visibility = View.GONE
                binding.cvOptionThirteen.visibility = View.GONE
                binding.cvOptionFourteen.visibility = View.GONE
                binding.cvOptionFifteen.visibility = View.GONE
            }
            10 -> {
                binding.rvOptionList.visibility = View.GONE
                binding.layoutOptions.visibility = View.VISIBLE
                binding.tvOptionOne.text = question!!.options[0].data
                binding.tvOptionTwo.text = question!!.options[1].data
                binding.tvOptionThree.text = question!!.options[2].data
                binding.tvOptionFour.text = question!!.options[3].data
                binding.tvOptionFive.text = question!!.options[4].data
                binding.tvOptionSix.text = question!!.options[5].data
                binding.tvOptionSeven.text = question!!.options[6].data
                binding.tvOptionEight.text = question!!.options[7].data
                binding.tvOptionNine.text = question!!.options[8].data
                binding.tvOptionTen.text = question!!.options[9].data
                binding.cvOptionThree.visibility = View.VISIBLE
                binding.cvOptionFour.visibility = View.VISIBLE
                binding.cvOptionFive.visibility = View.VISIBLE
                binding.cvOptionSix.visibility = View.VISIBLE
                binding.cvOptionSeven.visibility = View.VISIBLE
                binding.cvOptionEight.visibility = View.VISIBLE
                binding.cvOptionNine.visibility = View.VISIBLE
                binding.cvOptionTen.visibility = View.VISIBLE
                binding.cvOptionEleven.visibility = View.GONE
                binding.cvOptionTwelve.visibility = View.GONE
                binding.cvOptionThirteen.visibility = View.GONE
                binding.cvOptionFourteen.visibility = View.GONE
                binding.cvOptionFifteen.visibility = View.GONE
            }
            11 -> {
                binding.rvOptionList.visibility = View.GONE
                binding.layoutOptions.visibility = View.VISIBLE
                binding.tvOptionOne.text = question!!.options[0].data
                binding.tvOptionTwo.text = question!!.options[1].data
                binding.tvOptionThree.text = question!!.options[2].data
                binding.tvOptionFour.text = question!!.options[3].data
                binding.tvOptionFive.text = question!!.options[4].data
                binding.tvOptionSix.text = question!!.options[5].data
                binding.tvOptionSeven.text = question!!.options[6].data
                binding.tvOptionEight.text = question!!.options[7].data
                binding.tvOptionNine.text = question!!.options[8].data
                binding.tvOptionTen.text = question!!.options[9].data
                binding.tvOptionEleven.text = question!!.options[10].data
                binding.cvOptionThree.visibility = View.VISIBLE
                binding.cvOptionFour.visibility = View.VISIBLE
                binding.cvOptionFive.visibility = View.VISIBLE
                binding.cvOptionSix.visibility = View.VISIBLE
                binding.cvOptionSeven.visibility = View.VISIBLE
                binding.cvOptionEight.visibility = View.VISIBLE
                binding.cvOptionNine.visibility = View.VISIBLE
                binding.cvOptionTen.visibility = View.VISIBLE
                binding.cvOptionEleven.visibility = View.VISIBLE
                binding.cvOptionTwelve.visibility = View.GONE
                binding.cvOptionThirteen.visibility = View.GONE
                binding.cvOptionFourteen.visibility = View.GONE
                binding.cvOptionFifteen.visibility = View.GONE
            }
            12 -> {
                binding.rvOptionList.visibility = View.GONE
                binding.layoutOptions.visibility = View.VISIBLE
                binding.tvOptionOne.text = question!!.options[0].data
                binding.tvOptionTwo.text = question!!.options[1].data
                binding.tvOptionThree.text = question!!.options[2].data
                binding.tvOptionFour.text = question!!.options[3].data
                binding.tvOptionFive.text = question!!.options[4].data
                binding.tvOptionSix.text = question!!.options[5].data
                binding.tvOptionSeven.text = question!!.options[6].data
                binding.tvOptionEight.text = question!!.options[7].data
                binding.tvOptionNine.text = question!!.options[8].data
                binding.tvOptionTen.text = question!!.options[9].data
                binding.tvOptionEleven.text = question!!.options[10].data
                binding.tvOptionTwelve.text = question!!.options[11].data
                binding.cvOptionThree.visibility = View.VISIBLE
                binding.cvOptionFour.visibility = View.VISIBLE
                binding.cvOptionFive.visibility = View.VISIBLE
                binding.cvOptionSix.visibility = View.VISIBLE
                binding.cvOptionSeven.visibility = View.VISIBLE
                binding.cvOptionEight.visibility = View.VISIBLE
                binding.cvOptionNine.visibility = View.VISIBLE
                binding.cvOptionTen.visibility = View.VISIBLE
                binding.cvOptionEleven.visibility = View.VISIBLE
                binding.cvOptionTwelve.visibility = View.VISIBLE
                binding.cvOptionThirteen.visibility = View.GONE
                binding.cvOptionFourteen.visibility = View.GONE
                binding.cvOptionFifteen.visibility = View.GONE
            }
            13 -> {
                binding.rvOptionList.visibility = View.GONE
                binding.layoutOptions.visibility = View.VISIBLE
                binding.tvOptionOne.text = question!!.options[0].data
                binding.tvOptionTwo.text = question!!.options[1].data
                binding.tvOptionThree.text = question!!.options[2].data
                binding.tvOptionFour.text = question!!.options[3].data
                binding.tvOptionFive.text = question!!.options[4].data
                binding.tvOptionSix.text = question!!.options[5].data
                binding.tvOptionSeven.text = question!!.options[6].data
                binding.tvOptionEight.text = question!!.options[7].data
                binding.tvOptionNine.text = question!!.options[8].data
                binding.tvOptionTen.text = question!!.options[9].data
                binding.tvOptionEleven.text = question!!.options[10].data
                binding.tvOptionTwelve.text = question!!.options[11].data
                binding.tvOptionThirteen.text = question!!.options[12].data
                binding.cvOptionThree.visibility = View.VISIBLE
                binding.cvOptionFour.visibility = View.VISIBLE
                binding.cvOptionFive.visibility = View.VISIBLE
                binding.cvOptionSix.visibility = View.VISIBLE
                binding.cvOptionSeven.visibility = View.VISIBLE
                binding.cvOptionEight.visibility = View.VISIBLE
                binding.cvOptionNine.visibility = View.VISIBLE
                binding.cvOptionTen.visibility = View.VISIBLE
                binding.cvOptionEleven.visibility = View.VISIBLE
                binding.cvOptionTwelve.visibility = View.VISIBLE
                binding.cvOptionThirteen.visibility = View.VISIBLE
                binding.cvOptionFourteen.visibility = View.GONE
                binding.cvOptionFifteen.visibility = View.GONE
            }
            14 -> {
                binding.rvOptionList.visibility = View.GONE
                binding.layoutOptions.visibility = View.VISIBLE
                binding.tvOptionOne.text = question!!.options[0].data
                binding.tvOptionTwo.text = question!!.options[1].data
                binding.tvOptionThree.text = question!!.options[2].data
                binding.tvOptionFour.text = question!!.options[3].data
                binding.tvOptionFive.text = question!!.options[4].data
                binding.tvOptionSix.text = question!!.options[5].data
                binding.tvOptionSeven.text = question!!.options[6].data
                binding.tvOptionEight.text = question!!.options[7].data
                binding.tvOptionNine.text = question!!.options[8].data
                binding.tvOptionTen.text = question!!.options[9].data
                binding.tvOptionEleven.text = question!!.options[10].data
                binding.tvOptionTwelve.text = question!!.options[11].data
                binding.tvOptionThirteen.text = question!!.options[12].data
                binding.tvOptionFourteen.text = question!!.options[13].data
                binding.cvOptionThree.visibility = View.VISIBLE
                binding.cvOptionFour.visibility = View.VISIBLE
                binding.cvOptionFive.visibility = View.VISIBLE
                binding.cvOptionSix.visibility = View.VISIBLE
                binding.cvOptionSeven.visibility = View.VISIBLE
                binding.cvOptionEight.visibility = View.VISIBLE
                binding.cvOptionNine.visibility = View.VISIBLE
                binding.cvOptionTen.visibility = View.VISIBLE
                binding.cvOptionEleven.visibility = View.VISIBLE
                binding.cvOptionTwelve.visibility = View.VISIBLE
                binding.cvOptionThirteen.visibility = View.VISIBLE
                binding.cvOptionFourteen.visibility = View.VISIBLE
                binding.cvOptionFifteen.visibility = View.GONE
            }
            15 -> {
                binding.rvOptionList.visibility = View.GONE
                binding.layoutOptions.visibility = View.VISIBLE
                binding.tvOptionOne.text = question!!.options[0].data
                binding.tvOptionTwo.text = question!!.options[1].data
                binding.tvOptionThree.text = question!!.options[2].data
                binding.tvOptionFour.text = question!!.options[3].data
                binding.tvOptionFive.text = question!!.options[4].data
                binding.tvOptionSix.text = question!!.options[5].data
                binding.tvOptionSeven.text = question!!.options[6].data
                binding.tvOptionEight.text = question!!.options[7].data
                binding.tvOptionNine.text = question!!.options[8].data
                binding.tvOptionTen.text = question!!.options[9].data
                binding.tvOptionEleven.text = question!!.options[10].data
                binding.tvOptionTwelve.text = question!!.options[11].data
                binding.tvOptionThirteen.text = question!!.options[12].data
                binding.tvOptionFourteen.text = question!!.options[13].data
                binding.tvOptionFifteen.text = question!!.options[14].data
                binding.cvOptionThree.visibility = View.VISIBLE
                binding.cvOptionFour.visibility = View.VISIBLE
                binding.cvOptionFive.visibility = View.VISIBLE
                binding.cvOptionSix.visibility = View.VISIBLE
                binding.cvOptionSeven.visibility = View.VISIBLE
                binding.cvOptionEight.visibility = View.VISIBLE
                binding.cvOptionNine.visibility = View.VISIBLE
                binding.cvOptionTen.visibility = View.VISIBLE
                binding.cvOptionEleven.visibility = View.VISIBLE
                binding.cvOptionTwelve.visibility = View.VISIBLE
                binding.cvOptionThirteen.visibility = View.VISIBLE
                binding.cvOptionFourteen.visibility = View.VISIBLE
                binding.cvOptionFifteen.visibility = View.VISIBLE
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
                rvOptionList.visibility = View.VISIBLE
                layoutOptions.visibility = View.GONE
                val mOptionList = question!!.option_1.answer!!.split(",").toTypedArray()
                rvOptionList.layoutManager = GridLayoutManager(
                    requireActivity(),
                    3
                )
                rvOptionList.adapter = OptionListAdapter(
                    requireActivity(),
                    getListData(mOptionList)
                )
            }
            "2" -> {
                rvOptionList.visibility = View.GONE
                layoutOptions.visibility = View.VISIBLE
                tvOptionOne.text = question!!.option_1.answer
                tvOptionTwo.text = question!!.option_2.answer
                tvOptionThree.visibility = View.GONE
                tvOptionFour.visibility = View.GONE
            }
            "3" -> {
                rvOptionList.visibility = View.GONE
                layoutOptions.visibility = View.VISIBLE
                tvOptionOne.text = question!!.option_1.answer
                tvOptionTwo.text = question!!.option_2.answer
                tvOptionThree.text = question!!.option_3.answer
                tvOptionThree.visibility = View.VISIBLE
                tvOptionFour.visibility = View.GONE
            }
            "4" -> {
                rvOptionList.visibility = View.GONE
                layoutOptions.visibility = View.VISIBLE
                tvOptionOne.text = question!!.option_1.answer
                tvOptionTwo.text = question!!.option_2.answer
                tvOptionThree.text = question!!.option_3.answer
                tvOptionFour.text = question!!.option_4.answer
                tvOptionThree.visibility = View.VISIBLE
                tvOptionFour.visibility = View.VISIBLE
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
        options.add(0, binding.tvOptionOne)
        options.add(1, binding.tvOptionTwo)
        options.add(2, binding.tvOptionThree)
        options.add(3, binding.tvOptionFour)
        options.add(4, binding.tvOptionFive)
        options.add(5, binding.tvOptionSix)
        options.add(6, binding.tvOptionSeven)
        options.add(7, binding.tvOptionEight)
        options.add(8, binding.tvOptionNine)
        options.add(9, binding.tvOptionTen)
        options.add(10, binding.tvOptionEleven)
        options.add(11, binding.tvOptionTwelve)
        options.add(12, binding.tvOptionThirteen)
        options.add(13, binding.tvOptionFourteen)
        options.add(14, binding.tvOptionFifteen)

        for (option in options) {
            option.setTextColor(requireActivity().getColor(R.color.primaryGreen))
            option.setTypeface(option.typeface, Typeface.BOLD)
            option.background =
                ContextCompat.getDrawable(requireActivity(), R.drawable.edittext_background_box)
        }

    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun selectedOptionsView(
        tv: TextView,
        selectedAnswerId: ArrayList<Int>,
        answeredQuestionId: Int,
        selectedOptionId: String
    ) {
        defaultOptionsView()
        mSelectedOptionId = selectedAnswerId
        mAnsweredQuesitonId = answeredQuestionId
        tv.setTextColor(requireActivity().getColor(R.color.white))
        tv.setTypeface(tv.typeface, Typeface.BOLD)
        optionOne = false
        optionTwo = false
        optionThree = false
        optionFour = false
        optionFive = false
        optionSix = false
        optionSeven = false
        optionEight = false
        optionNine = false
        optionTen = false
        optionEleven = false
        optionTwelve = false
        optionThirteen = false
        optionFourteen = false
        optionFifteen = false
        //tv.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireActivity(), R.color.primaryGreen))
        tv.background =
            ContextCompat.getDrawable(requireActivity(), R.drawable.selected_background_option)
        count += 1
        multipleSelectionCount = 0
        nextOptionId = selectedOptionId
        selectedOptions = arrayListOf()
        preference!![PrefKeys.PREF_NEXT_QUESTION_ID] = nextOptionId
        if (preference!![PrefKeys.PREF_SET_ANSWER, ""]!!.isNotEmpty()) {
            val type: Type = object : TypeToken<ArrayList<EachAnswer?>?>() {}.type
            setAnswers = Gson().fromJson(preference!![PrefKeys.PREF_SET_ANSWER, ""]!!, type)
        }
        setAnswers.add(
            EachAnswer(
                answeredQuestionId,
                Answer(selectedAnswerId, binding.etOthers.text.toString().trim())
            )
        )
        binding.etOthers.setText("")
        binding.btnQuestionnaireContinue.visibility = View.GONE
        preference!![PrefKeys.PREF_SET_ANSWER] = Gson().toJson(setAnswers)
        if (nextOptionId != null) {
            binding.tvCheckApply.visibility = View.GONE
            binding.cvEdit.visibility = View.GONE
            setQuestion(questions!!)
        } else {
            sendAnswers()
        }
    }

    private fun optionSelection(tv: TextView, selected: Boolean) {
        if (selected) {
            tv.setTextColor(requireActivity().getColor(R.color.white))
            tv.setTypeface(tv.typeface, Typeface.BOLD)
            tv.background =
                ContextCompat.getDrawable(requireActivity(), R.drawable.selected_background_option)
        } else {
            tv.setTextColor(requireActivity().getColor(R.color.primaryGreen))
            tv.setTypeface(tv.typeface, Typeface.BOLD)
            tv.background =
                ContextCompat.getDrawable(requireActivity(), R.drawable.edittext_background_box)
        }
    }

    private fun setMaxProgress(progressCount: Int) {
        when (progressCount) {
            1 -> {
                binding.dashProgress1.visibility = View.VISIBLE
                binding.dashProgress2.visibility = View.GONE
                binding.dashProgress3.visibility = View.GONE
                binding.dashProgress4.visibility = View.GONE
                binding.dashProgress5.visibility = View.GONE
                binding.dashProgress6.visibility = View.GONE
                binding.dashProgress7.visibility = View.GONE
                binding.dashProgress8.visibility = View.GONE
                binding.dashProgress9.visibility = View.GONE
                binding.dashProgress10.visibility = View.GONE
                binding.dashProgress11.visibility = View.GONE
                binding.dashProgress12.visibility = View.GONE
                binding.dashProgress13.visibility = View.GONE
                binding.dashProgress14.visibility = View.GONE
                binding.dashProgress15.visibility = View.GONE
                binding.dashProgress16.visibility = View.GONE
                binding.dashProgress17.visibility = View.GONE
                binding.dashProgress18.visibility = View.GONE
                binding.dashProgress19.visibility = View.GONE
                binding.dashProgress20.visibility = View.GONE
                binding.dashProgress21.visibility = View.GONE
                binding.dashProgress22.visibility = View.GONE
                binding.dashProgress23.visibility = View.GONE
                binding.dashProgress24.visibility = View.GONE
                binding.dashProgress25.visibility = View.GONE
            }
            2 -> {
                binding.dashProgress1.visibility = View.VISIBLE
                binding.dashProgress2.visibility = View.VISIBLE
                binding.dashProgress3.visibility = View.GONE
                binding.dashProgress4.visibility = View.GONE
                binding.dashProgress5.visibility = View.GONE
                binding.dashProgress6.visibility = View.GONE
                binding.dashProgress7.visibility = View.GONE
                binding.dashProgress8.visibility = View.GONE
                binding.dashProgress9.visibility = View.GONE
                binding.dashProgress10.visibility = View.GONE
                binding.dashProgress11.visibility = View.GONE
                binding.dashProgress12.visibility = View.GONE
                binding.dashProgress13.visibility = View.GONE
                binding.dashProgress14.visibility = View.GONE
                binding.dashProgress15.visibility = View.GONE
                binding.dashProgress16.visibility = View.GONE
                binding.dashProgress17.visibility = View.GONE
                binding.dashProgress18.visibility = View.GONE
                binding.dashProgress19.visibility = View.GONE
                binding.dashProgress20.visibility = View.GONE
                binding.dashProgress21.visibility = View.GONE
                binding.dashProgress22.visibility = View.GONE
                binding.dashProgress23.visibility = View.GONE
                binding.dashProgress24.visibility = View.GONE
                binding.dashProgress25.visibility = View.GONE
            }
            3 -> {
                binding.dashProgress1.visibility = View.VISIBLE
                binding.dashProgress2.visibility = View.VISIBLE
                binding.dashProgress3.visibility = View.VISIBLE
                binding.dashProgress4.visibility = View.GONE
                binding.dashProgress5.visibility = View.GONE
                binding.dashProgress6.visibility = View.GONE
                binding.dashProgress7.visibility = View.GONE
                binding.dashProgress8.visibility = View.GONE
                binding.dashProgress9.visibility = View.GONE
                binding.dashProgress10.visibility = View.GONE
                binding.dashProgress11.visibility = View.GONE
                binding.dashProgress12.visibility = View.GONE
                binding.dashProgress13.visibility = View.GONE
                binding.dashProgress14.visibility = View.GONE
                binding.dashProgress15.visibility = View.GONE
                binding.dashProgress16.visibility = View.GONE
                binding.dashProgress17.visibility = View.GONE
                binding.dashProgress18.visibility = View.GONE
                binding.dashProgress19.visibility = View.GONE
                binding.dashProgress20.visibility = View.GONE
                binding.dashProgress21.visibility = View.GONE
                binding.dashProgress22.visibility = View.GONE
                binding.dashProgress23.visibility = View.GONE
                binding.dashProgress24.visibility = View.GONE
                binding.dashProgress25.visibility = View.GONE
            }
            4 -> {
                binding.dashProgress1.visibility = View.VISIBLE
                binding.dashProgress2.visibility = View.VISIBLE
                binding.dashProgress3.visibility = View.VISIBLE
                binding.dashProgress4.visibility = View.VISIBLE
                binding.dashProgress5.visibility = View.GONE
                binding.dashProgress6.visibility = View.GONE
                binding.dashProgress7.visibility = View.GONE
                binding.dashProgress8.visibility = View.GONE
                binding.dashProgress9.visibility = View.GONE
                binding.dashProgress10.visibility = View.GONE
                binding.dashProgress11.visibility = View.GONE
                binding.dashProgress12.visibility = View.GONE
                binding.dashProgress13.visibility = View.GONE
                binding.dashProgress14.visibility = View.GONE
                binding.dashProgress15.visibility = View.GONE
                binding.dashProgress16.visibility = View.GONE
                binding.dashProgress17.visibility = View.GONE
                binding.dashProgress18.visibility = View.GONE
                binding.dashProgress19.visibility = View.GONE
                binding.dashProgress20.visibility = View.GONE
                binding.dashProgress21.visibility = View.GONE
                binding.dashProgress22.visibility = View.GONE
                binding.dashProgress23.visibility = View.GONE
                binding.dashProgress24.visibility = View.GONE
                binding.dashProgress25.visibility = View.GONE
            }
            5 -> {
                binding.dashProgress1.visibility = View.VISIBLE
                binding.dashProgress2.visibility = View.VISIBLE
                binding.dashProgress3.visibility = View.VISIBLE
                binding.dashProgress4.visibility = View.VISIBLE
                binding.dashProgress5.visibility = View.VISIBLE
                binding.dashProgress6.visibility = View.GONE
                binding.dashProgress7.visibility = View.GONE
                binding.dashProgress8.visibility = View.GONE
                binding.dashProgress9.visibility = View.GONE
                binding.dashProgress10.visibility = View.GONE
                binding.dashProgress11.visibility = View.GONE
                binding.dashProgress12.visibility = View.GONE
                binding.dashProgress13.visibility = View.GONE
                binding.dashProgress14.visibility = View.GONE
                binding.dashProgress15.visibility = View.GONE
                binding.dashProgress16.visibility = View.GONE
                binding.dashProgress17.visibility = View.GONE
                binding.dashProgress18.visibility = View.GONE
                binding.dashProgress19.visibility = View.GONE
                binding.dashProgress20.visibility = View.GONE
                binding.dashProgress21.visibility = View.GONE
                binding.dashProgress22.visibility = View.GONE
                binding.dashProgress23.visibility = View.GONE
                binding.dashProgress24.visibility = View.GONE
                binding.dashProgress25.visibility = View.GONE
            }
            6 -> {
                binding.dashProgress1.visibility = View.VISIBLE
                binding.dashProgress2.visibility = View.VISIBLE
                binding.dashProgress3.visibility = View.VISIBLE
                binding.dashProgress4.visibility = View.VISIBLE
                binding.dashProgress5.visibility = View.VISIBLE
                binding.dashProgress6.visibility = View.VISIBLE
                binding.dashProgress7.visibility = View.GONE
                binding.dashProgress8.visibility = View.GONE
                binding.dashProgress9.visibility = View.GONE
                binding.dashProgress10.visibility = View.GONE
                binding.dashProgress11.visibility = View.GONE
                binding.dashProgress12.visibility = View.GONE
                binding.dashProgress13.visibility = View.GONE
                binding.dashProgress14.visibility = View.GONE
                binding.dashProgress15.visibility = View.GONE
                binding.dashProgress16.visibility = View.GONE
                binding.dashProgress17.visibility = View.GONE
                binding.dashProgress18.visibility = View.GONE
                binding.dashProgress19.visibility = View.GONE
                binding.dashProgress20.visibility = View.GONE
                binding.dashProgress21.visibility = View.GONE
                binding.dashProgress22.visibility = View.GONE
                binding.dashProgress23.visibility = View.GONE
                binding.dashProgress24.visibility = View.GONE
                binding.dashProgress25.visibility = View.GONE
            }
            7 -> {
                binding.dashProgress1.visibility = View.VISIBLE
                binding.dashProgress2.visibility = View.VISIBLE
                binding.dashProgress3.visibility = View.VISIBLE
                binding.dashProgress4.visibility = View.VISIBLE
                binding.dashProgress5.visibility = View.VISIBLE
                binding.dashProgress6.visibility = View.VISIBLE
                binding.dashProgress7.visibility = View.VISIBLE
                binding.dashProgress8.visibility = View.GONE
                binding.dashProgress9.visibility = View.GONE
                binding.dashProgress10.visibility = View.GONE
                binding.dashProgress11.visibility = View.GONE
                binding.dashProgress12.visibility = View.GONE
                binding.dashProgress13.visibility = View.GONE
                binding.dashProgress14.visibility = View.GONE
                binding.dashProgress15.visibility = View.GONE
                binding.dashProgress16.visibility = View.GONE
                binding.dashProgress17.visibility = View.GONE
                binding.dashProgress18.visibility = View.GONE
                binding.dashProgress19.visibility = View.GONE
                binding.dashProgress20.visibility = View.GONE
                binding.dashProgress21.visibility = View.GONE
                binding.dashProgress22.visibility = View.GONE
                binding.dashProgress23.visibility = View.GONE
                binding.dashProgress24.visibility = View.GONE
                binding.dashProgress25.visibility = View.GONE
            }
            8 -> {
                binding.dashProgress1.visibility = View.VISIBLE
                binding.dashProgress2.visibility = View.VISIBLE
                binding.dashProgress3.visibility = View.VISIBLE
                binding.dashProgress4.visibility = View.VISIBLE
                binding.dashProgress5.visibility = View.VISIBLE
                binding.dashProgress6.visibility = View.VISIBLE
                binding.dashProgress7.visibility = View.VISIBLE
                binding.dashProgress8.visibility = View.VISIBLE
                binding.dashProgress9.visibility = View.GONE
                binding.dashProgress10.visibility = View.GONE
                binding.dashProgress11.visibility = View.GONE
                binding.dashProgress12.visibility = View.GONE
                binding.dashProgress13.visibility = View.GONE
                binding.dashProgress14.visibility = View.GONE
                binding.dashProgress15.visibility = View.GONE
                binding.dashProgress16.visibility = View.GONE
                binding.dashProgress17.visibility = View.GONE
                binding.dashProgress18.visibility = View.GONE
                binding.dashProgress19.visibility = View.GONE
                binding.dashProgress20.visibility = View.GONE
                binding.dashProgress21.visibility = View.GONE
                binding.dashProgress22.visibility = View.GONE
                binding.dashProgress23.visibility = View.GONE
                binding.dashProgress24.visibility = View.GONE
                binding.dashProgress25.visibility = View.GONE
            }
            9 -> {
                binding.dashProgress1.visibility = View.VISIBLE
                binding.dashProgress2.visibility = View.VISIBLE
                binding.dashProgress3.visibility = View.VISIBLE
                binding.dashProgress4.visibility = View.VISIBLE
                binding.dashProgress5.visibility = View.VISIBLE
                binding.dashProgress6.visibility = View.VISIBLE
                binding.dashProgress7.visibility = View.VISIBLE
                binding.dashProgress8.visibility = View.VISIBLE
                binding.dashProgress9.visibility = View.VISIBLE
                binding.dashProgress10.visibility = View.GONE
                binding.dashProgress11.visibility = View.GONE
                binding.dashProgress12.visibility = View.GONE
                binding.dashProgress13.visibility = View.GONE
                binding.dashProgress14.visibility = View.GONE
                binding.dashProgress15.visibility = View.GONE
                binding.dashProgress16.visibility = View.GONE
                binding.dashProgress17.visibility = View.GONE
                binding.dashProgress18.visibility = View.GONE
                binding.dashProgress19.visibility = View.GONE
                binding.dashProgress20.visibility = View.GONE
                binding.dashProgress21.visibility = View.GONE
                binding.dashProgress22.visibility = View.GONE
                binding.dashProgress23.visibility = View.GONE
                binding.dashProgress24.visibility = View.GONE
                binding.dashProgress25.visibility = View.GONE
            }
            10 -> {
                binding.dashProgress1.visibility = View.VISIBLE
                binding.dashProgress2.visibility = View.VISIBLE
                binding.dashProgress3.visibility = View.VISIBLE
                binding.dashProgress4.visibility = View.VISIBLE
                binding.dashProgress5.visibility = View.VISIBLE
                binding.dashProgress6.visibility = View.VISIBLE
                binding.dashProgress7.visibility = View.VISIBLE
                binding.dashProgress8.visibility = View.VISIBLE
                binding.dashProgress9.visibility = View.VISIBLE
                binding.dashProgress10.visibility = View.VISIBLE
                binding.dashProgress11.visibility = View.GONE
                binding.dashProgress12.visibility = View.GONE
                binding.dashProgress13.visibility = View.GONE
                binding.dashProgress14.visibility = View.GONE
                binding.dashProgress15.visibility = View.GONE
                binding.dashProgress16.visibility = View.GONE
                binding.dashProgress17.visibility = View.GONE
                binding.dashProgress18.visibility = View.GONE
                binding.dashProgress19.visibility = View.GONE
                binding.dashProgress20.visibility = View.GONE
                binding.dashProgress21.visibility = View.GONE
                binding.dashProgress22.visibility = View.GONE
                binding.dashProgress23.visibility = View.GONE
                binding.dashProgress24.visibility = View.GONE
                binding.dashProgress25.visibility = View.GONE
            }
            11 -> {
                binding.dashProgress1.visibility = View.VISIBLE
                binding.dashProgress2.visibility = View.VISIBLE
                binding.dashProgress3.visibility = View.VISIBLE
                binding.dashProgress4.visibility = View.VISIBLE
                binding.dashProgress5.visibility = View.VISIBLE
                binding.dashProgress6.visibility = View.VISIBLE
                binding.dashProgress7.visibility = View.VISIBLE
                binding.dashProgress8.visibility = View.VISIBLE
                binding.dashProgress9.visibility = View.VISIBLE
                binding.dashProgress10.visibility = View.VISIBLE
                binding.dashProgress11.visibility = View.VISIBLE
                binding.dashProgress12.visibility = View.GONE
                binding.dashProgress13.visibility = View.GONE
                binding.dashProgress14.visibility = View.GONE
                binding.dashProgress15.visibility = View.GONE
                binding.dashProgress16.visibility = View.GONE
                binding.dashProgress17.visibility = View.GONE
                binding.dashProgress18.visibility = View.GONE
                binding.dashProgress19.visibility = View.GONE
                binding.dashProgress20.visibility = View.GONE
                binding.dashProgress21.visibility = View.GONE
                binding.dashProgress22.visibility = View.GONE
                binding.dashProgress23.visibility = View.GONE
                binding.dashProgress24.visibility = View.GONE
                binding.dashProgress25.visibility = View.GONE
            }
            12 -> {
                binding.dashProgress1.visibility = View.VISIBLE
                binding.dashProgress2.visibility = View.VISIBLE
                binding.dashProgress3.visibility = View.VISIBLE
                binding.dashProgress4.visibility = View.VISIBLE
                binding.dashProgress5.visibility = View.VISIBLE
                binding.dashProgress6.visibility = View.VISIBLE
                binding.dashProgress7.visibility = View.VISIBLE
                binding.dashProgress8.visibility = View.VISIBLE
                binding.dashProgress9.visibility = View.VISIBLE
                binding.dashProgress10.visibility = View.VISIBLE
                binding.dashProgress11.visibility = View.VISIBLE
                binding.dashProgress12.visibility = View.VISIBLE
                binding.dashProgress13.visibility = View.GONE
                binding.dashProgress14.visibility = View.GONE
                binding.dashProgress15.visibility = View.GONE
                binding.dashProgress16.visibility = View.GONE
                binding.dashProgress17.visibility = View.GONE
                binding.dashProgress18.visibility = View.GONE
                binding.dashProgress19.visibility = View.GONE
                binding.dashProgress20.visibility = View.GONE
                binding.dashProgress21.visibility = View.GONE
                binding.dashProgress22.visibility = View.GONE
                binding.dashProgress23.visibility = View.GONE
                binding.dashProgress24.visibility = View.GONE
                binding.dashProgress25.visibility = View.GONE
            }
            13 -> {
                binding.dashProgress1.visibility = View.VISIBLE
                binding.dashProgress2.visibility = View.VISIBLE
                binding.dashProgress3.visibility = View.VISIBLE
                binding.dashProgress4.visibility = View.VISIBLE
                binding.dashProgress5.visibility = View.VISIBLE
                binding.dashProgress6.visibility = View.VISIBLE
                binding.dashProgress7.visibility = View.VISIBLE
                binding.dashProgress8.visibility = View.VISIBLE
                binding.dashProgress9.visibility = View.VISIBLE
                binding.dashProgress10.visibility = View.VISIBLE
                binding.dashProgress11.visibility = View.VISIBLE
                binding.dashProgress12.visibility = View.VISIBLE
                binding.dashProgress13.visibility = View.VISIBLE
                binding.dashProgress14.visibility = View.GONE
                binding.dashProgress15.visibility = View.GONE
                binding.dashProgress16.visibility = View.GONE
                binding.dashProgress17.visibility = View.GONE
                binding.dashProgress18.visibility = View.GONE
                binding.dashProgress19.visibility = View.GONE
                binding.dashProgress20.visibility = View.GONE
                binding.dashProgress21.visibility = View.GONE
                binding.dashProgress22.visibility = View.GONE
                binding.dashProgress23.visibility = View.GONE
                binding.dashProgress24.visibility = View.GONE
                binding.dashProgress25.visibility = View.GONE
            }
            14 -> {
                binding.dashProgress1.visibility = View.VISIBLE
                binding.dashProgress2.visibility = View.VISIBLE
                binding.dashProgress3.visibility = View.VISIBLE
                binding.dashProgress4.visibility = View.VISIBLE
                binding.dashProgress5.visibility = View.VISIBLE
                binding.dashProgress6.visibility = View.VISIBLE
                binding.dashProgress7.visibility = View.VISIBLE
                binding.dashProgress8.visibility = View.VISIBLE
                binding.dashProgress9.visibility = View.VISIBLE
                binding.dashProgress10.visibility = View.VISIBLE
                binding.dashProgress11.visibility = View.VISIBLE
                binding.dashProgress12.visibility = View.VISIBLE
                binding.dashProgress13.visibility = View.VISIBLE
                binding.dashProgress14.visibility = View.VISIBLE
                binding.dashProgress15.visibility = View.GONE
                binding.dashProgress16.visibility = View.GONE
                binding.dashProgress17.visibility = View.GONE
                binding.dashProgress18.visibility = View.GONE
                binding.dashProgress19.visibility = View.GONE
                binding.dashProgress20.visibility = View.GONE
                binding.dashProgress21.visibility = View.GONE
                binding.dashProgress22.visibility = View.GONE
                binding.dashProgress23.visibility = View.GONE
                binding.dashProgress24.visibility = View.GONE
                binding.dashProgress25.visibility = View.GONE
            }
            15 -> {
                binding.dashProgress1.visibility = View.VISIBLE
                binding.dashProgress2.visibility = View.VISIBLE
                binding.dashProgress3.visibility = View.VISIBLE
                binding.dashProgress4.visibility = View.VISIBLE
                binding.dashProgress5.visibility = View.VISIBLE
                binding.dashProgress6.visibility = View.VISIBLE
                binding.dashProgress7.visibility = View.VISIBLE
                binding.dashProgress8.visibility = View.VISIBLE
                binding.dashProgress9.visibility = View.VISIBLE
                binding.dashProgress10.visibility = View.VISIBLE
                binding.dashProgress11.visibility = View.VISIBLE
                binding.dashProgress12.visibility = View.VISIBLE
                binding.dashProgress13.visibility = View.VISIBLE
                binding.dashProgress14.visibility = View.VISIBLE
                binding.dashProgress15.visibility = View.VISIBLE
                binding.dashProgress16.visibility = View.GONE
                binding.dashProgress17.visibility = View.GONE
                binding.dashProgress18.visibility = View.GONE
                binding.dashProgress19.visibility = View.GONE
                binding.dashProgress20.visibility = View.GONE
                binding.dashProgress21.visibility = View.GONE
                binding.dashProgress22.visibility = View.GONE
                binding.dashProgress23.visibility = View.GONE
                binding.dashProgress24.visibility = View.GONE
                binding.dashProgress25.visibility = View.GONE
            }
            16 -> {
                binding.dashProgress1.visibility = View.VISIBLE
                binding.dashProgress2.visibility = View.VISIBLE
                binding.dashProgress3.visibility = View.VISIBLE
                binding.dashProgress4.visibility = View.VISIBLE
                binding.dashProgress5.visibility = View.VISIBLE
                binding.dashProgress6.visibility = View.VISIBLE
                binding.dashProgress7.visibility = View.VISIBLE
                binding.dashProgress8.visibility = View.VISIBLE
                binding.dashProgress9.visibility = View.VISIBLE
                binding.dashProgress10.visibility = View.VISIBLE
                binding.dashProgress11.visibility = View.VISIBLE
                binding.dashProgress12.visibility = View.VISIBLE
                binding.dashProgress13.visibility = View.VISIBLE
                binding.dashProgress14.visibility = View.VISIBLE
                binding.dashProgress15.visibility = View.VISIBLE
                binding.dashProgress16.visibility = View.VISIBLE
                binding.dashProgress17.visibility = View.GONE
                binding.dashProgress18.visibility = View.GONE
                binding.dashProgress19.visibility = View.GONE
                binding.dashProgress20.visibility = View.GONE
                binding.dashProgress21.visibility = View.GONE
                binding.dashProgress22.visibility = View.GONE
                binding.dashProgress23.visibility = View.GONE
                binding.dashProgress24.visibility = View.GONE
                binding.dashProgress25.visibility = View.GONE
            }
            17 -> {
                binding.dashProgress1.visibility = View.VISIBLE
                binding.dashProgress2.visibility = View.VISIBLE
                binding.dashProgress3.visibility = View.VISIBLE
                binding.dashProgress4.visibility = View.VISIBLE
                binding.dashProgress5.visibility = View.VISIBLE
                binding.dashProgress6.visibility = View.VISIBLE
                binding.dashProgress7.visibility = View.VISIBLE
                binding.dashProgress8.visibility = View.VISIBLE
                binding.dashProgress9.visibility = View.VISIBLE
                binding.dashProgress10.visibility = View.VISIBLE
                binding.dashProgress11.visibility = View.VISIBLE
                binding.dashProgress12.visibility = View.VISIBLE
                binding.dashProgress13.visibility = View.VISIBLE
                binding.dashProgress14.visibility = View.VISIBLE
                binding.dashProgress15.visibility = View.VISIBLE
                binding.dashProgress16.visibility = View.VISIBLE
                binding.dashProgress17.visibility = View.VISIBLE
                binding.dashProgress18.visibility = View.GONE
                binding.dashProgress19.visibility = View.GONE
                binding.dashProgress20.visibility = View.GONE
                binding.dashProgress21.visibility = View.GONE
                binding.dashProgress22.visibility = View.GONE
                binding.dashProgress23.visibility = View.GONE
                binding.dashProgress24.visibility = View.GONE
                binding.dashProgress25.visibility = View.GONE
            }
            18 -> {
                binding.dashProgress1.visibility = View.VISIBLE
                binding.dashProgress2.visibility = View.VISIBLE
                binding.dashProgress3.visibility = View.VISIBLE
                binding.dashProgress4.visibility = View.VISIBLE
                binding.dashProgress5.visibility = View.VISIBLE
                binding.dashProgress6.visibility = View.VISIBLE
                binding.dashProgress7.visibility = View.VISIBLE
                binding.dashProgress8.visibility = View.VISIBLE
                binding.dashProgress9.visibility = View.VISIBLE
                binding.dashProgress10.visibility = View.VISIBLE
                binding.dashProgress11.visibility = View.VISIBLE
                binding.dashProgress12.visibility = View.VISIBLE
                binding.dashProgress13.visibility = View.VISIBLE
                binding.dashProgress14.visibility = View.VISIBLE
                binding.dashProgress15.visibility = View.VISIBLE
                binding.dashProgress16.visibility = View.VISIBLE
                binding.dashProgress17.visibility = View.VISIBLE
                binding.dashProgress18.visibility = View.VISIBLE
                binding.dashProgress19.visibility = View.GONE
                binding.dashProgress20.visibility = View.GONE
                binding.dashProgress21.visibility = View.GONE
                binding.dashProgress22.visibility = View.GONE
                binding.dashProgress23.visibility = View.GONE
                binding.dashProgress24.visibility = View.GONE
                binding.dashProgress25.visibility = View.GONE
            }
            19 -> {
                binding.dashProgress1.visibility = View.VISIBLE
                binding.dashProgress2.visibility = View.VISIBLE
                binding.dashProgress3.visibility = View.VISIBLE
                binding.dashProgress4.visibility = View.VISIBLE
                binding.dashProgress5.visibility = View.VISIBLE
                binding.dashProgress6.visibility = View.VISIBLE
                binding.dashProgress7.visibility = View.VISIBLE
                binding.dashProgress8.visibility = View.VISIBLE
                binding.dashProgress9.visibility = View.VISIBLE
                binding.dashProgress10.visibility = View.VISIBLE
                binding.dashProgress11.visibility = View.VISIBLE
                binding.dashProgress12.visibility = View.VISIBLE
                binding.dashProgress13.visibility = View.VISIBLE
                binding.dashProgress14.visibility = View.VISIBLE
                binding.dashProgress15.visibility = View.VISIBLE
                binding.dashProgress16.visibility = View.VISIBLE
                binding.dashProgress17.visibility = View.VISIBLE
                binding.dashProgress18.visibility = View.VISIBLE
                binding.dashProgress19.visibility = View.VISIBLE
                binding.dashProgress20.visibility = View.GONE
                binding.dashProgress21.visibility = View.GONE
                binding.dashProgress22.visibility = View.GONE
                binding.dashProgress23.visibility = View.GONE
                binding.dashProgress24.visibility = View.GONE
                binding.dashProgress25.visibility = View.GONE
            }
            20 -> {
                binding.dashProgress1.visibility = View.VISIBLE
                binding.dashProgress2.visibility = View.VISIBLE
                binding.dashProgress3.visibility = View.VISIBLE
                binding.dashProgress4.visibility = View.VISIBLE
                binding.dashProgress5.visibility = View.VISIBLE
                binding.dashProgress6.visibility = View.VISIBLE
                binding.dashProgress7.visibility = View.VISIBLE
                binding.dashProgress8.visibility = View.VISIBLE
                binding.dashProgress9.visibility = View.VISIBLE
                binding.dashProgress10.visibility = View.VISIBLE
                binding.dashProgress11.visibility = View.VISIBLE
                binding.dashProgress12.visibility = View.VISIBLE
                binding.dashProgress13.visibility = View.VISIBLE
                binding.dashProgress14.visibility = View.VISIBLE
                binding.dashProgress15.visibility = View.VISIBLE
                binding.dashProgress16.visibility = View.VISIBLE
                binding.dashProgress17.visibility = View.VISIBLE
                binding.dashProgress18.visibility = View.VISIBLE
                binding.dashProgress19.visibility = View.VISIBLE
                binding.dashProgress20.visibility = View.VISIBLE
                binding.dashProgress21.visibility = View.GONE
                binding.dashProgress22.visibility = View.GONE
                binding.dashProgress23.visibility = View.GONE
                binding.dashProgress24.visibility = View.GONE
                binding.dashProgress25.visibility = View.GONE
            }
            21 -> {
                binding.dashProgress1.visibility = View.VISIBLE
                binding.dashProgress2.visibility = View.VISIBLE
                binding.dashProgress3.visibility = View.VISIBLE
                binding.dashProgress4.visibility = View.VISIBLE
                binding.dashProgress5.visibility = View.VISIBLE
                binding.dashProgress6.visibility = View.VISIBLE
                binding.dashProgress7.visibility = View.VISIBLE
                binding.dashProgress8.visibility = View.VISIBLE
                binding.dashProgress9.visibility = View.VISIBLE
                binding.dashProgress10.visibility = View.VISIBLE
                binding.dashProgress11.visibility = View.VISIBLE
                binding.dashProgress12.visibility = View.VISIBLE
                binding.dashProgress13.visibility = View.VISIBLE
                binding.dashProgress14.visibility = View.VISIBLE
                binding.dashProgress15.visibility = View.VISIBLE
                binding.dashProgress16.visibility = View.VISIBLE
                binding.dashProgress17.visibility = View.VISIBLE
                binding.dashProgress18.visibility = View.VISIBLE
                binding.dashProgress19.visibility = View.VISIBLE
                binding.dashProgress20.visibility = View.VISIBLE
                binding.dashProgress21.visibility = View.VISIBLE
                binding.dashProgress22.visibility = View.GONE
                binding.dashProgress23.visibility = View.GONE
                binding.dashProgress24.visibility = View.GONE
                binding.dashProgress25.visibility = View.GONE
            }
            22 -> {
                binding.dashProgress1.visibility = View.VISIBLE
                binding.dashProgress2.visibility = View.VISIBLE
                binding.dashProgress3.visibility = View.VISIBLE
                binding.dashProgress4.visibility = View.VISIBLE
                binding.dashProgress5.visibility = View.VISIBLE
                binding.dashProgress6.visibility = View.VISIBLE
                binding.dashProgress7.visibility = View.VISIBLE
                binding.dashProgress8.visibility = View.VISIBLE
                binding.dashProgress9.visibility = View.VISIBLE
                binding.dashProgress10.visibility = View.VISIBLE
                binding.dashProgress11.visibility = View.VISIBLE
                binding.dashProgress12.visibility = View.VISIBLE
                binding.dashProgress13.visibility = View.VISIBLE
                binding.dashProgress14.visibility = View.VISIBLE
                binding.dashProgress15.visibility = View.VISIBLE
                binding.dashProgress16.visibility = View.VISIBLE
                binding.dashProgress17.visibility = View.VISIBLE
                binding.dashProgress18.visibility = View.VISIBLE
                binding.dashProgress19.visibility = View.VISIBLE
                binding.dashProgress20.visibility = View.VISIBLE
                binding.dashProgress21.visibility = View.VISIBLE
                binding.dashProgress22.visibility = View.VISIBLE
                binding.dashProgress23.visibility = View.GONE
                binding.dashProgress24.visibility = View.GONE
                binding.dashProgress25.visibility = View.GONE
            }
            22 -> {
                binding.dashProgress1.visibility = View.VISIBLE
                binding.dashProgress2.visibility = View.VISIBLE
                binding.dashProgress3.visibility = View.VISIBLE
                binding.dashProgress4.visibility = View.VISIBLE
                binding.dashProgress5.visibility = View.VISIBLE
                binding.dashProgress6.visibility = View.VISIBLE
                binding.dashProgress7.visibility = View.VISIBLE
                binding.dashProgress8.visibility = View.VISIBLE
                binding.dashProgress9.visibility = View.VISIBLE
                binding.dashProgress10.visibility = View.VISIBLE
                binding.dashProgress11.visibility = View.VISIBLE
                binding.dashProgress12.visibility = View.VISIBLE
                binding.dashProgress13.visibility = View.VISIBLE
                binding.dashProgress14.visibility = View.VISIBLE
                binding.dashProgress15.visibility = View.VISIBLE
                binding.dashProgress16.visibility = View.VISIBLE
                binding.dashProgress17.visibility = View.VISIBLE
                binding.dashProgress18.visibility = View.VISIBLE
                binding.dashProgress19.visibility = View.VISIBLE
                binding.dashProgress20.visibility = View.VISIBLE
                binding.dashProgress21.visibility = View.VISIBLE
                binding.dashProgress22.visibility = View.VISIBLE
                binding.dashProgress23.visibility = View.GONE
                binding.dashProgress24.visibility = View.GONE
                binding.dashProgress25.visibility = View.GONE
            }
            23 -> {
                binding.dashProgress1.visibility = View.VISIBLE
                binding.dashProgress2.visibility = View.VISIBLE
                binding.dashProgress3.visibility = View.VISIBLE
                binding.dashProgress4.visibility = View.VISIBLE
                binding.dashProgress5.visibility = View.VISIBLE
                binding.dashProgress6.visibility = View.VISIBLE
                binding.dashProgress7.visibility = View.VISIBLE
                binding.dashProgress8.visibility = View.VISIBLE
                binding.dashProgress9.visibility = View.VISIBLE
                binding.dashProgress10.visibility = View.VISIBLE
                binding.dashProgress11.visibility = View.VISIBLE
                binding.dashProgress12.visibility = View.VISIBLE
                binding.dashProgress13.visibility = View.VISIBLE
                binding.dashProgress14.visibility = View.VISIBLE
                binding.dashProgress15.visibility = View.VISIBLE
                binding.dashProgress16.visibility = View.VISIBLE
                binding.dashProgress17.visibility = View.VISIBLE
                binding.dashProgress18.visibility = View.VISIBLE
                binding.dashProgress19.visibility = View.VISIBLE
                binding.dashProgress20.visibility = View.VISIBLE
                binding.dashProgress21.visibility = View.VISIBLE
                binding.dashProgress22.visibility = View.VISIBLE
                binding.dashProgress23.visibility = View.VISIBLE
                binding.dashProgress24.visibility = View.GONE
                binding.dashProgress25.visibility = View.GONE
            }
            24 -> {
                binding.dashProgress1.visibility = View.VISIBLE
                binding.dashProgress2.visibility = View.VISIBLE
                binding.dashProgress3.visibility = View.VISIBLE
                binding.dashProgress4.visibility = View.VISIBLE
                binding.dashProgress5.visibility = View.VISIBLE
                binding.dashProgress6.visibility = View.VISIBLE
                binding.dashProgress7.visibility = View.VISIBLE
                binding.dashProgress8.visibility = View.VISIBLE
                binding.dashProgress9.visibility = View.VISIBLE
                binding.dashProgress10.visibility = View.VISIBLE
                binding.dashProgress11.visibility = View.VISIBLE
                binding.dashProgress12.visibility = View.VISIBLE
                binding.dashProgress13.visibility = View.VISIBLE
                binding.dashProgress14.visibility = View.VISIBLE
                binding.dashProgress15.visibility = View.VISIBLE
                binding.dashProgress16.visibility = View.VISIBLE
                binding.dashProgress17.visibility = View.VISIBLE
                binding.dashProgress18.visibility = View.VISIBLE
                binding.dashProgress19.visibility = View.VISIBLE
                binding.dashProgress20.visibility = View.VISIBLE
                binding.dashProgress21.visibility = View.VISIBLE
                binding.dashProgress22.visibility = View.VISIBLE
                binding.dashProgress23.visibility = View.VISIBLE
                binding.dashProgress24.visibility = View.VISIBLE
                binding.dashProgress25.visibility = View.GONE
            }
            25 -> {
                binding.dashProgress1.visibility = View.VISIBLE
                binding.dashProgress2.visibility = View.VISIBLE
                binding.dashProgress3.visibility = View.VISIBLE
                binding.dashProgress4.visibility = View.VISIBLE
                binding.dashProgress5.visibility = View.VISIBLE
                binding.dashProgress6.visibility = View.VISIBLE
                binding.dashProgress7.visibility = View.VISIBLE
                binding.dashProgress8.visibility = View.VISIBLE
                binding.dashProgress9.visibility = View.VISIBLE
                binding.dashProgress10.visibility = View.VISIBLE
                binding.dashProgress11.visibility = View.VISIBLE
                binding.dashProgress12.visibility = View.VISIBLE
                binding.dashProgress13.visibility = View.VISIBLE
                binding.dashProgress14.visibility = View.VISIBLE
                binding.dashProgress15.visibility = View.VISIBLE
                binding.dashProgress16.visibility = View.VISIBLE
                binding.dashProgress17.visibility = View.VISIBLE
                binding.dashProgress18.visibility = View.VISIBLE
                binding.dashProgress19.visibility = View.VISIBLE
                binding.dashProgress20.visibility = View.VISIBLE
                binding.dashProgress21.visibility = View.VISIBLE
                binding.dashProgress22.visibility = View.VISIBLE
                binding.dashProgress23.visibility = View.VISIBLE
                binding.dashProgress24.visibility = View.VISIBLE
                binding.dashProgress25.visibility = View.VISIBLE
            }
        }
    }

    private fun setCurrentProgress(progressCount: Int) {
        when (progressCount) {
            1 -> {
                binding.dashProgress1.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress2.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress3.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress4.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress5.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress6.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress7.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress8.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress9.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress10.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress11.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress12.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress13.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress14.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress15.setBackgroundResource(R.drawable.dash_progress_bar)
            }
            2 -> {
                binding.dashProgress1.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress2.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress3.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress4.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress5.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress6.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress7.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress8.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress9.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress10.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress11.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress12.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress13.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress14.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress15.setBackgroundResource(R.drawable.dash_progress_bar)
            }
            3 -> {
                binding.dashProgress1.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress2.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress3.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress4.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress5.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress6.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress7.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress8.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress9.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress10.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress11.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress12.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress13.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress14.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress15.setBackgroundResource(R.drawable.dash_progress_bar)
            }
            4 -> {
                binding.dashProgress1.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress2.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress3.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress4.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress5.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress6.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress7.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress8.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress9.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress10.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress11.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress12.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress13.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress14.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress15.setBackgroundResource(R.drawable.dash_progress_bar)
            }
            5 -> {
                binding.dashProgress1.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress2.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress3.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress4.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress5.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress6.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress7.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress8.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress9.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress10.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress11.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress12.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress13.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress14.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress15.setBackgroundResource(R.drawable.dash_progress_bar)
            }
            6 -> {
                binding.dashProgress1.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress2.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress3.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress4.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress5.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress6.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress7.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress8.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress9.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress10.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress11.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress12.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress13.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress14.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress15.setBackgroundResource(R.drawable.dash_progress_bar)
            }
            7 -> {
                binding.dashProgress1.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress2.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress3.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress4.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress5.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress6.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress7.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress8.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress9.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress10.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress11.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress12.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress13.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress14.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress15.setBackgroundResource(R.drawable.dash_progress_bar)
            }
            8 -> {
                binding.dashProgress1.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress2.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress3.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress4.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress5.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress6.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress7.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress8.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress9.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress10.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress11.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress12.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress13.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress14.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress15.setBackgroundResource(R.drawable.dash_progress_bar)
            }
            9 -> {
                binding.dashProgress1.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress2.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress3.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress4.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress5.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress6.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress7.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress8.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress9.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress10.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress11.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress12.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress13.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress14.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress15.setBackgroundResource(R.drawable.dash_progress_bar)
            }
            10 -> {
                binding.dashProgress1.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress2.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress3.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress4.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress5.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress6.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress7.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress8.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress9.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress10.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress11.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress12.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress13.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress14.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress15.setBackgroundResource(R.drawable.dash_progress_bar)
            }
            11 -> {
                binding.dashProgress1.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress2.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress3.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress4.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress5.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress6.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress7.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress8.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress9.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress10.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress11.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress12.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress13.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress14.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress15.setBackgroundResource(R.drawable.dash_progress_bar)
            }
            12 -> {
                binding.dashProgress1.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress2.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress3.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress4.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress5.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress6.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress7.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress8.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress9.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress10.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress11.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress12.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress13.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress14.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress15.setBackgroundResource(R.drawable.dash_progress_bar)
            }
            13 -> {
                binding.dashProgress1.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress2.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress3.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress4.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress5.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress6.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress7.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress8.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress9.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress10.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress11.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress12.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress13.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress14.setBackgroundResource(R.drawable.dash_progress_bar)
                binding.dashProgress15.setBackgroundResource(R.drawable.dash_progress_bar)
            }
            14 -> {
                binding.dashProgress1.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress2.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress3.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress4.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress5.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress6.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress7.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress8.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress9.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress10.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress11.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress12.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress13.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress14.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress15.setBackgroundResource(R.drawable.dash_progress_bar)
            }
            15 -> {
                binding.dashProgress1.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress2.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress3.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress4.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress5.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress6.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress7.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress8.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress9.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress10.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress11.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress12.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress13.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress14.setBackgroundResource(R.drawable.dash_white_progress_bar)
                binding.dashProgress15.setBackgroundResource(R.drawable.dash_white_progress_bar)
            }
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

    var selectedOptions: ArrayList<Int> = arrayListOf()
    var optionOne: Boolean = false
    var optionTwo: Boolean = false
    var optionThree: Boolean = false
    var optionFour: Boolean = false
    var optionFive: Boolean = false
    var optionSix: Boolean = false
    var optionSeven: Boolean = false
    var optionEight: Boolean = false
    var optionNine: Boolean = false
    var optionTen: Boolean = false
    var optionEleven: Boolean = false
    var optionTwelve: Boolean = false
    var optionThirteen: Boolean = false
    var optionFourteen: Boolean = false
    var optionFifteen: Boolean = false
    var nextQuestionId: String = ""

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.cv_option_one -> {
                if (question!!.options[0].option_id != null) {
                    nextQuestionId = question!!.options[0].option_id
                } else {
                    nextQuestionId = question!!.next
                }
                if (!optionOne) {
                    multipleSelectionCount += 1
                    selectedOptions.add(question!!.options[0].id)
                } else {
                    multipleSelectionCount -= 1
                    selectedOptions.remove(question!!.options[0].id)
                }
                optionOne = !optionOne
                if (binding.tvOptionOne.text.toString().lowercase().contains("other")) {
                    if (optionOne) {
                        binding.cvEdit.visibility = View.VISIBLE
                        binding.scrollViewOptions.post {
                            binding.scrollViewOptions.fullScroll(View.FOCUS_DOWN)
                        }
                    } else {
                        binding.etOthers.setText("")
                        binding.cvEdit.visibility = View.GONE
                    }
                } else if (binding.tvOptionOne.text.toString().lowercase().contains("suicidal")) {
                    binding.layoutResourcesHelp.visibility = View.VISIBLE
                    binding.layoutQuestionnaire.visibility = View.GONE
                    //displayCrisis()
                }
                if (!question!!.is_multiple) {
                    selectedOptionsView(
                        binding.tvOptionOne,
                        selectedOptions,
                        question!!.question_id,
                        nextQuestionId
                    )
                } else {
                    optionSelection(binding.tvOptionOne, optionOne)
                }
            }
            R.id.cv_option_two -> {
                if (question!!.options[1].option_id != null) {
                    nextQuestionId = question!!.options[1].option_id
                } else {
                    nextQuestionId = question!!.next
                }
                if (!optionTwo) {
                    multipleSelectionCount += 1
                    selectedOptions.add(question!!.options[1].id)
                } else {
                    multipleSelectionCount -= 1
                    selectedOptions.remove(question!!.options[1].id)
                }
                optionTwo = !optionTwo
                if (binding.tvOptionTwo.text.toString().lowercase().contains("other")) {
                    if (optionTwo) {
                        binding.cvEdit.visibility = View.VISIBLE
                        binding.scrollViewOptions.post {
                            binding.scrollViewOptions.fullScroll(View.FOCUS_DOWN)
                        }
                    } else {
                        binding.etOthers.setText("")
                        binding.cvEdit.visibility = View.GONE
                    }
                } else if (binding.tvOptionTwo.text.toString().lowercase().contains("homicidal")) {
                    //displayCrisis()
                    binding.layoutResourcesHelp.visibility = View.VISIBLE
                    binding.layoutQuestionnaire.visibility = View.GONE
                }
                if (!question!!.is_multiple) {
                    selectedOptionsView(
                        binding.tvOptionTwo,
                        selectedOptions,
                        question!!.question_id,
                        nextQuestionId
                    )
                } else {
                    optionSelection(binding.tvOptionTwo, optionTwo)
                }
            }
            R.id.cv_option_three -> {
                if (question!!.options[2].option_id != null) {
                    nextQuestionId = question!!.options[2].option_id
                } else {
                    nextQuestionId = question!!.next
                }
                if (!optionThree) {
                    multipleSelectionCount += 1
                    selectedOptions.add(question!!.options[2].id)
                } else {
                    multipleSelectionCount -= 1
                    selectedOptions.remove(question!!.options[2].id)
                }
                optionThree = !optionThree
                if (binding.tvOptionThree.text.toString().lowercase().contains("other")) {
                    if (optionThree) {
                        binding.cvEdit.visibility = View.VISIBLE
                        binding.scrollViewOptions.post {
                            binding.scrollViewOptions.fullScroll(View.FOCUS_DOWN)
                        }
                    } else {
                        binding.etOthers.setText("")
                        binding.cvEdit.visibility = View.GONE
                    }
                }
                if (!question!!.is_multiple) {
                    selectedOptionsView(
                        binding.tvOptionThree,
                        selectedOptions,
                        question!!.question_id,
                        nextQuestionId
                    )
                } else {
                    optionSelection(binding.tvOptionThree, optionThree)
                }
            }
            R.id.cv_option_four -> {
                if (question!!.options[3].option_id != null) {
                    nextQuestionId = question!!.options[3].option_id
                } else {
                    nextQuestionId = question!!.next
                }
                if (!optionFour) {
                    multipleSelectionCount += 1
                    selectedOptions.add(question!!.options[3].id)
                } else {
                    multipleSelectionCount -= 1
                    selectedOptions.remove(question!!.options[3].id)
                }
                optionFour = !optionFour
                if (binding.tvOptionFour.text.toString().lowercase().contains("other")) {
                    if (optionFour) {
                        binding.cvEdit.visibility = View.VISIBLE
                        binding.scrollViewOptions.post {
                            binding.scrollViewOptions.fullScroll(View.FOCUS_DOWN)
                        }
                    } else {
                        binding.etOthers.setText("")
                        binding.cvEdit.visibility = View.GONE
                    }
                }
                if (!question!!.is_multiple) {
                    selectedOptionsView(
                        binding.tvOptionFour,
                        selectedOptions,
                        question!!.question_id,
                        nextQuestionId
                    )
                } else {
                    optionSelection(binding.tvOptionFour, optionFour)
                }
            }
            R.id.cv_option_five -> {
                if (question!!.options[4].option_id != null) {
                    nextQuestionId = question!!.options[4].option_id
                } else {
                    nextQuestionId = question!!.next
                }
                if (!optionFive) {
                    multipleSelectionCount += 1
                    selectedOptions.add(question!!.options[4].id)
                } else {
                    multipleSelectionCount -= 1
                    selectedOptions.remove(question!!.options[4].id)
                }
                optionFive = !optionFive
                if (binding.tvOptionFive.text.toString().lowercase().contains("other")) {
                    if (optionFive) {
                        binding.cvEdit.visibility = View.VISIBLE
                        binding.scrollViewOptions.post {
                            binding.scrollViewOptions.fullScroll(View.FOCUS_DOWN)
                        }
                    } else {
                        binding.etOthers.setText("")
                        binding.cvEdit.visibility = View.GONE
                    }
                }
                if (!question!!.is_multiple) {
                    selectedOptionsView(
                        binding.tvOptionFive,
                        selectedOptions,
                        question!!.question_id,
                        nextQuestionId
                    )
                } else {
                    optionSelection(binding.tvOptionFive, optionFive)
                }
            }
            R.id.cv_option_six -> {
                if (question!!.options[5].option_id != null) {
                    nextQuestionId = question!!.options[5].option_id
                } else {
                    nextQuestionId = question!!.next
                }
                if (!optionSix) {
                    multipleSelectionCount += 1
                    selectedOptions.add(question!!.options[5].id)
                } else {
                    multipleSelectionCount -= 1
                    selectedOptions.remove(question!!.options[5].id)
                }
                optionSix = !optionSix
                if (binding.tvOptionSix.text.toString().lowercase().contains("other")) {
                    if (optionSix) {
                        binding.cvEdit.visibility = View.VISIBLE
                        binding.scrollViewOptions.post {
                            binding.scrollViewOptions.fullScroll(View.FOCUS_DOWN)
                        }
                    } else {
                        binding.etOthers.setText("")
                        binding.cvEdit.visibility = View.GONE
                    }
                }
                if (!question!!.is_multiple) {
                    selectedOptionsView(
                        binding.tvOptionSix,
                        selectedOptions,
                        question!!.question_id,
                        nextQuestionId
                    )
                } else {
                    optionSelection(binding.tvOptionSix, optionSix)
                }
            }
            R.id.cv_option_seven -> {
                if (question!!.options[6].option_id != null) {
                    nextQuestionId = question!!.options[6].option_id
                } else {
                    nextQuestionId = question!!.next
                }
                if (!optionSeven) {
                    multipleSelectionCount += 1
                    selectedOptions.add(question!!.options[6].id)
                } else {
                    multipleSelectionCount -= 1
                    selectedOptions.remove(question!!.options[6].id)
                }
                optionSeven = !optionSeven
                if (binding.tvOptionSeven.text.toString().lowercase().contains("other")) {
                    if (optionSeven) {
                        binding.cvEdit.visibility = View.VISIBLE
                        binding.scrollViewOptions.post {
                            binding.scrollViewOptions.fullScroll(View.FOCUS_DOWN)
                        }
                    } else {
                        binding.etOthers.setText("")
                        binding.cvEdit.visibility = View.GONE
                    }
                }
                if (!question!!.is_multiple) {
                    selectedOptionsView(
                        binding.tvOptionSeven,
                        selectedOptions,
                        question!!.question_id,
                        nextQuestionId
                    )
                } else {
                    optionSelection(binding.tvOptionSeven, optionSeven)
                }
            }
            R.id.cv_option_eight -> {
                if (question!!.options[7].option_id != null) {
                    nextQuestionId = question!!.options[7].option_id
                } else {
                    nextQuestionId = question!!.next
                }
                if (!optionEight) {
                    multipleSelectionCount += 1
                    selectedOptions.add(question!!.options[7].id)
                } else {
                    multipleSelectionCount -= 1
                    selectedOptions.remove(question!!.options[7].id)
                }
                optionEight = !optionEight
                if (binding.tvOptionEight.text.toString().lowercase().contains("other")) {
                    if (optionEight) {
                        binding.cvEdit.visibility = View.VISIBLE
                        binding.scrollViewOptions.post {
                            binding.scrollViewOptions.fullScroll(View.FOCUS_DOWN)
                        }
                    } else {
                        binding.etOthers.setText("")
                        binding.cvEdit.visibility = View.GONE
                    }
                }
                if (!question!!.is_multiple) {
                    selectedOptionsView(
                        binding.tvOptionEight,
                        selectedOptions,
                        question!!.question_id,
                        nextQuestionId
                    )
                } else {
                    optionSelection(binding.tvOptionEight, optionEight)
                }
            }
            R.id.cv_option_nine -> {
                if (question!!.options[8].option_id != null) {
                    nextQuestionId = question!!.options[8].option_id
                } else {
                    nextQuestionId = question!!.next
                }
                if (!optionNine) {
                    multipleSelectionCount += 1
                    selectedOptions.add(question!!.options[8].id)
                } else {
                    multipleSelectionCount -= 1
                    selectedOptions.remove(question!!.options[8].id)
                }
                optionNine = !optionNine
                if (binding.tvOptionNine.text.toString().lowercase().contains("other")) {
                    if (optionNine) {
                        binding.cvEdit.visibility = View.VISIBLE
                        binding.scrollViewOptions.post {
                            binding.scrollViewOptions.fullScroll(View.FOCUS_DOWN)
                        }
                    } else {
                        binding.etOthers.setText("")
                        binding.cvEdit.visibility = View.GONE
                    }
                }
                if (!question!!.is_multiple) {
                    selectedOptionsView(
                        binding.tvOptionNine,
                        selectedOptions,
                        question!!.question_id,
                        nextQuestionId
                    )
                } else {
                    optionSelection(binding.tvOptionNine, optionNine)
                }
            }
            R.id.cv_option_ten -> {
                if (question!!.options[9].option_id != null) {
                    nextQuestionId = question!!.options[9].option_id
                } else {
                    nextQuestionId = question!!.next
                }
                if (!optionTen) {
                    multipleSelectionCount += 1
                    selectedOptions.add(question!!.options[9].id)
                } else {
                    multipleSelectionCount -= 1
                    selectedOptions.remove(question!!.options[9].id)
                }
                optionTen = !optionTen
                if (binding.tvOptionTen.text.toString().lowercase().contains("other")) {
                    if (optionTen) {
                        binding.cvEdit.visibility = View.VISIBLE
                        binding.scrollViewOptions.post {
                            binding.scrollViewOptions.fullScroll(View.FOCUS_DOWN)
                        }
                    } else {
                        binding.etOthers.setText("")
                        binding.cvEdit.visibility = View.GONE
                    }
                }
                if (!question!!.is_multiple) {
                    selectedOptionsView(
                        binding.tvOptionTen,
                        selectedOptions,
                        question!!.question_id,
                        nextQuestionId
                    )
                } else {
                    optionSelection(binding.tvOptionTen, optionTen)
                }
            }
            R.id.cv_option_eleven -> {
                if (question!!.options[10].option_id != null) {
                    nextQuestionId = question!!.options[10].option_id
                } else {
                    nextQuestionId = question!!.next
                }
                if (!optionEleven) {
                    multipleSelectionCount += 1
                    selectedOptions.add(question!!.options[10].id)
                } else {
                    multipleSelectionCount -= 1
                    selectedOptions.remove(question!!.options[10].id)
                }
                optionEleven = !optionEleven
                if (binding.tvOptionEleven.text.toString().lowercase().contains("other")) {
                    if (optionEleven) {
                        binding.cvEdit.visibility = View.VISIBLE
                        binding.scrollViewOptions.post {
                            binding.scrollViewOptions.fullScroll(View.FOCUS_DOWN)
                        }
                    } else {
                        binding.etOthers.setText("")
                        binding.cvEdit.visibility = View.GONE
                    }
                }
                if (!question!!.is_multiple) {
                    selectedOptionsView(
                        binding.tvOptionEleven,
                        selectedOptions,
                        question!!.question_id,
                        nextQuestionId
                    )
                } else {
                    optionSelection(binding.tvOptionEleven, optionEleven)
                }
            }
            R.id.cv_option_twelve -> {
                if (question!!.options[11].option_id != null) {
                    nextQuestionId = question!!.options[11].option_id
                } else {
                    nextQuestionId = question!!.next
                }
                if (!optionTwelve) {
                    multipleSelectionCount += 1
                    selectedOptions.add(question!!.options[11].id)
                } else {
                    multipleSelectionCount -= 1
                    selectedOptions.remove(question!!.options[11].id)
                }
                optionTwelve = !optionTwelve
                if (binding.tvOptionTwelve.text.toString().lowercase().contains("other")) {
                    if (optionTwelve) {
                        binding.cvEdit.visibility = View.VISIBLE
                        binding.scrollViewOptions.post {
                            binding.scrollViewOptions.fullScroll(View.FOCUS_DOWN)
                        }
                    } else {
                        binding.etOthers.setText("")
                        binding.cvEdit.visibility = View.GONE
                    }
                }
                if (!question!!.is_multiple) {
                    selectedOptionsView(
                        binding.tvOptionTwelve,
                        selectedOptions,
                        question!!.question_id,
                        nextQuestionId
                    )
                } else {
                    optionSelection(binding.tvOptionTwelve, optionTwelve)
                }
            }
            R.id.cv_option_thirteen -> {
                if (question!!.options[12].option_id != null) {
                    nextQuestionId = question!!.options[12].option_id
                } else {
                    nextQuestionId = question!!.next
                }
                if (!optionThirteen) {
                    multipleSelectionCount += 1
                    selectedOptions.add(question!!.options[12].id)
                } else {
                    multipleSelectionCount -= 1
                    selectedOptions.remove(question!!.options[12].id)
                }
                optionThirteen = !optionThirteen
                if (binding.tvOptionThirteen.text.toString().lowercase().contains("other")) {
                    if (optionThirteen) {
                        binding.cvEdit.visibility = View.VISIBLE
                        binding.scrollViewOptions.post {
                            binding.scrollViewOptions.fullScroll(View.FOCUS_DOWN)
                        }
                    } else {
                        binding.etOthers.setText("")
                        binding.cvEdit.visibility = View.GONE
                    }
                }
                if (!question!!.is_multiple) {
                    selectedOptionsView(
                        binding.tvOptionThirteen,
                        selectedOptions,
                        question!!.question_id,
                        nextQuestionId
                    )
                } else {
                    optionSelection(binding.tvOptionThirteen, optionThirteen)
                }
            }
            R.id.cv_option_fourteen -> {
                if (question!!.options[13].option_id != null) {
                    nextQuestionId = question!!.options[13].option_id
                } else {
                    nextQuestionId = question!!.next
                }
                if (!optionFourteen) {
                    multipleSelectionCount += 1
                    selectedOptions.add(question!!.options[13].id)
                } else {
                    multipleSelectionCount -= 1
                    selectedOptions.remove(question!!.options[13].id)
                }
                optionFourteen = !optionFourteen
                if (binding.tvOptionFourteen.text.toString().lowercase().contains("other")) {
                    if (optionFourteen) {
                        binding.cvEdit.visibility = View.VISIBLE
                        binding.scrollViewOptions.post {
                            binding.scrollViewOptions.fullScroll(View.FOCUS_DOWN)
                        }
                    } else {
                        binding.etOthers.setText("")
                        binding.cvEdit.visibility = View.GONE
                    }
                }
                if (!question!!.is_multiple) {
                    selectedOptionsView(
                        binding.tvOptionFourteen,
                        selectedOptions,
                        question!!.question_id,
                        nextQuestionId
                    )
                } else {
                    optionSelection(binding.tvOptionFourteen, optionFourteen)
                }
            }
            R.id.cv_option_fifteen -> {
                if (question!!.options[14].option_id != null) {
                    nextQuestionId = question!!.options[14].option_id
                } else {
                    nextQuestionId = question!!.next
                }
                if (!optionFifteen) {
                    multipleSelectionCount += 1
                    selectedOptions.add(question!!.options[14].id)
                } else {
                    multipleSelectionCount -= 1
                    selectedOptions.remove(question!!.options[14].id)
                }
                optionFifteen = !optionFifteen
                if (binding.tvOptionFifteen.text.toString().lowercase().contains("other")) {
                    if (optionFifteen) {
                        binding.cvEdit.visibility = View.VISIBLE
                        binding.scrollViewOptions.post {
                            binding.scrollViewOptions.fullScroll(View.FOCUS_DOWN)
                        }
                    } else {
                        binding.etOthers.setText("")
                        binding.cvEdit.visibility = View.GONE
                    }
                }
                if (!question!!.is_multiple) {
                    selectedOptionsView(
                        binding.tvOptionFifteen,
                        selectedOptions,
                        question!!.question_id,
                        nextQuestionId
                    )
                } else {
                    optionSelection(binding.tvOptionFifteen, optionFifteen)
                }
            }
            R.id.btnQuestionnaireContinue -> {
                if (multipleSelectionCount > 0) {
                    if (question!!.is_multiple)
                        selectedOptionsView(
                            binding.tvOptionFifteen,
                            selectedOptions,
                            question!!.question_id,
                            nextQuestionId
                        )
                } else {
                    displayMsg("Alert", "Select the option.")
                }
            }
            R.id.btnResourceContinue -> {
                val builder = AlertDialog.Builder(mActivity!!)
                builder.setTitle("Confirmation")
                builder.setMessage("Do you wish to close the resource crisis screen?")
                builder.setPositiveButton("Yes") { dialog, which ->
                    binding.layoutResourcesHelp.visibility = View.GONE
                    binding.layoutQuestionnaire.visibility = View.VISIBLE
                }

                builder.setNegativeButton("No") { dialog, which ->
                    dialog.dismiss()
                }
                builder.setCancelable(false)
                builder.show()
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