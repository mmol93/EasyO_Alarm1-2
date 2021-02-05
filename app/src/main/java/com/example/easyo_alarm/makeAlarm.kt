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
        Log.d("makeAlarm", "hour: $requestCode")

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

        ListForPendingIntent = (weekList + ListForPendingIntent) as MutableList<Int>

        // intent에 putIntent를 하기 위해선 List -> array로 변환필요
        val arrayForPendingIntent : Array<Int> = ListForPendingIntent.toTypedArray()
        intent.putExtra("arrayForPendingIntent", arrayForPendingIntent)

        Log.d("makeAlarm", "ListForPendingIntent: $ListForPendingIntent")
        Log.d("makeAlarm", "arrayForPendingIntent: $arrayForPendingIntent")

        val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        )
        Log.d("makeAlarm", "Set exact Time: " + Date().toString())
        Log.d("makeAlarm", "hour: $hour")
        Log.d("makeAlarm", "min: $min")
        Log.d("makeAlarm", "hour: $requestCode")

        // 위에서 설정한 시간(Calendar.getInstance)에 알람이 울리게 한다
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            // API23 이상에서는 setExactAndAllowWhileIdle을 사용해야한다.
            alarmManager?.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }else{
            alarmManager?.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }

        // 위에서 울린 알림이 매일 울리게 설정한다
        val intervalDay = (24 * 60 * 60 * 1000).toLong() // 24시간

        var selectTime = calendar.timeInMillis
        val currenTime = System.currentTimeMillis()

        if(currenTime>selectTime){
            selectTime += intervalDay
        }

        // 지정한 시간에 매일 알람 울리게 설정
        alarmManager?.setRepeating(AlarmManager.RTC_WAKEUP, selectTime,  intervalDay, pendingIntent);
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
        Log.d("makeAlarm", "onReceive() 호출")
        val toast = Toast.makeText(context, "BroadcastReceiver() 호출", Toast.LENGTH_LONG)
        toast.show()
        // 오늘이 알람에서 설정한 요일과 맞는지 확인하기 위해 오늘 날짜의 요일을 가져온다
        val calendar = Calendar.getInstance()
        val present_week = calendar.get(Calendar.DAY_OF_WEEK)
        val arrayFromMakeAlarm = intent!!.getIntegerArrayListExtra("arrayForPendingIntent")
        Log.d("makeAlarm", "arrayFromMakeAlarm form onReceive(): $arrayFromMakeAlarm")

        // *** 1~7까지는 일요일~토요일 요일 확인
        for (i in 0..8){
            // 일요일
            if (i == 0) {
                if (arrayFromMakeAlarm!![i] == 1){
                    Log.d("makeAlarm", "일요일입니다.")
                    val frontAlarmActivity = Intent(context, FrontAlarmActivity::class.java)
                    frontAlarmActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    // arrayFromMakeAlarm[7] 에는 progress 대한 정보 들어있음
                    frontAlarmActivity.putExtra("progress", arrayFromMakeAlarm[7])
                    context?.startActivity(frontAlarmActivity)
                }
            }
            // 월요일
            if (i == 1){
                if (arrayFromMakeAlarm!![i] == 1){
                    Log.d("makeAlarm", "월요일입니다.")
                    val frontAlarmActivity = Intent(context, FrontAlarmActivity::class.java)
                    frontAlarmActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    // arrayFromMakeAlarm[7] 에는 progress 대한 정보 들어있음
                    frontAlarmActivity.putExtra("progress", arrayFromMakeAlarm[7])
                    context?.startActivity(frontAlarmActivity)
                }
            }
            // 화요일
            if (i == 2){
                if (arrayFromMakeAlarm!![i] == 1){
                    Log.d("makeAlarm", "화요일입니다.")
                    val frontAlarmActivity = Intent(context, FrontAlarmActivity::class.java)
                    frontAlarmActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    // arrayFromMakeAlarm[7] 에는 progress 대한 정보 들어있음
                    frontAlarmActivity.putExtra("progress", arrayFromMakeAlarm[7])
                    context?.startActivity(frontAlarmActivity)
                }
            }
            // 수요일
            if (i == 3){
                if (arrayFromMakeAlarm!![i] == 1){
                    Log.d("makeAlarm", "수요일입니다.")
                    val frontAlarmActivity = Intent(context, FrontAlarmActivity::class.java)
                    frontAlarmActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    // arrayFromMakeAlarm[7] 에는 progress 대한 정보 들어있음
                    frontAlarmActivity.putExtra("progress", arrayFromMakeAlarm[7])
                    context?.startActivity(frontAlarmActivity)
                }
            }
            // 목요일
            if (i == 4){
                if (arrayFromMakeAlarm!![i] == 1){
                    Log.d("makeAlarm", "목요일입니다.")
                    val frontAlarmActivity = Intent(context, FrontAlarmActivity::class.java)
                    frontAlarmActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    // arrayFromMakeAlarm[7] 에는 progress 대한 정보 들어있음
                    frontAlarmActivity.putExtra("progress", arrayFromMakeAlarm[7])
                    context?.startActivity(frontAlarmActivity)
                }
            }
            // 금요일
            if (i == 5){
                if (arrayFromMakeAlarm!![i] == 1){
                    Log.d("makeAlarm", "금요일입니다.")
                    val frontAlarmActivity = Intent(context, FrontAlarmActivity::class.java)
                    frontAlarmActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    // arrayFromMakeAlarm[7] 에는 progress 대한 정보 들어있음
                    frontAlarmActivity.putExtra("progress", arrayFromMakeAlarm[7])
                    context?.startActivity(frontAlarmActivity)
                }
            }
            // 토요일
            if (i == 6){
                if (arrayFromMakeAlarm!![i] == 1){
                    Log.d("makeAlarm", "토요일입니다.")
                    val frontAlarmActivity = Intent(context, FrontAlarmActivity::class.java)
                    frontAlarmActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    // arrayFromMakeAlarm[7] 에는 progress 대한 정보 들어있음
                    frontAlarmActivity.putExtra("progress", arrayFromMakeAlarm[7])
                    context?.startActivity(frontAlarmActivity)
                    // quick 알람이었을 경우
                }
            }
            // quick 알람인지 아닌지 판단(i = 7은 progress임)
            if (i == 8){
                // quick 알람이므로 자동으로 리스트의 토글 버튼을 off 한다
                if (arrayFromMakeAlarm!![i] == 1){

                }
            }
        }

    }
}
