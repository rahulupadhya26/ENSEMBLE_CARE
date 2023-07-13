package ensemblecare.csardent.com.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import ensemblecare.csardent.com.fragment.AssistantFragment
import ensemblecare.csardent.com.fragment.CareBuddyCommunityFragment
import ensemblecare.csardent.com.fragment.CareBuddyFragment
import ensemblecare.csardent.com.fragment.CompanionCommunityFragment
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
                CompanionCommunityFragment()
            }

            2 -> {
                CareBuddyFragment()
            }

            3 -> {
                ForumFragment()
            }

            else -> {
                AssistantFragment()
            }
        }
    }

    override fun getCount(): Int {
        return totalTabs
    }
}