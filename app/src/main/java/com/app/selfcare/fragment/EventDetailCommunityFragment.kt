package com.app.selfcare.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.app.selfcare.BaseActivity
import com.app.selfcare.R
import com.app.selfcare.data.EventCommunity
import com.app.selfcare.data.FetchCareBuddyDetail
import com.app.selfcare.databinding.FragmentActivityCarePlanBinding
import com.app.selfcare.databinding.FragmentEventDetailCommunityBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
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
 * Use the [EventDetailCommunityFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EventDetailCommunityFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var event: EventCommunity? = null
    private var param2: String? = null
    private lateinit var binding: FragmentEventDetailCommunityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            event = it.getParcelable(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEventDetailCommunityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getLayout(): Int {
        return R.layout.fragment_event_detail_community
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE
        updateStatusBarColor(R.color.white)

        binding.imgEventDetailBack.setOnClickListener {
            popBackStack()
        }

        getEventDetail { response ->
            val eventCommunityType: Type = object : TypeToken<ArrayList<EventCommunity?>?>() {}.type
            val eventCommunityList: ArrayList<EventCommunity> =
                Gson().fromJson(response, eventCommunityType)
            if (eventCommunityList.isNotEmpty()) {
                val event = eventCommunityList[0]
                Glide.with(requireActivity()).load(BaseActivity.baseURL.dropLast(5) + event.image)
                    .placeholder(R.drawable.events_img)
                    .transform(CenterCrop())
                    .into(binding.imgEventDetail)
                binding.txtEventDetailTitle.text = event.title
                binding.txtEventDetailTime.text =
                    event.start_time.dropLast(3) + " - " + event.end_time.dropLast(3)
                binding.txtEventDetailLocation.text = event.address
                binding.txtEventDetailAbout.text = event.description
            }
        }
    }

    private fun getEventDetail(myCallback: (result: String?) -> Unit) {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .getEventDetail(
                        "PI0070",
                        FetchCareBuddyDetail(event!!.id),
                        getAccessToken()
                    )
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
                            e.printStackTrace()
                            displayToast("Something went wrong.. Please try after sometime")
                        }
                    }, { error ->
                        hideProgress()
                        if ((error as HttpException).code() == 401) {
                            clearCache()
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
         * @return A new instance of fragment EventDetailCommunityFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: EventCommunity, param2: String = "") =
            EventDetailCommunityFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_Events_Community_Detail"
    }
}