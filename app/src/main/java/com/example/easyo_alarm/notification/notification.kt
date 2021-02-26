package com.example.easyo_alarm.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import com.example.easyo_alarm.R

class notification {
    fun makeNotification(context : Context, notificationManager : NotificationManager){
        // 8. notification 함수 호출
        // 채널 id와 이름 지정
        val builder1 = getNotification(context, "chanel1", "첫 번째 채널", notificationManager)

        // 9. notification의 작은 아이콘 설정(상단 작업표시줄에 상시 표시되는 작은 아이콘)
        builder1.setSmallIcon(android.R.drawable.ic_menu_search)

        // 10. notification의 큰 아이콘 설정(작업표시줄을 늘려서 볼 때 나오는 Notification 아이콘)
        // 큰 아이콘은 bitmap으로만 받아서 표시할 수 있다
        // 그래서 bitmap으로 디코딩을 해야한다
        val bitmap  = BitmapFactory.decodeResource(Resources.getSystem(), R.mipmap.ic_launcher )
        builder1.setLargeIcon(bitmap)

        // 숫자 설정(알림 메시지 안에 숫자 표시가능)
        // 주로 미확인 문자 메시지 수를 표기할 때 사용
        builder1.setNumber(100)

        // 알람이 계속 뜬 상채로 있게하기
        builder1.setOngoing(true)

        // 전체 알락 삭제를 눌러도 삭제 안되게 하기
        builder1.setAutoCancel(true)

        // 11. notification 타이틀 설정
        builder1.setContentTitle("타이틀1")

        // 12. notification 메시지 설정
        builder1.setContentText("텍스트1")

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
            val chanel = NotificationChannel(id, name, NotificationManager.IMPORTANCE_HIGH)

            // 5. Notification 보여줄 때 진동 사용 여부
            chanel.enableVibration(false)

            // 6. 알림 메시지를 관리하는 객체에 채널을 등록한다
            manager.createNotificationChannel(chanel)

            // 7. 알림 컨텐츠를 생성한다
            val builder = NotificationCompat.Builder(context, id)
            return builder
        }else{
            // Builder에 deprecate가 생기는 이유는 8.0 이상부턴 지원하지 않기 때문
            val builder = NotificationCompat.Builder(context)
            return builder
        }
    }

    fun cancelNotification(){

    }
}
