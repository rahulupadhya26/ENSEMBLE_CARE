package com.app.selfcare.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.R
import com.app.selfcare.adapters.CarePlanDayAdapter
import com.app.selfcare.adapters.CarePlanDayListAdapter
import com.app.selfcare.adapters.CarePlanExerciseTaskListAdapter
import com.app.selfcare.controller.OnCarePlanDayItemClickListener
import com.app.selfcare.controller.OnCarePlanPendingTaskItemClickListener
import com.app.selfcare.data.*
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper.get
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_care_plan_dashboard.*
import kotlinx.android.synthetic.main.fragment_exercise_care_plan.*
import retrofit2.HttpException
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
 * Use the [ExerciseCarePlanFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ExerciseCarePlanFragment : BaseFragment(), OnCarePlanDayItemClickListener,
    OnCarePlanPendingTaskItemClickListener {
    // TODO: Rename and change types of parameters
    private var exerciseCarePlanDayNumber: Int = 0
    private var param2: String? = null
    private var selectedDayNo: Int = 0
    private lateinit var dayWiseCarePlan: DayWiseCarePlan

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            exerciseCarePlanDayNumber = it.getInt(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_exercise_care_plan
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE
        updateStatusBarColor(R.color.white)
        selectedDayNo = exerciseCarePlanDayNumber

        carePlanExerciseBack.setOnClickListener {
            popBackStack()
        }

        getExerciseCarePlanTaskDetails(selectedDayNo)
    }

    private fun getExerciseCarePlanTaskDetails(dayNumber: Int) {
        getDayWiseCarePlanData(dayNumber) { response ->
            val dayWiseCarePlanType: Type =
                object : TypeToken<DayWiseCarePlan?>() {}.type
            dayWiseCarePlan = Gson().fromJson(response, dayWiseCarePlanType)

            recyclerViewCarePlanExerciseDayList.apply {
                layoutManager = LinearLayoutManager(
                    requireActivity(), RecyclerView.HORIZONTAL, false
                )
                adapter = CarePlanDayListAdapter(
                    mActivity!!,
                    dayWiseCarePlan.total_days,
                    selectedDayNo,
                    "Exercise",
                    this@ExerciseCarePlanFragment
                )
            }
            updateExerciseTodayTasks(dayWiseCarePlan.plan.tasks.exercise)
        }
    }

    private fun updateExerciseTodayTasks(exerciseTaskDetails: ArrayList<CareDayIndividualTaskDetail>) {
        recyclerViewCarePlanExerciseTask.apply {
            layoutManager = LinearLayoutManager(
                requireActivity(), RecyclerView.VERTICAL, false
            )
            adapter = CarePlanExerciseTaskListAdapter(
                mActivity!!,
                exerciseTaskDetails, this@ExerciseCarePlanFragment
            )
        }
    }

    private fun sendCarePlanPendingExerciseTask(detail: CareDayIndividualTaskDetail) {
        showProgress()
        val date: Calendar = Calendar.getInstance()
        val currentDate = SimpleDateFormat("MM/dd/yyyy").format(date.time)
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .sendExerciseCarePlanPendingTask(
                        "PI0052",
                        ExerciseCarePlan(
                            preference!![PrefKeys.PREF_PATIENT_ID, 0]!!,
                            currentDate,
                            dayWiseCarePlan.id,
                            is_completed = true,
                            detail.plan,
                            detail.id,
                            detail.time
                            ),
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
                            getExerciseCarePlanTaskDetails(selectedDayNo)
                        } catch (e: Exception) {
                            hideProgress()
                            e.printStackTrace()
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
                                sendCarePlanPendingExerciseTask(detail)
                            }
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
         * @return A new instance of fragment ExerciseCarePlanFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: Int, param2: String = "") =
            ExerciseCarePlanFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_care_plan_exercise"
    }

    override fun onCarePlanDayItemClickListener(dayNumber: Int) {
        selectedDayNo = dayNumber
        getExerciseCarePlanTaskDetails(dayNumber)
    }

    override fun onCarePlanPendingTaskItemClickListener(careDayIndividualTaskDetail: CareDayIndividualTaskDetail) {
        sendCarePlanPendingExerciseTask(careDayIndividualTaskDetail)
    }
}