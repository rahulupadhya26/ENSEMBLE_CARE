package com.app.selfcare.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.controller.OnCareBuddyDashboardItemClickListener
import com.app.selfcare.data.CareBuddyDashboard
import com.app.selfcare.databinding.LayoutItemCarebuddyDashboardBinding
import java.util.*

class CareBuddyDashboardAdapter(
    val context: Context,
    val list: List<CareBuddyDashboard>,
    private val adapterItemClickListener: OnCareBuddyDashboardItemClickListener?
) :
    RecyclerView.Adapter<CareBuddyDashboardAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CareBuddyDashboardAdapter.ViewHolder {
        val binding =
            LayoutItemCarebuddyDashboardBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        /*val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_carebuddy, parent, false)*/
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.binding.txtCbDashboardNameFirstLetter.visibility = View.VISIBLE
        holder.binding.txtCbDashboardNameFirstLetter.text = item.name.substring(0, 1).uppercase()
        holder.binding.imgCareBuddyDashboard.visibility = View.GONE

        holder.binding.txtCareBuddyDashboardName.text = item.name

        holder.binding.layoutCareBuddyDashboard.setOnClickListener {
            adapterItemClickListener!!.onCareBuddyDashboardItemClickListener(item)
        }
    }

    inner class ViewHolder(val binding: LayoutItemCarebuddyDashboardBinding) :
        RecyclerView.ViewHolder(binding.root)
}