package com.app.selfcare.fragment

import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.app.selfcare.R
import com.app.selfcare.adapters.NewsListAdapter
import com.app.selfcare.adapters.NewsSliderAdapter
import com.app.selfcare.controller.OnNewsItemClickListener
import com.app.selfcare.data.News
import kotlinx.android.synthetic.main.fragment_news_list.*
import kotlin.math.abs

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [NewsListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class NewsListFragment : BaseFragment(), OnNewsItemClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var sliderHandler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_news_list
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.VISIBLE
        getSubTitle().visibility = View.GONE

        displayNews()
    }

    private fun displayNews(){
        val newsList: ArrayList<News> = arrayListOf()
        newsList.add(News("Ayurvedic practitioner shares herbs that will help manage diabetes", "https://indianexpress.com/article/lifestyle/health/ayurveda-herbs-spices-manage-diabetes-immunity-blood-sugar-8009363/", "06-07-2022 12:02"))
        newsList.add(News("Listeria Outbreak Affects 23, What to Know", "https://www.healthline.com/health-news/listeria-outbreak-affects-23-what-to-know", "06-07-2022 13:02"))
        newsList.add(News("Universal Flu Vaccine Gets Closer to Reality As Phase 1 Testing Starts", "https://www.healthline.com/health-news/universal-flu-vaccine-gets-closer-to-reality-as-phase-1-testing-starts", "06-07-2022 13:52"))

        viewPagerNewsSlider.adapter =
            NewsSliderAdapter(mActivity!!,
                newsList, viewPagerNewsSlider, this)

        viewPagerNewsSlider.clipToPadding = false
        viewPagerNewsSlider.clipChildren = false
        viewPagerNewsSlider.offscreenPageLimit = 3
        viewPagerNewsSlider.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER

        val compositePageTransform = CompositePageTransformer()
        compositePageTransform.addTransformer(MarginPageTransformer(40))
        compositePageTransform.addTransformer { page, position ->
            val r: Float = 1 - abs(position)
            page.scaleY = 0.85f + r * 0.15f
        }
        viewPagerNewsSlider.setPageTransformer(compositePageTransform)

        viewPagerNewsSlider.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                sliderHandler.removeCallbacks(sliderRunnable)
                sliderHandler.postDelayed(sliderRunnable, 5000)
            }
        })

        recyclerviewNewsList.apply {
            layoutManager = LinearLayoutManager(
                mActivity!!,
                RecyclerView.VERTICAL,
                false
            )
            adapter = NewsListAdapter(
                mActivity!!,
                newsList, this@NewsListFragment
            )
        }
    }

    private val sliderRunnable: Runnable = Runnable {
        viewPagerNewsSlider.currentItem = viewPagerNewsSlider.currentItem + 1
    }

    override fun onPause() {
        super.onPause()
        sliderHandler.removeCallbacks(sliderRunnable)
    }

    override fun onResume() {
        super.onResume()
        sliderHandler.postDelayed(sliderRunnable, 3000)
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
        fun newInstance(param1: String, param2: String) =
            NewsListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
        const val TAG = "Screen_news"
    }

    override fun onNewsItemClicked(news: News) {
        replaceFragment(
            NewsDetailFragment.newInstance(news),
            R.id.layout_home,
            NewsDetailFragment.TAG
        )
    }
}