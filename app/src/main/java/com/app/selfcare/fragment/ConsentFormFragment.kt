package com.app.selfcare.fragment

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Base64
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.app.selfcare.R
import com.app.selfcare.utils.SignatureView
import kotlinx.android.synthetic.main.fragment_consent_form.*
import java.io.ByteArrayOutputStream

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ConsentFormFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ConsentFormFragment : BaseFragment(), SignatureView.OnSignedListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    var bSigned: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_consent_form
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE
        layout_consent_letter.visibility = View.VISIBLE
        layout_screenshot.visibility = View.GONE

        signatureView.setOnSignedListener(this)
        signatureView.clear()
        btnConsentLetter.setOnClickListener {
            if (bSigned) {
                val screenshot: Bitmap = takeScreenshotOfRootView(layout_consent_letter)
                img_screenshot.setImageBitmap(screenshot)
                layout_consent_letter.visibility = View.GONE
                layout_screenshot.visibility = View.VISIBLE
                //uploadConsentLetter(screenshot)
            } else {
                displayToast("Please sign the consent letter")
            }
        }

        btn_clear.setOnClickListener {
            signatureView.clear()
        }

        txt_screenshot_close.setOnClickListener {
            replaceFragmentNoBackStack(
                AppointCongratFragment(),
                R.id.layout_home,
                AppointCongratFragment.TAG
            )
        }
    }

    fun convert(bitmap: Bitmap): String? {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ConsentFormFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ConsentFormFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
        private fun takeScreenshot(view: View): Bitmap {
            view.isDrawingCacheEnabled = true
            view.buildDrawingCache(true)
            val b = Bitmap.createBitmap(view.drawingCache)
            view.buildDrawingCache(false)
            return b
        }

        fun takeScreenshotOfRootView(v: View): Bitmap {
            return takeScreenshot(v.rootView)
        }
        const val TAG = "Screen_consent_letter"
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