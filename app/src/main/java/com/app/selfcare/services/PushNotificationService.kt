package com.app.selfcare.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.app.selfcare.MainActivity
import com.app.selfcare.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class PushNotificationService: FirebaseMessagingService() {
    // Override onNewToken to get new token
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("TAG", "Refreshed token: $token");
    }

    // Override onMessageReceived() method to extract the
    // title and
    // body from the message passed in FCM
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        // First case when notifications are received via
        // data event
        // Here, 'title' and 'message' are the assumed names
        // of JSON
        // attributes. Since here we do not have any data
        // payload, This section is commented out. It is
        // here only for reference purposes.
        /*if(remoteMessage.getData().size()>0){
            showNotification(remoteMessage.getData().get("title"),
                          remoteMessage.getData().get("message"));
        }*/

        // Second case when notification payload is
        // received.
        if (message.notification != null) {
            // Since the notification is received directly
            // from FCM, the title and the body can be
            // fetched directly as below.
            showNotification(
                message.notification!!.title!!,
                message.notification!!.body!!
            );
        }
    }

    // Method to display the notifications
    private fun showNotification(
        title: String,
        message: String
    ) {
        // Pass the intent to switch to the MainActivity
        val intent = Intent(this, MainActivity::class.java)
        // Assign channel ID
        val channel_id = "ReachOut"
        // Here FLAG_ACTIVITY_CLEAR_TOP flag is set to clear
        // the activities present in the activity stack,
        // on the top of the Activity that is to be launched
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        // Pass the intent to PendingIntent to start the
        // next Activity
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALL)
        // Create a Builder object using NotificationCompat
        // class. This will allow control over all the flags
        val builder: NotificationCompat.Builder = NotificationCompat.Builder(this, channel_id)
            .setSmallIcon(R.drawable.emergency_911)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setSound(soundUri)
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setLights(Color.RED, 3000, 3000)
            .setContentIntent(pendingIntent)
        // Create an object of NotificationManager class to
        // notify the
        // user of events that happen in the background.
        val notificationManager = getSystemService(NotificationManager::class.java)
        // Check if the Android Version is greater than Oreo
        if (Build.VERSION.SDK_INT
            >= Build.VERSION_CODES.O
        ) {
            val notificationChannel = NotificationChannel(
                channel_id, title,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager!!.createNotificationChannel(
                notificationChannel
            )
        }
        notificationManager!!.notify(0, builder.build())
    }
}