package com.app.selfcare.adapters

import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.R
import com.app.selfcare.data.OptionModel
import com.app.selfcare.databinding.LayoutItemOptionListBinding

class OptionListAdapter(
    val context: Context,
    val list: ArrayList<OptionModel>
) :
    RecyclerView.Adapter<OptionListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): OptionListAdapter.ViewHolder {
        val binding =
            LayoutItemOptionListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        /*val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_option_list, parent, false)*/
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            val item = list[position]
            tvOption.text = item.text
            if (item.isSelected) {
                tvOption.setTextColor(context.getColor(R.color.white))
                tvOption.setTypeface(tvOption.typeface, Typeface.BOLD)
                tvOption.background =
                    ContextCompat.getDrawable(context, R.drawable.bg_box_border_selected)
            } else {
                tvOption.setTextColor(context.getColor(R.color.secondary_text))
                tvOption.typeface = Typeface.DEFAULT
                tvOption.background =
                    ContextCompat.getDrawable(context, R.drawable.bg_box_border_grey)
            }
            tvOption.setOnClickListener {
                item.isSelected = !item.isSelected
                if (item.isSelected) {
                    tvOption.setTextColor(context.getColor(R.color.white))
                    tvOption.setTypeface(tvOption.typeface, Typeface.BOLD)
                    tvOption.background =
                        ContextCompat.getDrawable(context, R.drawable.bg_box_border_selected)
                } else {
                    tvOption.setTextColor(context.getColor(R.color.secondary_text))
                    tvOption.typeface = Typeface.DEFAULT
                    tvOption.background =
                        ContextCompat.getDrawable(context, R.drawable.bg_box_border_grey)
                }
            }
        }
    }

    inner class ViewHolder(val binding: LayoutItemOptionListBinding) :
        RecyclerView.ViewHolder(binding.root)
}