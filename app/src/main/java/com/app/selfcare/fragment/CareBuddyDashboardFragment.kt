package com.app.selfcare.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.app.selfcare.BaseActivity
import com.app.selfcare.R
import com.app.selfcare.adapters.CareBuddyAdapter
import com.app.selfcare.adapters.CareBuddyDashboardAdapter
import com.app.selfcare.controller.OnCareBuddyDashboardItemClickListener
import com.app.selfcare.controller.OnCareBuddyItemClickListener
import com.app.selfcare.data.CareBuddy
import com.app.selfcare.data.CareBuddyDashboard
import com.app.selfcare.data.FetchCareBuddyList
import com.app.selfcare.databinding.FragmentCareBuddyDashboardBinding
import com.app.selfcare.databinding.FragmentLoginBinding
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper.get
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import java.lang.reflect.Type
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CareBuddyDashboardFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CareBuddyDashboardFragment : BaseFragment(), OnCareBuddyDashboardItemClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentCareBuddyDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_care_buddy_dashboard
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCareBuddyDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE

        binding.txtCareBuddyUserName.text = preference!![PrefKeys.PREF_FNAME, ""] + " " +
                preference!![PrefKeys.PREF_LNAME, ""]

        //Show Good morning, afternoon, evening or night message to user.
        showMessageToUser()

        onClickEvents()

        displayCareBuddyDashboard()
    }

    private fun onClickEvents(){
        binding.careBuddyDashboardSwipeToRefresh.setOnRefreshListener {
            displayCareBuddyDashboard()
        }

        binding.cardViewCareBuddyUserPic.setOnClickListener {
            replaceFragment(
                CareBuddyProfileFragment(),
                R.id.layout_home,
                CareBuddyProfileFragment.TAG
            )
        }
    }

    private fun displayCareBuddyDashboard() {
        binding.shimmerCareBuddyDashboard.visibility = View.VISIBLE
        binding.shimmerCareBuddyDashboard.startShimmer()
        fetchAddCareBuddy { response ->
            val careBuddyDashboardDataType: Type = object : TypeToken<ArrayList<CareBuddyDashboard?>?>() {}.type
            val careBuddyDashboardList: ArrayList<CareBuddyDashboard> =
                Gson().fromJson(response, careBuddyDashboardDataType)
            if (careBuddyDashboardList.isNotEmpty()) {
                binding.layoutNoCareBuddyDashboard.visibility = View.GONE
                binding.layoutListCareBuddyDashboard.visibility = View.VISIBLE
                binding.recyclerViewCareBuddyDashboardList.visibility = View.VISIBLE
                binding.recyclerViewCareBuddyDashboardList.apply {
                    layoutManager = GridLayoutManager(mActivity!!, 2)
                    adapter = CareBuddyDashboardAdapter(
                        requireActivity(),
                        careBuddyDashboardList, this@CareBuddyDashboardFragment
                    )
                }
            } else {
                binding.recyclerViewCareBuddyDashboardList.visibility = View.GONE
                binding.layoutListCareBuddyDashboard.visibility = View.GONE
                binding.layoutNoCareBuddyDashboard.visibility = View.VISIBLE
            }
        }
    }

    private fun fetchAddCareBuddy(myCallback: (result: String?) -> Unit) {
        binding.careBuddyDashboardSwipeToRefresh.isRefreshing = false
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .getCareBuddyDashboardData(getAccessToken())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ result ->
                        try {
                            hideProgress()
                            binding.shimmerCareBuddyDashboard.stopShimmer()
                            binding.shimmerCareBuddyDashboard.visibility = View.GONE
                            var responseBody = result.string()
                            Log.d("Response Body", responseBody)
                            val respBody = responseBody.split("|")
                            val status = respBody[1]
                            responseBody = respBody[0]
                            myCallback.invoke(responseBody)
                        } catch (e: Exception) {
                            hideProgress()
                            binding.shimmerCareBuddyDashboard.stopShimmer()
                            binding.shimmerCareBuddyDashboard.visibility = View.GONE
                            e.printStackTrace()
                            displayToast("Something went wrong.. Please try after sometime")
                        }
                    }, { error ->
                        hideProgress()
                        if ((error as HttpException).code() == 401) {
                            clearCache()
                        } else {
                            binding.shimmerCareBuddyDashboard.stopShimmer()
                            binding.shimmerCareBuddyDashboard.visibility = View.GONE
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
                binding.txtShowMessageToUser.text = "Good Morning,"
            }
            in 12..15 -> {
                binding.txtShowMessageToUser.text = "Good Afternoon,"
            }
            in 16..19 -> {
                binding.txtShowMessageToUser.text = "Good Evening,"
            }
            in 20..23 -> {
                binding.txtShowMessageToUser.text = "Good Night,"
            }
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            val photo = preference!![PrefKeys.PREF_PHOTO, ""]!!
            if (photo != "null" && photo.isNotEmpty()) {
                binding.imgCareBuddyUserPic.visibility = View.VISIBLE
                binding.txtCareBuddyUserPic.visibility = View.GONE
                Glide.with(requireActivity())
                    .load(BaseActivity.baseURL.dropLast(5) + photo)
                    .placeholder(R.drawable.user_pic)
                    .transform(CenterCrop(), RoundedCorners(5))
                    .into(binding.imgCareBuddyUserPic)
            } else {
                //img_user_pic.setImageResource(R.drawable.user_pic)
                binding.imgCareBuddyUserPic.visibility = View.GONE
                binding.txtCareBuddyUserPic.visibility = View.VISIBLE
                val userTxt = preference!![PrefKeys.PREF_FNAME, ""]!!
                if (userTxt.isNotEmpty()) {
                    binding.txtCareBuddyUserPic.text = userTxt.substring(0, 1).uppercase()
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
         * @return A new instance of fragment CareBuddyDashboardFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CareBuddyDashboardFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
        const val TAG = "Screen_Care_Buddy_Dashboard"
    }

    override fun onCareBuddyDashboardItemClickListener(careBuddyDashboard: CareBuddyDashboard) {

    }
}