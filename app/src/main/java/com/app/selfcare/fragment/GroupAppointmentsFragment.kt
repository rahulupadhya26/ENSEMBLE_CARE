package com.app.selfcare.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.selfcare.GroupVideoCall
import com.app.selfcare.R
import com.app.selfcare.adapters.GroupAppointmentsAdapter
import com.app.selfcare.controller.OnGroupAppointmentItemClickListener
import com.app.selfcare.data.GroupAppointment
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper.get
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.agora.agorauikit_android.AgoraVideoViewer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_group_appointments.*
import retrofit2.HttpException
import java.lang.reflect.Type

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [GroupAppointmentsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GroupAppointmentsFragment : BaseFragment(), OnGroupAppointmentItemClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun getLayout(): Int {
        return R.layout.fragment_group_appointments
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHeader().visibility = View.GONE
        getBackButton().visibility = View.VISIBLE
        getSubTitle().visibility = View.GONE

        //Appointments
        displayGroupAppointments()

        groupAppointRefresh.setOnClickListener {
            displayGroupAppointments()
        }
    }

    private fun displayGroupAppointments() {
        showProgress()
        runnable = Runnable {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .getGroupAppointmentList(getAccessToken())
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
                            var groupAppointmentLists: ArrayList<GroupAppointment> = arrayListOf()
                            val groupAppointmentList: Type =
                                object : TypeToken<ArrayList<GroupAppointment?>?>() {}.type
                            groupAppointmentLists =
                                Gson().fromJson(responseBody, groupAppointmentList)
                            if (groupAppointmentLists.isNotEmpty()) {
                                recyclerViewGroupAppointmentList.visibility = View.VISIBLE
                                txtNoGroupAppointmentList.visibility = View.GONE
                                recyclerViewGroupAppointmentList.apply {
                                    layoutManager = LinearLayoutManager(
                                        mActivity!!,
                                        RecyclerView.VERTICAL,
                                        false
                                    )
                                    adapter = GroupAppointmentsAdapter(
                                        mActivity!!,
                                        groupAppointmentLists, this@GroupAppointmentsFragment
                                    )
                                }
                            } else {
                                recyclerViewGroupAppointmentList.visibility = View.GONE
                                txtNoGroupAppointmentList.visibility = View.VISIBLE
                            }
                        } catch (e: Exception) {
                            hideProgress()
                            displayToast("Something went wrong.. Please try after sometime")
                        }
                    }, { error ->
                        hideProgress()
                        if ((error as HttpException).code() == 401) {
                            userLogin(
                                preference!![PrefKeys.PREF_EMAIL]!!,
                                preference!![PrefKeys.PREF_PASS]!!
                            ) { result ->
                                displayGroupAppointments()
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
         * @return A new instance of fragment GroupAppointmentsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            GroupAppointmentsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val TAG = "Screen_group_appointments"
    }

    var agView: AgoraVideoViewer? = null

    override fun onGroupAppointmentItemClickListener(groupAppointment: GroupAppointment) {
        // Create AgoraVideoViewer instance
        //displayMsg("Alert","Functionality under development")

        /*val intent = Intent(requireActivity(), GroupVideoCall::class.java)
        intent.putExtra("token", groupAppointment.rtc_token)
        intent.putExtra("channelName", groupAppointment.channel_name)
        startActivity(intent)
        requireActivity().overridePendingTransition(0, 0)*/
        /*replaceFragment(
            GroupVideoCallFragment.newInstance(
                groupAppointment.rtc_token,
                groupAppointment.channel_name
            ), R.id.layout_home, GroupVideoCallFragment.TAG
        )*/
        /*agView = AgoraVideoViewer(
            requireActivity(), AgoraConnectionData(BuildConfig.appId)
        )
        requireActivity().addContentView(
            agView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        val agoraSettings = AgoraSettings()
        val extraButton = AgoraButton(requireActivity())
        extraButton

        agView!!.join(
            groupAppointment.channel_name,
            groupAppointment.rtc_token,
            role = Constants.CLIENT_ROLE_BROADCASTER
        )*/
    }

    /*fun settingsWithExtraButtons(): AgoraSettings {
        val agoraSettings = AgoraSettings()
        agoraSettings.extraButtons[3].

        val agBeautyButton = AgoraButton(requireActivity())
        agBeautyButton.clickAction = {
            it.isSelected = !it.isSelected
            agBeautyButton.setImageResource(
                if (it.isSelected) R.drawable.btn_endcall else android.R.drawable.star_off
            )
            it.background.setTint(if (it.isSelected) Color.GREEN else Color.GRAY)
            this.agView!!.agkit?.setBeautyEffectOptions(it.isSelected, this.agView?.beautyOptions)
        }
        agBeautyButton.setImageResource(android.R.drawable.star_off)

        agoraSettings.extraButtons = mutableListOf(agBeautyButton)

        return agoraSettings
    }*/
}