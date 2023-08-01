package ensemblecare.csardent.com.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import ensemblecare.csardent.com.BaseActivity
import ensemblecare.csardent.com.R
import ensemblecare.csardent.com.controller.OnAppointmentItemClickListener
import ensemblecare.csardent.com.data.GetAppointment
import ensemblecare.csardent.com.databinding.LayoutItemAppointmentsBinding
import ensemblecare.csardent.com.utils.DateUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import ensemblecare.csardent.com.controller.OnRescheduleAppointment
import ensemblecare.csardent.com.utils.DateMethods
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

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
                if (item.group_name == null) "Group Appointment" else item.group_name
            holder.binding.txtAppointmentTherapistType.text =
                item.doctor_first_name + " " + item.doctor_last_name
            val dateTime = DateUtils(item.group_appointment!!.date + " " + "00:00:00")
            if (item.group_appointment.starttime != null) {
                holder.binding.txtAppointmentDateTime.text =
                    dateTime.getCurrentDay() + ", " +
                            dateTime.getDay() + " " +
                            dateTime.getMonth() + " at " +
                            item.group_appointment.starttime.dropLast(3)
            } else {
                holder.binding.txtAppointmentDateTime.text =
                    dateTime.getCurrentDay() + ", " +
                            dateTime.getDay() + " " +
                            dateTime.getMonth()
            }
        } else {
            if (item.appointment_type == "Training_appointment") {
                holder.binding.txtTherapistName.text =
                    item.host!!.first_name + " " + item.host.last_name
                holder.binding.txtAppointmentTherapistType.text = item.title + " (Training Session)"
                val dateTime = DateUtils(item.date.replace("-", " "))
                holder.binding.txtAppointmentDateTime.text =
                    dateTime.getCurrentDay() + ", " +
                            dateTime.getDay() + " " +
                            dateTime.getMonth() + " at " +
                            item.start_time.dropLast(3) + " - " +
                            item.end_time.dropLast(3)
            } else {
                holder.binding.txtTherapistName.text =
                    item.doctor_first_name + " " + item.doctor_last_name
                holder.binding.txtAppointmentTherapistType.text = item.doctor_designation
                val dateTime = DateUtils(item.appointment!!.date + " " + "00:00:00")
                holder.binding.txtAppointmentDateTime.text =
                    dateTime.getCurrentDay() + ", " +
                            dateTime.getDay() + " " +
                            dateTime.getMonth() + " at " +
                            item.appointment.time_slot.starting_time.dropLast(3) + " - " +
                            item.appointment.time_slot.ending_time.dropLast(3)
            }
        }

        if (!item.is_group_appointment) {
            if (item.appointment_type == "Training_appointment") {
                holder.binding.appointmentCall.setImageResource(R.drawable.video)
                holder.binding.appointmentCall.imageTintList =
                    ColorStateList.valueOf(
                        ContextCompat.getColor(
                            context,
                            R.color.primaryGreen
                        )
                    )
            } else {
                when (item.appointment!!.type_of_visit) {
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
            if (item.appointment_type == "Training_appointment") {
                Glide.with(context).load(BaseActivity.baseURL.dropLast(5) + item.host!!.photo)
                    .placeholder(R.drawable.doctor_img)
                    .transform(CenterCrop(), RoundedCorners(5))
                    .into(holder.binding.appointListImgUser)
            } else {
                Glide.with(context).load(BaseActivity.baseURL.dropLast(5) + item.doctor_photo)
                    .placeholder(R.drawable.doctor_img)
                    .transform(CenterCrop(), RoundedCorners(5))
                    .into(holder.binding.appointListImgUser)
            }
        }

        if (!item.is_group_appointment) {
            if (item.appointment_type == "Training_appointment") {
                holder.binding.txtCancelledAppt.visibility = View.GONE
                holder.binding.txtMissedByProviderOrYou.visibility = View.INVISIBLE
            } else {
                if (item.appointment!!.status != null) {
                    when (item.appointment.status) {
                        5 -> {
                            holder.binding.txtMissedByProviderOrYou.visibility = View.GONE
                            holder.binding.txtCancelledAppt.visibility = View.VISIBLE
                        }

                        6 -> {
                            holder.binding.txtCancelledAppt.visibility = View.GONE
                            holder.binding.txtMissedByProviderOrYou.visibility = View.VISIBLE
                            if (item.appointment.is_client_missed != null || item.appointment.is_provider_missed != null) {
                                if (item.appointment.is_client_missed) {
                                    holder.binding.txtMissedByProviderOrYou.text = "Missed by you"
                                } else if (item.appointment.is_provider_missed) {
                                    holder.binding.txtMissedByProviderOrYou.text =
                                        "Missed by provider"
                                } else {
                                    holder.binding.txtMissedByProviderOrYou.text =
                                        "Missed by provider/you"
                                }
                            } else {
                                holder.binding.txtMissedByProviderOrYou.text =
                                    "Missed by provider/you"
                            }
                        }

                        7 -> {
                            holder.binding.txtCancelledAppt.visibility = View.GONE
                            holder.binding.txtMissedByProviderOrYou.visibility = View.VISIBLE
                            if (item.appointment.reschedule_by == "Client") {
                                holder.binding.txtMissedByProviderOrYou.text =
                                    "Rescheduled by you"
                            } else {
                                holder.binding.txtMissedByProviderOrYou.text =
                                    "Rescheduled by " + item.appointment.reschedule_by
                            }
                        }

                        else -> {
                            holder.binding.txtCancelledAppt.visibility = View.GONE
                            holder.binding.txtMissedByProviderOrYou.visibility = View.INVISIBLE
                        }
                    }
                } else {
                    holder.binding.txtCancelledAppt.visibility = View.GONE
                    holder.binding.txtMissedByProviderOrYou.visibility = View.INVISIBLE
                }
            }
        }

        if (appointmentType == "Upcoming") {
            holder.binding.txtHoldToMoreOptions.visibility = View.VISIBLE
        }

        holder.binding.cardViewAppointmentItem.setOnClickListener {
            if (appointmentType == "Today")
                adapterItemClickListener!!.onAppointmentItemClickListener(item, true)
        }

        holder.binding.cardViewAppointmentItem.setOnLongClickListener {
            if (appointmentType == "Upcoming") {
                adapterItemClickListener!!.onAppointmentItemClickListener(item, false)
            }
            false
        }
    }

    inner class ViewHolder(val binding: LayoutItemAppointmentsBinding) :
        RecyclerView.ViewHolder(binding.root)
}