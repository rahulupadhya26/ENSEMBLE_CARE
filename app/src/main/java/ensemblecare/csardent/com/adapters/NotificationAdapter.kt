package ensemblecare.csardent.com.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ensemblecare.csardent.com.data.NotificationData
import ensemblecare.csardent.com.databinding.LayoutItemNotificationListBinding

class NotificationAdapter(
    val context: Context,
    val list: ArrayList<NotificationData>
) :
    RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NotificationAdapter.ViewHolder {
        val binding = LayoutItemNotificationListBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        /*val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_notification_list, parent, false)*/
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            val item = list[position]
            notifyTxt.text = item.notification_desc
            notifyTimeTxt.text = item.time
            if (item.is_group_appointment) {
                notifyListGroupImg.visibility = View.VISIBLE
                notifyListImgUser.visibility = View.GONE
            } else {
                notifyListImgUser.visibility = View.VISIBLE
                notifyListGroupImg.visibility = View.GONE
            }
        }
    }

    inner class ViewHolder(val binding: LayoutItemNotificationListBinding) :
        RecyclerView.ViewHolder(binding.root)
}