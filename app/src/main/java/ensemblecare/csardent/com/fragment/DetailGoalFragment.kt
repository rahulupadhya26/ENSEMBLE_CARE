package ensemblecare.csardent.com.fragment

import android.os.Bundle
import android.text.Html
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import ensemblecare.csardent.com.R
import ensemblecare.csardent.com.data.Goal
import ensemblecare.csardent.com.databinding.FragmentDetailGoalBinding
import ensemblecare.csardent.com.utils.DateUtils

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [DetailGoalFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DetailGoalFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var goal: Goal? = null
    private var param2: String? = null
    private lateinit var binding: FragmentDetailGoalBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            goal = it.getParcelable(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDetailGoalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getLayout(): Int {
        return R.layout.fragment_detail_goal
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.VISIBLE
        getSubTitle().visibility = View.GONE

        binding.txtGoalDesc.movementMethod = ScrollingMovementMethod()
        val goalDate = DateUtils(goal!!.start_date + " 01:00:00")
        binding.txtGoalDate.text =
            Html.fromHtml("<b>Date</b> : " + goalDate.getDay() + " " + goalDate.getMonth() + " " + goalDate.getYear())
        binding.txtGoalTitle.text = Html.fromHtml("<b>" + goal!!.title + "</b>")
        var durationTxt = ""
        when (goal!!.duration) {
            0 -> durationTxt = "Does not repeat"
            1 -> durationTxt = "Everyday"
            2 -> durationTxt = "Every week"
            3 -> durationTxt = "Every month"
            4 -> durationTxt = "Every year"
        }
        binding.txtGoalDuration.text = Html.fromHtml("<b>Duration</b> : $durationTxt")
        binding.txtGoalDesc.text = Html.fromHtml("<b>Description</b> : " + goal!!.description)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment DetailGoalFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(goal: Goal, param2: String = "") =
            DetailGoalFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM1, goal)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_detailed_goal"
    }
}