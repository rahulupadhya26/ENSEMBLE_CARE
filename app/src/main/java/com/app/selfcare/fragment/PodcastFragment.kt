package com.app.selfcare.fragment

import android.media.MediaPlayer
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.app.selfcare.R
import com.app.selfcare.adapters.PodcastListAdapter
import com.app.selfcare.controller.OnPodcastItemClickListener
import com.app.selfcare.data.Podcast
import com.app.selfcare.utils.AudioStream
import kotlinx.android.synthetic.main.fragment_podcast.*

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
    private var param1: String? = null
    private var param2: String? = null
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
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

        val podcastLists: ArrayList<Podcast> = arrayListOf()
        podcastLists.add(Podcast("Sample 1", "", "Artist 1", ""))
        podcastLists.add(Podcast("Sample 2", "", "Artist 2", ""))
        podcastLists.add(Podcast("Sample 3", "", "Artist 3", ""))
        recyclerViewPodcastList.apply {
            layoutManager = GridLayoutManager(
                mActivity!!,
                2
            )
            adapter = PodcastListAdapter(
                mActivity!!,
                podcastLists, this@PodcastFragment
            )
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
        fun newInstance(param1: String, param2: String) =
            PodcastFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
        const val TAG = "Screen_podcast_list"
    }

    override fun onPodcastItemClicked(podcast: Podcast) {
        AudioStream(requireActivity(), podcast, mediaPlayer!!).streamAudio()
    }
}