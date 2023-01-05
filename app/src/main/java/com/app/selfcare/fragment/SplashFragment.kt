package com.app.selfcare.fragment


import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.app.selfcare.MainActivity
import com.app.selfcare.R
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper.get
import com.app.selfcare.utils.Utils
import kotlinx.android.synthetic.main.fragment_splash.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SplashFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SplashFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun getLayout(): Int {
        return R.layout.fragment_splash
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE

        Handler().postDelayed(Runnable { // This method will be executed once the timer is over
            if (preference!![PrefKeys.PREF_USER_ID, ""]!!.isNotEmpty()) {
                if (preference!![PrefKeys.PREF_IS_LOGGEDIN, false]!!) {
                    /*if (preference!![PrefKeys.PREF_STEP, 0]!! == Utils.REGISTER) {
                        replaceFragmentNoBackStack(
                            RegisterPartCFragment(),
                            R.id.layout_home,
                            RegisterPartCFragment.TAG
                        )
                    } else {*/
                    replaceFragmentNoBackStack(
                        BottomNavigationFragment(),
                        R.id.layout_home,
                        BottomNavigationFragment.TAG
                    )
                    //}
                } else {
                    replaceFragmentNoBackStack(
                        WelcomeFragment(),
                        R.id.layout_home,
                        WelcomeFragment.TAG
                    )
                }
            } else {
                replaceFragmentNoBackStack(WelcomeFragment(), R.id.layout_home, WelcomeFragment.TAG)
            }
        }, 1000)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SplashFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SplashFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_Splash"
    }
}
