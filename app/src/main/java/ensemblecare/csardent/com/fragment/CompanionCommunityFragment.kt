package ensemblecare.csardent.com.fragment

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
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
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ensemblecare.csardent.com.R
import ensemblecare.csardent.com.adapters.CompanionAdapter
import ensemblecare.csardent.com.controller.OnCareBuddyItemClickListener
import ensemblecare.csardent.com.data.CareBuddy
import ensemblecare.csardent.com.data.FetchCareBuddyList
import ensemblecare.csardent.com.databinding.DialogDisplayReachOutSuccessBinding
import ensemblecare.csardent.com.databinding.FragmentCompanionCommunityBinding
import ensemblecare.csardent.com.preference.PrefKeys
import ensemblecare.csardent.com.preference.PreferenceHelper.get
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
 * Use the [CompanionCommunityFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CompanionCommunityFragment : BaseFragment(), OnCareBuddyItemClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentCompanionCommunityBinding
    private var companionList: ArrayList<CareBuddy> = arrayListOf()
    private var adapter: CompanionAdapter? = null
    var isSelected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_companion_community
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCompanionCommunityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE
        updateStatusBarColor(R.color.white)

        isSelected = false

        getCompanionDetails()

        binding.cardViewSelect.setOnClickListener {
            if (companionList.isNotEmpty()) {
                isSelected = !isSelected
                val layoutManager =
                    LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
                adapter = CompanionAdapter(
                    mActivity!!,
                    companionList, isSelected, this@CompanionCommunityFragment
                )
                binding.recyclerViewCompanionCommunity.layoutManager = layoutManager
                binding.recyclerViewCompanionCommunity.adapter = adapter
                clearValues()
            }
        }

        binding.cardViewReachOutSend.setOnClickListener {
            val selectedItems = adapter!!.getSelectedItems()
            if (selectedItems.isNotEmpty()) {
                val idList = ArrayList<Int>()
                selectedItems.mapTo(idList) { it.id }
                binding.txtReachOutMembers.text = ""
                binding.txtCompanionTitle.text = "Companions"
                animateLayoutHideToBottom()
                isSelected = false
                val layoutManager =
                    LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
                adapter = CompanionAdapter(
                    mActivity!!,
                    companionList, isSelected, this@CompanionCommunityFragment
                )
                binding.recyclerViewCompanionCommunity.layoutManager = layoutManager
                binding.recyclerViewCompanionCommunity.adapter = adapter
                setCareBuddyNotification(idList) {
                    clearValues()
                    displayReachOutSentSuccess()
                }
            }
        }

        binding.etCompanionCommunitySearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            }

            override fun afterTextChanged(editable: Editable) {
                if (editable.toString().isNotEmpty()) {
                    filterOne(editable.toString())
                } else {
                    val layoutManager =
                        LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
                    adapter = CompanionAdapter(
                        mActivity!!,
                        companionList, isSelected, this@CompanionCommunityFragment
                    )
                    binding.recyclerViewCompanionCommunity.layoutManager = layoutManager
                    binding.recyclerViewCompanionCommunity.adapter = adapter
                }
            }
        })
    }

    private fun getCompanionDetails() {
        binding.layoutNoCompanionCommunity.visibility = View.GONE
        binding.layoutCompanionCommunityList.visibility = View.VISIBLE
        binding.shimmerCompanionCommunity.startShimmer()
        binding.shimmerCompanionCommunity.visibility = View.VISIBLE
        binding.recyclerViewCompanionCommunity.visibility = View.GONE
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .getCareBuddyData(
                        "PI0069",
                        FetchCareBuddyList(
                            preference!![PrefKeys.PREF_PATIENT_ID, ""]!!.toInt(),
                            "Companion"
                        ),
                        getAccessToken()
                    )
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ result ->
                        try {
                            hideProgress()
                            binding.shimmerCompanionCommunity.stopShimmer()
                            binding.shimmerCompanionCommunity.visibility = View.GONE
                            var responseBody = result.string()
                            Log.d("Response Body", responseBody)
                            val respBody = responseBody.split("|")
                            val status = respBody[1]
                            responseBody = respBody[0]
                            val careBuddyType: Type =
                                object : TypeToken<ArrayList<CareBuddy?>?>() {}.type
                            companionList = Gson().fromJson(responseBody, careBuddyType)
                            if (companionList.isNotEmpty()) {
                                binding.layoutCompanionCommunityList.visibility = View.VISIBLE
                                binding.recyclerViewCompanionCommunity.visibility = View.VISIBLE
                                binding.layoutNoCompanionCommunity.visibility = View.GONE

                                val layoutManager =
                                    LinearLayoutManager(
                                        requireActivity(),
                                        LinearLayoutManager.VERTICAL,
                                        false
                                    )
                                adapter = CompanionAdapter(
                                    mActivity!!,
                                    companionList, isSelected, this
                                )
                                binding.recyclerViewCompanionCommunity.layoutManager = layoutManager
                                binding.recyclerViewCompanionCommunity.adapter = adapter
                            } else {
                                binding.recyclerViewCompanionCommunity.visibility = View.GONE
                                binding.layoutCompanionCommunityList.visibility = View.GONE
                                binding.layoutNoCompanionCommunity.visibility = View.VISIBLE
                            }

                        } catch (e: Exception) {
                            hideProgress()
                            binding.shimmerCompanionCommunity.stopShimmer()
                            binding.shimmerCompanionCommunity.visibility = View.GONE
                            e.printStackTrace()
                            displayToast("Something went wrong.. Please try after sometime")
                        }
                    }, { error ->
                        hideProgress()
                        if ((error as HttpException).code() == 401) {
                            clearCache()
                        } else {
                            binding.shimmerCompanionCommunity.stopShimmer()
                            binding.shimmerCompanionCommunity.visibility = View.GONE
                            displayAfterLoginErrorMsg(error)
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
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
            binding.recyclerViewCompanionCommunity.apply {
                layoutManager =
                    LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
                adapter = CompanionAdapter(
                    mActivity!!,
                    companionList, isSelected, this@CompanionCommunityFragment
                )
            }
        } else {
            val layoutManager =
                LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
            adapter = CompanionAdapter(
                mActivity!!,
                companionList, isSelected, this
            )
            adapter!!.filterList(filteredNames)
            binding.recyclerViewCompanionCommunity.layoutManager = layoutManager
            binding.recyclerViewCompanionCommunity.adapter = adapter
        }
    }

    private fun animateLayoutDisplayFromBottom() {
        val initialTranslationY = binding.layoutReachOut.height.toFloat()
        binding.layoutReachOut.translationY = initialTranslationY

        val animator = ValueAnimator.ofFloat(initialTranslationY, 0f)
        animator.duration = 500
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.addUpdateListener { valueAnimator ->
            val translationY = valueAnimator.animatedValue as Float
            binding.layoutReachOut.translationY = translationY
        }
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                binding.layoutReachOut.visibility = View.VISIBLE
            }
        })
        animator.start()
    }

    private fun animateLayoutHideToBottom() {
        val initialTranslationY = 0f
        val finalTranslationY = binding.layoutReachOut.height.toFloat()

        val animator = ValueAnimator.ofFloat(initialTranslationY, finalTranslationY)
        animator.duration = 500
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.addUpdateListener { valueAnimator ->
            val translationY = valueAnimator.animatedValue as Float
            binding.layoutReachOut.translationY = translationY
        }
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                binding.layoutReachOut.visibility = View.GONE
            }
        })
        animator.start()
    }

    private fun clearValues() {
        if (!isSelected) {
            adapter!!.clearSelectedItems()
            for (item in companionList) {
                item.isSelected = false
            }
            val selectedItems = adapter!!.getSelectedItems()
            if (selectedItems.isEmpty()) {
                binding.txtReachOutMembers.text = ""
                binding.txtCompanionTitle.text = "Companions"
                animateLayoutHideToBottom()
            }
        }
    }

    private fun displayReachOutSentSuccess() {
        val reachOutSentSuccessDialog = Dialog(requireActivity())
        reachOutSentSuccessDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        reachOutSentSuccessDialog.setCancelable(false)
        reachOutSentSuccessDialog.setCanceledOnTouchOutside(false)
        val reachOutSentSuccessStatus = DialogDisplayReachOutSuccessBinding.inflate(layoutInflater)
        val view = reachOutSentSuccessStatus.root
        reachOutSentSuccessDialog.setContentView(view)
        reachOutSentSuccessStatus.txtReachOutDesc.text =
            "Stay tight! We have notified your\ncompanions"
        reachOutSentSuccessStatus.cardViewReachOutSentOkay.setOnClickListener {
            reachOutSentSuccessDialog.dismiss()
        }
        reachOutSentSuccessDialog.show()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CompanionCommunityFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CompanionCommunityFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    @SuppressLint("SetTextI18n")
    override fun onCareBuddyItemClickListener(
        careBuddy: CareBuddy,
        isCall: Boolean,
        isReachOut: Boolean
    ) {
        if (isReachOut) {
            val selectedItems = adapter!!.getSelectedItems()
            if (selectedItems.isNotEmpty()) {
                binding.txtCompanionTitle.text = "" + selectedItems.size + " Selected"
                val names =
                    selectedItems.joinToString(separator = ", ") { careBuddy -> careBuddy.first_name }
                binding.txtReachOutMembers.text = "Reach out to $names"
                animateLayoutDisplayFromBottom()
            } else {
                binding.txtReachOutMembers.text = ""
                binding.txtCompanionTitle.text = "Companions"
                animateLayoutHideToBottom()
            }
        } else if (isCall) {
            val sIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${careBuddy.phone}"))
            startActivity(sIntent)
        } else {
            replaceFragment(
                ViewCareBuddyFragment.newInstance(careBuddy, "Companion"),
                R.id.layout_home,
                ViewCareBuddyFragment.TAG
            )
        }
    }
}