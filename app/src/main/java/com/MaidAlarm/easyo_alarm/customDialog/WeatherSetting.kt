package com.MaidAlarm.easyo_alarm.customDialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isGone
import com.MaidAlarm.easyo_alarm.AppClass
import com.MaidAlarm.easyo_alarm.R
import com.MaidAlarm.easyo_alarm.databinding.DialogWeatherSettingBinding

class WeatherSetting(context : Context) : Dialog(context){
    lateinit var binder : DialogWeatherSettingBinding
    val spinnerArray = arrayOf("01:00", "02:00", "03:00", "04:00", "05:00", "06:00", "07:00", "08:00", "09:00", "10:00", "11:00",
        "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00", "21:00", "22:00", "23:00")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binder = DialogWeatherSettingBinding.inflate(layoutInflater)
        val spinnerAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, spinnerArray)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1)
        binder.weatherAlarmSpinner.adapter = spinnerAdapter

        setContentView(binder.root)

        // 데이터 가져오기
        val morningWeatherData = context.getSharedPreferences("morningWeatherData", Context.MODE_PRIVATE)
        val weatherAlarmData = context.getSharedPreferences("weatherAlarmData", Context.MODE_PRIVATE)

        // 데이터에 대한 스위치 on/off 설정
        binder.morningWeatherSwitch.isChecked = morningWeatherData.getBoolean("morningSwitch", true)
        binder.weatherAlarmSwitch.isChecked = weatherAlarmData.getBoolean("weatherSwitch", false)
        binder.weatherFrontAlarmSwitch.isChecked = weatherAlarmData.getBoolean("weatherFrontSwitch", false)

        // 스위치 on/off에 대한 isGone 설정
        if (binder.weatherAlarmSwitch.isChecked){
            binder.weatherAlarmFrontDisplayLayout.isGone = false
            binder.weatherAlarmTimeSetLayout.isGone = false
        }else{
            binder.weatherAlarmFrontDisplayLayout.isGone = true
            binder.weatherAlarmTimeSetLayout.isGone = true
        }

        // 배경 투명하게 만들기
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // 아침 날씨 확인에 대한 ? 이미지 뷰를 클릭했을 때
        binder.morningWeatherImageView.setOnClickListener {
            Toast.makeText(AppClass.context, AppClass.context.getString(R.string.morning_weather_toast), Toast.LENGTH_LONG).show()
        }
        // 내일 날씨 알람에 대한 ? 이미지뷰를 클릭했을 때
        binder.weatherAlarmImageView.setOnClickListener {
            Toast.makeText(AppClass.context, AppClass.context.getString(R.string.tomorror_weather_toast), Toast.LENGTH_LONG).show()
        }
        // 내일 날씨 오버레이 알람에 대한 ? 이미지뷰를 클릭했을 때
        binder.weatherFrontAlarmImageView.setOnClickListener {
            Toast.makeText(AppClass.context, AppClass.context.getString(R.string.tomorror_weather_front_toast), Toast.LENGTH_LONG).show()
        }

        // 아침 날씨 스위치 리스너
        binder.morningWeatherSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            val prefEdit = morningWeatherData.edit()
            // On 상태
            if (isChecked){
                prefEdit.putBoolean("morningSwitch", true)
            }
            // Off 상태
            else{
                prefEdit.putBoolean("morningSwitch", false)
            }
            prefEdit.apply()
        }
        // 날씨 알림 설정 스위치 리스너
        binder.weatherAlarmSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            val prefEdit = weatherAlarmData.edit()
            // On 상태
            if (isChecked){
                prefEdit.putBoolean("weatherSwitch", true)
                // 오버레이 설정에 대해 isGone을 구현해준다
                binder.weatherAlarmFrontDisplayLayout.isGone = false
                binder.weatherAlarmTimeSetLayout.isGone = false
            }
            // Off 상태
            else{
                prefEdit.putBoolean("weatherSwitch", false)
                binder.weatherAlarmFrontDisplayLayout.isGone = true
                binder.weatherAlarmTimeSetLayout.isGone = true
            }
            prefEdit.apply()
        }
        // 날씨 알림 오버레이 스위치 리스너
        binder.weatherFrontAlarmSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            val prefEdit = weatherAlarmData.edit()
            // On
            if (isChecked){
                prefEdit.putBoolean("weatherFrontSwitch", true)
            }
            // Off
            else{
                prefEdit.putBoolean("weatherFrontSwitch", false)
            }
            prefEdit.apply()
        }
        // 스피너(콤보박스)에 대한 아이템 선택 리스너 설정
        binder.weatherAlarmSpinner.onItemSelectedListener = spinnerListener

    }
    // 스피너(콤보박스)에 대한 아이템 선택 리스너 정의
    private val spinnerListener = object : AdapterView.OnItemSelectedListener{
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            val weatherAlarmData = context.getSharedPreferences("weatherAlarmData", Context.MODE_PRIVATE)
            val prefEdit = weatherAlarmData.edit()
            // 23:00 같은 텍스트가 들어간다
            prefEdit.putString("weatherAlarmTime", spinnerArray[position])
            prefEdit.apply()
            Log.d("WeatherSetting", "스피너 선택됨: ${spinnerArray[position]}")
        }
        override fun onNothingSelected(parent: AdapterView<*>?) {

        }
    }
}