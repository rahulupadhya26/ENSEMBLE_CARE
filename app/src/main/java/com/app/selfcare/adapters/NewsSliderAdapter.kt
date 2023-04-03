package com.app.selfcare.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.app.selfcare.controller.OnNewsItemClickListener
import com.app.selfcare.data.Articles
import com.app.selfcare.databinding.LayoutItemNewsSliderBinding

class NewsSliderAdapter(
    val context: Context,
    var list: ArrayList<Articles>,
    val viewPager: ViewPager2,
    private val adapterItemClickListener: OnNewsItemClickListener?
) :
    RecyclerView.Adapter<NewsSliderAdapter.SliderViewHolder>() {

    class SliderViewHolder(val binding: LayoutItemNewsSliderBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SliderViewHolder {
        val binding =
            LayoutItemNewsSliderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        /*val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_news_slider, parent, false)*/
        return SliderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SliderViewHolder, position: Int) {
        holder.binding.apply {
            val item = list[position]
            txtNewsTitle.text = item.name
            txtNewsLink.text = item.article_url
            txtPubDate.text = item.published_date
            if (position == list.size - 2) {
                viewPager.post(runnable)
            }
            newsSlide.setOnClickListener {
                adapterItemClickListener!!.onNewsItemClicked(item)
            }
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