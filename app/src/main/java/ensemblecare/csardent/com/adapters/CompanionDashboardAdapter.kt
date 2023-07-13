package ensemblecare.csardent.com.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import ensemblecare.csardent.com.BaseActivity
import ensemblecare.csardent.com.R
import ensemblecare.csardent.com.controller.OnCareBuddyDashboardItemClickListener
import ensemblecare.csardent.com.data.CareBuddyDashboard
import ensemblecare.csardent.com.databinding.LayoutItemCompanionBinding

class CompanionDashboardAdapter(
    val context: Context,
    val list: List<CareBuddyDashboard>,
    private val adapterItemClickListener: OnCareBuddyDashboardItemClickListener?
) :
    RecyclerView.Adapter<CompanionDashboardAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CompanionDashboardAdapter.ViewHolder {
        val binding =
            LayoutItemCompanionBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        /*val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_companion, parent, false)*/
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.binding.imgCompanion.visibility = View.GONE
        holder.binding.txtCompanionLetter.visibility = View.VISIBLE
        holder.binding.txtCompanionLetter.text =
            item.name.substring(0, 1).uppercase()
        holder.binding.txtCompanionName.text = item.name
        holder.binding.txtCompanionNumber.text = item.phone

        holder.binding.imgCompanionCall.setOnClickListener {
            adapterItemClickListener!!.onCareBuddyDashboardItemClickListener(item, true, false)
        }

        holder.binding.imgCompanionChat.setOnClickListener {
            adapterItemClickListener!!.onCareBuddyDashboardItemClickListener(item, false, true)
        }
    }

    inner class ViewHolder(val binding: LayoutItemCompanionBinding) :
        RecyclerView.ViewHolder(binding.root)
}