package com.app.selfcare.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.selfcare.R
import com.app.selfcare.adapters.CareBuddyAdapter
import com.app.selfcare.adapters.CareBuddyCommunityDashboardAdapter
import com.app.selfcare.controller.OnCareBuddyDashboardItemClickListener
import com.app.selfcare.controller.OnCareBuddyItemClickListener
import com.app.selfcare.data.CareBuddy
import com.app.selfcare.data.CareBuddyDashboard
import com.app.selfcare.data.FetchCareBuddyList
import com.app.selfcare.data.NotificationType
import com.app.selfcare.databinding.FragmentCareBuddyCommunityBinding
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper.get
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import java.lang.reflect.Type

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CareBuddyCommunityFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CareBuddyCommunityFragment : BaseFragment(), OnCareBuddyDashboardItemClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentCareBuddyCommunityBinding
    private var careBuddyList: ArrayList<CareBuddyDashboard> = arrayListOf()
    private var adapter: CareBuddyCommunityDashboardAdapter? = null

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
        binding = FragmentCareBuddyCommunityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getLayout(): Int {
        return R.layout.fragment_care_buddy_community
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE

        displayCareBuddy()

        binding.etCareBuddySearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            }

            override fun afterTextChanged(editable: Editable) {
                if (editable.toString().isNotEmpty())
                    filterOne(editable.toString())
                else
                    binding.recyclerViewCareBuddyList.apply {
                        layoutManager =
                            LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
                        adapter = CareBuddyCommunityDashboardAdapter(
                            mActivity!!,
                            careBuddyList, this@CareBuddyCommunityFragment
                        )
                    }
            }
        })
    }

    private fun displayCareBuddy() {
        fetchAddCareBuddy { response ->
            val careBuddyType: Type = object : TypeToken<ArrayList<CareBuddyDashboard?>?>() {}.type
            careBuddyList = Gson().fromJson(response, careBuddyType)
            if (careBuddyList.isNotEmpty()) {
                binding.layoutListCareBuddy.visibility = View.VISIBLE
                binding.recyclerViewCareBuddyList.visibility = View.VISIBLE
                binding.layoutAddCareBuddy.visibility = View.GONE
                binding.recyclerViewCareBuddyList.apply {
                    layoutManager =
                        LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
                    adapter = CareBuddyCommunityDashboardAdapter(
                        requireActivity(),
                        careBuddyList, this@CareBuddyCommunityFragment
                    )
                }
            } else {
                binding.recyclerViewCareBuddyList.visibility = View.GONE
                binding.layoutListCareBuddy.visibility = View.GONE
                binding.layoutAddCareBuddy.visibility = View.VISIBLE
            }
        }
    }

    private fun fetchAddCareBuddy(myCallback: (result: String?) -> Unit) {
        binding.layoutListCareBuddy.visibility = View.VISIBLE
        binding.shimmerCareBuddy.startShimmer()
        binding.shimmerCareBuddy.visibility = View.VISIBLE
        binding.recyclerViewCareBuddyList.visibility = View.GONE
        binding.layoutAddCareBuddy.visibility = View.GONE
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .getCareBuddyDashboardData(getAccessToken())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ result ->
                        try {
                            hideProgress()
                            binding.shimmerCareBuddy.stopShimmer()
                            binding.shimmerCareBuddy.visibility = View.GONE
                            var responseBody = result.string()
                            Log.d("Response Body", responseBody)
                            val respBody = responseBody.split("|")
                            val status = respBody[1]
                            responseBody = respBody[0]
                            myCallback.invoke(responseBody)
                        } catch (e: Exception) {
                            hideProgress()
                            binding.shimmerCareBuddy.stopShimmer()
                            binding.shimmerCareBuddy.visibility = View.GONE
                            e.printStackTrace()
                            displayToast("Something went wrong.. Please try after sometime")
                        }
                    }, { error ->
                        hideProgress()
                        if ((error as HttpException).code() == 401) {
                            clearCache()
                        } else {
                            binding.shimmerCareBuddy.stopShimmer()
                            binding.shimmerCareBuddy.visibility = View.GONE
                            displayAfterLoginErrorMsg(error)
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    private fun filterOne(text: String) {
        val filteredNames = ArrayList<CareBuddyDashboard>()
        careBuddyList.filterTo(filteredNames) {
            it.name.toLowerCase().contains(
                text.toLowerCase()
            )
        }

        if (text.isEmpty()) {
            binding.recyclerViewCareBuddyList.apply {
                layoutManager =
                    LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
                adapter = CareBuddyCommunityDashboardAdapter(
                    mActivity!!,
                    careBuddyList, this@CareBuddyCommunityFragment
                )
            }
        } else {
            val layoutManager =
                LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
            adapter = CareBuddyCommunityDashboardAdapter(
                mActivity!!,
                careBuddyList, this
            )
            adapter!!.filterList(filteredNames)
            binding.recyclerViewCareBuddyList.layoutManager = layoutManager
            binding.recyclerViewCareBuddyList.adapter = adapter
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CareBuddyCommunityFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CareBuddyCommunityFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_CareBuddy_Community"
    }

    override fun onCareBuddyDashboardItemClickListener(
        careBuddyDashboard: CareBuddyDashboard,
        isCall: Boolean,
        isChat: Boolean
    ) {
        if (isCall) {
            val sIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${careBuddyDashboard.phone}"))
            startActivity(sIntent)
        } else if (isChat) {

        } else {
            replaceFragment(
                CareBuddyCarePlanFragment.newInstance(careBuddyDashboard),
                R.id.layout_home,
                CareBuddyCarePlanFragment.TAG
            )
        }
    }
}