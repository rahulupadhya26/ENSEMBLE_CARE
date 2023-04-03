package com.app.selfcare.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.BaseActivity
import com.app.selfcare.controller.OnPodcastItemClickListener
import com.app.selfcare.data.Podcast
import com.app.selfcare.databinding.LayoutItemPodcastListBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

class PodcastListAdapter(
    val context: Context,
    val list: List<Podcast>, private val adapterItemClickListener: OnPodcastItemClickListener?
) :
    RecyclerView.Adapter<PodcastListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PodcastListAdapter.ViewHolder {
        val binding =
            LayoutItemPodcastListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        /*val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_podcast_list, parent, false)*/
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            val item = list[position]
            podcastListTitle.text = item.name
            Glide.with(context)
                .load(BaseActivity.baseURL.dropLast(5) + item.podcast_image)
                .transform(CenterCrop(), RoundedCorners(5))
                .into(imgPodcast)
            cardviewPodcast.setOnClickListener {
                adapterItemClickListener!!.onPodcastItemClicked(item)
            }
        }
    }

    inner class ViewHolder(val binding: LayoutItemPodcastListBinding) :
        RecyclerView.ViewHolder(binding.root)
}