package com.app.selfcare.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.R
import com.app.selfcare.controller.OnJournalItemClickListener
import com.app.selfcare.data.Coaches
import com.app.selfcare.utils.DateUtils
import kotlinx.android.synthetic.main.layout_item_coach_list.view.*
import kotlinx.android.synthetic.main.layout_item_journal_list.view.*
import kotlinx.android.synthetic.main.layout_item_specialist.view.*

class CoachListAdapter(
    val context: Context,
    val list: List<Coaches>,
) :
    RecyclerView.Adapter<CoachListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CoachListAdapter.ViewHolder {
        val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_coach_list, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: CoachListAdapter.ViewHolder, position: Int) {
        val item = list[position]
        holder.coachName.text =
            "Dr. " + item.coach.first_name + " " + item.coach.middle_name + " " + item.coach.last_name
        holder.coachType.text = item.coach.doctor_type
        holder.coachQualification.text = item.coach.qualification
        holder.coachRating.text = "DOB : " + item.coach.dob
        /*holder.coachLayout.setOnClickListener {
            onItemClickListener!!.onTherapistItemClickListener(item)
        }*/
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val coachImage: ImageView = itemView.imgCoachPic
        val coachName: TextView = itemView.txtCoachName
        val coachType: TextView = itemView.txtCoachType
        val coachQualification: TextView = itemView.txtCoachQualification
        val coachRating: TextView = itemView.txtCoachRating
        val coachLayout: CardView = itemView.cardview_layout_coach
    }
}