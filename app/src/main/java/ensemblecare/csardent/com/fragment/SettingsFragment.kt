package ensemblecare.csardent.com.fragment

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import ensemblecare.csardent.com.BaseActivity
import ensemblecare.csardent.com.R
import ensemblecare.csardent.com.databinding.FragmentSettingsBinding
import ensemblecare.csardent.com.preference.PrefKeys
import ensemblecare.csardent.com.preference.PreferenceHelper.get
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

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
    private lateinit var binding: FragmentSettingsBinding

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
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
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

        binding.txtSettingsUserName.text = preference!![PrefKeys.PREF_FNAME, ""] + " " +
                preference!![PrefKeys.PREF_LNAME, ""]

        binding.txtSettingsEmailId.text = preference!![PrefKeys.PREF_EMAIL, ""]

        onClickEvents()

    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun onClickEvents() {
        binding.settingsBack.setOnClickListener {
            setBottomNavigation(null)
            setLayoutBottomNavigation(null)
            replaceFragmentNoBackStack(
                BottomNavigationFragment(),
                R.id.layout_home,
                BottomNavigationFragment.TAG
            )
        }

        binding.layoutAccountSettings.setOnClickListener {
            replaceFragment(
                ProfileFragment(),
                R.id.layout_home,
                ProfileFragment.TAG
            )
        }

        binding.layoutPlanMethod.setOnClickListener {
            replaceFragment(
                PlanSettingsFragment(),
                R.id.layout_home,
                PlanSettingsFragment.TAG
            )
        }

        binding.layoutNotificationSetting.setOnClickListener {
            replaceFragment(
                NotificationSettingsFragment(),
                R.id.layout_home,
                NotificationSettingsFragment.TAG
            )
        }

        binding.txtLogout.setOnClickListener {
            displayConfirmPopup()
        }

        /*imgUser.setOnClickListener {
            showImage(imgUser)
        }*/
    }

    override fun onResume() {
        super.onResume()
        try {
            val photo = preference!![PrefKeys.PREF_PHOTO, ""]!!
            if (photo != "null" && photo.isNotEmpty()) {
                binding.imgUser.visibility = View.VISIBLE
                binding.txtUser.visibility = View.GONE
                Glide.with(requireActivity())
                    .load(BaseActivity.baseURL.dropLast(5) + photo)
                    .placeholder(R.drawable.user_pic)
                    .transform(CenterCrop(), RoundedCorners(5))
                    .into(binding.imgUser)
            } else {
                binding.imgUser.visibility = View.GONE
                binding.txtUser.visibility = View.VISIBLE
                val userTxt = preference!![PrefKeys.PREF_FNAME, ""]!!
                if (userTxt.isNotEmpty()) {
                    binding.txtUser.text = userTxt.substring(0, 1).uppercase()
                }
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