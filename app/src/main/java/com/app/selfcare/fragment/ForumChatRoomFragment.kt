package com.app.selfcare.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.BaseActivity
import com.app.selfcare.BuildConfig
import com.app.selfcare.R
import com.app.selfcare.adapters.ChatRoomMsgAdapter
import com.app.selfcare.adapters.MessageAdapter
import com.app.selfcare.controller.OnBottomReachedListener
import com.app.selfcare.controller.OnChatRoomMessageClickListener
import com.app.selfcare.controller.OnMessageClickListener
import com.app.selfcare.data.ChatMsg
import com.app.selfcare.data.ChatRoomMsgBean
import com.app.selfcare.data.ChatRoomMsgs
import com.app.selfcare.data.ForumData
import com.app.selfcare.data.PrevMsg
import com.app.selfcare.databinding.FragmentForumChatRoomBinding
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper.get
import com.app.selfcare.utils.ChatRoomMsgUtil
import com.app.selfcare.utils.Utils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import io.agora.rtm.ErrorInfo
import io.agora.rtm.ResultCallback
import io.agora.rtm.RtmChannel
import io.agora.rtm.RtmChannelAttribute
import io.agora.rtm.RtmChannelListener
import io.agora.rtm.RtmChannelMember
import io.agora.rtm.RtmClient
import io.agora.rtm.RtmClientListener
import io.agora.rtm.RtmFileMessage
import io.agora.rtm.RtmImageMessage
import io.agora.rtm.RtmMediaOperationProgress
import io.agora.rtm.RtmMessage
import io.agora.rtm.RtmStatusCode
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject
import retrofit2.HttpException
import java.util.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ForumChatRoomFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ForumChatRoomFragment : BaseFragment(), OnChatRoomMessageClickListener,
    OnBottomReachedListener {
    // TODO: Rename and change types of parameters
    private var forumData: ForumData? = null
    private var rtmToken: String? = null
    private lateinit var binding: FragmentForumChatRoomBinding
    private var mRtmClient: RtmClient? = null
    private var mRtmChannel: RtmChannel? = null
    private var mIsPeerToPeerMode = false
    private var mPeerId = ""
    private var mChannelName = ""
    private var mChannelMemberCount = 1

    private val mMessageBeanList: ArrayList<ChatRoomMsgBean> = ArrayList()
    private var mMessageAdapter: ChatRoomMsgAdapter? = null
    private var mChatRoomMsgs: ArrayList<ChatRoomMsgs> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            forumData = it.getParcelable(ARG_PARAM1)
            rtmToken = it.getString(ARG_PARAM2)
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_forum_chat_room
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentForumChatRoomBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE

        binding.textChatRoomBack.setOnClickListener {
            endChatRoom()
        }

        try {
            val photo = preference!![PrefKeys.PREF_PHOTO, ""]!!
            if (photo != "null" && photo.isNotEmpty()) {
                binding.imgChatRoom.visibility = View.VISIBLE
                binding.txtChatRoom.visibility = View.GONE
                Glide.with(requireActivity())
                    .load(BaseActivity.baseURL.dropLast(5) + photo)
                    .placeholder(R.drawable.user_pic)
                    .transform(CenterCrop(), RoundedCorners(5))
                    .into(binding.imgChatRoom)
            } else {
                binding.imgChatRoom.visibility = View.GONE
                binding.txtChatRoom.visibility = View.VISIBLE
                val userTxt = preference!![PrefKeys.PREF_FNAME, ""]!!
                if (userTxt.isNotEmpty()) {
                    binding.txtChatRoom.text = userTxt.substring(0, 1).uppercase()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        binding.txtChatRoomtName.text = forumData!!.name
        binding.txtChatRoomDescription.text = forumData!!.description

        binding.layoutChatRoomSend.setOnClickListener {
            val msg: String = getText(binding.editTextChatRoomMessage)
            if (msg != "") {
                val message = mRtmClient!!.createMessage()
                message.text = msg
                val patientName =
                    preference!![PrefKeys.PREF_FNAME, ""]!!.take(1) + "" + preference!![PrefKeys.PREF_LNAME, ""]!!.take(
                        1
                    )

                val chatRoomMsg = ChatRoomMsgs(
                    0,
                    patientName,
                    preference!![PrefKeys.PREF_PHOTO, ""]!!,
                    0,
                    msg,
                    "",
                    "",
                    preference!![PrefKeys.PREF_PATIENT_ID, 0]!!,
                    forumData!!.id
                )
                val index: Int = if (mChatRoomMsgs.size == 0) 0 else mChatRoomMsgs.size - 1
                mChatRoomMsgs.add(index, chatRoomMsg)
                val messageBean = ChatRoomMsgBean(patientName, message, true, chatRoomMsg)
                mMessageBeanList.add(messageBean)
                mMessageAdapter!!.notifyItemRangeChanged(mMessageBeanList.size, 1)
                binding.chatRoomMessageList.scrollToPosition(mMessageBeanList.size - 1)
                sendChannelMessage(message)
            }
            binding.editTextChatRoomMessage.setText("")
        }

        mRtmClient = RtmClient.createInstance(
            requireActivity(),
            BuildConfig.appId,
            object : RtmClientListener {
                override fun onConnectionStateChanged(state: Int, reason: Int) {
                    runOnUiThread {
                        when (state) {
                            RtmStatusCode.ConnectionState.CONNECTION_STATE_RECONNECTING -> showToast(
                                getString(R.string.reconnecting)
                            )

                            RtmStatusCode.ConnectionState.CONNECTION_STATE_ABORTED -> {
                                showToast(getString(R.string.account_offline))
                                requireActivity().setResult(ChatRoomMsgUtil.ACTIVITY_RESULT_CONN_ABORTED)
                                popBackStack()
                            }
                        }
                    }
                }

                override fun onMessageReceived(message: RtmMessage?, peerId: String?) {
                    runOnUiThread {
                        var isFound = false
                        var roomMsg = ChatRoomMsgs()
                        for (chatRoomMsg in mChatRoomMsgs) {
                            if (peerId!!.toInt() == chatRoomMsg.user_id) {
                                isFound = true
                                roomMsg = ChatRoomMsgs(
                                    chatRoomMsg.id,
                                    chatRoomMsg.client_name,
                                    chatRoomMsg.client_photo,
                                    chatRoomMsg.user_id,
                                    message!!.text,
                                    "",
                                    "",
                                    chatRoomMsg.client,
                                    chatRoomMsg.chat_room
                                )
                                break
                            }
                        }
                        if (!isFound) {
                            getUserDetails(peerId!!.toInt()) { response ->
                                val jsonObject = JSONObject(response)
                                roomMsg = ChatRoomMsgs(
                                    0,
                                    jsonObject.getString("name"),
                                    jsonObject.getString("photo"),
                                    peerId!!.toInt(),
                                    message!!.text,
                                    "",
                                    "",
                                    peerId.toInt(),
                                    forumData!!.id
                                )
                                val index: Int =
                                    if (mChatRoomMsgs.size == 0) 0 else mChatRoomMsgs.size - 1
                                mChatRoomMsgs.add(index, roomMsg)
                                val messageBean = ChatRoomMsgBean(roomMsg.client_name, message, false, roomMsg)
                                messageBean.setBackground(getMessageColor(roomMsg.client_name))
                                mMessageBeanList.add(messageBean)
                                mMessageAdapter!!.notifyItemRangeChanged(mMessageBeanList.size, 1)
                                binding.chatRoomMessageList.scrollToPosition(mMessageBeanList.size - 1)
                            }
                        } else {
                            val index: Int =
                                if (mChatRoomMsgs.size == 0) 0 else mChatRoomMsgs.size - 1
                            mChatRoomMsgs.add(index, roomMsg)
                            val messageBean = ChatRoomMsgBean(peerId, message, false, roomMsg)
                            messageBean.setBackground(getMessageColor(peerId!!))
                            mMessageBeanList.add(messageBean)
                            mMessageAdapter!!.notifyItemRangeChanged(mMessageBeanList.size, 1)
                            binding.chatRoomMessageList.scrollToPosition(mMessageBeanList.size - 1)
                        }
                    }
                }

                override fun onImageMessageReceivedFromPeer(p0: RtmImageMessage?, p1: String?) {
                }

                override fun onFileMessageReceivedFromPeer(p0: RtmFileMessage?, p1: String?) {
                    Log.i("File received", "Hurrray ")
                }

                override fun onMediaUploadingProgress(p0: RtmMediaOperationProgress?, p1: Long) {
                }

                override fun onMediaDownloadingProgress(p0: RtmMediaOperationProgress?, p1: Long) {
                }

                override fun onTokenExpired() {
                }

                override fun onPeersOnlineStatusChanged(p0: MutableMap<String, Int>?) {
                }

            })

        doLogin()
    }

    private var nextPage = ""

    private fun getPreviousMessages() {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .getPreviousMsgs(
                        "PI0073",
                        PrevMsg(forumData!!.id.toString()),
                        getAccessToken()
                    )
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ result ->
                        try {
                            hideProgress()
                            var responseBody = result.string()
                            Log.d("Response Body", responseBody)
                            val respBody = responseBody.split("|")
                            val status = respBody[1]
                            responseBody = respBody[0]
                            val jsonObj = JSONObject(responseBody)
                            val count = jsonObj.getString("count")
                            var nextPagePart = jsonObj.getString("next").split("?")
                            nextPagePart = nextPagePart[1].split("&")
                            nextPage = nextPagePart[0].drop(5)
                            val jsonArr = jsonObj.getJSONArray("results")
                            for (i in 0 until jsonArr.length()) {
                                val peerId = jsonArr.getJSONObject(i).getString("client")
                                val message = mRtmClient!!.createMessage()
                                message.text = jsonArr.getJSONObject(i).getString("message")
                                val chatRoomMsgs = ChatRoomMsgs(
                                    jsonArr.getJSONObject(i).getInt("id"),
                                    jsonArr.getJSONObject(i).getString("client_name"),
                                    jsonArr.getJSONObject(i).getString("client_photo"),
                                    jsonArr.getJSONObject(i).getInt("user_id"),
                                    jsonArr.getJSONObject(i).getString("message"),
                                    jsonArr.getJSONObject(i).getString("created_at"),
                                    jsonArr.getJSONObject(i).getString("updated_at"),
                                    jsonArr.getJSONObject(i).getInt("client"),
                                    jsonArr.getJSONObject(i).getInt("chat_room")
                                )
                                mChatRoomMsgs.add(chatRoomMsgs)
                                if (preference!![PrefKeys.PREF_PATIENT_ID, ""] == peerId) {
                                    val messageBean =
                                        ChatRoomMsgBean(chatRoomMsgs.client_name, message, true, chatRoomMsgs)
                                    messageBean.setBackground(getMessageColor(chatRoomMsgs.client_name))
                                    mMessageBeanList.add(messageBean)
                                } else {
                                    val messageBean =
                                        ChatRoomMsgBean(chatRoomMsgs.client_name, message, false, chatRoomMsgs)
                                    messageBean.setBackground(getMessageColor(chatRoomMsgs.client_name))
                                    mMessageBeanList.add(messageBean)
                                }
                            }
                            mMessageBeanList.reverse()
                            mMessageAdapter!!.notifyItemRangeChanged(mMessageBeanList.size, 1)
                            binding.chatRoomMessageList.scrollToPosition(mMessageBeanList.size - 1)
                        } catch (e: Exception) {
                            hideProgress()
                            e.printStackTrace()
                            displayToast("Something went wrong.. Please try after sometime")
                        }
                    }, { error ->
                        hideProgress()
                        if ((error as HttpException).code() == 401) {
                            clearCache()
                        } else {
                            displayAfterLoginErrorMsg(error)
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    private fun getPrevPagePreviousMessages(page: String) {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .getNextPreviousMsgs(
                        page,
                        "PI0073",
                        PrevMsg(forumData!!.id.toString()),
                        getAccessToken()
                    )
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ result ->
                        try {
                            hideProgress()
                            var responseBody = result.string()
                            Log.d("Response Body", responseBody)
                            val respBody = responseBody.split("|")
                            val status = respBody[1]
                            responseBody = respBody[0]
                            val jsonObj = JSONObject(responseBody)
                            val count = jsonObj.getString("count")
                            val preListCount = mChatRoomMsgs.size
                            if (jsonObj.getString("next") != "null") {
                                var nextPagePart = jsonObj.getString("next").split("?")
                                nextPagePart = nextPagePart[1].split("&")
                                nextPage = nextPagePart[0].drop(5)
                            } else {
                                nextPage = ""
                            }
                            val jsonArr = jsonObj.getJSONArray("results")
                            for (i in 0 until jsonArr.length()) {
                                val peerId = jsonArr.getJSONObject(i).getString("client")
                                val message = mRtmClient!!.createMessage()
                                message.text = jsonArr.getJSONObject(i).getString("message")
                                val chatRoomMsgs = ChatRoomMsgs(
                                    jsonArr.getJSONObject(i).getInt("id"),
                                    jsonArr.getJSONObject(i).getString("client_name"),
                                    jsonArr.getJSONObject(i).getString("client_photo"),
                                    jsonArr.getJSONObject(i).getInt("user_id"),
                                    jsonArr.getJSONObject(i).getString("message"),
                                    jsonArr.getJSONObject(i).getString("created_at"),
                                    jsonArr.getJSONObject(i).getString("updated_at"),
                                    jsonArr.getJSONObject(i).getInt("client"),
                                    jsonArr.getJSONObject(i).getInt("chat_room")
                                )
                                mChatRoomMsgs.add(0, chatRoomMsgs)
                                if (preference!![PrefKeys.PREF_PATIENT_ID, ""] == peerId) {
                                    val messageBean =
                                        ChatRoomMsgBean(peerId, message, true, chatRoomMsgs)
                                    messageBean.setBackground(getMessageColor(peerId))
                                    mMessageBeanList.add(0, messageBean)
                                } else {
                                    val messageBean =
                                        ChatRoomMsgBean(peerId, message, false, chatRoomMsgs)
                                    messageBean.setBackground(getMessageColor(peerId))
                                    mMessageBeanList.add(0, messageBean)
                                }
                            }
                            mMessageAdapter!!.notifyItemRangeInserted(0, mChatRoomMsgs.size - preListCount)
                            //binding.chatRoomMessageList.scrollToPosition(mMessageBeanList.size - 1)
                        } catch (e: Exception) {
                            hideProgress()
                            e.printStackTrace()
                            displayToast("Something went wrong.. Please try after sometime")
                        }
                    }, { error ->
                        hideProgress()
                        if ((error as HttpException).code() == 401) {
                            clearCache()
                        } else {
                            displayAfterLoginErrorMsg(error)
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    private fun sendChatRoomMsg(message: String) {
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .sendChatRoomMsg(
                        "PI0073",
                        ChatMsg(
                            preference!![PrefKeys.PREF_PATIENT_ID, ""]!!,
                            message,
                            forumData!!.name
                        ),
                        getAccessToken()
                    )
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ result ->
                        try {
                            hideProgress()
                            var responseBody = result.string()
                            Log.d("Response Body", responseBody)
                            val respBody = responseBody.split("|")
                            val status = respBody[1]
                            responseBody = respBody[0]
                            Log.i("Chat Room", "Chat message sent successfully")
                        } catch (e: Exception) {
                            hideProgress()
                            e.printStackTrace()
                            displayToast("Something went wrong.. Please try after sometime")
                        }
                    }, { error ->
                        hideProgress()
                        if ((error as HttpException).code() == 401) {
                            clearCache()
                        } else {
                            displayAfterLoginErrorMsg(error)
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    private fun endChatRoom() {
        val builder = AlertDialog.Builder(mActivity!!)
        builder.setTitle("Confirmation")
        builder.setMessage("Do you wish to close this Forum?")
        builder.setPositiveButton("Yes") { dialog, which ->
            dialog.dismiss()
            doLogout()
            popBackStack()
        }
        builder.setNegativeButton("No") { dialog, which ->
            dialog.dismiss()
        }
        builder.setCancelable(false)
        builder.show()
    }

    private fun doLogin() {
        try {
            if (rtmToken!!.isNotEmpty()) {
                mRtmClient!!.login(
                    rtmToken!!,
                    preference!![PrefKeys.PREF_USER_ID, "0"]!!,
                    object : ResultCallback<Void?> {
                        override fun onSuccess(responseInfo: Void?) {
                            Utils.rtmLoggedIn = true
                            Log.i(VideoCallFragment.TAG, "login success")
                            runOnUiThread {
                                try {
                                    init()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }

                        override fun onFailure(errorInfo: ErrorInfo) {
                            val text: CharSequence =
                                "User: failed to log in to Signaling!$errorInfo"
                            Log.i(
                                VideoCallFragment.TAG,
                                "login failed: " + errorInfo.errorDescription
                            )
                            runOnUiThread {
                                displayToast(text.toString())
                            }
                        }
                    })
            } else {
                displayToast("RTM token is empty...")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun init() {
        mChannelName = forumData!!.name
        mChannelMemberCount = 1
        //binding.txtTextAppointName.text = mChannelName
        /*onlineChatView!!.txtTherapistChatName.text = MessageFormat.format(
            "{0}({1})",
            mChannelName,
            mChannelMemberCount
        )*/
        createAndJoinChannel()

        runOnUiThread {
            val layoutManager =
                LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
            layoutManager.orientation = RecyclerView.VERTICAL
            mMessageAdapter = ChatRoomMsgAdapter(
                requireActivity(),
                mMessageBeanList,
                preference!![PrefKeys.PREF_PHOTO, ""]!!,
                this,
                this
            )
            binding.chatRoomMessageList.layoutManager = layoutManager
            binding.chatRoomMessageList.adapter = mMessageAdapter
        }
        getPreviousMessages()
    }

    private fun createAndJoinChannel() {
        // step 1: create a channel instance
        mRtmChannel = mRtmClient!!.createChannel(forumData!!.name, myChannelListener)
        if (mRtmChannel == null) {
            showToast(getString(R.string.join_channel_failed))
            popBackStack()
            return
        }
        Log.e("channel", mRtmChannel.toString() + "")

        // step 2: join the channel
        mRtmChannel!!.join(object : ResultCallback<Void?> {
            override fun onSuccess(responseInfo: Void?) {
                Log.i(TextAppointmentFragment.TAG, "join channel success")
                runOnUiThread {
                    //showToast("join channel success")
                }
                getChannelMemberList()
            }

            override fun onFailure(errorInfo: ErrorInfo) {
                Log.e(TextAppointmentFragment.TAG, "join channel failed")
                runOnUiThread {
                    showToast(getString(R.string.join_channel_failed))
                    showToast(errorInfo.errorDescription + " - " + errorInfo.errorCode)
                    popBackStack()
                }
            }
        })
    }

    private fun getChannelMemberList() {
        mRtmChannel!!.getMembers(object : ResultCallback<List<RtmChannelMember?>> {
            override fun onSuccess(responseInfo: List<RtmChannelMember?>) {
                runOnUiThread {
                    mChannelMemberCount = responseInfo.size
                    refreshChannelTitle()
                }
            }

            override fun onFailure(errorInfo: ErrorInfo) {
                Log.e(
                    TextAppointmentFragment.TAG,
                    "failed to get channel members, err: " + errorInfo.errorCode
                )
            }
        })
    }

    private fun sendChannelMessage(message: RtmMessage) {
        mRtmChannel!!.sendMessage(message, object : ResultCallback<Void?> {
            override fun onSuccess(aVoid: Void?) {
                runOnUiThread {
                    sendChatRoomMsg(message.text)
                    //showToast("Message sent successfully...")
                }
            }

            override fun onFailure(errorInfo: ErrorInfo) {
                // refer to RtmStatusCode.ChannelMessageState for the message state
                val errorCode = errorInfo.errorCode
                runOnUiThread {
                    when (errorCode) {
                        RtmStatusCode.ChannelMessageError.CHANNEL_MESSAGE_ERR_TIMEOUT,
                        RtmStatusCode.ChannelMessageError.CHANNEL_MESSAGE_ERR_FAILURE ->
                            showToast(getString(R.string.send_msg_failed))

                        else -> showToast(errorInfo.errorDescription + " - " + errorInfo.errorCode)
                    }
                }
            }
        })
    }

    private fun leaveAndReleaseChannel() {
        if (mRtmChannel != null) {
            mRtmChannel!!.leave(null)
            mRtmChannel!!.release()
            mRtmChannel = null
        }
    }

    private val myChannelListener = object : RtmChannelListener {
        override fun onMemberCountUpdated(i: Int) {}
        override fun onAttributesUpdated(list: List<RtmChannelAttribute>) {}
        override fun onMessageReceived(message: RtmMessage, fromMember: RtmChannelMember) {
            runOnUiThread {
                var isFound = false
                var roomMsg = ChatRoomMsgs()
                for (chatRoomMsg in mChatRoomMsgs) {
                    if (fromMember.userId.toInt() == chatRoomMsg.user_id) {
                        isFound = true
                        roomMsg = ChatRoomMsgs(
                            chatRoomMsg.id,
                            chatRoomMsg.client_name,
                            chatRoomMsg.client_photo,
                            chatRoomMsg.user_id,
                            message.text,
                            "",
                            "",
                            chatRoomMsg.client,
                            chatRoomMsg.chat_room
                        )
                        break
                    }
                }
                if (!isFound) {
                    getUserDetails(fromMember.userId!!.toInt()) { response ->
                        val jsonObject = JSONObject(response)
                        roomMsg = ChatRoomMsgs(
                            0,
                            jsonObject.getString("name"),
                            jsonObject.getString("photo"),
                            fromMember.userId!!.toInt(),
                            message.text,
                            "",
                            "",
                            fromMember.userId.toInt(),
                            forumData!!.id
                        )
                        val index: Int =
                            if (mChatRoomMsgs.size == 0) 0 else mChatRoomMsgs.size - 1
                        mChatRoomMsgs.add(index, roomMsg)
                        val messageBean = ChatRoomMsgBean(roomMsg.client_name, message, false, roomMsg)
                        messageBean.setBackground(getMessageColor(roomMsg.client_name))
                        mMessageBeanList.add(messageBean)
                        mMessageAdapter!!.notifyItemRangeChanged(mMessageBeanList.size, 1)
                        binding.chatRoomMessageList.scrollToPosition(mMessageBeanList.size - 1)
                    }
                } else {
                    val index: Int =
                        if (mChatRoomMsgs.size == 0) 0 else mChatRoomMsgs.size - 1
                    mChatRoomMsgs.add(index, roomMsg)
                    val messageBean = ChatRoomMsgBean(roomMsg.client_name, message, false, roomMsg)
                    messageBean.setBackground(getMessageColor(roomMsg.client_name))
                    mMessageBeanList.add(messageBean)
                    mMessageAdapter!!.notifyItemRangeChanged(mMessageBeanList.size, 1)
                    binding.chatRoomMessageList.scrollToPosition(mMessageBeanList.size - 1)
                }


                /*val account = fromMember.userId
                Log.i(
                    TextAppointmentFragment.TAG,
                    "onMessageReceived account = $account msg = $message"
                )
                mPeerId = account
                val messageBean = ChatRoomMsgBean(account, message, false)
                messageBean.setBackground(getMessageColor(account))
                mMessageBeanList.add(messageBean)
                mMessageAdapter!!.notifyItemRangeChanged(mMessageBeanList.size, 1)
                binding.chatRoomMessageList.scrollToPosition(mMessageBeanList.size - 1)*/
            }
        }

        override fun onImageMessageReceived(p0: RtmImageMessage?, p1: RtmChannelMember?) {

        }

        override fun onFileMessageReceived(p0: RtmFileMessage?, p1: RtmChannelMember?) {
            Log.i("File received", "Hurrray ")
        }

        override fun onMemberJoined(member: RtmChannelMember) {
            runOnUiThread {
                mChannelMemberCount++
                refreshChannelTitle()
            }
        }

        override fun onMemberLeft(member: RtmChannelMember) {
            runOnUiThread {
                mChannelMemberCount--
                refreshChannelTitle()
            }
        }
    }

    private fun getMessageColor(account: String): Int {
        for (i in mMessageBeanList.indices) {
            if (account == mMessageBeanList[i].getAccount()) {
                return mMessageBeanList[i].getBackground()
            }
        }
        return ChatRoomMsgUtil.COLOR_ARRAY[ChatRoomMsgUtil.RANDOM.nextInt(ChatRoomMsgUtil.COLOR_ARRAY.size)]
    }

    private fun refreshChannelTitle() {
        val titleFormat = getString(R.string.channel_title)
        val title = String.format(titleFormat, mChannelName, mChannelMemberCount)
        //binding.txtTextAppointName.text = mChannelName
    }

    private fun showToast(text: String) {
        runOnUiThread {
            Toast.makeText(
                requireActivity(),
                text,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun doLogout() {
        try {
            leaveAndReleaseChannel()
            if (mRtmClient != null) {
                mRtmClient!!.logout(null)
                ChatRoomMsgUtil.cleanMessageListBeanList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ForumChatRoomFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: ForumData, param2: String) =
            ForumChatRoomFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_Forum_Chat_Room"
    }

    override fun onItemClick(message: ChatRoomMsgBean?) {

    }

    override fun onBottomReached(position: Int) {
        if (nextPage.isNotEmpty())
            getPrevPagePreviousMessages(nextPage)
    }
}