package com.app.selfcare.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.R
import com.app.selfcare.controller.OnNewsItemClickListener
import com.app.selfcare.data.Articles
import kotlinx.android.synthetic.main.layout_item_dashboard_news.view.*
import kotlin.math.min

class DashboardArticlesAdapter (
    val context: Context,
    val list: List<Articles>, private val adapterItemClickListener: OnNewsItemClickListener?
) :
    RecyclerView.Adapter<DashboardArticlesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DashboardArticlesAdapter.ViewHolder {
        val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_dashboard_news, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        val limit = 4
        return min(list.size, limit)
    }

    override fun onBindViewHolder(holder: DashboardArticlesAdapter.ViewHolder, position: Int) {

        val item = list[position]
        holder.newsTitle.text = item.name
        holder.newsDate.text = item.published_date
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