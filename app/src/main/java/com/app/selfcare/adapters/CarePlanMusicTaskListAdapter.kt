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
import kotlinx.android.synthetic.main.layout_item_care_plan_music_task.view.*

class CarePlanMusicTaskListAdapter(
    private val context: Context,
    private val list: ArrayList<CareDayIndividualTaskDetail>
) :
    RecyclerView.Adapter<CarePlanMusicTaskListAdapter.ViewHolder>() {

    var pos = -1

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CarePlanMusicTaskListAdapter.ViewHolder {
        val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_care_plan_music_task, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: CarePlanMusicTaskListAdapter.ViewHolder, position: Int) {
        val item = list[position]

        holder.txtMusicCompletedTaskTitle.text = item.duration
        holder.txtMusicCompletedTaskSubTitle.text = item.task_detail.details.name

        holder.txtMusicPendingTaskTitle.text = item.duration
        holder.txtMusicPendingTaskSubTitle.text = item.task_detail.details.name

        holder.txtMusicPendingLaterTaskTitle.text = item.duration
        holder.txtMusicPendingLaterTaskSubTitle.text = item.task_detail.details.name

        if (item.is_completed) {
            holder.layoutMusicPendingTask.visibility = View.GONE
            holder.layoutMusicPendingLaterTask.visibility = View.GONE
            holder.layoutMusicCompletedTask.visibility = View.VISIBLE
            if ((position + 1) < list.size) {
                holder.progressMusicCompletedTask.visibility = View.VISIBLE
                if (list[position + 1].is_completed) {
                    holder.progressMusicCompletedTask.progress = 50.0F
                }
            } else {
                holder.progressMusicCompletedTask.visibility = View.GONE
            }
        }

        if (pos == -1) {
            if (!item.is_completed) {
                holder.layoutMusicPendingLaterTask.visibility = View.GONE
                holder.layoutMusicCompletedTask.visibility = View.GONE
                holder.layoutMusicPendingTask.visibility = View.VISIBLE
                if ((position + 1) < list.size) {
                    holder.progressMusicPendingTask.visibility = View.VISIBLE
                    pos = position + 1
                } else {
                    holder.progressMusicPendingTask.visibility = View.GONE
                }
            }
        }

        if (pos == position) {
            pos = position + 1
            holder.layoutMusicCompletedTask.visibility = View.GONE
            holder.layoutMusicPendingTask.visibility = View.GONE
            holder.layoutMusicPendingLaterTask.visibility = View.VISIBLE
            if (pos < list.size) {
                holder.progressMusicPendingLaterTask.visibility = View.VISIBLE
            } else {
                holder.progressMusicPendingLaterTask.visibility = View.GONE
            }
        }

        holder.layoutMusicPendingTask.setOnClickListener {
            //
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val layoutMusicCompletedTask: LinearLayout = itemView.layoutMusicCompletedTask
        val progressMusicCompletedTask: ProgressView = itemView.progressMusicCompletedTask
        val txtMusicCompletedTaskTitle: TextView = itemView.txtMusicCompletedTaskTitle
        val txtMusicCompletedTaskSubTitle: TextView = itemView.txtMusicCompletedTaskSubTitle

        val layoutMusicPendingTask: LinearLayout = itemView.layoutMusicPendingTask
        val progressMusicPendingTask: ProgressView = itemView.progressMusicPendingTask
        val txtMusicPendingTaskTitle: TextView = itemView.txtMusicPendingTaskTitle
        val txtMusicPendingTaskSubTitle: TextView = itemView.txtMusicPendingTaskSubTitle

        val layoutMusicPendingLaterTask: LinearLayout = itemView.layoutMusicPendingLaterTask
        val progressMusicPendingLaterTask: ProgressView = itemView.progressMusicPendingLaterTask
        val txtMusicPendingLaterTaskTitle: TextView = itemView.txtMusicPendingLaterTaskTitle
        val txtMusicPendingLaterTaskSubTitle: TextView = itemView.txtMusicPendingLaterTaskSubTitle
    }
}