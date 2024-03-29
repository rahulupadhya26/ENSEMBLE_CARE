package ensemblecare.csardent.com.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import ensemblecare.csardent.com.R
import ensemblecare.csardent.com.controller.AdapterCallback
import ensemblecare.csardent.com.data.Plan


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
        if(!planObj.is_addon){
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
            /*if (planObj.therapy.name == "Plus") {
                holder.recommendImg.visibility = View.VISIBLE
            } else {
                holder.recommendImg.visibility = View.GONE
            }*/

            if (planObj.name == selectedPlan) {
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
            holder.planText.text = planObj.name
            holder.planPrice.text = "$" + planObj.monthly_price
            holder.btnStartPlan.setOnClickListener {
                onClickBack.onItemClicked(planObj);
            }
        }
    }

    override fun getItemCount() = planList.size
}