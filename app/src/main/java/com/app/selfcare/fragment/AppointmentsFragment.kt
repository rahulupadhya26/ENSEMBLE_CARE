package com.app.selfcare.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.GroupVideoCall
import com.app.selfcare.R
import com.app.selfcare.adapters.ActivityCarePlanAdapter
import com.app.selfcare.adapters.AppointmentTabAdapter
import com.app.selfcare.adapters.AppointmentsAdapter
import com.app.selfcare.controller.OnAppointmentItemClickListener
import com.app.selfcare.data.*
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper.get
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_appointments.*
import org.json.JSONObject
import retrofit2.HttpException
import java.lang.reflect.Type
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AppointmentsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AppointmentsFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_appointments
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE
        updateStatusBarColor(R.color.white)

        appointmentsBack.setOnClickListener {
            popBackStack()
        }

        tabLayoutAppointmentList.removeAllTabs()
        viewPagerAppointmentList.removeAllViewsInLayout()
        tabLayoutAppointmentList.addTab(tabLayoutAppointmentList.newTab().setText("Today"))
        tabLayoutAppointmentList.addTab(tabLayoutAppointmentList.newTab().setText("Upcoming"))
        tabLayoutAppointmentList.addTab(tabLayoutAppointmentList.newTab().setText("Past"))
        tabLayoutAppointmentList.tabGravity = TabLayout.GRAVITY_FILL
        val adapter = AppointmentTabAdapter(
            requireActivity(), childFragmentManager,
            tabLayoutAppointmentList.tabCount
        )
        viewPagerAppointmentList.adapter = adapter
        viewPagerAppointmentList.addOnPageChangeListener(
            TabLayout.TabLayoutOnPageChangeListener(
                tabLayoutAppointmentList
            )
        )
        tabLayoutAppointmentList.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPagerAppointmentList.currentItem = tab.position
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
         * @return A new instance of fragment AppointmentsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AppointmentsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_Appointments"
    }
}