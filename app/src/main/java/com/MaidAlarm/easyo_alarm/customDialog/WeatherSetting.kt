package com.MaidAlarm.easyo_alarm.customDialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
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

        binder.morningWeatherSwitch.setOnCheckedChangeListener { buttonView, isChecked ->

        }

    }
}