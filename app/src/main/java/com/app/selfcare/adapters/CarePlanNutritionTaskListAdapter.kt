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
import com.app.selfcare.databinding.LayoutItemCarePlanNutritionTaskBinding
import com.app.selfcare.databinding.LayoutItemGoalBinding
import com.skydoves.progressview.ProgressView

class CarePlanNutritionTaskListAdapter(
    private val context: Context,
    private val list: ArrayList<CareDayIndividualTaskDetail>,
    private val adapterClick: OnCarePlanPendingTaskItemClickListener
) :
    RecyclerView.Adapter<CarePlanNutritionTaskListAdapter.ViewHolder>() {

    var pos = -1

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CarePlanNutritionTaskListAdapter.ViewHolder {
        val binding = LayoutItemCarePlanNutritionTaskBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        /*val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_care_plan_nutrition_task, parent, false)*/
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

            txtNutritionCompletedTaskTitle.text = item.duration
            txtNutritionCompletedTaskSubTitle.text = item.task_detail.details.name

            txtNutritionPendingTaskTitle.text = item.duration
            txtNutritionPendingTaskSubTitle.text = item.task_detail.details.name

            txtNutritionPendingLaterTaskTitle.text = item.duration
            txtNutritionPendingLaterTaskSubTitle.text = item.task_detail.details.name

            if (item.is_completed) {
                layoutNutritionPendingTask.visibility = View.GONE
                layoutNutritionPendingLaterTask.visibility = View.GONE
                layoutNutritionCompletedTask.visibility = View.VISIBLE
                if ((position + 1) < list.size) {
                    progressNutritionCompletedTask.visibility = View.VISIBLE
                    if (list[position + 1].is_completed) {
                        progressNutritionCompletedTask.progress = 50.0F
                    }
                } else {
                    progressNutritionCompletedTask.visibility = View.GONE
                }
            }

            if (pos == -1) {
                if (!item.is_completed) {
                    layoutNutritionPendingLaterTask.visibility = View.GONE
                    layoutNutritionCompletedTask.visibility = View.GONE
                    layoutNutritionPendingTask.visibility = View.VISIBLE
                    if ((position + 1) < list.size) {
                        progressNutritionPendingTask.visibility = View.VISIBLE
                        pos = position + 1
                    } else {
                        progressNutritionPendingTask.visibility = View.GONE
                    }
                }
            }

            if (pos == position) {
                pos = position + 1
                layoutNutritionCompletedTask.visibility = View.GONE
                layoutNutritionPendingTask.visibility = View.GONE
                layoutNutritionPendingLaterTask.visibility = View.VISIBLE
                if (pos < list.size) {
                    progressNutritionPendingLaterTask.visibility = View.VISIBLE
                } else {
                    progressNutritionPendingLaterTask.visibility = View.GONE
                }
            }

            cardViewNutritionPendingTask.setOnClickListener {
                adapterClick.onCarePlanPendingTaskItemClickListener(item, true)
            }

            cardViewNutritionCompletedTask.setOnClickListener {
                adapterClick.onCarePlanPendingTaskItemClickListener(item, false)
            }
        }
    }

    inner class ViewHolder(val binding: LayoutItemCarePlanNutritionTaskBinding) :
        RecyclerView.ViewHolder(binding.root)
}