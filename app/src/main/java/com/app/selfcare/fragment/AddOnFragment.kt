package com.app.selfcare.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.app.selfcare.R
import com.app.selfcare.controller.IOnBackPressed
import com.app.selfcare.data.AddOn
import com.app.selfcare.data.Plan
import com.app.selfcare.databinding.FragmentAddOnBinding
import com.app.selfcare.utils.Utils

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val ARG_PARAM3 = "param3"

/**
 * A simple [Fragment] subclass.
 * Use the [AddOnFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddOnFragment : BaseFragment(), IOnBackPressed {
    // TODO: Rename and change types of parameters
    private var plan: Plan? = null
    private var planType: String? = null
    private var addOn: AddOn? = null
    private lateinit var binding: FragmentAddOnBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            plan = it.getParcelable(ARG_PARAM1)
            planType = it.getString(ARG_PARAM2)
            addOn = it.getParcelable(ARG_PARAM3)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddOnBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getLayout(): Int {
        return R.layout.fragment_add_on
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE

        binding.imgAddOnBack.setOnClickListener {
            popBackStack()
        }

        Utils.planName = plan!!.name

        binding.txtAddOnPrice.text = "$" + addOn!!.monthly_price

        binding.skipForNow.setOnClickListener {
            replaceFragment(
                PaymentSelectFragment.newInstance(plan!!, null, planType!!),
                R.id.layout_home,
                PaymentSelectFragment.TAG
            )
        }

        binding.btnSubscribe.setOnClickListener {
            replaceFragment(
                PaymentSelectFragment.newInstance(
                    plan!!,
                    addOn!!, planType!!
                ),
                R.id.layout_home,
                PaymentSelectFragment.TAG
            )
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    override fun onDetach() {
        super.onDetach()
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AddOnFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: Plan, param2: String = "", param3: AddOn) =
            AddOnFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                    putParcelable(ARG_PARAM3, param3)
                }
            }

        const val TAG = "Screen_add_on"
    }

    override fun onBackPressed(): Boolean {
        popBackStack()
        return false
    }
}