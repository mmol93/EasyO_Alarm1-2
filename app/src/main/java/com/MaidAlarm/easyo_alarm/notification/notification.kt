package com.MaidAlarm.easyo_alarm.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import com.MaidAlarm.easyo_alarm.AppClass
import com.MaidAlarm.easyo_alarm.MainActivity
import com.MaidAlarm.easyo_alarm.R

class notification {
    fun makeNotification(app : AppClass, context : Context, notificationManager : NotificationManager){
        // 8. notification 함수 호출
        // 채널 id와 이름 지정
        val builder1 = getNotification(context, "chanel1", "첫 번째 채널", notificationManager)

        // 9. notification의 작은 아이콘 설정(상단 작업표시줄에 상시 표시되는 작은 아이콘)
        builder1.setSmallIcon(R.drawable.notification_icon)

        // notification on/off 설정에 따라 구별하여 notification 만들기
        if (app.notificationSwitch == 1){
            // 알람이 계속 뜬 상채로 있게하기
            builder1.setOngoing(true)

            // 전체 알락 삭제를 눌러도 삭제 안되게 하기
            builder1.setAutoCancel(true)
        }else{
            // 알람 개별 삭제 가능
            builder1.setOngoing(false)

            // 전체 알락 삭제를 눌러도 삭제 가능하게 하기
            builder1.setAutoCancel(false)
        }

        // 11. notification 타이틀 설정
        builder1.setContentTitle(app.recentTime)

        // 12. notification 메시지 설정
        builder1.setContentText(app.recentWeek)

        // 띠링 하는 알람이 한번만 울리게 설정
        // notification의 내용은 계속 바뀐다. 단지 알람은 최초 한번만 실시됨
        builder1.setOnlyAlertOnce(true)

        // 알림에 진동이 오지 않게 처리
        builder1.setVibrate(null)

        // ** notification 클릭 시 MainActivity를 실행한다
        val intent = Intent(context, MainActivity::class.java)
        val pending = PendingIntent.getActivity(context, 10, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        // 클릭시 해당 intent를 연다
        builder1.setContentIntent(pending)

        // 13. getNotification.build(): 주어진 정보(옵션)를 종합하여 새로운 Notification 객체 반환
        val notification = builder1.build()

        // 14. notification 을 제어할 수 있는 getSystemService 객체 생성
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 15. notification 생성
        // id: 채널 id를 의미함
        manager.notify(10, notification)
    }

    // 기본적인 notification 의 설정 및 채널을 설정한다
    // NotificationManager 의 경우 받아오기로 한다
    fun getNotification(context : Context, id : String, name : String, manager : NotificationManager) : NotificationCompat.Builder{
        // OS 버전 분기
        // 안드로이드 8.0 이상이라면
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            // 3. 채널 객체 생성
            // IMPORTANCE_HIGH로 하지 않으면 메시지가 안나올 가능성 있음
            val chanel = NotificationChannel(id, name, NotificationManager.IMPORTANCE_LOW)

            // 5. Notification 보여줄 때 진동 사용 여부
            chanel.enableVibration(false)

            // notification의 소리를 off로 한다
            chanel.setSound(null, null)

            // 진동이 없게 설정
            chanel.enableVibration(false)

            // 알림 메시지를 관리하는 객체에 채널을 등록한다
            manager.createNotificationChannel(chanel)

            // 알림 컨텐츠를 생성한다
            val builder = NotificationCompat.Builder(context, id)
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

    fun cancelNotification(context : Context){
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(10)
    }
}
