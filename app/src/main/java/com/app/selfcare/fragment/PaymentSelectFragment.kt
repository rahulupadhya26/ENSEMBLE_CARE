package com.app.selfcare.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.app.selfcare.R
import com.app.selfcare.data.*
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper.get
import com.app.selfcare.utils.DateUtils
import com.google.gson.Gson
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_payment_select.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
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
    private var addOn: Int = 0
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            plan = it.getParcelable(ARG_PARAM1)
            addOn = it.getInt(ARG_PARAM2)
            planType = it.getString(ARG_PARAM3)
        }
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

        paymentSectionBack.setOnClickListener {
            popBackStack()
        }

        txtPaymentPlanName.text = plan!!.therapy.name + " plan"

        when (planType) {
            "Monthly" -> {
                txtPlanCheckOutSubTitle.text =
                    "$" + plan!!.therapy.monthly_price + " - Billed Monthly"
                txtPaymentPlanPrice.text = "$" + plan!!.therapy.monthly_price
                planPrice = plan!!.therapy.monthly_price.toInt()
            }
            "Quarterly" -> {
                txtPlanCheckOutSubTitle.text =
                    "$" + plan!!.therapy.quaterly_price + " - Billed Quarterly"
                txtPaymentPlanPrice.text = "$" + (plan!!.therapy.quaterly_price.toInt())
                planPrice = plan!!.therapy.quaterly_price.toInt()
            }
            "Annually" -> {
                txtPlanCheckOutSubTitle.text =
                    "$" + plan!!.therapy.annually_price + " - Billed Annually"
                txtPaymentPlanPrice.text = "$" + (plan!!.therapy.annually_price.toInt())
                planPrice = plan!!.therapy.annually_price.toInt()
            }
        }

        if (addOn == 0) {
            layoutAddOnService.visibility = View.GONE
        } else {
            layoutAddOnService.visibility = View.VISIBLE
            val finalAddon = addOn - plan!!.price
            when (planType) {
                "Monthly" -> {
                    txtAddonCheckOutSubTitle.text =
                        "$$finalAddon - Billed Monthly"
                    txtPaymentAddOnPrice.text = "$$finalAddon"
                    addOnPrice = finalAddon
                }
                "Quarterly" -> {
                    txtAddonCheckOutSubTitle.text =
                        "$$finalAddon - Billed Quarterly"
                    txtPaymentAddOnPrice.text = "$" + (finalAddon)
                    addOnPrice = finalAddon
                }
                "Annually" -> {
                    txtAddonCheckOutSubTitle.text =
                        "$$finalAddon - Billed Annually"
                    txtPaymentAddOnPrice.text = "$" + (finalAddon)
                    addOnPrice = finalAddon
                }
            }
        }

        txtPaymentTotalPrice.text = "$" + (planPrice + addOnPrice)

        cardViewSelfPay.setOnClickListener {
            imgInsurancePaySelected.visibility = View.GONE
            imgInsurancePayUnSelected.visibility = View.VISIBLE
            imgSelfPaySelected.visibility = View.VISIBLE
            imgSelfPayUnSelected.visibility = View.GONE
            selectedPaymentMethod = 1
        }

        cardViewInsurance.setOnClickListener {
            imgInsurancePaySelected.visibility = View.VISIBLE
            imgInsurancePayUnSelected.visibility = View.GONE
            imgSelfPaySelected.visibility = View.GONE
            imgSelfPayUnSelected.visibility = View.VISIBLE
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

        btnPaymentSection.setOnClickListener {
            if (selectedPaymentMethod == 1) {
                getSelfPayDetails()
            } else {
                replaceFragment(
                    InsuranceFragment.newInstance(plan!!, addOn),
                    R.id.layout_home,
                    InsuranceFragment.TAG
                )
            }
        }
    }

    private fun getSelfPayDetails() {
        showProgress()
        val priceId = if (addOn == 0) {
            plan!!.stripe_price_id
        } else {
            plan!!.therapy.add_on_plan.stripe_price_id
        }
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .getSelfPayDetails(
                        planType!!,
                        SelfPay(
                            preference!![PrefKeys.PREF_PATIENT_ID, ""]!!,
                            priceId
                            //"price_1LD7TsSBFqUpdWnSSJ0awbWQ"
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
        val configuration = PaymentSheet.Configuration.Builder("Ensemble Care")
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
        fun newInstance(param1: Plan, param2: Int, param3: String) =
            PaymentSelectFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM1, param1)
                    putInt(ARG_PARAM2, param2)
                    putString(ARG_PARAM3, param3)
                }
            }

        const val TAG = "Screen_Payment_Select"
    }
}