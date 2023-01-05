package com.app.selfcare.adapters

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.app.selfcare.data.GetAppointmentList
import com.app.selfcare.fragment.CarePlanDashboardFragment
import com.app.selfcare.fragment.PastAppointmentFragment
import com.app.selfcare.fragment.TodayAppointmentFragment
import com.app.selfcare.fragment.UpcomingAppointmentFragment

class AppointmentTabAdapter(
    private var context: Context,
    fm: FragmentManager,
    private var totalTabs: Int
) :
    FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                TodayAppointmentFragment()
            }
            1 -> {
                UpcomingAppointmentFragment()
            }
            else -> {
                PastAppointmentFragment()
            }
        }
    }

    override fun getCount(): Int {
        return totalTabs
    }
}