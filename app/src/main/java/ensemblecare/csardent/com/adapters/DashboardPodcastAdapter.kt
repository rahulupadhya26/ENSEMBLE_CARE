package ensemblecare.csardent.com.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ensemblecare.csardent.com.BaseActivity
import ensemblecare.csardent.com.R
import ensemblecare.csardent.com.controller.OnPodcastItemClickListener
import ensemblecare.csardent.com.data.Podcast
import ensemblecare.csardent.com.databinding.LayoutItemDashboardPodcastBinding
import ensemblecare.csardent.com.utils.Utils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import kotlin.math.min

class DashboardPodcastAdapter(
    val context: Context,
    var list: List<Podcast>,
    private val adapterItemClickListener: OnPodcastItemClickListener?,
    private val wellness: String
) :
    RecyclerView.Adapter<DashboardPodcastAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DashboardPodcastAdapter.ViewHolder {
        val binding = LayoutItemDashboardPodcastBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        /*val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_dashboard_podcast, parent, false)*/
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        val limit = 4
        return min(list.size, limit)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            val item = list[position]
            Glide.with(context).load(BaseActivity.baseURL.dropLast(5) + item.podcast_image)
                .transform(CenterCrop(), RoundedCorners(5))
                .into(imgPodcast)
            podcastTitle.text = item.name
            if (wellness.isNotEmpty()) {
                if (wellness == Utils.WELLNESS_NUTRITION || wellness == Utils.RESOURCE) {
                    podcastTitle.setTextColor(context.resources.getColor(R.color.black))
                } else {
                    podcastTitle.setTextColor(context.resources.getColor(R.color.white))
                }
            } else {
                podcastTitle.setTextColor(context.resources.getColor(R.color.black))
            }
            //podcastArtist.text = item.artist
            cardviewPodcast.setOnClickListener {
                adapterItemClickListener!!.onPodcastItemClicked(item)
            }
        }
    }

    inner class ViewHolder(val binding: LayoutItemDashboardPodcastBinding) :
        RecyclerView.ViewHolder(binding.root)

    fun filterList(filteredNames: ArrayList<Podcast>) {
        this.list = filteredNames
        notifyDataSetChanged()
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }
}