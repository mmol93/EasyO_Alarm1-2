package com.MaidAlarm.easyo_alarm

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.MaidAlarm.easyo_alarm.databinding.ActivityDeveloperBinding
import java.io.DataInputStream

// 개발자가 확인할 필요 있는 기능들을 넣어놓음
class Developer : AppCompatActivity() {
    lateinit var binder : ActivityDeveloperBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_developer)
        binder = ActivityDeveloperBinding.inflate(layoutInflater)

        // 설정된 모든 알람의 현재 브로드캐스트 설정 상태를 가져온다
        binder.buttonCheckAlarm.setOnClickListener {
            binder.textView.text = ""
            val SQLHelper = SQLHelper(this)
            val function = Function()

            // 모든 알람의 requestCode를 가져온다
            var requestCodeList = mutableListOf<Int>()
            requestCodeList = function.CheckRequestCodeSQL(this, SQLHelper, requestCodeList)

            binder.textView.append("${requestCodeList}\n")
            // 설정되어 있는 브로드캐스트를 requestCode를 이용하여 각각 가져온다
            val intent = Intent("com.maidalarm.easyo.alarm")
            intent.action = "com.maidalarm.easyo.alarm"
            intent.component = ComponentName("com.MaidAlarm.easyo_alarm", "com.MaidAlarm.easyo_alarm.Receiver")
            for (requestCode in requestCodeList) {
                // 브로드 캐스트 해당 requestCode를 갖고 있는 브로드캐스트 가져오기
                val alarmUp = PendingIntent.getBroadcast(
                    this, requestCode,
                    intent,
                    PendingIntent.FLAG_NO_CREATE
                ) != null

                if (alarmUp) {
                    binder.textView.append("${requestCode} 알람 설정 완료\n")
                } else {
                    binder.textView.append("알람 없음\n")
                }
            }
            val tomorrowAlarmCheck = PendingIntent.getBroadcast(
                this, 1000,
                intent,
                PendingIntent.FLAG_NO_CREATE
            ) != null
            if (tomorrowAlarmCheck) {
                binder.textView.append("내일 알람 설정 완료\n")
            } else {
                binder.textView.append("내일 알람 없음\n")
            }
        }
        // 여태 울린 알람의 이력을 가져온다
        binder.button1CheckHistoy.setOnClickListener {
            try{
                // 먼저 데이터를 가져온다
                // 파일 읽어오기

                // 브로드캐스트가 울렸는지 확인
                var fis = openFileInput("history2.bat")
                var dis = DataInputStream(fis)

                var lastAlarmHistory = dis.readLine()

                binder.textView.text = lastAlarmHistory

                // 지금 울릴 알람이 맞는지 확인
                fis = openFileInput("history.bat")
                dis = DataInputStream(fis)

                lastAlarmHistory = dis.readLine()

                binder.textView.append("\n$lastAlarmHistory\n")

                // frontAcitivity의 ok버튼 클릭 후 다시 시간 기록
                fis = openFileInput("data2.bat")
                dis = DataInputStream(fis)

                lastAlarmHistory = dis.readLine()

                binder.textView.append("$lastAlarmHistory\n")
            }catch (e:Exception){
                binder.textView.text = "기록된 알람 없음"
            }
        }
        setContentView(binder.root)
    }
}