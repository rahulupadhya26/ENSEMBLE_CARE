package com.app.selfcare.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.controller.OnJournalItemClickListener
import com.app.selfcare.data.Journal
import com.app.selfcare.databinding.LayoutItemJournalListBinding
import com.app.selfcare.utils.DateUtils

class JournalListAdapter(
    val context: Context,
    var list: List<Journal>, private val adapterItemClickListener: OnJournalItemClickListener?,
) :
    RecyclerView.Adapter<JournalListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): JournalListAdapter.ViewHolder {
        val binding =
            LayoutItemJournalListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        /*val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_journal_list, parent, false)*/
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            val item = list[position]
            if (item.journal_date == "") {
                journalCreatedDate.text = ""
                journalCreatedMonth.text = ""
                journalCreatedTime.text = ""
            } else {
                val journalDate = DateUtils(item.journal_date + " " + item.journal_time)
                journalCreatedDate.text = journalDate.getDay()
                journalCreatedMonth.text = journalDate.getMonth()
                journalCreatedTime.text = journalDate.getTime()
            }

            if (position % 3 == 0) {
                cardviewJournal.setCardBackgroundColor(Color.parseColor("#5C2E7E"))
            } else if (position % 3 == 1) {
                cardviewJournal.setCardBackgroundColor(Color.parseColor("#3CAACC"))
            } else if (position % 3 == 2) {
                cardviewJournal.setCardBackgroundColor(Color.parseColor("#301934"))
            }

            journalTitle.text = item.name
            journalDesc.text = item.description
            cardviewJournal.setOnClickListener {
                adapterItemClickListener!!.onJournalItemClicked(item, false)
            }
            imgDeletejournal.setOnClickListener {
                adapterItemClickListener!!.onJournalItemClicked(item, true)
            }
        }
    }

    inner class ViewHolder(val binding: LayoutItemJournalListBinding) :
        RecyclerView.ViewHolder(binding.root)

    @SuppressLint("NotifyDataSetChanged")
    fun filterList(filteredNames: ArrayList<Journal>) {
        this.list = filteredNames
        notifyDataSetChanged()
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }
}