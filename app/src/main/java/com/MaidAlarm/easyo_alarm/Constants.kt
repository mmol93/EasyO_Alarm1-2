package com.MaidAlarm.easyo_alarm

import android.util.Log
import kotlin.random.Random

object API{
    const val baseURL : String = "https://api.openweathermap.org/"
    const val ID = "20d09610edf386f289f89685cea78a00"
    const val ID2 = "7a7bd9fa2f5ed07cffff880be62094ad"
    const val ID3 = "eb7736d8318f2247e8d0b95a275f176c"

    fun getID() : String{
        val randomInt = Random.nextInt(3)
        val IDArray = arrayOf(ID, ID2, ID3)
        Log.d("weatherAlarm", "사용된 API ID: ${IDArray[randomInt]}")

        return IDArray[randomInt]
    }
}