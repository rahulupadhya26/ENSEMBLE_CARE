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
import kotlinx.android.synthetic.main.coach_type_list_item.view.*

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
        val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.coach_type_list_item, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: CoachTypesAdapter.ViewHolder, position: Int) {
        val item = list[position]
        holder.coachTypeTxt.text = item.mainText
        holder.coachTypeImage.setImageResource(item.image)
        holder.coachTypeSecondary.text = item.secondaryText
        holder.coachTypeSub.text = item.subText
        holder.coachType.setOnClickListener {
            onTypeClickListener!!.onCoachTypeClickListener(item)
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val coachTypeTxt: TextView = itemView.txtCoachType
        val coachTypeImage: ImageView = itemView.imageViewCoachType
        val coachTypeSecondary: TextView = itemView.txtCoachSecondary
        val coachTypeSub: TextView = itemView.txtCoachSub
        val coachType: CardView = itemView.cardViewTherapyType
    }
}