package com.example.easyo_alarm

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.easyo_alarm.databinding.ActivityInfoBinding
import com.example.easyo_alarm.databinding.ActivityLicenseBinding

// 오픈 라이센스 액티비티
class LicenseActivity : AppCompatActivity() {
    lateinit var binder : ActivityLicenseBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_license)

        binder = ActivityLicenseBinding.inflate(layoutInflater)
        setContentView(binder.root)
    }

    override fun onResume() {
        super.onResume()

        val textData = intent.getStringExtra("content")
        binder.textView8.text = textData
    }
}