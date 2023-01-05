package com.app.selfcare.fragment

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.RadioButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.app.selfcare.BaseActivity
import com.app.selfcare.R
import com.app.selfcare.adapters.CoachTypesAdapter
import com.app.selfcare.controller.OnCoachTypeClickListener
import com.app.selfcare.data.CoachType
import com.app.selfcare.data.SendOtp
import com.app.selfcare.data.Video
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper.get
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.dialog_inspiration.*
import kotlinx.android.synthetic.main.fragment_coaches.*
import kotlinx.android.synthetic.main.fragment_resources.*
import kotlinx.android.synthetic.main.fragment_sign_up.*
import org.json.JSONObject
import retrofit2.HttpException
import java.lang.reflect.Type

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CoachesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CoachesFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var quoteData: JSONObject = JSONObject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_coaches
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE
        updateStatusBarColor(R.color.white)

        displayWellnessData()

        cardViewExercise.setOnClickListener {
            replaceFragment(
                ExerciseFragment(),
                R.id.layout_home,
                ExerciseFragment.TAG
            )
        }

        cardViewNutrition.setOnClickListener {
            replaceFragment(
                NutritionFragment(),
                R.id.layout_home,
                NutritionFragment.TAG
            )
        }

        cardViewMindfulness.setOnClickListener {
            replaceFragment(
                MindfullnessFragment(),
                R.id.layout_home,
                MindfullnessFragment.TAG
            )
        }

        cardViewYoga.setOnClickListener {
            replaceFragment(
                YogaCoachFragment(),
                R.id.layout_home,
                YogaCoachFragment.TAG
            )
        }

        cardViewMusic.setOnClickListener {
            replaceFragment(
                MusicCoachFragment(),
                R.id.layout_home,
                MusicCoachFragment.TAG
            )
        }

        cardViewInspiration.setOnClickListener {
            val dialog = Dialog(requireActivity())
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setContentView(R.layout.dialog_inspiration)
            Glide.with(requireActivity())
                .load(BaseActivity.baseURL.dropLast(5) + quoteData.getString("img"))
                .into(dialog.imgQuote)
            dialog.txtShareInspiration.setOnClickListener {
                shareDetails("", "Quote", "", quoteData.getString("img"), "Quote")
            }
            dialog.show()
        }

        /*val coachTypeList: ArrayList<CoachType> = ArrayList()
        coachTypeList.add(CoachType("Exercise","Last Workout","59 mins", R.drawable.exercise_img))
        coachTypeList.add(CoachType("Nutrition", "New Recipe","Fruit Salad Bowl", R.drawable.nutrition_img))
        coachTypeList.add(CoachType("Mindfulness", "Recommended","45 mins", R.drawable.mindfulness_img))
        coachTypeList.add(CoachType("Yoga", "Recommended","49 mins", R.drawable.yoga_img))
        coachTypeList.add(CoachType("Music", "Session 2","15 mins", R.drawable.music_img))
        coachTypeList.add(CoachType("Inspiration", "Last Workout","59 mins", R.drawable.inspiration_img))

        val staggeredGridLayoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        staggeredGridLayoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_NONE
        recyclerViewCoachTypes.layoutManager = staggeredGridLayoutManager
        //recyclerViewCoachTypes.setHasFixedSize(true)
        recyclerViewCoachTypes.adapter =
            CoachTypesAdapter(mActivity!!, coachTypeList, this)*/

    }

    private fun displayWellnessData() {
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .getWellnessDashboardData(getAccessToken())
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
                            val exerciseData = jsonObj.getJSONObject("exercise")
                            val nutritionData = jsonObj.getJSONObject("nutrition")
                            val yogaData = jsonObj.getJSONObject("yoga")
                            val musicData = jsonObj.getJSONObject("music")
                            val mindfulnessData = jsonObj.getJSONObject("mindfulness")
                            quoteData = jsonObj.getJSONObject("quote")

                            txtExerciseSubTitle.text = exerciseData.getString("name")
                            txtExerciseRemainTime.text = exerciseData.getString("time")
                            txtNutritionSubTitle.text = nutritionData.getString("name")
                            txtNutritionFood.text = nutritionData.getString("time")
                            txtYogaSubTitle.text = yogaData.getString("name")
                            txtYogaRemainTime.text = yogaData.getString("time")
                            txtMindfulnessSubTitle.text = mindfulnessData.getString("name")
                            txtMindfulnessRemainTime.text = mindfulnessData.getString("time")
                            txtMusicSubTitle.text = musicData.getString("name")
                            txtMusicRemainTime.text = musicData.getString("time")

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
                                displayWellnessData()
                            }
                        } else {
                            displayAfterLoginErrorMsg(error)
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CoachesFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CoachesFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_Coaches"
    }
}