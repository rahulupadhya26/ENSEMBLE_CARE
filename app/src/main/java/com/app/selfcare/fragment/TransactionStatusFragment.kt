package com.app.selfcare.fragment

import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.app.selfcare.R
import com.app.selfcare.data.TransactionStatus
import com.app.selfcare.databinding.FragmentTransactionStatusBinding
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper.set
import com.app.selfcare.utils.Utils

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [TransactionStatusFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TransactionStatusFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var transSts: TransactionStatus? = null
    private var paymentSts: Boolean = false
    private lateinit var binding: FragmentTransactionStatusBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            transSts = it.getParcelable(ARG_PARAM1)
            paymentSts = it.getBoolean(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTransactionStatusBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getLayout(): Int {
        return R.layout.fragment_transaction_status
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE
        getSubTitle().text = ""

        preference!![PrefKeys.PREF_STEP] = Utils.PLAN_PAY

        if (paymentSts) {
            binding.imgTransaction.setImageResource(R.drawable.success)
            binding.txtTransStatus.text = "Transaction Successful"
            binding.txtTransStatus.setTextColor(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.primaryGreen
                )
            )
        } else {
            binding.imgTransaction.setImageResource(R.drawable.cancel)
            binding.txtTransStatus.text = "Transaction Failed"
            binding.txtTransStatus.setTextColor(ContextCompat.getColor(requireActivity(), R.color.red))
        }
        binding.txtTransId.text = transSts!!.transaction_id

        binding.btnTransStsContinue.setOnClickListener {
            Utils.isLoggedInFirstTime = true
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

        binding.layoutScreenshot.setOnClickListener {
            takeScreenshot()
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment TransactionStatusFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: TransactionStatus, param2: Boolean) =
            TransactionStatusFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM1, param1)
                    putBoolean(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_Trans_Status"
    }
}