package ensemblecare.csardent.com.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ensemblecare.csardent.com.controller.OnVideoItemClickListener
import ensemblecare.csardent.com.data.Video
import ensemblecare.csardent.com.databinding.LayoutItemDashboardVideoBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import kotlin.math.min

class DashboardVideosAdapter(
    val context: Context,
    val list: List<Video>, private val adapterItemClickListener: OnVideoItemClickListener?
) :
    RecyclerView.Adapter<DashboardVideosAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DashboardVideosAdapter.ViewHolder {
        val binding = LayoutItemDashboardVideoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        /*val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_dashboard_video, parent, false)*/
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        val limit = 4
        return min(list.size, limit)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            val item = list[position]
            var videoImg = item.video_url
            if (item.video_url.isNotEmpty() && item.video_url.contains("youtube")) {
                val videoId: String = item.video_url.split("v=")[1]
                videoImg =
                    "http://img.youtube.com/vi/$videoId/hqdefault.jpg" //high quality thumbnail
            }
            Glide.with(context).load(videoImg)
                .transform(CenterCrop(), RoundedCorners(5))
                .into(imgVideoBanner)
            txtVideoTitle.text = item.name
            cardviewVideo.setOnClickListener {
            }
        }
    }

    inner class ViewHolder(val binding: LayoutItemDashboardVideoBinding) :
        RecyclerView.ViewHolder(binding.root)

}