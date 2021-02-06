package com.example.easyo_alarm

import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.easyo_alarm.databinding.ActivityFrontAlarmBinding
import com.example.easyo_alarm.databinding.FragmentCalculateProblemBinding
import java.util.*
import kotlin.random.Random
import kotlin.random.Random.Default.nextInt

class FrontAlarmActivity : AppCompatActivity() {
    lateinit var binder : ActivityFrontAlarmBinding
    // 계산 문제에서 사용할 변수
    var problem1 = Random.nextInt(1, 100)
    var problem2 = Random.nextInt(1, 10)
    var user_answer = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_front_alarm)
        binder = ActivityFrontAlarmBinding.inflate(layoutInflater)
        val app = AppClass()
        val calculateProblemFragment = CalculateProblemFragment()

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

        // *** 음악 파일 실행 - 미구현


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
                        // 틀릴 시 진동하게 하기 - 미구현
                    }
                    if (counter >= app.counter){
                        finish()
                        // 음악 재생을 멈춘다 - 미구현
                    }
                }
            }
            else{
                finish()
                // 음악 재생을 멈춘다 - 미구현
            }
        }
        setContentView(binder.root)
    }
}