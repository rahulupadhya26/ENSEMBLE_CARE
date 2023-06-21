package ensemblecare.csardent.com.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ensemblecare.csardent.com.controller.OnCarePlanPendingTaskItemClickListener
import ensemblecare.csardent.com.controller.OnCarePlanTaskViewClickListener
import ensemblecare.csardent.com.data.CareDayIndividualTaskDetail
import ensemblecare.csardent.com.databinding.LayoutItemCarePlanYogaTaskBinding

class CarePlanYogaTaskListAdapter(
    private val context: Context,
    private val list: ArrayList<CareDayIndividualTaskDetail>,
    private val adapterClick: OnCarePlanPendingTaskItemClickListener,
    private val itemClick: OnCarePlanTaskViewClickListener
) :
    RecyclerView.Adapter<CarePlanYogaTaskListAdapter.ViewHolder>() {

    var pos = -1

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CarePlanYogaTaskListAdapter.ViewHolder {
        val binding = LayoutItemCarePlanYogaTaskBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        /*val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_care_plan_yoga_task, parent, false)*/
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            val item = list[position]

            txtYogaCompletedTaskTitle.text = item.duration
            txtYogaCompletedTaskSubTitle.text = item.task_detail.details.yoga_name

            txtYogaPendingTaskTitle.text = item.duration
            txtYogaPendingTaskSubTitle.text = item.task_detail.details.yoga_name

            txtYogaPendingLaterTaskTitle.text = item.duration
            txtYogaPendingLaterTaskSubTitle.text = item.task_detail.details.yoga_name

            if (item.is_completed) {
                layoutYogaPendingTask.visibility = View.GONE
                layoutYogaPendingLaterTask.visibility = View.GONE
                layoutYogaCompletedTask.visibility = View.VISIBLE
                if ((position + 1) < list.size) {
                    progressYogaCompletedTask.visibility = View.VISIBLE
                    if (list[position + 1].is_completed) {
                        progressYogaCompletedTask.progress = 50.0F
                    }
                } else {
                    progressYogaCompletedTask.visibility = View.GONE
                }
            }

            if (pos == -1) {
                if (!item.is_completed) {
                    layoutYogaPendingLaterTask.visibility = View.GONE
                    layoutYogaCompletedTask.visibility = View.GONE
                    layoutYogaPendingTask.visibility = View.VISIBLE
                    if ((position + 1) < list.size) {
                        progressYogaPendingTask.visibility = View.VISIBLE
                        pos = position + 1
                    } else {
                        progressYogaPendingTask.visibility = View.GONE
                    }
                }
            }

            if (pos == position) {
                pos = position + 1
                layoutYogaCompletedTask.visibility = View.GONE
                layoutYogaPendingTask.visibility = View.GONE
                layoutYogaPendingLaterTask.visibility = View.VISIBLE
                if (pos < list.size) {
                    progressYogaPendingLaterTask.visibility = View.VISIBLE
                } else {
                    progressYogaPendingLaterTask.visibility = View.GONE
                }
            }

            layoutYogaPendingTaskBox.setOnClickListener {
                itemClick.onCarePlanTaskViewClickListener(item)
            }

            cardViewYogaPendingTask.setOnClickListener {
                adapterClick.onCarePlanPendingTaskItemClickListener(item, true)
            }

            cardViewYogaCompletedTask.setOnClickListener {
                adapterClick.onCarePlanPendingTaskItemClickListener(item, false)
            }
        }
    }

    inner class ViewHolder(val binding: LayoutItemCarePlanYogaTaskBinding) :
        RecyclerView.ViewHolder(binding.root)
}