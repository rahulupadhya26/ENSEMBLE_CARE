package com.app.selfcare.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.R
import com.app.selfcare.controller.OnNewsItemClickListener
import com.app.selfcare.data.News
import kotlinx.android.synthetic.main.layout_item_dashboard_news.view.*
import kotlin.math.min

class DashboardNewsAdapter (
    val context: Context,
    val list: List<News>, private val adapterItemClickListener: OnNewsItemClickListener?
) :
    RecyclerView.Adapter<DashboardNewsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DashboardNewsAdapter.ViewHolder {
        val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_dashboard_news, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        val limit = 4
        return min(list.size, limit)
    }

    override fun onBindViewHolder(holder: DashboardNewsAdapter.ViewHolder, position: Int) {

        val item = list[position]
        holder.newsTitle.text = item.newsTitle
        holder.newsDate.text = item.newsPublishOn
        holder.layoutNewsPane.setOnClickListener {
            adapterItemClickListener!!.onNewsItemClicked(item)
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val newsTitle: TextView = itemView.txtNewsTitle
        val newsDate: TextView = itemView.txtNewsDate
        val layoutNewsPane: CardView = itemView.layoutNewsPane
    }
}