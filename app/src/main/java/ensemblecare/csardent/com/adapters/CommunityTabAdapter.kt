package ensemblecare.csardent.com.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import ensemblecare.csardent.com.fragment.CareBuddyCommunityFragment
import ensemblecare.csardent.com.fragment.CompanionFragment
import ensemblecare.csardent.com.fragment.EventsCommunityFragment
import ensemblecare.csardent.com.fragment.ForumFragment

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