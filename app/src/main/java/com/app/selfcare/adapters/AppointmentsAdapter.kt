package com.app.selfcare.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.BaseActivity
import com.app.selfcare.R
import com.app.selfcare.controller.OnAppointmentItemClickListener
import com.app.selfcare.data.GetAppointment
import com.app.selfcare.databinding.LayoutItemAppointmentsBinding
import com.app.selfcare.databinding.LayoutItemGoalBinding
import com.app.selfcare.utils.DateUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

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
        val binding = LayoutItemAppointmentsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        /*val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_appointments, parent, false)*/
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        if (item.is_group_appointment) {
            holder.binding.txtTherapistName.text =
                if (item.meeting_title == null) "Group Appointment" else item.meeting_title
            holder.binding.txtAppointmentTherapistType.text = item.doctor_first_name + " " + item.doctor_last_name
            val dateTime = DateUtils(item.group_appointment.date + " " + "00:00:00")
            holder.binding.txtAppointmentDateTime.text =
                dateTime.getCurrentDay() + ", " +
                        dateTime.getDay() + " " +
                        dateTime.getMonth() + " at " +
                        item.group_appointment.time + " " + item.group_appointment.select_am_or_pm
        } else {
            holder.binding.txtTherapistName.text = item.doctor_first_name + " " + item.doctor_last_name
            holder.binding.txtAppointmentTherapistType.text = item.doctor_designation
            var dateTime = DateUtils(item.appointment.date + " " + "00:00:00")
            if (item.appointment.booking_date != null) {
                dateTime = DateUtils(item.appointment.booking_date + " " + "00:00:00")
            }
            holder.binding.txtAppointmentDateTime.text =
                dateTime.getCurrentDay() + ", " +
                        dateTime.getDay() + " " +
                        dateTime.getMonth() + " at " +
                        item.appointment.time_slot.starting_time.dropLast(3) + " - " +
                        item.appointment.time_slot.ending_time.dropLast(3)
        }

        if (!item.is_group_appointment) {
            when (item.appointment.type_of_visit) {
                "Video" -> {
                    holder.binding.appointmentCall.setImageResource(R.drawable.video)
                    holder.binding.appointmentCall.imageTintList =
                        ColorStateList.valueOf(
                            ContextCompat.getColor(
                                context,
                                R.color.primaryGreen
                            )
                        )
                }
                "Audio" -> {
                    holder.binding.appointmentCall.setImageResource(R.drawable.telephone)
                    holder.binding.appointmentCall.imageTintList =
                        ColorStateList.valueOf(
                            ContextCompat.getColor(
                                context,
                                R.color.primaryGreen
                            )
                        )
                }
                else -> {
                    holder.binding.appointmentCall.setImageResource(R.drawable.chat)
                    holder.binding.appointmentCall.imageTintList =
                        ColorStateList.valueOf(
                            ContextCompat.getColor(
                                context,
                                R.color.primaryGreen
                            )
                        )
                }
            }
        } else {
            holder.binding.appointmentCall.setImageResource(R.drawable.video)
            holder.binding.appointmentCall.imageTintList =
                ColorStateList.valueOf(ContextCompat.getColor(context, R.color.primaryGreen))
        }

        if (item.is_group_appointment) {
            holder.binding.appointListImgUser.visibility = View.GONE
            holder.binding.appointListGroupImg.visibility = View.VISIBLE
        } else {
            holder.binding.appointListImgUser.visibility = View.VISIBLE
            holder.binding.appointListGroupImg.visibility = View.GONE
            Glide.with(context).load(BaseActivity.baseURL.dropLast(5) + item.doctor_photo)
                .placeholder(R.drawable.doctor_img)
                .transform(CenterCrop(), RoundedCorners(5))
                .into(holder.binding.appointListImgUser)
        }

        if (!item.is_group_appointment) {
            when (item.appointment.status) {
                5 -> {
                    holder.binding.txtMissedByProviderOrYou.visibility = View.GONE
                    holder.binding.txtCancelledAppt.visibility = View.VISIBLE
                }
                6 -> {
                    holder.binding.txtCancelledAppt.visibility = View.GONE
                    holder.binding.txtMissedByProviderOrYou.visibility = View.VISIBLE
                }
                else -> {
                    holder.binding.txtCancelledAppt.visibility = View.GONE
                    holder.binding.txtMissedByProviderOrYou.visibility = View.GONE
                }
            }
        }

        if (appointmentType == "Upcoming") {
            holder.binding.imgCancelAppointment.visibility = View.VISIBLE
        }

        holder.binding.cardViewAppointmentItem.setOnClickListener {
            if (appointmentType == "Today")
                adapterItemClickListener!!.onAppointmentItemClickListener(item, true)
        }

        holder.binding.imgCancelAppointment.setOnClickListener {
            if (appointmentType == "Upcoming") {
                adapterItemClickListener!!.onAppointmentItemClickListener(item, false)
            }
        }
    }

    inner class ViewHolder(val binding: LayoutItemAppointmentsBinding) :
        RecyclerView.ViewHolder(binding.root)
}