package ensemblecare.csardent.com.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ensemblecare.csardent.com.R
import ensemblecare.csardent.com.adapters.CarePlanDayAdapter
import ensemblecare.csardent.com.controller.OnCarePlanDayWiseItemClickListener
import ensemblecare.csardent.com.data.CareDay
import ensemblecare.csardent.com.data.CarePlans
import ensemblecare.csardent.com.databinding.FragmentCarePlanDashboardBinding
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
 * Use the [CarePlanDashboardFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CarePlanDashboardFragment : BaseFragment(), OnCarePlanDayWiseItemClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentCarePlanDashboardBinding

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
        binding = FragmentCarePlanDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getLayout(): Int {
        return R.layout.fragment_care_plan_dashboard
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE
        updateStatusBarColor(R.color.carePlanBackColor)

        runOnUiThread {
            getCarePlanDayData()
        }
    }

    private fun getCarePlanDayData() {
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .getCarePlanData(getAccessToken())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ result ->
                        try {
                            binding.shimmerCarePlanDashboard.stopShimmer()
                            binding.shimmerCarePlanDashboard.visibility = View.GONE
                            hideProgress()
                            var responseBody = result.string()
                            Log.d("Response Body", responseBody)
                            val respBody = responseBody.split("|")
                            val status = respBody[1]
                            responseBody = respBody[0]
                            val carePlanType: Type =
                                object : TypeToken<CarePlans?>() {}.type
                            val carePlanList: CarePlans =
                                Gson().fromJson(responseBody, carePlanType)
                            binding.recyclerViewCarePlanDashboard.visibility = View.VISIBLE
                            binding.txtNoCarePlanAssigned.visibility = View.GONE
                            binding.recyclerViewCarePlanDashboard.apply {
                                layoutManager = LinearLayoutManager(
                                    requireActivity(), RecyclerView.VERTICAL, false
                                )
                                adapter = CarePlanDayAdapter(
                                    mActivity!!,
                                    carePlanList, this@CarePlanDashboardFragment
                                )
                            }
                        } catch (e: Exception) {
                            hideProgress()
                            binding.shimmerCarePlanDashboard.stopShimmer()
                            binding.shimmerCarePlanDashboard.visibility = View.GONE
                            binding.recyclerViewCarePlanDashboard.visibility = View.GONE
                            binding.txtNoCarePlanAssigned.visibility = View.VISIBLE
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
                                getCarePlanDayData()
                            }
                        } else {
                            binding.shimmerCarePlanDashboard.stopShimmer()
                            binding.shimmerCarePlanDashboard.visibility = View.GONE
                            binding.recyclerViewCarePlanDashboard.visibility = View.GONE
                            binding.txtNoCarePlanAssigned.visibility = View.VISIBLE
                            //displayAfterLoginErrorMsg(error)
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
         * @return A new instance of fragment CarePlanDashboardFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CarePlanDashboardFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_care_plan_dashboard"
    }

    override fun onCarePlanDayWiseItemClickListener(carePlans: CarePlans, careDay: CareDay) {
        clearPreviousFragmentStack()
        replaceFragmentNoBackStack(
            CarePlanFragment.newInstance(carePlans, careDay),
            R.id.layout_home,
            CarePlanFragment.TAG
        )
    }
}