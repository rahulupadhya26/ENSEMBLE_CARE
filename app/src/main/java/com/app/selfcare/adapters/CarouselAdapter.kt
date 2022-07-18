package com.app.selfcare.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.app.selfcare.R
import kotlinx.android.synthetic.main.slide_item_container.view.*

class CarouselAdapter(val context: Context,
                      var list: ArrayList<Int>,
                      val viewPager: ViewPager2
) :
    RecyclerView.Adapter<CarouselAdapter.SliderViewHolder>() {

    class SliderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.img
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SliderViewHolder {
        val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.slide_item_container, parent, false)
        return SliderViewHolder(v)
    }

    override fun onBindViewHolder(holder: SliderViewHolder, position: Int) {
        val item = list[position]
        holder.image.setImageResource(item);
        if (position == list.size - 2) {
            viewPager.post(runnable)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("NotifyDataSetChanged")
    val runnable: Runnable = Runnable {
        list.addAll(list)
        notifyDataSetChanged()
    }
}