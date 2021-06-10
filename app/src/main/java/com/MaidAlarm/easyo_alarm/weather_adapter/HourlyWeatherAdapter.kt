package com.MaidAlarm.easyo_alarm.weather_adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.MaidAlarm.easyo_alarm.AppClass
import com.MaidAlarm.easyo_alarm.R
import com.MaidAlarm.easyo_alarm.Weather
import com.MaidAlarm.easyo_alarm.databinding.HourlyRowBinding

class HourlyWeatherAdapter(val context : Context,
                           val hourlyTemp : ArrayList<Long>, val hourlyPop : ArrayList<Double>, val hourlyMain : ArrayList<String>)
    : RecyclerView.Adapter<HourlyViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HourlyViewHolder {
        return HourlyViewHolder(LayoutInflater.from(context).inflate(R.layout.hourly_row, parent, false))
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holderHourly: HourlyViewHolder, position: Int) {
        holderHourly.row_tempText.text = "${hourlyTemp[position]}℃"
        holderHourly.row_rainText.text = "${hourlyPop[position].toInt()}%"
        holderHourly.row_timeText.text = "${AppClass.hour[position]}:00"
        // main에 있는 날씨 상태에 따른 다른 이미지 넣어주기
        when(hourlyMain[position]){
            "Thunderstorm" -> holderHourly.row_weatherImage.setImageResource(R.drawable.ic_thunder)
            "Drizzle" -> holderHourly.row_weatherImage.setImageResource(R.drawable.ic_little_rain)
            "Rain" -> holderHourly.row_weatherImage.setImageResource(R.drawable.ic_rain)
            "Snow" -> holderHourly.row_weatherImage.setImageResource(R.drawable.ic_snow)
            "Clear" -> {
                holderHourly.row_weatherImage.setImageResource(R.drawable.ic_sunny)
                // 리스트에 있는 시간이 해가 진 시간보다 더 늦은 시간일 때
                if (AppClass.hour[position].toInt() > Weather.sunSet.toInt()){
                    holderHourly.row_weatherImage.setImageResource(R.drawable.ic_moon)
                    Log.d("test", "hour: ${AppClass.hour[position]}")
                    Log.d("test", "sunSet: ${Weather.sunSet}")
                }else if (Weather.sunRise.toInt() > AppClass.hour[position].toInt()){
                    holderHourly.row_weatherImage.setImageResource(R.drawable.ic_moon)
                    Log.d("test", "hour: ${AppClass.hour[position]}")
                    Log.d("test", "sunRise: ${Weather.sunRise}")
                }
            }
            "Clouds" -> holderHourly.row_weatherImage.setImageResource(R.drawable.ic_clouds)
            "Mist", "Dust", "Fog", "Haze", "Sand", "Ash" -> holderHourly.row_weatherImage.setImageResource(R.drawable.ic_fog)
            "Tornado", "Squall" -> holderHourly.row_weatherImage.setImageResource(R.drawable.ic_tornado)
        }
    }

    override fun getItemCount(): Int {
        return hourlyTemp.size
    }
}

class HourlyViewHolder(view : View) : RecyclerView.ViewHolder(view){
    val binder : HourlyRowBinding = HourlyRowBinding.bind(view)

    val row_tempText = binder.tempTextView
    val row_weatherImage = binder.weatherImageView
    val row_rainText = binder.rainPercentTextView
    val row_timeText = binder.timeTextView
}