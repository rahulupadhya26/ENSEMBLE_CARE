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
import com.app.selfcare.databinding.ClientAvailablityItemBinding
import com.app.selfcare.databinding.LayoutItemGoalBinding

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
        val binding =
            ClientAvailablityItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        /*val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.client_availablity_item, parent, false)*/
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            val item = list[position]
            txtAvailability.text = item.name
            layoutAvailability.setOnClickListener {
                item.isSelected = !item.isSelected
                onClickListener!!.onClickListener(item.name, item.isSelected)
                if (item.isSelected) {
                    cardViewAvailability.setCardBackgroundColor(
                        ContextCompat.getColor(
                            context,
                            R.color.primaryGreen
                        )
                    )
                    txtAvailability.setTextColor(ContextCompat.getColor(context, R.color.white))
                } else {
                    cardViewAvailability.setCardBackgroundColor(
                        ContextCompat.getColor(
                            context,
                            R.color.white
                        )
                    )
                    txtAvailability.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.primaryGreen
                        )
                    )
                }
            }
        }
    }

    inner class ViewHolder(val binding: ClientAvailablityItemBinding) :
        RecyclerView.ViewHolder(binding.root)
}