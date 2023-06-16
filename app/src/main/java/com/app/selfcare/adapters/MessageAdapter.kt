package com.app.selfcare.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.BaseActivity
import com.app.selfcare.R
import com.app.selfcare.controller.OnBottomReachedListener
import com.app.selfcare.controller.OnMessageClickListener
import com.app.selfcare.data.MessageBean
import com.app.selfcare.databinding.MsgItemLayoutBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import io.agora.rtm.RtmMessageType


class MessageAdapter(
    val context: Context,
    val list: List<MessageBean>,
    val listener: OnMessageClickListener,
    val photo: String,
    private val receiverPhoto: String,
    private val onBottomReachedListener: OnBottomReachedListener?
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
                        itemMsgR.backgroundTintList =
                            ContextCompat.getColorStateList(context, R.color.chatMsgBackground)
                    } else {
                        itemMsgL.visibility = View.VISIBLE
                        itemMsgL.text = rtmMessage.text
                        itemMsgL.backgroundTintList =
                            ContextCompat.getColorStateList(context, R.color.chatMsgBackground)
                    }
                    itemImgR.visibility = View.GONE
                    itemImgL.visibility = View.GONE
                }

                else -> {
                    val text = String(rtmMessage.rawMessage)
                    val textArr = text.split(",")
                    if (bean.isBeSelf()) {
                        itemMsgR.visibility = View.VISIBLE
                        itemMsgR.text = textArr[0]
                        itemMsgR.backgroundTintList =
                            ContextCompat.getColorStateList(context, R.color.lightestGreyColor)
                    } else {
                        itemMsgL.visibility = View.VISIBLE
                        itemMsgL.text = textArr[0]
                        itemMsgL.backgroundTintList =
                            ContextCompat.getColorStateList(context, R.color.lightestGreyColor)
                    }
                }
            }

            if (photo != "null" && photo.isNotEmpty()) {
                if (bean.isBeSelf()) {
                    imgUserPicRight.visibility = View.VISIBLE
                    itemNameR.visibility = View.GONE
                    Glide.with(context)
                        .load(BaseActivity.baseURL.dropLast(5) + photo)
                        .placeholder(R.drawable.user_pic)
                        .transform(CenterCrop(), RoundedCorners(5))
                        .into(imgUserPicRight)
                }
            } else {
                if (bean.isBeSelf()) {
                    imgUserPicRight.visibility = View.GONE
                    itemNameR.visibility = View.VISIBLE
                }
            }

            if (receiverPhoto != null) {
                if (receiverPhoto != "null" && receiverPhoto.isNotEmpty()) {
                    if (!bean.isBeSelf()) {
                        imgUserPicLeft.visibility = View.VISIBLE
                        itemNameL.visibility = View.GONE
                        Glide.with(context)
                            .load(BaseActivity.baseURL.dropLast(5) + receiverPhoto)
                            .placeholder(R.drawable.user_pic)
                            .transform(CenterCrop(), RoundedCorners(5))
                            .into(imgUserPicLeft)
                    }
                } else {
                    if (!bean.isBeSelf()) {
                        imgUserPicLeft.visibility = View.GONE
                        itemNameL.visibility = View.VISIBLE
                    }
                }
            } else {
                if (!bean.isBeSelf()) {
                    imgUserPicLeft.visibility = View.GONE
                    itemNameL.visibility = View.VISIBLE
                }
            }

            itemLayoutR.visibility = if (bean.isBeSelf()) View.VISIBLE else View.GONE
            itemLayoutL.visibility = if (bean.isBeSelf()) View.GONE else View.VISIBLE

            itemMsgR.setOnClickListener {
                if (rtmMessage.messageType == RtmMessageType.RAW) {
                    listener.onItemClick(bean)
                }
            }

            itemMsgL.setOnClickListener {
                if (rtmMessage.messageType == RtmMessageType.RAW) {
                    listener.onItemClick(bean)
                }
            }

            if (position == 0) {
                onBottomReachedListener!!.onBottomReached(position);
            }
        }
    }

    inner class ViewHolder(val binding: MsgItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)
}