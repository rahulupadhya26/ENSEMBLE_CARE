package com.app.selfcare.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.app.selfcare.R
import com.app.selfcare.adapters.TherapyTypeListAdapter
import com.app.selfcare.controller.OnTherapyTypeClickListener
import com.app.selfcare.data.DeviceId
import com.app.selfcare.data.TherapyType
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper.set
import com.app.selfcare.utils.Utils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_carousel.*
import org.json.JSONObject
import kotlin.math.abs


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CarouselFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CarouselFragment : BaseFragment(), OnTherapyTypeClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var sliderHandler = Handler()
    private var therapySel: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_carousel
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE
        getSubTitle().text = ""

        /*cardViewChild.setOnClickListener {
            therapySel = "Child"
            sendDeviceId(therapySel!!)
        }

        cardViewTeen.setOnClickListener {
            therapySel = "Teen"
            sendDeviceId(therapySel!!)
        }

        cardViewAdult.setOnClickListener {
            therapySel = "Adult"
            sendDeviceId(therapySel!!)
        }

        cardViewCouple.setOnClickListener {
            therapySel = "Couple"
            sendDeviceId(therapySel!!)
        }

        cardViewLgbtq.setOnClickListener {
            therapySel = "Lgbtq"
            sendDeviceId(therapySel!!)
        }*/

        val therapyTypeList: ArrayList<TherapyType> = ArrayList()
        therapyTypeList.add(TherapyType("Child", R.drawable.child))
        therapyTypeList.add(TherapyType("Teen", R.drawable.teen))
        therapyTypeList.add(TherapyType("Adult", R.drawable.adult))
        therapyTypeList.add(TherapyType("Couple", R.drawable.couple))
        therapyTypeList.add(TherapyType("Lgbtq", R.drawable.lgbtq))

        val staggeredGridLayoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        staggeredGridLayoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_NONE
        recyclerViewTherapyTypeList.layoutManager = staggeredGridLayoutManager
        recyclerViewTherapyTypeList.setHasFixedSize(true)
        recyclerViewTherapyTypeList.adapter =
            TherapyTypeListAdapter(mActivity!!, therapyTypeList, this)

        /*viewPagerTherapySlider.clipToPadding = false
        viewPagerTherapySlider.clipChildren = false
        viewPagerTherapySlider.offscreenPageLimit = 3
        viewPagerTherapySlider.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER

        val compositePageTransform = CompositePageTransformer()
        compositePageTransform.addTransformer(MarginPageTransformer(40))
        compositePageTransform.addTransformer { page, position ->
            val r: Float = 1 - abs(position)
            page.scaleY = 0.85f + r * 0.15f
        }
        viewPagerTherapySlider.setPageTransformer(compositePageTransform)

        viewPagerTherapySlider.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                sliderHandler.removeCallbacks(sliderRunnable)
                sliderHandler.postDelayed(sliderRunnable, 5000)
            }
        })*/
    }

    @SuppressLint("HardwareIds")
    private fun sendDeviceId(selectedTherapy: String) {
        var selectedTherapyId = 0
        when (selectedTherapy) {
            "Teen" -> {
                selectedTherapyId = 1
            }
            "Adult" -> {
                selectedTherapyId = 2
            }
            "Couple" -> {
                selectedTherapyId = 3
            }
            "Lgbtq" -> {
                selectedTherapyId = 4
            }
        }
        showProgress()
        val deviceId: String =
            Settings.Secure.getString(requireActivity().contentResolver, Settings.Secure.ANDROID_ID)
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .sendDeviceId(DeviceId(deviceId, selectedTherapyId))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ result ->
                        try {
                            var responseBody = result.string()
                            Log.d("Response Body", responseBody)
                            val respBody = responseBody.split("|")
                            val status = respBody[1]
                            responseBody = respBody[0]
                            val jsonObject = JSONObject(responseBody)
                            preference!![PrefKeys.PREF_DEVICE_ID] =
                                jsonObject.getString("device_id")
                            preference!![PrefKeys.PREF_SELECTED_THERAPY] = selectedTherapy
                            preference!![PrefKeys.PREF_ID] =
                                jsonObject.getInt("id")
                            when (status) {
                                "500" -> {
                                    displayToast("Something went wrong.. Please try after sometime")
                                }
                                "400" -> {
                                    displayToast("Something went wrong.. Please try after sometime")
                                }
                                "200" -> {
                                    replaceFragmentNoBackStack(
                                        QuestionnaireFragment.newInstance(selectedTherapy),
                                        R.id.layout_home,
                                        QuestionnaireFragment.TAG
                                    )
                                }
                                "208" -> {
                                    replaceFragmentNoBackStack(
                                        QuestionnaireFragment.newInstance(selectedTherapy),
                                        R.id.layout_home,
                                        QuestionnaireFragment.TAG
                                    )
                                }
                            }
                            /*if (status == "208") {
                                replaceFragmentNoBackStack(
                                    RegistrationFragment(),
                                    R.id.layout_home,
                                    RegistrationFragment.TAG
                                )
                            } else {*/

                            //}
                            preference!![PrefKeys.PREF_STEP] = Utils.INTRO_SCREEN
                            //getQuestionnaire(selectedTherapy)
                        } catch (e: Exception) {
                            hideProgress()
                            displayToast("Something went wrong.. Please try after sometime")
                        }
                    }, { error ->
                        hideProgress()
                        displayToast("Error ${error.localizedMessage}")
                    })
            )
        }
        handler!!.postDelayed(runnable!!, 1000)

    }

    private val sliderRunnable: Runnable = Runnable {
        viewPagerTherapySlider.currentItem = viewPagerTherapySlider.currentItem + 1
    }

    override fun onPause() {
        super.onPause()
        sliderHandler.removeCallbacks(sliderRunnable)
    }

    override fun onResume() {
        super.onResume()
        sliderHandler.postDelayed(sliderRunnable, 3000)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CarouselFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CarouselFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onTherapyTypeClickListener(therapyType: TherapyType) {
        sendDeviceId(therapyType.text)
    }
}