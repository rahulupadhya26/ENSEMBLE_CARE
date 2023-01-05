package com.app.selfcare.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.R
import com.app.selfcare.controller.OnDocumentItemClickListener
import com.app.selfcare.data.DocumentData
import com.app.selfcare.utils.DateMethods
import com.app.selfcare.utils.DateUtils
import kotlinx.android.synthetic.main.layout_item_document_list.view.*

class DocumentListAdapter(
    val context: Context,
    val list: ArrayList<DocumentData>,
    private val adapterItem: OnDocumentItemClickListener
) :
    RecyclerView.Adapter<DocumentListAdapter.ViewHolder>() {


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DocumentListAdapter.ViewHolder {
        val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_document_list, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: DocumentListAdapter.ViewHolder, position: Int) {
        val item = list[position]
        val getDate = DateUtils(item.dateTime)
        val isToday = DateMethods().isToday(getDate.mDate)
        if (isToday) {
            holder.txtDocumentDay.text = "Today"
        } else {
            holder.txtDocumentDay.text = getDate.getDay() + " " + getDate.getMonth()
        }
        if (item.prescriptions.isEmpty()) {
            holder.layoutDocumentAppointmentConsent.visibility = View.GONE
        }
        if (item.consents.isEmpty()) {
            holder.layoutDocumentConsentTreatment.visibility = View.GONE
        }
        holder.layoutDocumentConsentTreatment.setOnClickListener {
            adapterItem.onDocumentItemClickListener(item.prescriptions, "Prescriptions")
        }

        holder.layoutDocumentAppointmentConsent.setOnClickListener {
            adapterItem.onDocumentItemClickListener(item.consents, "Consents")
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtDocumentDay: TextView = itemView.txtDocumentDay
        val layoutDocumentConsentTreatment: RelativeLayout = itemView.layoutDocumentConsentTreatment
        val layoutDocumentAppointmentConsent: RelativeLayout =
            itemView.layoutDocumentAppointmentConsent
        val layoutDocumentItem: LinearLayout = itemView.layoutDocumentItem
    }
}