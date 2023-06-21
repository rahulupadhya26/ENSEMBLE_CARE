package ensemblecare.csardent.com.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ensemblecare.csardent.com.controller.OnGoalItemClickListener
import ensemblecare.csardent.com.data.Goal
import ensemblecare.csardent.com.databinding.LayoutItemPersonalGoalBinding
import ensemblecare.csardent.com.utils.DateUtils

class PersonalGoalAdapter(
    val context: Context,
    val list: List<Goal>, private val adapterItemClickListener: OnGoalItemClickListener?
) :
    RecyclerView.Adapter<PersonalGoalAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PersonalGoalAdapter.ViewHolder {
        val binding = LayoutItemPersonalGoalBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        /*val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_personal_goal, parent, false)*/
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            val item = list[position]
            val goalDate = DateUtils(item.start_date + " 01:00:00")
            tvGoalMonth.text = goalDate.getFullMonthName()
            tvGoalDate.text = goalDate.getDay()
            tvGoalTitle.text = item.title
            var durationTxt = ""
            when (item.duration) {
                0 -> durationTxt = "Does not repeat"
                1 -> durationTxt = "Everyday"
                2 -> durationTxt = "Every week"
                3 -> durationTxt = "Every month"
                4 -> durationTxt = "Every year"
            }
            tvGoalDuration.text = durationTxt
            cardviewGoal.setOnClickListener {
                adapterItemClickListener!!.onGoalItemClickListener(item, false)
            }
            deleteGoal.setOnClickListener {
                adapterItemClickListener!!.onGoalItemClickListener(item, true)
            }
        }
    }

    inner class ViewHolder(val binding: LayoutItemPersonalGoalBinding) :
        RecyclerView.ViewHolder(binding.root)
}