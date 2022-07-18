package com.app.selfcare.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.R
import com.app.selfcare.controller.OnPodcastItemClickListener
import com.app.selfcare.data.Podcast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import kotlinx.android.synthetic.main.layout_item_dashboard_news.view.*
import kotlinx.android.synthetic.main.layout_item_dashboard_podcast.view.*
import kotlin.math.min

class DashboardPodcastAdapter (
    val context: Context,
    val list: List<Podcast>, private val adapterItemClickListener: OnPodcastItemClickListener?
) :
    RecyclerView.Adapter<DashboardPodcastAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DashboardPodcastAdapter.ViewHolder {
        val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_dashboard_podcast, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        val limit = 4
        return min(list.size, limit)
    }

    override fun onBindViewHolder(holder: DashboardPodcastAdapter.ViewHolder, position: Int) {
        val item = list[position]
        Glide.with(context).load(item.image)
            .placeholder(R.drawable.img_placeholder)
            .transform(CenterCrop(), RoundedCorners(5))
            .into(holder.podcastImage)
        holder.podcastTitle.text = item.title
        holder.podcastArtist.text = item.artist
        holder.podcastLayout.setOnClickListener {
            adapterItemClickListener!!.onPodcastItemClicked(item)
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val podcastImage: ImageView = itemView.img_podcast
        val podcastTitle: TextView = itemView.txt_podcast_title
        val podcastArtist: TextView = itemView.txt_podcast_artist
        val podcastLayout: CardView = itemView.cardview_podcast
    }
}