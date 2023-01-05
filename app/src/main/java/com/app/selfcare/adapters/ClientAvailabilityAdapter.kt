package com.app.selfcare.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.R
import com.app.selfcare.controller.OnClickListener
import com.app.selfcare.data.AvailabilityData
import kotlinx.android.synthetic.main.client_availablity_item.view.*

class ClientAvailabilityAdapter(
    val context: Context,
    val list: ArrayList<AvailabilityData>,
    private val onClickListener: OnClickListener?
) :
    RecyclerView.Adapter<ClientAvailabilityAdapter.ViewHolder>() {
    var row_index: Int = -1
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ClientAvailabilityAdapter.ViewHolder {
        val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.client_availablity_item, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: ClientAvailabilityAdapter.ViewHolder, position: Int) {
        val item = list[position]
        holder.availabilityTxt.text = item.name
        holder.availabilityLayout.setOnClickListener {
            item.isSelected = !item.isSelected
            onClickListener!!.onClickListener(item.name, item.isSelected)
            if (item.isSelected) {
                holder.cardViewAvailability.setCardBackgroundColor(ContextCompat.getColor(context, R.color.primaryGreen))
                holder.availabilityTxt.setTextColor(ContextCompat.getColor(context, R.color.white))
            } else {
                holder.cardViewAvailability.setCardBackgroundColor(ContextCompat.getColor(context, R.color.white))
                holder.availabilityTxt.setTextColor(ContextCompat.getColor(context, R.color.primaryGreen))
            }
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val availabilityTxt: TextView = itemView.txtAvailability
        val availabilityLayout: LinearLayout = itemView.layoutAvailability
        val cardViewAvailability : CardView = itemView.cardViewAvailability
    }
}