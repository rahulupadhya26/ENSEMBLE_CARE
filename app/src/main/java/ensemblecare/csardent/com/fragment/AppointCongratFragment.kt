package ensemblecare.csardent.com.fragment

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import ensemblecare.csardent.com.BaseActivity
import ensemblecare.csardent.com.R
import ensemblecare.csardent.com.databinding.FragmentAppointCongratBinding
import ensemblecare.csardent.com.preference.PrefKeys
import ensemblecare.csardent.com.preference.PreferenceHelper.get
import ensemblecare.csardent.com.utils.DateUtils
import ensemblecare.csardent.com.utils.Utils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

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
    private lateinit var binding: FragmentAppointCongratBinding

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
        binding = FragmentAppointCongratBinding.inflate(inflater, container, false)
        return binding.root
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
            binding.textFinalFname.text =
                "Hi, " + preference!![PrefKeys.PREF_FNAME, ""]!! + " " +
                        preference!![PrefKeys.PREF_LNAME, ""]!!

            binding.txtAppointedTherapistName.text = Utils.providerName
            binding.txtAppointedTherapistType.text = Utils.providerType

            val appointmentDate = DateUtils(Utils.aptScheduleDate + " 00:00:00")

            binding.textAppointmentDateTime.text = appointmentDate.getDay() + " " +
                    appointmentDate.getFullMonthName() + " at " + Utils.aptScheduleTime.dropLast(3)

            Glide.with(requireActivity())
                .load(BaseActivity.baseURL.dropLast(5) + Utils.providerPhoto)
                .placeholder(R.drawable.doctor_img)
                .transform(CenterCrop(), RoundedCorners(5))
                .into(binding.imgAppointCongrats)

            when (Utils.selectedCommunicationMode) {
                "Video" -> {
                    binding.appointedMode.setBackgroundResource(R.drawable.video)
                    binding.appointedMode.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireActivity(), R.color.primaryGreen))
                }
                "Audio" -> {
                    binding.appointedMode.setBackgroundResource(R.drawable.telephone)
                    binding.appointedMode.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireActivity(), R.color.primaryGreen))
                }
                else -> {
                    binding.appointedMode.setBackgroundResource(R.drawable.chat)
                    binding.appointedMode.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireActivity(), R.color.primaryGreen))
                }
            }

            binding.layoutAppointCongratsScreenshot.setOnClickListener {
                takeScreenshot()
            }

            binding.btnGoToDashboard.setOnClickListener {
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