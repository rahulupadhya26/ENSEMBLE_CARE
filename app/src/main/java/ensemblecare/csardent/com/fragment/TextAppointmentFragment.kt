package ensemblecare.csardent.com.fragment

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.app.ProgressDialog
import android.content.ContentResolver
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.webkit.MimeTypeMap
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ensemblecare.csardent.com.BaseActivity
import ensemblecare.csardent.com.adapters.MessageAdapter
import ensemblecare.csardent.com.controller.OnBottomReachedListener
import ensemblecare.csardent.com.controller.OnMessageClickListener
import ensemblecare.csardent.com.data.*
import ensemblecare.csardent.com.databinding.FragmentTextAppointmentBinding
import ensemblecare.csardent.com.preference.PrefKeys
import ensemblecare.csardent.com.preference.PreferenceHelper.get
import ensemblecare.csardent.com.realTimeMessaging.ChatManager
import ensemblecare.csardent.com.utils.DateUtils
import ensemblecare.csardent.com.utils.MessageUtil
import ensemblecare.csardent.com.utils.Utils
import com.bumptech.glide.Glide
import ensemblecare.csardent.com.R
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ensemblecare.csardent.com.BuildConfig
import ensemblecare.csardent.com.databinding.DialogAlertMsgBinding
import ensemblecare.csardent.com.databinding.DialogRescheduleApptBinding
import ensemblecare.csardent.com.databinding.DialogRescheduleApptMsgBinding
import io.agora.rtm.*
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.lang.reflect.Type
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
class TextAppointmentFragment : BaseFragment(), OnMessageClickListener, OnBottomReachedListener {
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
    private var mMessageAdapter: MessageAdapter? = null

    // Number of seconds displayed
    // on the stopwatch.
    private var seconds = 0

    // Is the stopwatch running?
    private var running = false

    private var wasRunning = false

    private var time: String = ""

    private lateinit var binding: FragmentTextAppointmentBinding
    private var isFileView: Boolean = false
    private lateinit var dialogRescheduleAppt: DialogRescheduleApptMsgBinding
    private lateinit var dialogDispMsgBinding: DialogAlertMsgBinding
    private lateinit var dialogRescheduleAppointment: DialogRescheduleApptBinding
    private var timerHandler: Handler = Handler(Looper.getMainLooper())

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

    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
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

        var dateTime = DateUtils(appointment!!.appointment!!.date + " " + "00:00:00")
        if (appointment!!.appointment!!.booking_date != null) {
            dateTime = DateUtils(appointment!!.appointment!!.booking_date + " " + "00:00:00")
        }
        binding.txtTextAppointStartTime.text =
            appointment!!.appointment!!.time_slot.starting_time.dropLast(3)
        binding.txtTextAppointStartDate.text =
            dateTime.getDay3LettersName() + ", " + dateTime.getMonth() + " " + dateTime.getDay()
        binding.txtTextAppointEndTime.text =
            appointment!!.appointment!!.time_slot.ending_time.dropLast(3)
        binding.txtTextAppointEndDate.text =
            dateTime.getDay3LettersName() + ", " + dateTime.getMonth() + " " + dateTime.getDay()

        reverseTimer((appointment!!.duration * 60).toLong(), binding.txtTextAppointTimeTaken)

        binding.imgCloseFileWebView.setOnClickListener {
            binding.layoutWebViewFile.visibility = View.GONE
            binding.webViewFile.clearCache(true)
            binding.webViewFile.clearHistory()
            binding.webViewFile.clearFormData()
            if (isFileView) {
                binding.layoutViewFile.visibility = View.VISIBLE
            } else {
                binding.layoutChatMessage.visibility = View.VISIBLE
            }
        }

