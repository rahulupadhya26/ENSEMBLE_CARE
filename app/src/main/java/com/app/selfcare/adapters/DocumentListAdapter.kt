package com.app.selfcare.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.TranslateAnimation
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.R
import com.app.selfcare.controller.OnDocumentItemClickListener
import com.app.selfcare.controller.OnDocumentsConsentRoisViewItemClickListener
import com.app.selfcare.data.Documents
import com.app.selfcare.data.ToDoData
import com.app.selfcare.utils.DateMethods
import com.app.selfcare.utils.DateUtils
import kotlinx.android.synthetic.main.layout_item_document_list.view.*

class DocumentListAdapter(
    val context: Context,
    val list: ArrayList<Documents>,
    private val adapterItem: OnDocumentItemClickListener,
    private val adapterConsentRoisItem: OnDocumentsConsentRoisViewItemClickListener,
) :
    RecyclerView.Adapter<DocumentListAdapter.ViewHolder>() {

    private val viewPool = RecyclerView.RecycledViewPool()

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
        val getDate = DateUtils(item.date + " 00:00:00")
        val isToday = DateMethods().isToday(getDate.mDate)
        if (isToday) {
            holder.txtDocumentDay.text = "Today"
        } else {
            holder.txtDocumentDay.text = getDate.getDay() + " " + getDate.getMonth()
        }
        if (item.Appointment != null) {
            if (item.Appointment[0].consents.isNotEmpty()) {
                holder.layoutDocumentAppointmentConsent.visibility = View.VISIBLE
            }
            if (item.Appointment[0].insurance.isNotEmpty()) {
                holder.layoutDocumentInsurance.visibility = View.VISIBLE
            }
            if (item.Appointment[0].prescriptions.isNotEmpty()) {
                holder.layoutDocumentAppointmentPrescription.visibility = View.VISIBLE
            }

            holder.layoutDocumentAppointmentPrescription.setOnClickListener {
                adapterItem.onDocumentItemClickListener(
                    item.Appointment[0].prescriptions,
                    "Prescriptions"
                )
            }

            holder.layoutDocumentAppointmentConsent.setOnClickListener {
                adapterItem.onDocumentItemClickListener(item.Appointment[0].consents, "Consents")
            }

            holder.layoutDocumentInsurance.setOnClickListener {
                adapterItem.onDocumentItemClickListener(item.Appointment[0].insurance, "Insurance")
            }
        }

        if (item.Consents != null) {
            holder.layoutDocumentConsentsRoisList.visibility = View.VISIBLE
            val childLayoutManager = LinearLayoutManager(
                holder.recyclerViewConsentsRoisView.context,
                LinearLayoutManager.VERTICAL,
                false
            )
            childLayoutManager.initialPrefetchItemCount = 4
            holder.recyclerViewConsentsRoisView.apply {
                layoutManager = childLayoutManager
                adapter =
                    ConsentsRoisViewAdapter(context, item.Consents, adapterConsentRoisItem)
                setRecycledViewPool(viewPool)
            }

            holder.layoutDocumentConsentsRois.setOnClickListener {
                if (holder.recyclerViewConsentsRoisView.isVisible) {
                    holder.recyclerViewConsentsRoisView.visibility = View.GONE
                } else {
                    holder.recyclerViewConsentsRoisView.visibility = View.VISIBLE
                }
            }
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtDocumentDay: TextView = itemView.txtDocumentDay
        val layoutDocumentAppointmentPrescription: RelativeLayout = itemView.layoutDocumentAppointmentPrescription
        val layoutDocumentAppointmentConsent: RelativeLayout =
            itemView.layoutDocumentAppointmentConsent
        val layoutDocumentItem: LinearLayout = itemView.layoutDocumentItem
        val layoutDocumentInsurance: RelativeLayout = itemView.layoutDocumentInsurance
        val recyclerViewConsentsRoisView: RecyclerView = itemView.recyclerViewConsentsRoisView
        val layoutDocumentConsentsRois: RelativeLayout = itemView.layoutDocumentConsentsRois
        val layoutDocumentConsentsRoisList: LinearLayout = itemView.layoutDocumentConsentsRoisList
    }
}