package com.MaidAlarm.easyo_alarm

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import java.io.DataOutputStream
import java.text.SimpleDateFormat
import java.time.LocalDate
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

                val weekList = mutableListOf<Int>()
                weekList.add(Sun)
                weekList.add(Mon)
                weekList.add(Tue)
                weekList.add(Wed)
                weekList.add(Thu)
                weekList.add(Fri)
                weekList.add(Sat)

                val makeAlarm = makeAlarm(context, hour, min, progress, weekList, requestCode, bellIndex)

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