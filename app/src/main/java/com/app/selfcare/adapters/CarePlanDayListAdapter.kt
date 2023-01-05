package com.app.selfcare.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.R
import com.app.selfcare.controller.OnCarePlanDayItemClickListener
import com.app.selfcare.fragment.ExerciseCarePlanFragment
import kotlinx.android.synthetic.main.layout_item_care_plan_day_list.view.*

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
        val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_care_plan_day_list, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return totalDays
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: CarePlanDayListAdapter.ViewHolder, position: Int) {
        if (totalDays < 10) {
            holder.txtDayNumber.text = "0" + (position + 1)
        } else {
            holder.txtDayNumber.text = (position + 1).toString()
        }

        holder.layoutDayWiseNumber.setOnClickListener {
            rowIndex = position
            holder.layoutDayWiseNumber.backgroundTintList =
                ContextCompat.getColorStateList(context, R.color.white)
            holder.txtDayTitle.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.carePlanDayNumber
                )
            )
            holder.txtDayNumber.setTextColor(
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
                    holder.layoutDayWiseNumber.backgroundTintList =
                        ContextCompat.getColorStateList(context, R.color.exercise_select_color)
                }
                "Nutrition" -> {
                    holder.layoutDayWiseNumber.backgroundTintList =
                        ContextCompat.getColorStateList(context, R.color.nutrition_select_color)
                }
                "Mindfulness" -> {
                    holder.layoutDayWiseNumber.backgroundTintList =
                        ContextCompat.getColorStateList(context, R.color.mindfulness_select_color)
                }
                "Music" -> {
                    holder.layoutDayWiseNumber.backgroundTintList =
                        ContextCompat.getColorStateList(context, R.color.music_select_color)
                }
                "Yoga" -> {
                    holder.layoutDayWiseNumber.backgroundTintList =
                        ContextCompat.getColorStateList(context, R.color.yoga_select_color)
                }
                else -> {
                    holder.layoutDayWiseNumber.backgroundTintList =
                        ContextCompat.getColorStateList(context, R.color.black)
                }
            }

            holder.txtDayTitle.setTextColor(ContextCompat.getColor(context, R.color.white))
            holder.txtDayNumber.setTextColor(ContextCompat.getColor(context, R.color.white))
        } else {
            holder.layoutDayWiseNumber.backgroundTintList =
                ContextCompat.getColorStateList(context, R.color.white)
            holder.txtDayTitle.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.carePlanDayNumber
                )
            )
            holder.txtDayNumber.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.carePlanDayNumber
                )
            )
        }

        if (selectedDayNo == (position + 1)) {
            when (type) {
                "Exercise" -> {
                    holder.layoutDayWiseNumber.backgroundTintList =
                        ContextCompat.getColorStateList(context, R.color.exercise_select_color)
                }
                "Nutrition" -> {
                    holder.layoutDayWiseNumber.backgroundTintList =
                        ContextCompat.getColorStateList(context, R.color.nutrition_select_color)
                }
                "Mindfulness" -> {
                    holder.layoutDayWiseNumber.backgroundTintList =
                        ContextCompat.getColorStateList(context, R.color.mindfulness_select_color)
                }
                "Music" -> {
                    holder.layoutDayWiseNumber.backgroundTintList =
                        ContextCompat.getColorStateList(context, R.color.music_select_color)
                }
                "Yoga" -> {
                    holder.layoutDayWiseNumber.backgroundTintList =
                        ContextCompat.getColorStateList(context, R.color.yoga_select_color)
                }
                else -> {
                    holder.layoutDayWiseNumber.backgroundTintList =
                        ContextCompat.getColorStateList(context, R.color.black)
                }
            }
            holder.txtDayTitle.setTextColor(ContextCompat.getColor(context, R.color.white))
            holder.txtDayNumber.setTextColor(ContextCompat.getColor(context, R.color.white))
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtDayNumber: TextView = itemView.txtDayNumber
        val txtDayTitle: TextView = itemView.txtDayTitle
        val layoutDayWiseNumber: LinearLayout = itemView.layoutDayWiseNumber
    }
}