package com.app.selfcare.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import com.app.selfcare.R
import com.app.selfcare.calendar.CalendarChangesObserver
import com.app.selfcare.calendar.CalendarViewManager
import com.app.selfcare.calendar.SingleRowCalendarAdapter
import com.app.selfcare.selection.CalendarSelectionManager
import com.app.selfcare.utils.DateUtil
import kotlinx.android.synthetic.main.calendar_item.view.*
import kotlinx.android.synthetic.main.fragment_activity_show_all.*
import kotlinx.android.synthetic.main.fragment_activity_show_all.btnLeft
import kotlinx.android.synthetic.main.fragment_activity_show_all.btnRight
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ActivityShowAllFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ActivityShowAllFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private val calendar = Calendar.getInstance()
    private var currentMonth = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_activity_show_all
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE

        layoutActivityCreateGoal.setOnClickListener {
            replaceFragment(
                ActivityCreateGoalFragment(),
                R.id.layout_home,
                ActivityCreateGoalFragment.TAG
            )
        }

        layoutActivityDrinkWater.setOnClickListener {
            replaceFragment(
                ActivityGoalDetailFragment(),
                R.id.layout_home,
                ActivityGoalDetailFragment.TAG
            )
        }

        activityShowAllBack.setOnClickListener {
            popBackStack()
        }

        calendar.time = Date()
        currentMonth = calendar[Calendar.MONTH]

        // calendar view manager is responsible for our displaying logic
        val myCalendarViewManager = object :
            CalendarViewManager {
            override fun setCalendarViewResourceId(
                position: Int,
                date: Date,
                isSelected: Boolean
            ): Int {
                // set date to calendar according to position where we are
                val cal = Calendar.getInstance()
                cal.time = date
                // if item is selected we return this layout items
                // in this example. monday, wednesday and friday will have special item views and other days
                // will be using basic item view
                return if (isSelected)
                    R.layout.selected_calendar_item
                /*when (cal[Calendar.DAY_OF_WEEK]) {
                    Calendar.MONDAY -> R.layout.first_special_selected_calendar_item
                    Calendar.WEDNESDAY -> R.layout.second_special_selected_calendar_item
                    Calendar.FRIDAY -> R.layout.third_special_selected_calendar_item
                    else -> R.layout.selected_calendar_item
                }*/
                else
                // here we return items which are not selected
                    R.layout.calendar_item
                /*when (cal[Calendar.DAY_OF_WEEK]) {
                    Calendar.MONDAY -> R.layout.first_special_calendar_item
                    Calendar.WEDNESDAY -> R.layout.second_special_calendar_item
                    Calendar.FRIDAY -> R.layout.third_special_calendar_item
                    else -> R.layout.calendar_item
                }*/

                // NOTE: if we don't want to do it this way, we can simply change color of background
                // in bindDataToCalendarView method
            }

            override fun bindDataToCalendarView(
                holder: SingleRowCalendarAdapter.CalendarViewHolder,
                date: Date,
                position: Int,
                isSelected: Boolean
            ) {
                // using this method we can bind data to calendar view
                // good practice is if all views in layout have same IDs in all item views
                holder.itemView.tv_date_calendar_item.text = DateUtil.getDayNumber(date)
                holder.itemView.tv_day_calendar_item.text = DateUtil.getDay3LettersName(date)
            }
        }

        // using calendar changes observer we can track changes in calendar
        val myCalendarChangesObserver = object :
            CalendarChangesObserver {
            override fun whenWeekMonthYearChanged(
                weekNumber: String,
                monthNumber: String,
                monthName: String,
                year: String,
                date: Date
            ) {
                super.whenWeekMonthYearChanged(weekNumber, monthNumber, monthName, year, date)
                //tvDate.text = "${DateUtil.getMonthName(date)} "
                //tvDay.text = DateUtil.getDayName(date)
            }

            // you can override more methods, in this example we need only this one
            override fun whenSelectionChanged(isSelected: Boolean, position: Int, date: Date) {
                super.whenSelectionChanged(isSelected, position, date)
                //tvDate.text = "${DateUtil.getMonthName(date)}, "
                if (isSelected) {
                    //tvDay.text = "${DateUtil.getDayNumber(date)} ${DateUtil.getDayName(date)}"
                    /*selectedDate = DateFormat.format("MM", date) as String + "/" +
                            DateFormat.format("dd", date) as String + "/" +
                            DateFormat.format("yyyy", date) as String
                    getTimeSlots()*/
                } else {
                    /* tvDay.text = ""
                     selectedTimeSlot = ""
                     layoutTimeSlotSelection.visibility = View.GONE*/
                }
            }
        }

        // selection manager is responsible for managing selection
        val mySelectionManager = object : CalendarSelectionManager {
            override fun canBeItemSelected(position: Int, date: Date): Boolean {
                // set date to calendar according to position
                //val cal = Calendar.getInstance()
                //cal.time = date
                // in this example sunday and saturday can't be selected, others can
                /*return when (cal[Calendar.DAY_OF_WEEK]) {
                    Calendar.SATURDAY -> false
                    Calendar.SUNDAY -> false
                    else -> true
                }*/
                return true
            }
        }

        // here we init our calendar, also you can set more properties if you haven't specified in XML layout
        val singleRowCalendar = activityShowAllCalendar.apply {
            calendarViewManager = myCalendarViewManager
            calendarChangesObserver = myCalendarChangesObserver
            calendarSelectionManager = mySelectionManager
            setDates(getFutureDatesOfCurrentMonth())
            init()
        }

        btnRight.setOnClickListener {
            //tvDay.text = ""
            //selectedTimeSlot = ""
            //layoutTimeSlotSelection.visibility = View.GONE
            singleRowCalendar.setDates(getDatesOfNextMonth())
        }

        btnLeft.setOnClickListener {
            /*tvDay.text = ""
            selectedTimeSlot = ""
            layoutTimeSlotSelection.visibility = View.GONE*/
            val cal = Calendar.getInstance()
            if (currentMonth > cal[Calendar.MONTH]) {
                singleRowCalendar.setDates(getDatesOfPreviousMonth())
            }
        }
    }

    private fun getDatesOfNextMonth(): List<Date> {
        currentMonth++ // + because we want next month
        if (currentMonth == 12) {
            // we will switch to january of next year, when we reach last month of year
            calendar.set(Calendar.YEAR, calendar[Calendar.YEAR] + 1)
            currentMonth = 0 // 0 == january
        }
        return getDates(mutableListOf())
    }

    private fun getDatesOfPreviousMonth(): List<Date> {
        currentMonth-- // - because we want previous month
        if (currentMonth == -1) {
            // we will switch to december of previous year, when we reach first month of year
            calendar.set(Calendar.YEAR, calendar[Calendar.YEAR] - 1)
            currentMonth = 11 // 11 == december
        }
        return getDates(mutableListOf())
    }

    private fun getFutureDatesOfCurrentMonth(): List<Date> {
        // get all next dates of current month
        currentMonth = calendar[Calendar.MONTH]
        return getDates(mutableListOf())
    }

    private fun getDates(list: MutableList<Date>): List<Date> {
        // load dates of whole month
        calendar.set(Calendar.MONTH, currentMonth)
        val cal = Calendar.getInstance()
        if (currentMonth == cal[Calendar.MONTH]) {
            calendar.set(Calendar.DAY_OF_MONTH, cal[Calendar.DAY_OF_MONTH])
        } else {
            calendar.set(Calendar.DAY_OF_MONTH, 1)
        }
        list.add(calendar.time)
        while (currentMonth == calendar[Calendar.MONTH]) {
            calendar.add(Calendar.DATE, +1)
            if (calendar[Calendar.MONTH] == currentMonth)
                list.add(calendar.time)
        }
        calendar.add(Calendar.DATE, -1)
        return list
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ActivityShowAllFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ActivityShowAllFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
        const val TAG = "Screen_activity_show_all"
    }
}