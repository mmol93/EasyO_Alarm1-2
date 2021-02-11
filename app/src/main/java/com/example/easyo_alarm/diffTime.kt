package com.example.easyo_alarm

import android.content.Context
import android.widget.Toast
import java.util.*

class diffTime {

    // 현재 요일과 예약한 요일중 제일 빠른 요일을 찾는 메서드
    // 매개변수로 해당 알람의 weekList와 알람 시간을 받는다
    // 반환값 : xx일 뒤 알람의 "xx" 부분 = 즉, 가장 가까운 알람의 남은 일 수 반환
    fun diffWeek(weekList:List<Int>, hour : Int) : Int {
        // weekList : 해당 알람의 weekList

        // 오늘의 요일을 가져온다
        val calendar = Calendar.getInstance()
        val presentWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val presentHour = calendar.get(Calendar.HOUR_OF_DAY)
        var diff = 0
        val restOfDay = mutableListOf<Int>()

        for (i in 0..7){
            if (weekList[i] == 1){
                // i는 인덱스이기 때문에 요일이랑 같아지기 위해서는 +1이 필요함
                diff = presentWeek - (i + 1)    // 오늘 요일 - 알람 예약 요일
                // 일 -> 토까지 순서대로 진행하며 시간 계산을 통해 남은 일수를 restOfDay에 넣는다
                when(diff){
                    0 -> {
                        if (hour - presentHour >= 0){
                            restOfDay.add(0)
                        }else{
                            restOfDay.add(6)
                        }
                    }
                    -1, 6 -> {
                        if (hour - presentHour >= 0){
                            restOfDay.add(1)
                        }else{
                            restOfDay.add(0)
                        }
                    }
                    -2, 5 -> {
                      if (hour - presentHour >= 0){
                          restOfDay.add(2)
                      }else{
                          restOfDay.add(1)
                      }
                    }
                    -3, 4 -> {
                        if (hour - presentHour >= 0){
                            restOfDay.add(3)
                        }else{
                            restOfDay.add(2)
                        }
                    }
                    -4, 3 -> {
                        if (hour - presentHour >= 0){
                            restOfDay.add(4)
                        }else{
                            restOfDay.add(3)
                        }
                    }
                    -5, 2 -> {
                        if (hour - presentHour >= 0){
                            restOfDay.add(5)
                        }else{
                            restOfDay.add(4)
                        }
                    }
                    -6, 1 -> {
                        if (hour - presentHour >= 0){
                            restOfDay.add(6)
                        }else{
                            restOfDay.add(5)
                        }
                    }
                }
            }
        }
        // restOfday 리스트 안에서 제일 작은 숫자를 반환한다
        return restOfDay.minOrNull()?:0
    }

    // 현재 시간과 설정한 알람의 시간의 차이를 구하는 메서드
    // 매개변수: 알람 시간
    // 반환값 : 알람시간까지 남은 시간
    fun diffHour(hour:Int) : Int {
        // 지금 시간을 가져온다
        val calendar = Calendar.getInstance()
        val presentHour = calendar.get(Calendar.HOUR_OF_DAY)

        // 예약한 시간과 현재 시간의 차이를 계산하여 남은 시간 반환
        if (hour - presentHour >= 0){
            return hour - presentHour
        }else{
            return 24 - (presentHour - hour)
        }
    }

    // 현재 분과 설정한 알람의 분의 차이를 구하는 메서드
    // 매개변수: 알람 분
    // 반환값 : 알람분까지 남은 분
    fun diffMin(min:Int) : Int {
        // 지금 시간을 가져온다
        val calendar = Calendar.getInstance()
        val presentMin = calendar.get(Calendar.MINUTE)

        // 예약한 시간과 현재 시간의 차이를 계산하여 남은 시간 반환
        if (min - presentMin >= 0){
            return min - presentMin
        }else{
            return 60 - (presentMin - min)
        }
    }

    // 위에 있는 메서드들을 이용해서 문자열을 만든다(예: xx일 xx시간 xx분 뒤 알람 울림)
    // 매개변수 : context 값, diffWeek 결과값, diffHour 결과값, diffMin 결과값
    // 반환값 : 문자열 반환
    fun makeTextWithDiffTime(context: Context, restOfWeek : Int, restOfHour : Int, restOfMin : Int) : String{

        var text : String = ""
        // 당일일 때
        if (restOfWeek == 0){
            text = context.getString(R.string.alarmToast_inform1) + " "+ restOfHour.toString() +
                    " "+ context.getString(R.string.alarmToast_inform3) + " "+ restOfMin.toString() + " "+ context.getString(R.string.alarmToast_inform4)

        }
        // 다음 알람이 1일 이상일 때
        else if (restOfWeek >= 1){
            text = context.getString(R.string.alarmToast_inform1) + " "+ restOfWeek.toString() + " "+ context.getString(R.string.alarmToast_inform2) +
                    " "+ restOfHour.toString() + " "+ context.getString(R.string.alarmToast_inform3) + " "+ restOfMin.toString() + " "+ context.getString(R.string.alarmToast_inform4)
        }
        return text
    }
}