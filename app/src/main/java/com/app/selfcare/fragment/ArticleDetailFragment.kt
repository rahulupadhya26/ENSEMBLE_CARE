package com.app.selfcare.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.webkit.WebViewClient
import com.app.selfcare.R
import com.app.selfcare.data.Articles
import kotlinx.android.synthetic.main.fragment_news_detail.*
import kotlinx.android.synthetic.main.fragment_podcast_detail.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [NewsDetailFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class NewsDetailFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var articles: Articles? = null
    private var wellnessType: String? = null
    private var isFavourite: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            articles = it.getParcelable(ARG_PARAM1)
            wellnessType = it.getString(ARG_PARAM2)
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_news_detail
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE
        updateStatusBarColor(R.color.white)

        if (articles!!.is_favourite) {
            articlesDetailFav.setImageResource(R.drawable.favorite)
        } else {
            articlesDetailFav.setImageResource(R.drawable.favorite_outline)
        }

        isFavourite = articles!!.is_favourite

        articlesDetailBack.setOnClickListener {
            popBackStack()
        }

        articlesDetailFav.setOnClickListener {
            if (wellnessType!!.isNotEmpty()) {
                sendFavoriteData(
                    articles!!.id,
                    "Article",
                    !isFavourite,
                    wellnessType!!
                ) {
                    if (!isFavourite) {
                        articlesDetailFav.setImageResource(R.drawable.favorite)
                    } else {
                        articlesDetailFav.setImageResource(R.drawable.favorite_outline)
                    }
                    isFavourite = !isFavourite
                }
            } else {
                sendResourceFavoriteData(articles!!.id, "Article", !isFavourite) {
                    if (!isFavourite) {
                        articlesDetailFav.setImageResource(R.drawable.favorite)
                    } else {
                        articlesDetailFav.setImageResource(R.drawable.favorite_outline)
                    }
                    isFavourite = !isFavourite
                }
            }
        }

        try {
            // WebViewClient allows you to handle
            // onPageFinished and override Url loading.
            newsWebview.webViewClient = WebViewClient()

            // this will load the url of the website
            newsWebview.loadUrl(articles!!.article_url)

            // this will enable the javascript settings
            newsWebview.settings.javaScriptEnabled = true

            // if you want to enable zoom feature
            newsWebview.settings.setSupportZoom(true)
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
         * @return A new instance of fragment NewsDetailFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(articles: Articles, param2: String) =
            NewsDetailFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM1, articles)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_news_detail"
    }
}