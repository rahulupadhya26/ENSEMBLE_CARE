package com.app.selfcare.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.R
import com.app.selfcare.data.NotificationData
import kotlinx.android.synthetic.main.layout_item_notification_list.view.*

class NotificationAdapter(
    val context: Context,
    val list: ArrayList<NotificationData>
) :
    RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NotificationAdapter.ViewHolder {
        val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_notification_list, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: NotificationAdapter.ViewHolder, position: Int) {
        val item = list[position]
        holder.notifyTxt.text = item.notification_desc
        holder.notifyTimeTxt.text = item.time
        if (item.is_group_appointment) {
            holder.notifyListGroupImg.visibility = View.VISIBLE
            holder.notifyListImgUser.visibility = View.GONE
        } else {
            holder.notifyListImgUser.visibility = View.VISIBLE
            holder.notifyListGroupImg.visibility = View.GONE
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val notifyTimeTxt: TextView = itemView.notifyTimeTxt
        val notifyTxt: TextView = itemView.notifyTxt
        val notifyListGroupImg: ImageView = itemView.notifyListGroupImg
        val notifyListImgUser: ImageView = itemView.notifyListImgUser
    }
}