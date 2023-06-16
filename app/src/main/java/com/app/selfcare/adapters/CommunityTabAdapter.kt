package com.app.selfcare.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.app.selfcare.fragment.CareBuddyCommunityFragment
import com.app.selfcare.fragment.CompanionFragment
import com.app.selfcare.fragment.EventsCommunityFragment
import com.app.selfcare.fragment.ForumFragment

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

            1 -> {
                CompanionFragment()
            }

            2 -> {
                CareBuddyCommunityFragment()
            }

            else -> {
                ForumFragment()
            }
        }
    }

    override fun getCount(): Int {
        return totalTabs
    }
}