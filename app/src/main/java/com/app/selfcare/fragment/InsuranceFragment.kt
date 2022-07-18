package com.app.selfcare.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.app.selfcare.R
import com.app.selfcare.data.Plan
import com.app.selfcare.data.TransactionStatus
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper.get
import kotlinx.android.synthetic.main.fragment_insurance.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [InsuranceFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class InsuranceFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var plan: Plan? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            plan = it.getParcelable(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_insurance
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.VISIBLE
        getSubTitle().visibility = View.GONE
        getSubTitle().text = ""

        btnInsuranceDetails.setOnClickListener {
            if (getText(etInsuranceCompany).isNotEmpty()) {
                if (getText(etPlanId).isNotEmpty()) {
                    if (getText(etMemberId).isNotEmpty()) {
                        if (getText(etGroupId).isNotEmpty()) {
                            if (getText(etMemberName).isNotEmpty()) {
                                //Call VerifyTx Api
                                val transSts = TransactionStatus(
                                    "1",
                                    "56uyw-12edr-edry5-edtgy",
                                    preference!![PrefKeys.PREF_EMAIL, ""]!!,
                                    10,
                                    "dfghj",
                                    true,
                                    12
                                )
                                replaceFragment(
                                    TransactionStatusFragment.newInstance(transSts),
                                    R.id.layout_home,
                                    TransactionStatusFragment.TAG
                                )
                            } else {
                                setEditTextError(etMemberName, "Member name cannot be empty.")
                            }
                        } else {
                            setEditTextError(etGroupId, "Group Id cannot be empty.")
                        }
                    } else {
                        setEditTextError(etMemberId, "Member Id cannot be empty.")
                    }
                } else {
                    setEditTextError(etPlanId, "Plan Id cannot be empty.")
                }
            } else {
                setEditTextError(etInsuranceCompany, "Insurance Company cannot be empty.")
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
         * @return A new instance of fragment InsuranceFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: Plan, param2: String = "") =
            InsuranceFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_Payment_Insurance"
    }
}