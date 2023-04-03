package com.app.selfcare.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.controller.OnDocumentItemClickListener
import com.app.selfcare.controller.OnDocumentsConsentRoisViewItemClickListener
import com.app.selfcare.data.Documents
import com.app.selfcare.databinding.LayoutItemDocumentListBinding
import com.app.selfcare.utils.DateMethods
import com.app.selfcare.utils.DateUtils

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
        val binding = LayoutItemDocumentListBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        /*val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_document_list, parent, false)*/
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            val item = list[position]
            val getDate = DateUtils(item.date + " 00:00:00")
            val isToday = DateMethods().isToday(getDate.mDate)
            if (isToday) {
                txtDocumentDay.text = "Today"
            } else {
                txtDocumentDay.text = getDate.getDay() + " " + getDate.getMonth()
            }
            if (item.Appointment != null) {
                if (item.Appointment[0].consents.isNotEmpty()) {
                    layoutDocumentAppointmentConsent.visibility = View.VISIBLE
                }
                if (item.Appointment[0].insurance.isNotEmpty()) {
                    layoutDocumentInsurance.visibility = View.VISIBLE
                }
                if (item.Appointment[0].prescriptions.isNotEmpty()) {
                    layoutDocumentAppointmentPrescription.visibility = View.GONE
                }

                layoutDocumentAppointmentPrescription.setOnClickListener {
                    adapterItem.onDocumentItemClickListener(
                        item.Appointment[0].prescriptions,
                        "Prescriptions"
                    )
                }

                layoutDocumentAppointmentConsent.setOnClickListener {
                    adapterItem.onDocumentItemClickListener(
                        item.Appointment[0].consents,
                        "Consents"
                    )
                }

                layoutDocumentInsurance.setOnClickListener {
                    adapterItem.onDocumentItemClickListener(
                        item.Appointment[0].insurance,
                        "Insurance"
                    )
                }
            }

            if (item.Consents != null) {
                layoutDocumentConsentsRoisList.visibility = View.VISIBLE
                val childLayoutManager = LinearLayoutManager(
                    recyclerViewConsentsRoisView.context,
                    LinearLayoutManager.VERTICAL,
                    false
                )
                childLayoutManager.initialPrefetchItemCount = 4
                recyclerViewConsentsRoisView.apply {
                    layoutManager = childLayoutManager
                    adapter =
                        ConsentsRoisViewAdapter(context, item.Consents, adapterConsentRoisItem)
                    setRecycledViewPool(viewPool)
                }

                layoutDocumentConsentsRois.setOnClickListener {
                    if (recyclerViewConsentsRoisView.isVisible) {
                        recyclerViewConsentsRoisView.visibility = View.GONE
                    } else {
                        recyclerViewConsentsRoisView.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    inner class ViewHolder(val binding: LayoutItemDocumentListBinding) :
        RecyclerView.ViewHolder(binding.root)
}