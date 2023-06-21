package ensemblecare.csardent.com.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ensemblecare.csardent.com.R
import ensemblecare.csardent.com.data.*
import ensemblecare.csardent.com.databinding.FragmentPaymentSelectBinding
import ensemblecare.csardent.com.preference.PrefKeys
import ensemblecare.csardent.com.preference.PreferenceHelper.get
import com.google.gson.Gson
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val ARG_PARAM3 = "param3"

/**
 * A simple [Fragment] subclass.
 * Use the [PaymentSelectFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PaymentSelectFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var plan: Plan? = null
    private var addOn: AddOn? = null
    private var planType: String? = ""
    private var PUBLISHABLE_KEY = ""
    private var customerID: String? = null
    private var ephemeralKey: String? = null
    private var clientSecret: String? = null
    private var transactionUid: String? = null
    private var paymentId: String? = null
    private var paymentSheet: PaymentSheet? = null
    private var transactionSts: TransactionStatus? = null
    private var planPrice: Int = 0
    private var addOnPrice: Int = 0
    private var selectedPaymentMethod: Int = 1
    private lateinit var binding: FragmentPaymentSelectBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            plan = it.getParcelable(ARG_PARAM1)
            addOn = it.getParcelable(ARG_PARAM2)
            planType = it.getString(ARG_PARAM3)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPaymentSelectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getLayout(): Int {
        return R.layout.fragment_payment_select
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE
        getSubTitle().text = ""
        updateStatusBarColor(R.color.white)

        binding.paymentSectionBack.setOnClickListener {
            popBackStack()
        }

        binding.txtPaymentPlanName.text = plan!!.name + " plan"

        when (planType) {
            "Monthly" -> {
                binding.txtPlanCheckOutSubTitle.text =
                    "$" + plan!!.monthly_price + " - Billed Monthly"
                binding.txtPaymentPlanPrice.text = "$" + plan!!.monthly_price
                planPrice = plan!!.monthly_price.toInt()
            }
            "Quarterly" -> {
                binding.txtPlanCheckOutSubTitle.text =
                    "$" + plan!!.quarterly_price + " - Billed Quarterly"
                binding.txtPaymentPlanPrice.text = "$" + (plan!!.quarterly_price.toInt() * 3)
                planPrice = plan!!.quarterly_price.toInt() * 3
            }
            "Annually" -> {
                binding.txtPlanCheckOutSubTitle.text =
                    "$" + plan!!.annually_price + " - Billed Annually"
                binding.txtPaymentPlanPrice.text = "$" + (plan!!.annually_price.toInt() * 12)
                planPrice = plan!!.annually_price.toInt() * 12
            }
        }

        var addOnPrice = 0
        if (addOn == null) {
            binding.layoutAddOnService.visibility = View.GONE
        } else {
            binding.layoutAddOnService.visibility = View.VISIBLE
            binding.txtAddonCheckOutSubTitle.text =
                "$" + addOn!!.monthly_price + " - Billed Monthly"
            binding.txtPaymentAddOnPrice.text = "$" + addOn!!.monthly_price
            addOnPrice = addOn!!.monthly_price.toInt()
        }

        binding.txtPaymentTotalPrice.text = "$" + (planPrice + addOnPrice)

        binding.cardViewSelfPay.setOnClickListener {
            binding.imgInsurancePaySelected.visibility = View.GONE
            binding.imgInsurancePayUnSelected.visibility = View.VISIBLE
            binding.imgSelfPaySelected.visibility = View.VISIBLE
            binding.imgSelfPayUnSelected.visibility = View.GONE
            selectedPaymentMethod = 1
        }

        binding.cardViewInsurance.setOnClickListener {
            binding.imgInsurancePaySelected.visibility = View.VISIBLE
            binding.imgInsurancePayUnSelected.visibility = View.GONE
            binding.imgSelfPaySelected.visibility = View.GONE
            binding.imgSelfPayUnSelected.visibility = View.VISIBLE
            selectedPaymentMethod = 2
        }

        /*val date: Calendar = Calendar.getInstance()
        date.add(Calendar.MONTH, 1)
        val nextMonthDate = SimpleDateFormat("MM/dd/yyyy").format(date.time)
        val nextMonthDateFormat = DateUtils("$nextMonthDate 00:00:00")
        txtPaymentRenewPlan.text =
            "Auto renews on " + nextMonthDateFormat.getFullMonthName() + " " +
                    nextMonthDateFormat.getDay() +
                    nextMonthDateFormat.getDayNumberSuffix(
                        nextMonthDateFormat.getDay().toInt()
                    ) + " " +
                    nextMonthDateFormat.getYear()*/

        paymentSheet = PaymentSheet(this) { paymentSheetResult: PaymentSheetResult? ->
            onPaymentResult(paymentSheetResult!!)
        }

        binding.btnPaymentSection.setOnClickListener {
            if (selectedPaymentMethod == 1) {
                getSelfPayDetails()
            } else {
                replaceFragment(
                    InsuranceFragment.newInstance(plan!!, addOn!!, planType!!),
                    R.id.layout_home,
                    InsuranceFragment.TAG
                )
            }
        }
    }

    private fun getSelfPayDetails() {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .getSelfPayDetails(
                        planType!!,
                        SelfPay(
                            preference!![PrefKeys.PREF_PATIENT_ID, ""]!!,
                            if (addOn == null) "" else addOn!!.id.toString(),
                            plan!!.id
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
                            val jsonObj = JSONObject(responseBody)
                            customerID = jsonObj.getString("customer")
                            ephemeralKey = jsonObj.getString("ephemeralKey")
                            clientSecret = jsonObj.getString("paymentIntent")
                            PUBLISHABLE_KEY = jsonObj.getString("publishableKey")
                            transactionUid = jsonObj.getString("transaction_uid")
                            paymentId = jsonObj.getString("id")
                            stripePayment()
                        } catch (e: Exception) {
                            hideProgress()
                            displayToast("Something went wrong.. Please try after sometime")
                        }
                    }, { error ->
                        hideProgress()
                        displayToast("Error ${error.localizedMessage}")
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    private fun stripePayment() {
        PaymentConfiguration.init(requireActivity(), PUBLISHABLE_KEY)

        val address = PaymentSheet.Address(country = "US")
        val billingDetails = PaymentSheet.BillingDetails(
            address = address,
            email = preference!![PrefKeys.PREF_EMAIL, ""]!!
        )

        val googlePayConfiguration = PaymentSheet.GooglePayConfiguration(
            environment = PaymentSheet.GooglePayConfiguration.Environment.Test,
            countryCode = "US",
            currencyCode = "USD" // Required for Setup Intents, optional for Payment Intents
        )
        val configuration = PaymentSheet.Configuration.Builder("EnsembleCare")
            .customer(
                PaymentSheet.CustomerConfiguration(
                    customerID!!,
                    ephemeralKey!!
                )
            )
            .googlePay(googlePayConfiguration)
            .defaultBillingDetails(billingDetails)
            .allowsDelayedPaymentMethods(true)
            .build()

        paymentSheet!!.presentWithPaymentIntent(clientSecret!!, configuration)
    }

    private fun onPaymentResult(paymentSheetResult: PaymentSheetResult) {
        when (paymentSheetResult) {
            is PaymentSheetResult.Completed -> {
                //displayToast("Payment success")
                Log.d("Payment - ", "Success")
                getTransStatus(true)
            }
            is PaymentSheetResult.Failed -> {
                //displayToast("Payment failed " + (paymentSheetResult as PaymentSheetResult.Failed).error)
                Log.e("App", "Got error: ", (paymentSheetResult as PaymentSheetResult.Failed).error)
                getTransStatus(false)
            }
            else -> {
                //displayMsg("Alert", "Payment cancelled")
                Log.d("Payment - ", "Canceled")
            }
        }
    }

    private fun getTransStatus(paymentStatus: Boolean) {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .getPaymentStatus("PI0035", PaymentStatus(paymentId!!), getAccessToken())
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
                            val jsonArray = JSONArray(responseBody)
                            transactionSts = Gson().fromJson(
                                jsonArray.getJSONObject(0).toString(),
                                TransactionStatus::class.java
                            )
                            if (paymentStatus) {
                                replaceFragment(
                                    TransactionStatusFragment.newInstance(transactionSts!!, true),
                                    R.id.layout_home,
                                    TransactionStatusFragment.TAG
                                )
                            } else {
                                displayMsg("Alert", "Transaction Failed.")
                            }
                        } catch (e: Exception) {
                            hideProgress()
                            e.printStackTrace()
                            displayToast("Something went wrong.. Please try after sometime")
                        }
                    }, { error ->
                        hideProgress()
                        displayToast("Error ${error.localizedMessage}")
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
         * @return A new instance of fragment PaymentSelectFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: Plan, param2: AddOn?, param3: String) =
            PaymentSelectFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM1, param1)
                    putParcelable(ARG_PARAM2, param2)
                    putString(ARG_PARAM3, param3)
                }
            }

        const val TAG = "Screen_Payment_Select"
    }
}