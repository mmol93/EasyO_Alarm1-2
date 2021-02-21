package com.example.easyo_alarm

import android.animation.ValueAnimator
import android.content.Context
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.animation.*
import androidx.recyclerview.widget.RecyclerView
import com.example.easyo_alarm.databinding.SettingRowBinding

class SettingRecyclerAdapter(val context : Context) : RecyclerView.Adapter<SettingViewHolder>() {
    lateinit var app : AppClass
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingViewHolder {
        return SettingViewHolder(LayoutInflater.from(context).inflate(R.layout.setting_row, parent, false))
    }

    override fun getItemCount(): Int {
        return 4
    }

    override fun onBindViewHolder(holder: SettingViewHolder, position: Int) {
        app = AppClass()
        // *** RecyclerView의 각 item 설정하기
        // ** item 중 텍스트뷰 설정하기
        val item1 = context.getString(R.string.settingItem_selectAlarm)
        val item2 = context.getString(R.string.settingItem_alarmMode)
        val item3 = context.getString(R.string.settingItem_notification)
        val item4 = context.getString(R.string.settingItem_info)
        val items = arrayOf(item1, item2, item3, item4)
        holder.row_mainText.text = items[position]

        // ** item중 Sub 텍스트뷰 설정하기
        val subItem1 = "아직 미정"
        var subItem2 = ""
        if (app.wayOfAlarm == 0){
            subItem2 = context.getString(R.string.settingItem_sub_alarmMode1)
        }else{
            subItem2 = context.getString(R.string.settingItem_sub_alarmMode2) + " " +
                    app.counter.toString() + context.getString(R.string.settingItem_sub_alarmMode2_2)
        }
        val subItem3 = context.getString(R.string.settingItem_sub_notification)
        val subItem4 = context.getString(R.string.settingItem_sub_info)
        val subItems = arrayOf(subItem1, subItem2, subItem3, subItem4)
        holder.row_SubText.text = subItems[position]


        // ** item 중 이미지뷰 설정하기
        when(position){
            0 -> {holder.row_image.setImageResource(R.drawable.setting_select_alarm)}
            1 -> {holder.row_image.setImageResource(R.drawable.calculator)}
            2 -> {holder.row_image.setImageResource(R.drawable.setting_notification)}
            3 -> {holder.row_image.setImageResource(R.drawable.setting_info)}
        }

        // 각 itemView 클릭 시
        holder.row_view.setOnClickListener {
            // 클릭된 항목은 텍스트 색 바뀌게 하기 &
            changeTextColorAndListener(holder.row_mainText, Color.BLACK, Color.rgb(0,204,204), View.LAYOUT_DIRECTION_LTR, 400, position)
        }
    }

    fun changeTextColorAndListener(textView: TextView, fromColor: Int, toColor: Int, direction: Int = View.LAYOUT_DIRECTION_LTR, duration:Long = 200, position : Int) {
        val ori_text = textView.text
        var startValue = 0
        var endValue = 0
        // 텍스트뷰의 텍스트의 왼쪽에서 오른쪽으로 색 변환
        if(direction == View.LAYOUT_DIRECTION_LTR){
            startValue = 0
            endValue = textView.text.length
        }
        // 텍스트뷰의 텍스트의 오른쪽에서 왼쪽으로 색 변환
        else if(direction == View.LAYOUT_DIRECTION_RTL) {
            startValue = textView.text.length
            endValue = 0
        }
        textView.setTextColor(fromColor)
        val valueAnimator = ValueAnimator.ofInt(startValue, endValue)
        valueAnimator.addUpdateListener { animator -> val spannableString = SpannableString(textView.text)
            if (direction == View.LAYOUT_DIRECTION_LTR) {
                spannableString.setSpan(ForegroundColorSpan(toColor), startValue, animator.animatedValue.toString().toInt(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            else if (direction == View.LAYOUT_DIRECTION_RTL) {
                spannableString.setSpan(ForegroundColorSpan(toColor), animator.animatedValue.toString().toInt(), spannableString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            textView.text = spannableString
        }
        valueAnimator.duration = duration
        valueAnimator.start()
        valueAnimator.doOnEnd {
            // ** 각 텍스뷰에 대한 행동 정의
            when(position){
                // Select Bell 클릭 시
                0 -> {
                    textView.text = context.getString(R.string.settingItem_selectAlarm)
                }
                // Set Alarm Mode 클릭 시
                1 -> {
                    textView.text = context.getString(R.string.settingItem_alarmMode)
                    // * 항목 선택 Dialog 설정
                    val modeItem = arrayOf(context.getString(R.string.settingItem_alarmModeItem1), context.getString(R.string.settingItem_alarmModeItem2))
                    val builder = AlertDialog.Builder(context)
                    builder.setTitle(context.getString(R.string.settingItem_alarmMode))
                    builder.setSingleChoiceItems(modeItem, 0 , null)
                    builder.setNeutralButton(context.getString(R.string.cancelBtn), null)

                    // * 아이템 선택했을 때 리스너 설정(람다식)
                    builder.setPositiveButton(context.getString(R.string.front_ok)){ dialogInterface: DialogInterface, i: Int ->
                        val alert = dialogInterface as AlertDialog
                        val idx = alert.listView.checkedItemPosition
                        // 선택된 아이템의 position에 따라 행동 조건 넣기
                        when(idx){
                            // Normal 클릭 시
                            0 -> {
                                app.wayOfAlarm = 0
                            }
                            // Calculate 클릭 시
                            1 -> {
                                app.wayOfAlarm = 1  // Calculator 사용 on
                                //
                                val counter = arrayOf("1", "2", "3", "4", "5")
                                val builder = AlertDialog.Builder(context)
                                builder.setTitle(context.getString(R.string.settingItem_calRepeat))
                                builder.setSingleChoiceItems(counter, app.wayOfAlarm , null)
                                builder.setNeutralButton(context.getString(R.string.cancelBtn), null)

                                builder.show()
                            }
                        }
                    }
                    builder.show()
                }
                // Set On/Off Notification 클릭 시
                2 -> {
                    textView.text = context.getString(R.string.settingItem_notification)
                }
                // AppInfo 클릭 시
                3 -> {
                    textView.text = context.getString(R.string.settingItem_info)
                }
            }

        }
    }
}

class SettingViewHolder(view : View) : RecyclerView.ViewHolder(view){
    val binder : SettingRowBinding = SettingRowBinding.bind(view)

    val row_mainText = binder.settingText
    val row_SubText = binder.settingSubText
    val row_image = binder.settingImage
    val row_view = binder.rowItemView
}