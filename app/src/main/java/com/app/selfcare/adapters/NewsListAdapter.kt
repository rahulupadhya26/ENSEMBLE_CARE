package com.app.selfcare.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.BaseActivity
import com.app.selfcare.R
import com.app.selfcare.controller.OnNewsItemClickListener
import com.app.selfcare.data.Articles
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import kotlinx.android.synthetic.main.layout_item_news_list.view.*

class NewsListAdapter(
    val context: Context,
    val list: List<Articles>, private val adapterItemClickListener: OnNewsItemClickListener?
) :
    RecyclerView.Adapter<NewsListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NewsListAdapter.ViewHolder {
        val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_news_list, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: NewsListAdapter.ViewHolder, position: Int) {

        val item = list[position]
        holder.articleListTitle.text = item.name
        Glide.with(context)
            .load(BaseActivity.baseURL.dropLast(5) + item.banner_image)
            .transform(CenterCrop(), RoundedCorners(20))
            .into(holder.articleImg)
        holder.layoutNewsPane.setOnClickListener {
            adapterItemClickListener!!.onNewsItemClicked(item)
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val articleImg: ImageView = itemView.imgArticle
        val articleListTitle: TextView = itemView.articleListTitle
        val layoutNewsPane: LinearLayout = itemView.layoutNewsPane
    }
}