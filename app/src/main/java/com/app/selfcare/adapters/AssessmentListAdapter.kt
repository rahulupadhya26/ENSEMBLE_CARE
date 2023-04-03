package com.app.selfcare.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.R
import com.app.selfcare.controller.OnAssessmentItemClickListener
import com.app.selfcare.data.Assessments
import com.app.selfcare.databinding.LayoutItemAssessmentListBinding
import com.app.selfcare.databinding.LayoutItemGoalBinding

class AssessmentListAdapter(
    val context: Context,
    val list: List<Assessments>,
    private val adapterItemClickListener: OnAssessmentItemClickListener?
) :
    RecyclerView.Adapter<AssessmentListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AssessmentListAdapter.ViewHolder {
        val binding = LayoutItemAssessmentListBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        /*val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_assessment_list, parent, false)*/
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = list[position]
        holder.binding.assessmentType.text = item.type_of_assessment
        holder.binding.txtTherapistName.text =
            "Dr. " + item.doctor_first_name + " " + item.doctor_last_name
        holder.binding.txtAssignDate.text = "Assigned Date : " + item.assign_date
        if (item.is_completed) {
            holder.binding.txtAssessmentBtn.text = "View"
        } else {
            holder.binding.txtAssessmentBtn.text = "Start"
        }
        holder.binding.startAssessment.setOnClickListener {
            val assessmentType = when (list[position].type_of_assessment) {
                "GAD 7" -> "GAD7"
                "PHQ 9" -> "PHQ9"
                else -> "NSESSS"
            }
            if (item.is_completed) {
                adapterItemClickListener!!.onAssessmentItemClickListener(item, assessmentType, true)
            } else {
                adapterItemClickListener!!.onAssessmentItemClickListener(
                    item,
                    assessmentType,
                    false
                )
            }
        }
    }

    inner class ViewHolder(val binding: LayoutItemAssessmentListBinding) :
        RecyclerView.ViewHolder(binding.root)
}