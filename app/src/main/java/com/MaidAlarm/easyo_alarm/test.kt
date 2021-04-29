package com.MaidAlarm.easyo_alarm

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager


class test : PreferenceFragmentCompat() {
    lateinit var app : AppClass
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.setting_pref)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        view!!.setBackgroundColor(Color.parseColor("#E6E6E6"))
        app = context!!.applicationContext as AppClass

        val pref = PreferenceManager.getDefaultSharedPreferences(context)
        // 알람 notification 스위치에 대한 값
        val alarmSwitch = pref.getBoolean("alarm_set", true)
        // 알람 모드에 대한 값
        var alarmModeValue = pref.getString("mode_set", null)
        // 알람 모드 반복에 대한 값
        val alarmModeRepeat = pref.getInt("mode_repeat", 1)

        val normalMode = resources.getStringArray(R.array.value_list)

        // 알람 모드 변경에 따라 반복 횟수 항목 보여주기 on/off
        findPreference<androidx.preference.Preference>("mode_set")!!.setOnPreferenceChangeListener { preference, newValue ->
            alarmModeValue = pref.getString("mode_set", null)
            // 알람 스위치가 off이거나 모드가 normal일 때
            if (alarmModeValue!! == normalMode[0]){
                findPreference<androidx.preference.Preference>("mode_repeat")!!.isVisible = false
            }else if (alarmModeValue!! != normalMode[0]){
                findPreference<androidx.preference.Preference>("mode_repeat")!!.isVisible = true
            }
            true
        }

        return view
    }
}