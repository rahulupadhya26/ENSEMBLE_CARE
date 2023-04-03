package com.app.selfcare.fragment

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.BaseActivity
import com.app.selfcare.BuildConfig
import com.app.selfcare.R
import com.app.selfcare.adapters.ChatMessageAdapter
import com.app.selfcare.adapters.MessageAdapter
import com.app.selfcare.controller.OnChatMessageClickListener
import com.app.selfcare.controller.OnMessageClickListener
import com.app.selfcare.data.ChatMessages
import com.app.selfcare.data.GetAppointment
import com.app.selfcare.data.MessageBean
import com.app.selfcare.data.MessageListBean
import com.app.selfcare.databinding.FragmentActivityCarePlanBinding
import com.app.selfcare.databinding.FragmentTextAppointmentBinding
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper.get
import com.app.selfcare.realTimeMessaging.ChatManager
import com.app.selfcare.services.SelfCareApplication
import com.app.selfcare.utils.DateUtils
import com.app.selfcare.utils.MessageUtil
import com.app.selfcare.utils.Utils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import io.agora.CallBack
import io.agora.ConnectionListener
import io.agora.chat.*
import io.agora.rtm.*
import java.text.SimpleDateFormat
import java.util.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val ARG_PARAM3 = "param3"
private const val ARG_PARAM4 = "param4"

