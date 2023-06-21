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
import ensemblecare.csardent.com.databinding.LayoutItemCarePlanExerciseTaskBinding

class CarePlanExerciseTaskListAdapter(
    private val context: Context,
    private val list: ArrayList<CareDayIndividualTaskDetail>,
    private val adapterClick: OnCarePlanPendingTaskItemClickListener,
    private val itemClick: OnCarePlanTaskViewClickListener
) :
    RecyclerView.Adapter<CarePlanExerciseTaskListAdapter.ViewHolder>() {

    var pos = -1

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CarePlanExerciseTaskListAdapter.ViewHolder {
        val binding = LayoutItemCarePlanExerciseTaskBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        /*val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_care_plan_exercise_task, parent, false)*/
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.binding.apply {
            val item = list[position]

            txtExerciseCompletedTaskTitle.text = item.duration
            txtExerciseCompletedTaskSubTitle.text = item.task_detail.details.exercise_name

            txtExercisePendingTaskTitle.text = item.duration
            txtExercisePendingTaskSubTitle.text = item.task_detail.details.exercise_name

            txtExercisePendingLaterTaskTitle.text = item.duration
            txtExercisePendingLaterTaskSubTitle.text = item.task_detail.details.exercise_name

            if (item.is_completed) {
                layoutExercisePendingTask.visibility = View.GONE
                layoutExercisePendingLaterTask.visibility = View.GONE
                layoutExerciseCompletedTask.visibility = View.VISIBLE
                if ((position + 1) < list.size) {
                    progressExerciseCompletedTask.visibility = View.VISIBLE
                    if (list[position + 1].is_completed) {
                        progressExerciseCompletedTask.progress = 50.0F
                    }
                } else {
                    progressExerciseCompletedTask.visibility = View.GONE
                }
            }

            if (pos == -1) {
                if (!item.is_completed) {
                    layoutExercisePendingLaterTask.visibility = View.GONE
                    layoutExerciseCompletedTask.visibility = View.GONE
                    layoutExercisePendingTask.visibility = View.VISIBLE
                    if ((position + 1) < list.size) {
                        progressExercisePendingTask.visibility = View.VISIBLE
                        pos = position + 1
                    } else {
                        progressExercisePendingTask.visibility = View.GONE
                    }
                }
            }

            if (pos == position) {
                pos = position + 1
                layoutExerciseCompletedTask.visibility = View.GONE
                layoutExercisePendingTask.visibility = View.GONE
                layoutExercisePendingLaterTask.visibility = View.VISIBLE
                if (pos < list.size) {
                    progressExercisePendingLaterTask.visibility = View.VISIBLE
                } else {
                    progressExercisePendingLaterTask.visibility = View.GONE
                }
            }

            layoutExercisePendingTaskBox.setOnClickListener {
                itemClick.onCarePlanTaskViewClickListener(item)
            }

            cardViewExercisePendingTask.setOnClickListener {
                adapterClick.onCarePlanPendingTaskItemClickListener(item, true)
            }

            cardViewExerciseCompletedTask.setOnClickListener {
                adapterClick.onCarePlanPendingTaskItemClickListener(item, false)
            }
        }
    }

    inner class ViewHolder(val binding: LayoutItemCarePlanExerciseTaskBinding) :
        RecyclerView.ViewHolder(binding.root)
}