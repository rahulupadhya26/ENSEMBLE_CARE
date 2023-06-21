package ensemblecare.csardent.com.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ensemblecare.csardent.com.controller.OnNewsItemClickListener
import ensemblecare.csardent.com.data.Articles
import ensemblecare.csardent.com.databinding.LayoutItemDashboardNewsBinding
import kotlin.math.min

class DashboardArticlesAdapter(
    val context: Context,
    val list: List<Articles>, private val adapterItemClickListener: OnNewsItemClickListener?
) :
    RecyclerView.Adapter<DashboardArticlesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DashboardArticlesAdapter.ViewHolder {
        val binding = LayoutItemDashboardNewsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        /*val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_dashboard_news, parent, false)*/
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        val limit = 4
        return min(list.size, limit)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            val item = list[position]
            txtNewsTitle.text = item.name
            txtNewsDate.text = item.published_date
            layoutNewsPane.setOnClickListener {
                adapterItemClickListener!!.onNewsItemClicked(item)
            }
        }
    }

    inner class ViewHolder(val binding: LayoutItemDashboardNewsBinding) :
        RecyclerView.ViewHolder(binding.root)
}