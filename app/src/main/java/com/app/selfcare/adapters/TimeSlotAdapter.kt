package com.app.selfcare.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.R
import com.app.selfcare.controller.OnTextClickListener
import com.app.selfcare.data.TimeSlot
import kotlinx.android.synthetic.main.time_slot_item.view.*

class TimeSlotAdapter(
    val context: Context,
    val list: ArrayList<TimeSlot>,
    private val onTextClickListener: OnTextClickListener?
) :
    RecyclerView.Adapter<TimeSlotAdapter.ViewHolder>() {
    var row_index: Int = -1
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TimeSlotAdapter.ViewHolder {
        val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.time_slot_item, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: TimeSlotAdapter.ViewHolder, position: Int) {
        val item = list[position]
        holder.timeSlotTxt.text =
            item.time_slot_start/* + " - " + item.time_slot_end.dropLast(3)*/
        holder.timeSlotLayout.setOnClickListener {
            row_index = position;
            notifyDataSetChanged()
            onTextClickListener!!.onTextClickListener(item)
        }
        if (row_index == position) {
            holder.cardViewTimeSlot.setCardBackgroundColor(ContextCompat.getColor(context, R.color.primaryGreen))
            //holder.timeSlotLayout.setBackgroundResource(R.drawable.bg_time_slot_rounded_selected)
            holder.timeSlotTxt.setTextColor(ContextCompat.getColor(context, R.color.white))
        } else {
            holder.cardViewTimeSlot.setCardBackgroundColor(ContextCompat.getColor(context, R.color.white))
            holder.timeSlotTxt.setTextColor(ContextCompat.getColor(context, R.color.primaryGreen))
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val timeSlotTxt: TextView = itemView.txtTimeSlot
        val timeSlotLayout: LinearLayout = itemView.layoutTimeSlot
        val cardViewTimeSlot :CardView = itemView.cardViewTimeSlot
    }
}