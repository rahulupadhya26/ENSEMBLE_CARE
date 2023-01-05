package com.app.selfcare.fragment

import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.app.selfcare.R
import com.app.selfcare.adapters.DashboardArticlesAdapter
import com.app.selfcare.adapters.NewsListAdapter
import com.app.selfcare.adapters.NewsSliderAdapter
import com.app.selfcare.controller.OnNewsItemClickListener
import com.app.selfcare.data.Articles
import com.app.selfcare.data.Podcast
import com.app.selfcare.data.Video
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper.get
import com.app.selfcare.utils.Utils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_dashboard.*
import kotlinx.android.synthetic.main.fragment_news_list.*
import org.json.JSONObject
import retrofit2.HttpException
import java.lang.reflect.Type
import kotlin.math.abs

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val ARG_PARAM3 = "param3"
private const val ARG_PARAM4 = "param4"

/**
 * A simple [Fragment] subclass.
 * Use the [NewsListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class NewsListFragment : BaseFragment(), OnNewsItemClickListener {
    // TODO: Rename and change types of parameters
    private var articles: ArrayList<Articles>? = null
    private var wellnessType: String? = null
    private var isFavourite: Boolean = false
    private var category: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            articles = it.getParcelableArrayList(ARG_PARAM1)
            wellnessType = it.getString(ARG_PARAM2)
            isFavourite = it.getBoolean(ARG_PARAM3)
            category = it.getString(ARG_PARAM4)
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_news_list
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE

        articlesBack.setOnClickListener {
            popBackStack()
        }

        if (articles != null && articles!!.isNotEmpty()) {
            displayArticles(articles!!)
        } else {
            when (wellnessType!!) {
                Utils.WELLNESS_EXERCISE -> {
                    if (isFavourite) {
                        getFavDetailArticleData()
                    } else {
                        getDetailArticleData("exercise_data/")
                    }
                }
                Utils.WELLNESS_NUTRITION -> {
                    if (isFavourite) {
                        getFavDetailArticleData()
                    } else {
                        getDetailArticleData("nutrition_data/")
                    }
                }
                Utils.WELLNESS_MINDFULNESS -> {
                    if (isFavourite) {
                        getFavDetailArticleData()
                    } else {
                        getDetailArticleData("mindfulness_data/")
                    }
                }
                Utils.WELLNESS_YOGA -> {
                    if (isFavourite) {
                        getFavDetailArticleData()
                    } else {
                        getDetailArticleData("yoga_data/")
                    }
                }
                else -> {
                    displayNews()
                }
            }
        }
    }

    private fun getFavDetailArticleData() {
        getFavoriteData(wellnessType!!) { response ->
            val jsonObj = JSONObject(response!!)
            val articleList: Type = object : TypeToken<ArrayList<Articles?>?>() {}.type
            val articlesList: ArrayList<Articles> =
                Gson().fromJson(jsonObj.getString("articles"), articleList)
            displayArticles(articlesList)
        }
    }

    private fun getDetailArticleData(type: String) {
        getDetailData(type, category!!) { response ->
            val jsonObj = JSONObject(response!!)
            val articleList: Type = object : TypeToken<ArrayList<Articles?>?>() {}.type
            val articlesList: ArrayList<Articles> =
                Gson().fromJson(jsonObj.getString("articles"), articleList)
            displayArticles(articlesList)
        }
    }

    private fun displayNews() {
        try {
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
                                //Articles
                                val articleList: Type =
                                    object : TypeToken<ArrayList<Articles?>?>() {}.type
                                val articlesList: ArrayList<Articles> =
                                    Gson().fromJson(jsonObj.getString("articles"), articleList)
                                displayArticles(articlesList)
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
                                    displayNews()
                                }
                            } else {
                                displayAfterLoginErrorMsg(error)
                            }
                        })
                )
            }
            handler.postDelayed(runnable!!, 1000)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun displayArticles(articlesList: ArrayList<Articles>) {
        if (articlesList.isNotEmpty()) {
            recyclerviewNewsList.apply {
                layoutManager = GridLayoutManager(mActivity!!, 2)
                adapter = NewsListAdapter(
                    mActivity!!,
                    articlesList, this@NewsListFragment
                )
            }
        } else {
            val builder = AlertDialog.Builder(mActivity!!)
            //builder.setIcon(R.drawable.work_in_progress)
            builder.setTitle("Alert")
            builder.setMessage("No articles found")
            builder.setPositiveButton("OK") { dialog, which ->
                dialog.dismiss()
                popBackStack()
            }
            builder.setCancelable(false)
            builder.show()
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment NewsListFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(
            param1: ArrayList<Articles>,
            param2: String,
            param3: Boolean,
            param4: String
        ) =
            NewsListFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                    putBoolean(ARG_PARAM3, param3)
                    putString(ARG_PARAM4, param4)
                }
            }

        const val TAG = "Screen_news"
    }

    override fun onNewsItemClicked(articles: Articles) {
        replaceFragment(
            NewsDetailFragment.newInstance(articles, wellnessType!!),
            R.id.layout_home,
            NewsDetailFragment.TAG
        )
    }
}