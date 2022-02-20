package com.MaidAlarm.easyo_alarm

import android.Manifest
import android.app.KeyguardManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
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
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType.FLEXIBLE
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.iammert.library.readablebottombar.ReadableBottomBar
import java.io.DataInputStream
import java.io.DataOutputStream
import java.util.*

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
    val alarmFragment = alarmFragment()
    // 세팅 화면 프래그먼트
    val settingFragment = settingFragment()
    // 날씨 화면 프래그먼트
    val weatherFragment = WeatherFragment()

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
    private var morningSwitch = false

    // 확인할 권한 리스트
    val permissionList = arrayOf(
        Manifest.permission.VIBRATE,
        Manifest.permission.WAKE_LOCK,
        Manifest.permission.RECEIVE_BOOT_COMPLETED,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mainBinder = ActivityMainBinding.inflate(layoutInflater)

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

        // 8.0 이상 부터는 브로드캐스트 등록을 해야한다
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val brFilter = IntentFilter("com.maidalarm.easyo.alarm")
            val broadCastReceiver = Receiver()
            registerReceiver(broadCastReceiver, brFilter)
        }

        // 아침 날씨 확인에 대한 인텐트 결과 처리
        morningSwitch = intent.getBooleanExtra("morningWeather", false)
        Log.d("WeatherAlarm - MainActivity.kt", "morningSwitch: $morningSwitch")

        // 바로 날씨 화면을 보여준다
        if (morningSwitch){
            Log.d("WeatherAlarm - MainActivity.kt", "MainActivity에서 날씨탭 발동")

            // 바텀버튼의 스위치가 1번을 누르게 설정하고
            mainBinder.BottomBar.selectItem(1)
            // 날씨 화면으로 전환한다
            val tran = supportFragmentManager.beginTransaction()
            tran.replace(R.id.container, weatherFragment)
            tran.commit()
        }
        // 앱 실행으로 MainActivity를 띄웠을 때만 업데이트 실행하게 하기
        else{
            // *** 앱 업데이트 있는지 검사하는 객체 가져오기
            appUpdateManager = AppUpdateManagerFactory.create(this)
            // 일부 휴대폰에서 onCreate()가 여러번 호출되기 때문에 여기에 권한 확인을 넣음
            if (savedInstanceState == null){
                Log.d("mainActivity", "savedInstanceState: $savedInstanceState")
                // 오버레이 권한 확인
                if (!Settings.canDrawOverlays(this)) {
                    // ask for setting
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                    startActivityForResult(intent, permissionCode)
                    Log.d("mainActivity", "오버레이 intent 호출")
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
                            && appUpdateInfo.isUpdateTypeAllowed(FLEXIBLE)) {
                            // 있을 경우 업데이트 실시
                            appUpdateManager?.startUpdateFlowForResult(appUpdateInfo, FLEXIBLE,this, REQUEST_CODE_UPDATE)
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

                // *** Task 종료에 대한 서비스를 실시한다
                // 잠금화면 상태에서는 실행하지 않게 한다
                val checkLockScreen = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
                if (checkLockScreen.isKeyguardLocked){

                }else{
                    try{
                        startService(Intent(this, Service::class.java))
                    }catch (e:Exception){

                    }
                }
            }
        }
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        // *** 내부 저장소에서 AppClass에 넣을 데이터 가져오기
        app = application as AppClass

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
            val fos = openFileOutput("data1.bat", MODE_PRIVATE)

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

        // ** 앱 실행 시 모든 알람 다시 예약하기(갱신)
        val function = Function()
        function.makeAlarmWithAllSQL(this)

        // ** 최초 화면은 알람탭의 화면을 보여주게 한다
        if (!morningSwitch){
            val tran = supportFragmentManager.beginTransaction()
            tran.replace(R.id.container, alarmFragment)

            tran.commit()
        }

        // 탭 클릭에 따른 리스너 설정
        mainBinder.BottomBar.setOnItemSelectListener(object : ReadableBottomBar.ItemSelectListener {
            override fun onItemSelected(index: Int) {
                when (index) {
                    // 메인 화면 프래그먼트
                    0 -> {
                        val tran = supportFragmentManager.beginTransaction()
                        tran.replace(R.id.container, alarmFragment)
                        tran.commit()
                    }
                    // 날씨 화면 프래그먼트
                    1 -> {
                        // 권한을 얻었는지 확인(getLastKnownLocation을 사용하기 위해서 반드시 필요한 사전 확인임)
                        if (ActivityCompat.checkSelfPermission(this@MainActivity, Manifest.permission.ACCESS_FINE_LOCATION) !=
                            PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this@MainActivity,Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {

                            Toast.makeText(AppClass.context, getString(R.string.location_permmision), Toast.LENGTH_LONG).show()

                            // 확인할 권한 리스트
                            val permissionList = arrayOf(
                                Manifest.permission.VIBRATE,
                                Manifest.permission.WAKE_LOCK,
                                Manifest.permission.RECEIVE_BOOT_COMPLETED,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                            requestPermissions(permissionList, 1)
                            mainBinder.BottomBar.selectItem(0)
                            Toast.makeText(this@MainActivity, getString(R.string.permission_overlay_toast), Toast.LENGTH_LONG).show()
                        }
                        // 날씨 권한을 다 얻었을 경우 날씨 fragment를 열게 한다
                        else{
                            val tran = supportFragmentManager.beginTransaction()
                            tran.replace(R.id.container, weatherFragment)
                            tran.commit()
                        }
                    }
                    // 설정 화면 프래그먼트
                    2 -> {
                        val tran = supportFragmentManager.beginTransaction()
                        tran.replace(R.id.container, settingFragment)
                        tran.commit()
                    }
                }
            }
        })

        setContentView(mainBinder.root)
    }

    // 날씨에 대한 권한에 대해 그 이후 결과 확인용
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        for (permission in grantResults){
            if (permission == PackageManager.PERMISSION_DENIED){
                Toast.makeText(this, "권한을 얻지않으면 GPS 사용불가", Toast.LENGTH_SHORT).show()
                return
            }
        }

    }

    override fun startActivityForResult(intent: Intent?, requestCode: Int) {
        super.startActivityForResult(intent, requestCode)

        // 모든 권한을 다 얻었는지 한번 더 체크
        if (requestCode == permissionCode){
            if (!Settings.canDrawOverlays(this)) {
                // 거부된 권한이 있을 경우 실행이 불가능 하다는 메시지를 남긴다
                val dialogBuilder = AlertDialog.Builder(this)

                dialogBuilder.setTitle(getString(R.string.permission_dialogTitle))
                dialogBuilder.setMessage(getString(R.string.permission_dialogMessage))
                dialogBuilder.setIcon(R.mipmap.icon_maidalarm)

                // overlay 권한을 허용 했는지 dialog로 확인
                // 이상하게 Activity 단게에서 확인하면 에러가 발생함...
                // 이용자가 버튼을 클릭 후 확인하는 방식으로 변경
                dialogBuilder.setNegativeButton(getString(R.string.permission_dialogGotIt)) { dialogInterface: DialogInterface, i: Int ->
                    if (Settings.canDrawOverlays(this)) {

                    } else {

                    }
                }
                dialogBuilder.setNeutralButton(getString(R.string.permission_dialog_No)) { dialogInterface: DialogInterface, i: Int ->
                    // ask for setting
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                    startActivityForResult(intent, permissionCode)
                }
                dialogBuilder.setOnCancelListener {
                    if (Settings.canDrawOverlays(this)) {

                    } else {

                    }
                }
                dialogBuilder.show()
            }
        }

    }

    override fun onResume() {
        super.onResume()
        if (!morningSwitch){
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
        val app : AppClass = application as AppClass

        // AppClass에 저장되어 있는 변수들을 파일에 저장한다
        val fos = openFileOutput("data1.bat", MODE_PRIVATE)

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

