package com.app.selfcare.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.app.selfcare.R
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
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE

        // Define container.
        /*val loginDialogContainer = requireActivity().supportFragmentManager.findFragmentById(R.id.main_fragment) as NavHostFragment
        // Set nav controller.
        val loginNavController: NavController = loginDialogContainer.navController

        val appBarConfiguration = AppBarConfiguration(loginNavController.graph)*/

        bottomNavigationView.background = null

        if (getBottomNavigation() == null) {
            val navigationView = bottomNavigationView
            setBottomNavigation(navigationView as BottomNavigationView)
            navigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
            getBottomNavigation()!!.selectedItemId = R.id.navigation_home
        }

        if (getLayoutBottomNavigation() == null) {
            setLayoutBottomNavigation(layoutBottomNav)
            layoutBottomNav.visibility = View.VISIBLE

            imgDashboard.visibility = View.GONE
            cardViewDashboard.visibility = View.VISIBLE

            imgCrisis.visibility = View.VISIBLE
            cardViewCrisis.visibility = View.GONE

            imgCoaches.visibility = View.VISIBLE
            cardViewCoaches.visibility = View.GONE

            imgActivity.visibility = View.VISIBLE
            cardViewActivity.visibility = View.GONE

            replaceFragmentNoBackStack(
                DashboardFragment(),
                R.id.layoutContent,
                DashboardFragment.TAG
            )

            fab.visibility = View.VISIBLE
        }

        fab.setOnClickListener {
            replaceFragment(
                ClientAvailabilityFragment.newInstance(false),
                R.id.layout_home,
                ClientAvailabilityFragment.TAG
            )
        }


        layoutDashboard.setOnClickListener {
            imgDashboard.visibility = View.GONE
            cardViewDashboard.visibility = View.VISIBLE

            imgCrisis.visibility = View.VISIBLE
            cardViewCrisis.visibility = View.GONE

            imgCoaches.visibility = View.VISIBLE
            cardViewCoaches.visibility = View.GONE

            imgActivity.visibility = View.VISIBLE
            cardViewActivity.visibility = View.GONE

            replaceFragmentNoBackStack(
                DashboardFragment(),
                R.id.layoutContent,
                DashboardFragment.TAG
            )
            fab.visibility = View.VISIBLE
        }

        layoutActivity.setOnClickListener {
            imgActivity.visibility = View.GONE
            cardViewActivity.visibility = View.VISIBLE

            imgDashboard.visibility = View.VISIBLE
            cardViewDashboard.visibility = View.GONE

            imgCoaches.visibility = View.VISIBLE
            cardViewCoaches.visibility = View.GONE

            imgCrisis.visibility = View.VISIBLE
            cardViewCrisis.visibility = View.GONE

            replaceFragmentNoBackStack(
                CarePlanDashboardFragment(),
                R.id.layoutContent,
                CarePlanDashboardFragment.TAG
            )
            fab.visibility = View.GONE
        }

        layoutCoaches.setOnClickListener {
            imgCoaches.visibility = View.GONE
            cardViewCoaches.visibility = View.VISIBLE

            imgActivity.visibility = View.VISIBLE
            cardViewActivity.visibility = View.GONE

            imgDashboard.visibility = View.VISIBLE
            cardViewDashboard.visibility = View.GONE

            imgCrisis.visibility = View.VISIBLE
            cardViewCrisis.visibility = View.GONE

            replaceFragmentNoBackStack(
                CoachesFragment(),
                R.id.layoutContent,
                CoachesFragment.TAG
            )
            fab.visibility = View.GONE
        }

        layoutCrisis.setOnClickListener {
            imgCrisis.visibility = View.GONE
            cardViewCrisis.visibility = View.VISIBLE

            imgCoaches.visibility = View.VISIBLE
            cardViewCoaches.visibility = View.GONE

            imgActivity.visibility = View.VISIBLE
            cardViewActivity.visibility = View.GONE

            imgDashboard.visibility = View.VISIBLE
            cardViewDashboard.visibility = View.GONE

            replaceFragmentNoBackStack(
                HealthInfoFragment(),
                R.id.layoutContent,
                HealthInfoFragment.TAG
            )
            fab.visibility = View.GONE
        }
    }

    private val mOnNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    /*replaceFragmentNoBackStack(
                        DashboardFragment(),
                        R.id.layoutContent,
                        DashboardFragment.TAG
                    )*/
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_activity -> {
                    /*replaceFragmentNoBackStack(
                        ExploreFragment(),
                        R.id.layoutContent,
                        ExploreFragment.TAG
                    )*/
                    //displayMsg("Message", "Screen under development")
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_personal_trainer -> {
                    //replaceFragmentNoBackStack(CoachesFragment(),R.id.layoutContent,CoachesFragment.TAG)
                    //displayMsg("Message", "Screen under development")
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_crisis -> {
                    /*replaceFragmentNoBackStack(
                        HealthInfoFragment(),
                        R.id.layoutContent,
                        HealthInfoFragment.TAG
                    )*/
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