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
import com.app.selfcare.databinding.LayoutItemCarePlanDayBinding
import com.app.selfcare.databinding.LayoutItemGoalBinding

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
        val binding =
            LayoutItemCarePlanDayBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        /*val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_care_plan_day, parent, false)*/
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.days.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            txtCarePlanTitle.text = list.title
            txtCarePlanDayNo.text = "Day " + list.days[position].day
            if (list.days[position].progress == 0.0) {
                txtCarePlanStatus.text = "Not Started"
            } else {
                txtCarePlanStatus.text =
                    "Task Completed " + list.days[position].completed + "/" + list.days[position].total_task
            }
            if (list.days[position].progress == 100.0) {
                imgBadge.visibility = View.VISIBLE
                txtDayTaskPercentage.visibility = View.GONE
            } else {
                imgBadge.visibility = View.GONE
                txtDayTaskPercentage.visibility = View.VISIBLE
                txtDayTaskPercentage.text = list.days[position].progress.toInt().toString() + "%"
            }
            dayProgress.setProgress(list.days[position].progress, 100.0)
            layoutDayWise.setOnClickListener {
                adapterItemClickListener!!.onCarePlanDayWiseItemClickListener(list, list.days[position])
            }
        }
    }

    inner class ViewHolder(val binding: LayoutItemCarePlanDayBinding) :
        RecyclerView.ViewHolder(binding.root)
}