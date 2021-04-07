package com.MaidAlarm.easyo_alarm

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.MaidAlarm.easyo_alarm.databinding.ActivityInfoBinding

class InfoActivity : AppCompatActivity() {
    lateinit var binder : ActivityInfoBinding
    override fun onResume() {
        super.onResume()
        binder.infoRecyclerView.layoutManager = LinearLayoutManager(this)
        binder.infoRecyclerView.adapter = InfoAdapter(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)
        binder = ActivityInfoBinding.inflate(layoutInflater)

        val function = Function()
        // 현재 버전 이름을 텍스트뷰에 넣기
        binder.infoTextView2.text = function.checkAppVersion(this)

        setContentView(binder.root)
    }
}