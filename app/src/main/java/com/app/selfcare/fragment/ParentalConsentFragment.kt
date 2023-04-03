package com.app.selfcare.fragment

import android.graphics.Bitmap
import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import android.telephony.PhoneNumberUtils
import android.text.Editable
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.app.selfcare.R
import com.app.selfcare.databinding.FragmentActivityCarePlanBinding
import com.app.selfcare.databinding.FragmentParentalConsentBinding
import com.app.selfcare.utils.SignatureView
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ParentalConsentFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ParentalConsentFragment : BaseFragment(), SignatureView.OnSignedListener {
    // TODO: Rename and change types of parameters
    private var selectedTherapy: String? = null
    private var param2: String? = null
    var bSigned: Boolean = false
    private var selectedRelationship: String? = null
    private var relationshipsData: Array<String>? = null
    private lateinit var binding: FragmentParentalConsentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            selectedTherapy = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentParentalConsentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getLayout(): Int {
        return R.layout.fragment_parental_consent
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.VISIBLE
        getSubTitle().visibility = View.GONE

        relationshipsSpinner()

        binding.etParentContactNo.addTextChangedListener(object : PhoneNumberFormattingTextWatcher("US") {
            private var mFormatting = false
            private var mAfter = 0

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                super.beforeTextChanged(s, start, count, after)
                mAfter = after
            }

            override fun afterTextChanged(s: Editable?) {
                super.afterTextChanged(s)
                if (!mFormatting) {
                    mFormatting = true
                    // using US formatting.
                    if (mAfter != 0) // in case back space ain't clicked.
                        PhoneNumberUtils.formatNumber(
                            s, PhoneNumberUtils.getFormatTypeForLocale(
                                Locale.US
                            )
                        )
                    mFormatting = false
                }
            }
        })

        binding.btnParentalConsent.setOnClickListener {
            if (isValidText(binding.etParentName)) {
                if (isValidEmail(binding.etParentalMailId)) {
                    if (isValidText(binding.etParentContactNo)) {
                        if (getText(binding.etParentContactNo).replace("-", "").length == 10) {
                            if (bSigned) {
                                val screenshot: Bitmap =
                                    takeScreenshotOfRootView(requireActivity().window.decorView.rootView)
                                createAnonymousUser(
                                    selectedTherapy!!,
                                    "data:image/jpg;base64," + convert(screenshot),
                                    getText(binding.etParentName),
                                    selectedRelationship!!,
                                    getText(binding.etParentContactNo)
                                )
                            } else {
                                displayMsg("Alert", "Please sign the consent letter")
                            }
                        } else {
                            setEditTextError(
                                binding.etParentContactNo,
                                "Enter valid phone number"
                            )
                        }
                    } else {
                        setEditTextError(
                            binding.etParentContactNo,
                            "Phone number cannot be blank."
                        )
                    }
                } else {
                    setEditTextError(
                        binding.etParentalMailId,
                        "Enter valid Mail Id"
                    )
                }
            } else {
                setEditTextError(
                    binding.etParentName,
                    "Parent name cannot be blank."
                )
            }
        }
    }

    private fun relationshipsSpinner() {
        try {
            relationshipsData = resources.getStringArray(R.array.relationship)
            val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
                requireActivity(),
                R.layout.spinner_dropdown_custom_item,
                relationshipsData!!
            )
            binding.spinnerRelationship.setAdapter(adapter)
            binding.spinnerRelationship.onItemClickListener =
                AdapterView.OnItemClickListener { parent, arg1, position, id ->
                    //TODO: You can your own logic.
                    selectedRelationship = relationshipsData!![position]
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ParentalConsentFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String = "") =
            ParentalConsentFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        private fun takeScreenshot(view: View): Bitmap {
            view.isDrawingCacheEnabled = true
            view.buildDrawingCache(true)
            val b = Bitmap.createBitmap(view.drawingCache)
            view.isDrawingCacheEnabled = false
            return b
        }

        fun takeScreenshotOfRootView(v: View): Bitmap {
            return takeScreenshot(v)
        }

        const val TAG = "Screen_parental_consent_letter"
    }

    override fun onStartSigning() {
        bSigned = true
    }

    override fun onSigned() {
        bSigned = true
    }

    override fun onClear() {
        bSigned = false
    }
}