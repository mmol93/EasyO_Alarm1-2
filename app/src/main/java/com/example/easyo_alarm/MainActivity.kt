package com.example.easyo_alarm

import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.easyo_alarm.databinding.ActivityMainBinding
import com.example.easyo_alarm.notification.notification
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.iammert.library.readablebottombar.ReadableBottomBar
import java.io.DataInputStream
import java.io.DataOutputStream
import java.lang.Exception

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

    // AppClass 변수 선언
    lateinit var app : AppClass

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mainBinder = ActivityMainBinding.inflate(layoutInflater)

        // *** 내부 저장소에서 AppClass에 넣을 데이터 가져오기
        app = application as AppClass

        // *** Task 종료에 대한 서비스를 실시한다
        startService(Intent(this, Service::class.java))

        // 내부저장소에서 데이터를 읽어온다
        try {
            // 먼저 데이터를 가져온다
            // 파일 읽어오기
            val fis = openFileInput("data1.bat")
            val dis = DataInputStream(fis)

            val data1 = dis.readInt()
            val data2 = dis.readInt()
            val data3 = dis.readInt()
            val data4 = dis.readInt()
            val data5 = dis.readInt()

            app.wayOfAlarm = data1
            app.counter = data2
            app.notificationSwitch = data3
            app.initialStart = data4
            app.bellIndex = data5
            Log.d("MainActivity", "앱을 기동했습니다.")

        }catch (e:Exception){
            // 어플을 처음 사용하는 거라서 데이터가 없는 경우에는 기본 값으로 만들어 준다
            val fos = openFileOutput("data1.bat",Context.MODE_PRIVATE)

            val dos = DataOutputStream(fos)
            dos.writeInt(app.wayOfAlarm)
            dos.writeInt(app.counter)
            dos.writeInt(app.notificationSwitch)
            dos.writeInt(app.initialStart)
            dos.writeInt(app.bellIndex)

            dos.flush()
            dos.close()
            Log.d("MainActivity", "처음 앱을 기동했습니다.")
        }

        // *** 애드몹 초기화
        MobileAds.initialize(this) {}
        // ** 애드몹 로드
        val adRequest = AdRequest.Builder().build()
        mainBinder.adView.loadAd(adRequest)

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

    override fun onDestroy() {
        super.onDestroy()
        Log.d("Exit", "Task 종료됨")
        val app : AppClass = application as AppClass

        // AppClass에 저장되어 있는 변수들을 파일에 저장한다
        val fos = openFileOutput("data1.bat", Context.MODE_PRIVATE)

        val dos = DataOutputStream(fos)
        dos.writeInt(app.wayOfAlarm)
        dos.writeInt(app.counter)
        dos.writeInt(app.notificationSwitch)
        dos.writeInt(app.initialStart)
        dos.writeInt(app.bellIndex)

        dos.flush()
        dos.close()
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