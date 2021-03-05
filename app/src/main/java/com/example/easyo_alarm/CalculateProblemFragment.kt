package com.example.easyo_alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.easyo_alarm.databinding.FragmentCalculateProblemBinding
import java.lang.Exception
import java.util.*
import kotlin.random.Random

class CalculateProblemFragment : Fragment() {
    lateinit var binder : FragmentCalculateProblemBinding
    var initial = 0
    var threadRunning = false
    lateinit var app : AppClass

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_calculate_problem, null)
        binder = FragmentCalculateProblemBinding.bind(view)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val FrontAlarmActivity = activity as FrontAlarmActivity
        app = context!!.applicationContext as AppClass

        // 뷰에 문제 게시
        binder.problemText1.text = FrontAlarmActivity.problem1.toString()
        binder.problemText2.text = FrontAlarmActivity.problem2.toString()

        // 숫자패드 1 클릭했을 때
        binder.button1.setOnClickListener {
            setNumber(1)
        }

        // 숫자패드 2 클릭했을 때
        binder.button2.setOnClickListener {
            setNumber(2)
        }

        // 숫자패드 3 클릭했을 때
        binder.button3.setOnClickListener {
            setNumber(3)
        }

        // 숫자패드 4 클릭했을 때
        binder.button4.setOnClickListener {
            setNumber(4)
        }

        // 숫자패드 5 클릭했을 때
        binder.button5.setOnClickListener {
            setNumber(5)
        }

        // 숫자패드 6을 클릭했을 때
        binder.button6.setOnClickListener {
            setNumber(6)
        }

        // 숫자패드 7을 클릭했을 때
        binder.button7.setOnClickListener {
            setNumber(7)
        }

        // 숫자패드 8을 클릭했을 때
        binder.button8.setOnClickListener {
            setNumber(8)
        }

        // 숫자패드 9를 클릭했을 때
        binder.button9.setOnClickListener {
            setNumber(9)
        }

        // 숫자패드 0을 클릭했을 때
        binder.button0.setOnClickListener {
            setNumber(0)
        }

        // 숫자패드 AC를 클릭했을 때 = 전부 삭제
        binder.buttonAC.setOnClickListener {
            binder.answerText.text = ""
        }

        // 숫자패드 C를 클릭했을 때 = 제일 마지막 글자 삭제
        binder.buttonC.setOnClickListener {
            var text = binder.answerText.text
            if (text.length <= 1){
                binder.answerText.text = ""
            }else{
                text = text.removeRange(text.length -2, text.length-1)
                binder.answerText.text = text
            }
        }
    }

    // 클릭한 버튼의 숫자를 textView에 입력
    fun setNumber(int : Int){
        if (initial == 0){
            binder.answerText.text = ""
            initial =1
        }
        binder.answerText.append(int.toString())
        var answer = 0 // 숫자를 입력할 때 마다 정답이 해당 변수에 들어감
        // answer는 입력될 때 마다 FrontAlarmActivity로 보내진다
        val FrontAlarmActivity = activity as FrontAlarmActivity
        FrontAlarmActivity.user_answer = binder.answerText.text.toString().toInt()
    }

    // 뷰에 문제 게시
    fun setProblem(){
        val FrontAlarmActivity = activity as FrontAlarmActivity
        binder.problemText1.text = FrontAlarmActivity.problem1.toString()
        binder.problemText2.text = FrontAlarmActivity.problem2.toString()
    }

    // 계산 문제를 풀 때는 소리를 끄게 한다
    fun stopSound(){
        // 이미 꺼져있는데 발동하면 에러를 일으킬 수 있으니 try~catch로 처리한다
        try {
            // 소리를 끈다 - 미구현

            // Receiver를 반동시킨다
            val alarmManager: AlarmManager? =
                    context!!.getSystemService(Context.ALARM_SERVICE) as AlarmManager?

            val receiver = Receiver()
            val filter = IntentFilter("SoundStop")
            context!!.registerReceiver(receiver, filter)

            val calendar = Calendar.getInstance()
            val intent = Intent("SoundStop")

            // 2분뒤
            val interval = 2 * 60 * 1000
            // 한번 쓰고 버릴 알람이기 때문에 requestCode는 2로 설정한다
            val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    2,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
            )
            // 위에서 설정한 시간(Calendar.getInstance)에 알람이 울리게 한다
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                // API23 이상에서는 setExactAndAllowWhileIdle을 사용해야한다.
                alarmManager?.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis + interval, pendingIntent)
            }else{
                alarmManager?.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis + interval, pendingIntent)
            }
        }catch (e:Exception){

        }
    }
}