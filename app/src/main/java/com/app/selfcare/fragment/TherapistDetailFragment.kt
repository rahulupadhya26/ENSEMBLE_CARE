package com.app.selfcare.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.view.ViewTreeObserver
import androidx.fragment.app.Fragment
import com.app.selfcare.BaseActivity
import com.app.selfcare.R
import com.app.selfcare.data.Therapist
import com.app.selfcare.utils.Utils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import kotlinx.android.synthetic.main.fragment_therapist_detail.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [TherapistDetailFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TherapistDetailFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var therapist: Therapist? = null
    private var param2: String? = null
    private var descriptionLineCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            therapist = it.getParcelable(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_therapist_detail
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE
        getSubTitle().text = ""
        updateStatusBarColor(R.color.white)

        txtTherapistName.text =
            therapist!!.first_name + " " + therapist!!.middle_name + " " + therapist!!.last_name
        therapistType.text = therapist!!.doctor_type
        txtTherapyDescription.text = therapist!!.description

        Glide.with(requireActivity())
            .load(BaseActivity.baseURL.dropLast(5) + therapist!!.photo)
            .placeholder(R.drawable.doctor_img)
            .transform(CenterCrop(), RoundedCorners(5))
            .into(imgTherapistDetail)

        img_back.setOnClickListener {
            popBackStack()
        }

        displayRating(therapist!!.ratings)

        // Invoking touch listener to detect movement of ScrollView
        //scrollTherapyDescription.setOnTouchListener(this)
        //scrollTherapyDescription.viewTreeObserver.addOnScrollChangedListener(this)

        txtTherapyDescription.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                txtTherapyDescription.viewTreeObserver.removeOnGlobalLayoutListener(this);
                descriptionLineCount = txtTherapyDescription.layout.lineCount
                if (descriptionLineCount < 10) {
                    txtReadMore.visibility = View.GONE
                } else {
                    txtReadMore.visibility = View.VISIBLE
                }
            }
        })


        txtReadMore.setOnClickListener {
            txtReadMore.visibility = View.GONE
            txtTherapyDescription.movementMethod = ScrollingMovementMethod()
        }

        btnConfirmDoctorDetail.setOnClickListener {
            if (Utils.providerId.isNotEmpty() &&
                Utils.providerType.isNotEmpty() &&
                Utils.providerName.isNotEmpty()
            ) {
                replaceFragment(
                    TherapyBasicDetailsCFragment.newInstance(false),
                    R.id.layout_home,
                    TherapyBasicDetailsCFragment.TAG
                )
            }
        }
    }

    private fun displayRating(rating: String) {
        when (rating) {
            "1" -> {
                detailFilledStar1.visibility = View.VISIBLE
                detailEmptyStar1.visibility = View.VISIBLE
                detailEmptyStar2.visibility = View.VISIBLE
                detailEmptyStar3.visibility = View.VISIBLE
                detailEmptyStar4.visibility = View.VISIBLE

                detailFilledStar2.visibility = View.GONE
                detailFilledStar3.visibility = View.GONE
                detailFilledStar4.visibility = View.GONE
                detailFilledStar5.visibility = View.GONE
                detailEmptyStar5.visibility = View.GONE
            }
            "2" -> {
                detailFilledStar1.visibility = View.VISIBLE
                detailFilledStar2.visibility = View.VISIBLE
                detailEmptyStar1.visibility = View.VISIBLE
                detailEmptyStar2.visibility = View.VISIBLE
                detailEmptyStar3.visibility = View.VISIBLE

                detailFilledStar3.visibility = View.GONE
                detailFilledStar4.visibility = View.GONE
                detailFilledStar5.visibility = View.GONE
                detailEmptyStar4.visibility = View.GONE
                detailEmptyStar5.visibility = View.GONE
            }
            "3" -> {
                detailFilledStar1.visibility = View.VISIBLE
                detailFilledStar2.visibility = View.VISIBLE
                detailFilledStar3.visibility = View.VISIBLE
                detailEmptyStar1.visibility = View.VISIBLE
                detailEmptyStar2.visibility = View.VISIBLE

                detailFilledStar4.visibility = View.GONE
                detailFilledStar5.visibility = View.GONE
                detailEmptyStar3.visibility = View.GONE
                detailEmptyStar4.visibility = View.GONE
                detailEmptyStar5.visibility = View.GONE
            }
            "4" -> {
                detailFilledStar1.visibility = View.VISIBLE
                detailFilledStar2.visibility = View.VISIBLE
                detailFilledStar3.visibility = View.VISIBLE
                detailFilledStar4.visibility = View.VISIBLE
                detailEmptyStar1.visibility = View.VISIBLE

                detailFilledStar5.visibility = View.GONE
                detailEmptyStar2.visibility = View.GONE
                detailEmptyStar3.visibility = View.GONE
                detailEmptyStar4.visibility = View.GONE
                detailEmptyStar5.visibility = View.GONE
            }
            "5" -> {
                detailFilledStar1.visibility = View.VISIBLE
                detailFilledStar2.visibility = View.VISIBLE
                detailFilledStar3.visibility = View.VISIBLE
                detailFilledStar4.visibility = View.VISIBLE
                detailFilledStar5.visibility = View.VISIBLE

                detailEmptyStar1.visibility = View.GONE
                detailEmptyStar2.visibility = View.GONE
                detailEmptyStar3.visibility = View.GONE
                detailEmptyStar4.visibility = View.GONE
                detailEmptyStar5.visibility = View.GONE
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
         * @return A new instance of fragment TherapistDetailFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: Therapist, param2: String = "") =
            TherapistDetailFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_Therapy_Detail"
    }
}