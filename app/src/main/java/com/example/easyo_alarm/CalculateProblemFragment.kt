package com.example.easyo_alarm

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.easyo_alarm.databinding.FragmentCalculateProblemBinding
import kotlin.random.Random

class CalculateProblemFragment : Fragment() {
    lateinit var binder : FragmentCalculateProblemBinding
    val app = AppClass()
    var initial = 0

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
        // *** 문제 생성
        app.problem1 = Random.nextInt(0, 100)
        app.problem2 = Random.nextInt(0, 10)
        app.answer = app.problem1 + app.problem2

        binder.problemText1.text = app.problem1.toString()
        binder.problemText2.text = app.problem2.toString()

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
            if (text.isNotEmpty()){
                text = text.removeRange(text.length -2, text.length-1)
                binder.answerText.text = text
            }
        }

    }

    fun setNumber(int : Int){
        if (initial == 0){
            binder.answerText.text = ""
            initial =1
        }
        binder.answerText.append(int.toString())
    }
}