package com.app.selfcare.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.app.selfcare.R
import com.app.selfcare.adapters.PlanViewPagerAdapter
import com.app.selfcare.controller.AdapterCallback
import com.app.selfcare.data.Plan
import com.app.selfcare.data.Question
import com.app.selfcare.data.VerifyOtp
import com.app.selfcare.utils.ZoomOutPageTransformer
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_plan.*
import kotlinx.android.synthetic.main.fragment_registration.*
import org.json.JSONArray
import org.json.JSONObject
import java.lang.reflect.Type
import kotlin.math.abs

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PlanFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PlanFragment : BaseFragment(), AdapterCallback {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_plan
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE
        getSubTitle().text = ""

        /*val planList = ArrayList<Plan>()
        planList.add(Plan("1", "Standard", "11", "15"))
        planList.add(Plan("2", "Plus", "12", "20"))
        planList.add(Plan("3", "Premium", "13", "30"))*/
        getPlanList()
    }

    private fun getPlanList() {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .getPlanList("PI0039")
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
                            val jsonArr = JSONArray(responseBody)
                            val planList = ArrayList<Plan>()
                            for (i in 0 until jsonArr.length()) {
                                val planObj = jsonArr.getJSONObject(i)
                                val plan = Plan(
                                    planObj.getJSONObject("therapy").getString("product_id"),
                                    planObj.getJSONObject("therapy").getString("name"),
                                    planObj.getString("price_id"), planObj.getString("price"),
                                    planObj.getString("stripe_price_id")
                                )
                                planList.add(plan)
                            }
                            val viewPagerAdapter = PlanViewPagerAdapter(requireActivity(),planList, this)
                            viewPagerPlan.adapter = viewPagerAdapter
                            viewPagerPlan.currentItem = 1
                            viewPagerPlan.clipToPadding = false
                            viewPagerPlan.clipChildren = false
                            viewPagerPlan.offscreenPageLimit = 3
                            viewPagerPlan.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
                            val compositePageTransform = CompositePageTransformer()
                            compositePageTransform.addTransformer(MarginPageTransformer(20))
                            compositePageTransform.addTransformer { page, position ->
                                val r: Float = 1 - abs(position)
                                page.scaleY = 0.85f + r * 0.15f
                            }
                            viewPagerPlan.setPageTransformer(compositePageTransform)
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
        handler.postDelayed(runnable!!, 1000)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PlanFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PlanFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_Plan"
    }

    override fun onItemClicked(plan: Plan) {
        replaceFragment(
            PaymentSelectFragment.newInstance(plan),
            R.id.layout_home,
            PaymentSelectFragment.TAG
        )
    }
}