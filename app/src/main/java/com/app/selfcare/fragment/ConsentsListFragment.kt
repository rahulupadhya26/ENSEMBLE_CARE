package com.app.selfcare.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.R
import com.app.selfcare.adapters.ConsentRoisListAdapter
import com.app.selfcare.adapters.GroupAppointmentsAdapter
import com.app.selfcare.controller.OnConsentRoisItemClickListener
import com.app.selfcare.controller.OnConsentRoisViewItemClickListener
import com.app.selfcare.data.ConsentRois
import com.app.selfcare.data.ConsentRoisFormsNotify
import com.app.selfcare.data.GroupAppointment
import com.app.selfcare.databinding.FragmentActivityCarePlanBinding
import com.app.selfcare.databinding.FragmentConsentsListBinding
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper.get
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.json.JSONException
import org.json.JSONObject
import retrofit2.HttpException
import java.lang.reflect.Type

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ConsentsListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ConsentsListFragment : BaseFragment(), OnConsentRoisItemClickListener,
    OnConsentRoisViewItemClickListener {
    // TODO: Rename and change types of parameters
    private var consentRoisFormsNotifyList: ArrayList<ConsentRoisFormsNotify>? = null
    private var param2: String? = null
    private lateinit var binding: FragmentConsentsListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            consentRoisFormsNotifyList = it.getParcelableArrayList(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentConsentsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getLayout(): Int {
        return R.layout.fragment_consents_list
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE
        updateStatusBarColor(R.color.white)

        binding.consentsRoisBack.setOnClickListener {
            setBottomNavigation(null)
            setLayoutBottomNavigation(null)
            replaceFragmentNoBackStack(
                BottomNavigationFragment(),
                R.id.layout_home,
                BottomNavigationFragment.TAG
            )
        }

        //Appointments
        displayConsentsList()

        binding.consentsRefresh.setOnClickListener {
            displayConsentsList()
        }
    }

    private fun displayConsentsList() {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .getConsentsList(getAccessToken())
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
                            val consentRois: ArrayList<ConsentRois> = arrayListOf()
                            val map: MutableMap<String, Any> = mutableMapOf()
                            val obj = JSONObject(responseBody)
                            val consentArr = obj.getJSONArray("consents")
                            val roisArr = obj.getJSONArray("rois")
                            val consentsNameListKeys: Iterator<String> =
                                obj.getJSONObject("consents_name_lst").keys()
                            for (key in consentsNameListKeys) {
                                try {
                                    val value: Any =
                                        obj.getJSONObject("consents_name_lst").getString(key)
                                    map[key] = value
                                } catch (e: JSONException) {
                                    e.printStackTrace()
                                }
                            }
                            for ((key, value) in map) {
                                for (arr in 0 until consentArr.length()) {
                                    if (consentArr.getJSONObject(arr).has(key)) {
                                        if (consentArr.getJSONObject(arr).get(key) == false) {
                                            val consentRoisData = ConsentRois(
                                                key,
                                                value as String,
                                                false,
                                                "None",
                                                "None",
                                                "None",
                                                "None"
                                            )
                                            consentRois.add(consentRoisData)
                                        } else {
                                            val consentRoisData = ConsentRois(
                                                key,
                                                value as String,
                                                true,
                                                consentArr.getJSONObject(arr).getJSONObject(key)
                                                    .getString("patient"),
                                                consentArr.getJSONObject(arr).getJSONObject(key)
                                                    .getString("doctor"),
                                                consentArr.getJSONObject(arr).getJSONObject(key)
                                                    .getString("patient_status"),
                                                consentArr.getJSONObject(arr).getJSONObject(key)
                                                    .getString("doctor_status")
                                            )
                                            consentRois.add(consentRoisData)
                                        }
                                    }
                                }
                            }

                            for ((key, value) in map) {
                                for (arr in 0 until roisArr.length()) {
                                    if (roisArr.getJSONObject(arr).has(key)) {
                                        if (roisArr.getJSONObject(arr).get(key) == false) {
                                            val consentRoisData = ConsentRois(
                                                key,
                                                value as String,
                                                false,
                                                "None",
                                                "None",
                                                "None",
                                                "None"
                                            )
                                            consentRois.add(consentRoisData)
                                        } else {
                                            val consentRoisData = ConsentRois(
                                                key,
                                                value as String,
                                                true,
                                                roisArr.getJSONObject(arr).getJSONObject(key)
                                                    .getString("patient"),
                                                roisArr.getJSONObject(arr).getJSONObject(key)
                                                    .getString("doctor"),
                                                roisArr.getJSONObject(arr).getJSONObject(key)
                                                    .getString("patient_status"),
                                                roisArr.getJSONObject(arr).getJSONObject(key)
                                                    .getString("doctor_status")
                                            )
                                            consentRois.add(consentRoisData)
                                        }
                                    }
                                }
                            }

                            if (consentRois.isNotEmpty()) {
                                binding.recyclerViewConsentsList.visibility = View.VISIBLE
                                binding.txtNoConsentList.visibility = View.GONE
                                binding.recyclerViewConsentsList.apply {
                                    layoutManager = LinearLayoutManager(
                                        mActivity!!,
                                        RecyclerView.VERTICAL,
                                        false
                                    )
                                    adapter = ConsentRoisListAdapter(
                                        consentRois,
                                        this@ConsentsListFragment,
                                        this@ConsentsListFragment
                                    )
                                }
                            } else {
                                binding.recyclerViewConsentsList.visibility = View.GONE
                                binding.txtNoConsentList.visibility = View.VISIBLE
                            }
                        } catch (e: Exception) {
                            hideProgress()
                            e.printStackTrace()
                            displayToast("Something went wrong.. Please try after sometime")
                        }
                    }, { error ->
                        hideProgress()
                        if ((error as HttpException).code() == 401) {
                            userLogin(
                                preference!![PrefKeys.PREF_EMAIL]!!,
                                preference!![PrefKeys.PREF_PASS]!!
                            ) { result ->
                                displayConsentsList()
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
         * @return A new instance of fragment ConsentsListFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: ArrayList<ConsentRoisFormsNotify>, param2: String = "") =
            ConsentsListFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_consents"
    }

    override fun onConsentRoisItemClickListener(consentRois: ArrayList<ConsentRois>) {
        replaceFragment(
            ConsentRoisSignFragment.newInstance(consentRoisFormsNotifyList!!),
            R.id.layout_home,
            ConsentRoisSignFragment.TAG
        )
    }

    override fun onConsentRoisViewItemClickListener(consentRois: ConsentRois) {
        replaceFragment(
            ConsentRoisSignFragment.newInstance(consentRoisFormsNotifyList!!),
            R.id.layout_home,
            ConsentRoisSignFragment.TAG
        )
    }
}