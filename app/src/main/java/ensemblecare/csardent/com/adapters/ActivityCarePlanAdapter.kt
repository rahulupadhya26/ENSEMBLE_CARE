package ensemblecare.csardent.com.adapters

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import ensemblecare.csardent.com.fragment.ActivityDashboardFragment
import ensemblecare.csardent.com.fragment.CarePlanDashboardFragment

internal class ActivityCarePlanAdapter(
    var context: Context,
    fm: FragmentManager,
    var totalTabs: Int
) :
    FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return if (position == 0) {
            ActivityDashboardFragment()
        } else {
            CarePlanDashboardFragment()
        }
    }

    override fun getCount(): Int {
        return totalTabs
    }
}