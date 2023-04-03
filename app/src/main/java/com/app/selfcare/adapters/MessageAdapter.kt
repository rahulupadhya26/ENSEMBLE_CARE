package com.app.selfcare.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.controller.OnMessageClickListener
import com.app.selfcare.data.MessageBean
import com.app.selfcare.databinding.MsgItemLayoutBinding
import io.agora.rtm.RtmMessageType

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
        val binding =
            MsgItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        /*val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.msg_item_layout, parent, false)*/
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            val bean: MessageBean = list[position]
            if (bean.isBeSelf()) {
                itemNameR.text = bean.getAccount()
            } else {
                itemNameL.text = bean.getAccount()
                if (bean.getBackground() !== 0) {
                    itemNameL.setBackgroundResource(bean.getBackground())
                }
            }

            val rtmMessage = bean.getMessage()
            when (rtmMessage!!.messageType) {
                RtmMessageType.TEXT -> {
                    if (bean.isBeSelf()) {
                        itemMsgR.visibility = View.VISIBLE
                        itemMsgR.text = rtmMessage.text
                    } else {
                        itemMsgL.visibility = View.VISIBLE
                        itemMsgL.text = rtmMessage.text
                    }
                    itemImgR.visibility = View.GONE
                    itemImgL.visibility = View.GONE
                }
                RtmMessageType.IMAGE -> {}
            }

            itemLayoutR.visibility = if (bean.isBeSelf()) View.VISIBLE else View.GONE
            itemLayoutL.visibility = if (bean.isBeSelf()) View.GONE else View.VISIBLE
        }
    }

    inner class ViewHolder(val binding: MsgItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)
}