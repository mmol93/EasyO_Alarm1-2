package com.example.easyo_alarm

class test1 {
    val SEC = 60
    val MIN = 60
    val HOUR = 24
    val DAY = 30
    val MONTH = 12

    // 매개변수로 설정 Mllis 시간을 받아온다
    fun formatTimeString(regTime: Long): String? {
        // 현재 시간 가져옴
        val curTime = System.currentTimeMillis()
        // 현재 시간에서 받아온 시간을 빼서 1000으로 나눈다(밀리 분-> 일반 분 변환)
        var diffTime = (curTime - regTime) / 1000
        var msg: String? = null

        if (diffTime < SEC) {
            msg = "방금 전"
        } else if (SEC.let { diffTime /= it; diffTime } < MIN) {
            msg = diffTime.toString() + "분 전"
        } else if (MIN.let { diffTime /= it; diffTime } < HOUR) {
            msg = diffTime.toString() + "시간 전"
        } else if (HOUR.let { diffTime /= it; diffTime } < DAY) {
            msg = diffTime.toString() + "일 전"
        } else if (DAY.let { diffTime /= it; diffTime } < MONTH) {
            msg = diffTime.toString() + "달 전"
        } else {
            msg = diffTime.toString() + "년 전"
        }
        return msg
    }
}