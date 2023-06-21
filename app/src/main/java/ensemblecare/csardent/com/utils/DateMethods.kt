package ensemblecare.csardent.com.utils

import android.annotation.SuppressLint
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class DateMethods {

    /**
     *
     * Checks if two dates are on the same day ignoring time.
     * @param date1  the first date, not altered, not null
     * @param date2  the second date, not altered, not null
     * @return true if they represent the same day
     * @throws IllegalArgumentException if either date is `null`
     */
    fun isSameDay(date1: Date?, date2: Date?): Boolean {
        require(!(date1 == null || date2 == null)) { "The dates must not be null" }
        val cal1: Calendar = Calendar.getInstance()
        cal1.time = date1
        val cal2: Calendar = Calendar.getInstance()
        cal2.time = date2
        return isSameDay(cal1, cal2)
    }

    /**
     *
     * Checks if two calendars represent the same day ignoring time.
     * @param cal1  the first calendar, not altered, not null
     * @param cal2  the second calendar, not altered, not null
     * @return true if they represent the same day
     * @throws IllegalArgumentException if either calendar is `null`
     */
    fun isSameDay(cal1: Calendar?, cal2: Calendar?): Boolean {
        require(!(cal1 == null || cal2 == null)) { "The dates must not be null" }
        return cal1.get(Calendar.ERA) === cal2.get(Calendar.ERA) && cal1.get(Calendar.YEAR) === cal2.get(
            Calendar.YEAR
        ) && cal1.get(Calendar.DAY_OF_YEAR) === cal2.get(Calendar.DAY_OF_YEAR)
    }

    /**
     *
     * Checks if a date is today.
     * @param date the date, not altered, not null.
     * @return true if the date is today.
     * @throws IllegalArgumentException if the date is `null`
     */
    fun isToday(date: Date?): Boolean {
        return isSameDay(date, Calendar.getInstance().getTime())
    }

    /**
     *
     * Checks if a calendar date is today.
     * @param cal  the calendar, not altered, not null
     * @return true if cal date is today
     * @throws IllegalArgumentException if the calendar is `null`
     */
    fun isToday(cal: Calendar?): Boolean {
        return isSameDay(cal, Calendar.getInstance())
    }

    /**
     *
     * Checks if the first date is before the second date ignoring time.
     * @param date1 the first date, not altered, not null
     * @param date2 the second date, not altered, not null
     * @return true if the first date day is before the second date day.
     * @throws IllegalArgumentException if the date is `null`
     */
    fun isBeforeDay(date1: Date?, date2: Date?): Boolean {
        require(!(date1 == null || date2 == null)) { "The dates must not be null" }
        val cal1: Calendar = Calendar.getInstance()
        cal1.setTime(date1)
        val cal2: Calendar = Calendar.getInstance()
        cal2.setTime(date2)
        return isBeforeDay(cal1, cal2)
    }

    /**
     *
     * Checks if the first calendar date is before the second calendar date ignoring time.
     * @param cal1 the first calendar, not altered, not null.
     * @param cal2 the second calendar, not altered, not null.
     * @return true if cal1 date is before cal2 date ignoring time.
     * @throws IllegalArgumentException if either of the calendars are `null`
     */
    fun isBeforeDay(cal1: Calendar?, cal2: Calendar?): Boolean {
        require(!(cal1 == null || cal2 == null)) { "The dates must not be null" }
        if (cal1.get(Calendar.ERA) < cal2.get(Calendar.ERA)) return true
        if (cal1.get(Calendar.ERA) > cal2.get(Calendar.ERA)) return false
        if (cal1.get(Calendar.YEAR) < cal2.get(Calendar.YEAR)) return true
        return if (cal1.get(Calendar.YEAR) > cal2.get(Calendar.YEAR)) false else cal1.get(Calendar.DAY_OF_YEAR) < cal2.get(
            Calendar.DAY_OF_YEAR
        )
    }

    /**
     *
     * Checks if the first date is after the second date ignoring time.
     * @param date1 the first date, not altered, not null
     * @param date2 the second date, not altered, not null
     * @return true if the first date day is after the second date day.
     * @throws IllegalArgumentException if the date is `null`
     */
    fun isAfterDay(date1: Date?, date2: Date?): Boolean {
        require(!(date1 == null || date2 == null)) { "The dates must not be null" }
        val cal1: Calendar = Calendar.getInstance()
        cal1.setTime(date1)
        val cal2: Calendar = Calendar.getInstance()
        cal2.setTime(date2)
        return isAfterDay(cal1, cal2)
    }

    /**
     *
     * Checks if the first calendar date is after the second calendar date ignoring time.
     * @param cal1 the first calendar, not altered, not null.
     * @param cal2 the second calendar, not altered, not null.
     * @return true if cal1 date is after cal2 date ignoring time.
     * @throws IllegalArgumentException if either of the calendars are `null`
     */
    fun isAfterDay(cal1: Calendar?, cal2: Calendar?): Boolean {
        require(!(cal1 == null || cal2 == null)) { "The dates must not be null" }
        if (cal1.get(Calendar.ERA) < cal2.get(Calendar.ERA)) return false
        if (cal1.get(Calendar.ERA) > cal2.get(Calendar.ERA)) return true
        if (cal1.get(Calendar.YEAR) < cal2.get(Calendar.YEAR)) return false
        return if (cal1.get(Calendar.YEAR) > cal2.get(Calendar.YEAR)) true else cal1.get(Calendar.DAY_OF_YEAR) > cal2.get(
            Calendar.DAY_OF_YEAR
        )
    }

    /**
     *
     * Checks if a date is after today and within a number of days in the future.
     * @param date the date to check, not altered, not null.
     * @param days the number of days.
     * @return true if the date day is after today and within days in the future .
     * @throws IllegalArgumentException if the date is `null`
     */
    fun isWithinDaysFuture(date: Date?, days: Int): Boolean {
        requireNotNull(date) { "The date must not be null" }
        val cal: Calendar = Calendar.getInstance()
        cal.setTime(date)
        return isWithinDaysFuture(cal, days)
    }

    /**
     *
     * Checks if a calendar date is after today and within a number of days in the future.
     * @param cal the calendar, not altered, not null
     * @param days the number of days.
     * @return true if the calendar date day is after today and within days in the future .
     * @throws IllegalArgumentException if the calendar is `null`
     */
    fun isWithinDaysFuture(cal: Calendar?, days: Int): Boolean {
        requireNotNull(cal) { "The date must not be null" }
        val today: Calendar = Calendar.getInstance()
        val future: Calendar = Calendar.getInstance()
        future.add(Calendar.DAY_OF_YEAR, days)
        return isAfterDay(cal, today) && !isAfterDay(cal, future)
    }

    /** Returns the given date with the time set to the start of the day.  */
    fun getStart(date: Date?): Date? {
        return clearTime(date)
    }

    /** Returns the given date with the time values cleared.  */
    fun clearTime(date: Date?): Date? {
        if (date == null) {
            return null
        }
        val c: Calendar = Calendar.getInstance()
        c.setTime(date)
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.getTime()
    }

    /** Determines whether or not a date has any time values (hour, minute,
     * seconds or millisecondsReturns the given date with the time values cleared. */

    /** Determines whether or not a date has any time values (hour, minute,
     * seconds or millisecondsReturns the given date with the time values cleared.  */
    /**
     * Determines whether or not a date has any time values.
     * @param date The date.
     * @return true iff the date is not null and any of the date's hour, minute,
     * seconds or millisecond values are greater than zero.
     */
    fun hasTime(date: Date?): Boolean {
        if (date == null) {
            return false
        }
        val c: Calendar = Calendar.getInstance()
        c.setTime(date)
        if (c.get(Calendar.HOUR_OF_DAY) > 0) {
            return true
        }
        if (c.get(Calendar.MINUTE) > 0) {
            return true
        }
        if (c.get(Calendar.SECOND) > 0) {
            return true
        }
        return if (c.get(Calendar.MILLISECOND) > 0) {
            true
        } else false
    }

    /** Returns the given date with time set to the end of the day  */
    fun getEnd(date: Date?): Date? {
        if (date == null) {
            return null
        }
        val c: Calendar = Calendar.getInstance()
        c.setTime(date)
        c.set(Calendar.HOUR_OF_DAY, 23)
        c.set(Calendar.MINUTE, 59)
        c.set(Calendar.SECOND, 59)
        c.set(Calendar.MILLISECOND, 999)
        return c.getTime()
    }

    /**
     * Returns the maximum of two dates. A null date is treated as being less
     * than any non-null date.
     */
    fun max(d1: Date?, d2: Date?): Date? {
        if (d1 == null && d2 == null) return null
        if (d1 == null) return d2
        if (d2 == null) return d1
        return if (d1.after(d2)) d1 else d2
    }

    /**
     * Returns the minimum of two dates. A null date is treated as being greater
     * than any non-null date.
     */
    fun min(d1: Date?, d2: Date?): Date? {
        if (d1 == null && d2 == null) return null
        if (d1 == null) return d2
        if (d2 == null) return d1
        return if (d1.before(d2)) d1 else d2
    }

    @SuppressLint("SimpleDateFormat")
    fun checkTimings(time: String, endTime: String): Boolean {
        val pattern = "HH:mm"
        val sdf = SimpleDateFormat(pattern)
        try {
            val date1: Date = sdf.parse(time)
            val date2: Date = sdf.parse(endTime)
            return date1.after(date2)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return false
    }

    /** The maximum date possible.  */
    var MAX_DATE: Date = Date(Long.MAX_VALUE)
}