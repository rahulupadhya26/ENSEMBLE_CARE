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
import kotlinx.android.synthetic.main.layout_item_personal_goal.view.*
import kotlin.math.min

class PersonalGoalAdapter(
    val context: Context,
    val list: List<Goal>, private val adapterItemClickListener: OnGoalItemClickListener?
) :
    RecyclerView.Adapter<PersonalGoalAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PersonalGoalAdapter.ViewHolder {
        val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_personal_goal, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: PersonalGoalAdapter.ViewHolder, position: Int) {
        val item = list[position]
        val goalDate = DateUtils(item.start_date + " 01:00:00")
        holder.goalMonth.text = goalDate.getFullMonthName()
        holder.goalDate.text = goalDate.getDay()
        holder.goalTitle.text = item.title
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
        val deleteGoal: ImageView = itemView.deleteGoal
        val goalMonth: TextView = itemView.tvGoalMonth
        val goalDate: TextView = itemView.tvGoalDate
        val goalTitle: TextView = itemView.tvGoalTitle
        val goalDuration: TextView = itemView.tvGoalDuration
        val goalLayout: CardView = itemView.cardviewGoal
    }
}