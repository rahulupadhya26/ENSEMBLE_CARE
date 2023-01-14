package com.app.selfcare.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.app.selfcare.R
import com.app.selfcare.adapters.ClientAvailabilityAdapter
import com.app.selfcare.controller.OnClickListener
import com.app.selfcare.data.AvailabilityData
import com.app.selfcare.data.ClientAvailability
import com.app.selfcare.data.Therapist
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper.get
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_client_availability.*
import retrofit2.HttpException
import java.lang.Exception
import java.lang.reflect.Type
import kotlin.collections.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ClientAvailabilityFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ClientAvailabilityFragment : BaseFragment(), OnClickListener {
    // TODO: Rename and change types of parameters
    private var isRegister: Boolean? = null
    private var param2: String? = null
    private var clientAvailability: ArrayList<String> = ArrayList()
    private var preferTime: ArrayList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            isRegister = it.getBoolean(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_client_availability
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE
        updateStatusBarColor(R.color.white)

        clientAvailability = arrayListOf()
        preferTime = arrayListOf()

        if (isRegister!!) {
            clientAvailabilityBack.visibility = View.GONE
        } else {
            clientAvailabilityBack.visibility = View.VISIBLE
        }

        clientAvailabilityBack.setOnClickListener {
            setBottomNavigation(null)
            setLayoutBottomNavigation(null)
            replaceFragmentNoBackStack(
                BottomNavigationFragment(),
                R.id.layout_home,
                BottomNavigationFragment.TAG
            )
        }

        val availData = resources.getStringArray(R.array.availability)
        val availability: ArrayList<AvailabilityData> = arrayListOf()
        for (data in availData) {
            availability.add(AvailabilityData(data))
        }

        val preferData = resources.getStringArray(R.array.preferred_time)
        val preferredTime: ArrayList<AvailabilityData> = arrayListOf()
        for (data in preferData) {
            preferredTime.add(AvailabilityData(data))
        }

        val noOfColumns = calculateNoOfColumns(requireActivity(), 150F)

        val layoutManager1 = GridLayoutManager(requireActivity(), noOfColumns)
        recyclerviewAvailability.setHasFixedSize(false)
        recyclerviewAvailability.layoutManager = layoutManager1
        recyclerviewAvailability.adapter =
            ClientAvailabilityAdapter(mActivity!!, availability, this)

        val layoutManager2 = GridLayoutManager(requireActivity(), noOfColumns)
        recyclerviewTimeSlots.setHasFixedSize(false)
        recyclerviewTimeSlots.layoutManager = layoutManager2
        recyclerviewTimeSlots.adapter = ClientAvailabilityAdapter(mActivity!!, preferredTime, this)

        btnAvailability.setOnClickListener {
            if (clientAvailability.isNotEmpty()) {
                if (preferTime.isNotEmpty()) {
                    getClientAvailability()
                } else {
                    displayMsg("Alert", "Select the preferred time")
                }
            } else {
                displayMsg("Alert", "Select the availability")
            }
        }
    }

    private fun getClientAvailability() {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .clientAvailability(
                        ClientAvailability(
                            preference!![PrefKeys.PREF_PATIENT_ID, 0]!!,
                            clientAvailability,
                            preferTime
                        ), getAccessToken()
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
                            val therapistList: Type =
                                object : TypeToken<java.util.ArrayList<Therapist?>?>() {}.type
                            val specialist: ArrayList<Therapist> =
                                Gson().fromJson(responseBody, therapistList)
                            replaceFragment(
                                TherapistListFragment.newInstance(false, specialist),
                                R.id.layout_home,
                                TherapistListFragment.TAG
                            )
                        } catch (e: Exception) {
                            hideProgress()
                            displayToast("Something went wrong.. Please try after sometime")
                        }
                    }, { error ->
                        hideProgress()
                        //displayToast("Error ${error.localizedMessage}")
                        if ((error as HttpException).code() == 401) {
                            userLogin(
                                preference!![PrefKeys.PREF_EMAIL]!!,
                                preference!![PrefKeys.PREF_PASS]!!
                            ) { result ->
                                getClientAvailability()
                            }
                        } else {
                            displayAfterLoginErrorMsg(error)
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    override fun onResume() {
        super.onResume()
        clientAvailability = arrayListOf()
        preferTime = arrayListOf()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ClientAvailabilityFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: Boolean, param2: String = "") =
            ClientAvailabilityFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_client_availability"
    }

    override fun onClickListener(string: String, isRemove: Boolean) {
        if (!isRemove) {
            if (clientAvailability.contains(string)) {
                clientAvailability.remove(string)
            }
            if (preferTime.contains(string)) {
                preferTime.remove(string)
            }
        } else {
            when (string) {
                "Monday" -> clientAvailability.add(string)
                "Tuesday" -> clientAvailability.add(string)
                "Wednesday" -> clientAvailability.add(string)
                "Thursday" -> clientAvailability.add(string)
                "Friday" -> clientAvailability.add(string)
                "Saturday" -> clientAvailability.add(string)
                "Sunday" -> clientAvailability.add(string)
                "Morning" -> preferTime.add(string)
                "Afternoon" -> preferTime.add(string)
                "Evening" -> preferTime.add(string)
            }
        }
    }
}