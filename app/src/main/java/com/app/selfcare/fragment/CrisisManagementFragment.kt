package com.app.selfcare.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import com.app.selfcare.BaseActivity
import com.app.selfcare.R
import com.app.selfcare.databinding.FragmentActivityCarePlanBinding
import com.app.selfcare.databinding.FragmentCrisisManagementBinding
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper.get
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CrisisManagementFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CrisisManagementFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentCrisisManagementBinding

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
        binding = FragmentCrisisManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getLayout(): Int {
        return R.layout.fragment_crisis_management
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE
        updateStatusBarColor(R.color.resource_background)

        binding.txtCrisisClientName.text = preference!![PrefKeys.PREF_FNAME, ""] + " " +
                preference!![PrefKeys.PREF_LNAME, ""]

        binding.txtCrisisViewProfile.setOnClickListener {
            replaceFragment(
                ProfileFragment(),
                R.id.layout_home,
                ProfileFragment.TAG
            )
        }

    }

    override fun onResume() {
        super.onResume()
        try {
            val photo = preference!![PrefKeys.PREF_PHOTO, ""]!!
            if (photo != "null" && photo.isNotEmpty()) {
                binding.imgCrisisClient.visibility = View.VISIBLE
                binding.txtCrisisUserPic.visibility = View.GONE
                Glide.with(requireActivity())
                    .load(BaseActivity.baseURL.dropLast(5) + photo)
                    .placeholder(R.drawable.user_pic)
                    .transform(CenterCrop(), RoundedCorners(5))
                    .into(binding.imgCrisisClient)
            } else {
                //img_user_pic.setImageResource(R.drawable.user_pic)
                binding.imgCrisisClient.visibility = View.GONE
                binding.txtCrisisUserPic.visibility = View.VISIBLE
                val userTxt = preference!![PrefKeys.PREF_FNAME, ""]!!
                if (userTxt.isNotEmpty()) {
                    binding.txtCrisisUserPic.text = userTxt.substring(0, 1).uppercase()
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
         * @return A new instance of fragment CrisisManagementFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CrisisManagementFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
        const val TAG = "Screen_Crisis_Management"
    }
}