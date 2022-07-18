package com.app.selfcare.adapters


import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.app.selfcare.fragment.CarouselFragment
import com.app.selfcare.fragment.IntroScreensFragment

class ViewPagerAdapter internal constructor(fm: FragmentManager) :
    FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        //return IntroScreensFragment.newInstance(position.toString(),"")
        return when (position) {
            0 -> IntroScreensFragment.newInstance(position.toString(), "")
            1 -> IntroScreensFragment.newInstance(position.toString(), "")
            2 -> CarouselFragment()
            else -> IntroScreensFragment.newInstance(position.toString(), "")
        }
    }

    override fun getCount(): Int {
        return 3
    }
}
