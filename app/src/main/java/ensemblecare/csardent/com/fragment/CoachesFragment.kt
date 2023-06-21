package ensemblecare.csardent.com.fragment

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.Fragment
import ensemblecare.csardent.com.BaseActivity
import ensemblecare.csardent.com.R
import ensemblecare.csardent.com.databinding.DialogInspirationBinding
import ensemblecare.csardent.com.databinding.FragmentCoachesBinding
import ensemblecare.csardent.com.preference.PrefKeys
import ensemblecare.csardent.com.preference.PreferenceHelper.get
import com.bumptech.glide.Glide
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject
import retrofit2.HttpException

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
    private var mLastClickTime: Long = 0
    private lateinit var binding: FragmentCoachesBinding

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
        binding = FragmentCoachesBinding.inflate(inflater, container, false)
        return binding.root
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

        binding.cardViewExercise.setOnClickListener {
            clearPreviousFragmentStack()
            replaceFragmentNoBackStack(
                ExerciseFragment(),
                R.id.layout_home,
                ExerciseFragment.TAG
            )
        }

        binding.cardViewNutrition.setOnClickListener {
            clearPreviousFragmentStack()
            replaceFragmentNoBackStack(
                NutritionFragment(),
                R.id.layout_home,
                NutritionFragment.TAG
            )
        }

        binding.cardViewMindfulness.setOnClickListener {
            clearPreviousFragmentStack()
            replaceFragmentNoBackStack(
                MindfullnessFragment(),
                R.id.layout_home,
                MindfullnessFragment.TAG
            )
        }

        binding.cardViewYoga.setOnClickListener {
            clearPreviousFragmentStack()
            replaceFragmentNoBackStack(
                YogaCoachFragment(),
                R.id.layout_home,
                YogaCoachFragment.TAG
            )
        }

        binding.cardViewMusic.setOnClickListener {
            clearPreviousFragmentStack()
            replaceFragmentNoBackStack(
                MusicCoachFragment(),
                R.id.layout_home,
                MusicCoachFragment.TAG
            )
        }

        binding.cardViewInspiration.setSafeOnClickListener {
            try {
                val dialog = Dialog(requireActivity())
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                val dialogInspiration = DialogInspirationBinding.inflate(layoutInflater)
                val view = dialogInspiration.root
                dialog.setContentView(view)

                if (quoteData.has("name") && quoteData.getString("name").isNotEmpty()) {
                    dialogInspiration.txtInspiration.visibility = View.VISIBLE
                    dialogInspiration.imgQuote.visibility = View.GONE
                    dialogInspiration.txtInspiration.text = quoteData.getString("name")
                    dialogInspiration.txtShareInspiration.setOnClickListener {
                        shareDetails("", quoteData.getString("name"), "", "", "Journal")
                    }
                } else {
                    dialogInspiration.txtInspiration.visibility = View.GONE
                    dialogInspiration.imgQuote.visibility = View.VISIBLE
                    Glide.with(requireActivity())
                        .load(BaseActivity.baseURL.dropLast(5) + quoteData.getString("img"))
                        .into(dialogInspiration.imgQuote)
                    dialogInspiration.txtShareInspiration.setOnClickListener {
                        shareDetails("", "Quote", "", quoteData.getString("img"), "Quote")
                    }
                }

                /*Glide.with(requireActivity())
                    .load(BaseActivity.baseURL.dropLast(5) + quoteData.getString("img"))
                    .into(dialogInspiration.imgQuote)*/
                /*dialogInspiration.txtShareInspiration.setOnClickListener {
                    shareDetails("", "Quote", "", quoteData.getString("img"), "Quote")
                }*/
                dialog.show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        /*cardViewInspiration.setOnClickListener {

        }*/

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

                            binding.txtExerciseSubTitle.text = exerciseData.getString("name")
                            binding.txtExerciseRemainTime.text = exerciseData.getString("time")
                            binding.txtNutritionSubTitle.text = nutritionData.getString("name")
                            binding.txtNutritionFood.text = nutritionData.getString("time")
                            binding.txtYogaSubTitle.text = yogaData.getString("name")
                            binding.txtYogaRemainTime.text = yogaData.getString("time")
                            binding.txtMindfulnessSubTitle.text = mindfulnessData.getString("name")
                            binding.txtMindfulnessRemainTime.text = mindfulnessData.getString("time")
                            binding.txtMusicSubTitle.text = musicData.getString("name")
                            binding.txtMusicRemainTime.text = musicData.getString("time")

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

    class SafeClickListener(
        private var defaultInterval: Int = 500,
        private val onSafeCLick: (View) -> Unit
    ) : View.OnClickListener {
        private var lastTimeClicked: Long = 0
        override fun onClick(v: View) {
            if (SystemClock.elapsedRealtime() - lastTimeClicked < defaultInterval) {
                return
            }
            lastTimeClicked = SystemClock.elapsedRealtime()
            onSafeCLick(v)
        }
    }

    fun View.setSafeOnClickListener(onSafeClick: (View) -> Unit) {
        val safeClickListener = SafeClickListener {
            onSafeClick(it)
        }
        setOnClickListener(safeClickListener)
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