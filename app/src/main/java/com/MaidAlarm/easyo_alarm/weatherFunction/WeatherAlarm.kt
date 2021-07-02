package com.MaidAlarm.easyo_alarm.weatherFunction

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.MaidAlarm.easyo_alarm.AppClass.Companion.context
import com.MaidAlarm.easyo_alarm.Receiver
import java.util.*

class WeatherAlarm(context:Context) {
    private val alarmManager: AlarmManager? =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager?

    fun setTomorrowWeatherAlarm(time:String){
        val hour = time.substring(0,1).toInt()

        // 브로드캐스트에 등록할 날짜를 지정
        val calendar: Calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            // 정확한 시간 설정
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        // 브로드캐스트 등록하기
        val intent = Intent(context, Receiver::class.java)
        intent.putExtra("hour", 0)
        intent.putExtra("action", "weather")

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            1000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        // 위에서 설정한 시간(Calendar.getInstance)에 알람이 울리게 한다
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            // API23 이상에서는 setExactAndAllowWhileIdle을 사용해야한다.
            alarmManager?.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }else{
            alarmManager?.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }
    }

    fun cancelTomorrowWeatherAlarm(){
        val intent = Intent(context, Receiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 1000, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        alarmManager?.cancel(pendingIntent)
        pendingIntent.cancel()
    }
}