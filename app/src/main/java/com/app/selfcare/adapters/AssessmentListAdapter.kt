package com.app.selfcare.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.R
import com.app.selfcare.controller.OnAppointmentItemClickListener
import com.app.selfcare.controller.OnAssessmentItemClickListener
import com.app.selfcare.data.Assessments
import com.app.selfcare.utils.DateUtils
import kotlinx.android.synthetic.main.layout_item_assessment_list.view.*
import kotlinx.android.synthetic.main.layout_item_journal_list.view.*

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
        val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_assessment_list, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: AssessmentListAdapter.ViewHolder, position: Int) {

        val item = list[position]
        holder.assessmentType.text = item.type_of_assessment
        holder.assessmentTherapistName.text =
            "Dr. " + item.doctor_first_name + " " + item.doctor_last_name
        holder.assessmentDate.text = "Assigned Date : " + item.assign_date
        if (item.is_completed) {
            holder.assessmentBtn.text = "View"
        } else {
            holder.assessmentBtn.text = "Start"
        }
        holder.assessmentStart.setOnClickListener {
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

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val assessmentType: TextView = itemView.assessmentType
        val assessmentTherapistName: TextView = itemView.txtTherapistName
        val assessmentDate: TextView = itemView.txtAssignDate
        val assessmentStart: CardView = itemView.startAssessment
        val assessmentBtn: TextView = itemView.txtAssessmentBtn
    }
}