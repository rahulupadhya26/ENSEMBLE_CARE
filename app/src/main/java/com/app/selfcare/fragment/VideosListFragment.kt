package com.app.selfcare.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.selfcare.R
import com.app.selfcare.adapters.VideosAdapter
import com.app.selfcare.controller.OnVideoItemClickListener
import com.app.selfcare.data.Video
import com.app.selfcare.databinding.FragmentActivityCarePlanBinding
import com.app.selfcare.databinding.FragmentVideosListBinding
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper.get
import com.app.selfcare.utils.Utils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject
import retrofit2.HttpException
import java.lang.reflect.Type

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val ARG_PARAM3 = "param3"
private const val ARG_PARAM4 = "param4"

/**
 * A simple [Fragment] subclass.
 * Use the [VideosListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class VideosListFragment : BaseFragment(), OnVideoItemClickListener {
    // TODO: Rename and change types of parameters
    private var videos: ArrayList<Video>? = null
    private var wellnessType: String? = null
    private var isFavourite: Boolean = false
    private var category: String? = null
    private lateinit var binding: FragmentVideosListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            videos = it.getParcelableArrayList(ARG_PARAM1)
            wellnessType = it.getString(ARG_PARAM2)
            isFavourite = it.getBoolean(ARG_PARAM3)
            category = it.getString(ARG_PARAM4)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentVideosListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getLayout(): Int {
        return R.layout.fragment_videos_list
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE
        updateStatusBarColor(R.color.white)

        binding.videosBack.setOnClickListener {
            popBackStack()
        }

        if (videos != null && videos!!.isNotEmpty()) {
            displayVideos(videos!!)
        } else {
            fetchVideos()
        }
    }

    private fun fetchVideos() {
        when (wellnessType!!) {
            Utils.WELLNESS_EXERCISE -> {
                if (isFavourite) {
                    getFavDetailVideoData()
                } else {
                    getDetailVideoData("exercise_data/")
                }
            }
            Utils.WELLNESS_NUTRITION -> {
                if (isFavourite) {
                    getFavDetailVideoData()
                } else {
                    getDetailVideoData("nutrition_data/")
                }
            }
            Utils.WELLNESS_MINDFULNESS -> {
                if (isFavourite) {
                    getFavDetailVideoData()
                } else {
                    getDetailVideoData("mindfulness_data/")
                }
            }
            Utils.WELLNESS_YOGA -> {
                if (isFavourite) {
                    getFavDetailVideoData()
                } else {
                    getDetailVideoData("yoga_data/")
                }
            }
            Utils.WELLNESS_MUSIC -> {
                if (isFavourite) {
                    getFavDetailVideoData()
                } else {
                    getDetailVideoData("music_data/")
                }
            }
            else -> {
                getVideosList()
            }
        }
    }

    private fun getFavDetailVideoData() {
        getFavoriteData(wellnessType!!) { response ->
            videos = arrayListOf()
            val jsonObj = JSONObject(response!!)
            val videoList: Type = object : TypeToken<ArrayList<Video?>?>() {}.type
            videos = Gson().fromJson(jsonObj.getString("videos"), videoList)
            displayVideos(videos!!)
        }
    }

    private fun getDetailVideoData(type: String) {
        getDetailData(type, category!!) { response ->
            videos = arrayListOf()
            val jsonObj = JSONObject(response!!)
            val videoList: Type = object : TypeToken<ArrayList<Video?>?>() {}.type
            videos = Gson().fromJson(jsonObj.getString("videos"), videoList)
            displayVideos(videos!!)
        }
    }

    private fun getVideosList() {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .getResourceDashboardData("Dashboard", getAccessToken())
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
                            //Videos
                            val videoList: Type = object : TypeToken<ArrayList<Video?>?>() {}.type
                            val videoLists: ArrayList<Video> =
                                Gson().fromJson(jsonObj.getString("videos"), videoList)
                            displayVideos(videoLists)
                        } catch (e: Exception) {
                            hideProgress()
                            displayToast("Something went wrong.. Please try after sometime")
                        }
                    }, { error ->
                        hideProgress()
                        if ((error as HttpException).code() == 401) {
                            userLogin(
                                preference!![PrefKeys.PREF_EMAIL]!!,
                                preference!![PrefKeys.PREF_PASS]!!
                            ) { result ->
                                getVideosList()
                            }
                        } else {
                            displayAfterLoginErrorMsg(error)
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    private fun displayVideos(videos: ArrayList<Video>) {
        if (videos.isNotEmpty()) {
            binding.shimmerVideosList.stopShimmer()
            binding.shimmerVideosList.visibility = View.GONE
            binding.recyclerViewVideosList.visibility = View.VISIBLE
            binding.recyclerViewVideosList.apply {
                layoutManager =
                    LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false)
                adapter = VideosAdapter(
                    mContext!!,
                    videos,
                    wellnessType!!.isEmpty(),
                    this@VideosListFragment
                )
                binding.txtNoVideos.visibility = View.GONE
            }
        } else {
            binding.shimmerVideosList.stopShimmer()
            binding.shimmerVideosList.visibility = View.GONE
            binding.recyclerViewVideosList.visibility = View.GONE
            binding.txtNoVideos.visibility = View.VISIBLE
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment VideosListFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: ArrayList<Video>, param2: String, param3: Boolean, param4: String) =
            VideosListFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                    putBoolean(ARG_PARAM3, param3)
                    putString(ARG_PARAM4, param4)
                }
            }

        const val TAG = "Screen_videos_list"
    }

    override fun onVideoItemClickListener(video: Video, isFav: Boolean, isWellness: Boolean) {
        if (isFav) {
            if (isWellness) {
                sendResourceFavoriteData(video.id, "Video", !video.is_favourite) {
                    fetchVideos()
                }
            } else {
                sendFavoriteData(video.id, "Video", !video.is_favourite, wellnessType!!) {
                    fetchVideos()
                }
            }
        } else {
            replaceFragment(
                VideoDetailFragment.newInstance(video),
                R.id.layout_home,
                VideoDetailFragment.TAG
            )
        }
    }
}