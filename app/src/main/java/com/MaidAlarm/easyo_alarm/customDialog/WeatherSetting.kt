package com.MaidAlarm.easyo_alarm.customDialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import com.MaidAlarm.easyo_alarm.R
import com.MaidAlarm.easyo_alarm.databinding.DialogWeatherSettingBinding

class WeatherSetting(context : Context) : Dialog(context){
    lateinit var binder : DialogWeatherSettingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binder = DialogWeatherSettingBinding.inflate(layoutInflater)
        setContentView(binder.root)
        // 배경 투명하게 만들기
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

    }
}