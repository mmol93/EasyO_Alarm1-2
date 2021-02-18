package com.example.easyo_alarm

import android.content.ComponentName
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.easyo_alarm.databinding.ActivityMainBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.iammert.library.readablebottombar.ReadableBottomBar

class MainActivity : AppCompatActivity() {
    // 메인 바인딩
    lateinit var mainBinder : ActivityMainBinding

    // 뒤로가기 두번 연속 클릭으로 종료 변수 설정
    private val TIME_INTERVAL = 2000    // 2 초내에 더블 클릭시...
    private var mBackPressed: Long = 0


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

        // 최초 화면은 알람탭의 화면을 보여주게 한다
        val tran = supportFragmentManager.beginTransaction()
        tran.replace(R.id.container, alarmFragment)

        tran.commit()

        // 탭 클릭에 따른 리스너 설정
        mainBinder.BottomBar.setOnItemSelectListener(object : ReadableBottomBar.ItemSelectListener {
            override fun onItemSelected(index: Int) {
                when (index) {
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

    // ** 연속 두 번 클릭하여 종료하기
    override fun onBackPressed() {
        if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis()) {
            super.onBackPressed();
            return;
        }
        else {
            Toast.makeText(getBaseContext(),
            getString(R.string.backButtonDoubleClick), Toast.LENGTH_SHORT).show();
        }
        mBackPressed = System.currentTimeMillis();
    }
}