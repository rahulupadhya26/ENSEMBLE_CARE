package com.app.selfcare.controller

import android.view.View
import android.widget.TextView

interface IController {
    fun getHeader(): View
    fun getBackButton(): View
    fun swipeSliderEnable(flag: Boolean)
    fun getSubTitle(): TextView
    fun showProgress()
    fun hideProgress()
    fun captureImage()
    fun uploadPicture()
    fun uploadVideo()
    fun clearTempFormData()
    fun getPhotoPath(): String
    fun getBitmapList(): ArrayList<String>
    fun hideKeyboard(view: View)
    fun setBottomMenu(id: Int)
}