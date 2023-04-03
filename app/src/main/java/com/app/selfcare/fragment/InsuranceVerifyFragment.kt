package com.app.selfcare.fragment

import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import com.app.selfcare.R
import com.app.selfcare.data.PlanDetails
import com.app.selfcare.databinding.FragmentActivityCarePlanBinding
import com.app.selfcare.databinding.FragmentInsuranceVerifyBinding
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper.get
import com.app.selfcare.preference.PreferenceHelper.set
import com.app.selfcare.utils.Utils

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val ARG_PARAM3 = "param3"

/**
 * A simple [Fragment] subclass.
 * Use the [InsuranceVerifyFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class InsuranceVerifyFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var planDetail: PlanDetails? = null
    private var vobStatus: Int = 0
    private var type: String? = null
    private lateinit var binding: FragmentInsuranceVerifyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            planDetail = it.getParcelable(ARG_PARAM1)
            vobStatus = it.getInt(ARG_PARAM2)
            type = it.getString(ARG_PARAM3)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentInsuranceVerifyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getLayout(): Int {
        return R.layout.fragment_insurance_verify
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE
        updateStatusBarColor(R.color.white)

        when (vobStatus) {
            Utils.VOB_PENDING -> {
                binding.layoutInsuranceVerifyProgress.visibility = View.VISIBLE
                binding.layoutInsuranceVerifyCompleted.visibility = View.GONE
                binding.layoutInsuranceVerifyFailed.visibility = View.GONE
            }
            Utils.VOB_COMPLETED -> {
                binding.layoutInsuranceVerifyCompleted.visibility = View.VISIBLE
                binding.layoutInsuranceVerifyProgress.visibility = View.GONE
                binding.layoutInsuranceVerifyFailed.visibility = View.GONE
                var htmlText = ""
                if (planDetail != null) {
                    htmlText =
                        "<font color='#5C2E7E'><b>Congratulations!</b></font> You have been subscribed to <b>" + planDetail!!.plan_detail + "</b> plan"
                } else {
                    htmlText = "<font color='#5C2E7E'><b>Congratulations!</b></font>"
                }
                binding.txtVobVerified.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Html.fromHtml(htmlText, Html.FROM_HTML_MODE_COMPACT)
                } else {
                    Html.fromHtml(htmlText)
                }
            }
            Utils.VOB_FAILED -> {
                binding.layoutInsuranceVerifyFailed.visibility = View.VISIBLE
                binding.layoutInsuranceVerifyCompleted.visibility = View.GONE
                binding.layoutInsuranceVerifyProgress.visibility = View.GONE
            }
        }

        binding.btnInsuranceVerificationCompleted.setOnClickListener {
            Utils.isLoggedInFirstTime = true
            preference!![PrefKeys.PREF_STEP] = Utils.PLAN_PAY
            preference!![PrefKeys.PREF_INSURANCE_VERIFICATION] = false
            val severityRating = 20
            /*if (preference!![PrefKeys.PREF_SEVERITY_SCORE, ""]!! != null) {
                if (preference!![PrefKeys.PREF_SEVERITY_SCORE, ""]!!.isNotEmpty()) {
                    severityRating = preference!![PrefKeys.PREF_SEVERITY_SCORE, ""]!!.toInt()
                }
            }*/
            preference!![PrefKeys.PREF_IS_LOGGEDIN] = true
            if (severityRating in 0..14) {
                Utils.isTherapististScreen = false
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
        }

        binding.btnInsuranceVerificationFailed.setOnClickListener {
            preference!![PrefKeys.PREF_INSURANCE_VERIFICATION] = false
            if (type != null && type == "EAP") {
                replaceFragmentNoBackStack(
                    PlanFragment.newInstance("Yes"),
                    R.id.layout_home,
                    PlanFragment.TAG
                )
            } else {
                replaceFragmentNoBackStack(
                    RegisterPartCFragment(),
                    R.id.layout_home,
                    RegisterPartCFragment.TAG
                )
            }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment InsuranceVerifyFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: PlanDetails?, param2: Int, param3: String?) =
            InsuranceVerifyFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM1, param1)
                    putInt(ARG_PARAM2, param2)
                    putString(ARG_PARAM3, param3)
                }
            }

        const val TAG = "Screen_Insurance_Verify"
    }
}