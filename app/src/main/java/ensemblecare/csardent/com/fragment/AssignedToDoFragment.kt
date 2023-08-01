package ensemblecare.csardent.com.fragment

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ensemblecare.csardent.com.R
import ensemblecare.csardent.com.adapters.ToDoListAdapter
import ensemblecare.csardent.com.controller.OnToDoItemClickListener
import ensemblecare.csardent.com.data.DeleteToDo
import ensemblecare.csardent.com.data.PatientId
import ensemblecare.csardent.com.data.ToDoDashboard
import ensemblecare.csardent.com.data.ToDoData
import ensemblecare.csardent.com.data.UpdateToDo
import ensemblecare.csardent.com.databinding.FragmentAssignedToDoBinding
import ensemblecare.csardent.com.databinding.TodoMenuBinding
import ensemblecare.csardent.com.preference.PrefKeys
import ensemblecare.csardent.com.preference.PreferenceHelper.get
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener
import ensemblecare.csardent.com.adapters.ToDoDataListAdapter
import ensemblecare.csardent.com.calendar.EventDecorator
import ensemblecare.csardent.com.utils.CalenderUtils
import ensemblecare.csardent.com.utils.CurrentDayDecorator
import ensemblecare.csardent.com.utils.DateUtils
import ensemblecare.csardent.com.utils.Utils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.Calendar

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AssignedToDoFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
/*
 * https://github.com/prolificinteractive/material-calendarview
 */
