package com.app.selfcare.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.app.selfcare.fragment.AssignedToDoFragment
import com.app.selfcare.fragment.ToDoFragment

class ToDoTabAdapter(
    fm: FragmentManager,
    private var totalTabs: Int
) :
    FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                ToDoFragment()
            }

            else -> {
                AssignedToDoFragment()
            }
        }
    }

    override fun getCount(): Int {
        return totalTabs
    }
}