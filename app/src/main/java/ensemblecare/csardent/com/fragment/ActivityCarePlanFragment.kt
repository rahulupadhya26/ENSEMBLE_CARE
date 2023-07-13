package ensemblecare.csardent.com.fragment

import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import ensemblecare.csardent.com.R
import ensemblecare.csardent.com.adapters.ActivityCarePlanAdapter
import ensemblecare.csardent.com.databinding.FragmentActivityCarePlanBinding
import com.google.android.material.tabs.TabLayout

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ActivityCarePlanFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ActivityCarePlanFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentActivityCarePlanBinding

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
        binding = FragmentActivityCarePlanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getLayout(): Int {
        return R.layout.fragment_activity_care_plan
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE

        binding.tabLayoutActivityCarePlan.removeAllTabs()
        binding.viewPagerActivityCarePlan.removeAllViewsInLayout()
        binding.tabLayoutActivityCarePlan.addTab(binding.tabLayoutActivityCarePlan.newTab().setText("Activity"))
        binding.tabLayoutActivityCarePlan.addTab(binding.tabLayoutActivityCarePlan.newTab().setText("Care Plan"))
        binding.tabLayoutActivityCarePlan.tabGravity = TabLayout.GRAVITY_FILL
        val adapter = ActivityCarePlanAdapter(
            requireActivity(), childFragmentManager,
            binding.tabLayoutActivityCarePlan.tabCount
        )
        binding.viewPagerActivityCarePlan.adapter = adapter
        binding.viewPagerActivityCarePlan.addOnPageChangeListener(
            TabLayout.TabLayoutOnPageChangeListener(
                binding.tabLayoutActivityCarePlan
            )
        )
        binding.tabLayoutActivityCarePlan.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                binding.viewPagerActivityCarePlan.currentItem = tab.position
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
         * @return A new instance of fragment ActivityCarePlanFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ActivityCarePlanFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_activity_care_plan"
    }
}