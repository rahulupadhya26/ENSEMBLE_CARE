package com.app.selfcare.adapters

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.app.selfcare.fragment.FacilityGoalFragment
import com.app.selfcare.fragment.PersonalGoalFragment

internal class GoalsAdapter(
    var context: Context,
    fm: FragmentManager,
    var totalTabs: Int
) :
    FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return if (position == 0) {
            PersonalGoalFragment()
        } else {
            FacilityGoalFragment()
        }
    }

    override fun getCount(): Int {
        return totalTabs
    }
}