package ensemblecare.csardent.com.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import ensemblecare.csardent.com.fragment.CareBuddyCommunityFragment
import ensemblecare.csardent.com.fragment.CompanionFragment

class CareBuddyTabAdapter(
    fm: FragmentManager,
    private var totalTabs: Int
) :
    FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                CompanionFragment()
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