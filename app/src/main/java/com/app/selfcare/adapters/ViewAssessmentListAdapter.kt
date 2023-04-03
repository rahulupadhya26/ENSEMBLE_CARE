package com.app.selfcare.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.data.QuestionAnswer
import com.app.selfcare.databinding.LayoutItemViewAssessmentListBinding

class ViewAssessmentListAdapter(val list: List<QuestionAnswer>) :
    RecyclerView.Adapter<ViewAssessmentListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewAssessmentListAdapter.ViewHolder {
        val binding = LayoutItemViewAssessmentListBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        /*val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_view_assessment_list, parent, false)*/
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.binding.txtQuestion.text = (position + 1).toString() + ". " + item.question
        holder.binding.submittedAnswer.text = item.answer
    }

    inner class ViewHolder(val binding: LayoutItemViewAssessmentListBinding) :
        RecyclerView.ViewHolder(binding.root)
}