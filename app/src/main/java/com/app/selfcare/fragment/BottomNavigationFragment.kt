package com.app.selfcare.fragment

import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import com.app.selfcare.R
import com.app.selfcare.databinding.FragmentBottomNavigationBinding
import com.app.selfcare.utils.Utils
import com.google.android.material.bottomnavigation.BottomNavigationView

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
    private lateinit var binding: FragmentBottomNavigationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBottomNavigationBinding.inflate(inflater, container, false)
        return binding.root
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

        binding.bottomNavigationView.background = null

        if (getBottomNavigation() == null) {
            val navigationView = binding.bottomNavigationView
            setBottomNavigation(navigationView as BottomNavigationView)
            navigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
            getBottomNavigation()!!.selectedItemId = R.id.navigation_home
        }

        if (getLayoutBottomNavigation() == null) {
            setLayoutBottomNavigation(binding.layoutBottomNav)
            binding.layoutBottomNav.visibility = View.VISIBLE
            if (Utils.bottomNav == Utils.BOTTOM_NAV_DASHBOARD) {
                navigateDashboard()
            }

            if (Utils.bottomNav == Utils.BOTTOM_NAV_COACH) {
                navigateCarePlan()
            }

            if (Utils.bottomNav == Utils.BOTTOM_NAV_WELLNESS) {
                navigateWellness()
            }

            if (Utils.bottomNav == Utils.BOTTOM_NAV_CRISIS) {
                navigateCrisis()
            }

            /*binding.imgCrisis.visibility = View.VISIBLE
            binding.cardViewCrisis.visibility = View.GONE

            binding.imgCoaches.visibility = View.VISIBLE
            binding.cardViewCoaches.visibility = View.GONE

            binding.imgActivity.visibility = View.VISIBLE
            binding.cardViewActivity.visibility = View.GONE*/

            /*replaceFragmentNoBackStack(
                DashboardFragment(),
                R.id.layoutContent,
                DashboardFragment.TAG
            )*/
        }

        binding.fab.setOnClickListener {
            Utils.isTherapististScreen = false
            clearTempFormData()
            replaceFragmentNoBackStack(
                ClientAvailabilityFragment.newInstance(false),
                R.id.layout_home,
                ClientAvailabilityFragment.TAG
            )
        }


        binding.layoutDashboard.setOnClickListener {
            navigateDashboard()
        }

        binding.layoutActivity.setOnClickListener {
            navigateCarePlan()
        }

        binding.layoutCoaches.setOnClickListener {
            navigateWellness()
        }

        binding.layoutCrisis.setOnClickListener {
            navigateCrisis()
        }
    }

    private fun navigateDashboard() {
        Utils.bottomNav = Utils.BOTTOM_NAV_DASHBOARD
        binding.imgDashboard.visibility = View.GONE
        binding.cardViewDashboard.visibility = View.VISIBLE

        binding.imgCrisis.visibility = View.VISIBLE
        binding.cardViewCrisis.visibility = View.GONE

        binding.imgCoaches.visibility = View.VISIBLE
        binding.cardViewCoaches.visibility = View.GONE

        binding.imgActivity.visibility = View.VISIBLE
        binding.cardViewActivity.visibility = View.GONE

        replaceFragmentNoBackStack(
            DashboardFragment(),
            R.id.layoutContent,
            DashboardFragment.TAG
        )
        binding.fab.visibility = View.VISIBLE
    }

    private fun navigateCarePlan() {
        Utils.bottomNav = Utils.BOTTOM_NAV_COACH
        binding.imgActivity.visibility = View.GONE
        binding.cardViewActivity.visibility = View.VISIBLE

        binding.imgDashboard.visibility = View.VISIBLE
        binding.cardViewDashboard.visibility = View.GONE

        binding.imgCoaches.visibility = View.VISIBLE
        binding.cardViewCoaches.visibility = View.GONE

        binding.imgCrisis.visibility = View.VISIBLE
        binding.cardViewCrisis.visibility = View.GONE

        replaceFragmentNoBackStack(
            CarePlanDashboardFragment(),
            R.id.layoutContent,
            CarePlanDashboardFragment.TAG
        )
        binding.fab.visibility = View.GONE
    }

    private fun navigateWellness() {
        Utils.bottomNav = Utils.BOTTOM_NAV_WELLNESS
        binding.imgCoaches.visibility = View.GONE
        binding.cardViewCoaches.visibility = View.VISIBLE

        binding.imgActivity.visibility = View.VISIBLE
        binding.cardViewActivity.visibility = View.GONE

        binding.imgDashboard.visibility = View.VISIBLE
        binding.cardViewDashboard.visibility = View.GONE

        binding.imgCrisis.visibility = View.VISIBLE
        binding.cardViewCrisis.visibility = View.GONE

        replaceFragmentNoBackStack(
            CoachesFragment(),
            R.id.layoutContent,
            CoachesFragment.TAG
        )
        binding.fab.visibility = View.GONE
    }

    private fun navigateCrisis() {
        Utils.bottomNav = Utils.BOTTOM_NAV_CRISIS
        binding.imgCrisis.visibility = View.GONE
        binding.cardViewCrisis.visibility = View.VISIBLE

        binding.imgCoaches.visibility = View.VISIBLE
        binding.cardViewCoaches.visibility = View.GONE

        binding.imgActivity.visibility = View.VISIBLE
        binding.cardViewActivity.visibility = View.GONE

        binding.imgDashboard.visibility = View.VISIBLE
        binding.cardViewDashboard.visibility = View.GONE

        replaceFragmentNoBackStack(
            CrisisManagementFragment(),
            R.id.layoutContent,
            CrisisManagementFragment.TAG
        )
        binding.fab.visibility = View.GONE
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