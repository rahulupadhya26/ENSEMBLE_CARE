package com.app.selfcare.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.R
import com.app.selfcare.adapters.CarePlanDayListAdapter
import com.app.selfcare.adapters.CarePlanMindfulnessTaskListAdapter
import com.app.selfcare.controller.OnCarePlanDayItemClickListener
import com.app.selfcare.data.CareDayIndividualTaskDetail
import com.app.selfcare.data.DayWiseCarePlan
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_mindfulness_care_plan.*
import java.lang.reflect.Type

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MindfulnessCarePlanFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MindfulnessCarePlanFragment : BaseFragment(), OnCarePlanDayItemClickListener {
    // TODO: Rename and change types of parameters
    private var mindfulnessCarePlanDayNumber: Int = 0
    private var param2: String? = null
    private var selectedDayNo: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mindfulnessCarePlanDayNumber = it.getInt(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
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
        carePlanMindfulnessBack.setOnClickListener {
            popBackStack()
        }
        getMindfulnessCarePlanTaskDetails(selectedDayNo)
    }

    private fun getMindfulnessCarePlanTaskDetails(dayNumber: Int) {
        getDayWiseCarePlanData(dayNumber) { response ->
            val dayWiseCarePlanType: Type =
                object : TypeToken<DayWiseCarePlan?>() {}.type
            val dayWiseCarePlan: DayWiseCarePlan =
                Gson().fromJson(response, dayWiseCarePlanType)

            recyclerViewCarePlanMindfulnessDayList.apply {
                layoutManager = LinearLayoutManager(
                    requireActivity(), RecyclerView.HORIZONTAL, false
                )
                adapter = CarePlanDayListAdapter(
                    mActivity!!,
                    dayWiseCarePlan.total_days, selectedDayNo, "Mindfulness",this@MindfulnessCarePlanFragment
                )
            }
            updateMindfulnessTodayTasks(dayWiseCarePlan.plan.tasks.mindfulness)
        }
    }

    private fun updateMindfulnessTodayTasks(mindfulnessTaskDetails: ArrayList<CareDayIndividualTaskDetail>) {
        recyclerViewCarePlanMindfulnessTask.apply {
            layoutManager = LinearLayoutManager(
                requireActivity(), RecyclerView.VERTICAL, false
            )
            adapter = CarePlanMindfulnessTaskListAdapter(
                mActivity!!,
                mindfulnessTaskDetails
            )
        }
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
}