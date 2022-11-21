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
import com.app.selfcare.data.Nutrition
import kotlinx.android.synthetic.main.layout_item_nutrition_slider.view.*

class NutritionSliderAdapter(
    val context: Context,
    var list: ArrayList<Nutrition>,
    val viewPager: ViewPager2
) :
    RecyclerView.Adapter<NutritionSliderAdapter.SliderViewHolder>() {

    class SliderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nutritionImage: ImageView = itemView.imgNutrition
        val nutritionText: TextView = itemView.textNutrition
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SliderViewHolder {
        val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_nutrition_slider, parent, false)
        return SliderViewHolder(v)
    }

    override fun onBindViewHolder(holder: SliderViewHolder, position: Int) {
        val item = list[position]
        holder.nutritionImage.setImageResource(item.image)
        holder.nutritionText.text = item.name
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