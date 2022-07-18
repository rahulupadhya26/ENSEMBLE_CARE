package com.app.selfcare.fragment

import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.R
import com.app.selfcare.adapters.TimeSlotAdapter
import com.app.selfcare.controller.OnTextClickListener
import com.app.selfcare.data.PatientId
import com.app.selfcare.data.Question
import com.app.selfcare.data.TimeSlot
import com.app.selfcare.data.TimeSlots
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper.get
import com.app.selfcare.utils.Utils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_therapy_selection.*
import java.lang.reflect.Type
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [TherapySelectionFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TherapySelectionFragment : BaseFragment(), OnTextClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    var selectedTimeSlot: String? = null
    var selectedDate: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_therapy_selection
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.VISIBLE
        getSubTitle().visibility = View.GONE

        layoutTimeSlotSelection.visibility = View.GONE

        calendarView.minDate = System.currentTimeMillis() - 1000
        calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            // Note that months are indexed from 0. So, 0 means January, 1 means february, 2 means march etc.
            selectedDate = year.toString() + "-" + (month + 1) + "-" + dayOfMonth
            getTimeSlots()
        }

        // Fetch long milliseconds from calenderView.
        val dateMillis: Long = calendarView.date
        // Create Date object from milliseconds.
        val date = Date(dateMillis)
        selectedDate = DateFormat.format("yyyy", date) as String + "-" +
                DateFormat.format("MM", date) as String + "-" +
                DateFormat.format("dd", date) as String

        getTimeSlots()

        btnTimeSlotContinue.setOnClickListener {
            if (selectedDate != null) {
                if (selectedTimeSlot != null) {
                    Utils.aptScheduleDate = selectedDate!!
                    Utils.aptScheduleTime = selectedTimeSlot!!
                    //Allergies, symptoms,
                    replaceFragment(
                        TherapyBasicDetailsCFragment(),
                        R.id.layout_home,
                        TherapyBasicDetailsCFragment.TAG
                    )
                } else {
                    displayToast("Select time slot")
                }
            } else {
                displayToast("Select date")
            }
        }
    }

    private fun getTimeSlots() {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .getTimeSlots(TimeSlots(Utils.providerPublicId, selectedDate!!))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ result ->
                        try {
                            hideProgress()
                            var responseBody = result.string()
                            Log.d("Response Body", responseBody)
                            val respBody = responseBody.split("|")
                            val status = respBody[1]
                            responseBody = respBody[0]
                            setTimeSlots(responseBody)
                        } catch (e: Exception) {
                            hideProgress()
                            displayToast("Something went wrong.. Please try after sometime")
                        }
                    }, { error ->
                        hideProgress()
                        displayToast("Error ${error.localizedMessage}")
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    private fun setTimeSlots(resp: String) {
        val timeSlotList: Type = object : TypeToken<ArrayList<TimeSlot?>?>() {}.type
        val timeSlots: ArrayList<TimeSlot> = Gson().fromJson(resp, timeSlotList)
        if (timeSlots.isNotEmpty()) {
            layoutTimeSlotSelection.visibility = View.VISIBLE
            recyclerviewTimeSlots.layoutManager = LinearLayoutManager(
                mActivity!!, RecyclerView.VERTICAL,
                false
            )
            recyclerviewTimeSlots.adapter = TimeSlotAdapter(mActivity!!, timeSlots, this)
        } else {
            displayMsg("Alert","Time slots are empty for selected date!")
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment TherapySelectionFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            TherapySelectionFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_Therapy_Selection"
    }

    override fun onTextClickListener(timeSlot: TimeSlot) {
        selectedTimeSlot =
            timeSlot.time_slot_start.dropLast(3) + " - " + timeSlot.time_slot_end.dropLast(3)
        Utils.appointmentId = timeSlot.appointment_id
        Utils.timeSlotId = timeSlot.time_slot_id
    }
}