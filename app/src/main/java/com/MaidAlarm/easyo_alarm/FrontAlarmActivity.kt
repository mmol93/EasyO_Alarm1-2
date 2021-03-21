package com.MaidAlarm.easyo_alarm

import android.app.AlarmManager
import android.app.KeyguardManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.*
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.MaidAlarm.easyo_alarm.databinding.ActivityFrontAlarmBinding
import com.MaidAlarm.easyo_alarm.notification.notification
import java.io.DataInputStream
import java.util.*
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
    var maxVolume : Int = 0
    lateinit var audioManager: AudioManager

    // *** FrontAlarmActivity가 열려있을 때는 backButton으로 액티비티를 닫지 못하게 한다 -> 그냥 이 메서드 비워두면됨
    override fun onBackPressed() {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_front_alarm)
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
        val app = application as AppClass
        val calculateProblemFragment = CalculateProblemFragment()
        // SQL에 대한 변수 선언
        val SQLHelper = SQLHelper(this)
        val sql = "select * from MaidAlarm"
        val c1 = SQLHelper.writableDatabase.rawQuery(sql, null)
        val size = c1.count

        try {
            // 지정한 알람음 데이터를 가져온다
            when(app.bellIndex){
                0 -> app.mediaPlayer = MediaPlayer.create(this, R.raw.normal_jazzbar)
                1 -> app.mediaPlayer = MediaPlayer.create(this, R.raw.normal_guitar)
                2 -> app.mediaPlayer = MediaPlayer.create(this, R.raw.normal_happytown)
                3 -> app.mediaPlayer = MediaPlayer.create(this, R.raw.normal_country)
                10 -> app.mediaPlayer = MediaPlayer.create(this, R.raw.voice_k_juyoeng)
                11 -> app.mediaPlayer = MediaPlayer.create(this, R.raw.vocie_k_minjeong)
            }
        }catch (e:Exception){
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

            when(app.bellIndex){
                0 -> app.mediaPlayer = MediaPlayer.create(this, R.raw.normal_jazzbar)
                1 -> app.mediaPlayer = MediaPlayer.create(this, R.raw.normal_guitar)
                2 -> app.mediaPlayer = MediaPlayer.create(this, R.raw.normal_happytown)
                3 -> app.mediaPlayer = MediaPlayer.create(this, R.raw.normal_country)
                10 -> app.mediaPlayer = MediaPlayer.create(this, R.raw.voice_k_juyoeng)
                11 -> app.mediaPlayer = MediaPlayer.create(this, R.raw.vocie_k_minjeong)
            }
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
        app.lastProgress = progress

        // *** 음악 파일 실행 - 미구현
        // 알람 울리기 전 볼륨 강제 조절
        adjustVolume(progress)
        mediaPlayer = app.mediaPlayer
        mediaPlayer.setVolume(1f, 1f)
        mediaPlayer.isLooping = true
        mediaPlayer.start()

        if (progress == -1){
            Log.d("FrontAlarmActivity", "FrontAlarmActivity 의 Vibrate 쪽에 에러 발생")
        }
        // progress가 0일 때는 진동이 울리게 정한다
        else if(progress == 0){
            Log.d("FrontAlarmActivity", "진동울리는중")
            val arrayTime = longArrayOf(1000, 1000, 1000, 1000)
            val arrayAmplitudes = intArrayOf(0, 150, 0, 150)
            vib = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vib.vibrate(VibrationEffect.createWaveform(arrayTime, arrayAmplitudes, 1))
            } else {
                vib.vibrate(1000)
            }
        }
        Log.d("FrontAlarmActivity", "progress: $progress")


        // *** 계산 문제를 표시할지 말지 결정한다
        // AppClass 변수의 wayOfAlarm = 1 일 때만 계산 문제를 보여준다
        if (app.wayOfAlarm == 1){
            // 계산 fragment를 표시한다
            val calculator = supportFragmentManager.beginTransaction()
            calculator.replace(R.id.front_container, calculateProblemFragment)
            calculator.commit()
        }

        var counter = 0 // 계산 문제 출제 횟수 카운터

        // ok버튼 클릭 시
        binder.buttonOk.setOnClickListener {
            // 알람 울릴 때 계산 문제를 사용할 때
            if (app.wayOfAlarm == 1){
                // 설정에서 지정한 계산문제 풀이 횟수 보다 작을 때
                if (counter < app.counter) {
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
                            vib.vibrate(VibrationEffect.createWaveform(arrayTime, arrayAmplitudes, 0))
                        } else {
                            vib.vibrate(500)
                        }
                    }
                    // 계산 문제를 카운터 만큼 실시 했을 때 -> 진동, 음악 멈추고 액티비티 종료
                    if (counter >= app.counter){
                        if (progress == 0){
                            vib.cancel()
                        }
                        finish()
                        // 1분뒤 소리 울리는거 취소 - 트리거 취소
                        app.threadTrigger = 0
                    }
                }
            }
            // 계산문제를 설정하지 않았을 때
            else{
                if (progress == 0){
                    vib.cancel()
                }
                finish()
            }

            // 그 다음으로 울릴 알람이 없다면 notification 삭제하기
            if (size > 0) {
                val recentAlarm = RecentAlarm()
                val recentTimeList = recentAlarm.checkSQL(SQLHelper)
                // 알림은 있지만 모든 토글이 off 일 때 -> notification 삭제
                if (recentTimeList[0] == -1) {
                    val notification = notification()
                    notification.cancelNotification(this)
                }
            }
            // 남아있는 알람이 없다면 notification 삭제
            else if (size == 0){
                val notification = notification()
                notification.cancelNotification(this)
            }
            // 남아있는 알람이 있다면 notification 갱신
            else{
                // 설정에서 xnotification이 "사용 상태"로 되어 있을 때
                // alarmFragment에 있는 view에 대한 갱신은 alarmFragment의 onResume에서 실시하기 때문에
                // 여기서는 notificatrion에 대한 갱신만 해주면 된다
                if (app.notificationSwitch == 1){
                    // ok 버튼을 눌렀을 때 notification의 내용을 갱신해준다
                    val notification = notification()
                    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notification.getNotification(this, "chanel1", "첫 번째 채널", notificationManager)
                    notification.makeNotification(app, this, notificationManager)
                }
            }
        }

        // "10분 뒤" 버튼 클릭 시 동작 설정
        binder.button10Min.setOnClickListener {
            val alarmManager: AlarmManager? =
                    this.getSystemService(Context.ALARM_SERVICE) as AlarmManager?
            val receiver = Receiver()
            val filter = IntentFilter("POSTPHONETIME")
            registerReceiver(receiver, filter)

            val calendar = Calendar.getInstance()
            val intent = Intent("POSTPHONETIME")

            // 다음 알람에도 progress 데이터는 필요하기 때문에 넘겨준다
            // 즉, FrontAlarmActivity를 호출하기 위해서 필요한 필수 데이터는 progress뿐
            intent.putExtra("progress", progress)

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

            finish()
        }
        setContentView(binder.root)
    }

    override fun onDestroy() {
        super.onDestroy()
        // 액티비티 종료 시 음악 끄기
        try {
            // 음악 끄기
            mediaPlayer.release()
            // 볼륨 원래대로 되돌리기
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, AudioManager.FLAG_PLAY_SOUND)
            // 진동 끄기
            vib.cancel()
        }
        catch (e: Exception){

        }
    }

    // 볼륨 강제 조절
    fun adjustVolume(volume : Int){
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val factor = volume.toFloat() / 100
        val targetVolume = (maxVolume * factor).toInt()
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVolume, AudioManager.FLAG_PLAY_SOUND)
    }

}