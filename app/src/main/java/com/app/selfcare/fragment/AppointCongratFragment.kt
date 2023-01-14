package com.app.selfcare.fragment

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.core.content.ContextCompat
import com.app.selfcare.BaseActivity
import com.app.selfcare.R
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper.get
import com.app.selfcare.utils.DateUtils
import com.app.selfcare.utils.Utils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import kotlinx.android.synthetic.main.fragment_appoint_congrat.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AppointCongratFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AppointCongratFragment : BaseFragment() {
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
        return R.layout.fragment_appoint_congrat
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE

        try {
            text_final_fname.text =
                "Hi, " + preference!![PrefKeys.PREF_FNAME, ""]!! + " " +
                        preference!![PrefKeys.PREF_LNAME, ""]!!

            txtAppointedTherapistName.text = Utils.providerName
            txtAppointedTherapistType.text = Utils.providerType

            val appointmentDate = DateUtils(Utils.aptScheduleDate + " 00:00:00")

            text_appointment_date_time.text = appointmentDate.getDay() + " " +
                    appointmentDate.getFullMonthName() + " at " + Utils.aptScheduleTime.dropLast(3)

            Glide.with(requireActivity())
                .load(BaseActivity.baseURL.dropLast(5) + Utils.providerPhoto)
                .placeholder(R.drawable.doctor_img)
                .transform(CenterCrop(), RoundedCorners(5))
                .into(imgAppointCongrats)

            if (Utils.selectedCommunicationMode == "Video") {
                appointedMode.setBackgroundResource(R.drawable.video)
                appointedMode.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireActivity(), R.color.primaryGreen))
            } else {
                appointedMode.setBackgroundResource(R.drawable.telephone)
                appointedMode.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireActivity(), R.color.primaryGreen))
            }

            layoutAppointCongratsScreenshot.setOnClickListener {
                takeScreenshot()
            }

            btn_go_to_dashboard.setOnClickListener {
                navigateToHomeScreen()
                /*replaceFragmentNoBackStack(
                    BottomNavigationFragment(),
                    R.id.layout_home,
                    BottomNavigationFragment.TAG
                )*/
            }
        } catch (e: Exception) {
            e.printStackTrace()
            navigateToHomeScreen()
            /*replaceFragmentNoBackStack(
                BottomNavigationFragment(),
                R.id.layout_home,
                BottomNavigationFragment.TAG
            )*/
        }
    }

    private fun navigateToHomeScreen() {
        /*if(Utils.isTherapististScreen){

        }else {
            for (i in 0 until mActivity!!.supportFragmentManager.backStackEntryCount) {
                if (mActivity!!.getCurrentFragment() !is DashboardFragment) {
                    popBackStack()
                } else {
                    break
                }
            }
        }*/
        setBottomNavigation(null)
        setLayoutBottomNavigation(null)
        replaceFragmentNoBackStack(
            BottomNavigationFragment(),
            R.id.layout_home,
            BottomNavigationFragment.TAG
        )
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AppointCongratFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AppointCongratFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_Therapy_Congrats"
    }
}