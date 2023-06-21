package ensemblecare.csardent.com.adapters

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import ensemblecare.csardent.com.fragment.PastAppointmentFragment
import ensemblecare.csardent.com.fragment.TodayAppointmentFragment
import ensemblecare.csardent.com.fragment.UpcomingAppointmentFragment

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