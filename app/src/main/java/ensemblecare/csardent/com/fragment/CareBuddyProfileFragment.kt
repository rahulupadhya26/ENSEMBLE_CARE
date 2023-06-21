package ensemblecare.csardent.com.fragment

import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import ensemblecare.csardent.com.BaseActivity
import ensemblecare.csardent.com.R
import ensemblecare.csardent.com.databinding.FragmentCareBuddyProfileBinding
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
 * Use the [CareBuddyProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CareBuddyProfileFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var selectedGender: String = "Prefer not to say"
    private var genderData: Array<String>? = null
    private lateinit var binding: FragmentCareBuddyProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_care_buddy_profile
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCareBuddyProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE

        onClickEvent()

        val firstName = preference!![PrefKeys.PREF_FNAME, ""]!!
        val lastName = preference!![PrefKeys.PREF_LNAME, ""]!!
        val email = preference!![PrefKeys.PREF_CARE_BUDDY_EMAIL, ""]!!
        val phoneNo = preference!![PrefKeys.PREF_PHONE_NO, ""]!!
        val address = preference!![PrefKeys.PREF_ADDRESS, ""]!!
        val address1 = preference!![PrefKeys.PREF_ADDRESS1, ""]!!
        val country = preference!![PrefKeys.PREF_COUNTRY, ""]!!
        val state = preference!![PrefKeys.PREF_STATE, ""]!!
        val city = preference!![PrefKeys.PREF_CITY, ""]!!
        val zipcode = preference!![PrefKeys.PREF_ZIPCODE, ""]!!
        val photo = preference!![PrefKeys.PREF_PHOTO, ""]!!

        binding.etCareBuddyProfileFname.setText(if (firstName == "null") "" else firstName)
        binding.etCareBuddyProfileLname.setText(if (lastName == "null") "" else lastName)
        genderSpinner()
        binding.etCareBuddyProfileMailId.text = if (email == "null") "" else email
        binding.etCareBuddyProfilePhoneNo.text = if (phoneNo == "null") "" else phoneNo
        binding.etCareBuddyProfileAddress.setText(if (address == "null") "" else address)
        binding.etCareBuddyProfileAddress2.setText(if (address1 == "null") "" else address1)
        binding.etCareBuddyProfileCountry.setText(if (country == "null") "" else country)
        binding.etCareBuddyProfileState.setText(if (state == "null") "" else state)
        binding.etCareBuddyProfileCity.setText(if (city == "null") "" else city)
        binding.etCareBuddyProfileZipcode.setText(if (zipcode == "null") "" else zipcode)

        if (photo != "null" && photo.isNotEmpty()) {
            binding.imgCareBuddyProfilePhoto.visibility = View.VISIBLE
            binding.txtCareBuddyProfilePhoto.visibility = View.GONE
            Glide.with(requireActivity())
                .load(BaseActivity.baseURL.dropLast(5) + photo)
                .placeholder(R.drawable.user_pic)
                .transform(CenterCrop(), RoundedCorners(5))
                .into(binding.imgCareBuddyProfilePhoto)
        } else {
            binding.imgCareBuddyProfilePhoto.visibility = View.GONE
            binding.txtCareBuddyProfilePhoto.visibility = View.VISIBLE
            if (firstName.isNotEmpty()) {
                binding.txtCareBuddyProfilePhoto.text = firstName.substring(0, 1).uppercase()
            }
        }
    }

    private fun onClickEvent() {
        binding.careBuddyProfileDetailBack.setOnClickListener {
            popBackStack()
        }

        binding.btnCareBuddyLogout.setOnClickListener {
            displayConfirmPopup()
        }
    }

    private fun genderSpinner() {
        if (preference!![PrefKeys.PREF_GENDER, ""]!!.isNotEmpty()) {
            Handler().postDelayed({
                if (binding.spinnerCareBuddyProfileGender != null)
                    binding.spinnerCareBuddyProfileGender.setText(preference!![PrefKeys.PREF_GENDER, ""]!!, false)
            }, 300)
        }
        genderData = resources.getStringArray(R.array.gender)
        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
            requireActivity(), R.layout.spinner_dropdown_custom_item, genderData!!
        )
        binding.spinnerCareBuddyProfileGender.setAdapter(adapter)
        binding.spinnerCareBuddyProfileGender.onItemClickListener =
            AdapterView.OnItemClickListener { parent, arg1, position, id ->
                //TODO: You can your own logic.
                selectedGender = genderData!![position]
            }

        binding.spinnerCareBuddyProfileGender.setOnClickListener {
            binding.spinnerCareBuddyProfileGender.showDropDown()
        }
    }

    private fun formatNumbersAsCode(s: CharSequence): String? {
        return String.format("%s-%s-%s", s.substring(0, 3), s.substring(3, 6), s.substring(6))
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CareBuddyProfileFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CareBuddyProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_Care_Buddy_Profile"
    }
}