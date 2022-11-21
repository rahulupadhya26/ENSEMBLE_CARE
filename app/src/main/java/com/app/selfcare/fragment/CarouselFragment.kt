package com.app.selfcare.fragment

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.app.selfcare.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.dialog_check_teen_dob.view.*
import kotlinx.android.synthetic.main.fragment_carousel.*
import java.text.SimpleDateFormat
import java.util.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CarouselFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CarouselFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var sliderHandler = Handler()
    private var therapySel: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_carousel
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE
        getSubTitle().text = ""
        updateStatusBarColor(R.color.initial_screen_background)

        cardViewSelf.setOnClickListener {
            relativeLayoutSelf.setBackgroundResource(R.drawable.bg_round_primary)
            relativeLayoutTeen.setBackgroundResource(R.drawable.bg_round_gray)
            relativeLayoutCouple.setBackgroundResource(R.drawable.bg_round_gray)
            relativeLayoutLgbtq.setBackgroundResource(R.drawable.bg_round_gray)
            imgSelf.imageTintList =
                ColorStateList.valueOf(ContextCompat.getColor(requireActivity(), R.color.white))
            imgTeen.imageTintList =
                ColorStateList.valueOf(ContextCompat.getColor(requireActivity(), R.color.black))
            imgCouple.imageTintList =
                ColorStateList.valueOf(ContextCompat.getColor(requireActivity(), R.color.black))
            imgLgbtq.imageTintList =
                ColorStateList.valueOf(ContextCompat.getColor(requireActivity(), R.color.black))
            therapySel = "Individual"
            getConfirmation(therapySel, txtTherapySelf.text.toString())
        }

        cardViewTeen.setOnClickListener {
            relativeLayoutSelf.setBackgroundResource(R.drawable.bg_round_gray)
            relativeLayoutTeen.setBackgroundResource(R.drawable.bg_round_primary)
            relativeLayoutCouple.setBackgroundResource(R.drawable.bg_round_gray)
            relativeLayoutLgbtq.setBackgroundResource(R.drawable.bg_round_gray)
            imgSelf.imageTintList =
                ColorStateList.valueOf(ContextCompat.getColor(requireActivity(), R.color.black))
            imgTeen.imageTintList =
                ColorStateList.valueOf(ContextCompat.getColor(requireActivity(), R.color.white))
            imgCouple.imageTintList =
                ColorStateList.valueOf(ContextCompat.getColor(requireActivity(), R.color.black))
            imgLgbtq.imageTintList =
                ColorStateList.valueOf(ContextCompat.getColor(requireActivity(), R.color.black))
            therapySel = "Teen"
            getConfirmation(therapySel, txtTherapyTeen.text.toString())
        }

        cardViewCouple.setOnClickListener {
            relativeLayoutSelf.setBackgroundResource(R.drawable.bg_round_gray)
            relativeLayoutTeen.setBackgroundResource(R.drawable.bg_round_gray)
            relativeLayoutCouple.setBackgroundResource(R.drawable.bg_round_primary)
            relativeLayoutLgbtq.setBackgroundResource(R.drawable.bg_round_gray)
            imgSelf.imageTintList =
                ColorStateList.valueOf(ContextCompat.getColor(requireActivity(), R.color.black))
            imgTeen.imageTintList =
                ColorStateList.valueOf(ContextCompat.getColor(requireActivity(), R.color.black))
            imgCouple.imageTintList =
                ColorStateList.valueOf(ContextCompat.getColor(requireActivity(), R.color.white))
            imgLgbtq.imageTintList =
                ColorStateList.valueOf(ContextCompat.getColor(requireActivity(), R.color.black))
            therapySel = "Couple"
            getConfirmation(therapySel, txtTherapyCouple.text.toString())
        }

        cardViewLgbtq.setOnClickListener {
            relativeLayoutSelf.setBackgroundResource(R.drawable.bg_round_gray)
            relativeLayoutTeen.setBackgroundResource(R.drawable.bg_round_gray)
            relativeLayoutCouple.setBackgroundResource(R.drawable.bg_round_gray)
            relativeLayoutLgbtq.setBackgroundResource(R.drawable.bg_round_primary)
            imgSelf.imageTintList =
                ColorStateList.valueOf(ContextCompat.getColor(requireActivity(), R.color.black))
            imgTeen.imageTintList =
                ColorStateList.valueOf(ContextCompat.getColor(requireActivity(), R.color.black))
            imgCouple.imageTintList =
                ColorStateList.valueOf(ContextCompat.getColor(requireActivity(), R.color.black))
            imgLgbtq.imageTintList =
                ColorStateList.valueOf(ContextCompat.getColor(requireActivity(), R.color.white))
            therapySel = "LGBTQ"
            getConfirmation(therapySel, txtTherapyLgbtqia.text.toString())
        }

        btnTherapySelect.setOnClickListener {
            if (therapySel.isNotEmpty()) {
                getConfirmation(therapySel,"")
            }
        }

        /*val therapyTypeList: ArrayList<TherapyType> = ArrayList()
        therapyTypeList.add(TherapyType("Child", R.drawable.child))
        therapyTypeList.add(TherapyType("Teen", R.drawable.teen))
        therapyTypeList.add(TherapyType("Adult", R.drawable.adult))
        therapyTypeList.add(TherapyType("Couple", R.drawable.couple))
        therapyTypeList.add(TherapyType("Lgbtq", R.drawable.lgbtq))

        val staggeredGridLayoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        staggeredGridLayoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_NONE
        recyclerViewTherapyTypeList.layoutManager = staggeredGridLayoutManager
        recyclerViewTherapyTypeList.setHasFixedSize(true)
        recyclerViewTherapyTypeList.adapter =
            TherapyTypeListAdapter(mActivity!!, therapyTypeList, this)*/

        /*viewPagerTherapySlider.clipToPadding = false
        viewPagerTherapySlider.clipChildren = false
        viewPagerTherapySlider.offscreenPageLimit = 3
        viewPagerTherapySlider.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER

        val compositePageTransform = CompositePageTransformer()
        compositePageTransform.addTransformer(MarginPageTransformer(40))
        compositePageTransform.addTransformer { page, position ->
            val r: Float = 1 - abs(position)
            page.scaleY = 0.85f + r * 0.15f
        }
        viewPagerTherapySlider.setPageTransformer(compositePageTransform)

        viewPagerTherapySlider.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                sliderHandler.removeCallbacks(sliderRunnable)
                sliderHandler.postDelayed(sliderRunnable, 5000)
            }
        })*/
    }

    @SuppressLint("HardwareIds")
    private fun sendDeviceId(selectedTherapy: String, actualTherapyTxt:String) {
        /*if (selectedTherapy == "Teen") {
            checkTeenDob(selectedTherapy)
        } else {*/
        //createAnonymousUser(selectedTherapy, "", "", "", "")
        //}
    }

    private fun checkTeenDob(selectedTherapy: String) {
        val createPostDialog = BottomSheetDialog(mContext!!)
        val checkTeenDob = requireActivity().layoutInflater.inflate(
            R.layout.dialog_check_teen_dob, null
        )
        //onlineChatView!!.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        createPostDialog.setContentView(checkTeenDob!!)
        createPostDialog.setCanceledOnTouchOutside(false)

        checkTeenDob.txtVerify.setOnClickListener {
            if (getAge(checkTeenDob.txtTeenDob.text.toString()) in 13..17) {
                createPostDialog.dismiss()
                //checkApplicationUser(selectedTherapy)
            } else {
                displayMsg(
                    "Alert",
                    "Age must be greater than 12 years and less than 18 years."
                )
            }
        }

        checkTeenDob.txtCancel.setOnClickListener {
            createPostDialog.dismiss()
        }
        val cal = Calendar.getInstance()
        //cal.add(Calendar.YEAR, -13)
        val dateSetListener =
            DatePickerDialog.OnDateSetListener { views, year, monthOfYear, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                val myFormat = "MM/dd/yyyy" // mention the format you need
                val sdf = SimpleDateFormat(myFormat)
                checkTeenDob.txtTeenDob.setText(sdf.format(cal.time))
            }

        checkTeenDob.txtTeenDob.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                mActivity!!, dateSetListener,
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            )
            //datePickerDialog.datePicker.maxDate = cal.timeInMillis
            datePickerDialog.show()
        }
        createPostDialog.show()
    }

    private fun getConfirmation(selectedTherapy: String, actualTherapyTxt:String) {
        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle("Confirmation")
        builder.setMessage("Selected therapy is $actualTherapyTxt")
        builder.setPositiveButton("Confirm") { dialog, _ ->
            dialog.dismiss()
            createAnonymousUser(selectedTherapy, "", "", "", "")
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    private val sliderRunnable: Runnable = Runnable {
        viewPagerTherapySlider.currentItem = viewPagerTherapySlider.currentItem + 1
    }

    override fun onPause() {
        super.onPause()
        sliderHandler.removeCallbacks(sliderRunnable)
    }

    override fun onResume() {
        super.onResume()
        sliderHandler.postDelayed(sliderRunnable, 3000)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CarouselFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CarouselFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_Therapy_Select"
    }
}