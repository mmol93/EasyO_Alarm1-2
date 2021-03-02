package com.example.easyo_alarm

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.easyo_alarm.databinding.InfoRowBinding

class InfoAdapter(val context : Context) : RecyclerView.Adapter<InfoViewHolder>()  {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InfoViewHolder {
        TODO("Not yet implemented")
    }

    override fun getItemCount(): Int {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: InfoViewHolder, position: Int) {
        TODO("Not yet implemented")
    }
}

class InfoViewHolder(view : View) : RecyclerView.ViewHolder(view){
    val binder : InfoRowBinding = InfoRowBinding.bind(view)

    val row_mainText = binder.settingText
    val row_SubText = binder.settingSubText
    val row_image = binder.settingImage
    val row_view = binder.rowItemView
}