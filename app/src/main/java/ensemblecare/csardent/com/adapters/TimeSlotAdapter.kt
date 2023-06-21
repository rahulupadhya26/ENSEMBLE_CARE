package ensemblecare.csardent.com.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import ensemblecare.csardent.com.R
import ensemblecare.csardent.com.controller.OnTextClickListener
import ensemblecare.csardent.com.data.TimeSlot
import ensemblecare.csardent.com.databinding.TimeSlotItemBinding

class TimeSlotAdapter(
    val context: Context,
    val list: ArrayList<TimeSlot>,
    private val onTextClickListener: OnTextClickListener?
) :
    RecyclerView.Adapter<TimeSlotAdapter.ViewHolder>() {
    var row_index: Int = -1
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TimeSlotAdapter.ViewHolder {
        val binding =
            TimeSlotItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        /*val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.time_slot_item, parent, false)*/
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            val item = list[position]
            txtTimeSlot.text =
                item.time_slot_start/* + " - " + item.time_slot_end.dropLast(3)*/
            layoutTimeSlot.setOnClickListener {
                row_index = position;
                notifyDataSetChanged()
                onTextClickListener!!.onTextClickListener(item)
            }
            if (row_index == position) {
                cardViewTimeSlot.setCardBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.primaryGreen
                    )
                )
                //timeSlotLayout.setBackgroundResource(R.drawable.bg_time_slot_rounded_selected)
                txtTimeSlot.setTextColor(ContextCompat.getColor(context, R.color.white))
            } else {
                cardViewTimeSlot.setCardBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.white
                    )
                )
                txtTimeSlot.setTextColor(ContextCompat.getColor(context, R.color.primaryGreen))
            }
        }
    }

    inner class ViewHolder(val binding: TimeSlotItemBinding) : RecyclerView.ViewHolder(binding.root)
}