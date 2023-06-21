package ensemblecare.csardent.com.fragment

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import ensemblecare.csardent.com.BaseActivity
import ensemblecare.csardent.com.R
import ensemblecare.csardent.com.adapters.ExerciseDashboardAdapter
import ensemblecare.csardent.com.controller.OnExerciseDashboardItemClickListener
import ensemblecare.csardent.com.data.ExerciseDashboard
import ensemblecare.csardent.com.databinding.FragmentExerciseBinding
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
import retrofit2.HttpException
import java.lang.Math.abs
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
class ExerciseFragment : BaseFragment(), OnExerciseDashboardItemClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentExerciseBinding
    private var sliderHandler = Handler()

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
        binding.shimmerFeaturedWorkoutExercise.visibility = View.VISIBLE
        binding.viewPagerFeaturedWorkout.visibility = View.GONE
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .getExerciseDashboardData("dashboard", getAccessToken())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ result ->
                        try {
                            hideProgress()
                            binding.shimmerFeaturedWorkoutExercise.visibility = View.GONE
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
                                binding.viewPagerFeaturedWorkout.visibility = View.VISIBLE
                                binding.txtNoFeaturedWorkout.visibility = View.GONE

                                val viewPagerAdapter =
                                    ExerciseDashboardAdapter(
                                        requireActivity(),
                                        exerciseDashboardDataList,
                                        binding.viewPagerFeaturedWorkout,
                                        this
                                    )
                                binding.viewPagerFeaturedWorkout.adapter = viewPagerAdapter
                                binding.viewPagerFeaturedWorkout.currentItem = 0
                                binding.viewPagerFeaturedWorkout.clipToPadding = false
                                binding.viewPagerFeaturedWorkout.clipChildren = false
                                binding.viewPagerFeaturedWorkout.offscreenPageLimit = 3
                                binding.viewPagerFeaturedWorkout.getChildAt(0).overScrollMode =
                                    RecyclerView.OVER_SCROLL_NEVER
                                val compositePageTransform = CompositePageTransformer()
                                compositePageTransform.addTransformer(MarginPageTransformer(50))
                                compositePageTransform.addTransformer { page, position ->
                                    val r: Float = 1 - abs(position)
                                    page.scaleY = 0.85f + r * 0.15f
                                }
                                binding.viewPagerFeaturedWorkout.setPageTransformer(compositePageTransform)
                                binding.viewPagerFeaturedWorkout.registerOnPageChangeCallback(object :
                                    ViewPager2.OnPageChangeCallback() {
                                    override fun onPageSelected(position: Int) {
                                        super.onPageSelected(position)
                                        sliderHandler.removeCallbacks(sliderRunnable)
                                        sliderHandler.postDelayed(sliderRunnable, 5000)
                                    }
                                })
                                /*binding.recyclerViewFeaturedWorkoutExercise.apply {
                                    layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.HORIZONTAL, false)
                                    adapter =
                                        ExerciseDashboardAdapter(
                                            requireActivity(),
                                            exerciseDashboardDataList,
                                            this@ExerciseFragment
                                        )
                                }*/

                                /*if (exerciseDashboardDataList.size == 1) {
                                    binding.cardViewExerciseData1.visibility = View.VISIBLE
                                    binding.txtExerciseName1.text =
                                        exerciseDashboardDataList[0].exercise_name
                                    displayVideoThumbnail(exerciseDashboardDataList[0], binding.imgExercise1)

                                    binding.cardViewExerciseData2.visibility = View.GONE
                                    binding.cardViewExerciseData3.visibility = View.GONE
                                } else if (exerciseDashboardDataList.size == 2) {
                                    binding.cardViewExerciseData1.visibility = View.VISIBLE
                                    binding.txtExerciseName1.text =
                                        exerciseDashboardDataList[0].exercise_name
                                    displayVideoThumbnail(exerciseDashboardDataList[0], binding.imgExercise1)

                                    binding.cardViewExerciseData2.visibility = View.VISIBLE
                                    binding.txtExerciseName2.text =
                                        exerciseDashboardDataList[1].exercise_name
                                    displayVideoThumbnail(exerciseDashboardDataList[1], binding.imgExercise2)

                                    binding.cardViewExerciseData3.visibility = View.GONE
                                } else if (exerciseDashboardDataList.size >= 3) {
                                    binding.cardViewExerciseData1.visibility = View.VISIBLE
                                    binding.txtExerciseName1.text =
                                        exerciseDashboardDataList[0].exercise_name

                                    displayVideoThumbnail(exerciseDashboardDataList[0], binding.imgExercise1)

                                    binding.cardViewExerciseData2.visibility = View.VISIBLE
                                    binding.txtExerciseName2.text =
                                        exerciseDashboardDataList[1].exercise_name

                                    displayVideoThumbnail(exerciseDashboardDataList[1], binding.imgExercise2)

                                    binding.cardViewExerciseData3.visibility = View.VISIBLE
                                    binding.txtExerciseName3.text =
                                        exerciseDashboardDataList[2].exercise_name
                                    displayVideoThumbnail(exerciseDashboardDataList[2], binding.imgExercise3)
                                    *//*Glide.with(requireActivity())
                                        .load(BaseActivity.baseURL.dropLast(5) + exerciseDashboardDataList[2].image)
                                        .transform(CenterCrop(), RoundedCorners(5))
                                        .into(binding.imgExercise3)*//*
                                }*/

                            } else {
                                binding.viewPagerFeaturedWorkout.visibility = View.GONE
                                binding.txtNoFeaturedWorkout.visibility = View.VISIBLE
                            }

                            /*binding.cardViewExerciseData1.setOnClickListener {
                                displayRespectiveScreen(exerciseDashboardDataList[0])
                            }

                            binding.cardViewExerciseData2.setOnClickListener {
                                displayRespectiveScreen(exerciseDashboardDataList[1])
                            }

                            binding.cardViewExerciseData3.setOnClickListener {
                                displayRespectiveScreen(exerciseDashboardDataList[2])
                            }*/

                        } catch (e: Exception) {
                            hideProgress()
                            displayToast("Something went wrong.. Please try after sometime")
                            binding.shimmerFeaturedWorkoutExercise.visibility = View.GONE
                            binding.txtNoFeaturedWorkout.visibility = View.VISIBLE
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
                        binding.shimmerFeaturedWorkoutExercise.visibility = View.GONE
                        binding.txtNoFeaturedWorkout.visibility = View.VISIBLE
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    private val sliderRunnable: Runnable = Runnable {
        binding.viewPagerFeaturedWorkout.currentItem = binding.viewPagerFeaturedWorkout.currentItem + 1
    }

    override fun onPause() {
        super.onPause()
        sliderHandler.removeCallbacks(sliderRunnable)
    }

    override fun onResume() {
        super.onResume()
        sliderHandler.postDelayed(sliderRunnable, 3000)
    }

    private fun displayVideoThumbnail(data: ExerciseDashboard, imageView: ImageView) {
        val videoImg = if (data.url.isNotEmpty() && data.url.contains("youtube")) {
            val videoId: String = data.url.split("v=")[1]
            "http://img.youtube.com/vi/$videoId/hqdefault.jpg" //high quality thumbnail
        } else {
            BaseActivity.baseURL.dropLast(5) + data.image
        }
        Glide.with(requireActivity()).load(videoImg)
            .transform(CenterCrop(), RoundedCorners(5))
            .into(imageView)
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

    override fun onExerciseDashboardItemClickListener(exerciseDashboard: ExerciseDashboard) {
        displayRespectiveScreen(exerciseDashboard)
    }
}