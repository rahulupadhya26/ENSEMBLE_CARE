package com.app.selfcare.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.R
import com.app.selfcare.adapters.DocumentListAdapter
import com.app.selfcare.controller.OnDocumentItemClickListener
import com.app.selfcare.controller.OnDocumentsConsentRoisViewItemClickListener
import com.app.selfcare.data.ConsentsRoisDocumentData
import com.app.selfcare.data.Documents
import com.app.selfcare.databinding.FragmentActivityCarePlanBinding
import com.app.selfcare.databinding.FragmentDocumentBinding
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper.get
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.json.JSONArray
import retrofit2.HttpException
import java.lang.reflect.Type
import kotlin.collections.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [DocumentFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DocumentFragment : BaseFragment(), OnDocumentItemClickListener,
    OnDocumentsConsentRoisViewItemClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentDocumentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDocumentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getLayout(): Int {
        return R.layout.fragment_document
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.GONE
        getSubTitle().visibility = View.GONE
        updateStatusBarColor(R.color.white)

        binding.documentBack.setOnClickListener {
            setBottomNavigation(null)
            setLayoutBottomNavigation(null)
            replaceFragmentNoBackStack(
                BottomNavigationFragment(),
                R.id.layout_home,
                BottomNavigationFragment.TAG
            )
        }

        getDocumentData()
    }

    private fun getDocumentData() {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .getDocumentsData(getAccessToken())
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
                            var documentList: ArrayList<Documents> = arrayListOf()
                            val jsonArray = JSONArray(responseBody)
                            for (i in 0 until jsonArray.length()) {
                                val jsonObj = jsonArray.getJSONObject(i).toString()
                                val documentType: Type = object : TypeToken<Documents?>() {}.type
                                val document: Documents = Gson().fromJson(jsonObj, documentType)
                                documentList.add(document)
                            }

                            /*val documentType: Type =
                                object : TypeToken<ArrayList<DocumentData?>?>() {}.type
                            val documentList: ArrayList<DocumentData> =
                                Gson().fromJson(responseBody, documentType)

                            for (i in 0 until documentList.size) {
                                documentList[i].dateTime =
                                    documentList[i].date + " " + documentList[i].time
                            }
                            val tempDocumentList: ArrayList<DocumentData> = arrayListOf()
                            for (documentData in documentList) {
                                if (!(documentData.prescriptions.isEmpty() &&
                                            documentData.consents.isEmpty() &&
                                            documentData.insurance.isEmpty())
                                ) {
                                    tempDocumentList.add(documentData)
                                }
                            }*/
                            /*var documents: ArrayList<SortedDocumentData> = arrayListOf()
                            val routesMap = documentList.groupBy { it.dateTime }
                            val keys = routesMap.keys
                            for (str in keys) {
                                var type = ""
                                var document: ArrayList<DocumentData> = arrayListOf()
                                val subMap = routesMap[str]!!.groupBy { it.type }
                                val subKeys = subMap.keys
                                for (str1 in subKeys) {
                                    val actualData = subMap[str1]
                                    type = str1
                                    document = actualData as ArrayList<DocumentData>
                                    documents.add(SortedDocumentData(str, type, document))
                                }
                            }*/
                            binding.recyclerViewDocumentList.apply {
                                layoutManager = LinearLayoutManager(
                                    requireActivity(), RecyclerView.VERTICAL, false
                                )
                                adapter = DocumentListAdapter(
                                    mActivity!!,
                                    documentList, this@DocumentFragment, this@DocumentFragment
                                )
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
                                getDocumentData()
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
         * @return A new instance of fragment DocumentFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            DocumentFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_document"
    }

    override fun onDocumentItemClickListener(imageList: ArrayList<String>, title: String) {
        replaceFragment(
            DisplayImagesFragment.newInstance(imageList, title),
            R.id.layout_home,
            DisplayImagesFragment.TAG
        )
    }

    override fun onDocumentsConsentRoisViewItemClickListener(consentsRoisDocumentData: ConsentsRoisDocumentData) {
        replaceFragment(
            ViewFormsFragment.newInstance(consentsRoisDocumentData),
            R.id.layout_home,
            ViewFormsFragment.TAG
        )
    }
}