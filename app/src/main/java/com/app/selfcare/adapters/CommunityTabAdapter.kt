package com.app.selfcare.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.app.selfcare.fragment.CareBuddyCommunityFragment
import com.app.selfcare.fragment.EventsCommunityFragment

class CommunityTabAdapter(
    fm: FragmentManager,
    private var totalTabs: Int
) :
    FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                EventsCommunityFragment()
            }
            else -> {
                CareBuddyCommunityFragment()
            }
        }
    }

    override fun getCount(): Int {
        return totalTabs
    }
}