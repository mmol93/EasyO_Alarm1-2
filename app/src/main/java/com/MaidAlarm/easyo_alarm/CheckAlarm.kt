package com.MaidAlarm.easyo_alarm

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import java.util.*


class CheckAlarm(val context: Context){
    fun checkAlarm(requestCode: Int){
        val intent = Intent(context, Receiver::class.java)
        val alarmUp = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE) != null

        if (alarmUp) {
            Log.d("CheckAlarm", "Alarm is already active")
        }

    }
}