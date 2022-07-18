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
import com.app.selfcare.controller.OnTherapyTypeClickListener
import com.app.selfcare.data.TherapyType
import kotlinx.android.synthetic.main.therapy_type_list_item.view.*

class TherapyTypeListAdapter(
    val context: Context,
    val list: ArrayList<TherapyType>,
    private val onTherapyTypeClickListener: OnTherapyTypeClickListener?
) :
    RecyclerView.Adapter<TherapyTypeListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TherapyTypeListAdapter.ViewHolder {
        val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.therapy_type_list_item, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: TherapyTypeListAdapter.ViewHolder, position: Int) {
        val item = list[position]
        holder.therapyTypeTxt.text = item.text
        holder.therapyImage.setImageResource(item.image)
        holder.therapyType.setOnClickListener {
            onTherapyTypeClickListener!!.onTherapyTypeClickListener(item)
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val therapyTypeTxt: TextView = itemView.txtTherapyType
        val therapyImage: ImageView = itemView.imageViewTherapyType
        val therapyType: CardView = itemView.cardViewTherapyType
    }
}