package com.app.selfcare.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.R
import kotlinx.android.synthetic.main.layout_item_final_review.view.*

class FinalReviewAdapter (
    val context: Context,
    val list: ArrayList<String>
) :
    RecyclerView.Adapter<FinalReviewAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FinalReviewAdapter.ViewHolder {
        val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_final_review, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: FinalReviewAdapter.ViewHolder, position: Int) {
        val item = list[position]
        holder.finalReview.text = item
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val finalReview: TextView = itemView.txt_final_review
    }
}