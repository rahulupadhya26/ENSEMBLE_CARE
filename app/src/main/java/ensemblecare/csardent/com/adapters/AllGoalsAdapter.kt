package ensemblecare.csardent.com.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ensemblecare.csardent.com.controller.OnGoalItemClickListener
import ensemblecare.csardent.com.data.Goal
import ensemblecare.csardent.com.databinding.LayoutItemGoalBinding
import ensemblecare.csardent.com.utils.DateUtils
import kotlin.math.min

class AllGoalsAdapter(
    val context: Context,
    val list: List<Goal>, private val adapterItemClickListener: OnGoalItemClickListener?,
    private val isProviderGoal: Boolean
) :
    RecyclerView.Adapter<AllGoalsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = LayoutItemGoalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        /*val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_goal, parent, false)*/
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        val limit = 4
        return min(list.size, limit)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        if (isProviderGoal) {
            holder.binding.txtDeleteGoal.visibility = View.GONE
        } else {
            holder.binding.txtDeleteGoal.visibility = View.VISIBLE
        }
        val goalDate = DateUtils(item.start_date + " 01:00:00")
        holder.binding.goalDate.text = goalDate.getFormattedDate()
        holder.binding.goalTitle.text = item.title
        holder.binding.txtGoalDesc.text = item.description
        var durationTxt = ""
        when (item.duration) {
            0 -> durationTxt = "Does not repeat"
            1 -> durationTxt = "Everyday"
            2 -> durationTxt = "Every week"
            3 -> durationTxt = "Every month"
            4 -> durationTxt = "Every year"
        }
        holder.binding.goalDuration.text = durationTxt
        holder.binding.cardviewGoal.setOnClickListener {
            adapterItemClickListener!!.onGoalItemClickListener(item, false)
        }
        holder.binding.txtDeleteGoal.setOnClickListener {
            adapterItemClickListener!!.onGoalItemClickListener(item, true)
        }
    }

    inner class ViewHolder(val binding: LayoutItemGoalBinding) :
        RecyclerView.ViewHolder(binding.root)
}