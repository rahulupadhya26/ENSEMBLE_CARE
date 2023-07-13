package ensemblecare.csardent.com.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ensemblecare.csardent.com.BaseActivity
import ensemblecare.csardent.com.R
import ensemblecare.csardent.com.adapters.CareBuddyDashboardAdapter
import ensemblecare.csardent.com.adapters.CompanionDashboardAdapter
import ensemblecare.csardent.com.controller.OnCareBuddyDashboardItemClickListener
import ensemblecare.csardent.com.data.CareBuddyDashboard
import ensemblecare.csardent.com.databinding.FragmentCareBuddyDashboardBinding
import ensemblecare.csardent.com.databinding.FragmentCompanionDashboardBinding
import ensemblecare.csardent.com.preference.PrefKeys
import ensemblecare.csardent.com.preference.PreferenceHelper.get
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import java.lang.reflect.Type
import java.util.ArrayList
import java.util.Calendar

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CompanionDashboardFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CompanionDashboardFragment : BaseFragment(), OnCareBuddyDashboardItemClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentCompanionDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_companion_dashboard
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCompanionDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE

        binding.txtCompanionUserName.text = preference!![PrefKeys.PREF_FNAME, ""] + " " +
                preference!![PrefKeys.PREF_LNAME, ""]

        //Show Good morning, afternoon, evening or night message to user.
        showMessageToUser()

        onClickEvents()

        displayCompanionDashboard()
    }

    private fun onClickEvents() {
        binding.companionDashboardSwipeToRefresh.setOnRefreshListener {
            displayCompanionDashboard()
        }

        binding.cardViewCompanionUserPic.setOnClickListener {
            replaceFragment(
                CompanionProfileFragment(),
                R.id.layout_home,
                CompanionProfileFragment.TAG
            )
        }
    }

    private fun displayCompanionDashboard() {
        binding.shimmerCompanionDashboard.visibility = View.VISIBLE
        binding.shimmerCompanionDashboard.startShimmer()
        fetchCompanionDetails { response ->
            val careBuddyDashboardDataType: Type =
                object : TypeToken<ArrayList<CareBuddyDashboard?>?>() {}.type
            val careBuddyDashboardList: ArrayList<CareBuddyDashboard> =
                Gson().fromJson(response, careBuddyDashboardDataType)
            if (careBuddyDashboardList.isNotEmpty()) {
                binding.layoutNoCompanionDashboard.visibility = View.GONE
                binding.layoutListCompanionDashboard.visibility = View.VISIBLE
                binding.recyclerViewCompanionDashboardList.visibility = View.VISIBLE
                binding.recyclerViewCompanionDashboardList.apply {
                    layoutManager =
                        LinearLayoutManager(mActivity!!, LinearLayoutManager.VERTICAL, false)
                    adapter = CompanionDashboardAdapter(
                        requireActivity(),
                        careBuddyDashboardList, this@CompanionDashboardFragment
                    )
                }
            } else {
                binding.recyclerViewCompanionDashboardList.visibility = View.GONE
                binding.layoutListCompanionDashboard.visibility = View.GONE
                binding.layoutNoCompanionDashboard.visibility = View.VISIBLE
            }
        }
    }

    private fun fetchCompanionDetails(myCallback: (result: String?) -> Unit) {
        binding.companionDashboardSwipeToRefresh.isRefreshing = false
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .getCareBuddyDashboardData(getAccessToken())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ result ->
                        try {
                            hideProgress()
                            binding.shimmerCompanionDashboard.stopShimmer()
                            binding.shimmerCompanionDashboard.visibility = View.GONE
                            var responseBody = result.string()
                            Log.d("Response Body", responseBody)
                            val respBody = responseBody.split("|")
                            val status = respBody[1]
                            responseBody = respBody[0]
                            myCallback.invoke(responseBody)
                        } catch (e: Exception) {
                            hideProgress()
                            binding.shimmerCompanionDashboard.stopShimmer()
                            binding.shimmerCompanionDashboard.visibility = View.GONE
                            e.printStackTrace()
                            displayToast("Something went wrong.. Please try after sometime")
                        }
                    }, { error ->
                        hideProgress()
                        if ((error as HttpException).code() == 401) {
                            clearCache()
                        } else {
                            binding.shimmerCompanionDashboard.stopShimmer()
                            binding.shimmerCompanionDashboard.visibility = View.GONE
                            displayAfterLoginErrorMsg(error)
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    private fun showMessageToUser() {
        val c = Calendar.getInstance()
        when (c[Calendar.HOUR_OF_DAY]) {
            in 0..11 -> {
                binding.txtShowMessageToCompanion.text = "Good Morning,"
            }

            in 12..15 -> {
                binding.txtShowMessageToCompanion.text = "Good Afternoon,"
            }

            in 16..19 -> {
                binding.txtShowMessageToCompanion.text = "Good Evening,"
            }

            in 20..23 -> {
                binding.txtShowMessageToCompanion.text = "Good Night,"
            }
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            val photo = preference!![PrefKeys.PREF_PHOTO, ""]!!
            if (photo != "null" && photo.isNotEmpty()) {
                binding.imgCompanionUserPic.visibility = View.VISIBLE
                binding.txtCompanionUserPic.visibility = View.GONE
                Glide.with(requireActivity())
                    .load(BaseActivity.baseURL.dropLast(5) + photo)
                    .placeholder(R.drawable.user_pic)
                    .transform(CenterCrop(), RoundedCorners(5))
                    .into(binding.imgCompanionUserPic)
            } else {
                binding.imgCompanionUserPic.visibility = View.GONE
                binding.txtCompanionUserPic.visibility = View.VISIBLE
                val userTxt = preference!![PrefKeys.PREF_FNAME, ""]!!
                if (userTxt.isNotEmpty()) {
                    binding.txtCompanionUserPic.text = userTxt.substring(0, 1).uppercase()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CompanionDashboardFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CompanionDashboardFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_Companion_Dashboard"
    }

    override fun onCareBuddyDashboardItemClickListener(
        careBuddyDashboard: CareBuddyDashboard,
        isCall: Boolean,
        isChat: Boolean
    ) {
        if (isCall) {
            dialNumber(careBuddyDashboard.phone)
        } else {

        }
    }
}