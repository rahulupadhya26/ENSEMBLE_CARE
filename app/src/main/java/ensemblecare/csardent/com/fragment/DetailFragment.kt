package ensemblecare.csardent.com.fragment

import android.annotation.SuppressLint
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
import ensemblecare.csardent.com.databinding.FragmentDetailBinding
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
import kotlin.collections.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [DetailFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DetailFragment : BaseFragment(), OnPodcastItemClickListener {
    // TODO: Rename and change types of parameters
    private var category: String? = null
    private var wellnessType: String? = null
    private var videoLists: ArrayList<Video> = arrayListOf()
    private var podcastLists: ArrayList<Podcast> = arrayListOf()
    private var articlesLists: ArrayList<Articles> = arrayListOf()
    private var isFavouriteVideo1: Boolean = false
    private var isFavouriteVideo2: Boolean = false
    private lateinit var binding: FragmentDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            category = it.getString(ARG_PARAM1)
            wellnessType = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getLayout(): Int {
        return R.layout.fragment_detail
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE

        binding.txtDetailTitle.text = getCapsSentences(category!!)
        binding.txtDetailTitle.isSelected = true

        onClickEvents()

        binding.layoutShimmerDetailDisplayVideos.startShimmer()
        binding.layoutShimmerDetailDisplayVideos.visibility = View.VISIBLE
        binding.shimmerDetailPodcast.startShimmer()
        binding.shimmerDetailPodcast.visibility = View.VISIBLE
        binding.layoutShimmerDetailDisplayArticles.startShimmer()
        binding.layoutShimmerDetailDisplayArticles.visibility = View.VISIBLE
        binding.detailSearch.visibility = View.VISIBLE
        binding.cardViewDetailNoVideos.visibility = View.GONE
        binding.cardViewDetailNoArticles.visibility = View.GONE
        binding.cardViewDetailNoPodcasts.visibility = View.GONE

        when (wellnessType!!) {
            Utils.WELLNESS_EXERCISE -> {
                binding.imgDetailBackground.setImageResource(R.drawable.exercise_background)
                updateStatusBarColor(R.color.primaryGreen)
                changeComponentColor(R.color.white)
                binding.detailFavImg1.setImageResource(R.drawable.favourite_white)
                binding.detailFavImg2.setImageResource(R.drawable.favourite_white)
                binding.layoutPodcastDetailData.visibility = View.VISIBLE
                binding.txtDetailArticlesTitle.text = "Articles"
                getExerciseDetail()
            }

            Utils.WELLNESS_NUTRITION -> {
                binding.imgDetailBackground.setColorFilter(resources.getColor(R.color.white))
                updateStatusBarColor(R.color.white)
                changeComponentColor(R.color.black)
                binding.detailFavImg1.setImageResource(R.drawable.favorite_outline)
                binding.detailFavImg2.setImageResource(R.drawable.favorite_outline)
                binding.layoutPodcastDetailData.visibility = View.GONE
                binding.txtDetailArticlesTitle.text = "Recipes"
                getNutritionDetail()
            }

            Utils.WELLNESS_MINDFULNESS -> {
                binding.imgDetailBackground.setImageResource(R.drawable.mindfulness_back_img)
                //frameLayoutDetail.background = resources.getDrawable(R.drawable.mindfulness_back_img)
                updateStatusBarColor(R.color.primaryGreen)
                changeComponentColor(R.color.white)
                binding.detailFavImg1.setImageResource(R.drawable.favourite_white)
                binding.detailFavImg2.setImageResource(R.drawable.favourite_white)
                binding.layoutPodcastDetailData.visibility = View.VISIBLE
                binding.txtDetailArticlesTitle.text = "Articles"
                getMindfulnessDetail()
            }

            Utils.WELLNESS_YOGA -> {
                binding.imgDetailBackground.setImageResource(R.drawable.yoga_background)
                updateStatusBarColor(R.color.yoga_status_bar)
                changeComponentColor(R.color.white)
                binding.detailFavImg1.setImageResource(R.drawable.favourite_white)
                binding.detailFavImg2.setImageResource(R.drawable.favourite_white)
                binding.layoutPodcastDetailData.visibility = View.VISIBLE
                binding.txtDetailArticlesTitle.text = "Articles"
                getYogaDetail()
            }

            Utils.WELLNESS_MUSIC -> {
                binding.imgDetailBackground.setImageResource(R.drawable.music_background)
                updateStatusBarColor(R.color.music_status_bar)
                changeComponentColor(R.color.white)
                binding.detailFavImg1.setImageResource(R.drawable.favourite_white)
                binding.detailFavImg2.setImageResource(R.drawable.favourite_white)
                binding.layoutPodcastDetailData.visibility = View.VISIBLE
                binding.txtDetailArticlesTitle.text = "Articles"
                getMusicDetail()
            }
        }

        binding.detailSearch.addTextChangedListener(object : TextWatcher {
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
        binding.detailBack.imageTintList = ColorStateList.valueOf(
            ContextCompat.getColor(
                requireActivity(),
                componentColor
            )
        )
        binding.txtDetailTitle.setTextColor(resources.getColor(componentColor))
        binding.txtDetailVideosTitle.setTextColor(resources.getColor(componentColor))
        binding.txtDetailVideoSeeAll.setTextColor(resources.getColor(componentColor))
        binding.detailVideoTitle1.setTextColor(resources.getColor(componentColor))
        binding.detailVideoDesc1.setTextColor(resources.getColor(componentColor))
        binding.detailFavTitle1.setTextColor(resources.getColor(componentColor))
        binding.detailVideoTitle2.setTextColor(resources.getColor(componentColor))
        binding.detailVideoDesc2.setTextColor(resources.getColor(componentColor))
        binding.detailFavTitle2.setTextColor(resources.getColor(componentColor))
        binding.txtDetailPodcastTitle.setTextColor(resources.getColor(componentColor))
        binding.txtDetailPodcastSeeAll.setTextColor(resources.getColor(componentColor))
        binding.txtDetailArticlesTitle.setTextColor(resources.getColor(componentColor))
        binding.txtDetailArticleSeeAll.setTextColor(resources.getColor(componentColor))
        binding.detailArticleTitle1.setTextColor(resources.getColor(componentColor))
        binding.detailArticleTitle2.setTextColor(resources.getColor(componentColor))
        binding.detailArticleTitle3.setTextColor(resources.getColor(componentColor))
        binding.detailArticleTitle4.setTextColor(resources.getColor(componentColor))
    }

    private fun onClickEvents() {
        binding.detailBack.setOnClickListener {
            popBackStack()
        }

        binding.imgDetailFav.setOnClickListener {
            replaceFragment(
                FavoriteFragment.newInstance(wellnessType!!),
                R.id.layout_home,
                FavoriteFragment.TAG
            )
        }

        binding.txtDetailVideoSeeAll.setOnClickListener {
            replaceFragment(
                VideosListFragment.newInstance(arrayListOf(), wellnessType!!, false, category!!),
                R.id.layout_home,
                VideosListFragment.TAG
            )
        }

        binding.txtDetailPodcastSeeAll.setOnClickListener {
            replaceFragment(
                PodcastFragment.newInstance(arrayListOf(), wellnessType!!, false, category!!),
                R.id.layout_home, PodcastFragment.TAG
            )
        }

        binding.txtDetailArticleSeeAll.setOnClickListener {
            replaceFragment(
                NewsListFragment.newInstance(arrayListOf(), wellnessType!!, false, category!!),
                R.id.layout_home, NewsListFragment.TAG
            )
        }
    }

    private fun fetchDetailData(
        type: String,
        category: String,
        myCallback: (result: String?) -> Unit
    ) {
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .getDetailData(type, category, getAccessToken())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ result ->
                        try {
                            hideProgress()
                            binding.layoutShimmerDetailDisplayVideos.stopShimmer()
                            binding.layoutShimmerDetailDisplayVideos.visibility = View.GONE
                            binding.shimmerDetailPodcast.stopShimmer()
                            binding.shimmerDetailPodcast.visibility = View.GONE
                            binding.layoutShimmerDetailDisplayArticles.stopShimmer()
                            binding.layoutShimmerDetailDisplayArticles.visibility = View.GONE
                            binding.detailSearch.visibility = View.VISIBLE
                            binding.txtDetailVideoSeeAll.visibility = View.VISIBLE
                            binding.txtDetailPodcastSeeAll.visibility = View.VISIBLE
                            binding.txtDetailArticleSeeAll.visibility = View.VISIBLE
                            binding.cardViewDetailNoVideos.visibility = View.GONE
                            binding.cardViewDetailNoArticles.visibility = View.GONE
                            binding.cardViewDetailNoPodcasts.visibility = View.GONE
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
                            binding.layoutShimmerDetailDisplayVideos.stopShimmer()
                            binding.layoutShimmerDetailDisplayVideos.visibility = View.GONE
                            binding.shimmerDetailPodcast.stopShimmer()
                            binding.shimmerDetailPodcast.visibility = View.GONE
                            binding.layoutShimmerDetailDisplayArticles.stopShimmer()
                            binding.layoutShimmerDetailDisplayArticles.visibility = View.GONE
                            binding.detailSearch.visibility = View.GONE
                            binding.txtDetailVideoSeeAll.visibility = View.GONE
                            binding.txtDetailPodcastSeeAll.visibility = View.GONE
                            binding.txtDetailArticleSeeAll.visibility = View.GONE
                            binding.cardViewDetailNoVideos.visibility = View.VISIBLE
                            binding.cardViewDetailNoArticles.visibility = View.VISIBLE
                            binding.cardViewDetailNoPodcasts.visibility = View.VISIBLE
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
                            binding.layoutShimmerDetailDisplayVideos.stopShimmer()
                            binding.layoutShimmerDetailDisplayVideos.visibility = View.GONE
                            binding.shimmerDetailPodcast.stopShimmer()
                            binding.shimmerDetailPodcast.visibility = View.GONE
                            binding.layoutShimmerDetailDisplayArticles.stopShimmer()
                            binding.layoutShimmerDetailDisplayArticles.visibility = View.GONE
                            binding.detailSearch.visibility = View.GONE
                            binding.txtDetailVideoSeeAll.visibility = View.GONE
                            binding.txtDetailPodcastSeeAll.visibility = View.GONE
                            binding.txtDetailArticleSeeAll.visibility = View.GONE
                            binding.cardViewDetailNoVideos.visibility = View.VISIBLE
                            binding.cardViewDetailNoArticles.visibility = View.VISIBLE
                            binding.cardViewDetailNoPodcasts.visibility = View.VISIBLE
                            displayAfterLoginErrorMsg(error)
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    private fun getExerciseDetail() {
        videoLists = arrayListOf()
        podcastLists = arrayListOf()
        articlesLists = arrayListOf()
        fetchDetailData("exercise_data/", category!!) { response ->
            val jsonObj = JSONObject(response)

            val videoList: Type = object : TypeToken<ArrayList<Video?>?>() {}.type
            videoLists = Gson().fromJson(jsonObj.getString("videos"), videoList)
            displayVideoList(videoLists)

            val podcastList: Type = object : TypeToken<ArrayList<Podcast?>?>() {}.type
            podcastLists = Gson().fromJson(jsonObj.getString("podcasts"), podcastList)
            displayPodcastList(podcastLists)

            val articleList: Type = object : TypeToken<ArrayList<Articles?>?>() {}.type
            articlesLists = Gson().fromJson(jsonObj.getString("articles"), articleList)
            displayArticleList(articlesLists)
        }
    }

    private fun getNutritionDetail() {
        videoLists = arrayListOf()
        podcastLists = arrayListOf()
        articlesLists = arrayListOf()
        fetchDetailData("nutrition_data/", category!!) { response ->

            val jsonObj = JSONObject(response)

            val videoList: Type = object : TypeToken<ArrayList<Video?>?>() {}.type
            videoLists = Gson().fromJson(jsonObj.getString("videos"), videoList)
            displayVideoList(videoLists)

            val podcastList: Type = object : TypeToken<ArrayList<Podcast?>?>() {}.type
            podcastLists = Gson().fromJson(jsonObj.getString("podcasts"), podcastList)
            displayPodcastList(podcastLists)

            val articleList: Type = object : TypeToken<ArrayList<Articles?>?>() {}.type
            articlesLists = Gson().fromJson(jsonObj.getString("articles"), articleList)
            displayArticleList(articlesLists)
        }
    }

    private fun getMindfulnessDetail() {
        videoLists = arrayListOf()
        podcastLists = arrayListOf()
        articlesLists = arrayListOf()
        fetchDetailData("mindfulness_data/", category!!) { response ->

            val jsonObj = JSONObject(response)

            val videoList: Type = object : TypeToken<ArrayList<Video?>?>() {}.type
            videoLists = Gson().fromJson(jsonObj.getString("videos"), videoList)
            displayVideoList(videoLists)

            val podcastList: Type = object : TypeToken<ArrayList<Podcast?>?>() {}.type
            podcastLists = Gson().fromJson(jsonObj.getString("podcasts"), podcastList)
            displayPodcastList(podcastLists)

            val articleList: Type = object : TypeToken<ArrayList<Articles?>?>() {}.type
            articlesLists = Gson().fromJson(jsonObj.getString("articles"), articleList)
            displayArticleList(articlesLists)
        }
    }

    private fun getYogaDetail() {
        videoLists = arrayListOf()
        podcastLists = arrayListOf()
        articlesLists = arrayListOf()
        fetchDetailData("yoga_data/", category!!) { response ->
            val jsonObj = JSONObject(response)

            val videoList: Type = object : TypeToken<ArrayList<Video?>?>() {}.type
            videoLists = Gson().fromJson(jsonObj.getString("videos"), videoList)
            displayVideoList(videoLists)

            val podcastList: Type = object : TypeToken<ArrayList<Podcast?>?>() {}.type
            podcastLists = Gson().fromJson(jsonObj.getString("podcasts"), podcastList)
            displayPodcastList(podcastLists)

            val articleList: Type = object : TypeToken<ArrayList<Articles?>?>() {}.type
            articlesLists = Gson().fromJson(jsonObj.getString("articles"), articleList)
            displayArticleList(articlesLists)
        }
    }

    private fun getMusicDetail() {
        videoLists = arrayListOf()
        podcastLists = arrayListOf()
        articlesLists = arrayListOf()
        fetchDetailData("music_data/", category!!) { response ->
            val jsonObj = JSONObject(response)

            val videoList: Type = object : TypeToken<ArrayList<Video?>?>() {}.type
            videoLists = Gson().fromJson(jsonObj.getString("videos"), videoList)
            displayVideoList(videoLists)

            val podcastList: Type = object : TypeToken<ArrayList<Podcast?>?>() {}.type
            podcastLists = Gson().fromJson(jsonObj.getString("podcasts"), podcastList)
            displayPodcastList(podcastLists)

            val articleList: Type = object : TypeToken<ArrayList<Articles?>?>() {}.type
            articlesLists = Gson().fromJson(jsonObj.getString("articles"), articleList)
            displayArticleList(articlesLists)
        }
    }

    private fun displayVideoList(videoList: ArrayList<Video>) {
        if (videoList.isNotEmpty()) {
            binding.layoutDetailDisplayVideos.visibility = View.VISIBLE
            if (videoList.size == 1) {
                isFavouriteVideo1 = videoList[0].is_favourite
                binding.layoutDetailVideoList1.visibility = View.VISIBLE
                setVideoImage(videoList[0], binding.detailVideoBanner1)
                binding.detailVideoTitle1.text = videoList[0].name
                binding.detailVideoDesc1.text = videoList[0].description
                if (videoList[0].is_favourite) {
                    binding.detailFavImg1.setImageResource(R.drawable.favorite)
                    binding.detailFavTitle1.text = "Added to favorites"
                } else {
                    if (wellnessType == Utils.WELLNESS_NUTRITION) {
                        binding.detailFavImg1.setImageResource(R.drawable.favorite_outline)
                    } else {
                        binding.detailFavImg1.setImageResource(R.drawable.favourite_white)
                    }
                    binding.detailFavTitle1.text = "Add to favorites"
                }
                binding.layoutDetailVideoList2.visibility = View.GONE
            } else if (videoList.size >= 2) {
                isFavouriteVideo1 = videoList[0].is_favourite
                binding.layoutDetailVideoList1.visibility = View.VISIBLE
                setVideoImage(videoList[0], binding.detailVideoBanner1)
                binding.detailVideoTitle1.text = videoList[0].name
                binding.detailVideoDesc1.text = videoList[0].description
                if (videoList[0].is_favourite) {
                    binding.detailFavImg1.setImageResource(R.drawable.favorite)
                    binding.detailFavTitle1.text = "Added to favorites"
                } else {
                    if (wellnessType == Utils.WELLNESS_NUTRITION) {
                        binding.detailFavImg1.setImageResource(R.drawable.favorite_outline)
                    } else {
                        binding.detailFavImg1.setImageResource(R.drawable.favourite_white)
                    }
                    binding.detailFavTitle1.text = "Add to favorites"
                }

                isFavouriteVideo2 = videoList[1].is_favourite
                binding.layoutDetailVideoList2.visibility = View.VISIBLE
                setVideoImage(videoList[1], binding.detailVideoBanner2)
                binding.detailVideoTitle2.text = videoList[1].name
                binding.detailVideoDesc2.text = videoList[1].description
                if (videoList[1].is_favourite) {
                    binding.detailFavImg2.setImageResource(R.drawable.favorite)
                    binding.detailFavTitle2.text = "Added to favorites"
                } else {
                    if (wellnessType == Utils.WELLNESS_NUTRITION) {
                        binding.detailFavImg2.setImageResource(R.drawable.favorite_outline)
                    } else {
                        binding.detailFavImg2.setImageResource(R.drawable.favourite_white)
                    }
                    binding.detailFavTitle2.text = "Add to favorites"
                }
            }

            binding.cardViewDetailVideo1.setOnClickListener {
                replaceFragment(
                    VideoDetailFragment.newInstance(videoList[0]),
                    R.id.layout_home,
                    VideoDetailFragment.TAG
                )
            }

            binding.cardViewDetailVideo2.setOnClickListener {
                replaceFragment(
                    VideoDetailFragment.newInstance(videoList[1]),
                    R.id.layout_home,
                    VideoDetailFragment.TAG
                )
            }

            binding.detailLayoutFav1.setOnClickListener {
                sendFavoriteData(
                    videoList[0].id,
                    "Video",
                    !isFavouriteVideo1,
                    wellnessType!!
                ) {
                    if (!isFavouriteVideo1) {
                        binding.detailFavImg1.setImageResource(R.drawable.favorite)
                        binding.detailFavTitle1.text = "Added to favorites"
                    } else {
                        if (wellnessType == Utils.WELLNESS_NUTRITION) {
                            binding.detailFavImg1.setImageResource(R.drawable.favorite_outline)
                        } else {
                            binding.detailFavImg1.setImageResource(R.drawable.favourite_white)
                        }
                        binding.detailFavTitle1.text = "Removed from favorites"
                    }

                    isFavouriteVideo1 = !isFavouriteVideo1
                }
            }

            binding.detailLayoutFav2.setOnClickListener {
                sendFavoriteData(
                    videoList[1].id,
                    "Video",
                    !isFavouriteVideo2,
                    wellnessType!!
                ) {
                    if (!isFavouriteVideo2) {
                        binding.detailFavImg2.setImageResource(R.drawable.favorite)
                        binding.detailFavTitle2.text = "Added to favorites"
                    } else {
                        if (wellnessType == Utils.WELLNESS_NUTRITION) {
                            binding.detailFavImg2.setImageResource(R.drawable.favorite_outline)
                        } else {
                            binding.detailFavImg2.setImageResource(R.drawable.favourite_white)
                        }
                        binding.detailFavTitle2.text = "Removed from favorites"
                    }
                    isFavouriteVideo2 = !isFavouriteVideo2
                }
            }
        } else {
            binding.layoutDetailDisplayVideos.visibility = View.GONE
        }
    }

    private fun setVideoImage(video: Video, imageView: ImageView) {
        var videoImg = video.video_url
        if (video.video_url.isNotEmpty() && video.video_url.contains("youtube")) {
            val videoId: String = video.video_url.split("v=")[1]
            videoImg = "http://img.youtube.com/vi/$videoId/hqdefault.jpg" //high quality thumbnail
        }
        Glide.with(requireActivity()).load(videoImg)
            .transform(CenterCrop(), RoundedCorners(5))
            .into(imageView)
    }

    private fun displayPodcastList(podcastList: ArrayList<Podcast>) {
        if (podcastList.isNotEmpty()) {
            binding.layoutPodcastDetailData.visibility = View.VISIBLE
            binding.recyclerViewDetailPodcast.visibility = View.VISIBLE
            binding.recyclerViewDetailPodcast.apply {
                layoutManager = LinearLayoutManager(mActivity!!, RecyclerView.HORIZONTAL, false)
                adapter =
                    DashboardPodcastAdapter(
                        mActivity!!,
                        podcastList,
                        this@DetailFragment,
                        wellnessType!!
                    )
            }
        } else {
            binding.layoutPodcastDetailData.visibility = View.GONE
            binding.recyclerViewDetailPodcast.visibility = View.GONE
        }
    }

    private fun displayArticleList(articleList: ArrayList<Articles>) {
        if (articleList.isNotEmpty()) {
            binding.layoutArticleDetailData.visibility = View.VISIBLE
            //binding.cardViewDetailNoArticles.visibility = View.GONE
            if (articleList.size == 1) {
                binding.layoutDetailArticleList1.visibility = View.VISIBLE
                binding.cardViewDetailArticle1.visibility = View.VISIBLE
                binding.detailArticleTitle1.text = articleList[0].name
                setArticleImage(articleList[0], binding.detailArticleBanner1)
                binding.cardViewDetailArticle2.visibility = View.GONE
                binding.layoutDetailArticleList2.visibility = View.GONE
                binding.cardViewDetailArticle3.visibility = View.GONE
                binding.cardViewDetailArticle4.visibility = View.GONE
            } else if (articleList.size == 2) {
                binding.layoutDetailArticleList1.visibility = View.VISIBLE
                binding.cardViewDetailArticle1.visibility = View.VISIBLE
                binding.detailArticleTitle1.text = articleList[0].name
                setArticleImage(articleList[0], binding.detailArticleBanner1)
                binding.cardViewDetailArticle2.visibility = View.VISIBLE
                binding.detailArticleTitle2.text = articleList[1].name
                setArticleImage(articleList[1], binding.detailArticleBanner2)
                binding.layoutDetailArticleList2.visibility = View.GONE
                binding.cardViewDetailArticle3.visibility = View.GONE
                binding.cardViewDetailArticle4.visibility = View.GONE
            } else if (articleList.size == 3) {
                binding.layoutDetailArticleList1.visibility = View.VISIBLE
                binding.cardViewDetailArticle1.visibility = View.VISIBLE
                binding.detailArticleTitle1.text = articleList[0].name
                setArticleImage(articleList[0], binding.detailArticleBanner1)
                binding.cardViewDetailArticle2.visibility = View.VISIBLE
                binding.detailArticleTitle2.text = articleList[1].name
                setArticleImage(articleList[1], binding.detailArticleBanner2)
                binding.layoutDetailArticleList2.visibility = View.VISIBLE
                binding.cardViewDetailArticle3.visibility = View.VISIBLE
                binding.detailArticleTitle3.text = articleList[2].name
                setArticleImage(articleList[2], binding.detailArticleBanner3)
                binding.cardViewDetailArticle4.visibility = View.GONE
            } else if (articleList.size >= 4) {
                binding.layoutDetailArticleList1.visibility = View.VISIBLE
                binding.cardViewDetailArticle1.visibility = View.VISIBLE
                binding.detailArticleTitle1.text = articleList[0].name
                setArticleImage(articleList[0], binding.detailArticleBanner1)
                binding.cardViewDetailArticle2.visibility = View.VISIBLE
                binding.detailArticleTitle2.text = articleList[1].name
                setArticleImage(articleList[1], binding.detailArticleBanner2)
                binding.layoutDetailArticleList2.visibility = View.VISIBLE
                binding.cardViewDetailArticle3.visibility = View.VISIBLE
                binding.detailArticleTitle3.text = articleList[2].name
                setArticleImage(articleList[2], binding.detailArticleBanner3)
                binding.cardViewDetailArticle4.visibility = View.VISIBLE
                binding.detailArticleTitle4.text = articleList[3].name
                setArticleImage(articleList[3], binding.detailArticleBanner4)
            }

            binding.cardViewDetailArticle1.setOnClickListener {
                replaceFragment(
                    NewsDetailFragment.newInstance(articleList[0], wellnessType!!),
                    R.id.layout_home,
                    NewsDetailFragment.TAG
                )
            }

            binding.cardViewDetailArticle2.setOnClickListener {
                replaceFragment(
                    NewsDetailFragment.newInstance(articleList[1], wellnessType!!),
                    R.id.layout_home,
                    NewsDetailFragment.TAG
                )
            }

            binding.cardViewDetailArticle3.setOnClickListener {
                replaceFragment(
                    NewsDetailFragment.newInstance(articleList[2], wellnessType!!),
                    R.id.layout_home,
                    NewsDetailFragment.TAG
                )
            }

            binding.cardViewDetailArticle4.setOnClickListener {
                replaceFragment(
                    NewsDetailFragment.newInstance(articleList[3], wellnessType!!),
                    R.id.layout_home,
                    NewsDetailFragment.TAG
                )
            }
        } else {
            binding.layoutArticleDetailData.visibility = View.GONE
            //binding.cardViewDetailNoArticles.visibility = View.VISIBLE
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
            binding.layoutVideoDetailData.visibility = View.VISIBLE
            displayVideoList(filteredVideos)
        } else {
            if (text.length < 2) {
                displayVideoList(videoLists)
            } else {
                binding.layoutVideoDetailData.visibility = View.GONE
            }
        }

        if (filteredPodcasts.isNotEmpty()) {
            binding.layoutPodcastDetailData.visibility = View.VISIBLE
            val adapter = DashboardPodcastAdapter(
                mActivity!!,
                podcastLists, this, wellnessType!!
            )
            adapter.filterList(filteredPodcasts)
            binding.recyclerViewDetailPodcast.adapter = adapter
        } else {
            if (text.length < 2) {
                val adapter = DashboardPodcastAdapter(
                    mActivity!!,
                    podcastLists, this, wellnessType!!
                )
                adapter.filterList(filteredPodcasts)
                binding.recyclerViewDetailPodcast.adapter = adapter
            } else {
                binding.layoutPodcastDetailData.visibility = View.GONE
            }
        }

        if (filteredArticles.isNotEmpty()) {
            binding.layoutArticleDetailData.visibility = View.VISIBLE
            displayArticleList(filteredArticles)
        } else {
            if (text.length < 2) {
                displayArticleList(articlesLists)
            } else {
                binding.layoutArticleDetailData.visibility = View.GONE
            }
        }
    }

    override fun onPause() {
        super.onPause()
        binding.detailSearch.setText("")
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ExerciseDetailFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            DetailFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_detail"
    }

    override fun onPodcastItemClicked(podcast: Podcast) {
        replaceFragment(
            PodcastDetailFragment.newInstance(podcast, wellnessType!!),
            R.id.layout_home,
            PodcastDetailFragment.TAG
        )
    }
}