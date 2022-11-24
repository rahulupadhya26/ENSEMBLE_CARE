package com.app.selfcare.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.app.selfcare.R
import kotlinx.android.synthetic.main.fragment_activity_dashboard.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ActivityDashboardFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ActivityDashboardFragment : BaseFragment() {
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
        return R.layout.fragment_activity_dashboard
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE

        /*drinkWaterProgress.maxProgress = 100.0
        drinkWaterProgress.setCurrentProgress(74.0)*/

        txtActivityShowAll.setOnClickListener {
            replaceFragment(
                ActivityShowAllFragment(),
                R.id.layout_home,
                ActivityShowAllFragment.TAG
            )
        }

        txtGoalShowAll.setOnClickListener {
            replaceFragment(
                ActivityShowAllFragment(),
                R.id.layout_home,
                ActivityShowAllFragment.TAG
            )
        }

        activityCreateGoal.setOnClickListener {
            replaceFragment(
                ActivityCreateGoalFragment(),
                R.id.layout_home,
                ActivityCreateGoalFragment.TAG
            )
        }

        cardViewActivityDrinkWater.setOnClickListener {
            replaceFragment(
                ActivityGoalDetailFragment(),
                R.id.layout_home,
                ActivityGoalDetailFragment.TAG
            )
        }

        drinkWaterProgress.setProgress(74.0,100.0)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ActivityDashboardFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ActivityDashboardFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
        const val TAG = "Screen_activity_dashboard"
    }
}