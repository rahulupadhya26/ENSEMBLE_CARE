package com.app.selfcare.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.R
import com.app.selfcare.controller.OnCarePlanDayItemClickListener
import com.app.selfcare.controller.OnCarePlanTaskItemClickListener
import com.app.selfcare.data.CareDayIndividualTaskDetail
import kotlinx.android.synthetic.main.layout_item_care_plan_today_task.view.*

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
        val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_care_plan_today_task, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: CarePlanTodayTaskListAdapter.ViewHolder, position: Int) {
        val item = list[position]
        if (item.task_detail.yoga != 0) {
            holder.txtCompletedTaskTitle.text = "Yoga"
            holder.txtPendingTaskTitle.text = "Yoga"
            holder.txtPendingLaterTaskTitle.text = "Yoga"
        } else if (item.task_detail.exercise != 0) {
            holder.txtCompletedTaskTitle.text = "Exercise"
            holder.txtPendingTaskTitle.text = "Exercise"
            holder.txtPendingLaterTaskTitle.text = "Exercise"
        } else if (item.task_detail.mindfulness != 0) {
            holder.txtCompletedTaskTitle.text = "Mindfulness"
            holder.txtPendingTaskTitle.text = "Mindfulness"
            holder.txtPendingLaterTaskTitle.text = "Mindfulness"
        } else if (item.task_detail.nutrition != 0) {
            holder.txtCompletedTaskTitle.text = "Nutrition"
            holder.txtPendingTaskTitle.text = "Nutrition"
            holder.txtPendingLaterTaskTitle.text = "Nutrition"
        } else if (item.task_detail.music != 0) {
            holder.txtCompletedTaskTitle.text = "Music"
            holder.txtPendingTaskTitle.text = "Music"
            holder.txtPendingLaterTaskTitle.text = "Music"
        }
        holder.txtCompletedTaskSubTitle.text =
            item.task_detail.title + " " + item.task_detail.time_taken

        holder.txtPendingTaskSubTitle.text =
            item.task_detail.title + " " + item.task_detail.time_taken

        holder.txtPendingLaterTaskSubTitle.text =
            item.task_detail.title + " " + item.task_detail.time_taken

        if (item.is_completed) {
            holder.layoutPendingTask.visibility = View.GONE
            holder.layoutPendingLaterTask.visibility = View.GONE
            holder.layoutCompletedTask.visibility = View.VISIBLE
            holder.txtTaskStatus.text = "Completed"
            if ((position + 1) < list.size) {
                holder.layoutTaskBar1.visibility = View.VISIBLE
                if (list[position + 1].is_completed) {
                    holder.view1.setBackgroundColor(context.resources.getColor(R.color.primaryGreen))
                } else {
                    holder.view1.setBackgroundColor(context.resources.getColor(R.color.lightestGreyColor))
                }
            } else {
                holder.layoutTaskBar1.visibility = View.GONE
            }
        }

        if (pos == -1) {
            if (!item.is_completed) {
                holder.layoutPendingLaterTask.visibility = View.GONE
                holder.layoutCompletedTask.visibility = View.GONE
                holder.layoutPendingTask.visibility = View.VISIBLE
                holder.txtPendingStatus.text = item.time.dropLast(3) + " hrs"
                if ((position + 1) < list.size) {
                    holder.layoutTaskBar2.visibility = View.VISIBLE
                    pos = position + 1
                } else {
                    holder.layoutTaskBar2.visibility = View.GONE
                }
            }
        }

        if (pos == position) {
            pos = position + 1
            holder.layoutCompletedTask.visibility = View.GONE
            holder.layoutPendingTask.visibility = View.GONE
            holder.layoutPendingLaterTask.visibility = View.VISIBLE
            holder.txtPendingLaterStatus.text = item.time.dropLast(3) + " hrs"
            if (pos < list.size) {
                holder.layoutTaskBar3.visibility = View.VISIBLE
            } else {
                holder.layoutTaskBar3.visibility = View.GONE
            }
        }

        holder.layoutCompleteTodayTask.setOnClickListener {
            adapterList.onCarePlanTaskItemClickListener(holder.txtCompletedTaskTitle.text.toString())
        }

        holder.layoutTodayPendingTask.setOnClickListener {
            adapterList.onCarePlanTaskItemClickListener(holder.txtPendingTaskTitle.text.toString())
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val layoutCompletedTask: LinearLayout = itemView.layoutCompletedTask
        val layoutCompleteTodayTask: LinearLayout = itemView.layoutCompleteTodayTask
        val txtCompletedTaskTitle: TextView = itemView.txtCompletedTaskTitle
        val txtCompletedTaskSubTitle: TextView = itemView.txtCompletedTaskSubTitle
        val txtTaskStatus: TextView = itemView.txtTaskStatus

        val layoutPendingTask: LinearLayout = itemView.layoutPendingTask
        val layoutTodayPendingTask: RelativeLayout = itemView.layoutTodayPendingTask
        val txtPendingTaskTitle: TextView = itemView.txtPendingTaskTitle
        val txtPendingTaskSubTitle: TextView = itemView.txtPendingTaskSubTitle
        val txtPendingStatus: TextView = itemView.txtPendingStatus

        val layoutPendingLaterTask: LinearLayout = itemView.layoutPendingLaterTask
        val txtPendingLaterTaskTitle: TextView = itemView.txtPendingLaterTaskTitle
        val txtPendingLaterTaskSubTitle: TextView = itemView.txtPendingLaterTaskSubTitle
        val txtPendingLaterStatus: TextView = itemView.txtPendingLaterStatus

        val layoutTaskBar1: LinearLayout = itemView.layoutTaskBar1
        val layoutTaskBar2: LinearLayout = itemView.layoutTaskBar2
        val layoutTaskBar3: LinearLayout = itemView.layoutTaskBar3

        val view1: View = itemView.view1
    }
}