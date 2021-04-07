package com.MaidAlarm.easyo_alarm

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager


class Function {
    // SQL의 모든 데이터를 매개변수에 집어넣음
    fun checkAllSQL(
            context: Context,
            SQLHelper: SQLHelper,
            idxList: MutableList<Int>,
            hourList: MutableList<Int>,
            minList: MutableList<Int>,
            progressList: MutableList<Int>,
            Sun: MutableList<Int>,
            Mon: MutableList<Int>,
            Tue: MutableList<Int>,
            Wed: MutableList<Int>,
            Thu: MutableList<Int>,
            Fri: MutableList<Int>,
            Sat: MutableList<Int>,
            requestCode: MutableList<Int>,
            quick: MutableList<Int>,
            switch: MutableList<Int>)
        {
        val app = context.applicationContext as AppClass
        // *** SQL의 모든 데이터를 가져와서 어댑터에 등록시킨다
        val sql_select = "select * from MaidAlarm"
        val c1 = SQLHelper.writableDatabase.rawQuery(sql_select, null)

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

            idxList.add(c1.getInt(index1))
            hourList.add(c1.getInt(index2))
            minList.add(c1.getInt(index3))
            progressList.add(c1.getInt(index4))
            Sun.add(c1.getInt(index5))
            Mon.add(c1.getInt(index6))
            Tue.add(c1.getInt(index7))
            Wed.add(c1.getInt(index8))
            Thu.add(c1.getInt(index9))
            Fri.add(c1.getInt(index10))
            Sat.add(c1.getInt(index11))
            requestCode.add(c1.getInt(index12))
            quick.add(c1.getInt(index13))
            switch.add(c1.getInt(index14))
        }
    }
    // 현재 앱의 버전을 가져옴
    fun checkAppVersion(context: Context) : String {
        val pi: PackageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        return pi.versionName
    }
}