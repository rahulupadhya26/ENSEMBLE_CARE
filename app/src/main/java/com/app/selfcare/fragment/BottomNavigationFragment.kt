package com.app.selfcare.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.app.selfcare.R
import com.app.selfcare.data.HealthInfo
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.fragment_bottom_navigation.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [BottomNavigationFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BottomNavigationFragment : BaseFragment() {
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
        return R.layout.fragment_bottom_navigation
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.VISIBLE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE

        bottomNavigationView.background = null

        if (getBottomNavigation() == null) {
            val navigationView = bottomNavigationView
            setBottomNavigation(navigationView as BottomNavigationView)
            navigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
            fab.setOnClickListener {
                replaceFragment(
                    TherapistListFragment.newInstance(false),
                    R.id.layout_home,
                    TherapistListFragment.TAG
                )
            }
            getBottomNavigation()!!.selectedItemId = R.id.navigation_home
        }
    }

    private val mOnNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    replaceFragmentNoBackStack(
                        DashboardFragment(),
                        R.id.layoutContent,
                        DashboardFragment.TAG
                    )
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_explore -> {
                    replaceFragmentNoBackStack(
                        ExploreFragment(),
                        R.id.layoutContent,
                        ExploreFragment.TAG
                    )
                    //displayMsg("Message", "Screen under development")
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_coaches -> {
                    replaceFragmentNoBackStack(CoachesFragment(),R.id.layoutContent,CoachesFragment.TAG)
                    //displayMsg("Message", "Screen under development")
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_myHealthInfo -> {
                    replaceFragmentNoBackStack(
                        HealthInfoFragment(),
                        R.id.layoutContent,
                        HealthInfoFragment.TAG
                    )
                    //displayMsg("Message", "Screen under development")
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment BottomNavigationFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            BottomNavigationFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_BottomMenu"
    }
}