package com.app.selfcare.adapters

import android.annotation.SuppressLint
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
import com.app.selfcare.controller.OnCareBuddyItemClickListener
import com.app.selfcare.data.CareBuddy
import com.app.selfcare.databinding.LayoutItemCarebuddyBinding
import com.app.selfcare.databinding.LayoutItemGoalBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import java.util.*

class CareBuddyAdapter(
    val context: Context,
    val list: List<CareBuddy>,
    private val adapterItemClickListener: OnCareBuddyItemClickListener?
) :
    RecyclerView.Adapter<CareBuddyAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CareBuddyAdapter.ViewHolder {
        val binding = LayoutItemCarebuddyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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
        if (item.photo != null) {
            if (item.photo.isNotEmpty()) {
                holder.binding.txtNameFirstLetter.visibility = View.GONE
                holder.binding.imgCareBuddy.visibility = View.VISIBLE
                Glide.with(context).load(BaseActivity.baseURL.dropLast(5) + item.photo)
                    .placeholder(R.drawable.events_img)
                    .transform(CenterCrop())
                    .into(holder.binding.imgCareBuddy)
            } else {
                holder.binding.txtNameFirstLetter.visibility = View.VISIBLE
                holder.binding.txtNameFirstLetter.text = item.first_name.substring(0, 1).uppercase()
                holder.binding.imgCareBuddy.visibility = View.GONE
            }
        } else {
            holder.binding.txtNameFirstLetter.visibility = View.VISIBLE
            holder.binding.txtNameFirstLetter.text = item.first_name.substring(0, 1).uppercase()
            holder.binding.imgCareBuddy.visibility = View.GONE
        }
        holder.binding.txtCareBuddyName.text = item.first_name + " " + item.last_name
        holder.binding.txtCareBuddyRelation.text = item.relation.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(
                Locale.getDefault()
            ) else it.toString()
        }

        holder.binding.layoutCareBuddy.setOnClickListener {
            adapterItemClickListener!!.onCareBuddyItemClickListener(item)
        }
    }

    inner class ViewHolder(val binding: LayoutItemCarebuddyBinding) : RecyclerView.ViewHolder(binding.root)
}