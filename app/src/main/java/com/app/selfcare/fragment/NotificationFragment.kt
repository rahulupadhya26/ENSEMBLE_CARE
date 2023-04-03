package com.app.selfcare.fragment

import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.R
import com.app.selfcare.adapters.NotificationAdapter
import com.app.selfcare.data.NotificationData
import com.app.selfcare.databinding.FragmentActivityCarePlanBinding
import com.app.selfcare.databinding.FragmentNotificationBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [NotificationFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class NotificationFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentNotificationBinding

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
        binding = FragmentNotificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getLayout(): Int {
        return R.layout.fragment_notification
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE
        updateStatusBarColor(R.color.white)

        binding.notificationListBack.setOnClickListener {
            popBackStack()
        }

        val notificationList: ArrayList<NotificationData> = arrayListOf()
        notificationList.add(
            NotificationData(
                1,
                "19/12/2022",
                "2h",
                "Your appointment has been confirmed with Molly Berry",
                false
            )
        )

        notificationList.add(
            NotificationData(
                2,
                "19/12/2022",
                "5h",
                "You have been added to Teen Therapy",
                true
            )
        )

        notificationList.add(
            NotificationData(
                3,
                "19/12/2022",
                "6h",
                "Your appointment has been confirmed with Molly Berry",
                false
            )
        )

        notificationList.add(
            NotificationData(
                4,
                "19/12/2022",
                "8h",
                "You have been added to Teen Therapy",
                true
            )
        )

        binding.recyclerViewNotificationList.apply {
            layoutManager = LinearLayoutManager(
                requireActivity(), RecyclerView.VERTICAL, false
            )
            adapter = NotificationAdapter(
                mActivity!!,
                notificationList
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
         * @return A new instance of fragment NotificationFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            NotificationFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
        const val TAG = "Screen_Notifications"
    }
}