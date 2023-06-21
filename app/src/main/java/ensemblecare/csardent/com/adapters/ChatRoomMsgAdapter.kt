package ensemblecare.csardent.com.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import ensemblecare.csardent.com.BaseActivity
import ensemblecare.csardent.com.R
import ensemblecare.csardent.com.controller.OnBottomReachedListener
import ensemblecare.csardent.com.controller.OnChatRoomMessageClickListener
import ensemblecare.csardent.com.data.ChatRoomMsgBean
import ensemblecare.csardent.com.databinding.ChatRoomMsgItemLayoutBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import io.agora.rtm.RtmMessageType

class ChatRoomMsgAdapter(
    val context: Context,
    val list: List<ChatRoomMsgBean>,
    private val selfPhoto: String,
    val listener: OnChatRoomMessageClickListener,
    private val onBottomReachedListener: OnBottomReachedListener?
) :
    RecyclerView.Adapter<ChatRoomMsgAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ChatRoomMsgAdapter.ViewHolder {
        val binding =
            ChatRoomMsgItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        /*val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.msg_item_layout, parent, false)*/
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            val bean: ChatRoomMsgBean = list[position]

            val rtmMessage = bean.getMessage()
            when (rtmMessage!!.messageType) {
                RtmMessageType.TEXT -> {
                    if (bean.isBeSelf()) {
                        layoutMsgRight.visibility = View.VISIBLE
                        itemMsgR.text = rtmMessage.text
                        itemReceiverName.text = bean.getChatRoomMsgs().client_name
                        itemReceiverDateTime.text = bean.getChatRoomMsgs().created_at
                        itemMsgR.backgroundTintList =
                            ContextCompat.getColorStateList(context, R.color.chatMsgBackground)
                    } else {
                        layoutMsgLeft.visibility = View.VISIBLE
                        itemMsgL.text = rtmMessage.text
                        itemSenderName.text = bean.getChatRoomMsgs().client_name
                        itemSenderDateTime.text = bean.getChatRoomMsgs().created_at
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
                        layoutMsgRight.visibility = View.VISIBLE
                        itemMsgR.text = textArr[0]
                        itemReceiverName.text = bean.getChatRoomMsgs().client_name
                        itemReceiverDateTime.text = bean.getChatRoomMsgs().created_at
                        itemMsgR.backgroundTintList =
                            ContextCompat.getColorStateList(context, R.color.lightestGreyColor)
                    } else {
                        layoutMsgLeft.visibility = View.VISIBLE
                        itemMsgL.text = textArr[0]
                        itemSenderName.text = bean.getChatRoomMsgs().client_name
                        itemSenderDateTime.text = bean.getChatRoomMsgs().created_at
                        itemMsgL.backgroundTintList =
                            ContextCompat.getColorStateList(context, R.color.lightestGreyColor)
                    }
                }
            }

            if (bean.isBeSelf()) {
                if (selfPhoto != "null" && selfPhoto.isNotEmpty()) {
                    imgUserPicRight.visibility = View.VISIBLE
                    itemNameR.visibility = View.GONE
                    Glide.with(context)
                        .load(BaseActivity.baseURL.dropLast(5) + selfPhoto)
                        .placeholder(R.drawable.user_pic)
                        .transform(CenterCrop(), RoundedCorners(5))
                        .into(imgUserPicRight)
                } else {
                    itemNameR.text = bean.getAccount()!!.substring(0, 1).uppercase()
                    imgUserPicRight.visibility = View.GONE
                    itemNameR.visibility = View.VISIBLE
                }
            } else {
                if (bean.getChatRoomMsgs().client_photo != "null" && bean.getChatRoomMsgs().client_photo.isNotEmpty()) {
                    imgUserPicLeft.visibility = View.VISIBLE
                    itemNameL.visibility = View.GONE
                    Glide.with(context)
                        .load(BaseActivity.baseURL.dropLast(5) + bean.getChatRoomMsgs().client_photo)
                        .placeholder(R.drawable.user_pic)
                        .transform(CenterCrop(), RoundedCorners(5))
                        .into(imgUserPicLeft)
                } else {
                    imgUserPicLeft.visibility = View.GONE
                    itemNameL.visibility = View.VISIBLE
                    itemNameL.text = bean.getAccount()!!.substring(0, 1).uppercase()
                    if (bean.getBackground() !== 0) {
                        itemNameL.setBackgroundResource(bean.getBackground())
                    }
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

    inner class ViewHolder(val binding: ChatRoomMsgItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)
}