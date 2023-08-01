package ensemblecare.csardent.com.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import ensemblecare.csardent.com.R
import ensemblecare.csardent.com.databinding.FragmentCongratsUserBinding
import ensemblecare.csardent.com.preference.PrefKeys
import ensemblecare.csardent.com.preference.PreferenceHelper.set
import ensemblecare.csardent.com.utils.Utils
import java.text.DecimalFormat
import java.text.NumberFormat

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CongratsUserFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CongratsUserFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var counter: CountDownTimer? = null
    private lateinit var binding: FragmentCongratsUserBinding

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
        binding = FragmentCongratsUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getLayout(): Int {
        return R.layout.fragment_congrats_user
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE
        getSubTitle().text = ""
        updateStatusBarColor(R.color.initial_screen_background)

        //Glide.with(requireActivity()).load(R.drawable.thankyouanim).into(imgThankYou);

        /*Handler().postDelayed({

        }, 3000)*/

        /*btn_thanks_continue.setOnClickListener {
            replaceFragmentNoBackStack(RegistrationFragment(), R.id.layout_home, RegistrationFragment.TAG)
        }*/

        counter = object : CountDownTimer(3000, 1000) {
            // Callback function, fired on regular interval
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                val f: NumberFormat = DecimalFormat("00")
                val min = millisUntilFinished / 60000 % 60
                val sec = millisUntilFinished / 1000 % 60

                binding.txtTimer.text = "continuing in " + f.format(sec) + " sec..."
            }

            // Callback function, fired
            // when the time is up
            override fun onFinish() {
                preference!![PrefKeys.PREF_GENDER] = ""
                preference!![PrefKeys.PREF_RELATIONSHIP] = ""
                preference!![PrefKeys.PREF_PREFERRED_LANG] = ""
                preference!![PrefKeys.PREF_ROLE] = ""
                preference!![PrefKeys.PREF_REG] = ""
                Utils.dob = ""
                replaceFragmentNoBackStack(
                    RegistrationFragment(),
                    R.id.layout_home,
                    RegistrationFragment.TAG
                )
            }
        }.start()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CongratsUserFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CongratsUserFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_Congrats_user"
    }
}