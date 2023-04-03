package com.app.selfcare.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.R
import com.app.selfcare.controller.OnCarePlanPendingTaskItemClickListener
import com.app.selfcare.data.CareDayIndividualTaskDetail
import com.app.selfcare.databinding.LayoutItemCarePlanMindfulnessTaskBinding
import com.app.selfcare.databinding.LayoutItemGoalBinding
import com.skydoves.progressview.ProgressView

class CarePlanMindfulnessTaskListAdapter(
    private val context: Context,
    private val list: ArrayList<CareDayIndividualTaskDetail>,
    private val adapterClick: OnCarePlanPendingTaskItemClickListener
) :
    RecyclerView.Adapter<CarePlanMindfulnessTaskListAdapter.ViewHolder>() {

    var pos = -1

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CarePlanMindfulnessTaskListAdapter.ViewHolder {
        val binding = LayoutItemCarePlanMindfulnessTaskBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        /*val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_care_plan_mindfulness_task, parent, false)*/
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.binding.apply {
            val item = list[position]

            txtMindfulnessCompletedTaskTitle.text = item.duration
            txtMindfulnessCompletedTaskSubTitle.text = item.task_detail.details.name

            txtMindfulnessPendingTaskTitle.text = item.duration
            txtMindfulnessPendingTaskSubTitle.text = item.task_detail.details.name

            txtMindfulnessPendingLaterTaskTitle.text = item.duration
            txtMindfulnessPendingLaterTaskSubTitle.text = item.task_detail.details.name

            if (item.is_completed) {
                layoutMindfulnessPendingTask.visibility = View.GONE
                layoutMindfulnessPendingLaterTask.visibility = View.GONE
                layoutMindfulnessCompletedTask.visibility = View.VISIBLE
                if ((position + 1) < list.size) {
                    progressMindfulnessCompletedTask.visibility = View.VISIBLE
                    if (list[position + 1].is_completed) {
                        progressMindfulnessCompletedTask.progress = 50.0F
                    }
                } else {
                    progressMindfulnessCompletedTask.visibility = View.GONE
                }
            }

            if (pos == -1) {
                if (!item.is_completed) {
                    layoutMindfulnessPendingLaterTask.visibility = View.GONE
                    layoutMindfulnessCompletedTask.visibility = View.GONE
                    layoutMindfulnessPendingTask.visibility = View.VISIBLE
                    if ((position + 1) < list.size) {
                        progressMindfulnessPendingTask.visibility = View.VISIBLE
                        pos = position + 1
                    } else {
                        progressMindfulnessPendingTask.visibility = View.GONE
                    }
                }
            }

            if (pos == position) {
                pos = position + 1
                layoutMindfulnessCompletedTask.visibility = View.GONE
                layoutMindfulnessPendingTask.visibility = View.GONE
                layoutMindfulnessPendingLaterTask.visibility = View.VISIBLE
                if (pos < list.size) {
                    progressMindfulnessPendingLaterTask.visibility = View.VISIBLE
                } else {
                    progressMindfulnessPendingLaterTask.visibility = View.GONE
                }
            }

            cardViewMindfulnessPendingTask.setOnClickListener {
                adapterClick.onCarePlanPendingTaskItemClickListener(item, true)
            }

            cardViewMindfulnessCompletedTask.setOnClickListener {
                adapterClick.onCarePlanPendingTaskItemClickListener(item, false)
            }
        }
    }

    inner class ViewHolder(val binding: LayoutItemCarePlanMindfulnessTaskBinding) :
        RecyclerView.ViewHolder(binding.root)
}