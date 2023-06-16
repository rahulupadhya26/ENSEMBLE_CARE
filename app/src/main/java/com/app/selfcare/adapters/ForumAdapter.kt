package com.app.selfcare.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.BaseActivity
import com.app.selfcare.R
import com.app.selfcare.controller.OnForumItemClickListener
import com.app.selfcare.data.ForumData
import com.app.selfcare.databinding.LayoutItemForumBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import java.util.ArrayList

class ForumAdapter(
    val context: Context,
    var list: List<ForumData>,
    private val adapterItemClickListener: OnForumItemClickListener?
) :
    RecyclerView.Adapter<ForumAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ForumAdapter.ViewHolder {
        val binding =
            LayoutItemForumBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        /*val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_carebuddy, parent, false)*/
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        if (item.image != null) {
            if (item.image.isNotEmpty()) {
                holder.binding.txtForumLetter.visibility = View.GONE
                holder.binding.imgForum.visibility = View.VISIBLE
                Glide.with(context).load(BaseActivity.baseURL.dropLast(5) + item.image)
                    .placeholder(R.drawable.events_img)
                    .transform(CenterCrop())
                    .into(holder.binding.imgForum)
            } else {
                holder.binding.txtForumLetter.visibility = View.VISIBLE
                holder.binding.txtForumLetter.text = item.name.substring(0, 1).uppercase()
                holder.binding.imgForum.visibility = View.GONE
            }
        } else {
            holder.binding.txtForumLetter.visibility = View.VISIBLE
            holder.binding.txtForumLetter.text = item.name.substring(0, 1).uppercase()
            holder.binding.imgForum.visibility = View.GONE
        }
        holder.binding.txtForumName.text = item.name
        holder.binding.txtForumDesc.text = item.description

        holder.binding.layoutForumItem.setOnClickListener {
            adapterItemClickListener!!.onForumItemClickListener(item)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun filterList(filteredNames: ArrayList<ForumData>) {
        this.list = filteredNames
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: LayoutItemForumBinding) :
        RecyclerView.ViewHolder(binding.root)
}