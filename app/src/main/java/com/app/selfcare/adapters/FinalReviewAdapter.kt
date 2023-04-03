package com.app.selfcare.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.databinding.LayoutItemFinalReviewBinding

class FinalReviewAdapter(
    val context: Context,
    val list: ArrayList<String>
) :
    RecyclerView.Adapter<FinalReviewAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FinalReviewAdapter.ViewHolder {
        val binding =
            LayoutItemFinalReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        /*val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_final_review, parent, false)*/
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.binding.txtFinalReview.text = item
    }

    inner class ViewHolder(val binding: LayoutItemFinalReviewBinding) :
        RecyclerView.ViewHolder(binding.root)
}