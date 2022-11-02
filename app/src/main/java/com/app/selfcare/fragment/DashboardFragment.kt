package com.app.selfcare.fragment

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AbsListView
import androidx.annotation.NonNull
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.R
import com.app.selfcare.adapters.*
import com.app.selfcare.controller.*
import com.app.selfcare.data.*
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper.get
import com.app.selfcare.utils.AudioStream
import com.app.selfcare.utils.DateUtils
import com.app.selfcare.utils.Utils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.koushikdutta.async.Util
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_dashboard.*
import kotlinx.android.synthetic.main.fragment_login.*
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
    private val isDebug: Boolean = true
    private var mediaPlayer: MediaPlayer? = null

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
        getHeader().visibility = View.VISIBLE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE
        mActivity!!.setUserDetails()

        //Appointments
        displayAppointments(true)

        handleViewMoreEvents()

        img_user_pic.setOnClickListener {
            replaceFragment(
                ProfileFragment(),
                R.id.layout_home,
                ProfileFragment.TAG
            )
        }

        layoutGroupAppointments.setOnClickListener {
            replaceFragment(
                GroupAppointmentsFragment(),
                R.id.layout_home,
                GroupAppointmentsFragment.TAG
            )
        }

        layoutArticles.setOnClickListener {
            replaceFragment(
                NewsListFragment(),
                R.id.layout_home,
                NewsListFragment.TAG
            )
        }

        /*layoutCoaches.setOnClickListener {
            //displayMsg("Message", "Screen under construction!")
            replaceFragment(
                CoachesFragment(),
                R.id.layout_home,
                CoachesFragment.TAG
            )
        }*/

        layoutAssessments.setOnClickListener {
            replaceFragment(
                MyAssessmentsFragment(),
                R.id.layout_home,
                MyAssessmentsFragment.TAG
            )
        }

        layoutGoals.setOnClickListener {
            replaceFragment(
                GoalsFragment(),
                R.id.layout_home,
                GoalsFragment.TAG
            )
        }

        fabCreateAppointmentBtn.setOnClickListener {
            replaceFragment(
                TherapistListFragment.newInstance(false),
                R.id.layout_home,
                TherapistListFragment.TAG
            )
        }

        itemsSwipeToRefresh.setOnRefreshListener {
            displayAppointments(true)
        }

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

    private fun displayAppointments(refresh: Boolean) {
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
        val appointmentLists: ArrayList<Appointment> = arrayListOf()
        val timeSlots: ArrayList<String> = arrayListOf()
        val sdf = SimpleDateFormat("MM/dd/yyyy' 'HH:mm:ss")
        val currentDate = sdf.format(Date())
        val currentDateTime = DateUtils(currentDate)
        txtMonth.text = currentDateTime.getFullMonthName()
        txtCurrentDate.text = currentDateTime.getDay()
        getAppointmentList { response ->
            val jsonArr = JSONArray(response)
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
            }
            if (appointmentLists.isNotEmpty()) {
                recyclerViewAppointments.visibility = View.VISIBLE
                txtNoAppointments.visibility = View.GONE
                txtViewAllAppointments.visibility = View.VISIBLE
                recyclerViewAppointments.apply {
                    layoutManager = LinearLayoutManager(mActivity!!, RecyclerView.HORIZONTAL, false)
                    adapter = DashboardAppointmentAdapter(
                        mActivity!!,
                        appointmentLists,
                        this@DashboardFragment
                    )
                }
                if (timeSlots.isNotEmpty()) {
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
                }

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
                txtNoAppointments.visibility = View.VISIBLE
                txtViewAllAppointments.visibility = View.VISIBLE
            }
            getRecommendedData()
        }
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
                            displayVideoList()
                        }
                    }, { error ->
                        hideProgress()
                        if ((error as HttpException).code() == 401) {
                            userLogin(
                                preference!![PrefKeys.PREF_EMAIL]!!,
                                preference!![PrefKeys.PREF_PASS]!!
                            ) { result ->
                                displayAppointments(true)
                            }
                        } else {
                            displayAfterLoginErrorMsg(error)
                            displayVideoList()
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    private fun cancelAppointment(appointment: Appointment) {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .cancelAppointment(
                        CancelAppointment(
                            preference!![PrefKeys.PREF_PATIENT_ID, ""]!!.toInt(),
                            appointment.appointment_id
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
                            displayAppointments(false)
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
        appointment: Appointment,
        isStartAppointment: Boolean
    ) {
        if (isStartAppointment) {
            //Start video call
            replaceFragment(
                VideoCallFragment.newInstance(appointment),
                R.id.layout_home,
                VideoCallFragment.TAG
            )
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