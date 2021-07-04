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
import com.MaidAlarm.easyo_alarm.weatherFunction.WeatherAlarm
import java.lang.Exception

class WeatherSetting(context : Context) : Dialog(context){
    lateinit var binder : DialogWeatherSettingBinding
    // 인덱스: 0 ~ 23
    val spinnerArray = arrayOf("01:00", "02:00", "03:00", "04:00", "05:00", "06:00", "07:00", "08:00", "09:00", "10:00", "11:00",
        "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00", "21:00", "22:00", "23:00", "")
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
//        binder.weatherFrontAlarmSwitch.isChecked = weatherAlarmData.getBoolean("weatherFrontSwitch", false) - 미사용

        // 데이터에 대해 스피너(콤보박스)의 값 설정해주기
        val spinnerData = weatherAlarmData.getString("weatherAlarmTime", "01:00")
        val targetSpinnerIndex = findDataInArray(spinnerData!!)
        if (targetSpinnerIndex == -1){
            Toast.makeText(context, context.getString(R.string.error_restart_toast), Toast.LENGTH_SHORT).show()
            return
        }
        // 이전에 스피너에 값을 지정했다면 그 지정한 값으로 스피너 맞춰주기
        binder.weatherAlarmSpinner.setSelection(targetSpinnerIndex)

        // 스위치 on/off에 대한 isGone 설정
        if (binder.weatherAlarmSwitch.isChecked){
//            binder.weatherAlarmFrontDisplayLayout.isGone = false - 미사용
            binder.weatherAlarmTimeSetLayout.isGone = false
        }else{
//            binder.weatherAlarmFrontDisplayLayout.isGone = true - 미사용
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
        // 내일 날씨 오버레이 알람에 대한 ? 이미지뷰를 클릭했을 때 - 미사용
//        binder.weatherFrontAlarmImageView.setOnClickListener {
//            Toast.makeText(AppClass.context, AppClass.context.getString(R.string.tomorror_weather_front_toast), Toast.LENGTH_LONG).show()
//        }

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
            val weatherAlarmData = context.getSharedPreferences("weatherAlarmData", Context.MODE_PRIVATE)

            // 브로드캐스트 만들기
            val weatherAlarm = WeatherAlarm(context)

            // On 상태
            if (isChecked){
                prefEdit.putBoolean("weatherSwitch", true)
                // 오버레이 설정에 대해 isGone을 구현해준다
//                binder.weatherAlarmFrontDisplayLayout.isGone = false - 미사용
                binder.weatherAlarmTimeSetLayout.isGone = false

                // 내일 날씨 알람 설정한 시간 가져오기
                val setHourData = weatherAlarmData.getString("weatherAlarmTime", "01:00")
                Log.d("weatherAlarm - WeatherSetting.kt", "설정한 시간: $setHourData")
                if (setHourData != null) {
                    // 해당 시간에 알림 오게 브로드캐스트 설정
                    weatherAlarm.setTomorrowWeatherAlarm(setHourData)
                }
            }
            // Off 상태
            else{
                prefEdit.putBoolean("weatherSwitch", false)
//                binder.weatherAlarmFrontDisplayLayout.isGone = true - 미사용
                binder.weatherAlarmTimeSetLayout.isGone = true
                // 브로드캐스트 끄기
                try{
                    weatherAlarm.cancelTomorrowWeatherAlarm()
                }catch (e:Exception){

                }

            }
            prefEdit.apply()
        }
        // 날씨 알림 오버레이 스위치 리스너 - 미사용
//        binder.weatherFrontAlarmSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
//            val prefEdit = weatherAlarmData.edit()
//            // On
//            if (isChecked){
//                prefEdit.putBoolean("weatherFrontSwitch", true)
//            }
//            // Off
//            else{
//                prefEdit.putBoolean("weatherFrontSwitch", false)
//            }
//            prefEdit.apply()
//        }
        // 스피너(콤보박스)에 대한 아이템 선택 리스너 설정
        binder.weatherAlarmSpinner.onItemSelectedListener = spinnerListener

    }
    // 스피너(콤보박스)에 대한 아이템 선택 리스너 정의
    private val spinnerListener = object : AdapterView.OnItemSelectedListener{
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            val weatherAlarmData = context.getSharedPreferences("weatherAlarmData", Context.MODE_PRIVATE)
            val prefEdit = weatherAlarmData.edit()
            // 23:00 같은 텍스트가 들어간다
            var settledData = spinnerArray[position]
            if (settledData == ""){
                settledData = "23:00"
                // 스피너도 "23:00"을 가리키도록 한다
                binder.weatherAlarmSpinner.setSelection(22)
            }

            // 해당 시간에 알림 오게 브로드캐스트 설정
            val weatherAlarm = WeatherAlarm(context)
            weatherAlarm.setTomorrowWeatherAlarm(settledData)

            prefEdit.putString("weatherAlarmTime", settledData)
            prefEdit.apply()
            Log.d("WeatherAlarm - WeatherSetting", "스피너 선택됨: $settledData")
        }
        override fun onNothingSelected(parent: AdapterView<*>?) {

        }
    }

    private fun findDataInArray(data : String) : Int{
        var i = 0
        for (target in spinnerArray){
            if (data == target){
                return i
            }else{
                i++
            }
        }
        return -1
    }
}