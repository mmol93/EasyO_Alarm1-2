package com.example.easyo_alarm.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService


class notification {
        fun makeNotification(context : Context, id : String, name : String, manager : NotificationManager) : NotificationCompat.Builder{
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
    }
