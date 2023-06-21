package ensemblecare.csardent.com.fragment

import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import ensemblecare.csardent.com.R
import ensemblecare.csardent.com.controller.IOnBackPressed
import ensemblecare.csardent.com.data.Video
import ensemblecare.csardent.com.databinding.FragmentVideoDetailBinding
import ensemblecare.csardent.com.utils.ExpoPlayerUtils

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
class VideoDetailFragment : BaseFragment(), IOnBackPressed {
    // TODO: Rename and change types of parameters
    private var videoDetail: Video? = null
    private var url: String? = null
    private var expoPlayerUtils: ExpoPlayerUtils? = null
    private lateinit var binding: FragmentVideoDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            videoDetail = it.getParcelable(ARG_PARAM1)
            url = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentVideoDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getLayout(): Int {
        return R.layout.fragment_video_detail
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE
        expoPlayerUtils = ExpoPlayerUtils()
        if (videoDetail != null) {
            displayVideoDetails()
        } else {
            expoPlayerUtils!!.initializePlayer(mActivity!!, binding.videosPlayer, url)
            binding.imgVideoPlay.visibility = View.GONE
        }
    }

    private fun displayVideoDetails() {
        binding.layoutDetailsVideo.visibility = View.VISIBLE
        binding.txtVideoTitle.text = videoDetail!!.name
        binding.txtVideoTitle.maxLines = 10
        binding.txtVideoDesc.text = videoDetail!!.description
        binding.txtVideoDesc.maxLines = 1000
        binding.imgVideo.visibility = View.GONE
        binding.videosPlayer.visibility = View.VISIBLE
        expoPlayerUtils!!.initializePlayer(
            mActivity!!,
            binding.videosPlayer,
            videoDetail!!.video_url
        )
        binding.imgVideoPlay.visibility = View.GONE

    }

    override fun onPause() {
        super.onPause()
        try {
            expoPlayerUtils!!.releasePlayer()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
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
        fun newInstance(videoDetail: Video? = null, param2: String = "") =
            VideoDetailFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM1, videoDetail)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_Video_Details"
    }

    override fun onBackPressed(): Boolean {
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        return false
    }
}