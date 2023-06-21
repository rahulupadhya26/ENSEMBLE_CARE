package ensemblecare.csardent.com.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ensemblecare.csardent.com.controller.OnCarePlanTaskViewClickListener
import ensemblecare.csardent.com.data.CareDayIndividualTaskDetail
import ensemblecare.csardent.com.databinding.LayoutItemCarePlanMusicTaskBinding

class CarePlanMusicTaskListAdapter(
    private val context: Context,
    private val list: ArrayList<CareDayIndividualTaskDetail>,
    private val itemClick: OnCarePlanTaskViewClickListener
) :
    RecyclerView.Adapter<CarePlanMusicTaskListAdapter.ViewHolder>() {

    var pos = -1

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CarePlanMusicTaskListAdapter.ViewHolder {
        val binding = LayoutItemCarePlanMusicTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        /*val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_care_plan_music_task, parent, false)*/
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            val item = list[position]

            txtMusicCompletedTaskTitle.text = item.duration
            txtMusicCompletedTaskSubTitle.text = item.task_detail.details.music_name

            txtMusicPendingTaskTitle.text = item.duration
            txtMusicPendingTaskSubTitle.text = item.task_detail.details.music_name

            txtMusicPendingLaterTaskTitle.text = item.duration
            txtMusicPendingLaterTaskSubTitle.text = item.task_detail.details.music_name

            if (item.is_completed) {
                layoutMusicPendingTask.visibility = View.GONE
                layoutMusicPendingLaterTask.visibility = View.GONE
                layoutMusicCompletedTask.visibility = View.VISIBLE
                if ((position + 1) < list.size) {
                    progressMusicCompletedTask.visibility = View.VISIBLE
                    if (list[position + 1].is_completed) {
                        progressMusicCompletedTask.progress = 50.0F
                    }
                } else {
                    progressMusicCompletedTask.visibility = View.GONE
                }
            }

            if (pos == -1) {
                if (!item.is_completed) {
                    layoutMusicPendingLaterTask.visibility = View.GONE
                    layoutMusicCompletedTask.visibility = View.GONE
                    layoutMusicPendingTask.visibility = View.VISIBLE
                    if ((position + 1) < list.size) {
                        progressMusicPendingTask.visibility = View.VISIBLE
                        pos = position + 1
                    } else {
                        progressMusicPendingTask.visibility = View.GONE
                    }
                }
            }

            if (pos == position) {
                pos = position + 1
                layoutMusicCompletedTask.visibility = View.GONE
                layoutMusicPendingTask.visibility = View.GONE
                layoutMusicPendingLaterTask.visibility = View.VISIBLE
                if (pos < list.size) {
                    progressMusicPendingLaterTask.visibility = View.VISIBLE
                } else {
                    progressMusicPendingLaterTask.visibility = View.GONE
                }
            }

            layoutMusicPendingTaskBox.setOnClickListener {
                itemClick.onCarePlanTaskViewClickListener(item)
            }

            layoutMusicPendingTask.setOnClickListener {
                //
            }
        }
    }

    inner class ViewHolder(val binding: LayoutItemCarePlanMusicTaskBinding) : RecyclerView.ViewHolder(binding.root)
}