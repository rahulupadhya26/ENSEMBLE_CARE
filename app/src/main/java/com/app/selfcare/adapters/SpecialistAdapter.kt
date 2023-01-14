package com.app.selfcare.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.BaseActivity
import com.app.selfcare.R
import com.app.selfcare.controller.OnItemTherapistImageClickListener
import com.app.selfcare.controller.OnTherapistItemClickListener
import com.app.selfcare.data.Therapist
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.card.MaterialCardView
import kotlinx.android.synthetic.main.dialog_appointment_cancelled_alert.*
import kotlinx.android.synthetic.main.fragment_therapist_detail.*
import kotlinx.android.synthetic.main.layout_item_specialist.view.*

class SpecialistAdapter(
    val context: Context,
    val list: List<Therapist>,
    private val onItemImageClickListener: OnItemTherapistImageClickListener?,
    private val onItemClickListener: OnTherapistItemClickListener?
) :
    RecyclerView.Adapter<SpecialistAdapter.ViewHolder>() {
    var row_index: Int = -1
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SpecialistAdapter.ViewHolder {
        val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_specialist, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: SpecialistAdapter.ViewHolder, position: Int) {
        val item = list[position]
        holder.doctorName.text =
            item.first_name + " " + item.middle_name + " " + item.last_name
        holder.doctorType.text = item.doctor_type
        holder.doctorNextAvailSlot.text = "Friday, 11 Nov"

        Glide.with(context)
            .load(BaseActivity.baseURL.dropLast(5) + item.photo)
            .placeholder(R.drawable.doctor_img)
            .transform(CenterCrop(), RoundedCorners(5))
            .into(holder.imgTherapistPic)

        if (position < 2) {
            holder.cardViewRecommendedTherapist.visibility = View.VISIBLE
        } else {
            holder.cardViewRecommendedTherapist.visibility = View.GONE
        }

        when (item.ratings) {
            "1" -> {
                holder.filledStar1.visibility = View.VISIBLE
                holder.emptyStar1.visibility = View.VISIBLE
                holder.emptyStar2.visibility = View.VISIBLE
                holder.emptyStar3.visibility = View.VISIBLE
                holder.emptyStar4.visibility = View.VISIBLE

                holder.filledStar2.visibility = View.GONE
                holder.filledStar3.visibility = View.GONE
                holder.filledStar4.visibility = View.GONE
                holder.filledStar5.visibility = View.GONE
                holder.emptyStar5.visibility = View.GONE
            }
            "2" -> {
                holder.filledStar1.visibility = View.VISIBLE
                holder.filledStar2.visibility = View.VISIBLE
                holder.emptyStar1.visibility = View.VISIBLE
                holder.emptyStar2.visibility = View.VISIBLE
                holder.emptyStar3.visibility = View.VISIBLE

                holder.filledStar3.visibility = View.GONE
                holder.filledStar4.visibility = View.GONE
                holder.filledStar5.visibility = View.GONE
                holder.emptyStar4.visibility = View.GONE
                holder.emptyStar5.visibility = View.GONE
            }
            "3" -> {
                holder.filledStar1.visibility = View.VISIBLE
                holder.filledStar2.visibility = View.VISIBLE
                holder.filledStar3.visibility = View.VISIBLE
                holder.emptyStar1.visibility = View.VISIBLE
                holder.emptyStar2.visibility = View.VISIBLE


                holder.filledStar4.visibility = View.GONE
                holder.filledStar5.visibility = View.GONE
                holder.emptyStar3.visibility = View.GONE
                holder.emptyStar4.visibility = View.GONE
                holder.emptyStar5.visibility = View.GONE
            }
            "4" -> {
                holder.filledStar1.visibility = View.VISIBLE
                holder.filledStar2.visibility = View.VISIBLE
                holder.filledStar3.visibility = View.VISIBLE
                holder.filledStar4.visibility = View.VISIBLE
                holder.emptyStar1.visibility = View.VISIBLE

                holder.filledStar5.visibility = View.GONE
                holder.emptyStar2.visibility = View.GONE
                holder.emptyStar3.visibility = View.GONE
                holder.emptyStar4.visibility = View.GONE
                holder.emptyStar5.visibility = View.GONE
            }
            "5" -> {
                holder.filledStar1.visibility = View.VISIBLE
                holder.filledStar2.visibility = View.VISIBLE
                holder.filledStar3.visibility = View.VISIBLE
                holder.filledStar4.visibility = View.VISIBLE
                holder.filledStar5.visibility = View.VISIBLE

                holder.emptyStar1.visibility = View.GONE
                holder.emptyStar2.visibility = View.GONE
                holder.emptyStar3.visibility = View.GONE
                holder.emptyStar4.visibility = View.GONE
                holder.emptyStar5.visibility = View.GONE
            }
        }

        holder.doctorLayout.setOnClickListener {
            /*row_index = position
            notifyDataSetChanged()
            onItemClickListener!!.onTherapistItemClickListener(item)*/
            onItemImageClickListener!!.onItemTherapistImageClickListener(item)
        }
        /*holder.doctorLayout.strokeColor = ContextCompat.getColor(context, R.color.primaryGreen)
        if (row_index == position) {
            holder.doctorLayout.strokeWidth = 4
        } else {
            holder.doctorLayout.strokeWidth = 0
        }*/

        /*holder.doctorLayout.setOnLongClickListener {
            onItemImageClickListener!!.onItemTherapistImageClickListener(item)
            true
        }*/
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val doctorName: TextView = itemView.txtTherapistName
        val doctorType: TextView = itemView.txtTherapistType
        val imgTherapistPic: ImageView = itemView.imgTherapistPic
        val doctorNextAvailSlot: TextView = itemView.txtTherapistNextSlot
        val cardViewRecommendedTherapist: CardView = itemView.cardViewRecommendedTherapist
        val doctorLayout: RelativeLayout = itemView.cardview_layout_therapist
        val filledStar1: ImageView = itemView.filledStar1
        val filledStar2: ImageView = itemView.filledStar2
        val filledStar3: ImageView = itemView.filledStar3
        val filledStar4: ImageView = itemView.filledStar4
        val filledStar5: ImageView = itemView.filledStar5
        val emptyStar1: ImageView = itemView.emptyStar1
        val emptyStar2: ImageView = itemView.emptyStar2
        val emptyStar3: ImageView = itemView.emptyStar3
        val emptyStar4: ImageView = itemView.emptyStar4
        val emptyStar5: ImageView = itemView.emptyStar5
    }
}