package com.MaidAlarm.easyo_alarm

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.MaidAlarm.easyo_alarm.databinding.ActivityDeveloperBinding

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
            val intent = Intent(this, Receiver::class.java)
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
        }
        // 여태 울린 알람의 이력을 가져온다
        binder.button1CheckHistoy.setOnClickListener {

        }
        setContentView(binder.root)
    }
}