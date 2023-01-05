package com.app.selfcare.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.R
import com.app.selfcare.controller.OnConsentRoisItemClickListener
import com.app.selfcare.data.ConsentRois
import kotlinx.android.synthetic.main.layout_item_consents_rois.view.*

class ConsentRoisListAdapter(
    val list: ArrayList<ConsentRois>,
    private val adapterItemClickListener: OnConsentRoisItemClickListener?
) :
    RecyclerView.Adapter<ConsentRoisListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ConsentRoisListAdapter.ViewHolder {
        val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_consents_rois, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ConsentRoisListAdapter.ViewHolder, position: Int) {
        val item = list[position]
        holder.consentRoisText.text = item.text
        if (item.isCompleted) {
            holder.consentRoisStatus.setBackgroundResource(R.color.darkGreen)
            holder.consentRoisStatus.text = "Completed"
        } else {
            holder.consentRoisStatus.setBackgroundResource(R.color.red)
            holder.consentRoisStatus.text = "Incomplete"
        }
        holder.consentRoisLayout.setOnClickListener {
            if (!item.isCompleted) {
                adapterItemClickListener!!.onConsentRoisItemClickListener(list)
            }
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val consentRoisText: TextView = itemView.txtConsentRois
        val consentRoisStatus: TextView = itemView.txtConsentRoisStatus
        val consentRoisStatusCardView: CardView = itemView.cardViewStatus
        val consentRoisLayout: LinearLayout = itemView.layoutConsentRois
    }
}