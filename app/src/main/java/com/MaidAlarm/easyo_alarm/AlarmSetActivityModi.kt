package com.MaidAlarm.easyo_alarm

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import com.MaidAlarm.easyo_alarm.databinding.ActivityAlarmSetModiBinding
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import java.util.ArrayList

class AlarmSetActivityModi : AppCompatActivity() {
    lateinit var binder : ActivityAlarmSetModiBinding
    lateinit var app : AppClass
    lateinit var SQLHelper : SQLHelper

    // diffTime에 대한 객체 생성
    val diffTime = com.MaidAlarm.easyo_alarm.diffTime()
    var restOfWeek : Int = 0
    var restOfHour : Int = 1
    var restOfMin : Int = 0

    var seekValue = 100

    // 예약한 요일을 담는 List
    var weekList = ArrayList<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm_set_modi)
        binder = ActivityAlarmSetModiBinding.inflate(layoutInflater)
        app = application as AppClass
        SQLHelper = SQLHelper(this)

        // 아래의 getIntExtra는 RecyclerAdapter에서 옴
        val position = intent.getIntExtra("position", -1)
        var setHour = intent.getIntExtra("setHour", 0)
        var setMin = intent.getIntExtra("setMin", 0)
        val requestCode = intent.getIntExtra("requestCode", 0)
        val setProgress = intent.getIntExtra("setProgress", 0)
        var setQuick = intent.getIntExtra("setQuick", 0)
        weekList = intent.getIntegerArrayListExtra("setWeek")!!
        var setAMPM = 0

        // 제대로된 아이템의 위치를 알 수 없거나 requestCode가 이상할 경우 토스트 출력 후 액티비티 종료
        if (position == -1 || requestCode == 0){
            Toast.makeText(this, getString(R.string.AlarmSetActivityModi_error), Toast.LENGTH_SHORT).show()
            finish()
        }

        // quick 알람을 수정할 경우 일반 알람으로 바뀐다는 것을 통지
        if (setQuick == 1){
            Toast.makeText(this, getString(R.string.AlarmSetActivityModi_quickToNormal), Toast.LENGTH_LONG).show()
        }


        // 2. 애드몹 로드
        val adRequest = AdRequest.Builder().build()
        binder.adView.loadAd(adRequest)

        binder.adView.adListener = object : AdListener(){
            override fun onAdFailedToLoad(p0: Int) {
                super.onAdFailedToLoad(p0)
                Log.d("adMob", "alarmSet 광고 로드 실패")
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
                Log.d("adMob", "alarmSet 광고 열림 성공")
            }
        }

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

        // *** 위에서 가져온 데이터를 각 뷰에 넣어서 적용
        if (setHour > 12){
            setHour -= 12
            setAMPM = 1
        }

        // 시간 및 오전오후 표시
        binder.numberPickerHour.value = setHour
        binder.numberPickerMin.value = setMin
        binder.numberPickerAMPM.value = setAMPM

        // 사운드 이미지 갱신 - setProgress가 0일 경우에만 실시한다
        if (setProgress == 0){
            binder.imageVolume.setImageResource(R.drawable.volume_mute)
        }

        // 아래 항목은 잘 들어오는거 확인됨
        Log.d("AlarmSetActivityModi", "before_setHour: $setHour")
        Log.d("AlarmSetActivityModi", "before_setMin: $setMin")
        Log.d("AlarmSetActivityModi", "before_setAMPM: $setAMPM")
        Log.d("AlarmSetActivityModi", "before_position: $position")

        Log.d("AlarmSetActivityModi", "before_weekList: $weekList")

        // 요일 클릭에 대한 변수 정의
        var Sun = weekList[0]
        var Mon = weekList[1]
        var Tue = weekList[2]
        var Wed = weekList[3]
        var Thu = weekList[4]
        var Fri = weekList[5]
        var Sat = weekList[6]

        // 위 결과에 따라 각 요일에 색 칠하기
        textWeek_initial(binder.alarmSetSun, Sun)
        textWeek_initial(binder.alarmSetMon, Mon)
        textWeek_initial(binder.alarmSetTues, Tue)
        textWeek_initial(binder.alarmSetWed, Wed)
        textWeek_initial(binder.alarmSetThur, Thu)
        textWeek_initial(binder.alarmSetFri, Fri)
        textWeek_initial(binder.alarmSetSat, Sat)

        // *** seekBar도 초기값 설정해주기
        binder.volumeSeekBar.progress = setProgress

        seekValue = setProgress

        // numberPickerHour 터치시
        binder.numberPickerHour.setOnValueChangedListener { picker, oldVal, newVal ->
            // 시간 계산하여 메시지 표시
            informNextAlarm(newVal)
        }

        // numberPickerMin 터치시
        binder.numberPickerMin.setOnValueChangedListener { picker, oldVal, newVal ->
            // 시간 계산하여 메시지 표시
            informNextAlarm(binder.numberPickerHour.value)
        }

        // numberPickerAMPM 터치시
        binder.numberPickerAMPM.setOnValueChangedListener { picker, oldVal, newVal ->
            // 시간 계산하여 메시지 표시
            informNextAlarm(binder.numberPickerHour.value)
        }

        // Cancel 버튼 클릭 시
        binder.buttonCancel.setOnClickListener {
            finish()
        }

        // *** save 버튼 클릭 시
        binder.buttonSave.setOnClickListener {
            if (Sun == 1 || Mon == 1 || Tue == 1 || Wed == 1 || Thu == 1 || Fri == 1 || Sat== 1){
                // ** 시간, 분에 대한 설정
                // binder.numberPickerAMPM.value == 0은 AM을 가리킨다
                if (binder.numberPickerAMPM.value == 0){
                    setHour = binder.numberPickerHour.value
                }else{
                    setHour = binder.numberPickerHour.value + 12
                    // 24시는 0시로 설정되게 한다
                    if (setHour == 24){
                        setHour = 0
                    }
                }

                setMin = binder.numberPickerMin.value

                // SQL에서 해당 row의 데이터를 변경한다
                var sql_update = "update MaidAlarm set hourData = ? where  idx = ?"
                // ** position은 리스트의 인덱스이기 때문에 sql의 인덱스와 맞추기 위해서는 +1을 해줘야한다
                // ** update의 경우 컬럼 1개당 1개씩 수정을 해야한다...
                // 시간 데이터 수정
                var arg1 = arrayOf("$setHour", "${position + 1}")
                SQLHelper.writableDatabase.execSQL(sql_update, arg1)

                // 분 데이터 수정
                sql_update = "update MaidAlarm set minData = ? where  idx = ?"
                arg1 = arrayOf("$setMin", "${position + 1}")
                SQLHelper.writableDatabase.execSQL(sql_update, arg1)

                // 볼륨(progress) 데이터 수정
                sql_update = "update MaidAlarm set progressData = ? where  idx = ?"
                arg1 = arrayOf("$seekValue", "${position + 1}")
                SQLHelper.writableDatabase.execSQL(sql_update, arg1)

                // * 각 요일별로도 새로운 데이터를 다 넣어야한다
                val argWeek = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                var i = 0
                for (week in argWeek){
                    sql_update = "update MaidAlarm set $week = ? where  idx = ?"
                    arg1 = arrayOf("${weekList[i]}", "${position + 1}")
                    SQLHelper.writableDatabase.execSQL(sql_update, arg1)
                    i += 1
                }

                // requestCode 부분 수정
                sql_update = "update MaidAlarm set requestCode = ? where  idx = ?"
                arg1 = arrayOf("$requestCode", "${position + 1}")
                SQLHelper.writableDatabase.execSQL(sql_update, arg1)

                // quick 부분 수정 - 수정할 시 반드시 0이 들어가게 된다 = 변경하면 무조건 normal 알람으로 변경됨
                setQuick = 0
                sql_update = "update MaidAlarm set quick = ? where  idx = ?"
                arg1 = arrayOf("$setQuick", "${position + 1}")
                SQLHelper.writableDatabase.execSQL(sql_update, arg1)

                Log.d("AlarmSetActivityModi", "after_setHour: $setHour")
                Log.d("AlarmSetActivityModi", "after_setMin: $setMin")
                Log.d("AlarmSetActivityModi", "after_setAMPM: $setAMPM")
                Log.d("AlarmSetActivityModi", "after_requestCode: $requestCode")
                Log.d("AlarmSetActivityModi", "after_position: $position")
                Log.d("AlarmSetActivityModi", "after_quick: $setQuick")
                Log.d("AlarmSetActivityModi", "after_weekList: $weekList")

                SQLHelper.close()

                // 해당 requestCode로 Notification을 갱신해준다
                val newAlarm = makeAlarm(this, setHour, setMin, binder.volumeSeekBar.progress, weekList, requestCode)
                newAlarm.addNewAlarm_normal()

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
            restOfMin = diffTime.diffMin(binder.numberPickerMin.value)
            if (Sun == 0){
                Sun = textWeek(binder.alarmSetSun, Sun)
                weekList[0] = 1
                informNextAlarm(binder.numberPickerHour.value)
            }else{
                Sun = textWeek(binder.alarmSetSun, Sun)
                weekList[0] = 0
                informNextAlarm(binder.numberPickerHour.value)
            }
        }

        // 월요 텍스트 클릭
        binder.alarmSetMon.setOnClickListener {
            restOfMin = diffTime.diffMin(binder.numberPickerMin.value)
            if (Mon == 0){
                Mon = textWeek(binder.alarmSetMon, Mon)
                weekList[1] = 1
                informNextAlarm(binder.numberPickerHour.value)
            }else{
                Mon = textWeek(binder.alarmSetMon, Mon)
                weekList[1] = 0
                informNextAlarm(binder.numberPickerHour.value)
            }
        }

        // 화요 텍스트 클릭
        binder.alarmSetTues.setOnClickListener {
            restOfMin = diffTime.diffMin(binder.numberPickerMin.value)
            if (Tue == 0){
                Tue = textWeek(binder.alarmSetTues, Tue)
                weekList[2] = 1
                informNextAlarm(binder.numberPickerHour.value)
            }else{
                Tue = textWeek(binder.alarmSetTues, Tue)
                weekList[2] = 0
                informNextAlarm(binder.numberPickerHour.value)
            }

        }

        // 수요 텍스트 클릭
        binder.alarmSetWed.setOnClickListener {
            restOfMin = diffTime.diffMin(binder.numberPickerMin.value)
            if (Wed == 0){
                Wed = textWeek(binder.alarmSetWed, Wed)
                weekList[3] = 1
                informNextAlarm(binder.numberPickerHour.value)
            }else{
                Wed = textWeek(binder.alarmSetWed, Wed)
                weekList[3] = 0
                informNextAlarm(binder.numberPickerHour.value)
            }
        }

        // 목요 텍스트 클릭
        binder.alarmSetThur.setOnClickListener {
            restOfMin = diffTime.diffMin(binder.numberPickerMin.value)
            if (Thu == 0){
                Thu = textWeek(binder.alarmSetThur, Thu)
                weekList[4] = 1
                informNextAlarm(binder.numberPickerHour.value)
            }else{
                Thu = textWeek(binder.alarmSetThur, Thu)
                weekList[4] = 0
                informNextAlarm(binder.numberPickerHour.value)
            }
        }

        // 금요 텍스트 클릭
        binder.alarmSetFri.setOnClickListener {
            restOfMin = diffTime.diffMin(binder.numberPickerMin.value)
            if (Fri == 0){
                Fri = textWeek(binder.alarmSetFri, Fri)
                weekList[5] = 1
                informNextAlarm(binder.numberPickerHour.value)
            }else{
                Fri = textWeek(binder.alarmSetFri, Fri)
                weekList[5] = 0
                informNextAlarm(binder.numberPickerHour.value)
            }
        }

        // 토요 텍스트 클릭
        binder.alarmSetSat.setOnClickListener {
            restOfMin = diffTime.diffMin(binder.numberPickerMin.value)
            if (Sat == 0){
                Sat = textWeek(binder.alarmSetSat, Sat)
                weekList[6] = 1
                informNextAlarm(binder.numberPickerHour.value)
            }else{
                Sat = textWeek(binder.alarmSetSat, Sat)
                weekList[6] = 0
                informNextAlarm(binder.numberPickerHour.value)
            }
        }

        // * 불륨 이미지를 클릭했을 때
        binder.imageVolume.setOnClickListener {
            if (binder.volumeSeekBar.progress > 0){
                binder.volumeSeekBar.progress = 0
                binder.imageVolume.setImageResource(R.drawable.volume_mute)
            }else{
                binder.volumeSeekBar.progress = 100
                binder.imageVolume.setImageResource(R.drawable.volume_icon)
            }
        }

        // * seekBar의 Progress 값을 가져온다
        binder.volumeSeekBar.setOnSeekBarChangeListener(seekListener)

        setContentView(binder.root)
    }

    // 텍스트뷰에 색깔 넣기 - 클릭 시
    fun textWeek(textView : TextView, week : Int) : Int {
        if (week == 0){
            textView.setBackgroundColor(Color.parseColor("#1ABC9C"))
            return 1
        }
        else{
            textView.setBackgroundColor(Color.parseColor("#FFFFFF"))
            return 0
        }
    }

    // 텍스트뷰에 색깔 넣기 - 최초 기동 시
    fun textWeek_initial(textView : TextView, week : Int) : Int {
        if (week == 1){
            textView.setBackgroundColor(Color.parseColor("#1ABC9C"))
            return 0
        }
        else{
            textView.setBackgroundColor(Color.parseColor("#FFFFFF"))
            return 1
        }
    }

    // seekBar에 대한 리스너 정의
    val seekListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            when (seekBar?.id) {
                R.id.volumeSeekBar -> {
                    seekValue = progress
                    if (progress == 0){
                        binder.imageVolume.setImageResource(R.drawable.volume_mute)
                    }else{
                        binder.imageVolume.setImageResource(R.drawable.volume_icon)
                    }
                }
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {

        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {

        }
    }

    // NumberPickerHour/Min, 각 요일의 텍스트를 클릭할 때 마다 알람까지 남은 시간 표기하는 메서드
    fun informNextAlarm(localHour : Int){
        restOfMin = diffTime.diffMin(binder.numberPickerMin.value)
        if (binder.numberPickerAMPM.value == 0){
            restOfHour = diffTime.diffHour(localHour, binder.numberPickerMin.value)
            restOfWeek = diffTime.diffWeek(weekList, localHour, binder.numberPickerMin.value)
            binder.alarmSetInform.text = diffTime.makeTextWithDiffTime(this, restOfWeek, restOfHour, restOfMin)
        }else{
            var localHour2 = localHour + 12
            // 24시는 0시로 설정되게 한다
            if (localHour2 == 24){
                localHour2 = 0
            }
            restOfHour = diffTime.diffHour(localHour2, binder.numberPickerMin.value)
            restOfWeek = diffTime.diffWeek(weekList, localHour2, binder.numberPickerMin.value)
            binder.alarmSetInform.text = diffTime.makeTextWithDiffTime(this, restOfWeek, restOfHour, restOfMin)
        }
    }
}