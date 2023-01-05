package com.app.selfcare.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.R
import com.app.selfcare.controller.OnGroupAppointmentItemClickListener
import com.app.selfcare.data.GroupAppointment
import com.app.selfcare.utils.DateUtils
import kotlinx.android.synthetic.main.layout_item_group_appointments.view.*

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
        val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_group_appointments, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: GroupAppointmentsAdapter.ViewHolder, position: Int) {
        val item = list[position]

    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val appointmentDateTime: TextView = itemView.groupAppointmentDateTime
        val therapistName: TextView = itemView.txtGroupTherapistName
        val therapyType: TextView = itemView.txtGroupTherapistType
        val startAppointment: CardView = itemView.startGroupAppointment
    }
}