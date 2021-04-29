package com.MaidAlarm.easyo_alarm

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import java.io.DataOutputStream

class Service : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    // 이 앱이 완전히 종료됐을 때 호출
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Log.d("Exit", "Task 종료됨")
        val app : AppClass = application as AppClass

        // 브로드캐스트에 등록했던 리시버도 종료해야한다(안하면 2개씩 나옴)
        val receiver = Receiver()
        unregisterReceiver(receiver)

        // AppClass에 저장되어 있는 변수들을 파일에 저장한다
        val fos = openFileOutput("data1.bat", Context.MODE_PRIVATE)

        val dos = DataOutputStream(fos)
        dos.writeInt(app.wayOfAlarm)
        dos.writeInt(app.counter)
        dos.writeInt(app.notificationSwitch)
        dos.writeInt(app.initialStart)
        dos.writeInt(app.bellIndex)

        dos.flush()
        dos.close()

        // 마지막에는 서비스도 종료하기
        stopSelf()
    }
}