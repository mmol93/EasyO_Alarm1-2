package com.MaidAlarm.easyo_alarm

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import com.MaidAlarm.easyo_alarm.databinding.ActivityMainBinding
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.AppUpdateType.FLEXIBLE
import com.google.android.play.core.install.model.AppUpdateType.IMMEDIATE
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.iammert.library.readablebottombar.ReadableBottomBar
import java.io.DataInputStream
import java.io.DataOutputStream
import java.util.*
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
    val alarmFragment = com.MaidAlarm.easyo_alarm.alarmFragment()
    // 세팅 화면 프래그먼트
    val settingFragment = com.MaidAlarm.easyo_alarm.settingFragment()

    val testFragment = test()

    // AppClass 변수 선언
    lateinit var app : AppClass

    // 앱 업데이트 검사
    lateinit var appUpdateManager: AppUpdateManager
    private val REQUEST_CODE_UPDATE = 10

    // update 확인에 사용할 변수들
    private val function = Function()
    private var lastUpdate = 0L
    private var currentTime = System.currentTimeMillis()
    private val updateInterval : Long = 7 * 24 * 60 * 60 * 1000 // 7일 뒤

    // 확인할 권한 리스트
    val permissionList = arrayOf(
        Manifest.permission.VIBRATE,
        Manifest.permission.WAKE_LOCK,
        Manifest.permission.RECEIVE_BOOT_COMPLETED,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mainBinder = ActivityMainBinding.inflate(layoutInflater)

        // *** 앱 업데이트 있는지 검사하는 객체 가져오기
        appUpdateManager = AppUpdateManagerFactory.create(this)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        // 일부 휴대폰에서 onCreate()가 여러번 호출되기 때문에 여기에 권한 확인을 넣음
        if (savedInstanceState == null){
            // 오버레이 권한 확인
            if (!Settings.canDrawOverlays(this)) {
                // ask for setting
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivityForResult(intent, permissionCode)
            }

            // 다른 권한 확인
            for (permission in permissionList){
                val check = checkCallingOrSelfPermission(permission)
                if (check == PackageManager.PERMISSION_GRANTED){
                    Log.d("MainActivity", "${permission} 승인됨")
                }
                else{
                    Log.d("MainActivity", "${permission} 거부됨")
                    requestPermissions(permissionList, 0)
                }
            }

            // 리시버 등록
            val receiver = Receiver()
            val filter = IntentFilter("ActionButton")
            registerReceiver(receiver, filter)

            // 업데이트 주기 확인을 위해 데이터 가져오기
            try{
                val fis = openFileInput("data3.bat")
                val dis = DataInputStream(fis)

                lastUpdate = dis.readLong()
            }catch (e:Exception){

            }

            // AppUpdateManager 업데이트 초기화
            appUpdateManager?.let {
                it.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
                    // 이용가능한 업데이트가 있는지 확인 - 한 번 취소 했을 경우 일주일 이상 경과했을 때만 뜨게 하기
                    // 마지막 업데이트 확인 or 거부 후 일주일 이상 지났을 때
                    if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                        && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                        // 있을 경우 업데이트 실시
                        appUpdateManager?.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.FLEXIBLE,this, REQUEST_CODE_UPDATE)
                    }
                }
            }

            // 인앱 업데이트 상태 리스너
            val updateListener = InstallStateUpdatedListener { state ->
                if (state.installStatus() == InstallStatus.DOWNLOADED){
                    Toast.makeText(this, getString(R.string.main_updateDownloadDone), Toast.LENGTH_LONG).show()
                }
            }
            appUpdateManager.registerListener(updateListener)
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
        mainBinder.adView.loadAd(adRequest)

        mainBinder.adView.adListener = object : AdListener(){
            override fun onAdFailedToLoad(p0: LoadAdError) {
                super.onAdFailedToLoad(p0)
                Log.d("adMob", "메인광고 로드 실패")
            }

            override fun onAdLoaded() {
                super.onAdOpened()
                Log.d("adMob", "메인광고 열림 성공")
            }
        }

        // ** 앱 실행 시 모든 알람 다시 예약하기(갱신)
        val function = Function()
        function.makeAlarmWithAllSQL(this)

        // ** 최초 화면은 알람탭의 화면을 보여주게 한다
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
        // 브로드캐스트에 등록했던 리시버도 종료해야한다(안하면 2개씩 나옴) - 종료 서비스에도 있음
        val receiver = Receiver()
        try{
            unregisterReceiver(receiver)
        }catch (e:Exception){

        }

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

    override fun onResume() {
        super.onResume()

        // 업데이트가 끝났을 경우
        appUpdateManager
            .appUpdateInfo
            .addOnSuccessListener { appUpdateInfo ->
                // 업뎃 다운이 끝났을 경우 설치를 해라고 알려줘야함
                if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                    val dialogBuilder = AlertDialog.Builder(this)

                    dialogBuilder.setTitle(getString(R.string.update_dialogTitle))
                    dialogBuilder.setMessage(getString(R.string.update_dialogMessage))
                    dialogBuilder.setIcon(R.mipmap.icon_maidalarm)

                    // YES 부분 버튼 설정
                    dialogBuilder.setNegativeButton(getString(R.string.list_dialog_Yes)){ dialogInterface: DialogInterface, i: Int ->
                        appUpdateManager.completeUpdate()
                    }

                    dialogBuilder.show()
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_UPDATE) {
            // 업데이트를 거부했을 경우 -> 거부한 시간을 파일 데이터로써 집어넣는다
            if (resultCode != RESULT_OK) {
            }
            // 업데이트를 수락했을 경우
            else{
                Toast.makeText(this, getString(R.string.main_updateDownloading), Toast.LENGTH_LONG).show()
            }
        }


    }

    override fun onStop() {
        super.onStop()
//        Toast.makeText(this, "onStop 호출됨", Toast.LENGTH_SHORT).show()
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
                baseContext,
                getString(R.string.backButtonDoubleClick), Toast.LENGTH_SHORT
            ).show();
        }
        mBackPressed = System.currentTimeMillis();
    }
}

