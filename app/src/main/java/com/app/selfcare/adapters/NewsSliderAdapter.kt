package com.app.selfcare.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.app.selfcare.R
import com.app.selfcare.controller.OnNewsItemClickListener
import com.app.selfcare.data.News
import kotlinx.android.synthetic.main.layout_item_news_slider.view.*
import kotlinx.android.synthetic.main.slide_item_container.view.newsSlide

class NewsSliderAdapter(
    val context: Context,
    var list: ArrayList<News>,
    val viewPager: ViewPager2,
    private val adapterItemClickListener: OnNewsItemClickListener?
) :
    RecyclerView.Adapter<NewsSliderAdapter.SliderViewHolder>() {

    class SliderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val newsTitle: TextView = itemView.txtNewsTitle
        val newsLink: TextView = itemView.txtNewsLink
        val newsPubDate: TextView = itemView.txtPubDate
        val newsPane: CardView = itemView.newsSlide
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SliderViewHolder {
        val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_news_slider, parent, false)
        return SliderViewHolder(v)
    }

    override fun onBindViewHolder(holder: SliderViewHolder, position: Int) {
        val item = list[position]
        holder.newsTitle.text = item.newsTitle
        holder.newsLink.text = item.newsLink
        holder.newsPubDate.text = item.newsPublishOn
        if (position == list.size - 2) {
            viewPager.post(runnable)
        }
        holder.newsPane.setOnClickListener {
            adapterItemClickListener!!.onNewsItemClicked(item)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("NotifyDataSetChanged")
    val runnable: Runnable = Runnable {
        list.addAll(list)
        notifyDataSetChanged()
    }
}