package com.app.selfcare.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.selfcare.R
import com.app.selfcare.adapters.AppointmentsAdapter
import com.app.selfcare.adapters.EventCommunityAdapter
import com.app.selfcare.controller.OnEventItemClickListener
import com.app.selfcare.data.EventCommunity
import com.app.selfcare.data.GetAppointmentList
import com.app.selfcare.databinding.FragmentActivityCarePlanBinding
import com.app.selfcare.databinding.FragmentEventsCommunityBinding
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
 * Use the [EventsCommunityFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EventsCommunityFragment : BaseFragment(), OnEventItemClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentEventsCommunityBinding

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
        binding = FragmentEventsCommunityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getLayout(): Int {
        return R.layout.fragment_events_community
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE

        displayEvents()
    }

    private fun displayEvents(){
        getEventsList { response ->
            val eventCommunityType: Type = object : TypeToken<ArrayList<EventCommunity?>?>() {}.type
            val eventCommunityList: ArrayList<EventCommunity> =
                Gson().fromJson(response, eventCommunityType)
            if (eventCommunityList.isNotEmpty()) {
                binding.eventCommunity.visibility = View.VISIBLE
                binding.txtNoEventCommunity.visibility = View.GONE
                binding.eventCommunity.apply {
                    layoutManager =
                        LinearLayoutManager(mActivity!!, LinearLayoutManager.VERTICAL, false)
                    adapter = EventCommunityAdapter(
                        requireActivity(),
                        eventCommunityList, this@EventsCommunityFragment
                    )
                }
            } else {
                binding.eventCommunity.visibility = View.GONE
                binding.txtNoEventCommunity.visibility = View.VISIBLE
            }
        }
    }

    private fun getEventsList(myCallback: (result: String?) -> Unit) {
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .getEventData(
                        "PI0070",
                        getAccessToken()
                    )
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ result ->
                        try {
                            hideProgress()
                            binding.shimmerEventCommunity.stopShimmer()
                            binding.shimmerEventCommunity.visibility = View.GONE
                            var responseBody = result.string()
                            Log.d("Response Body", responseBody)
                            val respBody = responseBody.split("|")
                            val status = respBody[1]
                            responseBody = respBody[0]
                            myCallback.invoke(responseBody)
                        } catch (e: Exception) {
                            hideProgress()
                            binding.shimmerEventCommunity.stopShimmer()
                            binding.shimmerEventCommunity.visibility = View.GONE
                            e.printStackTrace()
                            displayToast("Something went wrong.. Please try after sometime")
                        }
                    }, { error ->
                        hideProgress()
                        if ((error as HttpException).code() == 401) {
                            clearCache()
                        } else {
                            binding.shimmerEventCommunity.stopShimmer()
                            binding.shimmerEventCommunity.visibility = View.GONE
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
         * @return A new instance of fragment EventsCommunityFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            EventsCommunityFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
        const val TAG = "Screen_Events_Community"
    }

    override fun onEventItemClickListener(events: EventCommunity) {
        replaceFragment(
            EventDetailCommunityFragment.newInstance(events),
            R.id.layout_home,
            EventDetailCommunityFragment.TAG
        )
    }
}