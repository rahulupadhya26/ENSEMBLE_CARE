package com.app.selfcare.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.R
import com.app.selfcare.controller.OnCarePlanPendingTaskItemClickListener
import com.app.selfcare.data.CareDayIndividualTaskDetail
import com.skydoves.progressview.ProgressView
import kotlinx.android.synthetic.main.layout_item_care_plan_exercise_task.view.*

class CarePlanExerciseTaskListAdapter(
    private val context: Context,
    private val list: ArrayList<CareDayIndividualTaskDetail>,
    private val adapterClick: OnCarePlanPendingTaskItemClickListener
) :
    RecyclerView.Adapter<CarePlanExerciseTaskListAdapter.ViewHolder>() {

    var pos = -1

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CarePlanExerciseTaskListAdapter.ViewHolder {
        val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_care_plan_exercise_task, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onBindViewHolder(
        holder: CarePlanExerciseTaskListAdapter.ViewHolder,
        position: Int
    ) {
        val item = list[position]

        holder.txtExerciseCompletedTaskTitle.text = item.duration
        holder.txtExerciseCompletedTaskSubTitle.text = item.task_detail.details.name

        holder.txtExercisePendingTaskTitle.text = item.duration
        holder.txtExercisePendingTaskSubTitle.text = item.task_detail.details.name

        holder.txtExercisePendingLaterTaskTitle.text = item.duration
        holder.txtExercisePendingLaterTaskSubTitle.text = item.task_detail.details.name

        if (item.is_completed) {
            holder.layoutExercisePendingTask.visibility = View.GONE
            holder.layoutExercisePendingLaterTask.visibility = View.GONE
            holder.layoutExerciseCompletedTask.visibility = View.VISIBLE
            if ((position + 1) < list.size) {
                holder.progressExerciseCompletedTask.visibility = View.VISIBLE
                if (list[position + 1].is_completed) {
                    holder.progressExerciseCompletedTask.progress = 50.0F
                }
            } else {
                holder.progressExerciseCompletedTask.visibility = View.GONE
            }
        }

        if (pos == -1) {
            if (!item.is_completed) {
                holder.layoutExercisePendingLaterTask.visibility = View.GONE
                holder.layoutExerciseCompletedTask.visibility = View.GONE
                holder.layoutExercisePendingTask.visibility = View.VISIBLE
                if ((position + 1) < list.size) {
                    holder.progressExercisePendingTask.visibility = View.VISIBLE
                    pos = position + 1
                } else {
                    holder.progressExercisePendingTask.visibility = View.GONE
                }
            }
        }

        if (pos == position) {
            pos = position + 1
            holder.layoutExerciseCompletedTask.visibility = View.GONE
            holder.layoutExercisePendingTask.visibility = View.GONE
            holder.layoutExercisePendingLaterTask.visibility = View.VISIBLE
            if (pos < list.size) {
                holder.progressExercisePendingLaterTask.visibility = View.VISIBLE
            } else {
                holder.progressExercisePendingLaterTask.visibility = View.GONE
            }
        }

        holder.cardViewExercisePendingTask.setOnClickListener {
            adapterClick.onCarePlanPendingTaskItemClickListener(item)
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val layoutExerciseCompletedTask: LinearLayout = itemView.layoutExerciseCompletedTask
        val progressExerciseCompletedTask: ProgressView = itemView.progressExerciseCompletedTask
        val txtExerciseCompletedTaskTitle: TextView = itemView.txtExerciseCompletedTaskTitle
        val txtExerciseCompletedTaskSubTitle: TextView = itemView.txtExerciseCompletedTaskSubTitle

        val layoutExercisePendingTask: LinearLayout = itemView.layoutExercisePendingTask
        val cardViewExercisePendingTask: CardView = itemView.cardViewExercisePendingTask
        val progressExercisePendingTask: ProgressView = itemView.progressExercisePendingTask
        val txtExercisePendingTaskTitle: TextView = itemView.txtExercisePendingTaskTitle
        val txtExercisePendingTaskSubTitle: TextView = itemView.txtExercisePendingTaskSubTitle

        val layoutExercisePendingLaterTask: LinearLayout = itemView.layoutExercisePendingLaterTask
        val progressExercisePendingLaterTask: ProgressView =
            itemView.progressExercisePendingLaterTask
        val txtExercisePendingLaterTaskTitle: TextView = itemView.txtExercisePendingLaterTaskTitle
        val txtExercisePendingLaterTaskSubTitle: TextView =
            itemView.txtExercisePendingLaterTaskSubTitle
    }
}