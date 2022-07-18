package com.app.selfcare.fragment

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.app.selfcare.R
import com.app.selfcare.adapters.PlanViewPagerAdapter
import com.app.selfcare.data.*
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper.get
import com.app.selfcare.utils.ZoomOutPageTransformer
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_payment_select.*
import kotlinx.android.synthetic.main.fragment_plan.*
import org.json.JSONArray
import org.json.JSONObject
import java.lang.reflect.Type

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PaymentSelectFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PaymentSelectFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var plan: Plan? = null
    private var param2: String? = null
    private var PUBLISHABLE_KEY =
        "pk_test_51LASdbADbguK99nMZVLNAO85JxRjkpxS0O9nwWebDxrpna4DA72tSxIsicx17TvtA7MXmyZgFcZvclav6Kol13T2002cE0xWlR"
    private var customerID: String? = null
    private var ephemeralKey: String? = null
    private var clientSecret: String? = null
    private var transactionUid: String? = null
    private var paymentId: String? = null
    private var paymentSheet: PaymentSheet? = null
    private var transactionSts: TransactionStatus? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            plan = it.getParcelable(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_payment_select
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.VISIBLE
        getSubTitle().visibility = View.GONE
        getSubTitle().text = ""

        paymentSheet = PaymentSheet(this) { paymentSheetResult: PaymentSheetResult? ->
            onPaymentResult(paymentSheetResult!!)
        }

        cardViewInsurance.setOnClickListener {
            replaceFragment(
                InsuranceFragment.newInstance(plan!!),
                R.id.layout_home,
                InsuranceFragment.TAG
            )
        }

        cardViewSelfPay.setOnClickListener {
            getSelfPayDetails()
        }
    }

    private fun getSelfPayDetails() {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .getSelfPayDetails(
                        SelfPay(
                            preference!![PrefKeys.PREF_PATIENT_ID, ""]!!,
                            plan!!.stripe_price_id
                        )
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

        val configuration = PaymentSheet.Configuration.Builder("Self Care")
            .customer(
                PaymentSheet.CustomerConfiguration(
                    customerID!!,
                    ephemeralKey!!
                )
            ) // Set `allowsDelayedPaymentMethods` to true if your business can handle payment methods
            // that complete payment after a delay, like SEPA Debit and Sofort.
            .allowsDelayedPaymentMethods(true)
            .build()

        paymentSheet!!.presentWithPaymentIntent(clientSecret!!, configuration)
    }

    private fun onPaymentResult(paymentSheetResult: PaymentSheetResult) {
        when (paymentSheetResult) {
            is PaymentSheetResult.Completed -> {
                displayToast("Payment success")
                Log.d("Payment - ", "Success")
            }
            is PaymentSheetResult.Failed -> {
                displayToast("Payment failed " + (paymentSheetResult as PaymentSheetResult.Failed).error)
                Log.e("App", "Got error: ", (paymentSheetResult as PaymentSheetResult.Failed).error)
            }
            else -> {
                displayToast("Payment cancelled")
                Log.d("Payment - ", "Canceled")
            }
        }
        getTransStatus()
    }

    private fun getTransStatus() {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .getPaymentStatus("PI0035", PaymentStatus(paymentId!!))
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
                            transactionSts = Gson().fromJson(responseBody, TransactionStatus::class.java)
                            replaceFragment(
                                TransactionStatusFragment.newInstance(transactionSts!!),
                                R.id.layout_home,
                                TransactionStatusFragment.TAG
                            )
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
        fun newInstance(param1: Plan, param2: String = "") =
            PaymentSelectFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_Payment_Select"
    }
}