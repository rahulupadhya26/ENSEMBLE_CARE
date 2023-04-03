package com.app.selfcare.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.controller.OnGroupAppointmentItemClickListener
import com.app.selfcare.data.GroupAppointment
import com.app.selfcare.databinding.LayoutItemGroupAppointmentsBinding

class GroupAppointmentsAdapter(
    val context: Context,
    val list: List<GroupAppointment>,
    private val adapterItemClickListener: OnGroupAppointmentItemClickListener?
) :
    RecyclerView.Adapter<GroupAppointmentsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): GroupAppointmentsAdapter.ViewHolder {
        val binding = LayoutItemGroupAppointmentsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        /*val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_group_appointments, parent, false)*/
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

    }

    inner class ViewHolder(val binding: LayoutItemGroupAppointmentsBinding) :
        RecyclerView.ViewHolder(binding.root)
}