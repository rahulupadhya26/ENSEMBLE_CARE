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
import com.app.selfcare.controller.OnChatMessageClickListener
import com.app.selfcare.data.ChatMessages
import com.app.selfcare.databinding.LayoutItemGoalBinding
import com.app.selfcare.databinding.MsgItemLayoutBinding
import io.agora.chat.ChatMessage


class ChatMessageAdapter(
    val context: Context,
    val list: List<ChatMessages>,
    val listener: OnChatMessageClickListener
) :
    RecyclerView.Adapter<ChatMessageAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ChatMessageAdapter.ViewHolder {
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
            val bean: ChatMessages = list[position]
            if (bean.isBeSelf()) {
                itemNameR.text = bean.getFrom()
            } else {
                itemNameL.text = bean.getFrom()
                if (bean.getBackground() !== 0) {
                    itemNameL.setBackgroundResource(bean.getBackground())
                }
            }

            when (bean.getType()) {
                ChatMessage.Type.TXT -> {
                    if (bean.isBeSelf()) {
                        itemMsgR.visibility = View.VISIBLE
                        itemMsgR.text = bean.getMessage()
                    } else {
                        itemMsgL.visibility = View.VISIBLE
                        itemMsgL.text = bean.getMessage()
                    }
                    itemImgR.visibility = View.GONE
                    itemImgL.visibility = View.GONE
                }
                ChatMessage.Type.IMAGE -> {

                }
                else -> {

                }
            }

            itemLayoutR.visibility = if (bean.isBeSelf()) View.VISIBLE else View.GONE
            itemLayoutL.visibility = if (bean.isBeSelf()) View.GONE else View.VISIBLE
        }
    }

    inner class ViewHolder(val binding: MsgItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)
}