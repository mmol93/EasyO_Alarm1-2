package com.example.easyo_alarm

import java.util.*

class test4 {
    fun test4(weekList : List<Int>){
        // weekList : 해당 알람의 weekList

        // 오늘 날짜 시, 분, 요일을 가져온다
        val calendar = Calendar.getInstance()
        val presentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val presentMin = calendar.get(Calendar.MINUTE)
        val presentWeek = calendar.get(Calendar.DAY_OF_WEEK)

        // presentWeek는 1부터 시작하고
        // weekList의 인덱스는 0부터 시작하므로 presentWeek에 -1 해줌
        // weekList에서 오늘 부분에 1이 있는지 확인
        if (weekList[presentWeek -1 ] == 1){

        }
        else if()
    }
}