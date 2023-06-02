package com.app.selfcare.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.BaseActivity
import com.app.selfcare.R
import com.app.selfcare.adapters.DashboardPodcastAdapter
import com.app.selfcare.controller.OnPodcastItemClickListener
import com.app.selfcare.data.Articles
import com.app.selfcare.data.Podcast
import com.app.selfcare.data.Video
import com.app.selfcare.databinding.FragmentActivityCarePlanBinding
import com.app.selfcare.databinding.FragmentResourcesBinding
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper.get
import com.app.selfcare.utils.Utils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
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

/**
 * A simple [Fragment] subclass.
 * Use the [ResourcesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ResourcesFragment : BaseFragment(), OnPodcastItemClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var videoLists: ArrayList<Video> = arrayListOf()
    private var podcastLists: ArrayList<Podcast> = arrayListOf()
    private var articlesLists: ArrayList<Articles> = arrayListOf()
    private var isFavouriteVideo1: Boolean = false
    private var isFavouriteVideo2: Boolean = false
    private lateinit var binding: FragmentResourcesBinding

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
        binding = FragmentResourcesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getLayout(): Int {
        return R.layout.fragment_resources
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE
        updateStatusBarColor(R.color.resource_background)

        onClickEvents()

        binding.layoutShimmerDisplayVideos.startShimmer()
        binding.layoutShimmerDisplayVideos.visibility = View.VISIBLE
        binding.shimmerResourcePodcast.startShimmer()
        binding.shimmerResourcePodcast.visibility = View.VISIBLE
        binding.layoutShimmerDisplayArticles.startShimmer()
        binding.layoutShimmerDisplayArticles.visibility = View.VISIBLE
        binding.cardViewNoVideos.visibility = View.GONE
        binding.cardViewNoPodcasts.visibility = View.GONE
        binding.cardViewNoArticles.visibility = View.GONE

        getAllResourceData()

        binding.imgSearch.setOnClickListener {
            binding.resourceSearch.isFocusableInTouchMode = true
            binding.resourceSearch.isFocusable = true
            binding.resourceSearch.requestFocus()
        }

        binding.resourceSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            }

            override fun afterTextChanged(editable: Editable) {
                if (editable.toString().isNotEmpty())
                    filterData(editable.toString())
            }
        })
    }

    private fun onClickEvents() {
        binding.resourceBack.setOnClickListener {
            setBottomNavigation(null)
            setLayoutBottomNavigation(null)
            replaceFragmentNoBackStack(
                BottomNavigationFragment(),
                R.id.layout_home,
                BottomNavigationFragment.TAG
            )
        }

        binding.resourceFavourite.setOnClickListener {
            replaceFragment(
                FavoriteFragment.newInstance(Utils.RESOURCE),
                R.id.layout_home,
                FavoriteFragment.TAG
            )
        }

        binding.txtVideoSeeAll.setOnClickListener {
            replaceFragment(
                VideosListFragment.newInstance(arrayListOf(), "", false, ""),
                R.id.layout_home,
                VideosListFragment.TAG
            )
        }

        binding.txtPodcastSeeAll.setOnClickListener {
            replaceFragment(
                PodcastFragment.newInstance(arrayListOf(), "", false, ""),
                R.id.layout_home,
                PodcastFragment.TAG
            )
        }

        binding.txtArticleSeeAll.setOnClickListener {
            replaceFragment(
                NewsListFragment.newInstance(arrayListOf(), "", false, ""),
                R.id.layout_home,
                NewsListFragment.TAG
            )
        }
    }

    private fun getAllResourceData() {
        videoLists = arrayListOf()
        podcastLists = arrayListOf()
        articlesLists = arrayListOf()
        //layoutResources.visibility = View.GONE
        binding.layoutShimmerDisplayVideos.startShimmer()
        binding.layoutShimmerDisplayVideos.visibility = View.VISIBLE
        binding.shimmerResourcePodcast.startShimmer()
        binding.shimmerResourcePodcast.visibility = View.VISIBLE
        binding.layoutShimmerDisplayArticles.startShimmer()
        binding.layoutShimmerDisplayArticles.visibility = View.VISIBLE
        binding.cardViewNoVideos.visibility = View.GONE
        binding.cardViewNoPodcasts.visibility = View.GONE
        binding.cardViewNoArticles.visibility = View.GONE
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .getResourceDashboardData("Dashboard", getAccessToken())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ result ->
                        try {
                            binding.layoutShimmerDisplayVideos.stopShimmer()
                            binding.layoutShimmerDisplayVideos.visibility = View.GONE
                            binding.shimmerResourcePodcast.stopShimmer()
                            binding.shimmerResourcePodcast.visibility = View.GONE
                            binding.layoutShimmerDisplayArticles.stopShimmer()
                            binding.layoutShimmerDisplayArticles.visibility = View.GONE
                            hideProgress()
                            var responseBody = result.string()
                            Log.d("Response Body", responseBody)
                            val respBody = responseBody.split("|")
                            val status = respBody[1]
                            responseBody = respBody[0]
                            val jsonObj = JSONObject(responseBody)
                            //Videos
                            val videoList: Type = object : TypeToken<ArrayList<Video?>?>() {}.type
                            videoLists = Gson().fromJson(jsonObj.getString("videos"), videoList)
                            displayVideoList(videoLists)

                            //Podcast
                            val podcastList: Type =
                                object : TypeToken<ArrayList<Podcast?>?>() {}.type
                            podcastLists =
                                Gson().fromJson(jsonObj.getString("podcasts"), podcastList)
                            displayPodcastList(podcastLists)

                            //Articles
                            val articleList: Type =
                                object : TypeToken<ArrayList<Articles?>?>() {}.type
                            articlesLists =
                                Gson().fromJson(jsonObj.getString("articles"), articleList)
                            displayArticleList(articlesLists)

                            if (videoLists.isEmpty() && podcastLists.isEmpty() && articlesLists.isEmpty()) {
                                binding.layoutResourceSearch.visibility = View.GONE
                            } else {
                                binding.layoutResourceSearch.visibility = View.VISIBLE
                            }
                        } catch (e: Exception) {
                            hideProgress()
                            binding.layoutShimmerDisplayVideos.stopShimmer()
                            binding.layoutShimmerDisplayVideos.visibility = View.GONE
                            binding.shimmerResourcePodcast.stopShimmer()
                            binding.shimmerResourcePodcast.visibility = View.GONE
                            binding.layoutShimmerDisplayArticles.stopShimmer()
                            binding.layoutShimmerDisplayArticles.visibility = View.GONE
                            binding.layoutResourceSearch.visibility = View.GONE
                            binding.cardViewNoVideos.visibility = View.VISIBLE
                            binding.cardViewNoPodcasts.visibility = View.VISIBLE
                            binding.cardViewNoArticles.visibility = View.VISIBLE
                            e.printStackTrace()
                            displayToast("Something went wrong.. Please try after sometime")
                        }
                    }, { error ->
                        hideProgress()
                        if ((error as HttpException).code() == 401) {
                            userLogin(
                                preference!![PrefKeys.PREF_EMAIL]!!,
                                preference!![PrefKeys.PREF_PASS]!!
                            ) { result ->
                                getAllResourceData()
                            }
                        } else {
                            binding.layoutShimmerDisplayVideos.stopShimmer()
                            binding.layoutShimmerDisplayVideos.visibility = View.GONE
                            binding.shimmerResourcePodcast.stopShimmer()
                            binding.shimmerResourcePodcast.visibility = View.GONE
                            binding.layoutShimmerDisplayArticles.stopShimmer()
                            binding.layoutShimmerDisplayArticles.visibility = View.GONE
                            binding.layoutResourceSearch.visibility = View.GONE
                            binding.cardViewNoVideos.visibility = View.VISIBLE
                            binding.cardViewNoPodcasts.visibility = View.VISIBLE
                            binding.cardViewNoArticles.visibility = View.VISIBLE
                            displayAfterLoginErrorMsg(error)
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    private fun displayVideoList(videoList: ArrayList<Video>) {
        if (videoList.isNotEmpty()) {
            binding.layoutDisplayVideos.visibility = View.VISIBLE
            binding.cardViewNoVideos.visibility = View.GONE
            if (videoList.size == 1) {
                isFavouriteVideo1 = videoList[0].is_favourite
                binding.layoutVideoList1.visibility = View.VISIBLE
                setVideoImage(videoList[0], binding.videoBanner1)
                binding.resourceVideoTitle1.text = videoList[0].name
                binding.resourceVideoDesc1.text = videoList[0].description
                if (videoList[0].is_favourite) {
                    binding.resourceFavImg1.setImageResource(R.drawable.favorite)
                    binding.resourceFavTitle1.text = "Added to favorites"
                } else {
                    binding.resourceFavImg1.setImageResource(R.drawable.favorite_outline)
                    binding.resourceFavTitle1.text = "Add to favorites"
                }
                binding.layoutVideoList2.visibility = View.GONE
            } else if (videoList.size >= 2) {
                isFavouriteVideo2 = videoList[1].is_favourite
                binding.layoutVideoList1.visibility = View.VISIBLE
                setVideoImage(videoList[0], binding.videoBanner1)
                binding.resourceVideoTitle1.text = videoList[0].name
                binding.resourceVideoDesc1.text = videoList[0].description
                if (videoList[0].is_favourite) {
                    binding.resourceFavImg1.setImageResource(R.drawable.favorite)
                    binding.resourceFavTitle1.text = "Added to favorites"
                } else {
                    binding.resourceFavImg1.setImageResource(R.drawable.favorite_outline)
                    binding.resourceFavTitle1.text = "Add to favorites"
                }
                binding.layoutVideoList2.visibility = View.VISIBLE
                setVideoImage(videoList[1], binding.videoBanner2)
                binding.resourceVideoTitle2.text = videoList[1].name
                binding.resourceVideoDesc2.text = videoList[1].description
                if (videoList[1].is_favourite) {
                    binding.resourceFavImg2.setImageResource(R.drawable.favorite)
                    binding.resourceFavTitle2.text = "Added to favorites"
                } else {
                    binding.resourceFavImg2.setImageResource(R.drawable.favorite_outline)
                    binding.resourceFavTitle2.text = "Add to favorites"
                }
            }

            binding.cardViewVideo1.setOnClickListener {
                replaceFragment(
                    VideoDetailFragment.newInstance(videoList[0]),
                    R.id.layout_home,
                    VideoDetailFragment.TAG
                )
            }

            binding.cardViewVideo2.setOnClickListener {
                replaceFragment(
                    VideoDetailFragment.newInstance(videoList[1]),
                    R.id.layout_home,
                    VideoDetailFragment.TAG
                )
            }

            binding.resourceLayoutFav1.setOnClickListener {
                sendResourceFavoriteData(videoList[0].id, "Video", !isFavouriteVideo1) {
                    if (!isFavouriteVideo1) {
                        binding.resourceFavImg1.setImageResource(R.drawable.favorite)
                        binding.resourceFavTitle1.text = "Added to favorites"
                    } else {
                        binding.resourceFavImg1.setImageResource(R.drawable.favorite_outline)
                        binding.resourceFavTitle1.text = "Removed from favorites"
                    }
                    isFavouriteVideo1 = !isFavouriteVideo1
                }
            }

            binding.resourceLayoutFav2.setOnClickListener {
                sendResourceFavoriteData(videoList[1].id, "Video", !isFavouriteVideo2) {
                    if (!isFavouriteVideo2) {
                        binding.resourceFavImg2.setImageResource(R.drawable.favorite)
                        binding.resourceFavTitle2.text = "Added to favorites"
                    } else {
                        binding.resourceFavImg2.setImageResource(R.drawable.favorite_outline)
                        binding.resourceFavTitle2.text = "Removed from favorites"
                    }
                    isFavouriteVideo2 = !isFavouriteVideo2
                }
            }
        } else {
            binding.layoutDisplayVideos.visibility = View.GONE
            binding.cardViewNoVideos.visibility = View.VISIBLE
        }
    }

    private fun setVideoImage(video: Video, imageView: ImageView) {
        val videoImg = if (video.video_url.isNotEmpty() && video.video_url.contains("youtube")) {
            val videoId: String = video.video_url.split("v=")[1]
            "http://img.youtube.com/vi/$videoId/hqdefault.jpg" //high quality thumbnail
        } else {
            BaseActivity.baseURL.dropLast(5) + video.video_url
        }
        Glide.with(requireActivity()).load(videoImg)
            .transform(CenterCrop(), RoundedCorners(5))
            .into(imageView)
    }

    private fun displayPodcastList(podcastList: ArrayList<Podcast>) {
        if (podcastList.isNotEmpty()) {
            binding.recyclerViewResourcePodcast.visibility = View.VISIBLE
            binding.cardViewNoPodcasts.visibility = View.GONE
            binding.recyclerViewResourcePodcast.apply {
                layoutManager = LinearLayoutManager(mActivity!!, RecyclerView.HORIZONTAL, false)
                adapter =
                    DashboardPodcastAdapter(mActivity!!, podcastList, this@ResourcesFragment, "")
            }
        } else {
            binding.recyclerViewResourcePodcast.visibility = View.GONE
            binding.cardViewNoPodcasts.visibility = View.VISIBLE
        }
    }

    private fun displayArticleList(articleList: ArrayList<Articles>) {
        if (articleList.isNotEmpty()) {
            binding.layoutDisplayArticles.visibility = View.VISIBLE
            binding.cardViewNoArticles.visibility = View.GONE
            if (articleList.size == 1) {
                binding.layoutArticleList1.visibility = View.VISIBLE
                binding.cardViewArticle1.visibility = View.VISIBLE
                binding.articleTitle1.text = articleList[0].name
                setArticleImage(articleList[0], binding.articleBanner1)
                binding.cardViewArticle2.visibility = View.GONE
                binding.layoutArticleList2.visibility = View.GONE
                binding.cardViewArticle3.visibility = View.GONE
                binding.cardViewArticle4.visibility = View.GONE
            } else if (articleList.size == 2) {
                binding.layoutArticleList1.visibility = View.VISIBLE
                binding.cardViewArticle1.visibility = View.VISIBLE
                binding.articleTitle1.text = articleList[0].name
                setArticleImage(articleList[0], binding.articleBanner1)
                binding.cardViewArticle2.visibility = View.VISIBLE
                binding.articleTitle2.text = articleList[1].name
                setArticleImage(articleList[1], binding.articleBanner2)
                binding.layoutArticleList2.visibility = View.GONE
                binding.cardViewArticle3.visibility = View.GONE
                binding.cardViewArticle4.visibility = View.GONE
            } else if (articleList.size == 3) {
                binding.layoutArticleList1.visibility = View.VISIBLE
                binding.cardViewArticle1.visibility = View.VISIBLE
                binding.articleTitle1.text = articleList[0].name
                setArticleImage(articleList[0], binding.articleBanner1)
                binding.cardViewArticle2.visibility = View.VISIBLE
                binding.articleTitle2.text = articleList[1].name
                setArticleImage(articleList[1], binding.articleBanner2)
                binding.layoutArticleList2.visibility = View.VISIBLE
                binding.cardViewArticle3.visibility = View.VISIBLE
                binding.articleTitle3.text = articleList[2].name
                setArticleImage(articleList[2], binding.articleBanner3)
                binding.cardViewArticle4.visibility = View.GONE
            } else if (articleList.size >= 4) {
                binding.layoutArticleList1.visibility = View.VISIBLE
                binding.cardViewArticle1.visibility = View.VISIBLE
                binding.articleTitle1.text = articleList[0].name
                setArticleImage(articleList[0], binding.articleBanner1)
                binding.cardViewArticle2.visibility = View.VISIBLE
                binding.articleTitle2.text = articleList[1].name
                setArticleImage(articleList[1], binding.articleBanner2)
                binding.layoutArticleList2.visibility = View.VISIBLE
                binding.cardViewArticle3.visibility = View.VISIBLE
                binding.articleTitle3.text = articleList[2].name
                setArticleImage(articleList[2], binding.articleBanner3)
                binding.cardViewArticle4.visibility = View.VISIBLE
                binding.articleTitle4.text = articleList[3].name
                setArticleImage(articleList[3], binding.articleBanner4)
            }

            binding.cardViewArticle1.setOnClickListener {
                replaceFragment(
                    NewsDetailFragment.newInstance(articleList[0], ""),
                    R.id.layout_home,
                    NewsDetailFragment.TAG
                )
            }

            binding.cardViewArticle2.setOnClickListener {
                replaceFragment(
                    NewsDetailFragment.newInstance(articleList[1], ""),
                    R.id.layout_home,
                    NewsDetailFragment.TAG
                )
            }

            binding.cardViewArticle3.setOnClickListener {
                replaceFragment(
                    NewsDetailFragment.newInstance(articleList[2], ""),
                    R.id.layout_home,
                    NewsDetailFragment.TAG
                )
            }

            binding.cardViewArticle4.setOnClickListener {
                replaceFragment(
                    NewsDetailFragment.newInstance(articleList[3], ""),
                    R.id.layout_home,
                    NewsDetailFragment.TAG
                )
            }
        } else {
            binding.layoutDisplayArticles.visibility = View.GONE
            binding.cardViewNoArticles.visibility = View.VISIBLE
        }
    }

    private fun setArticleImage(article: Articles, imageView: ImageView) {
        if (article.banner_image != null) {
            val articleImg =
                if (article.banner_image.isNotEmpty() && article.banner_image.contains("youtube")) {
                    val articleId: String = article.banner_image.split("v=")[1]
                    "http://img.youtube.com/vi/$articleId/hqdefault.jpg" //high quality thumbnail
                } else {
                    BaseActivity.baseURL.dropLast(5) + article.banner_image
                }
            Glide.with(requireActivity()).load(articleImg)
                .transform(CenterCrop(), RoundedCorners(5))
                .into(imageView)
        }
    }

    private fun filterData(text: String) {
        val filteredVideos = ArrayList<Video>()
        val filteredPodcasts = ArrayList<Podcast>()
        val filteredArticles = ArrayList<Articles>()
        videoLists.filterTo(filteredVideos) {
            it.name.toLowerCase().contains(text.toLowerCase())
        }
        podcastLists.filterTo(filteredPodcasts) {
            it.name.toLowerCase().contains(text.toLowerCase())
        }
        articlesLists.filterTo(filteredArticles) {
            it.name.toLowerCase().contains(text.toLowerCase())
        }

        if (filteredVideos.isNotEmpty()) {
            binding.layoutVideoResourceData.visibility = View.VISIBLE
            displayVideoList(filteredVideos)
        } else {
            if (text.length < 2) {
                displayVideoList(videoLists)
            } else {
                binding.layoutVideoResourceData.visibility = View.GONE
            }
        }

        if (filteredPodcasts.isNotEmpty()) {
            binding.layoutPodcastResourceData.visibility = View.VISIBLE
            val adapter = DashboardPodcastAdapter(
                mActivity!!,
                podcastLists, this, ""
            )
            adapter.filterList(filteredPodcasts)
            binding.recyclerViewResourcePodcast.adapter = adapter
        } else {
            if (text.length < 2) {
                val adapter = DashboardPodcastAdapter(
                    mActivity!!,
                    podcastLists, this, ""
                )
                adapter.filterList(filteredPodcasts)
                binding.recyclerViewResourcePodcast.adapter = adapter
            } else {
                binding.layoutPodcastResourceData.visibility = View.GONE
            }
        }

        if (filteredArticles.isNotEmpty()) {
            binding.layoutArticlesResourceData.visibility = View.VISIBLE
            displayArticleList(filteredArticles)
        } else {
            if (text.length < 2) {
                displayArticleList(articlesLists)
            } else {
                binding.layoutArticlesResourceData.visibility = View.GONE
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (binding.resourceSearch != null)
            binding.resourceSearch.setText("")
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ResourcesFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ResourcesFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_resources"
    }

    override fun onPodcastItemClicked(podcast: Podcast) {
        replaceFragment(
            PodcastDetailFragment.newInstance(podcast, ""),
            R.id.layout_home,
            PodcastDetailFragment.TAG
        )
    }
}