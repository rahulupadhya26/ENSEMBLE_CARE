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
import com.skydoves.progressview.ProgressView
import kotlinx.android.synthetic.main.layout_item_care_plan_nutrition_task.view.*

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
        val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_care_plan_nutrition_task, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onBindViewHolder(
        holder: CarePlanNutritionTaskListAdapter.ViewHolder,
        position: Int
    ) {
        val item = list[position]

        holder.txtNutritionCompletedTaskTitle.text = item.duration
        holder.txtNutritionCompletedTaskSubTitle.text = item.task_detail.details.name

        holder.txtNutritionPendingTaskTitle.text = item.duration
        holder.txtNutritionPendingTaskSubTitle.text = item.task_detail.details.name

        holder.txtNutritionPendingLaterTaskTitle.text = item.duration
        holder.txtNutritionPendingLaterTaskSubTitle.text = item.task_detail.details.name

        if (item.is_completed) {
            holder.layoutNutritionPendingTask.visibility = View.GONE
            holder.layoutNutritionPendingLaterTask.visibility = View.GONE
            holder.layoutNutritionCompletedTask.visibility = View.VISIBLE
            if ((position + 1) < list.size) {
                holder.progressNutritionCompletedTask.visibility = View.VISIBLE
                if (list[position + 1].is_completed) {
                    holder.progressNutritionCompletedTask.progress = 50.0F
                }
            } else {
                holder.progressNutritionCompletedTask.visibility = View.GONE
            }
        }

        if (pos == -1) {
            if (!item.is_completed) {
                holder.layoutNutritionPendingLaterTask.visibility = View.GONE
                holder.layoutNutritionCompletedTask.visibility = View.GONE
                holder.layoutNutritionPendingTask.visibility = View.VISIBLE
                if ((position + 1) < list.size) {
                    holder.progressNutritionPendingTask.visibility = View.VISIBLE
                    pos = position + 1
                } else {
                    holder.progressNutritionPendingTask.visibility = View.GONE
                }
            }
        }

        if (pos == position) {
            pos = position + 1
            holder.layoutNutritionCompletedTask.visibility = View.GONE
            holder.layoutNutritionPendingTask.visibility = View.GONE
            holder.layoutNutritionPendingLaterTask.visibility = View.VISIBLE
            if (pos < list.size) {
                holder.progressNutritionPendingLaterTask.visibility = View.VISIBLE
            } else {
                holder.progressNutritionPendingLaterTask.visibility = View.GONE
            }
        }

        holder.cardViewNutritionPendingTask.setOnClickListener {
            adapterClick.onCarePlanPendingTaskItemClickListener(item)
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val layoutNutritionCompletedTask: LinearLayout = itemView.layoutNutritionCompletedTask
        val progressNutritionCompletedTask: ProgressView = itemView.progressNutritionCompletedTask
        val txtNutritionCompletedTaskTitle: TextView = itemView.txtNutritionCompletedTaskTitle
        val txtNutritionCompletedTaskSubTitle: TextView = itemView.txtNutritionCompletedTaskSubTitle

        val layoutNutritionPendingTask: LinearLayout = itemView.layoutNutritionPendingTask
        val cardViewNutritionPendingTask: CardView = itemView.cardViewNutritionPendingTask
        val progressNutritionPendingTask: ProgressView = itemView.progressNutritionPendingTask
        val txtNutritionPendingTaskTitle: TextView = itemView.txtNutritionPendingTaskTitle
        val txtNutritionPendingTaskSubTitle: TextView = itemView.txtNutritionPendingTaskSubTitle

        val layoutNutritionPendingLaterTask: LinearLayout = itemView.layoutNutritionPendingLaterTask
        val progressNutritionPendingLaterTask: ProgressView =
            itemView.progressNutritionPendingLaterTask
        val txtNutritionPendingLaterTaskTitle: TextView = itemView.txtNutritionPendingLaterTaskTitle
        val txtNutritionPendingLaterTaskSubTitle: TextView =
            itemView.txtNutritionPendingLaterTaskSubTitle
    }
}