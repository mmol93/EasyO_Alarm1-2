package com.example.easyo_alarm

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.easyo_alarm.databinding.ActivityInfoBinding

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

        setContentView(binder.root)
    }
}