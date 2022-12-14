package com.app.selfcare.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.app.selfcare.BaseActivity
import com.app.selfcare.R
import com.app.selfcare.controller.OnNutritionDashboardItemClickListener
import com.app.selfcare.data.NutritionDashboard
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import kotlinx.android.synthetic.main.layout_item_nutrition_slider.view.*

class NutritionSliderAdapter(
    val context: Context,
    var list: ArrayList<NutritionDashboard>,
    val viewPager: ViewPager2,
    private val adapterItemClickListener: OnNutritionDashboardItemClickListener?
) :
    RecyclerView.Adapter<NutritionSliderAdapter.SliderViewHolder>() {

    class SliderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nutritionImage: ImageView = itemView.imgNutrition
        val nutritionText: TextView = itemView.textNutrition
        val nutritionPane:LinearLayout = itemView.layoutNutritionSliderPane
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SliderViewHolder {
        val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_nutrition_slider, parent, false)
        return SliderViewHolder(v)
    }

    override fun onBindViewHolder(holder: SliderViewHolder, position: Int) {
        val item = list[position]
        Glide.with(context)
            .load(BaseActivity.baseURL.dropLast(5) + item.image)
            .transform(CenterCrop(), RoundedCorners(5))
            .into(holder.nutritionImage)
        holder.nutritionText.text = item.nutrition_name
        if (position == list.size - 2) {
            viewPager.post(runnable)
        }
        holder.nutritionPane.setOnClickListener {
            adapterItemClickListener!!.onNutritionDashboardItemClicked(item)
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