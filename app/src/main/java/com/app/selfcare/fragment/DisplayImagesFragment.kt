package com.app.selfcare.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.app.selfcare.R
import com.app.selfcare.adapters.ImageViewPagerAdapter
import com.app.selfcare.databinding.FragmentActivityCarePlanBinding
import com.app.selfcare.databinding.FragmentDisplayImagesBinding
import me.relex.circleindicator.CircleIndicator

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [DisplayImagesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DisplayImagesFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var imageList: ArrayList<String>? = null
    private var title: String? = null
    lateinit var viewPagerAdapter: ImageViewPagerAdapter
    lateinit var indicator: CircleIndicator
    private lateinit var binding: FragmentDisplayImagesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            imageList = it.getStringArrayList(ARG_PARAM1)
            title = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDisplayImagesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getLayout(): Int {
        return R.layout.fragment_display_images
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE
        updateStatusBarColor(R.color.white)

        binding.documentDisplayImageBack.setOnClickListener {
            popBackStack()
        }

        binding.txtDisplayImageTitle.text = title

        imageList!!.let {
            viewPagerAdapter = ImageViewPagerAdapter(requireContext(), it)
            binding.viewPager.adapter = viewPagerAdapter
            indicator = requireView().findViewById(R.id.displayImageIndicator) as CircleIndicator
            indicator.setViewPager(binding.viewPager)
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment DisplayImagesFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: ArrayList<String>, param2: String) =
            DisplayImagesFragment().apply {
                arguments = Bundle().apply {
                    putStringArrayList(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_display_images"
    }
}