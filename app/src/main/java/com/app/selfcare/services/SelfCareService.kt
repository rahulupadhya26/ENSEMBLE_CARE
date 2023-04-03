package com.app.selfcare.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Location
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.app.selfcare.MainActivity
import com.app.selfcare.R
import com.app.selfcare.crypto.DecryptionImpl
import com.app.selfcare.crypto.EncryptionImpl
import com.app.selfcare.data.HealthInfo
import com.app.selfcare.interceptor.DecryptionInterceptor
import com.app.selfcare.interceptor.EncryptionInterceptor
import com.app.selfcare.preference.PrefKeys
import com.app.selfcare.preference.PreferenceHelper
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.*
import com.app.selfcare.preference.PreferenceHelper.get
import com.app.selfcare.preference.PreferenceHelper.set
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class SelfCareService : Service() {

    var interval: Long = 1000 * 60 * 5
    var delay: Long = 5000
    private var timer: Timer? = null
    private var timerTask: TimerTask? = null
    val TAG: String = "SelfCareService"
    var totalSteps = "0"
    var totalDistance = "0.0"
    var totalCalories = "0.0"
    var totalHeartRate = "0.0"
    var latitude = "0.0"
    var longitude = "0.0"
    val context: Context = this
    var mCompositeDisposable: CompositeDisposable = CompositeDisposable()
    private var preference: SharedPreferences? = null

    private val fitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE)
        .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
        .addDataType(DataType.TYPE_DISTANCE_DELTA)
        .addDataType(DataType.TYPE_HEART_RATE_BPM)
        .addDataType(DataType.TYPE_CALORIES_EXPENDED)
        .build()

    override fun onCreate() {
        super.onCreate()
        val channelId =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel("my_service", "My Background Service")
            } else {
                // If earlier version channel ID is not used
                // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
                ""
            }

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
        val notification = notificationBuilder.setOngoing(true)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setLargeIcon(BitmapFactory.decodeResource(this.resources, R.mipmap.ic_launcher))
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setContentTitle("DiscoverTx")
            .build()
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
            startForeground(1, notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String {
        val chan = NotificationChannel(
            channelId,
            channelName, NotificationManager.IMPORTANCE_NONE
        )
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        preference = PreferenceHelper.defaultPrefs(context)
        startTimer();
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceStarted = false
        stopTimerTask()
        if (mCompositeDisposable.size() >= 1) {
            mCompositeDisposable.clear()
        }
        val broadcastIntent = Intent()
        broadcastIntent.action = "restartservice"
        broadcastIntent.setClass(this, RestartBroadcastReceiver::class.java)
        this.sendBroadcast(broadcastIntent)
    }

    companion object {
        var mLocation: Location? = null
        var isServiceStarted = false
    }

    private fun startTimer() {
        isServiceStarted = true
        timer = Timer()
        //initialize the TimerTask's job
        initialiseTimerTask()
        //schedule the timer, to wake up every 1 second
        timer!!.schedule(timerTask, delay, interval)
    }

    private fun initialiseTimerTask() {
        timerTask = object : TimerTask() {
            override fun run() {
                //Log.i(TAG, "Timer is running " + counter++)
                //Check if journals is added or not, If not added then send notification
                checkForJournals()
                //Check if personal goals is added or not, If not added then send notification
                checkForPersonalGoal()
                //Getting running logs of Health - Steps, distance, calories and heart rate
                readData()
                /*//Getting location update of user
                LocationHelper().startListeningUserLocation(
                    context, object : MyLocationListener {
                        override fun onLocationChanged(location: Location?) {
                            mLocation = location
                            mLocation?.let {
                                latitude = it.latitude.toString()
                                longitude = it.longitude.toString()
                                //Log.i(TAG, "Latitude: $latitude")
                                //Log.i(TAG, "Longitude: $longitude")
                            }
                        }
                    })*/
                //Post the running logs and location details to server
                postBackgroundData()
            }
        }
    }

    private fun stopTimerTask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer!!.cancel()
            timer = null
        }
    }

    private fun postBackgroundData() {
        try {
            mCompositeDisposable.add(
                getEncryptedRequestInterface()
                    .sendHealthInfo("",
                        HealthInfo(preference!![PrefKeys.PREF_PATIENT_ID, ""]!!.toInt(),
                            totalSteps.toInt(),
                            totalDistance.toInt(),
                            totalCalories.toInt(),
                            totalHeartRate.toInt()
                        ), preference!![PrefKeys.PREF_ACCESS_TOKEN, ""]!!
                    )
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ result ->
                        val responseBody = result.string()
                        Log.d("Response Body", responseBody)
                    }, { error ->
                        Log.e(TAG, error.localizedMessage.toString())
                    })
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun readData() {
        Fitness.getHistoryClient(context, getGoogleAccount())
            .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
            .addOnSuccessListener { dataSet ->
                val total = when {
                    dataSet.isEmpty -> 0
                    else -> dataSet.dataPoints.first().getValue(Field.FIELD_STEPS).asInt()
                }
                //Log.i(TAG, "Total steps: $total")
                totalSteps = total.toString()
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "There was a problem getting the step count.", e)
            }

        Fitness.getHistoryClient(context, getGoogleAccount())
            .readDailyTotal(DataType.TYPE_DISTANCE_DELTA)
            .addOnSuccessListener { dataSet ->
                val total = when {
                    dataSet.isEmpty -> 0
                    else -> dataSet.dataPoints.first().getValue(Field.FIELD_DISTANCE).asFloat()
                }
                //Log.i(TAG, "Total distance: $total")
                totalDistance =
                    String.format("%.2f", convertMeterToKilometer(total.toFloat()))
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "There was a problem getting the step count.", e)
            }

        Fitness.getHistoryClient(context, getGoogleAccount())
            .readDailyTotal(DataType.TYPE_HEART_RATE_BPM)
            .addOnSuccessListener { dataSet ->
                val total = when {
                    dataSet.isEmpty -> 0
                    else -> dataSet.dataPoints.first().getValue(Field.FIELD_AVERAGE)
                }
                //Log.i(TAG, "Total BPM: $total")
                totalHeartRate = "$total"
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "There was a problem getting the step count.", e)
            }
        Fitness.getHistoryClient(context, getGoogleAccount())
            .readDailyTotal(DataType.TYPE_CALORIES_EXPENDED)
            .addOnSuccessListener { dataSet ->
                var total = when {
                    dataSet.isEmpty -> 0
                    else -> dataSet.dataPoints.first().getValue(Field.FIELD_CALORIES)
                }
                if (total == null) {
                    total = ""
                }
                //Log.i(TAG, "Total Calories: $total")
                totalCalories = "$total"
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "There was a problem getting the step count.", e)
            }
    }

    private fun convertMeterToKilometer(meter: Float): Float {
        return (meter * 0.001).toFloat()
    }

    private fun getGoogleAccount() = GoogleSignIn.getAccountForExtension(context, fitnessOptions)

    fun getEncryptedRequestInterface(): RequestInterface {
        val httpClient = getHttpClient()
        //Encryption Interceptor
        val encryptionInterceptor = EncryptionInterceptor(EncryptionImpl())
        //Decryption Interceptor
        val decryptionInterceptor = DecryptionInterceptor(DecryptionImpl())
        httpClient.addInterceptor(encryptionInterceptor)
        httpClient.addInterceptor(decryptionInterceptor)
        return Retrofit.Builder()
            .baseUrl("https://ensemblecare.csardent.com/api/").client(httpClient.build())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(RequestInterface::class.java)
    }

    private fun getHttpClient(): OkHttpClient.Builder {
        val httpClient = OkHttpClient.Builder()
        httpClient.connectTimeout(180, TimeUnit.SECONDS)
        httpClient.callTimeout(4, TimeUnit.MINUTES)
        httpClient.readTimeout(180, TimeUnit.SECONDS)
        httpClient.writeTimeout(1000, TimeUnit.SECONDS)

        val logging = HttpLoggingInterceptor()
        // set your desired log level
        logging.level = HttpLoggingInterceptor.Level.BODY

        // add logging as last interceptor
        httpClient.addInterceptor(logging)  // <-- this is the important line!
        return httpClient
    }

    private fun checkForJournals() {
        val cal = Calendar.getInstance()
        val timeOfDay = cal[Calendar.HOUR_OF_DAY]
        val currDate = cal[Calendar.DAY_OF_MONTH]
        val myFormat = "dd-MM-yyyy"
        val sdf = SimpleDateFormat(myFormat)
        cal.time = sdf.parse(preference!![PrefKeys.PREF_Journal_Added, "01-01-2022"]!!)
        val storedDate = cal[Calendar.DAY_OF_MONTH]
        if (currDate != storedDate) {
            if (timeOfDay >= 12) {
                sendNotification("Add Journal", "Today's journal is not added")
                preference!![PrefKeys.PREF_Journal_Added] = sdf.format(Calendar.getInstance().time)
            }
        }
    }

    private fun checkForPersonalGoal() {
        val cal = Calendar.getInstance()
        val timeOfDay = cal[Calendar.HOUR_OF_DAY]
        val currDate = cal[Calendar.DAY_OF_MONTH]
        val myFormat = "dd-MM-yyyy"
        val sdf = SimpleDateFormat(myFormat)
        cal.time = sdf.parse(preference!![PrefKeys.PREF_Personal_Goal_Added, "01-01-2022"]!!)
        val storedDate = cal[Calendar.DAY_OF_MONTH]
        if (currDate != storedDate) {
            if (timeOfDay >= 12) {
                sendNotification("Add Personal Goal", "Today's personal goal is not added")
                preference!![PrefKeys.PREF_Personal_Goal_Added] =
                    sdf.format(Calendar.getInstance().time)
            }
        }
    }

    private fun sendNotification(messageTitle: String, messageBody: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0 /* Request code */, intent,
            PendingIntent.FLAG_ONE_SHOT
        )
        val channelId = getString(R.string.app_name)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.notification_icon)
            .setColor(ContextCompat.getColor(applicationContext, R.color.red))
            .setContentTitle(messageTitle)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(
            System.currentTimeMillis().toInt() /* ID of notification */,
            notificationBuilder.build()
        )
    }
}