package com.app.selfcare.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.R
import com.app.selfcare.controller.OnAppointmentItemClickListener
import com.app.selfcare.data.Appointment
import com.app.selfcare.utils.DateUtils
import kotlinx.android.synthetic.main.layout_item_appointments.view.*
import kotlinx.android.synthetic.main.layout_item_dashboard_appointment.view.*
import kotlinx.android.synthetic.main.layout_item_dashboard_appointment.view.txtTherapistType

class AppointmentsAdapter(
    val context: Context,
    val list: List<Appointment>,
    private val adapterItemClickListener: OnAppointmentItemClickListener?
) :
    RecyclerView.Adapter<AppointmentsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AppointmentsAdapter.ViewHolder {
        val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_appointments, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: AppointmentsAdapter.ViewHolder, position: Int) {
        val item = list[position]
        val dateTime = DateUtils(item.booking_date + " " + item.starting_time.dropLast(3) + ":00")
        holder.appointmentDateTime.text =
            dateTime.getDay() + " " + dateTime.getMonthYear() + "  " + dateTime.getTime()
        holder.therapistName.text = item.first_name + " " + item.last_name
        holder.therapyType.text = item.doctor_type
        holder.startAppointment.setOnClickListener {
            adapterItemClickListener!!.onAppointmentItemClickListener(item, true)
        }
        holder.cancelAppointment.setOnClickListener {
            adapterItemClickListener!!.onAppointmentItemClickListener(item, false)
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val appointmentDateTime: TextView = itemView.appointmentDateTime
        val therapistName: TextView = itemView.txtTherapistName
        val therapyType: TextView = itemView.txtTherapistType
        val startAppointment: CardView = itemView.startAppointment
        val cancelAppointment: CardView = itemView.cancelAppointment
    }
}