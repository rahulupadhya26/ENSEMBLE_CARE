package com.app.selfcare.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import com.app.selfcare.BaseActivity
import com.app.selfcare.R
import com.app.selfcare.data.ExerciseDashboard
import com.app.selfcare.data.Video
import com.app.selfcare.databinding.FragmentActivityCarePlanBinding
import com.app.selfcare.databinding.FragmentExerciseBinding
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
import org.json.JSONObject
import retrofit2.HttpException
import java.lang.reflect.Type
import java.util.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ExerciseFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ExerciseFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentExerciseBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentExerciseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getLayout(): Int {
        return R.layout.fragment_exercise
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE
        updateStatusBarColor(R.color.primaryGreen)

        onClickEvents()

        displayExerciseData()
    }

    private fun onClickEvents(){
        binding.exerciseBack.setOnClickListener {
            setBottomNavigation(null)
            setLayoutBottomNavigation(null)
            replaceFragmentNoBackStack(
                BottomNavigationFragment(),
                R.id.layout_home,
                BottomNavigationFragment.TAG
            )
        }

        binding.exerciseFav.setOnClickListener {
            replaceFragment(
                FavoriteFragment.newInstance(Utils.WELLNESS_EXERCISE),
                R.id.layout_home,
                FavoriteFragment.TAG
            )
        }

        binding.layoutCardio.setOnClickListener {
            replaceFragment(
                DetailFragment.newInstance(binding.txtCardio.text.toString(), Utils.WELLNESS_EXERCISE),
                R.id.layout_home,
                DetailFragment.TAG
            )
        }

        binding.layoutFlexibility.setOnClickListener {
            replaceFragment(
                DetailFragment.newInstance(binding.txtFlexibility.text.toString(), Utils.WELLNESS_EXERCISE),
                R.id.layout_home,
                DetailFragment.TAG
            )
        }

        binding.layoutStrength.setOnClickListener {
            replaceFragment(
                DetailFragment.newInstance(binding.txtStrength.text.toString(), Utils.WELLNESS_EXERCISE),
                R.id.layout_home,
                DetailFragment.TAG
            )
        }

        binding.layoutBalance.setOnClickListener {
            replaceFragment(
                DetailFragment.newInstance(binding.txtBalance.text.toString(), Utils.WELLNESS_EXERCISE),
                R.id.layout_home,
                DetailFragment.TAG
            )
        }
    }

    private fun displayExerciseData() {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .getExerciseDashboardData("dashboard", getAccessToken())
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
                            val exerciseDashboardDataType: Type =
                                object : TypeToken<ArrayList<ExerciseDashboard?>?>() {}.type
                            val exerciseDashboardDataList: ArrayList<ExerciseDashboard> =
                                Gson().fromJson(responseBody, exerciseDashboardDataType)

                            if (exerciseDashboardDataList.isNotEmpty()) {
                                binding.hsvFeaturedWorkout.visibility = View.VISIBLE
                                binding.txtNoFeaturedWorkout.visibility = View.GONE

                                if (exerciseDashboardDataList.size == 1) {
                                    binding.cardViewExerciseData1.visibility = View.VISIBLE
                                    binding.txtExerciseName1.text =
                                        exerciseDashboardDataList[0].exercise_name
                                    Glide.with(requireActivity())
                                        .load(BaseActivity.baseURL.dropLast(5) + exerciseDashboardDataList[0].image)
                                        .transform(CenterCrop(), RoundedCorners(5))
                                        .into(binding.imgExercise1)

                                    binding.cardViewExerciseData2.visibility = View.GONE
                                    binding.cardViewExerciseData3.visibility = View.GONE
                                } else if (exerciseDashboardDataList.size == 2) {
                                    binding.cardViewExerciseData1.visibility = View.VISIBLE
                                    binding.txtExerciseName1.text =
                                        exerciseDashboardDataList[0].exercise_name
                                    Glide.with(requireActivity())
                                        .load(BaseActivity.baseURL.dropLast(5) + exerciseDashboardDataList[0].image)
                                        .transform(CenterCrop(), RoundedCorners(5))
                                        .into(binding.imgExercise1)

                                    binding.cardViewExerciseData2.visibility = View.VISIBLE
                                    binding.txtExerciseName2.text =
                                        exerciseDashboardDataList[1].exercise_name
                                    Glide.with(requireActivity())
                                        .load(BaseActivity.baseURL.dropLast(5) + exerciseDashboardDataList[1].image)
                                        .transform(CenterCrop(), RoundedCorners(5))
                                        .into(binding.imgExercise2)

                                    binding.cardViewExerciseData3.visibility = View.GONE
                                } else if (exerciseDashboardDataList.size >= 3) {
                                    binding.cardViewExerciseData1.visibility = View.VISIBLE
                                    binding.txtExerciseName1.text =
                                        exerciseDashboardDataList[0].exercise_name
                                    Glide.with(requireActivity())
                                        .load(BaseActivity.baseURL.dropLast(5) + exerciseDashboardDataList[0].image)
                                        .transform(CenterCrop(), RoundedCorners(5))
                                        .into(binding.imgExercise1)

                                    binding.cardViewExerciseData2.visibility = View.VISIBLE
                                    binding.txtExerciseName2.text =
                                        exerciseDashboardDataList[1].exercise_name
                                    Glide.with(requireActivity())
                                        .load(BaseActivity.baseURL.dropLast(5) + exerciseDashboardDataList[1].image)
                                        .transform(CenterCrop(), RoundedCorners(5))
                                        .into(binding.imgExercise2)

                                    binding.cardViewExerciseData3.visibility = View.VISIBLE
                                    binding.txtExerciseName3.text =
                                        exerciseDashboardDataList[2].exercise_name
                                    Glide.with(requireActivity())
                                        .load(BaseActivity.baseURL.dropLast(5) + exerciseDashboardDataList[2].image)
                                        .transform(CenterCrop(), RoundedCorners(5))
                                        .into(binding.imgExercise3)
                                }

                            } else {
                                binding.hsvFeaturedWorkout.visibility = View.GONE
                                binding.txtNoFeaturedWorkout.visibility = View.VISIBLE
                            }

                            binding.cardViewExerciseData1.setOnClickListener {
                                displayRespectiveScreen(exerciseDashboardDataList[0])
                            }

                            binding.cardViewExerciseData2.setOnClickListener {
                                displayRespectiveScreen(exerciseDashboardDataList[1])
                            }

                            binding.cardViewExerciseData3.setOnClickListener {
                                displayRespectiveScreen(exerciseDashboardDataList[2])
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
                                displayExerciseData()
                            }
                        } else {
                            displayAfterLoginErrorMsg(error)
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    private fun displayRespectiveScreen(exerciseDashboard: ExerciseDashboard) {
        when (exerciseDashboard.type) {
            "Video" -> {
                replaceFragment(
                    VideoDetailFragment.newInstance(exerciseDashboard.related_videos),
                    R.id.layout_home,
                    VideoDetailFragment.TAG
                )
            }
            "Podcast" -> {
                replaceFragment(
                    PodcastDetailFragment.newInstance(exerciseDashboard.related_podcast, Utils.WELLNESS_EXERCISE),
                    R.id.layout_home,
                    PodcastDetailFragment.TAG
                )
            }
            "Article" -> {
                replaceFragment(
                    NewsDetailFragment.newInstance(exerciseDashboard.related_articles, Utils.WELLNESS_EXERCISE),
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
         * @return A new instance of fragment ExerciseFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ExerciseFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_Exercise"
    }
}