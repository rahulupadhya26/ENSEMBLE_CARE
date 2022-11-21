package com.app.selfcare.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.View
import android.app.ActivityManager
import android.util.Base64
import android.util.Log
import android.widget.ImageView
import androidx.core.graphics.drawable.toBitmap
import com.app.selfcare.MainActivity
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException


object Utils {

    const val CONST_ENCRYPT_DECRYPT = true
    var rtmLoggedIn = false

    const val CONST_LIKE = "Likes"

    const val CONST_EVENT_RESPONSE = "eventresponse"

    const val CONST_SURVEY_RESPONSE = "surveyresponse"

    const val CONST_UPDATE_PROFILE = "updateProfile"

    const val CONST_QUESTION_TYPE_TEXT = "Text"
    const val CONST_QUESTION_TYPE_TEXT_BOX = "Textbox"
    const val CONST_QUESTION_TYPE_RADIO = "Radio"
    const val CONST_QUESTION_TYPE_CHECKBOX = "Multiselect"
    const val CONST_QUESTION_TYPE_DROPDOWN = "Dropdown"
    const val CONST_QUESTION_TYPE_DATE = "Date"
    const val CONST_QUESTION_TYPE_SCALE = "Scale"
    const val CONST_QUESTION_TYPE_RATING = "Rating"

    const val CONST_EVENT_GOING = "Going"
    const val CONST_EVENT_INTERESTED = "Interested"
    const val CONST_EVENT_CANTGO = "Cant_Go"

    const val CONST_STATUS_ACTION_CHANGE_STATUS = "modifyStatus"
    const val CONST_STATUS_ACTION_RESET_STATUS = "resetStatus"
    const val CONST_STATUS_ACTION_CHANGE_PASSWORD = "changePassword"

    const val SYNC_HEALTH_LOGIN_TLH03 = "QMTBnH3KBDcE4R+sIFSmmg=="
    const val SYNC_HEALTH_LOGIN_TLH04 = "Vc/3vSLVeUHaV29+zzsfPQ=="
    const val SYNC_HEALTH_LOGIN_TLH05 = "stkU52WDpBdF31YE5FDqQg=="
    const val SYNC_HEALTH_LOGIN_TLH06 = "nwjJl7mFhpQvIaQKhq2aHw=="
    const val SYNC_HEALTH_LOGIN_TLH07 = "hJ+Rh05X1NOsXir+bFfvnA=="
    const val SYNC_HEALTH_LOGIN_TLH08 = "oy6sYo7kR5xDaAWg+0dGkQ=="

    const val EVENT_START_APPOINTMENT = 1
    const val EVENT_CANCEL_APPOINTMENT = 2
    const val EVENT_APPOINTMENT_CHAT = 3
    const val EVENT_APPOINTMENT_EMAIL = 4

    const val QUICK_OPTION_GOALS = 1
    const val QUICK_OPTION_ARTICLES = 2
    const val QUICK_OPTION_VIDEOS = 3
    const val QUICK_OPTION_NEWS = 4
    const val QUICK_OPTION_PODCASTS = 5
    const val QUICK_OPTION_JOURNALS = 6
    const val QUICK_OPTION_DISCUSSIONS = 7

    const val CONST_SYMPTOMS_LIST = "Symptoms"
    const val CONST_ALLERGY_LIST = "allergy_issue_list"

    const val SYNC_HEALTH_BASE_URL = "https://demoehr.csardent.com"

    //const val SYNC_HEALTH_BASE_URL = "https://ehr.psyclarity.csardent.com"
    const val SYNC_HEALTH_URL_PART = "/apis/v2/"

    const val INTRO_SCREEN = 1
    const val QUESTIONNAIRE = 2
    const val REGISTER = 3
    const val PLAN_PAY = 4

    const val TABLE_VIDEO = "PI0014"
    const val TABLE_PODCAST = "PI0014"
    const val TABLE_ARTICLES = "PI0014"
    const val TABLE_GOALS = "PI0014"
    const val TABLE_JOURNALS = "PI0014"

    const val APPOINTMENT_CREATED = 1
    const val APPOINTMENT_BOOKED = 2
    const val APPOINTMENT_ONGOING = 3
    const val APPOINTMENT_COMPLETED = 4
    const val APPOINTMENT_CANCEL = 5

    const val RECOMMENDED_VIDEOS = 0
    const val RECOMMENDED_PODCAST = 1
    const val RECOMMENDED_ARTICLES = 2
    const val RECOMMENDED_PROVIDER_GOAL = 3

    var firstName = ""
    var middleName = ""
    var lastName = ""
    var ssn = ""
    var dob = ""
    var gender = ""
    var email = ""
    var phoneNo = ""
    var pass = ""
    var confirmPass = ""
    var employeeId = ""
    var employer = ""
    var refEmp = false
    var userType = "Patient"

