package ensemblecare.csardent.com.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ensemblecare.csardent.com.R
import ensemblecare.csardent.com.adapters.DashboardPodcastAdapter
import ensemblecare.csardent.com.controller.OnPodcastItemClickListener
import ensemblecare.csardent.com.data.Podcast
import ensemblecare.csardent.com.databinding.FragmentMusicCoachBinding
import ensemblecare.csardent.com.preference.PrefKeys
import ensemblecare.csardent.com.preference.PreferenceHelper.get
import ensemblecare.csardent.com.utils.Utils
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
 * Use the [MusicCoachFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MusicCoachFragment : BaseFragment(), OnPodcastItemClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentMusicCoachBinding

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
        binding = FragmentMusicCoachBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getLayout(): Int {
        return R.layout.fragment_music_coach
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE
        updateStatusBarColor(R.color.music_status_bar)

        displayMusicDashboardData()

        onClickEvents()
    }

    private fun onClickEvents() {
        binding.musicBack.setOnClickListener {
            setBottomNavigation(null)
            setLayoutBottomNavigation(null)
            replaceFragmentNoBackStack(
                BottomNavigationFragment(),
                R.id.layout_home,
                BottomNavigationFragment.TAG
            )
        }

        binding.musicFav.setOnClickListener {
            replaceFragment(
                FavoriteFragment.newInstance(Utils.WELLNESS_MUSIC),
                R.id.layout_home,
                FavoriteFragment.TAG
            )
        }

        binding.layoutRelax.setOnClickListener {
            replaceFragment(
                DetailFragment.newInstance("Relaxation", Utils.WELLNESS_MUSIC),
                R.id.layout_home,
                DetailFragment.TAG
            )
        }

        binding.layoutReminiscence.setOnClickListener {
            replaceFragment(
                DetailFragment.newInstance("Reminiscence", Utils.WELLNESS_MUSIC),
                R.id.layout_home,
                DetailFragment.TAG
            )
        }

        binding.layoutSelfExpression.setOnClickListener {
            replaceFragment(
                DetailFragment.newInstance("Self expression", Utils.WELLNESS_MUSIC),
                R.id.layout_home,
                DetailFragment.TAG
            )
        }

        binding.layoutEnhancedFocus.setOnClickListener {
            replaceFragment(
                DetailFragment.newInstance("Enhanced focus", Utils.WELLNESS_MUSIC),
                R.id.layout_home,
                DetailFragment.TAG
            )
        }

        binding.layoutWellBeing.setOnClickListener {
            replaceFragment(
                DetailFragment.newInstance("Well being", Utils.WELLNESS_MUSIC),
                R.id.layout_home,
                DetailFragment.TAG
            )
        }

        binding.layoutNeuroMusic.setOnClickListener {
            replaceFragment(
                DetailFragment.newInstance("Neuro music", Utils.WELLNESS_MUSIC),
                R.id.layout_home,
                DetailFragment.TAG
            )
        }
    }

    private fun displayMusicDashboardData() {
        fetchMusicDashboardData() { response ->
            val jsonObj = JSONObject(response)
            val musicDashboardDataType: Type =
                object : TypeToken<ArrayList<Podcast?>?>() {}.type
            val musicDashboardRecommendedDataList: ArrayList<Podcast> =
                Gson().fromJson(jsonObj.getString("podcasts"), musicDashboardDataType)

            val musicDashboardRelaxationDataList: ArrayList<Podcast> =
                Gson().fromJson(jsonObj.getString("relaxation"), musicDashboardDataType)

            val musicDashboardWellBeingDataList: ArrayList<Podcast> =
                Gson().fromJson(jsonObj.getString("Well being"), musicDashboardDataType)

            if (musicDashboardRecommendedDataList.isEmpty() &&
                musicDashboardRelaxationDataList.isEmpty() &&
                musicDashboardWellBeingDataList.isEmpty()
            ) {
                binding.layoutDashboardMusic.visibility = View.GONE
            } else {
                if (musicDashboardRecommendedDataList.isNotEmpty()) {
                    binding.layoutMusicRecommended.visibility = View.VISIBLE
                    binding.recyclerViewRecommendedMusic.apply {
                        layoutManager =
                            LinearLayoutManager(mActivity!!, RecyclerView.HORIZONTAL, false)
                        adapter =
                            DashboardPodcastAdapter(
                                mActivity!!,
                                musicDashboardRecommendedDataList,
                                this@MusicCoachFragment,
                                Utils.WELLNESS_MUSIC
                            )
                    }
                } else {
                    binding.layoutMusicRecommended.visibility = View.GONE
                }

                if (musicDashboardRelaxationDataList.isNotEmpty()) {
                    binding.layoutMusicRelaxation.visibility = View.VISIBLE
                    binding.recyclerViewRelaxation.apply {
                        layoutManager =
                            LinearLayoutManager(mActivity!!, RecyclerView.HORIZONTAL, false)
                        adapter =
                            DashboardPodcastAdapter(
                                mActivity!!,
                                musicDashboardRelaxationDataList,
                                this@MusicCoachFragment,
                                Utils.WELLNESS_MUSIC
                            )
                    }
                } else {
                    binding.layoutMusicRelaxation.visibility = View.GONE
                }

                if (musicDashboardWellBeingDataList.isNotEmpty()) {
                    binding.layoutMusicWellBeing.visibility = View.VISIBLE
                    binding.recyclerViewWellBeing.apply {
                        layoutManager =
                            LinearLayoutManager(mActivity!!, RecyclerView.HORIZONTAL, false)
                        adapter =
                            DashboardPodcastAdapter(
                                mActivity!!,
                                musicDashboardWellBeingDataList,
                                this@MusicCoachFragment,
                                Utils.WELLNESS_MUSIC
                            )
                    }
                } else {
                    binding.layoutMusicWellBeing.visibility = View.GONE
                }
            }
        }
    }

    private fun fetchMusicDashboardData(myCallback: (result: String?) -> Unit) {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .getMusicDashboardData(getAccessToken())
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
                            myCallback.invoke(responseBody)
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
                                clearCache()
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
         * @return A new instance of fragment MusicCoachFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MusicCoachFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_Music"
    }

    override fun onPodcastItemClicked(podcast: Podcast) {
        replaceFragment(
            PodcastDetailFragment.newInstance(podcast, Utils.WELLNESS_MUSIC),
            R.id.layout_home,
            PodcastDetailFragment.TAG
        )
    }
}