package com.app.selfcare.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.R
import com.app.selfcare.controller.OnItemTherapistImageClickListener
import com.app.selfcare.controller.OnTherapistItemClickListener
import com.app.selfcare.data.Therapist
import com.google.android.material.card.MaterialCardView
import kotlinx.android.synthetic.main.fragment_therapist_detail.*
import kotlinx.android.synthetic.main.layout_item_specialist.view.*

class SpecialistAdapter(
    val context: Context,
    val list: List<Therapist>,
    private val onItemImageClickListener: OnItemTherapistImageClickListener?,
    private val onItemClickListener: OnTherapistItemClickListener?
) :
    RecyclerView.Adapter<SpecialistAdapter.ViewHolder>() {
    var row_index: Int = -1
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SpecialistAdapter.ViewHolder {
        val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_specialist, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: SpecialistAdapter.ViewHolder, position: Int) {
        val item = list[position]
        holder.doctorName.text =
            item.first_name + " " + item.middle_name + " " + item.last_name
        holder.doctorType.text = item.doctor_type
        holder.doctorNextAvailSlot.text = "Friday, 11 Nov"

        holder.doctorLayout.setOnClickListener {
            row_index = position
            notifyDataSetChanged()
            onItemClickListener!!.onTherapistItemClickListener(item)
        }
        holder.doctorLayout.strokeColor = ContextCompat.getColor(context, R.color.primaryGreen)
        if (row_index == position) {
            holder.doctorLayout.strokeWidth = 4
        } else {
            holder.doctorLayout.strokeWidth = 0
        }

        holder.doctorLayout.setOnLongClickListener {
            onItemImageClickListener!!.onItemTherapistImageClickListener(item)
            true
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val doctorName: TextView = itemView.txtTherapistName
        val doctorType: TextView = itemView.txtTherapistType
        val doctorNextAvailSlot: TextView = itemView.txtTherapistNextSlot
        val doctorLayout: MaterialCardView = itemView.cardview_layout_therapist

    }
}