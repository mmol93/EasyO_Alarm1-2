package com.example.easyo_alarm

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import android.widget.Toast
import com.example.easyo_alarm.databinding.ActivityShortAlarmSetBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds

class ShortAlarmSetActivity : AppCompatActivity() {
    // 모든 숫자(시간) 초기화하기
    var hour = 0
    var min = 0

    lateinit var binder: ActivityShortAlarmSetBinding

    var seekValue = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_short_alarm_set)
        binder = ActivityShortAlarmSetBinding.inflate(layoutInflater)

        // 1. 애드몹 초기화
        MobileAds.initialize(this) {}
        // 2. 애드몹 로드
        val adRequest = AdRequest.Builder().build()
        binder.adView.loadAd(adRequest)

        // +60분 버튼 클릭시
        binder.button60.setOnClickListener {
            hour += 1
            if (hour > 99) {
                hour = 99
                val t1 = Toast.makeText(this, getString(R.string.timeOver), Toast.LENGTH_SHORT)
                t1.show()
            }
            // 포맷 바꾸기(문자표기 + 자연수 2자리로 변경)
            val transHour = String.format("%02d", hour)
            binder.textView6Hour.text = "$transHour"
        }

        // +30분 버튼 클릭시
        binder.button30.setOnClickListener {
            addMinute(30)
        }

        // +15분 버튼 클릭 시
        binder.button15.setOnClickListener {
            addMinute(15)
        }

        // +10분 버튼 클릭 시
        binder.button10.setOnClickListener {
            addMinute(10)
        }

        // +5분 버튼 클릭 시
        binder.button5.setOnClickListener {
            addMinute(5)
        }

        // +1분 버튼 클릭 시
        binder.button1.setOnClickListener {
            addMinute(1)
        }

        // 캔슬 버튼 클릭시
        binder.buttonCancel.setOnClickListener {
            finish()
        }

        // 저장 버튼 클릭 시
        binder.buttonSave.setOnClickListener {
            val result_intent = Intent()

            result_intent.putExtra("hour", hour)
            result_intent.putExtra("min", min)
            result_intent.putExtra("progress", seekValue)

            setResult(100, result_intent)

            finish()
        }

        // seekingBar 리스너
        // seekBar의 Progress 값을 가져온다
        binder.volumeSeekBar.setOnSeekBarChangeListener(seekListener)

        setContentView(binder.root)
    }

    fun addMinute(add1: Int) {
        min += add1
        if (min >= 60) {
            hour += 1
            min -= 60
            // 시간을 01시 처럼 2자리로 표현하기 위해 변환
            val transHour = String.format("%02d", hour)
            binder.textView6Hour.text = "$transHour"
            // 분을 01시 처럼 2자리로 표현하기 위해 변환
            val transMin = String.format("%02d", min)
            binder.textView8Min.text = "$transMin"
        } else {
            val transMin = String.format("%02d", min)
            binder.textView8Min.text = "$transMin"
        }
    }

    val seekListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            when (seekBar?.id) {
                R.id.volumeSeekBar -> {
                    seekValue = progress
                }
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {

        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {

        }
    }
}