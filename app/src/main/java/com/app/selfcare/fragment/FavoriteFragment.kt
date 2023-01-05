package com.app.selfcare.fragment

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
import kotlinx.android.synthetic.main.fragment_favorite.*
import kotlinx.android.synthetic.main.fragment_resources.*
import org.json.JSONObject
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            wellnessType = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
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
                imgFavBackground.setImageResource(R.drawable.exercise_background)
                updateStatusBarColor(R.color.primaryGreen)
                changeComponentColor(R.color.white)
            }
            Utils.WELLNESS_NUTRITION -> {
                imgFavBackground.setColorFilter(resources.getColor(R.color.white))
                updateStatusBarColor(R.color.white)
                changeComponentColor(R.color.black)
            }
            Utils.WELLNESS_MINDFULNESS -> {
                imgFavBackground.setImageResource(R.drawable.mindfulness_back_img)
                //frameLayoutDetail.background = resources.getDrawable(R.drawable.mindfulness_back_img)
                updateStatusBarColor(R.color.primaryGreen)
                changeComponentColor(R.color.white)
            }
            Utils.WELLNESS_YOGA -> {
                imgFavBackground.setImageResource(R.drawable.yoga_background)
                updateStatusBarColor(R.color.yoga_status_bar)
                changeComponentColor(R.color.white)
            }
            else -> {
                imgFavBackground.setColorFilter(resources.getColor(R.color.white))
                updateStatusBarColor(R.color.white)
                changeComponentColor(R.color.black)
            }
        }

        getFavouriteDetails()

        favSearch.addTextChangedListener(object : TextWatcher {
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
        favBack.imageTintList = ColorStateList.valueOf(
            ContextCompat.getColor(
                requireActivity(),
                componentColor
            )
        )
        txtFavTitle.setTextColor(resources.getColor(componentColor))
        favSearch.setHintTextColor(resources.getColor(componentColor))
        txtFavVideosTitle.setTextColor(resources.getColor(componentColor))
        txtFavVideoSeeAll.setTextColor(resources.getColor(componentColor))
        favVideoTitle1.setTextColor(resources.getColor(componentColor))
        favVideoDesc1.setTextColor(resources.getColor(componentColor))
        favFavTitle1.setTextColor(resources.getColor(componentColor))
        favVideoTitle2.setTextColor(resources.getColor(componentColor))
        favVideoDesc2.setTextColor(resources.getColor(componentColor))
        favFavTitle2.setTextColor(resources.getColor(componentColor))
        txtFavPodcastTitle.setTextColor(resources.getColor(componentColor))
        txtFavPodcastSeeAll.setTextColor(resources.getColor(componentColor))
        txtFavArticlesTitle.setTextColor(resources.getColor(componentColor))
        txtFavArticleSeeAll.setTextColor(resources.getColor(componentColor))
        favArticleTitle1.setTextColor(resources.getColor(componentColor))
        favArticleTitle2.setTextColor(resources.getColor(componentColor))
        favArticleTitle3.setTextColor(resources.getColor(componentColor))
        favArticleTitle4.setTextColor(resources.getColor(componentColor))
    }

    private fun onClickEvents() {
        favBack.setOnClickListener {
            popBackStack()
        }

        txtFavVideoSeeAll.setOnClickListener {
            replaceFragment(
                VideosListFragment.newInstance(arrayListOf(), wellnessType!!, true, ""),
                R.id.layout_home,
                VideosListFragment.TAG
            )
        }

        txtFavPodcastSeeAll.setOnClickListener {
            replaceFragment(
                PodcastFragment.newInstance(arrayListOf(), wellnessType!!, true, ""),
                R.id.layout_home, PodcastFragment.TAG
            )
        }

        txtFavArticleSeeAll.setOnClickListener {
            replaceFragment(
                NewsListFragment.newInstance(arrayListOf(), wellnessType!!, true, ""),
                R.id.layout_home, NewsListFragment.TAG
            )
        }
    }

    private fun getFavouriteDetails() {
        videoLists = arrayListOf()
        podcastLists = arrayListOf()
        articlesLists = arrayListOf()
        if(wellnessType!!.isNotEmpty()) {
            getFavoriteData(wellnessType!!) { response ->
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
        } else {
            getResourceFavoriteData { response ->
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
    }

    private fun displayVideoList(videoList: ArrayList<Video>) {
        if (videoList.isNotEmpty()) {
            layoutFavDisplayVideos.visibility = View.VISIBLE
            cardViewFavNoVideos.visibility = View.GONE
            if (videoList.size == 1) {
                layoutFavVideoList1.visibility = View.VISIBLE
                setVideoImage(videoList[0], favVideoBanner1)
                favVideoTitle1.text = videoList[0].name
                favVideoDesc1.text = videoList[0].description
                if (videoList[0].is_favourite) {
                    favFavImg1.setImageResource(R.drawable.favorite)
                    favFavTitle1.text = "Added to favorites"
                } else {
                    if (wellnessType == Utils.WELLNESS_NUTRITION) {
                        favFavImg1.setImageResource(R.drawable.favorite_outline)
                    } else {
                        favFavImg1.setImageResource(R.drawable.favourite_white)
                    }
                    favFavTitle1.text = "Add to favorites"
                }
                layoutFavVideoList2.visibility = View.GONE
            } else if (videoList.size >= 2) {
                layoutFavVideoList1.visibility = View.VISIBLE
                setVideoImage(videoList[0], favVideoBanner1)
                favVideoTitle1.text = videoList[0].name
                favVideoDesc1.text = videoList[0].description
                if (videoList[0].is_favourite) {
                    favFavImg1.setImageResource(R.drawable.favorite)
                    favFavTitle1.text = "Added to favorites"
                } else {
                    if (wellnessType == Utils.WELLNESS_NUTRITION) {
                        favFavImg1.setImageResource(R.drawable.favorite_outline)
                    } else {
                        favFavImg1.setImageResource(R.drawable.favourite_white)
                    }
                    favFavTitle1.text = "Add to favorites"
                }

                layoutFavVideoList2.visibility = View.VISIBLE
                setVideoImage(videoList[1], favVideoBanner2)
                favVideoTitle2.text = videoList[1].name
                favVideoDesc2.text = videoList[1].description
                if (videoList[1].is_favourite) {
                    favFavImg2.setImageResource(R.drawable.favorite)
                    favFavTitle2.text = "Added to favorites"
                } else {
                    if (wellnessType == Utils.WELLNESS_NUTRITION) {
                        favFavImg2.setImageResource(R.drawable.favorite_outline)
                    } else {
                        favFavImg2.setImageResource(R.drawable.favourite_white)
                    }
                    favFavTitle2.text = "Add to favorites"
                }
            }

            cardViewFavVideo1.setOnClickListener {
                replaceFragment(
                    VideoDetailFragment.newInstance(videoList[0]),
                    R.id.layout_home,
                    VideoDetailFragment.TAG
                )
            }

            cardViewFavVideo2.setOnClickListener {
                replaceFragment(
                    VideoDetailFragment.newInstance(videoList[1]),
                    R.id.layout_home,
                    VideoDetailFragment.TAG
                )
            }

            favLayoutFav1.setOnClickListener {
                if(wellnessType!!.isNotEmpty()) {
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

            favLayoutFav2.setOnClickListener {
                if(wellnessType!!.isNotEmpty()) {
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
            layoutFavDisplayVideos.visibility = View.GONE
            cardViewFavNoVideos.visibility = View.VISIBLE
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
            recyclerViewFavPodcast.visibility = View.VISIBLE
            cardViewFavNoPodcasts.visibility = View.GONE
            recyclerViewFavPodcast.apply {
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
            recyclerViewFavPodcast.visibility = View.GONE
            cardViewFavNoPodcasts.visibility = View.VISIBLE
        }
    }

    private fun displayArticleList(articleList: ArrayList<Articles>) {
        if (articleList.isNotEmpty()) {
            layoutFavDisplayArticles.visibility = View.VISIBLE
            cardViewFavNoArticles.visibility = View.GONE
            if (articleList.size == 1) {
                layoutFavArticleList1.visibility = View.VISIBLE
                cardViewFavArticle1.visibility = View.VISIBLE
                favArticleTitle1.text = articleList[0].name
                setArticleImage(articleList[0], favArticleBanner1)
                cardViewFavArticle2.visibility = View.GONE
                layoutFavArticleList2.visibility = View.GONE
                cardViewFavArticle3.visibility = View.GONE
                cardViewFavArticle4.visibility = View.GONE
            } else if (articleList.size == 2) {
                layoutFavArticleList1.visibility = View.VISIBLE
                cardViewFavArticle1.visibility = View.VISIBLE
                favArticleTitle1.text = articleList[0].name
                setArticleImage(articleList[0], favArticleBanner1)
                cardViewFavArticle2.visibility = View.VISIBLE
                favArticleTitle2.text = articleList[1].name
                setArticleImage(articleList[1], favArticleBanner2)
                layoutFavArticleList2.visibility = View.GONE
                cardViewFavArticle3.visibility = View.GONE
                cardViewFavArticle4.visibility = View.GONE
            } else if (articleList.size == 3) {
                layoutFavArticleList1.visibility = View.VISIBLE
                cardViewFavArticle1.visibility = View.VISIBLE
                favArticleTitle1.text = articleList[0].name
                setArticleImage(articleList[0], favArticleBanner1)
                cardViewFavArticle2.visibility = View.VISIBLE
                favArticleTitle2.text = articleList[1].name
                setArticleImage(articleList[1], favArticleBanner2)
                layoutFavArticleList2.visibility = View.VISIBLE
                cardViewFavArticle3.visibility = View.VISIBLE
                favArticleTitle3.text = articleList[2].name
                setArticleImage(articleList[2], favArticleBanner3)
                cardViewFavArticle4.visibility = View.GONE
            } else if (articleList.size >= 4) {
                layoutFavArticleList1.visibility = View.VISIBLE
                cardViewFavArticle1.visibility = View.VISIBLE
                favArticleTitle1.text = articleList[0].name
                setArticleImage(articleList[0], favArticleBanner1)
                cardViewFavArticle2.visibility = View.VISIBLE
                favArticleTitle2.text = articleList[1].name
                setArticleImage(articleList[1], favArticleBanner2)
                layoutFavArticleList2.visibility = View.VISIBLE
                cardViewFavArticle3.visibility = View.VISIBLE
                favArticleTitle3.text = articleList[2].name
                setArticleImage(articleList[2], favArticleBanner3)
                cardViewFavArticle4.visibility = View.VISIBLE
                favArticleTitle4.text = articleList[3].name
                setArticleImage(articleList[3], favArticleBanner4)
            }

            cardViewFavArticle1.setOnClickListener {
                replaceFragment(
                    NewsDetailFragment.newInstance(articleList[0], wellnessType!!),
                    R.id.layout_home,
                    NewsDetailFragment.TAG
                )
            }

            cardViewFavArticle2.setOnClickListener {
                replaceFragment(
                    NewsDetailFragment.newInstance(articleList[1], wellnessType!!),
                    R.id.layout_home,
                    NewsDetailFragment.TAG
                )
            }

            cardViewFavArticle3.setOnClickListener {
                replaceFragment(
                    NewsDetailFragment.newInstance(articleList[2], wellnessType!!),
                    R.id.layout_home,
                    NewsDetailFragment.TAG
                )
            }

            cardViewFavArticle4.setOnClickListener {
                replaceFragment(
                    NewsDetailFragment.newInstance(articleList[3], wellnessType!!),
                    R.id.layout_home,
                    NewsDetailFragment.TAG
                )
            }
        } else {
            layoutFavDisplayArticles.visibility = View.GONE
            cardViewFavNoArticles.visibility = View.VISIBLE
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
            layoutVideoFavData.visibility = View.VISIBLE
            displayVideoList(filteredVideos)
        } else {
            if (text.length < 2) {
                displayVideoList(videoLists)
            } else {
                layoutVideoFavData.visibility = View.GONE
            }
        }

        if (filteredPodcasts.isNotEmpty()) {
            layoutPodcastFavData.visibility = View.VISIBLE
            val adapter = DashboardPodcastAdapter(
                mActivity!!,
                podcastLists, this, wellnessType!!
            )
            adapter.filterList(filteredPodcasts)
            recyclerViewFavPodcast.adapter = adapter
        } else {
            if (text.length < 2) {
                val adapter = DashboardPodcastAdapter(
                    mActivity!!,
                    podcastLists, this, wellnessType!!
                )
                adapter.filterList(filteredPodcasts)
                recyclerViewFavPodcast.adapter = adapter
            } else {
                layoutPodcastFavData.visibility = View.GONE
            }
        }

        if (filteredArticles.isNotEmpty()) {
            layoutArticleFavData.visibility = View.VISIBLE
            displayArticleList(filteredArticles)
        } else {
            if (text.length < 2) {
                displayArticleList(articlesLists)
            } else {
                layoutArticleFavData.visibility = View.GONE
            }
        }
    }

    override fun onPause() {
        super.onPause()
        favSearch.setText("")
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