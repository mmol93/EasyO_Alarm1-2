package com.example.easyo_alarm

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.easyo_alarm.databinding.ActivityMainBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.iammert.library.readablebottombar.ReadableBottomBar
import java.io.DataInputStream
import java.io.DataOutputStream
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity() {
    // 메인 바인딩
    lateinit var mainBinder : ActivityMainBinding

    // 뒤로가기 두번 연속 클릭으로 종료 변수 설정
    // 2 초내에 더블 클릭시...
    private val TIME_INTERVAL = 2000
    private var mBackPressed: Long = 0

    // 권한 확인용 액티비티 콜백 코드
    private val permissionCode = 100

    // 알람 화면 프래그먼트
    val alarmFragment = com.example.easyo_alarm.alarmFragment()
    // 세팅 화면 프래그먼트
    val settingFragment = com.example.easyo_alarm.settingFragment()

    // AppClass 변수 선언
    lateinit var app : AppClass

    // 확인할 권한 리스트
    val permissionList = arrayOf(
        Manifest.permission.SYSTEM_ALERT_WINDOW,
        Manifest.permission.VIBRATE,
        Manifest.permission.WAKE_LOCK,
        Manifest.permission.RECEIVE_BOOT_COMPLETED
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mainBinder = ActivityMainBinding.inflate(layoutInflater)

        // 오버레이 권한 확인
        if (!Settings.canDrawOverlays(this)) {
            // ask for setting
            val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, permissionCode)
        }

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

        }catch (e: Exception){
            // 어플을 처음 사용하는 거라서 데이터가 없는 경우에는 기본 값으로 만들어 준다
            val fos = openFileOutput("data1.bat", Context.MODE_PRIVATE)

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
        val adView = findViewById<AdView>(R.id.adView)
        adView.loadAd(adRequest)

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

    override fun startActivityForResult(intent: Intent?, requestCode: Int) {
        super.startActivityForResult(intent, requestCode)

        // 모든 권한을 다 얻었는지 한번 더 체크
        if (requestCode == permissionCode){
            // 거부된 권한이 있을 경우 실행이 불가능 하다는 메시지를 남긴다
            val dialogBuilder = AlertDialog.Builder(this)

            dialogBuilder.setTitle(getString(R.string.permission_dialogTitle))
            dialogBuilder.setMessage(getString(R.string.permission_dialogMessage))
            dialogBuilder.setIcon(R.mipmap.icon_maidalarm)

            // overlay 권한을 허용 했는지 dialog로 확인
            // 이상하게 Activity 단게에서 확인하면 에러가 발생함...
            // 이용자가 버튼을 클릭 후 확인하는 방식으로 변경
            dialogBuilder.setNegativeButton(getString(R.string.permission_dialogGotIt)){ dialogInterface: DialogInterface, i: Int ->
                if (Settings.canDrawOverlays(this)){

                }
                else{
                    ActivityCompat.finishAffinity(this)
                    exitProcess(0)
                }
            }
            dialogBuilder.setOnCancelListener {
                if (Settings.canDrawOverlays(this)){

                }
                else{
                    ActivityCompat.finishAffinity(this)
                    exitProcess(0)
                }
            }
            dialogBuilder.show()
        }
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
            Toast.makeText(
                getBaseContext(),
                getString(R.string.backButtonDoubleClick), Toast.LENGTH_SHORT
            ).show();
        }
        mBackPressed = System.currentTimeMillis();
    }
}