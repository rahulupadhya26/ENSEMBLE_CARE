package com.app.selfcare.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.LinearLayout
import com.app.selfcare.R
import com.app.selfcare.data.Video
import com.app.selfcare.utils.ExpoPlayerUtils
import kotlinx.android.synthetic.main.fragment_video_detail.*
import kotlinx.android.synthetic.main.layout_item_video.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val ARG_PARAM3 = "param3"

/**
 * A simple [Fragment] subclass.
 * Use the [VideoDetailFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class VideoDetailFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var videoDetail: Video? = null
    private var param2: String? = null
    private var expoPlayerUtils: ExpoPlayerUtils? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            videoDetail = it.getParcelable(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_video_detail
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.VISIBLE
        getSubTitle().visibility = View.GONE

        if (videoDetail != null)
            displayVideoDetails()
    }

    private fun displayVideoDetails() {
        layout_details_video.visibility = View.VISIBLE
        txtVideoTitle.text = videoDetail!!.name
        txtVideoTitle.maxLines = 10
        txtVideoDesc.text = videoDetail!!.description
        txtVideoDesc.maxLines = 1000
        layout_video.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        )
        img_video.visibility = View.GONE
        videosPlayer.visibility = View.VISIBLE
        expoPlayerUtils = ExpoPlayerUtils()
        expoPlayerUtils!!.initializePlayer(mActivity!!, videosPlayer, videoDetail!!.video_url)
        imgVideoPlay.visibility = View.GONE
    }

    override fun onPause() {
        super.onPause()
        try {
            expoPlayerUtils!!.releasePlayer()
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
         * @return A new instance of fragment VideoDetailFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(videoDetail: Video? = null, param2: String = "", videoId: String = "") =
            VideoDetailFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM1, videoDetail)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_Video_Details"
    }
}