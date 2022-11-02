package com.app.selfcare.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.app.selfcare.utils.Utils

class RestartBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (!Utils.isServiceRunning(context, SelfCareService::class.java)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(Intent(context, SelfCareService::class.java))
            } else {
                context.startService(Intent(context, SelfCareService::class.java))
            }
        }
    }
}