package com.app.selfcare.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.BaseActivity
import com.app.selfcare.R
import com.app.selfcare.controller.OnAppointmentItemClickListener
import com.app.selfcare.data.GetAppointment
import com.app.selfcare.utils.DateUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import kotlinx.android.synthetic.main.layout_item_appointments.view.*

class AppointmentsAdapter(
    val context: Context,
    val list: List<GetAppointment>,
    private val appointmentType: String,
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
        if (item.is_group_appointment) {
            holder.therapistName.text =
                if (item.meeting_title == null) "Group Appointment" else item.meeting_title
            holder.therapyType.text = item.doctor_first_name + " " + item.doctor_last_name
            val dateTime = DateUtils(item.group_appointment.date + " " + "00:00:00")
            holder.appointmentDateTime.text =
                dateTime.getCurrentDay() + ", " +
                        dateTime.getDay() + " " +
                        dateTime.getMonth() + " at " +
                        item.group_appointment.time + " " + item.group_appointment.select_am_or_pm
        } else {
            holder.therapistName.text = item.doctor_first_name + " " + item.doctor_last_name
            holder.therapyType.text = item.doctor_designation
            val dateTime = DateUtils(item.appointment.booking_date + " " + "00:00:00")
            holder.appointmentDateTime.text =
                dateTime.getCurrentDay() + ", " +
                        dateTime.getDay() + " " +
                        dateTime.getMonth() + " at " +
                        item.appointment.time_slot.starting_time.dropLast(3) + " - " +
                        item.appointment.time_slot.ending_time.dropLast(3)
        }

        if (!item.is_group_appointment) {
            if (item.appointment.type_of_visit == "Video") {
                holder.appointmentCall.setImageResource(R.drawable.video)
                holder.appointmentCall.imageTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(context, R.color.primaryGreen))
            } else {
                holder.appointmentCall.setImageResource(R.drawable.telephone)
                holder.appointmentCall.imageTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(context, R.color.primaryGreen))
            }
        } else {
            holder.appointmentCall.setImageResource(R.drawable.telephone)
            holder.appointmentCall.imageTintList =
                ColorStateList.valueOf(ContextCompat.getColor(context, R.color.primaryGreen))
        }

        if (item.is_group_appointment) {
            holder.therapyImage.visibility = View.GONE
            holder.groupAppointmentImg.visibility = View.VISIBLE
        } else {
            holder.therapyImage.visibility = View.VISIBLE
            holder.groupAppointmentImg.visibility = View.GONE
            Glide.with(context).load(BaseActivity.baseURL.dropLast(5) + item.doctor_photo)
                .placeholder(R.drawable.doctor_icon)
                .transform(CenterCrop(), RoundedCorners(5))
                .into(holder.therapyImage)
        }

        holder.startAppointment.setOnClickListener {
            if (appointmentType == "Today")
                adapterItemClickListener!!.onAppointmentItemClickListener(item, true)
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val appointmentDateTime: TextView = itemView.txtAppointmentDateTime
        val therapistName: TextView = itemView.txtTherapistName
        val therapyType: TextView = itemView.txtAppointmentTherapistType
        val therapyImage: ImageView = itemView.appointListImgUser
        val groupAppointmentImg: ImageView = itemView.appointListGroupImg
        val appointmentCall: ImageView = itemView.appointmentCall
        val startAppointment: CardView = itemView.cardViewAppointmentItem
    }
}