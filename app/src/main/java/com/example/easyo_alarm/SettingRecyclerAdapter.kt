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
        return 3
    }

    override fun onBindViewHolder(holder: SettingViewHolder, position: Int) {
        val item1 = context.getString(R.string.settingItem_selectAlarm)
        val item2 = context.getString(R.string.settingItem_alarmMode)
        val item3 = context.getString(R.string.settingItem_notification)
        val item4 = context.getString(R.string.settingItem_info)
        val item = arrayOf("1", "집2", "집3")
        holder.row_mainText.text = item[position]
        Log.d("settingRecyclerAdapter", "item: ${item[position]}")
    }
}

class SettingViewHolder(view : View) : RecyclerView.ViewHolder(view){
    val binder : SettingRowBinding = SettingRowBinding.bind(view)

    val row_mainText = binder.settingText
}