package com.MaidAlarm.easyo_alarm

import android.content.Intent
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

        // 그림 길게 누를 시 개발자 화면 띄우기
        binder.infoImageView1.setOnLongClickListener{
            val developerActivity = Intent(this, Developer::class.java)
            startActivityForResult(developerActivity, 100)

            true
        }
        setContentView(binder.root)
    }
}