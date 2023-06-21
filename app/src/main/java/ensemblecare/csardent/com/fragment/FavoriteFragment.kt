package ensemblecare.csardent.com.fragment

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ensemblecare.csardent.com.BaseActivity
import ensemblecare.csardent.com.R
import ensemblecare.csardent.com.adapters.DashboardPodcastAdapter
import ensemblecare.csardent.com.controller.OnPodcastItemClickListener
import ensemblecare.csardent.com.data.Articles
import ensemblecare.csardent.com.data.Podcast
import ensemblecare.csardent.com.data.Video
import ensemblecare.csardent.com.databinding.FragmentFavoriteBinding
import ensemblecare.csardent.com.preference.PrefKeys
import ensemblecare.csardent.com.preference.PreferenceHelper.get
import ensemblecare.csardent.com.utils.Utils
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
 * Use the [FavoriteFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FavoriteFragment : BaseFragment(), OnPodcastItemClickListener {
    // TODO: Rename and change types of parameters
    private var wellnessType: String? = null
    private var param2: String? = null
    private var videoLists: ArrayList<Video> = arrayListOf()
    private var podcastLists: ArrayList<Podcast> = arrayListOf()
    private var articlesLists: ArrayList<Articles> = arrayListOf()
    private lateinit var binding: FragmentFavoriteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            wellnessType = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFavoriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getLayout(): Int {
        return R.layout.fragment_favorite
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE

        onClickEvents()

        when (wellnessType!!) {
            Utils.WELLNESS_EXERCISE -> {
                binding.imgFavBackground.setImageResource(R.drawable.exercise_background)
                updateStatusBarColor(R.color.primaryGreen)
                changeComponentColor(R.color.white)
            }

            Utils.WELLNESS_NUTRITION -> {
                binding.imgFavBackground.setColorFilter(resources.getColor(R.color.white))
                updateStatusBarColor(R.color.white)
                changeComponentColor(R.color.black)
                binding.layoutPodcastFavData.visibility = View.GONE
                binding.txtFavArticlesTitle.text = "Recipes"
                binding.txtNoFavorites.setTextColor(resources.getColor(R.color.localBackground))
            }

            Utils.WELLNESS_MINDFULNESS -> {
                binding.imgFavBackground.setImageResource(R.drawable.mindfulness_back_img)
                //frameLayoutDetail.background = resources.getDrawable(R.drawable.mindfulness_back_img)
                updateStatusBarColor(R.color.primaryGreen)
                changeComponentColor(R.color.white)
            }

            Utils.WELLNESS_YOGA -> {
                binding.imgFavBackground.setImageResource(R.drawable.yoga_background)
                updateStatusBarColor(R.color.yoga_status_bar)
                changeComponentColor(R.color.white)
            }

            Utils.WELLNESS_MUSIC -> {
                binding.imgFavBackground.setImageResource(R.drawable.music_background)
                updateStatusBarColor(R.color.music_status_bar)
                changeComponentColor(R.color.white)
            }

            else -> {
                binding.imgFavBackground.setColorFilter(resources.getColor(R.color.white))
                updateStatusBarColor(R.color.white)
                changeComponentColor(R.color.black)
                binding.txtNoFavorites.setTextColor(resources.getColor(R.color.localBackground))
            }
        }

        getFavouriteDetails()

        binding.favSearch.addTextChangedListener(object : TextWatcher {
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

    private fun changeComponentColor(componentColor: Int) {
        binding.favBack.imageTintList = ColorStateList.valueOf(
            ContextCompat.getColor(
                requireActivity(),
                componentColor
            )
        )
        binding.txtFavTitle.setTextColor(resources.getColor(componentColor))
        binding.favSearch.setHintTextColor(resources.getColor(componentColor))
        binding.txtFavVideosTitle.setTextColor(resources.getColor(componentColor))
        binding.txtFavVideoSeeAll.setTextColor(resources.getColor(componentColor))
        binding.favVideoTitle1.setTextColor(resources.getColor(componentColor))
        binding.favVideoDesc1.setTextColor(resources.getColor(componentColor))
        binding.favFavTitle1.setTextColor(resources.getColor(componentColor))
        binding.favVideoTitle2.setTextColor(resources.getColor(componentColor))
        binding.favVideoDesc2.setTextColor(resources.getColor(componentColor))
        binding.favFavTitle2.setTextColor(resources.getColor(componentColor))
        binding.txtFavPodcastTitle.setTextColor(resources.getColor(componentColor))
        binding.txtFavPodcastSeeAll.setTextColor(resources.getColor(componentColor))
        binding.txtFavArticlesTitle.setTextColor(resources.getColor(componentColor))
        binding.txtFavArticleSeeAll.setTextColor(resources.getColor(componentColor))
        binding.favArticleTitle1.setTextColor(resources.getColor(componentColor))
        binding.favArticleTitle2.setTextColor(resources.getColor(componentColor))
        binding.favArticleTitle3.setTextColor(resources.getColor(componentColor))
        binding.favArticleTitle4.setTextColor(resources.getColor(componentColor))
        binding.txtNoFavorites.setTextColor(resources.getColor(componentColor))
    }

    private fun onClickEvents() {
        binding.favBack.setOnClickListener {
            popBackStack()
        }

        binding.txtFavVideoSeeAll.setOnClickListener {
            replaceFragment(
                VideosListFragment.newInstance(arrayListOf(), wellnessType!!, true, ""),
                R.id.layout_home,
                VideosListFragment.TAG
            )
        }

        binding.txtFavPodcastSeeAll.setOnClickListener {
            replaceFragment(
                PodcastFragment.newInstance(arrayListOf(), wellnessType!!, true, ""),
                R.id.layout_home, PodcastFragment.TAG
            )
        }

        binding.txtFavArticleSeeAll.setOnClickListener {
            replaceFragment(
                NewsListFragment.newInstance(arrayListOf(), wellnessType!!, true, ""),
                R.id.layout_home, NewsListFragment.TAG
            )
        }
    }

    private fun fetchFavoriteData(wellnessType: String, myCallback: (result: String?) -> Unit) {
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .getFavoriteData(wellnessType + "_favourites", getAccessToken())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ result ->
                        try {
                            hideProgress()
                            binding.layoutShimmerFavDisplayVideos.stopShimmer()
                            binding.layoutShimmerFavDisplayVideos.visibility = View.GONE
                            binding.shimmerFavPodcast.stopShimmer()
                            binding.shimmerFavPodcast.visibility = View.GONE
                            binding.layoutShimmerFavDisplayArticles.stopShimmer()
                            binding.layoutShimmerFavDisplayArticles.visibility = View.GONE
                            binding.favSearch.visibility = View.VISIBLE
                            binding.txtFavVideoSeeAll.visibility = View.VISIBLE
                            binding.txtFavPodcastSeeAll.visibility = View.VISIBLE
                            binding.txtFavArticleSeeAll.visibility = View.VISIBLE
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
                                    clearCache()
                                }
                            } else {
                                myCallback.invoke(responseBody)
                            }
                        } catch (e: Exception) {
                            hideProgress()
                            binding.layoutShimmerFavDisplayVideos.stopShimmer()
                            binding.layoutShimmerFavDisplayVideos.visibility = View.GONE
                            binding.shimmerFavPodcast.stopShimmer()
                            binding.shimmerFavPodcast.visibility = View.GONE
                            binding.layoutShimmerFavDisplayArticles.stopShimmer()
                            binding.layoutShimmerFavDisplayArticles.visibility = View.GONE
                            binding.favSearch.visibility = View.GONE
                            binding.cardViewFavNoVideos.visibility = View.VISIBLE
                            binding.cardViewFavNoArticles.visibility = View.VISIBLE
                            binding.cardViewFavNoPodcasts.visibility = View.VISIBLE
                            e.printStackTrace()
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
                                clearCache()
                            }
                        } else {
                            binding.layoutShimmerFavDisplayVideos.stopShimmer()
                            binding.layoutShimmerFavDisplayVideos.visibility = View.GONE
                            binding.shimmerFavPodcast.stopShimmer()
                            binding.shimmerFavPodcast.visibility = View.GONE
                            binding.layoutShimmerFavDisplayArticles.stopShimmer()
                            binding.layoutShimmerFavDisplayArticles.visibility = View.GONE
                            binding.favSearch.visibility = View.GONE
                            binding.cardViewFavNoVideos.visibility = View.VISIBLE
                            binding.cardViewFavNoArticles.visibility = View.VISIBLE
                            binding.cardViewFavNoPodcasts.visibility = View.VISIBLE
                            displayAfterLoginErrorMsg(error)
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    private fun getResourceFavoriteData(myCallback: (result: String?) -> Unit) {
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .getResourceFavoriteData(getAccessToken())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ result ->
                        try {
                            hideProgress()
                            binding.layoutShimmerFavDisplayVideos.stopShimmer()
                            binding.layoutShimmerFavDisplayVideos.visibility = View.GONE
                            binding.shimmerFavPodcast.stopShimmer()
                            binding.shimmerFavPodcast.visibility = View.GONE
                            binding.layoutShimmerFavDisplayArticles.stopShimmer()
                            binding.layoutShimmerFavDisplayArticles.visibility = View.GONE
                            binding.favSearch.visibility = View.VISIBLE
                            binding.txtFavVideoSeeAll.visibility = View.VISIBLE
                            binding.txtFavPodcastSeeAll.visibility = View.VISIBLE
                            binding.txtFavArticleSeeAll.visibility = View.VISIBLE
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

                                }
                            } else {
                                myCallback.invoke(responseBody)
                            }
                        } catch (e: Exception) {
                            hideProgress()
                            binding.layoutShimmerFavDisplayVideos.stopShimmer()
                            binding.layoutShimmerFavDisplayVideos.visibility = View.GONE
                            binding.shimmerFavPodcast.stopShimmer()
                            binding.shimmerFavPodcast.visibility = View.GONE
                            binding.layoutShimmerFavDisplayArticles.stopShimmer()
                            binding.layoutShimmerFavDisplayArticles.visibility = View.GONE
                            binding.favSearch.visibility = View.GONE
                            binding.txtFavVideoSeeAll.visibility = View.GONE
                            binding.txtFavPodcastSeeAll.visibility = View.GONE
                            binding.txtFavArticleSeeAll.visibility = View.GONE
                            binding.cardViewFavNoVideos.visibility = View.VISIBLE
                            binding.cardViewFavNoArticles.visibility = View.VISIBLE
                            binding.cardViewFavNoPodcasts.visibility = View.VISIBLE
                            e.printStackTrace()
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

                            }
                        } else {
                            binding.layoutShimmerFavDisplayVideos.stopShimmer()
                            binding.layoutShimmerFavDisplayVideos.visibility = View.GONE
                            binding.shimmerFavPodcast.stopShimmer()
                            binding.shimmerFavPodcast.visibility = View.GONE
                            binding.layoutShimmerFavDisplayArticles.stopShimmer()
                            binding.layoutShimmerFavDisplayArticles.visibility = View.GONE
                            binding.favSearch.visibility = View.GONE
                            binding.txtFavVideoSeeAll.visibility = View.GONE
                            binding.txtFavPodcastSeeAll.visibility = View.GONE
                            binding.txtFavArticleSeeAll.visibility = View.GONE
                            binding.cardViewFavNoVideos.visibility = View.VISIBLE
                            binding.cardViewFavNoArticles.visibility = View.VISIBLE
                            binding.cardViewFavNoPodcasts.visibility = View.VISIBLE
                            displayAfterLoginErrorMsg(error)
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    private fun getFavouriteDetails() {
        videoLists = arrayListOf()
        podcastLists = arrayListOf()
        articlesLists = arrayListOf()
        binding.layoutShimmerFavDisplayVideos.startShimmer()
        binding.layoutVideoFavData.visibility = View.VISIBLE
        binding.layoutFavDisplayVideos.visibility = View.GONE
        binding.layoutShimmerFavDisplayVideos.visibility = View.VISIBLE
        binding.shimmerFavPodcast.startShimmer()
        binding.layoutPodcastFavData.visibility = View.VISIBLE
        binding.recyclerViewFavPodcast.visibility = View.GONE
        binding.shimmerFavPodcast.visibility = View.VISIBLE
        binding.layoutShimmerFavDisplayArticles.startShimmer()
        binding.layoutArticleFavData.visibility = View.VISIBLE
        binding.layoutFavDisplayArticles.visibility = View.GONE
        binding.layoutShimmerFavDisplayArticles.visibility = View.VISIBLE
        binding.favSearch.visibility = View.GONE
        if (wellnessType!! != Utils.RESOURCE) {
            fetchFavoriteData(wellnessType!!) { response ->
                val jsonObj = JSONObject(response)

                val videoList: Type = object : TypeToken<ArrayList<Video?>?>() {}.type
                videoLists = Gson().fromJson(jsonObj.getString("videos"), videoList)

                val podcastList: Type = object : TypeToken<ArrayList<Podcast?>?>() {}.type
                podcastLists = Gson().fromJson(jsonObj.getString("podcasts"), podcastList)

                val articleList: Type = object : TypeToken<ArrayList<Articles?>?>() {}.type
                articlesLists = Gson().fromJson(jsonObj.getString("articles"), articleList)

                if (videoLists.isEmpty() && podcastLists.isEmpty() && articlesLists.isEmpty()) {
                    binding.txtNoFavorites.visibility = View.VISIBLE
                    binding.scrollViewFavorite.visibility = View.GONE
                    binding.favSearch.visibility = View.GONE
                } else {
                    binding.favSearch.visibility = View.VISIBLE
                    binding.scrollViewFavorite.visibility = View.VISIBLE
                    displayVideoList(videoLists)
                    displayPodcastList(podcastLists)
                    displayArticleList(articlesLists)
                }
            }
        } else {
            getResourceFavoriteData { response ->
                val jsonObj = JSONObject(response)

                val videoList: Type = object : TypeToken<ArrayList<Video?>?>() {}.type
                videoLists = Gson().fromJson(jsonObj.getString("videos"), videoList)

                val podcastList: Type = object : TypeToken<ArrayList<Podcast?>?>() {}.type
                podcastLists = Gson().fromJson(jsonObj.getString("podcasts"), podcastList)

                val articleList: Type = object : TypeToken<ArrayList<Articles?>?>() {}.type
                articlesLists = Gson().fromJson(jsonObj.getString("articles"), articleList)

                if (videoLists.isEmpty() && podcastLists.isEmpty() && articlesLists.isEmpty()) {
                    binding.txtNoFavorites.visibility = View.VISIBLE
                    binding.scrollViewFavorite.visibility = View.GONE
                    binding.favSearch.visibility = View.GONE
                } else {
                    binding.favSearch.visibility = View.VISIBLE
                    binding.scrollViewFavorite.visibility = View.VISIBLE
                    displayVideoList(videoLists)
                    displayPodcastList(podcastLists)
                    displayArticleList(articlesLists)
                }
            }
        }
    }

    private fun displayVideoList(videoList: ArrayList<Video>) {
        if (videoList.isNotEmpty()) {
            binding.layoutFavDisplayVideos.visibility = View.VISIBLE
            binding.layoutVideoFavData.visibility = View.VISIBLE
            //binding.cardViewFavNoVideos.visibility = View.GONE
            if (videoList.size == 1) {
                binding.layoutFavVideoList1.visibility = View.VISIBLE
                setVideoImage(videoList[0], binding.favVideoBanner1)
                binding.favVideoTitle1.text = videoList[0].name
                binding.favVideoDesc1.text = videoList[0].description
                if (videoList[0].is_favourite) {
                    binding.favFavImg1.setImageResource(R.drawable.favorite)
                    binding.favFavTitle1.text = "Added to favorites"
                } else {
                    if (wellnessType == Utils.WELLNESS_NUTRITION) {
                        binding.favFavImg1.setImageResource(R.drawable.favorite_outline)
                    } else {
                        binding.favFavImg1.setImageResource(R.drawable.favourite_white)
                    }
                    binding.favFavTitle1.text = "Add to favorites"
                }
                binding.layoutFavVideoList2.visibility = View.GONE
            } else if (videoList.size >= 2) {
                binding.layoutFavVideoList1.visibility = View.VISIBLE
                setVideoImage(videoList[0], binding.favVideoBanner1)
                binding.favVideoTitle1.text = videoList[0].name
                binding.favVideoDesc1.text = videoList[0].description
                if (videoList[0].is_favourite) {
                    binding.favFavImg1.setImageResource(R.drawable.favorite)
                    binding.favFavTitle1.text = "Added to favorites"
                } else {
                    if (wellnessType == Utils.WELLNESS_NUTRITION) {
                        binding.favFavImg1.setImageResource(R.drawable.favorite_outline)
                    } else {
                        binding.favFavImg1.setImageResource(R.drawable.favourite_white)
                    }
                    binding.favFavTitle1.text = "Add to favorites"
                }

                binding.layoutFavVideoList2.visibility = View.VISIBLE
                setVideoImage(videoList[1], binding.favVideoBanner2)
                binding.favVideoTitle2.text = videoList[1].name
                binding.favVideoDesc2.text = videoList[1].description
                if (videoList[1].is_favourite) {
                    binding.favFavImg2.setImageResource(R.drawable.favorite)
                    binding.favFavTitle2.text = "Added to favorites"
                } else {
                    if (wellnessType == Utils.WELLNESS_NUTRITION) {
                        binding.favFavImg2.setImageResource(R.drawable.favorite_outline)
                    } else {
                        binding.favFavImg2.setImageResource(R.drawable.favourite_white)
                    }
                    binding.favFavTitle2.text = "Add to favorites"
                }
            }

            binding.cardViewFavVideo1.setOnClickListener {
                replaceFragment(
                    VideoDetailFragment.newInstance(videoList[0]),
                    R.id.layout_home,
                    VideoDetailFragment.TAG
                )
            }

            binding.cardViewFavVideo2.setOnClickListener {
                replaceFragment(
                    VideoDetailFragment.newInstance(videoList[1]),
                    R.id.layout_home,
                    VideoDetailFragment.TAG
                )
            }

            binding.favLayoutFav1.setOnClickListener {
                if (wellnessType!! != Utils.RESOURCE) {
                    sendFavoriteData(
                        videoList[0].id,
                        "Video",
                        !videoList[0].is_favourite,
                        wellnessType!!
                    ) {
                        getFavouriteDetails()
                    }
                } else {
                    sendResourceFavoriteData(videoList[0].id, "Video", !videoList[0].is_favourite) {
                        getFavouriteDetails()
                    }
                }
            }

            binding.favLayoutFav2.setOnClickListener {
                if (wellnessType!! != Utils.RESOURCE) {
                    sendFavoriteData(
                        videoList[1].id,
                        "Video",
                        !videoList[1].is_favourite,
                        wellnessType!!
                    ) {
                        getFavouriteDetails()
                    }
                } else {
                    sendResourceFavoriteData(videoList[0].id, "Video", !videoList[0].is_favourite) {
                        getFavouriteDetails()
                    }
                }
            }
        } else {
            binding.layoutVideoFavData.visibility = View.GONE
            //binding.cardViewFavNoVideos.visibility = View.VISIBLE
        }
    }

    private fun setVideoImage(video: Video, imageView: ImageView) {
        val videoImg = if (video.video_url.isNotEmpty() && video.video_url.contains("youtube")) {
            val videoId: String = video.video_url.split("v=")[1]
            "http://img.youtube.com/vi/$videoId/hqdefault.jpg" //high quality thumbnail
        } else {
            BaseActivity.baseURL.dropLast(5) + video.banner
        }
        Glide.with(requireActivity()).load(videoImg)
            .transform(CenterCrop(), RoundedCorners(5))
            .into(imageView)
    }

    private fun displayPodcastList(podcastList: ArrayList<Podcast>) {
        if (podcastList.isNotEmpty()) {
            binding.layoutPodcastFavData.visibility = View.VISIBLE
            binding.recyclerViewFavPodcast.visibility = View.VISIBLE
            //binding.cardViewFavNoPodcasts.visibility = View.GONE
            binding.recyclerViewFavPodcast.apply {
                layoutManager = LinearLayoutManager(mActivity!!, RecyclerView.HORIZONTAL, false)
                adapter =
                    DashboardPodcastAdapter(
                        mActivity!!,
                        podcastList,
                        this@FavoriteFragment,
                        wellnessType!!
                    )
            }
        } else {
            binding.layoutPodcastFavData.visibility = View.GONE
            binding.recyclerViewFavPodcast.visibility = View.GONE
            //binding.cardViewFavNoPodcasts.visibility = View.VISIBLE
        }
    }

    private fun displayArticleList(articleList: ArrayList<Articles>) {
        if (articleList.isNotEmpty()) {
            binding.layoutArticleFavData.visibility = View.VISIBLE
            binding.layoutFavDisplayArticles.visibility = View.VISIBLE
            //binding.cardViewFavNoArticles.visibility = View.GONE
            if (articleList.size == 1) {
                binding.layoutFavArticleList1.visibility = View.VISIBLE
                binding.cardViewFavArticle1.visibility = View.VISIBLE
                binding.favArticleTitle1.text = articleList[0].name
                setArticleImage(articleList[0], binding.favArticleBanner1)
                binding.cardViewFavArticle2.visibility = View.GONE
                binding.layoutFavArticleList2.visibility = View.GONE
                binding.cardViewFavArticle3.visibility = View.GONE
                binding.cardViewFavArticle4.visibility = View.GONE
            } else if (articleList.size == 2) {
                binding.layoutFavArticleList1.visibility = View.VISIBLE
                binding.cardViewFavArticle1.visibility = View.VISIBLE
                binding.favArticleTitle1.text = articleList[0].name
                setArticleImage(articleList[0], binding.favArticleBanner1)
                binding.cardViewFavArticle2.visibility = View.VISIBLE
                binding.favArticleTitle2.text = articleList[1].name
                setArticleImage(articleList[1], binding.favArticleBanner2)
                binding.layoutFavArticleList2.visibility = View.GONE
                binding.cardViewFavArticle3.visibility = View.GONE
                binding.cardViewFavArticle4.visibility = View.GONE
            } else if (articleList.size == 3) {
                binding.layoutFavArticleList1.visibility = View.VISIBLE
                binding.cardViewFavArticle1.visibility = View.VISIBLE
                binding.favArticleTitle1.text = articleList[0].name
                setArticleImage(articleList[0], binding.favArticleBanner1)
                binding.cardViewFavArticle2.visibility = View.VISIBLE
                binding.favArticleTitle2.text = articleList[1].name
                setArticleImage(articleList[1], binding.favArticleBanner2)
                binding.layoutFavArticleList2.visibility = View.VISIBLE
                binding.cardViewFavArticle3.visibility = View.VISIBLE
                binding.favArticleTitle3.text = articleList[2].name
                setArticleImage(articleList[2], binding.favArticleBanner3)
                binding.cardViewFavArticle4.visibility = View.GONE
            } else if (articleList.size >= 4) {
                binding.layoutFavArticleList1.visibility = View.VISIBLE
                binding.cardViewFavArticle1.visibility = View.VISIBLE
                binding.favArticleTitle1.text = articleList[0].name
                setArticleImage(articleList[0], binding.favArticleBanner1)
                binding.cardViewFavArticle2.visibility = View.VISIBLE
                binding.favArticleTitle2.text = articleList[1].name
                setArticleImage(articleList[1], binding.favArticleBanner2)
                binding.layoutFavArticleList2.visibility = View.VISIBLE
                binding.cardViewFavArticle3.visibility = View.VISIBLE
                binding.favArticleTitle3.text = articleList[2].name
                setArticleImage(articleList[2], binding.favArticleBanner3)
                binding.cardViewFavArticle4.visibility = View.VISIBLE
                binding.favArticleTitle4.text = articleList[3].name
                setArticleImage(articleList[3], binding.favArticleBanner4)
            }

            binding.cardViewFavArticle1.setOnClickListener {
                replaceFragment(
                    NewsDetailFragment.newInstance(articleList[0], wellnessType!!),
                    R.id.layout_home,
                    NewsDetailFragment.TAG
                )
            }

            binding.cardViewFavArticle2.setOnClickListener {
                replaceFragment(
                    NewsDetailFragment.newInstance(articleList[1], wellnessType!!),
                    R.id.layout_home,
                    NewsDetailFragment.TAG
                )
            }

            binding.cardViewFavArticle3.setOnClickListener {
                replaceFragment(
                    NewsDetailFragment.newInstance(articleList[2], wellnessType!!),
                    R.id.layout_home,
                    NewsDetailFragment.TAG
                )
            }

            binding.cardViewFavArticle4.setOnClickListener {
                replaceFragment(
                    NewsDetailFragment.newInstance(articleList[3], wellnessType!!),
                    R.id.layout_home,
                    NewsDetailFragment.TAG
                )
            }
        } else {
            binding.layoutArticleFavData.visibility = View.GONE
            //binding.cardViewFavNoArticles.visibility = View.VISIBLE
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
            binding.layoutVideoFavData.visibility = View.VISIBLE
            displayVideoList(filteredVideos)
        } else {
            if (text.length < 2) {
                displayVideoList(videoLists)
            } else {
                binding.layoutVideoFavData.visibility = View.GONE
            }
        }

        if (filteredPodcasts.isNotEmpty()) {
            binding.layoutPodcastFavData.visibility = View.VISIBLE
            val adapter = DashboardPodcastAdapter(
                mActivity!!,
                podcastLists, this, wellnessType!!
            )
            adapter.filterList(filteredPodcasts)
            binding.recyclerViewFavPodcast.adapter = adapter
        } else {
            if (text.length < 2) {
                displayPodcastList(podcastLists)
            } else {
                binding.layoutPodcastFavData.visibility = View.GONE
            }
        }

        if (filteredArticles.isNotEmpty()) {
            binding.layoutArticleFavData.visibility = View.VISIBLE
            displayArticleList(filteredArticles)
        } else {
            if (text.length < 2) {
                displayArticleList(articlesLists)
            } else {
                binding.layoutArticleFavData.visibility = View.GONE
            }
        }
    }

    override fun onPause() {
        super.onPause()
        binding.favSearch.setText("")
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FavoriteFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String = "") =
            FavoriteFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_Favorite"
    }

    override fun onPodcastItemClicked(podcast: Podcast) {
        replaceFragment(
            PodcastDetailFragment.newInstance(podcast, wellnessType!!),
            R.id.layout_home,
            PodcastDetailFragment.TAG
        )
    }
}