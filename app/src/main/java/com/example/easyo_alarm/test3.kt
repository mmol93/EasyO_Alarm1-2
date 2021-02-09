package com.example.easyo_alarm

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import java.time.LocalDateTime
import java.time.temporal.ChronoField

@Suppress("UNREACHABLE_CODE")
class test3 {
    @RequiresApi(Build.VERSION_CODES.O)
    fun test1(){
        val now = LocalDateTime.now()
        val dateToCompare : LocalDateTime = TODO()
        val minutesOfDayNow = now.get(ChronoField.MINUTE_OF_DAY)
        val minutesOfDayToCompare = dateToCompare.get(ChronoField.MINUTE_OF_DAY)

        when {
            minutesOfDayNow == minutesOfDayToCompare -> Log.d("test3", "test3")
            minutesOfDayNow > minutesOfDayToCompare -> Log.d("test3", "test3")
            minutesOfDayNow < minutesOfDayToCompare -> Log.d("test3", "test3")
        }
    }

}