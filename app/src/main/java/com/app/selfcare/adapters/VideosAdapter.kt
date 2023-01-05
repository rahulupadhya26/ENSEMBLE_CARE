package com.app.selfcare.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.R
import com.app.selfcare.controller.OnVideoItemClickListener
import com.app.selfcare.data.Video
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import kotlinx.android.synthetic.main.fragment_resources.*
import kotlinx.android.synthetic.main.layout_item_news_list.view.*
import kotlinx.android.synthetic.main.layout_item_video.view.*

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
        val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_video, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: VideosAdapter.ViewHolder, position: Int) {
        val item = list[position]
        holder.videoTitle.text = item.name
        holder.videoDesc.text = item.description

        var videoImg = item.video_url
        if (item.video_url.isNotEmpty() && item.video_url.contains("youtube")) {
            val videoId: String = item.video_url.split("v=")[1]
            videoImg = "http://img.youtube.com/vi/$videoId/hqdefault.jpg" //high quality thumbnail
        }
        Glide.with(context)
            .load(videoImg).transform(CenterCrop(), RoundedCorners(20))
            .into(holder.videoImage)

        if (item.is_favourite) {
            holder.favVideoImg.setImageResource(R.drawable.favorite)
            holder.favVideoTitle.text = "Added to favorites"
        } else {
            holder.favVideoImg.setImageResource(R.drawable.favorite_outline)
            holder.favVideoTitle.text = "Add to favorites"
        }

        holder.layoutVideoFav.setOnClickListener {
            onItemClicked.onVideoItemClickListener(item, true, isWellness)
        }

        holder.videoLayout.setOnClickListener {
            onItemClicked.onVideoItemClickListener(item, false, isWellness)
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val videoImage: ImageView = itemView.img_video
        val videoTitle: TextView = itemView.videoTitle
        val videoDesc: TextView = itemView.videoDesc
        val layoutVideoFav: LinearLayout = itemView.layoutVideoFav
        val favVideoImg: ImageView = itemView.favVideoImg
        val favVideoTitle: TextView = itemView.favVideoTitle
        val videoLayout: CardView = itemView.layout_Video
    }


}