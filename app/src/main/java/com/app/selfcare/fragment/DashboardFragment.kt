package com.app.selfcare.fragment

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.R
import com.app.selfcare.adapters.DashboardAppointmentAdapter
import com.app.selfcare.adapters.DashboardJournalAdapter
import com.app.selfcare.adapters.DashboardNewsAdapter
import com.app.selfcare.adapters.DashboardPodcastAdapter
import com.app.selfcare.controller.OnAppointmentItemClickListener
import com.app.selfcare.controller.OnJournalItemClickListener
import com.app.selfcare.controller.OnNewsItemClickListener
import com.app.selfcare.controller.OnPodcastItemClickListener
import com.app.selfcare.data.Appointment
import com.app.selfcare.data.Journal
import com.app.selfcare.data.News
import com.app.selfcare.data.Podcast
import kotlinx.android.synthetic.main.fragment_dashboard.*
import com.app.selfcare.preference.PreferenceHelper.get
import com.app.selfcare.utils.AudioStream
import java.util.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [DashboardFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DashboardFragment : BaseFragment(), OnNewsItemClickListener, OnPodcastItemClickListener,
    OnJournalItemClickListener, OnAppointmentItemClickListener {
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

        handleData()
        handleViewMoreEvents()

        layoutVideos.setOnClickListener {
            replaceFragment(
                VideosListFragment(),
                R.id.layout_home,
                VideosListFragment.TAG
            )
        }

        layoutArticles.setOnClickListener {
            displayMsg("Message","Screen under construction!")
        }

        layoutCoaches.setOnClickListener {
            displayMsg("Message","Screen under construction!")
        }

        layoutGoals.setOnClickListener {
            displayMsg("Message","Screen under construction!")
        }

        fabCreateAppointmentBtn.setOnClickListener {
            replaceFragment(
                TherapistListFragment.newInstance(false),
                R.id.layout_home,
                TherapistListFragment.TAG
            )
        }
    }

    private fun handleData() {
        //Appointments
        val appointmentLists: ArrayList<Appointment> = arrayListOf()
        if (isDebug) {
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
        }
        if (appointmentLists.isNotEmpty()) {
            layoutAppointments.visibility = View.VISIBLE
            cardViewNoAppointments.visibility = View.GONE
            txtViewAllAppointments.visibility = View.VISIBLE
            recyclerViewAppointments.apply {
                layoutManager = LinearLayoutManager(mActivity!!, RecyclerView.HORIZONTAL, false)
                adapter = DashboardAppointmentAdapter(
                    mActivity!!,
                    appointmentLists,
                    this@DashboardFragment
                )
            }
        } else {
            layoutAppointments.visibility = View.GONE
            cardViewNoAppointments.visibility = View.VISIBLE
            txtViewAllAppointments.visibility = View.GONE
        }

        //Podcasts
        val podcastLists: ArrayList<Podcast> = arrayListOf()
        if (isDebug) {
            podcastLists.add(Podcast("Sample 1", "", "Artist 1", ""))
            podcastLists.add(Podcast("Sample 2", "", "Artist 2", ""))
            podcastLists.add(Podcast("Sample 3", "", "Artist 3", ""))
        }
        if (podcastLists.isNotEmpty()) {
            recyclerViewPodcast.visibility = View.VISIBLE
            txtNoPodcastData.visibility = View.GONE
            txtPodcastViewMore.visibility = View.VISIBLE
            recyclerViewPodcast.apply {
                layoutManager = LinearLayoutManager(mActivity!!, RecyclerView.HORIZONTAL, false)
                adapter = DashboardPodcastAdapter(mActivity!!, podcastLists, this@DashboardFragment)
            }
        } else {
            recyclerViewPodcast.visibility = View.GONE
            txtNoPodcastData.visibility = View.VISIBLE
            txtPodcastViewMore.visibility = View.GONE
        }

        //News
        val newsLists: ArrayList<News> = arrayListOf()
        if (isDebug) {
            newsLists.add(News("Ayurvedic practitioner shares herbs that will help manage diabetes", "https://indianexpress.com/article/lifestyle/health/ayurveda-herbs-spices-manage-diabetes-immunity-blood-sugar-8009363/", "06-07-2022 12:02"))
            newsLists.add(News("Listeria Outbreak Affects 23, What to Know", "https://www.healthline.com/health-news/listeria-outbreak-affects-23-what-to-know", "06-07-2022 13:02"))
            newsLists.add(News("Universal Flu Vaccine Gets Closer to Reality As Phase 1 Testing Starts", "https://www.healthline.com/health-news/universal-flu-vaccine-gets-closer-to-reality-as-phase-1-testing-starts", "06-07-2022 13:52"))
        }
        if (newsLists.isNotEmpty()) {
            recyclerViewNews.visibility = View.VISIBLE
            txtNoNewsData.visibility = View.GONE
            txtNewsViewMore.visibility = View.VISIBLE
            recyclerViewNews.apply {
                layoutManager = LinearLayoutManager(mActivity!!, RecyclerView.HORIZONTAL, false)
                adapter = DashboardNewsAdapter(mActivity!!, newsLists, this@DashboardFragment)
            }
        } else {
            recyclerViewNews.visibility = View.GONE
            txtNoNewsData.visibility = View.VISIBLE
            txtNewsViewMore.visibility = View.GONE
        }

        //Journals
        val journalLists: ArrayList<Journal> = arrayListOf()
        if (isDebug) {
            journalLists.add(
                Journal(
                    "Walking",
                    "Walk for 10 kms for good health",
                    "2022-07-06 11:12:06",
                    "July",
                    "2022",
                    "11:12"
                )
            )
            journalLists.add(
                Journal(
                    "Sleeping",
                    "Sleep for 8 hours for good health",
                    "2022-07-06 12:14:06",
                    "July",
                    "2022",
                    "12:14"
                )
            )
            journalLists.add(
                Journal(
                    "Eating",
                    "Eat 3 times a day good health",
                    "2022-07-06 12:15:16",
                    "July",
                    "2022",
                    "12:14"
                )
            )
        }
        if (journalLists.isNotEmpty()) {
            recyclerViewJournals.visibility = View.VISIBLE
            txtNoJournalData.visibility = View.GONE
            txtJournalsViewMore.visibility = View.VISIBLE
            recyclerViewJournals.apply {
                layoutManager = LinearLayoutManager(mActivity!!, RecyclerView.HORIZONTAL, false)
                adapter = DashboardJournalAdapter(mActivity!!, journalLists, this@DashboardFragment)
            }
        } else {
            recyclerViewJournals.visibility = View.GONE
            txtNoJournalData.visibility = View.VISIBLE
            txtJournalsViewMore.visibility = View.GONE
        }
    }

    private fun handleViewMoreEvents() {
        txtNewsViewMore.setOnClickListener {
            replaceFragment(NewsListFragment(), R.id.layout_home, NewsListFragment.TAG)
        }

        txtPodcastViewMore.setOnClickListener {
            replaceFragment(PodcastFragment(), R.id.layout_home, PodcastFragment.TAG)
        }

        txtJournalsViewMore.setOnClickListener {
            replaceFragment(JournalFragment(), R.id.layout_home, JournalFragment.TAG)
        }

        txtViewAllAppointments.setOnClickListener {
            displayMsg("Message","Screen under construction!")
        }
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

    override fun onNewsItemClicked(news: News) {
        //Navigate to News detail screen
        replaceFragment(
            NewsDetailFragment.newInstance(news),
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
            //Cancel the appointment
        }
    }
}