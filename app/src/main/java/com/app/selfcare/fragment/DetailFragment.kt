package com.app.selfcare.fragment

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.BaseActivity
import com.app.selfcare.R
import com.app.selfcare.adapters.DashboardPodcastAdapter
import com.app.selfcare.controller.OnPodcastItemClickListener
import com.app.selfcare.data.Articles
import com.app.selfcare.data.Podcast
import com.app.selfcare.data.Video
import com.app.selfcare.utils.Utils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_detail.*
import kotlinx.android.synthetic.main.fragment_resources.*
import org.json.JSONObject
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            category = it.getString(ARG_PARAM1)
            wellnessType = it.getString(ARG_PARAM2)
        }
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

        txtDetailTitle.text = category!!

        onClickEvents()

        when (wellnessType!!) {
            Utils.WELLNESS_EXERCISE -> {
                imgDetailBackground.setImageResource(R.drawable.exercise_background)
                updateStatusBarColor(R.color.primaryGreen)
                changeComponentColor(R.color.white)
                detailFavImg1.setImageResource(R.drawable.favourite_white)
                detailFavImg2.setImageResource(R.drawable.favourite_white)
                getExerciseDetail()
            }
            Utils.WELLNESS_NUTRITION -> {
                imgDetailBackground.setColorFilter(resources.getColor(R.color.white))
                updateStatusBarColor(R.color.white)
                changeComponentColor(R.color.black)
                detailFavImg1.setImageResource(R.drawable.favorite_outline)
                detailFavImg2.setImageResource(R.drawable.favorite_outline)
                getNutritionDetail()
            }
            Utils.WELLNESS_MINDFULNESS -> {
                imgDetailBackground.setImageResource(R.drawable.mindfulness_back_img)
                //frameLayoutDetail.background = resources.getDrawable(R.drawable.mindfulness_back_img)
                updateStatusBarColor(R.color.primaryGreen)
                changeComponentColor(R.color.white)
                detailFavImg1.setImageResource(R.drawable.favourite_white)
                detailFavImg2.setImageResource(R.drawable.favourite_white)
                getMindfulnessDetail()
            }
            Utils.WELLNESS_YOGA -> {
                imgDetailBackground.setImageResource(R.drawable.yoga_background)
                updateStatusBarColor(R.color.yoga_status_bar)
                changeComponentColor(R.color.white)
                detailFavImg1.setImageResource(R.drawable.favourite_white)
                detailFavImg2.setImageResource(R.drawable.favourite_white)
                getYogaDetail()
            }
        }

        detailSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            }

            override fun afterTextChanged(editable: Editable) {
                filterData(editable.toString())
            }
        })
    }

    private fun changeComponentColor(componentColor: Int) {
        detailBack.imageTintList = ColorStateList.valueOf(
            ContextCompat.getColor(
                requireActivity(),
                componentColor
            )
        )
        txtDetailTitle.setTextColor(resources.getColor(componentColor))
        txtDetailVideosTitle.setTextColor(resources.getColor(componentColor))
        txtDetailVideoSeeAll.setTextColor(resources.getColor(componentColor))
        detailVideoTitle1.setTextColor(resources.getColor(componentColor))
        detailVideoDesc1.setTextColor(resources.getColor(componentColor))
        detailFavTitle1.setTextColor(resources.getColor(componentColor))
        detailVideoTitle2.setTextColor(resources.getColor(componentColor))
        detailVideoDesc2.setTextColor(resources.getColor(componentColor))
        detailFavTitle2.setTextColor(resources.getColor(componentColor))
        txtDetailPodcastTitle.setTextColor(resources.getColor(componentColor))
        txtDetailPodcastSeeAll.setTextColor(resources.getColor(componentColor))
        txtDetailArticlesTitle.setTextColor(resources.getColor(componentColor))
        txtDetailArticleSeeAll.setTextColor(resources.getColor(componentColor))
        detailArticleTitle1.setTextColor(resources.getColor(componentColor))
        detailArticleTitle2.setTextColor(resources.getColor(componentColor))
        detailArticleTitle3.setTextColor(resources.getColor(componentColor))
        detailArticleTitle4.setTextColor(resources.getColor(componentColor))
    }

    private fun onClickEvents() {
        detailBack.setOnClickListener {
            popBackStack()
        }

        imgDetailFav.setOnClickListener {
            replaceFragment(
                FavoriteFragment.newInstance(wellnessType!!),
                R.id.layout_home,
                FavoriteFragment.TAG
            )
        }

        txtDetailVideoSeeAll.setOnClickListener {
            replaceFragment(
                VideosListFragment.newInstance(arrayListOf(), wellnessType!!, false, category!!),
                R.id.layout_home,
                VideosListFragment.TAG
            )
        }

        txtDetailPodcastSeeAll.setOnClickListener {
            replaceFragment(
                PodcastFragment.newInstance(arrayListOf(), wellnessType!!, false, category!!),
                R.id.layout_home, PodcastFragment.TAG
            )
        }

        txtDetailArticleSeeAll.setOnClickListener {
            replaceFragment(
                NewsListFragment.newInstance(arrayListOf(), wellnessType!!, false, category!!),
                R.id.layout_home, NewsListFragment.TAG
            )
        }
    }

    private fun getExerciseDetail() {
        videoLists = arrayListOf()
        podcastLists = arrayListOf()
        articlesLists = arrayListOf()
        getDetailData("exercise_data/", category!!) { response ->
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
        getDetailData("nutrition_data/", category!!) { response ->
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
        getDetailData("mindfulness_data/", category!!) { response ->
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
        getDetailData("yoga_data/", category!!) { response ->
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
            layoutDetailDisplayVideos.visibility = View.VISIBLE
            cardViewDetailNoVideos.visibility = View.GONE
            if (videoList.size == 1) {
                isFavouriteVideo1 = videoList[0].is_favourite
                layoutDetailVideoList1.visibility = View.VISIBLE
                setVideoImage(videoList[0], detailVideoBanner1)
                detailVideoTitle1.text = videoList[0].name
                detailVideoDesc1.text = videoList[0].description
                if (videoList[0].is_favourite) {
                    detailFavImg1.setImageResource(R.drawable.favorite)
                    detailFavTitle1.text = "Added to favorites"
                } else {
                    if (wellnessType == Utils.WELLNESS_NUTRITION) {
                        detailFavImg1.setImageResource(R.drawable.favorite_outline)
                    } else {
                        detailFavImg1.setImageResource(R.drawable.favourite_white)
                    }
                    detailFavTitle1.text = "Add to favorites"
                }
                layoutDetailVideoList2.visibility = View.GONE
            } else if (videoList.size >= 2) {
                isFavouriteVideo2 = videoList[1].is_favourite
                layoutDetailVideoList1.visibility = View.VISIBLE
                setVideoImage(videoList[0], detailVideoBanner1)
                detailVideoTitle1.text = videoList[0].name
                detailVideoDesc1.text = videoList[0].description
                if (videoList[0].is_favourite) {
                    detailFavImg1.setImageResource(R.drawable.favorite)
                    detailFavTitle1.text = "Added to favorites"
                } else {
                    if (wellnessType == Utils.WELLNESS_NUTRITION) {
                        detailFavImg1.setImageResource(R.drawable.favorite_outline)
                    } else {
                        detailFavImg1.setImageResource(R.drawable.favourite_white)
                    }
                    detailFavTitle1.text = "Add to favorites"
                }

                layoutDetailVideoList2.visibility = View.VISIBLE
                setVideoImage(videoList[1], detailVideoBanner2)
                detailVideoTitle2.text = videoList[1].name
                detailVideoDesc2.text = videoList[1].description
                if (videoList[1].is_favourite) {
                    detailFavImg2.setImageResource(R.drawable.favorite)
                    detailFavTitle2.text = "Added to favorites"
                } else {
                    if (wellnessType == Utils.WELLNESS_NUTRITION) {
                        detailFavImg2.setImageResource(R.drawable.favorite_outline)
                    } else {
                        detailFavImg2.setImageResource(R.drawable.favourite_white)
                    }
                    detailFavTitle2.text = "Add to favorites"
                }
            }

            cardViewDetailVideo1.setOnClickListener {
                replaceFragment(
                    VideoDetailFragment.newInstance(videoList[0]),
                    R.id.layout_home,
                    VideoDetailFragment.TAG
                )
            }

            cardViewDetailVideo2.setOnClickListener {
                replaceFragment(
                    VideoDetailFragment.newInstance(videoList[1]),
                    R.id.layout_home,
                    VideoDetailFragment.TAG
                )
            }

            detailLayoutFav1.setOnClickListener {
                sendFavoriteData(
                    videoList[0].id,
                    "Video",
                    !isFavouriteVideo1,
                    wellnessType!!
                ) {
                    if (!isFavouriteVideo1) {
                        detailFavImg1.setImageResource(R.drawable.favorite)
                        detailFavTitle1.text = "Added to favorites"
                    } else {
                        if (wellnessType == Utils.WELLNESS_NUTRITION) {
                            detailFavImg1.setImageResource(R.drawable.favorite_outline)
                        } else {
                            detailFavImg1.setImageResource(R.drawable.favourite_white)
                        }
                        detailFavTitle1.text = "Removed from favorites"
                    }

                    isFavouriteVideo1 = !isFavouriteVideo1
                }
            }

            detailLayoutFav2.setOnClickListener {
                sendFavoriteData(
                    videoList[1].id,
                    "Video",
                    !isFavouriteVideo2,
                    wellnessType!!
                ) {
                    if (!isFavouriteVideo2) {
                        detailFavImg2.setImageResource(R.drawable.favorite)
                        detailFavTitle2.text = "Added to favorites"
                    } else {
                        if (wellnessType == Utils.WELLNESS_NUTRITION) {
                            detailFavImg2.setImageResource(R.drawable.favorite_outline)
                        } else {
                            detailFavImg2.setImageResource(R.drawable.favourite_white)
                        }
                        detailFavTitle2.text = "Removed from favorites"
                    }
                    isFavouriteVideo2 = !isFavouriteVideo2
                }
            }
        } else {
            layoutDetailDisplayVideos.visibility = View.GONE
            cardViewDetailNoVideos.visibility = View.VISIBLE
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
            recyclerViewDetailPodcast.visibility = View.VISIBLE
            cardViewDetailNoPodcasts.visibility = View.GONE
            recyclerViewDetailPodcast.apply {
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
            recyclerViewDetailPodcast.visibility = View.GONE
            cardViewDetailNoPodcasts.visibility = View.VISIBLE
        }
    }

    private fun displayArticleList(articleList: ArrayList<Articles>) {
        if (articleList.isNotEmpty()) {
            layoutDetailDisplayArticles.visibility = View.VISIBLE
            cardViewDetailNoArticles.visibility = View.GONE
            if (articleList.size == 1) {
                layoutDetailArticleList1.visibility = View.VISIBLE
                cardViewDetailArticle1.visibility = View.VISIBLE
                detailArticleTitle1.text = articleList[0].name
                setArticleImage(articleList[0], detailArticleBanner1)
                cardViewDetailArticle2.visibility = View.GONE
                layoutDetailArticleList2.visibility = View.GONE
                cardViewDetailArticle3.visibility = View.GONE
                cardViewDetailArticle4.visibility = View.GONE
            } else if (articleList.size == 2) {
                layoutDetailArticleList1.visibility = View.VISIBLE
                cardViewDetailArticle1.visibility = View.VISIBLE
                detailArticleTitle1.text = articleList[0].name
                setArticleImage(articleList[0], detailArticleBanner1)
                cardViewDetailArticle2.visibility = View.VISIBLE
                detailArticleTitle2.text = articleList[1].name
                setArticleImage(articleList[1], detailArticleBanner2)
                layoutDetailArticleList2.visibility = View.GONE
                cardViewDetailArticle3.visibility = View.GONE
                cardViewDetailArticle4.visibility = View.GONE
            } else if (articleList.size == 3) {
                layoutDetailArticleList1.visibility = View.VISIBLE
                cardViewDetailArticle1.visibility = View.VISIBLE
                detailArticleTitle1.text = articleList[0].name
                setArticleImage(articleList[0], detailArticleBanner1)
                cardViewDetailArticle2.visibility = View.VISIBLE
                detailArticleTitle2.text = articleList[1].name
                setArticleImage(articleList[1], detailArticleBanner2)
                layoutDetailArticleList2.visibility = View.VISIBLE
                cardViewDetailArticle3.visibility = View.VISIBLE
                detailArticleTitle3.text = articleList[2].name
                setArticleImage(articleList[2], detailArticleBanner3)
                cardViewDetailArticle4.visibility = View.GONE
            } else if (articleList.size >= 4) {
                layoutDetailArticleList1.visibility = View.VISIBLE
                cardViewDetailArticle1.visibility = View.VISIBLE
                detailArticleTitle1.text = articleList[0].name
                setArticleImage(articleList[0], detailArticleBanner1)
                cardViewDetailArticle2.visibility = View.VISIBLE
                detailArticleTitle2.text = articleList[1].name
                setArticleImage(articleList[1], detailArticleBanner2)
                layoutDetailArticleList2.visibility = View.VISIBLE
                cardViewDetailArticle3.visibility = View.VISIBLE
                detailArticleTitle3.text = articleList[2].name
                setArticleImage(articleList[2], detailArticleBanner3)
                cardViewDetailArticle4.visibility = View.VISIBLE
                detailArticleTitle4.text = articleList[3].name
                setArticleImage(articleList[3], detailArticleBanner4)
            }

            cardViewDetailArticle1.setOnClickListener {
                replaceFragment(
                    NewsDetailFragment.newInstance(articleList[0], wellnessType!!),
                    R.id.layout_home,
                    NewsDetailFragment.TAG
                )
            }

            cardViewDetailArticle2.setOnClickListener {
                replaceFragment(
                    NewsDetailFragment.newInstance(articleList[1], wellnessType!!),
                    R.id.layout_home,
                    NewsDetailFragment.TAG
                )
            }

            cardViewDetailArticle3.setOnClickListener {
                replaceFragment(
                    NewsDetailFragment.newInstance(articleList[2], wellnessType!!),
                    R.id.layout_home,
                    NewsDetailFragment.TAG
                )
            }

            cardViewDetailArticle4.setOnClickListener {
                replaceFragment(
                    NewsDetailFragment.newInstance(articleList[3], wellnessType!!),
                    R.id.layout_home,
                    NewsDetailFragment.TAG
                )
            }
        } else {
            layoutDetailDisplayArticles.visibility = View.GONE
            cardViewDetailNoArticles.visibility = View.VISIBLE
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
            layoutVideoDetailData.visibility = View.VISIBLE
            displayVideoList(filteredVideos)
        } else {
            if (text.length < 2) {
                displayVideoList(videoLists)
            } else {
                layoutVideoDetailData.visibility = View.GONE
            }
        }

        if (filteredPodcasts.isNotEmpty()) {
            layoutPodcastDetailData.visibility = View.VISIBLE
            val adapter = DashboardPodcastAdapter(
                mActivity!!,
                podcastLists, this, wellnessType!!
            )
            adapter.filterList(filteredPodcasts)
            recyclerViewDetailPodcast.adapter = adapter
        } else {
            if (text.length < 2) {
                val adapter = DashboardPodcastAdapter(
                    mActivity!!,
                    podcastLists, this, wellnessType!!
                )
                adapter.filterList(filteredPodcasts)
                recyclerViewDetailPodcast.adapter = adapter
            } else {
                layoutPodcastDetailData.visibility = View.GONE
            }
        }

        if (filteredArticles.isNotEmpty()) {
            layoutArticleDetailData.visibility = View.VISIBLE
            displayArticleList(filteredArticles)
        } else {
            if (text.length < 2) {
                displayArticleList(articlesLists)
            } else {
                layoutArticleDetailData.visibility = View.GONE
            }
        }
    }

    override fun onPause() {
        super.onPause()
        detailSearch.setText("")
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