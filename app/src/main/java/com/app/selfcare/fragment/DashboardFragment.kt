package com.app.selfcare.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.AbsListView
import androidx.annotation.NonNull
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.BuildConfig
import com.app.selfcare.R
import com.app.selfcare.adapters.*
import com.app.selfcare.controller.*
import com.app.selfcare.data.*
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper.get
import com.app.selfcare.utils.AudioStream
import com.app.selfcare.utils.DateUtils
import com.app.selfcare.utils.Utils
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.MPPointF
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_dashboard.*
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.HttpException
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

enum class FitActionRequestCode {
    SUBSCRIBE,
    READ_DATA
}

/**
 * A simple [Fragment] subclass.
 * Use the [DashboardFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
open class DashboardFragment : BaseFragment(), OnNewsItemClickListener, OnPodcastItemClickListener,
    OnJournalItemClickListener, OnAppointmentItemClickListener, OnVideoItemClickListener,
    OnRecommendedItemClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var mediaPlayer: MediaPlayer? = null

    private val fitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE)
        .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
        .addDataType(DataType.TYPE_DISTANCE_DELTA)
        .addDataType(DataType.TYPE_HEART_RATE_BPM)
        .addDataType(DataType.TYPE_CALORIES_EXPENDED)
        .build()

    private var GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 1

    private val runningQOrLater =
        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_dashboard
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE
        mActivity!!.setUserDetails()

        checkPermissionsAndRun(FitActionRequestCode.SUBSCRIBE)

        //Show Good morning, afternoon, evening or night message to user.
        showMessageToUser()

        txtUserName.text = preference!![PrefKeys.PREF_FNAME, ""] + " " +
                preference!![PrefKeys.PREF_LNAME, ""]

        //Appointments
        displayAppointments()

        handleViewMoreEvents()

        img_user_pic.setOnClickListener {
            replaceFragment(
                ProfileFragment(),
                R.id.layout_home,
                ProfileFragment.TAG
            )
        }

        layoutUserName.setOnClickListener {
            replaceFragment(
                ProfileFragment(),
                R.id.layout_home,
                ProfileFragment.TAG
            )
        }

        layoutAppointments.setOnClickListener {
            replaceFragment(
                AppointmentsFragment(),
                R.id.layout_home,
                AppointmentsFragment.TAG
            )
        }

        layoutAssessments.setOnClickListener {
            replaceFragment(
                MyAssessmentsFragment(),
                R.id.layout_home,
                MyAssessmentsFragment.TAG
            )
        }

        layoutGoals.setOnClickListener {
            replaceFragment(
                ActivityCarePlanFragment(),
                R.id.layout_home,
                ActivityCarePlanFragment.TAG
            )
        }

        layoutJournals.setOnClickListener {
            replaceFragment(
                JournalFragment(),
                R.id.layout_home,
                JournalFragment.TAG
            )
        }

        fabCreateAppointmentBtn.setOnClickListener {
            Utils.isTherapististScreen = false
            replaceFragment(
                TherapistListFragment.newInstance(false),
                R.id.layout_home,
                TherapistListFragment.TAG
            )
        }

        itemsSwipeToRefresh.setOnRefreshListener {
            displayAppointments()
        }

        pieChart.setUsePercentValues(true)
        pieChart.description.isEnabled = false
        pieChart.setExtraOffsets(2f, 2f, 2f, 2f)
        // on below line we are setting drag for our pie chart
        pieChart.dragDecelerationFrictionCoef = 0.95f
        // on below line we are setting hole
        // and hole color for pie chart
        pieChart.isDrawHoleEnabled = true
        pieChart.setHoleColor(Color.WHITE)
        // on below line we are setting circle color and alpha
        pieChart.setTransparentCircleColor(Color.WHITE)
        pieChart.setTransparentCircleAlpha(110)
        // on  below line we are setting hole radius
        pieChart.holeRadius = 60f
        pieChart.transparentCircleRadius = 63f
        // on below line we are setting center text
        pieChart.setDrawCenterText(true)
        // on below line we are setting
        // rotation for our pie chart
        pieChart.rotationAngle = 0f
        // enable rotation of the pieChart by touch
        pieChart.isRotationEnabled = true
        pieChart.isHighlightPerTapEnabled = true
        // on below line we are setting animation for our pie chart
        pieChart.animateY(1400, Easing.EaseInOutQuad)
        // on below line we are disabling our legend for pie chart
        pieChart.legend.isEnabled = false
        pieChart.setEntryLabelColor(Color.WHITE)
        pieChart.setEntryLabelTextSize(0f)
        // on below line we are creating array list and
        // adding data to it to display in pie chart
        val entries: ArrayList<PieEntry> = ArrayList()
        entries.add(PieEntry(70f))
        entries.add(PieEntry(30f))
        // on below line we are setting pie data set
        val dataSet = PieDataSet(entries, "")
        // on below line we are setting icons.
        dataSet.setDrawIcons(false)
        // on below line we are setting slice for pie
        dataSet.sliceSpace = 0f
        dataSet.iconsOffset = MPPointF(0f, 40f)
        dataSet.selectionShift = 5f
        // add a lot of colors to list
        val colors: ArrayList<Int> = ArrayList()
        colors.add(ContextCompat.getColor(requireActivity(), R.color.primaryGreen))
        colors.add(ContextCompat.getColor(requireActivity(), R.color.pie_chart_color))

        // on below line we are setting colors.
        dataSet.colors = colors

        // on below line we are setting pie data set
        val data = PieData(dataSet)
        data.setValueTextSize(0f)
        data.setValueTypeface(Typeface.DEFAULT_BOLD)
        data.setValueTextColor(Color.WHITE)
        pieChart.data = data

        // undo all highlights
        pieChart.highlightValues(null)

        // loading chart
        pieChart.invalidate()
    }

    private fun showMessageToUser() {
        val c = Calendar.getInstance()
        val timeOfDay = c[Calendar.HOUR_OF_DAY]
        if (timeOfDay in 0..11) {
            txtShowMessageToUser.text = "Good Morning,"
        } else if (timeOfDay in 12..15) {
            txtShowMessageToUser.text = "Good Afternoon,"
        } else if (timeOfDay in 16..19) {
            txtShowMessageToUser.text = "Good Evening,"
        } else if (timeOfDay in 20..23) {
            txtShowMessageToUser.text = "Good Night,"
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            val photo = preference!![PrefKeys.PREF_PHOTO, ""]!!
            if (photo.isNotEmpty()) {
                val base64Image = photo.split(",")[1]
                val decodedString: ByteArray = Base64.decode(base64Image, Base64.DEFAULT)
                val decodedByte =
                    BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                img_user_pic.setImageBitmap(decodedByte)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        //Appointments
        displayAppointments()
    }

    private fun handleViewMoreEvents() {
        txtViewAllArticles.setOnClickListener {
            replaceFragment(NewsListFragment(), R.id.layout_home, NewsListFragment.TAG)
        }

        txtViewAllPodcast.setOnClickListener {
            replaceFragment(PodcastFragment(), R.id.layout_home, PodcastFragment.TAG)
        }

        txtViewAllVideos.setOnClickListener {
            replaceFragment(VideosListFragment(), R.id.layout_home, VideosListFragment.TAG)
        }

        txtViewAllAppointments.setOnClickListener {
            replaceFragment(AppointmentsFragment(), R.id.layout_home, AppointmentsFragment.TAG)
        }

        txtViewAllRecommendedForYou.setOnClickListener {
            replaceFragment(
                RecommendedDataFragment(),
                R.id.layout_home,
                RecommendedDataFragment.TAG
            )
        }
    }

    private fun getData(tableId: String, myCallback: (result: String?) -> Unit) {
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .getData(tableId, getAccessToken())
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
                            if (status == "401") {
                                userLogin(
                                    preference!![PrefKeys.PREF_EMAIL]!!,
                                    preference!![PrefKeys.PREF_PASS]!!
                                ) { result ->
                                    when (tableId) {
                                        "PI0014" -> displayVideoList()
                                        "PI0020" -> displayPodcastList()
                                        "PI0011" -> displayArticleList()
                                    }
                                }
                            } else {
                                myCallback.invoke(responseBody)
                            }
                        } catch (e: Exception) {
                            hideProgress()
                            //displayToast("Something went wrong.. Please try after sometime")
                            when (tableId) {
                                "PI0020" -> displayPodcastList()
                                "PI0011" -> displayArticleList()
                            }
                        }
                    }, { error ->
                        hideProgress()
                        //displayToast("Error ${error.localizedMessage}")
                        if ((error as HttpException).code() == 401) {
                            userLogin(
                                preference!![PrefKeys.PREF_EMAIL]!!,
                                preference!![PrefKeys.PREF_PASS]!!
                            ) { result ->
                                when (tableId) {
                                    "PI0014" -> displayVideoList()
                                    "PI0020" -> displayPodcastList()
                                    "PI0011" -> displayArticleList()
                                }
                            }
                        } else {
                            displayAfterLoginErrorMsg(error)
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    @SuppressLint("SetTextI18n")
    private fun displayAppointments() {
        itemsSwipeToRefresh.isRefreshing = false
        /*if (isDebug) {
            appointmentLists.add(
                Appointment(
                    "10", "", "1", "1",
                    "1", "1", "Teen Therapy", "",
                    "06-07-2022", "10:00", "true", "10:30",
                    "00:30", "", "Dr. Mike", "S",
                    "963258741", "Teen", "mike@gmail.com"
                )
            )
            appointmentLists.add(
                Appointment(
                    "20", "", "1", "1",
                    "1", "1", "Child Therapy", "",
                    "06-07-2022", "11:00", "true", "11:30",
                    "00:30", "", "Dr. Denial", "Rich",
                    "963258741", "Child", "denial@gmail.com"
                )
            )
        }*/
        var appointmentLists: ArrayList<GetAppointment> = arrayListOf()
        val timeSlots: ArrayList<String> = arrayListOf()
        val sdf = SimpleDateFormat("MM/dd/yyyy' 'HH:mm:ss")
        val currentDate = sdf.format(Date())
        val currentDateTime = DateUtils(currentDate)
        txtMonth.text = currentDateTime.getFullMonthName()
        txtCurrentDate.text = currentDateTime.getDay()
        getAppointmentList { response ->

            val appointmentList: Type = object : TypeToken<ArrayList<GetAppointment?>?>() {}.type
            appointmentLists = Gson().fromJson(response, appointmentList)

            /*val jsonArr = JSONArray(response)
            if (jsonArr.length() != 0) {
                for (i in 0 until jsonArr.length()) {
                    val appointmentObj = jsonArr.getJSONObject(i)
                    val appointment = Appointment(
                        appointmentObj.getInt("appointment_id"),
                        appointmentObj.getString("date"),
                        appointmentObj.getBoolean("is_book"),
                        appointmentObj.getString("type_of_visit"),
                        appointmentObj.getString("booking_date"),
                        appointmentObj.getJSONObject("doctor").getString("doctor_id"),
                        appointmentObj.getJSONObject("doctor").getString("ssn"),
                        appointmentObj.getJSONObject("doctor").getString("first_name"),
                        appointmentObj.getJSONObject("doctor").getString("middle_name"),
                        appointmentObj.getJSONObject("doctor").getString("last_name"),
                        appointmentObj.getJSONObject("doctor").getString("doctor_type"),
                        appointmentObj.getJSONObject("doctor").getString("dob"),
                        appointmentObj.getJSONObject("doctor").getString("qualification"),
                        appointmentObj.getJSONObject("doctor").getString("years_of_experiance"),
                        appointmentObj.getJSONObject("doctor").getString("gender"),
                        appointmentObj.getJSONObject("doctor").getString("practice_state"),
                        appointmentObj.getJSONObject("time_slot").getString("time_slot_id"),
                        appointmentObj.getJSONObject("time_slot").getString("starting_time"),
                        appointmentObj.getJSONObject("time_slot").getString("ending_time"),
                        appointmentObj.getJSONObject("meeting").getString("meeting_id"),
                        appointmentObj.getJSONObject("meeting").getString("appointment"),
                        appointmentObj.getJSONObject("meeting").getString("doctor"),
                        appointmentObj.getJSONObject("meeting").getString("patient"),
                        appointmentObj.getJSONObject("meeting").getString("meeting_title"),
                        appointmentObj.getJSONObject("meeting").getString("channel_name"),
                        appointmentObj.getJSONObject("meeting").getString("rtc_token"),
                        appointmentObj.getJSONObject("meeting").getString("rtm_token"),
                        appointmentObj.getJSONObject("meeting").getString("rtm_token_doctor"),
                        appointmentObj.getJSONObject("meeting").getString("meeting_date"),
                        appointmentObj.getJSONObject("meeting").getString("duration")
                    )
                    appointmentLists.add(appointment)
                    timeSlots.add(
                        appointmentObj.getJSONObject("time_slot").getString("starting_time")
                            .dropLast(3)
                    )
                }
            }*/
            if (appointmentLists.isNotEmpty()) {
                recyclerViewAppointments.visibility = View.GONE
                cardViewAppointment.visibility = View.VISIBLE
                txtNoAppointments.visibility = View.GONE
                txtViewAllAppointments.visibility = View.GONE

                txtAppointTherapistName.text = appointmentLists[0].doctor_first_name + " " +
                        appointmentLists[0].doctor_last_name
                txtAppointTherapistType.text = appointmentLists[0].doctor_designation

                val appointmentDate =
                    DateUtils(appointmentLists[0].appointment.booking_date + " 00:00:00")

                txtAppointmentDateTime.text =
                    appointmentDate.getCurrentDay() + ", " +
                            appointmentDate.getDay() + " " +
                            appointmentDate.getMonth() + " at " +
                            "11:00 AM" + " - " + "11:30 AM"

                if (appointmentLists[0].appointment.type_of_visit == "Video") {
                    appointmentCall.setImageResource(R.drawable.video)
                    appointmentCall.imageTintList =
                        ColorStateList.valueOf(
                            ContextCompat.getColor(
                                requireActivity(),
                                R.color.primaryGreen
                            )
                        )
                } else {
                    appointmentCall.setImageResource(R.drawable.telephone)
                    appointmentCall.imageTintList =
                        ColorStateList.valueOf(
                            ContextCompat.getColor(
                                requireActivity(),
                                R.color.primaryGreen
                            )
                        )
                }

                cardViewAppointment.setOnClickListener {
                    getToken(appointmentLists[0])
                }

                recyclerViewAppointments.apply {
                    layoutManager = LinearLayoutManager(mActivity!!, RecyclerView.HORIZONTAL, false)
                    adapter = DashboardAppointmentAdapter(
                        mActivity!!,
                        appointmentLists,
                        this@DashboardFragment
                    )
                }
                /*if (timeSlots.isNotEmpty()) {
                    txtTimeSlots.text = timeSlots[0]
                }
                previousItem.setOnClickListener {
                    val layoutManager: RecyclerView.LayoutManager? =
                        recyclerViewAppointments.layoutManager
                    var currentPosition = 0
                    if (layoutManager is LinearLayoutManager) {
                        currentPosition = layoutManager.findFirstCompletelyVisibleItemPosition()
                    }
                    if ((currentPosition - 1) != -1) {
                        recyclerViewAppointments.smoothScrollToPosition(currentPosition - 1)
                    }
                    if ((currentPosition - 1) != -1) {
                        txtTimeSlots.text = timeSlots[currentPosition - 1]
                    }
                }

                nextItem.setOnClickListener {
                    val layoutManager: RecyclerView.LayoutManager? =
                        recyclerViewAppointments.layoutManager
                    var currentPosition = 0
                    if (layoutManager is LinearLayoutManager) {
                        currentPosition = layoutManager.findFirstCompletelyVisibleItemPosition()
                    }
                    if (appointmentLists.size != (currentPosition + 1)) {
                        recyclerViewAppointments.smoothScrollToPosition(currentPosition + 1)
                    }
                    if (timeSlots.size != currentPosition + 1) {
                        txtTimeSlots.text = timeSlots[currentPosition + 1]
                    }
                }*/

                recyclerViewAppointments.addOnScrollListener(object :
                    RecyclerView.OnScrollListener() {
                    override fun onScrollStateChanged(
                        @NonNull recyclerView: RecyclerView,
                        newState: Int
                    ) {
                        super.onScrollStateChanged(recyclerView, newState)
                        when (newState) {
                            AbsListView.OnScrollListener.SCROLL_STATE_IDLE -> {
                                try {
                                    val layoutManager: RecyclerView.LayoutManager? =
                                        recyclerViewAppointments.layoutManager
                                    if (layoutManager is LinearLayoutManager) {
                                        val pos: Int =
                                            layoutManager.findFirstCompletelyVisibleItemPosition()
                                        txtTimeSlots.text = timeSlots[pos]
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }

                    }
                })
            } else {
                txtTimeSlots.text = ""
                recyclerViewAppointments.visibility = View.GONE
                cardViewAppointment.visibility = View.GONE
                txtNoAppointments.visibility = View.VISIBLE
                //txtViewAllAppointments.visibility = View.VISIBLE
            }
            //getRecommendedData()
        }
    }

    private fun getToken(appointment: GetAppointment) {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .getToken(
                        GetToken(appointment.appointment.appointment_id),
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
                            if(appointment.is_group_appointment){

                            } else{
                                //Start video call
                                replaceFragment(
                                    VideoCallFragment.newInstance(appointment, responseBody),
                                    R.id.layout_home,
                                    VideoCallFragment.TAG
                                )
                            }

                        } catch (e: Exception) {
                            hideProgress()
                            //displayToast("Something went wrong.. Please try after sometime")
                        }
                    }, { error ->
                        hideProgress()
                        if ((error as HttpException).code() == 401) {
                            userLogin(
                                preference!![PrefKeys.PREF_EMAIL]!!,
                                preference!![PrefKeys.PREF_PASS]!!
                            ) { result ->
                                getToken(appointment)
                            }
                        } else {
                            displayAfterLoginErrorMsg(error)
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    private fun displayVideoList() {
        /*if (isDebug) {
            videoLists.add(Video("Video 1", "", "Artist 1", "", "", ""))
            videoLists.add(Video("Video 2", "", "Artist 2", "", "", ""))
            videoLists.add(Video("Video 3", "", "Artist 3", "", "", ""))
        }*/
        getData("PI0014") { response ->
            var videoLists: ArrayList<Video> = arrayListOf()
            val videoList: Type = object : TypeToken<ArrayList<Video?>?>() {}.type
            videoLists = Gson().fromJson(response, videoList)

            if (videoLists.isNotEmpty()) {
                recyclerviewVideos.visibility = View.VISIBLE
                txtNoVideosData.visibility = View.GONE
                recyclerviewVideos.apply {
                    layoutManager = LinearLayoutManager(mActivity!!, RecyclerView.HORIZONTAL, false)
                    adapter =
                        DashboardVideosAdapter(mActivity!!, videoLists, this@DashboardFragment)
                }
            } else {
                recyclerviewVideos.visibility = View.GONE
                txtNoVideosData.visibility = View.VISIBLE
            }
            displayPodcastList()
        }
    }

    private fun displayPodcastList() {
        /*if (isDebug) {
            podcastLists.add(Podcast("Podcast 1", "", "Artist 1", ""))
            podcastLists.add(Podcast("Podcast 2", "", "Artist 2", ""))
            podcastLists.add(Podcast("Podcast 3", "", "Artist 3", ""))
        }*/
        getData("PI0020") { response ->
            val podcastLists: ArrayList<Podcast>
            val podcastList: Type = object : TypeToken<ArrayList<Podcast?>?>() {}.type
            podcastLists = Gson().fromJson(response, podcastList)

            if (podcastLists.isNotEmpty()) {
                recyclerViewPodcast.visibility = View.VISIBLE
                txtNoPodcastData.visibility = View.GONE
                recyclerViewPodcast.apply {
                    layoutManager = LinearLayoutManager(mActivity!!, RecyclerView.HORIZONTAL, false)
                    adapter =
                        DashboardPodcastAdapter(mActivity!!, podcastLists, this@DashboardFragment)
                }
            } else {
                recyclerViewPodcast.visibility = View.GONE
                txtNoPodcastData.visibility = View.VISIBLE
            }
            displayArticleList()
        }
    }

    private fun displayArticleList() {
        /*if (isDebug) {
            articlesLists.add(
                Articles(
                    "Ayurvedic practitioner shares herbs that will help manage diabetes",
                    "https://indianexpress.com/article/lifestyle/health/ayurveda-herbs-spices-manage-diabetes-immunity-blood-sugar-8009363/",
                    "06-07-2022 12:02"
                )
            )
            articlesLists.add(
                Articles(
                    "Listeria Outbreak Affects 23, What to Know",
                    "https://www.healthline.com/health-news/listeria-outbreak-affects-23-what-to-know",
                    "06-07-2022 13:02"
                )
            )
            articlesLists.add(
                Articles(
                    "Universal Flu Vaccine Gets Closer to Reality As Phase 1 Testing Starts",
                    "https://www.healthline.com/health-news/universal-flu-vaccine-gets-closer-to-reality-as-phase-1-testing-starts",
                    "06-07-2022 13:52"
                )
            )
        }*/
        getData("PI0011") { response ->
            var articlesLists: ArrayList<Articles> = arrayListOf()
            val articleList: Type = object : TypeToken<ArrayList<Articles?>?>() {}.type
            articlesLists = Gson().fromJson(response, articleList)

            if (articlesLists.isNotEmpty()) {
                recyclerViewArticles.visibility = View.VISIBLE
                txtNoArticlesData.visibility = View.GONE
                recyclerViewArticles.apply {
                    layoutManager = LinearLayoutManager(mActivity!!, RecyclerView.HORIZONTAL, false)
                    adapter =
                        DashboardArticlesAdapter(mActivity!!, articlesLists, this@DashboardFragment)
                }
            } else {
                recyclerViewArticles.visibility = View.GONE
                txtNoArticlesData.visibility = View.VISIBLE
            }
        }
    }

    private fun getAppointmentList(myCallback: (result: String?) -> Unit) {
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .getAppointmentList(
                        AppointmentPatientId(preference!![PrefKeys.PREF_PATIENT_ID, ""]!!.toInt()),
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
                            myCallback.invoke(responseBody)
                        } catch (e: Exception) {
                            hideProgress()
                            //displayToast("Something went wrong.. Please try after sometime")
                        }
                    }, { error ->
                        hideProgress()
                        if ((error as HttpException).code() == 401) {
                            userLogin(
                                preference!![PrefKeys.PREF_EMAIL]!!,
                                preference!![PrefKeys.PREF_PASS]!!
                            ) { result ->
                                displayAppointments()
                            }
                        } else {
                            displayAfterLoginErrorMsg(error)
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    private fun cancelAppointment(appointment: GetAppointment) {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .cancelAppointment(
                        CancelAppointment(
                            preference!![PrefKeys.PREF_PATIENT_ID, ""]!!.toInt(),
                            appointment.appointment.appointment_id
                        ), getAccessToken()
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
                            displayAppointments()
                        } catch (e: Exception) {
                            hideProgress()
                            displayToast("Something went wrong.. Please try after sometime")
                        }
                    }, { error ->
                        hideProgress()
                        //displayToast("Error ${error.localizedMessage}")
                        if ((error as HttpException).code() == 401) {
                            userLogin(
                                preference!![PrefKeys.PREF_EMAIL]!!,
                                preference!![PrefKeys.PREF_PASS]!!
                            ) { result ->
                                cancelAppointment(appointment)
                            }
                        } else {
                            displayAfterLoginErrorMsg(error)
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    private fun getRecommendedData() {
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .getRecommendedData(getAccessToken())
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

                            val recommendData: ArrayList<RecommendedData> = ArrayList()
                            val jsonObj = JSONObject(responseBody)

                            val videoArr = jsonObj.getJSONArray("videos")
                            for (i in 0 until videoArr.length()) {
                                val recommendedData = RecommendedData()
                                recommendedData.type = 0
                                recommendedData.id =
                                    videoArr.getJSONObject(i).getString("id").toInt()
                                recommendedData.name = videoArr.getJSONObject(i).getString("name")
                                recommendedData.description =
                                    videoArr.getJSONObject(i).getString("description")
                                recommendedData.video_url =
                                    videoArr.getJSONObject(i).getString("video_url")
                                recommendData.add(recommendedData)
                            }

                            val podcastArr = jsonObj.getJSONArray("podcasts")
                            for (i in 0 until podcastArr.length()) {
                                val recommendedData = RecommendedData()
                                recommendedData.type = 1
                                recommendedData.id =
                                    podcastArr.getJSONObject(i).getString("id").toInt()
                                recommendedData.name = podcastArr.getJSONObject(i).getString("name")
                                recommendedData.description =
                                    podcastArr.getJSONObject(i).getString("description")
                                recommendedData.podcast_image =
                                    podcastArr.getJSONObject(i).getString("podcast_image")
                                recommendedData.artist =
                                    podcastArr.getJSONObject(i).getString("artist")
                                recommendedData.podcast_url =
                                    podcastArr.getJSONObject(i).getString("podcast_url")
                                recommendData.add(recommendedData)
                            }

                            val articlesArr = jsonObj.getJSONArray("articles")
                            for (i in 0 until articlesArr.length()) {
                                val recommendedData = RecommendedData()
                                recommendedData.type = 2
                                recommendedData.id =
                                    articlesArr.getJSONObject(i).getString("id").toInt()
                                recommendedData.name =
                                    articlesArr.getJSONObject(i).getString("name")
                                recommendedData.description =
                                    articlesArr.getJSONObject(i).getString("description")
                                recommendedData.banner_image =
                                    articlesArr.getJSONObject(i).getString("banner_image")
                                recommendedData.published_date =
                                    articlesArr.getJSONObject(i).getString("published_date")
                                recommendedData.article_url =
                                    articlesArr.getJSONObject(i).getString("article_url")
                                recommendData.add(recommendedData)
                            }

                            if (recommendData.isNotEmpty()) {
                                recyclerviewRecommendedForYou.visibility = View.VISIBLE
                                txtNoRecommendedForYouData.visibility = View.GONE
                                recyclerviewRecommendedForYou.apply {
                                    layoutManager =
                                        LinearLayoutManager(
                                            mActivity!!,
                                            RecyclerView.HORIZONTAL,
                                            false
                                        )
                                    adapter =
                                        DashboardRecommendedAdapter(
                                            mActivity!!,
                                            recommendData,
                                            this@DashboardFragment
                                        )
                                }
                            } else {
                                recyclerviewRecommendedForYou.visibility = View.GONE
                                txtNoRecommendedForYouData.visibility = View.VISIBLE
                            }

                            displayVideoList()
                        } catch (e: Exception) {
                            hideProgress()
                            //displayToast("Something went wrong.. Please try after sometime")
                            displayVideoList()
                        }
                    }, { error ->
                        hideProgress()
                        if ((error as HttpException).code() == 401) {
                            userLogin(
                                preference!![PrefKeys.PREF_EMAIL]!!,
                                preference!![PrefKeys.PREF_PASS]!!
                            ) { result ->
                                getRecommendedData()
                            }
                        } else {
                            displayAfterLoginErrorMsg(error)
                            txtNoRecommendedForYouData.visibility = View.VISIBLE
                            displayVideoList()
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    override fun onPause() {
        super.onPause()
        if (mediaPlayer != null) {
            mediaPlayer?.stop()
            mediaPlayer?.reset()
        }
    }

    private fun checkPermissionsAndRun(fitActionRequestCode: FitActionRequestCode) {
        if (permissionApproved()) {
            fitSignIn(fitActionRequestCode)
        } else {
            requestRuntimePermissions(fitActionRequestCode)
        }
    }

    /**
     * Checks that the user is signed in, and if so, executes the specified function. If the user is
     * not signed in, initiates the sign in flow, specifying the post-sign in function to execute.
     *
     * @param requestCode The request code corresponding to the action to perform after sign in.
     */
    private fun fitSignIn(requestCode: FitActionRequestCode) {
        try {
            if (oAuthPermissionsApproved()) {
                performActionForRequestCode(requestCode)
            } else {
                requestCode.let {
                    GoogleSignIn.requestPermissions(
                        this,
                        GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
                        getGoogleAccount(), fitnessOptions
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Handles the callback from the OAuth sign in flow, executing the post sign in function
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (resultCode) {
            AppCompatActivity.RESULT_OK -> {
                val postSignInAction = FitActionRequestCode.values()[requestCode]
                postSignInAction.let {
                    performActionForRequestCode(postSignInAction)
                }
            }
            else -> oAuthErrorMsg(requestCode, resultCode)
        }
    }

    /**
     * Runs the desired method, based on the specified request code. The request code is typically
     * passed to the Fit sign-in flow, and returned with the success callback. This allows the
     * caller to specify which method, post-sign-in, should be called.
     *
     * @param requestCode The code corresponding to the action to perform.
     */
    private fun performActionForRequestCode(requestCode: FitActionRequestCode) =
        when (requestCode) {
            FitActionRequestCode.READ_DATA -> readData()
            FitActionRequestCode.SUBSCRIBE -> subscribe()
        }

    private fun oAuthErrorMsg(requestCode: Int, resultCode: Int) {
        val message = """
            There was an error signing into Fit. Check the troubleshooting section of the README
            for potential issues.
            Request code was: $requestCode
            Result code was: $resultCode
        """.trimIndent()
        Log.e(TAG, message)
    }

    private fun oAuthPermissionsApproved() =
        GoogleSignIn.hasPermissions(getGoogleAccount(), fitnessOptions)

    /**
     * Gets a Google account for use in creating the Fitness client. This is achieved by either
     * using the last signed-in account, or if necessary, prompting the user to sign in.
     * `getAccountForExtension` is recommended over `getLastSignedInAccount` as the latter can
     * return `null` if there has been no sign in before.
     */
    private fun getGoogleAccount() =
        GoogleSignIn.getAccountForExtension(requireActivity(), fitnessOptions)

    /** Records step data by requesting a subscription to background step data.  */
    private fun subscribe() {
        // To create a subscription, invoke the Recording API. As soon as the subscription is
        // active, fitness data will start recording.
        Fitness.getRecordingClient(requireActivity(), getGoogleAccount())
            .subscribe(DataType.TYPE_STEP_COUNT_CUMULATIVE)
            //.listSubscriptions()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.i(TAG, "Successfully subscribed!")
                    fitSignIn(FitActionRequestCode.READ_DATA)
                } else {
                    Log.w(TAG, "There was a problem subscribing.", task.exception)
                }
            }
    }

    private fun convertMeterToKilometer(meter: Float): Float {
        return (meter * 0.001).toFloat()
    }

    /**
     * Reads the current daily step total, computed from midnight of the current day on the device's
     * current timezone.
     */
    private fun readData() {
        Fitness.getHistoryClient(requireActivity(), getGoogleAccount())
            .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
            .addOnSuccessListener { dataSet ->
                val total = when {
                    dataSet.isEmpty -> 0
                    else -> dataSet.dataPoints.first().getValue(Field.FIELD_STEPS).asInt()
                }
                Log.i(TAG, "Total steps: $total")
                txtStepsValue.text = total.toString()

                txtStepsValue.text = "1537"
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "There was a problem getting the step count.", e)
            }

        Fitness.getHistoryClient(requireActivity(), getGoogleAccount())
            .readDailyTotal(DataType.TYPE_DISTANCE_DELTA)
            .addOnSuccessListener { dataSet ->
                val total = when {
                    dataSet.isEmpty -> 0
                    else -> dataSet.dataPoints.first().getValue(Field.FIELD_DISTANCE).asFloat()
                }
                Log.i(TAG, "Total distance: $total")
                txtDistanceValue.text =
                    String.format("%.2f", convertMeterToKilometer(total.toFloat())) + " KM"

                txtDistanceValue.text = "1.10 KM"
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "There was a problem getting the step count.", e)
            }

        Fitness.getHistoryClient(requireActivity(), getGoogleAccount())
            .readDailyTotal(DataType.TYPE_HEART_RATE_BPM)
            .addOnSuccessListener { dataSet ->
                val total = when {
                    dataSet.isEmpty -> 0
                    else -> dataSet.dataPoints.first().getValue(Field.FIELD_AVERAGE)
                }
                Log.i(TAG, "Total BPM: $total")
                //txt_heart_rate.text = "$total"
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "There was a problem getting the step count.", e)
            }
        Fitness.getHistoryClient(requireActivity(), getGoogleAccount())
            .readDailyTotal(DataType.TYPE_CALORIES_EXPENDED)
            .addOnSuccessListener { dataSet ->
                var total = when {
                    dataSet.isEmpty -> 0
                    else -> dataSet.dataPoints.first().getValue(Field.FIELD_CALORIES)
                }
                if (total == null) {
                    total = ""
                }
                Log.i(TAG, "Total Calories: $total")
                txtCaloriesValue.text = "$total KCAL"
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "There was a problem getting the step count.", e)
            }

        val sdf = SimpleDateFormat("MM/dd/yyyy' 'HH:mm:ss")
        val currentDate = sdf.format(Date())
        val currentDateTime = DateUtils(currentDate)

        txtViewUpdatedAt.text = "Updated today at " + currentDateTime.getTimeWithFormat()
    }

    private fun permissionApproved(): Boolean {
        val approved = if (runningQOrLater) {
            PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACTIVITY_RECOGNITION
            )
        } else {
            true
        }
        return approved
    }

    private fun requestRuntimePermissions(requestCode: FitActionRequestCode) {
        val shouldProvideRationale =
            ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.ACTIVITY_RECOGNITION
            )

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        requestCode.let {
            if (shouldProvideRationale) {
                Log.i(TAG, "Displaying permission rationale to provide additional context.")
                Snackbar.make(
                    dashboardLayout,
                    "Permission Rationale",
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction("OK") {
                        // Request permission
                        ActivityCompat.requestPermissions(
                            requireActivity(),
                            arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                            1001
                        )
                    }
                    .show()
            } else {
                Log.i(TAG, "Requesting permission")
                // Request permission. It's possible this can be auto answered if device policy
                // sets the permission in a given state or the user denied the permission
                // previously and checked "Never ask again".
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                    requestCode.ordinal
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when {
            grantResults.isEmpty() -> {
                // If user interaction was interrupted, the permission request
                // is cancelled and you receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.")
            }
            grantResults[0] == PackageManager.PERMISSION_GRANTED -> {
                // Permission was granted.
                val fitActionRequestCode = FitActionRequestCode.values()[requestCode]
                fitActionRequestCode.let {
                    fitSignIn(fitActionRequestCode)
                }
            }
            else -> {
                // Permission denied.

                // In this Activity we've chosen to notify the user that they
                // have rejected a core permission for the app since it makes the Activity useless.
                // We're communicating this message in a Snackbar since this is a sample app, but
                // core permissions would typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.

                Snackbar.make(
                    dashboardLayout,
                    "Permission Denied",
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction(R.string.settings) {
                        // Build intent that displays the App settings screen.
                        val intent = Intent()
                        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        val uri = Uri.fromParts(
                            "package",
                            BuildConfig.APPLICATION_ID, null
                        )
                        intent.data = uri
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                    }
                    .show()
            }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment DashboardFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            DashboardFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_dashboard"
    }

    override fun onNewsItemClicked(articles: Articles) {
        //Navigate to News detail screen
        replaceFragment(
            NewsDetailFragment.newInstance(articles),
            R.id.layout_home,
            NewsDetailFragment.TAG
        )
    }

    override fun onPodcastItemClicked(podcast: Podcast) {
        //Play podcast
        mediaPlayer = MediaPlayer()
        AudioStream(requireActivity(), podcast, mediaPlayer!!).streamAudio()
    }

    override fun onJournalItemClicked(journal: Journal, isDelete: Boolean) {
        if (isDelete) {
            //Delete Journal
        } else {
            //Navigate to Journal detail screen
            replaceFragment(
                DetailJournalFragment.newInstance(journal),
                R.id.layout_home,
                DetailJournalFragment.TAG
            )
        }
    }

    override fun onAppointmentItemClickListener(
        appointment: GetAppointment,
        isStartAppointment: Boolean
    ) {
        if (isStartAppointment) {
            //Start video call
            /*replaceFragment(
                VideoCallFragment.newInstance(appointment),
                R.id.layout_home,
                VideoCallFragment.TAG
            )*/
        } else {
            val builder = AlertDialog.Builder(requireActivity())
            builder.setTitle("Alert")
            builder.setMessage("Do you really want to cancel this appointment?")
            builder.setPositiveButton("Yes") { dialogInterface, _ ->
                dialogInterface.dismiss()
                //Cancel the appointment
                cancelAppointment(appointment)
            }
            //performing cancel action
            builder.setNeutralButton("Cancel") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            val alert = builder.create()
            alert.setCancelable(false)
            alert.show()
        }
    }

    override fun onVideoItemClickListener(video: Video) {
        replaceFragment(
            VideoDetailFragment.newInstance(video),
            R.id.layout_home,
            VideoDetailFragment.TAG
        )
    }

    override fun onRecommendedItemClickListener(recommendedData: RecommendedData) {
        when (recommendedData.type) {
            Utils.RECOMMENDED_VIDEOS -> {
                val video = Video(
                    recommendedData.id,
                    recommendedData.name,
                    recommendedData.description,
                    recommendedData.video_url,
                    "",
                    "",
                    ""
                )
                replaceFragment(
                    VideoDetailFragment.newInstance(video),
                    R.id.layout_home,
                    VideoDetailFragment.TAG
                )
            }
            Utils.RECOMMENDED_PODCAST -> {
                val podcast = Podcast(
                    recommendedData.id,
                    recommendedData.name,
                    recommendedData.description,
                    recommendedData.podcast_image,
                    recommendedData.artist,
                    recommendedData.podcast_url
                )
                //Play podcast
                mediaPlayer = MediaPlayer()
                AudioStream(requireActivity(), podcast, mediaPlayer!!).streamAudio()
            }
            Utils.RECOMMENDED_ARTICLES -> {
                val article = Articles(
                    recommendedData.id,
                    recommendedData.name,
                    recommendedData.description,
                    "",
                    recommendedData.article_url,
                    recommendedData.published_date
                )
                //Navigate to News detail screen
                replaceFragment(
                    NewsDetailFragment.newInstance(article),
                    R.id.layout_home,
                    NewsDetailFragment.TAG
                )
            }
        }
    }
}