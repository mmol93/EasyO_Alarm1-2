package com.example.easyo_alarm

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.easyo_alarm.databinding.InfoRowBinding

class InfoAdapter(val context : Context) : RecyclerView.Adapter<InfoViewHolder>()  {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InfoViewHolder {
        return InfoViewHolder(LayoutInflater.from(context).inflate(R.layout.info_row, parent, false))
    }

    override fun getItemCount(): Int {
        return 3
    }

    override fun onBindViewHolder(holder: InfoViewHolder, position: Int) {
        when(position){
            // 0 : 후원탭
            0 ->{
                holder.binder.infoImage.setImageResource(R.drawable.info_support)
                holder.binder.infoText.context.getString(R.string.infoItem_support)
            }
            // 1 : 문의탭
            1 -> {
                holder.binder.infoImage.setImageResource(R.drawable.info_emal)
                holder.binder.infoText.context.getString(R.string.infoItem_contact)
            }
            // 2 : 오픈소스탭
            2 ->{
                holder.binder.infoImage.setImageResource(R.drawable.info_open)
                holder.binder.infoText.context.getString(R.string.infoItem_version)
            }
        }
    }
}

class InfoViewHolder(view : View) : RecyclerView.ViewHolder(view){
    val binder : InfoRowBinding = InfoRowBinding.bind(view)

    val row_mainText = binder.infoText
    val row_SubText = binder.infoSubText
    val row_image = binder.infoImage
    val row_view = binder.rowItemView
}