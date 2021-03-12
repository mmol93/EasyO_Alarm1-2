package com.example.easyo_alarm

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.easyo_alarm.databinding.ActivitySelectRingBinding

class SelectRingActivity : AppCompatActivity() {
    lateinit var binder : ActivitySelectRingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_ring)
        binder = ActivitySelectRingBinding.inflate(layoutInflater)



        setContentView(binder.root)
    }
}