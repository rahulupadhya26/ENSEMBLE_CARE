package ensemblecare.csardent.com.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import ensemblecare.csardent.com.R
import ensemblecare.csardent.com.data.InterestData
import ensemblecare.csardent.com.data.SendSelectedInterests
import ensemblecare.csardent.com.databinding.FragmentInterestBinding
import ensemblecare.csardent.com.preference.PrefKeys
import ensemblecare.csardent.com.preference.PreferenceHelper.set
import ensemblecare.csardent.com.utils.Utils
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [InterestFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class InterestFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var screenName: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentInterestBinding
    private var interestList: ArrayList<String> = arrayListOf()
    private var selectedInterests: ArrayList<String> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            screenName = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_interest
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentInterestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE
        updateStatusBarColor(R.color.white)

        getInterestData()

        binding.imgInterest.setOnClickListener {
            setBottomNavigation(null)
            setLayoutBottomNavigation(null)
            replaceFragmentNoBackStack(
                BottomNavigationFragment(),
                R.id.layout_home,
                BottomNavigationFragment.TAG
            )
        }

        binding.btnSelectedInterests.setOnClickListener {
            if (selectedInterests.isNotEmpty()) {
                sendSelectedInterests()
            } else {
                displayMsg("Alert", "Select the Interests")
            }
        }

        binding.etInterestSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            }

            override fun afterTextChanged(editable: Editable) {
                if (editable.toString().isNotEmpty()) {
                    filterOne(editable.toString())
                } else {
                    binding.chipGroupInterest.removeAllViews()
                    interestList.forEach {
                        binding.chipGroupInterest.addChip(requireActivity(), it)
                    }
                }
            }
        })
    }

    private fun getInterestData() {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .getInterestData(getAccessToken())
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
                            val interestsData =
                                Gson().fromJson(responseBody, InterestData::class.java)
                            interestList = interestsData.interests
                            if (interestList.isNotEmpty()) {
                                binding.layoutInterestList.visibility = View.VISIBLE
                                binding.chipGroupInterest.visibility = View.VISIBLE
                                binding.txtNoInterests.visibility = View.GONE

                                interestList.forEach {
                                    binding.chipGroupInterest.addChip(requireActivity(), it)
                                }
                            } else {
                                binding.chipGroupInterest.visibility = View.GONE
                                binding.layoutInterestList.visibility = View.GONE
                                binding.txtNoInterests.visibility = View.VISIBLE
                            }

                        } catch (e: Exception) {
                            hideProgress()
                            e.printStackTrace()
                            displayToast("Something went wrong.. Please try after sometime")
                        }
                    }, { error ->
                        hideProgress()
                        if ((error as HttpException).code() == 401) {
                            clearCache()
                        } else {
                            displayAfterLoginErrorMsg(error)
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun ChipGroup.addChip(context: Context, label: String) {
        Chip(context).apply {
            id = View.generateViewId()
            text = label
            isClickable = true
            isCheckable = true
            setChipSpacingHorizontalResource(R.dimen._15dp)
            isCheckedIconVisible = true
            isFocusable = true
            checkedIcon = requireActivity().getDrawable(R.drawable.done)
            setTextColor(ContextCompat.getColorStateList(context, R.color.black))
            chipBackgroundColor =
                ContextCompat.getColorStateList(context, R.color.interests_background)
            if (selectedInterests.isNotEmpty()) {
                if (selectedInterests.contains(label)) {
                    isChecked = true
                    setTextColor(
                        ContextCompat.getColorStateList(
                            requireActivity(),
                            R.color.white
                        )
                    )
                    chipBackgroundColor = ContextCompat.getColorStateList(
                        requireActivity(),
                        R.color.initial_screen_background
                    )
                }
            }
            setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    buttonView.setTextColor(
                        ContextCompat.getColorStateList(
                            requireActivity(),
                            R.color.white
                        )
                    )
                    chipBackgroundColor = ContextCompat.getColorStateList(
                        requireActivity(),
                        R.color.initial_screen_background
                    )
                    if (!selectedInterests.contains(text.toString())) {
                        selectedInterests.add(text.toString())
                    }
                } else {
                    if (selectedInterests.contains(text.toString())) {
                        selectedInterests.remove(text.toString())
                    }
                    buttonView.setTextColor(
                        ContextCompat.getColorStateList(
                            requireActivity(),
                            R.color.black
                        )
                    )
                    chipBackgroundColor = ContextCompat.getColorStateList(
                        requireActivity(),
                        R.color.interests_background
                    )
                }
            }
            addView(this)
        }
    }

    private fun filterOne(text: String) {
        val filteredNames = ArrayList<String>()
        interestList.filterTo(filteredNames) {
            it.lowercase().contains(text.lowercase())
        }
        binding.chipGroupInterest.removeAllViews()
        filteredNames.forEach {
            binding.chipGroupInterest.addChip(requireActivity(), it)
        }
    }

    private fun sendSelectedInterests() {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .sendInterestData(SendSelectedInterests(selectedInterests), getAccessToken())
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
                            preference!![PrefKeys.PREF_INTEREST_SELECTED] = true
                            when (screenName) {
                                "wellness" -> {
                                    Utils.bottomNav = Utils.BOTTOM_NAV_WELLNESS
                                    setBottomNavigation(null)
                                    setLayoutBottomNavigation(null)
                                    replaceFragmentNoBackStack(
                                        BottomNavigationFragment(),
                                        R.id.layout_home,
                                        BottomNavigationFragment.TAG
                                    )
                                }

                                "resources" -> {
                                    clearPreviousFragmentStack()
                                    replaceFragmentNoBackStack(
                                        ResourcesFragment(),
                                        R.id.layout_home,
                                        ResourcesFragment.TAG
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            hideProgress()
                            e.printStackTrace()
                            displayToast("Something went wrong.. Please try after sometime")
                        }
                    }, { error ->
                        hideProgress()
                        if ((error as HttpException).code() == 401) {
                            clearCache()
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
         * @return A new instance of fragment InterestFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String = "") =
            InterestFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_interest"
    }
}