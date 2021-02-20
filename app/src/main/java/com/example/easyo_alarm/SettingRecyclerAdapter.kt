package com.example.easyo_alarm

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.easyo_alarm.databinding.SettingRowBinding

class SettingRecyclerAdapter(val context : Context) : RecyclerView.Adapter<SettingViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingViewHolder {
        return SettingViewHolder(LayoutInflater.from(context).inflate(R.layout.setting_row, parent, false))
    }

    override fun getItemCount(): Int {
        return 4
    }

    override fun onBindViewHolder(holder: SettingViewHolder, position: Int) {
        // *** RecyclerView의 각 item 설정하기
        // ** item 중 텍스트뷰 설정하기
        val item1 = context.getString(R.string.settingItem_selectAlarm)
        val item2 = context.getString(R.string.settingItem_alarmMode)
        val item3 = context.getString(R.string.settingItem_notification)
        val item4 = context.getString(R.string.settingItem_info)
        val item = arrayOf(item1, item2, item3, item4)
        holder.row_mainText.text = item[position]

        // ** item 중 이미지뷰 설정하기
        when(position){
            0 -> {holder.row_image.setImageResource(R.drawable.setting_select_alarm)}
            1 -> {holder.row_image.setImageResource(R.drawable.calculator)}
            2 -> {holder.row_image.setImageResource(R.drawable.setting_notification)}
            3 -> {holder.row_image.setImageResource(R.drawable.setting_info)}
        }
    }
}

class SettingViewHolder(view : View) : RecyclerView.ViewHolder(view){
    val binder : SettingRowBinding = SettingRowBinding.bind(view)

    val row_mainText = binder.settingText
    val row_image = binder.settingImage
}