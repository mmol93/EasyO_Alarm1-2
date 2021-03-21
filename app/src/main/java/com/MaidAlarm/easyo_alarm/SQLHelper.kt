package com.MaidAlarm.easyo_alarm

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class SQLHelper : SQLiteOpenHelper {
    constructor(context: Context) : super(context, "MaidAlarm.db",null, 1)
    override fun onCreate(db: SQLiteDatabase?) {
        // ** 테이블 구조 생성
        // 각 컬럼에 대한 설명
        /// idx: 키 인덱스를 위한 컬럼
        // hourData: 설정한 시간이 담김
        // minData: 설정한 분이 담김
        // progressData: 음량이 담김
        // Sun~Sat: 각 요일별로 off=0 / on=1이 담김
        // quick: Quick 알람인지 일반 알람인지 구분(Quick 알람은 한 번 울리고 리스트에서 삭제되게 하기 위해)(0: normal 알람, 1: quick 알람)
        // requestCode: 알람을 생성한 시간(Calendar.getInstance())의 timeInMillis가 담긴다(이미 지나간 시간은 유니크 숫자라서 사용)
        // -> 알람 매니저를 호출할 때 requestCode로 사용된다
        // switch: Recycler의 아이템중 토글 버튼 기록 = 따로 지정할 필요 x
        // notification : 나중에 추가할 기능에 대비한 컬럼 생성 (지금은 사용안함)
        val sql = """
            create table MaidAlarm
                (idx integer primary key,
                hourData integer not null,
                minData integer not null,
                progressData integer not null,
                Sun integer not null default 0,
                Mon integer not null default 0,
                Tue integer not null default 0,
                Wed integer not null default 0,
                Thu integer not null default 0,
                Fri integer not null default 0,
                Sat integer not null default 0,
                quick integer not null default 0,
                requestCode integer not null,
                switch integer not null default 1,
                notification integer not null default 0
                )
        """.trimIndent()

        // 데이터 베이스 구성 실행
        db?.execSQL(sql)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("Not yet implemented")
    }

}