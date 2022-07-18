package com.app.selfcare.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.R
import com.app.selfcare.controller.OnVideoItemClickListener
import com.app.selfcare.data.Video
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import kotlinx.android.synthetic.main.layout_item_news_list.view.*
import kotlinx.android.synthetic.main.layout_item_video.view.*

class VideosAdapter(
    private val context: Context,
    private val list: ArrayList<Video>,
    private val onItemClicked: OnVideoItemClickListener,
) : RecyclerView.Adapter<VideosAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): VideosAdapter.ViewHolder {
        val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_video, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: VideosAdapter.ViewHolder, position: Int) {
        val item = list[position]

        holder.videoTitle.text = item.title
        if (item.desc.isNotEmpty()) {
            holder.videoDesc.text = item.desc
        } else {
            holder.videoDesc.visibility = View.GONE
        }
        var videoImg = item.link
        if (item.link.isNotEmpty() && item.link.contains("youtube")) {
            val videoId: String = item.link.split("v=")[1]
            videoImg = "http://img.youtube.com/vi/$videoId/hqdefault.jpg" //high quality thumbnail
        }
        Glide.with(context)
            .load(videoImg).transform(CenterCrop(), RoundedCorners(20))
            .into(holder.videoImage)
        holder.videoLayout.setOnClickListener {
            onItemClicked.onVideoItemClickListener(item)
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val videoImage: ImageView = itemView.img_video
        val videoTitle: TextView = itemView.txtVideoTitle
        val videoDesc: TextView = itemView.txtVideoDesc
        val videoLayout: ConstraintLayout = itemView.layout_item_video
    }


}