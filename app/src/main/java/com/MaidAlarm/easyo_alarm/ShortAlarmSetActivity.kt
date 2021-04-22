package com.MaidAlarm.easyo_alarm

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.MaidAlarm.easyo_alarm.databinding.ActivityShortAlarmSetBinding
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import java.util.*

class ShortAlarmSetActivity : AppCompatActivity() {
    // 모든 숫자(시간) 초기화하기
    var hour = 0
    var min = 0

    lateinit var binder: ActivityShortAlarmSetBinding

    var seekValue = 100

    // 알람음 세팅 Activity에서 돌아오는 ResultCode
    val selectRingActivityBack = 1

    lateinit var app : AppClass

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_short_alarm_set)
        binder = ActivityShortAlarmSetBinding.inflate(layoutInflater)

        app = application as AppClass

        // 새로운 알람 설정의 경우 초기값이 들어가도록 한다
        app.bellIndex = 0
        app.wayOfAlarm = 0
        binder.textCurrentBell.text = getString(R.string.typeOfBell_Normal_Bar)
        binder.textCurrentMode.text = getString(R.string.alarmSet_selectModeNormal)

        // 2. 애드몹 로드
        val adRequest = AdRequest.Builder().build()
        binder.adView.loadAd(adRequest)

        binder.adView.adListener = object : AdListener(){
            override fun onAdFailedToLoad(p0: LoadAdError) {
                super.onAdFailedToLoad(p0)
                Log.d("ShortAlarmSetActivity", "quickSet 광고 로드 실패")
            }

            override fun onAdLoaded() {
                super.onAdOpened()
                Log.d("adMob", "quickSet 광고 열림 성공")
            }
        }

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

        // SelectBell 클릭 시
        binder.buttonBell.setOnClickListener {
            val intent = Intent(this, SelectRingActivity::class.java)
            this.startActivityForResult(intent, selectRingActivityBack)
        }

        // SelectMode 클릭 시
        binder.buttonMode.setOnClickListener {
            Log.d("SettingRecyclerAdapter", "wayOfAlarm: ${app.wayOfAlarm}")
            Log.d("SettingRecyclerAdapter", "counter: ${app.counter}")
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
                        Log.d("shortAlarmSetActivity", "wayOfAlarm: ${app.wayOfAlarm}")
                        binder.textCurrentMode.text = getString(R.string.alarmSet_selectModeNormal)
                    }
                    // Calculate 클릭 시
                    1 -> {
                        app.wayOfAlarm = 1  // Calculator 사용 on
                        Log.d("shortAlarmSetActivity", "wayOfAlarm: ${app.wayOfAlarm}")
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
                            Log.d("shortAlarmSetActivity", "mode: ${app.wayOfAlarm}")
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


        // 저장 버튼 클릭 시
        binder.buttonSave.setOnClickListener {
            val result_intent = Intent()

            result_intent.putExtra("hour", hour)
            result_intent.putExtra("min", min)
            result_intent.putExtra("progress", seekValue)
            result_intent.putExtra("bellIndex", app.bellIndex)
            result_intent.putExtra("alarmMode", app.wayOfAlarm)

            // ** Toast로 xx일 xx분 뒤 알람 울립니다 만들어주기
            // - 현재 날짜랑 연동해서 계산할 필요가 있음
            val calendar = Calendar.getInstance()
            val presentHour = calendar.get(Calendar.HOUR_OF_DAY)
            val presentMin = calendar.get(Calendar.MINUTE)
            var toast_min = presentMin + min

            val toast_day : Int = (presentHour + hour) / 24
            val taost_hour : Int = (presentHour + hour) % 24

            // 1일을 초과하지 않을 때 (xx 시간 xx분 뒤 알람울림)
            if (toast_day <= 0){
             val text = getString(R.string.alarmToast_inform1) + " "+ hour.toString() +
                     " "+ getString(R.string.alarmToast_inform3) + " "+ min.toString() + " "+ getString(R.string.alarmToast_inform4)
             val toast = Toast.makeText(this, text, Toast.LENGTH_LONG)
             toast.show()
            }

            // 1일을 초과할 때 (xx일 xx시간 xx분 뒤 알람울림)
            else{
                val text = getString(R.string.alarmToast_inform1) + " "+ toast_day + " "+ getString(R.string.alarmToast_inform2) +
                " "+ taost_hour.toString() + " "+ getString(R.string.alarmToast_inform3) + " "+ toast_min.toString() + " "+ getString(R.string.alarmToast_inform4)
                val toast = Toast.makeText(this, text, Toast.LENGTH_LONG)
                toast.show()
            }
            // ** -> alarmFragment로 이동한다
            setResult(100, result_intent)
            finish()
        }

        // seekingBar 리스너
        // seekBar의 Progress 값을 가져온다
        binder.volumeSeekBar.setOnSeekBarChangeListener(seekListener)

        // * 볼륨 이미지 클릭 시
        binder.imageView2.setOnClickListener {
            if (binder.volumeSeekBar.progress > 0){
                binder.volumeSeekBar.progress = 0
                binder.imageView2.setImageResource(R.drawable.volume_mute)
            }else{
                binder.volumeSeekBar.progress = 100
                binder.imageView2.setImageResource(R.drawable.volume_icon)
            }
        }

        setContentView(binder.root)
    }

    // +1분 등 버튼 클릭 시 분 변수에 해당 버튼에 해당하는 값을 더해줌
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

    // SeekBar 리스너
    val seekListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            when (seekBar?.id) {
                R.id.volumeSeekBar -> {
                    seekValue = progress
                    if (progress == 0){
                        binder.imageView2.setImageResource(R.drawable.volume_mute)
                    }else{
                        binder.imageView2.setImageResource(R.drawable.volume_icon)
                    }
                }
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
        }
    }
}