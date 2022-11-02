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
import kotlinx.android.synthetic.main.layout_item_podcast_list.view.*

class PodcastListAdapter(
    val context: Context,
    val list: List<Podcast>, private val adapterItemClickListener: OnPodcastItemClickListener?
) :
    RecyclerView.Adapter<PodcastListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PodcastListAdapter.ViewHolder {
        val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_podcast_list, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: PodcastListAdapter.ViewHolder, position: Int) {
        val item = list[position]
        Glide.with(context)
            .load(item.podcast_image)
            .placeholder(R.drawable.sample_img)
            .transform(CenterCrop(), RoundedCorners(5))
            .into(holder.podcastImage)
        holder.podcastTitle.text = item.name
        holder.podcastArtist.text = item.artist
        holder.podcastLayout.setOnClickListener {
            adapterItemClickListener!!.onPodcastItemClicked(item)
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val podcastImage: ImageView = itemView.imgPodcast
        val podcastTitle: TextView = itemView.txtPodcastTitle
        val podcastArtist: TextView = itemView.txtPodcastArtist
        val podcastLayout: CardView = itemView.cardview_podcast
    }
}