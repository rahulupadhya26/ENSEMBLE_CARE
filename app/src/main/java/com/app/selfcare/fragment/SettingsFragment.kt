package com.app.selfcare.fragment

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.annotation.RequiresApi
import com.app.selfcare.BaseActivity
import com.app.selfcare.R
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper.get
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import kotlinx.android.synthetic.main.fragment_settings.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SettingsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SettingsFragment : BaseFragment() {
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
        return R.layout.fragment_settings
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE
        updateStatusBarColor(R.color.white)

        txtSettingsUserName.text = preference!![PrefKeys.PREF_FNAME, ""] + " " +
                preference!![PrefKeys.PREF_LNAME, ""]

        txtSettingsEmailId.text = preference!![PrefKeys.PREF_EMAIL, ""]

        onClickEvents()

    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun onClickEvents() {
        settingsBack.setOnClickListener {
            popBackStack()
        }

        layoutAccountSettings.setOnClickListener {
            replaceFragment(
                ProfileFragment(),
                R.id.layout_home,
                ProfileFragment.TAG
            )
        }

        layoutPlanMethod.setOnClickListener {
            replaceFragment(
                PlanSettingsFragment(),
                R.id.layout_home,
                PlanSettingsFragment.TAG
            )
        }

        layoutNotificationSetting.setOnClickListener {
            replaceFragment(
                NotificationSettingsFragment(),
                R.id.layout_home,
                NotificationSettingsFragment.TAG
            )
        }

        txtLogout.setOnClickListener {
            displayConfirmPopup()
        }

        imgUser.setOnClickListener {
            showImage(imgUser)
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            val photo = preference!![PrefKeys.PREF_PHOTO, ""]!!
            if (photo.isNotEmpty()) {
                Glide.with(requireActivity())
                    .load(BaseActivity.baseURL.dropLast(5) + photo)
                    .placeholder(R.drawable.user_pic)
                    .transform(CenterCrop(), RoundedCorners(5))
                    .into(imgUser)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SettingsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SettingsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_settings"
    }
}