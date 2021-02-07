package com.example.easyo_alarm

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.easyo_alarm.databinding.ActivityAlarmSetBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds

class AlarmSetActivity : AppCompatActivity() {
    lateinit var binder : ActivityAlarmSetBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm_set)
        binder = ActivityAlarmSetBinding.inflate(layoutInflater)

        // 1. 애드몹 초기화
        MobileAds.initialize(this) {}
        // 2. 애드몹 로드
        val adRequest = AdRequest.Builder().build()
        binder.adView.loadAd(adRequest)

        // numberPicker의 시간 부분 최대, 최소값 설정
        binder.numberPickerHour.maxValue = 12
        binder.numberPickerHour.minValue = 1

        // numberPicker의 분 부분 최대, 최소값 설정
        binder.numberPickerMin.maxValue = 59
        binder.numberPickerMin.minValue = 0

        // numberPicker에 오전 오후 텍스트 세팅
        val arg1 = arrayOf<String>(getString(R.string.alarmSet_AM), getString(R.string.alarmSet_PM))
        binder.numberPickerAMPM.minValue = 0
        binder.numberPickerAMPM.maxValue = arg1.size - 1
        binder.numberPickerAMPM.displayedValues = arg1

        // Cancel 버튼 클릭 시
        binder.buttonCancel.setOnClickListener {
            finish()
        }

        // Save 버튼 클릭 시
        binder.buttonSave.setOnClickListener {

        }


        setContentView(binder.root)
    }
}