/**
 * A simple [Fragment] subclass.
 * Use the [TextAppointmentFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TextAppointmentFragment : BaseFragment(), OnMessageClickListener, OnChatMessageClickListener {
    // TODO: Rename and change types of parameters
    private var appointment: GetAppointment? = null
    private var RTC_TOKEN: String? = null
    private var RTM_TOKEN: String? = null
    private var CHANNEL_NAME: String? = null
    private var actualStartTime: String? = null
    private var actualEndTime: String? = null
    private var duration: String? = null

    private var mChatManager: ChatManager? = null
    private var mRtmClient: RtmClient? = null
    private var mClientListener: RtmClientListener? = null
    private var mRtmChannel: RtmChannel? = null

    //private var mChatClient: ChatClient? = null
    private var mIsPeerToPeerMode = false
    private var mPeerId = ""
    private var mChannelName = ""
    private var mChannelMemberCount = 1

    private val mMessageBeanList: ArrayList<MessageBean> = ArrayList()
    private val mMessageList: ArrayList<ChatMessages> = ArrayList()
    private var mMessageAdapter: MessageAdapter? = null
    private var mChatMessageAdapter: ChatMessageAdapter? = null

    // Number of seconds displayed
    // on the stopwatch.
    private var seconds = 0

    // Is the stopwatch running?
    private var running = false

    private var wasRunning = false

    private var time: String = ""
    var mFileMessage: ChatMessage? = null
    private val APP_ID = BuildConfig.appId

    private lateinit var binding: FragmentTextAppointmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            appointment = it.getParcelable(ARG_PARAM1)
            RTC_TOKEN = it.getString(ARG_PARAM2)
            RTM_TOKEN = it.getString(ARG_PARAM3)
            CHANNEL_NAME = it.getString(ARG_PARAM4)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTextAppointmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getLayout(): Int {
        return R.layout.fragment_text_appointment
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE

        binding.textAppointBack.setOnClickListener {
            endVideoCall()
        }

        try {
            val photo = preference!![PrefKeys.PREF_PHOTO, ""]!!
            if (photo != "null" && photo.isNotEmpty()) {
                binding.imgTxtAppoint.visibility = View.VISIBLE
                binding.txtTxtAppoint.visibility = View.GONE
                Glide.with(requireActivity())
                    .load(BaseActivity.baseURL.dropLast(5) + photo)
                    .placeholder(R.drawable.user_pic)
                    .transform(CenterCrop(), RoundedCorners(5))
                    .into(binding.imgTxtAppoint)
            } else {
                binding.imgTxtAppoint.visibility = View.GONE
                binding.txtTxtAppoint.visibility = View.VISIBLE
                val userTxt = preference!![PrefKeys.PREF_FNAME, ""]!!
                if (userTxt.isNotEmpty()) {
                    binding.txtTxtAppoint.text = userTxt.substring(0, 1).uppercase()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        var dateTime = DateUtils(appointment!!.appointment.date + " " + "00:00:00")
        if (appointment!!.appointment.booking_date != null) {
            dateTime = DateUtils(appointment!!.appointment.booking_date + " " + "00:00:00")
        }
        binding.txtTextAppointStartTime.text =
            appointment!!.appointment.time_slot.starting_time.dropLast(3)
        binding.txtTextAppointStartDate.text =
            dateTime.getDay3LettersName() + ", " + dateTime.getMonth() + " " + dateTime.getDay()
        binding.txtTextAppointEndTime.text =
            appointment!!.appointment.time_slot.ending_time.dropLast(3)
        binding.txtTextAppointEndDate.text =
            dateTime.getDay3LettersName() + ", " + dateTime.getMonth() + " " + dateTime.getDay()

        reverseTimer((appointment!!.duration * 60).toLong(), binding.txtTextAppointTimeTaken)

        binding.imgAddFile.setOnClickListener {
            var chooseFile = Intent(Intent.ACTION_GET_CONTENT)
            chooseFile.type = "*/*"
            chooseFile = Intent.createChooser(chooseFile, "Choose a file")
            startActivityForResult(chooseFile, PICKFILE_REQUEST_CODE)
        }

        binding.layoutTextAppointSend.setOnClickListener {
            val msg: String = getText(binding.editTextTextAppointMessage)
            if (msg != "") {
                /*mPeerId = appointment!!.doctor_first_name + " " + appointment!!.doctor_last_name
                val clientName =
                    preference!![PrefKeys.PREF_FNAME, ""]!!.take(1) + "" + preference!![PrefKeys.PREF_LNAME, ""]!!.take(
                        1
                    )
                val messageBean = ChatMessages(ChatMessage.Type.TXT, msg, clientName, true)
                mMessageList.add(messageBean)
                mChatMessageAdapter!!.notifyItemRangeChanged(mMessageList.size, 1)
                binding.textAppointMessageList.scrollToPosition(mMessageList.size - 1)
                val message = ChatMessage.createTextSendMessage(msg, mPeerId)
                message.setMessageStatusCallback(object : CallBack {
                    override fun onSuccess() {
                    }

                    override fun onError(code: Int, error: String?) {
                    }

                })
                ChatClient.getInstance().chatManager().sendMessage(message)*/
                val message = mRtmClient!!.createMessage()
                message.text = msg
                val patientName =
                    preference!![PrefKeys.PREF_FNAME, ""]!!.take(1) + "" + preference!![PrefKeys.PREF_LNAME, ""]!!.take(
                        1
                    )
                val messageBean = MessageBean(patientName, message, true)
                mMessageBeanList.add(messageBean)
                mMessageAdapter!!.notifyItemRangeChanged(mMessageBeanList.size, 1)
                binding.textAppointMessageList.scrollToPosition(mMessageBeanList.size - 1)
                if (mIsPeerToPeerMode) {
                    sendPeerMessage(message)
                } else {
                    sendChannelMessage(message)
                }
            }
            binding.editTextTextAppointMessage.setText("")
        }
        /*val options = ChatOptions()
        options.appKey = APP_ID
        ChatClient.getInstance().init(requireActivity(), options)
        loginToAgora()*/

        mChatManager = SelfCareApplication.instance.getChatManager()
        mRtmClient = mChatManager!!.getRtmClient()
        if (!Utils.rtmLoggedIn) {
            doLogin()
        }
    }

    private fun initListener() {
        // Adds message event callbacks.
        ChatClient.getInstance().chatManager().addMessageListener { messages: List<ChatMessage> ->
            mMessageBeanList.clear()
            val targetName =
                appointment!!.doctor_first_name.take(1) + "" + appointment!!.doctor_last_name.take(
                    1
                )
            for (message in messages) {
                val account = message.from
                var messageBean: ChatMessages? = null
                when (message.type) {
                    ChatMessage.Type.TXT -> {
                        messageBean = ChatMessages(
                            message.type,
                            (message.body as TextMessageBody).message,
                            message.from, false
                        )
                    }
                    else -> {
                        val callBack: CallBack = object : CallBack {
                            @Override
                            override fun onSuccess() {
                                // Download successfully
                                // After the download finishes, get the URI of the local file.
                                val fileMessageBody = message.body as NormalFileMessageBody
                                val fileRemoteUrl = fileMessageBody.remoteUrl
                                val fileLocalUri = fileMessageBody.localUri
                                messageBean = ChatMessages(
                                    message.type,
                                    fileLocalUri.toString(),
                                    message.from, false
                                )
                            }

                            override fun onProgress(progress: Int, status: String) {
                                // Show progress
                            }

                            override fun onError(code: Int, error: String) {
                                // Download failed
                            }
                        }
                        message.setMessageStatusCallback(callBack)
                        ChatClient.getInstance().chatManager().downloadAttachment(message)
                    }
                }
                messageBean!!.setBackground(getMessageColor(account))
                mMessageList.add(messageBean!!)
                mChatMessageAdapter!!.notifyItemRangeChanged(mMessageList.size, 1)
                binding.textAppointMessageList.scrollToPosition(mMessageList.size - 1)
            }

            /*for (message in messages) {
                val builder = StringBuilder()
                builder.append("Receive a ").append(message.type.name)
                    .append(" message from: ").append(message.from)
                if (message.type == ChatMessage.Type.TXT) {
                    builder.append(" content:")
                        .append((message.body as TextMessageBody).message)
                }
            }*/
        }

        // Adds connection event callbacks.
        ChatClient.getInstance().addConnectionListener(object : ConnectionListener {
            override fun onConnected() {
                //showLog("onConnected", false)
            }

            override fun onDisconnected(error: Int) {
                //showLog("onDisconnected: $error", false)
            }

            override fun onLogout(errorCode: Int) {
                //showLog("User needs to log out: $errorCode", false)
                ChatClient.getInstance().logout(false, null)
            }

            // This callback occurs when the token expires. When the callback is triggered, the app client must get a new token from the app server and logs in to the app again.
            override fun onTokenExpired() {
                //showLog("ConnectionListener onTokenExpired", true)
            }

            // This callback occurs when the token is about to expire.
            override fun onTokenWillExpire() {
                //showLog("ConnectionListener onTokenWillExpire", true)
            }
        })
    }

    private fun loginToAgora() {
        ChatClient.getInstance().loginWithAgoraToken(
            preference!![PrefKeys.PREF_EMAIL, ""],
            RTM_TOKEN,
            object : CallBack {
                override fun onSuccess() {
                    //showLog("Sign in success!", true)
                    val layoutManager = LinearLayoutManager(requireActivity())
                    layoutManager.orientation = RecyclerView.VERTICAL
                    mChatMessageAdapter = ChatMessageAdapter(
                        requireActivity(),
                        mMessageList,
                        this@TextAppointmentFragment
                    )
                    binding.textAppointMessageList.layoutManager = layoutManager
                    binding.textAppointMessageList.adapter = mMessageAdapter
                    initListener()
                }

                override fun onError(code: Int, error: String) {
                    Log.i("Error $code", error)
                    //showLog(error, true)
                }
            })
    }

    // Logs out.
    private fun signOut() {
        if (ChatClient.getInstance().isLoggedInBefore) {
            ChatClient.getInstance().logout(true, object : CallBack {
                override fun onSuccess() {
                    MessageUtil.cleanMessageListBeanList()
                    //showLog("Sign out success!", true)
                }

                override fun onError(code: Int, error: String) {
                    //showLog(error, true)
                }
            })
        } else {
            //showLog("You were not logged in", false)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICKFILE_REQUEST_CODE && resultCode == RESULT_OK) {
            // Get the file's content URI from the incoming Intent
            val fileLocalUri: Uri = data!!.data!!
            val clientName =
                preference!![PrefKeys.PREF_FNAME, ""]!!.take(1) + "" + preference!![PrefKeys.PREF_LNAME, ""]!!.take(
                    1
                )

            /*val messageBean =
                ChatMessages(ChatMessage.Type.FILE, fileLocalUri.toString(), clientName, true)
            mMessageList.add(messageBean)
            mChatMessageAdapter!!.notifyItemRangeChanged(mMessageList.size, 1)
            binding.textAppointMessageList.scrollToPosition(mMessageList.size - 1)
            // Set fileLocalUri as the URI of the file message on the local device.
            mFileMessage = ChatMessage.createFileSendMessage(fileLocalUri, mPeerId)
            // Sets the chat type as one-to-one chat, group chat, or chatroom.
            mFileMessage!!.chatType = ChatMessage.ChatType.Chat
            mFileMessage!!.setMessageStatusCallback(object : CallBack {
                override fun onSuccess() {
                }

                override fun onError(code: Int, error: String?) {
                }

            })
            ChatClient.getInstance().chatManager().sendMessage(mFileMessage)*/
        }
    }

    private fun reverseTimer(secs: Long, tv: TextView) {
        object : CountDownTimer(secs * 1000 + 1000, 1000) {
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                var seconds = (millisUntilFinished / 1000).toInt()
                val minutes = seconds / 60
                seconds %= 60
                if (minutes > 1) {
                    tv.text = String.format("%02d", minutes) + " mins left"
                } else {
                    if (seconds > 9) {
                        tv.text = String.format("%02d", seconds) + " secs left"
                    } else {
                        tv.text = String.format("%02d", seconds) + " sec left"
                    }
                }
            }

            override fun onFinish() {
                val date: Calendar = Calendar.getInstance()
                val nextMonthDate = SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(date.time)
                val endTime = DateUtils(nextMonthDate)
                actualEndTime = endTime.getTime()
                duration = time
                val builder = AlertDialog.Builder(mActivity!!)
                builder.setTitle("Text Appointment")
                builder.setMessage("Your appointment is end")
                builder.setPositiveButton("OK") { dialog, which ->
                    dialog.dismiss()
                    doLogout()
                    Utils.rtmLoggedIn = false
                    sendApptStatus(
                        appointment!!.appointment.appointment_id.toString(),
                        actualStartTime!!, actualEndTime!!, duration!!
                    ) {
                        clearPreviousFragmentStack()
                        replaceFragmentNoBackStack(
                            TherapistFeedbackFragment.newInstance(appointment!!.appointment.appointment_id.toString()),
                            R.id.layout_home,
                            TherapistFeedbackFragment.TAG
                        )
                    }
                }
                builder.setCancelable(false)
                builder.show()
            }
        }.start()
    }

    private fun endVideoCall() {
        val builder = AlertDialog.Builder(mActivity!!)
        builder.setTitle("Confirmation")
        builder.setMessage("Do you wish to end the Appointment?")
        builder.setPositiveButton("Yes") { dialog, which ->
            dialog.dismiss()
            val date: Calendar = Calendar.getInstance()
            val nextMonthDate = SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(date.time)
            val endTime = DateUtils(nextMonthDate)
            actualEndTime = endTime.getTime()
            duration = time
            doLogout()
            //signOut()
            Utils.rtmLoggedIn = false
            sendApptStatus(
                appointment!!.appointment.appointment_id.toString(),
                actualStartTime!!, actualEndTime!!, duration!!
            ) {
                clearPreviousFragmentStack()
                replaceFragmentNoBackStack(
                    TherapistFeedbackFragment.newInstance(appointment!!.appointment.appointment_id.toString()),
                    R.id.layout_home,
                    TherapistFeedbackFragment.TAG
                )
            }
        }
        builder.setNegativeButton("No") { dialog, which ->
            dialog.dismiss()
        }
        builder.setCancelable(false)
        builder.show()
    }

    override fun onPause() {
        super.onPause()
        wasRunning = running;
        running = false;
    }

    override fun onResume() {
        super.onResume()
        if (wasRunning) {
            running = true;
        }
    }

    // Sets the NUmber of seconds on the timer.
    // The runTimer() method uses a Handler
    // to increment the seconds and
    // update the text view.
    private fun runTimer() {

        // Creates a new Handler
        val handler = Handler()

        // Call the post() method,
        // passing in a new Runnable.
        // The post() method processes
        // code without a delay,
        // so the code in the Runnable
        // will run almost immediately.
        handler.post(object : Runnable {
            override fun run() {
                val hours: Int = seconds / 3600
                val minutes: Int = seconds % 3600 / 60
                val secs: Int = seconds % 60

                // Format the seconds into hours, minutes,
                // and seconds.
                time = String
                    .format(
                        Locale.getDefault(),
                        "%02d:%02d:%02d", hours,
                        minutes, secs
                    )

                // If running is true, increment the
                // seconds variable.
                if (running) {
                    seconds--
                }

                // Post the code again
                // with a delay of 1 second.
                handler.postDelayed(this, 1000)
            }
        })
    }

    /**
     * API CALL: login RTM server
     */
    private fun doLogin() {
        try {
            if (RTM_TOKEN!!.isNotEmpty()) {
                mRtmClient!!.login(
                    RTM_TOKEN!!,
                    preference!![PrefKeys.PREF_EMAIL, ""],
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
                            Log.i(
                                VideoCallFragment.TAG,
                                "login failed: " + errorInfo.errorDescription
                            )
                            runOnUiThread {
                                displayToast(getString(R.string.login_failed))
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
        val date: Calendar = Calendar.getInstance()
        val nextMonthDate = SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(date.time)
        val startTime = DateUtils(nextMonthDate)
        actualStartTime = startTime.getTime()
        running = true
        runTimer()
        mClientListener = rtmClientListener
        mChatManager!!.registerListener(mClientListener)
        mIsPeerToPeerMode = false
        val targetName =
            appointment!!.doctor_first_name + " " + appointment!!.doctor_last_name
        if (mIsPeerToPeerMode) {
            mPeerId = targetName
            binding.txtTextAppointName.text = mPeerId

            // load history chat records
            val messageListBean = MessageUtil.getExistMessageListBean(mPeerId)
            if (messageListBean != null) {
                mMessageBeanList.addAll(messageListBean.getMessageBeanList()!!)
            }

            // load offline messages since last chat with this peer.
            // Then clear cached offline messages from message pool
            // since they are already consumed.
            val peerName =
                appointment!!.doctor_first_name.take(1) + "" + appointment!!.doctor_last_name.take(1)
            val offlineMessageBean = MessageListBean(peerName, mChatManager!!)
            mMessageBeanList.addAll(offlineMessageBean.getMessageBeanList()!!)
            mChatManager!!.removeAllOfflineMessages(mPeerId)
        } else {
            mChannelName = targetName
            mChannelMemberCount = 1
            binding.txtTextAppointName.text = mChannelName
            /*onlineChatView!!.txtTherapistChatName.text = MessageFormat.format(
                "{0}({1})",
                mChannelName,
                mChannelMemberCount
            )*/
            createAndJoinChannel()
        }
        runOnUiThread {
            val layoutManager = LinearLayoutManager(requireActivity())
            layoutManager.orientation = RecyclerView.VERTICAL
            mMessageAdapter = MessageAdapter(requireActivity(), mMessageBeanList, this)
            binding.textAppointMessageList.layoutManager = layoutManager
            binding.textAppointMessageList.adapter = mMessageAdapter
        }
    }

    /**
     * API CALL: send message to peer
     */
    private fun sendPeerMessage(message: RtmMessage) {
        mRtmClient!!.sendMessageToPeer(
            mPeerId,
            message,
            mChatManager!!.getSendMessageOptions(),
            object : ResultCallback<Void?> {
                override fun onSuccess(aVoid: Void?) {
                    // do nothing
                    runOnUiThread {
                        //showToast("Message sent successfully...")
                    }
                }

                override fun onFailure(errorInfo: ErrorInfo) {
                    // refer to RtmStatusCode.PeerMessageState for the message state
                    val errorCode = errorInfo.errorCode
                    runOnUiThread {
                        when (errorCode) {
                            RtmStatusCode.PeerMessageError.PEER_MESSAGE_ERR_TIMEOUT, RtmStatusCode.PeerMessageError.PEER_MESSAGE_ERR_FAILURE ->
                                showToast(getString(R.string.send_msg_failed))
                            RtmStatusCode.PeerMessageError.PEER_MESSAGE_ERR_PEER_UNREACHABLE ->
                                showToast(getString(R.string.peer_offline))
                            RtmStatusCode.PeerMessageError.PEER_MESSAGE_ERR_CACHED_BY_SERVER ->
                                showToast(getString(R.string.message_cached))
                            else -> {
                                showToast(errorInfo.errorDescription + " - " + errorInfo.errorCode)
                            }
                        }
                    }
                }
            })
    }

    /**
     * API CALL: create and join channel
     */
    private fun createAndJoinChannel() {
        // step 1: create a channel instance
        mRtmChannel = mRtmClient!!.createChannel(CHANNEL_NAME, myChannelListener)
        if (mRtmChannel == null) {
            showToast(getString(R.string.join_channel_failed))
            popBackStack()
            return
        }
        Log.e("channel", mRtmChannel.toString() + "")

        // step 2: join the channel
        mRtmChannel!!.join(object : ResultCallback<Void?> {
            override fun onSuccess(responseInfo: Void?) {
                Log.i(OnlineChatFragment.TAG, "join channel success")
                runOnUiThread {
                    //showToast("join channel success")
                }
                getChannelMemberList()
            }

            override fun onFailure(errorInfo: ErrorInfo) {
                Log.e(OnlineChatFragment.TAG, "join channel failed")
                runOnUiThread {
                    showToast(getString(R.string.join_channel_failed))
                    showToast(errorInfo.errorDescription + " - " + errorInfo.errorCode)
                    popBackStack()
                }
            }
        })
    }

    /**
     * API CALL: get channel member list
     */
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
                    OnlineChatFragment.TAG,
                    "failed to get channel members, err: " + errorInfo.errorCode
                )
            }
        })
    }

    /**
     * API CALL: send message to a channel
     */
    private fun sendChannelMessage(message: RtmMessage) {
        mRtmChannel!!.sendMessage(message, object : ResultCallback<Void?> {
            override fun onSuccess(aVoid: Void?) {
                runOnUiThread {
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

    /**
     * API CALL: leave and release channel
     */
    private fun leaveAndReleaseChannel() {
        if (mRtmChannel != null) {
            mRtmChannel!!.leave(null)
            mRtmChannel!!.release()
            mRtmChannel = null
        }
    }

    /**
     * API CALLBACK: rtm event listener
     */
    private val rtmClientListener = object : RtmClientListener {
        override fun onConnectionStateChanged(state: Int, reason: Int) {
            runOnUiThread {
                when (state) {
                    RtmStatusCode.ConnectionState.CONNECTION_STATE_RECONNECTING -> showToast(
                        getString(R.string.reconnecting)
                    )
                    RtmStatusCode.ConnectionState.CONNECTION_STATE_ABORTED -> {
                        showToast(getString(R.string.account_offline))
                        requireActivity().setResult(MessageUtil.ACTIVITY_RESULT_CONN_ABORTED)
                        popBackStack()
                    }
                }
            }
        }

        override fun onMessageReceived(message: RtmMessage, peerId: String) {
            runOnUiThread {
                if (peerId == mPeerId) {
                    val messageBean = MessageBean(peerId, message, false)
                    messageBean.setBackground(getMessageColor(peerId))
                    mMessageBeanList.add(messageBean)
                    mMessageAdapter!!.notifyItemRangeChanged(mMessageBeanList.size, 1)
                    binding.textAppointMessageList.scrollToPosition(mMessageBeanList.size - 1)
                } else {
                    MessageUtil.addMessageBean(peerId, message)
                }
            }
        }

        override fun onTokenExpired() {}
        override fun onTokenPrivilegeWillExpire() {}
        override fun onPeersOnlineStatusChanged(map: Map<String, Int>) {}
    }

    /**
     * API CALLBACK: rtm channel event listener
     */
    private val myChannelListener = object : RtmChannelListener {
        override fun onMemberCountUpdated(i: Int) {}
        override fun onAttributesUpdated(list: List<RtmChannelAttribute>) {}
        override fun onMessageReceived(message: RtmMessage, fromMember: RtmChannelMember) {
            runOnUiThread {
                val targetName =
                    appointment!!.doctor_first_name.take(1) + "" + appointment!!.doctor_last_name.take(
                        1
                    )
                val account = fromMember.userId
                Log.i(OnlineChatFragment.TAG, "onMessageReceived account = $account msg = $message")
                val messageBean = MessageBean(targetName, message, false)
                messageBean.setBackground(getMessageColor(account))
                mMessageBeanList.add(messageBean)
                mMessageAdapter!!.notifyItemRangeChanged(mMessageBeanList.size, 1)
                binding.textAppointMessageList.scrollToPosition(mMessageBeanList.size - 1)
            }
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
        return MessageUtil.COLOR_ARRAY[MessageUtil.RANDOM.nextInt(MessageUtil.COLOR_ARRAY.size)]
    }

    private fun refreshChannelTitle() {
        val titleFormat = getString(R.string.channel_title)
        val title = String.format(titleFormat, mChannelName, mChannelMemberCount)
        binding.txtTextAppointName.text = mChannelName
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

    /**
     * API CALL: logout from RTM server
     */
    private fun doLogout() {
        try {
            if (mRtmClient != null) {
                mRtmClient!!.logout(null)
                MessageUtil.cleanMessageListBeanList()
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
         * @return A new instance of fragment TextAppointmentFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: GetAppointment, param2: String, param3: String, param4: String) =
            TextAppointmentFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                    putString(ARG_PARAM3, param3)
                    putString(ARG_PARAM4, param4)
                }
            }

        const val TAG = "Screen_Text_Appointment"
        private const val PICKFILE_REQUEST_CODE = 9
    }

    override fun onItemClick(message: MessageBean?) {

    }

    override fun onItemClick(message: ChatMessages?) {

    }
}