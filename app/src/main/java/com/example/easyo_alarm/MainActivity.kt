package com.example.easyo_alarm

import android.content.ComponentName
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.easyo_alarm.databinding.ActivityMainBinding
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.iammert.library.readablebottombar.ReadableBottomBar

class MainActivity : AppCompatActivity() {
    // 메인 바인딩
    lateinit var mainBinder : ActivityMainBinding

    // 알람 화면 프래그먼트
    val alarmFragment = com.example.easyo_alarm.alarmFragment()
    // 세팅 화면 프래그먼트
    val settingFragment = com.example.easyo_alarm.settingFragment()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mainBinder = ActivityMainBinding.inflate(layoutInflater)

        // 1. 애드몹 초기화
        MobileAds.initialize(this) {}
        // 2. 애드몹 로드
        val adRequest = AdRequest.Builder().build()
        mainBinder.adView.loadAd(adRequest)

        val receiver = ComponentName(this, Receiver::class.java)

        // 기기 재부팅시 수신기를 사용 가능한 상태로 만들기
        this.packageManager.setComponentEnabledSetting(
                receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
        )

        // 최초 화면은 알람탭의 화면을 보여주게 한다
        val tran = supportFragmentManager.beginTransaction()
        tran.replace(R.id.container, alarmFragment)

        tran.commit()

        // 탭 클릭에 따른 리스너 설정
        mainBinder.BottomBar.setOnItemSelectListener( object : ReadableBottomBar.ItemSelectListener{
            override fun onItemSelected(index: Int) {
                when(index){
                    0 -> {
                        val tran = supportFragmentManager.beginTransaction()
                        tran.replace(R.id.container, alarmFragment)
                        tran.commit()
                    }

                    1 -> {
                        val tran = supportFragmentManager.beginTransaction()
                        tran.replace(R.id.container, settingFragment)
                        tran.commit()
                    }
                }
            }
        })
        setContentView(mainBinder.root)
    }
}