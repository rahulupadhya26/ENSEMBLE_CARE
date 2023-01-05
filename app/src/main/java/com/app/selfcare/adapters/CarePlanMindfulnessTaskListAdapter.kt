package com.app.selfcare.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.R
import com.app.selfcare.data.CareDayIndividualTaskDetail
import com.skydoves.progressview.ProgressView
import kotlinx.android.synthetic.main.layout_item_care_plan_mindfulness_task.view.*

class CarePlanMindfulnessTaskListAdapter(
    private val context: Context,
    private val list: ArrayList<CareDayIndividualTaskDetail>
) :
    RecyclerView.Adapter<CarePlanMindfulnessTaskListAdapter.ViewHolder>() {

    var pos = -1

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CarePlanMindfulnessTaskListAdapter.ViewHolder {
        val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_care_plan_mindfulness_task, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: CarePlanMindfulnessTaskListAdapter.ViewHolder, position: Int) {
        val item = list[position]

        holder.txtMindfulnessCompletedTaskTitle.text = item.duration
        holder.txtMindfulnessCompletedTaskSubTitle.text = item.task_detail.details.name

        holder.txtMindfulnessPendingTaskTitle.text = item.duration
        holder.txtMindfulnessPendingTaskSubTitle.text = item.task_detail.details.name

        holder.txtMindfulnessPendingLaterTaskTitle.text = item.duration
        holder.txtMindfulnessPendingLaterTaskSubTitle.text = item.task_detail.details.name

        if (item.is_completed) {
            holder.layoutMindfulnessPendingTask.visibility = View.GONE
            holder.layoutMindfulnessPendingLaterTask.visibility = View.GONE
            holder.layoutMindfulnessCompletedTask.visibility = View.VISIBLE
            if ((position + 1) < list.size) {
                holder.progressMindfulnessCompletedTask.visibility = View.VISIBLE
                if (list[position + 1].is_completed) {
                    holder.progressMindfulnessCompletedTask.progress = 50.0F
                }
            } else {
                holder.progressMindfulnessCompletedTask.visibility = View.GONE
            }
        }

        if (pos == -1) {
            if (!item.is_completed) {
                holder.layoutMindfulnessPendingLaterTask.visibility = View.GONE
                holder.layoutMindfulnessCompletedTask.visibility = View.GONE
                holder.layoutMindfulnessPendingTask.visibility = View.VISIBLE
                if ((position + 1) < list.size) {
                    holder.progressMindfulnessPendingTask.visibility = View.VISIBLE
                    pos = position + 1
                } else {
                    holder.progressMindfulnessPendingTask.visibility = View.GONE
                }
            }
        }

        if (pos == position) {
            pos = position + 1
            holder.layoutMindfulnessCompletedTask.visibility = View.GONE
            holder.layoutMindfulnessPendingTask.visibility = View.GONE
            holder.layoutMindfulnessPendingLaterTask.visibility = View.VISIBLE
            if (pos < list.size) {
                holder.progressMindfulnessPendingLaterTask.visibility = View.VISIBLE
            } else {
                holder.progressMindfulnessPendingLaterTask.visibility = View.GONE
            }
        }

        holder.layoutMindfulnessPendingTask.setOnClickListener {
            //
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val layoutMindfulnessCompletedTask: LinearLayout = itemView.layoutMindfulnessCompletedTask
        val progressMindfulnessCompletedTask: ProgressView = itemView.progressMindfulnessCompletedTask
        val txtMindfulnessCompletedTaskTitle: TextView = itemView.txtMindfulnessCompletedTaskTitle
        val txtMindfulnessCompletedTaskSubTitle: TextView = itemView.txtMindfulnessCompletedTaskSubTitle

        val layoutMindfulnessPendingTask: LinearLayout = itemView.layoutMindfulnessPendingTask
        val progressMindfulnessPendingTask: ProgressView = itemView.progressMindfulnessPendingTask
        val txtMindfulnessPendingTaskTitle: TextView = itemView.txtMindfulnessPendingTaskTitle
        val txtMindfulnessPendingTaskSubTitle: TextView = itemView.txtMindfulnessPendingTaskSubTitle

        val layoutMindfulnessPendingLaterTask: LinearLayout = itemView.layoutMindfulnessPendingLaterTask
        val progressMindfulnessPendingLaterTask: ProgressView = itemView.progressMindfulnessPendingLaterTask
        val txtMindfulnessPendingLaterTaskTitle: TextView = itemView.txtMindfulnessPendingLaterTaskTitle
        val txtMindfulnessPendingLaterTaskSubTitle: TextView = itemView.txtMindfulnessPendingLaterTaskSubTitle
    }
}