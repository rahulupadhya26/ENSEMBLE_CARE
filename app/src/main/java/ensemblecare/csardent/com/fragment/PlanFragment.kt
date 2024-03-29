package ensemblecare.csardent.com.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ensemblecare.csardent.com.R
import ensemblecare.csardent.com.controller.AdapterCallback
import ensemblecare.csardent.com.data.AddOn
import ensemblecare.csardent.com.data.Plan
import ensemblecare.csardent.com.databinding.FragmentPlanBinding
import ensemblecare.csardent.com.preference.PrefKeys
import ensemblecare.csardent.com.preference.PreferenceHelper.get
import ensemblecare.csardent.com.preference.PreferenceHelper.set
import ensemblecare.csardent.com.utils.DateUtils
import ensemblecare.csardent.com.utils.Utils
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

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
    private var addOnNeeded: String? = null
    private var param2: String? = null
    private var selectedPlan = ""
    private var addOnPlan: AddOn? = null
    private lateinit var binding: FragmentPlanBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            addOnNeeded = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlanBinding.inflate(inflater, container, false)
        return binding.root
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

        binding.planBack.setOnClickListener {
            popBackStack()
        }

        val date: Calendar = Calendar.getInstance()
        date.add(Calendar.MONTH, 1)
        val nextMonthDate = SimpleDateFormat("MM/dd/yyyy").format(date.time)
        val nextMonthDateFormat = DateUtils("$nextMonthDate 00:00:00")

        binding.txtRenewPlan.text =
            "Auto renews on " + nextMonthDateFormat.getFullMonthName() + " " +
                    nextMonthDateFormat.getDay() +
                    nextMonthDateFormat.getDayNumberSuffix(
                        nextMonthDateFormat.getDay().toInt()
                    ) + " " +
                    nextMonthDateFormat.getYear()

        binding.txtPlanAgreeByTerms.makeLinks(
            Pair("our terms", View.OnClickListener {
                val termsApplyDialog = BottomSheetDialog(requireActivity(), R.style.SheetDialog)
                val termsApplyDialogView: View = layoutInflater.inflate(
                    R.layout.dialog_register_part_terms_conditions, null
                )
                termsApplyDialog.setContentView(termsApplyDialogView)
                termsApplyDialog.behavior.isFitToContents = false
                termsApplyDialog.behavior.halfExpandedRatio = 0.6f
                termsApplyDialog.setCanceledOnTouchOutside(true)
                termsApplyDialog.show()
            })
        )

        getPlanList()
    }

    @SuppressLint("SetTextI18n")
    private fun getPlanList() {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .getData("PI0038", getAccessToken())
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
                                if (!planList[i].is_addon) {
                                    planLists.add(planList[i])
                                } else {
                                    addOnPlan = AddOn(
                                        planList[i].id,
                                        planList[i].name,
                                        planList[i].stripe_product_id,
                                        planList[i].is_addon,
                                        planList[i].monthly_price,
                                        planList[i].quarterly_price,
                                        planList[i].annually_price,
                                        planList[i].no_of_sessions
                                    )
                                }
                            }

                            binding.txtCardViewPlan1.text = planLists[0].name
                            binding.txtPlan1.text = planLists[0].name
                            binding.txtCardViewPlan2.text = planLists[1].name
                            binding.txtPlan2.text = planLists[1].name

                            if (Utils.planName.isNotEmpty()) {
                                when (Utils.planName) {
                                    planLists[0].name -> {
                                        selectPlanA(planLists)
                                    }
                                    planLists[1].name -> {
                                        selectPlanB(planLists)
                                    }
                                    else -> {
                                        selectPlanA(planLists)
                                    }
                                }
                            } else {
                                selectPlanA(planLists)
                            }

                            binding.txtPlan1.setOnClickListener {
                                selectPlanA(planLists)
                            }

                            binding.txtPlan2.setOnClickListener {
                                selectPlanB(planLists)
                            }

                            binding.txtMonthlyPlanTerms.makeLinks(
                                Pair("Terms apply", View.OnClickListener {
                                    val termsApplyDialog =
                                        BottomSheetDialog(requireActivity(), R.style.SheetDialog)
                                    val termsApplyDialogView: View = layoutInflater.inflate(
                                        R.layout.dialog_register_part_terms_conditions, null
                                    )
                                    termsApplyDialog.setContentView(termsApplyDialogView)
                                    termsApplyDialog.behavior.isFitToContents = false
                                    termsApplyDialog.behavior.halfExpandedRatio = 0.6f
                                    termsApplyDialog.setCanceledOnTouchOutside(true)
                                    termsApplyDialog.show()
                                })
                            )

                            binding.txtQuarterlyPlanTerms.makeLinks(
                                Pair("Terms apply", View.OnClickListener {
                                    val termsApplyDialog =
                                        BottomSheetDialog(requireActivity(), R.style.SheetDialog)
                                    val termsApplyDialogView: View = layoutInflater.inflate(
                                        R.layout.dialog_register_part_terms_conditions, null
                                    )
                                    termsApplyDialog.setContentView(termsApplyDialogView)
                                    termsApplyDialog.behavior.isFitToContents = false
                                    termsApplyDialog.behavior.halfExpandedRatio = 0.6f
                                    termsApplyDialog.setCanceledOnTouchOutside(true)
                                    termsApplyDialog.show()
                                })
                            )

                            binding.txtAnnuallyPlanTerms.makeLinks(
                                Pair("Terms apply", View.OnClickListener {
                                    val termsApplyDialog =
                                        BottomSheetDialog(requireActivity(), R.style.SheetDialog)
                                    val termsApplyDialogView: View = layoutInflater.inflate(
                                        R.layout.dialog_register_part_terms_conditions, null
                                    )
                                    termsApplyDialog.setContentView(termsApplyDialogView)
                                    termsApplyDialog.behavior.isFitToContents = false
                                    termsApplyDialog.behavior.halfExpandedRatio = 0.6f
                                    termsApplyDialog.setCanceledOnTouchOutside(true)
                                    termsApplyDialog.show()
                                })
                            )

                            binding.layoutPlanMonthly.setOnClickListener {
                                if (selectedPlan == planLists[0].name) {
                                    onClickPlan(planLists[0], "Monthly")
                                } else {
                                    onClickPlan(planLists[1], "Monthly")
                                }
                            }

                            binding.layoutPlanQuarterly.setOnClickListener {
                                if (selectedPlan == planLists[0].name) {
                                    onClickPlan(planLists[0], "Quarterly")
                                } else {
                                    onClickPlan(planLists[1], "Quarterly")
                                }
                            }

                            binding.layoutPlanAnnually.setOnClickListener {
                                if (selectedPlan == planLists[0].name) {
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

    @SuppressLint("SetTextI18n")
    private fun selectPlanA(planLists: ArrayList<Plan>) {
        selectedPlan = planLists[0].name
        binding.txtPlan1.visibility = View.GONE
        binding.cardViewPlan1.visibility = View.VISIBLE
        binding.txtPlan2.visibility = View.VISIBLE
        binding.cardViewPlan2.visibility = View.GONE
        binding.tick1.visibility = View.INVISIBLE
        binding.tick2.visibility = View.INVISIBLE
        binding.tick3.visibility = View.INVISIBLE
        binding.txtPlanMonthlyPrice.text = "$" + planLists[0].monthly_price
        binding.txtPlanQuarterlyPrice.text =
            "$" + planLists[0].quarterly_price
        binding.txtPlanAnnuallyPrice.text =
            "$" + planLists[0].annually_price

        binding.txtMonthlySessions.text =
            planLists[0].no_of_sessions.toString() + " Sessions"
        binding.txtQuarterlySessions.text =
            (planLists[0].no_of_sessions * 3).toString() + " Sessions"
        binding.txtAnnuallySessions.text =
            (planLists[0].no_of_sessions * 12).toString() + " Sessions"
    }

    @SuppressLint("SetTextI18n")
    private fun selectPlanB(planLists: ArrayList<Plan>) {
        selectedPlan = planLists[1].name
        binding.txtPlan2.visibility = View.GONE
        binding.cardViewPlan2.visibility = View.VISIBLE
        binding.txtPlan1.visibility = View.VISIBLE
        binding.cardViewPlan1.visibility = View.GONE
        binding.tick1.visibility = View.VISIBLE
        binding.tick2.visibility = View.VISIBLE
        binding.tick3.visibility = View.VISIBLE
        binding.txtPlanMonthlyPrice.text = "$" + planLists[1].monthly_price
        binding.txtPlanQuarterlyPrice.text =
            "$" + planLists[1].quarterly_price
        binding.txtPlanAnnuallyPrice.text =
            "$" + planLists[1].annually_price

        binding.txtMonthlySessions.text =
            planLists[1].no_of_sessions.toString() + " Sessions"
        binding.txtQuarterlySessions.text =
            (planLists[1].no_of_sessions * 3).toString() + " Sessions"
        binding.txtAnnuallySessions.text =
            (planLists[1].no_of_sessions * 12).toString() + " Sessions"
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
        fun newInstance(param1: String, param2: String = "") =
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
            if (addOnNeeded == "Yes") {
                replaceFragment(
                    AddOnFragment.newInstance(plan, selectedPlan, addOnPlan!!),
                    R.id.layout_home,
                    PaymentSelectFragment.TAG
                )
            } else {
                replaceFragment(
                    PaymentSelectFragment.newInstance(plan, null, selectedPlan),
                    R.id.layout_home,
                    PaymentSelectFragment.TAG
                )
            }
        } else {
            if (plan.name == alreadySelectedPlan) {
                var severityRating = 20
                /*if (preference!![PrefKeys.PREF_SEVERITY_SCORE, ""]!! != null) {
                    if (preference!![PrefKeys.PREF_SEVERITY_SCORE, ""]!!.isNotEmpty()) {
                        severityRating = preference!![PrefKeys.PREF_SEVERITY_SCORE, ""]!!.toInt()
                    }
                }*/
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
                    "Plus" -> if (plan.name == "Premium" || plan.name == "Plus") {
                        if (addOnNeeded == "Yes") {
                            replaceFragment(
                                AddOnFragment.newInstance(plan, selectedPlan, addOnPlan!!),
                                R.id.layout_home,
                                PaymentSelectFragment.TAG
                            )
                        } else {
                            replaceFragment(
                                PaymentSelectFragment.newInstance(plan, null, selectedPlan),
                                R.id.layout_home,
                                PaymentSelectFragment.TAG
                            )
                        }
                    } else {
                        displayMsg("Alert", "Plan cannot be downgrade.")
                    }
                    "Premium" -> if (plan.name == "Premium") {
                        if (addOnNeeded == "Yes") {
                            replaceFragment(
                                AddOnFragment.newInstance(plan, selectedPlan, addOnPlan!!),
                                R.id.layout_home,
                                PaymentSelectFragment.TAG
                            )
                        } else {
                            replaceFragment(
                                PaymentSelectFragment.newInstance(plan, null, selectedPlan),
                                R.id.layout_home,
                                PaymentSelectFragment.TAG
                            )
                        }
                    } else {
                        displayMsg("Alert", "Plan cannot be downgrade.")
                    }
                    else -> {
                        if (addOnNeeded == "Yes") {
                            replaceFragment(
                                AddOnFragment.newInstance(plan, selectedPlan, addOnPlan!!),
                                R.id.layout_home,
                                PaymentSelectFragment.TAG
                            )
                        } else {
                            replaceFragment(
                                PaymentSelectFragment.newInstance(plan, null, selectedPlan),
                                R.id.layout_home,
                                PaymentSelectFragment.TAG
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onItemClicked(plan: Plan) {
        val selectedPlan = preference!![PrefKeys.PREF_SELECTED_PLAN, ""]!!
        if (selectedPlan == "null") {
            replaceFragment(
                AddOnFragment.newInstance(plan, "", addOnPlan!!),
                R.id.layout_home,
                PaymentSelectFragment.TAG
            )
        } else {
            if (plan.name == selectedPlan) {
                var severityRating = 20
                /*if (preference!![PrefKeys.PREF_SEVERITY_SCORE, ""]!! != null) {
                    if (preference!![PrefKeys.PREF_SEVERITY_SCORE, ""]!!.isNotEmpty()) {
                        severityRating = preference!![PrefKeys.PREF_SEVERITY_SCORE, ""]!!.toInt()
                    }
                }*/
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
                    "Plus" -> if (plan.name == "Premium" || plan.name == "Plus") {
                        replaceFragment(
                            AddOnFragment.newInstance(plan, "", addOnPlan!!),
                            R.id.layout_home,
                            AddOnFragment.TAG
                        )
                    } else {
                        displayMsg("Alert", "Plan cannot be downgrade.")
                    }
                    "Premium" -> if (plan.name == "Premium") {
                        replaceFragment(
                            AddOnFragment.newInstance(plan, "", addOnPlan!!),
                            R.id.layout_home,
                            AddOnFragment.TAG
                        )
                    } else {
                        displayMsg("Alert", "Plan cannot be downgrade.")
                    }
                    else -> {
                        replaceFragment(
                            AddOnFragment.newInstance(plan, "", addOnPlan!!),
                            R.id.layout_home,
                            AddOnFragment.TAG
                        )
                    }
                }
            }
        }
    }
}