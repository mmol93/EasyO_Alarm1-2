package com.example.easyo_alarm

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import com.example.easyo_alarm.databinding.ActivityAlarmSetBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import java.util.ArrayList

class AlarmSetActivity : AppCompatActivity() {
    lateinit var binder: ActivityAlarmSetBinding
    var seekValue = 0

    // 모든 숫자(시간) 초기화하기
    var hour = 0
    var min = 0

    // 예약한 요일을 담는 List
    val weekList = ArrayList<Int>(7)

    // diffTime에 대한 객체 생성
    val diffTime = com.example.easyo_alarm.diffTime()
    var restOfWeek : Int = 0
    var restOfHour : Int = 1
    var restOfMin : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        // weekList 초기화
        for (i in 0..7){
            weekList.add(0)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm_set)
        binder = ActivityAlarmSetBinding.inflate(layoutInflater)
        // 요일 클릭에 대한 변수 정의
        var Sun = 0
        var Mon = 0
        var Tue = 0
        var Wed = 0
        var Thu = 0
        var Fri = 0
        var Sat = 0

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

        // numberPickerHour 터치시
        binder.numberPickerHour.setOnValueChangedListener { picker, oldVal, newVal ->
            // binder.numberPickerAMPM.value == 0은 AM을 가리킨다
            if (binder.numberPickerAMPM.value == 0){
                restOfHour = diffTime.diffHour(newVal)
                restOfWeek = diffTime.diffWeek(weekList, newVal)
                binder.alarmSetInform.text = diffTime.makeTextWithDiffTime(this, restOfWeek, restOfHour, restOfMin)
            }else{
                val newVal2 = newVal + 12
                restOfHour = diffTime.diffHour(newVal2)
                restOfWeek = diffTime.diffWeek(weekList, newVal2)
                // 24시는 0시로 설정되게 한다
                if (restOfHour == 24){
                    restOfHour = 0
                }
                binder.alarmSetInform.text = diffTime.makeTextWithDiffTime(this, restOfWeek, restOfHour, restOfMin)
            }
        }

        // numberPickerMin 터치시
        binder.numberPickerMin.setOnValueChangedListener { picker, oldVal, newVal ->
            restOfMin = diffTime.diffMin(newVal)
            binder.alarmSetInform.text = diffTime.makeTextWithDiffTime(this, restOfWeek, restOfHour, restOfMin)
        }

        // Cancel 버튼 클릭 시
        binder.buttonCancel.setOnClickListener {
            finish()
        }

        // Save 버튼 클릭 시
        // 요일에 대한 정보도 같이 넘겨줘야한다
        binder.buttonSave.setOnClickListener {
            // ** 시간, 분에 대한 설정
            // binder.numberPickerAMPM.value == 0은 AM을 가리킨다
            if (binder.numberPickerAMPM.value == 0){
                hour = binder.numberPickerHour.value
            }else{
                hour = binder.numberPickerHour.value + 12
                // 24시는 0시로 설정되게 한다
                if (hour == 24){
                    hour = 0
                }
            }

            min = binder.numberPickerMin.value
            Log.d("AlarmSetActivity", "hour: $hour")
            Log.d("AlarmSetActivity", "min: $min")

            // ** 요일을 한개라도 설정해야 데이터를 SQL에 쓰고 액티비티를 종료하게 한다
            if (Sun == 1 || Mon == 1 || Tue == 1 || Wed == 1 || Thu == 1 || Fri == 1 || Sat== 1){

                val result_intent = Intent()
                result_intent.putExtra("hour", hour)
                result_intent.putExtra("min", min)
                result_intent.putExtra("progress", seekValue)
                result_intent.putExtra("weekList", weekList)

                // ** -> alarmFragment로 이동한다
                setResult(200, result_intent)

                finish()
            }
            // ** 요일을 하나라도 설정하지 않을 경우 Toast로 알려준다
            else{
                val toast = Toast.makeText(this, getString(R.string.alarmSet_Toast), Toast.LENGTH_LONG)
                toast.show()
            }
        }

        // 일요 텍스트 클릭
        binder.alarmSetSun.setOnClickListener {
            restOfMin = binder.numberPickerMin.value
            if (Sun == 0){
                Sun = textWeek(binder.alarmSetSun, Sun)
                weekList[0] = 1
                if (binder.numberPickerAMPM.value == 0){
                    hour = binder.numberPickerHour.value
                }else{
                    hour = binder.numberPickerHour.value + 12
                }
                restOfWeek = diffTime.diffWeek(weekList, hour)
                binder.alarmSetInform.text = diffTime.makeTextWithDiffTime(this, restOfWeek, restOfHour, restOfMin)
            }else{
                Sun = textWeek(binder.alarmSetSun, Sun)
                weekList[0] = 0
                restOfWeek = diffTime.diffWeek(weekList, hour)
                binder.alarmSetInform.text = diffTime.makeTextWithDiffTime(this, restOfWeek, restOfHour, restOfMin)
            }
        }

