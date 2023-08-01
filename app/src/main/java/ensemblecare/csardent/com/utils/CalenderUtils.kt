package ensemblecare.csardent.com.utils

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import android.util.Log
import android.widget.Toast
import ensemblecare.csardent.com.R
import java.util.*


object CalenderUtils {

    var eventId: Long = 0
    var calID: Long = 3

    // Projection array. Creating indices for this array instead of doing
// dynamic lookups improves performance.
    private val EVENT_PROJECTION: Array<String> = arrayOf(
        CalendarContract.Calendars._ID,                     // 0
        CalendarContract.Calendars.ACCOUNT_NAME,            // 1
        CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,   // 2
        CalendarContract.Calendars.OWNER_ACCOUNT            // 3
    )

    // The indices for the projection array above.
    private const val PROJECTION_ID_INDEX: Int = 0
    private const val PROJECTION_ACCOUNT_NAME_INDEX: Int = 1
    private const val PROJECTION_DISPLAY_NAME_INDEX: Int = 2
    private const val PROJECTION_OWNER_ACCOUNT_INDEX: Int = 3

    // Add an event to the calendar of the user.
    fun addEvent(
        context: Context,
        sampleDate: String,
        title: String,
        desc: String,
        duration: String,
        dailyCount: String,
        hourOfDay: String,
        reminderTimeBefore: Int
    ) {
        val sDate = DateUtils(sampleDate)
        val startMillis: Long = Calendar.getInstance().run {
            set(
                Integer.parseInt(sDate.getYear()),
                (Integer.parseInt(sDate.getMonthNoFormat()) - 1),
                Integer.parseInt(sDate.getDay()),
                Integer.parseInt(hourOfDay),
                0
            )
            timeInMillis
        }
        try {
            val calendarId = getCalendarId(context)
            //Toast.makeText(context, "Calendar ID - $calendarId", Toast.LENGTH_LONG).show()
            if (calendarId != null) {
                //Call operations e.g.: Insert operation
            }
            val cr: ContentResolver = context.contentResolver

            // Run query
            val queryUri: Uri = CalendarContract.Calendars.CONTENT_URI
            val cur: Cursor = cr.query(queryUri, EVENT_PROJECTION, null, null, null)!!
            // Use the cursor to step through the returned records
            while (cur.moveToNext()) {
                // Get the field values
                calID = cur.getLong(PROJECTION_ID_INDEX)
                val displayName: String = cur.getString(PROJECTION_DISPLAY_NAME_INDEX)
                val accountName: String = cur.getString(PROJECTION_ACCOUNT_NAME_INDEX)
                val ownerName: String = cur.getString(PROJECTION_OWNER_ACCOUNT_INDEX)
            }

            val values = ContentValues()
            values.put(CalendarContract.Events.DTSTART, startMillis)
            values.put(CalendarContract.Events.DTEND, startMillis + 60 * 60 * 1000)
            values.put(CalendarContract.Events.TITLE, title)
            values.put(CalendarContract.Events.DESCRIPTION, desc)
            val durationData: Array<String> =
                context.resources.getStringArray(R.array.goal_duration)
            when (duration) {
                durationData[0] -> {
                    values.put(CalendarContract.Events.DTEND, startMillis + 60 * 60 * 1000)
                }

                durationData[1] -> {
                    values.put(CalendarContract.Events.RRULE, "FREQ=DAILY;COUNT=$dailyCount")
                }

                durationData[2] -> {
                    values.put(CalendarContract.Events.RRULE, "FREQ=WEEKLY;COUNT=10")
                }

                durationData[3] -> {
                    values.put(CalendarContract.Events.RRULE, "FREQ=MONTHLY;COUNT=12")
                }

                durationData[4] -> {
                    values.put(CalendarContract.Events.RRULE, "FREQ=YEARLY;COUNT=10")
                }
            }
            values.put(CalendarContract.Events.CALENDAR_ID, calendarId)
            values.put(CalendarContract.Events.EVENT_TIMEZONE, Calendar.getInstance().timeZone.id)

            /*Toast.makeText(
                context,
                "Calendar ID - $calendarId",Toast.LENGTH_LONG
            ).show()*/
            println(Calendar.getInstance().timeZone.id)
            if (!isEventInCalendar(context, title, startMillis, (startMillis + 60 * 60 * 1000))) {
                val uri: Uri? = cr.insert(CalendarContract.Events.CONTENT_URI, values)

                // Save the eventId into the Task object for possible future delete.
                this.eventId = uri!!.lastPathSegment!!.toLong()
                // Add a 5 minute, 1 hour and 1 day reminders (3 reminders)
                setReminder(context, cr, this.eventId, reminderTimeBefore)
                //setReminder(cr, this.eventId, 60)
                //setReminder(cr, this.eventId, 1440)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // routine to add reminders with the event
    @SuppressLint("Range")
    private fun setReminder(context: Context, cr: ContentResolver, eventID: Long, timeBefore: Int) {
        try {
            val values = ContentValues()
            values.put(CalendarContract.Reminders.MINUTES, timeBefore)
            //values.put(CalendarContract.Reminders.DURATION, duration)
            values.put(CalendarContract.Reminders.EVENT_ID, eventID)
            values.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT)
            val uri = cr.insert(CalendarContract.Reminders.CONTENT_URI, values)
            val c: Cursor = CalendarContract.Reminders.query(
                cr,
                eventID,
                arrayOf(CalendarContract.Reminders.MINUTES)
            )
            if (c.moveToFirst()) {
                println(
                    "calendar" + c.getInt(c.getColumnIndex(CalendarContract.Reminders.MINUTES))
                )
            }
            c.close()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            Toast.makeText(
                context,
                e.toString(),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // function to remove an event from the calendar using the eventId stored within the Task object.
    fun removeEvent(context: Context) {
        val cr = context.contentResolver
        val iNumRowsDeleted: Int
        val deleteUri: Uri =
            ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId)
        iNumRowsDeleted = cr.delete(deleteUri, null, null)
        Log.i("DEBUG_TAG", "Deleted $iNumRowsDeleted calendar entry.")
    }

    private fun getCalendarId(context: Context): Long? {
        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME
        )

        var calCursor = context.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection,
            CalendarContract.Calendars.VISIBLE + " = 1 AND " + CalendarContract.Calendars.IS_PRIMARY + "=1",
            null,
            CalendarContract.Calendars._ID + " ASC"
        )

        if (calCursor != null && calCursor.count <= 0) {
            calCursor = context.contentResolver.query(
                CalendarContract.Calendars.CONTENT_URI,
                projection,
                CalendarContract.Calendars.VISIBLE + " = 1",
                null,
                CalendarContract.Calendars._ID + " ASC"
            )
        }

        if (calCursor != null) {
            if (calCursor.moveToFirst()) {
                val calName: String
                val calID: String
                val nameCol = calCursor.getColumnIndex(projection[1])
                val idCol = calCursor.getColumnIndex(projection[0])

                calName = calCursor.getString(nameCol)
                calID = calCursor.getString(idCol)

                calCursor.close()
                return calID.toLong()
            }
        }
        return null
    }

    @SuppressLint("Range")
    private fun isEventInCalendar(
        context: Context,
        titleText: String,
        dtStart: Long,
        dtEnd: Long
    ): Boolean {
        val projection = arrayOf(
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END,
            CalendarContract.Instances.TITLE
        )
        val cursor =
            CalendarContract.Instances.query(context.contentResolver, projection, dtStart, dtEnd)
        return cursor != null && cursor.moveToFirst() && cursor.getString(
            cursor.getColumnIndex(
                CalendarContract.Instances.TITLE
            )
        ).equals(titleText, ignoreCase = true)
    }
}