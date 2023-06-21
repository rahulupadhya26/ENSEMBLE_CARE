package ensemblecare.csardent.com.fragment

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import ensemblecare.csardent.com.R
import ensemblecare.csardent.com.adapters.TimeSlotAdapter
import ensemblecare.csardent.com.calendar.CalendarChangesObserver
import ensemblecare.csardent.com.calendar.CalendarViewManager
import ensemblecare.csardent.com.calendar.SingleRowCalendarAdapter
import ensemblecare.csardent.com.controller.OnTextClickListener
import ensemblecare.csardent.com.data.TimeSlot
import ensemblecare.csardent.com.data.TimeSlots
import ensemblecare.csardent.com.databinding.FragmentTherapySelectionBinding
import ensemblecare.csardent.com.preference.PrefKeys
import ensemblecare.csardent.com.preference.PreferenceHelper.get
import ensemblecare.csardent.com.selection.CalendarSelectionManager
import ensemblecare.csardent.com.utils.DateUtil
import ensemblecare.csardent.com.utils.Utils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.varunbarad.highlightable_calendar_view.DayDecorator
import com.varunbarad.highlightable_calendar_view.OnDateSelectListener
import com.varunbarad.highlightable_calendar_view.OnMonthChangeListener
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import java.lang.reflect.Type
import java.util.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/*https://github.com/VarunBarad/Highlightable-Calendar-View*/

