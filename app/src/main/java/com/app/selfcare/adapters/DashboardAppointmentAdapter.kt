package com.app.selfcare.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.R
import com.app.selfcare.controller.OnAppointmentItemClickListener
import com.app.selfcare.data.Appointment
import kotlinx.android.synthetic.main.layout_item_dashboard_appointment.view.*

class DashboardAppointmentAdapter(
    val context: Context,
    val list: List<Appointment>,
    private val adapterItemClickListener: OnAppointmentItemClickListener?
) :
    RecyclerView.Adapter<DashboardAppointmentAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DashboardAppointmentAdapter.ViewHolder {
        val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_dashboard_appointment, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: DashboardAppointmentAdapter.ViewHolder, position: Int) {
        val item = list[position]
        holder.therapistName.text = item.first_name + " " + item.last_name
        holder.therapyType.text = item.doctor_type + ", 5 yrs"
        holder.therapyDateTime.text = item.booking_date + " " + item.starting_time.dropLast(3)
        holder.startAppointment.setOnClickListener {
            adapterItemClickListener!!.onAppointmentItemClickListener(item, true)
        }
        holder.cancelAppointment.setOnClickListener {
            adapterItemClickListener!!.onAppointmentItemClickListener(item, false)
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val therapistName: TextView = itemView.txtTherapist
        val therapyType: TextView = itemView.txtTherapistType
        val therapyDateTime: TextView = itemView.txtTherapyDateTime
        val startAppointment: LinearLayout = itemView.btnStartAppointment
        val cancelAppointment: LinearLayout = itemView.btnCancelAppointment
    }
}