        // 월요 텍스트 클릭
        binder.alarmSetMon.setOnClickListener {
            restOfMin = binder.numberPickerMin.value
            if (Mon == 0){
                Mon = textWeek(binder.alarmSetMon, Mon)
                weekList[1] = 1
                if (binder.numberPickerAMPM.value == 0){
                    hour = binder.numberPickerHour.value
                }else{
                    hour = binder.numberPickerHour.value + 12
                }
                restOfWeek = diffTime.diffWeek(weekList, hour)
                binder.alarmSetInform.text = diffTime.makeTextWithDiffTime(this, restOfWeek, restOfHour, restOfMin)
            }else{
                Mon = textWeek(binder.alarmSetMon, Mon)
                weekList[1] = 0
                restOfWeek = diffTime.diffWeek(weekList, hour)
                binder.alarmSetInform.text = diffTime.makeTextWithDiffTime(this, restOfWeek, restOfHour, restOfMin)
            }
        }

        // 화요 텍스트 클릭
        binder.alarmSetTues.setOnClickListener {
            restOfMin = binder.numberPickerMin.value
            if (Tue == 0){
                Tue = textWeek(binder.alarmSetTues, Tue)
                weekList[2] = 1
                if (binder.numberPickerAMPM.value == 0){
                    hour = binder.numberPickerHour.value
                }else{
                    hour = binder.numberPickerHour.value + 12
                }
                restOfWeek = diffTime.diffWeek(weekList, hour)
                binder.alarmSetInform.text = diffTime.makeTextWithDiffTime(this, restOfWeek, restOfHour, restOfMin)
            }else{
                Tue = textWeek(binder.alarmSetTues, Tue)
                weekList[2] = 0
                restOfWeek = diffTime.diffWeek(weekList, hour)
                binder.alarmSetInform.text = diffTime.makeTextWithDiffTime(this, restOfWeek, restOfHour, restOfMin)
            }

        }

        // 수요 텍스트 클릭
        binder.alarmSetWed.setOnClickListener {
            restOfMin = binder.numberPickerMin.value
            if (Wed == 0){
                Wed = textWeek(binder.alarmSetWed, Wed)
                weekList[3] = 1
                if (binder.numberPickerAMPM.value == 0){
                    hour = binder.numberPickerHour.value
                }else{
                    hour = binder.numberPickerHour.value + 12
                }
                restOfWeek = diffTime.diffWeek(weekList, hour)
                binder.alarmSetInform.text = diffTime.makeTextWithDiffTime(this, restOfWeek, restOfHour, restOfMin)
            }else{
                Wed = textWeek(binder.alarmSetWed, Wed)
                weekList[3] = 0
                restOfWeek = diffTime.diffWeek(weekList, hour)
                binder.alarmSetInform.text = diffTime.makeTextWithDiffTime(this, restOfWeek, restOfHour, restOfMin)
            }
        }

        // 목요 텍스트 클릭
        binder.alarmSetThur.setOnClickListener {
            restOfMin = binder.numberPickerMin.value
            if (Thu == 0){
                Thu = textWeek(binder.alarmSetThur, Thu)
                weekList[4] = 1
                if (binder.numberPickerAMPM.value == 0){
                    hour = binder.numberPickerHour.value
                }else{
                    hour = binder.numberPickerHour.value + 12
                }
                restOfWeek = diffTime.diffWeek(weekList, hour)
                binder.alarmSetInform.text = diffTime.makeTextWithDiffTime(this, restOfWeek, restOfHour, restOfMin)
            }else{
                Thu = textWeek(binder.alarmSetThur, Thu)
                weekList[4] = 0
                restOfWeek = diffTime.diffWeek(weekList, hour)
                binder.alarmSetInform.text = diffTime.makeTextWithDiffTime(this, restOfWeek, restOfHour, restOfMin)
            }
        }

        // 금요 텍스트 클릭
        binder.alarmSetFri.setOnClickListener {
            restOfMin = binder.numberPickerMin.value
            if (Fri == 0){
                Fri = textWeek(binder.alarmSetFri, Fri)
                weekList[5] = 1
                if (binder.numberPickerAMPM.value == 0){
                    hour = binder.numberPickerHour.value
                }else{
                    hour = binder.numberPickerHour.value + 12
                }
                restOfWeek = diffTime.diffWeek(weekList, hour)
                binder.alarmSetInform.text = diffTime.makeTextWithDiffTime(this, restOfWeek, restOfHour, restOfMin)
            }else{
                Fri = textWeek(binder.alarmSetFri, Fri)
                weekList[5] = 0
                restOfWeek = diffTime.diffWeek(weekList, hour)
                binder.alarmSetInform.text = diffTime.makeTextWithDiffTime(this, restOfWeek, restOfHour, restOfMin)
            }
        }

        // 토요 텍스트 클릭
        binder.alarmSetSat.setOnClickListener {
            restOfMin = binder.numberPickerMin.value
            if (Sat == 0){
                Sat = textWeek(binder.alarmSetSat, Sat)
                weekList[6] = 1
                if (binder.numberPickerAMPM.value == 0){
                    hour = binder.numberPickerHour.value
                }else{
                    hour = binder.numberPickerHour.value + 12
                }
                restOfWeek = diffTime.diffWeek(weekList, hour)
                binder.alarmSetInform.text = diffTime.makeTextWithDiffTime(this, restOfWeek, restOfHour, restOfMin)
            }else{
                Sat = textWeek(binder.alarmSetSat, Sat)
                weekList[6] = 0
                restOfWeek = diffTime.diffWeek(weekList, hour)
                binder.alarmSetInform.text = diffTime.makeTextWithDiffTime(this, restOfWeek, restOfHour, restOfMin)
            }
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

        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {

        }
    }

    // 텍스트뷰에 색깔 넣기
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

}