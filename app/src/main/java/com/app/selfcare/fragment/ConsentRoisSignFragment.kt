package com.app.selfcare.fragment

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.app.selfcare.BaseActivity
import com.app.selfcare.R
import com.app.selfcare.data.ConsentRois
import com.app.selfcare.data.ConsentRoisFormsNotify
import com.app.selfcare.data.FormSignature
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper.get
import com.app.selfcare.utils.SignatureView
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.dialog_sign_form.view.*
import kotlinx.android.synthetic.main.fragment_consent_rois_sign.*
import retrofit2.HttpException
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ConsentRoisSignFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ConsentRoisSignFragment : BaseFragment(), SignatureView.OnSignedListener {
    // TODO: Rename and change types of parameters
    private var consentRois: ArrayList<ConsentRois>? = null
    private var consentRoisFormsNotifyList: ArrayList<ConsentRoisFormsNotify>? = null
    private var signedCount: Int = 0
    var bSigned: Boolean = false
    private var createSignFormSheet: BottomSheetDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            consentRois = it.getParcelableArrayList(ARG_PARAM1)
            consentRoisFormsNotifyList = it.getParcelableArrayList(ARG_PARAM2)
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_consent_rois_sign
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE

        consentFormBack.setOnClickListener {
            popBackStack()
        }

        webviewConsentRoisForm.settings.javaScriptEnabled = true
        webviewConsentRoisForm.settings.builtInZoomControls = true
        webviewConsentRoisForm.settings.pluginState = WebSettings.PluginState.ON
        webviewConsentRoisForm.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                view?.loadUrl(url!!)
                return true
            }
        }
        webviewConsentRoisForm.webViewClient = object : WebViewClient() {
            private var mProgressDialog: ProgressDialog? = null
            override fun onPageFinished(view: WebView, url: String?) {
                view.settings.loadsImagesAutomatically = true
                webviewConsentRoisForm.visibility = View.VISIBLE
                //progressView.setVisibility(View.VISIBLE);
                dismissProgressDialog()
                Log.v("after load", view.url!!)
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                showProgressDialog()
            }

            override fun onReceivedError(
                view: WebView?,
                errorCode: Int,
                description: String?,
                failingUrl: String?
            ) {
                Log.e("error", description!!)
            }

            private fun showProgressDialog() {
                dismissProgressDialog()
                mProgressDialog = ProgressDialog.show(mContext, "", "Loading...")
            }

            private fun dismissProgressDialog() {
                if (mProgressDialog != null && mProgressDialog!!.isShowing) {
                    mProgressDialog!!.dismiss()
                    mProgressDialog = null
                }
            }
        }

        displayForms()

        consentFormSign.setOnClickListener {
            createSignFormSheet = BottomSheetDialog(requireActivity(), R.style.SheetDialog)
            val formSignDialog: View = layoutInflater.inflate(
                R.layout.dialog_sign_form, null
            )
            createSignFormSheet!!.setContentView(formSignDialog)
            createSignFormSheet!!.setCanceledOnTouchOutside(true)
            //createSignFormSheet.behavior.isDraggable = false
            formSignDialog.txtFormTitle.text = consentRois!![signedCount].text
            formSignDialog.formSignatureView.setOnSignedListener(this)
            formSignDialog.formSignatureView.clear()
            formSignDialog.btnClearFormSigned.setOnClickListener {
                formSignDialog.formSignatureView.clear()
            }
            formSignDialog.btnConfirmSignature.setOnClickListener {
                if (bSigned) {
                    val bitmap = formSignDialog.formSignatureView.getSignatureBitmap()
                    sendFormSignature(consentRois!![signedCount].key, bitmap)
                } else {
                    displayToast("Please sign the consent letter")
                }
            }
            createSignFormSheet!!.show()
        }

        /*if (consentRois!!.isCompleted) {
            webviewConsentRoisForm.loadUrl(
                "http://docs.google.com/gview?embedded=true&url=" + BaseActivity.baseURL.dropLast(5) + consentRois!!.pdf_file_url
            )
        } else {
            webviewConsentRoisForm.loadUrl(
                BaseActivity.baseURL.dropLast(5) + "/patient/consent_mobile/" + consentRois!!.key + "/" + getAccessToken().drop(
                    7
                )
            )
        }*/
    }

    private fun displayForms() {
        if (signedCount < consentRois!!.size) {
            webviewConsentRoisForm.loadUrl(
                BaseActivity.baseURL.dropLast(5) + "/patient/consent_mobile/" + consentRois!![signedCount].key + "/" + getAccessToken().drop(
                    7
                )
            )
        } else {
            val builder = AlertDialog.Builder(mActivity!!)
            builder.setTitle("Message")
            builder.setMessage("All forms are signed.")
            builder.setPositiveButton("OK") { dialog, which ->
                dialog.dismiss()
                popBackStack()
            }
            builder.setCancelable(false)
            builder.show()
        }
    }

    private fun sendFormSignature(consentName: String, signature: Bitmap) {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .sendFormSignature(
                        FormSignature(
                            consentName,
                            "data:image/jpg;base64," + convert(signature),
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
                            if (createSignFormSheet!! != null) {
                                createSignFormSheet!!.dismiss()
                            }
                            signedCount += 1
                            for (i in 0 until consentRoisFormsNotifyList!!.size) {
                                if (consentRoisFormsNotifyList!![i].description == consentName) {
                                    updateNotificationStatus(consentRoisFormsNotifyList!![i].id)
                                    break
                                }
                            }
                            displayForms()
                        } catch (e: Exception) {
                            hideProgress()
                            e.printStackTrace()
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
                                sendFormSignature(consentName, signature)
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
         * @return A new instance of fragment ConsentRoisSignFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: ArrayList<ConsentRois>, param2: ArrayList<ConsentRoisFormsNotify>) =
            ConsentRoisSignFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList(ARG_PARAM1, param1)
                    putParcelableArrayList(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_consents_rois_form"
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