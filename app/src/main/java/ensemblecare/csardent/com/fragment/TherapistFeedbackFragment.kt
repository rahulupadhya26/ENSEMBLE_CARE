package ensemblecare.csardent.com.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import androidx.fragment.app.Fragment
import ensemblecare.csardent.com.R
import ensemblecare.csardent.com.data.Feedback
import ensemblecare.csardent.com.data.GetAppointment
import ensemblecare.csardent.com.data.GroupVideoCallFeedback
import ensemblecare.csardent.com.databinding.FragmentTherapistFeedbackBinding
import ensemblecare.csardent.com.preference.PrefKeys
import ensemblecare.csardent.com.preference.PreferenceHelper.get
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException

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
    private var appointment: GetAppointment? = null
    private var param2: String? = null
    private var therapistFeedbackRating: String? = null
    private var serviceFeedbackRating: String? = null
    private lateinit var binding: FragmentTherapistFeedbackBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            appointment = it.getParcelable(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTherapistFeedbackBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getLayout(): Int {
        return R.layout.fragment_therapist_feedback
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE

        binding.ratingBarTherapistRating.onRatingBarChangeListener =
            RatingBar.OnRatingBarChangeListener { p0, p1, p2 ->
                therapistFeedbackRating = p1.toString()
            }

        binding.ratingBarServiceRating.onRatingBarChangeListener =
            RatingBar.OnRatingBarChangeListener { p0, p1, p2 ->
                serviceFeedbackRating = p1.toString()
            }

        binding.btnTherapistFeedback.setOnClickListener {
            if (serviceFeedbackRating != null) {
                if (therapistFeedbackRating != null) {
                    if (getText(binding.editTxtTherapistFeedback).isNotEmpty()) {
                        if(appointment!!.is_group_appointment) {
                            sendGroupVideoCallFeedback()
                        } else {
                            sendFeedback()
                        }
                    } else {
                        setEditTextError(binding.editTxtTherapistFeedback, "Please provide feedback")
                    }
                } else {
                    displayMsg("Alert", "Please provide therapist rating")
                }
            } else {
                displayMsg("Alert", "Please provide service rating")
            }
        }
    }

    private fun sendFeedback() {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .sendFeedback(
                        "PI0045",
                        Feedback(
                            preference!![PrefKeys.PREF_PATIENT_ID, ""]!!.toInt(),
                            appointment!!.appointment!!.appointment_id,
                            therapistFeedbackRating!!.toDouble(),
                            getText(binding.editTxtTherapistFeedback),
                            serviceFeedbackRating!!.toDouble(),
                            ""
                        ), getAccessToken()
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
                            if (status == "201") {
                                /*for (i in 0 until mActivity!!.supportFragmentManager.backStackEntryCount) {
                                    if (mActivity!!.getCurrentFragment() !is DashboardFragment) {
                                        popBackStack()
                                    } else {
                                        break
                                    }
                                }*/
                                setBottomNavigation(null)
                                setLayoutBottomNavigation(null)
                                replaceFragmentNoBackStack(
                                    BottomNavigationFragment(),
                                    R.id.layout_home,
                                    BottomNavigationFragment.TAG
                                )
                            }
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
                                sendFeedback()
                            }
                        } else {
                            displayAfterLoginErrorMsg(error)
                            popBackStack()
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    private fun sendGroupVideoCallFeedback() {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .sendGroupVideoFeedback(
                        "PI0045",
                        GroupVideoCallFeedback(
                            preference!![PrefKeys.PREF_PATIENT_ID, ""]!!.toInt(),
                            appointment!!.group_appointment!!.id,
                            therapistFeedbackRating!!.toDouble(),
                            getText(binding.editTxtTherapistFeedback),
                            serviceFeedbackRating!!.toDouble(),
                            ""
                        ), getAccessToken()
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
                            if (status == "201") {
                                /*for (i in 0 until mActivity!!.supportFragmentManager.backStackEntryCount) {
                                    if (mActivity!!.getCurrentFragment() !is DashboardFragment) {
                                        popBackStack()
                                    } else {
                                        break
                                    }
                                }*/
                                setBottomNavigation(null)
                                setLayoutBottomNavigation(null)
                                replaceFragmentNoBackStack(
                                    BottomNavigationFragment(),
                                    R.id.layout_home,
                                    BottomNavigationFragment.TAG
                                )
                            }
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
                                sendFeedback()
                            }
                        } else {
                            displayAfterLoginErrorMsg(error)
                            popBackStack()
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
         * @return A new instance of fragment TherapistFeedbackFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: GetAppointment, param2: String = "") =
            TherapistFeedbackFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_therapist_feedback"
    }
}