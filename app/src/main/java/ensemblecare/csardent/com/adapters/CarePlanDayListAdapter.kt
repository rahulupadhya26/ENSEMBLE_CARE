package ensemblecare.csardent.com.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import ensemblecare.csardent.com.R
import ensemblecare.csardent.com.controller.OnCarePlanDayItemClickListener
import ensemblecare.csardent.com.databinding.LayoutItemCarePlanDayListBinding

class CarePlanDayListAdapter(
    private val context: Context,
    private val totalDays: Int,
    private val selectedDayNo: Int,
    private val type: String,
    private val adapterItemClickListener: OnCarePlanDayItemClickListener?
) :
    RecyclerView.Adapter<CarePlanDayListAdapter.ViewHolder>() {

    var rowIndex: Int = -1

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CarePlanDayListAdapter.ViewHolder {
        val binding = LayoutItemCarePlanDayListBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        /*val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_care_plan_day_list, parent, false)*/
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return totalDays
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            if (totalDays < 10) {
                txtDayNumber.text = "0" + (position + 1)
            } else {
                txtDayNumber.text = (position + 1).toString()
            }

            layoutDayWiseNumber.setOnClickListener {
                rowIndex = position
                layoutDayWiseNumber.backgroundTintList =
                    ContextCompat.getColorStateList(context, R.color.white)
                txtDayTitle.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.carePlanDayNumber
                    )
                )
                txtDayNumber.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.carePlanDayNumber
                    )
                )
                notifyDataSetChanged()
                adapterItemClickListener!!.onCarePlanDayItemClickListener((position + 1))
            }

            if (rowIndex == position) {
                when (type) {
                    "Exercise" -> {
                        layoutDayWiseNumber.backgroundTintList =
                            ContextCompat.getColorStateList(context, R.color.exercise_select_color)
                    }
                    "Nutrition" -> {
                        layoutDayWiseNumber.backgroundTintList =
                            ContextCompat.getColorStateList(context, R.color.nutrition_select_color)
                    }
                    "Mindfulness" -> {
                        layoutDayWiseNumber.backgroundTintList =
                            ContextCompat.getColorStateList(context, R.color.mindfulness_select_color)
                    }
                    "Music" -> {
                        layoutDayWiseNumber.backgroundTintList =
                            ContextCompat.getColorStateList(context, R.color.music_select_color)
                    }
                    "Yoga" -> {
                        layoutDayWiseNumber.backgroundTintList =
                            ContextCompat.getColorStateList(context, R.color.yoga_select_color)
                    }
                    else -> {
                        layoutDayWiseNumber.backgroundTintList =
                            ContextCompat.getColorStateList(context, R.color.black)
                    }
                }

                txtDayTitle.setTextColor(ContextCompat.getColor(context, R.color.white))
                txtDayNumber.setTextColor(ContextCompat.getColor(context, R.color.white))
            } else {
                layoutDayWiseNumber.backgroundTintList =
                    ContextCompat.getColorStateList(context, R.color.white)
                txtDayTitle.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.carePlanDayNumber
                    )
                )
                txtDayNumber.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.carePlanDayNumber
                    )
                )
            }

            if (selectedDayNo == (position + 1)) {
                when (type) {
                    "Exercise" -> {
                        layoutDayWiseNumber.backgroundTintList =
                            ContextCompat.getColorStateList(context, R.color.exercise_select_color)
                    }
                    "Nutrition" -> {
                        layoutDayWiseNumber.backgroundTintList =
                            ContextCompat.getColorStateList(context, R.color.nutrition_select_color)
                    }
                    "Mindfulness" -> {
                        layoutDayWiseNumber.backgroundTintList =
                            ContextCompat.getColorStateList(context, R.color.mindfulness_select_color)
                    }
                    "Music" -> {
                        layoutDayWiseNumber.backgroundTintList =
                            ContextCompat.getColorStateList(context, R.color.music_select_color)
                    }
                    "Yoga" -> {
                        layoutDayWiseNumber.backgroundTintList =
                            ContextCompat.getColorStateList(context, R.color.yoga_select_color)
                    }
                    else -> {
                        layoutDayWiseNumber.backgroundTintList =
                            ContextCompat.getColorStateList(context, R.color.black)
                    }
                }
                txtDayTitle.setTextColor(ContextCompat.getColor(context, R.color.white))
                txtDayNumber.setTextColor(ContextCompat.getColor(context, R.color.white))
            }
        }
    }

    inner class ViewHolder(val binding: LayoutItemCarePlanDayListBinding) :
        RecyclerView.ViewHolder(binding.root)
}