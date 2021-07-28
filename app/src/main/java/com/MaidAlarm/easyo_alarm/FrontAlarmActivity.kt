package com.MaidAlarm.easyo_alarm

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.*
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import com.MaidAlarm.easyo_alarm.AppClass.Companion.context
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

    lateinit var pref : SharedPreferences
    private var alarmSwitch  = 0
    private var volume = 0
    private var alarmCounter = 0

    val weatherFragment = WeatherFragment()

    var backButtonCounter = 0

    // *** FrontAlarmActivity가 열려있을 때는 backButton으로 액티비티를 닫지 못하게 한다 -> 그냥 이 메서드 비워두면됨
    // 날씨 fragment를 띄우고 있을 때는 뒤로가기로 현재 액티비티 종료 가능
    override fun onBackPressed() {
        if (backButtonCounter == 1){
            finishAndRemoveTask()
        }
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
                0 -> app.mediaPlayer = MediaPlayer.create(this, R.raw.normal_walking)
                1 -> app.mediaPlayer = MediaPlayer.create(this, R.raw.normal_pianoman)
                2 -> app.mediaPlayer = MediaPlayer.create(this, R.raw.normal_happytown)
                3 -> app.mediaPlayer = MediaPlayer.create(this, R.raw.normal_loney)
                10 -> app.mediaPlayer = MediaPlayer.create(this, R.raw.voice_k_juyoeng)
                11 -> app.mediaPlayer = MediaPlayer.create(this, R.raw.vocie_k_minjeong)
                40 -> app.mediaPlayer = MediaPlayer.create(this, R.raw.sonic_16746)
                41 -> app.mediaPlayer = MediaPlayer.create(this, R.raw.sonic_15805)
                42 -> app.mediaPlayer = MediaPlayer.create(this, R.raw.sonic_14918)
                43 -> app.mediaPlayer = MediaPlayer.create(this, R.raw.sonic_14080)
            }

        }catch (e: Exception){

        }

        Log.d("FrontAlarmActivity", "bellIndex: ${app.bellIndex}")

        // *** 화면에 보여줄 시간을 가져온다
        val calendar = Calendar.getInstance()
        val present_hour = calendar.get(Calendar.HOUR_OF_DAY)
        val present_min = calendar.get(Calendar.MINUTE)
        var present_hourText = ""

        Log.d("FrontAlarmActivity", "problem1: $problem1")
        Log.d("FrontAlarmActivity", "problem2: $problem2")

        // *** 시간은 항상 두 자리로 표시해야하기 때문에 조건을 설정해준다
        if (present_hour < 10){
            binder.FrontHour.text = "0$present_hour"
            present_hourText = "0$present_hour"
        }else{
            binder.FrontHour.text = "$present_hour"
            present_hourText = "$present_hour"
        }
        // 분은 항상 두 자리로 표시해야하기 때문에 조건을 설정해준다
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
        // (21.07.13) !binder.buttonContainer.isGone 추가 -> 즉, 날씨 fragment를 보여주고 있다면 음악재생을 하지 않는다
        if (progress > 0 && !binder.buttonContainer.isGone){
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

        var recentAlarm = RecentAlarm()
        var recentTimeList = recentAlarm.checkSQLGetSecond(SQLHelper)

        fun checkNextAlarm(){
            // 설정에서 notification이 "사용 상태"로 되어 있을 때
            // alarmFragment에 있는 view에 대한 갱신은 alarmFragment의 onResume에서 실시하기 때문에
            // 여기서는 notification에 대한 갱신만 해주면 된다
            if (alarmSwitch == 1) {
                // * 가장 가까운 알람의 시간 알아내기
                recentAlarm = RecentAlarm()
                recentTimeList = recentAlarm.checkSQLGetSecond(SQLHelper)

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
            }
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
                        try{
                            if (progress == 0) {
                                vib.cancel()
                            }else{
                                mediaPlayer.release()
                                // 볼륨 원래대로 되돌리기
                                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, AudioManager.FLAG_PLAY_SOUND)
                            }
                        }catch (e:Exception){

                        }

                        checkNextAlarm()

                        // 1분뒤 소리 울리는거 취소 - 트리거 취소
                        app.threadTrigger = 0

                        // 아침 날씨확인 및 관련 기능 실행
                        okButtonClicked(present_hour)
                    }
                }
            }
            // 계산문제를 설정하지 않았을 때
            else {
                try{
                    // 설정 볼륨이 0일 때는 진동만 끄게 한다
                    if (progress == 0) {
                        vib.cancel()
                    }// 볼륨이 0을 넘으면 소리를 끄게 한다
                    else{
                        mediaPlayer.release()
                        // 볼륨 원래대로 되돌리기
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, AudioManager.FLAG_PLAY_SOUND)
                    }
                }catch (e:Exception){

                }
                checkNextAlarm()

                // 아침 날씨확인 및 관련 기능 실행
                okButtonClicked(present_hour)
            }
        }

        // "10분 뒤" 버튼 클릭 시 동작 설정
        binder.button10Min.setOnClickListener {
            val alarmManager: AlarmManager? =
                this.getSystemService(Context.ALARM_SERVICE) as AlarmManager?

            val intent = Intent(this, Receiver::class.java)
            val calendar = Calendar.getInstance()

            // 10분뒤에 울릴 알람도 똑같은 조건으로 울려야하기 때문에 같이 넘겨준다
            intent.putExtra("progress", progress)
            intent.putExtra("bellIndex", bellIndex)
            intent.putExtra("alarmMode", alarmMode)
            intent.putExtra("action", "POSTPHONETIME")

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
    fun okButtonClicked(present_hour : Int){
        // 아침 날씨 확인 스위치가 on인지 확인
        val pref = getSharedPreferences("morningWeatherData", Context.MODE_PRIVATE)
        val morningWeatherSwitch = pref.getBoolean("morningSwitch", false)
        // 알람이 울린 시간이 5 ~ 9시 사이 & 아침 날씨 스위치가 on 이라면 ok 후 날씨 화면을 보여준다
        if (present_hour in 5..8 && morningWeatherSwitch){
            Log.d("test", "아침 날씨 알람")

            val intent = Intent(context, MainActivity::class.java)
            finishAffinity()

            // 다른 액티비티를 모두 제거하고 액티비티를 띄운다
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra("morningWeather", true)
            startActivity(intent)

        }else{
            finishAndRemoveTask()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try{
            // 음악(소리) 끄기
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