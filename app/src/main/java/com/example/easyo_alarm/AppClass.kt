package com.example.easyo_alarm

import android.app.Application
import android.content.Context
import com.example.easyo_alarm.databinding.FragmentAlarmBinding

class AppClass : Application() {

    // 0 : 계산 문제 모드 off / 1 : 계산 문제 모드 on
    var wayOfAlarm = 0  // 계산 문제를 제출할지 말지 선택(설정 탭에서 선택 가능)

    // 계산 문제에서 사용될 변수
    var counter = 1 // 문제 반복 몇 번 할지

    // notification 의 on / off 스위치
    var notificationSwitch = 1

    // 가장 최근 울릴 알람
    var recentTime = ""

    // 가장 최근 울릴 알람의 요일
    var recentWeek = ""

    // 1일 때 에러 발생했다는 의미임
    var errorCode = 0

    // alarmFragment의 binder 변수
    // 1. RecyclerAdapter.kt에서 쓰레기 버튼 클릭 시 alarmFragment에 있는 view를 컨트롤 하기 위해 사용됨
    lateinit var binder_alarmFragent : FragmentAlarmBinding

    // alarmFragment의 context 변수
    // 1. RecyclerAdapter.kt에서 쓰레기 버튼 클릭 시 alarmFragment에 있는 view를 컨트롤 하기 위해 사용됨
    lateinit var context_alarmFragent : Context

}