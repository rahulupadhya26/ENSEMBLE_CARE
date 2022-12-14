package com.app.selfcare.fragment

import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.app.selfcare.BaseActivity
import com.app.selfcare.R
import com.app.selfcare.adapters.NutritionSliderAdapter
import com.app.selfcare.controller.OnNutritionDashboardItemClickListener
import com.app.selfcare.data.ExerciseDashboard
import com.app.selfcare.data.Nutrition
import com.app.selfcare.data.NutritionDashboard
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
import kotlinx.android.synthetic.main.fragment_exercise.*
import kotlinx.android.synthetic.main.fragment_nutrition.*
import retrofit2.HttpException
import java.lang.reflect.Type
import kotlin.math.abs

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [NutritionFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class NutritionFragment : BaseFragment(), OnNutritionDashboardItemClickListener {
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
        return R.layout.fragment_nutrition
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE

        nutritionBack.setOnClickListener {
            popBackStack()
        }

        nutritionFav.setOnClickListener {
            replaceFragment(
                FavoriteFragment.newInstance(Utils.WELLNESS_NUTRITION),
                R.id.layout_home,
                FavoriteFragment.TAG
            )
        }

        cardViewNutritionSalad.setOnClickListener {
            replaceFragment(
                DetailFragment.newInstance(
                    txtNutritionSalad.text.toString(),
                    Utils.WELLNESS_NUTRITION
                ),
                R.id.layout_home,
                DetailFragment.TAG
            )
        }

        cardViewNutritionBreakfast.setOnClickListener {
            replaceFragment(
                DetailFragment.newInstance(
                    txtNutritionBreakfast.text.toString(),
                    Utils.WELLNESS_NUTRITION
                ),
                R.id.layout_home,
                DetailFragment.TAG
            )
        }

        cardViewNutritionSmoothie.setOnClickListener {
            replaceFragment(
                DetailFragment.newInstance(
                    txtNutritionSmoothie.text.toString(),
                    Utils.WELLNESS_NUTRITION
                ),
                R.id.layout_home,
                DetailFragment.TAG
            )
        }

        cardViewNutritionLunch.setOnClickListener {
            replaceFragment(
                DetailFragment.newInstance(
                    txtNutritionLunch.text.toString(),
                    Utils.WELLNESS_NUTRITION
                ),
                R.id.layout_home,
                DetailFragment.TAG
            )
        }

        displayNutritionData()
    }

    private fun displayNutritionData() {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .getNutritionDashboardData("dashboard", getAccessToken())
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
                            val nutritionDashboardDataType: Type =
                                object :
                                    TypeToken<java.util.ArrayList<NutritionDashboard?>?>() {}.type
                            val nutritionDashboardDataList: java.util.ArrayList<NutritionDashboard> =
                                Gson().fromJson(responseBody, nutritionDashboardDataType)

                            if (nutritionDashboardDataList.isNotEmpty()) {
                                viewPagerNutritionSlider.adapter =
                                    NutritionSliderAdapter(
                                        mActivity!!,
                                        nutritionDashboardDataList, viewPagerNutritionSlider, this
                                    )

                                viewPagerNutritionSlider.clipToPadding = false
                                viewPagerNutritionSlider.clipChildren = false
                                viewPagerNutritionSlider.offscreenPageLimit = 3
                                viewPagerNutritionSlider.getChildAt(0).overScrollMode =
                                    RecyclerView.OVER_SCROLL_NEVER

                                val compositePageTransform = CompositePageTransformer()
                                compositePageTransform.addTransformer(MarginPageTransformer(0))
                                compositePageTransform.addTransformer { page, position ->
                                    val r: Float = 1 - abs(position)
                                    page.scaleY = 0.85f + r * 0.15f
                                }
                                viewPagerNutritionSlider.setPageTransformer(compositePageTransform)

                                viewPagerNutritionSlider.registerOnPageChangeCallback(object :
                                    ViewPager2.OnPageChangeCallback() {
                                    override fun onPageSelected(position: Int) {
                                        super.onPageSelected(position)
                                        sliderHandler.removeCallbacks(sliderRunnable)
                                        sliderHandler.postDelayed(sliderRunnable, 5000)
                                    }
                                })

                                if (nutritionDashboardDataList.size == 1) {
                                    cardViewNutritionCard1.visibility = View.VISIBLE
                                    txtNutrition1.text =
                                        nutritionDashboardDataList[0].nutrition_name
                                    txtNutritionTimeTaken1.text =
                                        nutritionDashboardDataList[0].time_taken
                                    Glide.with(requireActivity())
                                        .load(BaseActivity.baseURL.dropLast(5) + nutritionDashboardDataList[0].image)
                                        .transform(CenterCrop(), RoundedCorners(5))
                                        .into(imgNutrition1)

                                    cardViewNutritionCard2.visibility = View.GONE
                                } else if (nutritionDashboardDataList.size >= 2) {
                                    cardViewNutritionCard1.visibility = View.VISIBLE
                                    txtNutrition1.text =
                                        nutritionDashboardDataList[0].nutrition_name
                                    txtNutritionTimeTaken1.text =
                                        nutritionDashboardDataList[0].time_taken
                                    Glide.with(requireActivity())
                                        .load(BaseActivity.baseURL.dropLast(5) + nutritionDashboardDataList[0].image)
                                        .transform(CenterCrop(), RoundedCorners(5))
                                        .into(imgNutrition1)

                                    cardViewNutritionCard2.visibility = View.VISIBLE
                                    txtNutrition2.text =
                                        nutritionDashboardDataList[1].nutrition_name
                                    txtNutritionTimeTaken2.text =
                                        nutritionDashboardDataList[1].time_taken
                                    Glide.with(requireActivity())
                                        .load(BaseActivity.baseURL.dropLast(5) + nutritionDashboardDataList[1].image)
                                        .transform(CenterCrop(), RoundedCorners(5))
                                        .into(imgNutrition2)
                                }
                            } else {
                                cardViewNutritionCard1.visibility = View.GONE
                                cardViewNutritionCard2.visibility = View.GONE
                            }

                            cardViewNutritionCard1.setOnClickListener {
                                displayRespectiveScreen(nutritionDashboardDataList[0])
                            }

                            cardViewNutritionCard2.setOnClickListener {
                                displayRespectiveScreen(nutritionDashboardDataList[1])
                            }

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
                                displayNutritionData()
                            }
                        } else {
                            displayAfterLoginErrorMsg(error)
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    private val sliderRunnable: Runnable = Runnable {
        viewPagerNutritionSlider.currentItem = viewPagerNutritionSlider.currentItem + 1
    }

    override fun onPause() {
        super.onPause()
        sliderHandler.removeCallbacks(sliderRunnable)
    }

    override fun onResume() {
        super.onResume()
        sliderHandler.postDelayed(sliderRunnable, 3000)
    }

    private fun displayRespectiveScreen(nutritionDashboard: NutritionDashboard) {
        when (nutritionDashboard.type) {
            "Video" -> {
                replaceFragment(
                    VideoDetailFragment.newInstance(nutritionDashboard.related_videos),
                    R.id.layout_home,
                    VideoDetailFragment.TAG
                )
            }
            "Podcast" -> {
                replaceFragment(
                    PodcastDetailFragment.newInstance(nutritionDashboard.related_podcast, Utils.WELLNESS_NUTRITION),
                    R.id.layout_home,
                    PodcastDetailFragment.TAG
                )
            }
            "Article" -> {
                replaceFragment(
                    NewsDetailFragment.newInstance(nutritionDashboard.related_articles, Utils.WELLNESS_NUTRITION),
                    R.id.layout_home,
                    NewsDetailFragment.TAG
                )
            }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment NutritionFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            NutritionFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_Nutrition"
    }

    override fun onNutritionDashboardItemClicked(nutritionDashboard: NutritionDashboard) {
        displayRespectiveScreen(nutritionDashboard)
    }
}