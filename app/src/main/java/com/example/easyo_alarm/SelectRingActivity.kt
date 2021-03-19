package com.example.easyo_alarm

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.isGone
import com.example.easyo_alarm.databinding.ActivitySelectRingBinding

class SelectRingActivity : AppCompatActivity() {
    lateinit var binder : ActivitySelectRingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_ring)
        binder = ActivitySelectRingBinding.inflate(layoutInflater)

        // 필요 없는 항목은 보이지 않게 하기
        binder.typeEnglish.isGone = true
        binder.typeJapanese.isGone = true

        setContentView(binder.root)
    }
}