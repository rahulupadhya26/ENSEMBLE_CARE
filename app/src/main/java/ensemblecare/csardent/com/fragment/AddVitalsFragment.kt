package ensemblecare.csardent.com.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ensemblecare.csardent.com.R
import ensemblecare.csardent.com.data.SendVitals
import ensemblecare.csardent.com.databinding.FragmentAddVitalsBinding
import ensemblecare.csardent.com.preference.PrefKeys
import ensemblecare.csardent.com.preference.PreferenceHelper.get
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AddVitalsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddVitalsFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentAddVitalsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_add_vitals
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddVitalsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE
        updateStatusBarColor(R.color.white)

        binding.addVitalsBack.setOnClickListener {
            popBackStack()
        }

        binding.cardViewAddVitals.setOnClickListener {
            if (isValidText(binding.editTextAddVitalsWeight) ||
                isValidText(binding.editTextAddVitalsHeight) ||
                isValidText(binding.editTextAddVitalsBpSystolic) ||
                isValidText(binding.editTextAddVitalsBpDiastolic) ||
                isValidText(binding.editTextAddVitalsTemperature) ||
                isValidText(binding.editTextAddVitalsPulse) ||
                isValidText(binding.editTextAddVitalsRespiration) ||
                isValidText(binding.editTextAddVitalsO2Saturation) ||
                isValidText(binding.editTextAddVitalsGlucoseReading)
            ) {
                sendVitals()
            } else {
                displayMsg("Alert", "Fill up the vitals details")
            }
        }

        binding.editTextAddVitalsWeight.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            }

            override fun afterTextChanged(editable: Editable) {
                if (editable.toString().isNotEmpty()) {
                    if (isValidText(binding.editTextAddVitalsHeight)) {
                        val height =
                            feetToCentimeters(getText(binding.editTextAddVitalsHeight).toDouble())
                        val bmi = calculateBMI(
                            getText(binding.editTextAddVitalsWeight).toDouble(), height
                        )
                        binding.txtAddVitalsBmi.text = String.format("%.2f", bmi)
                    }
                }
            }
        })

        binding.editTextAddVitalsHeight.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            }

            override fun afterTextChanged(editable: Editable) {
                if (editable.toString().isNotEmpty()) {
                    if (isValidText(binding.editTextAddVitalsWeight)) {
                        val height =
                            feetToCentimeters(getText(binding.editTextAddVitalsHeight).toDouble())
                        val bmi = calculateBMI(
                            getText(binding.editTextAddVitalsWeight).toDouble(), height
                        )
                        binding.txtAddVitalsBmi.text = String.format("%.2f", bmi)
                    }
                }
            }
        })
    }

    private fun feetToCentimeters(feet: Double): Double {
        val inches = feet * 12
        return inches * 2.54
    }

    private fun calculateBMI(weightLbs: Double, height: Double): Double {
        // Convert height to meters
        val heightInInches = height * 12.0
        // Calculate BMI
        return (weightLbs / (heightInInches * heightInInches)) * 703
    }

    private fun sendVitals() {
        val cal = Calendar.getInstance()
        val myFormat = "MM/dd/yyyy"
        val sdf = SimpleDateFormat(myFormat)
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .sendVitals(
                        "PI0075",
                        SendVitals(
                            preference!![PrefKeys.PREF_PATIENT_ID, 0]!!,
                            sdf.format(cal.time),
                            getText(binding.editTextAddVitalsWeight),
                            getText(binding.editTextAddVitalsHeight),
                            getText(binding.editTextAddVitalsBpSystolic),
                            getText(binding.editTextAddVitalsBpDiastolic),
                            getText(binding.editTextAddVitalsTemperature),
                            getText(binding.editTextAddVitalsPulse),
                            getText(binding.editTextAddVitalsRespiration),
                            getText(binding.editTextAddVitalsO2Saturation),
                            binding.txtAddVitalsBmi.text.toString()
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
                            popBackStack()
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
         * @return A new instance of fragment AddVitalsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AddVitalsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_add_vitals"
    }
}