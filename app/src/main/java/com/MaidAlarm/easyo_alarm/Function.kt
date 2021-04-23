package com.MaidAlarm.easyo_alarm

import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageInfo
import android.util.Log
import com.MaidAlarm.easyo_alarm.notification.notification
import java.io.DataOutputStream
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class Function {
    // SQL의 모든 데이터를 매개변수에 집어넣음
    fun makeAlarmWithAllSQL(
            context: Context,
            ) {
        // ** SQL에서 모든 데이터를 들고와서 다시 알람 매니저에 등록해준다
        val SQLHelper = SQLHelper(context!!)
        val sql = "select * from MaidAlarm"
        val c1 = SQLHelper.writableDatabase.rawQuery(sql, null)
        val size = c1.count

        // SQL에 데이터가 1개라도 있을 때만 실시한다
        if (size > 0){
            // 모든 컬럼, 모든 레코드의 값 가져와서 리스트 안에 넣기
            while (c1.moveToNext()){
                val index1 = c1.getColumnIndex("idx")
                val index2 = c1.getColumnIndex("hourData")
                val index3 = c1.getColumnIndex("minData")
                val index4 = c1.getColumnIndex("progressData")
                val index5 = c1.getColumnIndex("Sun")
                val index6 = c1.getColumnIndex("Mon")
                val index7 = c1.getColumnIndex("Tue")
                val index8 = c1.getColumnIndex("Wed")
                val index9 = c1 .getColumnIndex("Thu")
                val index10 = c1.getColumnIndex("Fri")
                val index11 = c1.getColumnIndex("Sat")
                val index12 = c1.getColumnIndex("requestCode")
                val index13 = c1.getColumnIndex("quick")
                val index14 = c1.getColumnIndex("switch")
                val index16 = c1.getColumnIndex("bell")
                val index17 = c1.getColumnIndex("mode")

                // ** SQL에서 데이터를 가져와서 다시 알람 매니저로 보낸다 (아래 설명은 그 순서임)
                // 각 항목의 값을 해당 이름의 변수에 넣는다 -> Sun ~ Sat은 weekList로 만든다 ->
                // context, hour, min, progress, weekList, requestCode를 이용하여 makeAlarm() 객체를 만든다
                // switch의 값에 따라 once Or normal 알람 메서드를 호출한다

                val hour = c1.getInt(index2)
                val min = c1.getInt(index3)
                val progress = c1.getInt(index4)
                val Sun = c1.getInt(index5)
                val Mon = c1.getInt(index6)
                val Tue = c1.getInt(index7)
                val Wed = c1.getInt(index8)
                val Thu = c1.getInt(index9)
                val Fri = c1.getInt(index10)
                val Sat = c1.getInt(index11)
                val requestCode = c1.getInt(index12)
                val quick = c1.getInt(index13)
                val switch = c1.getInt(index14)
                val bellIndex = c1.getInt(index16)
                val alarmMode = c1.getInt(index17)

                val weekList = mutableListOf<Int>()
                weekList.add(Sun)
                weekList.add(Mon)
                weekList.add(Tue)
                weekList.add(Wed)
                weekList.add(Thu)
                weekList.add(Fri)
                weekList.add(Sat)

                val makeAlarm = makeAlarm(context, hour, min, progress, weekList, requestCode, bellIndex, alarmMode)

                // quick 알람일 경우
                if (quick == 1 && switch == 1){
                    makeAlarm.addNewAlarm_once()
                }
                // normal 알람일 경우
                else if (quick == 0 && switch == 1){
                    makeAlarm.addNewAlarm_normal()
                }
            }
        }
    }

    // 새로은 데이터를 SQL 데이터 베이스에 등록하고 알람 매니저도 등록한다
    fun makeSQLSetStraightAlarm(context: Context, actionTime : Int){
        // 현재 날따에서 몇 분 뒤를 설정하기 때문에 날짜 데이터를 가져온다
        val calendar = Calendar.getInstance()
        val presentDay = calendar.get(Calendar.DAY_OF_YEAR)
        val presentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val presentMin = calendar.get(Calendar.MINUTE)
        val presentSecond = calendar.get(Calendar.SECOND)
        val presentTimeMilli = calendar.timeInMillis
        var interval = 0

        // alarmFragment에서 사용한 방법과 동일한 방법으로 requestCode를 만든다
        val requestCode = presentDay.toString() + presentHour.toString() +
                presentMin.toString() + presentSecond.toString()

        // 설정한 시간에 따라 interval MilliSeconds를 부여한다
        when(actionTime){
            10 -> interval = 10 * 60 * 1000
            30 -> interval = 30 * 60 * 1000
            60 -> interval = 60 * 60 * 1000
        }

        val setTimeMilli = presentTimeMilli + interval

        // 여기서 인터벌 만큼의 MillisSeconds를 해당 시간의 시각, 분, 요일로 변경한다
        calendar.timeInMillis = setTimeMilli
        val setHour = calendar.get(Calendar.HOUR_OF_DAY)
        val setMin = calendar.get(Calendar.MINUTE)
        val setWeek = calendar.get(Calendar.DAY_OF_WEEK)
        var weekString = ""

        // sql 데이터 저장을 위해 해당 요일의 문자열을 판별
        when(setWeek){
            1 -> weekString = "Sun"
            2 -> weekString = "Mon"
            3 -> weekString = "Tue"
            4 -> weekString = "Wed"
            5 -> weekString = "Thu"
            6 -> weekString = "Fri"
            7 -> weekString = "Sat"
        }

        // sql 데이터 입력에 필요한 progress, bellIndex, alarmMode를 불러온다(setting에서 설정)
        

        // sql 데이터에 넣기
        val sqlHelper = SQLHelper(context)
        val sql_insert = """
                insert into MaidAlarm (hourData, minData, progressData, $weekString, requestCode, quick, bell, mode)
                values(?, ?, ?, ?, ?, ?, ?, ?)
                """.trimIndent()
        val c1 = sqlHelper.writableDatabase.rawQuery(sql_insert, null)

//        val arg1 = arrayOf(setHour, setMin, progress, 1, requestCode.toInt(), 1, bellIndex, alarmMode)
    }

    // notification 갱신
    fun renewNotification(context: Context){
        val SQLHelper = SQLHelper(context!!)
        val sql = "select * from MaidAlarm"
        val c1 = SQLHelper.writableDatabase.rawQuery(sql, null)
        val size = c1.count

        if (size > 0){
            // RecentAlarm 갱신하기
            val recentAlarm = RecentAlarm()
            val recentTimeList = recentAlarm.checkSQL(SQLHelper)
            // 알림은 있지만 모든 토글이 off 일 떄
            if (recentTimeList[0] == -1){
                val notification = notification()
                notification.cancelNotification(context)
            }
            else{
                var textForWeek = ""     // notification에 사용하기 위한 텍스트를 정의1
                // 시간 부분 입력
                var recentHour = ""
                var recentMin = ""
                if (recentTimeList[7] < 10){
                    recentHour = "0${recentTimeList[7]}"
                }else{
                    recentHour = "${recentTimeList[7]}"
                }
                if (recentTimeList[8] < 10){
                    recentMin = "0${recentTimeList[8]}"
                }else{
                    recentMin = "${recentTimeList[8]}"
                }

                // 다음 알림의 시각
                val recentTime = "$recentHour : $recentMin"

                // 월요일에 알람 있을 때 ~ 일요일에 알람 있을 때 -> 요일 부분 입력
                if (recentTimeList[1] == 1){
                    textForWeek = textForWeek + context.getString(R.string.week_mon) + ", "
                }
                if (recentTimeList[2] == 1){
                    textForWeek = textForWeek + context.getString(R.string.week_tue) + ", "
                }
                if (recentTimeList[3] == 1){
                    textForWeek = textForWeek + context.getString(R.string.week_wed) + ", "
                }
                if (recentTimeList[4] == 1){
                    textForWeek = textForWeek + context.getString(R.string.week_thur) + ", "
                }
                if (recentTimeList[5] == 1){
                    textForWeek = textForWeek + context.getString(R.string.week_fri) + ", "
                }
                if (recentTimeList[6] == 1){
                    textForWeek = textForWeek + context.getString(R.string.week_sat) + ", "
                }
                if (recentTimeList[0] == 1){
                    textForWeek = textForWeek + context.getString(R.string.week_sun) + ", "
                }
                // 체크된 요일을 문자로 표시한다
                if (recentTimeList[0] == 1 || recentTimeList[1] == 1 || recentTimeList[2] == 1 || recentTimeList[3] == 1 || recentTimeList[4] == 1
                        || recentTimeList[5] == 1 || recentTimeList[6] == 1){
                    // textForWeek에서 마지막 부분 콤마 제거하기
                    if (textForWeek.length > 2){
                        textForWeek = textForWeek.removeRange(
                                textForWeek.length - 2,
                                textForWeek.length - 1
                        )
                    }

                }

                val recentWeek = textForWeek    // 다음 알림의 주

                // notification 갱신
                if (recentTime.isNotEmpty()){
                    val notification = notification()
                    val notificationManager =context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notification.getNotification(
                            context!!,
                            "chanel1",
                            "첫 번째 채널",
                            notificationManager
                    )
                    notification.makeNotification(recentTime, recentWeek, context!!, notificationManager)
                }
            }
        }
    }

    // notification 및 MainTextView 갱신
    // 토글 부분 확인함 & RecyclerView에서 사용됨
    fun renewNotiInRecycler(context: Context, app:AppClass, SQLHelper : SQLHelper){
        // ** notification 재설정
        val recentAlarm = RecentAlarm()
        val recentTimeList = recentAlarm.checkSQL(SQLHelper)
        // 1개라도 on인 토글이 있을 때
        if (recentTimeList[0] != -1){
            // 시간 부분 입력
            var recentHour = ""
            var recentMin = ""
            if (recentTimeList[7] < 10){
                recentHour = "0${recentTimeList[7]}"
            }else{
                recentHour = "${recentTimeList[7]}"
            }
            if (recentTimeList[8] < 10){
                recentMin = "0${recentTimeList[8]}"
            }else{
                recentMin = "${recentTimeList[8]}"
            }
            app.recentTime = "$recentHour : $recentMin"
            Log.d("RecyclerAdapter", "recentHour: $recentHour, recentMin: $recentMin")
        }

        var textForWeek = ""     // notification에 사용하기 위한 텍스트를 정의1

        // 월요일에 알람 있을 때 ~ 일요일에 알람 있을 때 -> 요일 부분 입력
        if (recentTimeList[1] == 1){
            textForWeek = textForWeek + context.getString(R.string.week_mon) + ", "
        }
        if (recentTimeList[2] == 1){
            textForWeek = textForWeek + context.getString(R.string.week_tue) + ", "
        }
        if (recentTimeList[3] == 1){
            textForWeek = textForWeek + context.getString(R.string.week_wed) + ", "
        }
        if (recentTimeList[4] == 1){
            textForWeek = textForWeek + context.getString(R.string.week_thur) + ", "
        }
        if (recentTimeList[5] == 1){
            textForWeek = textForWeek + context.getString(R.string.week_fri) + ", "
        }
        if (recentTimeList[6] == 1){
            textForWeek = textForWeek + context.getString(R.string.week_sat) + ", "
        }
        if (recentTimeList[0] == 1){
            textForWeek = textForWeek + context.getString(R.string.week_sun) + ", "
        }
        // 체크된 요일을 문자로 표시한다
        if (recentTimeList[0] == 1 || recentTimeList[1] == 1 || recentTimeList[2] == 1 || recentTimeList[3] == 1 || recentTimeList[4] == 1
                || recentTimeList[5] == 1 || recentTimeList[6] == 1){

            // textForWeek에서 마지막 부분 콤마 제거하기
            if (textForWeek.length > 2){
                textForWeek = textForWeek.removeRange(
                        textForWeek.length - 2,
                        textForWeek.length - 1
                )
            }
            app.recentWeek = textForWeek    // notification에 사용하기 위한 텍스트 정의2
        }

        // notification 갱신
        val notification = notification()
        val notificationManager = context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notification.getNotification(context!!, "chanel1", "첫 번째 채널", notificationManager)
        notification.makeNotification(app, context!!, notificationManager)
    }


    // SQL의 모든 row에서 requestCode 컬럼 부분만 가져오기
    fun CheckRequestCodeSQL(context: Context, SQLHelper: SQLHelper, requestCode: MutableList<Int>): MutableList<Int> {
        val app = context.applicationContext as AppClass
        // *** SQL의 모든 데이터를 가져와서 어댑터에 등록시킨다
        val sql_select = "select * from MaidAlarm"
        val c1 = SQLHelper.writableDatabase.rawQuery(sql_select, null)

        while (c1.moveToNext()){
            val index12 = c1.getColumnIndex("requestCode")
            requestCode.add(c1.getInt(index12))
        }
        return requestCode
    }

    // 현재 앱의 버전을 가져옴
    fun checkAppVersion(context: Context) : String {
        val pi: PackageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        return pi.versionName
    }

    // 파일 데이터 저장(makeAlarm에서 data2.bat 사용중 - 최근 울린 알람 기록용)
    fun saveFileAsString(fileName : String, context: Context){
        val currentDateTime = Calendar.getInstance().time
        var dateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.KOREA).format(currentDateTime)

        val fos = context.openFileOutput(fileName, Context.MODE_PRIVATE)
        val dos = DataOutputStream(fos)
        dos.writeChars(dateFormat)

        dos.flush()
        dos.close()
    }

    // 업데이트 거부 시 해당 날짜 데이터 저장하게 하기
    fun saveFileWithCurrentTime(fileName : String, context: Context){
        val currentTimeMilli = System.currentTimeMillis()

        val fos = context.openFileOutput(fileName, Context.MODE_PRIVATE)
        val dos = DataOutputStream(fos)
        dos.writeLong(currentTimeMilli)

        dos.flush()
        dos.close()
    }
}