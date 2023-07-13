package ensemblecare.csardent.com.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ensemblecare.csardent.com.controller.OnCareBuddyDashboardItemClickListener
import ensemblecare.csardent.com.data.CareBuddyDashboard
import ensemblecare.csardent.com.databinding.LayoutItemCarebuddyBinding
import java.util.ArrayList

class CareBuddyCommunityDashboardAdapter(
    val context: Context,
    var list: List<CareBuddyDashboard>,
    private val adapterItemClickListener: OnCareBuddyDashboardItemClickListener?
) :
    RecyclerView.Adapter<CareBuddyCommunityDashboardAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CareBuddyCommunityDashboardAdapter.ViewHolder {
        val binding =
            LayoutItemCarebuddyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.binding.txtCompanionCareBuddyLetter.visibility = View.VISIBLE
        holder.binding.txtCompanionCareBuddyLetter.text = item.name.substring(0, 1).uppercase()
        holder.binding.imgCompanionCareBuddy.visibility = View.GONE
        holder.binding.txtCompanionCareBuddyName.text = item.name
        if (item.phone != null) {
            holder.binding.txtCompanionCareBuddyRelation.text = item.phone
        }

        holder.binding.layoutCompanionCareBuddy.setOnClickListener {
            adapterItemClickListener!!.onCareBuddyDashboardItemClickListener(item,
                isCall = false,
                isChat = false
            )
        }

        holder.binding.imgCompanionCareBuddyCall.setOnClickListener {
            adapterItemClickListener!!.onCareBuddyDashboardItemClickListener(item,
                isCall = true,
                isChat = false
            )
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun filterList(filteredNames: ArrayList<CareBuddyDashboard>) {
        this.list = filteredNames
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: LayoutItemCarebuddyBinding) :
        RecyclerView.ViewHolder(binding.root)
}