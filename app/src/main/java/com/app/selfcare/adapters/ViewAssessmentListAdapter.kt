package com.app.selfcare.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.R
import com.app.selfcare.data.QuestionAnswer
import kotlinx.android.synthetic.main.layout_item_view_assessment_list.view.*

class ViewAssessmentListAdapter(val list: List<QuestionAnswer>) :
    RecyclerView.Adapter<ViewAssessmentListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewAssessmentListAdapter.ViewHolder {
        val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_view_assessment_list, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewAssessmentListAdapter.ViewHolder, position: Int) {

        val item = list[position]
        holder.textQuestion.text = (position + 1).toString() + ". " + item.question
        holder.submittedAnswer.text = item.answer
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textQuestion: TextView = itemView.txtQuestion
        val submittedAnswer: TextView = itemView.submittedAnswer
    }
}