package ensemblecare.csardent.com.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import ensemblecare.csardent.com.BaseActivity
import ensemblecare.csardent.com.R
import ensemblecare.csardent.com.controller.OnCareBuddyItemClickListener
import ensemblecare.csardent.com.data.CareBuddy
import ensemblecare.csardent.com.databinding.LayoutItemCarebuddyBinding
import java.util.Locale
import kotlin.collections.ArrayList

class CompanionAdapter(
    val context: Context,
    var list: List<CareBuddy>,
    private val isSelected: Boolean,
    private val adapterItemClickListener: OnCareBuddyItemClickListener?
) :
    RecyclerView.Adapter<CompanionAdapter.ViewHolder>() {

    private val selectedItems = ArrayList<CareBuddy>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CompanionAdapter.ViewHolder {
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
        holder.binding.imgCompanionCareBuddyReachOut.visibility = View.GONE
        if (isSelected) {
            holder.binding.imgCompanionCareBuddyCall.setImageResource(R.drawable.circle_border_dot)
        } else {
            holder.binding.imgCompanionCareBuddyCall.setImageResource(R.drawable.call_community)
        }
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
                holder.binding.txtCompanionCareBuddyLetter.text =
                    item.first_name.substring(0, 1).uppercase()
                holder.binding.imgCompanionCareBuddy.visibility = View.GONE
            }
        } else {
            holder.binding.txtCompanionCareBuddyLetter.visibility = View.VISIBLE
            holder.binding.txtCompanionCareBuddyLetter.text =
                item.first_name.substring(0, 1).uppercase()
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

        if (isSelected)
            if (item.isSelected) {
                holder.binding.imgCompanionCareBuddyCall.setImageResource(R.drawable.task_alt)
            } else {
                holder.binding.imgCompanionCareBuddyCall.setImageResource(R.drawable.circle_border_dot)
            }

        holder.binding.imgCompanionCareBuddyCall.setOnClickListener {
            if (isSelected) {
                if (item.isSelected) {
                    holder.binding.imgCompanionCareBuddyCall.setImageResource(R.drawable.circle_border_dot)
                } else {
                    holder.binding.imgCompanionCareBuddyCall.setImageResource(R.drawable.task_alt)
                }
                toggleSelection(item)
                adapterItemClickListener!!.onCareBuddyItemClickListener(
                    item,
                    isCall = false,
                    isReachOut = true
                )
            } else {
                adapterItemClickListener!!.onCareBuddyItemClickListener(
                    item,
                    isCall = true,
                    isReachOut = false
                )
            }
        }

        holder.binding.layoutCompanionCareBuddy.setOnClickListener {
            adapterItemClickListener!!.onCareBuddyItemClickListener(
                item,
                isCall = false,
                isReachOut = false
            )
        }

        holder.binding.imgCompanionCareBuddyReachOut.setOnClickListener {
            adapterItemClickListener!!.onCareBuddyItemClickListener(
                item,
                isCall = false,
                isReachOut = true
            )
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun toggleSelection(item: CareBuddy) {
        item.isSelected = !item.isSelected
        if (item.isSelected) {
            selectedItems.add(item)
        } else {
            selectedItems.remove(item)
        }
        notifyDataSetChanged()
    }

    fun getSelectedItems(): ArrayList<CareBuddy> {
        return selectedItems
    }

    fun clearSelectedItems() {
        selectedItems.clear()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun filterList(filteredNames: ArrayList<CareBuddy>) {
        this.list = filteredNames
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: LayoutItemCarebuddyBinding) :
        RecyclerView.ViewHolder(binding.root)
}