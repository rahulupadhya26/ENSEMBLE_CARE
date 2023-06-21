package ensemblecare.csardent.com.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import ensemblecare.csardent.com.R
import ensemblecare.csardent.com.adapters.PersonalGoalAdapter
import ensemblecare.csardent.com.controller.OnGoalItemClickListener
import ensemblecare.csardent.com.data.Goal
import ensemblecare.csardent.com.data.PersonalGoalData
import ensemblecare.csardent.com.databinding.FragmentPersonalGoalBinding
import ensemblecare.csardent.com.preference.PrefKeys
import ensemblecare.csardent.com.preference.PreferenceHelper.get
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import java.lang.Exception
import java.lang.reflect.Type
import java.util.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PersonalGoalFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PersonalGoalFragment : BaseFragment(), OnGoalItemClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var offset: Int? = 0
    private lateinit var binding: FragmentPersonalGoalBinding

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
        binding = FragmentPersonalGoalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getLayout(): Int {
        return R.layout.fragment_personal_goal
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fabAddGoals.setOnClickListener {
            replaceFragment(CreateGoalFragment(), R.id.layout_home, CreateGoalFragment.TAG)
        }
        getPersonalGoalsList()
    }

    private fun getPersonalGoalsList() {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .getPersonalGoalData(
                        "PI0010",
                        PersonalGoalData(preference!![PrefKeys.PREF_PATIENT_ID, 0]!!),
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
                            var goalLists: ArrayList<Goal> = arrayListOf()
                            val goalList: Type = object : TypeToken<ArrayList<Goal?>?>() {}.type
                            goalLists = Gson().fromJson(responseBody, goalList)

                            if (goalLists.isNotEmpty()) {
                                binding.recyclerViewPersonalGoalsList.visibility = View.VISIBLE
                                binding.txtNoPersonalGoals.visibility = View.GONE
                                binding.recyclerViewPersonalGoalsList.apply {
                                    layoutManager = GridLayoutManager(mActivity!!, 2)
                                    adapter = PersonalGoalAdapter(
                                        mActivity!!,
                                        goalLists,
                                        this@PersonalGoalFragment
                                    )
                                }
                            } else {
                                binding.recyclerViewPersonalGoalsList.visibility = View.GONE
                                binding.txtNoPersonalGoals.visibility = View.VISIBLE
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
                                getPersonalGoalsList()
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
         * @return A new instance of fragment PersonalGoalFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PersonalGoalFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_personal_goals"
    }

    override fun onGoalItemClickListener(goal: Goal, isDelete: Boolean) {
        if (isDelete) {
            //Delete Goal
            deleteData("PI0010", goal.id) { response ->
                if (response == "Success") {
                    getPersonalGoalsList()
                }
            }
        } else {
            replaceFragment(
                DetailGoalFragment.newInstance(goal),
                R.id.layout_home,
                DetailGoalFragment.TAG
            )
        }
    }
}