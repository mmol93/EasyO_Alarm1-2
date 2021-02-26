package com.example.easyo_alarm

import android.app.Application

class AppClass : Application() {

    // 0 : 계산 문제 모드 off / 1 : 계산 문제 모드 on
    var wayOfAlarm = 0  // 계산 문제를 제출할지 말지 선택(설정 탭에서 선택 가능)

    // 계산 문제에서 사용될 변수
    var counter = 1 // 문제 반복 몇 번 할지

    // notification 의 on / off 스위치
    var notificationSwitch = 0

    // 가장 최근 울릴 알람
    var recentTime = ""

    // 가장 최근 울릴 알람의 요일
    var recentWeek = ""
}