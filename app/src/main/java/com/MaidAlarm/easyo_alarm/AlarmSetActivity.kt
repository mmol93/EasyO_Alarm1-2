package com.MaidAlarm.easyo_alarm

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.MaidAlarm.easyo_alarm.databinding.ActivityAlarmSetBinding
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import java.util.ArrayList

class AlarmSetActivity : AppCompatActivity() {
    lateinit var binder: ActivityAlarmSetBinding
    var seekValue = 100

    // 모든 숫자(시간) 초기화하기
    var hour = 0
    var min = 0

    // 예약한 요일을 담는 List
    val weekList = ArrayList<Int>()

    // diffTime에 대한 객체 생성
    val diffTime = com.MaidAlarm.easyo_alarm.diffTime()
    var restOfWeek : Int = 0
    var restOfHour : Int = 1
    var restOfMin : Int = 0

    // 알람음 세팅 Activity에서 돌아오는 ResultCode
    val selectRingActivityBack = 1

    lateinit var app : AppClass

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        // weekList 초기화
        for (i in 0..6){
            weekList.add(0)
        }
        Log.d("AlarmSetActivity", "weekList: $weekList")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm_set)
        app = application as AppClass

        binder = ActivityAlarmSetBinding.inflate(layoutInflater)

        // 새로운 알람 설정의 경우 초기값이 들어가도록 한다
        app.bellIndex = 0
        app.wayOfAlarm = 0
        binder.textCurrentBell.text = getString(R.string.typeOfBell_Normal_Bar)
        binder.textCurrentMode.text = getString(R.string.alarmSet_selectModeNormal)

        // 요일 클릭에 대한 변수 정의
        var Sun = 0
        var Mon = 0
        var Tue = 0
        var Wed = 0
        var Thu = 0
        var Fri = 0
        var Sat = 0

        // 애드몹 로드
        val adRequest = AdRequest.Builder().build()
        binder.adView.loadAd(adRequest)

        binder.adView.adListener = object : AdListener(){
            override fun onAdFailedToLoad(p0: LoadAdError) {
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

        // SelectBell 클릭 시
        binder.buttonBell.setOnClickListener {
            val intent = Intent(this, SelectRingActivity::class.java)
            this.startActivityForResult(intent, selectRingActivityBack)
        }

        // SelectMode 클릭 시
        binder.buttonMode.setOnClickListener {
            Log.d("alarmSetActivity", "wayOfAlarm: ${app.wayOfAlarm}")
            Log.d("alarmSetActivity", "counter: ${app.counter}")
            // ** 항목 선택 Dialog 설정
            val modeItem = arrayOf(getString(R.string.settingItem_alarmModeItem1), getString(R.string.settingItem_alarmModeItem2))
            val builder = AlertDialog.Builder(this)
            builder.setTitle(getString(R.string.settingItem_alarmMode))
            builder.setSingleChoiceItems(modeItem, app.wayOfAlarm , null)
            builder.setNeutralButton(getString(R.string.cancelBtn), null)

            // * 아이템 선택했을 때 리스너 설정(람다식)
            builder.setPositiveButton(getString(R.string.front_ok)){ dialogInterface: DialogInterface, i: Int ->
                val alert = dialogInterface as AlertDialog
                val idx = alert.listView.checkedItemPosition
                // * 선택된 아이템의 position에 따라 행동 조건 넣기
                when(idx){
                    // Normal 클릭 시
                    0 -> {
                        app.wayOfAlarm = 0  // Calculator 사용 off
                        Log.d("SettingRecyclerAdapter", "wayOfAlarm: ${app.wayOfAlarm}")
                        binder.textCurrentMode.text = getString(R.string.alarmSet_selectModeNormal)
                    }
                    // Calculate 클릭 시
                    1 -> {
                        app.wayOfAlarm = 1  // Calculator 사용 on
                        Log.d("alarmSetActivity", "wayOfAlarm: ${app.wayOfAlarm}")
                        // * 반복 횟수 설정하기 => AlertDialog 띄우기
                        val counter = arrayOf("1", "2", "3", "4", "5")
                        val builder = AlertDialog.Builder(this)
                        builder.setTitle(getString(R.string.settingItem_calRepeat))
                        builder.setSingleChoiceItems(counter, app.counter - 1  , null)
                        builder.setNeutralButton(getString(R.string.cancelBtn), null)
                        // 최종적으로 ok버튼까지 눌렀을 때
                        builder.setPositiveButton(getString(R.string.front_ok)){ dialogInterface: DialogInterface, i: Int ->
                            val alert = dialogInterface as AlertDialog
                            val idx = alert.listView.checkedItemPosition
                            Log.d("alarmSetActivity", "mode: ${app.wayOfAlarm}")
                            binder.textCurrentMode.text = getString(R.string.alarmSet_selectModeCAL) + " ${app.wayOfAlarm}"
                            when(idx){
                                0 -> app.wayOfAlarm = 1
                                1 -> app.wayOfAlarm = 2
                                2 -> app.wayOfAlarm = 3
                                3 -> app.wayOfAlarm = 4
                                4 -> app.wayOfAlarm = 5
                            }
                        }
                        builder.show()
                    }
                }
            }
            builder.show()
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
                result_intent.putExtra("bellIndex", app.bellIndex)
                result_intent.putExtra("alarmMode", app.wayOfAlarm)


                Log.d("AlarmSetActivity", "progress: $seekValue")

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // 알람음 선택에서 돌아왔을 때
            if (requestCode == selectRingActivityBack){
                when(app.bellIndex){
                    0 -> binder.textCurrentBell.text = getString(R.string.typeOfBell_Normal_Bar)
                    1 -> binder.textCurrentBell.text = getString(R.string.typeOfBell_Normal_Guitar)
                    2 -> binder.textCurrentBell.text = getString(R.string.typeOfBell_Normal_Happy)
                    3 -> binder.textCurrentBell.text = getString(R.string.typeOfBell_Normal_Country)
                    // 한국어 알람음
                    10 -> binder.textCurrentBell.text = getString(R.string.typeOfBell_Korean_Jeongyeon)
                    11 -> binder.textCurrentBell.text = getString(R.string.typeOfBell_Korean_MinJjeong)
                    // 값이 null일 때(아마...)
                    else -> binder.textCurrentBell.text = getString(R.string.typeOfBell_Normal_Bar)
                }
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

    // 텍스트뷰에 색깔 넣기
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

    // NumberPickerHour/Min, 각 요일의 텍스트를 클릭할 때 마다 알람까지 남은 시간 표기하는 메서드
    fun informNextAlarm(localHour : Int){
        Log.d("AlarmSetActivity", "numberPickerAMPM: ${binder.numberPickerAMPM.value}")
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
        Log.d("AlarmSetActivity", "restOfWeek: $restOfWeek")
        Log.d("AlarmSetActivity", "restOfHour: $restOfHour")
    }

}