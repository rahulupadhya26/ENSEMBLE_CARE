package ensemblecare.csardent.com.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ensemblecare.csardent.com.R
import ensemblecare.csardent.com.adapters.CareBuddyCarePlanDataAdapter
import ensemblecare.csardent.com.adapters.CarePlanDayListAdapter
import ensemblecare.csardent.com.controller.OnCarePlanDayItemClickListener
import ensemblecare.csardent.com.controller.OnCarePlanPendingTaskItemClickListener
import ensemblecare.csardent.com.data.CareBuddyDashboard
import ensemblecare.csardent.com.data.CareDayIndividualTaskDetail
import ensemblecare.csardent.com.data.CarePlans
import ensemblecare.csardent.com.data.DayWiseCarePlan
import ensemblecare.csardent.com.databinding.FragmentCareBuddyCarePlanBinding
import ensemblecare.csardent.com.preference.PrefKeys
import ensemblecare.csardent.com.preference.PreferenceHelper.get
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import java.lang.reflect.Type

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CareBuddyCarePlanFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CareBuddyCarePlanFragment : BaseFragment(), OnCarePlanDayItemClickListener,
    OnCarePlanPendingTaskItemClickListener {
    // TODO: Rename and change types of parameters
    private var careBuddy: CareBuddyDashboard? = null
    private var param2: String? = null
    private lateinit var binding: FragmentCareBuddyCarePlanBinding
    private var selectedDayNo: Int = 0
    private lateinit var mAdapter: CarePlanDayListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            careBuddy = it.getParcelable(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_care_buddy_care_plan
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCareBuddyCarePlanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE

        onClickEvents()

        binding.swipeRefreshCareBuddyCarePlan.setOnRefreshListener {
            displayCareBuddyCarePlanData()
        }

        displayCareBuddyCarePlanData()

        try {
            binding.imgCareBuddyCarePlan.visibility = View.GONE
            binding.txtCbCarePlanNameFirstLetter.visibility = View.VISIBLE
            val userTxt = careBuddy!!.name
            if (userTxt.isNotEmpty()) {
                binding.txtCbCarePlanNameFirstLetter.text = userTxt.substring(0, 1).uppercase()
            }

            binding.txtCareBuddyCarePlanName.text = careBuddy!!.name
            binding.txtCareBuddyCarePlanPhoneNo.text = careBuddy!!.phone
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun onClickEvents() {
        binding.careBuddyCarePlanBack.setOnClickListener {
            popBackStack()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun displayCareBuddyCarePlanData() {
        binding.swipeRefreshCareBuddyCarePlan.isRefreshing = false
        getCareBuddyCarePlanDayWiseData { response ->
            val carePlanType: Type =
                object : TypeToken<CarePlans?>() {}.type
            val carePlanList: CarePlans =
                Gson().fromJson(response, carePlanType)
            if (carePlanList.days.isNotEmpty()) {
                selectedDayNo = if (carePlanList.current_day <= carePlanList.days.size) {
                    carePlanList.current_day
                } else {
                    1
                }
                binding.recyclerViewCarePlanDayList.visibility = View.VISIBLE
                binding.recyclerViewCarePlanDayWiseData.visibility = View.VISIBLE
                binding.txtNoCarePlanAssigned.visibility = View.GONE
                binding.recyclerViewCarePlanDayList.apply {
                    layoutManager = LinearLayoutManager(
                        requireActivity(), RecyclerView.HORIZONTAL, false
                    )
                    adapter = CarePlanDayListAdapter(
                        mActivity!!,
                        carePlanList.days.size, selectedDayNo, "", this@CareBuddyCarePlanFragment
                    )
                }
                callCareBuddyDayWiseCarePlanData(selectedDayNo)
            } else {
                binding.recyclerViewCarePlanDayList.visibility = View.GONE
                binding.recyclerViewCarePlanDayWiseData.visibility = View.GONE
                binding.txtNoCarePlanAssigned.visibility = View.VISIBLE
                binding.txtNoCarePlanAssigned.text =
                    "CarePlan has not been assigned to " + preference!![PrefKeys.PREF_FNAME, ""]!! + " " + preference!![PrefKeys.PREF_LNAME, ""]!!
            }
        }
    }

    private fun callCareBuddyDayWiseCarePlanData(dayNo: Int) {
        getCareBuddyDayWiseCarePlanData(dayNo) { response ->
            val dayWiseCarePlanType: Type =
                object : TypeToken<DayWiseCarePlan?>() {}.type
            val dayWiseCarePlan: DayWiseCarePlan =
                Gson().fromJson(response, dayWiseCarePlanType)
            binding.recyclerViewCarePlanDayList.apply {
                layoutManager = LinearLayoutManager(
                    requireActivity(), RecyclerView.HORIZONTAL, false
                )
                adapter = CarePlanDayListAdapter(
                    mActivity!!,
                    dayWiseCarePlan.total_days, dayNo, "", this@CareBuddyCarePlanFragment
                )
            }
            val tasks: ArrayList<CareDayIndividualTaskDetail> = arrayListOf()
            if (dayWiseCarePlan.plan.tasks.yoga != null && dayWiseCarePlan.plan.tasks.yoga.isNotEmpty())
                tasks.addAll(dayWiseCarePlan.plan.tasks.yoga)
            if (dayWiseCarePlan.plan.tasks.exercise != null && dayWiseCarePlan.plan.tasks.exercise.isNotEmpty())
                tasks.addAll(dayWiseCarePlan.plan.tasks.exercise)
            if (dayWiseCarePlan.plan.tasks.nutrition != null && dayWiseCarePlan.plan.tasks.nutrition.isNotEmpty())
                tasks.addAll(dayWiseCarePlan.plan.tasks.nutrition)
            if (dayWiseCarePlan.plan.tasks.mindfulness != null && dayWiseCarePlan.plan.tasks.mindfulness.isNotEmpty())
                tasks.addAll(dayWiseCarePlan.plan.tasks.mindfulness)
            if (dayWiseCarePlan.plan.tasks.music != null && dayWiseCarePlan.plan.tasks.music.isNotEmpty())
                tasks.addAll(dayWiseCarePlan.plan.tasks.music)

            tasks.sortBy { it.time }

            binding.recyclerViewCarePlanDayWiseData.apply {
                layoutManager = LinearLayoutManager(
                    requireActivity(), RecyclerView.VERTICAL, false
                )
                adapter = CareBuddyCarePlanDataAdapter(
                    mActivity!!,
                    tasks, this@CareBuddyCarePlanFragment
                )
            }

        }
    }

    private fun getCareBuddyCarePlanDayWiseData(myCallback: (result: String?) -> Unit) {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .getCareBuddyCarePlanDayWiseData(careBuddy!!.pk, getAccessToken())
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
                            myCallback.invoke(responseBody)
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

    private fun getCareBuddyDayWiseCarePlanData(dayNo: Int, myCallback: (result: String?) -> Unit) {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .getCareBuddyDayWiseCarePlanData(careBuddy!!.pk, dayNo, getAccessToken())
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
                            myCallback.invoke(responseBody)
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

                            }
                        } else {
                            displayAfterLoginErrorMsg(error)
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CareBuddyCarePlanFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: CareBuddyDashboard, param2: String = "") =
            CareBuddyCarePlanFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_Care_Buddy_Care_Plan"
    }

    override fun onCarePlanDayItemClickListener(dayNumber: Int) {
        selectedDayNo = dayNumber
        callCareBuddyDayWiseCarePlanData(dayNumber)
    }

    override fun onCarePlanPendingTaskItemClickListener(
        careDayIndividualTaskDetail: CareDayIndividualTaskDetail,
        isCompleted: Boolean
    ) {
        if (!isCompleted) {
            when (careDayIndividualTaskDetail.task_detail.details.type) {
                "Video" -> {
                    replaceFragment(
                        VideoDetailFragment.newInstance(
                            null,
                            careDayIndividualTaskDetail.task_detail.details.url
                        ),
                        R.id.layout_home,
                        VideoDetailFragment.TAG
                    )
                }

                "Podcast" -> {
                    replaceFragment(
                        PodcastDetailFragment.newInstance(
                            null,
                            "",
                            careDayIndividualTaskDetail.task_detail.details.url
                        ),
                        R.id.layout_home,
                        PodcastDetailFragment.TAG
                    )
                }

                "Article" -> {

                }
            }
        }
    }
}