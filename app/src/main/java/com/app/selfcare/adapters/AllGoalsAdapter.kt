package com.app.selfcare.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.R
import com.app.selfcare.controller.OnGoalItemClickListener
import com.app.selfcare.data.Goal
import com.app.selfcare.utils.DateUtils
import kotlinx.android.synthetic.main.layout_item_goal.view.*
import kotlinx.android.synthetic.main.layout_item_personal_goal.view.*
import kotlin.math.min

class AllGoalsAdapter(
    val context: Context,
    val list: List<Goal>, private val adapterItemClickListener: OnGoalItemClickListener?,
    val isProviderGoal: Boolean
) :
    RecyclerView.Adapter<AllGoalsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AllGoalsAdapter.ViewHolder {
        val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_goal, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        val limit = 4
        return min(list.size, limit)
    }

    override fun onBindViewHolder(holder: AllGoalsAdapter.ViewHolder, position: Int) {
        val item = list[position]
        if (isProviderGoal) {
            holder.deleteGoal.visibility = View.GONE
        } else {
            holder.deleteGoal.visibility = View.VISIBLE
        }
        val goalDate = DateUtils(item.start_date + " 01:00:00")
        holder.goalDate.text = goalDate.getFormattedDate()
        holder.goalTitle.text = item.title
        holder.goalDesc.text = item.description
        var durationTxt = ""
        when (item.duration) {
            0 -> durationTxt = "Does not repeat"
            1 -> durationTxt = "Everyday"
            2 -> durationTxt = "Every week"
            3 -> durationTxt = "Every month"
            4 -> durationTxt = "Every year"
        }
        holder.goalDuration.text = durationTxt
        holder.goalLayout.setOnClickListener {
            adapterItemClickListener!!.onGoalItemClickListener(item, false)
        }
        holder.deleteGoal.setOnClickListener {
            adapterItemClickListener!!.onGoalItemClickListener(item, true)
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val deleteGoal: ImageView = itemView.txtDeleteGoal
        val goalDesc: TextView = itemView.txtGoalDesc
        val goalDate: TextView = itemView.txtGoalDate
        val goalTitle: TextView = itemView.goalTitle
        val goalDuration: TextView = itemView.goalDuration
        val goalLayout: CardView = itemView.cardview_goal
    }
}