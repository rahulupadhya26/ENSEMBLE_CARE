package ensemblecare.csardent.com.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ensemblecare.csardent.com.data.Coaches
import ensemblecare.csardent.com.databinding.LayoutItemCoachListBinding

class CoachListAdapter(
    val context: Context,
    val list: List<Coaches>,
) :
    RecyclerView.Adapter<CoachListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CoachListAdapter.ViewHolder {
        val binding =
            LayoutItemCoachListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        /*val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_coach_list, parent, false)*/
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            val item = list[position]
            txtCoachName.text =
                "Dr. " + item.coach.first_name + " " + item.coach.middle_name + " " + item.coach.last_name
            txtCoachType.text = item.coach.doctor_type
            txtCoachQualification.text = item.coach.qualification
            txtCoachRating.text = "DOB : " + item.coach.dob
            /*coachLayout.setOnClickListener {
                onItemClickListener!!.onTherapistItemClickListener(item)
            }*/
        }
    }

    inner class ViewHolder(val binding: LayoutItemCoachListBinding) :
        RecyclerView.ViewHolder(binding.root)
}