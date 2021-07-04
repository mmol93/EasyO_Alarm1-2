package com.MaidAlarm.easyo_alarm.notification

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import androidx.core.app.NotificationCompat
import com.MaidAlarm.easyo_alarm.Convertor
import com.MaidAlarm.easyo_alarm.MainActivity
import com.MaidAlarm.easyo_alarm.R

class WeatherNotification(val context: Context) {
    @SuppressLint("UseCompatLoadingForDrawables")
    fun makeWeatherNotification(title:String, maxTemp:Int, minTemp:Int, rainPop:Int, notificationManager : NotificationManager){
        // 8. notification 함수 호출
        // 채널 id와 이름 지정
        val builder1 = getWeatherNotification("weatherNoti", notificationManager)
        // 내일 날씨에 따른 타이틀과 아이콘 설정
        when(title){
            "Thunderstorm" -> {
                builder1.setSmallIcon(R.drawable.ic_thunder)
//                builder1.setContentTitle(context.getString(R.string.Thunderstorm))
                setLargeIcon(context, R.drawable.ic_thunder, builder1)
            }
            "Drizzle" -> {
                builder1.setSmallIcon(R.drawable.ic_little_rain)
//                builder1.setContentTitle(context.getString(R.string.drizzle))
                setLargeIcon(context, R.drawable.ic_little_rain, builder1)
            }
            "Rain" -> {
                builder1.setSmallIcon(R.drawable.ic_rain)
//                builder1.setContentTitle(context.getString(R.string.rain))
                setLargeIcon(context, R.drawable.ic_rain, builder1)
            }
            "Snow" -> {
                builder1.setSmallIcon(R.drawable.ic_snow)
//                builder1.setContentTitle(context.getString(R.string.snow))
                setLargeIcon(context, R.drawable.ic_snow, builder1)
            }
            "Clear" -> {
                builder1.setSmallIcon(R.drawable.ic_sunny)
//                builder1.setContentTitle(context.getString(R.string.clear))
                setLargeIcon(context, R.drawable.ic_sunny, builder1)
            }
            "Clouds" -> {
                builder1.setSmallIcon(R.drawable.ic_clouds)
//                builder1.setContentTitle(context.getString(R.string.clouds))
                setLargeIcon(context, R.drawable.ic_clouds, builder1)
            }
            "Mist", "Dust", "Fog", "Haze", "Sand", "Ash" -> {
                builder1.setSmallIcon(R.drawable.ic_fog)
//                builder1.setContentTitle(context.getString(R.string.mist))
                setLargeIcon(context, R.drawable.ic_fog, builder1)
            }
            "Tornado", "Squall" -> {
                builder1.setSmallIcon(R.drawable.ic_tornado)
//                builder1.setContentTitle(context.getString(R.string.tornado))
                setLargeIcon(context, R.drawable.ic_tornado, builder1)
            }
        }
        builder1.setContentText("${minTemp}℃ / ${maxTemp}℃   " + context.getString(R.string.precipitation) + "${rainPop}%")
        builder1.setContentTitle(context.getString(R.string.tomorrowWeather))
        builder1.setVibrate(null)

        // 띠링 하는 알람이 한번만 울리게 설정
        // notification의 내용은 계속 바뀐다. 단지 알람은 최초 한번만 실시됨
        builder1.setOnlyAlertOnce(true)

        // notification 클릭 시 자동으로 noti 사라지게 만들기
        builder1.setAutoCancel(true)

        // ** notification 클릭 시 MainActivity를 실행한다
        val intent = Intent(context, MainActivity::class.java)
        val pending = PendingIntent.getActivity(context, 10, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        // 클릭시 해당 intent를 연다
        builder1.setContentIntent(pending)

        // 14. notification 을 제어할 수 있는 getSystemService 객체 생성
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 13. getNotification.build(): 주어진 정보(옵션)를 종합하여 새로운 Notification 객체 반환
        val notification = builder1.build()

        // 15. notification 생성
        // id: 채널 id를 의미함
        manager.notify(1000, notification)
    }

    fun getWeatherNotification(name : String, manager : NotificationManager) : NotificationCompat.Builder{
        // OS 버전 분기
        // 안드로이드 8.0 이상이라면
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            // 3. 채널 객체 생성
            // IMPORTANCE_HIGH로 하지 않으면 메시지가 안나올 가능성 있음
            val channel = NotificationChannel("channel2", name, NotificationManager.IMPORTANCE_LOW)

            // 5. Notification 보여줄 때 진동 사용 여부
            channel.enableVibration(false)

            // notification의 소리를 off로 한다
            channel.setSound(null, null)

            // 진동이 없게 설정
            channel.enableVibration(false)

            // 알림 메시지를 관리하는 객체에 채널을 등록한다
            manager.createNotificationChannel(channel)

            // 알림 컨텐츠를 생성한다
            val builder = NotificationCompat.Builder(context, "channel2")
            return builder
        }else{
            // Builder에 deprecate가 생기는 이유는 8.0 이상부턴 지원하지 않기 때문
            val builder = NotificationCompat.Builder(context)

            builder.setSound(null)

            // 알림에 진동이 오지 않게 처리
            builder.setVibrate(null)
            return builder
        }
    }

    fun setLargeIcon(context: Context, imageDrawable : Int, builder1 : NotificationCompat.Builder){
        val bitmap = Convertor.bitmapConvertor(context, imageDrawable)
        builder1.setLargeIcon(bitmap)
    }
}