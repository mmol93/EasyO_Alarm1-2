package com.example.easyo_alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import java.util.*
import kotlin.collections.ArrayList

class makeAlarm(
        val context: Context,
        val hour: Int,
        val min: Int,
        val progress: Int,
        val weekList : List<Int>,
        val requestCode: Int,
        ){

    private val alarmManager: AlarmManager? =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager?

    // *** 한번만 울리는 새로운 알람을 알람 매니저에 등록한다
    fun addNewAlarm_once(){
        val quick = 1   // 이 메서드는 once 이므로 반드시 한번만 울린다

        val calendar: Calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            // 정확한 시간 설정
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, min)
            set(Calendar.SECOND, 0)
        }

        val intent = Intent(context, Receiver::class.java)
        // *** 여기서 intent에 데이터를 넣어서 BroadCast에서 사용할 수 있다
        // pendingIntent 에는 1개의 변수만 넣을 수 있기 때문에 리스트를 임시로 만들어 넣는게 제일 좋다
        // 넘겨줄 항목은 다음과 같다.
        // Sun ~ Sat, progress, quick => 9개의 항목을 가진 리스트
        var ListForPendingIntent = mutableListOf<Int>()
        ListForPendingIntent.add(progress)
        ListForPendingIntent.add(quick)

        // weekList 에는 alarmFragment 에서 받아온 alarmWeek에 대한 리스트 정보가 담겨있다
        ListForPendingIntent = (weekList + ListForPendingIntent) as ArrayList<Int>

        // intent에 putIntent를 하기 위해선 List -> array로 변환필요
        Log.d("makeAlarm", "ListForPendingIntent: $ListForPendingIntent")
        intent.putExtra("arrayForPendingIntent", ListForPendingIntent)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        Log.d("makeAlarm", "Set exact Time: " + Date().toString())
        Log.d("makeAlarm", "hour: $hour")
        Log.d("makeAlarm", "min: $min")
        Log.d("makeAlarm", "requestCode: $requestCode")

        // 위에서 설정한 시간(Calendar.getInstance)에 알람이 울리게 한다
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            // API23 이상에서는 setExactAndAllowWhileIdle을 사용해야한다.
            alarmManager?.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }else{
            alarmManager?.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }
    }

    // *** 매일 울리는 새로운 알람을 알람 매니저에 등록한다
    fun addNewAlarm_normal(){
        val quick = 0   // 이 메서드는 normal 이므로 반복해서 울린다.

        val calendar: Calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            // 정확한 시간 설정
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, min)
            set(Calendar.SECOND, 0)
        }

        val intent = Intent(context, Receiver::class.java)
        // *** 여기서 intent에 데이터를 넣어서 BroadCast에서 사용할 수 있다
        // pendingIntent 에는 1개의 변수만 넣을 수 있기 때문에 리스트를 임시로 만들어 넣는게 제일 좋다
        // 넘겨줄 항목은 다음과 같다.
        // Sun ~ Sat, progress, quick => 9개의 항목을 가진 리스트
        var ListForPendingIntent = mutableListOf<Int>()
        ListForPendingIntent.add(progress)
        ListForPendingIntent.add(quick)

        // weekList 에는 alarmFragment 에서 받아온 alarmWeek에 대한 리스트 정보가 담겨있다
        ListForPendingIntent = (weekList + ListForPendingIntent) as ArrayList<Int>

        // intent에 putIntent를 하기 위해선 List -> array로 변환필요
        Log.d("makeAlarm", "ListForPendingIntent: $ListForPendingIntent")
        intent.putExtra("arrayForPendingIntent", ListForPendingIntent)

        val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        )
        Log.d("makeAlarm", "Set exact Time: " + Date().toString())
        Log.d("makeAlarm", "hour: $hour")
        Log.d("makeAlarm", "min: $min")
        Log.d("makeAlarm", "requestCode: $requestCode")

        // 위에서 설정한 시간(Calendar.getInstance)에 알람이 울리게 한다
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
//            // API23 이상에서는 setExactAndAllowWhileIdle을 사용해야한다.
//            alarmManager?.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
//        }else{
//            alarmManager?.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
//        }

        // 위에서 울린 알림이 매일 울리게 설정한다
//        val intervalDay = (60 * 1000).toLong() // 24시간
//
//        var selectTime = calendar.timeInMillis

        // 지정한 시간에 매일 알람 울리게 설정
        alarmManager?.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis,  AlarmManager.INTERVAL_DAY, pendingIntent)
    }

    // *** 이미 있는 알람을 삭제한다.
    fun cancelAlarm(requestCode: Int){
        val intent = Intent(context, Receiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        Log.d("makeAlarm", "Cancel(): requestCode: $requestCode")
        // 6. 해당 펜딩인텐트에 있는 알람을 해제(삭제, 취소)한다
        alarmManager?.cancel(pendingIntent)
    }
}

class Receiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent!!.action == "android.intent.action.BOOT_COMPLETED") {
            Log.d("makeAlarm", "재부팅됨")
        }
        else{
            Log.d("makeAlarm", "onReceive() 호출")
            val toast = Toast.makeText(context, "BroadcastReceiver() 호출", Toast.LENGTH_LONG)
            toast.show()
            // 오늘이 알람에서 설정한 요일과 맞는지 확인하기 위해 오늘 날짜의 요일을 가져온다
            val calendar = Calendar.getInstance()
            // 밑에서 사용될 arrayFromMakeAlarm의 경우 인덱스가 0부터 시작
            // 하지만 일요일 = 1 ~ 토요일 = 7이기 때문에 1부터 시작해서 -1을 해줘야 해당 요일과 인덱스가 매칭된다
            val present_week = calendar.get(Calendar.DAY_OF_WEEK) - 1
            val arrayFromMakeAlarm = intent!!.getIntegerArrayListExtra("arrayForPendingIntent")

            // 순서대로 일 ~ 토, progress, quick  = 9개 항목 들어있음
            Log.d("makeAlarm", "arrayFromMakeAlarm form onReceive(): $arrayFromMakeAlarm")
            Log.d("makeAlarm", "present_week: $present_week")

            // 알람에서 설정한 요일일 때만 액티비티 띄워서 알람 울리게 설정
            if (arrayFromMakeAlarm!![present_week] == 1){
                Log.d("makeAlarm", "${present_week}요일입니다.")
                val frontAlarmActivity = Intent(context, FrontAlarmActivity::class.java)
                frontAlarmActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                // arrayFromMakeAlarm[7] 에는 progress 대한 정보 들어있음
                frontAlarmActivity.putExtra("progress", arrayFromMakeAlarm[7])
                context?.startActivity(frontAlarmActivity)
            }

            if (arrayFromMakeAlarm!![8] == 1){
                // quick 알람이르므로 자동으로 토글 off - 미구현
            }
        }
    }
}
