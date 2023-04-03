package com.app.selfcare.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.BaseActivity
import com.app.selfcare.R
import com.app.selfcare.controller.OnEventItemClickListener
import com.app.selfcare.data.EventCommunity
import com.app.selfcare.databinding.LayoutItemEventsBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop

class EventCommunityAdapter(
    val context: Context,
    val list: List<EventCommunity>,
    private val adapterItemClickListener: OnEventItemClickListener?
) :
    RecyclerView.Adapter<EventCommunityAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): EventCommunityAdapter.ViewHolder {
        val binding =
            LayoutItemEventsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        /*val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_events, parent, false)*/
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            val item = list[position]
            Glide.with(context).load(BaseActivity.baseURL.dropLast(5) + item.image)
                .placeholder(R.drawable.events_img)
                .transform(CenterCrop())
                .into(imgEvent)
            txtEventTitle.text = item.title
            txtEventTimings.text =
                item.start_time.dropLast(3) + " - " + item.end_time.dropLast(3)
            txtEventLocation.text = item.address

            cardViewEvent.setOnClickListener {
                adapterItemClickListener!!.onEventItemClickListener(item)
            }
        }
    }

    inner class ViewHolder(val binding: LayoutItemEventsBinding) :
        RecyclerView.ViewHolder(binding.root)
}