package ensemblecare.csardent.com.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ensemblecare.csardent.com.controller.OnCarePlanPendingTaskItemClickListener
import ensemblecare.csardent.com.data.CareDayIndividualTaskDetail
import ensemblecare.csardent.com.databinding.LayoutItemCarebuddyCarePlanDataBinding

class CareBuddyCarePlanDataAdapter(
    val context: Context,
    val list: List<CareDayIndividualTaskDetail>,
    private val adapterItemClickListener: OnCarePlanPendingTaskItemClickListener?
) :
    RecyclerView.Adapter<CareBuddyCarePlanDataAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CareBuddyCarePlanDataAdapter.ViewHolder {
        val binding =
            LayoutItemCarebuddyCarePlanDataBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        /*val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_carebuddy_care_plan_data, parent, false)*/
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            val item = list[position]
            if (item.task_detail.yoga != 0) {
                txtCareBuddyCarePlanTitle.text = "Yoga"
            } else if (item.task_detail.exercise != 0) {
                txtCareBuddyCarePlanTitle.text = "Exercise"
            } else if (item.task_detail.mindfulness != 0) {
                txtCareBuddyCarePlanTitle.text = "Mindfulness"
            } else if (item.task_detail.nutrition != 0) {
                txtCareBuddyCarePlanTitle.text = "Nutrition"
            } else if (item.task_detail.music != 0) {
                txtCareBuddyCarePlanTitle.text = "Music"
            }

            txtCareBuddyCarePlanTask.text = item.task_detail.title
            if (item.is_completed) {
                txtCareBuddyCarePlanStatus.text = "Completed"
            } else {
                txtCareBuddyCarePlanStatus.text = "In Progress"
            }

            layoutCareBuddyCarePlanDayWiseData.setOnClickListener {
                adapterItemClickListener!!.onCarePlanPendingTaskItemClickListener(
                    item,
                    item.is_completed
                )
            }
        }
    }

    inner class ViewHolder(val binding: LayoutItemCarebuddyCarePlanDataBinding) :
        RecyclerView.ViewHolder(binding.root)
}