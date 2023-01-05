package com.app.selfcare.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.R
import com.app.selfcare.controller.OnJournalItemClickListener
import com.app.selfcare.data.Journal
import com.app.selfcare.utils.DateUtils
import kotlinx.android.synthetic.main.layout_item_journal_list.view.*

class JournalListAdapter(
    val context: Context,
    var list: List<Journal>, private val adapterItemClickListener: OnJournalItemClickListener?,
) :
    RecyclerView.Adapter<JournalListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): JournalListAdapter.ViewHolder {
        val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_journal_list, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: JournalListAdapter.ViewHolder, position: Int) {

        val item = list[position]
        if (item.journal_date == "") {
            holder.journalDate.text = ""
            holder.journalMonth.text = ""
            holder.journalTime.text = ""
        } else {
            val journalDate = DateUtils(item.journal_date + " " + item.journal_time)
            holder.journalDate.text = journalDate.getDay()
            holder.journalMonth.text = journalDate.getMonth()
            holder.journalTime.text = journalDate.getTime()
        }

        if (position % 3 == 0) {
            holder.journalLayout.setCardBackgroundColor(Color.parseColor("#5C2E7E"))
        } else if (position % 3 == 1) {
            holder.journalLayout.setCardBackgroundColor(Color.parseColor("#3CAACC"))
        } else if (position % 3 == 2) {
            holder.journalLayout.setCardBackgroundColor(Color.parseColor("#301934"))
        }

        holder.journalTitle.text = item.name
        holder.journalDesc.text = item.description
        holder.journalLayout.setOnClickListener {
            adapterItemClickListener!!.onJournalItemClicked(item, false)
        }
        holder.journalDelete.setOnClickListener {
            adapterItemClickListener!!.onJournalItemClicked(item, true)
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val journalDate: TextView = itemView.journal_created_date
        val journalMonth: TextView = itemView.journal_created_month
        val journalTime: TextView = itemView.journal_created_time
        val journalTitle: TextView = itemView.journal_title
        val journalDesc: TextView = itemView.journal_desc
        val journalDelete: ImageView = itemView.imgDeletejournal
        val journalLayout: CardView = itemView.cardview_journal
    }

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