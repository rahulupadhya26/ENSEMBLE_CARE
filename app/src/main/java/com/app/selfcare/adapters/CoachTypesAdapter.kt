package com.app.selfcare.adapters

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.R
import com.app.selfcare.controller.OnCoachTypeClickListener
import com.app.selfcare.data.CoachType
import com.app.selfcare.databinding.CoachTypeListItemBinding

class CoachTypesAdapter(
    val context: Context,
    val list: ArrayList<CoachType>,
    private val onTypeClickListener: OnCoachTypeClickListener?
) :
    RecyclerView.Adapter<CoachTypesAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CoachTypesAdapter.ViewHolder {
        val binding =
            CoachTypeListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        /*val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.coach_type_list_item, parent, false)*/
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            val item = list[position]
            txtCoachType.text = item.mainText
            imageViewCoachType.setImageResource(item.image)
            txtCoachSecondary.text = item.secondaryText
            txtCoachSub.text = item.subText
            cardViewTherapyType.setOnClickListener {
                onTypeClickListener!!.onCoachTypeClickListener(item)
            }
        }
    }

    inner class ViewHolder(val binding: CoachTypeListItemBinding) :
        RecyclerView.ViewHolder(binding.root)
}