package com.MaidAlarm.easyo_alarm

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.*
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.MaidAlarm.easyo_alarm.databinding.ActivityFrontAlarmBinding
import com.MaidAlarm.easyo_alarm.notification.notification
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import java.io.DataInputStream
import java.util.*
import kotlin.math.log
import kotlin.random.Random


class FrontAlarmActivity : AppCompatActivity() {
    lateinit var binder : ActivityFrontAlarmBinding

    // 계산 문제에서 사용할 변수
    var problem1 = Random.nextInt(1, 100)
    var problem2 = Random.nextInt(1, 10)
    var user_answer = 0
    lateinit var vib : Vibrator // 진동관련 변수 - 여기서 정의해야 ok버튼의 리스너에서 사용가능
    lateinit var mediaPlayer: MediaPlayer
    var currentVolume : Int = 0
    lateinit var audioManager: AudioManager
    lateinit var app : AppClass
    var bellIndex = 0
    var alarmMode = 0
<<<<<<< HEAD
=======

    lateinit var pref : SharedPreferences
    private var alarmSwitch  = 0
    private var volume = 0
    private var alarmCounter = 0

>>>>>>> ver2.1

    // *** FrontAlarmActivity가 열려있을 때는 backButton으로 액티비티를 닫지 못하게 한다 -> 그냥 이 메서드 비워두면됨
    override fun onBackPressed() {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_front_alarm)

        pref = getSharedPreferences("simpleAlarmData", Context.MODE_PRIVATE)
        alarmSwitch = pref.getInt("alarmSwitch", 1)

        // 현재 화면이 자동으로 꺼지지 않게 유지 & 잠금화면에 액티비티 띄우기
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1){
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        }else{
            this.window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        }

        binder = ActivityFrontAlarmBinding.inflate(layoutInflater)
        app = application as AppClass
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        vib = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        val calculateProblemFragment = CalculateProblemFragment()
        // SQL에 대한 변수 선언
        val SQLHelper = SQLHelper(this)
        val sql = "select * from MaidAlarm"
        val c1 = SQLHelper.writableDatabase.rawQuery(sql, null)
        val size = c1.count

        // 광고 초기화
        // *** 애드몹 초기화
        MobileAds.initialize(this) {}

        // ** 애드몹 로드
        // receiver로 띄운 액티비티에 바로 광고를 로드하면 정책위반임
        // -> 즉, 먼저 더미 액티비티(시간 확인등)를 띄우고 다음에 계산 문제 액티비티에 광고를 띄우는 것이 알맞음
