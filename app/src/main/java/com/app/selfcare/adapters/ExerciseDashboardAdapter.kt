package com.app.selfcare.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.app.selfcare.BaseActivity
import com.app.selfcare.controller.OnExerciseDashboardItemClickListener
import com.app.selfcare.data.ExerciseDashboard
import com.app.selfcare.databinding.LayoutItemExerciseDashboardBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

class ExerciseDashboardAdapter(
    val context: Context,
    val list: ArrayList<ExerciseDashboard>,
    val viewPager: ViewPager2,
    private val adapterItemClickListener: OnExerciseDashboardItemClickListener
) :
    RecyclerView.Adapter<ExerciseDashboardAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ExerciseDashboardAdapter.ViewHolder {
        val binding = LayoutItemExerciseDashboardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        /*val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_exercise_dashboard, parent, false)*/
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            val item = list[position]
            when (item.type) {
                "Video" -> {
                    var videoImg = item.url
                    if (item.url.isNotEmpty() && item.url.contains("youtube")) {
                        val videoId: String = item.url.split("v=")[1]
                        videoImg =
                            "http://img.youtube.com/vi/$videoId/hqdefault.jpg" //high quality thumbnail
                    }
                    Glide.with(context)
                        .load(videoImg).transform(CenterCrop(), RoundedCorners(5))
                        .into(imgExercise)
                }

                else -> {
                    Glide.with(context).load(BaseActivity.baseURL.dropLast(5) + item.image)
                        .transform(CenterCrop(), RoundedCorners(5))
                        .into(imgExercise)
                }
            }
            txtExerciseName.text = item.exercise_name
            if (position == list.size - 2) {
                viewPager.post(runnable)
            }
            cardViewExerciseData.setOnClickListener {
                adapterItemClickListener.onExerciseDashboardItemClickListener(item)
            }
        }
    }

    inner class ViewHolder(val binding: LayoutItemExerciseDashboardBinding) :
        RecyclerView.ViewHolder(binding.root)

    @SuppressLint("NotifyDataSetChanged")
    val runnable: Runnable = Runnable {
        list.addAll(list)
        notifyDataSetChanged()
    }

}