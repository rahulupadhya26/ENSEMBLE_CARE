package com.app.selfcare.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import com.app.selfcare.BaseActivity
import com.app.selfcare.R
import com.app.selfcare.data.YogaDashboard
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
import kotlinx.android.synthetic.main.fragment_yoga_coach.*
import retrofit2.HttpException
import java.lang.reflect.Type
import java.util.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [YogaCoachFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class YogaCoachFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_yoga_coach
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE
        updateStatusBarColor(R.color.yoga_status_bar)

        onClickEvents()

        displayYogaData()

    }

    private fun onClickEvents() {
        yogaBack.setOnClickListener {
            popBackStack()
        }

        yogaFav.setOnClickListener {
            replaceFragment(
                FavoriteFragment.newInstance(Utils.WELLNESS_YOGA),
                R.id.layout_home,
                FavoriteFragment.TAG
            )
        }

        layoutWeightLoss.setOnClickListener {
            replaceFragment(
                DetailFragment.newInstance(txtWeightLoss.text.toString(), Utils.WELLNESS_YOGA),
                R.id.layout_home,
                DetailFragment.TAG
            )
        }

        layoutBackPain.setOnClickListener {
            replaceFragment(
                DetailFragment.newInstance(txtBackPain.text.toString(), Utils.WELLNESS_YOGA),
                R.id.layout_home,
                DetailFragment.TAG
            )
        }

        layoutHairGrowth.setOnClickListener {
            replaceFragment(
                DetailFragment.newInstance(txtHairGrowth.text.toString(), Utils.WELLNESS_YOGA),
                R.id.layout_home,
                DetailFragment.TAG
            )
        }

        layoutStress.setOnClickListener {
            replaceFragment(
                DetailFragment.newInstance(txtStress.text.toString(), Utils.WELLNESS_YOGA),
                R.id.layout_home,
                DetailFragment.TAG
            )
        }

        layoutDiabetes.setOnClickListener {
            replaceFragment(
                DetailFragment.newInstance(txtDiabetes.text.toString(), Utils.WELLNESS_YOGA),
                R.id.layout_home,
                DetailFragment.TAG
            )
        }

        layoutConstipation.setOnClickListener {
            replaceFragment(
                DetailFragment.newInstance(txtConstipation.text.toString(), Utils.WELLNESS_YOGA),
                R.id.layout_home,
                DetailFragment.TAG
            )
        }
    }

    private fun displayYogaData() {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .getYogaDashboardData("dashboard", getAccessToken())
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
                            val yogaDashboardDataType: Type =
                                object : TypeToken<ArrayList<YogaDashboard?>?>() {}.type
                            val yogaDashboardDataList: ArrayList<YogaDashboard> =
                                Gson().fromJson(responseBody, yogaDashboardDataType)

                            if (yogaDashboardDataList.isNotEmpty()) {
                                layoutWeeklyPicks.visibility = View.VISIBLE

                                if (yogaDashboardDataList.size == 1) {
                                    layoutYoga1.visibility = View.VISIBLE
                                    txtYogaName1.text =
                                        yogaDashboardDataList[0].yoga_name
                                    Glide.with(requireActivity())
                                        .load(BaseActivity.baseURL.dropLast(5) + yogaDashboardDataList[0].image)
                                        .transform(CenterCrop(), RoundedCorners(5))
                                        .into(imgYogaBackground1)

                                    layoutYoga2.visibility = View.GONE
                                } else if (yogaDashboardDataList.size >= 2) {
                                    layoutYoga1.visibility = View.VISIBLE
                                    txtYogaName1.text =
                                        yogaDashboardDataList[0].yoga_name
                                    Glide.with(requireActivity())
                                        .load(BaseActivity.baseURL.dropLast(5) + yogaDashboardDataList[0].image)
                                        .transform(CenterCrop(), RoundedCorners(5))
                                        .into(imgYogaBackground1)

                                    layoutYoga2.visibility = View.VISIBLE
                                    txtYogaName2.text =
                                        yogaDashboardDataList[1].yoga_name
                                    Glide.with(requireActivity())
                                        .load(BaseActivity.baseURL.dropLast(5) + yogaDashboardDataList[1].image)
                                        .transform(CenterCrop(), RoundedCorners(5))
                                        .into(imgYogaBackground2)
                                }

                            } else {
                                layoutWeeklyPicks.visibility = View.GONE
                            }

                            layoutYoga1.setOnClickListener {
                                displayRespectiveScreen(yogaDashboardDataList[0])
                            }

                            layoutYoga2.setOnClickListener {
                                displayRespectiveScreen(yogaDashboardDataList[1])
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
                                displayYogaData()
                            }
                        } else {
                            displayAfterLoginErrorMsg(error)
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    private fun displayRespectiveScreen(yogaDashboard: YogaDashboard) {
        if (yogaDashboard.type != null) {
            when (yogaDashboard.type) {
                "Video" -> {
                    replaceFragment(
                        VideoDetailFragment.newInstance(yogaDashboard.related_videos),
                        R.id.layout_home,
                        VideoDetailFragment.TAG
                    )
                }
                "Podcast" -> {
                    replaceFragment(
                        PodcastDetailFragment.newInstance(
                            yogaDashboard.related_podcast,
                            Utils.WELLNESS_YOGA
                        ),
                        R.id.layout_home,
                        PodcastDetailFragment.TAG
                    )
                }
                "Article" -> {
                    replaceFragment(
                        NewsDetailFragment.newInstance(yogaDashboard.related_articles, Utils.WELLNESS_YOGA),
                        R.id.layout_home,
                        NewsDetailFragment.TAG
                    )
                }
            }
        } else {
            displayToast("Invalid Data.")
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment YogaCoachFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            YogaCoachFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_Yoga"
    }
}