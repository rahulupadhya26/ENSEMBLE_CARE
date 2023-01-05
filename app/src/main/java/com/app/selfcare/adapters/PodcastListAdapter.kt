package com.app.selfcare.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.BaseActivity
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
        holder.podcastListTitle.text = item.name
        Glide.with(context)
            .load(BaseActivity.baseURL.dropLast(5) + item.podcast_image)
            .transform(CenterCrop(), RoundedCorners(5))
            .into(holder.podcastImage)
        holder.podcastLayout.setOnClickListener {
            adapterItemClickListener!!.onPodcastItemClicked(item)
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val podcastImage: ImageView = itemView.img_podcast
        val podcastListTitle: TextView = itemView.podcastListTitle
        val podcastLayout: CardView = itemView.cardviewPodcast
    }
}