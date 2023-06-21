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
import ensemblecare.csardent.com.BaseActivity
import ensemblecare.csardent.com.R
import ensemblecare.csardent.com.data.ToDoData
import ensemblecare.csardent.com.data.ToDoSignature
import ensemblecare.csardent.com.databinding.DialogSignFormBinding
import ensemblecare.csardent.com.databinding.FragmentAssignedToDoWebViewBinding
import ensemblecare.csardent.com.preference.PrefKeys
import ensemblecare.csardent.com.preference.PreferenceHelper.get
import ensemblecare.csardent.com.utils.SignatureView
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AssignedToDoWebViewFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AssignedToDoWebViewFragment : BaseFragment(), SignatureView.OnSignedListener {
    // TODO: Rename and change types of parameters
    private var toDoData: ToDoData? = null
    private var param2: String? = null
    var bSigned: Boolean = false
    private var createSignFormSheet: BottomSheetDialog? = null
    private lateinit var binding: FragmentAssignedToDoWebViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            toDoData = it.getParcelable(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_assigned_to_do_web_view
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAssignedToDoWebViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE

        binding.assignedToDoWebViewBack.setOnClickListener {
            popBackStack()
        }

        binding.assignedToDoSign.setOnClickListener {
            displaySignaturePad()
        }

        val browser = binding.webViewAssignedToDo.settings
        browser.javaScriptEnabled = true
        browser.builtInZoomControls = true
        browser.pluginState = WebSettings.PluginState.ON
        binding.webViewAssignedToDo.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                view?.loadUrl(url!!)
                return true
            }
        }

        binding.webViewAssignedToDo.webViewClient = object : WebViewClient() {
            private var mProgressDialog: ProgressDialog? = null
            override fun onPageFinished(view: WebView, url: String?) {
                view.settings.loadsImagesAutomatically = true
                binding.webViewAssignedToDo.visibility = View.VISIBLE
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

        binding.webViewAssignedToDo.loadUrl(
            BaseActivity.baseURL.dropLast(5) + toDoData!!.assessment_link
        )
    }

    private fun sendFormSignature(signature: Bitmap) {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getReqRespInterface()
                    .sendAssignedToDoSign(
                        toDoData!!.assessment_link.drop(1),
                        ToDoSignature("data:image/jpg;base64," + convert(signature))
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
                            popBackStack()
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

    private fun displaySignaturePad() {
        createSignFormSheet = BottomSheetDialog(requireActivity(), R.style.SheetDialog)
        val formSignDialog = DialogSignFormBinding.inflate(layoutInflater)
        val view = formSignDialog.root
        createSignFormSheet!!.setContentView(view)
        createSignFormSheet!!.setCanceledOnTouchOutside(true)
        formSignDialog.txtViewDocument.visibility = View.GONE
        formSignDialog.txtFormTitle.text = toDoData!!.title
        formSignDialog.formSignatureView.setOnSignedListener(this)
        formSignDialog.formSignatureView.clear()
        formSignDialog.btnClearFormSigned.setOnClickListener {
            formSignDialog.formSignatureView.clear()
        }
        formSignDialog.btnConfirmSignature.setOnClickListener {
            if (bSigned) {
                binding.webViewAssignedToDo.loadUrl("javascript:data_submit()")
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
         * @return A new instance of fragment AssignedToDoWebViewFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: ToDoData, param2: String = "") =
            AssignedToDoWebViewFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_assigned_todo_web_view"
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