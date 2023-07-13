package ensemblecare.csardent.com.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ensemblecare.csardent.com.R
import ensemblecare.csardent.com.adapters.CommunityTabAdapter
import ensemblecare.csardent.com.databinding.FragmentCommunityBinding
import com.google.android.material.tabs.TabLayout

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CommunityFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CommunityFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentCommunityBinding
    private var tabPos = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCommunityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getLayout(): Int {
        return R.layout.fragment_community
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE
        updateStatusBarColor(R.color.white)

        binding.communityBack.setOnClickListener {
            setBottomNavigation(null)
            setLayoutBottomNavigation(null)
            replaceFragmentNoBackStack(
                BottomNavigationFragment(),
                R.id.layout_home,
                BottomNavigationFragment.TAG
            )
        }

        binding.companionAdd.setOnClickListener {
            if (binding.viewPagerCommunities.currentItem == 1) {
                replaceFragment(
                    AddCareBuddyFragment.newInstance("Companion"),
                    R.id.layout_home,
                    AddCareBuddyFragment.TAG
                )
            } else if (binding.viewPagerCommunities.currentItem == 2) {
                replaceFragment(
                    AddCareBuddyFragment.newInstance("Carebuddy"),
                    R.id.layout_home,
                    AddCareBuddyFragment.TAG
                )
            }
        }

        val icons = intArrayOf(
            R.drawable.local_activity,
            R.drawable.relax,
            R.drawable.volunteer_activism,
            R.drawable.diversity_1,
            R.drawable.assistant
        )

        binding.tabLayoutCommunities.removeAllTabs()
        binding.viewPagerCommunities.removeAllViewsInLayout()
        binding.tabLayoutCommunities.addTab(
            binding.tabLayoutCommunities.newTab().setText("Events").setIcon(icons[0])
        )
        binding.tabLayoutCommunities.addTab(
            binding.tabLayoutCommunities.newTab().setText("Companion").setIcon(icons[1])
        )
        binding.tabLayoutCommunities.addTab(
            binding.tabLayoutCommunities.newTab().setText("CareBuddy").setIcon(icons[2])
        )
        binding.tabLayoutCommunities.addTab(
            binding.tabLayoutCommunities.newTab().setText("Forum").setIcon(icons[3])
        )
        binding.tabLayoutCommunities.addTab(
            binding.tabLayoutCommunities.newTab().setText("AI Chat").setIcon(icons[4])
        )
        binding.tabLayoutCommunities.tabGravity = TabLayout.GRAVITY_FILL
        val adapter =
            CommunityTabAdapter(childFragmentManager, binding.tabLayoutCommunities.tabCount)
        binding.viewPagerCommunities.adapter = adapter
        binding.viewPagerCommunities.addOnPageChangeListener(
            TabLayout.TabLayoutOnPageChangeListener(binding.tabLayoutCommunities)
        )
        binding.tabLayoutCommunities.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                binding.viewPagerCommunities.currentItem = tab.position
                if (tab.position == 1 || tab.position == 2) {
                    binding.companionAdd.visibility = View.VISIBLE
                } else {
                    binding.companionAdd.visibility = View.GONE
                }
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
         * @return A new instance of fragment CommunityFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CommunityFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_Community"
    }
}