package com.app.selfcare.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.R
import com.app.selfcare.controller.OnCarePlanTaskItemClickListener
import com.app.selfcare.data.CareDayIndividualTaskDetail
import com.app.selfcare.databinding.LayoutItemCarePlanTodayTaskBinding
import com.app.selfcare.databinding.LayoutItemGoalBinding
import com.app.selfcare.utils.DateMethods
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CarePlanTodayTaskListAdapter(
    private val context: Context,
    private val list: ArrayList<CareDayIndividualTaskDetail>,
    private val adapterList: OnCarePlanTaskItemClickListener
) :
    RecyclerView.Adapter<CarePlanTodayTaskListAdapter.ViewHolder>() {

    var pos = -1

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CarePlanTodayTaskListAdapter.ViewHolder {
        val binding = LayoutItemCarePlanTodayTaskBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        /*val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_care_plan_today_task, parent, false)*/
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            val item = list[position]
            if (item.task_detail.yoga != 0) {
                txtCompletedTaskTitle.text = "Yoga"
                txtPendingTaskTitle.text = "Yoga"
                txtPendingLaterTaskTitle.text = "Yoga"
            } else if (item.task_detail.exercise != 0) {
                txtCompletedTaskTitle.text = "Exercise"
                txtPendingTaskTitle.text = "Exercise"
                txtPendingLaterTaskTitle.text = "Exercise"
            } else if (item.task_detail.mindfulness != 0) {
                txtCompletedTaskTitle.text = "Mindfulness"
                txtPendingTaskTitle.text = "Mindfulness"
                txtPendingLaterTaskTitle.text = "Mindfulness"
            } else if (item.task_detail.nutrition != 0) {
                txtCompletedTaskTitle.text = "Nutrition"
                txtPendingTaskTitle.text = "Nutrition"
                txtPendingLaterTaskTitle.text = "Nutrition"
            } else if (item.task_detail.music != 0) {
                txtCompletedTaskTitle.text = "Music"
                txtPendingTaskTitle.text = "Music"
                txtPendingLaterTaskTitle.text = "Music"
            }
            txtCompletedTaskSubTitle.text =
                item.task_detail.title + " - " + item.task_detail.time_taken + " mins"

            txtPendingTaskSubTitle.text =
                item.task_detail.title + " - " + item.task_detail.time_taken + " mins"

            txtPendingLaterTaskSubTitle.text =
                item.task_detail.title + " - " + item.task_detail.time_taken + " mins"

            val sdf = SimpleDateFormat("HH:mm")
            val currentDate = sdf.format(Date())

            if (pos == -1) {
                if (!item.is_completed) {
                    layoutPendingLaterTask.visibility = View.GONE
                    layoutCompletedTask.visibility = View.GONE
                    layoutPendingTask.visibility = View.VISIBLE
                    txtPendingStatus.text = item.time.dropLast(3) + " hrs"
                    if ((position + 1) < list.size) {
                        layoutTaskBar2.visibility = View.VISIBLE
                        pos = position + 1
                    } else {
                        layoutTaskBar2.visibility = View.GONE
                    }
                } else if (DateMethods().checkTimings(currentDate, item.time.dropLast(3))) {
                    layoutPendingLaterTask.visibility = View.GONE
                    layoutCompletedTask.visibility = View.GONE
                    layoutPendingTask.visibility = View.VISIBLE
                    txtPendingStatus.text = item.time.dropLast(3) + " hrs"
                    if ((position + 1) < list.size) {
                        layoutTaskBar2.visibility = View.VISIBLE
                        pos = position + 1
                    } else {
                        layoutTaskBar2.visibility = View.GONE
                    }
                }
            }

            if (pos == position) {
                pos = position + 1
                if (DateMethods().checkTimings(currentDate, item.time.dropLast(3))) {
                    layoutPendingLaterTask.visibility = View.GONE
                    layoutCompletedTask.visibility = View.GONE
                    layoutPendingTask.visibility = View.VISIBLE
                    txtPendingStatus.text = item.time.dropLast(3) + " hrs"
                    if ((position + 1) < list.size) {
                        layoutTaskBar2.visibility = View.VISIBLE
                        pos = position + 1
                    } else {
                        layoutTaskBar2.visibility = View.GONE
                    }
                } else {
                    layoutCompletedTask.visibility = View.GONE
                    layoutPendingTask.visibility = View.GONE
                    layoutPendingLaterTask.visibility = View.VISIBLE
                    txtPendingLaterStatus.text = item.time.dropLast(3) + " hrs"
                    if (pos < list.size) {
                        layoutTaskBar3.visibility = View.VISIBLE
                    } else {
                        layoutTaskBar3.visibility = View.GONE
                    }
                }
            }

            if (item.is_completed) {
                layoutPendingTask.visibility = View.GONE
                layoutPendingLaterTask.visibility = View.GONE
                layoutCompletedTask.visibility = View.VISIBLE
                txtTaskStatus.text = "Completed"
                if ((position + 1) < list.size) {
                    layoutTaskBar1.visibility = View.VISIBLE
                    if (list[position + 1].is_completed) {
                        view1.setBackgroundColor(context.resources.getColor(R.color.primaryGreen))
                    } else {
                        view1.setBackgroundColor(context.resources.getColor(R.color.lightestGreyColor))
                    }
                } else {
                    layoutTaskBar1.visibility = View.GONE
                }
            }

            layoutCompleteTodayTask.setOnClickListener {
                adapterList.onCarePlanTaskItemClickListener(txtCompletedTaskTitle.text.toString())
            }

            layoutTodayPendingTask.setOnClickListener {
                adapterList.onCarePlanTaskItemClickListener(txtPendingTaskTitle.text.toString())
            }
        }
    }

    inner class ViewHolder(val binding: LayoutItemCarePlanTodayTaskBinding) :
        RecyclerView.ViewHolder(binding.root)
}