package com.app.selfcare.utils

import java.text.SimpleDateFormat
import java.util.*


class DateUtils(date: String?) {
    var mDate: Date? = null

    init {
        mDate = SimpleDateFormat("MM/dd/yyyy' 'HH:mm:ss", Locale.getDefault()).parse(date!!)
    }

    fun getSimpleFormattedDate():String{
        return SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(mDate!!.time)
    }

    fun getFormattedDate(): String {
        return SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(mDate!!.time)
    }

    fun getDay(): String {
        return SimpleDateFormat("dd", Locale.getDefault()).format(mDate!!.time)
    }

    fun getCurrentDay(): String {
        return SimpleDateFormat("EEEE", Locale.getDefault()).format(mDate!!.time)
    }

    fun getMonth(): String {
        return SimpleDateFormat("MMM", Locale.getDefault()).format(mDate!!.time)
    }

    fun getFullMonthName(): String {
        return SimpleDateFormat("MMMM", Locale.getDefault()).format(mDate!!.time)
    }

    fun getMonthNoFormat(): String {
        return SimpleDateFormat("MM", Locale.getDefault()).format(mDate!!.time)
    }

    fun getYear(): String {
        return SimpleDateFormat("yyyy", Locale.getDefault()).format(mDate!!.time)
    }

    fun getMonthYear(): String {
        return SimpleDateFormat("MMMM, yyyy", Locale.getDefault()).format(mDate!!.time)
    }

    fun getTime(): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(mDate!!.time)
    }

    fun getTimeWithFormat(): String {
        return SimpleDateFormat("HH:mm a", Locale.getDefault()).format(mDate!!.time)
    }

    fun getHourOfDay(): String {
        return SimpleDateFormat("h", Locale.getDefault()).format(mDate!!.time)
    }

    fun getMinutes(): String {
        return SimpleDateFormat("mm", Locale.getDefault()).format(mDate!!.time)
    }

    fun getDayNumberSuffix(day: Int): String? {
        return if (day in 11..13) {
            "th"
        } else when (day % 10) {
            1 -> "st"
            2 -> "nd"
            3 -> "rd"
            else -> "th"
        }
    }
}