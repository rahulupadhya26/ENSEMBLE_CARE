package ensemblecare.csardent.com.fragment

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.recyclerview.widget.LinearLayoutManager
import ensemblecare.csardent.com.R
import ensemblecare.csardent.com.adapters.CareBuddyAdapter
import ensemblecare.csardent.com.controller.OnCareBuddyItemClickListener
import ensemblecare.csardent.com.data.CareBuddy
import ensemblecare.csardent.com.data.FetchCareBuddyList
import ensemblecare.csardent.com.data.NotificationType
import ensemblecare.csardent.com.databinding.DialogDisplayReachOutSuccessBinding
import ensemblecare.csardent.com.databinding.FragmentCompanionBinding
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
 * Use the [CompanionFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CompanionFragment : BaseFragment(), OnCareBuddyItemClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentCompanionBinding
    private var companionList: ArrayList<CareBuddy> = arrayListOf()
    private var adapter: CareBuddyAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_companion
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCompanionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE
        updateStatusBarColor(R.color.white)

        binding.cardViewReachOut.visibility = View.GONE

        getCompanionDetails()

        binding.cardViewReachOut.setOnClickListener {
            val idList = ArrayList<Int>()
            companionList.mapTo(idList) { it.id }
            setCareBuddyNotification(idList){
                displayReachOutSentSuccess()
            }
        }

        binding.etCompanionSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            }

            override fun afterTextChanged(editable: Editable) {
                if (editable.toString().isNotEmpty())
                    filterOne(editable.toString())
                else
                    binding.recyclerViewCompanionCareBuddy.apply {
                        layoutManager =
                            LinearLayoutManager(
                                requireActivity(),
                                LinearLayoutManager.VERTICAL,
                                false
                            )
                        adapter = CareBuddyAdapter(
                            mActivity!!,
                            companionList, this@CompanionFragment
                        )
                    }
            }
        })
    }

    private fun getCompanionDetails() {
        binding.layoutNoCompanion.visibility = View.GONE
        binding.layoutCompanionList.visibility = View.VISIBLE
        binding.shimmerCompanionCareBuddy.startShimmer()
        binding.shimmerCompanionCareBuddy.visibility = View.VISIBLE
        binding.recyclerViewCompanionCareBuddy.visibility = View.GONE
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .getCareBuddyData(
                        "PI0069",
                        FetchCareBuddyList(preference!![PrefKeys.PREF_PATIENT_ID, ""]!!.toInt(),
                            "Carebuddy"),
                        getAccessToken()
                    )
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ result ->
                        try {
                            hideProgress()
                            binding.shimmerCompanionCareBuddy.stopShimmer()
                            binding.shimmerCompanionCareBuddy.visibility = View.GONE
                            var responseBody = result.string()
                            Log.d("Response Body", responseBody)
                            val respBody = responseBody.split("|")
                            val status = respBody[1]
                            responseBody = respBody[0]
                            val careBuddyType: Type =
                                object : TypeToken<ArrayList<CareBuddy?>?>() {}.type
                            companionList = Gson().fromJson(responseBody, careBuddyType)
                            if (companionList.isNotEmpty()) {
                                binding.cardViewReachOut.visibility = View.VISIBLE
                                binding.layoutCompanionList.visibility = View.VISIBLE
                                binding.recyclerViewCompanionCareBuddy.visibility = View.VISIBLE
                                binding.layoutNoCompanion.visibility = View.GONE
                                binding.recyclerViewCompanionCareBuddy.apply {
                                    layoutManager = LinearLayoutManager(
                                        requireActivity(),
                                        LinearLayoutManager.VERTICAL,
                                        false
                                    )
                                    adapter = CareBuddyAdapter(
                                        requireActivity(),
                                        companionList, this@CompanionFragment
                                    )
                                }
                            } else {
                                binding.cardViewReachOut.visibility = View.GONE
                                binding.recyclerViewCompanionCareBuddy.visibility = View.GONE
                                binding.layoutCompanionList.visibility = View.GONE
                                binding.layoutNoCompanion.visibility = View.VISIBLE
                            }

                        } catch (e: Exception) {
                            hideProgress()
                            binding.shimmerCompanionCareBuddy.stopShimmer()
                            binding.shimmerCompanionCareBuddy.visibility = View.GONE
                            e.printStackTrace()
                            displayToast("Something went wrong.. Please try after sometime")
                        }
                    }, { error ->
                        hideProgress()
                        if ((error as HttpException).code() == 401) {
                            clearCache()
                        } else {
                            binding.shimmerCompanionCareBuddy.stopShimmer()
                            binding.shimmerCompanionCareBuddy.visibility = View.GONE
                            displayAfterLoginErrorMsg(error)
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    private fun displayReachOutSentSuccess() {
        val reachOutSentSuccessDialog = Dialog(requireActivity())
        reachOutSentSuccessDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        reachOutSentSuccessDialog.setCancelable(false)
        reachOutSentSuccessDialog.setCanceledOnTouchOutside(false)
        val reachOutSentSuccessStatus = DialogDisplayReachOutSuccessBinding.inflate(layoutInflater)
        val view = reachOutSentSuccessStatus.root
        reachOutSentSuccessDialog.setContentView(view)
        reachOutSentSuccessStatus.cardViewReachOutSentOkay.setOnClickListener {
            reachOutSentSuccessDialog.dismiss()
        }
        reachOutSentSuccessDialog.show()
    }

    private fun filterOne(text: String) {
        val filteredNames = ArrayList<CareBuddy>()
        companionList.filterTo(filteredNames) {
            it.first_name.toLowerCase().contains(
                text.toLowerCase()
            ) || it.last_name.toLowerCase().contains(
                text.toLowerCase()
            )
        }

        if (filteredNames.isEmpty()) {
            binding.recyclerViewCompanionCareBuddy.apply {
                layoutManager =
                    LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
                adapter = CareBuddyAdapter(
                    mActivity!!,
                    companionList, this@CompanionFragment
                )
            }
        } else {
            val layoutManager =
                LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
            adapter = CareBuddyAdapter(
                mActivity!!,
                companionList, this
            )
            adapter!!.filterList(filteredNames)
            binding.recyclerViewCompanionCareBuddy.layoutManager = layoutManager
            binding.recyclerViewCompanionCareBuddy.adapter = adapter
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CompanionFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CompanionFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_Companion"
    }

    override fun onCareBuddyItemClickListener(
        careBuddy: CareBuddy,
        isCall: Boolean,
        isReachOut: Boolean
    ) {
        if (isCall) {
            val sIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${careBuddy.phone}"))
            startActivity(sIntent)
        } else if (isReachOut) {
            val id = ArrayList<Int>()
            id.add(careBuddy.id)
            setCareBuddyNotification(arrayListOf(careBuddy.id)){
                displayReachOutSentSuccess()
            }
        } else {
            replaceFragment(
                ViewCareBuddyFragment.newInstance(careBuddy),
                R.id.layout_home,
                ViewCareBuddyFragment.TAG
            )
        }
    }
}