package ensemblecare.csardent.com.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.tabs.TabLayout
import ensemblecare.csardent.com.R
import ensemblecare.csardent.com.adapters.CareBuddyTabAdapter
import ensemblecare.csardent.com.databinding.FragmentCareBuddyBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CareBuddyFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CareBuddyFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentCareBuddyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_care_buddy
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCareBuddyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE

        binding.tabLayoutCareBuddies.removeAllTabs()
        binding.viewPagerCareBuddies.removeAllViewsInLayout()
        binding.tabLayoutCareBuddies.addTab(
            binding.tabLayoutCareBuddies.newTab().setText("My CareBuddies")
        )
        binding.tabLayoutCareBuddies.addTab(
            binding.tabLayoutCareBuddies.newTab().setText("I am CareBuddy for")
        )

        binding.tabLayoutCareBuddies.tabGravity = TabLayout.GRAVITY_FILL
        val adapter =
            CareBuddyTabAdapter(childFragmentManager, binding.tabLayoutCareBuddies.tabCount)
        binding.viewPagerCareBuddies.adapter = adapter
        binding.viewPagerCareBuddies.addOnPageChangeListener(
            TabLayout.TabLayoutOnPageChangeListener(binding.tabLayoutCareBuddies)
        )
        binding.tabLayoutCareBuddies.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                binding.viewPagerCareBuddies.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CareBuddyFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CareBuddyFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
        const val TAG = "Screen_CareBuddies"
    }
}