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

        setContentView(binder.root)
    }
}