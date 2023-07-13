package ensemblecare.csardent.com.fragment

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AlertDialog
import ensemblecare.csardent.com.BaseActivity
import ensemblecare.csardent.com.R
import ensemblecare.csardent.com.databinding.FragmentCommonWebViewBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CommonWebViewFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CommonWebViewFragment : BaseFragment() {
    // TODO: Rename and change types of parameters
    private var webUrl: String? = null
    private var screenName: String? = null
    private lateinit var binding: FragmentCommonWebViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            webUrl = it.getString(ARG_PARAM1)
            screenName = it.getString(ARG_PARAM2)
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_common_web_view
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCommonWebViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE

        binding.imgCommonWebViewBack.setOnClickListener {
            val builder = AlertDialog.Builder(mActivity!!)
            builder.setTitle("Confirmation")
            builder.setMessage("Do you wish to close this window?")
            builder.setPositiveButton("Yes") { dialog, which ->
                dialog.dismiss()
                popBackStack()
            }
            builder.setNegativeButton("No") { dialog, which ->
                dialog.dismiss()
            }
            builder.setCancelable(false)
            builder.show()
        }

        binding.txtCommonUrl.text = screenName
        binding.webViewCommon.settings.apply {
            loadWithOverviewMode = true
            useWideViewPort = true
            builtInZoomControls = true
            displayZoomControls = false
            setSupportZoom(true)
            javaScriptEnabled = true
            pluginState = WebSettings.PluginState.ON
        }
        binding.webViewCommon.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                view?.loadUrl(url!!)
                return true
            }
        }
        binding.webViewCommon.webViewClient = object : WebViewClient() {
            private var mProgressDialog: ProgressDialog? = null
            override fun onPageFinished(view: WebView, url: String?) {
                view.settings.loadsImagesAutomatically = true
                binding.webViewCommon.visibility = View.VISIBLE
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

        if (webUrl!!.contains("pdf")) {
            binding.webViewCommon.loadUrl(
                "http://docs.google.com/gview?embedded=true&url=" +
                        BaseActivity.baseURL.dropLast(5) + "/media/" + webUrl
            )
        } else {
            binding.webViewCommon.loadUrl(BaseActivity.baseURL.dropLast(5) + webUrl!!)
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CommonWebViewFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CommonWebViewFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_Common_WebView"
    }
}