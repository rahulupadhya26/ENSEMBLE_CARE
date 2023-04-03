package com.app.selfcare.fragment

import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import com.app.selfcare.R
import com.app.selfcare.adapters.AppointmentTabAdapter
import com.app.selfcare.databinding.FragmentAppointmentsBinding
import com.google.android.material.tabs.TabLayout

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AppointmentsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AppointmentsFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentAppointmentsBinding

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
        binding = FragmentAppointmentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getLayout(): Int {
        return R.layout.fragment_appointments
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE
        updateStatusBarColor(R.color.white)

        binding.appointmentsBack.setOnClickListener {
            setBottomNavigation(null)
            setLayoutBottomNavigation(null)
            replaceFragmentNoBackStack(
                BottomNavigationFragment(),
                R.id.layout_home,
                BottomNavigationFragment.TAG
            )
        }

        binding.tabLayoutAppointmentList.removeAllTabs()
        binding.viewPagerAppointmentList.removeAllViewsInLayout()
        binding.tabLayoutAppointmentList.addTab(binding.tabLayoutAppointmentList.newTab().setText("Today"))
        binding.tabLayoutAppointmentList.addTab(binding.tabLayoutAppointmentList.newTab().setText("Upcoming"))
        binding.tabLayoutAppointmentList.addTab(binding.tabLayoutAppointmentList.newTab().setText("Past"))
        binding.tabLayoutAppointmentList.tabGravity = TabLayout.GRAVITY_FILL
        val adapter = AppointmentTabAdapter(
            requireActivity(), childFragmentManager,
            binding.tabLayoutAppointmentList.tabCount
        )
        binding.viewPagerAppointmentList.adapter = adapter
        binding.viewPagerAppointmentList.addOnPageChangeListener(
            TabLayout.TabLayoutOnPageChangeListener(
                binding.tabLayoutAppointmentList
            )
        )
        binding.tabLayoutAppointmentList.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                binding.viewPagerAppointmentList.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AppointmentsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AppointmentsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_Appointments"
    }
}