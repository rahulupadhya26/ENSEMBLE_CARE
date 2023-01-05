package com.app.selfcare.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import antonkozyriatskyi.circularprogressindicator.CircularProgressIndicator
import com.app.selfcare.R
import com.app.selfcare.controller.OnCarePlanDayWiseItemClickListener
import com.app.selfcare.data.CarePlans
import kotlinx.android.synthetic.main.layout_item_care_plan_day.view.*

class CarePlanDayAdapter(
    private val context: Context,
    private val list: CarePlans,
    private val adapterItemClickListener: OnCarePlanDayWiseItemClickListener?
) :
    RecyclerView.Adapter<CarePlanDayAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CarePlanDayAdapter.ViewHolder {
        val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_care_plan_day, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return list.days.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: CarePlanDayAdapter.ViewHolder, position: Int) {
        holder.txtCarePlanTitle.text = list.title
        holder.txtCarePlanDayNo.text = "Day " + list.days[position].day
        if (list.days[position].progress == 0.0) {
            holder.txtCarePlanStatus.text = "Not Started"
        } else {
            holder.txtCarePlanStatus.text =
                "Task Completed " + list.days[position].completed + "/" + list.days[position].total_task
        }
        if (list.days[position].progress == 100.0) {
            holder.imgBadge.visibility = View.VISIBLE
            holder.txtDayTaskPercentage.visibility = View.GONE
        } else {
            holder.imgBadge.visibility = View.GONE
            holder.txtDayTaskPercentage.visibility = View.VISIBLE
            holder.txtDayTaskPercentage.text = list.days[position].progress.toInt().toString() + "%"
        }
        holder.dayProgress.setProgress(list.days[position].progress, 100.0)
        holder.layoutDayWise.setOnClickListener {
            adapterItemClickListener!!.onCarePlanDayWiseItemClickListener(list, list.days[position])
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtCarePlanTitle: TextView = itemView.txtCarePlanTitle
        val txtCarePlanDayNo: TextView = itemView.txtCarePlanDayNo
        val txtCarePlanStatus: TextView = itemView.txtCarePlanStatus
        val imgBadge: ImageView = itemView.imgBadge
        val txtDayTaskPercentage: TextView = itemView.txtDayTaskPercentage
        val dayProgress: CircularProgressIndicator = itemView.dayProgress
        val layoutDayWise: LinearLayout = itemView.layoutDayWise
    }
}