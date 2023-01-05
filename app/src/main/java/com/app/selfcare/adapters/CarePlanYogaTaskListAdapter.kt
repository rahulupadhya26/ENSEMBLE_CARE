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
import kotlinx.android.synthetic.main.layout_item_care_plan_yoga_task.view.*

class CarePlanYogaTaskListAdapter(
    private val context: Context,
    private val list: ArrayList<CareDayIndividualTaskDetail>,
    private val adapterClick: OnCarePlanPendingTaskItemClickListener
) :
    RecyclerView.Adapter<CarePlanYogaTaskListAdapter.ViewHolder>() {

    var pos = -1

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CarePlanYogaTaskListAdapter.ViewHolder {
        val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_care_plan_yoga_task, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: CarePlanYogaTaskListAdapter.ViewHolder, position: Int) {
        val item = list[position]

        holder.txtYogaCompletedTaskTitle.text = item.duration
        holder.txtYogaCompletedTaskSubTitle.text = item.task_detail.details.name

        holder.txtYogaPendingTaskTitle.text = item.duration
        holder.txtYogaPendingTaskSubTitle.text = item.task_detail.details.name

        holder.txtYogaPendingLaterTaskTitle.text = item.duration
        holder.txtYogaPendingLaterTaskSubTitle.text = item.task_detail.details.name

        if (item.is_completed) {
            holder.layoutYogaPendingTask.visibility = View.GONE
            holder.layoutYogaPendingLaterTask.visibility = View.GONE
            holder.layoutYogaCompletedTask.visibility = View.VISIBLE
            if ((position + 1) < list.size) {
                holder.progressYogaCompletedTask.visibility = View.VISIBLE
                if (list[position + 1].is_completed) {
                    holder.progressYogaCompletedTask.progress = 50.0F
                }
            } else {
                holder.progressYogaCompletedTask.visibility = View.GONE
            }
        }

        if (pos == -1) {
            if (!item.is_completed) {
                holder.layoutYogaPendingLaterTask.visibility = View.GONE
                holder.layoutYogaCompletedTask.visibility = View.GONE
                holder.layoutYogaPendingTask.visibility = View.VISIBLE
                if ((position + 1) < list.size) {
                    holder.progressYogaPendingTask.visibility = View.VISIBLE
                    pos = position + 1
                } else {
                    holder.progressYogaPendingTask.visibility = View.GONE
                }
            }
        }

        if (pos == position) {
            pos = position + 1
            holder.layoutYogaCompletedTask.visibility = View.GONE
            holder.layoutYogaPendingTask.visibility = View.GONE
            holder.layoutYogaPendingLaterTask.visibility = View.VISIBLE
            if (pos < list.size) {
                holder.progressYogaPendingLaterTask.visibility = View.VISIBLE
            } else {
                holder.progressYogaPendingLaterTask.visibility = View.GONE
            }
        }

        holder.cardViewYogaPendingTask.setOnClickListener {
            adapterClick.onCarePlanPendingTaskItemClickListener(item)
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val layoutYogaCompletedTask: LinearLayout = itemView.layoutYogaCompletedTask
        val progressYogaCompletedTask: ProgressView = itemView.progressYogaCompletedTask
        val txtYogaCompletedTaskTitle: TextView = itemView.txtYogaCompletedTaskTitle
        val txtYogaCompletedTaskSubTitle: TextView = itemView.txtYogaCompletedTaskSubTitle

        val layoutYogaPendingTask: LinearLayout = itemView.layoutYogaPendingTask
        val cardViewYogaPendingTask: CardView = itemView.cardViewYogaPendingTask
        val progressYogaPendingTask: ProgressView = itemView.progressYogaPendingTask
        val txtYogaPendingTaskTitle: TextView = itemView.txtYogaPendingTaskTitle
        val txtYogaPendingTaskSubTitle: TextView = itemView.txtYogaPendingTaskSubTitle

        val layoutYogaPendingLaterTask: LinearLayout = itemView.layoutYogaPendingLaterTask
        val progressYogaPendingLaterTask: ProgressView = itemView.progressYogaPendingLaterTask
        val txtYogaPendingLaterTaskTitle: TextView = itemView.txtYogaPendingLaterTaskTitle
        val txtYogaPendingLaterTaskSubTitle: TextView = itemView.txtYogaPendingLaterTaskSubTitle
    }
}