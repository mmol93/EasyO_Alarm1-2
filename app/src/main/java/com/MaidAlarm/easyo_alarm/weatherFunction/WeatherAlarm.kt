package com.MaidAlarm.easyo_alarm.weatherFunction

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.MaidAlarm.easyo_alarm.AppClass.Companion.context
import com.MaidAlarm.easyo_alarm.Receiver
import java.util.*

class WeatherAlarm(context:Context) {
    private val alarmManager: AlarmManager? =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager?

    fun setTomorrowWeatherAlarm(time:String){
        val hour = time.substring(0, 2).toInt()
        Log.d("WeatherAlarm - WeatherAlarm.kt", "Calendar에 들어갈 (설정된)시간: $hour")

        // 브로드캐스트에 등록할 날짜를 지정
        val calendar: Calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            // 정확한 시간 설정
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        var calendarTimeMillis = calendar.timeInMillis
        if (calendarTimeMillis <= System.currentTimeMillis()){
            val intervalTime = (24 * 60 * 60 * 1000) // 24시간
            calendarTimeMillis += intervalTime
        }
        Log.d("WeatherAlarm - WeatherAlar.kt", "설정된 브로드캐스트 시간: ${calendarTimeMillis}")
        Log.d("WeatherAlarm - WeatherAlar.kt", "현재 시간: ${System.currentTimeMillis()}")
        Log.d("WeatherAlarm - WeatherAlar.kt", "발동까지 남은 분: ${(calendarTimeMillis - System.currentTimeMillis())/(1000*60)}")

        // 브로드캐스트 등록하기
        val intent = Intent(context, Receiver::class.java)
        intent.putExtra("hour", 0)
        intent.putExtra("action", "weather")
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            1000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        // 날씨 알람은 매일 울리기 때문에 setRepeat로 지정한다
        alarmManager?.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendarTimeMillis, pendingIntent)
    }

    fun cancelTomorrowWeatherAlarm(){
        val intent = Intent(context, Receiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 1000, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        alarmManager?.cancel(pendingIntent)
        pendingIntent.cancel()
    }
}