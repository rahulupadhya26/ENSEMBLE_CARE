package com.app.selfcare.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.R
import com.app.selfcare.adapters.CarePlanDayListAdapter
import com.app.selfcare.adapters.CarePlanMindfulnessTaskListAdapter
import com.app.selfcare.controller.OnCarePlanDayItemClickListener
import com.app.selfcare.controller.OnCarePlanPendingTaskItemClickListener
import com.app.selfcare.data.*
import com.app.selfcare.databinding.FragmentActivityCarePlanBinding
import com.app.selfcare.databinding.FragmentMindfulnessCarePlanBinding
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper.get
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
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
 * Use the [MindfulnessCarePlanFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MindfulnessCarePlanFragment : BaseFragment(), OnCarePlanDayItemClickListener,
    OnCarePlanPendingTaskItemClickListener {
    // TODO: Rename and change types of parameters
    private var mindfulnessCarePlanDayNumber: Int = 0
    private var param2: String? = null
    private var selectedDayNo: Int = 0
    private lateinit var dayWiseCarePlan: DayWiseCarePlan
    private lateinit var binding: FragmentMindfulnessCarePlanBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mindfulnessCarePlanDayNumber = it.getInt(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMindfulnessCarePlanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getLayout(): Int {
        return R.layout.fragment_mindfulness_care_plan
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE
        updateStatusBarColor(R.color.white)
        selectedDayNo = mindfulnessCarePlanDayNumber
        binding.carePlanMindfulnessBack.setOnClickListener {
            popBackStack()
        }
        getMindfulnessCarePlanTaskDetails(selectedDayNo)
    }

    @SuppressLint("SetTextI18n")
    private fun getMindfulnessCarePlanTaskDetails(dayNumber: Int) {
        getDayWiseCarePlanData(dayNumber) { response ->
            val dayWiseCarePlanType: Type =
                object : TypeToken<DayWiseCarePlan?>() {}.type
            dayWiseCarePlan = Gson().fromJson(response, dayWiseCarePlanType)
            binding.txtMindfulnessCarePlanAssignedBy.text = dayWiseCarePlan.coach.name
            binding.txtMindfulnessCarePlanTaskAssigned.text = "Task assigned " +
                    dayWiseCarePlan.plan.task_completed.completed + "/" + dayWiseCarePlan.plan.task_completed.total
            binding.txtMindfulnessCarePlanCurrentDay.text = "Day " +
                    dayNumber.toString() + "/" + dayWiseCarePlan.total_days
            binding.recyclerViewCarePlanMindfulnessDayList.apply {
                layoutManager = LinearLayoutManager(
                    requireActivity(), RecyclerView.HORIZONTAL, false
                )
                adapter = CarePlanDayListAdapter(
                    mActivity!!,
                    dayWiseCarePlan.total_days,
                    selectedDayNo,
                    "Mindfulness",
                    this@MindfulnessCarePlanFragment
                )
            }
            updateMindfulnessTodayTasks(dayWiseCarePlan.plan.tasks.mindfulness)
        }
    }

    private fun updateMindfulnessTodayTasks(mindfulnessTaskDetails: ArrayList<CareDayIndividualTaskDetail>) {
        binding.recyclerViewCarePlanMindfulnessTask.apply {
            layoutManager = LinearLayoutManager(
                requireActivity(), RecyclerView.VERTICAL, false
            )
            adapter = CarePlanMindfulnessTaskListAdapter(
                mActivity!!,
                mindfulnessTaskDetails, this@MindfulnessCarePlanFragment
            )
        }
    }

    private fun sendCarePlanPendingMindfulnessTask(detail: CareDayIndividualTaskDetail) {
        showProgress()
        val date: Calendar = Calendar.getInstance()
        val currentDate = SimpleDateFormat("MM/dd/yyyy").format(date.time)
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .sendMindfulnessCarePlanPendingTask(
                        "PI0065",
                        MindfulnessCarePlan(
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
                            getMindfulnessCarePlanTaskDetails(selectedDayNo)
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
                                sendCarePlanPendingMindfulnessTask(detail)
                            }
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    private fun sendCarePlanRemoveMindfulnessTask(detail: CareDayIndividualTaskDetail) {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .sendMindfulnessCarePlanDeleteTask(
                        "PI0065",
                        RemoveCarePlan(
                            detail.task_input_id
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
                            getMindfulnessCarePlanTaskDetails(selectedDayNo)
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
                                sendCarePlanRemoveMindfulnessTask(detail)
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
         * @return A new instance of fragment MindfulnessCarePlanFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: Int, param2: String = "") =
            MindfulnessCarePlanFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_care_plan_mindfulness"
    }

    override fun onCarePlanDayItemClickListener(dayNumber: Int) {
        selectedDayNo = dayNumber
        getMindfulnessCarePlanTaskDetails(dayNumber)
    }

    override fun onCarePlanPendingTaskItemClickListener(
        careDayIndividualTaskDetail: CareDayIndividualTaskDetail,
        isCompleted: Boolean
    ) {
        if (isCompleted)
            sendCarePlanPendingMindfulnessTask(careDayIndividualTaskDetail)
        else
            sendCarePlanRemoveMindfulnessTask(careDayIndividualTaskDetail)
    }
}