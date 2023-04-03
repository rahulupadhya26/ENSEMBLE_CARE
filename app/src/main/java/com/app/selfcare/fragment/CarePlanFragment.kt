package com.app.selfcare.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.R
import com.app.selfcare.adapters.CarePlanDayListAdapter
import com.app.selfcare.adapters.CarePlanTodayTaskListAdapter
import com.app.selfcare.controller.OnCarePlanDayItemClickListener
import com.app.selfcare.controller.OnCarePlanTaskItemClickListener
import com.app.selfcare.data.CareDay
import com.app.selfcare.data.CareDayIndividualTaskDetail
import com.app.selfcare.data.CarePlans
import com.app.selfcare.data.DayWiseCarePlan
import com.app.selfcare.databinding.FragmentCarePlanBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CarePlanFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CarePlanFragment : BaseFragment(), OnCarePlanDayItemClickListener,
    OnCarePlanTaskItemClickListener {
    // TODO: Rename and change types of parameters
    private var carePlans: CarePlans? = null
    private var careDay: CareDay? = null
    private var selectedDayNo: Int = 0
    private lateinit var binding: FragmentCarePlanBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            carePlans = it.getParcelable(ARG_PARAM1)
            careDay = it.getParcelable(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCarePlanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getLayout(): Int {
        return R.layout.fragment_care_plan
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE
        updateStatusBarColor(R.color.white)

        selectedDayNo = careDay!!.day
        onClickEvents()

        getCarePlanTaskDetails(selectedDayNo)
    }

    private fun onClickEvents() {
        binding.carePlanBack.setOnClickListener {
            setBottomNavigation(null)
            setLayoutBottomNavigation(null)
            replaceFragmentNoBackStack(
                BottomNavigationFragment(),
                R.id.layout_home,
                BottomNavigationFragment.TAG
            )
        }
    }

    private fun getCarePlanTaskDetails(dayNumber: Int) {
        getDayWiseCarePlanData(dayNumber) { response ->
            val dayWiseCarePlanType: Type =
                object : TypeToken<DayWiseCarePlan?>() {}.type
            val dayWiseCarePlan: DayWiseCarePlan =
                Gson().fromJson(response, dayWiseCarePlanType)

            binding.recyclerViewCarePlanDayList.apply {
                layoutManager = LinearLayoutManager(
                    requireActivity(), RecyclerView.HORIZONTAL, false
                )
                adapter = CarePlanDayListAdapter(
                    mActivity!!,
                    dayWiseCarePlan.total_days, selectedDayNo, "", this@CarePlanFragment
                )
            }
            try {
                updateTaskProgressData(dayWiseCarePlan)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            try {
                updateTodayTasks(dayWiseCarePlan)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateTaskProgressData(dayWiseCarePlan: DayWiseCarePlan) {
        if (dayWiseCarePlan.plan.task_completed.toString() != "{}") {
            if (dayWiseCarePlan.plan.task_completed.completed != null)
                binding.txtTaskCompleted.text =
                    dayWiseCarePlan.plan.task_completed.completed + " of " +
                            dayWiseCarePlan.plan.task_completed.total
            else
                binding.layoutTaskProgressTaskCompleted.visibility = View.GONE
        } else {
            binding.layoutTaskProgressTaskCompleted.visibility = View.GONE
        }

        if (dayWiseCarePlan.plan.calories != null && dayWiseCarePlan.plan.calories.toString() != "{}") {
            if (dayWiseCarePlan.plan.calories.completed != null)
                binding.txtCaloriesData.text =
                    dayWiseCarePlan.plan.calories.completed.dropLast(4) + " of " +
                            dayWiseCarePlan.plan.calories.total.dropLast(4) + " mins"
            else
                binding.layoutTaskProgressCalories.visibility = View.GONE
        } else {
            binding.layoutTaskProgressCalories.visibility = View.GONE
        }

        if (dayWiseCarePlan.plan.exercise != null && dayWiseCarePlan.plan.exercise.toString() != "{}") {
            if (dayWiseCarePlan.plan.exercise.completed != null)
                binding.txtExerciseData.text =
                    dayWiseCarePlan.plan.exercise.completed.dropLast(4) + " of " +
                            dayWiseCarePlan.plan.exercise.total.dropLast(4) + " mins"
            else
                binding.layoutTaskProgressExercise.visibility = View.GONE
        } else {
            binding.layoutTaskProgressExercise.visibility = View.GONE
        }

        if (dayWiseCarePlan.plan.music != null && dayWiseCarePlan.plan.music.toString() != "{}") {
            if (dayWiseCarePlan.plan.music.completed != null)
                binding.txtMusicData.text =
                    dayWiseCarePlan.plan.music.completed.dropLast(4) + " of " +
                            dayWiseCarePlan.plan.music.total.dropLast(4) + " mins"
            else
                binding.layoutTaskProgressMusic.visibility = View.GONE
        } else {
            binding.layoutTaskProgressMusic.visibility = View.GONE
        }

        if (dayWiseCarePlan.plan.mindfulness != null && dayWiseCarePlan.plan.mindfulness.toString() != "{}") {
            if (dayWiseCarePlan.plan.mindfulness.completed != null)
                binding.txtMindfulnessData.text =
                    dayWiseCarePlan.plan.mindfulness.completed.dropLast(4) + " of " +
                            dayWiseCarePlan.plan.mindfulness.total.dropLast(4) + " mins"
            else
                binding.layoutTaskProgressMindfulness.visibility = View.GONE
        } else {
            binding.layoutTaskProgressMindfulness.visibility = View.GONE
        }

        if (dayWiseCarePlan.plan.yoga != null && dayWiseCarePlan.plan.yoga.toString() != "{}") {
            if (dayWiseCarePlan.plan.yoga.completed != null)
                binding.txtYogaData.text =
                    dayWiseCarePlan.plan.yoga.completed.dropLast(4) + " of " +
                            dayWiseCarePlan.plan.yoga.total.dropLast(4) + " mins"
            else
                binding.layoutTaskProgressYoga.visibility = View.GONE
        } else {
            binding.layoutTaskProgressYoga.visibility = View.GONE
        }
    }

    private fun updateTodayTasks(dayWiseCarePlan: DayWiseCarePlan) {
        val tasks: ArrayList<CareDayIndividualTaskDetail> = arrayListOf()
        if (dayWiseCarePlan.plan.tasks.yoga != null && dayWiseCarePlan.plan.tasks.yoga.isNotEmpty())
            tasks.addAll(dayWiseCarePlan.plan.tasks.yoga)
        if (dayWiseCarePlan.plan.tasks.exercise != null && dayWiseCarePlan.plan.tasks.exercise.isNotEmpty())
            tasks.addAll(dayWiseCarePlan.plan.tasks.exercise)
        if (dayWiseCarePlan.plan.tasks.nutrition != null && dayWiseCarePlan.plan.tasks.nutrition.isNotEmpty())
            tasks.addAll(dayWiseCarePlan.plan.tasks.nutrition)
        if (dayWiseCarePlan.plan.tasks.mindfulness != null && dayWiseCarePlan.plan.tasks.mindfulness.isNotEmpty())
            tasks.addAll(dayWiseCarePlan.plan.tasks.mindfulness)
        if (dayWiseCarePlan.plan.tasks.music != null && dayWiseCarePlan.plan.tasks.music.isNotEmpty())
            tasks.addAll(dayWiseCarePlan.plan.tasks.music)

        tasks.sortBy { it.time }

        binding.recyclerViewTodayTask.apply {
            layoutManager = LinearLayoutManager(
                requireActivity(), RecyclerView.VERTICAL, false
            )
            adapter = CarePlanTodayTaskListAdapter(
                mActivity!!,
                tasks, this@CarePlanFragment
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
         * @return A new instance of fragment CarePlanFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: CarePlans, param2: CareDay) =
            CarePlanFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM1, param1)
                    putParcelable(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_care_plan"
    }

    override fun onCarePlanDayItemClickListener(dayNumber: Int) {
        selectedDayNo = dayNumber
        getCarePlanTaskDetails(dayNumber)
    }

    override fun onCarePlanTaskItemClickListener(type: String) {
        when (type) {
            "Exercise" -> {
                replaceFragment(
                    ExerciseCarePlanFragment.newInstance(selectedDayNo),
                    R.id.layout_home,
                    ExerciseCarePlanFragment.TAG
                )
            }
            "Nutrition" -> {
                replaceFragment(
                    NutritionCarePlanFragment.newInstance(selectedDayNo),
                    R.id.layout_home,
                    NutritionCarePlanFragment.TAG
                )
            }
            "Mindfulness" -> {
                replaceFragment(
                    MindfulnessCarePlanFragment.newInstance(selectedDayNo),
                    R.id.layout_home,
                    MindfulnessCarePlanFragment.TAG
                )
            }
            "Music" -> {
                replaceFragment(
                    MusicCarePlanFragment.newInstance(selectedDayNo),
                    R.id.layout_home,
                    MusicCarePlanFragment.TAG
                )
            }
            "Yoga" -> {
                replaceFragment(
                    YogaCarePlanFragment.newInstance(selectedDayNo),
                    R.id.layout_home,
                    YogaCarePlanFragment.TAG
                )
            }
        }
    }
}