package ensemblecare.csardent.com.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ensemblecare.csardent.com.BaseActivity
import ensemblecare.csardent.com.R
import ensemblecare.csardent.com.controller.OnCareBuddyItemClickListener
import ensemblecare.csardent.com.data.CareBuddy
import ensemblecare.csardent.com.databinding.LayoutItemCarebuddyBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import java.util.*

class CareBuddyAdapter(
    val context: Context,
    var list: List<CareBuddy>,
    private val adapterItemClickListener: OnCareBuddyItemClickListener?
) :
    RecyclerView.Adapter<CareBuddyAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CareBuddyAdapter.ViewHolder {
        val binding =
            LayoutItemCarebuddyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        /*val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_carebuddy, parent, false)*/
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        if (item.photo != null) {
            if (item.photo.isNotEmpty()) {
                holder.binding.txtCompanionCareBuddyLetter.visibility = View.GONE
                holder.binding.imgCompanionCareBuddy.visibility = View.VISIBLE
                Glide.with(context).load(BaseActivity.baseURL.dropLast(5) + item.photo)
                    .placeholder(R.drawable.events_img)
                    .transform(CenterCrop())
                    .into(holder.binding.imgCompanionCareBuddy)
            } else {
                holder.binding.txtCompanionCareBuddyLetter.visibility = View.VISIBLE
                holder.binding.txtCompanionCareBuddyLetter.text = item.first_name.substring(0, 1).uppercase()
                holder.binding.imgCompanionCareBuddy.visibility = View.GONE
            }
        } else {
            holder.binding.txtCompanionCareBuddyLetter.visibility = View.VISIBLE
            holder.binding.txtCompanionCareBuddyLetter.text = item.first_name.substring(0, 1).uppercase()
            holder.binding.imgCompanionCareBuddy.visibility = View.GONE
        }
        holder.binding.txtCompanionCareBuddyName.text = item.first_name + " " + item.last_name
        if (item.relation != null) {
            holder.binding.txtCompanionCareBuddyRelation.text = item.relation.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.getDefault()
                ) else it.toString()
            }
        }

        holder.binding.layoutCompanionCareBuddy.setOnClickListener {
            adapterItemClickListener!!.onCareBuddyItemClickListener(item,
                isCall = false,
                isChat = false
            )
        }

        holder.binding.imgCompanionCareBuddyCall.setOnClickListener {
            adapterItemClickListener!!.onCareBuddyItemClickListener(item,
                isCall = true,
                isChat = false
            )
        }

        holder.binding.imgCompanionCareBuddyChat.setOnClickListener {
            adapterItemClickListener!!.onCareBuddyItemClickListener(item,
                isCall = false,
                isChat = true
            )
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun filterList(filteredNames: ArrayList<CareBuddy>) {
        this.list = filteredNames
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: LayoutItemCarebuddyBinding) :
        RecyclerView.ViewHolder(binding.root)
}