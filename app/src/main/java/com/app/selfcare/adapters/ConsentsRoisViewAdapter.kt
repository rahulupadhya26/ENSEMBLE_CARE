package com.app.selfcare.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.R
import com.app.selfcare.controller.OnDocumentsConsentRoisViewItemClickListener
import com.app.selfcare.data.ConsentsRoisDocumentData
import kotlinx.android.synthetic.main.layout_item_document_consent_rois.view.*

class ConsentsRoisViewAdapter(
    private val context: Context,
    private val list: ArrayList<ConsentsRoisDocumentData>,
    private val adapterItemClick: OnDocumentsConsentRoisViewItemClickListener
) :
    RecyclerView.Adapter<ConsentsRoisViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ConsentsRoisViewAdapter.ViewHolder {
        val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_document_consent_rois, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ConsentsRoisViewAdapter.ViewHolder, position: Int) {
        val item = list[position]
        holder.txtDocumentViewConsentRois.text = item.name
        holder.layoutDocumentConsentRoisView.setOnClickListener {
            adapterItemClick.onDocumentsConsentRoisViewItemClickListener(item)
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val layoutDocumentConsentRoisView: LinearLayout = itemView.layoutDocumentConsentRoisView
        val txtDocumentViewConsentRois: TextView = itemView.txtDocumentViewConsentRois
    }
}