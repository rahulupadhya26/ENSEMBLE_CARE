package com.app.selfcare.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.app.selfcare.R
import kotlinx.android.synthetic.main.fragment_intro_screens.*
import kotlinx.android.synthetic.main.fragment_intro_screens.view.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [IntroScreensFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class IntroScreensFragment : BaseFragment() {
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
        return R.layout.fragment_intro_screens
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        when (param1) {
            "0" -> {
                view.txt_subtext.text = "Journey towards sobriety starts here"
                view.img_intro.setImageResource(R.drawable.img_intro_1)
            }
            "1" -> {
                view.txt_subtext.text = "Take that extra mile and you will be the winner"
                view.img_intro.setImageResource(R.drawable.img_intro_2)
            }

        }

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment IntroScreensFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            IntroScreensFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
