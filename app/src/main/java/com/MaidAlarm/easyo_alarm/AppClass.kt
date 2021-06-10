package com.MaidAlarm.easyo_alarm

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.media.MediaPlayer
import android.os.Vibrator
import com.MaidAlarm.easyo_alarm.databinding.FragmentAlarmBinding

class AppClass : Application() {

    // 싱글턴으로 사용
    companion object{
        @SuppressLint("StaticFieldLeak")
        lateinit var context : AppClass
        var provider = ""
        var countryCode = ""
        var stateCode : String? = ""
        var stateName : String? = ""
        var cityName : String? = ""
        var lat = 0.0
        var lon = 0.0
        val hour = ArrayList<String>()
        val min = ArrayList<String>()
    }

    // 처음 사용 설명서 표시 유무
    var initialStart = 0

    // 0 : 계산 문제 모드 off / 1 : 계산 문제 모드 on
    var wayOfAlarm = 0  // 계산 문제를 제출할지 말지 선택(설정 탭에서 선택 가능)

    // 계산 문제에서 사용될 변수
    var counter = 1 // 문제 반복 몇 번 할지

    // notification 의 on / off 스위치
    var notificationSwitch = 1

    // 가장 최근 울릴 알람
    // 시간:분 형태
    var recentTime = ""

    // 가장 최근 울릴 알람의 요일
    var recentWeek = ""

    // alarmFragment의 binder 변수
    // 1. RecyclerAdapter.kt에서 쓰레기 버튼 클릭 시 alarmFragment에 있는 view를 컨트롤 하기 위해 사용됨
    lateinit var binder_alarmFragent : FragmentAlarmBinding

    // alarmFragment의 context 변수
    // 1. RecyclerAdapter.kt에서 쓰레기 버튼 클릭 시 alarmFragment에 있는 view를 컨트롤 하기 위해 사용됨
    lateinit var context_alarmFragent : Context

    // vibration 조절하기
    lateinit var vibrate : Vibrator

    // 가장 최근 울린 알람의 progress
    var lastProgress = 0

    // thread 트리거
    var threadTrigger = 0

    // 어떤 음악을 울릴지 지정
    var bellIndex = 0

    // 버튼 클릭 시 음악 정지를 위해 설정
    lateinit var mediaPlayer: MediaPlayer

    // 항상 어떤 Activity가 실행되면 해당 Activity의 context 가져오게 하기
    override fun onCreate() {
        super.onCreate()
        context = this
    }
}