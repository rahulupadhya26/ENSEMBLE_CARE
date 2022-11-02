package com.app.selfcare.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.R
import com.app.selfcare.controller.OnMessageClickListener
import com.app.selfcare.data.MessageBean
import io.agora.rtm.RtmMessageType
import kotlinx.android.synthetic.main.msg_item_layout.view.*

class MessageAdapter(
    val context: Context,
    val list: List<MessageBean>,
    val listener: OnMessageClickListener
) :
    RecyclerView.Adapter<MessageAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MessageAdapter.ViewHolder {
        val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.msg_item_layout, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: MessageAdapter.ViewHolder, position: Int) {

        val bean: MessageBean = list[position]
        if (bean.isBeSelf()) {
            holder.textViewSelfName.text = bean.getAccount()
        } else {
            holder.textViewOtherName.text = bean.getAccount()
            if (bean.getBackground() !== 0) {
                holder.textViewOtherName.setBackgroundResource(bean.getBackground())
            }
        }

        holder.itemView.setOnClickListener { v: View? ->
            listener.onItemClick(bean)
        }

        val rtmMessage = bean.getMessage()
        when (rtmMessage!!.messageType) {
            RtmMessageType.TEXT -> {
                if (bean.isBeSelf()) {
                    holder.textViewSelfMsg.visibility = View.VISIBLE
                    holder.textViewSelfMsg.text = rtmMessage.text
                } else {
                    holder.textViewOtherMsg.visibility = View.VISIBLE
                    holder.textViewOtherMsg.text = rtmMessage.text
                }
                holder.imageViewSelfImg.visibility = View.GONE
                holder.imageViewOtherImg.visibility = View.GONE
            }
            RtmMessageType.IMAGE -> {}
        }

        holder.layoutRight.visibility = if (bean.isBeSelf()) View.VISIBLE else View.GONE
        holder.layoutLeft.visibility = if (bean.isBeSelf()) View.GONE else View.VISIBLE
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewOtherName: TextView = itemView.item_name_l
        val textViewOtherMsg: TextView = itemView.item_msg_l
        val imageViewOtherImg: ImageView = itemView.item_img_l
        val textViewSelfName: TextView = itemView.item_name_r
        val textViewSelfMsg: TextView = itemView.item_msg_r
        val imageViewSelfImg: ImageView = itemView.item_img_r
        val layoutLeft: RelativeLayout = itemView.item_layout_l
        val layoutRight: RelativeLayout = itemView.item_layout_r
    }
}