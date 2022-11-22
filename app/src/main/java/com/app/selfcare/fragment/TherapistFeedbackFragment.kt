package com.app.selfcare.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import com.app.selfcare.R
import kotlinx.android.synthetic.main.fragment_therapist_feedback.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [TherapistFeedbackFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TherapistFeedbackFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var apptId: String? = null
    private var param2: String? = null
    private var therapistFeedbackRating: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            apptId = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_therapist_feedback
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE

        ratingBarTherapistRating.onRatingBarChangeListener =
            RatingBar.OnRatingBarChangeListener { p0, p1, p2 ->
                therapistFeedbackRating = p1.toString()
            }

        btnTherapistFeedback.setOnClickListener {
            if (therapistFeedbackRating != null) {
                if (getText(editTxtTherapistFeedback).isNotEmpty()) {
                    replaceFragment(
                        ServiceFeedbackFragment.newInstance(
                            therapistFeedbackRating!!,
                            getText(editTxtTherapistFeedback),
                            apptId!!
                        ),
                        R.id.layout_home,
                        ServiceFeedbackFragment.TAG
                    )
                } else {
                    displayMsg("Alert", "Please provide feedback")
                }
            } else {
                displayMsg("Alert", "Please give rating")
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
         * @return A new instance of fragment TherapistFeedbackFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String = "") =
            TherapistFeedbackFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_therapist_feedback"
    }
}