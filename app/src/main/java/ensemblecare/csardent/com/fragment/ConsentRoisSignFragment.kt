package ensemblecare.csardent.com.fragment

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import ensemblecare.csardent.com.BaseActivity
import ensemblecare.csardent.com.R
import ensemblecare.csardent.com.data.ConsentRoisFormsNotify
import ensemblecare.csardent.com.data.FormSignature
import ensemblecare.csardent.com.data.NotifyStatus
import ensemblecare.csardent.com.databinding.DialogSignFormBinding
import ensemblecare.csardent.com.databinding.FragmentConsentRoisSignBinding
import ensemblecare.csardent.com.preference.PrefKeys
import ensemblecare.csardent.com.preference.PreferenceHelper.get
import ensemblecare.csardent.com.preference.PreferenceHelper.set
import ensemblecare.csardent.com.utils.SignatureView
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val ARG_PARAM3 = "param3"

/**
 * A simple [Fragment] subclass.
 * Use the [ConsentRoisSignFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ConsentRoisSignFragment : BaseFragment(), SignatureView.OnSignedListener {
    // TODO: Rename and change types of parameters
    //private var consentRois: ArrayList<ConsentRois>? = null
    private var consentRoisFormsNotifyList: ArrayList<ConsentRoisFormsNotify>? = null
    private var isFromJournal: Boolean = false
    private var signedCount: Int = 0
    var bSigned: Boolean = false
    private var createSignFormSheet: BottomSheetDialog? = null
    private lateinit var binding: FragmentConsentRoisSignBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            //consentRois = it.getParcelableArrayList(ARG_PARAM1)
            consentRoisFormsNotifyList = it.getParcelableArrayList(ARG_PARAM1)
            isFromJournal = it.getBoolean(ARG_PARAM2)
            //consentRoisObj = it.getParcelable(ARG_PARAM3)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentConsentRoisSignBinding.inflate(inflater, container, false)
        return binding.root
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

        if (isFromJournal) {
            binding.consentFormBack.visibility = View.GONE
        } else {
            binding.consentFormBack.visibility = View.VISIBLE
        }

        binding.consentFormBack.setOnClickListener {
            if (isFromJournal) {
                popBackStack()
            } else {
                setBottomNavigation(null)
                setLayoutBottomNavigation(null)
                replaceFragmentNoBackStack(
                    BottomNavigationFragment(),
                    R.id.layout_home,
                    BottomNavigationFragment.TAG
                )
            }
        }

        val browser = binding.webviewConsentRoisForm.settings
        browser.javaScriptEnabled = true
        browser.builtInZoomControls = true
        browser.pluginState = WebSettings.PluginState.ON
        binding.webviewConsentRoisForm.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                view?.loadUrl(url!!)
                return true
            }
        }
        binding.webviewConsentRoisForm.webViewClient = object : WebViewClient() {
            private var mProgressDialog: ProgressDialog? = null
            override fun onPageFinished(view: WebView, url: String?) {
                view.settings.loadsImagesAutomatically = true
                binding.webviewConsentRoisForm.visibility = View.VISIBLE
                //progressView.setVisibility(View.VISIBLE);
                dismissProgressDialog()
                view.loadUrl("javascript:clickFunction(){  })()")
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
                dismissProgressDialog()
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

        binding.progressViewConsentFormCount.max = 100.0F
        binding.progressViewConsentFormCount.progress = 0.0F

        binding.progressViewConsentFormCount.labelText =
            signedCount.toString() + "/" + consentRoisFormsNotifyList!!.size

        displayForms()

        binding.consentFormSign.setOnClickListener {
            displaySignaturePad()
        }
    }

    private fun displayForms() {
        if (signedCount < consentRoisFormsNotifyList!!.size) {
            if (consentRoisFormsNotifyList!![signedCount].type.contains("consent", true)) {
                if (consentRoisFormsNotifyList!![signedCount].extra_data.category != null) {
                    if (consentRoisFormsNotifyList!![signedCount].extra_data.category!!.contains(
                            "consent",
                            ignoreCase = true
                        )
                    ) {
                        binding.webviewConsentRoisForm.loadUrl(
                            BaseActivity.baseURL.dropLast(5) + "/patient/consent_mobile/" + consentRoisFormsNotifyList!![signedCount].description + "/" + consentRoisFormsNotifyList!![signedCount].extra_data.pk + "/" + getAccessToken().drop(
                                7
                            )
                        )
                    } else {
                        binding.webviewConsentRoisForm.loadUrl(
                            BaseActivity.baseURL.dropLast(5) + "/patient/form_mobile/" + consentRoisFormsNotifyList!![signedCount].description + "/" + consentRoisFormsNotifyList!![signedCount].extra_data.pk + "/" + getAccessToken().drop(
                                7
                            )
                        )
                    }
                } else {
                    binding.webviewConsentRoisForm.loadUrl(
                        BaseActivity.baseURL.dropLast(5) + "/patient/consent_mobile/" + consentRoisFormsNotifyList!![signedCount].description + "/" + consentRoisFormsNotifyList!![signedCount].extra_data.pk + "/" + getAccessToken().drop(
                            7
                        )
                    )
                }
            } else {
                binding.webviewConsentRoisForm.loadUrl(
                    BaseActivity.baseURL.dropLast(5) + "/patient/roi_mobile/" + consentRoisFormsNotifyList!![signedCount].description + "/" + consentRoisFormsNotifyList!![signedCount].extra_data.pk + "/" + getAccessToken().drop(
                        7
                    )
                )
            }
        } else {
            val builder = AlertDialog.Builder(mActivity!!)
            builder.setTitle("Message")
            if (!isFromJournal) {
                builder.setMessage("Consent/ROI’s Completed")
            } else {
                builder.setMessage("Columbia Severity Form Completed")
            }
            builder.setPositiveButton("OK") { dialog, which ->
                dialog.dismiss()
                if (isFromJournal) {
                    preference!![PrefKeys.PREF_IS_COLUMBIA_SEVERITY] = ""
                    popBackStack()
                } else {
                    setBottomNavigation(null)
                    setLayoutBottomNavigation(null)
                    replaceFragmentNoBackStack(
                        BottomNavigationFragment(),
                        R.id.layout_home,
                        BottomNavigationFragment.TAG
                    )
                }
            }
            builder.setCancelable(false)
            builder.show()
        }
    }

    private fun sendFormSignature(signature: Bitmap) {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .sendFormSignature(
                        FormSignature(
                            consentRoisFormsNotifyList!![signedCount].description,
                            consentRoisFormsNotifyList!![signedCount].extra_data.pk,
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
                            if (!isFromJournal) {
                                updateNotificationStatus(consentRoisFormsNotifyList!![signedCount].id)
                            } else {
                                signedCount += 1
                                displayForms()
                            }
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
                                sendFormSignature(signature)
                            }
                        } else {
                            displayAfterLoginErrorMsg(error)
                        }
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    private fun updateNotificationStatus(notificationId: Int) {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .updateNotificationStatus(
                        "PI0061",
                        NotifyStatus(notificationId),
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
                            signedCount += 1
                            binding.progressViewConsentFormCount.progress =
                                (signedCount.toFloat() / consentRoisFormsNotifyList!!.size.toFloat()) * 100F
                            binding.progressViewConsentFormCount.labelText =
                                signedCount.toString() + "/" + consentRoisFormsNotifyList!!.size
                            displayForms()
                        } catch (e: Exception) {
                            hideProgress()
                            e.printStackTrace()
                        }
                    }, { error ->
                        hideProgress()
                        displayToast("Error ${error.localizedMessage}")
                    })
            )
        }
        handler.postDelayed(runnable!!, 1000)
    }

    private fun displaySignaturePad() {
        createSignFormSheet = BottomSheetDialog(requireActivity(), R.style.SheetDialog)
        /*val formSignDialog: View = layoutInflater.inflate(
            R.layout.dialog_sign_form, null
        )*/
        val formSignDialog = DialogSignFormBinding.inflate(layoutInflater)
        val view = formSignDialog.root
        createSignFormSheet!!.setContentView(view)
        createSignFormSheet!!.setCanceledOnTouchOutside(true)
        //createSignFormSheet.behavior.isDraggable = false
        formSignDialog.txtFormTitle.text = consentRoisFormsNotifyList!![signedCount].title
        if (isFromJournal) {
            formSignDialog.txtViewDocument.visibility = View.GONE
        } else {
            formSignDialog.txtViewDocument.visibility = View.VISIBLE
        }
        formSignDialog.formSignatureView.setOnSignedListener(this)
        formSignDialog.formSignatureView.clear()
        formSignDialog.btnClearFormSigned.setOnClickListener {
            formSignDialog.formSignatureView.clear()
        }
        formSignDialog.btnConfirmSignature.setOnClickListener {
            if (bSigned) {
                binding.webviewConsentRoisForm.loadUrl("javascript:data_submit()")
                val bitmap = formSignDialog.formSignatureView.getSignatureBitmap()
                sendFormSignature(bitmap)
            } else {
                displayToast("Please sign the consent letter")
            }
        }
        createSignFormSheet!!.show()
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
        fun newInstance(param1: ArrayList<ConsentRoisFormsNotify>?, param2: Boolean = false) =
            ConsentRoisSignFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList(ARG_PARAM1, param1)
                    putBoolean(ARG_PARAM2, param2)
                    //putParcelable(ARG_PARAM3, param3)
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