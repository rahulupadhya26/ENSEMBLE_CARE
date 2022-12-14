package com.app.selfcare.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import com.app.selfcare.BaseActivity
import com.app.selfcare.R
import com.app.selfcare.data.MindfulnessDashboard
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
import kotlinx.android.synthetic.main.fragment_mindfullness.*
import retrofit2.HttpException
import java.lang.reflect.Type
import java.util.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MindfullnessFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MindfullnessFragment : BaseFragment() {
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
        return R.layout.fragment_mindfullness
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE
        updateStatusBarColor(R.color.primaryGreen)

        onClickEvents()

        displayMindfulnessData()

    }

    private fun onClickEvents(){
        mindfulnessBack.setOnClickListener {
            popBackStack()
        }

        mindfulnessFav.setOnClickListener {
            replaceFragment(
                FavoriteFragment.newInstance(Utils.WELLNESS_MINDFULNESS),
                R.id.layout_home,
                FavoriteFragment.TAG
            )
        }

        layoutSpiritual.setOnClickListener {
            replaceFragment(
                DetailFragment.newInstance(txtSpiritual.text.toString(), Utils.WELLNESS_MINDFULNESS),
                R.id.layout_home,
                DetailFragment.TAG
            )
        }

        layoutMovement.setOnClickListener {
            replaceFragment(
                DetailFragment.newInstance(txtMovement.text.toString(), Utils.WELLNESS_MINDFULNESS),
                R.id.layout_home,
                DetailFragment.TAG
            )
        }

        layoutFocused.setOnClickListener {
            replaceFragment(
                DetailFragment.newInstance(txtFocused.text.toString(), Utils.WELLNESS_MINDFULNESS),
                R.id.layout_home,
                DetailFragment.TAG
            )
        }

        layoutProgressive.setOnClickListener {
            replaceFragment(
                DetailFragment.newInstance(txtProgressive.text.toString(), Utils.WELLNESS_MINDFULNESS),
                R.id.layout_home,
                DetailFragment.TAG
            )
        }

        layoutMantra.setOnClickListener {
            replaceFragment(
                DetailFragment.newInstance(txtMantra.text.toString(), Utils.WELLNESS_MINDFULNESS),
                R.id.layout_home,
                DetailFragment.TAG
            )
        }

        layoutTranscendental.setOnClickListener {
            replaceFragment(
                DetailFragment.newInstance(txtTranscendental.text.toString(), Utils.WELLNESS_MINDFULNESS),
                R.id.layout_home,
                DetailFragment.TAG
            )
        }

        layoutVisualization.setOnClickListener {
            replaceFragment(
                DetailFragment.newInstance(txtVisualization.text.toString(), Utils.WELLNESS_MINDFULNESS),
                R.id.layout_home,
                DetailFragment.TAG
            )
        }

        layoutNature.setOnClickListener {
            replaceFragment(
                DetailFragment.newInstance(txtNature.text.toString(), Utils.WELLNESS_MINDFULNESS),
                R.id.layout_home,
                DetailFragment.TAG
            )
        }
    }

    private fun displayMindfulnessData(){
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .getMindfulnessDashboardData("dashboard", getAccessToken())
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
                            val mindfulnessDashboardDataType: Type =
                                object : TypeToken<ArrayList<MindfulnessDashboard?>?>() {}.type
                            val mindfulnessDashboardDataList: ArrayList<MindfulnessDashboard> =
                                Gson().fromJson(responseBody, mindfulnessDashboardDataType)

                            if (mindfulnessDashboardDataList.isNotEmpty()) {
                                layoutTopPicksForYou.visibility = View.VISIBLE

                                if (mindfulnessDashboardDataList.size == 1) {
                                    layoutMindfulness1.visibility = View.VISIBLE
                                    txtMindfulnessName1.text =
                                        mindfulnessDashboardDataList[0].mindfulness_name
                                    Glide.with(requireActivity())
                                        .load(BaseActivity.baseURL.dropLast(5) + mindfulnessDashboardDataList[0].image)
                                        .transform(CenterCrop(), RoundedCorners(5))
                                        .into(imgMindfulnessBackground1)

                                    layoutMindfulness2.visibility = View.GONE
                                } else if (mindfulnessDashboardDataList.size >= 2) {
                                    layoutMindfulness1.visibility = View.VISIBLE
                                    txtMindfulnessName1.text =
                                        mindfulnessDashboardDataList[0].mindfulness_name
                                    Glide.with(requireActivity())
                                        .load(BaseActivity.baseURL.dropLast(5) + mindfulnessDashboardDataList[0].image)
                                        .transform(CenterCrop(), RoundedCorners(5))
                                        .into(imgMindfulnessBackground1)

                                    layoutMindfulness2.visibility = View.VISIBLE
                                    txtMindfulnessName2.text =
                                        mindfulnessDashboardDataList[1].mindfulness_name
                                    Glide.with(requireActivity())
                                        .load(BaseActivity.baseURL.dropLast(5) + mindfulnessDashboardDataList[1].image)
                                        .transform(CenterCrop(), RoundedCorners(5))
                                        .into(imgMindfulnessBackground2)
                                }

                            } else {
                                layoutTopPicksForYou.visibility = View.GONE
                            }

                            layoutMindfulness1.setOnClickListener {
                                displayRespectiveScreen(mindfulnessDashboardDataList[0])
                            }

                            layoutMindfulness2.setOnClickListener {
                                displayRespectiveScreen(mindfulnessDashboardDataList[1])
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
                                displayMindfulnessData()
                            }
                        } else {
                            displayAfterLoginErrorMsg(error)
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    private fun displayRespectiveScreen(mindfulnessDashboard: MindfulnessDashboard) {
        when (mindfulnessDashboard.type) {
            "Video" -> {
                replaceFragment(
                    VideoDetailFragment.newInstance(mindfulnessDashboard.related_videos),
                    R.id.layout_home,
                    VideoDetailFragment.TAG
                )
            }
            "Podcast" -> {
                replaceFragment(
                    PodcastDetailFragment.newInstance(mindfulnessDashboard.related_podcast, Utils.WELLNESS_MINDFULNESS),
                    R.id.layout_home,
                    PodcastDetailFragment.TAG
                )
            }
            "Article" -> {
                replaceFragment(
                    NewsDetailFragment.newInstance(mindfulnessDashboard.related_articles, Utils.WELLNESS_MINDFULNESS),
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
         * @return A new instance of fragment MindfullnessFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MindfullnessFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
        const val TAG = "Screen_Mindfulness"
    }
}