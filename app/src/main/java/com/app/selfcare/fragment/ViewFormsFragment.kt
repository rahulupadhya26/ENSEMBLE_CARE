package com.app.selfcare.fragment

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.app.selfcare.BaseActivity
import com.app.selfcare.R
import com.app.selfcare.data.ConsentsRoisDocumentData
import com.app.selfcare.databinding.FragmentActivityCarePlanBinding
import com.app.selfcare.databinding.FragmentViewFormsBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ViewFormsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ViewFormsFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var consentsRoisDocumentData: ConsentsRoisDocumentData? = null
    private var param2: String? = null
    private lateinit var binding: FragmentViewFormsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            consentsRoisDocumentData = it.getParcelable(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentViewFormsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getLayout(): Int {
        return R.layout.fragment_view_forms
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE
        updateStatusBarColor(R.color.white)

        binding.imgViewFormBack.setOnClickListener {
            popBackStack()
        }

        if (consentsRoisDocumentData != null) {
            if (consentsRoisDocumentData!!.title.contains("Consents")) {
                binding.txtViewFormName.text = "Consent form"
            } else {
                binding.txtViewFormName.text = "ROI form"
            }
        }

        val browser = binding.webViewFormView.settings
        browser.javaScriptEnabled = true
        browser.builtInZoomControls = true
        browser.pluginState = WebSettings.PluginState.ON
        binding.webViewFormView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                view?.loadUrl(url!!)
                return true
            }
        }
        binding.webViewFormView.webViewClient = object : WebViewClient() {
            private var mProgressDialog: ProgressDialog? = null
            override fun onPageFinished(view: WebView, url: String?) {
                view.settings.loadsImagesAutomatically = true
                binding.webViewFormView.visibility = View.VISIBLE
                //progressView.setVisibility(View.VISIBLE);
                dismissProgressDialog()
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

        val docUrl = if (consentsRoisDocumentData!!.title.contains("Consents")) {
            BaseActivity.baseURL.dropLast(5) + "/patient/consent_mobile/" + consentsRoisDocumentData!!.description + "/" + consentsRoisDocumentData!!.pk + "/" + getAccessToken().drop(
                7
            )
        } else {
            BaseActivity.baseURL.dropLast(5) + "/patient/roi_mobile/" + consentsRoisDocumentData!!.description + "/" + consentsRoisDocumentData!!.pk + "/" + getAccessToken().drop(
                7
            )
        }

        binding.webViewFormView.loadUrl(docUrl)

        /*binding.webViewFormView.loadUrl(
            "http://docs.google.com/gview?embedded=true&url=" +
                    BaseActivity.baseURL.dropLast(5) + "/media/" + consentsRoisDocumentData!!.pdf_url
        )*/

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ViewFormsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: ConsentsRoisDocumentData, param2: String = "") =
            ViewFormsFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_document_view_form"
    }
}