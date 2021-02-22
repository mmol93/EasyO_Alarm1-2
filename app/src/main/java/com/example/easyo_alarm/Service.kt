package com.example.easyo_alarm

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import java.io.DataOutputStream

class Service : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    // 이 앱이 완전히 종료됐을 때 호출
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        val app : AppClass = application as AppClass

        // AppClass에 저장되어 있는 변수들을 파일에 저장한다
        val fos = openFileOutput("data1.bat", Context.MODE_PRIVATE)

        val dos = DataOutputStream(fos)
        dos.writeInt(app.wayOfAlarm)
        dos.writeInt(app.counter)

        dos.flush()
        dos.close()

        // 마지막에는 서비스도 종료하기
        stopSelf()
    }
}