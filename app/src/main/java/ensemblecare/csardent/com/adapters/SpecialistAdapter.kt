package ensemblecare.csardent.com.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import ensemblecare.csardent.com.BaseActivity
import ensemblecare.csardent.com.R
import ensemblecare.csardent.com.controller.OnItemTherapistImageClickListener
import ensemblecare.csardent.com.controller.OnTherapistItemClickListener
import ensemblecare.csardent.com.data.Therapist
import ensemblecare.csardent.com.databinding.LayoutItemSpecialistBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

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
        val binding =
            LayoutItemSpecialistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        /*val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_specialist, parent, false)*/
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            val item = list[position]
            txtTherapistName.text =
                item.first_name + " " + item.middle_name + " " + item.last_name
            txtTherapistType.text = item.doctor_type
            txtTherapistNextSlot.text = "Friday, 11 Nov"

            Glide.with(context)
                .load(BaseActivity.baseURL.dropLast(5) + item.photo)
                .placeholder(R.drawable.doctor_img)
                .transform(CenterCrop(), RoundedCorners(5))
                .into(imgTherapistPic)

            if (position < 2) {
                cardViewRecommendedTherapist.visibility = View.VISIBLE
            } else {
                cardViewRecommendedTherapist.visibility = View.GONE
            }

            if (item.ratings != null) {
                when (item.ratings) {
                    "1" -> {
                        filledStar1.visibility = View.VISIBLE
                        emptyStar1.visibility = View.VISIBLE
                        emptyStar2.visibility = View.VISIBLE
                        emptyStar3.visibility = View.VISIBLE
                        emptyStar4.visibility = View.VISIBLE

                        filledStar2.visibility = View.GONE
                        filledStar3.visibility = View.GONE
                        filledStar4.visibility = View.GONE
                        filledStar5.visibility = View.GONE
                        emptyStar5.visibility = View.GONE
                    }
                    "2" -> {
                        filledStar1.visibility = View.VISIBLE
                        filledStar2.visibility = View.VISIBLE
                        emptyStar1.visibility = View.VISIBLE
                        emptyStar2.visibility = View.VISIBLE
                        emptyStar3.visibility = View.VISIBLE

                        filledStar3.visibility = View.GONE
                        filledStar4.visibility = View.GONE
                        filledStar5.visibility = View.GONE
                        emptyStar4.visibility = View.GONE
                        emptyStar5.visibility = View.GONE
                    }
                    "3" -> {
                        filledStar1.visibility = View.VISIBLE
                        filledStar2.visibility = View.VISIBLE
                        filledStar3.visibility = View.VISIBLE
                        emptyStar1.visibility = View.VISIBLE
                        emptyStar2.visibility = View.VISIBLE


                        filledStar4.visibility = View.GONE
                        filledStar5.visibility = View.GONE
                        emptyStar3.visibility = View.GONE
                        emptyStar4.visibility = View.GONE
                        emptyStar5.visibility = View.GONE
                    }
                    "4" -> {
                        filledStar1.visibility = View.VISIBLE
                        filledStar2.visibility = View.VISIBLE
                        filledStar3.visibility = View.VISIBLE
                        filledStar4.visibility = View.VISIBLE
                        emptyStar1.visibility = View.VISIBLE

                        filledStar5.visibility = View.GONE
                        emptyStar2.visibility = View.GONE
                        emptyStar3.visibility = View.GONE
                        emptyStar4.visibility = View.GONE
                        emptyStar5.visibility = View.GONE
                    }
                    "5" -> {
                        filledStar1.visibility = View.VISIBLE
                        filledStar2.visibility = View.VISIBLE
                        filledStar3.visibility = View.VISIBLE
                        filledStar4.visibility = View.VISIBLE
                        filledStar5.visibility = View.VISIBLE

                        emptyStar1.visibility = View.GONE
                        emptyStar2.visibility = View.GONE
                        emptyStar3.visibility = View.GONE
                        emptyStar4.visibility = View.GONE
                        emptyStar5.visibility = View.GONE
                    }
                }
            } else {
                filledStar1.visibility = View.VISIBLE
                filledStar2.visibility = View.VISIBLE
                filledStar3.visibility = View.VISIBLE
                filledStar4.visibility = View.VISIBLE
                filledStar5.visibility = View.VISIBLE

                emptyStar1.visibility = View.GONE
                emptyStar2.visibility = View.GONE
                emptyStar3.visibility = View.GONE
                emptyStar4.visibility = View.GONE
                emptyStar5.visibility = View.GONE
            }

            cardviewLayoutTherapist.setOnClickListener {
                /*row_index = position
                notifyDataSetChanged()
                onItemClickListener!!.onTherapistItemClickListener(item)*/
                onItemImageClickListener!!.onItemTherapistImageClickListener(item)
            }
            /*doctorLayout.strokeColor = ContextCompat.getColor(context, R.color.primaryGreen)
            if (row_index == position) {
                doctorLayout.strokeWidth = 4
            } else {
                doctorLayout.strokeWidth = 0
            }*/

            /*doctorLayout.setOnLongClickListener {
                onItemImageClickListener!!.onItemTherapistImageClickListener(item)
                true
            }*/
        }
    }

    inner class ViewHolder(val binding: LayoutItemSpecialistBinding) :
        RecyclerView.ViewHolder(binding.root)
}