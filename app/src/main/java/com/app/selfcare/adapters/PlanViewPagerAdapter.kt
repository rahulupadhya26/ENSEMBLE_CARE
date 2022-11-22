package com.app.selfcare.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.R
import com.app.selfcare.controller.AdapterCallback
import com.app.selfcare.data.Plan


class PlanViewPagerAdapter(
    private val context: Context,
    private val planList: List<Plan>,
    private val onClickBack: AdapterCallback,
    private val selectedPlan: String
) : RecyclerView.Adapter<PlanViewPagerAdapter.PlanViewHolder>() {

    private var planName = "Plus"

    class PlanViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {
        val recommendImg: ImageView = itemView.findViewById(R.id.imgRecommended)
        val planText: TextView = itemView.findViewById(R.id.txtChoosePlan)
        val planPrice: TextView = itemView.findViewById(R.id.planPrice)
        val planBtn:TextView = itemView.findViewById(R.id.txtPlanBtn)
        val btnStartPlan: CardView = itemView.findViewById(R.id.btnStartToday)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlanViewHolder {
        return PlanViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_plan_adapter, parent, false
            )
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: PlanViewHolder, position: Int) {
        val planObj: Plan = planList[position]
        planName = if (selectedPlan.isNotEmpty()) {
            when (selectedPlan) {
                "Standard" -> "Plus"
                "Plus" -> "Premium"
                "Premium" -> "Premium"
                else -> "Plus"
            }
        } else {
            "Plus"
        }
        if (planObj.plan == "Plus") {
            holder.recommendImg.visibility = View.VISIBLE
        } else {
            holder.recommendImg.visibility = View.GONE
        }

        if (planObj.plan == selectedPlan) {
            holder.planBtn.text = "Selected"
        }
        /*when (planObj.plan) {
            "Standard" -> {
                holder.planText.setTextColor(ContextCompat.getColor(context, R.color.white))
            }
            "Plus" -> {
                holder.planText.setTextColor(ContextCompat.getColor(context, R.color.white))
            }
            "Premium" -> {
                holder.planText.setTextColor(ContextCompat.getColor(context, R.color.white))
            }
        }*/
        holder.planText.text = planObj.plan
        holder.planPrice.text = "$" + planObj.price
        holder.btnStartPlan.setOnClickListener {
            onClickBack.onItemClicked(planObj);
        }
    }

    override fun getItemCount() = planList.size
}