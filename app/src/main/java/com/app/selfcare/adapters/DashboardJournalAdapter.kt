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
import com.app.selfcare.R
import com.app.selfcare.controller.OnJournalItemClickListener
import com.app.selfcare.data.Journal
import kotlinx.android.synthetic.main.layout_item_dashboard_journal.view.*
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
        val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_dashboard_journal, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        val limit = 4
        return min(list.size, limit)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: DashboardJournalAdapter.ViewHolder, position: Int) {
        val item = list[position]
        holder.journalDuration.text = item.journal_date
        holder.createdJournalDate.text = item.created_on
        holder.journalTitle.text = item.name
        holder.journalDesc.text = item.description
        holder.journalDate.text = item.journal_date
        holder.journalDelete.setOnClickListener {
            adapterItemClickListener!!.onJournalItemClicked(item, true)
        }
        holder.journalLayout.setOnClickListener {
            adapterItemClickListener!!.onJournalItemClicked(item, false)
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val journalDuration: TextView = itemView.journalDuration
        val journalTitle: TextView = itemView.txtJournalTitle
        val journalDesc: TextView = itemView.txtJournalDesc
        val journalDate: TextView = itemView.txtJournalDate
        val createdJournalDate: TextView = itemView.txtCreatedJournalDate
        val journalDelete: ImageView = itemView.imgDeleteJournal
        val journalLayout: CardView = itemView.cardViewJournal
    }
}