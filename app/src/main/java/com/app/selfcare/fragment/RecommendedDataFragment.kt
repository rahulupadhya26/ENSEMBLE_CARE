package com.app.selfcare.fragment

import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.R
import com.app.selfcare.adapters.*
import com.app.selfcare.controller.OnGoalItemClickListener
import com.app.selfcare.controller.OnNewsItemClickListener
import com.app.selfcare.controller.OnPodcastItemClickListener
import com.app.selfcare.controller.OnVideoItemClickListener
import com.app.selfcare.data.*
import com.app.selfcare.databinding.FragmentActivityCarePlanBinding
import com.app.selfcare.databinding.FragmentRecommendedDataBinding
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper.get
import com.app.selfcare.utils.AudioStream
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject
import retrofit2.HttpException
import java.lang.reflect.Type
import java.util.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [RecommendedDataFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RecommendedDataFragment : BaseFragment(),
    OnPodcastItemClickListener, OnNewsItemClickListener, OnGoalItemClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var mediaPlayer: MediaPlayer? = null
    private var recommended: Recommended = Recommended()
    private lateinit var binding: FragmentRecommendedDataBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRecommendedDataBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getLayout(): Int {
        return R.layout.fragment_recommended_data
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.VISIBLE
        getSubTitle().visibility = View.GONE

        getRecommendedData()

        binding.txtViewAllRecommendedVideos.setOnClickListener {
            /*if (recommended.videos.isNotEmpty()) {
                replaceFragment(
                    VideosListFragment.newInstance(recommended.videos, ""),
                    R.id.layout_home,
                    VideosListFragment.TAG
                )
            } else {
                displayToast("Currently there are no recommended videos.")
            }*/
        }

        binding.txtViewAllRecommendedProviderGoals.setOnClickListener {
            if (recommended.provider_goals.isNotEmpty()) {
                replaceFragment(
                    FacilityGoalFragment.newInstance(recommended.provider_goals, ""),
                    R.id.layout_home,
                    FacilityGoalFragment.TAG
                )
            } else {
                displayToast("Currently there are no recommended provider goals.")
            }
        }
    }

    private fun getRecommendedData() {
        showProgress()
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
                            val recommend: Type = object : TypeToken<Recommended?>() {}.type
                            recommended = Gson().fromJson(responseBody, recommend)

                            //displayVideos(recommended)
                            displayPodcasts(recommended)
                            displayArticles(recommended)
                            displayProviderGoals(recommended)
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
                                getRecommendedData()
                            }
                        } else {
                            displayAfterLoginErrorMsg(error)
                            binding.txtNoRecommendedVideos.visibility = View.VISIBLE
                            binding.txtNoRecommendedPodcast.visibility = View.VISIBLE
                            binding.txtNoRecommendedArticles.visibility = View.VISIBLE
                            binding.txtNoRecommendedProviderGoals.visibility = View.VISIBLE
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    /*private fun displayVideos(recommended: Recommended) {
        if (recommended.videos.isNotEmpty()) {
            recyclerViewRecommendedVideos.visibility = View.VISIBLE
            txtNoRecommendedVideos.visibility = View.GONE
            recyclerViewRecommendedVideos.apply {
                layoutManager =
                    LinearLayoutManager(
                        mActivity!!,
                        RecyclerView.HORIZONTAL,
                        false
                    )
                adapter =
                    DashboardVideosAdapter(
                        mActivity!!,
                        recommended.videos,
                        this@RecommendedDataFragment
                    )
            }
        } else {
            recyclerViewRecommendedVideos.visibility = View.GONE
            txtNoRecommendedVideos.visibility = View.VISIBLE
        }
    }*/

    private fun displayPodcasts(recommended: Recommended) {
        if (recommended.podcasts.isNotEmpty()) {
            binding.recyclerViewRecommendedPodcasts.visibility = View.VISIBLE
            binding.txtNoRecommendedPodcast.visibility = View.GONE
            binding.recyclerViewRecommendedPodcasts.apply {
                layoutManager =
                    LinearLayoutManager(
                        mActivity!!,
                        RecyclerView.HORIZONTAL,
                        false
                    )
                adapter =
                    DashboardPodcastAdapter(
                        mActivity!!,
                        recommended.podcasts,
                        this@RecommendedDataFragment, ""
                    )
            }
        } else {
            binding.recyclerViewRecommendedPodcasts.visibility = View.GONE
            binding.txtNoRecommendedPodcast.visibility = View.VISIBLE
        }
    }

    private fun displayArticles(recommended: Recommended) {
        if (recommended.articles.isNotEmpty()) {
            binding.recyclerViewRecommendedArticles.visibility = View.VISIBLE
            binding.txtNoRecommendedArticles.visibility = View.GONE
            binding.recyclerViewRecommendedArticles.apply {
                layoutManager =
                    LinearLayoutManager(
                        mActivity!!,
                        RecyclerView.HORIZONTAL,
                        false
                    )
                adapter =
                    DashboardArticlesAdapter(
                        mActivity!!,
                        recommended.articles,
                        this@RecommendedDataFragment
                    )
            }
        } else {
            binding.recyclerViewRecommendedArticles.visibility = View.GONE
            binding.txtNoRecommendedArticles.visibility = View.VISIBLE
        }
    }

    private fun displayProviderGoals(recommended: Recommended) {
        if (recommended.provider_goals.isNotEmpty()) {
            binding.recyclerViewRecommendedProviderGoals.visibility = View.VISIBLE
            binding.txtNoRecommendedProviderGoals.visibility = View.GONE
            binding.recyclerViewRecommendedProviderGoals.apply {
                layoutManager =
                    LinearLayoutManager(
                        mActivity!!,
                        RecyclerView.HORIZONTAL,
                        false
                    )
                adapter =
                    AllGoalsAdapter(
                        mActivity!!,
                        recommended.provider_goals,
                        this@RecommendedDataFragment, true
                    )
            }
        } else {
            binding.recyclerViewRecommendedProviderGoals.visibility = View.GONE
            binding.txtNoRecommendedProviderGoals.visibility = View.VISIBLE
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
         * @return A new instance of fragment RecommendedDataFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            RecommendedDataFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_recommended_data"
    }


    override fun onPodcastItemClicked(podcast: Podcast) {
        //Play podcast
        mediaPlayer = MediaPlayer()
        AudioStream(requireActivity(), podcast, mediaPlayer!!).streamAudio()
    }

    override fun onNewsItemClicked(articles: Articles) {
        //Navigate to News detail screen
        replaceFragment(
            NewsDetailFragment.newInstance(articles, ""),
            R.id.layout_home,
            NewsDetailFragment.TAG
        )
    }

    override fun onGoalItemClickListener(goal: Goal, isDelete: Boolean) {
        if (isDelete) {
            //Delete Goal
            deleteData("PI0010", goal.id) { response ->
                if (response == "Success") {
                    getRecommendedData()
                }
            }
        } else {
            replaceFragment(
                DetailGoalFragment.newInstance(goal),
                R.id.layout_home,
                DetailGoalFragment.TAG
            )
        }
    }
}