    var selectedSymptoms = ""
    var selectedStreet = ""
    var selectedCity = ""
    var selectedState = ""
    var selectedPostalCode = ""
    var selectedCountry = ""
    var selectedPhoneNo = ""
    var selectedCommunicationMode = ""
    var providerId = ""
    var providerPublicId = ""
    var providerType = ""
    var providerName = ""
    var aptScheduleDate = ""
    var aptScheduleTime = ""
    var aptEndTime = "00:30:00"
    var pharmacyVal = ""
    var allergies = ""
    var medication = ""
    var details = ""
    var appointmentId = ""
    var timeSlotId = ""
    var apptPcId = ""
    var isTherapististScreen = false

    const val NAVIGATE_FROM_DASHBOARD = "fromDashboard"
    const val QUICK_BOOK = "quick_book"

    const val SHARE_MOMENT = "share moment"
    const val SHARE_GRATITUDE = "share gratitude"
    const val FEED_LIKE = "feed like"
    const val FEED_COMMENT = "feed comment"
    const val FEED_SHARE = "feed share"
    const val ARTICLE_LIKE = "article like"
    const val ARTICLE_COMMENT = "article comment"
    const val ARTICLE_SHARE = "article share"
    const val VIDEO_LIKE = "video like"
    const val VIDEO_COMMENT = "video comment"
    const val VIDEO_SHARE = "video share"
    const val NAV_EVENT = "navigated to event"
    const val NAV_VIDEO = "navigated to video"
    const val NAV_EXPLORE = "navigated to explore"
    const val NAV_LOGOUT = "navigated to logout"
    const val NAV_CHALLENGE = "navigated to challenge"
    const val NAV_SYNC_HEALTH = "navigated to sync health"
    const val NAV_JOURNALS = "navigated to journals"
    const val NAV_QUOTES = "navigated to quotes"
    const val NAV_SOBRIETY_CLOCK = "navigated to sobriety clock"

    const val NSFW_CONFIDENCE_THRESHOLD = 0.7
    const val LABEL_SFW = "nude"
    const val LABEL_NSFW = "nonnude"

    val NSFW_WORDS = listOf(
        "fuck",
        "bitch",
        "ass",
        "bastard",
        "sex",
        "bellend",
        "butt",
        "cunt",
        "shit",
        "dumbass",
        "fucker",
        "witch",
        "bullshit",
        "motherfucker",
        "dick"
    )

    const val CONST_1_BID = "Twice a day"
    const val CONST_2_TID = "Three times a day"
    const val CONST_3_QID = "Four times a day"
    const val CONST_4_Q3H = "Every 3 hours"
    const val CONST_5_Q4H = "Every 4 hours"
    const val CONST_6_Q5H = "Every 5 hours"
    const val CONST_7_Q6H = "Every 6 hours"
    const val CONST_8_Q8H = "Every 8 hours"
    const val CONST_9_QD = "Once a day"
    const val CONST_10_AC = "Before meals"
    const val CONST_11_PC = "After meals"
    const val CONST_12_AM = "Morning"
    const val CONST_13_PM = "Evening"
    const val CONST_14_ANTE = "In front of"
    const val CONST_15_H = "H"
    const val CONST_16_HS = "Hours of sleep"
    const val CONST_17_PRN = "When necessary"
    const val CONST_18_STAT = "Shortest turn around time"

    const val CONST_1 = "1"
    const val CONST_2 = "2"
    const val CONST_3 = "3"
    const val CONST_4 = "4"
    const val CONST_5 = "5"
    const val CONST_6 = "6"
    const val CONST_7 = "7"
    const val CONST_8 = "8"
    const val CONST_9 = "9"
    const val CONST_10 = "10"
    const val CONST_11 = "11"
    const val CONST_12 = "12"
    const val CONST_13 = "13"
    const val CONST_14 = "14"
    const val CONST_15 = "15"
    const val CONST_16 = "16"
    const val CONST_17 = "17"
    const val CONST_18 = "18"

    fun viewToImage(view: View): Bitmap? {
        val returnedBitmap =
            Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnedBitmap)
        val bgDrawable = view.background
        if (bgDrawable != null) bgDrawable.draw(canvas) else canvas.drawColor(Color.WHITE)
        view.draw(canvas)
        return returnedBitmap
    }

    fun getDisplayMetrics(activity: MainActivity): DisplayMetrics {
        val displayMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics
    }

    fun convertDpToPx(dp: Float, context: Context): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            getDisplayMetrics(context as MainActivity)
        ).toInt()
    }

    fun isServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
        for (service in manager!!.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                Log.i("isMyServiceRunning?", true.toString() + "")
                return true
            }
        }
        Log.i("isMyServiceRunning?", false.toString() + "")
        return false
    }

    @Throws(IOException::class)
    fun getFileFromAssets(context: Context, fileName: String): File =
        File(context.cacheDir, fileName)
            .also {
                if (!it.exists()) {
                    it.outputStream().use { cache ->
                        context.assets.open(fileName).use { inputStream ->
                            inputStream.copyTo(cache)
                        }
                    }
                }
            }

    fun convertImageToBitmap(imageView: ImageView): Bitmap {
        return imageView.drawable.toBitmap()
    }

    fun convert(bitmap: Bitmap): String? {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
    }
}