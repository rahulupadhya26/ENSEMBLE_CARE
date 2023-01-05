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
import com.app.selfcare.controller.OnVideoItemClickListener
import com.app.selfcare.data.Video
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import kotlinx.android.synthetic.main.layout_item_dashboard_video.view.*
import kotlin.math.min

class DashboardVideosAdapter (
    val context: Context,
    val list: List<Video>, private val adapterItemClickListener: OnVideoItemClickListener?
) :
    RecyclerView.Adapter<DashboardVideosAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DashboardVideosAdapter.ViewHolder {
        val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_dashboard_video, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        val limit = 4
        return min(list.size, limit)
    }

    override fun onBindViewHolder(holder: DashboardVideosAdapter.ViewHolder, position: Int) {
        val item = list[position]
        var videoImg = item.video_url
        if (item.video_url.isNotEmpty() && item.video_url.contains("youtube")) {
            val videoId: String = item.video_url.split("v=")[1]
            videoImg = "http://img.youtube.com/vi/$videoId/hqdefault.jpg" //high quality thumbnail
        }
        Glide.with(context).load(videoImg)
            .transform(CenterCrop(), RoundedCorners(5))
            .into(holder.videoImage)
        holder.videoTitle.text = item.name
        holder.videoLayout.setOnClickListener {
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val videoImage: ImageView = itemView.img_video_banner
        val videoTitle: TextView = itemView.txt_video_title
        val videoLayout: CardView = itemView.cardview_video
    }

}