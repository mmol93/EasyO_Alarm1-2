package com.MaidAlarm.easyo_alarm

import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.MaidAlarm.easyo_alarm.databinding.FragmentCalculateProblemBinding
import java.lang.Exception
import java.util.*

class CalculateProblemFragment : Fragment() {
    lateinit var binder : FragmentCalculateProblemBinding
    var initial = 0
    var threadRunning = false
    lateinit var app : AppClass
    lateinit var FrontAlarmActivity : FrontAlarmActivity

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
        FrontAlarmActivity = activity as FrontAlarmActivity
        app = context!!.applicationContext as AppClass


        // 뷰에 문제 게시
        binder.problemText1.text = FrontAlarmActivity.problem1.toString()
        binder.problemText2.text = FrontAlarmActivity.problem2.toString()

        // 숫자패드 1 클릭했을 때
        binder.button1.setOnClickListener {
            setNumber(1)
            stopSound()
        }

        // 숫자패드 2 클릭했을 때
        binder.button2.setOnClickListener {
            setNumber(2)
            stopSound()
        }

        // 숫자패드 3 클릭했을 때
        binder.button3.setOnClickListener {
            setNumber(3)
            stopSound()
        }

        // 숫자패드 4 클릭했을 때
        binder.button4.setOnClickListener {
            setNumber(4)
            stopSound()
        }

        // 숫자패드 5 클릭했을 때
        binder.button5.setOnClickListener {
            setNumber(5)
            stopSound()
        }

        // 숫자패드 6을 클릭했을 때
        binder.button6.setOnClickListener {
            setNumber(6)
            stopSound()
        }

        // 숫자패드 7을 클릭했을 때
        binder.button7.setOnClickListener {
            setNumber(7)
            stopSound()
        }

        // 숫자패드 8을 클릭했을 때
        binder.button8.setOnClickListener {
            setNumber(8)
            stopSound()
        }

        // 숫자패드 9를 클릭했을 때
        binder.button9.setOnClickListener {
            setNumber(9)
            stopSound()
        }

        // 숫자패드 0을 클릭했을 때
        binder.button0.setOnClickListener {
            setNumber(0)
            stopSound()
        }

        // 숫자패드 AC를 클릭했을 때 = 전부 삭제
        binder.buttonAC.setOnClickListener {
            binder.answerText.text = ""
        }

        // 숫자패드 C를 클릭했을 때 = 제일 마지막 글자 삭제
        binder.buttonC.setOnClickListener {
            var text = binder.answerText.text
            Log.d("Calculate", "text.length: ${text.length}")
            if (text.length <= 1 || text.toString() == ""){
                binder.answerText.text = ""
            }else{
                text = text.removeRange(text.length -1, text.length)
                binder.answerText.text = text

                // answer는 입력될 때 마다 FrontAlarmActivity로 보내진다
                FrontAlarmActivity.user_answer = binder.answerText.text.toString().toInt()
            }
        }
    }

    // 클릭한 버튼의 숫자를 textView에 입력
    fun setNumber(int : Int){
        // 일단 처음 뭔가 입력하면 공란으로 만듬
        if (initial == 0){
            binder.answerText.text = ""
            initial =1
        }
        binder.answerText.append(int.toString())
        // answer는 입력될 때 마다 FrontAlarmActivity로 보내진다
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
        if (app.threadTrigger == 0){
            // 트리거를 on해서 중복해서 발동하지 않게 한다
            app.threadTrigger = 1
            Log.d("makeAlarm", "thread 발동함")
            // 이미 꺼져있는데 발동하면 에러를 일으킬 수 있으니 try~catch로 처리한다
            try {
                // 소리를 끈다 - 미구현
                if (app.lastProgress > 0){
                    app.mediaPlayer.pause()
                }
                // thread로 1분 대기
                val thread = object : Thread(){
                    override fun run() {
                        super.run()
                        // 80초간 음악 멈추기
                        SystemClock.sleep(80 * 1000)
                        // 도중에 "10분 뒤" 버튼이나 "ok"버튼을 눌렀을 경우 트리거는 0이되어 다시 소리가 나지 않게 한다
                        if (app.threadTrigger == 1){
                            // 음악파일 재생
                            Log.d("makeAlarm", "다시 음악 재생")
                            app.mediaPlayer.setVolume(1f, 1f)
                            app.mediaPlayer.isLooping = true
                            app.mediaPlayer.start()
                        }
                        Log.d("makeAlarm", "thread 끝남")
                        // 트리거가 끝났으니 원래대로 돌려준다
                        app.threadTrigger = 0
                    }
                }
                thread.start()
            }catch (e:Exception){

            }
        }
    }
}