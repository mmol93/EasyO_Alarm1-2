package com.example.easyo_alarm

import java.util.*

class diffTime {
    fun diffWeek(weekList:List<Int>, hour : Int) : Int {
        // weekList : 해당 알람의 weekList

        // 오늘의 요일을 가져온다
        val calendar = Calendar.getInstance()
        val presentWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val presentHour = calendar.get(Calendar.HOUR_OF_DAY)
        var diff = 0

        for (i in 0..7){
            if (weekList[i] == 1){
                diff = presentWeek - (i + 1)    // i는 인덱스이기 때문에 요일이랑 같아지기 위해서는 +1이 필요함
                when(diff){
                    0 -> return 0
                    -1, 6 -> {
                        if (hour - presentHour >= 0){
                            return 1
                        }else{
                            return 0
                        }
                    }
                    -2, 5 -> {
                      if (hour - presentHour >= 0){
                          return 2
                      }else{
                          return 1
                      }
                    }
                    -3, 4 -> {
                        if (hour - presentHour >= 0){
                            return 3
                        }else{
                            return 2
                        }
                    }
                    -4, 3 -> {
                        if (hour - presentHour >= 0){
                            return 4
                        }else{
                            return 3
                        }
                    }
                    -5, 2 -> {
                        if (hour - presentHour >= 0){
                            return 5
                        }else{
                            return 4
                        }
                    }
                    -6, 1 -> {
                        if (hour - presentHour >= 0){
                            return 6
                        }else{
                            return 5
                        }
                    }
                }
            }
        }
        // 뭔가 다른 에러 발생할 시
        return -1
    }

    fun diffHour(hour:Int) : Int {
        // 지금 시간을 가져온다
        val calendar = Calendar.getInstance()
        val presentHour = calendar.get(Calendar.HOUR_OF_DAY)

        if (hour - presentHour >= 0){
            return hour - presentHour
        }else{
            return 24 - (presentHour - hour)
        }
    }

    fun diffMin(min:Int) : Int {
        // 지금 시간을 가져온다
        val calendar = Calendar.getInstance()
        val presentMin = calendar.get(Calendar.MINUTE)

        if (min - presentMin >= 0){
            return min - presentMin
        }else{
            return 60 - (presentMin - min)
        }

    }
}