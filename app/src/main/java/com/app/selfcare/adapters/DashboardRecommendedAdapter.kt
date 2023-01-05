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
import com.app.selfcare.controller.OnRecommendedItemClickListener
import com.app.selfcare.data.RecommendedData
import com.app.selfcare.utils.Utils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import kotlinx.android.synthetic.main.layout_item_dashboard_news.view.*
import kotlinx.android.synthetic.main.layout_item_dashboard_podcast.view.*
import kotlinx.android.synthetic.main.layout_item_dashboard_video.view.*
import kotlin.math.min

class DashboardRecommendedAdapter(
    val context: Context,
    val list: ArrayList<RecommendedData>,
    private val adapterItemClickListener: OnRecommendedItemClickListener?
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val videos = 0
    private val podcast: Int = 1
    private val articles: Int = 2
    private val providerGoals: Int = 3

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {

        val view: View
        when (viewType) {
            videos -> {
                view = LayoutInflater.from(context)
                    .inflate(R.layout.layout_item_dashboard_video, parent, false)
                return VideosViewHolder(view)
            }
            podcast -> {
                view = LayoutInflater.from(context)
                    .inflate(R.layout.layout_item_dashboard_podcast, parent, false)
                return PodcastViewHolder(view)
            }
            articles -> {
                view = LayoutInflater.from(context)
                    .inflate(R.layout.layout_item_dashboard_news, parent, false)
                return ArticlesViewHolder(view)
            }
            providerGoals -> {
                /*view = LayoutInflater.from(context)
                     .inflate(R.layout.layout_item_dashboard_video, parent, false)
                 return VideosViewHolder(view)*/
            }
        }
        view = LayoutInflater.from(context)
            .inflate(R.layout.layout_item_dashboard_video, parent, false)
        return VideosViewHolder(view)
    }

    override fun getItemCount(): Int {
        val limit = 4
        return min(list.size, limit)
    }

    override fun getItemViewType(position: Int): Int {
        when(list[position].type){
            Utils.RECOMMENDED_VIDEOS -> return videos
            Utils.RECOMMENDED_PODCAST -> return podcast
            Utils.RECOMMENDED_ARTICLES -> return articles
            Utils.RECOMMENDED_PROVIDER_GOAL -> return providerGoals
        }
        return super.getItemViewType(position)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is VideosViewHolder -> bindVideoView(holder, list[position])
            is PodcastViewHolder -> bindPodcastView(holder, list[position])
            is ArticlesViewHolder -> bindArticlesView(holder, list[position])
        }
    }

    private fun bindVideoView(holder: VideosViewHolder, item: RecommendedData) {
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
            adapterItemClickListener!!.onRecommendedItemClickListener(item)
        }
    }

    private fun bindPodcastView(holder: PodcastViewHolder, item: RecommendedData) {
        Glide.with(context).load(BaseActivity.baseURL.dropLast(5) + item.podcast_image)
            .transform(CenterCrop(), RoundedCorners(5))
            .into(holder.podcastImage)
    }

    private fun bindArticlesView(holder: ArticlesViewHolder, item: RecommendedData) {
        holder.newsTitle.text = item.name
        holder.newsDate.text = item.published_date
        holder.layoutNewsPane.setOnClickListener {
            adapterItemClickListener!!.onRecommendedItemClickListener(item)
        }
    }

    inner class VideosViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val videoImage: ImageView = itemView.img_video_banner
        val videoTitle: TextView = itemView.txt_video_title
        val videoLayout: CardView = itemView.cardview_video
    }

    inner class PodcastViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val podcastImage: ImageView = itemView.img_podcast
    }

    inner class ArticlesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val newsTitle: TextView = itemView.txtNewsTitle
        val newsDate: TextView = itemView.txtNewsDate
        val layoutNewsPane: CardView = itemView.layoutNewsPane
    }
}