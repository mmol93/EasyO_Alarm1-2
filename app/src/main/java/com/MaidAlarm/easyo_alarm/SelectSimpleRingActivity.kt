package com.MaidAlarm.easyo_alarm

import android.content.Context
import android.content.ContextWrapper
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import androidx.core.view.isGone
import com.MaidAlarm.easyo_alarm.databinding.ActivitySelectSimpleRingBinding
import java.io.DataOutputStream
import java.lang.Exception

class SelectSimpleRingActivity : AppCompatActivity() {
    lateinit var binder : ActivitySelectSimpleRingBinding
    lateinit var app : AppClass
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_simple_ring)
        binder = ActivitySelectSimpleRingBinding.inflate(layoutInflater)
        app = application as AppClass
        val pref = getSharedPreferences("simpleAlarmData", Context.MODE_PRIVATE)

        // 필요 없는 항목은 보이지 않게 하기 - 미구현 목록들
        binder.typeEnglish.isGone = true
        binder.typeJapanese.isGone = true
        binder.radioButtonE1.isGone = true
        binder.radioButtonJ1.isGone = true

        // preference 생성
        var bellIndex = pref.getInt("bellIndex", 0)
        var volume = pref.getInt("volume", 0)

        // 액티비티를 열었을 때 설정되어 있는 불륨 반영하기
        binder.volumeSeekBar.progress = volume
        if (volume == 0) {
            binder.imageVolume.setImageResource(R.drawable.volume_mute)
        }else {
            binder.imageVolume.setImageResource(R.drawable.volume_icon)
        }

        // app.bellIndex에 들어있는 값을 베이스로 라디오 버튼에 불 들어오게 하기
        when(bellIndex){
            // 일반 알람음
            0 -> binder.radioButtonN1.isChecked = true
            1 -> binder.radioButtonN2.isChecked = true
            2 -> binder.radioButtonN3.isChecked = true
            3 -> binder.radioButtonN4.isChecked = true
            // 한국어 알람음
            10 -> binder.radioButtonK1.isChecked = true
            11 -> binder.radioButtonK2.isChecked = true
            // 고주파 알람음
            40 -> binder.radioButtonSonic1.isChecked = true
            41 -> binder.radioButtonSonic2.isChecked = true
            42 -> binder.radioButtonSonic3.isChecked = true
            43 -> binder.radioButtonSonic4.isChecked = true
            // 값이 null일 때(아마...)
            else -> binder.radioButtonN1.isChecked = true
        }

        // * 불륨 이미지를 클릭했을 때
        binder.imageVolume.setOnClickListener {
            if (binder.volumeSeekBar.progress > 0){
                binder.volumeSeekBar.progress = 0
                binder.imageVolume.setImageResource(R.drawable.volume_mute)
            }else{
                binder.volumeSeekBar.progress = 100
                binder.imageVolume.setImageResource(R.drawable.volume_icon)
            }
        }

        // seekBar에 대한 리스너 정의
        val seekListener = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                when (seekBar?.id) {
                    R.id.volumeSeekBar -> {
                        if (progress == 0){
                            binder.imageVolume.setImageResource(R.drawable.volume_mute)
                        }else{
                            binder.imageVolume.setImageResource(R.drawable.volume_icon)
                        }
                    }
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        }

        // * seekBar의 Progress 값을 가져온다
        binder.volumeSeekBar.setOnSeekBarChangeListener(seekListener)

        // 라디오 버튼 클릭 리스너
        binder.RadioGroup.setOnCheckedChangeListener { group, checkedId ->
            val prefEdit = pref.edit()
            when(checkedId){
                // 일반 알람음
                R.id.radioButton_N1 -> prefEdit.putInt("bellIndex", 0)
                R.id.radioButton_N2 -> prefEdit.putInt("bellIndex", 1)
                R.id.radioButton_N3 -> prefEdit.putInt("bellIndex", 2)
                R.id.radioButton_N4 -> prefEdit.putInt("bellIndex", 3)
                // 한국어 알람음
                R.id.radioButton_K1 -> prefEdit.putInt("bellIndex", 10)
                R.id.radioButton_K2 -> prefEdit.putInt("bellIndex", 11)
                // 고주파 알람음
                R.id.radioButton_Sonic1 -> prefEdit.putInt("bellIndex", 40)
                R.id.radioButton_Sonic2 -> prefEdit.putInt("bellIndex", 41)
                R.id.radioButton_Sonic3 -> prefEdit.putInt("bellIndex", 42)
                R.id.radioButton_Sonic4 -> prefEdit.putInt("bellIndex", 43)
            }
            prefEdit.commit()
        }

        // ok 버튼 클릭 시
        binder.buttonOk.setOnClickListener {
            // 오디오 겹치면 에러 발생하니 정지 처리
            try {
                app.mediaPlayer.stop()
            }catch (e: Exception){

            }
            finish()
        }

        // play 버튼 클릭 시
        binder.buttonPlay.setOnClickListener {
            try {
                app.mediaPlayer.stop()
            }catch (e: Exception){

            }
            // 여기서 bellIndex를 한 번 더 가져와야 라디오 버튼 리스너에서 설정된 값이 적용됨
            bellIndex = pref.getInt("bellIndex", 0)
            selectMusic(bellIndex)
            app.mediaPlayer.setVolume(1f, 1f)
            app.mediaPlayer.isLooping = true
            app.mediaPlayer.start()
            Log.d("SelectRingActivity", "소리 울림")
        }
        setContentView(binder.root)
    }

    override fun onDestroy() {
        super.onDestroy()
        val pref = getSharedPreferences("simpleAlarmData", Context.MODE_PRIVATE)
        val prefEdit = pref.edit()
        prefEdit.putInt("volume", binder.volumeSeekBar.progress)
        prefEdit.commit()
        try {
            app.mediaPlayer.release()
        }catch (e: Exception){

        }
    }

    fun selectMusic(index : Int){
        when(index){
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
    }
}