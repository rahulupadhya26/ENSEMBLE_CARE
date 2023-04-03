package com.app.selfcare.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.R
import com.app.selfcare.controller.OnVideoItemClickListener
import com.app.selfcare.data.Video
import com.app.selfcare.databinding.LayoutItemVideoBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

class VideosAdapter(
    private val context: Context,
    private val list: ArrayList<Video>,
    private val isWellness: Boolean,
    private val onItemClicked: OnVideoItemClickListener,
) : RecyclerView.Adapter<VideosAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): VideosAdapter.ViewHolder {
        val binding =
            LayoutItemVideoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        /*val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_video, parent, false)*/
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            val item = list[position]
            videoTitle.text = item.name
            videoDesc.text = item.description

            var videoImg = item.video_url
            if (item.video_url.isNotEmpty() && item.video_url.contains("youtube")) {
                val videoId: String = item.video_url.split("v=")[1]
                videoImg =
                    "http://img.youtube.com/vi/$videoId/hqdefault.jpg" //high quality thumbnail
            }
            Glide.with(context)
                .load(videoImg).transform(CenterCrop(), RoundedCorners(20))
                .into(imgVideo)

            if (item.is_favourite) {
                favVideoImg.setImageResource(R.drawable.favorite)
                favVideoTitle.text = "Added to favorites"
            } else {
                favVideoImg.setImageResource(R.drawable.favorite_outline)
                favVideoTitle.text = "Add to favorites"
            }

            layoutVideoFav.setOnClickListener {
                onItemClicked.onVideoItemClickListener(item, true, isWellness)
            }

            layoutVideo.setOnClickListener {
                onItemClicked.onVideoItemClickListener(item, false, isWellness)
            }
        }
    }

    inner class ViewHolder(val binding: LayoutItemVideoBinding) :
        RecyclerView.ViewHolder(binding.root)

}