//        val adRequest = AdRequest.Builder().build()
//        binder.adView.loadAd(adRequest)
//        binder.adView.adListener = object : AdListener(){
//            override fun onAdFailedToLoad(p0: Int) {
//                super.onAdFailedToLoad(p0)
//                Log.d("FrontActivity", "front 광고 로드 실패")
//            }
//            override fun onAdLoaded() {
//                super.onAdOpened()
//                Log.d("adMob", "front 광고 열림 성공")
//            }
//        }

        try {
            // 파일 읽어오기
            val fis = openFileInput("data1.bat")
            val dis = DataInputStream(fis)

            val data1 = dis.readInt()
            val data2 = dis.readInt()
            val data3 = dis.readInt()
            val data4 = dis.readInt()

            app.wayOfAlarm = data1
            app.counter = data2
            app.notificationSwitch = data3
            app.initialStart = data4

            bellIndex = intent.getIntExtra("bellIndex", 0)

            when(bellIndex){
                0 -> app.mediaPlayer = MediaPlayer.create(this, R.raw.normal_jazzbar)
                1 -> app.mediaPlayer = MediaPlayer.create(this, R.raw.normal_guitar)
                2 -> app.mediaPlayer = MediaPlayer.create(this, R.raw.normal_happytown)
                3 -> app.mediaPlayer = MediaPlayer.create(this, R.raw.normal_country)
                10 -> app.mediaPlayer = MediaPlayer.create(this, R.raw.voice_k_juyoeng)
                11 -> app.mediaPlayer = MediaPlayer.create(this, R.raw.vocie_k_minjeong)
            }

        }catch (e: Exception){

        }

        Log.d("FrontAlarmActivity", "bellIndex: ${app.bellIndex}")

        // *** 화면에 보여줄 시간을 가져온다
        val calendar = Calendar.getInstance()
        val present_hour = calendar.get(Calendar.HOUR_OF_DAY)
        val present_min = calendar.get(Calendar.MINUTE)

        Log.d("FrontAlarmActivity", "problem1: $problem1")
        Log.d("FrontAlarmActivity", "problem2: $problem2")

        // *** 시간은 항상 두 자리로 표시해야하기 때문에 조건을 설정해준다
        if (present_hour < 10){
            binder.FrontHour.text = "0$present_hour"
        }else{
            binder.FrontHour.text = "$present_hour"
        }
        // 시간은 항상 두 자리로 표시해야하기 때문에 조건을 설정해준다
        if (present_min < 10){
            binder.FrontMin.text = "0$present_min"
        }else{
            binder.FrontMin.text = "$present_min"
        }

        // 먼저 progress 값을 가져온다
        val progress = intent.getIntExtra("progress", -1)
        currentVolume = intent.getIntExtra("currentVolume", 0)

        Log.d("FrontAlarmActivity", "currentVolume in Front: $currentVolume")
        app.lastProgress = progress

        // *** 음악 파일 실행
        if (progress > 0){
            mediaPlayer = app.mediaPlayer
            mediaPlayer.setVolume(1f, 1f)
            mediaPlayer.isLooping = true
            mediaPlayer.start()
        }


        if (progress == -1){
            Log.d("FrontAlarmActivity", "FrontAlarmActivity 의 Vibrate 쪽에 에러 발생")
        }
        // progress가 0일 때는 진동이 울리게 정한다
        else if(progress == 0){
            Log.d("FrontAlarmActivity", "진동울리는중")
            val pattern = LongArray(2) { 500 }

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vib.vibrate(VibrationEffect.createWaveform(pattern, 1))
            } else {
                vib.vibrate(1000)
            }
        }
        Log.d("FrontAlarmActivity", "progress: $progress")

        // *** 계산 문제를 표시할지 말지 결정한다
        alarmMode = intent.getIntExtra("alarmMode", 0)

        // alarmMode가 1~5일 때만 계산문제를 표기한다
        if (alarmMode in 1..5){
            // 계산 fragment를 표시한다
            val calculator = supportFragmentManager.beginTransaction()
            calculator.replace(R.id.front_container, calculateProblemFragment)
            calculator.commit()
        }

        var counter = 0 // 계산 문제 출제 횟수 카운터

        // ok버튼 클릭 시
        binder.buttonOk.setOnClickListener {
            Log.d("FrontActivity", "alarmMode: $alarmMode")
            // 알람 울릴 때 계산 문제를 사용할 때
            if (alarmMode in 1..5) {
                // 설정에서 지정한 계산문제 풀이 횟수 보다 작을 때
                if (counter < alarmMode) {
                    // 정답을 맞췄을 때
                    if (problem1 + problem2 == user_answer) {
                        counter += 1
                        // 계산문제 다시 출제
                        problem1 = Random.nextInt(1, 100)
                        problem2 = Random.nextInt(1, 10)
                        // 문제를 CalculateProblemFragment의 TextView에 반영한다
                        val calculateProblemFragment = supportFragmentManager.findFragmentById(R.id.front_container) as CalculateProblemFragment
                        calculateProblemFragment.setProblem()
                        calculateProblemFragment.binder.answerText.text = ""

                        Log.d("FrontAlarmActivity", "Right")
                        Log.d("FrontAlarmActivity", "problem1: $problem1")
                        Log.d("FrontAlarmActivity", "problem2: $problem2")
                        Log.d("FrontAlarmActivity", "user_answer: $user_answer")
                    }
                    // 답 틀릴 시 answer 텍스트뷰 초기화 하고 진동하게 하기
                    else {
                        Log.d("FrontAlarmActivity", "Wrong")
                        Log.d("FrontAlarmActivity", "problem1: $problem1")
                        Log.d("FrontAlarmActivity", "problem2: $problem2")
                        Log.d("FrontAlarmActivity", "user_answer: $user_answer")
                        calculateProblemFragment.binder.answerText.text = ""

                        // 틀릴 시 진동하게 하기 - 살짝만 진동
                        val arrayTime = longArrayOf(0, 500, 0, 0)
                        val arrayAmplitudes = intArrayOf(0, 150, 0, 0)
                        vib = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                        app.vibrate = vib

                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            vib.vibrate(VibrationEffect.createWaveform(arrayTime, arrayAmplitudes, -1))
                        } else {
                            vib.vibrate(arrayTime, -1)
                        }
                    }
                    // 계산 문제를 카운터 만큼 실시 했을 때 -> 진동, 음악 멈추고 액티비티 종료
                    if (counter >= alarmMode) {
                        if (progress == 0) {
                            vib.cancel()
                        }
                        finishAndRemoveTask()
                        // 1분뒤 소리 울리는거 취소 - 트리거 취소
                        app.threadTrigger = 0
                    }
                }
            }
            // 계산문제를 설정하지 않았을 때
            else {
                if (progress == 0) {
                    vib.cancel()
                }
                finishAndRemoveTask()
            }

            var recentAlarm = RecentAlarm()
            var recentTimeList = recentAlarm.checkSQL(SQLHelper)

            // 그 다음으로 울릴 알람이 없다면 notification 삭제하기
            if (size > 0) {
                // 알림은 있지만 모든 토글이 off 일 때 -> notification 삭제
                if (recentTimeList[0] == -1) {

                    val notification = notification()
                    notification.cancelNotification(this)
                }
                // 남아있는 알람이 있다면 notification 갱신
                else {
                    // 휴식 상태인 휴대폰 깨우기
                    val wakeLock: PowerManager.WakeLock =
<<<<<<< HEAD
                            (this.getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::MyWakelockTag").apply {
                                    acquire()
                                }
                            }

                    // 60초만 지속되게 하기
                    wakeLock.acquire(70*1000L )

=======
                        (this.getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                            newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::MyWakelockTag")
                        }

                    // 70초만 지속되게 하기
                    wakeLock.acquire(70*1000L )
>>>>>>> ver2.1
                    // 현재 시간(시간 + 분)과 울린 알람의 시간(시간 + 분)이 동일함
                        // -> notification을 갱신하더라도 지금 울린 알람이 제일 가까운 알람으로 인식됨
                    val thread = object : Thread(){
                        override fun run() {
                            super.run()
                            Log.d("FrontActivity", "스레드 시작")
                            // 1분간 대기
                            sleep(60 * 1000)
                            Log.d("FrontActivity", "스레드 티이머 끝")
                            // 설정에서 notification이 "사용 상태"로 되어 있을 때
                            // alarmFragment에 있는 view에 대한 갱신은 alarmFragment의 onResume에서 실시하기 때문에
                            // 여기서는 notification에 대한 갱신만 해주면 된다
                            if (alarmSwitch == 1) {
                                // * 가장 가까운 알람의 시간 알아내기
                                recentAlarm = RecentAlarm()
                                recentTimeList = recentAlarm.checkSQL(SQLHelper)

                                var recentHour = ""
                                var recentMin = ""
                                if (recentTimeList[7] < 10) {
                                    recentHour = "0${recentTimeList[7]}"
                                } else {
                                    recentHour = "${recentTimeList[7]}"
                                }
                                if (recentTimeList[8] < 10) {
                                    recentMin = "0${recentTimeList[8]}"
                                } else {
                                    recentMin = "${recentTimeList[8]}"
                                }
                                app.recentTime = "$recentHour : $recentMin"

                                // * 가장 가까운 알람의 요일 알아내기
                                var textForWeek = ""
                                if (recentTimeList[1] == 1) {
                                    textForWeek = textForWeek + getString(R.string.week_mon) + ", "
                                }
                                if (recentTimeList[2] == 1) {
                                    textForWeek = textForWeek + getString(R.string.week_tue) + ", "
                                }
                                if (recentTimeList[3] == 1) {
                                    textForWeek = textForWeek + getString(R.string.week_wed) + ", "
                                }
                                if (recentTimeList[4] == 1) {
                                    textForWeek = textForWeek + getString(R.string.week_thur) + ", "
                                }
                                if (recentTimeList[5] == 1) {
                                    textForWeek = textForWeek + getString(R.string.week_fri) + ", "
                                }
                                if (recentTimeList[6] == 1) {
                                    textForWeek = textForWeek + getString(R.string.week_sat) + ", "
                                }
                                if (recentTimeList[0] == 1) {
                                    textForWeek = textForWeek + getString(R.string.week_sun) + ", "
                                }

                                // 체크된 요일을 문자로 표시한다
                                if (recentTimeList[0] == 1 || recentTimeList[1] == 1 || recentTimeList[2] == 1 || recentTimeList[3] == 1 || recentTimeList[4] == 1
                                        || recentTimeList[5] == 1 || recentTimeList[6] == 1) {

                                    // textForWeek에서 마지막 부분 콤마 제거하기
                                    if (textForWeek.length > 2) {
                                        textForWeek = textForWeek.removeRange(textForWeek.length - 2, textForWeek.length - 1)
                                    }
                                    app.recentWeek = textForWeek    // notification에 사용하기 위한 텍스트 정의2
                                }
                                // ok 버튼을 눌렀을 때 notification의 내용을 갱신해준다
                                val notification = notification()
                                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                                notification.getNotification(this@FrontAlarmActivity, "chanel1", "첫 번째 채널", notificationManager)
                                notification.makeNotification(app, this@FrontAlarmActivity, notificationManager)
                                Log.d("FrontActivity", "recentTime: ${app.recentTime}")
                                Log.d("FrontActivity", "recentWeek: ${app.recentWeek}")
                                Log.d("FrontActivity", "스레드 끝")
                            }
                        }
                    }
                    thread.start()
                }
            }
            // 남아있는 알람이 없다면 notification 삭제
            else{
                val notification = notification()
                notification.cancelNotification(this)
            }
        }

        // "10분 뒤" 버튼 클릭 시 동작 설정
        binder.button10Min.setOnClickListener {
            val alarmManager: AlarmManager? =
                    this.getSystemService(Context.ALARM_SERVICE) as AlarmManager?
            val receiver = Receiver()
            val filter = IntentFilter("POSTPHONETIME")

            val calendar = Calendar.getInstance()
            val intent = Intent("POSTPHONETIME")

<<<<<<< HEAD
            // 다음 알람에도 progress 데이터는 필요하기 때문에 넘겨준다
=======
            // 10분뒤에 울릴 알람도 똑같은 조건으로 울려야하기 때문에 같이 넘겨준다
>>>>>>> ver2.1
            intent.putExtra("progress", progress)
            intent.putExtra("bellIndex", bellIndex)
            intent.putExtra("alarmMode", alarmMode)

            // 10분뒤 알람이므로 현재 시간에 + 10분(10 * 60 * 1000)을 해준다
            val intervalTen = 10 * 60 * 1000

            // 한번 쓰고 버릴 알람이기 때문에 requestCode는 1로 설정한다
            val pendingIntent = PendingIntent.getBroadcast(
                    this,
                    1,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
            )
            // 위에서 설정한 시간(Calendar.getInstance)에 알람이 울리게 한다
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                // API23 이상에서는 setExactAndAllowWhileIdle을 사용해야한다.
                alarmManager?.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis + intervalTen, pendingIntent)
            }else{
                alarmManager?.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis + intervalTen, pendingIntent)
            }
            // 1분뒤 소리 울리는거 취소 = 트리거 취소
            app.threadTrigger = 0

            Toast.makeText(this, getString(R.string.front_10minutes), Toast.LENGTH_LONG).show()

            finishAndRemoveTask()
        }
        setContentView(binder.root)
    }

    override fun onDestroy() {
        super.onDestroy()

        try{
            // 음악 끄기
            mediaPlayer.release()
            // 볼륨 원래대로 되돌리기
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, AudioManager.FLAG_PLAY_SOUND)
        }catch (e:Exception){

        }

        try {
            // 진동 끄기
            vib.cancel()
        }catch (e:Exception){

        }


        Log.d("FrontActivity", "onDestroy()")
    }
}