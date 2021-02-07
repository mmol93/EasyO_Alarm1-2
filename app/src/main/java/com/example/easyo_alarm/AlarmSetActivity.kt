package com.example.easyo_alarm

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import android.widget.TextView
import com.example.easyo_alarm.databinding.ActivityAlarmSetBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds

class AlarmSetActivity : AppCompatActivity() {
    lateinit var binder: ActivityAlarmSetBinding
    var seekValue = 0

    // 모든 숫자(시간) 초기화하기
    var hour = 0
    var min = 0
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

        // numberPicker에 오전 오후 텍스트 세팅..
        val arg1 = arrayOf<String>(getString(R.string.alarmSet_AM), getString(R.string.alarmSet_PM))
        binder.numberPickerAMPM.minValue = 0
        binder.numberPickerAMPM.maxValue = arg1.size - 1
        binder.numberPickerAMPM.displayedValues = arg1

        // Cancel 버튼 클릭 시
        binder.buttonCancel.setOnClickListener {
            finish()
        }

        // Save 버튼 클릭 시 - 미완성
        // 요일에 대한 정보도 같이 넘겨줘야한다
        binder.buttonSave.setOnClickListener {
            hour = binder.numberPickerHour.value
            // binder.numberPickerAMPM.value == 0은 AM을 가리킨다
            if (binder.numberPickerAMPM.value == 0){

            }else{
                // 자동으로 else는 PM을 가리키게 된다

            }
            min = binder.numberPickerMin.value

            val result_intent = Intent()
            result_intent.putExtra("hour", hour)
            result_intent.putExtra("min", min)
            result_intent.putExtra("progress", seekValue)

            // -> alarmFragment로 이동한다
            setResult(200, result_intent)

            finish()
        }

        // 요일 클릭에 대한 정의
        var Sun = 0
        var Mon = 0
        var Tue = 0
        var Wed = 0
        var Thu = 0
        var Fri = 0
        var Sat = 0

        // 일요 텍스트 클릭
        binder.alarmSetSun.setOnClickListener {
            Sun = textWeek(binder.alarmSetSun, Sun)
        }

        // 월요 텍스트 클릭
        binder.alarmSetMon.setOnClickListener {
            Mon = textWeek(binder.alarmSetMon, Mon)
        }

        // 화요 텍스트 클릭
        binder.alarmSetTues.setOnClickListener {
            Tue = textWeek(binder.alarmSetTues, Tue)
        }

        // 수요 텍스트 클릭
        binder.alarmSetWed.setOnClickListener {
            Wed = textWeek(binder.alarmSetWed, Wed)
        }

        // 목요 텍스트 클릭
        binder.alarmSetThur.setOnClickListener {
            Thu = textWeek(binder.alarmSetThur, Thu)
        }

        // 금요 텍스트 클릭
        binder.alarmSetFri.setOnClickListener {
            Fri = textWeek(binder.alarmSetFri, Fri)
        }

        // 토요 텍스트 클릭
        binder.alarmSetSat.setOnClickListener {
            Sat = textWeek(binder.alarmSetSat, Sat)
        }

        // seekBar의 Progress 값을 가져온다
        binder.volumeSeekBar.setOnSeekBarChangeListener(seekListener)
        setContentView(binder.root)


    }

    // seekBar에 대한 리스너 정의
    val seekListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            when (seekBar?.id) {
                R.id.volumeSeekBar -> {
                    seekValue = progress
                }
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
            TODO("Not yet implemented")
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            TODO("Not yet implemented")
        }
    }

    fun textWeek(textView : TextView, week : Int) : Int {
        if (week == 0){
            textView.setBackgroundColor(Color.parseColor("#1ABC9C"))
            return 1
        }
        else{
            textView.setBackgroundColor(Color.parseColor("#aaaaaa"))
            return 0
        }
    }

    fun calculateTime(hour : Int, min : Int){

    }

}