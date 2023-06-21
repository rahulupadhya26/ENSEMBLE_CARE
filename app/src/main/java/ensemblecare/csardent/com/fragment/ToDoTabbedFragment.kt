package ensemblecare.csardent.com.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ensemblecare.csardent.com.R
import ensemblecare.csardent.com.adapters.ToDoTabAdapter
import ensemblecare.csardent.com.databinding.FragmentToDoTabbedBinding
import com.google.android.material.tabs.TabLayout

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ToDoTabbedFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ToDoTabbedFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentToDoTabbedBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_to_do_tabbed
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentToDoTabbedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE
        updateStatusBarColor(R.color.white)

        onClickEvents()

        val icons = intArrayOf(
            R.drawable.task_alt,
            R.drawable.assignment
        )

        binding.tabLayoutToDo.removeAllTabs()
        binding.viewPagerToDo.removeAllViewsInLayout()
        binding.tabLayoutToDo.addTab(
            binding.tabLayoutToDo.newTab().setText("Personal").setIcon(icons[0])
        )
        binding.tabLayoutToDo.addTab(
            binding.tabLayoutToDo.newTab().setText("Assigned").setIcon(icons[1])
        )
        binding.tabLayoutToDo.tabGravity = TabLayout.GRAVITY_FILL
        val adapter =
            ToDoTabAdapter(childFragmentManager, binding.tabLayoutToDo.tabCount)
        binding.viewPagerToDo.adapter = adapter
        binding.viewPagerToDo.addOnPageChangeListener(
            TabLayout.TabLayoutOnPageChangeListener(binding.tabLayoutToDo)
        )
        binding.tabLayoutToDo.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                binding.viewPagerToDo.currentItem = tab.position

            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun onClickEvents() {
        binding.imgToDoBack.setOnClickListener {
            setBottomNavigation(null)
            setLayoutBottomNavigation(null)
            replaceFragmentNoBackStack(
                BottomNavigationFragment(),
                R.id.layout_home,
                BottomNavigationFragment.TAG
            )
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ToDoTabbedFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ToDoTabbedFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_ToDo_Tabbed"
    }
}