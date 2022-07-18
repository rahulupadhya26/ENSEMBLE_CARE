package com.app.selfcare.adapters

import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.R
import com.app.selfcare.data.OptionModel
import kotlinx.android.synthetic.main.layout_item_option_list.view.*

class OptionListAdapter(
    val context: Context,
    val list: ArrayList<OptionModel>
) :
    RecyclerView.Adapter<OptionListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): OptionListAdapter.ViewHolder {
        val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_option_list, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: OptionListAdapter.ViewHolder, position: Int) {
        val item = list[position]
        holder.option.text = item.text
        if (item.isSelected) {
            holder.option.setTextColor(context.getColor(R.color.white))
            holder.option.setTypeface(holder.option.typeface, Typeface.BOLD)
            holder.option.background =
                ContextCompat.getDrawable(context, R.drawable.bg_box_border_selected)
        } else {
            holder.option.setTextColor(context.getColor(R.color.secondary_text))
            holder.option.typeface = Typeface.DEFAULT
            holder.option.background =
                ContextCompat.getDrawable(context, R.drawable.bg_box_border_grey)
        }
        holder.option.setOnClickListener {
            item.isSelected = !item.isSelected
            if (item.isSelected) {
                holder.option.setTextColor(context.getColor(R.color.white))
                holder.option.setTypeface(holder.option.typeface, Typeface.BOLD)
                holder.option.background =
                    ContextCompat.getDrawable(context, R.drawable.bg_box_border_selected)
            } else {
                holder.option.setTextColor(context.getColor(R.color.secondary_text))
                holder.option.typeface = Typeface.DEFAULT
                holder.option.background =
                    ContextCompat.getDrawable(context, R.drawable.bg_box_border_grey)
            }
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val option: TextView = itemView.tv_option
    }
}