class AssignedToDoFragment : BaseFragment(), OnToDoItemClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentAssignedToDoBinding
    private var widget: MaterialCalendarView? = null
    private var isCalendarView: Boolean = false
    private var sortedList: List<ToDoData> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_assigned_to_do
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAssignedToDoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE

        widget = binding.calendarViewAssignedTodo

        isCalendarView = false

        displayAssignedToDoList()

        binding.swipeRefreshLayoutAssignedToDo.setOnRefreshListener {
            displayAssignedToDoList()
        }

        binding.fabAssignedToDo.setOnClickListener {
            if (isCalendarView) {
                binding.scrollViewCalendarAssignedToDo.visibility = View.GONE
                binding.recyclerViewAssignedToDoList.visibility = View.VISIBLE
                binding.fabAssignedToDo.setImageResource(R.drawable.calendar_img)
                widget = null
            } else {
                binding.recyclerViewAssignedToDoList.visibility = View.GONE
                binding.scrollViewCalendarAssignedToDo.visibility = View.VISIBLE
                binding.fabAssignedToDo.setImageResource(R.drawable.tasklist)
                widget = binding.calendarViewAssignedTodo
                calendarView()
            }
            isCalendarView = !isCalendarView
        }
    }

    private fun displayAssignedToDoList() {
        showProgress()
        binding.swipeRefreshLayoutAssignedToDo.isRefreshing = false
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .getToDoData(
                        "PI0009",
                        PatientId(preference!![PrefKeys.PREF_PATIENT_ID, ""]!!.toInt(), "True"),
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
                            val toDoType: Type =
                                object : TypeToken<ToDoDashboard?>() {}.type
                            val toDoList: ToDoDashboard =
                                Gson().fromJson(responseBody, toDoType)

                            if (toDoList.results.isNotEmpty()) {
                                binding.txtNoAssignedToDoList.visibility = View.GONE
                                binding.recyclerViewAssignedToDoList.visibility = View.VISIBLE
                                sortedList =
                                    toDoList.results.sortedWith(
                                        compareByDescending<ToDoData> { it.created_on }.then(
                                            compareBy { it.is_completed })
                                    )

                                val toDoMap = sortedList.groupBy { it.created_on }
                                binding.recyclerViewAssignedToDoList.apply {
                                    layoutManager = LinearLayoutManager(
                                        requireActivity(), RecyclerView.VERTICAL, false
                                    )
                                    adapter = ToDoListAdapter(
                                        mActivity!!,
                                        toDoMap.keys.toMutableList() as ArrayList<String>,
                                        toDoMap,
                                        this@AssignedToDoFragment
                                    )
                                }

                                calendarView()
                            } else {
                                binding.txtNoAssignedToDoList.visibility = View.VISIBLE
                                binding.recyclerViewAssignedToDoList.visibility = View.GONE
                            }
                        } catch (e: Exception) {
                            hideProgress()
                            e.printStackTrace()
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
                                clearCache()
                            }
                        } else {
                            displayAfterLoginErrorMsg(error)
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    private fun calendarView() {
        val hashSet = HashSet<CalendarDay>()
        for (list in sortedList) {
            val durationData = resources.getStringArray(R.array.goal_duration)
            CalenderUtils.addEvent(
                requireActivity(),
                "${list.created_on} 00:00:00",
                "Journal - " + list.title,
                list.description ?: "",
                durationData[0], "30", "09",
                30
            )
            if (list.is_assign == "True") {
                val dt = DateUtils(list.created_on + " 00:00:00")
                val myDate = CalendarDay.from(
                    dt.getYear().toInt(),
                    dt.getMonthNoFormat().toInt(),
                    dt.getDay().toInt()
                )
                hashSet.add(myDate)
            }
        }
        if (hashSet.isNotEmpty()) {
            val colorId = ContextCompat.getColor(
                requireActivity(),
                R.color.black
            )
            widget!!.addDecorators(EventDecorator(colorId, hashSet))
        }

        val myDate = CalendarDay.today()
        widget!!.addDecorators(
            CurrentDayDecorator(
                requireActivity(),
                myDate,
                R.drawable.bg_round_gray
            )
        )
        previewCalendarEvent(hashSet, myDate)

        widget!!.setOnDateChangedListener { widget, date, selected ->
            previewCalendarEvent(hashSet, date)
        }
    }

    private fun previewCalendarEvent(hashSet: HashSet<CalendarDay>, date: CalendarDay) {
        val toDoMap = ArrayList<ToDoData>()
        val todayDate = CalendarDay.today()
        if (hashSet.isNotEmpty()) {
            for (set in hashSet) {
                if (set == date) {
                    var month = ""
                    if (date.month < 10) {
                        month = "0" + date.month
                    } else {
                        month += date.month
                    }
                    var day = ""
                    if (date.day < 10) {
                        day = "0" + date.day
                    } else {
                        day += date.day
                    }
                    val selectedDate =
                        "" + month + "/" + day + "/" + date.year
                    sortedList.filterTo(toDoMap) {
                        it.created_on == selectedDate
                    }
                    break
                }
            }
        }
        if (toDoMap.isNotEmpty()) {
            binding.recyclerViewSelectedAssignedToDoList.visibility =
                View.VISIBLE
            binding.txtNoSelectedAssignedToDoList.visibility = View.GONE
            val childLayoutManager = LinearLayoutManager(
                requireActivity(),
                LinearLayoutManager.VERTICAL,
                false
            )
            binding.recyclerViewSelectedAssignedToDoList.apply {
                layoutManager = childLayoutManager
                adapter =
                    ToDoDataListAdapter(
                        context,
                        toDoMap,
                        this@AssignedToDoFragment
                    )
            }
        } else if (date == todayDate) {
            binding.recyclerViewSelectedAssignedToDoList.visibility =
                View.GONE
            binding.txtNoSelectedAssignedToDoList.visibility =
                View.VISIBLE
            binding.txtNoSelectedAssignedToDoList.text =
                "Today you don't have any Assigned ToDo's"
        } else {
            binding.recyclerViewSelectedAssignedToDoList.visibility =
                View.GONE
            binding.txtNoSelectedAssignedToDoList.visibility =
                View.VISIBLE
            binding.txtNoSelectedAssignedToDoList.text =
                "There are no Assigned ToDo's on selected date."
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AssignedToDoFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AssignedToDoFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_assigned_todo_list"
    }

    private fun updateToDoList(isCompleted: Boolean, toDoData: ToDoData) {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .updateToDoData(
                        "PI0009",
                        UpdateToDo(
                            toDoData.id,
                            preference!![PrefKeys.PREF_PATIENT_ID, ""]!!.toInt(),
                            toDoData.title,
                            toDoData.description ?: "",
                            toDoData.end_date,
                            is_completed = isCompleted
                        ),
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
                            displayAssignedToDoList()
                        } catch (e: Exception) {
                            hideProgress()
                            e.printStackTrace()
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
                                clearCache()
                            }
                        } else {
                            displayAfterLoginErrorMsg(error)
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    private fun deleteToDo(toDoData: ToDoData) {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .deleteToDo("PI0009", DeleteToDo(toDoData.id), getAccessToken())
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
                            displayAssignedToDoList()
                        } catch (e: Exception) {
                            hideProgress()
                            e.printStackTrace()
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
                                clearCache()
                            }
                        } else {
                            displayAfterLoginErrorMsg(error)
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    private var popup: PopupWindow? = null

    @SuppressLint("RestrictedApi")
    private fun showPopUp(view: View, isComplete: Boolean, toDoData: ToDoData) {
        val toDoMenu = TodoMenuBinding.inflate(layoutInflater)
        val v: View = toDoMenu.root
        val layoutToDoUpdate: LinearLayout = toDoMenu.layoutToDoUpdate
        val layoutToDoDelete: LinearLayout = toDoMenu.layoutToDoDelete
        popup = PopupWindow(v, 500, RelativeLayout.LayoutParams.WRAP_CONTENT, true)
        if (isComplete) {
            layoutToDoUpdate.visibility = View.VISIBLE
        } else {
            layoutToDoUpdate.visibility = View.GONE
        }
        popup!!.contentView.setOnClickListener {
            popup!!.dismiss()
        }
        layoutToDoUpdate.setOnClickListener {
            popup!!.dismiss()
            updateToDoList(false, toDoData)
        }
        layoutToDoDelete.visibility = View.GONE
        layoutToDoDelete.setOnClickListener {
            popup!!.dismiss()
            deleteToDo(toDoData)
        }
        popup!!.showAsDropDown(view, -500, 0)
    }

    override fun onToDoItemClickListener(
        view: View,
        showPopup: Boolean,
        status: String,
        toDoData: ToDoData
    ) {
        if (showPopup) {
            if (status == "Complete") {
                showPopUp(view, false, toDoData)
            } else {
                showPopUp(view, true, toDoData)
            }
        } else {
            if (status == "Document") {
                if (toDoData.document != null) {
                    replaceFragment(
                        CommonWebViewFragment.newInstance(toDoData.document, toDoData.title),
                        R.id.layout_home,
                        AssignedToDoWebViewFragment.TAG
                    )
                }
            } else {
                replaceFragment(
                    AssignedToDoWebViewFragment.newInstance(toDoData),
                    R.id.layout_home,
                    AssignedToDoWebViewFragment.TAG
                )
            }
        }
    }
}