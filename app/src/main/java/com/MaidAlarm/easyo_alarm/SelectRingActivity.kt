package com.MaidAlarm.easyo_alarm

import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.view.isGone
import com.MaidAlarm.easyo_alarm.databinding.ActivitySelectRingBinding
import java.lang.Exception

class SelectRingActivity : AppCompatActivity() {
    lateinit var binder : ActivitySelectRingBinding
    lateinit var app : AppClass
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_ring)
        binder = ActivitySelectRingBinding.inflate(layoutInflater)
        app = application as AppClass

        // 필요 없는 항목은 보이지 않게 하기 - 미구현 목록들
        binder.typeEnglish.isGone = true
        binder.typeJapanese.isGone = true
        binder.radioButtonE1.isGone = true
        binder.radioButtonJ1.isGone = true

        // app.bellIndex에 들어있는 값을 베이스로 라디오 버튼에 불 들어오게 하기
        when(app.bellIndex){
            // 일반 알람음
            0 -> binder.radioButtonN1.isChecked = true
            1 -> binder.radioButtonN2.isChecked = true
            2 -> binder.radioButtonN3.isChecked = true
            3 -> binder.radioButtonN4.isChecked = true
            // 한국어 알람음
            10 -> binder.radioButtonK1.isChecked = true
            11 -> binder.radioButtonK2.isChecked = true
            // 값이 null일 때(아마...)
            else -> binder.radioButtonN1.isChecked = true
        }


        // 라디오 버튼 클릭 리스너
        binder.RadioGroup.setOnCheckedChangeListener { group, checkedId ->
            when(checkedId){
                // 일반 알람음
                R.id.radioButton_N1 -> app.bellIndex = 0
                R.id.radioButton_N2 -> app.bellIndex = 1
                R.id.radioButton_N3 -> app.bellIndex = 2
                R.id.radioButton_N4 -> app.bellIndex = 3
                // 한국어 알람음
                R.id.radioButton_K1 -> app.bellIndex = 10
                R.id.radioButton_K2 -> app.bellIndex = 11
            }
        }

        // ok 버튼 클릭 시
        binder.buttonOk.setOnClickListener {
            // 오디오 겹치면 에러 발생하니 정지 처리
            try {
                app.mediaPlayer.stop()
            }catch (e:Exception){

            }
            finish()
        }

        // play 버튼 클릭 시
        binder.buttonPlay.setOnClickListener {
            try {
                app.mediaPlayer.stop()
            }catch (e:Exception){

            }
            selectMusic(app.bellIndex)
            app.mediaPlayer.setVolume(1f, 1f)
            app.mediaPlayer.isLooping = true
            app.mediaPlayer.start()
            Log.d("SelectRingActivity", "소리 울림")
        }
        setContentView(binder.root)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            app.mediaPlayer.release()
        }catch (e:Exception){

        }
    }

    fun selectMusic(index : Int){
        when(index){
            0 -> app.mediaPlayer = MediaPlayer.create(this, R.raw.normal_jazzbar)
            1 -> app.mediaPlayer = MediaPlayer.create(this, R.raw.normal_guitar)
            2 -> app.mediaPlayer = MediaPlayer.create(this, R.raw.normal_happytown)
            3 -> app.mediaPlayer = MediaPlayer.create(this, R.raw.normal_country)
            10 -> app.mediaPlayer = MediaPlayer.create(this, R.raw.voice_k_juyoeng)
            11 -> app.mediaPlayer = MediaPlayer.create(this, R.raw.vocie_k_minjeong)
        }
    }
}