/**
 * A simple [Fragment] subclass.
 * Use the [TherapySelectionFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TherapySelectionFragment : BaseFragment(), OnTextClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    var selectedTimeSlot: String = ""
    var selectedDate: String? = null

    private val calendar = Calendar.getInstance()
    private var currentMonth = 0
    private lateinit var binding: FragmentTherapySelectionBinding

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
        binding = FragmentTherapySelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getLayout(): Int {
        return R.layout.fragment_therapy_selection
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.VISIBLE
        getSubTitle().visibility = View.GONE

        binding.layoutTimeSlotSelection.visibility = View.GONE

        selectedTimeSlot = ""

        val c = Calendar.getInstance()
        val currentDay = c.get(Calendar.DAY_OF_MONTH)

        binding.calendarViewNew.dayDecorators = listOf(
            DayDecorator(
                Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, currentDay) },
                ContextCompat.getColor(requireActivity(), R.color.white),
                ContextCompat.getColor(requireActivity(), R.color.darkGreen)
            )
        )

        binding.calendarViewNew.onMonthChangeListener = OnMonthChangeListener { oldMonth, newMonth ->
            val oldMonthDisplay = oldMonth.getDisplayName(
                Calendar.MONTH,
                Calendar.LONG,
                Locale.getDefault()
            )
            val newMonthDisplay = newMonth.getDisplayName(
                Calendar.MONTH,
                Calendar.LONG,
                Locale.getDefault()
            )
            selectedTimeSlot = ""
            binding.layoutTimeSlotSelection.visibility = View.GONE
        }

        binding.calendarViewNew.onDateSelectListener = OnDateSelectListener { selDate ->
            val date = String.format("%02d", selDate.get(Calendar.DAY_OF_MONTH))
            val month = selDate.getDisplayName(
                Calendar.MONTH,
                Calendar.SHORT_FORMAT,
                Locale.getDefault()
            )
            val year = selDate.get(Calendar.YEAR).toString()
            val monthVal = selDate.get(Calendar.MONTH)
            selectedDate = (monthVal + 1).toString() + "/" + date + "/" + year
            getTimeSlots()
        }

        binding.calendarView.minDate = System.currentTimeMillis() - 1000
        binding.calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            // Note that months are indexed from 0. So, 0 means January, 1 means february, 2 means march etc.
            selectedDate = (month + 1).toString() + "/" + dayOfMonth + "/" + year.toString()
            //getTimeSlots()
        }

        // Fetch long milliseconds from calenderView.
        val dateMillis: Long = binding.calendarView.date
        // Create Date object from milliseconds.
        val date = Date(dateMillis)
        selectedDate = DateFormat.format("MM", date) as String + "/" +
                DateFormat.format("dd", date) as String + "/" +
                DateFormat.format("yyyy", date) as String

        //getTimeSlots()

        binding.btnTimeSlotContinue.setOnClickListener {
            if (selectedDate != null) {
                if (selectedTimeSlot.isNotEmpty()) {
                    Utils.aptScheduleDate = selectedDate!!
                    Utils.aptScheduleTime = selectedTimeSlot
                    //Allergies, symptoms,

                } else {
                    displayToast("Select time slot")
                }
            } else {
                displayToast("Select date")
            }
        }

        calendar.time = Date()
        currentMonth = calendar[Calendar.MONTH]

        // enable white status bar with black icons
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requireActivity().window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            requireActivity().window.statusBarColor = Color.WHITE
        }

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
                /*holder.itemView.tv_date_calendar_item.text = DateUtil.getDayNumber(date)
                holder.itemView.tv_day_calendar_item.text = DateUtil.getDay3LettersName(date)*/
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
                binding.tvDate.text = "${DateUtil.getMonthName(date)} "
                //tvDay.text = DateUtil.getDayName(date)
            }

            // you can override more methods, in this example we need only this one
            override fun whenSelectionChanged(isSelected: Boolean, position: Int, date: Date) {
                super.whenSelectionChanged(isSelected, position, date)
                binding.tvDate.text = "${DateUtil.getMonthName(date)}, "
                if (isSelected) {
                    binding.tvDay.text = "${DateUtil.getDayNumber(date)} ${DateUtil.getDayName(date)}"
                    selectedDate = DateFormat.format("MM", date) as String + "/" +
                            DateFormat.format("dd", date) as String + "/" +
                            DateFormat.format("yyyy", date) as String
                    getTimeSlots()
                } else {
                    binding.tvDay.text = ""
                    selectedTimeSlot = ""
                    binding.layoutTimeSlotSelection.visibility = View.GONE
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
        val singleRowCalendar = binding.mainSingleRowCalendar.apply {
            calendarViewManager = myCalendarViewManager
            calendarChangesObserver = myCalendarChangesObserver
            calendarSelectionManager = mySelectionManager
            setDates(getFutureDatesOfCurrentMonth())
            init()
        }

        binding.btnRight.setOnClickListener {
            binding.tvDay.text = ""
            selectedTimeSlot = ""
            binding.layoutTimeSlotSelection.visibility = View.GONE
            singleRowCalendar.setDates(getDatesOfNextMonth())
        }

        binding.btnLeft.setOnClickListener {
            binding.tvDay.text = ""
            selectedTimeSlot = ""
            binding.layoutTimeSlotSelection.visibility = View.GONE
            val cal = Calendar.getInstance()
            if (currentMonth > cal[Calendar.MONTH]) {
                singleRowCalendar.setDates(getDatesOfPreviousMonth())
            }
        }
    }

    private fun getTimeSlots() {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .getTimeSlots(
                        TimeSlots(Utils.providerPublicId, selectedDate!!),
                        getAccessToken()
                    )
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
                        //displayToast("Error ${error.localizedMessage}")
                        if ((error as HttpException).code() == 401) {
                            userLogin(
                                preference!![PrefKeys.PREF_EMAIL]!!,
                                preference!![PrefKeys.PREF_PASS]!!
                            ) { result ->
                                getTimeSlots()
                            }
                        } else {
                            displayAfterLoginErrorMsg(error)
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    private fun setTimeSlots(resp: String) {
        val timeSlotList: Type = object : TypeToken<ArrayList<TimeSlot?>?>() {}.type
        val timeSlots: ArrayList<TimeSlot> = Gson().fromJson(resp, timeSlotList)
        if (timeSlots.isNotEmpty()) {
            binding.layoutTimeSlotSelection.visibility = View.VISIBLE
            /*recyclerviewTimeSlots.layoutManager = LinearLayoutManager(
                mActivity!!, RecyclerView.HORIZONTAL,
                false
            )*/
            val layoutManager = GridLayoutManager(requireActivity(), 3)
            binding.recyclerviewTimeSlots.layoutManager = layoutManager
            binding.recyclerviewTimeSlots.adapter = TimeSlotAdapter(mActivity!!, timeSlots, this)
        } else {
            displayMsg("Alert", "Time slots are empty for selected date!")
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
        selectedTimeSlot = timeSlot.time_slot_start + " - " + timeSlot.time_slot_end
        Utils.appointmentId = timeSlot.appointment_id
        Utils.timeSlotId = timeSlot.time_slot_id
    }
}