
package com.app.selfcare.utils

import java.text.SimpleDateFormat
import java.util.*


class DateUtils(date: String?) {
    var mDate:Date? = null

    init {
        mDate = SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss", Locale.getDefault()).parse(date!!)
    }

    fun getFormattedDate():String
    {
        return SimpleDateFormat("dd MMM, yyyy",Locale.getDefault()).format(mDate!!.time)
    }

    fun getDay():String
    {
        return SimpleDateFormat("dd",Locale.getDefault()).format(mDate!!.time)
    }

    fun getMonth():String
    {
        return SimpleDateFormat("MMM",Locale.getDefault()).format(mDate!!.time)
    }

    fun getMonthNoFormat():String
    {
        return SimpleDateFormat("MM",Locale.getDefault()).format(mDate!!.time)
    }

    fun getYear():String
    {
        return SimpleDateFormat("yyyy",Locale.getDefault()).format(mDate!!.time)
    }

    fun getMonthYear():String
    {
        return SimpleDateFormat("MMM , yyyy",Locale.getDefault()).format(mDate!!.time)
    }

    fun getTime():String
    {
        return SimpleDateFormat("h:mm a",Locale.getDefault()).format(mDate!!.time)
    }

    fun getHourOfDay():String
    {
        return SimpleDateFormat("h",Locale.getDefault()).format(mDate!!.time)
    }

    fun getMinutes():String
    {
        return SimpleDateFormat("mm",Locale.getDefault()).format(mDate!!.time)
    }
}