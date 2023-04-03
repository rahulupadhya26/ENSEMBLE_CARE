package com.app.selfcare.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.controller.OnJournalItemClickListener
import com.app.selfcare.data.Journal
import com.app.selfcare.databinding.LayoutItemDashboardJournalBinding
import kotlin.math.min

class DashboardJournalAdapter(
    val context: Context,
    val list: List<Journal>, private val adapterItemClickListener: OnJournalItemClickListener?
) :
    RecyclerView.Adapter<DashboardJournalAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DashboardJournalAdapter.ViewHolder {
        val binding = LayoutItemDashboardJournalBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        /*val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_dashboard_journal, parent, false)*/
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        val limit = 4
        return min(list.size, limit)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            val item = list[position]
            journalDuration.text = item.journal_date
            txtCreatedJournalDate.text = item.created_on
            txtJournalTitle.text = item.name
            txtJournalDesc.text = item.description
            txtJournalDate.text = item.journal_date
            imgDeleteJournal.setOnClickListener {
                adapterItemClickListener!!.onJournalItemClicked(item, true)
            }
            cardViewJournal.setOnClickListener {
                adapterItemClickListener!!.onJournalItemClicked(item, false)
            }
        }
    }

    inner class ViewHolder(val binding: LayoutItemDashboardJournalBinding) :
        RecyclerView.ViewHolder(binding.root)
}