        binding.imgViewFiles.setOnClickListener {
            val fileList: ArrayList<MessageBean> = ArrayList()
            if (mMessageBeanList.isNotEmpty()) {
                for (i in 0 until mMessageBeanList.size) {
                    val rtmMessage = mMessageBeanList[i].getMessage()
                    if (rtmMessage!!.messageType != RtmMessageType.TEXT) {
                        fileList.add(mMessageBeanList[i])
                    }
                }
                if (fileList.isNotEmpty()) {
                    isFileView = true
                    binding.layoutViewFile.visibility = View.VISIBLE
                    val layoutManager =
                        LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
                    layoutManager.orientation = RecyclerView.VERTICAL
                    binding.recyclerviewFileList.setHasFixedSize(true)

                    mMessageAdapter = MessageAdapter(
                        requireActivity(),
                        fileList,
                        this,
                        preference!![PrefKeys.PREF_PHOTO, ""]!!,
                        appointment!!.doctor_photo,
                        this
                    )
                    binding.recyclerviewFileList.layoutManager = layoutManager
                    binding.recyclerviewFileList.adapter = mMessageAdapter
                } else {
                    binding.layoutViewFile.visibility = View.GONE
                    displayMsg("Alert", "Files are not shared")
                }
            } else {
                binding.layoutViewFile.visibility = View.GONE
                displayMsg("Alert", "Messages are not found")
            }
        }

        binding.imgCloseViewFile.setOnClickListener {
            isFileView = false
            binding.layoutViewFile.visibility = View.GONE
            binding.layoutChatMessage.visibility = View.VISIBLE
        }

