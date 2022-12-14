package com.app.selfcare.fragment

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import com.app.selfcare.R
import kotlinx.android.synthetic.main.fragment_welcome.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [WelcomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class WelcomeFragment : BaseFragment() {
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
        return R.layout.fragment_welcome
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE
        getSubTitle().text = ""

        val rawId = resources.getIdentifier("splash_video", "raw", requireActivity().packageName)
        val video: Uri =
            Uri.parse("android.resource://" + requireActivity().packageName + "/" + rawId)

        imgSplashBackground.setVideoURI(video)
        imgSplashBackground.setOnPreparedListener { mp ->
            mp.isLooping = true
            imgSplashBackground.start()
        }

        txtLoginButton.setOnClickListener {
            replaceFragmentNoBackStack(
                LoginFragment(),
                R.id.layout_home,
                LoginFragment.TAG
            )
        }
        /*Glide.with(requireActivity())
            .load("https://drive.google.com/file/d/16paRRS2WNT8jdMLFlCGrsJLbz_fguYri/view?usp=share_link")
            .into(imgSplashBackground)*/
        btnSplash.setOnClickListener {
            replaceFragmentNoBackStack(
                CarouselFragment(),
                R.id.layout_home,
                CarouselFragment.TAG
            )
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    override fun onDetach() {
        super.onDetach()
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment LoginFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            WelcomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
        const val TAG = "Screen_Welcome"
    }
}