package com.example.easyo_alarm

import java.util.*

// SQL 데이터 안에서 가장 최근의 알람을 찾아낸다
class RecentAlarm {
    fun checkSQL(SQLHelper : SQLHelper): MutableList<Int> {
        val timeList = mutableListOf<Int>() // 여기에 SQL 데이터에서 뽑아낸 시간 차이 정보가 들어간다

        // diffWeek로 오늘로부터 몇 일 뒤의 알람인지 알 수 있다
        // diffHour로 몇 시간 뒤의 알람인지알 수 있다
        // diffMin로 몇 분 뒤의 알람인지 알 수 있다
        // 문장려로 diffWeek + diffHour + diffMin 을 실시하고 int형으로 바꿀 경우
        // 제일 빠른 알람이 제일 작은 수가 된다 -> 이를 이용하여 제일 먼저 울리는 알람을 가려낸다

        // *** SQL의 모든 데이터를 가져와서 어댑터에 등록시킨다
        val sql_select = "select * from MaidAlarm"
        val c1 = SQLHelper.writableDatabase.rawQuery(sql_select, null)

        val idxList = mutableListOf<Int>()
        val hourList = mutableListOf<Int>()
        val minList = mutableListOf<Int>()
        val progressList = mutableListOf<Int>()
        val Sun = mutableListOf<Int>()
        val Mon = mutableListOf<Int>()
        val Tue = mutableListOf<Int>()
        val Wed = mutableListOf<Int>()
        val Thu = mutableListOf<Int>()
        val Fri = mutableListOf<Int>()
        val Sat = mutableListOf<Int>()
        val requestCode = mutableListOf<Int>()
        val quick = mutableListOf<Int>()
        val switch = mutableListOf<Int>()

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

            idxList.add(c1.getInt(index1))  // idx는 1부터 시작한다
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
        val weekList = mutableListOf<Int>()
        // 토글 버튼이 on인 것들만 판별
        for (i in idxList){
            if (switch[i-1] == 1){
                val hour = hourList[i-1]
                val min = minList[i-1]
                weekList.add(Sun[i-1])
                weekList.add(Mon[i-1])
                weekList.add(Tue[i-1])
                weekList.add(Wed[i-1])
                weekList.add(Thu[i-1])
                weekList.add(Fri[i-1])
                weekList.add(Sat[i-1])

                val diffTime = diffTime()
                val diffWeek = diffTime.diffWeek(weekList, hour, min)   // 알람까지 남은 일 수
                var diffHour = diffTime.diffHour(hour)  // 남은 시간
                var diffMin = diffTime.diffMin(min) // 남은 분

                var diffHourString = ""
                var diffMinString = ""

                // 동일 조건 계산을 위해 diffHour, diffMin은 자릿수를 항상 2자리로 만든다
                if (diffHour < 10){
                  diffHourString = "0$diffHour"
                }else{
                    diffHourString = diffHour.toString()
                }

                if (diffMin < 10){
                    diffMinString = "0$diffMin"
                }else{
                    diffMinString = diffMin.toString()
                }

                // 새로운 숫자를 만든다
                val resultTime = diffWeek.toString() + diffHourString + diffMinString
                timeList.add(resultTime.toInt())
            }
        }
        // timeList 안에서 제일 작은 숫자만 골라낸다
        val recentTime = timeList.minOrNull()?:0
        val resultIdx = timeList.indexOf(recentTime)    // 여기에 제일 가까운 알람의 인덱스가 들어간다

        // 위 resultIdx를 이용하여 해당 인덱스의 Hour, Min을 알 수 있다
        val resultHour = hourList[resultIdx]
        val resultMin = minList[resultIdx]

        val totalResult = mutableListOf<Int>(Sun[resultIdx], Mon[resultIdx], Tue[resultIdx], Wed[resultIdx],
        Thu[resultIdx], Fri[resultIdx], Sat[resultIdx], hourList[resultIdx], minList[resultIdx])

        return totalResult
    }
}