        binding.imgAddFile.setOnClickListener {
            val mimeTypes = arrayOf("image/jpeg", "image/png", "image/gif", "application/pdf")
            var intent: Intent
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            } else {
                intent = Intent(Intent.ACTION_GET_CONTENT)
            }
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                intent.type = if (mimeTypes.size == 1) mimeTypes[0] else "*/*"
                if (mimeTypes.isNotEmpty()) {
                    intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
                }
            } else {
                var mimeTypesStr = ""
                for (mimeType in mimeTypes) {
                    mimeTypesStr += "$mimeType|"
                }
                intent.type = mimeTypesStr.substring(0, mimeTypesStr.length - 1)
            }
            try {
                intent = Intent.createChooser(intent, "Select a file")
                startActivityForResult(intent, PICKFILE_REQUEST_CODE)
                //sActivityResultLauncher.launch(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
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
                                requireActivity().setResult(MessageUtil.ACTIVITY_RESULT_CONN_ABORTED)
                                popBackStack()
                            }
                        }
                    }
                }

                override fun onMessageReceived(message: RtmMessage?, peerId: String?) {
                    runOnUiThread {
                        if (peerId == mPeerId) {
                            val messageBean = MessageBean(peerId, message, false)
                            messageBean.setBackground(getMessageColor(peerId))
                            mMessageBeanList.add(messageBean)
                            mMessageAdapter!!.notifyItemRangeChanged(mMessageBeanList.size, 1)
                            binding.textAppointMessageList.scrollToPosition(mMessageBeanList.size - 1)
                        } else {
                            MessageUtil.addMessageBean(peerId!!, message)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PICKFILE_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                data.data.let { returnUri ->
                    requireActivity().contentResolver.query(returnUri!!, null, null, null, null)
                }?.use { cursor ->
                    /*
                 * Get the column indexes of the data in the Cursor,
                 * move to the first row in the Cursor, get the data,
                 * and display it.
                 */
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    cursor.moveToFirst()
                    val selectedFile = data?.data
                    convertToString(
                        selectedFile!!,
                        cursor.getString(nameIndex),
                        cursor.getLong(sizeIndex)
                    )
                }
            }
        }
    }

    private fun convertToString(uri: Uri, fileName: String, fileSize: Long) {
        if (fileSize <= 10000000) {
            val uriString = uri.toString()
            val fileType = getMimeType(uri)
            Log.d("data", "onActivityResult: uri$uriString")
            //            myFile = new File(uriString);
            //            ret = myFile.getAbsolutePath();
            try {
                val inputStream: InputStream =
                    requireActivity().contentResolver.openInputStream(uri)!!
                val bytes = getBytes(inputStream)
                var document = Base64.encodeToString(bytes, Base64.NO_WRAP)
                /*val document =
                    "JVBERi0xLjMKMyAwIG9iago8PC9UeXBlIC9QYWdlCi9QYXJlbnQgMSAwIFIKL1Jlc291cmNlcyAyIDAgUgovQ29udGVudHMgNCAwIFI+PgplbmRvYmoKNCAwIG9iago8PC9GaWx0ZXIgL0ZsYXRlRGVjb2RlIC9MZW5ndGggNjQ3Pj4Kc3RyZWFtCnicnZbBctowFEX3fMVbtjNBlmRZsndtSNLSaVMK7qJLBz8cN8YmspxM/r6ygdrgpDMVCyHEWOceJD3B4cuEkkDB8+QyBu+GAQsIpRBv4DreD3Fgqh9itkuBDlqdTR5tl1FuWxGJQ6szsI/7jLAIpJREMYhTePerajQs4zks8bHB2sAmLzCFulmvsa43TVG8kPcQ/7Ys+DGhkO1D+MD4WS5xMvRqLh4SPwBplRgHbkMICVPOiQxBI6ygzc0DdtKe5KbWK+xyLzHLa6MTk1cl3DbbO9R9TB4JEjInEI8UkWJIWnxfxStv6V173Pcok1INQJ1QEHEihItQoBQJaIe5TbY4MnCZ+WAwmPqTXePkCVZ5md2PssuIKOmUPbAR/Q5wlRiEagM3eWEZYwsHxtGih1AxpXzKKfdHDsIeiMjJweeEBfsNZc/ADaI9B2vMnzAdWzhQjhY9htFRei6IYE7paUSE2m/S5GWLpYFvVfrKLnIgHJP3iHlpUJdo4DIpH+wyX8BMY5obqDRc4Z3tzBKdggfLxqbpPlzAz8V85EsZUb6Lrwht2YpOfJe4QY3lGt+qAS60g/sAN1t8nH39HMeXIfx9nXsJFRHqdJKEFG2dakGxTso6WXdVbWUS09QjIxfO0agHzartrkBjS/1qUOpHToEkfujkJFhbufalenizPOfmfuzkwDk69aAr3CXadPvCFqNFVZvBr/d/d5Rg9kunO8qPZFsmusXEAnf3Vfnm5nShHLQHGMrYlPtUiJCPriY/7N5cPJTfFo0WcL1N8gLm45LoMvsxfz+9NnmK9cOHvEzzZGdXjWTVE8nLs7V79a9Q2I/8A6lkVwMCEgWH6pEhMI/1hD/eriz+CmVuZHN0cmVhbQplbmRvYmoKMSAwIG9iago8PC9UeXBlIC9QYWdlcwovS2lkcyBbMyAwIFIgXQovQ291bnQgMQovTWVkaWFCb3ggWzAgMCA1OTUuMjggODQxLjg5XQo+PgplbmRvYmoKNSAwIG9iago8PC9UeXBlIC9Gb250Ci9CYXNlRm9udCAvSGVsdmV0aWNhLUJvbGQKL1N1YnR5cGUgL1R5cGUxCi9FbmNvZGluZyAvV2luQW5zaUVuY29kaW5nCj4+CmVuZG9iago2IDAgb2JqCjw8L1R5cGUgL0ZvbnQKL0Jhc2VGb250IC9IZWx2ZXRpY2EtT2JsaXF1ZQovU3VidHlwZSAvVHlwZTEKL0VuY29kaW5nIC9XaW5BbnNpRW5jb2RpbmcKPj4KZW5kb2JqCjcgMCBvYmoKPDwvVHlwZSAvRm9udAovQmFzZUZvbnQgL1RpbWVzLVJvbWFuCi9TdWJ0eXBlIC9UeXBlMQovRW5jb2RpbmcgL1dpbkFuc2lFbmNvZGluZwo+PgplbmRvYmoKOCAwIG9iago8PC9UeXBlIC9Gb250Ci9CYXNlRm9udCAvSGVsdmV0aWNhCi9TdWJ0eXBlIC9UeXBlMQovRW5jb2RpbmcgL1dpbkFuc2lFbmNvZGluZwo+PgplbmRvYmoKMiAwIG9iago8PAovUHJvY1NldCBbL1BERiAvVGV4dCAvSW1hZ2VCIC9JbWFnZUMgL0ltYWdlSV0KL0ZvbnQgPDwKL0YxIDUgMCBSCi9GMiA2IDAgUgovRjMgNyAwIFIKL0Y0IDggMCBSCj4+Ci9YT2JqZWN0IDw8Cj4+Cj4+CmVuZG9iago5IDAgb2JqCjw8Ci9Qcm9kdWNlciAoRlBERiAxLjcpCi9DcmVhdGlvbkRhdGUgKEQ6MjAyMzAyMDQwMDE0MzkpCj4+CmVuZG9iagoxMCAwIG9iago8PAovVHlwZSAvQ2F0YWxvZwovUGFnZXMgMSAwIFIKPj4KZW5kb2JqCnhyZWYKMCAxMQowMDAwMDAwMDAwIDY1NTM1IGYgCjAwMDAwMDA4MDQgMDAwMDAgbiAKMDAwMDAwMTI5MCAwMDAwMCBuIAowMDAwMDAwMDA5IDAwMDAwIG4gCjAwMDAwMDAwODcgMDAwMDAgbiAKMDAwMDAwMDg5MSAwMDAwMCBuIAowMDAwMDAwOTkyIDAwMDAwIG4gCjAwMDAwMDEwOTYgMDAwMDAgbiAKMDAwMDAwMTE5NCAwMDAwMCBuIAowMDAwMDAxNDI0IDAwMDAwIG4gCjAwMDAwMDE0OTkgMDAwMDAgbiAKdHJhaWxlcgo8PAovU2l6ZSAxMQovUm9vdCAxMCAwIFIKL0luZm8gOSAwIFIKPj4Kc3RhcnR4cmVmCjE1NDkKJSVFT0YK"*/
                sendFile(
                    appointment!!.appointment!!.appointment_id,
                    fileName.split(".")[0],
                    fileType.split("/")[1],
                    "data:$fileType;base64,$document"
                ) { response ->
                    hideProgress()
                    Log.i("Response", response.toString())
                    val jsonObj = JSONObject(response.toString())
                    val message = mRtmClient!!.createMessage()
                    val msg = "$fileName," + jsonObj.getString("file")
                    val list: MutableList<String> = ArrayList()
                    list.add(fileName)
                    list.add(jsonObj.getString("file"))

                    message.rawMessage = msg.toByteArray()
                    sendChannelMessage(message)
                    //message.text = "$fileName,"+jsonObj.getString("file")
                    val patientName =
                        preference!![PrefKeys.PREF_FNAME, ""]!!.take(1) + "" + preference!![PrefKeys.PREF_LNAME, ""]!!.take(
                            1
                        )
                    message.rawMessage = msg.toByteArray()
                    val messageBean = MessageBean(patientName, message, true)
                    mMessageBeanList.add(messageBean)
                    mMessageAdapter!!.notifyItemRangeChanged(mMessageBeanList.size, 1)
                    binding.textAppointMessageList.scrollToPosition(mMessageBeanList.size - 1)
                }
            } catch (e: java.lang.Exception) {
                // TODO: handle exception
                e.printStackTrace()
                Log.d("error", "onActivityResult: $e")
            }
        } else {
            displayMsg("Alert", "File size should not exceed more than 10 MB")
        }
    }

    private fun getMimeType(uri: Uri): String {
        var mimeType = ""
        try {
            mimeType = if (uri.scheme.equals(ContentResolver.SCHEME_CONTENT)) {
                val cr = requireActivity().contentResolver
                cr.getType(uri)!!
            } else {
                val fileExt: String = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExt.lowercase())!!
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return mimeType
    }

    @Throws(IOException::class)
    private fun getBytes(inputStream: InputStream): ByteArray? {
        val byteBuffer = ByteArrayOutputStream()
        val bufferSize = 1024
        val buffer = ByteArray(bufferSize)
        var len = 0
        while (inputStream.read(buffer).also { len = it } != -1) {
            byteBuffer.write(buffer, 0, len)
        }
        return byteBuffer.toByteArray()
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
                        appointment!!.appointment!!.appointment_id.toString(),
                        actualStartTime!!, actualEndTime!!, duration!!
                    ) {
                        clearPreviousFragmentStack()
                        replaceFragmentNoBackStack(
                            TherapistFeedbackFragment.newInstance(appointment!!),
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
                appointment!!.appointment!!.appointment_id.toString(),
                actualStartTime!!, actualEndTime!!, duration!!
            ) {
                clearPreviousFragmentStack()
                replaceFragmentNoBackStack(
                    TherapistFeedbackFragment.newInstance(appointment!!),
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

    private fun runTimer() {
        timerHandler.post(object : Runnable {
            override fun run() {
                val hours: Int = seconds / 3600
                val minutes: Int = seconds % 3600 / 60
                val secs: Int = seconds % 60

                // Format the seconds into hours, minutes,
                // and seconds.
                val time: String = String
                    .format(
                        Locale.getDefault(),
                        "%02d:%02d:%02d", hours,
                        minutes, secs
                    )

                // If running is true, increment the
                // seconds variable.
                if (running) {
                    seconds++
                }

                if (minutes > 14) {
                    getChannelMemberList()
                    if (mChannelMemberCount < 2) {
                        wasRunning = running
                        running = false
                        displayProviderNotJoinedMsg()
                    }
                }

                // Post the code again
                // with a delay of 1 second.
                if (running)
                    timerHandler.postDelayed(this, 1000)
            }
        })
    }

    private fun displayProviderNotJoinedMsg() {
        timerHandler.removeCallbacksAndMessages(null)
        val dialog = Dialog(requireActivity())
        dialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        dialogDispMsgBinding = DialogAlertMsgBinding.inflate(layoutInflater)
        val view = dialogDispMsgBinding.root
        dialog.setContentView(view)
        dialog.setCanceledOnTouchOutside(true)
        dialogDispMsgBinding.txtAlertMsg.text =
            "Provider not joined the call.\nYou can reschedule the appointment."
        dialogDispMsgBinding.txtOkBtn.setOnClickListener {
            dialog.dismiss()

            val date: Calendar = Calendar.getInstance()
            val nextMonthDate = SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(date.time)
            val endTime = DateUtils(nextMonthDate)
            actualEndTime = endTime.getTime()
            duration = time
            doLogout()
            Utils.rtmLoggedIn = false
            missedAppointment(
                appointment!!.appointment!!.appointment_id.toString(),
                isClientMissed = true, isProviderMissed = false
            ) {
                displayRescheduleAppointmentMsg()
            }
        }
        dialog.show()
    }

    private fun displayRescheduleAppointmentMsg() {
        val dialog = Dialog(requireActivity())
        dialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        dialogRescheduleAppt = DialogRescheduleApptMsgBinding.inflate(layoutInflater)
        val view = dialogRescheduleAppt.root
        dialog.setContentView(view)
        dialog.setCanceledOnTouchOutside(true)
        dialogRescheduleAppt.txtRescheduleApptYesBtn.setOnClickListener {
            dialog.dismiss()
            rescheduleAppointment(appointment!!.appointment!!.appointment_id.toString()) { response ->
                val rescheduleApptType: Type = object : TypeToken<RescheduleAppointment?>() {}.type
                val rescheduleAppt: RescheduleAppointment =
                    Gson().fromJson(response, rescheduleApptType)
                displayRescheduleAppointment(rescheduleAppt)
            }
        }
        dialogRescheduleAppt.txtRescheduleApptNoBtn.setOnClickListener {
            dialog.dismiss()
            setBottomNavigation(null)
            setLayoutBottomNavigation(null)
            replaceFragmentNoBackStack(
                BottomNavigationFragment(),
                R.id.layout_home,
                BottomNavigationFragment.TAG
            )
        }
        dialog.show()
    }

    @SuppressLint("SetTextI18n")
    private fun displayRescheduleAppointment(rescheduleAppt: RescheduleAppointment) {
        val dialog = Dialog(requireActivity())
        dialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        dialogRescheduleAppointment = DialogRescheduleApptBinding.inflate(layoutInflater)
        val view = dialogRescheduleAppointment.root
        dialog.setContentView(view)
        dialog.setCanceledOnTouchOutside(true)

        dialogRescheduleAppointment.txtReschApptTherapistName.text = rescheduleAppt.doctor.name
        dialogRescheduleAppointment.txtReschApptTherapistType.text =
            rescheduleAppt.doctor.designation

        val appointmentDate =
            DateUtils(rescheduleAppt.date + " " + rescheduleAppt.time_slot.starting_time)

        dialogRescheduleAppointment.txtReschApptDateTime.text = appointmentDate.getDay() + " " +
                appointmentDate.getFullMonthName() + " at " + rescheduleAppt.time_slot.starting_time.dropLast(
            3
        )

        Glide.with(requireActivity())
            .load(BaseActivity.baseURL.dropLast(5) + rescheduleAppt.doctor.photo)
            .placeholder(R.drawable.doctor_img)
            .transform(CenterCrop(), RoundedCorners(5))
            .into(dialogRescheduleAppointment.reschApptMode)

        when (rescheduleAppt.type_of_visit) {
            "Video" -> {
                dialogRescheduleAppointment.reschApptMode.setBackgroundResource(R.drawable.video)
                dialogRescheduleAppointment.reschApptMode.backgroundTintList =
                    ColorStateList.valueOf(
                        ContextCompat.getColor(
                            requireActivity(),
                            R.color.primaryGreen
                        )
                    )
            }

            "Audio" -> {
                dialogRescheduleAppointment.reschApptMode.setBackgroundResource(R.drawable.telephone)
                dialogRescheduleAppointment.reschApptMode.backgroundTintList =
                    ColorStateList.valueOf(
                        ContextCompat.getColor(
                            requireActivity(),
                            R.color.primaryGreen
                        )
                    )
            }

            else -> {
                dialogRescheduleAppointment.reschApptMode.setBackgroundResource(R.drawable.chat)
                dialogRescheduleAppointment.reschApptMode.backgroundTintList =
                    ColorStateList.valueOf(
                        ContextCompat.getColor(
                            requireActivity(),
                            R.color.primaryGreen
                        )
                    )
            }
        }

        dialogRescheduleAppointment.txtRescheduleApptOkBtn.setOnClickListener {
            dialog.dismiss()
            setBottomNavigation(null)
            setLayoutBottomNavigation(null)
            replaceFragmentNoBackStack(
                BottomNavigationFragment(),
                R.id.layout_home,
                BottomNavigationFragment.TAG
            )
        }
        dialog.show()
    }

    /**
     * API CALL: login RTM server
     */
    private fun doLogin() {
        try {
            if (RTM_TOKEN!!.isNotEmpty()) {
                mRtmClient!!.login(
                    RTM_TOKEN!!,
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
        val date: Calendar = Calendar.getInstance()
        val nextMonthDate = SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(date.time)
        val startTime = DateUtils(nextMonthDate)
        actualStartTime = startTime.getTime()
        running = true
        runTimer()
        mIsPeerToPeerMode = false
        val targetName =
            appointment!!.doctor_first_name + " " + appointment!!.doctor_last_name
        if (mIsPeerToPeerMode) {
            mPeerId = targetName
            //binding.txtTextAppointName.text = mPeerId

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
            //binding.txtTextAppointName.text = mChannelName
            /*onlineChatView!!.txtTherapistChatName.text = MessageFormat.format(
                "{0}({1})",
                mChannelName,
                mChannelMemberCount
            )*/
            createAndJoinChannel()
        }
        runOnUiThread {
            val layoutManager =
                LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
            layoutManager.orientation = RecyclerView.VERTICAL
            mMessageAdapter = MessageAdapter(
                requireActivity(),
                mMessageBeanList,
                this,
                preference!![PrefKeys.PREF_PHOTO, ""]!!,
                appointment!!.doctor_photo,
                this
            )
            binding.textAppointMessageList.layoutManager = layoutManager
            binding.textAppointMessageList.adapter = mMessageAdapter

            binding.layoutTextAppointSend.setOnClickListener {
                val msg: String = getText(binding.editTextTextAppointMessage)
                if (msg != "") {
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
                Log.i(TAG, "join channel success")
                runOnUiThread {
                    //showToast("join channel success")
                }
                getChannelMemberList()
            }

            override fun onFailure(errorInfo: ErrorInfo) {
                Log.e(TAG, "join channel failed")
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
                    TAG,
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
                Log.i(TAG, "onMessageReceived account = $account msg = $message")
                val messageBean = MessageBean(targetName, message, false)
                messageBean.setBackground(getMessageColor(account))
                mMessageBeanList.add(messageBean)
                mMessageAdapter!!.notifyItemRangeChanged(mMessageBeanList.size, 1)
                binding.textAppointMessageList.scrollToPosition(mMessageBeanList.size - 1)
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
        return MessageUtil.COLOR_ARRAY[MessageUtil.RANDOM.nextInt(MessageUtil.COLOR_ARRAY.size)]
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

    /**
     * API CALL: logout from RTM server
     */
    private fun doLogout() {
        try {
            leaveAndReleaseChannel()
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
        binding.layoutChatMessage.visibility = View.GONE
        binding.layoutViewFile.visibility = View.GONE
        binding.layoutWebViewFile.visibility = View.VISIBLE
        val browser = binding.webViewFile.settings
        browser.javaScriptEnabled = true
        browser.builtInZoomControls = true
        browser.pluginState = WebSettings.PluginState.ON
        browser.loadWithOverviewMode = true
        browser.useWideViewPort = true
        binding.webViewFile.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                view?.loadUrl(url!!)
                return true
            }
        }
        binding.webViewFile.webViewClient = object : WebViewClient() {
            private var mProgressDialog: ProgressDialog? = null
            override fun onPageFinished(view: WebView, url: String?) {
                view.settings.loadsImagesAutomatically = true
                binding.webViewFile.visibility = View.VISIBLE
                //progressView.setVisibility(View.VISIBLE);
                dismissProgressDialog()
                view.loadUrl("javascript:clickFunction(){  })()")
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                showProgressDialog()
            }

            override fun onReceivedError(
                view: WebView?,
                errorCode: Int,
                description: String?,
                failingUrl: String?
            ) {
                dismissProgressDialog()
                Log.e("error", description!!)
            }

            private fun showProgressDialog() {
                dismissProgressDialog()
                mProgressDialog = ProgressDialog.show(mContext, "", "Loading...")
            }

            private fun dismissProgressDialog() {
                if (mProgressDialog != null && mProgressDialog!!.isShowing) {
                    mProgressDialog!!.dismiss()
                    mProgressDialog = null
                }
            }
        }

        val mMessage = String(message!!.getMessage()!!.rawMessage)
        val mMsgArr = mMessage.split(",")
        if (mMsgArr[1].contains("pdf")) {
            binding.webViewFile.loadUrl(
                "http://docs.google.com/gview?embedded=true&url=" + BaseActivity.baseURL.dropLast(
                    5
                ) + mMsgArr[1]
            )
        } else {
            binding.webViewFile.loadUrl(BaseActivity.baseURL.dropLast(5) + mMsgArr[1])
        }
    }

    override fun onBottomReached(position: Int) {

    }
}