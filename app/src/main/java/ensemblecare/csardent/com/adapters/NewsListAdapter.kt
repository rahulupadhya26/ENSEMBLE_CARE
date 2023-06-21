package ensemblecare.csardent.com.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ensemblecare.csardent.com.BaseActivity
import ensemblecare.csardent.com.controller.OnNewsItemClickListener
import ensemblecare.csardent.com.data.Articles
import ensemblecare.csardent.com.databinding.LayoutItemNewsListBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

class NewsListAdapter(
    val context: Context,
    val list: List<Articles>, private val adapterItemClickListener: OnNewsItemClickListener?
) :
    RecyclerView.Adapter<NewsListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NewsListAdapter.ViewHolder {
        val binding =
            LayoutItemNewsListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        /*val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_news_list, parent, false)*/
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            val item = list[position]
            articleListTitle.text = item.name
            Glide.with(context)
                .load(BaseActivity.baseURL.dropLast(5) + item.banner_image)
                .transform(CenterCrop(), RoundedCorners(20))
                .into(imgArticle)
            layoutNewsPane.setOnClickListener {
                adapterItemClickListener!!.onNewsItemClicked(item)
            }
        }
    }

    inner class ViewHolder(val binding: LayoutItemNewsListBinding) :
        RecyclerView.ViewHolder(binding.root)
}