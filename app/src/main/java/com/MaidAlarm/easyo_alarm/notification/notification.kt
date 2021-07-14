package com.MaidAlarm.easyo_alarm.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.core.app.NotificationCompat
import com.MaidAlarm.easyo_alarm.AppClass
import com.MaidAlarm.easyo_alarm.MainActivity
import com.MaidAlarm.easyo_alarm.R
import com.MaidAlarm.easyo_alarm.Receiver

class notification {
    lateinit var pref : SharedPreferences
    private var alarmSwitch  = 0
    private var bellIndex = 0
    private var volume = 0
    private var alarmMode = 0
    private var alarmCounter = 0

    // 일반적인 notification 생성에서 사용됨(다음 알람 표시용)
    fun makeNotification(app : AppClass, context : Context, notificationManager : NotificationManager){
        pref = context.getSharedPreferences("simpleAlarmData", Context.MODE_PRIVATE)
        alarmSwitch = pref.getInt("alarmSwitch", 1)

        // 8. notification 함수 호출
        // 채널 id와 이름 지정
        val builder1 = getNotification(context, "chanel1", "첫 번째 채널", notificationManager)

        // 9. notification의 작은 아이콘 설정(상단 작업표시줄에 상시 표시되는 작은 아이콘)
        builder1.setSmallIcon(R.drawable.notification_icon)

        // notification on/off 설정에 따라 구별하여 notification 만들기
        if (alarmSwitch == 1){
            // 알람이 계속 뜬 상채로 있게하기
            builder1.setOngoing(true)

            // 전체 알림 삭제를 눌러도 삭제 안되게 하기
            builder1.setAutoCancel(false)
        }else{
            // 알람 개별 삭제 가능
            builder1.setOngoing(false)

            // 전체 알림 삭제를 눌러도 삭제 가능하게 하기
            builder1.setAutoCancel(true)
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
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        val pending = PendingIntent.getActivity(context, 10, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        // 클릭시 해당 intent를 연다
        builder1.setContentIntent(pending)

        // 14. notification 을 제어할 수 있는 getSystemService 객체 생성
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Action1 설정 - 10분 뒤
        val intent2 = Intent(context, Receiver::class.java)
        intent2.putExtra("action", "action1")
        val pending2 = PendingIntent.getBroadcast(context, 110, intent2, PendingIntent.FLAG_UPDATE_CURRENT)
        val builder2 = NotificationCompat.Action.Builder(R.mipmap.ic_launcher, context.getString(R.string.actionButton1), pending2)
        val action2 = builder2.build()

        builder1.addAction(action2)

        // Action2 설정 - 15분 뒤
        val intent3 = Intent(context, Receiver::class.java)
        intent3.putExtra("action", "action2")
        val pending3 = PendingIntent.getBroadcast(context, 120, intent3, PendingIntent.FLAG_UPDATE_CURRENT)
        val builder3 = NotificationCompat.Action.Builder(R.mipmap.ic_launcher, context.getText(R.string.actionButton2), pending3)
        val action3 = builder3.build()

        builder1.addAction(action3)

        // Action3 설정 - 30분 뒤
        val intent4 = Intent(context, Receiver::class.java)
        intent4.putExtra("action", "action3")
        val pending4 = PendingIntent.getBroadcast(context, 130, intent4, PendingIntent.FLAG_UPDATE_CURRENT)
        val builder4 = NotificationCompat.Action.Builder(R.mipmap.ic_launcher, context.getText(R.string.actionButton3), pending4)
        val action4 = builder4.build()

        builder1.addAction(action4)

        // 13. getNotification.build(): 주어진 정보(옵션)를 종합하여 새로운 Notification 객체 반환
        val notification = builder1.build()

        // 15. notification 생성
        // id: 채널 id를 의미함
        manager.notify(10, notification)
    }

    // 재부팅 시 notification 갱신에 사용됨
    // 위의 makeNotification과의 차이점
    // 여긴 앱이 실행되어 있지 않기 때문에 SQL에서 데이터를 추출하여 연산을 하여 notification에 넣을 텍스트를 같이 매개변수로 넣어준다
    // 즉, setContentTitle과 setContentText 부분만 다르다
    fun makeNotification(contentTitle : String, contentText : String, context : Context, notificationManager : NotificationManager){
        pref = context.getSharedPreferences("simpleAlarmData", Context.MODE_PRIVATE)
        alarmSwitch = pref.getInt("alarmSwitch", 1)

        // 8. notification 함수 호출
        // 채널 id와 이름 지정
        val builder1 = getNotification(context, "chanel1", "첫 번째 채널", notificationManager)

        // 9. notification의 작은 아이콘 설정(상단 작업표시줄에 상시 표시되는 작은 아이콘)
        builder1.setSmallIcon(R.drawable.notification_icon)

        // notification on/off 설정에 따라 구별하여 notification 만들기
        if (alarmSwitch == 1){
            // 알람이 계속 뜬 상채로 있게하기
            builder1.setOngoing(true)

            // 전체 알림 삭제를 눌러도 삭제 안되게 하기
            builder1.setAutoCancel(false)
        }else{
            // 알람 개별 삭제 가능
            builder1.setOngoing(false)

            // 전체 알림 삭제를 눌러도 삭제 가능하게 하기
            builder1.setAutoCancel(true)
        }

        // 11. notification 타이틀 설정
        builder1.setContentTitle(contentTitle)

        // 12. notification 메시지 설정
        builder1.setContentText(contentText)

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

        // 14. notification 을 제어할 수 있는 getSystemService 객체 생성
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Action1 설정 - 10분 뒤
        val intent2 = Intent(context, Receiver::class.java)
        intent2.putExtra("action", "action1")
        val pending2 = PendingIntent.getBroadcast(context, 110, intent2, PendingIntent.FLAG_UPDATE_CURRENT)
        val builder2 = NotificationCompat.Action.Builder(R.mipmap.ic_launcher, context.getString(R.string.actionButton1), pending2)
        val action2 = builder2.build()

        builder1.addAction(action2)

        // Action2 설정 - 15분 뒤
        val intent3 = Intent(context, Receiver::class.java)
        intent3.putExtra("action", "action2")
        val pending3 = PendingIntent.getBroadcast(context, 120, intent3, PendingIntent.FLAG_UPDATE_CURRENT)
        val builder3 = NotificationCompat.Action.Builder(R.mipmap.ic_launcher, context.getText(R.string.actionButton2), pending3)
        val action3 = builder3.build()

        builder1.addAction(action3)

        // Action3 설정 - 30분 뒤
        val intent4 = Intent(context, Receiver::class.java)
        intent4.putExtra("action", "action3")
        val pending4 = PendingIntent.getBroadcast(context, 130, intent4, PendingIntent.FLAG_UPDATE_CURRENT)
        val builder4 = NotificationCompat.Action.Builder(R.mipmap.ic_launcher, context.getText(R.string.actionButton3), pending4)
        val action4 = builder4.build()

        builder1.addAction(action4)

        // 13. getNotification.build(): 주어진 정보(옵션)를 종합하여 새로운 Notification 객체 반환
        val notification = builder1.build()

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
