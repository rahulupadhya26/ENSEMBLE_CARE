package com.app.selfcare.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import com.app.selfcare.R
import com.app.selfcare.adapters.PlanViewPagerAdapter
import com.app.selfcare.controller.AdapterCallback
import com.app.selfcare.data.Plan
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper.get
import com.app.selfcare.preference.PreferenceHelper.set
import com.app.selfcare.utils.DateUtils
import com.app.selfcare.utils.Utils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_plan.*
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
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
    private var selectedPlan = ""

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

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE
        getSubTitle().text = ""
        updateStatusBarColor(R.color.white)

        planBack.setOnClickListener {
            popBackStack()
        }

        val date: Calendar = Calendar.getInstance()
        date.add(Calendar.MONTH, 1)
        val nextMonthDate = SimpleDateFormat("MM/dd/yyyy").format(date.time)
        val nextMonthDateFormat = DateUtils("$nextMonthDate 00:00:00")

        txtRenewPlan.text =
            "Auto renews on " + nextMonthDateFormat.getFullMonthName() + " " +
                    nextMonthDateFormat.getDay() +
                    nextMonthDateFormat.getDayNumberSuffix(
                        nextMonthDateFormat.getDay().toInt()
                    ) + " " +
                    nextMonthDateFormat.getYear()

        getPlanList()
    }

    private fun getPlanList() {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .getData("PI0039", getAccessToken())
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

                            val planType: Type = object : TypeToken<ArrayList<Plan?>?>() {}.type
                            val planList: ArrayList<Plan> = Gson().fromJson(responseBody, planType)
                            val planLists: ArrayList<Plan> = arrayListOf()
                            for (i in 0 until planList.size) {
                                if (!planList[i].therapy.has_addon) {
                                    planLists.add(planList[i])
                                }
                            }

                            selectedPlan = "Plus"

                            txtCardViewPlan1.text = planLists[0].therapy.name
                            txtPlan1.text = planLists[0].therapy.name
                            txtCardViewPlan2.text = planLists[1].therapy.name
                            txtPlan2.text = planLists[1].therapy.name

                            txtPlanMonthlyPrice.text = "$" + planLists[0].therapy.monthly_price
                            txtPlanQuarterlyPrice.text = "$" + planLists[0].therapy.quaterly_price
                            txtPlanAnnuallyPrice.text = "$" + planLists[0].therapy.annually_price

                            txtPlan1.setOnClickListener {
                                selectedPlan = planLists[0].therapy.name
                                txtPlan1.visibility = View.GONE
                                cardViewPlan1.visibility = View.VISIBLE
                                txtPlan2.visibility = View.VISIBLE
                                cardViewPlan2.visibility = View.GONE
                                tick1.visibility = View.INVISIBLE
                                tick2.visibility = View.INVISIBLE
                                tick3.visibility = View.INVISIBLE
                                txtPlanMonthlyPrice.text = "$" + planLists[0].therapy.monthly_price
                                txtPlanQuarterlyPrice.text =
                                    "$" + planLists[0].therapy.quaterly_price
                                txtPlanAnnuallyPrice.text =
                                    "$" + planLists[0].therapy.annually_price
                            }

                            txtPlan2.setOnClickListener {
                                selectedPlan = planLists[1].therapy.name
                                txtPlan2.visibility = View.GONE
                                cardViewPlan2.visibility = View.VISIBLE
                                txtPlan1.visibility = View.VISIBLE
                                cardViewPlan1.visibility = View.GONE
                                tick1.visibility = View.VISIBLE
                                tick2.visibility = View.VISIBLE
                                tick3.visibility = View.VISIBLE
                                txtPlanMonthlyPrice.text = "$" + planLists[1].therapy.monthly_price
                                txtPlanQuarterlyPrice.text =
                                    "$" + planLists[1].therapy.quaterly_price
                                txtPlanAnnuallyPrice.text =
                                    "$" + planLists[1].therapy.annually_price
                            }

                            layoutPlanMonthly.setOnClickListener {
                                if (selectedPlan == planLists[0].therapy.name) {
                                    onClickPlan(planLists[0], "Monthly")
                                } else {
                                    onClickPlan(planLists[1], "Monthly")
                                }
                            }

                            layoutPlanQuarterly.setOnClickListener {
                                if (selectedPlan == planLists[0].therapy.name) {
                                    onClickPlan(planLists[0], "Quarterly")
                                } else {
                                    onClickPlan(planLists[1], "Quarterly")
                                }
                            }

                            layoutPlanAnnually.setOnClickListener {
                                if (selectedPlan == planLists[0].therapy.name) {
                                    onClickPlan(planLists[0], "Annually")
                                } else {
                                    onClickPlan(planLists[1], "Annually")
                                }
                            }

                            /*val viewPagerAdapter =
                                PlanViewPagerAdapter(
                                    requireActivity(),
                                    planLists,
                                    this,
                                    preference!![PrefKeys.PREF_SELECTED_PLAN, ""]!!
                                )
                            viewPagerPlan.adapter = viewPagerAdapter
                            val selectedPlan = preference!![PrefKeys.PREF_SELECTED_PLAN, ""]!!
                            var planIndex = 0
                            for (i in 0 until planLists.size) {
                                if (selectedPlan.isNotEmpty() && selectedPlan == planLists[i].therapy.name) {
                                    planIndex = i
                                    break
                                }
                            }
                            viewPagerPlan.currentItem = planIndex
                            viewPagerPlan.clipToPadding = false
                            viewPagerPlan.clipChildren = false
                            viewPagerPlan.offscreenPageLimit = 3
                            viewPagerPlan.getChildAt(0).overScrollMode =
                                RecyclerView.OVER_SCROLL_NEVER
                            val compositePageTransform = CompositePageTransformer()
                            compositePageTransform.addTransformer(MarginPageTransformer(20))
                            compositePageTransform.addTransformer { page, position ->
                                val r: Float = 1 - abs(position)
                                page.scaleY = 0.85f + r * 0.15f
                            }
                            viewPagerPlan.setPageTransformer(compositePageTransform)*/
                        } catch (e: Exception) {
                            hideProgress()
                            e.printStackTrace()
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

    private fun onClickPlan(plan: Plan, selectedPlan: String) {
        val alreadySelectedPlan = preference!![PrefKeys.PREF_SELECTED_PLAN, ""]!!
        if (alreadySelectedPlan == "null") {
            replaceFragment(
                AddOnFragment.newInstance(plan, selectedPlan),
                R.id.layout_home,
                PaymentSelectFragment.TAG
            )
        } else {
            if (plan.therapy.name == alreadySelectedPlan) {
                val severityRating = preference!![PrefKeys.PREF_SEVERITY_SCORE, ""]!!.toInt()
                preference!![PrefKeys.PREF_IS_LOGGEDIN] = true
                if (severityRating in 0..14) {
                    replaceFragmentNoBackStack(
                        BottomNavigationFragment(),
                        R.id.layout_home,
                        BottomNavigationFragment.TAG
                    )
                } else {
                    Utils.isTherapististScreen = true
                    replaceFragmentNoBackStack(
                        ClientAvailabilityFragment.newInstance(true),
                        R.id.layout_home,
                        ClientAvailabilityFragment.TAG
                    )
                }
            } else {
                when (alreadySelectedPlan) {
                    "Plus" -> if (plan.therapy.name == "Premium" || plan.therapy.name == "Plus") {
                        replaceFragment(
                            AddOnFragment.newInstance(plan, selectedPlan),
                            R.id.layout_home,
                            AddOnFragment.TAG
                        )
                    } else {
                        displayMsg("Alert", "Plan cannot be downgrade.")
                    }
                    "Premium" -> if (plan.therapy.name == "Premium") {
                        replaceFragment(
                            AddOnFragment.newInstance(plan, selectedPlan),
                            R.id.layout_home,
                            AddOnFragment.TAG
                        )
                    } else {
                        displayMsg("Alert", "Plan cannot be downgrade.")
                    }
                    else -> {
                        replaceFragment(
                            AddOnFragment.newInstance(plan, selectedPlan),
                            R.id.layout_home,
                            AddOnFragment.TAG
                        )
                    }
                }
            }
        }
    }

    override fun onItemClicked(plan: Plan) {
        val selectedPlan = preference!![PrefKeys.PREF_SELECTED_PLAN, ""]!!
        if (selectedPlan == "null") {
            replaceFragment(
                AddOnFragment.newInstance(plan),
                R.id.layout_home,
                PaymentSelectFragment.TAG
            )
        } else {
            if (plan.therapy.name == selectedPlan) {
                val severityRating = preference!![PrefKeys.PREF_SEVERITY_SCORE, ""]!!.toInt()
                preference!![PrefKeys.PREF_IS_LOGGEDIN] = true
                if (severityRating in 0..14) {
                    replaceFragmentNoBackStack(
                        BottomNavigationFragment(),
                        R.id.layout_home,
                        BottomNavigationFragment.TAG
                    )
                } else {
                    Utils.isTherapististScreen = true
                    replaceFragmentNoBackStack(
                        ClientAvailabilityFragment.newInstance(true),
                        R.id.layout_home,
                        ClientAvailabilityFragment.TAG
                    )
                }
            } else {
                when (selectedPlan) {
                    "Plus" -> if (plan.therapy.name == "Premium" || plan.therapy.name == "Plus") {
                        replaceFragment(
                            AddOnFragment.newInstance(plan),
                            R.id.layout_home,
                            AddOnFragment.TAG
                        )
                    } else {
                        displayMsg("Alert", "Plan cannot be downgrade.")
                    }
                    "Premium" -> if (plan.therapy.name == "Premium") {
                        replaceFragment(
                            AddOnFragment.newInstance(plan),
                            R.id.layout_home,
                            AddOnFragment.TAG
                        )
                    } else {
                        displayMsg("Alert", "Plan cannot be downgrade.")
                    }
                    else -> {
                        replaceFragment(
                            AddOnFragment.newInstance(plan),
                            R.id.layout_home,
                            AddOnFragment.TAG
                        )
                    }
                }
            }
        }
    }
}