package com.app.selfcare.fragment

import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.app.selfcare.R
import com.app.selfcare.adapters.PodcastListAdapter
import com.app.selfcare.controller.OnPodcastItemClickListener
import com.app.selfcare.data.Podcast
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper.get
import com.app.selfcare.utils.AudioStream
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_podcast.*
import retrofit2.HttpException
import java.lang.reflect.Type

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PodcastFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PodcastFragment : BaseFragment(), OnPodcastItemClickListener {
    // TODO: Rename and change types of parameters
    private var podcasts: ArrayList<Podcast>? = null
    private var param2: String? = null
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            podcasts = it.getParcelableArrayList(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_podcast
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.VISIBLE
        getSubTitle().visibility = View.GONE
        mediaPlayer = MediaPlayer()

        /*val podcastLists: ArrayList<Podcast> = arrayListOf()
        podcastLists.add(Podcast("Sample 1", "", "Artist 1", ""))
        podcastLists.add(Podcast("Sample 2", "", "Artist 2", ""))
        podcastLists.add(Podcast("Sample 3", "", "Artist 3", ""))*/
        if (podcasts != null && podcasts!!.isNotEmpty()) {
            displayPodcasts(podcasts!!)
        } else {
            getPodcastList()
        }
    }

    private fun getPodcastList() {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .getData("PI0020", getAccessToken())
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
                            val podcastLists: ArrayList<Podcast>
                            val podcastList: Type =
                                object : TypeToken<ArrayList<Podcast?>?>() {}.type
                            podcastLists = Gson().fromJson(responseBody, podcastList)
                            displayPodcasts(podcastLists)
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
                                getPodcastList()
                            }
                        } else {
                            displayAfterLoginErrorMsg(error)
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    private fun displayPodcasts(podcasts: ArrayList<Podcast>) {
        if (podcasts.isNotEmpty()) {
            txtNoPodcasts.visibility = View.GONE
            recyclerViewPodcastList.visibility = View.VISIBLE
            recyclerViewPodcastList.apply {
                layoutManager = GridLayoutManager(
                    mActivity!!,
                    2
                )
                adapter = PodcastListAdapter(
                    mActivity!!,
                    podcasts, this@PodcastFragment
                )
            }
        } else {
            txtNoPodcasts.visibility = View.VISIBLE
            recyclerViewPodcastList.visibility = View.GONE
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
         * @return A new instance of fragment PodcastFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: ArrayList<Podcast>, param2: String) =
            PodcastFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_podcast_list"
    }

    override fun onPodcastItemClicked(podcast: Podcast) {
        AudioStream(requireActivity(), podcast, mediaPlayer!!).streamAudio()
    }
}