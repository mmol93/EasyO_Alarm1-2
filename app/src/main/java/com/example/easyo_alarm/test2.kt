package com.example.easyo_alarm

import java.text.SimpleDateFormat
import java.util.*

class test2 {
    fun test2(){

        val valid_until = "1/1/1990"    // 기준 날짜임
        var catalog_outdated = 0

        val sdf = SimpleDateFormat("dd/MM/yyyy")
        val strDate: Date = sdf.parse(valid_until)
        // 기준날짜보다 지금 날짜가 과거인지 확인 즉, 기준날짜 > 현재 날짜면 true
        if (Date().after(strDate)) {
            catalog_outdated = 1
        }
    }
}